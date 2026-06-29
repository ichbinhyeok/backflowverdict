# Context Tracker

## Current status
- BackflowPath is tracked as an owner-facing utility-first compliance product.
- The public site centers on utility, state, metro, guide, tester, failed-test, provider, and request-help pages.
- Lead capture remains file-backed and manually reviewed through `/admin`.
- Public provider browse remains secondary to the governing utility workflow.
- As of 2026-06-29, 80 utility pages, 5 state guides, 13 metro pages, and 10 evergreen guides are fresh again and publishable.
- `/readyz` returns `ready` locally with `publishedUtilityCount=80`, `blockedUtilityCount=0`, and `staleUtilityCount=0`.

## Latest decisions
- Keep the governing utility or water authority as the canonical entity.
- Keep official guidance and public provider directories visibly separate.
- Do not reintroduce private setup or internal routing surfaces into the published experience.
- Manual request review is allowed; private internal routing workflows are not part of the active product.
- Freshness, verification, and source clarity outrank conversion experiments.
- Verification must surface operational freshness and link-health failures; a registry with zero published utility or state-guide pages is `needs-review`, not `ok`.
- Automated source URL audits can refresh freshness dates only when unresolved URLs are also recorded in `data/ops/broken_links.csv` for follow-up suppression.

## What changed this session
- Refreshed `lastVerified` / `lastReviewed` dates to 2026-06-29 for utility, state, metro, and guide JSON records.
- Ran an automated source URL audit across 215 unique official/support URLs; 119 passed and 96 unresolved URLs were recorded in `data/ops/broken_links.csv`.
- Added `app.ops.current-date` so tests and manual verification can use a fixed freshness date without changing production behavior.
- Changed verification reports to include freshness, broken-link, conflict, and zero-published-registry findings.
- Added a regression test proving a fully stale registry returns `needs-review` instead of `ok`.
- Added the Gradle Foojay toolchain resolver convention plugin so Java 21 toolchain auto-provisioning no longer emits the Gradle 10 deprecation warning.

## Next recommended tasks
1. Work through the 96 rows in `data/ops/broken_links.csv`, starting with true 404s before likely bot-protected 403s.
2. Replace or remove dead official URLs, then mark resolved rows with `ok`, `resolved`, `fixed`, `200`, `301`, or `302`.
3. Re-run `/ops/verification/run` after broken-link cleanup; status should return to `ok` once source URL issues are resolved.
4. Keep widening source-backed utility coverage and public provider inventory where official lists support it.
5. Keep hardening manual review, freshness, and indexing controls on the public owner-facing surface.

## Open questions
- Whether legacy internal provider-status fields should be renamed in code and data files after the public cleanup is complete.
- How far metro and provider aggregation should expand before it begins to dilute utility-first trust.
- Whether request-help follow-up needs a more explicit public service-level expectation on the site.
- Whether 403/time-out source audit failures should be tracked separately from definite 404s so bot-protected official pages do not suppress otherwise valid pages after the grace window.
