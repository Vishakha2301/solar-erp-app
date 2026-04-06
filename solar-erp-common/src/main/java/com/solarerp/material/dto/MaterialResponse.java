package com.solarerp.material.dto;

import com.solarerp.material.entity.MaterialCategory;
import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        MaterialCategory category,
        String categoryLabel,
        String componentKey,
        String brandName,
        String modelName,
        String specification,
        String unit,
        String warranty,
        String hsnCode,
        boolean active,
        Instant createdAt,
        UUID createdBy
) {}
