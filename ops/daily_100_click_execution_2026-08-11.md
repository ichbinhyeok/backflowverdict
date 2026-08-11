# Daily 100 Organic Click Execution Model — 2026-08-11

## Decision

The target is 100 organic clicks per day, measured as a rolling 28-day average in Google Search Console. This is a portfolio target, not a ranking promise. The current intervention concentrates crawl equity and query ownership on useful national decision pages while preserving source-backed local winners.

## Current baseline

| Metric | Latest 28 days | Previous 28 days | Change |
|---|---:|---:|---:|
| Clicks | 116 | 28 | +314% |
| Impressions | 7,465 | 2,978 | +151% |
| CTR | 1.5539% | 0.94% | +0.61 percentage points |
| Average position | 14.12 | 12.33 | -1.79 positions |
| Clicks per day | 4.14 | 1.00 | +3.14 |

The lower average position is consistent with new query coverage entering at low rank while clicks and impressions grow. The target requires 2,800 clicks per 28 days. The remaining gap is 2,684 clicks per 28 days, or 24.1 times the current daily click rate.

## 100-click portfolio model

The operating model is `impressions × organic CTR = clicks`. A target mix requires roughly 74,700 impressions per 28 days at a 3.75% weighted CTR:

| Query cluster | Primary page portfolio | 28-day impressions | Target CTR | 28-day clicks |
|---|---|---:|---:|---:|
| Device identification and comparison | `/backflow-preventer/`, RPZ/PVB/DCVA, vacuum breaker, diagrams | 27,000 | 4.0% | 1,080 |
| Failure, leak, repair, and replace | leaking, failed test, repair/rebuild/replacement decision pages | 20,000 | 4.0% | 800 |
| Parts, repair kits, and installation | repair-kit index, model passports, installation/detail/cost pages | 15,000 | 4.0% | 600 |
| Testing, notice, and report workflow | test, notice, portal verification, report submission | 7,700 | 3.5% | 270 |
| Source-backed local tasks | city/utility annual, portal, approved tester, failed-test routes | 5,000 | 1.0% | 50 |
| **Total** | **National decision + local verification portfolio** | **74,700** | **3.75% weighted** | **2,800** |

This requires about 10 times the current impression base and 2.4 times the current CTR. Rank distribution must move materially: the majority of target impressions need to come from positions 1–10, with the highest-volume national decision pages reaching positions 1–5. More indexed URLs alone will not close the gap.

## Query ownership and cannibalization decisions

| Intent | Owner | Supporting pages | Boundary |
|---|---|---|---|
| Repair kits by model and size | `/backflow-preventer-repair-kits/` | model passports, singular repair-kit decision page | Index owns model/size/part lookup; singular guide owns repair-scope selection. |
| Backflow preventer installation requirements/detail | `/backflow-preventer-installation/` | installation cost, RPZ installation | Device, placement, access, drainage, and closeout details. |
| Installation code and approval workflow | `/backflow-installation-requirements/` | local utility records | Governing authority, adopted code, plan review, inspection, and approval responsibility. |
| RPZ versus the broader category | `/backflow-preventer/` | `/rpz-backflow-preventer/`, RPZ comparisons | Category answer; model/device pages support identification. |
| Generic portal-family lookup | `/backflow-reporting-portals` | vendor-family hubs and local submission routes | Lookup database for portal name, city, and utility. |
| Verify an official portal before filing | `/backflow-reporting-portal/` | notice and report-submission workflows | Verification procedure only; it does not compete for the generic portal index query. |
| Local annual testing | city annual-testing route when supported | utility annual page and city submit page | City route owns city-led query; utility page supplies governing evidence; submit page owns filing intent. |

Observed GSC examples support these boundaries: `annual backflow testing fort worth` was split across city annual, city submit, and utility annual URLs; `trackmybackflow` was split between the portal-family hub and local submit routes. Internal anchors and titles should state the boundary rather than repeat the same head term on every page.

## Implemented in this pass

- Strengthened the repair-kit finder with model, size, part-reference, FAQ, ItemList, CollectionPage, and breadcrumb evidence.
- Strengthened installation and RPZ-category pages with explicit intent answers and comparison links.
- Added homepage links to the repair-kit finder, installation decision page, and RPZ/category answer.
- Retargeted the singular portal guide from generic portal lookup to official-portal verification and linked it to the plural lookup hub.
- Rewrote TrackMyBackflow, Tokay WebTest, SpryBackflow, SwiftComply, and VEPO/Envirotrax titles around the exact portal plus city/utility lookup task.
- Preserved all winner URL contracts, canonical paths, local sources, and the user-owned `analysis/` directory.

## Measurement gates

These are validation ranges, not forecasts or guarantees.

| Gate | Leading indicators | Continue / change rule |
|---|---|---|
| 30 days | 12k–18k impressions/28d; 1.8%–2.2% CTR; 8–14 clicks/day; each new priority page reaches 50 impressions or has had 14 indexed days | Keep intent and snippet if impressions and top-20 queries grow. Rewrite only pages with enough signal and clear mismatch. |
| 60 days | 28k–45k impressions/28d; 2.3%–3.0% CTR; 23–48 clicks/day; priority clusters increasingly in positions 4–10 | Consolidate recurring query splits, deepen pages that reach positions 6–15, and refresh weak snippets with page-level evidence. |
| 90 days | 60k–90k impressions/28d; 3.2%–4.2% CTR; 69–135 clicks/day; multiple national clusters in top 5 | Validate 100/day only from the rolling GSC average. If impressions remain under 30k, authority and distribution—not more pages—are the primary constraint. |

Weekly review should record page, query, clicks, impressions, CTR, position, query owner, and action. Use a minimum of 50 impressions or 14 indexed days before a major rewrite unless the page has an objective technical or intent defect.

## External dependencies

- Qualified reviewer validation for any field-installation detail that could be interpreted as code or trade instruction.
- Original device/nameplate and installation-context photos where rights and privacy are clear.
- Normal editorial outreach to utilities, tester organizations, property-management resources, and relevant trade references; no paid-link or templated mass outreach.
- Continued GSC page/query exports and URL inspection after deployment.
