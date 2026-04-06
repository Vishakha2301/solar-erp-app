package com.solarerp.quotation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuotationInstalmentRequest(
        @NotNull int instalmentNo,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal percentage
) {}