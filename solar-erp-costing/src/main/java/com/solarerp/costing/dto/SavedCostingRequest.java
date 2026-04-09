package com.solarerp.costing.dto;

import jakarta.validation.constraints.NotNull;

public record SavedCostingRequest(
        @NotNull CostingContextDto context,
        @NotNull CostingSnapshotDto snapshot
) {}
