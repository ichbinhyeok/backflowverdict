# 09 Backflow Workflow Insertion Strategy

## 1) One-line strategy
BackflowPath should not mainly ask vendors or customers to "visit our site." It should insert into the real backflow work already happening by giving vendors a customer-ready brief they can send after a notice, failed test, irrigation question, fire-line question, or due-date call.

## 2) Why this is BackflowPath-specific
- Backflow work is local, fragmented, and authority-driven.
- The customer usually arrives confused after a real trigger: annual notice, failed test, repair quote, irrigation install, or commercial compliance request.
- The vendor already spends time explaining the same rules, next steps, and local links over and over.
- The customer often does not trust generic plumbing copy. They want the exact local rule, the official program, and the next safe step.
- That means the best insertion point is not a generic landing page. It is a reusable handoff artifact that helps the vendor communicate faster and helps the customer act with less confusion.

## 3) Strategy principle
- Provide value first inside the existing workflow.
- Keep authority on source-backed utility and focus pages.
- Use handoff surfaces as distribution and recovery layers, not as the main indexed SEO target.
- If a surface does not make the vendor faster or the customer clearer, it is not the next thing to build.

## 4) Core actors and value exchange
### Vendor or sender
- Certified tester
- Backflow repair plumber
- Irrigation contractor
- Fire-line or commercial compliance vendor
- Property manager or maintenance coordinator forwarding the issue internally

### Customer or recipient
- Homeowner
- Restaurant or retail operator
- HOA or apartment manager
- Facility or property manager

### Value to the vendor
- Send a clean explanation without rewriting the rule every time.
- Reduce repetitive phone time and avoid missed steps.
- Look more organized and more trustworthy in front of the customer.
- Reuse the same artifact across similar jobs.

### Value to the customer
- See the exact local situation in plain language.
- Understand the next few steps, due date, and where to go officially.
- Get the full rule and official program link one click away.
- Move from confusion to action without reading a long SEO page first.

### Value to BackflowPath
- Recover direct and organic attention through useful briefs that get clicked and shared.
- Learn which issue types and utility surfaces generate real workflow pull.
- Grow on top of utility-first authority pages instead of weakening them.

## 5) Product surface roles in this project
### Canonical authority surfaces
- `/utilities/{state}/{utility-slug}/`
- `/utilities/{state}/{utility-slug}/annual-testing`
- `/utilities/{state}/{utility-slug}/failed-test`
- `/utilities/{state}/{utility-slug}/irrigation`
- `/utilities/{state}/{utility-slug}/fire-line`
- Source-backed evergreen guides when the answer is cross-utility, not utility-specific

These are the pages that should own search authority because they hold the rule, source, last-verified context, and full workflow.

### Workflow insertion surfaces
- `/handoffs/new` - sender-side builder that turns a real issue into a sendable brief
- `/handoffs/{handoffId}` - vendor send kit with text, email, and tracked recovery links
- `/handoffs/{handoffId}/brief` - customer-facing brief
- `/r/cta?...` - tracked recovery path back into the full rule, official program, or help surface

These are not the main SEO destination. They are operational surfaces that sit between the vendor conversation and the canonical rule page.

## 6) Surface authority rules
- Handoff pages stay `noindex`.
- Handoff pages canonically point back to the full rule page, not to themselves.
- Internal notes must never leak into the public customer brief.
- The public brief should always keep the official program and full rule within one click.
- Do not try to make handoff pages rank for broad discovery terms.
- Discovery happens on utility and focus pages; handoffs exist to move real workflow forward and recover qualified attention.

## 7) Best workflow insertion points
### A) Annual notice follow-up
Use when the customer got a letter or email and asks, "What do I need to do now?"

Why it works:
- High repetition
- Clear next step
- Easy to reuse per utility

### B) Failed test follow-up
Use when the tester or repair vendor needs to explain what failed, what usually happens next, and what needs to be submitted.

Why it works:
- High urgency
- High confusion
- Strong need for a structured customer-ready explanation

### C) Irrigation install or seasonal activation question
Use when the vendor is explaining whether testing, permits, or an assembly apply.

Why it works:
- Often starts with phone or text
- Customers need local clarification fast
- Good wedge for residential plus light commercial work

### D) Fire-line or commercial compliance question
Use when a property manager or commercial operator needs a clean summary for a stakeholder, tenant, or internal approver.

Why it works:
- More stakeholders
- More forwarding and sharing behavior
- Higher chance the brief gets circulated

## 8) Current best wedge for BackflowPath
The first wedge should be repeated, confusing, locally specific issues where the sender already explains the same answer every week.

That makes the current best wedge:
- annual testing notice follow-up
- failed test follow-up
- utility-specific irrigation questions

This matches the strongest current BackflowPath surface because the codebase already has:
- utility-first local rule pages
- annual, failed-test, irrigation, and fire-line focus pages
- a first handoff builder and public brief flow
- tracked recovery links back into full rule, official program, and help paths

