package com.solarerp.customer.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerSiteResponse(
        UUID id,
        String siteLabel,
        String address,
        String city,
        String state,
        String pincode,
        boolean isDefault,
        Instant createdAt
) {}
