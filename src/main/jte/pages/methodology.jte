@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Methodology</span>
                <h1>How BackflowPath verifies local rules</h1>
                <p class="lede">Each published page is built from source-backed utility or authority material, normalized into the file registry, reviewed on a freshness window, and updated or suppressed when the source record no longer supports the public page. The method is intentionally conservative because local compliance pages can create expensive or safety-sensitive mistakes when they drift.</p>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Review model</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Source -> verify -> publish</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">The public page is only as strong as the current source evidence. When source evidence weakens, the page should be refreshed, downgraded, or removed from indexing.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Step 1</span>
                <h2>Resolve the governing entity</h2>
                <p>BackflowPath starts by identifying the utility, district, or authority that actually controls testing, submission, or enforcement. City aliases and metro clusters are secondary discovery paths, not the compliance source of truth.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Step 2</span>
                <h2>Capture exact source evidence</h2>
                <p>Rule fields are extracted from public authority materials such as utility program pages, published tester lists, reporting portals, and related official instructions. The page stores source links, source excerpt text, review date, and a short verification code from the registry process.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Step 3</span>
                <h2>Apply freshness thresholds</h2>
                <p>Utilities and guides carry freshness windows in the registry. When the source is broken, the rule changes, or the review window expires, the page should be re-verified or suppressed from indexing rather than left to drift.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Source hierarchy</span>
                <h2>What counts as strong source evidence</h2>
                <ul class="detail-list">
                    <li>Utility or water authority program pages</li>
                    <li>Official tester lists or reporting portals published by the governing program</li>
                    <li>Authority PDFs, manuals, notices, and ordinance-linked guidance</li>
                    <li>Related public instructions that clarify submission timing, penalties, or who is affected</li>
                </ul>
                <p class="detail-meta">Commercial provider sites, generic directories, or anecdotal forum claims are not enough to define the official rule.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Extraction rule</span>
                <h2>How public pages are assembled</h2>
                <p>BackflowPath does not publish a local page from a loose summary alone. The page needs enough structured evidence to support fields such as testing cadence, due basis, submission methods, penalties, tester route classification, and the public source block.</p>
                <ul class="detail-list">
                    <li>If the source cannot support a field, that field should stay blank or the page should stay unpublished.</li>
                    <li>If a city search term maps to a utility, the city route should stay secondary to the utility page.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Update discipline</span>
                <h2>Why freshness is part of the method</h2>
                <p>This site is not trying to win by volume. It wins by staying more legible and more current than a thin programmatic page set. That means source decay is a publishing issue, not a cosmetic issue.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Verification codes</span>
                <h2>How verification is disclosed today</h2>
                <p>Current public records expose a verification code and review date on-page. The code is a registry provenance marker tied to the internal review log, not a public staff profile.</p>
                <ul class="detail-list">
                    <li id="verification-code-tl"><strong>TL</strong> - verification code currently attached to the published registry set in this repository</li>
                </ul>
                <p class="detail-meta">If named staff profiles are added later, they should be supported by real public identities and not inferred from the current code field alone.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Freshness logic</span>
                <h2>How stale content is handled</h2>
                <ul class="detail-list">
                    <li>Utilities carry short freshness windows because local program rules can change quickly.</li>
                    <li>Guides carry longer windows because they summarize repeated patterns, not one utility's current rule.</li>
                    <li>If a page loses source support, the correct action is refresh, noindex, or removal, not silent drift.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Publication gates</span>
                <h2>What must be true before a page stays live</h2>
                <ul class="detail-list">
                    <li>The governing entity is resolved with enough confidence to support a canonical local page.</li>
                    <li>The page has source links, source excerpt support, freshness metadata, and a clear route classification.</li>
                    <li>The commercial layer does not obscure the official workflow.</li>
                    <li>The page adds real local value instead of paraphrasing a generic guide.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Suppression rules</span>
                <h2>When a route should be downgraded or removed</h2>
                <ul class="detail-list">
                    <li>The source no longer supports the field claims shown on-page.</li>
                    <li>The route becomes a thin bridge with little utility-specific value.</li>
                    <li>The official tester route classification is no longer defensible.</li>
                    <li>Freshness thresholds expire without re-verification.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--muted">
        <div class="container content-grid">
            <article class="detail-card">
                <span class="eyebrow">Public page signals</span>
                <h2>What a strong page should show</h2>
                <ul class="detail-list">
                    <li>Official workflow before commercial routing</li>
                    <li>Source block and submission path near the top of the page</li>
                    <li>Verification code, review date, and correction path</li>
                    <li>Clear distinction between official tester routes and non-official directories</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Related policies</span>
                <h2>Follow-up pages</h2>
                <div class="card-actions">
                    <a class="button button--secondary" href="/about">About</a>
                    <a class="button button--secondary" href="/editorial-standards">Editorial standards</a>
                    <a class="button button--secondary" href="/corrections">Corrections policy</a>
                    <a class="button button--secondary" href="/contact">Contact</a>
                </div>
            </article>
        </div>
    </section>
`)
