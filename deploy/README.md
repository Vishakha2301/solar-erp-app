# Solar ERP — Production Deployment

This folder contains everything needed to deploy the backend to a Linux VPS.

## Architecture

```
+--------------------------------------------------------------+
|                  VPS (Ubuntu 24.04)                          |
|                                                              |
|  +------------+      +-----------------------------------+   |
|  | Caddy/     |----->| Backend JAR (systemd service)     |   |
|  | nginx:443  |      | localhost:8080                    |   |
|  +------------+      +-----------------------------------+   |
|       ^                          |                           |
|       | HTTPS                    | JDBC                      |
|       |                          v                           |
|       |              +---------------------------+           |
|       |              | Postgres 17 (Docker)      |           |
|       |              | 127.0.0.1:5432 (loopback) |           |
|       |              +---------------------------+           |
+-------|------------------------------------------------------+
        | (public)
   Customer's Android APK
```

- **Backend** runs as a native systemd service on the host (not in Docker).
  Faster restarts, simpler logs via `journalctl`.
- **Postgres** runs in Docker, bound to **loopback only** — never exposed to
  the public internet. Backend connects via `127.0.0.1:5432`.
- **HTTPS termination** happens at Caddy (or nginx) on port 443; reverse-proxies
  to the backend on `localhost:8080`. (Caddy setup is a later stage.)

## Files

| File | Purpose |
|---|---|
| `docker-compose.prod.yml` | Postgres-only compose, hardened for prod |
| `.env.prod.example`       | Template for `.env.prod` (real file is gitignored) |
| `solar-erp-app.service`   | Systemd unit for the backend JAR |
| `README.md`               | This file |

## VPS layout

```
/opt/solar-erp/
├── app/
│   └── solar-erp-app.jar          (the runnable JAR — scp'd from dev laptop)
└── deploy/
    ├── docker-compose.prod.yml    (this folder, copied to VPS)
    ├── .env.prod                  (NOT in git — created on VPS from .example)
    └── solar-erp-app.service      (copied to /etc/systemd/system/)
```

Owner: a dedicated `solarerp` user. Filesystem perms: `750` on directories,
`640` on `.env.prod`, owned by `solarerp:solarerp`.

## First-time deployment (high-level steps)

These are summarized here. The detailed walkthrough happens during the
"Stage 4: backend deployment" of the go-live runbook.

1. **VPS prep**
   - `sudo apt update && sudo apt upgrade -y`
   - Install Java 21: `sudo apt install -y openjdk-21-jre-headless`
   - Install Docker Engine + compose plugin (standard Docker docs)
   - Create user: `sudo useradd -r -m -d /opt/solar-erp -s /bin/bash solarerp`
   - Add `solarerp` to the `docker` group: `sudo usermod -aG docker solarerp`
   - Make folders: `/opt/solar-erp/app`, `/opt/solar-erp/deploy`,
     `/var/log/solar-erp` (chown all to `solarerp:solarerp`)

2. **Ship deploy artifacts**
   - From dev laptop:
     `scp deploy/docker-compose.prod.yml deploy/.env.prod.example deploy/solar-erp-app.service <vps>:/opt/solar-erp/deploy/`
   - On VPS: `cp .env.prod.example .env.prod` and fill in real values from
     Bitwarden (DB_PASSWORD, JWT_SECRET, BOOTSTRAP_ADMIN_PASSWORD, etc.)
   - `chmod 640 .env.prod && chown solarerp:solarerp .env.prod`

3. **Start Postgres**
   - `cd /opt/solar-erp/deploy`
   - `docker compose -f docker-compose.prod.yml --env-file .env.prod up -d`
   - Verify: `docker compose -f docker-compose.prod.yml ps` — should show healthy

4. **Build and ship backend JAR**
   - On dev laptop:
     `./mvnw clean package -pl solar-erp-app -am -DskipTests=false`
   - `scp solar-erp-app/target/solar-erp-app-0.0.1-SNAPSHOT.jar <vps>:/opt/solar-erp/app/solar-erp-app.jar`

5. **Install systemd service**
   - On VPS:
     `sudo cp /opt/solar-erp/deploy/solar-erp-app.service /etc/systemd/system/`
   - `sudo systemctl daemon-reload`
   - `sudo systemctl enable --now solar-erp-app`
   - `sudo systemctl status solar-erp-app`
   - `sudo journalctl -u solar-erp-app -f`

6. **Smoke test (still on HTTP, before Caddy)**
   - `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`
   - Log in via the bootstrap admin to confirm DB connectivity

7. **Disable bootstrap after first login**
   - Edit `/opt/solar-erp/deploy/.env.prod` → `BOOTSTRAP_ADMIN_ENABLED=false`
   - `sudo systemctl restart solar-erp-app`

8. **Add HTTPS** (Caddy stage — covered separately)

## Updating the backend later

```bash
# On dev laptop:
./mvnw clean package -pl solar-erp-app -am
scp solar-erp-app/target/solar-erp-app-0.0.1-SNAPSHOT.jar \
    <vps>:/opt/solar-erp/app/solar-erp-app.jar

# On VPS:
sudo systemctl restart solar-erp-app
sudo journalctl -u solar-erp-app -f
```

## Backups (Postgres)

Daily logical dump + retention. Set up via a cron job on the VPS:

```bash
# Example crontab entry — runs daily at 02:30 IST
30 2 * * * docker exec solar-erp-postgres-prod \
    pg_dump -U solarerp solarerp \
    | gzip > /var/backups/solar-erp/db-$(date +\%F).sql.gz \
    && find /var/backups/solar-erp -name 'db-*.sql.gz' -mtime +14 -delete
```

(This is a starting point; production-grade backup setup is a later stage.)

## Rollback

If a deploy goes bad:

1. Keep the previous JAR around (rename, don't overwrite):
   `mv solar-erp-app.jar solar-erp-app.jar.prev`
2. To roll back: `mv solar-erp-app.jar.prev solar-erp-app.jar && sudo systemctl restart solar-erp-app`
3. For DB changes that are migration-driven, restore from the latest
   `pg_dump` snapshot — see backup script above.
