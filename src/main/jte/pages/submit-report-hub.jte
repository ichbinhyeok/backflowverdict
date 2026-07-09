@import owner.backflow.web.PageMeta
@import owner.backflow.web.FaqItem
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<UtilityRecord> utilities
@param java.util.Map<String, java.util.List<CityAliasRecord>> cityAliasesByUtility = java.util.Map.of()
@param java.util.Map<String, String> noticeIdentifierHints = java.util.Map.of()
@param java.util.Map<String, String> reportAcceptanceHints = java.util.Map.of()
@param java.util.Map<String, String> portalNamesByUtility = java.util.Map.of()
@param java.util.Map<String, String> portalHubPathsByUtility = java.util.Map.of()
@param java.util.List<FaqItem> faqItems = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Report submission hub</span>
                <h1>Submit backflow test reports through the right city, utility, and portal route.</h1>
                <p class="lede">Use this hub when the field test is done but the compliance cycle still depends on BSI, WEIRS, SwiftComply, VEPO, Envirotrax, Aqua/TrackMyBackflow, Tokay WebTest, SpryBackflow, or a utility online submission path.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/notice-finder">Find from notice</a>
                    <a class="button button--secondary" href="/backflow-reporting-portals">Compare portal families</a>
                    <a class="button button--secondary" href="/guides/backflow-reporting-portals">Read portal guide</a>
                </div>
                <div class="hero__stats">
                    <span>${utilities.size()} portal or online submission utility workflows</span>
                    <span>City report-submission routes</span>
                    <span>Notice and device clues</span>
                    <span>Accepted report proof</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Submission rule</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Passed is not always filed</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">The tester can pass the assembly in the field and the owner can still have a compliance problem if the report is not accepted through the correct utility or portal route.</p>
            </aside>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Submission checklist</span>
                <h2>Do this before treating the test as complete</h2>
                <ul class="detail-list">
                    <li>Match the notice to the exact utility, city, service address, device record, and due date.</li>
                    <li>Confirm the named portal or online submission path before the tester files the report.</li>
                    <li>Confirm tester acceptance, credential, license, insurance, or calibration gates where the utility requires them.</li>
                    <li>Keep proof that the report was submitted and accepted, not only that the field test passed.</li>
                </ul>
            </article>
            <article class="detail-card">
                <span class="eyebrow">When to use this page</span>
                <h2>Use this hub for filing, upload, and report-proof searches</h2>
                <p>Start here for searches like submit backflow report, upload backflow test, BSI report submission, Austin WEIRS, Dallas SwiftComply, Aurora SpryBackflow, TrackMyBackflow Hazard ID, Tokay WebTest report, or VEPO Envirotrax tester submission.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/notice-finder?q=submit%20backflow%20report">Search submit report</a>
                    <a class="button button--secondary" href="/notice-finder?q=failed%20backflow%20retest">Search failed retest</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <article class="table-card">
                <div class="section-heading">
                    <span class="eyebrow">Report routes</span>
                    <h2>City and utility report-submission paths</h2>
                    <p>Open the city submission route when the notice names a city. Open the utility workflow when the notice names only the governing water authority.</p>
                </div>
                <div class="table-wrap">
                    <table class="data-table data-table--responsive">
                        <thead>
                        <tr>
                            <th>Utility and cities</th>
                            <th>Portal or submission route</th>
                            <th>Notice clue</th>
                            <th>Acceptance proof</th>
                            <th>Open route</th>
                        </tr>
                        </thead>
                        <tbody>
                        @for (var utility : utilities)
                            <tr>
                                <td data-label="Utility and cities">
                                    <strong>${utility.utilityName()}</strong>
                                    @if (!cityAliasesByUtility.getOrDefault(utility.utilityId(), java.util.List.of()).isEmpty())
                                        <div class="detail-meta">
                                            @for (var alias : cityAliasesByUtility.getOrDefault(utility.utilityId(), java.util.List.of()))
                                                <a href="/cities/${alias.state()}/${alias.aliasSlug()}/submit-backflow-report">${alias.city()}</a>
                                            @endfor
                                        </div>
                                    @else
                                        <div class="detail-meta">${utility.state()}</div>
                                    @endif
                                </td>
                                <td data-label="Portal or submission route">
                                    <strong>${portalNamesByUtility.getOrDefault(utility.utilityId(), "Online submission")}</strong>
                                    @if (!utility.submissionMethods().isEmpty())
                                        @for (var method : utility.submissionMethods())
                                            <div class="detail-meta">${method.label()}</div>
                                        @endfor
                                    @endif
                                </td>
                                <td data-label="Notice clue">${noticeIdentifierHints.getOrDefault(utility.utilityId(), "Look for the account, notice, device, or assembly identifier.")}</td>
                                <td data-label="Acceptance proof">${reportAcceptanceHints.getOrDefault(utility.utilityId(), "Keep proof that the report was submitted and accepted.")}</td>
                                <td data-label="Open route">
                                    <div class="card-actions">
                                        <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Utility workflow</a>
                                        @if (portalHubPathsByUtility.get(utility.utilityId()) != null)
                                            <a class="button button--secondary" href="${portalHubPathsByUtility.get(utility.utilityId())}">Portal hub</a>
                                        @endif
                                    </div>
                                </td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            </article>
        </div>
    </section>

    @if (!faqItems.isEmpty())
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Submission FAQ</span>
                    <h2>Report filing questions that change the next step</h2>
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

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card">
                <span class="eyebrow">Related guides</span>
                <h2>Read before filing if the notice is unclear</h2>
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
            <article class="trust-box">
                <span class="trust-box__label">Authority split</span>
                <h2>The portal is not the authority</h2>
                <p>A portal can collect reports, but the utility still controls who must test, which tester is accepted, what identifier is required, and whether a failed test needs repair, retest, or resubmission.</p>
            </article>
        </div>
    </section>
`)
