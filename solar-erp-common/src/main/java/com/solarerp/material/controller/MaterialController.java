package com.solarerp.material.controller;

import com.solarerp.material.dto.MaterialCategoryResponse;
import com.solarerp.material.dto.MaterialRequest;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/materials")
public class MaterialController {

    private static final Map<MaterialCategory, String> CATEGORY_LABELS = Map.of(
            MaterialCategory.PANEL, "Solar Panel",
            MaterialCategory.INVERTER, "Inverter",
            MaterialCategory.CABLE, "Cable",
            MaterialCategory.STRUCTURE, "Mounting Structure",
            MaterialCategory.ELECTRICAL, "Electrical Components",
            MaterialCategory.OTHER, "Other"
    );

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public List<MaterialResponse> getAll() {
        return materialService.getAll();
    }

    @GetMapping("/{id}")
    public MaterialResponse getById(@PathVariable UUID id) {
        return materialService.getById(id);
    }

    @GetMapping("/category/{category}")
    public List<MaterialResponse> getByCategory(@PathVariable MaterialCategory category) {
        return materialService.getByCategory(category);
    }

    @GetMapping("/component/{componentKey}")
    public List<MaterialResponse> getByComponentKey(@PathVariable String componentKey) {
        return materialService.getByComponentKey(componentKey);
    }

    @GetMapping("/search")
    public List<MaterialResponse> search(@RequestParam String brandName) {
        return materialService.search(brandName);
    }

    @GetMapping("/categories")
    public List<MaterialCategoryResponse> getCategories() {
        return Arrays.stream(MaterialCategory.values())
                .map(c -> new MaterialCategoryResponse(
                        c.name(),
                        CATEGORY_LABELS.getOrDefault(c, c.name())
                ))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialResponse create(
            @Valid @RequestBody MaterialRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return materialService.create(request, UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/{id}")
    public MaterialResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialRequest request) {
        return materialService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        materialService.deactivate(id);
    }
}
