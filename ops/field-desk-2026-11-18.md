# Field desk redesign and conversion work — 2026-11-18

Status: **released and verified on 2026-11-18**, together with the second pass. See [production release evidence](field-desk-release-2026-11-18.md). The implementation-time audit and local validation below are retained as history.

## What the live audit found

At a 1037 × 683 desktop viewport the diagram document was roughly 11,974px tall and installation 12,317px. The diagram's main help CTA was near y=8,016; installation's near y=8,460. These are layout observations, not analytics about actual user scrolling.

- “I know the model and size” revealed the same text and a separate Continue link, then sent the visitor to an unselected parts finder. It did not use the model knowledge the visitor already had.
- Live finder contained 82 kit rows but only three rows with stored retailer links. FEBCO 765 / 1/2–3/4-inch results had no help action in the main content.
- Installation had a static checklist labelled Installer brief, without a generated, copyable or printable brief.
- The fire-installation CTA passed issueType=installation, but the form had no matching option. It arrived with an empty task.
- Hard-coded help links passed source only. They omitted the page-family context and signing token required by LeadRoutingService, so the backend would hold the request as unverified context. Source inspection established this; no production form was submitted.
- Help copy exposed “Noindex service handoff” and required a phone number for an uncertain manual-review outcome.

## Implemented

Visual thesis: an inspectable industrial product reference, warm brass imagery and a restrained green action color, with a useful workspace immediately after the opening.

Content plan: device illustration and direct action → parts lookup or installer brief → ten visible model-reference routes → existing sourced drawings, cases, questions and official utility verification. Removed repeated generic workflow/identification/boundary sections only from these two entrances.

Interaction thesis: labeled device points explain what is visible; task tabs expose a working tool; exact selections travel into the review form. Keyboard tabs, reduced motion and no-JavaScript paths remain usable.

### Diagram and installation entrances

- New generated RPZ family illustration with keyboard-accessible component callouts. Clearly labelled AI-generated; no dimensions or exact-model identity are inferred from the image.
- Direct in-page lookup uses the existing ten published model records and their stored kit/merchant information. It reveals real part numbers, contents, compatibility and existing retailer links.
- Every completed lookup has a specific “Request a parts check” action. Unidentified/missing models also have a route.
- Visible model index links these search entrances to the ten existing model pages. This is a crawlable directory, not links hidden only in JavaScript templates.
- Installation brief uses job, protected system and exposure to assemble a practical scope checklist. It can be copied or printed, including model/authority boundaries, fire coordination, drainage, freeze exposure, quote line items and closeout responsibilities.
- Changing the installation choices after creating a brief updates it, preventing an old brief from being passed along.

### Review flow

- Parts-check and installation-scope choices exist in the form and have specific headlines.
- Generated links preserve signed source context. Client selections are self-reported notes, never trusted utility verification.
- Public, structured tool choices pass through browser session storage for up to two hours and only into a form with the matching source and task. No name, phone, email or location is collected by the tools themselves.
- Email or phone is sufficient. Name is optional. Server validation rejects missing/invalid contact and keeps the existing consent, rate-limit, honeypot and persistence-confirmed success gates.
- Existing standalone kit finder also gets a contextual review route.
- Saved requests use a confirmation screen consistent with the new visual system. Visiting the thanks URL alone does not claim that a request was saved.

### Measurement

Added named stage events: diagram_point_select, field_task_select, part_model_select, part_scope_select, installation_brief_created, installation_brief_copy, installation_brief_print, context_help_start. Existing kit_match_result, merchant_outbound_click, lead_form_attempt and lead_form_success remain separate.

Events include fixed public categories/IDs, not free-text briefs, contact fields or query strings. Server persistence remains the inquiry source of truth. GA4 reporting access is still unresolved; event code does not prove production collection.

## More aggressive growth and commercial plan

The owner asked for stronger action than passive monitoring. The next investment is to make the two successful entrances feed the existing commercial-intent model portfolio and to learn from actual requests.

