@import owner.backflow.web.PageMeta
@import owner.backflow.web.NoticeFinderResult
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param String query = ""
@param java.util.List<NoticeFinderResult> results = java.util.List.of()
@param java.util.List<NoticeFinderResult> popularPortals = java.util.List.of()
@param java.util.List<UtilityRecord> featuredUtilities = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Notice finder</span>
                <h1>Turn a backflow notice into the right next page.</h1>
                <p class="lede">Search the city, utility, portal name, notice ID clue, or failed-test phrase. BackflowPath will route you to the most specific source-backed page it has.</p>
                <form class="stack-form notice-finder-form" method="get" action="/notice-finder">
                    <label class="field" data-required="false">
                        <span>Notice, city, portal, or utility</span>
                        <input name="q" type="search" value="${query}" placeholder="Example: Euless TrackMyBackflow Hazard ID" autocomplete="off">
                    </label>
                    <div class="hero__actions">
                        <button class="button button--primary" type="submit">Find route</button>
                        <a class="button button--secondary" href="/guides/backflow-test-notice-next-steps">Notice guide</a>
                    </div>
                </form>
                <div class="hero__stats">
                    <span>City and utility matching</span>
                    <span>Portal family routing</span>
                    <span>Failed-test and tester intent</span>
                    <span>Official-source pages first</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">What to paste</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">City + clue</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">Good clues include BSI, SwiftComply, VEPO, TrackMyBackflow, Tokay, CCN, Hazard ID, Site ID, approved tester, annual notice, failed test, or retest.</p>
            </aside>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container">
            @if (!query.isBlank())
                <article class="table-card">
                    <div class="section-heading">
                        <span class="eyebrow">Best matches</span>
                        <h2>Routes for "${query}"</h2>
                        <p>Open the most specific city or utility route first. Portal hubs help when the notice names a software system but the local utility still controls the rule.</p>
                    </div>
                    @if (results.isEmpty())
                        <div class="content-grid">
                            <article class="detail-card detail-card--warm">
                                <span class="eyebrow">No direct match</span>
                                <h3>Use broader clues from the notice</h3>
                                <p>Try the city, utility, portal name, account or device clue, or failed-test wording. If the page is not live yet, start from state guides and keep the official notice available.</p>
                                <div class="card-actions">
                                    <a class="button button--secondary" href="/states">Browse state guides</a>
                                    <a class="button button--secondary" href="/backflow-reporting-portals">Browse portal hub</a>
                                </div>
                            </article>
                        </div>
                    @else
                        <div class="card-grid">
                            @for (var result : results)
                                <article class="detail-card detail-card--quiet">
                                    <span class="pill">${result.resultType()}</span>
                                    <h3>${result.label()}</h3>
                                    <p>${result.summary()}</p>
                                    @if (!result.signals().isEmpty())
                                        <ul class="detail-list">
                                            @for (var signal : result.signals())
                                                <li>${signal}</li>
                                            @endfor
                                        </ul>
                                    @endif
                                    <div class="card-actions">
                                        <a class="button button--secondary" href="${result.path()}">Open route</a>
                                    </div>
                                </article>
                            @endfor
                        </div>
                    @endif
                </article>
            @else
                <div class="content-grid">
                    <article class="detail-card detail-card--key">
                        <span class="eyebrow">Search examples</span>
                        <h2>Start from the words on the notice</h2>
                        <div class="support-links">
                            <a class="support-link" href="/notice-finder?q=Euless%20TrackMyBackflow%20Hazard%20ID">
                                <div>
                                    <strong>Euless TrackMyBackflow Hazard ID</strong>
                                    <span>Portal plus city plus identifier clue.</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                            <a class="support-link" href="/notice-finder?q=Dallas%20SwiftComply%20annual%20notice">
                                <div>
                                    <strong>Dallas SwiftComply annual notice</strong>
                                    <span>City plus portal plus due-date intent.</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                            <a class="support-link" href="/notice-finder?q=Oxnard%20Tokay%20approved%20tester">
                                <div>
                                    <strong>Oxnard Tokay approved tester</strong>
                                    <span>City plus portal plus tester credential intent.</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                        </div>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">Checklist</span>
                        <h2>Keep these notice fields visible</h2>
                        <ul class="detail-list">
                            <li>Utility name and service address</li>
                            <li>Due date, annual window, or failed-test deadline</li>
                            <li>Portal name, account number, CCN, Hazard ID, Site ID, device ID, or assembly serial</li>
                            <li>Tester credential, approved-list, filing-fee, repair, or retest language</li>
                        </ul>
                    </article>
                </div>
            @endif
        </div>
    </section>

    <section class="section">
        <div class="container page-frame">
            <div class="stack">
                <section class="section section--tight">
                    <div class="section-heading">
                        <span class="eyebrow">Portal shortcuts</span>
                        <h2>Jump to a named portal family</h2>
                    </div>
                    <div class="card-grid">
                        @for (var portal : popularPortals)
                            <article class="detail-card detail-card--quiet">
                                <h3>${portal.label()}</h3>
                                <p>${portal.summary()}</p>
                                <div class="card-actions">
                                    <a class="button button--secondary" href="${portal.path()}">Open portal</a>
                                </div>
                            </article>
                        @endfor
                    </div>
                </section>

                <article class="detail-card detail-card--key">
                    <span class="eyebrow">Featured utility records</span>
                    <h2>Open a utility workflow when the notice names the authority</h2>
                    <div class="support-links">
                        @for (var utility : featuredUtilities)
                            <a class="support-link" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">
                                <div>
                                    <strong>${utility.utilityName()}</strong>
                                    <span>${utility.verdictSummary()}</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                        @endfor
                    </div>
                </article>
            </div>

            <aside class="side-stack">
                <article class="detail-card">
                    <span class="eyebrow">Related guides</span>
                    <h2>Read if the notice is unclear</h2>
                    <div class="support-links">
                        @for (var guide : relatedGuides)
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
            </aside>
        </div>
    </section>
`)
