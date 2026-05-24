package com.solarerp.sizing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Reference data: Peak Sun Hours (kWh/kWp/day) per state.
 * Used by the sizing engine to convert required generation into kW capacity.
 * Editable so admins can tune values for their service area.
 */
@Entity
@Table(name = "state_irradiance")
@Getter
@Setter
@NoArgsConstructor
public class StateIrradiance {

    @Id
    @Column(length = 100)
    private String state;

    @Column(name = "peak_sun_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal peakSunHours;
}
