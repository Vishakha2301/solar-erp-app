-- Adds pricing to the material catalog.
-- unit_price is the current catalog price per `unit` (e.g. per Wp, per Nos, per Mtr).
-- gst_rate is the applicable GST percentage for this material.
-- Both are nullable so existing rows remain valid; the admin sets them via the
-- material screens. Historical quotations are unaffected because saved_costings
-- freezes prices into its JSONB snapshot at quote time.

ALTER TABLE materials
    ADD COLUMN unit_price NUMERIC(12, 2),
    ADD COLUMN gst_rate   NUMERIC(5, 2);
