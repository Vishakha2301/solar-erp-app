package com.solarerp.material.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        MaterialCategoryResponse category,
        String componentKey,
        String brandName,
        String modelName,
        String specification,
        String unit,
        String warranty,
        String hsnCode,
        BigDecimal unitPrice,
        BigDecimal gstRate,
        boolean active,
        Instant createdAt,
        UUID createdBy
) {}
