# Context Tracker

## 2026-08-11 — repository-wide SEO execution

- Crawled all 1,323 sitemap URLs locally; every URL returned 200 with one H1, unique title and description, an indexable robots directive, and a matching canonical.
- Added direct homepage crawl paths to the repair-kit finder, installation requirements/detail page, and RPZ/category answer.
- Narrowed `/backflow-reporting-portal/` to official-portal verification so `/backflow-reporting-portals` can own generic portal-family lookup intent; rewrote the strongest portal-family titles around exact TrackMyBackflow, Tokay, SpryBackflow, SwiftComply, VEPO, city, and utility language.
- Added `ops/daily_100_click_execution_2026-08-11.md` with the 74,700-impression / 3.75%-CTR portfolio model, query ownership boundaries, and 30/60/90-day GSC gates.

## 2026-08-11 — first post-launch ranking intervention

- Rechecked the three manually requested launch URLs in Google Search Console; all now return `PASS` and `Submitted and indexed`, with successful mobile fetches, allowed indexing, and matching Google/user canonicals.
- Measured the first 2026-08-02 through 2026-08-08 query sample: repair kits received 7 impressions at position 45.6, installation detail 1 at 38, installation requirements 1 at 60, and the RPZ-difference query 2 at 91.
- Confirmed the site-level 28-day comparison is expanding rather than collapsing: clicks rose 28 to 116, impressions 2,978 to 7,465, and CTR 0.94% to 1.55%; average position moved from 12.33 to 14.12 as lower-ranked query coverage expanded.
- Strengthened `/backflow-preventer-repair-kits/` with an exact-intent title/H1, visible model-size-part matrix, matching rules, FAQ content, model-passport return links, and CollectionPage, ItemList, FAQPage, and Breadcrumb structured data.
- Strengthened `/backflow-preventer-installation/` with explicit installation-requirements and installation-detail sections, field and closeout checklists, and direct RPZ/DCVA/PVB comparison paths.
- Assigned `backflow preventer installation requirements` and `backflow preventer installation detail` to the installation decision page; narrowed `/backflow-installation-requirements/` to adopted-code and approval workflow intent.
- Assigned `difference between rpz and backflow preventer` and `is an rpz a backflow preventer` to `/backflow-preventer/`, added a visible RPZ boundary answer, and linked the exact comparison routes.
- Verified the change with the full Gradle suite, 6/6 Playwright browser contracts, and fresh desktop/mobile Chromium captures under `output/playwright/`.

## 2026-08-02 — production deployment and GSC submission

- Deployed the 100-asset BackflowVerdict decision portfolio to production at commit `beb0b1c` through GitHub Actions run `30752485808`.
- Confirmed full Gradle tests, 6/6 browser contracts, OCI health and route smoke checks, canonical, robots, and sitemap checks.
- Verified the live main sitemap at 1,323 URLs and the live priority sitemap at 447 URLs.
- Resubmitted `sitemap.xml` and `sitemap-priority.xml` in the logged-in Google Search Console browser; both submissions were confirmed.
- Manually requested indexing for `/backflow-preventer/`, `/backflow-preventer-repair-kits/`, and `/backflow-preventer-installation/`; all three were added to Google's priority crawl queue.
- Saved the deployment and submission evidence in `analysis/deployment_gsc_submission_2026-08-02.md`.

## 2026-08-02 — strengthened decision portfolio

- Expanded the explicit decision-guide set from 9 to 26 routes using the validated Wave A/B portfolio. Wave C generic check-valve pages remain intentionally gated until traction.
- Added water-pressure-regulator, PVB/AVB/hose-bib, irrigation, RPZ, backwater installation/cost, and relief-vent paths.
- Added interactive identifier, diagnostic, repair score, PSI, and cost modules.
- Added centralized EPA, manufacturer, and municipal evidence ledgers plus Article/Breadcrumb structured data.
- Added page-group/tool-result analytics events and automated guide link/accessibility contracts.
- Deployment now runs the full test suite and no longer uses destructive `rsync --delete` for project data.

## Current status
- The repository-wide SEO pass is merge-ready locally: full Gradle tests and Playwright 6/6 pass, and the sitemap crawl has no metadata, canonical, status, or internal-link defects.
- The three manually requested BackflowVerdict launch URLs are indexed as of 2026-08-11 and have begun receiving low-volume exploratory impressions.
- The first ranking pass is implemented locally and verified, but is not yet committed or deployed.
- BackflowPath is tracked as an owner-facing utility-first compliance product.
- The public site centers on utility, state, metro, guide, tester, failed-test, provider, and request-help pages.
- Lead capture remains file-backed and manually reviewed through `/admin`.
- Public provider browse remains secondary to the governing utility workflow.
- As of 2026-06-30, 80 utility pages, 5 state guides, 13 metro pages, and 10 evergreen guides are publishable.
- `/readyz` must stay `ready` as long as utility pages are explicitly published and not blocked, even when freshness warnings exist.

