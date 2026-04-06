package com.solarerp.customer.dto;

import com.solarerp.customer.entity.CustomerType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        CustomerType customerType,
        String name,
        String companyName,
        String phone,
        String email,
        String address,
        String city,
        String state,
        String pincode,
        String gstNumber,
        boolean active,
        Instant createdAt,
        UUID createdBy,
        List<CustomerSiteResponse> sites
) {}
