# Oracle Linux deploy notes

## Preconditions
- Java 21 installed at `/usr/bin/java`
- `backflowverdict.jar` built with `./gradlew bootJar`
- app data copied to `/opt/backflowverdict/data`
- runtime writable paths live under `/opt/backflowverdict/{data,ops,storage,leads,logs}` and should be owned by the `backflow` user

## Files
- `install-or-update.sh`: installs the jar, unit file, and env file
- `backflowverdict.service`: `systemd` unit
- `backflowverdict.env.example`: production env template

## Typical deploy
1. Build the jar with `./gradlew bootJar`.
2. Copy the repo or at least `build/libs`, `data`, and `ops/oracle` to the server.
3. Review `/etc/backflowverdict/backflowverdict.env`.
4. Run `sudo bash ops/oracle/install-or-update.sh`.
5. Verify `systemctl status backflowverdict` and `curl http://127.0.0.1:8080/healthz`.

## Notes
- `/ops/**` should stay private. Use a strong `APP_OPS_VERIFICATION_TOKEN`.
- `/admin` stays disabled until both `APP_ADMIN_USERNAME` and `APP_ADMIN_PASSWORD` are explicitly set.
- The example env file writes ops reports outside `build/` so they survive service restarts and redeploys.
- The install script re-owns writable runtime directories so JSON, CSV, report, snapshot, lead, and provider-commercial-state files are not lost on restart.
- Put nginx or another reverse proxy in front of the app and only expose public routes.
