package com.solarerp.costing.controller;

import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.service.CostingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/costings")
public class CostingController {

    private final CostingService costingService;

    public CostingController(CostingService costingService) {
        this.costingService = costingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<SavedCostingResponse> getAll() {
        return costingService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public SavedCostingResponse getById(@PathVariable UUID id) {
        return costingService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public SavedCostingResponse create(
            @Valid @RequestBody SavedCostingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return costingService.create(request, userId(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public SavedCostingResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SavedCostingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return costingService.update(id, request, userId(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        costingService.delete(id, userId(jwt));
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
