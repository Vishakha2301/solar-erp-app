package com.solarerp.quotation.dto;

import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.dto.CustomerSiteResponse;
import com.solarerp.quotation.entity.QuotationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuotationResponse(
        UUID id,
        String quotationNumber,
        CustomerResponse customer,
        CustomerSiteResponse customerSite,
        QuotationStatus status,
        String systemType,
        int validityDays,
        BigDecimal discount,
        String scopeOfWork,
        String paymentTerms,
        String termsAndConditions,
        String notes,
        boolean financingAvailable,
        BigDecimal financingRate,
        String rejectionReason,
        String approvalNotes,
        UUID createdBy,
        Instant submittedAt,
        UUID approvedRejectedBy,
        Instant approvedRejectedAt,
        Instant createdAt,
        Instant updatedAt,
        List<QuotationCostingResponse> costings,
        List<QuotationInstalmentResponse> instalments,
        List<QuotationPackageResponse> packages
) {}