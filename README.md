# BackflowVerdict

**Date:** 2026-04-04 (Asia/Seoul)  
**Purpose:** This folder is a self-contained design packet for building a US-focused **backflow testing and compliance decision site** with local-rule SEO and direct sponsor monetization.

## What you are building
A utility-first compliance site that helps owners and managers answer whether backflow testing is required, when it is due, what a failed test means, what it will cost, and who can handle it locally.

## Why this concept is attractive
- Search intent is highly action-oriented.
- Demand is recurring because many assemblies require annual testing.
- The moat is data normalization, not writing generic plumbing articles.
- Direct local sponsor revenue is more natural than waiting on lead-network approvals.

## File map
- `AGENT_START_HERE.md` - read order and handoff rules for any future agent
- `ops/context_tracker.md` - current status, decisions, and next tasks
- `spec/00_strategy.md` - market thesis, positioning, core wedge, and rollout philosophy
- `spec/01_query_and_user_map.md` - jobs-to-be-done, query families, funnel logic, and priority page sets
- `spec/02_site_architecture.md` - canonical entities, URL graph, page modules, schema, and internal linking
- `spec/03_data_and_operations.md` - source classes, registry schema, verification workflow, and update cadence
- `spec/04_commercial_model.md` - sponsor model, provider onboarding, CTA logic, and consent/lead handling
- `spec/05_editorial_rules_and_execution.md` - writing rules, trust guardrails, delivery phases, KPIs, and definition of done
- `spec/06_indexing_quality_and_analytics.md` - indexing rules, quality gates, kill rules, and measurement plan
- `spec/07_technical_architecture.md` - system boundaries, package map, rendering model, and operational services
- `spec/08_delivery_and_handoff.md` - workstreams, milestones, acceptance criteria, and handoff order

## Package root
`owner.backflow`

## Agent read order
1. `AGENT_START_HERE.md`
2. `ops/context_tracker.md`
3. This file
4. `spec/00_strategy.md` through `spec/08_delivery_and_handoff.md`

## Build principles
- Treat the governing utility or water authority as the canonical entity.
- Show exact local sources and `last verified` dates on every local page.
- Separate official requirements from sponsor content with zero ambiguity.
- Build for boring trust, not broad consumer traffic.
- Launch storage is file-backed: JSON is the source of truth and CSV is used for bulk imports, exports, and outreach lists.
- No database is required for launch.
- Texas is the current seeded baseline because the codebase already has the deepest utility coverage there, but the product strategy is representative multi-state expansion, not Texas-only depth forever.
- Agents may add new states when those utilities satisfy the same official-source, verification, and stale-page rules used for Texas.
