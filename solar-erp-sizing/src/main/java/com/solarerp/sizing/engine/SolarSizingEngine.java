package com.solarerp.sizing.engine;

import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.sizing.dto.BomLineItem;
import com.solarerp.sizing.dto.SizingRequest;
import com.solarerp.sizing.dto.SizingResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.solarerp.sizing.engine.SizingParameters.*;

/**
 * Pure, deterministic rooftop on-grid sizing.
 *
 * Scope (DECIDED with the EPC): this produces a SYSTEM DESIGN only -- target
 * consumption, required kW, roof-limited kW, panel count, inverter size,
 * generation, and a priced bill of materials. It does NOT produce a quote:
 * no labour, transport, net-metering fees, subsidy, or payback. Those are
 * added per job by staff in the costing step.
 *
 * No database, no LLM -- same inputs always give the same result.
 *
 * Sizing rules:
 *  - Consumption: average the supplied readings (or take the known average),
 *    then add a growth buffer (default 10%). Size to 100% of that.
 *  - Roof: area / area-per-panel = panels that fit; panels x panel Wp = the
 *    kW the roof supports.
 *  - Recommended kW = min(required, roof-limited).
 */
@Component
public class SolarSizingEngine {

    private static final int MONEY_SCALE = 2;
    private static final int KW_SCALE = 2;
    private static final BigDecimal WATTS_PER_KW = new BigDecimal("1000");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public SizingResult calculate(SizingRequest request,
                                  BigDecimal peakSunHours,
                                  MaterialCatalog catalog) {

        BigDecimal psh = peakSunHours != null
                ? peakSunHours : DEFAULT_PEAK_SUN_HOURS;

        // 1. Monthly average consumption.
        BigDecimal monthlyAverage = monthlyAverage(request);

        // 2. Apply growth buffer (default 10% when not supplied).
        BigDecimal bufferPercent = request.growthBufferPercent() != null
                ? request.growthBufferPercent()
                : DEFAULT_GROWTH_BUFFER_PERCENT;

        BigDecimal target = monthlyAverage
                .add(monthlyAverage.multiply(bufferPercent)
                        .divide(HUNDRED, 6, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Capacity required to cover 100% of target consumption.
        BigDecimal dailyTarget = target
                .divide(DAYS_PER_MONTH, 6, RoundingMode.HALF_UP);
        BigDecimal requiredKw = dailyTarget
                .divide(psh.multiply(SYSTEM_LOSS_FACTOR),
                        KW_SCALE, RoundingMode.HALF_UP);

        // 4. Roof-limited capacity -- depends on the chosen panel Wp.
        int panelsThatFit = request.availableRoofAreaSqm()
                .divide(AREA_PER_PANEL_SQM, 0, RoundingMode.DOWN)
                .intValue();
        BigDecimal roofLimitedKw = new BigDecimal(panelsThatFit)
                .multiply(request.panelWattageWp())
                .divide(WATTS_PER_KW, KW_SCALE, RoundingMode.HALF_UP);

        // 5. Recommended = min(required, roof-limited).
        BigDecimal recommendedKw = requiredKw.min(roofLimitedKw);
        boolean roofConstrained = roofLimitedKw.compareTo(requiredKw) < 0;

        if (recommendedKw.compareTo(BigDecimal.ONE) < 0) {
            recommendedKw = new BigDecimal("1.00");
        }

        // 6. Panel count for the recommended capacity (round up to meet it),
        //    never exceeding what physically fits.
        int panelCount = recommendedKw
                .multiply(WATTS_PER_KW)
                .divide(request.panelWattageWp(), 0, RoundingMode.UP)
                .intValue();
        if (panelsThatFit > 0 && panelCount > panelsThatFit) {
            panelCount = panelsThatFit;
        }

        // 7. Inverter sizing from DC:AC ratio.
        BigDecimal inverterKw = recommendedKw
                .divide(DC_AC_RATIO, KW_SCALE, RoundingMode.HALF_UP);

        // 8. Annual generation.
        BigDecimal annualKwh = recommendedKw
                .multiply(psh)
                .multiply(SYSTEM_LOSS_FACTOR)
                .multiply(DAYS_PER_YEAR)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        // 9. Bill of materials, priced from the catalog.
        List<BomLineItem> bom = buildBom(recommendedKw, panelCount,
                request.panelWattageWp(), catalog);

        // 10. Indicative material cost = sum of priced BOM line base prices, ex-GST.
        //     GST is added as a single blended 8.9% at the end in the costing step,
        //     consistent with how calculateQuotation works in the Flutter costing form.
        BigDecimal indicativeMaterialCost = bom.stream()
                .map(BomLineItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        boolean fullyPriced = !bom.isEmpty()
                && bom.stream().allMatch(BomLineItem::priced);

        // 11. Reasonableness flag against a typical household.
        boolean exceedsTypical = recommendedKw.compareTo(
                TYPICAL_HOUSEHOLD_KW.multiply(TYPICAL_HOUSEHOLD_FLAG_MULTIPLE))
                > 0;

        return new SizingResult(
                monthlyAverage.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                bufferPercent,
                target,
                requiredKw,
                roofLimitedKw,
                recommendedKw.setScale(KW_SCALE, RoundingMode.HALF_UP),
                roofConstrained,
                panelCount,
                inverterKw,
                annualKwh,
                indicativeMaterialCost,
                bom,
                fullyPriced,
                exceedsTypical
        );
    }

    // ---- Consumption ----------------------------------------------------

    /**
     * Computes the monthly average from whichever consumption input was given.
     * The service guarantees exactly one of the two is present.
     */
    BigDecimal monthlyAverage(SizingRequest request) {
        if (request.knownMonthlyAverageKwh() != null) {
            return request.knownMonthlyAverageKwh();
        }
        List<BigDecimal> readings = request.monthlyConsumptionReadingsKwh();
        BigDecimal sum = readings.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(readings.size()),
                4, RoundingMode.HALF_UP);
    }

    // ---- Bill of materials ---------------------------------------------

    private List<BomLineItem> buildBom(BigDecimal kw,
                                       int panelCount,
                                       BigDecimal panelWattageWp,
                                       MaterialCatalog catalog) {
        List<BomLineItem> bom = new ArrayList<>();

        addLine(bom, catalog.preferredFor(MaterialCategory.PANEL),
                MaterialCategory.PANEL, new BigDecimal(panelCount));

        addLine(bom, catalog.preferredFor(MaterialCategory.INVERTER),
                MaterialCategory.INVERTER, BigDecimal.ONE);

        addLine(bom, catalog.preferredFor(MaterialCategory.CABLE, "dcCable"),
                MaterialCategory.CABLE,
                kw.multiply(new BigDecimal("30"))
                        .setScale(0, RoundingMode.UP));

        addLine(bom, catalog.preferredFor(MaterialCategory.STRUCTURE),
                MaterialCategory.STRUCTURE, new BigDecimal(panelCount));

        addLine(bom, catalog.preferredFor(MaterialCategory.ELECTRICAL),
                MaterialCategory.ELECTRICAL, BigDecimal.ONE);

        return bom;
    }

    private void addLine(List<BomLineItem> bom,
                         Optional<Material> material,
                         MaterialCategory category,
                         BigDecimal quantity) {
        if (material.isEmpty()) {
            bom.add(new BomLineItem(
                    null, category.name(), null, null, null, null,
                    quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, false));
            return;
        }

        Material m = material.get();
        BigDecimal unitPrice = m.getUnitPrice();
        boolean priced = unitPrice != null && unitPrice.signum() > 0;

        BigDecimal effectiveUnit = priced ? unitPrice : BigDecimal.ZERO;
        BigDecimal gst = m.getGstRate() != null
                ? m.getGstRate() : BigDecimal.ZERO;
        BigDecimal total = effectiveUnit.multiply(quantity)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        bom.add(new BomLineItem(
                m.getId(),
                category.name(),
                m.getBrandName(),
                m.getModelName(),
                m.getSpecification(),
                m.getUnit(),
                quantity,
                effectiveUnit,
                gst,
                total,
                priced
        ));
    }

    private BigDecimal gstAmount(BigDecimal base, BigDecimal gstRate) {
        if (base == null || gstRate == null || gstRate.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return base.multiply(gstRate)
                .divide(HUNDRED, MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
