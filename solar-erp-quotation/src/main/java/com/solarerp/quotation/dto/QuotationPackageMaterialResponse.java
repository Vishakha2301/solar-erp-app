package com.solarerp.quotation.dto;

import com.solarerp.material.dto.MaterialResponse;
import java.util.UUID;

public record QuotationPackageMaterialResponse(
        UUID id,
        MaterialResponse material,
        String componentKey,
        boolean isRecommended
) {}