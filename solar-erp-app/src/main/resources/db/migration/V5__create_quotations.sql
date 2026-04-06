CREATE TYPE quotation_status AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'APPROVED',
    'REJECTED',
    'REVISED'
);

CREATE TABLE quotations (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quotation_number        VARCHAR(20) NOT NULL UNIQUE,
    customer_id             UUID NOT NULL REFERENCES customers(id),
    customer_site_id        UUID REFERENCES customer_sites(id),
    status                  quotation_status NOT NULL DEFAULT 'DRAFT',
    system_type             VARCHAR(100),
    validity_days           INT NOT NULL DEFAULT 30,
    discount                DECIMAL(12, 2) DEFAULT 0,
    scope_of_work           TEXT,
    payment_terms           TEXT,
    terms_and_conditions    TEXT,
    notes                   TEXT,
    financing_available     BOOLEAN NOT NULL DEFAULT false,
    financing_rate          DECIMAL(5, 2),
    rejection_reason        TEXT,
    approval_notes          TEXT,
    created_by              UUID NOT NULL REFERENCES users(id),
    submitted_at            TIMESTAMPTZ,
    approved_rejected_by    UUID REFERENCES users(id),
    approved_rejected_at    TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE quotation_costings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quotation_id    UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    costing_id      UUID NOT NULL REFERENCES saved_costings(id),
    roof_label      VARCHAR(255)
);

CREATE TABLE quotation_instalments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quotation_id    UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    instalment_no   INT NOT NULL,
    description     VARCHAR(255) NOT NULL,
    percentage      DECIMAL(5, 2) NOT NULL
);

CREATE TABLE quotation_packages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quotation_id    UUID NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    package_name    VARCHAR(100) NOT NULL,
    is_recommended  BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE quotation_package_materials (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_id      UUID NOT NULL REFERENCES quotation_packages(id) ON DELETE CASCADE,
    material_id     UUID NOT NULL REFERENCES materials(id),
    component_key   VARCHAR(50) NOT NULL,
    is_recommended  BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_quotations_customer_id ON quotations(customer_id);
CREATE INDEX idx_quotations_status ON quotations(status);
CREATE INDEX idx_quotations_created_by ON quotations(created_by);
CREATE INDEX idx_quotation_costings_quotation_id ON quotation_costings(quotation_id);
CREATE INDEX idx_quotation_instalments_quotation_id ON quotation_instalments(quotation_id);
CREATE INDEX idx_quotation_packages_quotation_id ON quotation_packages(quotation_id);
