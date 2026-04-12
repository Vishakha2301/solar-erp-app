# solar-erp-app
Backend for solar ERP.

## Production configuration

### 1) Enable production profile
Start the backend with:

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw -pl solar-erp-app spring-boot:run
```

### 2) Required environment variables for production
The following variables must be configured in production:

- `DB_HOST`
- `DB_PORT` (optional, default `5432`)
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGIN_PATTERNS` (comma-separated, e.g. `https://erp.example.com,https://admin.example.com`)

### 3) Optional tuning variables

- `DB_MIN_IDLE` (default `5`)
- `DB_MAX_POOL_SIZE` (default `25`)
- `DB_CONNECTION_TIMEOUT_MS` (default `30000`)
- `DB_MAX_LIFETIME` (default `1500000`)
- `DB_KEEPALIVE_TIME` (default `300000`)
- `DB_VALIDATION_TIMEOUT_MS` (default `5000`)
- `CORS_ALLOW_CREDENTIALS` (default `false`)
- `SERVER_PORT` (default `8080`)

### 4) Actuator endpoints
In production profile, actuator exposes only:

- `/actuator/health`
- `/actuator/info`
