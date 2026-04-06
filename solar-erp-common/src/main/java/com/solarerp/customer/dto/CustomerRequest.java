package com.solarerp.customer.dto;

import com.solarerp.customer.entity.CustomerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomerRequest(
        @NotNull CustomerType customerType,
        @NotBlank String name,
        String companyName,
        @NotBlank String phone,
        String email,
        String address,
        String city,
        String state,
        String pincode,
        String gstNumber,
        List<CustomerSiteRequest> sites
) {}
