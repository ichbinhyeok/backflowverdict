# 00 Strategy and Product Architecture

## 1) One-line thesis
BackflowPath should be a utility-first compliance site for people who already have a backflow problem: they need to know whether testing is required, when it is due, what happens if the device fails, what it may cost, and what the next safe local action is.

## 2) Why this vertical works
- EPA and state policy set the frame, but obligations are enforced locally by cities, utilities, and water districts.
- That fragmentation creates strong local search intent and weak national answers.
- The highest-intent user already got a notice, has an annual deadline, failed a test, is installing irrigation, or needs to submit paperwork.
- This makes the site a "local requirement plus next action" product, not a generic education site.

## 3) Primary users
- Small commercial owners and operators: restaurants, retail, medical, mixed-use, car washes.
- HOA, apartment, and facility managers handling annual compliance across multiple addresses.
- Homeowners with irrigation systems, pools, fire suppression lines, or assemblies that trigger testing.
- Builders, remodelers, and buyers trying to confirm whether a device or test program applies.
- Contractors and testers are secondary users who may browse the same public utility pages, but they are not the product center.

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

7. Next-action layer
- Official tester list when one exists
- Non-official directory only when clearly labeled
- Request-help path after the rule is visible

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

## 9) Product constraints
- Do not hide the official workflow behind a directory or contact form.
- Keep provider browse surfaces public, labeled, and secondary.
- Manual request review is allowed; private internal routing flows are not part of the active product.
- Trust and source clarity are more important than aggressive conversion mechanics.

## 10) Compliance and trust rules
- Show exact official sources and `last verified` dates on every local page.
- Never claim a tester is approved or certified unless the local authority says so.
- Separate non-official directories from official lists with zero ambiguity.
- Do not provide legal or compliance guarantees.
- A stale page is worse than no page in this category.
- Use `/approved-testers` only when the authority publishes an official approved or certified list.
- Use `/find-a-tester` when no official list exists and label the page as a non-official directory experience.
- Freshness checks and stale-page suppression are launch-critical, not a later optimization.

## 11) Build sequence
1. Define the data schema, file contracts, source standards, and freshness policy first.
2. Start with one strong baseline state where utilities publish clear annual testing rules and forms. The current seed implementation uses Texas for this role, but Texas is not a permanent product boundary.
3. Launch evergreen support guides first so local pages have strong internal links.
4. Build 10-15 strong utility pages in the baseline state, then add one or two representative expansion states as soon as they can meet the same structured modules, source format, and freshness workflow.
5. Ship refresh operations before broad expansion: source recheck queue, stale-page suppression, and change logs.
6. Add failed-test and tester-routing pages once page eligibility rules are met.
7. Do not treat Texas-only depth as the goal. Expand to additional states when they add useful utility-pattern coverage and can still pass the same refresh SLA.
