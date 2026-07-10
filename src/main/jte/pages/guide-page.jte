@import owner.backflow.web.PageMeta
@import owner.backflow.web.PriorityRoute
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
@param java.util.List<PriorityRoute> priorityRoutes = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Evergreen guide</span>
                <h1>${guide.slug().equals("backflow-reporting-portals") ? "Backflow reporting portals" : guide.title()}</h1>
                <p class="lede">${guide.intro()}</p>
                @if (guide.slug().equals("backflow-reporting-portals"))
                    <div class="hero__actions">
                        <a class="button button--primary" href="#portal-family-routes">Find my portal</a>
                    </div>
                @endif
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

    @if (!priorityRoutes.isEmpty())
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Best next local pages</span>
                    <h2>Apply this guide to a source-backed city or utility route</h2>
                    <p class="lede">These pages are selected from the utility records most relevant to this guide and ranked by structured evidence.</p>
                </div>
                <div class="support-links">
                    @for (var route : priorityRoutes)
                        <a class="support-link" href="${route.path()}">
                            <div>
                                <strong>${route.label()}</strong>
                                <span>${route.eyebrow()} · ${route.description()}</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    @endfor
                </div>
            </div>
        </section>
    @endif

    @if (guide.slug().equals("backflow-test-notice-next-steps"))
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Notice funnel</span>
                    <h2>Turn the notice into the right next page</h2>
                    <p class="lede">Start with the word or problem named in the notice. Then open the exact utility page before scheduling or filing.</p>
                </div>
                <div class="card-grid card-grid--three">
                    <article class="detail-card detail-card--key">
                        <span class="pill">Deadline</span>
                        <h3>Notice says annual test or due date</h3>
                        <p>Use the due-date guide when the notice is about timing, annual testing, a calendar window, or an anniversary date.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/guides/anniversary-date-vs-calendar-deadline">Open due-date guide</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">Portal</span>
                        <h3>Notice names BSI, SwiftComply, WEIRS, or VEPO</h3>
                        <p>Use the portal guide when the report has to be entered through a named online system or tester account.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/guides/backflow-reporting-portals">Open portal guide</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">Tester</span>
                        <h3>Notice says certified, registered, or approved tester</h3>
                        <p>Use the tester guide when the issue is whether the provider is accepted by the city, county, utility, or portal.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/guides/approved-testers-vs-find-a-tester">Open tester guide</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--warm">
                        <span class="pill">Failed test</span>
                        <h3>Device failed or needs repair</h3>
                        <p>Use the failed-test guide when the next step is repair, retest, report correction, or a short compliance window.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/guides/failed-backflow-test-next-steps">Open failed-test guide</a>
                        </div>
                    </article>
                    <article class="detail-card">
                        <span class="pill">Cost</span>
                        <h3>Notice creates a pricing question</h3>
                        <p>Use the cost guide when the owner needs to separate tester pricing, filing fees, repair exposure, and retest costs.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/guides/backflow-test-cost">Open cost guide</a>
                        </div>
                    </article>
                    <article class="detail-card">
                        <span class="pill">Utility</span>
                        <h3>Notice names a city or water provider</h3>
                        <p>Use the mapped utility page for the exact phone, source trail, submission method, tester route, and local rule.</p>
                        @if (!relatedUtilities.isEmpty())
                            <div class="card-actions">
                                <a class="button button--primary" href="/utilities/${relatedUtilities.get(0).state()}/${relatedUtilities.get(0).canonicalSlug()}/">Open mapped utility</a>
                            </div>
                        @endif
                    </article>
                </div>
            </div>
        </section>
    @endif

    @if (guide.slug().equals("backflow-reporting-portals"))
        <section class="section section--tight">
            <div class="container" id="portal-family-routes">
                <div class="section-heading">
                    <span class="eyebrow">Portal lookup</span>
                    <h2>Jump to the portal family named in the notice</h2>
                    <p class="lede">These hub pages group utilities by the reporting system named in the public workflow, then send the user back to the exact authority page.</p>
                </div>
                <div class="card-grid">
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">BSI Online</span>
                        <h3>BSI backflow reporting</h3>
                        <p>Use this route when the notice mentions BSI Online, Backflow Solutions, a CCN, or online test report entry through BSI.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/bsi">Open BSI utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">SwiftComply</span>
                        <h3>SwiftComply test reports</h3>
                        <p>Use this route when the utility uses SwiftComply or C3Swift for tester registration and report submission.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/swiftcomply">Open SwiftComply utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">WEIRS</span>
                        <h3>WEIRS tester and report routes</h3>
                        <p>Use this route when the local workflow references WEIRS for tester lookup, inspection, or report submission.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/weirs">Open WEIRS utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">VEPO Envirotrax</span>
                        <h3>VEPO tester registration and reports</h3>
                        <p>Use this route when the utility names VEPO or Envirotrax for BPAT registration, credential verification, approved-list status, or report submission.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/vepo">Open VEPO utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">Aqua Backflow</span>
                        <h3>TrackMyBackflow filing routes</h3>
                        <p>Use this route when the notice mentions Aqua Backflow, TrackMyBackflow, a filing fee, or a hazard/site ID for online report entry.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/aqua">Open Aqua utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">Tokay WebTest</span>
                        <h3>Tokay online test entry</h3>
                        <p>Use this route when the utility requires Tokay WebTest, tester credentials, test kit calibration records, or online report entry through a Tokay portal.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/tokay">Open Tokay utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">SpryBackflow</span>
                        <h3>SpryBackflow online submission</h3>
                        <p>Use this route when the official workflow names SpryBackflow or a utility-specific Spry backflow site for online test result submission.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/sprybackflow">Open SpryBackflow utilities</a>
                        </div>
                    </article>
                </div>
            </div>
        </section>
    @endif

    @if (guide.slug().equals("backflow-test-notice-next-steps") || guide.slug().equals("backflow-reporting-portals") || guide.slug().equals("backflow-test-cost") || guide.slug().equals("approved-testers-vs-find-a-tester"))
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Priority local routes</span>
                    <h2>Open the exact city or utility path when the notice gets specific</h2>
                    <p class="lede">These routes carry concrete local intent: portal names, annual notices, inspection language, tester gates, and utility-specific report acceptance.</p>
                </div>
                <div class="card-grid card-grid--three">
                    <article class="detail-card detail-card--key">
                        <span class="pill">WEIRS</span>
                        <h3>Austin WEIRS report and tester route</h3>
                        <p>Austin search demand already shows WEIRS language, so the city route should carry the portal, tester, and report-submission path directly.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/cities/texas/austin/backflow-reporting-portal">Austin WEIRS portal</a>
                            <a class="button button--secondary" href="/cities/texas/austin/submit-backflow-report">Submit Austin report</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--key">
                        <span class="pill">SwiftComply</span>
                        <h3>Dallas SwiftComply report submission</h3>
                        <p>Use this path when the notice or tester workflow is about SwiftComply report entry, device records, failed tests, or accepted submission proof.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/cities/texas/dallas/backflow-reporting-portal">Dallas SwiftComply portal</a>
                            <a class="button button--secondary" href="/cities/texas/dallas/submit-backflow-report">Submit Dallas report</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--warm">
                        <span class="pill">Fort Worth</span>
                        <h3>Fort Worth annual and VEPO report routes</h3>
                        <p>Fort Worth has separate annual-testing, portal, failed-test, and report-submission intent. Do not flatten those into one generic backflow page.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/cities/texas/fort-worth/annual-backflow-testing">Fort Worth annual testing</a>
                            <a class="button button--secondary" href="/cities/texas/fort-worth/submit-backflow-report">Submit Fort Worth report</a>
                        </div>
                    </article>
                    <article class="detail-card">
                        <span class="pill">Colorado</span>
                        <h3>Aurora backflow inspection route</h3>
                        <p>Aurora already has search visibility. Keep the utility workflow, inspection language, and portal path close together.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/colorado/aurora-water/">Aurora utility workflow</a>
                            <a class="button button--secondary" href="/cities/colorado/aurora/submit-backflow-report">Submit Aurora report</a>
                        </div>
                    </article>
                    <article class="detail-card">
                        <span class="pill">California</span>
                        <h3>Anaheim backflow prevention and tester route</h3>
                        <p>Anaheim has installation and prevention impressions, so the route should make the official program and tester path easy to reach.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/cities/california/anaheim/backflow-testing">Anaheim city route</a>
                            <a class="button button--secondary" href="/utilities/california/anaheim-cross-connection-control/approved-testers">Anaheim tester route</a>
                        </div>
                    </article>
                    <article class="detail-card">
                        <span class="pill">Florida</span>
                        <h3>Hillsborough annual testing and cost context</h3>
                        <p>Hillsborough is already getting impressions and clicks. Keep annual-testing, cost, and utility source context linked from the guide layer.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/florida/hillsborough-county-backflow-testing/">Hillsborough utility workflow</a>
                            <a class="button button--secondary" href="/utilities/florida/hillsborough-county-backflow-testing/annual-testing">Hillsborough annual testing</a>
                        </div>
                    </article>
                </div>
            </div>
        </section>
    @endif

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
