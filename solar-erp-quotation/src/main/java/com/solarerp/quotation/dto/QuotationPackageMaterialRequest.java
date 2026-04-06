package com.solarerp.quotation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record QuotationPackageMaterialRequest(
        @NotNull UUID materialId,
        @NotBlank String componentKey,
        boolean isRecommended
) {}