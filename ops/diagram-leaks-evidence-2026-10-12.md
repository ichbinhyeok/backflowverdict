# Diagram and leak evidence expansion

Scope: four existing canonical routes — `/backflow-preventer-diagram/`, `/backflow-preventer-leaking/`, `/vacuum-breaker-leaking/`, `/double-check-valve-leaking/`. No new indexed routes. Existing vent-specific, repair, kit, installation/code and local utility ownership remains intact.

The diagram page now separates functional sketches, exploded parts views and installation details, with original DCVA/PVB observation maps and exact-model PDF page links. The three leak pages distinguish RPZ discharge patterns, PVB minor/major vent discharge, and external DCVA water versus failed internal checks. Five native questions per page, observation checklists and contextual service/local routes make the material usable without ordering guessed parts.

Attributed cases: measured RPZ replacement at Brenham Hospital; UMass large-RPZ flood prevention; EPA-compiled historical Michigan AVB and California school double-check incidents. Cases clearly state what is and is not documented. No successful leak repair, field inspection, copied project photograph or savings claim is invented. Source-level boundaries: `storage/snapshots/diagram-leaks-2026-10-12/source-review.md`.

The shared template preserves installation anchors and generalizes case/question headings for the new pages. EPA source clicks are classified as official sources rather than manufacturer sources. All source navigation remains separate from merchant/help intent and revenue.

Retain the October 12 experiment baseline and December 11 / January 10 review gates. This expansion is an additional intervention, not a restart or demonstrated traffic effect. Private QA and research evidence stays in `analysis/`, excluded from Git and deployment.

Local validation: all 92 Gradle tests and 11 existing Chromium contracts passed. Additional browser QA covered all four changed pages plus installation: single H1, every contents anchor, questions opened with keyboard and with JavaScript disabled, no 390px document overflow, and no page JavaScript errors. Original diagram and mobile screenshots were inspected. EPA and manufacturer case/reference actions produced separate source events and no merchant/provider event. Actual GA4 ingestion and traffic effects remain unverified.

Release provenance will be recorded after deployment and public verification.
