@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param CityAliasRecord alias
@param UtilityRecord utility
@param String eyebrow
@param String heading
@param String intro
@param java.util.List<String> highlights = java.util.List.of()
@param java.util.List<String> workflowSteps = java.util.List.of()
@param String primaryPath
@param String primaryLabel
@param String utilityPath
@param String failedTestPath
@param String testerPath = null
@param String testerLabel = null
@param String portalHubPath = null
@param String portalHubLabel = null
@param String noticeIdentifierHint
@param String reportAcceptanceHint
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">${eyebrow}</span>
                <h1>${heading}</h1>
                <p class="lede">${intro}</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="${primaryPath}">${primaryLabel}</a>
                    <a class="button button--secondary" href="${utilityPath}">Utility requirements</a>
                    <a class="button button--secondary" href="/notice-finder?q=${alias.aliasSlug()}">Find from notice</a>
                    <a class="button button--secondary" href="${page.requestHelpPathOrDefault()}">Request local help</a>
                </div>
                <div class="hero__stats">
                    <span>City: ${alias.city()}</span>
                    <span>Utility: ${utility.utilityName()}</span>
                    <span>Cadence: ${utility.testingFrequency()}</span>
                    <span>Last verified: ${utility.lastVerified().toString()}</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Intent route</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">${alias.city()}</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">${utility.verdictSummary()}</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Local answer</span>
                <h2>What to check for ${alias.city()}</h2>
                <p>${intro}</p>
                <ul class="detail-list">
                    <li><strong>Due basis:</strong> ${utility.dueBasis()}</li>
                    <li><strong>Notice or device clue:</strong> ${noticeIdentifierHint}</li>
                    <li><strong>Who is affected:</strong> ${utility.whoIsAffected()}</li>
                    <li><strong>Acceptance rule:</strong> ${reportAcceptanceHint}</li>
                    <li><strong>Program phone:</strong> ${utility.phone()}</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Evidence-backed next step</span>
                <h2>Use the governing utility workflow</h2>
                <p>${alias.justification()}</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="${primaryPath}">${primaryLabel}</a>
                    @if (portalHubPath != null && portalHubLabel != null)
                        <a class="button button--secondary" href="${portalHubPath}">${portalHubLabel}</a>
                    @endif
                    @if (testerPath != null && testerLabel != null)
                        <a class="button button--secondary" href="${testerPath}">${testerLabel}</a>
                    @endif
                </div>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container page-frame">
            <div class="stack">
                @if (!highlights.isEmpty())
                    <article class="detail-card">
                        <span class="eyebrow">Local signals</span>
                        <h2>Signals that matter before you act</h2>
                        <ul class="detail-list">
                            @for (var highlight : highlights)
                                <li>${highlight}</li>
                            @endfor
                        </ul>
                    </article>
                @endif

                @if (!workflowSteps.isEmpty())
                    <article class="panel panel--dark">
                        <div class="section-heading">
                            <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Workflow</span>
                            <h2 style="color: #ffffff;">${alias.city()} workflow order</h2>
                        </div>
                        <ol class="step-list">
                            @for (var step : workflowSteps)
                                <li>${step}</li>
                            @endfor
                        </ol>
                    </article>
                @endif

                <article class="detail-card">
                    <span class="eyebrow">Official source trail</span>
                    <h2>Source-backed utility record</h2>
                    <p>${utility.sourceExcerpt()}</p>
                    <div class="resource-list">
                        @for (var source : utility.sources())
                            <a class="resource-item" href="${source.url()}">
                                <div>
                                    <strong>${source.label()}</strong>
                                    <span>${source.kind()}</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                        @endfor
                    </div>
                </article>
            </div>

            <aside class="side-stack">
                <article class="detail-card">
                    <span class="eyebrow">Related next pages</span>
                    <h2>Do not stop at the city label</h2>
                    <div class="support-links">
                        <a class="support-link" href="${utilityPath}">
                            <div>
                                <strong>${utility.utilityName()}</strong>
                                <span>Canonical utility requirements, source trail, contact, and submission methods.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                        <a class="support-link" href="${failedTestPath}">
                            <div>
                                <strong>${alias.city()} failed backflow test</strong>
                                <span>Repair, retest, and report-submission order.</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    </div>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Support guides</span>
                    <h2>Read before scheduling</h2>
                    <div class="support-links">
                        @for (var guide : relatedGuides)
                            @if (relatedGuides.indexOf(guide) < 4)
                                <a class="support-link" href="/guides/${guide.slug()}">
                                    <div>
                                        <strong>${guide.title()}</strong>
                                        <span>${guide.description()}</span>
                                    </div>
                                    <span class="material-symbols-outlined">arrow_forward</span>
                                </a>
                            @endif
                        @endfor
                    </div>
                </article>
            </aside>
        </div>
    </section>
`)
