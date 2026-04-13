package com.solarerp.quotation.controller;

import com.solarerp.quotation.dto.QuotationRequest;
import com.solarerp.quotation.dto.QuotationResponse;
import com.solarerp.quotation.entity.QuotationStatus;
import com.solarerp.quotation.service.QuotationDocumentService;
import com.solarerp.quotation.service.QuotationService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {

    private final QuotationService quotationService;
    private final QuotationDocumentService quotationDocumentService;

    public QuotationController(
            QuotationService quotationService,
            QuotationDocumentService quotationDocumentService) {
        this.quotationService = quotationService;
        this.quotationDocumentService = quotationDocumentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<QuotationResponse> getAll() {
        return quotationService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public QuotationResponse getById(@PathVariable UUID id) {
        return quotationService.getById(id);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<QuotationResponse> getByStatus(@PathVariable QuotationStatus status) {
        return quotationService.getByStatus(status);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<QuotationResponse> getByCustomer(@PathVariable UUID customerId) {
        return quotationService.getByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public QuotationResponse create(
            @Valid @RequestBody QuotationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.create(request, userId(jwt));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public QuotationResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody QuotationRequest request) {
        return quotationService.update(id, request);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public QuotationResponse submit(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.submit(id, userId(jwt));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public QuotationResponse approve(
            @PathVariable UUID id,
            @RequestParam(required = false) String approvalNotes,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.approve(id, approvalNotes, userId(jwt));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public QuotationResponse reject(
            @PathVariable UUID id,
            @RequestParam String rejectionReason,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.reject(id, rejectionReason, userId(jwt));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public QuotationResponse cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return quotationService.cancel(id, userId(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void delete(@PathVariable UUID id) {
        quotationService.delete(id);
    }

    @GetMapping("/{id}/document")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID id) {
        byte[] document = quotationDocumentService.generateDocx(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("quotation-" + id + ".docx")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(document);
    }

    private UUID userId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            return new UUID(0L, 0L);
        }
        return UUID.fromString(jwt.getSubject());
    }
}
