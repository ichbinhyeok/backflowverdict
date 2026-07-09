@import owner.backflow.web.PageMeta
@import owner.backflow.web.FaqItem
@import owner.backflow.web.NoticeFinderResult
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param String query = ""
@param java.util.List<NoticeFinderResult> results = java.util.List.of()
@param java.util.List<NoticeFinderResult> popularPortals = java.util.List.of()
@param java.util.List<UtilityRecord> featuredUtilities = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()
@param java.util.List<FaqItem> faqItems = java.util.List.of()

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
                            <a class="support-link" href="/notice-finder?q=Austin%20WEIRS%20approved%20tester">
                                <div>
                                    <strong>Austin WEIRS approved tester</strong>
                                    <span>Portal plus tester credential and city intent.</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                            <a class="support-link" href="/notice-finder?q=Fort%20Worth%20annual%20backflow%20testing">
                                <div>
                                    <strong>Fort Worth annual backflow testing</strong>
                                    <span>Annual notice plus city route.</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                            <a class="support-link" href="/notice-finder?q=Irving%20Envirotrax%20failed%20test">
                                <div>
                                    <strong>Irving Envirotrax failed test</strong>
                                    <span>Portal plus failed-test and report acceptance intent.</span>
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

                <section class="section section--tight">
                    <div class="section-heading">
                        <span class="eyebrow">Notice clue routes</span>
                        <h2>Use the exact clue instead of a broad search</h2>
                        <p>These shortcuts mirror the repeated winning pattern: city plus portal, city plus tester gate, city plus failed-test or annual notice.</p>
                    </div>
                    <div class="card-grid card-grid--three">
                        <article class="detail-card detail-card--quiet">
                            <span class="pill">Portal</span>
                            <h3>Portal or upload wording</h3>
                            <p>Use this when the notice mentions BSI, SwiftComply, WEIRS, VEPO, Envirotrax, TrackMyBackflow, or Tokay WebTest.</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="/notice-finder?q=SwiftComply%20portal">SwiftComply portal</a>
                                <a class="button button--secondary" href="/notice-finder?q=TrackMyBackflow%20Hazard%20ID">TrackMyBackflow Hazard ID</a>
                            </div>
                        </article>
                        <article class="detail-card detail-card--quiet">
                            <span class="pill">Tester</span>
                            <h3>Approved tester wording</h3>
                            <p>Use this when the notice says approved, certified, registered, credential, license, insurance, or gauge calibration.</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="/notice-finder?q=approved%20backflow%20tester">Approved tester</a>
                                <a class="button button--secondary" href="/official-backflow-tester-lists">Tester-list hub</a>
                            </div>
                        </article>
                        <article class="detail-card detail-card--quiet">
                            <span class="pill">Urgency</span>
                            <h3>Due date or failed-test wording</h3>
                            <p>Use this when the notice mentions annual, due, deadline, anniversary, failed, repair, or retest.</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="/notice-finder?q=annual%20backflow%20notice">Annual notice</a>
                                <a class="button button--secondary" href="/notice-finder?q=failed%20backflow%20retest">Failed retest</a>
                            </div>
                        </article>
                    </div>
                </section>

                <section class="section section--tight">
                    <div class="section-heading">
                        <span class="eyebrow">Popular notice routes</span>
                        <h2>Open source-backed routes without searching</h2>
                        <p>These are the priority notice and portal paths to crawl first because they combine city, portal, tester, and report-submission intent.</p>
                    </div>
                    <div class="support-links">
                        <a class="support-link" href="/cities/texas/euless/backflow-reporting-portal">
                            <div>
                                <strong>Euless TrackMyBackflow reporting portal</strong>
                                <span>Hazard ID, Site ID, Aqua Backflow, and tester registration context.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/california/buena-park/backflow-reporting-portal">
                            <div>
                                <strong>Buena Park Aqua Backflow reporting portal</strong>
                                <span>Annual notice, customer record lookup, and online report upload context.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/california/pleasanton/backflow-reporting-portal">
                            <div>
                                <strong>Pleasanton Aqua Backflow tester reporting</strong>
                                <span>Aqua Backflow plan language, tester qualification, and online reporting context.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/california/oxnard/backflow-reporting-portal">
                            <div>
                                <strong>Oxnard Tokay WebTest reporting portal</strong>
                                <span>Tokay online test entry, approved tester, and credential maintenance context.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/california/dublin/backflow-reporting-portal">
                            <div>
                                <strong>Dublin DSRSD Tokay reporting portal</strong>
                                <span>DSRSD test entry, repair, retest, and approved tester route.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/california/san-ramon/backflow-reporting-portal">
                            <div>
                                <strong>San Ramon DSRSD Tokay reporting portal</strong>
                                <span>DSRSD Tokay test entry, approved tester, repair, and retest route.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/texas/fort-worth/backflow-reporting-portal">
                            <div>
                                <strong>Fort Worth VEPO Envirotrax reporting portal</strong>
                                <span>Registered BPAT, Envirotrax report submission, and failed paperwork risk.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/texas/irving/backflow-reporting-portal">
                            <div>
                                <strong>Irving Envirotrax reporting portal</strong>
                                <span>Online Envirotrax submission, report window, and failed-test customer notice context.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="/cities/texas/dallas/backflow-reporting-portal">
                            <div>
                                <strong>Dallas SwiftComply reporting portal</strong>
                                <span>SwiftComply annual testing, failed-test, and report submission workflow.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
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

    @if (!faqItems.isEmpty())
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Notice FAQ</span>
                    <h2>What to read from a backflow notice</h2>
                </div>
                <div class="faq-list">
                    @for (var faqItem : faqItems)
                        <details class="faq-item">
                            <summary>${faqItem.question()}</summary>
                            <p>${faqItem.answer()}</p>
                        </details>
                    @endfor
                </div>
            </div>
        </section>
    @endif
`)
