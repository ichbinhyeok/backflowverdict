@import owner.backflow.web.PageMeta
@import owner.backflow.service.HandoffRecord
@param PageMeta page
@param HandoffRecord handoff
@param String briefUrl = ""
@param String pdfPath = ""
@param String fullRuleUrl = ""
@param String officialProgramUrl = ""
@param String fullRuleTrackedPath = ""
@param String officialProgramTrackedPath = ""
@param String helpTrackedPath = ""
@param String submissionGuidance = ""

@template.layouts.document(page = page, content = @`
    <section class="document-shell">
        <div class="document-toolbar document-toolbar--record" data-print-hide="true">
            <div class="document-toolbar__group">
                @if (pdfPath != null && !pdfPath.isBlank())
                    <a class="button button--primary" href="${pdfPath}">Download PDF</a>
                @endif
                @if (briefUrl != null && !briefUrl.isBlank())
                <a class="button button--secondary" href="${briefUrl}">Open customer brief</a>
                @endif
                @if (fullRuleTrackedPath != null && !fullRuleTrackedPath.isBlank())
                    <a class="button button--secondary" href="${fullRuleTrackedPath}">View full local rule</a>
                @endif
            </div>
            <span class="document-toolbar__note">Internal page for office use after the customer brief is sent.</span>
        </div>

        <div class="document-context" data-print-hide="true">
            <div class="document-context__intro">
                <span class="eyebrow">Internal workflow</span>
                <strong>Keep the customer brief in front and this office record behind it.</strong>
                <p>Use this page for filing references, note checks, and the saved PDF after the customer-facing send is already out.</p>
            </div>
            <div class="document-context__grid">
                <div class="document-context__card">
                    <span>Front surface</span>
                    <strong>Customer brief</strong>
                    <p>Link or PDF that explains the result and next step in plain language.</p>
                </div>
                <div class="document-context__card">
                    <span>Back surface</span>
                    <strong>Office record</strong>
                    <p>Internal copy for source links, filing references, device details, and office notes.</p>
                </div>
                <div class="document-context__card">
                    <span>Workflow</span>
                    <strong>${handoff.submissionLabel()}</strong>
                    <p>@if (handoff.dueDate() != null) Action by ${handoff.dueDate().toString()}. @else Use this record after the front send is already handled. @endif</p>
                </div>
            </div>
        </div>

        <article class="document-sheet document-sheet--record document-sheet--packet">
            <header class="document-header">
                <div class="document-kicker-row">
                    <div class="document-brandline">
                        <strong class="document-brandline__mark">BackflowPath</strong>
                        <span class="eyebrow">Office record</span>
                    </div>
                    <span class="document-proof ${"pass".equals(handoff.resultStatus()) ? "document-proof--success" : ("fail".equals(handoff.resultStatus()) ? "document-proof--danger" : "document-proof--warning")}">${handoff.resultLabel()}</span>
                </div>
                <h1>${handoff.headline()}</h1>
                <p class="document-intro">${handoff.publicSummary()}</p>
                <div class="document-meta-grid">
                    <div class="document-meta-card document-meta-card--action ${"submitted".equals(handoff.submissionStatus()) ? "document-meta-card--success" : "document-meta-card--pending"}">
                        <strong>Status with city or utility</strong>
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
                </div>
            </header>

            @if (submissionGuidance != null && !submissionGuidance.isBlank())
                <section class="document-banner">
                    <span class="eyebrow">Status with city or utility</span>
                    <p>${submissionGuidance}</p>
                </section>
            @endif

            @if ((handoff.noticeSummary() != null && !handoff.noticeSummary().isBlank()) || (handoff.failedReason() != null && !handoff.failedReason().isBlank()))
                <section class="document-banner">
                    <span class="eyebrow">Result note</span>
                    <p>
                        @if (handoff.noticeSummary() != null && !handoff.noticeSummary().isBlank())
                            ${handoff.noticeSummary()}
                        @endif
                        @if (handoff.noticeSummary() != null && !handoff.noticeSummary().isBlank() && handoff.failedReason() != null && !handoff.failedReason().isBlank())
                            <br><br>
                        @endif
                        @if (handoff.failedReason() != null && !handoff.failedReason().isBlank())
                            ${handoff.failedReason()}
                        @endif
                    </p>
                </section>
            @endif

            <div class="document-grid">
                <section class="document-section document-section--emphasis">
                    <span class="eyebrow">Device record</span>
                    <h2>Assembly details</h2>
                    <ul class="detail-list">
                        @if (handoff.deviceLocation() != null && !handoff.deviceLocation().isBlank())
                            <li>Device location: ${handoff.deviceLocation()}</li>
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
                        @if (handoff.assemblyMakeModel() != null && !handoff.assemblyMakeModel().isBlank())
                            <li>Assembly make and model: ${handoff.assemblyMakeModel()}</li>
                        @endif
                        @if (handoff.assemblySize() != null && !handoff.assemblySize().isBlank())
                            <li>Assembly size: ${handoff.assemblySize()}</li>
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
                        @if (handoff.submissionReference() != null && !handoff.submissionReference().isBlank())
                            <li>Portal confirmation or receipt: ${handoff.submissionReference()}</li>
                        @endif
                        @if (handoff.permitOrReportNumber() != null && !handoff.permitOrReportNumber().isBlank())
                            <li>Permit or report number: ${handoff.permitOrReportNumber()}</li>
                        @endif
                    </ul>
                </section>

                <section class="document-section">
                    <span class="eyebrow">Field readings</span>
                    <h2>Recorded test readings</h2>
                    @if ((handoff.testReadingSummary() != null && !handoff.testReadingSummary().isBlank())
                            || (handoff.checkValveOneReading() != null && !handoff.checkValveOneReading().isBlank())
                            || (handoff.checkValveTwoReading() != null && !handoff.checkValveTwoReading().isBlank())
                            || (handoff.openingPointReading() != null && !handoff.openingPointReading().isBlank())
                            || (handoff.repairSummary() != null && !handoff.repairSummary().isBlank()))
                        <ul class="detail-list">
                            @if (handoff.checkValveOneReading() != null && !handoff.checkValveOneReading().isBlank())
                                <li>Check valve 1 held at: ${handoff.checkValveOneReading()}</li>
                            @endif
                            @if (handoff.checkValveTwoReading() != null && !handoff.checkValveTwoReading().isBlank())
                                <li>Check valve 2 held at: ${handoff.checkValveTwoReading()}</li>
                            @endif
                            @if (handoff.openingPointReading() != null && !handoff.openingPointReading().isBlank())
                                <li>Relief or air inlet opened at: ${handoff.openingPointReading()}</li>
                            @endif
                            @if (handoff.repairSummary() != null && !handoff.repairSummary().isBlank())
                                <li>Repairs or materials used: ${handoff.repairSummary()}</li>
                            @endif
                        </ul>
                        @if (handoff.testReadingSummary() != null && !handoff.testReadingSummary().isBlank())
                            <p><strong>Additional reading notes:</strong> ${handoff.testReadingSummary()}</p>
                        @endif
                    @else
                        <p>No structured readings were recorded for this office record.</p>
                    @endif
                </section>
            </div>

            <div class="document-grid">
                <section class="document-section">
                    <span class="eyebrow">Prepared for</span>
                    <h2>Customer and site</h2>
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
                        @if (handoff.propertyType() != null && !handoff.propertyType().isBlank())
                            <li>Property type: ${handoff.propertyType()}</li>
                        @endif
                    </ul>
                </section>

                <section class="document-section">
                    <span class="eyebrow">Prepared by</span>
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
                        @if (handoff.testerLicenseNumber() != null && !handoff.testerLicenseNumber().isBlank())
                            <li>Tester license or cert: ${handoff.testerLicenseNumber()}</li>
                        @endif
                    </ul>
                </section>
            </div>

            <div class="document-grid">
                <section class="document-section">
                    <span class="eyebrow">Customer next step</span>
                    <h2>What happens now</h2>
                    <ul class="detail-list">
                        @for (var step : handoff.customerActions())
                            <li>${step}</li>
                        @endfor
                    </ul>
                </section>

                <section class="document-section">
                    <span class="eyebrow">Office workflow</span>
                    <h2>What the office is still finishing</h2>
                    <ol class="step-list">
                        @for (var step : handoff.vendorActions())
                            <li>${step}</li>
                        @endfor
                    </ol>
                </section>
            </div>

            @if (!handoff.commonMistakes().isEmpty())
                <section class="document-section document-section--warn">
                    <span class="eyebrow">Watchouts</span>
                    <h2>What keeps the file open</h2>
                    <ul class="detail-list">
                        @for (var item : handoff.commonMistakes())
                            <li>${item}</li>
                        @endfor
                    </ul>
                </section>
            @endif

            <section class="document-section">
                <span class="eyebrow">Source drawer</span>
                <h2>Verified local sources</h2>
                <p>Use the full local rule or the official program only when someone needs the governing source behind this office record.</p>
                <div class="document-actions" data-print-hide="true">
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
                <ul class="detail-list document-link-list">
                    @if (briefUrl != null && !briefUrl.isBlank())
                            <li>Customer brief: ${briefUrl}</li>
                    @endif
                    @if (fullRuleUrl != null && !fullRuleUrl.isBlank())
                        <li>Full local rule: ${fullRuleUrl}</li>
                    @endif
                    @if (officialProgramUrl != null && !officialProgramUrl.isBlank())
                        <li>Official program page: ${officialProgramUrl}</li>
                    @endif
                </ul>
            </section>

            <footer class="document-footer">
                <p>This office record was prepared from verified local rule and official program sources. Use the source links when someone needs the governing requirement.</p>
            </footer>
        </article>
    </section>
`)
