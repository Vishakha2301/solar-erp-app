# Production Readiness Check for First Customer

Date: 2026-04-13 (UTC)  
Scope: repository-level review of configuration, security controls, and operational readiness for `solar-erp-app`.

## Executive summary

**Current recommendation: Almost ready, but still NOT READY for first external customer until the remaining operational controls are completed.**

## Completed in repository

- [x] One-time bootstrap admin flow and removal of permanent seeded admin.
- [x] RBAC enforcement on business endpoints.
- [x] Login abuse protection (rate limiting + account lockout + auth audit logging).
- [x] Draft docs for runbooks and observability baseline.

## Remaining before first customer go-live

1. **Operationalize login abuse controls in production**
   - Confirm thresholds in real traffic (`LOGIN_RATE_LIMIT_*`, `LOGIN_LOCKOUT_*`).
   - Validate lockout/reset behavior in UAT and incident simulations.

2. **Finalize operational runbooks with ownership and drills**
   - Convert `docs/operations-runbook.md` into an on-call owned SOP.
   - Run at least one backup/restore drill and record achieved RPO/RTO.

3. **Wire observability baseline into real tooling**
   - Implement dashboards and alerts from `docs/observability-alerting.md`.
   - Ensure centralized searchable logs and alert routing are live.

4. **Produce release evidence from CI in a network-enabled environment**
   - Attach a clean quality run: `mvn test`, coverage, and static analysis.
   - Keep the build/test artifact as go-live evidence.

## Go-live gate checklist (must be green)

- [ ] Login abuse thresholds tuned and validated in UAT.
- [ ] Backup/restore drill completed and documented with actual RPO/RTO.
- [ ] Production alerting + dashboards enabled (5xx, latency, auth failures, DB saturation).
- [ ] Centralized log search verified with request-id correlation.
- [ ] Clean CI quality report attached to release ticket.
- [ ] TLS/reverse-proxy/CORS/domain controls validated in production environment.

## Verdict

Once the six checklist items above are complete, the backend is in a suitable state for first customer onboarding.
