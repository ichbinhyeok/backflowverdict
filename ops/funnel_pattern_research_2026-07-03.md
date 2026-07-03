# Backflow Funnel Pattern Research - 2026-07-03

## Funnel Goal
Move search users from a vague query such as "backflow testing near me" or "BSI backflow notice" into the right BackflowPath route:

1. Notice or city search
2. Utility workflow
3. Tester eligibility
4. Portal/report submission
5. Cost, repair, retest, or failed-test follow-up

## Strong Patterns Found

### 1. Notice-first pages beat generic education
The best official and commercial examples answer the user's real starting point: "I got a letter or notice, what now?"

Examples:
- Dallas explains the SwiftComply transition, mandatory participation, tester registration, and online submission path.
- Tampa frames the workflow around notices, SwiftComply enrollment, and timing.
- NYC explains annual notification letters, annual testing, and consequences for not completing the test.
- City Certified Backflow leads with "Got a backflow notice?" and promises test, repair if needed, filing, copy, invoice, and annual reminders.

Action copied:
- Added `/guides/backflow-test-notice-next-steps`.
- Added a notice funnel section that routes users to due date, portal, tester, failed-test, cost, or utility pages.

### 2. Portal pages need credential and fee detail
Winning portal pages do not merely say "submit online." They mention the named system, tester account setup, credential acceptance, filing fee, and pass/fail handling.

Examples:
- Queen Creek states BSI Online filing and per-report fee details.
- Cedar Park states BSI Online submission and filing fee.
- Johnstown states the BSI launch date and fee.
- Fort Worth, Marble Falls, Mansfield, Taylor, Buda, Seguin, and Irving expose VEPO/Envirotrax as the tester submission system.
- Longmont announces a future SwiftComply switch and pass-test fee.

Action copied:
- Kept the BSI/SwiftComply/WEIRS hub pattern.
- Added portal owner/tester checklist language.
- Added the notice guide to portal hub related guides.

### 3. Commercial winners sell the complete chain
Provider pages that look strong do not only say "we test backflow." They promise scheduling, certification, report filing, annual reminders, repairs, local city coverage, and proof.

Examples:
- Pittsburgh Backflow Testing leads with flat pricing, electronic report submission, annual reminders, and online scheduling.
- City Certified Backflow leads with water-district filing, annual reminders, certifications, Google reviews, and a three-step process.
- David's Plumbing lists location pages and says the annual certification report is filed with the water district.
- H2O & Backflow Tests emphasizes TCEQ licensing, BSI/VEPO registration, fire line work, pricing, and service-area coverage.
- A Plus Backflow uses city-specific copy and says only utility-registered certified testers can submit portal reports.

Action copied without overclaiming:
- BackflowPath now exposes owner/tester responsibility, not provider promises.
- Utility and city pages show test, tester, portal, and fee path before generic provider discovery.
- Metro pages now group high-intent paths and city route links.

### 4. Failed-test flow is a separate intent
Official examples frequently separate failed tests from passed tests. Some require faster notice or different fee handling.

Examples:
- Hillsborough County requires county notification within 48 hours of a failed assembly test.
- Manatee County says failed test reports must be entered no later than one business day.
- Irving says there is no fee for failed test reports.
- Wilsonville says repair/replacement and retest should happen within 30 days.

Action copied:
- The notice guide explicitly routes failed tests to the failed-test guide.
- Utility/city pages already include failed-test routes and failure highlights.

## Implemented Files
- `data/guides/backflow-test-notice-next-steps.json`
- `src/main/jte/pages/guide-page.jte`
- `src/main/java/owner/backflow/web/SiteController.java`
- `ops/gsc_priority_reindex_urls_2026-07-03.txt`

## Source URLs
- https://dallascityhall.com/departments/waterutilities/Pages/Backflow-Test-Reports.aspx
- https://www.tampa.gov/water/water-quality/backflow-testing
- https://www.queencreekaz.gov/government/utilities/water/backflow-information
- https://hcfl.gov/residents/property-owners-and-renters/water-and-sewer/backflow-and-cross-connection-service-testing
- https://www.fortworthtexas.gov/departments/water/backflow
- https://www.pittsburghbackflowtesting.com/
- https://www.citycertifiedbackflowprevention.com/
- https://www.davidsplumbingmadera.com/backflow-testing
- https://www.h2obackflowtests.com/
- https://aplusbackflow.co/north-port-fl/
