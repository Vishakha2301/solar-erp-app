# Production Readiness Check for First Customer

Date: 2026-04-13 (UTC)
Scope: repository-level review of configuration, security defaults, and operational setup for `solar-erp-app`.

## Executive summary

**Current recommendation: NOT READY for first external customer without a short hardening sprint.**

The codebase already includes several strong production foundations (profile-based config, database migrations, JWT auth, graceful shutdown, and request correlation logging). However, there are a few high-priority gaps that should be closed before onboarding a real customer.

## What is already in good shape

1. **Production profile exists and is env-driven**
   - Dedicated `prod` profile with required env vars for DB and JWT secret.
   - CORS origins are externally configurable.
2. **Schema lifecycle management is in place**
   - Flyway migrations are enabled by default with versioned SQL migrations.
3. **Security baseline exists**
   - API is locked down by default except login endpoints.
   - Password hashing uses BCrypt.
4. **Operational basics exist**
   - Graceful shutdown enabled.
   - Actuator endpoint exposure is reduced to `health` and `info`.
   - Request correlation ID is logged and returned via `X-Request-Id`.

## High-priority blockers before first customer

1. **Replace permanent seeded admin with controlled bootstrap flow**
   - Avoid shipping a static default admin in SQL migrations.
   - Use one-time bootstrap admin env vars only when the users table is empty, then disable the bootstrap flag after first login.

2. **Login abuse protections are still pending**
   - No account lockout, failed-attempt throttling, or rate limiting found for `/api/v1/auth/login`.
   - Add brute-force protections before exposing publicly.

3. **Operational runbooks and SLO alerting still pending**
   - No repository evidence of backup/restore cadence, RPO/RTO targets, or restore drill checklist.
   - Production operations also need centralized logs, alerting, and latency/error-rate dashboards.

## Medium-priority gaps (recommended before/soon after go-live)

1. **Runbook and disaster recovery**
   - No repository evidence of backup/restore cadence, RPO/RTO targets, or restore drill checklist.

2. **Observability depth**
   - Health/info endpoints are good, but production operations typically also need centralized logs, alerting, and error-rate/latency SLO dashboards.

3. **Release readiness evidence**
   - In this environment, full `mvn test` could not be executed because Maven wrapper dependency download failed.
   - Capture a clean CI run artifact before go-live.

## Go-live checklist (first-customer minimum)

- [ ] Use one-time bootstrap admin creation and keep it disabled by default.
- [x] Define and enforce RBAC on business endpoints.
- [ ] Add login rate limiting and failed-attempt controls.
- [ ] Produce green CI report (tests + coverage + static analysis).
- [ ] Document backup/restore process and perform one restore drill.
- [ ] Confirm production secrets are injected via secret manager (not shell history or compose files).
- [ ] Confirm TLS termination, domain allowlist for CORS, and reverse-proxy hardening.
- [ ] Create on-call runbook for incidents (auth failures, DB saturation, migration rollback).

## Suggested 1-week hardening plan

### Day 1-2
- Finalize one-time bootstrap admin behavior for production and disable after setup.
- Validate newly-applied method-level role authorization (`@PreAuthorize`) in UAT.

### Day 3
- Add login throttling (IP/user-based), lockout policy, and structured audit events.

### Day 4
- Set up production dashboards and alerts (auth failures, 5xx spikes, p95 latency, DB pool exhaustion).

### Day 5
- Execute full CI and smoke tests against prod-like environment.
- Perform backup restore drill and sign off readiness.

## Verdict

With the blockers above fixed and one clean production-like validation run, this service should be in a solid position for first customer onboarding.
