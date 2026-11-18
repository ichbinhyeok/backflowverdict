# BackflowPath nginx notes

Use `backflowpath.conf` when the OCI box runs the Docker container on host port `8093` and nginx fronts the public hostname on port `80` behind Cloudflare.

## What this fixes
- Routes `backflowpath.com` and `www.backflowpath.com` to `127.0.0.1:8093`
- Blocks `/ops/**` from the public edge
- Keeps `/healthz` host-local

## Current symptom this config addresses
If `https://backflowpath.com/` returns another site, but GitHub Actions shows the BackflowPath container healthy on `127.0.0.1:8093`, nginx or the upstream hostname mapping is still pointing at the wrong app.

## Install
The GitHub Actions release now applies only the upload-size directive inside existing BackflowPath server blocks. `apply-upload-limit.py` reads nginx's active file inventory, backs up changed files under the release backup directory, validates with `nginx -t`, and reloads. Other virtual hosts and existing TLS/proxy settings are retained. The workflow restores these files if release verification fails.

The release requires passwordless sudo for the nginx updater, an installed `python3`, and the existing nginx service. Local parser checks: `python -m unittest discover -s ops/nginx -p 'test_*.py'`.

Manual installation of the complete reference configuration:
1. Copy `backflowpath.conf` to `/etc/nginx/conf.d/backflowpath.conf` or your distro's sites-available path.
2. Run `sudo nginx -t`.
3. Reload nginx with `sudo systemctl reload nginx`.

## Verify on the OCI host
```bash
curl -fsS http://127.0.0.1:8093/healthz
curl -I -H 'Host: backflowpath.com' http://127.0.0.1/
curl -I https://backflowpath.com/
```

The second and third checks should resolve to BackflowPath, not another app.
