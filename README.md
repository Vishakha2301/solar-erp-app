# solar-erp-app
Backend for solar ERP.

## Runtime profiles

- `local` (default): developer-friendly defaults.
- `prod`: strict production configuration driven by environment variables.

## Local run

```bash
./mvnw -pl solar-erp-app spring-boot:run
```

## Production configuration

### 1) Enable production profile

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw -pl solar-erp-app spring-boot:run
```

### 2) Required environment variables for production

- `DB_HOST`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS` (comma-separated list, e.g. `https://erp.example.com,https://admin.example.com`)

Bootstrap admin for first deployment (optional but recommended when DB is empty):
- `BOOTSTRAP_ADMIN_ENABLED=true`
- `BOOTSTRAP_ADMIN_USERNAME`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`
- `BOOTSTRAP_ADMIN_ROLE` (optional, default `ADMIN`)

### 3) Optional tuning variables

- `DB_PORT` (default `5432`)
- `DB_MIN_IDLE` (default `5`)
- `DB_MAX_POOL_SIZE` (default `25`)
- `DB_CONNECTION_TIMEOUT_MS` (default `30000`)
- `DB_MAX_LIFETIME` (default `1500000`)
- `DB_KEEPALIVE_TIME` (default `300000`)
- `DB_VALIDATION_TIMEOUT_MS` (default `5000`)
- `CORS_ALLOW_CREDENTIALS` (default `false`)
- `LOGIN_RATE_LIMIT_WINDOW_MINUTES` (default `1`)
- `LOGIN_RATE_LIMIT_MAX_ATTEMPTS` (default `20`)
- `LOGIN_LOCKOUT_MAX_FAILED_ATTEMPTS` (default `5`)
- `LOGIN_LOCKOUT_DURATION_MINUTES` (default `15`)
- `SERVER_PORT` (default `8080`)

### 4) Actuator endpoints in production

Only these endpoints are exposed in `prod`:

- `/actuator/health`
- `/actuator/info`

### 5) First customer bootstrap flow (exact sequence)

1. Start once with bootstrap enabled and credentials set:

```bash
SPRING_PROFILES_ACTIVE=prod \
BOOTSTRAP_ADMIN_ENABLED=true \
BOOTSTRAP_ADMIN_USERNAME=owner.admin \
BOOTSTRAP_ADMIN_EMAIL=owner.admin@yourcompany.com \
BOOTSTRAP_ADMIN_PASSWORD='<strong-random-password>' \
./mvnw -pl solar-erp-app spring-boot:run
```

2. On startup, the app checks:
   - if `BOOTSTRAP_ADMIN_ENABLED` is `true`
   - and if the `users` table is empty

3. If both are true, it creates exactly one user with BCrypt password hash and configured role (default `ADMIN`).

4. Login via `/api/v1/auth/login` using that bootstrap username/email + password.

5. Immediately disable bootstrap (`BOOTSTRAP_ADMIN_ENABLED=false`) and restart.

6. Future restarts will not create another bootstrap user, and if users already exist the bootstrap step is skipped.


## Production readiness assessment

- See `docs/first-customer-production-readiness.md` for the latest first-customer go-live review and checklist.
- See `docs/operations-runbook.md` for incident and backup/restore playbooks.
- See `docs/observability-alerting.md` for alert and dashboard baseline.
- For first login without a seeded SQL user, use the bootstrap admin environment variables above, then disable `BOOTSTRAP_ADMIN_ENABLED` after initial setup.
