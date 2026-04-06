@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param StateGuideRecord stateGuide
@param java.util.List<UtilityRecord> featuredUtilities
@param java.util.List<UtilityRecord> allUtilities
@param java.util.List<GuideRecord> guides

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Representative state</span>
                <h1>${stateGuide.title()}</h1>
                <p class="lede">${stateGuide.summary()}</p>
                <div class="hero__stats">
                    <span>Last verified: ${stateGuide.lastVerified().toString()}</span>
                    <span>${allUtilities.size()} live utility pages in this state</span>
                </div>
            </div>
            <aside class="detail-card detail-card--key">
                <span class="stat-card__label">Metadata</span>
                <div class="metadata-grid">
                    <div class="metadata-block">
                        <strong>Jurisdiction</strong>
                        <span>${stateGuide.state()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Reviewer</strong>
                        <span>${stateGuide.reviewerInitials()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Freshness</strong>
                        <span>${stateGuide.staleAfterDays()} days</span>
                    </div>
                </div>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <div class="section-heading">
                    <span class="eyebrow">Statewide rule floor</span>
                    <h2>${stateGuide.title()}</h2>
                    <p>${stateGuide.authoritySummary()}</p>
                </div>
                <ul class="detail-list">
                    @for (var highlight : stateGuide.statewideHighlights())
                        <li>${highlight}</li>
                    @endfor
                </ul>
            </article>

            <article class="panel panel--dark">
                <div class="visual-card" style="min-height: 100%;">
                    <img src="/images/design/state-map-texas.jpg" alt="Technical map style illustration used for representative state coverage">
                    <div class="visual-card__overlay">
                        <strong>Regional distribution</strong>
                        <p>State hubs widen discovery while utilities remain the operational pages where the real testing workflow lives.</p>
                    </div>
                </div>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Featured utilities</span>
                <h2>Flagship utility pages with the clearest public workflows</h2>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : featuredUtilities)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${utility.testingFrequency()}</span>
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

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="panel panel--dark">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(197, 234, 223, 0.92);">Technical evergreen layer</span>
                    <h2 style="color: #ffffff;">Guides that support this state</h2>
                    <p>State pages create a clean bridge into evergreen reading paths without pretending a state summary is enough to act on.</p>
                </div>
                <div class="support-links">
                    @for (var guide : guides)
                        <a class="support-link" href="/guides/${guide.slug()}">
                            <div>
                                <strong>${guide.title()}</strong>
                                <span>${guide.description()}</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    @endfor
                </div>
            </article>

            <article class="detail-card">
                <div class="visual-card">
                    <img src="/images/design/state-tech-equipment.jpg" alt="Technical backflow testing equipment in a mechanical room">
                    <div class="visual-card__overlay">
                        <strong>Source-backed state layer</strong>
                        <p>${stateGuide.sourceExcerpt()}</p>
                    </div>
                </div>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">All live utilities</span>
                <h2>All live utilities</h2>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : allUtilities)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${utility.testingFrequency()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Utility page</a>
                            @if (utility.supportsAnnualTestingPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/annual-testing">Annual testing</a>
                            @endif
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/failed-test">Failed test</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>
`)
