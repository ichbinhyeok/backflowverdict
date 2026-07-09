# Daily 100 Click Execution - 2026-07-09

## Current GSC Signal

Reviewed the latest available GSC window on 2026-07-09. GSC data still lags through 2026-07-06.

Top query signals:

- `swiftcomply portal`: 14 impressions, 1 click, average position 8.86.
- `austin weirs`: 3 impressions, 0 clicks, average position 8.33.
- `annual backflow testing fort worth`: 4 impressions, 0 clicks, average position 65.5.
- `backflow inspection`: 6 impressions, 0 clicks, average position 34.33.
- `backflow preventer aurora co`: 6 impressions, 0 clicks, average position 17.5.
- Anaheim, Avondale, Goodyear, and Hillsborough utility/city pages are showing early local impressions.

Top page signals:

- Aurora utility page: 47 impressions, average position 7.17.
- Hillsborough utility page: 33 impressions, 2 clicks, average position 8.36.
- Anaheim utility page: 29 impressions, average position 8.97.
- SwiftComply portal page: 1 impression, 1 click, average position 9.

## Work Completed

1. Added source-gated `submit-backflow-report` city intent pages.
   - Generated only when the mapped utility has a portal or online submission workflow.
   - Targets deeper long-tail intent such as `Austin WEIRS submit test report`, `Dallas SwiftComply report submission`, and `Fort Worth VEPO backflow report`.
   - Added FAQ schema and visible city-specific FAQ answers for the submission path.

2. Added `/notice-finder` WebApplication structured data.
   - Keeps the notice finder from looking like a thin content page.
   - Declares feature list and SearchAction for city, utility, portal, notice ID, tester, due-date, and failed-test matching.

3. Added contextual guide links into priority local routes.
   - Guides now link directly into Austin WEIRS, Dallas SwiftComply, Fort Worth annual/report submission, Aurora, Anaheim, and Hillsborough routes.
   - This follows the Reddit pSEO pattern that surviving generated pages need editorial in-body links, not only sitemap discovery.

4. Expanded `/sitemap-priority.xml`.
   - Added submit-report pages for Austin, Dallas, Fort Worth, Euless, Oxnard, Irving, Tampa, and Aurora.
   - Added high-signal utility/city pages for Aurora, Anaheim, Goodyear, and Hillsborough.

5. Deepened the submit-report pages and portal hubs.
   - Added visible "Submission packet" blocks to `submit-backflow-report` pages so they read like actual procedure pages, not thin city variants.
   - Added HowTo structured data to `submit-backflow-report` pages.
   - Added ItemList structured data to portal hubs so Google sees the utility and city report-submission route collection.
   - Added visible portal-hub links directly into the city submit-report routes.

6. Added a top-level `/submit-backflow-report` hub.
   - Targets the broader `submit backflow report`, `upload backflow test`, `file backflow report`, and portal-specific report-submission intent that does not always name a city first.
   - Links into source-gated city submission routes, utility workflows, portal hubs, notice-finder searches, and related guides.
   - Added BreadcrumbList, ItemList, and FAQPage structured data so the route reads as a submission-path directory rather than a generic article.
   - Added primary navigation, mobile navigation, footer, and home-card links plus `report_submission_route_click` tracking.

7. Raised the procedure-density lever on existing high-intent pages.
   - Added a `Notice-to-closeout map` to city intent pages so annual, portal, submit-report, approved-tester, failed-test, irrigation, and fire-line routes expose authority, notice trigger, tester gate, submission route, proof to keep, and failed-test branch.
   - Added `Procedure facts` to utility focus pages so annual, irrigation, and fire-line pages show the same source-backed closeout logic above the workflow.
   - Added `Failure closeout facts` to failed-test utility pages so repair, retest, accepted submission, proof, cost exposure, and source trail are visible before provider routing.
   - Expanded HowTo structured data from submit-report city pages to all city intent pages with workflow steps, plus utility focus and failed-test pages.

8. Applied the GSC/Reddit CTR pattern to pages already earning impressions.
   - Rewrote utility detail title/meta generation around exact click jobs: official testers, named portal reports, due dates, report steps, and failed-test handling.
   - Added a top-of-page `Fast compliance answer` block to utility detail pages so the user immediately sees deadline trigger, tester gate, report route, proof to keep, failed-test branch, and source trail.
   - Upgraded provider pages from static public profiles into booking-decision pages with call/website/help CTAs, primary-utility booking checks, and utility-card proof/submission details.
   - This targets the current GSC pattern where Aurora, Hillsborough, Anaheim, and provider pages have impressions or early clicks but still need stronger CTR and action confidence.

