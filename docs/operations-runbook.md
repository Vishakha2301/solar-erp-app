# Operations Runbook (Starter)

## Incident response playbook

1. **Triage (0-10 min)**
   - Confirm impact: affected endpoints, customer count, start time.
   - Check `/actuator/health` and latest error logs.
2. **Containment (10-30 min)**
   - If auth abuse: raise rate-limit thresholds only if false positives are confirmed.
   - If DB pressure: scale read replicas / tune pool / enable temporary traffic shaping.
3. **Mitigation (30+ min)**
   - Roll back latest release if incident began after deployment.
   - Apply targeted fix and verify with smoke checks.
4. **Comms**
   - Internal update every 30 minutes until resolved.
   - Customer update cadence per severity policy.
5. **Post-incident**
   - Publish RCA within 2 business days.

## Backup / restore procedure

### Targets
- **RPO**: 15 minutes
- **RTO**: 60 minutes

### Backup cadence
- WAL archiving: continuous
- Full snapshot: daily
- Retention: 30 days

### Restore drill checklist
1. Provision isolated restore environment.
2. Restore latest full backup.
3. Replay WAL up to target timestamp.
4. Run migration validation (`flyway validate`).
5. Execute smoke tests (`/health`, login, key read/write flows).
6. Record elapsed restore time and compare with RTO.

## Ownership
- Primary on-call: Platform
- Secondary on-call: Backend
- Escalation: Engineering Manager
