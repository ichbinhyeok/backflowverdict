@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.ProviderRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param MetroRecord metro
@param java.util.List<UtilityRecord> utilities
@param java.util.List<ProviderRecord> providers
@param java.util.Map<String, Integer> providerCoverageCounts = java.util.Map.of()
@param java.util.List<GuideRecord> guides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Metro support layer</span>
                <h1>${metro.title()}</h1>
                <p class="lede">${metro.description()}</p>
                <div class="hero__stats">
                    <span>${utilities.size()} mapped utilities</span>
                    <span>${providers.size()} public providers</span>
                    <span>${metro.lastReviewed().toString()} last reviewed</span>
                </div>
                <div class="hero__actions">
                    @if (!utilities.isEmpty())
                        <a class="button button--primary" href="/utilities/${utilities.get(0).state()}/${utilities.get(0).canonicalSlug()}/">Open canonical utility page</a>
                        <a class="button button--secondary" href="#mapped-utilities">Review mapped utilities</a>
                    @endif
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Coverage summary</span>
                <div class="stat-grid">
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Cities</strong>
                        <span style="color: #ffffff;">${metro.cityNames().size()}</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Counties</strong>
                        <span style="color: #ffffff;">${metro.countyNames().size()}</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Guide links</strong>
                        <span style="color: #ffffff;">${guides.size()}</span>
                    </div>
                </div>
                <p class="detail-meta" style="color: rgba(239, 241, 243, 0.74);">Use the metro layer to compare nearby workflows and commercial coverage, but drop into the exact utility page before acting on a rule.</p>
            </aside>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">How to use this</span>
                <h2>Utility pages stay canonical</h2>
                <p>${metro.summary()}</p>
                <p>Use this metro page to compare nearby utility workflows and commercial options, then drop into the exact utility page before acting on a compliance step.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Coverage</span>
                <h2>Coverage</h2>
                <div class="cluster-grid">
                    @for (var city : metro.cityNames())
                        <div class="metadata-block">
                            <strong>City</strong>
                            <span>${city}</span>
                        </div>
                    @endfor
                    @for (var county : metro.countyNames())
                        <div class="metadata-block">
                            <strong>County</strong>
                            <span>${county}</span>
                        </div>
                    @endfor
                </div>
            </article>
        </div>
    </section>

    <section class="section" id="mapped-utilities">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Mapped utilities</span>
                <h2>Utility pages inside this metro cluster</h2>
                <p class="lede">This is the part that matters first. Open the exact utility page before you use any provider or help surface.</p>
            </div>
            <div class="card-grid">
                @for (var utility : utilities)
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

    <section class="section section--muted">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Provider surface</span>
                <h2>Public providers already mapped to this metro</h2>
                <p class="lede">Use this layer only after the local rule and next step are clear on the utility page.</p>
            </div>
            <div class="card-grid">
                @for (var provider : providers)
                    <article class="detail-card">
                        <span class="pill">${provider.isSponsored() ? "Sponsored" : "Public profile"}</span>
                        <h3><a href="/providers/${provider.providerId()}/">${provider.providerName()}</a></h3>
                        <p>${provider.pageLabel()}</p>
                        <ul class="detail-list">
                            <li>Covers ${providerCoverageCounts.getOrDefault(provider.providerId(), provider.coverageTargets().size())} utility pages in this metro</li>
                            @if (provider.phone() != null && !provider.phone().isBlank())
                                <li>${provider.phone()}</li>
                            @endif
                            @if (provider.email() != null && !provider.email().isBlank())
                                <li>${provider.email()}</li>
                            @endif
                            @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                                <li><a href="${provider.officialApprovalSourceUrl()}">Authority source</a></li>
                            @endif
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/providers/${provider.providerId()}/">Open provider profile</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    @if (!guides.isEmpty())
        <section class="section">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Support guides</span>
                    <h2>Guides that reinforce this metro cluster</h2>
                </div>
                <div class="card-grid">
                    @for (var guide : guides)
                        <article class="detail-card">
                            <span class="eyebrow">Guide route</span>
                            <h3>${guide.title()}</h3>
                            <p>${guide.description()}</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="/guides/${guide.slug()}">Open guide</a>
                            </div>
                        </article>
                    @endfor
                </div>
            </div>
        </section>
    @endif
`)
