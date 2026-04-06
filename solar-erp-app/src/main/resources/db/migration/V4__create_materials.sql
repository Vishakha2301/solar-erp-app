CREATE TYPE material_category AS ENUM (
    'PANEL',
    'INVERTER',
    'CABLE',
    'STRUCTURE',
    'ELECTRICAL',
    'OTHER'
);

CREATE TABLE materials (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category        material_category NOT NULL,
    component_key   VARCHAR(50),
    brand_name      VARCHAR(255) NOT NULL,
    model_name      VARCHAR(255) NOT NULL,
    specification   TEXT,
    unit            VARCHAR(20),
    warranty        VARCHAR(255),
    hsn_code        VARCHAR(20),
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID NOT NULL REFERENCES users(id)
);

CREATE INDEX idx_materials_category ON materials(category);
CREATE INDEX idx_materials_component_key ON materials(component_key);
