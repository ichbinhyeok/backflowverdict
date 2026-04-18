# 10 Vendor Strategy and Revenue Memo

## 1) Why this document exists
This memo captures the current strategic understanding behind the vendor handoff feature so a future agent can resume work without relying on chat history.

It is not a final go-to-market plan. It is the current locked context for product definition, target user, likely monetization path, and the questions that still need real-world validation.

## 2) Current product definition to lock first
The vendor feature should not currently be framed as a full vendor portal, a generic CRM, or a broad back-office system.

The narrower definition to lock is:

BackflowPath gives a backflow vendor a fast way to create a customer-ready brief after an annual notice or failed test, send it as a link or PDF, and reduce repetitive explanation work.

The most useful one-line version is:

The vendor can create a customer brief in about two minutes, send it, and see whether it was opened.

## 3) What the feature is, and what it is not
### What it is
- A handoff generator for annual notice and failed-test workflows
- A customer explanation layer sitting on top of the canonical utility and rule pages
- A workflow tool for real jobs already in progress
- A reusable communication artifact for vendors

### What it is not
- Not a full vendor CRM
- Not a broad field-service operating system
- Not a utility submission platform
- Not a replacement for Jobber, Housecall Pro, ServiceTitan, SwiftComply, VEPO, or BSI
- Not yet a full SaaS workspace with accounts, billing, teams, and permissions

## 4) Current product state
The current implementation already moved meaningfully toward a real vendor workflow surface.

What exists now:
- A vendor landing page at `/vendors/customer-briefs`
- A handoff builder at `/handoffs/new`
- A sender-side result surface
- A public customer brief
- PDF outputs
- Event tracking for opens and downloads
- Internal and public token separation for new handoff records
- Required failure-note validation for failed and unable-to-test outcomes

What still needs hardening before this should be treated as a trusted vendor workflow:
- Remove legacy fallback paths that still accept raw handoff IDs for older records
- Reduce CSV mirroring of sensitive handoff and event data
- Add a better vendor identity key than free-text company name

## 5) Main product insight from this strategy work
The strongest version of this feature is not:
"vendors should share our site."

The strongest version is:
"vendors already explain the same local compliance situation every week, and BackflowPath turns that repeated explanation into a cleaner customer handoff."

That means the feature is strongest when:
- the trigger is real
- the local rule is already known
- the customer is confused
- the vendor wants to move the job forward quickly

## 6) Best first workflow wedge
The current best wedge remains:
- annual notice follow-up
- failed test follow-up

These scenarios are the best first wedge because they are:
- repetitive
- locally specific
- easy to explain badly by phone
- painful enough that vendors may reuse a structured artifact

Irrigation and fire-line remain important adjacent workflows, but the current strategy should treat them as second-wave expansions rather than the core initial motion.

## 7) Organic acquisition view
The handoff feature can support organic growth, but not in the simple sense of "the handoff pages themselves will rank."

The better organic thesis is:
- canonical utility and issue pages own search authority
- handoff pages remain operational and mostly noindex
- the vendor landing page and similar problem-solution pages can attract narrow but high-intent traffic

The feature is therefore more useful for:
- product-led discovery
- workflow insertion
- recovery of qualified attention

It is less useful as a broad top-of-funnel SEO bet by itself.

The practical implication:
BackflowPath should keep treating the handoff tool as a workflow layer that sits behind stronger local rule and issue pages, not as the main SEO surface.

## 8) B2C and B2B relationship
The current strategy should not treat B2C and B2B as competing bets.

The working view is:
- B2C utility and issue pages are the compounding asset
- B2B vendor outreach is the push channel that may produce earlier cash flow
- both should reuse the same underlying rule, issue, and workflow surface

This matters because the vendor handoff feature only makes sense if it is built on top of the same local authority and issue pages that support the public product.

The wrong path would be:
- one product for public SEO
- a disconnected second product for vendors

The better path is:
- one source-backed backflow knowledge layer
- one vendor workflow insertion layer
- two different distribution motions

## 9) B2B and revenue view
The likely paid path is not "launch a polished self-serve SaaS and wait."

The more realistic early revenue path is:
- free or cheap utility-like workflow surface
- founder-led sales
- manual setup for pilot vendors
- optional setup fee plus monthly subscription

The strongest early paid offer is closer to:
"we will set up your customer brief workflow for annual notices and failed tests"

