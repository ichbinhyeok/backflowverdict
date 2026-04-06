@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Corrections</span>
                <h1>How BackflowPath handles errors and stale rules</h1>
                <p class="lede">If a utility page, guide, or provider record no longer matches the source material, the correct response is to review the source, update the record, or suppress the page from indexing until it is supported again. A correction is not just a cosmetic content edit. On this site it is part of the publishing control system.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/contact">Send a correction</a>
                    <a class="button button--secondary" href="/methodology">Review methodology</a>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Correction standard</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Verify against the source</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">BackflowPath does not treat anecdotal reports or provider sales claims as sufficient evidence for changing an authority rule.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Good correction reports</span>
                <h2>What to include</h2>
                <ul class="detail-list">
                    <li>The exact BackflowPath URL that appears wrong</li>
                    <li>The utility or authority source URL that conflicts with it</li>
                    <li>A short explanation of what changed</li>
                    <li>The date you saw the change</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">What happens next</span>
                <h2>Review and update path</h2>
                <ul class="detail-list">
                    <li>Source evidence is checked against the current public page.</li>
                    <li>If the correction is confirmed, the registry record is updated and the page is republished.</li>
                    <li>If the source support is broken or unclear, the page should be downgraded or removed from indexing until it is re-verified.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Commercial boundary</span>
                <h2>Provider disputes do not override utility rules</h2>
                <p>Provider feedback can help identify stale links, bad phone numbers, or broken routes, but it does not override the utility's own published workflow or approval status.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Correction triggers</span>
                <h2>What usually causes a correction review</h2>
                <ul class="detail-list">
                    <li>An official source URL changes or breaks.</li>
                    <li>A utility changes a deadline basis, portal, or submission method.</li>
                    <li>A tester route changes from official to non-official or vice versa.</li>
                    <li>A page is still indexed even though it no longer adds enough local value.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Evidence rule</span>
                <h2>What counts as a strong correction</h2>
                <p>The best correction requests point to a current authority page, portal, list, or notice. Provider claims can help flag a problem, but the public page should be updated only when the underlying source support is strong enough to defend the change.</p>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card">
                <span class="eyebrow">Possible outcomes</span>
                <h2>What a correction review can lead to</h2>
                <ul class="detail-list">
                    <li>Direct page update when the source clearly supports the change</li>
                    <li>Route reclassification when an official list is no longer real or a non-official route was mislabeled</li>
                    <li>Temporary suppression from indexing while evidence is rechecked</li>
                    <li>No public change when the submitted evidence is not strong enough</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Public expectation</span>
                <h2>What users should expect from this policy</h2>
                <p>BackflowPath should prefer being incomplete over being confidently wrong. If a page cannot be supported to a reasonable standard, the right choice is to weaken it, hide it, or remove it from search rather than leave a misleading answer in place.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Next step</span>
                <h2>How to report an issue well</h2>
                <div class="card-actions">
                    <a class="button button--secondary" href="/contact">Contact</a>
                    <a class="button button--secondary" href="/methodology">Methodology</a>
                    <a class="button button--secondary" href="/editorial-standards">Editorial standards</a>
                </div>
            </article>
        </div>
    </section>
`)
