# 00 Strategy and Product Architecture

## 1) One-line thesis
BackflowPath should be a utility-first compliance site for people who already have a backflow problem: they need to know whether testing is required, when it is due, what happens if the device fails, what it will cost, and who can handle it.

## 2) Why this vertical works
- EPA treats cross-connection control and backflow prevention as a core drinking-water protection function, but obligations are enforced locally by cities, utilities, and water districts.
- That fragmentation creates strong local search intent and weak national answers.
- The highest-intent user already got a notice, has an annual deadline, failed a test, is installing irrigation, or needs to submit paperwork.
- This makes the site a "local requirement plus next action" product, not a generic education site.

## 3) Primary users
- Small commercial owners and operators: restaurants, retail, medical, mixed-use, car washes.
- HOA, apartment, and facility managers handling annual compliance and vendor coordination.
- Homeowners with irrigation systems, pools, fire suppression lines, or assemblies that trigger testing.
- Builders, remodelers, and buyers trying to confirm whether a device or test program applies.
- Local testers and plumbers are secondary users but primary monetization partners.

## 4) Query families
### Compliance lookup
- `city + backflow test`
- `utility + backflow requirement`
- `annual backflow testing`

### Failure handling
- `failed backflow test`
- `backflow repair and retest`
- `what happens if backflow device fails`

### Administrative workflow
- `submit backflow report`
- `approved backflow tester`
- `backflow forms`
- `retest fee`

### Cost intent
- `backflow test cost`
- `rpz repair cost`
- `annual backflow inspection cost`

### Device and install intent
- `do i need a backflow preventer`
- `irrigation backflow requirement`
- `fire line backflow test`
- `new irrigation backflow permit`
- `backflow installation requirements`

## 5) Competitive angle
- National plumbing sites cover "what is backflow" but usually do not normalize local testing rules, forms, submission paths, approved tester lists, and failure workflows.
- The moat is a local rule graph.
- The goal is not to outrank broad home-improvement giants on generic terms. The goal is to own utility and city long-tail where the searcher needs one exact answer.

## 6) Canonical entities and URL structure
The canonical entity should usually be the utility or water provider, not just the city.

### Core pattern
- `/utilities/{state}/{utility-slug}/`
- `/utilities/{state}/{utility-slug}/annual-testing`
- `/utilities/{state}/{utility-slug}/failed-test`
- `/utilities/{state}/{utility-slug}/approved-testers`
- `/utilities/{state}/{utility-slug}/find-a-tester`
- `/utilities/{state}/{utility-slug}/irrigation`
- `/utilities/{state}/{utility-slug}/fire-line`

### Evergreen support pages
- `/guides/who-needs-a-backflow-preventer`
- `/guides/backflow-test-cost`
- `/guides/rpz-vs-dcva-vs-pvb`
- `/guides/failed-backflow-test-next-steps`

### Aggregation pages
- `/states/{state}/backflow-testing`
- `/metros/{metro}/backflow-testers`

City alias pages should exist only where search demand is city-led and should route users to the governing utility page.
If one city cleanly maps to one utility and adds no separate enforcement detail, use a 301 redirect to the utility page.
Keep an indexable city page only when the city has its own code, notice flow, or measurable search demand that materially differs from the utility page.

## 7) Page modules
Every local utility page should include:

1. Verdict box
- Testing required?
- Frequency
- Who is affected?
- Last verified

2. Requirement matrix
- Property types
- Device types
- Hazard classes
- Annual cadence

3. Workflow module
- Notice -> test -> submit report -> repair/retest -> enforcement

4. Failure module
- What a failed test usually means
- Who repairs it
- When retest is needed

5. Local source block
- Utility doc
- Code link
- Form or portal link
- Contact phone
- Submission path

6. Cost band
- Local test range
- Repair/retest range
- What changes price

7. Provider CTA
- Tester directory
- Quote request
- Sponsor slot

8. FAQ and internal links
- Local FAQs
- Nearby utilities
- Device guides
- Cost guides
- Failure guides

## 8) Data moat
The moat is a normalized local rule graph, not content volume.

### Core fields
- Utility name
- Service area
- City/county/state mapping
- Test frequency
- Covered device/property types
- City-to-utility alias mappings
- Approved tester requirements
- Submission method and forms
- Retest and repair workflow
- Fees, deadlines, penalties if published
- Official source URLs
- Source excerpts and snapshot paths
- Reviewer initials and stale threshold
- Last checked date

### Additional moat layers
- Entity resolution: many users search by city while compliance lives under a utility, district, or authority.
- Update discipline: change log, stale-page flagging, and source snapshots.

## 9) Monetization roadmap
### Phase 1
- Build trust first in pilot markets and collect a sponsor prospect list.
- Optional call tracking or lead forms only after the page is trusted.

### Phase 2
- Start sponsor outreach to local testers and plumbers in the pilot markets.
- Premium provider profiles with service area, certifications, and emergency repair CTA.
- Pay-per-call or pay-per-form referrals to testers, irrigation contractors, and fire protection vendors.

### Phase 3
- Compliance reminder product for property managers: annual reminder emails or SMS.

Retail affiliate is possible but should not be the primary plan.

## 10) Compliance and trust rules
- Show exact official sources and `last verified` dates on every local page.
- Never claim a tester is approved or certified unless the local authority says so.
- Separate paid listings from official lists with zero ambiguity.
- Do not provide legal or compliance guarantees.
- A stale page is worse than no page in this category.
- Use `/approved-testers` only when the authority publishes an official approved or certified list.
- Use `/find-a-tester` when no official list exists and label the page as a sponsor or directory experience, not an authority list.
- Freshness checks and stale-page suppression are launch-critical, not a later optimization.

## 11) Build sequence
1. Define the data schema, file contracts, source standards, and freshness policy first.
2. Start with one strong baseline state where utilities publish clear annual testing rules and forms. The current seed implementation uses Texas for this role, but Texas is not a permanent product boundary.
3. Launch evergreen support guides first so local pages have strong internal links.
4. Build 10-15 strong utility pages in the baseline state, then add one or two representative expansion states as soon as they can meet the same structured modules, source format, and freshness workflow.
5. Ship refresh operations before broad expansion: source recheck queue, stale-page suppression, and change logs.
6. Add `failed test` and either `approved testers` or `find a tester` pages once page eligibility rules are met.
7. Do not treat Texas-only depth as the goal. Expand to additional states when they add useful utility-pattern coverage and can still pass the same refresh SLA.

## 12) Why it can win
- It is boring, local, regulatory, and action-driven.
- The searcher usually wants one practical answer, not a long explainer.
- That makes it a strong "small but real" asset candidate for repeatable local SEO.

## 13) Anchor sources
- EPA Cross-Connection Control Manual: https://19january2021snapshot.epa.gov/sites/static/files/2015-09/documents/epa816r03002_0.pdf
- Grand Prairie Cross-Connection Program example: https://www.gptx.org/files/sharedassets/public/v/2/departments/public-health-amp-environmental-quality/documents/cross-connection-control-and-prevention.pdf
- Boynton Beach code example: https://codelibrary.amlegal.com/codes/boyntonbeach/latest/boyntonbeach_fl/0-0-0-55023
