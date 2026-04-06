@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<StateGuideRecord> stateGuides
@param java.util.Map<String, Integer> utilityCounts
@param int publishedUtilityCount
@param java.util.List<UtilityRecord> featuredUtilities

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Browse by state</span>
                <h1>Start with a state guide, then open the exact utility page.</h1>
                <p class="lede">State guides help you understand the broader local landscape without pretending a statewide summary is enough to act on. Use them to narrow down the right utility page, then follow the exact testing and submission workflow there.</p>
                <div class="hero__actions">
                    @if (!stateGuides.isEmpty())
                        <a class="button button--primary" href="/states/${stateGuides.get(0).state()}/backflow-testing">Open a state guide</a>
                    @endif
                    <a class="button button--secondary" href="/guides">Browse common questions</a>
                    <a class="button button--secondary" href="/leads/new">Request help</a>
                </div>
                <div class="hero__stats">
                    <span>${stateGuides.size()} published state guides</span>
                    <span>${publishedUtilityCount} mapped utility pages</span>
                    <span>Utility pages remain canonical</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">What these pages do</span>
                <div class="stat-grid">
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Scope</strong>
                        <span style="color: #ffffff;">Statewide context</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Best next step</strong>
                        <span style="color: #ffffff;">Open the local utility page</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Use case</strong>
                        <span style="color: #ffffff;">Find the right local workflow</span>
                    </div>
                </div>
                <p class="detail-meta" style="color: rgba(239, 241, 243, 0.74);">These pages are for orientation. The exact requirement still lives on the utility page that governs the property.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Published state guides</span>
                <h2>Choose a state, then drop into the right local authority</h2>
                <p class="lede">Each state guide summarizes statewide context, highlights the strongest utility pages in that state, and links into the next page a real user would need.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var stateGuide : stateGuides)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${stateGuide.state()}</span>
                        <h3>${stateGuide.title()}</h3>
                        <p>${stateGuide.description()}</p>
                        <ul class="detail-list">
                            <li>${utilityCounts.getOrDefault(stateGuide.state(), 0)} live utility pages</li>
                            <li>Last verified ${stateGuide.lastVerified().toString()}</li>
                            <li>${stateGuide.reviewerInitials()} verification code</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/states/${stateGuide.state()}/backflow-testing">Open state guide</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">Why this matters</span>
                <h2>Useful statewide context without generic SEO drift</h2>
                <p>These pages help users orient themselves, compare state-level patterns, and discover the right local authority page. They are intentionally support pages, not replacements for the local rule.</p>
            </article>

            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">What to expect</span>
                    <h2>The local workflow still wins</h2>
                    <p>State guides are most helpful when a user knows the state but not the utility. Once the utility is identified, the next click should be the utility page or one of its next-step pages.</p>
                </div>
                <div class="card-actions">
                    <a class="button button--secondary" href="/metros">Browse metro pages</a>
                    <a class="button button--secondary" href="/guides">Browse backflow guides</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section section--muted">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Featured utilities</span>
                <h2>Strong local pages across the published states</h2>
                <p class="lede">These utility pages show the level of specificity the product aims for once a user reaches the correct authority.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : featuredUtilities)
                    <article class="detail-card">
                        <span class="pill">${utility.state()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility page</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>
`)
