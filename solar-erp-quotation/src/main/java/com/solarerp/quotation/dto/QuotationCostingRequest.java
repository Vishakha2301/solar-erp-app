package com.solarerp.quotation.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record QuotationCostingRequest(
        @NotNull UUID costingId,
        String roofLabel,
        BigDecimal subsidyAmount
) {}