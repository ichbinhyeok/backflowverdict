# Daily 100-click execution pass - 2026-07-22

## Goal

Move the site closer to the daily 100 organic-click target without creating thin pSEO pages. The expert review concluded that the next bottleneck is not generic schema volume; it is query-fit, source-backed hard facts, portal/task-page clarity, and a weekly Search Console rewrite loop.

## Baseline used

- Prior Search Console audit baseline: 17 clicks, 2,031 impressions, 0.84% CTR, average position 12.0 across the latest 28-day window reviewed in early July.
- Daily 100 clicks means roughly 3,000 organic clicks per month.
- At 2% CTR, the site needs about 150,000 monthly impressions.
- At 3% CTR, the site needs about 100,000 monthly impressions.
- At 5% CTR, the site still needs about 60,000 monthly impressions.

## Changes made

1. Turned the admin Search Console bottleneck table into a rewrite queue.
   - `SearchConsolePageMetric` now exposes a priority label, suggested title pattern, and specific rewrite action.
   - `/admin` now labels the table as the weekly title/meta/H1 rewrite queue.
   - `/admin/seo-scorecard.json` now includes a `rewriteQueue` array for downstream tooling.
   - `/admin/seo-rewrite-queue.csv` exports the weekly rewrite queue with priority, URL, bottleneck, clicks, impressions, CTR, position, suggested title pattern, and action.
   - `/admin/seo-query-rewrite-queue.csv` exports query-level title/H1/meta rewrites from `storage/search-console/queries.csv`, including a target path and label when the query can be matched to a city task page, utility focus page, or fallback hub.
   - `/admin/seo-deep-fact-queue.csv` exports the utilities most in need of official-source fact expansion before more pSEO pages are created.
   - CTR bottlenecks direct title/meta/H1 rewrites.
   - Ranking bottlenecks direct source-backed fact expansion.
   - Discovery bottlenecks direct contextual internal links and indexing checks.

2. Made `/backflow-reporting-portals` behave more like a lookup/comparison asset.
   - Added a `Portal lookup intent` block for owner, tester, failed-test, and AI-answer-source use cases.
   - Added a `Portal lookup database` family matrix for BSI, WEIRS, SwiftComply, VEPO, Aqua/TrackMyBackflow, Tokay, and SpryBackflow.
   - Exposed the portal comparison table on the overview hub, not only on individual portal-family pages.
   - The table keeps portal evidence, notice/device ID, tester gate, report acceptance, timing/fee clue, and failed-test clue together in one extractable block.

3. Moved utility hard facts higher on utility pages.
   - Added the notice/device ID clue to the fast compliance answer.
   - Made failed-test deadline labels visible when structured failed-test policy exists.
   - Added portal vendor, portal name, accepted submitter, and acceptance proof to the report packet block.
   - Added tester credential summary to the rejection-risk block.

## Why this matters

The practical traffic path is:

1. Increase impressions with stronger portal and city-task coverage.
2. Increase CTR by making high-impression pages look like task answers in the SERP.
3. Improve AEO extraction by putting answer-ready hard facts near the top of the page.
4. Avoid thin page expansion unless a utility/city page has at least three unique facts.

## Validation

- `.\gradlew.bat test --tests owner.backflow.web.SiteControllerTest --tests owner.backflow.web.AdminControllerTest` passed.
- `.\gradlew.bat test --tests owner.backflow.web.AdminControllerTest` passed after adding rewrite queue export coverage.
- `.\gradlew.bat test --tests owner.backflow.ops.SearchConsoleQueryPerformanceServiceTest --tests owner.backflow.web.AdminControllerTest --tests owner.backflow.web.SiteControllerTest` passed after adding query-level GSC and deep fact queues.
- `.\gradlew.bat test --tests owner.backflow.web.AdminControllerTest --tests owner.backflow.ops.SearchConsoleQueryPerformanceServiceTest` passed after adding query-to-target-route matching.

## Next pressure point

The next non-code bottleneck is still authority and fresh GSC input:

- Upload the latest Search Console page/query exports.
- Run the admin rewrite queue weekly.
- Add hard facts to more utility JSON records: fee, deadline, portal, tester gate, failed-test branch, rejection reason.
- Build external links or mentions into the portal/tester-list assets.
