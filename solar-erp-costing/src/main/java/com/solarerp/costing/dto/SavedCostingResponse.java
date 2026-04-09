package com.solarerp.costing.dto;

import java.time.Instant;
import java.util.UUID;

public record SavedCostingResponse(
        UUID id,
        Instant createdAt,
        UUID createdBy,
        CostingContextDto context,
        CostingSnapshotDto snapshot
) {}
