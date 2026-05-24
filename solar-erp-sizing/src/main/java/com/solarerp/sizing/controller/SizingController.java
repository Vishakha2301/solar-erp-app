package com.solarerp.sizing.controller;

import com.solarerp.sizing.dto.SizingEstimateResponse;
import com.solarerp.sizing.dto.SizingRequest;
import com.solarerp.sizing.service.SizingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sizing")
public class SizingController {

    private final SizingService sizingService;

    public SizingController(SizingService sizingService) {
        this.sizingService = sizingService;
    }

    @PostMapping("/estimates")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public SizingEstimateResponse create(
            @Valid @RequestBody SizingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return sizingService.createEstimate(request, userId(jwt));
    }

    @GetMapping("/estimates")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<SizingEstimateResponse> getAll() {
        return sizingService.getAll();
    }

    @GetMapping("/estimates/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public SizingEstimateResponse getById(@PathVariable UUID id) {
        return sizingService.getById(id);
    }

    @GetMapping("/estimates/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<SizingEstimateResponse> getByCustomer(
            @PathVariable UUID customerId) {
        return sizingService.getByCustomer(customerId);
    }

    /**
     * Creates a SavedCosting from this estimate and links it. Returns the new
     * costing id so the client can open it in the costing form.
     */
    @PostMapping("/estimates/{id}/costing")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public Map<String, UUID> convertToCosting(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID costingId = sizingService.convertToCosting(id, userId(jwt));
        return Map.of("costingId", costingId);
    }

    @DeleteMapping("/estimates/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable UUID id,
                       @AuthenticationPrincipal Jwt jwt) {
        sizingService.delete(id, userId(jwt));
    }

    private UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
