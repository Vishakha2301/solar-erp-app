-- Only remove the seeded admin if no application data was created under it.
-- On databases with existing records this is a no-op so the migration stays safe.
-- Note: sizing_estimates (V11) does not exist yet at this migration step.
DELETE FROM users
WHERE username = 'admin'
  AND email = 'admin@solarerp.com'
  AND NOT EXISTS (SELECT 1 FROM saved_costings WHERE created_by = users.id)
  AND NOT EXISTS (SELECT 1 FROM customers      WHERE created_by = users.id)
  AND NOT EXISTS (SELECT 1 FROM materials      WHERE created_by = users.id)
  AND NOT EXISTS (SELECT 1 FROM quotations     WHERE created_by = users.id);
