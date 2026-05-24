package com.solarerp.sizing.service;

import com.solarerp.sizing.dto.SizingEstimateResponse;
import com.solarerp.sizing.dto.SizingRequest;

import java.util.List;
import java.util.UUID;

public interface SizingService {

    /**
     * Runs the sizing calculation, persists the estimate, and returns it.
     */
    SizingEstimateResponse createEstimate(SizingRequest request, UUID userId);

    SizingEstimateResponse getById(UUID id);

    List<SizingEstimateResponse> getAll();

    List<SizingEstimateResponse> getByCustomer(UUID customerId);

    void delete(UUID id, UUID userId);

    /**
     * Creates a SavedCosting from a stored estimate and links it back, so the
     * estimate can flow into the existing quotation pipeline. Returns the id
     * of the created costing.
     */
    UUID convertToCosting(UUID estimateId, UUID userId);
}
