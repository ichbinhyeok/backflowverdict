# 02 Site Architecture

## 1) Canonical entities
- Utility / water authority
- State
- Metro
- Evergreen guide
- Provider profile

## 2) URL graph
### Utility pages
- `/utilities/{state}/{utility-slug}/`
- `/utilities/{state}/{utility-slug}/annual-testing`
- `/utilities/{state}/{utility-slug}/failed-test`
- `/utilities/{state}/{utility-slug}/approved-testers`
- `/utilities/{state}/{utility-slug}/find-a-tester`
- `/utilities/{state}/{utility-slug}/irrigation`
- `/utilities/{state}/{utility-slug}/fire-line`

### Support content
- `/guides/who-needs-a-backflow-preventer`
- `/guides/backflow-test-cost`
- `/guides/failed-backflow-test-next-steps`
- `/guides/rpz-vs-dcva-vs-pvb`

### Aggregation
- `/states/{state}/backflow-testing`
- `/metros/{metro}/backflow-testers`
- `/cities/{state}/{city-slug}/backflow-testing`

## 2a) Route policy
- `/approved-testers` is allowed only when the governing authority publishes an official approved or certified tester list.
- `/find-a-tester` is the non-official provider discovery page when no official list exists.
- City alias routes default to 301 redirects when a city maps cleanly to one utility and has no separate enforcement layer.
- If a city route must exist for discovery but adds little unique content, keep it as a noindex bridge until it earns an indexed exception.

## 3) Local page render order
1. H1 + direct answer
2. Requirement verdict box
3. Property/device matrix
4. Notice-to-completion workflow
5. Failed-test module
6. Cost band
7. Local forms / portal / contact links
8. Provider CTA
9. FAQ
10. Sources and last verified

## 4) Schema and structured content
- `FAQPage` for local and evergreen FAQs
- `BreadcrumbList` for utility/state hierarchy
- `HowTo` only for universal process pages, not local legal/compliance pages
- No `LocalBusiness` on utility pages unless the page is actually a provider profile

## 5) Internal linking system
- Utility page -> annual testing / failed test / approved testers or find-a-tester
- Utility page -> evergreen cost guide
- Evergreen cost guide -> provider discovery pages
- Metro provider page -> nearby utility pages
- State hub -> top utilities + evergreen support guides

## 6) Programmatic page eligibility
A local page should go live only if it has:
- governing entity identified
- official source URL
- stored source excerpt or snapshot
- testing frequency
- at least one clear next-action path
- reviewer initials
- verification date

Provider discovery pages should go live only if they have:
- a mapped utility or metro target
- explicit non-official labeling
- provider or sponsor inventory
- service area clarity
- review date

## 7) Non-index or hold rules
- No official source
- unclear governing authority
- stale or contradictory requirements
- no actionable local path beyond generic education
- `/approved-testers` with no official list
- city bridge pages in `noindex-bridge` mode
