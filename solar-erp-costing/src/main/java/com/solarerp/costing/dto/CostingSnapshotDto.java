package com.solarerp.costing.dto;

public record CostingSnapshotDto(
        double systemSubTotal,
        double subsidyProcessingFee,
        double contingency,
        double cp1,
        double cp2,
        double amc,
        double grandTotal,
        double projectCostAfterGst,
        double perWpAfterGst
) {}
