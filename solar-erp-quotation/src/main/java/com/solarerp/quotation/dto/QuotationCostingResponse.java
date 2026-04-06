package com.solarerp.quotation.dto;

import java.util.UUID;

public record QuotationCostingResponse(
        UUID id,
        UUID costingId,
        String roofLabel
) {}