# 06 Indexing Quality And Analytics

## Indexing principle
Only index pages with real local compliance value.

## Index by default
- Utility or district rule pages
- Pages with published forms, registries, or explicit annual testing logic
- Failed-test pages with documented next steps

## Index selectively
- Property-type or device-type pages
- Approved-tester pages with an official source list
- Find-a-tester pages with provider inventory and explicit non-official labeling
- Metro discovery pages

## Noindex rules
- Pages with no official local source
- Thin city aliases
- City bridge pages marked `noindex-bridge`
- Approved-tester pages with no official list
- Directory pages with no registry or public provider inventory

## Quality gates
- Official source present
- Visible `last verified` date
- Source snapshot or excerpt stored in the file registry
- Clear next-step workflow
- Page mode set to `publish`
- Clear distinction between official guidance and non-official directory content

## Core analytics
- Organic landings by utility or district
- CTA clicks by page family
- Approved-tester CTR
- Failed-test page request-help rate
- Stale page rate

## Kill rules
- Review local pages that have fewer than 10 impressions after 60 days in the sitemap and at least 3 internal links.
- Noindex provider discovery pages that have at least 150 organic landings and less than 1 percent CTA click-through rate over 30 days.
- Convert city aliases to 301 redirects when the canonical utility page outranks them for 28 consecutive days or the alias adds no distinct enforcement content.
- Auto-suppress pilot utility pages when `last_verified` exceeds 45 days or a broken source link remains unresolved for 7 days.