## 8a) Current first market slice
The first manual push should start in DFW, not across every published state.

Why DFW first:
- the project already has a dense Texas utility cluster
- the workflow patterns repeat across annual testing, failed tests, irrigation, and some fire-line cases
- a vendor can reuse the same BackflowPath motion across multiple nearby utilities without changing the whole explanation style

The clearest first utility set is:
- Grand Prairie Water Utilities for annual notice and failed-test follow-up
- Arlington Water Utilities for annual, irrigation, and fire-line explanation
- Dallas Water Utilities and Fort Worth Water Utilities for broader commercial and property-manager conversations
- Garland, Lewisville, Frisco, McKinney, and Mesquite as adjacent reuse utilities once the first brief pattern lands

This makes the first push concrete:
1. Start with annual notice and failed-test briefs in Grand Prairie and Arlington.
2. Add irrigation and fire-line variants in Arlington where the issue split is stronger.
3. Expand only after one sender persona starts reusing the brief across the nearby DFW utility set.

## 9) What this strategy is not
- It is not "send people to our homepage."
- It is not "blast a BackflowPath link and hope for SEO later."
- It is not conversion-first landing-page optimization.
- It is not generic lead-gen copy that asks for contact info before giving clarity.
- It is not replacing official guidance with sponsor messaging.

## 10) Manual push comes before broad automation
The first push should be manual because the team still needs to learn:
- which vendor persona reuses the brief fastest
- which issue type gets the strongest customer click-through
- which parts vendors still rewrite manually
- whether PDF or notice-ingest is needed before broader rollout

The right first manual motion is:
1. Pick a tight vendor group in surfaces already covered well by the codebase.
2. Create real sample handoffs tied to actual utilities and issue types.
3. Show the vendor a customer-ready artifact, not a product pitch.
4. Ask what part they would send today, what they would edit, and what is still missing.

The first three sample briefs should be:
1. Grand Prairie annual notice brief
2. Grand Prairie failed test brief
3. Arlington irrigation or fire-line brief, depending on which vendor you are talking to

## 11) Recommended first push personas
### 1. Certified testers and backflow compliance shops
Best first persona because they already handle annual tests, failures, submissions, and deadline questions.

### 2. Irrigation contractors
Strong second persona because homeowner confusion is high and the issue often starts before a formal compliance workflow exists.

### 3. Commercial repair or fire-line vendors
Good third persona because the forwarding behavior is strong, but the workflow can be more customized and slower to standardize.

## 12) Current implementation rules for the handoff flow
- The utility or focus page creates the context.
- The builder creates a specific brief for a live issue, not a generic page.
- The result page gives the sender usable text and email copy.
- The public brief front-loads the situation, due date, next steps, full rule path, official program path, and help path.
- Recovery clicks should teach which canonical surfaces deserve more depth.

## 13) What to build next only if manual push confirms reuse
### High-priority additions
- Notice-to-brief helper for pasted notice text
- PDF or printable handoff for attachments and forwarding
- Saved templates by issue type and utility

### Later additions
- Vendor sender presets or light branding
- Reminder follow-up sequences
- Faster issue extraction from uploaded notice documents

Do not build these first if the current brief is not yet being reused manually.

## 14) Success signals
- Vendors reuse the brief without asking for a homepage pitch.
- Customers click from the brief into the full rule or official program.
- The same utility and issue combinations repeat.
- Vendors ask for one more issue template, not a generic feature tour.
- BackflowPath recovers qualified traffic through the brief instead of trying to manufacture generic top-of-funnel visits.

## 15) Decision rule for future agents
When choosing between a new indexed page and a new workflow surface:
- build the indexed page when it increases source-backed authority on a local rule
- build the workflow surface when it helps a vendor explain, send, or recover action inside an existing job

For BackflowPath right now, the workflow surface should stay secondary to the utility-first canonical page, but it should be treated as a real growth mechanism, not a side feature.

## 16) Workflow monetization guardrails
BackflowPath should now be read as a three-layer motion:
- free sendable handoff
- paid setup
- later recurring convenience layer

The free handoff should stay strong enough that a vendor would actually send it.
If the free artifact is too weak, there is no credible setup offer above it.

The paid setup layer should align the handoff to one office:
- company sender defaults
- annual notice template
- failed-test template
- PDF wording and contact alignment

The recurring layer should not be treated as the first commercial motion.
It only makes sense after repeated office use proves that memory, presets, history, and reuse remove real friction.

## 17) Branding rule for handoff outputs
The current preferred position is vendor-first co-branding, not full white-label.

Why:
- the vendor needs the artifact to feel usable with its own customer relationship
- BackflowPath still needs customer recovery and proof-based trust

That means the output should usually keep:
- the vendor visibly present first
- BackflowPath visible as the proof, rule, or recovery layer

The product should not trade away the recovery loop too early by defaulting to full white-label.
