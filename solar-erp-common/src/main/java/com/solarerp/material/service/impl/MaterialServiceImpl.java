package com.solarerp.material.service.impl;

import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.dto.MaterialCategoryResponse;
import com.solarerp.material.dto.MaterialRequest;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.material.service.MaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {

    private static final Map<MaterialCategory, String> CATEGORY_LABELS = Map.of(
            MaterialCategory.PANEL, "Solar Panel",
            MaterialCategory.INVERTER, "Inverter",
            MaterialCategory.CABLE, "Cable",
            MaterialCategory.STRUCTURE, "Mounting Structure",
            MaterialCategory.ELECTRICAL, "Electrical Components",
            MaterialCategory.OTHER, "Other"
    );

    private final MaterialRepository repository;

    public MaterialServiceImpl(MaterialRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MaterialResponse> getAll() {
        return repository.findAllByActiveTrueOrderByBrandNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MaterialResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<MaterialResponse> getByCategory(MaterialCategory category) {
        return repository
                .findByCategoryAndActiveTrueOrderByBrandNameAsc(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MaterialResponse> getByComponentKey(String componentKey) {
        return repository
                .findByComponentKeyAndActiveTrueOrderByBrandNameAsc(componentKey)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MaterialResponse> search(String brandName) {
        return repository
                .findByBrandNameContainingIgnoreCaseAndActiveTrue(brandName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MaterialCategoryResponse> getCategories() {
        return Arrays.stream(MaterialCategory.values())
                .map(c -> new MaterialCategoryResponse(
                        c.name(),
                        CATEGORY_LABELS.getOrDefault(c, c.name())))
                .toList();
    }

    @Override
    public MaterialResponse create(MaterialRequest request, UUID userId) {
        Material material = new Material();
        mapRequestToEntity(request, material);
        material.setCreatedBy(userId);
        return toResponse(repository.save(material));
    }

    @Override
    public MaterialResponse update(UUID id, MaterialRequest request) {
        Material material = findOrThrow(id);
        mapRequestToEntity(request, material);
        return toResponse(repository.save(material));
    }

    @Override
    public void deactivate(UUID id) {
        Material material = findOrThrow(id);
        material.setActive(false);
        repository.save(material);
    }

    private Material findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Material", id));
    }

    private void mapRequestToEntity(MaterialRequest request,
                                     Material material) {
        material.setCategory(request.category());
        material.setComponentKey(request.componentKey());
        material.setBrandName(request.brandName());
        material.setModelName(request.modelName());
        material.setSpecification(request.specification());
        material.setUnit(request.unit());
        material.setWarranty(request.warranty());
        material.setHsnCode(request.hsnCode());
        material.setUnitPrice(request.unitPrice());
        material.setGstRate(request.gstRate());
    }

    private MaterialResponse toResponse(Material material) {
        return new MaterialResponse(
                material.getId(),
                new MaterialCategoryResponse(
                        material.getCategory().name(),
                        CATEGORY_LABELS.getOrDefault(
                                material.getCategory(), "Other")),
                material.getComponentKey(),
                material.getBrandName(),
                material.getModelName(),
                material.getSpecification(),
                material.getUnit(),
                material.getWarranty(),
                material.getHsnCode(),
                material.getUnitPrice(),
                material.getGstRate(),
                material.isActive(),
                material.getCreatedAt(),
                material.getCreatedBy()
        );
    }
}