9. Added a sitemap index for discovery hygiene.
   - Added `/sitemap-index.xml` with standard `<sitemapindex>` output for `/sitemap.xml` and `/sitemap-priority.xml`.
   - Exposed the sitemap index in `robots.txt` while keeping preview hosts empty/no-discovery.
   - This gives Search Console one stable sitemap entry point while preserving the priority sitemap for high-intent URL resubmission.

10. Added public-source hard facts to the highest-pressure portal workflows.
   - Added SpryBackflow as a first-class portal family after checking Aurora and existing Greeley source data.
   - Deepened Aurora, Anaheim, Hillsborough, Austin, and Fort Worth records with actual closeout blockers: SpryBackflow due-date submission, SwiftComply assembly photo, Backflow BMP plus test-kit calibration records, WEIRS TMR packet fields, gauge serial/calibration, and VEPO inspection-failure risk.
   - Added a `Report packet` / `Rejection risk` block to utility pages so existing indexed utility pages now expose the exact evidence needed before filing is treated as complete.
   - Added `/backflow-reporting-portals/sprybackflow` and Aurora reporting-portal paths into priority discovery.
   - Documented the source review in `ops/public_source_fact_research_2026-07-09.md`.

## Verification

- `.\gradlew.bat test --rerun-tasks`: passed.
- Local HTTP checks on `http://localhost:8105`: passed.
  - `/cities/texas/austin/submit-backflow-report`
  - `/cities/texas/dallas/submit-backflow-report`
  - `/cities/colorado/aurora/submit-backflow-report`
  - `/guides/backflow-reporting-portals`
  - `/notice-finder`
  - `/sitemap-priority.xml`
- Additional local HTTP checks on `http://localhost:8106`: passed.
  - `/cities/texas/dallas/submit-backflow-report` includes HowTo schema and Submission packet content.
  - `/backflow-reporting-portals` includes ItemList schema and city submit-report links.
  - `/backflow-reporting-portals/swiftcomply` includes ItemList schema and Dallas submit-report links.
- Latest local HTTP checks on `http://localhost:8107`: passed.
  - `/submit-backflow-report` returns 200 and includes ItemList, FAQPage, Austin, and Dallas city submission links.
  - `/sitemap.xml` and `/sitemap-priority.xml` include `https://backflowpath.com/submit-backflow-report`.
- Latest procedure-density tests:
  - City intent pages include `Notice-to-closeout map` and HowTo schema.
  - Utility focus pages include `Procedure facts` and HowTo schema.
  - Failed-test utility pages include `Failure closeout facts` and HowTo schema.
- Latest local HTTP checks on `http://localhost:8108`: passed.
  - `/cities/texas/dallas/annual-backflow-testing`
  - `/cities/texas/dallas/backflow-reporting-portal`
  - `/utilities/texas/dallas-water-utilities/annual-testing`
  - `/utilities/texas/fort-worth-water-utilities/failed-test`
- Latest CTR/provider upgrade checks on `http://localhost:8109`: passed.
  - `/utilities/colorado/aurora-water/`
  - `/utilities/florida/hillsborough-county-backflow-testing/`
  - `/utilities/california/anaheim-cross-connection-control/`
  - `/providers/prescott-arizona-backflow-care/`
  - `/providers/phoenix-western-backflow/`
- Latest public-source hard-fact local HTTP checks on `http://localhost:8111`: passed.
  - `/backflow-reporting-portals/sprybackflow`
  - `/notice-finder?q=Aurora%20SpryBackflow`
  - `/utilities/colorado/aurora-water/`
  - `/utilities/california/anaheim-cross-connection-control/`
  - `/utilities/florida/hillsborough-county-backflow-testing/`
  - `/utilities/texas/austin-water-utilities/`
  - `/utilities/texas/fort-worth-water-utilities/`
  - `/sitemap-priority.xml`

## Next Pressure Point

This improves the probability of clicks from already visible queries, but daily 100 clicks still needs more than code:

- Manual GSC URL inspection/request indexing for the new submit-report routes.
- More exact utility facts: fees, deadlines, notice IDs, accepted-report proof, rejected-report reasons.
- External links or citations into the portal/tester-list assets.
- A stronger notice-first conversion path: paste notice details, identify portal, then request help.
