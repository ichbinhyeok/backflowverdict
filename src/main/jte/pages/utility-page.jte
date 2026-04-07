@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.service.LeadRoutingService
@param PageMeta page
@param UtilityRecord utility
@param String annualTestingPath = null
@param String irrigationPath = null
@param String fireLinePath = null
@param String testerPath = null
@param String testerLabel = null
@param java.util.List<owner.backflow.web.FaqItem> faqItems
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()
@param java.util.List<MetroRecord> metros = java.util.List.of()
@param StateGuideRecord stateGuide = null
@param int activeSponsorCount = 0

@template.layouts.app(page = page, content = @`
    <section class="page-hero page-hero--utility">
        <div class="container page-frame">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">${utility.governingEntityType()}</span>
                <h1>${utility.utilityName()} backflow testing requirements</h1>
                <p class="lede">${utility.verdictSummary()}</p>
                <div class="hero__stats">
                    <span>Testing cadence: ${utility.testingFrequency()}</span>
                    <span>Last verified: ${utility.lastVerified().toString()}</span>
                    <span>Verification code: <a href="/methodology#verification-code-${utility.reviewerInitials().toLowerCase()}">${utility.reviewerInitials()}</a></span>
                    <span>Freshness window: ${utility.staleAfterDays()} days</span>
                </div>
                <div class="hero__actions">
                    <a class="button button--primary" href="${utility.utilityUrl()}">Official program page</a>
                    @if (testerPath != null && testerLabel != null)
                        <a class="button button--secondary" href="${testerPath}">${testerLabel}</a>
                    @endif
                    <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility")}">Request local help</a>
                </div>
            </div>
            <aside class="panel panel--status">
                <span class="stat-card__label">Status</span>
                <div class="stat-card__value">${utility.testingFrequency()}</div>
                <p class="stat-card__meta">${utility.dueBasis()}</p>
                <div class="metadata-grid" style="margin-top: 1rem;">
                    <div class="metadata-block">
                        <strong>Penalty risk</strong>
                        <span>${utility.penalties()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Who is affected</strong>
                        <span>${utility.whoIsAffected()}</span>
                    </div>
                </div>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container page-frame">
            <div class="stack">
                @if (annualTestingPath != null || irrigationPath != null || fireLinePath != null)
                    <article class="detail-card">
                        <div class="section-heading">
                            <span class="eyebrow">Local page set</span>
                            <h2>Cluster pages around the same utility workflow</h2>
                        </div>
                        <div class="card-actions">
                            @if (annualTestingPath != null)
                                <a class="button button--secondary" href="${annualTestingPath}">Annual testing</a>
                            @endif
                            @if (irrigationPath != null)
                                <a class="button button--secondary" href="${irrigationPath}">Irrigation</a>
                            @endif
                            @if (fireLinePath != null)
                                <a class="button button--secondary" href="${fireLinePath}">Fire line</a>
                            @endif
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/failed-test">Failed test</a>
                        </div>
                    </article>
                @endif

                <div class="content-grid">
                    <article class="detail-card detail-card--key">
                        <span class="eyebrow">Testing cadence</span>
                        <h2>Annual or event-based timing</h2>
                        <p>${utility.whoIsAffected()}</p>
                        <ul class="detail-list">
                            <li>${utility.testingFrequency()}</li>
                            <li>${utility.dueBasis()}</li>
                        </ul>
                    </article>

                    <article class="detail-card detail-card--warm">
                        <span class="eyebrow">Penalty exposure</span>
                        <h2>Non-compliance penalties</h2>
                        <p>${utility.penalties()}</p>
                        <ul class="detail-list">
                            @for (var failureHighlight : utility.failureHighlights())
                                <li>${failureHighlight}</li>
                            @endfor
                        </ul>
                    </article>
                </div>

                <article class="panel panel--dark">
                    <div class="section-heading">
                        <span class="eyebrow" style="color: rgba(197, 234, 223, 0.92);">Compliance workflow</span>
                        <h2 style="color: #ffffff;">Official workflow</h2>
                        <p>Use the authority steps first. Repair, retest, and provider routing all sit downstream from this compliance sequence.</p>
                    </div>
                    <ol class="step-list">
                        @for (var step : utility.workflowSteps())
                            <li>${step}</li>
                        @endfor
                    </ol>
                </article>

                <div class="content-grid">
                    <article class="detail-card detail-card--quiet">
                        <span class="eyebrow">Source block</span>
                        <h2>Source block</h2>
                        <p>${utility.sourceExcerpt()}</p>
                        <ul class="detail-list">
                            <li>Last verified: ${utility.lastVerified().toString()}</li>
                            <li>Verification code: <a href="/methodology#verification-code-${utility.reviewerInitials().toLowerCase()}">${utility.reviewerInitials()}</a></li>
                            <li>Freshness window: ${utility.staleAfterDays()} days</li>
                            <li><a href="/corrections">Report a correction or stale source</a></li>
                        </ul>
                        <ul class="detail-list">
                            @for (var source : utility.sources())
                                <li><a href="${source.url()}">${source.label()}</a> <span class="detail-type">${source.kind()}</span></li>
                            @endfor
                        </ul>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">Submission path</span>
                        <h2>Submission methods and utility contact</h2>
                        <div class="resource-list">
                            @for (var submissionMethod : utility.submissionMethods())
                                <a class="resource-item" href="${submissionMethod.url()}">
                                    <div>
                                        <strong>${submissionMethod.label()}</strong>
                                        <span>${submissionMethod.kind()}</span>
                                    </div>
                                    <span class="material-symbols-outlined">arrow_forward</span>
                                </a>
                            @endfor
                        </div>
                        <p class="detail-meta"><strong>Official program page:</strong> <a href="${utility.utilityUrl()}">${utility.utilityUrl()}</a></p>
                        <p class="detail-meta"><strong>Program phone:</strong> ${utility.phone()}</p>
                        <p class="detail-meta"><a href="/methodology">See review methodology</a> and <a href="/editorial-standards">editorial standards</a>.</p>
                    </article>
                </div>

                <div class="card-grid">
                    <article class="detail-card">
                        <span class="eyebrow">Covered property types</span>
                        <h2>Where the rule applies</h2>
                        <ul class="detail-list">
                            @for (var propertyType : utility.coveredPropertyTypes())
                                <li>${propertyType}</li>
                            @endfor
                        </ul>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">Covered device types</span>
                        <h2>Devices in scope</h2>
                        <ul class="detail-list">
                            @for (var deviceType : utility.coveredDeviceTypes())
                                <li>${deviceType}</li>
                            @endfor
                        </ul>
                    </article>

                    @if (!utility.residentialNotes().isEmpty())
                        <article class="detail-card">
                            <span class="eyebrow">Residential notes</span>
                            <h2>Residential notes</h2>
                            <ul class="detail-list">
                                @for (var note : utility.residentialNotes())
                                    <li>${note}</li>
                                @endfor
                            </ul>
                        </article>
                    @endif

                    @if (!utility.commercialNotes().isEmpty())
                        <article class="detail-card">
                            <span class="eyebrow">Commercial focus</span>
                            <h2>Commercial and managed properties</h2>
                            <ul class="detail-list">
                                @for (var note : utility.commercialNotes())
                                    <li>${note}</li>
                                @endfor
                            </ul>
                        </article>
                    @endif
                </div>

                <article class="detail-card detail-card--key">
                    <div class="section-heading">
                        <span class="eyebrow">Market cost analysis</span>
                        <h2>Local cost band</h2>
                        <p>Typical testing and repair pricing used to frame next-action decisions in the metro around this utility.</p>
                    </div>
                    <div class="content-grid">
                        <div class="metadata-block">
                            <strong>Testing</strong>
                            <span>${utility.costBand().testingRange()}</span>
                        </div>
                        <div class="metadata-block">
                            <strong>Repair and retest</strong>
                            <span>${utility.costBand().repairRetestRange()}</span>
                        </div>
                    </div>
                    <p class="detail-meta">${utility.costBand().pricingNotes()}</p>
                </article>

                <article class="panel panel--dashed">
                    <div class="section-heading">
                        <span class="eyebrow">Commercial layer</span>
                        <h2>Featured provider direction</h2>
                        <p>Commercial routing stays clearly labeled below the official workflow. This block exists to frame provider discovery without implying authority status.</p>
                    </div>
                    <div class="card-grid">
                        <div class="mini-provider">
                            <img src="/images/design/utility-provider-1.jpg" alt="Backflow technician inspecting an industrial assembly">
                            <div>
                                <strong>Local testing profiles</strong>
                                <span>Use provider profiles and metro pages only after confirming the utility workflow and list rules above.</span>
                            </div>
                        </div>
                        <div class="mini-provider">
                            <img src="/images/design/utility-provider-2.jpg" alt="Pressure vacuum breaker on an exterior wall">
                            <div>
                                <strong>Sponsor layer stays separate</strong>
                                <span>Provider help is reviewed separately from the official utility workflow and never replaces the authority guidance above.</span>
                            </div>
                        </div>
                    </div>
                </article>

                <article class="trust-box trust-box--warning">
                    <span class="trust-box__label">Next step</span>
                    <h2>Tester routing</h2>
                    @if (testerPath != null && testerLabel != null)
                        @if (utility.supportsApprovedTestersPage())
                            <p>Start with the governing authority's published tester list. This route keeps official list entries first and leaves sponsor routing secondary.</p>
                        @else
                            <p>No official tester list is stored for this utility, so the available tester route is a clearly labeled non-official directory.</p>
                        @endif
                        <div class="hero__actions">
                            <a class="button button--primary" href="${testerPath}">${testerLabel}</a>
                            <a class="button button--secondary" href="${utility.utilityUrl()}">Official program page</a>
                            <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility")}">Request local help</a>
                        </div>
                    @else
                        <p>No public tester route is live for this utility yet. Stay on the authority workflow and submission methods before treating any outside provider directory as reliable.</p>
                        <div class="hero__actions">
                            <a class="button button--primary" href="${utility.utilityUrl()}">Official program page</a>
                            <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility")}">Request local help</a>
                        </div>
                    @endif
                </article>

                <section class="section section--tight">
                    <div class="section-heading">
                        <span class="eyebrow">FAQ</span>
                        <h2>Local questions people actually ask</h2>
                    </div>
                    <div class="faq-list">
                        @for (var faqItem : faqItems)
                            <details class="faq-item">
                                <summary>${faqItem.question()}</summary>
                                <p>${faqItem.answer()}</p>
                            </details>
                        @endfor
                    </div>
                </section>
            </div>

            <aside class="side-stack">
                <article class="detail-card">
                    <span class="eyebrow">State compliance layer</span>
                    <h2>State compliance layer</h2>
                    @if (stateGuide != null)
                        <p><a href="/states/${stateGuide.state()}/backflow-testing">${stateGuide.title()}</a></p>
                        <p>${stateGuide.authoritySummary()}</p>
                    @else
                        <p>No published state hub is mapped to this utility yet.</p>
                    @endif
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Support guides</span>
                    <h2>Support guides</h2>
                    @if (relatedGuides.isEmpty())
                        <p>No support guides are mapped yet.</p>
                    @else
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
                    @endif
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Metro coverage</span>
                    <h2>Metro coverage</h2>
                    @if (metros.isEmpty())
                        <p>No metro support pages are mapped to this utility yet.</p>
                    @else
                        <div class="support-links">
                            @for (var metro : metros)
                                <a class="support-link" href="/metros/${metro.state()}/${metro.metroSlug()}/backflow-testing">
                                    <div>
                                        <strong>${metro.title()}</strong>
                                        <span>${metro.description()}</span>
                                    </div>
                                    <span class="material-symbols-outlined">arrow_forward</span>
                                </a>
                            @endfor
                        </div>
                    @endif
                </article>

                <article class="panel panel--dark">
                    <span class="eyebrow" style="color: rgba(197, 234, 223, 0.92);">Technical help</span>
                    <h2 style="color: #ffffff;">Need a direct contact path?</h2>
                    <p>The utility program contact stays visible on-page so the commercial layer never becomes the only way to act.</p>
                    <p><strong>${utility.phone()}</strong></p>
                </article>
            </aside>
        </div>
    </section>
`)
