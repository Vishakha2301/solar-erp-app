package com.solarerp.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerSiteRequest(
        @NotBlank String siteLabel,
        String address,
        String city,
        String state,
        String pincode,
        boolean isDefault
) {}
