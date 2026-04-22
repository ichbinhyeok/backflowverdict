# Context Tracker

## Current status
- BackflowPath is tracked as an owner-facing utility-first compliance product.
- The public site centers on utility, state, metro, guide, tester, failed-test, provider, and request-help pages.
- Lead capture remains file-backed and manually reviewed through `/admin`.
- Public provider browse remains secondary to the governing utility workflow.

## Latest decisions
- Keep the governing utility or water authority as the canonical entity.
- Keep official guidance and public provider directories visibly separate.
- Do not reintroduce private setup or internal routing surfaces into the published experience.
- Manual request review is allowed; private internal routing workflows are not part of the active product.
- Freshness, verification, and source clarity outrank conversion experiments.

## What changed this session
- Removed legacy internal-routing language from the active read order and public docs.
- Renamed the support and execution specs to match the owner-facing product model.
- Rewrote privacy, request-help, corrections, and failed-test template copy toward public workflow language.
- Kept public provider browse framed as secondary support after the governing rule is visible.

## Next recommended tasks
1. Keep scrubbing residual internal-routing language from legacy implementation notes that are no longer part of the public product.
2. Decide whether internal compatibility fields such as legacy provider status columns should be renamed later or simply documented as implementation leftovers.
3. Keep widening source-backed utility coverage and public provider inventory where official lists support it.
4. Keep hardening manual review, freshness, and indexing controls on the public owner-facing surface.

## Open questions
- Whether legacy internal provider-status fields should be renamed in code and data files after the public cleanup is complete.
- How far metro and provider aggregation should expand before it begins to dilute utility-first trust.
- Whether request-help follow-up needs a more explicit public service-level expectation on the site.
