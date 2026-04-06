package com.solarerp.quotation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record QuotationInstalmentResponse(
        UUID id,
        int instalmentNo,
        String description,
        BigDecimal percentage
) {}