## Latest decisions
- Let `/backflow-reporting-portals` own generic portal-family lookup; keep `/backflow-reporting-portal/` narrowly focused on verifying an official filing route before data is submitted.
- Use the 74,700-impression / 3.75%-CTR model as a measurable portfolio requirement, not a traffic promise.
- Do not passively wait on low initial rankings or repeat indexing requests; improve query ownership and visible evidence now, then judge results on a 7-day cadence after deployment.
- Use 50 impressions per page or 14 elapsed days as the minimum signal before treating a new page's average position as stable enough for a major rewrite decision.
- Keep the plural repair-kit finder focused on model/size/part lookup, the singular repair-kit decision guide focused on scope selection, and the installation code page focused on authority/approval workflow.
- Keep the governing utility or water authority as the canonical entity.
- Keep official guidance and public provider directories visibly separate.
- Do not reintroduce private setup or internal routing surfaces into the published experience.
- Manual request review is allowed; private internal routing workflows are not part of the active product.
- Freshness, verification, source clarity, and stable crawlable URLs outrank conversion experiments.
- Freshness and broken-link warnings no longer remove explicitly published pages from public routes; missing source evidence or explicit conflict blocks still can.
- Verification must surface operational freshness and link-health failures; a registry with zero published utility or state-guide pages is `needs-review`, not `ok`.
- Automated source URL audits can refresh freshness dates only when unresolved URLs are also recorded in `data/ops/broken_links.csv` for follow-up suppression.

## What changed this session
- Added homepage links to the three priority national pages, tightened exact portal-family titles, and separated portal lookup from portal-verification intent.
- Crawled all sitemap pages and internal links, parsed priority JSON-LD, and visually checked desktop and mobile output in Chromium.
- Queried live GSC URL inspection, sitemap, page/query, period-comparison, striking-distance, and cannibalization data on 2026-08-11.
- Built and rendered a source-backed ranking diagnostic report for the three launch pages.
- Implemented the repair-kit, installation, and RPZ-comparison ranking interventions described above.
- Added controller and service contracts for structured data, visible search-intent sections, model-index links, and exclusive query ownership.
- Passed the full Gradle suite and all 6 browser contracts; visually checked repair-kit desktop/mobile plus installation and device-identification desktop pages in Chromium.
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
- Added a top-level `/submit-backflow-report` hub with primary/mobile/footer/home links, ItemList and FAQPage schema, city submission-route links, portal/utility handoffs, and `report_submission_route_click` analytics tracking.
- Raised procedure density across existing high-intent pages by adding `Notice-to-closeout map` blocks to city intent routes, `Procedure facts` to utility focus pages, `Failure closeout facts` to failed-test pages, and broader HowTo schema for pages with visible workflow steps.
- Strengthened pages that already have GSC impressions by rewriting utility detail title/meta formulas, adding `Fast compliance answer` blocks to utility pages, and turning provider profiles into booking-check pages with utility-specific notice, submission, proof, and failed-test context.
- Added `/sitemap-index.xml`, exposed it in `robots.txt`, and kept preview-host discovery files empty so GSC can use one stable sitemap index while the priority sitemap remains separately submit-ready.
- Added official-source hard facts from Aurora, Hillsborough County, Anaheim, Austin, Fort Worth, and Euless into `ops/public_source_fact_research_2026-07-09.md`; added SpryBackflow as a portal family, added utility-page `Report packet` / `Rejection risk` content, and deepened Aurora, Anaheim, Hillsborough, Austin, and Fort Worth records with submission, calibration, photo, TMR, permit, and inspection-failure details.
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
- Added page-level SEO/AEO metadata so indexed pages expose sitemap-aligned `article:modified_time`, image alt text, Open Graph locale, and accurate 512x512 social image dimensions from shared `PageMeta`.
- Strengthened AEO extraction by allowing large image previews on indexed pages, adding utility/city `Service` and source-backed answer-card JSON-LD, and replacing generic HowTo step names with action-specific step names.
- Reclassified ops broken-link health counting so fixed links are resolved, 403/timeout rows remain manual-review evidence, and only confirmed broken statuses count as unresolved source-link failures.
- Reworked core JSON-LD generation in `SiteController` from manual string concatenation to Jackson node builders for WebPage, Service, answer-card, FAQ, WebApplication, ItemList, HowTo, and Breadcrumb schema.
- Added `ops/daily_100_click_execution_2026-07-22.md` after the daily 100-click expert review. The implementation turned the admin Search Console bottleneck table into a weekly title/meta/H1 rewrite queue, exposed portal comparison rows on the main reporting-portal hub, added a portal lookup intent block, and moved utility hard facts such as notice/device ID, portal vendor/name, submitter, acceptance proof, and failed-test deadlines higher on utility pages.
- Extended the weekly SEO rewrite loop so `/admin/seo-scorecard.json` includes a `rewriteQueue` array and `/admin/seo-rewrite-queue.csv` exports the priority, URL, bottleneck, performance, title pattern, and concrete rewrite action for weekly execution.
- Added query-level GSC support through `app.ops.search-console-queries-path`, `SearchConsoleQueryPerformanceService`, `searchConsoleQueries`, `queryRewriteQueue`, and `/admin/seo-query-rewrite-queue.csv`, so Search Console query exports can drive title, H1, and meta rewrites by detected intent family.
- Extended the query-level rewrite queue with target route matching so city, utility, and intent queries point to the likely city task page, utility focus page, or fallback hub before title/H1/meta rewrites are performed.
- Added `deepFactQueue` plus `/admin/seo-deep-fact-queue.csv` so utilities missing report workflow, tester gate, deadline, failed-test, fee, portal, identifier, or acceptance-proof facts can be prioritized before creating more pSEO pages.
- Strengthened `/backflow-reporting-portals` as a lookup database by adding a portal family matrix for BSI, WEIRS, SwiftComply, VEPO, Aqua/TrackMyBackflow, Tokay, and SpryBackflow searches.

