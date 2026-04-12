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

### 3) Optional tuning variables

- `DB_PORT` (default `5432`)
- `DB_MIN_IDLE` (default `5`)
- `DB_MAX_POOL_SIZE` (default `25`)
- `DB_CONNECTION_TIMEOUT_MS` (default `30000`)
- `DB_MAX_LIFETIME` (default `1500000`)
- `DB_KEEPALIVE_TIME` (default `300000`)
- `DB_VALIDATION_TIMEOUT_MS` (default `5000`)
- `CORS_ALLOW_CREDENTIALS` (default `false`)
- `SERVER_PORT` (default `8080`)

### 4) Actuator endpoints in production

Only these endpoints are exposed in `prod`:

- `/actuator/health`
- `/actuator/info`