Fresh GSC query/page sample, 2026-10-19–11-15:
- backflow preventer installation diagram: 37 impressions, position 7.59, 0 visible-query clicks.
- backflow preventer parts diagram: 28 impressions, position 17.25, 0 visible-query clicks.
- residential backflow preventer installation diagram: 16 impressions, position 7.56.
- rpz installation diagram: 13 impressions, position 3.54, 1 visible-query click.

These small rows support where to strengthen the answer. They do not forecast traffic. Most property clicks are anonymized out of query rows. Query/page aggregation also differs from query-only aggregation.

1. Publish this concrete tool-led experience when release is authorized and verify collection on the correct production hostname. Preserve existing titles/canonicals and the October experiment baseline.
2. Prioritize actual model selections and requests to decide which of the 82 parts need additional exact-SKU retailer verification. Current stored merchant links are ordinary uncompensated links; no affiliate income is claimed.
3. Use the installer brief as a shareable artifact and a reason to request manual scope review. Keep the tool free to use without an email gate; ask for contact only when the visitor requests review.
4. Use resulting qualified parts/installation demand to approach a specific supplier or local installer under separately authorized outreach, with actual acceptance terms. No provider booking or commercial agreement exists from this change.
5. For a 30-day first pass, use 10 completed tool uses and 3 relevant manual requests as stretch learning targets, not forecasts or automatic pass/fail thresholds. At this traffic level, inspect each request and denominator; do not invent a reliable conversion rate.
6. Keep the December 11 / January 10 search review gates. These conversion tools supplement the existing experiment; they do not reset day zero or prove that an intervention caused later changes.

## Verification

- Full Gradle suite: 94 tests, zero failures/errors.
- Chromium suite: 15/15, including exact kit matching, model/brief carryover, signed source metadata, mobile width, keyboard tabs, no-JavaScript paths, analytics separation and existing recovery contracts.
- Manual local browser submission: email-only selected 975XL / 3/4–1 in / RK34-975XLC request returned persistence-confirmed success; checked the local JSONL record for the correct source, issue, notes and VERIFIED_PAGE_CONTEXT.
- Local runtime uses build/review-leads, disabled email delivery and disabled GA4. No production inquiry was submitted and no message was sent.
- Desktop/mobile screenshots: output/playwright/conversion-v2-*.png.
- Source ledgers and old verification dates were not bulk-refreshed.

Remaining operating gaps: actual manual-review capacity, a reliable notification/checking routine for the inbox, verified response expectations, production signing-secret configuration and trustworthy GA4 property/hostname reporting. No paid service, automated dispatch, commercial partner or revenue result is created by this implementation.

## Image provenance

Built-in image_gen tool; source generated image retained under the Codex generated-images directory. Project asset: src/main/resources/static/images/devices/rpz-studio-v2.webp (156,566 bytes). Converted from the generated PNG to WebP without semantic editing.

Final prompt:

> Create a polished, photorealistic technical product illustration for a water-device reference website. One single generic bronze/brass reduced-pressure-zone (RPZ) backflow preventer assembly, horizontal left-to-right plumbing axis, isolated on a seamless warm ivory #f4f1e8 studio background. Landscape canvas, assembly centered with generous clear margins, full device visible. Physically plausible catalog product appearance: an isolation ball valve with dark teal lever handle at each far end, substantial cast bronze center body with two round raised check-service covers on its upper surface, small brass test connections, and a clearly visible relief-valve outlet pointing down beneath the central section. Short threaded pipe stubs only at left and right. Camera at a gentle elevated front three-quarter angle so top covers and downward outlet can both be seen. Fine real metal grain, machined details, understated patina, sharp edges, soft contact shadow, professional industrial photography lighting. This is an illustrative device-family visualization, not a specific manufacturer model or construction plan. Exterior only, no cutaway, no exploded parts, no water, no hands, no people, no logos, no brand, no text, no arrows, no dimensions, no UI, no decorative border. The material realism and clear silhouette should make the component recognizable rather than looking like a flat web icon. Save final image as an asset for the local BackflowVerdict project.
