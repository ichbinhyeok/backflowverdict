# 03 Data And Operations

## 0) Storage model
- No database at launch.
- JSON is the source of truth for utilities, rule interpretations, page eligibility, and verification history.
- CSV is used for bulk imports, exports, outreach lists, and provider inventories that are easy to review manually.
- Every published page must be derivable from versioned files plus linked official sources.

## 1) Source classes
- Utility manuals and compliance program pages
- Municipal code pages
- Approved tester lists
- Submission forms and portals
- Fee schedules
- Contact pages for cross-connection programs

## 2) Registry design
### Utility registry JSON
- path: `data/utilities/{state}/{utility-slug}.json`
- utility_id
- utility_name
- governing_entity_type
- canonical_slug
- state
- service_area_cities
- service_area_counties
- search_aliases
- utility_url
- official_program_urls
- testing_frequency
- due_basis
- covered_property_types
- covered_device_types
- approved_tester_mode
- approved_tester_list_url
- submission_methods
- phone
- penalties
- source_excerpt
- source_snapshot_path
- reviewer_initials
- stale_after_days
- page_status
- last_verified

### City alias mapping CSV
- path: `data/city_aliases.csv`
- city
- state
- utility_id
- alias_slug
- alias_mode
- justification
- last_reviewed

### Provider registry CSV
- path: `data/providers/providers.csv`
- provider_id
- provider_name
- coverage_type
- coverage_targets
- license_or_certification_notes
- official_approval_source_url
- phone/email/site
- sponsor_status
- page_label
- last_reviewed

### Operations logs
- `data/ops/change_log.jsonl`
- `data/ops/broken_links.csv`
- `data/ops/conflicts.csv`

## 3) Verification workflow
1. Capture the official source URL and save a snapshot or durable excerpt.
2. Extract exact requirement text and normalize fields into the utility JSON contract.
3. Save `last_verified`, reviewer initials, and the next stale threshold.
4. Resolve city aliases and page mode: `publish`, `hold`, `stale`, `redirect`, or `noindex-bridge`.
5. Re-check pilot markets monthly and long-tail markets on the defined cadence.
6. Mark stale and suppress indexing when links break, rules change, or the stale threshold is exceeded.

## 4) Refresh cadence
- Pilot utilities: monthly, stale after 45 days
- State hubs: monthly
- Evergreen guides: quarterly
- Long-tail utilities: every 90-120 days, stale after 120 days

## 5) Data quality gates
- Never infer "approved tester" status without source support.
- Never guess testing frequency.
- Never publish if only secondary sources exist.
- Never publish an `approved-testers` page unless `approved_tester_mode` is backed by an official list.
- Never publish a city alias without a row in `data/city_aliases.csv`.
- Keep an operations log for broken links and conflicting rules.

## 6) Scale strategy
- Start with 1 baseline state where public utility documentation is strong. The current seed set uses Texas, but the model is not meant to stay single-state.
- Normalize the file contracts before adding more utilities.
- Add one representative second state as soon as the team can preserve the same source evidence, verification cadence, and stale-page suppression rules there.
- Expand only after the first 25-50 utilities across states can be updated repeatably from the JSON and CSV source files.
