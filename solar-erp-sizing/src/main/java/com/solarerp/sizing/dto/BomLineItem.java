package com.solarerp.sizing.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One line in a sizing bill of materials.
 *
 * materialId links back to the catalog Material row the price and brand came
 * from. When a price is missing in the catalog, unitPrice and totalPrice are
 * zero and priced is false so the UI can flag the gap for manual entry in the
 * costing form.
 */
public record BomLineItem(
        UUID materialId,
        String category,
        String brandName,
        String modelName,
        String specification,
        String unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal gstRate,
        BigDecimal totalPrice,
        boolean priced
) {}
