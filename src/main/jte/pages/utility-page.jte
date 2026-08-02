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
@param String utilityHeading = null
@param String utilitySubmissionLabel = null
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

@template.layouts.verdict(page = page, content = @`
    <article class="bv-utility-record">
        <header class="bv-utility-hero">
            <div class="bv-shell">
                <nav class="bv-breadcrumb" aria-label="Breadcrumb"><a href="/">Home</a><span>/</span><a href="/states/${utility.state()}/backflow-testing">${utility.state()}</a><span>/</span><span>Utility record</span></nav>
                <p class="bv-kicker">${utility.governingEntityType()} · official record</p>
                <h1>${utilityHeading == null ? utility.utilityName() + " backflow requirements" : utilityHeading}</h1>
                <p class="bv-lede">${utility.verdictSummary()}</p>
                <div class="bv-utility-status">
                    <div><span>Last verified</span><strong>${utility.lastVerified().toString()}</strong></div>
                    <div><span>Testing frequency</span><strong>${utility.testingFrequency()}</strong></div>
                    <div><span>Report route</span><strong>${utilitySubmissionLabel == null ? (utility.submissionMethods().isEmpty() ? "Confirm with utility" : utility.submissionMethods().get(0).kind()) : utilitySubmissionLabel}</strong></div>
                    <div><span>Evidence</span><strong>${utility.sources().size()} official source link(s)</strong></div>
                </div>
            </div>
        </header>

        <section class="bv-section bv-section--paper">
            <div class="bv-shell bv-utility-verdict">
                <div><p class="bv-kicker">Fast answer</p><h2>Who, when, and what device?</h2><p>${utility.dueBasis()}</p></div>
                <dl>
                    <div><dt>Who is affected</dt><dd>${utility.whoIsAffected()}</dd></div>
                    <div><dt>Covered devices</dt><dd>@if (utility.coveredDeviceTypes().isEmpty())Confirm the assembly with the utility.@else${String.join(" · ", utility.coveredDeviceTypes())}@endif</dd></div>
                    <div><dt>Risk if missed</dt><dd>${utility.penalties()}</dd></div>
                </dl>
            </div>
        </section>

        <section class="bv-section" id="next-step-paths">
            <div class="bv-shell bv-document-grid">
                <div class="bv-section-heading"><p class="bv-kicker">What to do now</p><h2>Choose the task on your notice</h2><p>The official rule stays on this record; national guidance explains the task.</p></div>
                <div class="bv-choice-list">
                    <a class="bv-choice" href="${annualTestingPath != null ? annualTestingPath : "/backflow-test/"}"><span class="bv-index">01</span><span><strong>Test notice or annual testing</strong><small>Confirm the due basis, eligible tester, and next filing step.</small></span><span class="bv-choice__arrow">→</span></a>
                    <a class="bv-choice bv-choice--warning" href="/failed-backflow-test/"><span class="bv-index">02</span><span><strong>The device failed</strong><small>Repair, retest, and return to this utility workflow.</small></span><span class="bv-choice__arrow">→</span></a>
                    @if (testerPath != null && testerLabel != null)<a class="bv-choice" href="${testerPath}"><span class="bv-index">03</span><span><strong>${testerLabel}</strong><small>Use the official tester route before a general provider search.</small></span><span class="bv-choice__arrow">→</span></a>@endif
                    <a class="bv-choice" href="/submit-backflow-report"><span class="bv-index">04</span><span><strong>Submit or confirm the report</strong><small>${reportAcceptanceHint}</small></span><span class="bv-choice__arrow">→</span></a>
                </div>
            </div>
        </section>

        <section class="bv-section bv-section--ink">
            <div class="bv-shell">
                <div class="bv-section-heading"><p class="bv-kicker">Exact workflow</p><h2>Follow the local sequence</h2></div>
                <ol class="bv-steps">
                    @for (var step : utility.workflowSteps())
                        <li><span>0${utility.workflowSteps().indexOf(step) + 1}</span><div><h3>${step}</h3></div></li>
                    @endfor
                </ol>
            </div>
        </section>

        <section class="bv-section bv-section--paper">
            <div class="bv-shell bv-official-grid">
                <div>
                    <p class="bv-kicker">Tester gate</p><h2>Who can produce an accepted result?</h2>
                    @if (utility.testerGate().hasContent())<p>${utility.testerGate().credentialSummary()}</p>@else<p>Confirm tester eligibility directly with ${utility.utilityName()} before scheduling.</p>@endif
                    @if (testerPath != null && testerLabel != null)<a class="bv-button" href="${testerPath}">${testerLabel}</a>@endif
                </div>
                <div>
                    <p class="bv-kicker">Submission route</p><h2>Where the result goes</h2>
                    @if (utility.reportWorkflow().hasContent())
                        <p><strong>${utility.reportWorkflow().portalName() != null && !utility.reportWorkflow().portalName().isBlank() ? utility.reportWorkflow().portalName() : "Utility reporting workflow"}</strong></p>
                        @if (utility.reportWorkflow().submissionDeadlineDaysAfterTest() != null)<p><strong>Submission deadline:</strong> ${utility.reportWorkflow().deadlineLabel()}</p>@endif
                        @if (!utility.reportWorkflow().requiredIdentifiers().isEmpty())<p><strong>Report packet:</strong> ${String.join(", ", utility.reportWorkflow().requiredIdentifiers())}</p>@endif
                        @if (!utility.testerGate().credentialDocuments().isEmpty())<p><strong>Tester credentials:</strong> ${String.join(", ", utility.testerGate().credentialDocuments())}</p>@endif
                        <p>${noticeIdentifierHint}</p><p>${reportAcceptanceHint}</p>
                    @else<p>Use the official program contact and submission method below.</p>@endif
                    @if (!utility.submissionMethods().isEmpty())
                        <ul class="bv-inline-facts">@for (var method : utility.submissionMethods())<li>${method.label()} <small>${method.kind()}</small></li>@endfor</ul>
                    @endif
                    @if (portalHubPath != null)<a class="bv-text-link" href="${portalHubPath}">${portalHubLabel == null ? "Review portal route" : portalHubLabel} →</a>@endif
                </div>
            </div>
        </section>

        <section class="bv-section">
            <div class="bv-shell bv-utility-detail">
                <div><p class="bv-kicker">Local detail</p><h2>Property, device, and enforcement notes</h2><p>Open the parts that match the property and system. These details stay subordinate to the fast workflow above.</p></div>
                <div>
                    <details open><summary>Program and failure notes</summary><ul>@for (var item : utility.failureHighlights())<li>${item}</li>@endfor @if (utility.annualTesting() != null)@for (var item : utility.annualTesting().highlights())<li>${item}</li>@endfor @endif</ul></details>
                    @if (!utility.residentialNotes().isEmpty())<details><summary>Residential notes</summary><ul>@for (var item : utility.residentialNotes())<li>${item}</li>@endfor</ul></details>@endif
                    @if (!utility.commercialNotes().isEmpty())<details><summary>Commercial notes</summary><ul>@for (var item : utility.commercialNotes())<li>${item}</li>@endfor</ul></details>@endif
                    @if (utility.irrigation() != null)<details><summary>Irrigation</summary><p>${utility.irrigation().summary()}</p><ul>@for (var item : utility.irrigation().highlights())<li>${item}</li>@endfor</ul></details>@endif
                    @if (utility.fireLine() != null)<details><summary>Fire line</summary><p>${utility.fireLine().summary()}</p><ul>@for (var item : utility.fireLine().highlights())<li>${item}</li>@endfor</ul></details>@endif
                    <details><summary>Cost and fee context</summary><p>${utility.costBand().testingRange()}</p><p>${utility.costBand().repairRetestRange()}</p><p>${utility.costBand().pricingNotes()}</p></details>
                </div>
            </div>
        </section>

        <section class="bv-section bv-local">
            <div class="bv-shell bv-source-ledger">
                <div><p class="bv-kicker">Evidence ledger</p><h2>Official sources</h2><p>${utility.sourceExcerpt()}</p><p>BackflowVerdict summarizes these records; the governing entity remains the authority.</p><a class="bv-button" href="${utility.utilityUrl()}">Open official program page</a></div>
                <ol>
                    @for (var source : utility.sources())
                        <li><span>0${utility.sources().indexOf(source) + 1}</span><div><strong>${source.label()}</strong><small>${source.kind()}</small><a href="${source.url()}">Open source ↗</a></div></li>
                    @endfor
                </ol>
            </div>
        </section>

        <section class="bv-section">
            <div class="bv-shell bv-handoff">
                <div><p class="bv-kicker">Commercial handoff · separate layer</p><h2>Still need help after reading the rule?</h2><p>The request keeps this utility ID and source page with the handoff.</p></div>
                <a class="bv-button" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/", "general-testing", "utility")}">Get help with ${utility.utilityName()}</a>
            </div>
        </section>
    </article>
`)
