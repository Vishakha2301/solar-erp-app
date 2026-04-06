package com.solarerp.customer.service;

import com.solarerp.customer.dto.CustomerRequest;
import com.solarerp.customer.dto.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<CustomerResponse> getAll();

    CustomerResponse getById(UUID id);

    List<CustomerResponse> search(String name);

    CustomerResponse create(CustomerRequest request, UUID userId);

    CustomerResponse update(UUID id, CustomerRequest request);

    void deactivate(UUID id);
}
