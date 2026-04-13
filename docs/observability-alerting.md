# Observability & Alerting Baseline

## Required telemetry

- Centralized searchable logs (request-id correlated).
- API metrics: request count, p95/p99 latency, 4xx/5xx rates.
- Auth metrics: login success/failure, lockouts, rate-limit rejections.
- DB metrics: connection pool usage, acquisition timeout, slow queries.

## Minimum production alerts

1. **5xx error rate**
   - Trigger: >2% for 5 minutes
2. **Latency**
   - Trigger: p95 > 1.5s for 10 minutes
3. **Auth failures spike**
   - Trigger: login failures > 3x baseline for 10 minutes
4. **DB saturation**
   - Trigger: pool utilization > 85% for 10 minutes
5. **Health endpoint**
   - Trigger: readiness check fails 3 consecutive probes

## Dashboard sections

- API overview
- Authentication health
- Database health
- Top failing endpoints
- Deployment markers / release versions
