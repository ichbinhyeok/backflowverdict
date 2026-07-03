# Context Tracker

## Current status
- BackflowPath is tracked as an owner-facing utility-first compliance product.
- The public site centers on utility, state, metro, guide, tester, failed-test, provider, and request-help pages.
- Lead capture remains file-backed and manually reviewed through `/admin`.
- Public provider browse remains secondary to the governing utility workflow.
- As of 2026-06-30, 80 utility pages, 5 state guides, 13 metro pages, and 10 evergreen guides are publishable.
- `/readyz` must stay `ready` as long as utility pages are explicitly published and not blocked, even when freshness warnings exist.

## Latest decisions
- Keep the governing utility or water authority as the canonical entity.
- Keep official guidance and public provider directories visibly separate.
- Do not reintroduce private setup or internal routing surfaces into the published experience.
- Manual request review is allowed; private internal routing workflows are not part of the active product.
- Freshness, verification, source clarity, and stable crawlable URLs outrank conversion experiments.
- Freshness and broken-link warnings no longer remove explicitly published pages from public routes; missing source evidence or explicit conflict blocks still can.
- Verification must surface operational freshness and link-health failures; a registry with zero published utility or state-guide pages is `needs-review`, not `ok`.
- Automated source URL audits can refresh freshness dates only when unresolved URLs are also recorded in `data/ops/broken_links.csv` for follow-up suppression.

## What changed this session
- Ran a 2026-07-03 search/SEO audit using Google Search Console, live crawl checks, Lighthouse, and public web source checks.
- Added `ops/click_100_audit_2026-07-03.md` with the current search score, 50 official-source cases, technical findings, and a one-month plan to reach 100 monthly organic clicks.
- Confirmed the latest 28-day GSC window, ending 2026-06-30, shows 17 clicks, 2,031 impressions, 0.84% CTR, and average position 12.0.
- Confirmed live sitemap exposure at 643 URLs and Lighthouse home scores of Performance 73, Accessibility 100, Best Practices 100, and SEO 100.
- Found one definite source issue in the 50-case URL spot check: the stored Lee County Utilities official program URL returned 404, then replaced it with official Lee County Utilities cross-connection policy and BSI instruction PDFs.
- Strengthened portal search landing pages with sharper BSI, WEIRS, and SwiftComply title/meta copy plus utility-page and city-page internal links back to the relevant portal hub.
- Rewrote current high-impression guide and metro labels for reporting portals, backflow test cost, due-date intent, Phoenix metro, and Dallas-Fort Worth metro.
- Added `ops/gsc_priority_reindex_urls_2026-07-03.txt` with 50 priority BackflowPath URLs for manual Search Console indexing requests.
- Added stronger crawl paths from portal hub pages to matching city pages, from metro city lists to city landing pages, and from the reporting-portals guide to BSI, SwiftComply, and WEIRS hub pages.
- Added regression coverage for the new portal, guide, and metro internal links.
- Added `ops/winning_pattern_research_2026-07-03.md` after reviewing official and commercial examples from Holly Hill, SwiftComply, Dallas, Tampa, Queen Creek, Fort Worth, Accuracy Backflow, and Backflow Paradise.
- Replicated the strongest visible patterns into the site: above-fold test/tester/portal/fee checklist, owner-vs-tester action split, portal credential checklist, utility-to-city route links, metro high-intent path cards, and FAQ entries for portal, scheduling, and cost questions.
- Verified the replicated patterns on a fresh local server at `http://localhost:8096` across Tampa utility, Dallas city, BSI portal, and DFW metro pages.
- Refreshed `lastVerified` / `lastReviewed` dates to 2026-06-29 for utility, state, metro, and guide JSON records.
- Ran an automated source URL audit across 215 unique official/support URLs; 119 passed and 96 unresolved URLs were recorded in `data/ops/broken_links.csv`.
- Added `app.ops.current-date` so tests and manual verification can use a fixed freshness date without changing production behavior.
- Changed verification reports to include freshness, broken-link, conflict, and zero-published-registry findings.
- Added a regression test proving a fully stale registry stays published but returns re-verification warnings.
- Added official tester-list and reporting-portal hub routes for higher-intent search demand.
- Restored `/claim-listing` as a public provider correction and source-update path.
- Converted direct city aliases from 301 utility redirects into indexable city landing pages that still route users to the governing utility workflow.
- Kept county/weak authority city bridge aliases noindex and excluded them from sitemap.
- Added city route reindex and 100 clicks/day strategy files under `ops/`.
- Added the Gradle Foojay toolchain resolver convention plugin so Java 21 toolchain auto-provisioning no longer emits the Gradle 10 deprecation warning.

## Next recommended tasks
1. Continue working through the unresolved rows in `data/ops/broken_links.csv`, starting with true 404s before likely bot-protected 403s.
2. Submit the sitemap plus URLs from `ops/gsc_priority_reindex_urls_2026-07-03.txt` in Search Console, then measure movement against the 2026-08-03 100-click monthly target.
3. Manually recheck Greeley and JEA because the 2026-07-03 URL spot check errored under automation.
4. Keep strengthening `/backflow-reporting-portals/swiftcomply`, `/bsi`, and `/weirs` as search landing pages as more portal utilities are added.
5. Re-run `/ops/verification/run` after broken-link cleanup; status should return to `ok` once source URL issues are resolved.

## Open questions
- Whether legacy internal provider-status fields should be renamed in code and data files after the public cleanup is complete.
- How far metro and provider aggregation should expand before it begins to dilute utility-first trust.
- Whether request-help follow-up needs a more explicit public service-level expectation on the site.
- Whether 403/time-out source audit failures should be tracked separately from definite 404s so bot-protected official pages do not suppress otherwise valid pages after the grace window.
