package com.solarerp.quotation.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record QuotationRequest(
        @NotNull UUID customerId,
        UUID customerSiteId,
        String systemType,
        int validityDays,
        BigDecimal discount,
        String scopeOfWork,
        String paymentTerms,
        String termsAndConditions,
        String notes,
        boolean financingAvailable,
        BigDecimal financingRate,
        List<QuotationCostingRequest> costings,
        List<QuotationInstalmentRequest> instalments,
        List<QuotationPackageRequest> packages
) {}