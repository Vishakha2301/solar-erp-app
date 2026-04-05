package com.solarerp.costing.dto;

import jakarta.validation.constraints.NotNull;

public record SavedCostingRequest(
        @NotNull Object context,
        @NotNull Object snapshot
) {}