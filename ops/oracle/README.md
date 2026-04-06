# Oracle Linux deploy notes

## Preconditions
- Java 21 installed at `/usr/bin/java`
- `backflowpath.jar` built with `./gradlew bootJar`
- app data copied to `/opt/backflowpath/data`
- runtime writable paths live under `/opt/backflowpath/{data,ops,storage,leads,logs}` and should be owned by the `backflow` user

## Files
- `install-or-update.sh`: installs the jar, unit file, and env file
- `backflowpath.service`: `systemd` unit
- `backflowpath.env.example`: production env template

## Typical deploy
1. Build the jar with `./gradlew bootJar`.
2. Copy the repo or at least `build/libs`, `data`, and `ops/oracle` to the server.
3. Review `/etc/backflowpath/backflowpath.env`.
4. Run `sudo bash ops/oracle/install-or-update.sh`.
5. Verify `systemctl status backflowpath` and `curl http://127.0.0.1:8093/healthz`.

## Notes
- `/ops/**` should stay private. Use a strong `APP_OPS_VERIFICATION_TOKEN`.
- `/admin` stays disabled until both `APP_ADMIN_USERNAME` and `APP_ADMIN_PASSWORD` are explicitly set.
- The example env file writes ops reports outside `build/` so they survive service restarts and redeploys.
- The install script re-owns writable runtime directories so JSON, CSV, report, snapshot, lead, and provider-commercial-state files are not lost on restart.
- Put nginx or another reverse proxy in front of the app and only expose public routes.
- If `https://backflowpath.com/` renders another app while `curl http://127.0.0.1:8093/healthz` is healthy, the issue is the nginx or Cloudflare hostname mapping, not the BackflowPath container.
