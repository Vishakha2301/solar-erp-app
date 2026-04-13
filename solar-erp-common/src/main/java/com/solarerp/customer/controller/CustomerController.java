package com.solarerp.customer.controller;

import com.solarerp.customer.dto.CustomerRequest;
import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<CustomerResponse> getAll() {
        return customerService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public CustomerResponse getById(@PathVariable UUID id) {
        return customerService.getById(id);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','VIEWER')")
    public List<CustomerResponse> search(@RequestParam String name) {
        return customerService.search(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public CustomerResponse create(
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return customerService.create(request, UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES')")
    public CustomerResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void deactivate(@PathVariable UUID id) {
        customerService.deactivate(id);
    }
}
