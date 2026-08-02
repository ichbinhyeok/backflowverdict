# Design System — BackflowVerdict

## Product Context
- **What this is:** A decision tool that helps people identify a water-control device, understand an observable problem, and then verify the local utility rule before they act.
- **Who it is for:** US homeowners, property managers, commercial operators, and people holding a backflow notice or failed test report.
- **Space/industry:** Water safety, plumbing diagnostics, backflow compliance, and utility reporting.
- **Project type:** Search-led public product with diagnostic workflows and source-backed utility records.

## Product Thesis
- **Visual thesis:** A field manual crossed with a trustworthy public service: precise, calm, inspectable, and useful under pressure.
- **Content plan:** Start with the event, identify the device, show safe checks, make a decision, then reveal the official local route.
- **Interaction thesis:** Choice rows reveal the next decision, status changes are visible without animation dependence, and a single contextual action stays available on mobile.

## Aesthetic Direction
- **Direction:** Industrial / utilitarian editorial.
- **Decoration level:** Intentional. Technical line diagrams, numbered rules, and status bars do the visual work.
- **Mood:** Competent and unhurried. It should feel like the useful page someone wishes had been attached to the notice.
- **Reference patterns:** GOV.UK's question-led service patterns, iFixit's symptom-led troubleshooting, and official manufacturer service documentation.
- **Deliberate risk:** Use an editorial case-file composition instead of a conventional plumbing-company hero or a SaaS card dashboard.
- **Deliberate risk:** Replace generic industrial photography with device silhouettes and fault markers. This is less decorative but materially improves identification.

## Typography
- **Display/Hero:** Source Sans 3, 700–800 — direct, legible, and credible at both poster and document sizes.
- **Body/UI:** Source Sans 3, 400–700 — one family reduces visual noise and keeps long guidance readable.
- **Data/Status:** IBM Plex Mono, 500–600 — reserved for verification dates, step numbers, evidence labels, and device codes.
- **Loading:** Google Fonts with system sans and Consolas fallbacks; the experience must remain usable if webfonts fail.
- **Scale:** 14, 16, 18, 22, 30, 44, 64px with fluid clamps for the top three levels.

## Color
- **Approach:** Restrained; one action accent plus semantic colors.
- **Ink:** `#17201D` — primary text and dark surfaces.
- **Paper:** `#F4F1E8` — default background.
- **Surface:** `#FCFBF7` — working surface.
- **Line:** `#C8CCC4` — structure without card chrome.
- **Action:** `#C64E32` — the only routine CTA accent.
- **Official:** `#176B5B` — verified utility evidence only.
- **Warning:** `#9A5B13`.
- **Error:** `#B42318`.
- **Info:** `#285F8F`.
- **Dark mode:** Not shipped in the first release; safety/status colors are calibrated for the paper surface.

## Spacing
- **Base unit:** 4px.
- **Density:** Comfortable for guidance, compact inside facts and result rows.
- **Scale:** 4, 8, 12, 16, 24, 32, 48, 72, 96px.

## Layout
- **Approach:** Hybrid editorial grid: strong full-width opening, disciplined document grid below.
- **Grid:** 4 columns mobile, 8 tablet, 12 desktop.
- **Max content width:** 1240px; reading measure 720px.
- **Border radius:** 0, 4, 8px. Full pills are limited to status labels.
- **Cards:** Only when the entire surface is interactive. Static content uses rules, columns, and sections.

## Motion
- **Approach:** Minimal-functional.
- **Easing:** enter `cubic-bezier(.2,.8,.2,1)`, exit `ease-in`, move `ease-in-out`.
- **Duration:** micro 90ms, short 160ms, medium 260ms.
- **Required moments:** staged first-view entrance, diagnostic result reveal, and choice-row affordance.
- **Accessibility:** All state is available without motion; honor `prefers-reduced-motion`.

## Content Rules
- Start with what the user observed, not with an industry definition.
- Never present a likely cause as a confirmed diagnosis.
- Separate potable-water backflow devices, atmospheric/vacuum breakers, and sewer backwater valves.
- Show official utility requirements before any commercial handoff.
- Never imply that a portal sets the rule; the utility does.
- Label national guidance and local verified facts separately.

## Decisions Log
| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-08-02 | Rebuild as a decision product, not a directory | GSC winners are task/portal/failed-test routes; page-count expansion diluted impressions. |
| 2026-08-02 | Preserve existing winner URLs and official data | Search equity and verified utility evidence are the asset being salvaged. |
| 2026-08-02 | Use diagrams instead of generic hero photography | Device identification requires shape and fault-location evidence, not atmosphere. |
| 2026-08-02 | Keep one action accent | Urgency and official status must remain semantically clear. |
