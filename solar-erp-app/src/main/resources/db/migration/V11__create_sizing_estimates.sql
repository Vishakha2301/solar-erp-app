-- Rooftop on-grid solar sizing.
--
-- A sizing estimate is a SYSTEM DESIGN for a customer: target consumption,
-- recommended kW, panel count, inverter size, generation, and a bill of
-- materials. It deliberately holds NO subsidy, net cost or payback -- those
-- depend on the real per-job cost, which is built in the costing step.
-- indicative_material_cost is materials only and is not a quote.
--
-- The bill of materials is JSONB (bom): a frozen result whose shape may
-- evolve without a schema migration, mirroring saved_costings.snapshot.
-- If a SavedCosting is created from the estimate, costing_id links them.
--
-- state_irradiance is a small editable reference table for Peak Sun Hours
-- (kWh/kWp/day) per state, used by the sizing engine. Seeded with typical
-- India values; admins can tune them.

CREATE TABLE state_irradiance (
    state           VARCHAR(100) PRIMARY KEY,
    peak_sun_hours  NUMERIC(4, 2) NOT NULL
);

INSERT INTO state_irradiance (state, peak_sun_hours) VALUES
    ('Tamil Nadu',      4.80),
    ('Maharashtra',     5.00),
    ('Karnataka',       5.10),
    ('Rajasthan',       5.50),
    ('Gujarat',         5.30),
    ('Kerala',          4.60),
    ('Delhi',           4.70),
    ('Telangana',       5.00),
    ('Andhra Pradesh',  5.10),
    ('Uttar Pradesh',   4.80),
    ('Madhya Pradesh',  5.20),
    ('Punjab',          4.70),
    ('Haryana',         4.80),
    ('West Bengal',     4.50);

CREATE TABLE sizing_estimates (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id                     UUID NOT NULL REFERENCES customers(id),
    customer_site_id                UUID REFERENCES customer_sites(id),

    -- Inputs
    monthly_average_kwh             NUMERIC(12, 2) NOT NULL,
    growth_buffer_percent           NUMERIC(5, 2) NOT NULL,
    state                           VARCHAR(100) NOT NULL,
    available_roof_area_sqm         NUMERIC(10, 2) NOT NULL,
    panel_wattage_wp                NUMERIC(8, 2) NOT NULL,
    connection_type                 VARCHAR(20) NOT NULL,
    phase_type                      VARCHAR(20) NOT NULL,

    -- Outputs (system design)
    target_monthly_consumption_kwh  NUMERIC(12, 2) NOT NULL,
    recommended_capacity_kw         NUMERIC(8, 2) NOT NULL,
    roof_constrained                BOOLEAN NOT NULL DEFAULT false,
    panel_count                     INT NOT NULL,
    inverter_capacity_kw            NUMERIC(8, 2) NOT NULL,
    annual_generation_kwh           NUMERIC(12, 2) NOT NULL,
    indicative_material_cost        NUMERIC(14, 2) NOT NULL,

    bom                             JSONB NOT NULL,

    costing_id                      UUID REFERENCES saved_costings(id),

    created_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by                      UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_sizing_estimates_customer_id
    ON sizing_estimates (customer_id);
CREATE INDEX idx_sizing_estimates_created_by
    ON sizing_estimates (created_by, created_at DESC);
