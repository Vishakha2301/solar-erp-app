package com.solarerp.sizing.engine;

import java.math.BigDecimal;

/**
 * Tunable constants for the rooftop on-grid sizing engine.
 *
 * These are engineering defaults -- NOT prices. All component prices come
 * from the Material catalog.
 */
public final class SizingParameters {

    private SizingParameters() {}

    /** Overall system derating (soiling, temperature, wiring, inverter). */
    public static final BigDecimal SYSTEM_LOSS_FACTOR = new BigDecimal("0.80");

    /**
     * Physical area occupied by one panel (m2). Roof area divided by this
     * gives how many panels fit; panel count multiplied by the chosen panel
     * Wp gives the kW the roof supports. A typical large-format module
     * footprint; tune to the modules actually installed.
     */
    public static final BigDecimal AREA_PER_PANEL_SQM = new BigDecimal("2.2");

    /** DC:AC ratio -- array kW divided by this gives inverter kW. */
    public static final BigDecimal DC_AC_RATIO = new BigDecimal("1.15");

    /** Default growth buffer applied to average consumption when none given. */
    public static final BigDecimal DEFAULT_GROWTH_BUFFER_PERCENT =
            new BigDecimal("10");

    /** Fallback Peak Sun Hours when a state is not in the reference table. */
    public static final BigDecimal DEFAULT_PEAK_SUN_HOURS =
            new BigDecimal("4.80");

    /** Days per month used to convert monthly consumption to daily. */
    public static final BigDecimal DAYS_PER_MONTH = new BigDecimal("30");

    /** Days per year used for annual generation. */
    public static final BigDecimal DAYS_PER_YEAR = new BigDecimal("365");

    /**
     * Reference size of a typical Indian household (2 AC, fridge, water
     * heater, TV). Used ONLY as a reasonableness flag -- if a sizing result
     * greatly exceeds this, the user should double-check the consumption
     * inputs. It is not used in any calculation.
     */
    public static final BigDecimal TYPICAL_HOUSEHOLD_KW = new BigDecimal("2.0");

    /**
     * Multiple of TYPICAL_HOUSEHOLD_KW above which the result is flagged as
     * exceeding a typical household.
     */
    public static final BigDecimal TYPICAL_HOUSEHOLD_FLAG_MULTIPLE =
            new BigDecimal("3");
}
