@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.config.SiteRuntimeConfig
@import owner.backflow.service.HandoffIssueType
@import owner.backflow.service.HandoffResultStatus
@import owner.backflow.service.HandoffSubmissionStatus
@param PageMeta page
@param java.util.List<UtilityRecord> utilities
@param java.util.List<HandoffIssueType> issueTypes
@param java.util.List<HandoffResultStatus> resultStatuses
@param java.util.List<HandoffSubmissionStatus> submissionStatuses
@param String selectedUtilityId = ""
@param String selectedIssueType = ""
@param String selectedPropertyType = ""
@param String propertyLabel = ""
@param String siteAddress = ""
@param String deviceLocation = ""
@param String technicianName = ""
@param String gaugeId = ""
@param String calibrationReference = ""
@param String testReadingSummary = ""
@param String assemblySize = ""
@param String assemblyType = ""
@param String assemblySerial = ""
@param String checkValveOneReading = ""
@param String checkValveTwoReading = ""
@param String openingPointReading = ""
@param String repairSummary = ""
@param String selectedResultStatus = ""
@param String selectedSubmissionStatus = ""
@param String testDateValue = ""
@param String dueDateValue = ""
@param String submissionDateValue = ""
@param String customerFirstName = ""
@param String accountIdentifier = ""
@param String vendorCompanyName = ""
@param String vendorContactName = ""
@param String vendorPhone = ""
@param String vendorEmail = ""
@param String testerLicenseNumber = ""
@param String assemblyMakeModel = ""
@param String submissionReference = ""
@param String permitOrReportNumber = ""
@param String noticeSummary = ""
@param String failedReason = ""
@param String internalNote = ""
@param String sourcePath = ""
@param String formError = ""
@param UtilityRecord selectedUtility = null
@param String returnPath = ""

