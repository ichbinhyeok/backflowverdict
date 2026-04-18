@import owner.backflow.web.PageMeta
@import owner.backflow.service.HandoffRecord
@param PageMeta page
@param HandoffRecord handoff
@param String shareUrl
@param String pdfPath = ""
@param String submissionGuidance = ""
@param String fullRuleTrackedPath = ""
@param String officialProgramTrackedPath = ""

@template.layouts.document(page = page, content = @`
    <section class="document-shell">
        <div class="document-toolbar" data-print-hide="true">
            @if (pdfPath != null && !pdfPath.isBlank())
                <a class="button button--primary" href="${pdfPath}">Download PDF</a>
            @endif
        </div>

        <article class="document-sheet">
            <header class="document-header">
                <div class="document-kicker-row">
                    <span class="eyebrow">Customer brief</span>
                    <span class="document-proof">${handoff.resultLabel()}</span>
                </div>
                <h1>${handoff.headline()}</h1>
                <p class="document-intro">${handoff.publicSummary()}</p>
                <section class="document-banner document-banner--summary">
                    <span class="eyebrow">Plain-language summary</span>
                    <p>
                        <strong>Why you got this:</strong>
                        @if (handoff.failedReason() != null && !handoff.failedReason().isBlank())
                            ${handoff.failedReason()}
                        @elseif (handoff.noticeSummary() != null && !handoff.noticeSummary().isBlank())
                            ${handoff.noticeSummary()}
                        @elseif ("failed-test-repair".equals(handoff.issueType()))
                            This brief is tied to a failed backflow test for this site, so the repair, retest, or closeout step on that failed-device file still needs attention.
                        @elseif ("general-testing".equals(handoff.issueType()))
                            This brief is tied to the site's annual backflow testing requirement, so the testing or filing step connected to that notice still needs attention.
                        @else
                            This site still has a backflow requirement open, so the next test or filing step has to be handled before it is treated as complete.
                        @endif
                    </p>
                    <p>
                        <strong>What you need to do:</strong>
                        @if (!handoff.customerActions().isEmpty())
                            ${handoff.customerActions().get(0)}
                        @else
                            Review the result, follow the next step on this page, and keep the due date in view if one is shown.
                        @endif
                    </p>
                    <p>
                        <strong>Who is handling the filing:</strong>
                        @if (handoff.vendorCompanyName() != null && !handoff.vendorCompanyName().isBlank())
                            ${handoff.vendorCompanyName()} is handling the utility filing and follow-up.
                        @else
                            Your vendor is handling the utility filing and follow-up.
                        @endif
                    </p>
                </section>
                <div class="document-meta-grid">
                    <div class="document-meta-card document-meta-card--action">
                        <strong>Utility filing status</strong>
                        <span>${handoff.submissionLabel()}</span>
                    </div>
                    <div class="document-meta-card">
                        <strong>Utility</strong>
                        <span>${handoff.utilityName()}</span>
                    </div>
                    @if (handoff.testDate() != null)
                        <div class="document-meta-card">
                            <strong>Test date</strong>
                            <span>${handoff.testDate().toString()}</span>
                        </div>
                    @endif
                    @if (handoff.dueDate() != null)
                        <div class="document-meta-card">
                            <strong>Action by</strong>
                            <span>${handoff.dueDate().toString()}</span>
                        </div>
                    @endif
                    @if (handoff.propertyLabel() != null && !handoff.propertyLabel().isBlank())
                        <div class="document-meta-card">
                            <strong>Site</strong>
                            <span>${handoff.propertyLabel()}</span>
                        </div>
                    @endif
                </div>
            </header>

            @if (submissionGuidance != null && !submissionGuidance.isBlank())
                <section class="document-banner">
                    <span class="eyebrow">Utility filing status</span>
                    <p>${submissionGuidance}</p>
                </section>
            @endif

            @if ((fullRuleTrackedPath != null && !fullRuleTrackedPath.isBlank())
                    || (officialProgramTrackedPath != null && !officialProgramTrackedPath.isBlank()))
                <section class="document-banner document-banner--office" data-print-hide="true">
                    <span class="eyebrow">Need the official rule?</span>
                    <p>Use the local rule or the official program page when you need the governing source behind this brief.</p>
                    <div class="document-actions">
                        @if (fullRuleTrackedPath != null && !fullRuleTrackedPath.isBlank())
                            <a class="button button--secondary" href="${fullRuleTrackedPath}">View full local rule</a>
                        @endif
                        @if (officialProgramTrackedPath != null && !officialProgramTrackedPath.isBlank())
                            <a class="button button--secondary" href="${officialProgramTrackedPath}">Official program page</a>
                        @endif
                    </div>
                </section>
            @endif

            <div class="document-grid">
                <section class="document-section document-section--emphasis">
                    <span class="eyebrow">What to do now</span>
                    <h2>Next step</h2>
                    <ul class="detail-list">
                        @for (var step : handoff.customerActions())
                            <li>${step}</li>
                        @endfor
                    </ul>
                </section>

                <section class="document-section">
                    <span class="eyebrow">Who is handling the filing</span>
                    <h2>What your vendor is handling</h2>
                    <ol class="step-list">
                        @for (var step : handoff.vendorActions())
                            <li>${step}</li>
                        @endfor
                    </ol>
                </section>
            </div>

            <div class="document-grid">
                <section class="document-section">
                    <span class="eyebrow">Who to contact</span>
                    <h2>Vendor contact</h2>
                    <ul class="detail-list">
                        @if (handoff.vendorCompanyName() != null && !handoff.vendorCompanyName().isBlank())
                            <li>Company: ${handoff.vendorCompanyName()}</li>
                        @endif
                        @if (handoff.vendorContactName() != null && !handoff.vendorContactName().isBlank())
                            <li>Contact: ${handoff.vendorContactName()}</li>
                        @endif
                        @if (handoff.vendorPhone() != null && !handoff.vendorPhone().isBlank())
                            <li>Phone: ${handoff.vendorPhone()}</li>
                        @endif
                        @if (handoff.vendorEmail() != null && !handoff.vendorEmail().isBlank())
                            <li>Email: ${handoff.vendorEmail()}</li>
                        @endif
                    </ul>
                </section>

                <section class="document-section">
                    <span class="eyebrow">What this brief is tied to</span>
                    <h2>Site details</h2>
                    <ul class="detail-list">
                        @if (handoff.customerFirstName() != null && !handoff.customerFirstName().isBlank())
                            <li>Customer or contact: ${handoff.customerFirstName()}</li>
                        @endif
                        @if (handoff.accountIdentifier() != null && !handoff.accountIdentifier().isBlank())
                            <li>Account or site ID: ${handoff.accountIdentifier()}</li>
                        @endif
                        @if (handoff.propertyLabel() != null && !handoff.propertyLabel().isBlank())
                            <li>Site: ${handoff.propertyLabel()}</li>
                        @endif
                        @if (handoff.siteAddress() != null && !handoff.siteAddress().isBlank())
                            <li>Address: ${handoff.siteAddress()}</li>
                        @endif
                    </ul>
                </section>
            </div>

            @if (!handoff.commonMistakes().isEmpty())
                <section class="document-section document-section--warn">
                    <span class="eyebrow">Watchouts</span>
                    <h2>What leaves the file open</h2>
                    <ul class="detail-list">
                        @for (var item : handoff.commonMistakes())
                            <li>${item}</li>
                        @endfor
                    </ul>
                </section>
            @endif

            <footer class="document-footer">
                <p>For job follow-up, use the vendor contact on this page. For the governing requirement, use the official rule or program links above.</p>
            </footer>
        </article>
    </section>
`)
