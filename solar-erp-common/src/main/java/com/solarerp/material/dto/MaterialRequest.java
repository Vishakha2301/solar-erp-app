package com.solarerp.material.dto;

import com.solarerp.material.entity.MaterialCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MaterialRequest(
        @NotNull MaterialCategory category,
        String componentKey,
        @NotBlank String brandName,
        @NotBlank String modelName,
        String specification,
        String unit,
        String warranty,
        String hsnCode,
        @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
        BigDecimal unitPrice,
        @DecimalMin(value = "0.0", message = "GST rate cannot be negative")
        BigDecimal gstRate
) {}