rather than:
"buy a complete vendor platform."

## 10) Self-serve versus setup-led motion
For this project, "self-serve SaaS" means:
- the vendor finds the site
- signs up alone
- configures the tool without help
- enters payment without direct sales help

That is probably too ambitious for the current stage.

The better early motion is setup-led:
- direct outreach
- demo with a real local scenario
- vendor-specific text or branding preset
- paid pilot

This is the right tradeoff because the immediate goal is first revenue, not product elegance.

## 11) Target customer profile
The strongest first target is not the biggest office and not the pure solo operator.

The current best ICP is:
- small but busy backflow-related vendors
- roughly owner plus admin, or about 2 to 8 people
- repeated annual notice and failed-test volume
- still relying on phone, text, and ad hoc explanation
- weak or incomplete communication workflow
- sometimes using utility portals, but not deeply systematized in customer communication

This often includes multi-service companies that do more than backflow.

That is not a problem. It is part of the opportunity.

The important point is not that they only do backflow. The important point is that backflow creates a uniquely annoying communication problem inside a broader service business.

## 12) Anti-ICP
The first paid target is probably not:
- large vendors already deeply committed to a broad operating stack
- utilities themselves
- fully solo operators with very low administrative tolerance

Large teams are more likely to ask for integration and broader workflow replacement.
Solo operators may feel the pain but often resist another paid tool unless the value is immediate and overwhelming.

## 13) Why vendors might still use this despite existing tools
The market is not empty. Existing tools already cover adjacent ground.

Known substitute categories include:
- utility or compliance portals like SwiftComply, VEPO, and BSI
- backflow-specific operational tools like Syncta
- generic field-service systems like Jobber and Housecall Pro

BackflowPath should therefore avoid pretending that no alternatives exist.

The likely reason a vendor would still use this feature is narrower:
- existing systems may handle scheduling, invoicing, or official submission
- they may still not give the vendor a clean customer-facing explanation artifact for local annual notice and failed-test situations
- BackflowPath can win as a focused communication and conversion layer, not as a full system replacement

## 14) Revenue shape to assume for now
The early commercial shape should be treated as:
- setup fee plus monthly subscription
- or paid pilot plus later recurring plan

The current narrower internal assumption is lower than earlier rough brainstorming ranges because the product is still selling a tightly scoped workflow setup, not a full vendor platform.

Working early ranges:
- beta setup: roughly $149 to $199 for a narrow annual-notice plus failed-test alignment
- a later standard setup can rise once the workflow and sample utility pack are clearly repeatable
- optional recurring, if offered early at all, should stay light and tied to memory and reuse rather than broad software promises

These are still not validated prices. They are working assumptions for strategy, not forecast-grade numbers.

## 15) Time and proof expectations
Reaching 20 paying vendors is possible, but not because the market is empty and not because SEO alone will do it quickly.

Current judgment:
- 3 months to 20 paid vendors is a stretch case
- 6 to 12 months is the more realistic base case
- 3 to 5 paid vendors is a much better first proof target

The path to 20 paid vendors likely requires:
- tight ICP
- direct outreach
- live demos using real utilities and real issue types
- manual setup
- a very narrow and repeatable offer

The more important short-term checkpoints are:
- first paid vendor this month
- 3 to 5 paid vendors before trying to optimize a full self-serve flow
- repeated use from the same office before broadening product scope

## 16) How to find the first vendor prospects
The initial outreach motion should start with companies already visible inside the real backflow workflow, not generic broad contractor lists.

The first prospect channels are:

### A) Official tester lists and registration PDFs
Use utility and city pages that publish registered or approved tester lists.

Examples confirmed in current sources:
- Grand Prairie publishes a registered tester list and annual registration requirement
- Dallas Water Utilities requires licensed testers to register and submit through SwiftComply
- Arlington points testers to Envirotrax and a registered BPAT lookup

These lists are useful because they identify companies already active in the exact compliance workflow.

### B) Portal-linked tester ecosystems
Use utilities and portal providers that reveal the workflow stack:
- SwiftComply
- BSI Online
- VEPO / Envirotrax

These are not just product references. They are signals that:
- the utility has an active compliance workflow
- testers in that city are already doing repeated annual work
- the local market likely has real recurring backflow volume

### C) Company website enrichment
Once a company appears on an official list, enrich it with:
- website
- owner or office phone
- office email
- service area
- whether the company does only backflow or broader plumbing and irrigation work

