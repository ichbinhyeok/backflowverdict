@import owner.backflow.web.PageMeta
@import owner.backflow.web.CityIntentLink
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.ProviderRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param CityAliasRecord alias
@param UtilityRecord utility
@param String annualTestingPath = null
@param String portalCityPath = null
@param String submitReportPath = null
@param String irrigationPath = null
@param String fireLinePath = null
@param String failedTestPath
@param String testerPath = null
@param String testerLabel = null
@param String portalHubPath = null
@param String portalHubLabel = null
@param String noticeIdentifierHint
@param String reportAcceptanceHint
@param boolean usesPortalWorkflow
@param java.util.List<CityIntentLink> cityIntentLinks = java.util.List.of()
@param java.util.List<ProviderRecord> providers = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">City backflow route</span>
                <h1>${alias.city()} backflow testing routes through ${utility.utilityName()}.</h1>
                <p class="lede">${alias.justification()} This page keeps the city search term visible while routing the actual compliance work to the governing utility record.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility requirements</a>
                    <a class="button button--secondary" href="/notice-finder?q=${alias.aliasSlug()}">Find from notice</a>
                    @if (annualTestingPath != null)
                        <a class="button button--secondary" href="${annualTestingPath}">Annual testing</a>
                    @endif
                    @if (submitReportPath != null)
                        <a class="button button--secondary" href="${submitReportPath}">Submit report</a>
                    @endif
                    @if (testerPath != null)
                        <a class="button button--secondary" href="${testerPath}">${testerLabel}</a>
                    @endif
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
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Local answer</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Use the utility workflow</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">${utility.verdictSummary()}</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Next action</span>
                <h2>Pick the ${alias.city()} backflow path that matches the problem</h2>
                <p class="lede">The city term helps discovery. The governing utility still decides the rule, submission method, tester route, and follow-up order.</p>
            </div>
            <div class="card-grid card-grid--three">
                <article class="detail-card detail-card--key">
                    <span class="eyebrow">Requirements</span>
                    <h3>${alias.city()} backflow prevention requirements</h3>
                    <p>Start with the utility page to confirm who is affected, accepted submission methods, phone contact, and source evidence.</p>
                    <div class="card-actions">
                        <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Utility requirements</a>
                    </div>
                </article>

                @if (annualTestingPath != null)
                    <article class="detail-card">
                        <span class="eyebrow">Annual notice</span>
                        <h3>${alias.city()} annual backflow testing</h3>
                        <p>${utility.testingFrequency()} ${utility.dueBasis()}</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="${annualTestingPath}">Annual testing workflow</a>
                        </div>
                    </article>
                @endif

                @if (usesPortalWorkflow)
                    <article class="detail-card">
                        <span class="eyebrow">Reporting</span>
                        <h3>${alias.city()} backflow reporting portal</h3>
                        <p>Use the utility page to confirm whether reports go through BSI, WEIRS, SwiftComply, a city portal, or another official submission path.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="${portalCityPath != null ? portalCityPath : "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/"}">Portal and submission steps</a>
                            @if (submitReportPath != null)
                                <a class="button button--secondary" href="${submitReportPath}">Submit report route</a>
                            @endif
                            @if (portalHubPath != null && portalHubLabel != null)
                                <a class="button button--secondary" href="${portalHubPath}">${portalHubLabel}</a>
                            @endif
                        </div>
                    </article>
                @endif

                @if (testerPath != null)
                    <article class="detail-card">
                        <span class="eyebrow">Tester route</span>
                        <h3>${alias.city()} ${utility.supportsApprovedTestersPage() ? "approved backflow testers" : "backflow tester options"}</h3>
                        <p>${utility.supportsApprovedTestersPage() ? "This utility has an official tester-list route. Confirm status on the governing list before treating a provider as approved." : "This route is clearly labeled as non-official provider discovery and should come after the utility workflow."}</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="${testerPath}">${testerLabel}</a>
                        </div>
                    </article>
                @endif

                <article class="detail-card detail-card--warm">
                    <span class="eyebrow">Repair or failure</span>
                    <h3>${alias.city()} failed backflow test</h3>
                    <p>Use the failed-test page when the assembly has already failed and the next step is repair, retest, and report submission.</p>
                    <div class="card-actions">
                        <a class="button button--secondary" href="${failedTestPath}">Failed-test next steps</a>
                    </div>
                </article>

                @if (irrigationPath != null)
                    <article class="detail-card">
                        <span class="eyebrow">Irrigation</span>
                        <h3>${alias.city()} irrigation backflow testing</h3>
                        <p>Use this path when the question is tied to sprinkler systems, reclaimed water, outdoor service, or irrigation assemblies.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="${irrigationPath}">Irrigation rules</a>
                        </div>
                    </article>
                @endif

                @if (fireLinePath != null)
                    <article class="detail-card">
                        <span class="eyebrow">Fire line</span>
                        <h3>${alias.city()} fire-line backflow testing</h3>
                        <p>Use this path when the assembly serves fire protection equipment or a managed commercial site.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="${fireLinePath}">Fire-line rules</a>
                        </div>
                    </article>
                @endif
            </div>
        </div>
    </section>

    @if (!cityIntentLinks.isEmpty())
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">City intent map</span>
                    <h2>${alias.city()} pages linked from this hub</h2>
                    <p class="lede">These routes keep local searches connected to the exact city, while each page still routes back to the governing utility record.</p>
                </div>
                <div class="support-links">
                    @for (var intentLink : cityIntentLinks)
                        <a class="support-link" href="${intentLink.path()}">
                            <div>
                                <strong>${intentLink.label()}</strong>
                                <span>${intentLink.summary()}</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    @endfor
                </div>
            </div>
        </section>
    @endif

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Before scheduling in ${alias.city()}</span>
                <h2>Check the local rule before treating this as a generic tester search</h2>
                <ul class="detail-list">
                    <li><strong>Cadence:</strong> ${utility.testingFrequency()}</li>
                    <li><strong>Due basis:</strong> ${utility.dueBasis()}</li>
                    <li><strong>Notice or device clue:</strong> ${noticeIdentifierHint}</li>
                    <li><strong>Submission:</strong> ${utility.submissionMethods().isEmpty() ? "Confirm the current report path with the utility." : utility.submissionMethods().get(0).label()}</li>
                    <li><strong>Acceptance rule:</strong> ${reportAcceptanceHint}</li>
                    @if (utility.costBand() != null)
                        <li><strong>Cost signal:</strong> ${utility.costBand().testingRange()}</li>
                    @endif
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Owner vs tester</span>
                <h2>${alias.city()} action split</h2>
                <div class="metadata-grid">
                    <div class="metadata-block">
                        <strong>Owner</strong>
                        <span>Confirm the notice, address, device, due date, and utility before choosing a provider.</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Tester</strong>
                        <span>Confirm registration, certification, gauge calibration, and whether the report must go through a portal.</span>
                    </div>
                </div>
                @if (portalHubPath != null && portalHubLabel != null)
                    <p class="detail-meta">Portal family: <a href="${portalHubPath}">${portalHubLabel}</a></p>
                @endif
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Authority mapping</span>
                <h2>Why ${alias.city()} maps to ${utility.utilityName()}</h2>
                <p>${alias.justification()}</p>
                <ul class="detail-list">
                    <li>${utility.whoIsAffected()}</li>
                    <li>${utility.penalties()}</li>
                    <li>Program phone: ${utility.phone()}</li>
                    <li>City route reviewed: ${alias.lastReviewed().toString()}</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Official source trail</span>
                <h2>Source-backed workflow</h2>
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
    </section>

    @if (!providers.isEmpty())
        <section class="section section--muted">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Provider layer</span>
                    <h2>Public provider profiles mapped to this utility</h2>
                    <p class="lede">Provider profiles can help after the city and utility workflow is clear. They do not replace the official source trail.</p>
                </div>
                <div class="card-grid card-grid--three">
                    @for (var provider : providers)
                        @if (providers.indexOf(provider) < 6)
                            <article class="detail-card detail-card--quiet">
                                <span class="pill">Public profile</span>
                                <h3>${provider.providerName()}</h3>
                                <p>${provider.pageLabel()}</p>
                                <div class="card-actions">
                                    @if (provider.phone() != null && !provider.phone().isBlank())
                                        <a class="button button--primary" href="tel:${provider.phone().replace(" ", "")}">Call</a>
                                    @endif
                                    <a class="button button--secondary" href="/providers/${provider.providerId()}/">Open provider profile</a>
                                    @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                                        <a class="button button--secondary" href="${provider.officialApprovalSourceUrl()}">Authority source</a>
                                    @endif
                                </div>
                            </article>
                        @endif
                    @endfor
                </div>
            </div>
        </section>
    @endif

    @if (!relatedGuides.isEmpty())
        <section class="section section--tight">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Support guides</span>
                    <h2>Read these before acting on the ${alias.city()} workflow</h2>
                </div>
                <div class="card-grid">
                    @for (var guide : relatedGuides)
                        @if (relatedGuides.indexOf(guide) < 4)
                            <article class="detail-card">
                                <span class="eyebrow">Guide</span>
                                <h3>${guide.title()}</h3>
                                <p>${guide.description()}</p>
                                <div class="card-actions">
                                    <a class="button button--secondary" href="/guides/${guide.slug()}">Open guide</a>
                                </div>
                            </article>
                        @endif
                    @endfor
                </div>
            </div>
        </section>
    @endif
`)
