package com.solarerp.quotation.service;

import com.solarerp.quotation.dto.QuotationRequest;
import com.solarerp.quotation.dto.QuotationResponse;
import com.solarerp.quotation.entity.QuotationStatus;

import java.util.List;
import java.util.UUID;

public interface QuotationService {

    List<QuotationResponse> getAll();

    QuotationResponse getById(UUID id);

    List<QuotationResponse> getByStatus(QuotationStatus status);

    List<QuotationResponse> getByCustomer(UUID customerId);

    QuotationResponse create(QuotationRequest request, UUID userId);

    QuotationResponse update(UUID id, QuotationRequest request);

    QuotationResponse submit(UUID id, UUID userId);

    QuotationResponse approve(UUID id, String approvalNotes, UUID userId);

    QuotationResponse reject(UUID id, String rejectionReason, UUID userId);

    void delete(UUID id);
}