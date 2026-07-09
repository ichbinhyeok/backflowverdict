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
- Added `ops/funnel_pattern_research_2026-07-03.md` after expanding research into official and commercial notice-first funnel examples such as Dallas, Tampa, Queen Creek, Hillsborough County, Fort Worth, Pittsburgh Backflow Testing, City Certified Backflow, David's Plumbing, H2O & Backflow Tests, and A Plus Backflow.
- Added a new top-of-funnel guide at `/guides/backflow-test-notice-next-steps` with a notice-routing section for deadline, portal, tester, failed-test, cost, and utility workflows.
- Added the new notice guide to utility support guides, state support guides, metro guide selection, portal related guides, sitemap, tests, and the 50-URL GSC priority reindex file.
- Verified the notice guide and sitemap on a fresh local server at `http://localhost:8097`.
- Added daily-click expansion work in `ops/daily_100_click_expansion_2026-07-03.md` after the goal was clarified from monthly 100 clicks to daily 100 clicks.
- Added evidence-gated city intent routes for annual testing, reporting portals, approved testers, failed tests, irrigation testing, and fire-line testing; unsupported city/intent combinations return 404 and noindex bridge aliases stay out of the sitemap.
- Added `src/main/jte/pages/city-intent-page.jte` plus controller/sitemap/test coverage for the new route family.
- Updated the GSC priority reindex file to include high-intent city URLs such as Dallas portal/annual/failed-test, Queen Creek portal/approved-testers, Tampa portal/annual, Fort Worth portal, and Aurora portal.
- Verified representative city-intent pages and sitemap output on `http://localhost:8098`; local sitemap now renders 970 URLs.
- Added VEPO/Envirotrax as a source-backed reporting portal family after reviewing official Fort Worth/VEPO workflow language; added `/backflow-reporting-portals/vepo`, guide links, sitemap coverage, and GSC priority URLs without generating unsupported city combinations.
- Added 7 official-source VEPO/Envirotrax Texas utility records for Southlake, Bedford, Mansfield, Cleburne, Marble Falls, Taylor, and Buda, plus city aliases, source snapshots, DFW/Central Texas metro membership, and refreshed GSC priority URLs for the strongest new city-intent pages.
- Added portal comparison tables to reporting portal hubs and 4 additional official-source Texas utilities: League City BSI, Irving Envirotrax, Baytown Envirotrax/CSI, and Liberty City WSC VEPO. Added city aliases, source snapshots, a Texas Gulf Coast metro page, and refreshed GSC priority URLs for the new high-intent routes.
- Added Aqua/TrackMyBackflow and Tokay WebTest as source-backed reporting portal families after reviewing official Euless, Buena Park, Pleasanton, Oxnard, and DSRSD workflow language; added `/backflow-reporting-portals/aqua`, `/backflow-reporting-portals/tokay`, 5 utility records, 6 city aliases, source snapshots, metro membership, guide links, sitemap coverage, and GSC priority URLs.
- Verified the Aqua/Tokay expansion on `http://localhost:8102`; local sitemap now renders 1,167 URLs and includes the new portal hubs plus Euless and Oxnard city-intent routes.
- Added `/notice-finder` as an indexable notice-to-route tool, wired it into header, home, footer, sitemap, and GSC priority URLs. It matches city, utility, portal, tester, due-date, failed-test, irrigation, and fire-line clues to source-backed routes.
- Added above-fold notice checklists to utility, city bridge, and city-intent pages, and strengthened portal comparison tables with notice/device ID and report-acceptance columns.
- Verified the notice finder and checklist expansion on `http://localhost:8103`; local sitemap now renders 1,168 URLs and includes `/notice-finder`.
- Search Console inspection showed `/notice-finder` had been crawled but not indexed, while new Aqua/Tokay city and portal URLs were still unknown to Google. Added `/sitemap-priority.xml`, exposed it in `robots.txt`, and added direct home plus notice-finder links to the highest-intent notice routes.
- On 2026-07-09, strengthened the codebase-side click funnel without adding unsupported pages: portal/city/utility titles now name the actual portal family, `/notice-finder` exposes clue-based shortcuts and FAQ schema, portal hubs expose visible FAQ blocks plus FAQPage schema, city-intent pages expose city-specific FAQ blocks, WebSite structured data now declares `/notice-finder?q={search_term_string}` as SearchAction, notice-finder searches emit a GA event, and `/sitemap-priority.xml` now includes WEIRS, Austin, Fort Worth annual/failed, portal guide, cost guide, and official tester-list routes.
- Added `ops/reddit_pseo_100_case_comparison_2026-07-09.md`, a Reddit-sourced review of 100 pSEO cases/patterns compared against BackflowPath. The key conclusion is that BackflowPath is structurally aligned, but daily 100-click odds depend on deeper per-page facts, contextual internal links, notice-first conversion, and authority/backlink work rather than page count alone.
- Added `submit-backflow-report` as a source-gated city intent route for utilities with portal/online submission workflows, added WebApplication schema to `/notice-finder`, expanded priority sitemap coverage for Austin, Dallas, Fort Worth, Aurora, Euless, Oxnard, Irving, Tampa, Anaheim, Goodyear, and Hillsborough routes, and added contextual guide links into GSC-visible priority pages. Documented the work in `ops/daily_100_click_execution_2026-07-09.md`.
- Deepened the same execution pass by adding visible submission-packet blocks plus HowTo schema to `submit-backflow-report` pages, adding ItemList schema to portal hubs, and linking portal hub utility cards directly into city submit-report routes.
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
2. Submit `/sitemap.xml` and `/sitemap-priority.xml` in Search Console, then use URL inspection for the 50 URLs in `ops/gsc_priority_reindex_urls_2026-07-03.txt`.
3. Manually recheck Greeley and JEA because the 2026-07-03 URL spot check errored under automation.
4. Add sourced fee, deadline, and rejection-risk fields to more utilities so the notice finder and portal matrices can expose harder facts above the fold.
5. Re-run `/ops/verification/run` after broken-link cleanup; status should return to `ok` once source URL issues are resolved.

## Open questions
- Whether legacy internal provider-status fields should be renamed in code and data files after the public cleanup is complete.
- How far metro and provider aggregation should expand before it begins to dilute utility-first trust.
- Whether request-help follow-up needs a more explicit public service-level expectation on the site.
- Whether 403/time-out source audit failures should be tracked separately from definite 404s so bot-protected official pages do not suppress otherwise valid pages after the grace window.
