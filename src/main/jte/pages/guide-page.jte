@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param GuideRecord guide
@param java.util.List<StateGuideRecord> stateGuides = java.util.List.of()
@param java.util.List<MetroRecord> relatedMetros = java.util.List.of()
@param java.util.List<UtilityRecord> relatedUtilities = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Evergreen guide</span>
                <h1>${guide.title()}</h1>
                <p class="lede">${guide.intro()}</p>
                <div class="hero__stats">
                    <span>Last reviewed: ${guide.lastReviewed().toString()}</span>
                    <span>Verification code: <a href="/methodology#verification-code-${guide.reviewerInitials().toLowerCase()}">${guide.reviewerInitials()}</a></span>
                    <span>Freshness window: ${guide.staleAfterDays()} days</span>
                </div>
                @if (!relatedUtilities.isEmpty())
                    <div class="hero__actions">
                        <a class="button button--primary" href="/utilities/${relatedUtilities.get(0).state()}/${relatedUtilities.get(0).canonicalSlug()}/">Open a mapped utility page</a>
                    </div>
                @endif
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Guide record</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Evidence-backed guide</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">${guide.callout()}</p>
                <div class="metadata-grid" style="margin-top: 1rem;">
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Slug</strong>
                        <span style="color: #ffffff; font-size: 0.95rem; line-height: 1.5;">${guide.slug()}</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Source excerpt</strong>
                        <span style="color: #ffffff; font-size: 0.95rem; line-height: 1.5;">${guide.sourceExcerpt()}</span>
                    </div>
                </div>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <div class="section-heading">
                    <span class="eyebrow">Why this page exists</span>
                    <h2>Support the local pages without blurring the official rule</h2>
                    <p>${guide.callout()}</p>
                </div>
                <div class="stat-grid">
                    <div class="metadata-block">
                        <strong>Mapped utilities</strong>
                        <span>${relatedUtilities.size()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Mapped metros</strong>
                        <span>${relatedMetros.size()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>State hubs</strong>
                        <span>${stateGuides.size()}</span>
                    </div>
                </div>
            </article>

            <article class="visual-card">
                <img src="/images/design/state-tech-equipment.jpg" alt="Backflow instrumentation and technical ledger panel">
                <div class="visual-card__overlay">
                    <strong>Technical reading path</strong>
                    <p>Guides should reduce interpretation risk, then route the user back down into the exact utility rule and tester workflow.</p>
                </div>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            @for (var section : guide.sections())
                <article class="detail-card detail-card--quiet">
                    <span class="eyebrow">Guide chapter</span>
                    <h2>${section.heading()}</h2>
                    <p>${section.body()}</p>
                    @if (!section.bullets().isEmpty())
                        <ul class="detail-list">
                            @for (var bullet : section.bullets())
                                <li>${bullet}</li>
                            @endfor
                        </ul>
                    @endif
                </article>
            @endfor
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Utility layer</span>
                <h2>Use this guide with local utility pages</h2>
                @if (relatedUtilities.isEmpty())
                    <p>No related utility pages are mapped yet.</p>
                @else
                    <div class="support-links">
                        @for (var utility : relatedUtilities)
                            <a class="support-link" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">
                                <div>
                                    <strong>${utility.utilityName()}</strong>
                                    <span>${utility.verdictSummary()}</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                        @endfor
                    </div>
                @endif
            </article>

            <article class="detail-card">
                <span class="eyebrow">Metro layer</span>
                <h2>Metro clusters where this guide matters</h2>
                @if (!relatedMetros.isEmpty())
                    <div class="support-links">
                        @for (var metro : relatedMetros)
                            <a class="support-link" href="/metros/${metro.state()}/${metro.metroSlug()}/backflow-testing">
                                <div>
                                    <strong>${metro.title()}</strong>
                                    <span>${metro.description()}</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                        @endfor
                    </div>
                @else
                    <p>No metro support page is mapped to this guide yet.</p>
                @endif
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="panel panel--dark">
                <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">State support</span>
                <h2 style="color: #ffffff;">Keep the state layer in view</h2>
                @if (!stateGuides.isEmpty())
                    <ul class="detail-list">
                        @for (var stateGuide : stateGuides)
                            <li><a href="/states/${stateGuide.state()}/backflow-testing">${stateGuide.title()}</a></li>
                        @endfor
                    </ul>
                @else
                    <p>No representative state hub is published for this guide yet.</p>
                @endif
            </article>

            <article class="detail-card">
                <span class="eyebrow">Review record</span>
                <h2>How this guide is kept trustworthy</h2>
                <p>${guide.sourceExcerpt()}</p>
                <ul class="detail-list">
                    <li>Last reviewed: ${guide.lastReviewed().toString()}</li>
                    <li>Verification code: <a href="/methodology#verification-code-${guide.reviewerInitials().toLowerCase()}">${guide.reviewerInitials()}</a></li>
                    <li>Freshness window: ${guide.staleAfterDays()} days</li>
                    <li><a href="/editorial-standards">Editorial standards</a></li>
                    <li><a href="/corrections">Report a correction</a></li>
                </ul>
                <p class="detail-meta">This page exists to make a repeated question legible without pretending to replace utility-specific authority language.</p>
            </article>
        </div>
    </section>

    @if (!relatedGuides.isEmpty())
        <section class="section section--muted">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Related guides</span>
                    <h2>Support pages that belong in the same reading path</h2>
                </div>
                <div class="card-grid">
                    @for (var relatedGuide : relatedGuides)
                        <article class="detail-card">
                            <span class="eyebrow">Reading path</span>
                            <h3>${relatedGuide.title()}</h3>
                            <p>${relatedGuide.description()}</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="/guides/${relatedGuide.slug()}">Open guide</a>
                            </div>
                        </article>
                    @endfor
                </div>
            </div>
        </section>
    @endif

    <section class="section section--muted">
        <div class="container">
            <div class="detail-card">
                <span class="eyebrow">Source trail</span>
                <h2>Source trail</h2>
                <div class="source-list">
                    @for (var source : guide.sources())
                        <a class="source-item" href="${source.url()}">
                            <div>
                                <strong>${source.label()}</strong>
                                <span>${guide.sourceExcerpt()}</span>
                            </div>
                            <em>${source.kind()}</em>
                        </a>
                    @endfor
                </div>
            </div>
        </div>
    </section>
`)
