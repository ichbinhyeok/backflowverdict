@import owner.backflow.web.PageMeta
@import owner.backflow.service.HandoffRecord
@import owner.backflow.config.SiteRuntimeConfig
@param PageMeta page
@param HandoffRecord handoff
@param String shareUrl
@param String packetUrl = ""
@param String briefPdfPath = ""
@param String packetPdfPath = ""
@param String returnPath = ""
@param String briefPreviewPath = ""
@param String briefTrackedPath = ""
@param String packetTrackedPath = ""
@param String fullRuleTrackedPath = ""
@param String officialProgramTrackedPath = ""
@param String helpTrackedPath = ""
@param String submissionGuidance = ""
@param String handoffEventPath = ""
@param long officePreviewOpenCount = 0
@param long publicBriefOpenCount = 0
@param long publicBriefPdfDownloadCount = 0
@param long sendPreparedCount = 0
@param long sendMarkedCount = 0
@param String lastPublicBriefOpenAt = ""
@param String lastSendMarkedAt = ""

@template.layouts.workbench(page = page, content = @`
    <section class="section section--tight">
        <div class="container workbench-shell" data-handoff-event-endpoint="${handoffEventPath}">
            <header class="workbench-header workbench-header--result">
                <div class="workbench-header__row">
                    <div class="workbench-header__title">
                        <span class="eyebrow kicker-rule">Customer brief ready</span>
                        <h1>${handoff.headline()}</h1>
                        <p>First send the brief from your office, confirm it here, then watch for the open. Keep the office record and source links behind it.</p>
                    </div>
                    <div class="workbench-header__actions">
                        @if (returnPath != null && !returnPath.isBlank())
                            <a class="button button--secondary button--compact" href="${returnPath}">Back to current rule</a>
                        @endif
                    </div>
                </div>
                <div class="workbench-statusbar workbench-statusbar--result">
                    <div class="workbench-stat">
                        <strong>Test result</strong>
                        <span>${handoff.resultLabel()}</span>
                    </div>
                    <div class="workbench-stat">
                        <strong>Utility filing status</strong>
                        <span>${handoff.submissionLabel()}</span>
                    </div>
                    @if (handoff.propertyLabel() != null && !handoff.propertyLabel().isBlank())
                        <div class="workbench-stat">
                            <strong>Site</strong>
                            <span>${handoff.propertyLabel()}</span>
                        </div>
                    @endif
                    @if (handoff.dueDate() != null)
                        <div class="workbench-stat">
                            <strong>Action by</strong>
                            <span>${handoff.dueDate().toString()}</span>
                        </div>
                    @endif
                </div>
            </header>

            <article class="workbench-pane workbench-pane--send handoff-send-surface">
                <div class="handoff-send-surface__header">
                    <div class="workbench-pane__header">
                        <span class="eyebrow">First send</span>
                        <h2>Send the customer brief from your office</h2>
                        <p>This is the front surface for a copied link, text draft, or email draft. BackflowPath does not deliver the message for you in this free tool. Your office sends it, confirms it here, then watches for open activity.</p>
                    </div>
                    <div class="handoff-send-surface__actions">
                        @if (briefTrackedPath != null && !briefTrackedPath.isBlank())
                            <a class="button button--primary button--compact" href="${briefTrackedPath}">Open customer brief</a>
                        @elseif (briefPreviewPath != null && !briefPreviewPath.isBlank())
                            <a class="button button--primary button--compact" href="${briefPreviewPath}">Open customer brief</a>
                        @endif
                        <button class="button button--secondary button--compact" type="button" data-copy-target="handoff-first-send-url" data-handoff-event-type="brief_link_copied">Copy brief link</button>
                        <button class="button button--secondary button--compact" type="button" data-handoff-mark-sent="brief_link_marked_sent">Confirm link sent</button>
                        @if (briefPdfPath != null && !briefPdfPath.isBlank())
                            <a class="button button--secondary button--compact" href="${briefPdfPath}">Open customer PDF</a>
                        @endif
                    </div>
                </div>
                <div class="handoff-send-surface__grid">
                    <div class="handoff-send-surface__panel">
                        <label class="field">
                            <span>Customer brief URL</span>
                            <input id="handoff-first-send-url" type="text" readonly value="${shareUrl}">
                        </label>
                        <p>Use this when the customer only needs the result, the next step, and the vendor line in one clean page. This is the sendable surface.</p>
                    </div>
                    <div class="handoff-send-surface__panel handoff-send-surface__panel--support">
                        <strong>Send and recipient activity</strong>
                        <p>After your office sends the first message from its own phone or email, this page can tell you whether the brief was opened.</p>
                        <details class="workbench-disclosure">
                            <summary>
                                <div class="workbench-disclosure__summary-copy">
                                    <span class="eyebrow">Optional tracking</span>
                                    <strong>Open tracking and send notes</strong>
                                </div>
                                <span class="workbench-disclosure__hint">Use this only when you need the exact loop signal after the send.</span>
                            </summary>
                            <div class="workbench-disclosure__body">
                                <p>Prep signals captured ${sendPreparedCount} time(s). Office preview opened on ${officePreviewOpenCount} day(s).</p>
                                @if (sendMarkedCount > 0)
                                    <p>Office confirmed a send on ${sendMarkedCount} day(s)@if (lastSendMarkedAt != null && !lastSendMarkedAt.isBlank()) . Last confirmed ${lastSendMarkedAt}@endif.</p>
                                @else
                                    <p>No manual send confirmation yet. After your office sends the first message from its own phone or email, confirm it here.</p>
                                @endif
                                @if (publicBriefOpenCount > 0)
                                    <p>Recipient open activity landed on ${publicBriefOpenCount} day(s)@if (lastPublicBriefOpenAt != null && !lastPublicBriefOpenAt.isBlank()) . Last opened ${lastPublicBriefOpenAt}@endif.</p>
                                @else
                                    <p>No recipient open yet. This is the signal to watch before you widen the workflow.</p>
                                @endif
                                @if (publicBriefPdfDownloadCount > 0)
                                    <p>Recipient PDF downloaded ${publicBriefPdfDownloadCount} time(s).</p>
                                @endif
                            </div>
                        </details>
                    </div>
                </div>
            </article>

            @if (submissionGuidance != null && !submissionGuidance.isBlank())
                <article class="workbench-pane workbench-pane--subtle">
                    <div class="workbench-pane__header">
                        <span class="eyebrow">Utility filing status</span>
                        <h2>How the submission step works here</h2>
                        <p>${submissionGuidance}</p>
                    </div>
                </article>
            @endif
        </div>
    </section>

    <section class="section section--handoff-result">
        <div class="container handoff-result-layout">
            <div class="stack">
                <details class="workbench-disclosure">
                    <summary>
                        <div class="workbench-disclosure__summary-copy">
                            <span class="eyebrow">Office follow-through</span>
                            <strong>Need the office record or archive PDF?</strong>
                        </div>
                        <span class="workbench-disclosure__hint">Keep this closed unless the office needs the saved copy, deeper proof, or PDF after the first send.</span>
                    </summary>
                    <div class="workbench-disclosure__body">
                        <article class="detail-card">
                            <h2>Office record</h2>
                            <p>Keep this behind the first send. Use it when the office needs the stored attachment, PDF, or deeper proof after the customer brief is already out.</p>
                            <label class="field">
                                <span>Office record URL</span>
                                <input id="handoff-packet-url" type="text" readonly value="${packetUrl}">
                            </label>
                            <div class="card-actions">
                                <button class="button button--secondary" type="button" data-copy-target="handoff-packet-url">Copy archive link</button>
                                @if (packetTrackedPath != null && !packetTrackedPath.isBlank())
                                    <a class="button button--secondary" href="${packetTrackedPath}">Open archive copy</a>
                                @endif
                                @if (packetPdfPath != null && !packetPdfPath.isBlank())
                                    <a class="button button--secondary" href="${packetPdfPath}">Download archive PDF</a>
                                @endif
                            </div>
                        </article>
                    </div>
                </details>

                <details class="workbench-disclosure">
                    <summary>
                        <div class="workbench-disclosure__summary-copy">
                            <span class="eyebrow">Optional send drafts</span>
                            <strong>Need a text or email for the same brief?</strong>
                        </div>
                        <span class="workbench-disclosure__hint">Keep this closed unless the office wants reusable text after the main brief link is ready.</span>
                    </summary>
                    <div class="workbench-disclosure__body">
                        <article class="detail-card detail-card--quiet handoff-send-copy">
                            <p>Use these only after you decide the customer brief link is the front surface. They save retyping the same annual-notice or failed-test explanation.</p>
                            <div class="content-grid">
                                <div>
                                    <label class="field">
                                        <span>Text draft</span>
                                        <textarea id="handoff-sms-text" rows="7" readonly>${handoff.smsText()}</textarea>
                                    </label>
                                    <div class="card-actions">
                                        <button class="button button--secondary" type="button" data-copy-target="handoff-sms-text" data-handoff-event-type="brief_text_copied">Copy text</button>
                                        <button class="button button--secondary" type="button" data-handoff-mark-sent="brief_text_marked_sent">Confirm text sent</button>
                                    </div>
                                </div>

                                <div>
                                    <label class="field">
                                        <span>Email subject</span>
                                        <input id="handoff-email-subject" type="text" readonly value="${handoff.emailSubject()}">
                                    </label>
                                    <div class="card-actions">
                                        <button class="button button--secondary" type="button" data-copy-target="handoff-email-subject">Copy subject</button>
                                    </div>

                                    <label class="field">
                                        <span>Email draft</span>
                                        <textarea id="handoff-email-body" rows="13" readonly>${handoff.emailBody()}</textarea>
                                    </label>
                                    <div class="card-actions">
                                        <button class="button button--secondary" type="button" data-copy-target="handoff-email-body" data-handoff-event-type="brief_email_draft_copied">Copy email draft</button>
                                        <button class="button button--secondary" type="button" data-handoff-mark-sent="brief_email_marked_sent">Confirm email sent</button>
                                    </div>
                                </div>
                            </div>
                        </article>
                    </div>
                </details>

                <div class="content-grid handoff-decision-grid">
                    <article class="panel panel--dark">
                        <div class="section-heading section-heading--vendor-dark">
                            <span class="eyebrow">Office workflow</span>
                            <h2>What the office still owns</h2>
                            <p>Keep the office workflow explicit so the customer does not mistake a field visit for a finished utility file.</p>
                        </div>
                        <ol class="step-list">
                            @for (var step : handoff.vendorActions())
                                <li>${step}</li>
                            @endfor
                        </ol>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">Customer next step</span>
                        <h2>What the customer needs to know</h2>
                        <ul class="detail-list">
                            @for (var step : handoff.customerActions())
                                <li>${step}</li>
                            @endfor
                        </ul>
                    </article>
                </div>

                <article class="detail-card detail-card--key">
                    <span class="eyebrow">Manual office setup</span>
                    <h2>Need this aligned for one real office?</h2>
                    <p>If this brief is useful but your office still rewrites the sender line, annual-notice note, failed-test note, or PDF wording every time, the next layer is setup, not a bigger dashboard. Pilot office setup is $149 one-time.</p>
                    <ul class="detail-list">
                        <li>One office sender default</li>
                        <li>One annual-notice default brief</li>
                        <li>One failed-test default brief</li>
                        <li>Sample utilities and sample PDF output</li>
                    </ul>
                    <div class="card-actions">
                        <a class="button button--secondary" href="/vendors/customer-briefs#setup-package">See office setup</a>
                        <a class="button button--secondary" href="/vendors/customer-briefs#setup-intake">See the intake checklist</a>
                        @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                            <a class="button button--secondary" href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup&amp;body=Company%20name%3A%0AUtilities%3A%0AWorkflow%3A%20annual%20notice%20/%20failed%20test%20/%20both%0AWhat%20you%20want%20aligned%3A%0A">Email setup request</a>
                        @endif
                    </div>
                </article>

                <details class="source-drawer source-drawer--office">
                    <summary>
                        <span class="trust-box__label">Office detail snapshot</span>
                        <strong>Open device, filing, and note details only after the send is already handled</strong>
                        <span class="source-drawer__hint">Keep this page focused on sending the brief first.</span>
                    </summary>
                    <div class="source-drawer__body source-drawer__body--stack">
                        <article class="detail-card">
                            <span class="eyebrow">Job record</span>
                            <h2>What this handoff is tied to</h2>
                            <div class="content-grid">
                                <div>
                                    <strong>Site and device</strong>
                                    <ul class="detail-list">
                                        @if (handoff.propertyLabel() != null && !handoff.propertyLabel().isBlank())
                                            <li>Site: ${handoff.propertyLabel()}</li>
                                        @endif
                                        @if (handoff.accountIdentifier() != null && !handoff.accountIdentifier().isBlank())
                                            <li>Account or site ID: ${handoff.accountIdentifier()}</li>
                                        @endif
                                        @if (handoff.siteAddress() != null && !handoff.siteAddress().isBlank())
                                            <li>Address: ${handoff.siteAddress()}</li>
                                        @endif
                                        @if (handoff.deviceLocation() != null && !handoff.deviceLocation().isBlank())
                                            <li>Device location: ${handoff.deviceLocation()}</li>
                                        @endif
                                        @if (handoff.assemblyMakeModel() != null && !handoff.assemblyMakeModel().isBlank())
                                            <li>Assembly make and model: ${handoff.assemblyMakeModel()}</li>
                                        @endif
                                        @if (handoff.assemblySize() != null && !handoff.assemblySize().isBlank())
                                            <li>Assembly size: ${handoff.assemblySize()}</li>
                                        @endif
                                        @if (handoff.technicianName() != null && !handoff.technicianName().isBlank())
                                            <li>Technician: ${handoff.technicianName()}</li>
                                        @endif
                                        @if (handoff.gaugeId() != null && !handoff.gaugeId().isBlank())
                                            <li>Gauge ID: ${handoff.gaugeId()}</li>
                                        @endif
                                        @if (handoff.calibrationReference() != null && !handoff.calibrationReference().isBlank())
                                            <li>Calibration reference: ${handoff.calibrationReference()}</li>
                                        @endif
                                        @if (handoff.assemblyType() != null && !handoff.assemblyType().isBlank())
                                            <li>Assembly type: ${handoff.assemblyType()}</li>
                                        @endif
                                        @if (handoff.assemblySerial() != null && !handoff.assemblySerial().isBlank())
                                            <li>Assembly serial: ${handoff.assemblySerial()}</li>
                                        @endif
                                        @if (handoff.testDate() != null)
                                            <li>Test date: ${handoff.testDate().toString()}</li>
                                        @endif
                                        @if (handoff.submissionDate() != null)
                                            <li>Submission date: ${handoff.submissionDate().toString()}</li>
                                        @endif
                                    </ul>
                                </div>

                                <div>
                                    <strong>Vendor contact</strong>
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
                                        @if (handoff.testerLicenseNumber() != null && !handoff.testerLicenseNumber().isBlank())
                                            <li>Tester license or cert: ${handoff.testerLicenseNumber()}</li>
                                        @endif
                                        @if (handoff.submissionReference() != null && !handoff.submissionReference().isBlank())
                                            <li>Portal confirmation or receipt: ${handoff.submissionReference()}</li>
                                        @endif
                                        @if (handoff.permitOrReportNumber() != null && !handoff.permitOrReportNumber().isBlank())
                                            <li>Permit or report number: ${handoff.permitOrReportNumber()}</li>
                                        @endif
                                    </ul>
                                </div>
                            </div>
                        </article>

                        @if ((handoff.testReadingSummary() != null && !handoff.testReadingSummary().isBlank())
                                || (handoff.checkValveOneReading() != null && !handoff.checkValveOneReading().isBlank())
                                || (handoff.checkValveTwoReading() != null && !handoff.checkValveTwoReading().isBlank())
                                || (handoff.openingPointReading() != null && !handoff.openingPointReading().isBlank())
                                || (handoff.repairSummary() != null && !handoff.repairSummary().isBlank())
                                || (handoff.submissionReference() != null && !handoff.submissionReference().isBlank())
                                || (handoff.permitOrReportNumber() != null && !handoff.permitOrReportNumber().isBlank()))
                            <article class="detail-card">
                                <span class="eyebrow">Field record</span>
                                <h2>Readings and filing references</h2>
                                <ul class="detail-list">
                                    @if (handoff.submissionReference() != null && !handoff.submissionReference().isBlank())
                                        <li>Portal confirmation or receipt: ${handoff.submissionReference()}</li>
                                    @endif
                                    @if (handoff.permitOrReportNumber() != null && !handoff.permitOrReportNumber().isBlank())
                                        <li>Permit or report number: ${handoff.permitOrReportNumber()}</li>
                                    @endif
                                    @if (handoff.checkValveOneReading() != null && !handoff.checkValveOneReading().isBlank())
                                        <li>Check valve 1 held at: ${handoff.checkValveOneReading()}</li>
                                    @endif
                                    @if (handoff.checkValveTwoReading() != null && !handoff.checkValveTwoReading().isBlank())
                                        <li>Check valve 2 held at: ${handoff.checkValveTwoReading()}</li>
                                    @endif
                                    @if (handoff.openingPointReading() != null && !handoff.openingPointReading().isBlank())
                                        <li>Relief or air inlet opened at: ${handoff.openingPointReading()}</li>
                                    @endif
                                </ul>
                                @if (handoff.repairSummary() != null && !handoff.repairSummary().isBlank())
                                    <p><strong>Repairs or materials used:</strong> ${handoff.repairSummary()}</p>
                                @endif
                                @if (handoff.testReadingSummary() != null && !handoff.testReadingSummary().isBlank())
                                    <p><strong>Additional reading notes:</strong> ${handoff.testReadingSummary()}</p>
                                @endif
                            </article>
                        @endif

                        @if ((handoff.noticeSummary() != null && !handoff.noticeSummary().isBlank()) || (handoff.failedReason() != null && !handoff.failedReason().isBlank()) || (handoff.internalNote() != null && !handoff.internalNote().isBlank()))
                            <article class="detail-card${(handoff.failedReason() != null && !handoff.failedReason().isBlank()) ? " detail-card--warm" : ""}">
                                <span class="eyebrow">Notes</span>
                                <h2>What the team is carrying with this handoff</h2>
                                @if (handoff.noticeSummary() != null && !handoff.noticeSummary().isBlank())
                                    <p><strong>Customer note:</strong> ${handoff.noticeSummary()}</p>
                                @endif
                                @if (handoff.failedReason() != null && !handoff.failedReason().isBlank())
                                    <p><strong>Result note:</strong> ${handoff.failedReason()}</p>
                                @endif
                                @if (handoff.internalNote() != null && !handoff.internalNote().isBlank())
                                    <p><strong>Vendor-only note:</strong> ${handoff.internalNote()}</p>
                                @endif
                            </article>
                        @endif

                        @if (!handoff.commonMistakes().isEmpty())
                            <article class="detail-card detail-card--warm">
                                <span class="eyebrow">Watchouts</span>
                                <h2>What keeps files open</h2>
                                <ul class="detail-list">
                                    @for (var item : handoff.commonMistakes())
                                        <li>${item}</li>
                                    @endfor
                                </ul>
                            </article>
                        @endif
                    </div>
                </details>

                <details class="source-drawer">
                    <summary>
                        <span class="trust-box__label">Source drawer</span>
                        <strong>Open authority links only when someone needs proof</strong>
                    </summary>
                    <div class="source-drawer__body">
                        <p>The customer brief should carry the handoff. Pull in the rule and official program only when a property owner or manager asks for the governing source.</p>
                        <div class="hero__actions">
                            @if (fullRuleTrackedPath != null && !fullRuleTrackedPath.isBlank())
                                <a class="button button--secondary" href="${fullRuleTrackedPath}">View full local rule</a>
                            @endif
                            @if (officialProgramTrackedPath != null && !officialProgramTrackedPath.isBlank())
                                <a class="button button--secondary" href="${officialProgramTrackedPath}">Official program page</a>
                            @endif
                            @if (helpTrackedPath != null && !helpTrackedPath.isBlank())
                                <a class="button button--secondary" href="${helpTrackedPath}">Request local help</a>
                            @endif
                        </div>
                    </div>
                </details>
            </div>
        </div>
    </section>
`)