This helps identify the best early ICP:
- small but busy
- office function exists
- repeated backflow jobs
- not obviously locked into a large operating stack

### D) Geographic wedge first
The first outbound wedge should stay narrow:
- DFW first
- utilities already covered well by the product
- annual notice and failed-test scenarios only

This is important because the outreach pitch should show a real local handoff example, not a generic SaaS pitch.

## 17) Prospect list rules
The first target sheet should track:
- company name
- utility or city where they are already registered
- official-list source URL
- website
- phone
- office email if available
- service mix: backflow only, irrigation, plumbing, fire, mixed
- apparent size: solo, small office, larger team
- likely fit: high, medium, low
- notes for the first outreach angle

The goal is not to build a massive lead list first.
The goal is to build a tight list of likely-fit shops that match the first wedge.

## 18) What should not be built yet
Do not let this feature expand into a full vendor suite before real reuse is proven.

Things that should stay out of scope for now:
- full account systems
- team permissions
- broad automation sequences
- large workflow builders
- generalized CRM behavior
- broad expansion beyond backflow-specific communication pain

## 19) What should be measured next
The most important validation questions are operational, not theoretical.

Measure:
- whether the same vendor reuses the brief more than once
- whether annual notice and failed-test are both strong enough to support repeat use
- whether customers open the brief or PDF
- whether vendors ask for saved presets, templates, or branding
- whether the tool reduces phone explanation and back-and-forth work

## 20) Decision rules for future agents
- Keep the feature definition narrow until repeated reuse is proven.
- Do not reframe this as a full vendor SaaS without evidence.
- Prefer workflow value over dashboard breadth.
- Prefer direct revenue experiments over speculative feature expansion.
- Treat setup-led revenue as the likely first commercial win.
- Keep the feature anchored to annual notice and failed-test until the first paid wedge is real.
- Treat official tester lists, portal-linked utility pages, and narrow metro slices as the first outbound prospect sources.

## 21) Open questions
- Which sender persona reuses the brief fastest in practice?
- Is the first buyer a backflow testing shop, a repair-heavy plumbing shop, or a mixed-service operator?
- Is a branded preset enough, or will vendors immediately ask for history and team reuse?
- Does a paid pilot close faster with setup fee plus subscription, or with one simple monthly fee?
- How much value comes from customer opens and download tracking versus the core brief itself?
- Which official-list and portal-backed metros produce the highest-density prospect pool for the first manual push?

## 22) Current practical conclusion
The vendor feature should first be locked as a narrow, repeatable handoff workflow.

Only after that should BackflowPath decide whether it is becoming:
- a lightweight paid vendor workspace
- a setup-led service with recurring revenue
- or a larger vendor product

For now, the safest strategic position is:

BackflowPath is building a backflow-specific customer handoff tool that may later become a lightweight vendor product, but it should earn the right to become that by proving repeated use first.

## 23) Current monetization ladder to lock
The product should now be understood as a three-layer ladder, but not as three equally mature revenue motions.

### Layer 1: Free sendable wedge
Purpose:
- let a vendor office send one real customer brief today
- create customer recovery and qualified BackflowPath traffic
- prove that the artifact is useful in a real annual notice or failed-test job

What free must already do well:
- create a customer-ready brief
- create a link and PDF
- show the local rule context and official program proof
- show the vendor contact line
- remain strong enough that a vendor would actually send it

Free should feel complete enough for one live job.
If free is too weak, the paid offer below it will also feel weak.

### Layer 2: Paid setup
Purpose:
- align the free workflow to one vendor office so it becomes ready-to-send for that company

What setup sells:
- company name, phone, email, and sender defaults
- annual notice and failed-test default copy
- PDF header and wording alignment
- sample briefs for the vendor's common utilities
- one round of adjustment

This is the current first paid motion.
It should be framed as customer-brief workflow setup, not as a broad software subscription.

### Layer 3: Paid recurring plan
Purpose:
- remove repeated office friction once real reuse exists

What recurring revenue should eventually sell:
- stored company presets
- utility-specific templates
- issue-specific default wording
- last-brief reuse and regeneration
- office memory and history
- repeated-use reporting and light tracking continuity

Recurring revenue should only be treated as real once BackflowPath proves that the same office wants to avoid repeated setup work.

