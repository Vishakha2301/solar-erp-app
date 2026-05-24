package com.solarerp.sizing.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The pure computed output of SolarSizingEngine for rooftop on-grid.
 *
 * This is a SYSTEM DESIGN, not a quote. indicativeMaterialCost is the sum of
 * catalog-priced BOM lines only -- it excludes labour, transport, net-metering
 * fees and margin, which are added per job in the costing step.
 */
public record SizingResult(

        BigDecimal monthlyAverageKwh,
        BigDecimal growthBufferPercentApplied,
        BigDecimal targetMonthlyConsumptionKwh,

        BigDecimal requiredCapacityKw,
        BigDecimal roofLimitedCapacityKw,
        BigDecimal recommendedCapacityKw,
        boolean roofConstrained,

        int panelCount,
        BigDecimal inverterCapacityKw,
        BigDecimal annualGenerationKwh,

        BigDecimal indicativeMaterialCost,

        List<BomLineItem> billOfMaterials,
        boolean fullyPriced,
        boolean exceedsTypicalHousehold
) {}
