package com.solarerp.sizing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A stored rooftop on-grid sizing estimate as returned to clients.
 *
 * This is a system design. indicativeMaterialCost is materials only -- not a
 * quote. There is deliberately no subsidy, net cost or payback here.
 */
public record SizingEstimateResponse(
        UUID id,
        UUID customerId,
        UUID customerSiteId,

        BigDecimal monthlyAverageKwh,
        BigDecimal growthBufferPercent,
        BigDecimal targetMonthlyConsumptionKwh,
        String state,
        BigDecimal availableRoofAreaSqm,
        BigDecimal panelWattageWp,
        String connectionType,
        String phaseType,

        BigDecimal recommendedCapacityKw,
        boolean roofConstrained,
        int panelCount,
        BigDecimal inverterCapacityKw,
        BigDecimal annualGenerationKwh,
        BigDecimal indicativeMaterialCost,

        List<BomLineItem> billOfMaterials,
        boolean fullyPriced,
        boolean exceedsTypicalHousehold,

        UUID costingId,

        Instant createdAt,
        UUID createdBy
) {}
