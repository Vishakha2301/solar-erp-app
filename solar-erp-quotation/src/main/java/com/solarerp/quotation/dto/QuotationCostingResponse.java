package com.solarerp.quotation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record QuotationCostingResponse(
        UUID id,
        UUID costingId,
        String roofLabel,
        BigDecimal subsidyAmount
) {}