package com.solarerp.material.service;

import com.solarerp.material.dto.MaterialCategoryResponse;
import com.solarerp.material.dto.MaterialRequest;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.MaterialCategory;

import java.util.List;
import java.util.UUID;

public interface MaterialService {

    List<MaterialResponse> getAll();

    MaterialResponse getById(UUID id);

    List<MaterialResponse> getByCategory(MaterialCategory category);

    List<MaterialResponse> getByComponentKey(String componentKey);

    List<MaterialResponse> search(String brandName);

    List<MaterialCategoryResponse> getCategories();

    MaterialResponse create(MaterialRequest request, UUID userId);

    MaterialResponse update(UUID id, MaterialRequest request);

    void deactivate(UUID id);
}