## Next recommended tasks
1. Review, commit, and deploy the 2026-08-11 ranking intervention, then confirm the production HTML, canonicals, structured data, and sitemap modified dates.
2. Recheck the three launch pages after 7 days and again at 50 impressions per page; compare query fit, top-20 entry, impressions, and clicks rather than average position alone.
3. If a page remains outside position 30 after 50 impressions or 14 days, run a second pass on SERP format, evidence depth, internal-link concentration, and external authority.
4. Measure whether the new portal ownership and exact-family titles reduce `envirotrax`, `trackmybackflow`, and `vepo backflow` URL splitting without weakening utility-first local routes.
5. Continue resolving confirmed 404 rows in `data/ops/broken_links.csv` before bot-protected 403/time-out rows.

## Open questions
- Whether exact portal-family title changes lift CTR at positions 5–10 without moving generic portal impressions away from the lookup hub.
- Whether the repair-kit finder needs additional model cohorts before the current ten model passports accumulate enough exact-query impressions.
- Whether installation demand separates into a national detail page and local installation pages after the next 14-day sample, or remains best consolidated in the current national decision page plus utility overlay.
- Whether legacy internal provider-status fields should be renamed in code and data files after the public cleanup is complete.
- How far metro and provider aggregation should expand before it begins to dilute utility-first trust.
- Whether request-help follow-up needs a more explicit public service-level expectation on the site.
- Whether 403/time-out source audit failures should be tracked separately from definite 404s so bot-protected official pages do not suppress otherwise valid pages after the grace window.
# 2026-08-02 search-salvage product reset

- Rebuilt the primary product around identify → diagnose → decide → official utility verification.
- Added nine JSON-backed national decision guides and a fail-fast `DecisionGuideService`.
- Added dedicated decision controller, JTE layout, technical SVG device diagrams, and restrained field-manual design system.
- Rebuilt homepage, utility record, submit-report finder, and noindex help handoff without removing existing utility/provider/city URLs.
- Added current GSC winner URL contract coverage and kept the official utility data layer separate from commercial handoff.
- The target remains search acquisition first; lead economics are measured later against qualified traffic.

## 2026-08-02 release-readiness strengthening

- Limited search launch to 12 indexable Wave A decision screens; 15 additional screens remain accessible but `noindex,follow` and absent from sitemap.
- Added the data-prioritized `/backflow-preventer-installation/` decision screen and an explicit `data/decision-query-ownership.csv` canonical-owner ledger.
- Replaced unsupported national dollar ranges with a quote-scope comparison tool.
- Added selected-utility overlays with official cadence, due basis, verification date, canonical utility record, and direct official source.
- Moved decision evidence into a claim-ID and freshness-controlled ledger checked against unresolved broken-link rows.
- Expanded protection from 12 sample routes to all 107 latest GSC `KEEP_WINNER` URLs.
- Added fail-fast registry duplicates/references and a 96-utility identity baseline.
- Changed deployment to immutable SHA images with pre-copy data snapshots, rollback, and route/canonical/robots/sitemap smoke checks.
- Added six Playwright Chromium contracts. Java/Spring: 82 passing; browser: 6 passing; npm audit: 0 vulnerabilities.
- No deployment was performed.
