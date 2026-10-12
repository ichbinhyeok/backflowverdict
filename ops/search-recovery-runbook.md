# Search recovery operating plan

Scope: strengthen the eight URLs in `search-recovery-experiment.json`, preserve local winners and official-first routing, and test purchase-adjacent intent before further page expansion. Prepared 2026-10-12; the experiment clock starts only after production verification. These are decision thresholds, not promised traffic or revenue.

Production verified on 2026-10-12 at runtime commit `47d3942bf3afbc3cc7a31cde9a4b4d77cfcb9faa`, [successful deployment](https://github.com/ichbinhyeok/backflowverdict/actions/runs/38231440081). Full suite: 92 server tests and 11 Chromium contracts. All eight public URLs returned 200 with one H1, matching canonical, indexable robots and the new analytics asset; model schema is TechArticle/Thing. The live 975XL size/scope selection and manufacturer/merchant event separation passed, with no mobile overflow or page JavaScript errors. No production inquiry was submitted as a test. Public `/healthz` is restricted; internal deployment health passed and public `/readyz` returned ready.

GSC Wizard group `Recovery focus — 8 existing URLs` is saved with eight exact-match rules (ID in the manifest), and the release annotation is saved. Both live sitemaps were resubmitted and accepted on 2026-10-12; downloading/indexing remains Google's work. Main sitemap: 1,323 URLs including all eight focus URLs. Priority sitemap: 447 URLs including the five focus decision guides; models and the kit hub remain discoverable through the main sitemap and internal links. Day 60 is 2026-12-11; day 90 is 2027-01-10. Reviews use the latest settled 28 days at those checkpoints.

## Measurement

Both public layouts use `/analytics.js`. Query strings, contact details, free-text notes, and routing tokens are excluded from custom click/form parameters. GA4 page location and referrer omit query strings and fragments. Enhanced measurement can independently collect events; do not add its generic outbound clicks to these mutually classified custom counts.

| Event | Meaning | Revenue interpretation |
| --- | --- | --- |
| `manufacturer_source_click` | Opens manufacturer evidence | Reference use |
| `official_source_click`, `official_route_click` | Opens official/local workflow | Compliance navigation |
| `tester_route_click` | Opens explicitly official or directory route | Discovery, not a purchased lead |
| `provider_website_click` | Explicit provider link or existing provider redirect | Provider intent |
| `kit_match_result` | User selects a size in the kit tool | Product intent, not a successful repair |
| `merchant_outbound_click` | Opens a separately labeled retailer listing | Purchase intent, not a purchase |
| `lead_form_attempt` | Browser submits the form | Attempt only |
| `lead_form_success` | Server saved the request and returned a one-use success flag | Saved inquiry, not accepted/paid business |

Register `content_group`, `model_slug`, `guide_slug`, `destination_host`, `part_number`, and `size_band` as event-scoped GA4 custom dimensions if needed. Mark `lead_form_success` as a key event after DebugView verification. Leave reference clicks and attempts unmarked. GA4 collection still depends on the visitor permitting scripts/network access; backend file records are the inquiry source of truth.

As of 2026-10-12 the OAuth connection can list GA4 property `properties/531520200` (backflow), but GSC Wizard has no property linked to `sc-domain:backflowpath.com`. Its overview returns `notConfigured: no_property`. The exposed tools have no GA4 binding mutation. In [GSC Wizard](https://tool.gscwizard.com), open this property's Analytics page and select that property. Do not create another stream or change measurement ID to resolve this binding. An unauthenticated browser would require a new sign-in/terms step; none was performed.

## Weekly review

1. Check HTTP, canonical, robots, source links, saved-inquiry spam and GA4 collection after deployment. Use actual production behavior before marking deployment complete.
2. Export settled GSC web data: 28 daily rows and a page breakdown for exactly the same window/filters. Preserve the whole-site total independently of page totals. Compare equal periods and annotate seasonality.
3. Run `python scripts/recovery_scorecard.py --dates dates.csv --pages pages.csv --as-of YYYY-MM-DD`. Do not multiply a keyword bucket by an assumed rank/CTR to claim an outcome.
4. Review the eight URLs as a cohort plus individual pages with at least 50 impressions or 14 days. Preserve local URLs, titles that already work, and official routes.
5. Compare product-intent events and server-saved requests. Manually record the number of relevant accepted requests, actual partner terms, collected dollars, refunds, and operating time in private storage. Never put lead PII in analytics or Git.
6. Day 60 targets: 150 whole-site / 25 focus-page clicks in the latest 28 settled days. Day 90 targets: 200 / 40. Below 150 site clicks at day 90 plus stalled focus pages and no relevant commercial progress triggers a stop-new-development review. Incomplete data or seasonality requires an explicit judgment, not an automatic kill switch.

## Source maintenance

`python scripts/recovery_scorecard.py --refresh-queue build/ops/source-refresh-queue.csv` produces the utility review queue without changing public dates. Prioritize existing click winners, then high-impression local routes. Read official content and compare cadence, tester approval, reporting ownership, portal links, phone and deadlines. Store evidence and update the reviewed record only after that review. A 200 status is not semantic verification; a 403 from an automated client is not proof a source is dead. Keep explicitly published useful pages accessible with the existing freshness warnings.

The new national field references were reviewed against named model documents; their snapshots/review note live under `storage/snapshots/recovery-2026-10-12/`. Older dates on other source entries are retained deliberately. No bulk date reset was performed.

## Monetization prerequisites

The three initial retailer links are ordinary uncompensated links. Measure demand before applying for a relevant merchant program. Add affiliate tracking and a clear disclosure only after approval and exact part/size matching. Do not fabricate prices, offers, stock, reviews, or a commission relationship. A paid local inquiry product requires a real partner, eligibility/coverage terms, consent and agreed acceptance criteria before charging. Public help is still manual review, with no promised dispatch or booking.

## Release checklist

Run the full Gradle suite and existing plus recovery Playwright contracts. Verify all protected winner URLs remain covered by their contract. Capture desktop and mobile diagrams/kit results, check one H1 and canonical/indexability on the eight URLs, and ensure model JSON-LD contains TechArticle/Thing without Product offers. After deployment, set deployment date/commit in the manifest and record the actual release and production checks in the context tracker. Do not backdate the experiment to preparation.
