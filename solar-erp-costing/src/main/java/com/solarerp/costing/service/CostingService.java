package com.solarerp.costing.service;

import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;

import java.util.List;
import java.util.UUID;

public interface CostingService {

    List<SavedCostingResponse> getAll();

    SavedCostingResponse getById(UUID id);

    SavedCostingResponse create(SavedCostingRequest request, UUID userId);

    SavedCostingResponse update(UUID id, SavedCostingRequest request, UUID userId);

    void delete(UUID id, UUID userId);
}