@template.layouts.workbench(page = page, content = @`
    <section class="section section--tight">
        <div class="container workbench-shell workbench-shell--builder">
            <header class="workbench-header workbench-header--builder">
                <div class="workbench-header__row">
                    <div class="workbench-header__title">
                        <span class="eyebrow kicker-rule">Customer brief desk</span>
                        <h1>Create a 2-minute customer brief</h1>
                        <p>Use this for annual notice and failed-test follow-up. Fill the customer-facing brief first and leave office-only detail behind disclosure.</p>
                    </div>
                    <div class="workbench-header__actions">
                        @if (returnPath != null && !returnPath.isBlank())
                            <a class="button button--secondary button--compact" href="${returnPath}">Back to current rule</a>
                        @endif
                    </div>
                </div>
                <div class="workbench-sequence" aria-label="Handoff order">
                    <div class="workbench-sequence__item">
                        <strong>1</strong>
                        <span>Paste the notice and lock the site</span>
                    </div>
                    <div class="workbench-sequence__item">
                        <strong>2</strong>
                        <span>Add the next date and sender line</span>
                    </div>
                    <div class="workbench-sequence__item">
                        <strong>3</strong>
                        <span>Generate the brief before opening office-only detail</span>
                    </div>
                </div>
            </header>

            <div class="workbench-layout">
                <div class="workbench-main">
                    <article class="workbench-pane workbench-pane--desk">
                        <div class="workbench-pane__header workbench-pane__header--compact">
                            <span class="eyebrow">Office handoff</span>
                            <h2>Prepare the first send</h2>
                            <p>Most jobs should stop after the required fields, customer explanation, and sender line. Open the office record only when the PDF or filing needs more detail.</p>
                        </div>

                        @if (formError != null && !formError.isBlank())
                            <p class="form-error">${formError}</p>
                        @endif

                        <div class="workbench-inline-preview">
                            <div class="workbench-inline-preview__copy">
                                <strong>Customer brief preview</strong>
                                <span>This is the first surface the customer sees. Keep it plain before you open office-only detail.</span>
                            </div>
                            <div data-handoff-send-preview-inline></div>
                        </div>

                        <form method="post" action="/handoffs" class="stack-form workbench-form" data-handoff-builder-form>
                            <input type="hidden" name="sourcePath" value="${sourcePath}">

                            <section class="workbench-section workbench-section--intake">
                                <div class="workbench-section__header">
                                    <span class="eyebrow">Quick intake</span>
                                    <h3>Paste the notice or failed result</h3>
                                    <p>Start here. Pull the obvious date, account, and address context forward before you type anything else. Customer-facing copy stays manual unless you review one of the suggestions below.</p>
                                </div>

                                <div class="claim-form-grid">
                                    <label class="field field--span-2">
                                        <span>Pasted notice or failed-result text</span>
                                        <textarea rows="6" data-handoff-intake-text placeholder="Paste the annual notice, failed-test result, or office note here."></textarea>
                                    </label>
                                </div>
                                <div class="workbench-toolbar">
                                    <button class="button button--secondary" type="button" data-handoff-intake-apply>Pull from pasted text</button>
                                    <button class="button button--secondary" type="button" data-handoff-intake-to-note>Suggest customer note</button>
                                    <button class="button button--secondary" type="button" data-handoff-intake-to-failed-reason>Suggest failed reason</button>
                                </div>
                                @if (selectedUtility == null)
                                    <div class="workbench-demo-strip">
                                        <div class="workbench-demo-strip__copy">
                                            <strong>Need a fast demo first?</strong>
                                            <span>Load one sample job to see the brief, note, and sender line without typing from scratch.</span>
                                        </div>
                                        <div class="workbench-toolbar">
                                            <button class="button button--secondary button--compact" type="button" data-handoff-intake-sample-annual>Try annual sample</button>
                                            <button class="button button--secondary button--compact" type="button" data-handoff-intake-sample-failed>Try failed-test sample</button>
                                        </div>
                                    </div>
                                @endif
                            </section>

                            <section class="workbench-section">
                                <div class="workbench-section__header">
                                    <span class="eyebrow">Customer situation</span>
                                    <h3>Lock the site anchor</h3>
                                    <p>Use the site name, account, or service address the customer will recognize right away.</p>
                                </div>

                                <div class="claim-form-grid">
                                    <label class="field field--span-2">
                                        <span>Site or facility</span>
                                        <input name="propertyLabel" type="text" value="${propertyLabel}" placeholder="Main Street Retail Center">
                                    </label>

                                    <label class="field">
                                        <span>Account or site ID</span>
                                        <input name="accountIdentifier" type="text" value="${accountIdentifier}" placeholder="ACC-2049">
                                    </label>

                                    <label class="field field--span-2">
                                        <span>Service address</span>
                                        <input name="siteAddress" type="text" value="${siteAddress}" placeholder="120 Main Street, Grand Prairie, TX">
                                    </label>
                                </div>
                            </section>

                            <section class="workbench-section">
                                <div class="workbench-section__header">
                                    <span class="eyebrow">Required to send</span>
                                    <h3>Only the fields needed to send</h3>
                                    <p>The free brief should stop at the governing utility, the issue, the outcome, the filing state, and the next date.</p>
                                </div>

                                <div class="claim-form-grid">
                                    <div class="field field--context-card">
                                        <span>Utility</span>
                                        @if (selectedUtility != null)
                                            <input type="hidden" name="utilityId" value="${selectedUtility.utilityId()}" data-selected-label="${selectedUtility.utilityName()}">
                                            <div class="locked-context">
                                                <strong>${selectedUtility.state()} - ${selectedUtility.utilityName()}</strong>
                                                <p>${selectedUtility.verdictSummary()}</p>
                                            </div>
                                        @else
                                            <select name="utilityId" required>
                                                <option value="">Select one</option>
                                                @for (var utility : utilities)
                                                    <option value="${utility.utilityId()}" selected="${utility.utilityId().equals(selectedUtilityId)}">${utility.state()} - ${utility.utilityName()}</option>
                                                @endfor
                                            </select>
                                        @endif
                                    </div>

                                    <div class="field field--context-card">
                                        <span>Issue type</span>
                                        @if ("general-testing".equals(selectedIssueType))
                                            <input type="hidden" name="issueType" value="${selectedIssueType}" data-selected-label="Annual or routine testing">
                                            <div class="locked-context">
                                                <strong>Annual or routine testing</strong>
                                                <p>Locked from the current annual-notice path.</p>
                                            </div>
                                        @elseif ("failed-test-repair".equals(selectedIssueType))
                                            <input type="hidden" name="issueType" value="${selectedIssueType}" data-selected-label="Failed test repair or retest">
                                            <div class="locked-context">
                                                <strong>Failed test repair or retest</strong>
                                                <p>Locked from the current failed-test path.</p>
                                            </div>
                                        @else
                                            <select name="issueType" required>
                                                <option value="">Select one</option>
                                                @for (var issueType : issueTypes)
                                                    <option value="${issueType.code()}" selected="${issueType.code().equals(selectedIssueType)}">${issueType.formLabel()}</option>
                                                @endfor
                                            </select>
                                        @endif
                                    </div>

                                    <label class="field">
                                        <span>Test result</span>
                                        <select name="resultStatus" required>
                                            <option value="">Select one</option>
                                            @for (var resultStatus : resultStatuses)
                                                <option value="${resultStatus.code()}" selected="${resultStatus.code().equals(selectedResultStatus)}">${resultStatus.label()}</option>
                                            @endfor
                                        </select>
                                    </label>

                                    <label class="field">
                                        <span>Utility filing</span>
                                        <select name="submissionStatus" required>
                                            <option value="">Select one</option>
                                            @for (var submissionStatus : submissionStatuses)
                                                <option value="${submissionStatus.code()}" selected="${submissionStatus.code().equals(selectedSubmissionStatus)}">${submissionStatus.label()}</option>
                                            @endfor
                                        </select>
                                    </label>

                                    <label class="field">
                                        <span data-handoff-due-date-label>Next due or filing by</span>
                                        <input name="dueDate" type="date" value="${dueDateValue}" required>
                                    </label>
                                </div>
                            </section>

                            <section class="workbench-section">
                                <div class="workbench-section__header">
                                    <span class="eyebrow">Customer explanation</span>
                                    <h3>Tell the customer why this is in their inbox</h3>
                                    <p>Be direct about why this brief exists and what happens next.</p>
                                </div>

                                <div class="claim-form-grid">
                                    <label class="field field--span-2">
                                        <span>Customer-facing note</span>
                                        <textarea name="noticeSummary" rows="3" placeholder="Optional short note that should appear on the customer copy.">${noticeSummary}</textarea>
                                    </label>

                                    <label class="field field--span-2" data-handoff-failed-reason-field>
                                        <span data-handoff-failed-reason-label>Failure or return-visit reason</span>
                                        <textarea
                                                name="failedReason"
                                                rows="3"
                                                data-handoff-failed-reason-input
                                                placeholder="Required for failed or unable-to-test jobs. Explain what happened and what still has to change."
                                        >${failedReason}</textarea>
                                        <small class="field-help" data-handoff-failed-reason-help>Required when the device failed or the test could not be completed.</small>
                                    </label>
                                </div>
                            </section>

                            <section class="workbench-section">
                                <div class="workbench-section__header">
                                    <span class="eyebrow">Vendor delivery</span>
                                    <h3>Add the sender line</h3>
                                    <p>Company plus phone or email is enough for the first send.</p>
                                </div>

                                <div class="claim-form-grid">
                                    <label class="field">
                                        <span>Vendor company</span>
                                        <input name="vendorCompanyName" type="text" value="${vendorCompanyName}" placeholder="DFW Backflow Services" required>
                                    </label>

                                    <label class="field">
                                        <span>Vendor phone</span>
                                        <input name="vendorPhone" type="text" value="${vendorPhone}" placeholder="972-555-0144">
                                    </label>

                                    <label class="field field--span-2">
                                        <span>Vendor email</span>
                                        <input name="vendorEmail" type="email" value="${vendorEmail}" placeholder="jordan@dfwbackflow.example">
                                    </label>
                                </div>
                            </section>

                            <details class="workbench-disclosure" data-handoff-office-record>
                                <summary>
                                    <div class="workbench-disclosure__summary-copy">
                                        <span class="eyebrow">Optional office record</span>
                                        <strong>Open only for filing, PDF, or vendor-only detail</strong>
                                    </div>
                                    <span class="workbench-disclosure__hint">Property type, assembly detail, readings, filing references, and internal notes stay here.</span>
                                </summary>

                                <div class="workbench-disclosure__body">
                                    <div class="form-divider">
                                        <span class="eyebrow">Site and office detail</span>
                                        <p>Most jobs should stay closed here. Open this only when the office record or PDF needs more than the customer-facing brief above.</p>
                                    </div>

                                    <div class="claim-form-grid">
                                        <label class="field">
                                            <span>Property type</span>
                                            <select name="propertyType">
                                                <option value="" selected="${selectedPropertyType == null || selectedPropertyType.isBlank()}">Select one</option>
                                                <option value="residential" selected="${"residential".equals(selectedPropertyType)}">Residential</option>
                                                <option value="commercial" selected="${"commercial".equals(selectedPropertyType)}">Commercial</option>
                                                <option value="hoa" selected="${"hoa".equals(selectedPropertyType)}">HOA or multifamily</option>
                                                <option value="restaurant-medical" selected="${"restaurant-medical".equals(selectedPropertyType)}">Restaurant or medical</option>
                                                <option value="other" selected="${"other".equals(selectedPropertyType)}">Other</option>
                                            </select>
                                        </label>

                                        <label class="field">
                                            <span>Test date</span>
                                            <input name="testDate" type="date" value="${testDateValue}">
                                        </label>

                                        <label class="field">
                                            <span>Submission date</span>
                                            <input name="submissionDate" type="date" value="${submissionDateValue}">
                                        </label>

                                        <label class="field">
                                            <span>Device location</span>
                                            <input name="deviceLocation" type="text" value="${deviceLocation}" placeholder="Rear mechanical room">
                                        </label>

                                        <label class="field field--span-2">
                                            <span>Customer or site contact</span>
                                            <input name="customerFirstName" type="text" value="${customerFirstName}" placeholder="Morgan">
                                        </label>

                                        <label class="field field--span-2">
                                            <span>Assembly make and model</span>
                                            <input name="assemblyMakeModel" type="text" value="${assemblyMakeModel}" placeholder="Watts 909">
                                        </label>

                                        <label class="field">
                                            <span>Assembly type</span>
                                            <input name="assemblyType" type="text" value="${assemblyType}" placeholder="RP">
                                        </label>

                                        <label class="field">
                                            <span>Assembly size</span>
                                            <input name="assemblySize" type="text" value="${assemblySize}" placeholder="2 in.">
                                        </label>

                                        <label class="field">
                                            <span>Assembly serial</span>
                                            <input name="assemblySerial" type="text" value="${assemblySerial}" placeholder="RP-88421">
                                        </label>
                                    </div>

                                    <div class="form-divider">
                                        <span class="eyebrow">Readings and filing detail</span>
                                        <p>Use this only when the office file needs readings, filing numbers, certification lines, or internal notes.</p>
                                    </div>

                                    <div class="claim-form-grid">
                                        <label class="field">
                                            <span>Portal confirmation or receipt</span>
                                            <input name="submissionReference" type="text" value="${submissionReference}" placeholder="BSI-2049">
                                        </label>

                                        <label class="field">
                                            <span>Permit or report number</span>
                                            <input name="permitOrReportNumber" type="text" value="${permitOrReportNumber}" placeholder="RPT-88421">
                                        </label>

                                        <label class="field">
                                            <span>Check valve 1 held at</span>
                                            <input name="checkValveOneReading" type="text" value="${checkValveOneReading}" placeholder="5.8 psid">
                                        </label>

                                        <label class="field">
                                            <span>Check valve 2 held at</span>
                                            <input name="checkValveTwoReading" type="text" value="${checkValveTwoReading}" placeholder="2.4 psid">
                                        </label>

                                        <label class="field">
                                            <span>Relief or air inlet opened at</span>
                                            <input name="openingPointReading" type="text" value="${openingPointReading}" placeholder="3.1 psid">
                                        </label>

                                        <label class="field field--span-2">
                                            <span>Repairs or materials used</span>
                                            <textarea name="repairSummary" rows="3" placeholder="No repair needed, or note kit, parts, cleanout, or rebuild.">${repairSummary}</textarea>
                                        </label>

                                        <label class="field">
                                            <span>Tester license or cert</span>
                                            <input name="testerLicenseNumber" type="text" value="${testerLicenseNumber}" placeholder="BPAT-7781">
                                        </label>

                                        <label class="field">
                                            <span>Vendor contact</span>
                                            <input name="vendorContactName" type="text" value="${vendorContactName}" placeholder="Jordan Lee">
                                        </label>

                                        <label class="field">
                                            <span>Technician name</span>
                                            <input name="technicianName" type="text" value="${technicianName}" placeholder="Jordan Lee">
                                        </label>

                                        <label class="field">
                                            <span>Gauge ID</span>
                                            <input name="gaugeId" type="text" value="${gaugeId}" placeholder="G-3148">
                                        </label>

                                        <label class="field field--span-2">
                                            <span>Calibration reference</span>
                                            <input name="calibrationReference" type="text" value="${calibrationReference}" placeholder="Cal due 2027-01-15">
                                        </label>

                                        <label class="field field--span-2">
                                            <span>Additional reading notes</span>
                                            <textarea name="testReadingSummary" rows="4" placeholder="Any extra field note that does not fit the structured readings.">${testReadingSummary}</textarea>
                                        </label>

                                        <label class="field field--span-2">
                                            <span>Internal note</span>
                                            <textarea name="internalNote" rows="3" placeholder="Optional. Keep any vendor-only note here.">${internalNote}</textarea>
                                        </label>
                                    </div>
                                </div>
                            </details>

                            <div class="workbench-submitbar">
                                <div class="workbench-submitbar__copy">
                                    <strong>Stop here when the brief is ready to send</strong>
                                    <span>Generate the customer brief first. Office-only detail is optional.</span>
                                </div>
                                <div class="workbench-toolbar">
                                    <button class="button button--primary" type="submit">Generate customer brief</button>
                                </div>
                            </div>
                            <p class="workbench-note" data-handoff-storage-state>Fast path: lock the site, next date, and sender line, then generate the brief.</p>
                        </form>
                    </article>
                </div>

                <aside class="workbench-inspector">
                    <article class="workbench-pane workbench-pane--send workbench-pane--side">
                        <div class="workbench-pane__header">
                            <span class="eyebrow">First send</span>
                            <h2>Customer brief preview</h2>
                            <p>This is the surface the customer sees first. Keep it plain before you open office-only detail.</p>
                        </div>
                        <div class="handoff-preview" data-handoff-send-preview></div>
                    </article>

                    <article class="workbench-pane workbench-pane--subtle workbench-pane--side">
                        <div class="workbench-pane__header">
                            <span class="eyebrow">Selected utility</span>
                            <h2>@if (selectedUtility != null) ${selectedUtility.utilityName()} @else Use the local rule only when needed @endif</h2>
                            @if (selectedUtility != null)
                                <p>${selectedUtility.verdictSummary()}</p>
                            @else
                                <p>Choose the governing utility first. Keep the rule behind the customer brief unless the owner or manager asks for proof.</p>
                            @endif
                        </div>
                        <ul class="detail-list">
                            @if (selectedUtility != null)
                                <li>Last verified: ${selectedUtility.lastVerified().toString()}</li>
                                <li>Testing cadence: ${selectedUtility.testingFrequency()}</li>
                                <li>Who is affected: ${selectedUtility.whoIsAffected()}</li>
                            @endif
                            <li>Lead with the customer brief and keep source proof behind it.</li>
                            <li>Browser memory and last-job reuse stay scoped to the selected utility and issue path.</li>
                        </ul>
                    </article>

                    <article class="workbench-pane workbench-pane--side">
                        <details class="workbench-disclosure">
                            <summary>
                                <div class="workbench-disclosure__summary-copy">
                                    <span class="eyebrow">Optional browser memory</span>
                                    <strong>Saved profile and recent assemblies</strong>
                                </div>
                                <span class="workbench-disclosure__hint">Keep this closed unless repeat office work will save time on this utility and issue path.</span>
                            </summary>

                            <div class="workbench-disclosure__body">
                                <p class="workbench-note">Load last job restores the customer brief and any saved office-only detail for this utility and issue path in this browser. Vendor profile and assembly presets stay separate.</p>
                                <div class="workbench-toolbar">
                                    <button class="button button--secondary button--compact" type="button" data-handoff-load-last>Load last job</button>
                                    <button class="button button--secondary button--compact" type="button" data-handoff-apply-defaults>Use saved vendor profile</button>
                                    <button class="button button--secondary button--compact" type="button" data-handoff-save-defaults>Save vendor profile</button>
                                    <button class="button button--secondary button--compact" type="button" data-handoff-save-assembly>Save assembly preset</button>
                                </div>

                                <div class="workbench-memory">
                                    <div class="workbench-memory__section">
                                        <div class="workbench-memory__header">
                                            <strong>Saved vendor profile</strong>
                                        </div>
                                        <div data-handoff-vendor-profile></div>
                                    </div>

                                    <div class="workbench-memory__section">
                                        <div class="workbench-memory__header">
                                            <strong>Recent assemblies</strong>
                                            <button class="button button--secondary button--compact" type="button" data-handoff-clear-assemblies hidden>Clear history</button>
                                        </div>
                                        <div data-handoff-assembly-presets></div>
                                    </div>
                                </div>
                            </div>
                        </details>
                    </article>
                </aside>
            </div>
        </div>
    </section>
`)
