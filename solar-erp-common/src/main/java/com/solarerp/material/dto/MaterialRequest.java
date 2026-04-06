package com.solarerp.material.dto;

import com.solarerp.material.entity.MaterialCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MaterialRequest(
        @NotNull MaterialCategory category,
        String componentKey,
        @NotBlank String brandName,
        @NotBlank String modelName,
        String specification,
        String unit,
        String warranty,
        String hsnCode
) {}
