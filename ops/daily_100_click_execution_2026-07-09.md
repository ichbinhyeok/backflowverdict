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

## Next Pressure Point

This improves the probability of clicks from already visible queries, but daily 100 clicks still needs more than code:

- Manual GSC URL inspection/request indexing for the new submit-report routes.
- More exact utility facts: fees, deadlines, notice IDs, accepted-report proof, rejected-report reasons.
- External links or citations into the portal/tester-list assets.
- A stronger notice-first conversion path: paste notice details, identify portal, then request help.
