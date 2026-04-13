package com.solarerp.material.controller;

import com.solarerp.material.dto.MaterialCategoryResponse;
import com.solarerp.material.dto.MaterialRequest;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/materials")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<MaterialResponse> getAll() {
        return materialService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public MaterialResponse getById(@PathVariable UUID id) {
        return materialService.getById(id);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<MaterialResponse> getByCategory(
            @PathVariable MaterialCategory category) {
        return materialService.getByCategory(category);
    }

    @GetMapping("/component/{componentKey}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<MaterialResponse> getByComponentKey(
            @PathVariable String componentKey) {
        return materialService.getByComponentKey(componentKey);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<MaterialResponse> search(@RequestParam String brandName) {
        return materialService.search(brandName);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<MaterialCategoryResponse> getCategories() {
        return materialService.getCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MaterialResponse create(
            @Valid @RequestBody MaterialRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return materialService.create(request,
                UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MaterialResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody MaterialRequest request) {
        return materialService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable UUID id) {
        materialService.deactivate(id);
    }
}
