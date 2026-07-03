# Backflow SEO Winning Pattern Research - 2026-07-03

## Objective
Raise BackflowPath from 17 clicks in the latest 28-day GSC window toward 100 monthly clicks by copying search patterns already visible on strong official and commercial backflow pages.

## Pattern Sources Reviewed

| Source | Pattern worth copying | URL |
| --- | --- | --- |
| Holly Hill, FL | BSI Online, annual test reports, filing fee, CCN/notice number, FAQ by customer action | https://www.hollyhillfl.org/publicworks/page/cross-connection-control-program-online-backflow-testing-reporting |
| SwiftComply tester page | Account setup, tester approval, required certifications, test-kit/gauge document updates, online report history | https://www.swiftcomply.com/backflow-testers/ |
| Dallas Water Utilities | Mandatory SwiftComply, registered tester requirement, passed-test fee, required certification and test-kit information | https://dallascityhall.com/departments/waterutilities/Pages/Backflow-Test-Reports.aspx |
| Tampa Water Department | Notice-driven workflow, 30-day scheduling window, seven-day submission window, registered tester list, residential vs commercial cadence | https://www.tampa.gov/water/water-quality/backflow-testing |
| Queen Creek, AZ | BSI filing fee, credential rejection risk, list/quote guidance, accepted-test requirement | https://www.queencreekaz.gov/government/utilities/water/backflow-information |
| Fort Worth, TX | Registered BPAT, VEPO submission, incomplete reports causing failed inspection, installation/replacement/removal handling | https://www.fortworthtexas.gov/departments/water/backflow |
| Accuracy Backflow | Local service-area city list, "testing and repairs", and certificate filing promise | https://www.accuracybackflow.com/ |
| Backflow Paradise | Phoenix-specific compliance language and FAQ format around annual testing, penalties, repairs, and commercial/job-site use cases | https://www.backflowparadise.com/ |

## Replicated Patterns

### 1. Put the commercial search terms above the fold
Winning pages do not wait until the bottom to mention annual testing, certified testers, portals, fees, and filing. BackflowPath utility pages now expose:
- Testing cadence
- Submission type
- Due basis
- Tester route
- Cost signal
- Portal hub link when available

Implemented in:
- `src/main/jte/pages/utility-page.jte`
- `src/main/java/owner/backflow/web/SiteController.java`

### 2. Split owner and tester responsibilities
Official pages repeatedly separate the owner notice from the tester submission job. BackflowPath now mirrors that with owner/tester blocks on utility and city pages.

Implemented in:
- `src/main/jte/pages/utility-page.jte`
- `src/main/jte/pages/city-bridge.jte`

### 3. Treat portal pages as money pages, not thin hubs
BSI, SwiftComply, VEPO, and similar pages win because users search for the reporting system. BackflowPath portal hubs now include:
- Owner checklist
- Tester checklist
- Credentials and gauge-calibration reminders
- Passed/failed/replacement handling language
- City route links where available

Implemented in:
- `src/main/jte/pages/portal-hub.jte`

### 4. Replicate local city-list coverage
Commercial winners list every served city. BackflowPath copies the internal-link benefit without pretending to be a generic provider page: city links now map back to the exact governing utility workflow.

Implemented in:
- `src/main/jte/pages/metro-page.jte`
- `src/main/jte/pages/utility-page.jte`
- `src/main/java/owner/backflow/web/SiteController.java`

### 5. Add FAQ questions that match real searches
Utility FAQ now adds:
- Which portal does this utility use?
- What should I check before scheduling?
- What costs or portal fees should I expect?

Implemented in:
- `src/main/java/owner/backflow/web/SiteController.java`

## Verification
- `.\gradlew.bat test` passes.
- Local server on `http://localhost:8096` renders the new utility, city, portal, and metro pattern blocks.
- Checked representative pages:
  - `/utilities/florida/tampa-water-department/`
  - `/cities/texas/dallas/backflow-testing`
  - `/backflow-reporting-portals/bsi`
  - `/metros/texas/dallas-fort-worth-metroplex/backflow-testing`

## Expected Impact
This does not guarantee 100 clicks by itself. It improves the odds by matching the exact intent already showing in GSC and public search results:
- Better CTR on pages already near page one.
- More internal links into city routes.
- Stronger topical matching for BSI, SwiftComply, portal fee, approved tester, and annual testing searches.
- More FAQ schema coverage for long-tail queries.

The next required step is deployment, sitemap submission, and manual GSC indexing for the 50 priority URLs in `ops/gsc_priority_reindex_urls_2026-07-03.txt`.
