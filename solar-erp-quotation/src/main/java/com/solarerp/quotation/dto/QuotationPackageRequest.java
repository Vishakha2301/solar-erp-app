package com.solarerp.quotation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuotationPackageRequest(
        @NotBlank String packageName,
        boolean isRecommended,
        @NotNull List<QuotationPackageMaterialRequest> materials
) {}