## 24) Why all three can exist, but should not be pushed equally at once
Conceptually, all three layers can exist at the same time:
- free
- paid setup
- paid recurring

But the commercial motion should not treat them as equally ready.

Why:
- free proves one sendable success
- setup proves the office wants this workflow customized to its own brand and wording
- recurring only makes sense after the office feels real repeat friction

If BackflowPath tries to sell recurring too early, the offer becomes fuzzy:
- the vendor hears "software subscription"
- but the real current value is still first-time alignment and faster initial use

The right sequence is:
1. free makes the first send believable
2. setup makes the first office adopt it
3. recurring only appears once reuse and memory become real

So the practical answer is not "do not have all three."
The practical answer is:

BackflowPath can expose all three layers conceptually, but it should sell free plus setup first and treat recurring as secondary until repeat use is proven.

## 25) Current revenue thesis for the next 3 months
The near-term revenue goal should not be modeled as a self-serve SaaS goal.

The current working target is:
- treat the next 3 months as a setup-win target, not an MRR target
- read the first cash proof as a handful of paid setup engagements, not as broad subscription adoption

The practical implication:
- the first commercial proof is not 20 subscribers
- the first commercial proof is several paid vendor setups in a narrow DFW wedge

This is the right framing because:
- the current product already supports a useful free sendable artifact
- setup value is easier to explain than recurring software value
- the product is still learning what repeat-office workflow actually matters

## 26) Current pricing shape to assume
Pricing should remain simple and low-complexity at this stage.

The current public pilot price is now locked as:
- $149 one-time for one office

What that public pilot price covers:
- annual notice plus failed-test workflow alignment
- company sender defaults
- PDF wording cleanup
- about 1 to 3 starter utilities
- one revision round

Important pricing rule:
- do not charge monthly for one-time branding work alone
- charge setup for initial alignment
- do not publicly sell recurring yet
- revisit recurring only after repeat use proves that memory, reuse, and continuity are real needs

## 27) Co-branding versus white-label
The current preferred position is co-branded, not fully white-labeled.

Why co-branded fits the current strategy better:
- the vendor still gets a customer-facing artifact that feels like its own workflow
- the customer still sees proof rails and recovery paths back into BackflowPath
- BackflowPath keeps the organic recovery loop instead of surrendering it too early

The current preferred structure is:
- vendor-first presentation
- BackflowPath proof rail, official rule access, or small recovery footer

Full white-label may become a later premium option, but it should not be the default now because the current strategy still depends on customer recovery and source-backed trust.

## 28) Setup package framing to lock
The setup offer should not be framed as:
- branding help
- PDF customization
- SaaS onboarding

The stronger framing is:

BackflowPath sets up one vendor office's annual-notice and failed-test customer-brief workflow so that office can send its own customer-ready links or PDFs without rewriting the same explanation each time.

That means setup is selling office readiness, not feature breadth.

## 29) What the setup package should include
The current locked setup scope is:
- one office
- company name, phone, email, and sender-line alignment
- one annual-notice default template
- one failed-test default template
- PDF wording and header cleanup
- about 1 to 3 utility-specific sample briefs
- one revision round
- one short onboarding step

This is enough to create a paid alignment layer without pretending BackflowPath is already a full vendor platform.

## 30) What setup should exclude for now
To stay sellable and not collapse into custom consulting, setup should exclude:
- unlimited utilities
- unlimited revisions
- all issue types
- team permissions
- CRM behavior
- auto email or SMS sending
- portal integration
- utility submission replacement
- default full white-label

If those needs dominate, the office is asking for a different product tier.

## 31) Setup intake to keep short
The recommended setup intake should stay close to the output:
- company name
- office phone and email
- preferred sender line
- the first 1 to 3 utilities to support
- one annual-notice example
- one failed-test example
- any required next-step wording
- one office reviewer
- onboarding preference: short call or recorded walkthrough

This intake is intentionally short because the setup package should feel like alignment work, not discovery-heavy consulting.

## 32) Current setup operating flow to lock
The current sales and delivery flow should remain manual and email-first.

Locked flow:
1. vendor emails in from the setup CTA
2. BackflowPath confirms fit for one office and the annual/failed wedge
3. vendor sends the short intake
4. payment happens before work begins
5. first draft is delivered within 3 business days after payment and intake are complete
6. one revision round closes the setup

This is intentionally simple because the product is still proving the paid wedge, not scaling onboarding.
