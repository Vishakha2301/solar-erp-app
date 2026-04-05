CREATE TABLE saved_costings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID NOT NULL REFERENCES users(id),

    context     JSONB NOT NULL,
    snapshot    JSONB NOT NULL
);

CREATE INDEX idx_saved_costings_created_by
    ON saved_costings (created_by, created_at DESC);