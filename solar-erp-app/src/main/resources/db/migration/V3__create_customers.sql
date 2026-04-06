CREATE TYPE customer_type AS ENUM ('INDIVIDUAL', 'COMPANY', 'SOCIETY');

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_type   customer_type NOT NULL,
    name            VARCHAR(255) NOT NULL,
    company_name    VARCHAR(255),
    phone           VARCHAR(20) NOT NULL,
    email           VARCHAR(255),
    address         TEXT,
    city            VARCHAR(100),
    state           VARCHAR(100),
    pincode         VARCHAR(10),
    gst_number      VARCHAR(20),
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID NOT NULL REFERENCES users(id)
);

CREATE TABLE customer_sites (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    site_label  VARCHAR(255) NOT NULL,
    address     TEXT,
    city        VARCHAR(100),
    state       VARCHAR(100),
    pincode     VARCHAR(10),
    is_default  BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_customers_created_by ON customers(created_by);
CREATE INDEX idx_customer_sites_customer_id ON customer_sites(customer_id);
