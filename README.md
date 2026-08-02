# BackflowVerdict on BackflowPath

**Date:** 2026-04-22 (Asia/Seoul)  
**Purpose:** A US-focused decision product that starts with device/problem intent and connects the result to source-backed BackflowPath utility records.

## 2026-08-02 product reset and strengthened portfolio

The primary product is no longer a directory-shaped collection of local pages. It is a decision system:

1. Identify the device or the event.
2. Observe the symptom without claiming a remote diagnosis.
3. Decide the safe next step.
4. Verify the tester, deadline, and report route with the governing utility.

The existing utility data and GSC winner URLs are preserved. New national decision content lives in `data/decision-guides/` and is loaded by `DecisionGuideService`; it is not copied into the local `UtilityRecord` model.

The core now includes 26 explicit, data-backed Wave A/B decision routes, the homepage, report-submission finder, utility records, and the noindex help handoff. The portfolio covers backflow assemblies, vacuum breakers, irrigation protection, backwater valves, and residential water-pressure regulators. Design tokens and content rules are in `DESIGN.md`.

Interactive modules include device and symptom routing, repair-versus-replacement scoring, static/flowing PSI interpretation, and planning-range calculators. Every decision guide displays a centralized official/manufacturer evidence ledger and emits Article plus BreadcrumbList structured data.

## What you are building
An owner-facing product that helps homeowners, property managers, and facility coordinators identify water-control devices, handle leaks or failed tests, and verify the correct local compliance closeout.

## Product posture
- The governing utility or water authority is the canonical local entity.
- Official rules and dates outrank every other surface.
- Public provider directories and request-help pages are optional support layers, not the main answer.
- Launch storage stays file-backed: JSON is the source of truth and CSV supports imports, exports, and review-friendly inventories.
- No database is required for launch.

## File map
- `AGENT_START_HERE.md` - read order and continuity rules for future agents
- `ops/context_tracker.md` - current status, decisions, and next tasks
- `spec/00_strategy.md` - product thesis, user model, and rollout philosophy
- `spec/01_query_and_user_map.md` - jobs-to-be-done, query families, and priority page sets
- `spec/02_site_architecture.md` - canonical entities, URL graph, page modules, schema, and internal linking
- `spec/03_data_and_operations.md` - source classes, registry schema, verification workflow, and update cadence
- `spec/04_public_support_model.md` - request-help rules, public provider directory posture, and privacy constraints
- `spec/05_editorial_rules_and_execution.md` - writing rules, trust guardrails, delivery phases, KPIs, and definition of done
- `spec/06_indexing_quality_and_analytics.md` - indexing rules, quality gates, kill rules, and measurement plan
- `spec/07_technical_architecture.md` - system boundaries, package map, rendering model, and operational services
- `spec/08_delivery_and_execution.md` - execution workstreams, milestones, acceptance criteria, and sequencing

## Package root
`owner.backflow`

## Agent read order
1. `AGENT_START_HERE.md`
2. `ops/context_tracker.md`
3. This file
4. `spec/00_strategy.md` through `spec/08_delivery_and_execution.md`

## Build principles
- Show exact local sources and `last verified` dates on every local page.
- Separate official requirements from public provider directories and request-help surfaces with zero ambiguity.
- Build for trust and clarity, not generic traffic.
- Texas is the current seeded baseline because the codebase already has the deepest utility coverage there, but the product strategy is representative multi-state expansion, not Texas-only depth forever.
- Agents may add new states when those utilities satisfy the same official-source, verification, and stale-page rules used for Texas.

## Deployment
- Container image: `shinhyeok22/backflow`
- Runtime port mapping: external `8093` to internal `8080`
- Deploy path: GitHub Actions workflow at `.github/workflows/deploy.yml`
- Runtime memory cap: `mem_limit: 512m` in `docker-compose.yml`
- This project stays on Java 21 because Spring Boot 4 requires it.
- Public hostname routing is a separate layer from the container deploy. The app can be healthy on `127.0.0.1:8093` while `https://backflowpath.com/` still serves another site if nginx or Cloudflare is pointing at the wrong upstream.
- The deploy workflow checks both the container health endpoint and a host-routed `Host: backflowpath.com` request on the OCI host so a wrong public upstream fails visibly.
- nginx reference config: `ops/nginx/backflowpath.conf`
