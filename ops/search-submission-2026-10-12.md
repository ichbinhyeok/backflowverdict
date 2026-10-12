# Post-expansion Search Console submissions

Property: `sc-domain:backflowpath.com`. Date: October 12, 2026.

The owner requested sitemap and manual URL submissions after the installation, diagram and leak content releases.

- Re-submitted the reachable, valid XML `sitemap-index.xml`, `sitemap.xml` and `sitemap-priority.xml` through the authorized GSC Wizard OAuth connector. Google accepted and confirmed all three at 11:04:42 UTC / 20:04:42 KST.
- The main sitemap contains 1,323 URLs and the priority sitemap 447; both include all five updated guides. The sitemap index references nine child sitemaps. Counts overlap and are not additive unique URL totals.
- A subsequent GSC read confirmed the main sitemap downloaded at 11:04:45 UTC, with zero reported warnings/errors. Index and priority download status remained pending at that read.
- Inspected all five guides: each returned PASS, Submitted and indexed, INDEXING_ALLOWED, and matching Google/user canonicals. Last reported crawls were September 13–29, before the new content release.
- Separately used the authenticated Search Console browser to request indexing for installation, diagram, general leaking, vacuum-breaker leaking and double-check-valve leaking. All five displayed the success receipt confirming addition to the priority crawl queue, approximately 20:04–20:09 KST.

Raw API results, live XML validation and the five browser receipt/page screenshots are retained privately in `analysis/salvage_2026-10-12/`. API inspection is a status check; it is not the manual submission action. Request acceptance does not confirm completed recrawl, updated search results, traffic growth or revenue. No repeated same-URL requests or recurring monitoring were created. The production runtime remains `79a0659` and the recovery baseline/review gates are unchanged.
