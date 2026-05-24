package com.solarerp.sizing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Inputs for a rooftop on-grid sizing calculation.
 *
 * Consumption is flexible: the caller supplies EITHER a list of monthly
 * consumption readings (one or more, in kWh) OR a directly-known monthly
 * average. The service computes the monthly average from whatever is given
 * and rejects both-or-neither.
 *
 * A growth buffer (default 10%) is then applied on top of the average; the
 * system is sized to cover 100% of that buffered figure.
 *
 * Roof capacity depends on panel wattage: availableRoofAreaSqm plus
 * panelWattageWp determine how many panels physically fit and hence the kW
 * the roof can support.
 */
public record SizingRequest(
        @NotNull UUID customerId,
        UUID customerSiteId,

        List<@DecimalMin(value = "0.0", inclusive = false,
                message = "Each consumption reading must be positive")
                BigDecimal> monthlyConsumptionReadingsKwh,

        @DecimalMin(value = "0.0", inclusive = false,
                message = "Known monthly average must be positive")
        BigDecimal knownMonthlyAverageKwh,

        @DecimalMin(value = "0.0", message = "Growth buffer cannot be negative")
        BigDecimal growthBufferPercent,

        @NotBlank String state,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Usable roof area must be positive")
        BigDecimal availableRoofAreaSqm,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Panel wattage must be positive")
        BigDecimal panelWattageWp,

        @NotBlank String connectionType,   // RESIDENTIAL, COMMERCIAL, INDUSTRIAL
        @NotBlank String phaseType         // SINGLE, THREE
) {}
