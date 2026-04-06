@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<MetroRecord> metros
@param java.util.Map<String, Integer> providerCounts
@param int publicProviderCount
@param java.util.List<UtilityRecord> featuredUtilities

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Browse by metro</span>
                <h1>Compare nearby utilities and public provider coverage in one place.</h1>
                <p class="lede">Metro pages are for people who know the general market but still need the exact local utility page. They group nearby utility routes, public provider profiles, and supporting guides without replacing the local authority.</p>
                <div class="hero__actions">
                    @if (!metros.isEmpty())
                        <a class="button button--primary" href="/metros/${metros.get(0).state()}/${metros.get(0).metroSlug()}/backflow-testing">Open a metro page</a>
                    @endif
                    <a class="button button--secondary" href="/states">Browse states</a>
                    <a class="button button--secondary" href="/leads/new">Request help</a>
                </div>
                <div class="hero__stats">
                    <span>${metros.size()} published metro pages</span>
                    <span>${publicProviderCount} mapped public providers</span>
                    <span>Utility pages remain the decision page</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Best use of metro pages</span>
                <div class="stat-grid">
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Compare</strong>
                        <span style="color: #ffffff;">Nearby utility workflows</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Check</strong>
                        <span style="color: #ffffff;">Public provider coverage</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Then</strong>
                        <span style="color: #ffffff;">Open the exact utility page</span>
                    </div>
                </div>
                <p class="detail-meta" style="color: rgba(239, 241, 243, 0.74);">Metro pages are helpful when the service area crosses city lines or when a user needs provider coverage across multiple nearby utilities.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Published metros</span>
                <h2>Market pages that group utilities without replacing them</h2>
                <p class="lede">Each metro page combines nearby utilities, public providers, and supporting guides so users can narrow down the right local workflow faster.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var metro : metros)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${metro.state()}</span>
                        <h3>${metro.title()}</h3>
                        <p>${metro.description()}</p>
                        <ul class="detail-list">
                            <li>${metro.utilityIds().size()} mapped utility pages</li>
                            <li>${providerCounts.getOrDefault(metro.metroId(), 0)} public providers</li>
                            <li>Last reviewed ${metro.lastReviewed().toString()}</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/metros/${metro.state()}/${metro.metroSlug()}/backflow-testing">Open metro page</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">What these pages do well</span>
                <h2>Useful market context without blurring the rule page</h2>
                <p>Metro pages are where the product can show density. They help users compare nearby service areas and spot public provider coverage, while still keeping the utility page as the canonical source of action.</p>
            </article>

            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">Common path</span>
                    <h2>Use a metro page to narrow down the local answer</h2>
                    <p>A user can start with the metro, compare nearby authorities, then click into the utility page that actually controls testing, due dates, and report submission.</p>
                </div>
                <div class="card-actions">
                    <a class="button button--secondary" href="/states">Browse state guides</a>
                    <a class="button button--secondary" href="/guides">Browse backflow guides</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section section--muted">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Featured utilities</span>
                <h2>Utility pages behind the metro coverage</h2>
                <p class="lede">These local pages show the detail users should reach before they schedule a test, submit a report, or choose a provider.</p>
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
