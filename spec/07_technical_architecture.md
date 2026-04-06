# 07 Technical Architecture

## Package root
`owner.backflow`

## Recommended application shape
- Spring Boot SSR application
- JTE templates
- File-backed utility or district rule store
- JSON as the canonical application data format
- CSV for provider inventories, alias mappings, and exports
- No relational database at launch
- Scheduled refresh jobs for rules and approved-tester lists

## Package map
- `owner.backflow.web`
- `owner.backflow.pages`
- `owner.backflow.data`
- `owner.backflow.files`
- `owner.backflow.ingest`
- `owner.backflow.seo`
- `owner.backflow.leads`
- `owner.backflow.analytics`
- `owner.backflow.ops`

## Core services
- Rule file loader
- Provider CSV loader
- City alias resolver
- Page spec builder
- Page eligibility evaluator
- Source freshness auditor
- Snapshot and change-log writer
- CTA routing service
- Sitemap policy service

## Operational requirements
- Read and write the JSON and CSV contracts without a database dependency
- Refresh rules and registries on defined cadence
- Suppress stale or unverifiable pages
- Keep provider listings separate from official lists
