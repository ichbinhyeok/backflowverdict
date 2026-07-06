# Daily 100 Click Expansion - 2026-07-03

## Goal
Move from the earlier monthly-click framing to a daily-click target: 100 organic clicks per day.

The current GSC baseline was 17 clicks in 28 days, so daily 100 requires a step-change in indexed search surface, topical clarity, and ongoing GSC iteration.

## Code Expansion Added

### City intent pages
BackflowPath now creates city-level intent pages only when the mapped utility has enough evidence for that intent:

- `/cities/{state}/{city}/annual-backflow-testing`
- `/cities/{state}/{city}/backflow-reporting-portal`
- `/cities/{state}/{city}/approved-backflow-testers`
- `/cities/{state}/{city}/failed-backflow-test`
- `/cities/{state}/{city}/irrigation-backflow-testing`
- `/cities/{state}/{city}/fire-line-backflow-testing`

Unsupported city-intent combinations return 404. Noindex bridge city aliases are excluded from sitemap expansion.

### Why this helps
The previous city page covered many intents in one route. Daily 100 needs separate, high-intent crawlable surfaces for the searches people actually make:

- city + annual backflow testing
- city + backflow reporting portal
- city + BSI / SwiftComply / VEPO
- city + approved backflow testers
- city + failed backflow test
- city + irrigation backflow testing
- city + fire line backflow testing

### Sitemap result
Local sitemap validation showed 970 URLs after the expansion.

### Source-backed portal family expansion
After reviewing actual portal and utility copy, VEPO/Envirotrax was promoted from incidental copy to a first-class portal family. This is grounded in Fort Worth's stored official utility record and live public examples where VEPO/Envirotrax controls BPAT registration, credential verification, approved-list visibility, and online report submission.

Added surfaces:

- `/backflow-reporting-portals/vepo`
- `/cities/texas/fort-worth/backflow-reporting-portal`
- Fort Worth city-intent crawl paths for annual and fire-line searches

### Added VEPO city cluster
Added 7 more official-source Texas utilities where public pages name VEPO, Envirotrax, online tester submission, registered BPAT search, or credential verification:

- Southlake
- Bedford
- Mansfield
- Cleburne
- Marble Falls
- Taylor
- Buda

These records create new utility pages, city pages, city-intent pages, portal hub entries, approved-tester routes, and metro links without creating unsupported combinations. The GSC priority list now favors the highest-intent new routes such as `city + backflow reporting portal`, `city + annual backflow testing`, and `city + approved backflow testers`.

### Added BSI and Envirotrax comparison cluster
Added 4 more official-source Texas utilities where public pages expose high-intent portal details:

- League City: BSI, CCN, 48-hour submission, device registration fee, enforcement risk.
- Irving: Envirotrax, 10-day submission, failed-test customer notice, permit verification.
- Baytown: Envirotrax online test reports and CSI report submission.
- Liberty City WSC: VEPO records and annual testing for septic plus irrigation properties.

The portal hub now includes a comparison table so BSI, SwiftComply, WEIRS, and VEPO pages behave more like decision tools instead of thin lists.

## Quality Guardrails
- Intent routes are generated from existing utility evidence.
- Routes are not created for unsupported utility conditions.
- Official source trails remain visible.
- Provider discovery remains secondary to utility authority.
- City routes still link back to canonical utility workflows.

## Verification
- `.\gradlew.bat test` passed.
- Local server on `http://localhost:8098` verified representative city-intent pages and sitemap output.
- Representative checks:
  - `/cities/texas/dallas/backflow-reporting-portal`
  - `/cities/arizona/queen-creek/approved-backflow-testers`
  - `/cities/texas/dallas/approved-backflow-testers` correctly returns 404 because Dallas has no official approved-tester route in the registry.

## Remaining Growth Needed For Daily 100
This materially increases the indexed surface, but daily 100 likely still requires:

- Expanding utility data beyond the current 91 utilities.
- Continuing to add more portal families and utilities only when the source text names the workflow.
- Building county and metro intent routes where authority mapping is strong.
- Adding real provider inventory where official tester/source support exists.
- Weekly GSC rewrites for high-impression, zero-click URLs.
- External citations or links from provider, utility-adjacent, or local directory sources.
