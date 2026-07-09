@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.ProviderRecord
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
@param String portalHubPath = null
@param String portalHubLabel = null
@param String noticeIdentifierHint
@param String reportAcceptanceHint
@param java.util.List<owner.backflow.web.FaqItem> faqItems
@param java.util.List<CityAliasRecord> cityAliases = java.util.List.of()
@param java.util.List<ProviderRecord> providers = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()
@param java.util.List<MetroRecord> metros = java.util.List.of()
@param StateGuideRecord stateGuide = null

@template.layouts.app(page = page, content = @`
    <section class="page-hero page-hero--utility page-hero--compact-mobile">
        <div class="container page-frame">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">${utility.governingEntityType()}</span>
                <h1>${utility.utilityName()} backflow testing requirements</h1>
                <p class="lede">${utility.verdictSummary()}</p>
                <p class="detail-meta">Use this page to confirm the governing rule, then open the focused page that matches your exact situation.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="${utility.utilityUrl()}">Official program page</a>
                    <a class="button button--secondary" href="/notice-finder?q=${utility.canonicalSlug()}">Find from notice</a>
                    <a class="button button--secondary" href="#next-step-paths">Choose your situation</a>
                </div>
                <div class="hero__stats">
                    <span>Testing cadence: ${utility.testingFrequency()}</span>
                    <span>Submission: ${utility.submissionMethods().isEmpty() ? "Confirm with utility" : utility.submissionMethods().get(0).kind()}</span>
                    <span>Last verified: ${utility.lastVerified().toString()}</span>
                    <span>Verification code: <a href="/methodology#verification-code-${utility.reviewerInitials().toLowerCase()}">${utility.reviewerInitials()}</a></span>
                    <span>Freshness window: ${utility.staleAfterDays()} days</span>
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

    <section class="section section--tight">
        <div class="container">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Fast compliance answer</span>
                <h2>What to confirm first for ${utility.utilityName()}</h2>
                <p>Searchers usually need the tester gate, the filing route, and the proof trail before they need a long explainer. This source-backed snapshot keeps those actions visible before you call, repair, retest, or upload a report.</p>
                <div class="metadata-grid">
                    <div class="metadata-block">
                        <strong>Deadline trigger</strong>
                        @if (utility.deadlinePolicy().reportDueDaysAfterTest() != null)
                            <span>${utility.deadlinePolicy().reportDueLabel()}</span>
                        @else
                            <span>${utility.testingFrequency()} - ${utility.dueBasis()}</span>
                        @endif
                    </div>
                    <div class="metadata-block">
                        <strong>Tester gate</strong>
                        @if (utility.testerGate().hasContent())
                            <span>${utility.testerGate().credentialSummary()}</span>
                        @else
                            <span>${testerPath != null && testerLabel != null ? testerLabel : "Confirm tester status with the utility before scheduling."}</span>
                        @endif
                    </div>
                    <div class="metadata-block">
                        <strong>Report route</strong>
                        @if (utility.reportWorkflow().hasContent())
                            <span>${utility.reportWorkflow().portalName() != null && !utility.reportWorkflow().portalName().isBlank() ? utility.reportWorkflow().portalName() : "Utility reporting workflow"}</span>
                        @else
                            <span>${utility.submissionMethods().isEmpty() ? "Confirm the current utility filing path before the test." : utility.submissionMethods().get(0).label()}</span>
                        @endif
                    </div>
                    <div class="metadata-block">
                        <strong>Proof to keep</strong>
                        <span>${utility.reportWorkflow().acceptanceProof() != null && !utility.reportWorkflow().acceptanceProof().isBlank() ? utility.reportWorkflow().acceptanceProof() : reportAcceptanceHint}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Failed-test branch</strong>
                        <span>Repair first, complete a passing retest, then file the accepted report path for this utility.</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Source trail</strong>
                        <span>${utility.sources().size()} source link(s), verified ${utility.lastVerified().toString()}.</span>
                    </div>
                </div>
                <div class="card-actions">
                    @if (testerPath != null && testerLabel != null)
                        <a class="button button--secondary" href="${testerPath}">${testerLabel}</a>
                    @endif
                    @if (portalHubPath != null && portalHubLabel != null)
                        <a class="button button--secondary" href="${portalHubPath}">${portalHubLabel}</a>
                    @endif
                    <a class="button button--primary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/failed-test">Failed-test steps</a>
                    <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility-fast-answer")}">Request local help</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card">
                <span class="eyebrow">Report packet</span>
                <h2>What has to be ready before the filing is complete</h2>
                <ul class="detail-list">
                    @if (utility.reportWorkflow().submissionDeadlineDaysAfterTest() != null)
                        <li><strong>Submission deadline:</strong> ${utility.reportWorkflow().deadlineLabel()}</li>
                    @endif
                    @if (utility.reportWorkflow().paperAccepted() != null)
                        <li><strong>Paper reports:</strong> ${utility.reportWorkflow().paperAcceptedLabel()}</li>
                    @endif
                    @if (utility.reportWorkflow().filingFee() != null && utility.reportWorkflow().filingFee().hasContent())
                        <li><strong>Filing fee:</strong> ${utility.reportWorkflow().filingFee().display()}</li>
                    @endif
                    @if (!utility.reportWorkflow().requiredIdentifiers().isEmpty())
                        @for (var requiredIdentifier : utility.reportWorkflow().requiredIdentifiers())
                            <li><strong>Report field:</strong> ${requiredIdentifier}</li>
                        @endfor
                    @endif
                    @for (var step : utility.workflowSteps())
                        <li>${step}</li>
                    @endfor
                </ul>
            </article>

            <article class="detail-card detail-card--warm">
                <span class="eyebrow">Rejection risk</span>
                <h2>What can keep the account out of compliance</h2>
                <ul class="detail-list">
                    @if (utility.testerGate().gaugeCalibrationWindowMonths() != null)
                        <li>${utility.testerGate().calibrationLabel()}</li>
                    @endif
                    @if (utility.testerGate().deviceScopeLimit() != null && !utility.testerGate().deviceScopeLimit().isBlank())
                        <li>${utility.testerGate().deviceScopeLimit()}</li>
                    @endif
                    @if (utility.failedTestPolicy().failedReportDeadlineHours() != null)
                        <li>${utility.failedTestPolicy().failedReportDeadlineLabel()}</li>
                    @endif
                    @if (utility.failedTestPolicy().repairDeadlineDays() != null)
                        <li>${utility.failedTestPolicy().repairDeadlineLabel()}</li>
                    @endif
                    @for (var failureHighlight : utility.failureHighlights())
                        <li>${failureHighlight}</li>
                    @endfor
                    <li>${utility.costBand().pricingNotes()}</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Before scheduling</span>
                <h2>Notice checklist before you call or file</h2>
                <p>The strongest local backflow pages answer these questions before the user calls anyone. This utility record keeps the notice clues and acceptance gates together so the next step is not just a generic backflow testing search.</p>
                <ul class="detail-list">
                    <li><strong>Due basis:</strong> ${utility.dueBasis()}</li>
                    <li><strong>Notice or device clue:</strong> ${noticeIdentifierHint}</li>
                    <li><strong>Tester route:</strong> ${testerPath != null && testerLabel != null ? testerLabel : "Confirm tester status with the utility before scheduling."}</li>
                    <li><strong>Submission path:</strong> ${utility.submissionMethods().isEmpty() ? "Confirm the current report path with the program contact." : utility.submissionMethods().get(0).label()}</li>
                    <li><strong>Acceptance rule:</strong> ${reportAcceptanceHint}</li>
                    <li><strong>Cost signal:</strong> ${utility.costBand().testingRange()}</li>
                </ul>
                <div class="card-actions">
                    @if (annualTestingPath != null)
                        <a class="button button--secondary" href="${annualTestingPath}">Annual testing</a>
                    @endif
                    @if (portalHubPath != null && portalHubLabel != null)
                        <a class="button button--secondary" href="${portalHubPath}">${portalHubLabel}</a>
                    @endif
                </div>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Owner vs tester</span>
                <h2>Know who has to act</h2>
                <div class="metadata-grid">
                    <div class="metadata-block">
                        <strong>Property owner</strong>
                        <span>Match the notice, address, device type, due date, and utility record before hiring or approving work.</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Tester</strong>
                        <span>Confirm certification, registration, gauge calibration, and the required submission portal before filing the report.</span>
                    </div>
                </div>
                <p class="detail-meta">This split matters because many utility pages reject reports when the tester, credentials, portal entry, or fee path is wrong.</p>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container page-frame">
            <div class="stack">
                <section class="section section--tight" id="next-step-paths">
                    <div class="section-heading">
                        <span class="eyebrow">Next-step paths</span>
                        <h2>Start with the page that matches your situation</h2>
                        <p>This page is the rule hub. Use it to confirm the governing utility workflow, then open the focused page that matches the actual situation on site.</p>
                    </div>
                    <div class="card-grid">
                        @if (annualTestingPath != null)
                            <article class="detail-card detail-card--key">
                                <span class="eyebrow">Routine notice</span>
                                <h2>Annual testing</h2>
                                <p>Open the annual page when the utility notice is about routine testing, timing, and accepted submission methods.</p>
                                <div class="card-actions">
                                    <a class="button button--secondary" href="${annualTestingPath}">Open annual testing</a>
                                </div>
                            </article>
                        @endif

                        <article class="detail-card detail-card--warm">
                            <span class="eyebrow">Urgent status</span>
                            <h2>Failed test</h2>
                                <p>Open the failed-test page when the device already failed and you need the repair, retest, and reporting order.</p>
                                <div class="card-actions">
                                    <a class="button button--primary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/failed-test">Open failed-test steps</a>
                                </div>
                            </article>

                        @if (irrigationPath != null)
                            <article class="detail-card">
                                <span class="eyebrow">System-specific</span>
                                <h2>Irrigation</h2>
                                <p>Open the irrigation page when the question is tied to sprinkler systems, reclaimed water, or landscape devices.</p>
                                <div class="card-actions">
                                    <a class="button button--secondary" href="${irrigationPath}">Open irrigation rules</a>
                                </div>
                            </article>
                        @endif

                        @if (fireLinePath != null)
                            <article class="detail-card">
                                <span class="eyebrow">System-specific</span>
                                <h2>Fire line</h2>
                                <p>Open the fire-line page when the backflow assembly serves fire protection equipment or a managed commercial site.</p>
                                <div class="card-actions">
                                    <a class="button button--secondary" href="${fireLinePath}">Open fire-line rules</a>
                                </div>
                            </article>
                        @endif

                        @if (testerPath != null && testerLabel != null)
                            <article class="detail-card">
                                <span class="eyebrow">Provider route</span>
                                <h2>${testerLabel}</h2>
                                @if (utility.supportsApprovedTestersPage())
                                    <p>Use the published tester route after you confirm the rule, due basis, and submission path on this utility page.</p>
                                @else
                                    <p>Use the clearly labeled non-official tester route only after you confirm the utility workflow and reporting requirements.</p>
                                @endif
                                <div class="card-actions">
                                    <a class="button button--secondary" href="${testerPath}">${testerLabel}</a>
                                </div>
                            </article>
                        @endif
                    </div>
                </section>

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
                        <p>Every focused page on this utility still runs through this authority sequence. Confirm the rule here before you branch into repair, testing, or provider routing.</p>
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
                        @if (portalHubPath != null && portalHubLabel != null)
                            <p class="detail-meta"><strong>Portal family:</strong> <a href="${portalHubPath}">${portalHubLabel}</a></p>
                        @endif
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

                <article class="trust-box trust-box--warning">
                    <span class="trust-box__label">After the rule is clear</span>
                    <h2>Need a tester or local help?</h2>
                    @if (testerPath != null && testerLabel != null)
                        @if (utility.supportsApprovedTestersPage())
                            <p>Start with the governing authority's published tester list. Use provider help only after the official rule, due basis, and submission path are clear.</p>
                        @else
                            <p>No official tester list is stored for this utility, so the available tester route is a clearly labeled non-official directory.</p>
                        @endif
                        <div class="hero__actions">
                            <a class="button button--primary" href="${testerPath}">${testerLabel}</a>
                            <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility")}">Request local help</a>
                        </div>
                    @else
                        <p>No public tester route is live for this utility yet. Stay on the authority workflow and submission methods before treating any outside provider directory as reliable.</p>
                        <div class="hero__actions">
                            <a class="button button--primary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility")}">Request local help</a>
                        </div>
                    @endif
                </article>

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
                        <span class="eyebrow">Provider browse layer</span>
                        <h2>Public provider direction</h2>
                        <p>Provider routing stays clearly labeled below the official workflow. This block exists to frame public provider discovery without implying authority status.</p>
                    </div>
                    @if (providers.isEmpty())
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
                                    <strong>Public directory stays separate</strong>
                                    <span>Provider help is reviewed separately from the official utility workflow and never replaces the authority guidance above.</span>
                                </div>
                            </div>
                        </div>
                    @else
                        <div class="card-grid card-grid--three">
                            @for (var provider : providers)
                                <article class="detail-card detail-card--quiet">
                                    <span class="pill">Mapped provider</span>
                                    <h3>${provider.providerName()}</h3>
                                    <p>${provider.pageLabel()}</p>
                                    <div class="card-actions">
                                        @if (provider.phone() != null && !provider.phone().isBlank())
                                            <a class="button button--primary" href="tel:${provider.phone().replace(" ", "")}">Call</a>
                                        @endif
                                        <a class="button button--secondary" href="/providers/${provider.providerId()}/">Profile</a>
                                        @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                                            <a class="button button--secondary" href="${provider.officialApprovalSourceUrl()}">Authority source</a>
                                        @endif
                                    </div>
                                </article>
                            @endfor
                        </div>
                    @endif
                </article>
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

                @if (!cityAliases.isEmpty())
                    <article class="detail-card">
                        <span class="eyebrow">City routes</span>
                        <h2>Local city searches mapped here</h2>
                        <p>These city pages preserve the local search term while keeping the governing utility workflow intact.</p>
                        <div class="support-links">
                            @for (var alias : cityAliases)
                                <a class="support-link" href="/cities/${alias.state()}/${alias.aliasSlug()}/backflow-testing">
                                    <div>
                                        <strong>${alias.city()} backflow testing</strong>
                                        <span>${alias.justification()}</span>
                                    </div>
                                    <span class="material-symbols-outlined">arrow_forward</span>
                                </a>
                            @endfor
                        </div>
                    </article>
                @endif

                <article class="panel panel--dark">
                    <span class="eyebrow" style="color: rgba(197, 234, 223, 0.92);">Technical help</span>
                    <h2 style="color: #ffffff;">Need a direct contact path?</h2>
                    <p>The utility program contact stays visible on-page so the provider layer never becomes the only way to act.</p>
                    <p><strong>${utility.phone()}</strong></p>
                </article>
            </aside>
        </div>
    </section>
`)
