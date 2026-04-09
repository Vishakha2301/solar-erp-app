package com.solarerp.costing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CostingContextDto(
        double plantCapacity,
        String systemType,
        String phaseType,
        String roofType,
        String roofIdentifier,
        @JsonProperty("isSubsidyProject") boolean isSubsidyProject
) {}
