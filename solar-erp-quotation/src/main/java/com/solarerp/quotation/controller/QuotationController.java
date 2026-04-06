package com.solarerp.quotation.controller;

import com.solarerp.quotation.dto.QuotationRequest;
import com.solarerp.quotation.dto.QuotationResponse;
import com.solarerp.quotation.entity.QuotationStatus;
import com.solarerp.quotation.service.QuotationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    private final QuotationService quotationService;

    public QuotationController(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @GetMapping
    public List<QuotationResponse> getAll() {
        return quotationService.getAll();
    }

    @GetMapping("/{id}")
    public QuotationResponse getById(@PathVariable UUID id) {
        return quotationService.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<QuotationResponse> getByStatus(@PathVariable QuotationStatus status) {
        return quotationService.getByStatus(status);
    }

    @GetMapping("/customer/{customerId}")
    public List<QuotationResponse> getByCustomer(@PathVariable UUID customerId) {
        return quotationService.getByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuotationResponse create(
            @Valid @RequestBody QuotationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.create(request, UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/{id}")
    public QuotationResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody QuotationRequest request) {
        return quotationService.update(id, request);
    }

    @PostMapping("/{id}/submit")
    public QuotationResponse submit(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.submit(id, UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/{id}/approve")
    public QuotationResponse approve(
            @PathVariable UUID id,
            @RequestParam(required = false) String approvalNotes,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.approve(id, approvalNotes, UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/{id}/reject")
    public QuotationResponse reject(
            @PathVariable UUID id,
            @RequestParam String rejectionReason,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.reject(id, rejectionReason, UUID.fromString(jwt.getSubject()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        quotationService.delete(id);
    }
}