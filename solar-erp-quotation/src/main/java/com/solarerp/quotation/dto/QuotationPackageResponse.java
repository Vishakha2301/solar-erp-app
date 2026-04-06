package com.solarerp.quotation.dto;

import java.util.List;
import java.util.UUID;

public record QuotationPackageResponse(
        UUID id,
        String packageName,
        boolean isRecommended,
        List<QuotationPackageMaterialResponse> materials
) {}