@import owner.backflow.web.PageMeta
@import owner.backflow.service.AdminWorkspaceInspectorService.StorageFileSummary
@import owner.backflow.service.HandoffChannelReportService.HandoffChannelReport
@import owner.backflow.service.LeadInboxItem
@import owner.backflow.service.HandoffRecord
@param PageMeta page
@param boolean authenticated = false
@param boolean adminConfigured = true
@param boolean error = false
@param String username = "admin"
@param String csrfToken = ""
@param java.util.List<LeadInboxItem> inbox = java.util.List.of()
@param int leadCount = 0
@param int assignedLeadCount = 0
@param int unassignedLeadCount = 0
@param int uncoveredLeadCount = 0
@param java.util.Map<String, Long> issueCounts = java.util.Map.of()
@param java.util.Map<String, Long> pageFamilyCounts = java.util.Map.of()
@param java.util.Map<String, Long> utilityCounts = java.util.Map.of()
@param java.util.Map<String, Long> providerCoverageGaps = java.util.Map.of()
@param java.util.Map<String, Long> sponsorProspectMetroCounts = java.util.Map.of()
@param java.util.List<owner.backflow.data.model.ProviderRecord> privateSponsorInventory = java.util.List.of()
@param java.util.List<owner.backflow.service.LeadDeliveryRecord> recentDeliveries = java.util.List.of()
@param java.util.List<owner.backflow.service.ProviderClaimRecord> providerClaims = java.util.List.of()
@param int publicProviderCount = 0
@param int sponsorOnlyProviderCount = 0
@param int sponsorActiveProviderCount = 0
@param int sponsorProspectProviderCount = 0
@param int deliveredLeadCount = 0
@param int queuedDeliveryCount = 0
@param int providerClaimCount = 0
@param int heldProviderCount = 0
@param HandoffChannelReport handoffReport = null
@param java.util.List<HandoffRecord> recentHandoffs = java.util.List.of()
@param String storageRoot = ""
@param java.util.List<StorageFileSummary> storageFiles = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">Admin</span>
            <h1>${authenticated ? "Admin control room" : "Admin login"}</h1>
            <p class="lede">${authenticated ? "Review captured leads, vendor handoff workflow signals, and the live JSON or CSV store from one protected page." : "Use the configured admin credentials to open the private lead queue."}</p>
            @if (authenticated)
                <div class="hero__stats">
                    <span>${leadCount} leads stored</span>
                    <span>${assignedLeadCount} assigned</span>
                    <span>${unassignedLeadCount} unassigned</span>
                    <span>${uncoveredLeadCount} without provider coverage</span>
                    <span>${providerClaimCount} provider requests</span>
                    <span>${handoffReport != null ? handoffReport.totalHandoffs() : 0} handoffs</span>
                    <span>${handoffReport != null ? handoffReport.recipientOpenCount() : 0} recipient open days</span>
                    <span>${handoffReport != null ? handoffReport.ctaClickCount() : 0} handoff clicks</span>
                </div>
            @endif
        </div>
    </section>

    @if (!adminConfigured)
        <section class="section section--tight">
            <div class="container">
                <article class="trust-box trust-box--warning">
                    <span class="trust-box__label">Disabled</span>
                    <h2>Admin credentials are not configured</h2>
                    <p>Set APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD in the runtime environment before exposing /admin.</p>
                </article>
            </div>
        </section>
    @else
        @if (!authenticated)
            <section class="section section--tight">
                <div class="container content-grid">
                    <article class="detail-card">
                        <h2>Sign in</h2>
                        @if (error)
                            <p class="form-error">Invalid username or password.</p>
                        @endif
                        <form method="post" action="/admin/login" class="stack-form">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <label class="field">
                                <span>Username</span>
                                <input name="username" type="text" value="${username}" autocomplete="username" required>
                            </label>
                            <label class="field">
                                <span>Password</span>
                                <input name="password" type="password" autocomplete="current-password" required>
                            </label>
                            <button class="button button--primary" type="submit">Sign in</button>
                        </form>
                    </article>

                    <article class="trust-box">
                        <span class="trust-box__label">Current gate</span>
                        <h2>Configured internal credentials</h2>
                        <p>This page uses a lightweight session login because the admin scope is only lead review and export, not multi-user account management.</p>
                        <p><strong>Configured username:</strong> ${username}</p>
                    </article>
                </div>
            </section>
        @else
            <section class="section section--tight">
                <div class="container utility-grid">
                    <article class="detail-card">
                        <h2>Storage snapshot</h2>
                        <p class="detail-meta">All lead, vendor, and click logs are stored outside the build artifact and previewed here from the live storage root.</p>
                        @if (storageRoot != null && !storageRoot.isBlank())
                            <p><strong>Root:</strong> <code>${storageRoot}</code></p>
                        @endif
                        <ul class="detail-list">
                            <li>${storageFiles.size()} file(s) visible in storage</li>
                            <li>${leadCount} lead record(s) in inbox view</li>
                            <li>${handoffReport != null ? handoffReport.totalEvents() : 0} vendor workflow event(s)</li>
                        </ul>
                        <div class="hero__actions">
                            <a class="button button--secondary" href="/admin/export.json">Download JSON</a>
                            <a class="button button--secondary" href="/admin/export.csv">Download CSV</a>
                        </div>
                    </article>

                    <article class="detail-card">
                        <h2>Vendor workflow</h2>
                        <ul class="detail-list">
                            <li>${handoffReport != null ? handoffReport.totalHandoffs() : 0} briefs created</li>
                            <li>${handoffReport != null ? handoffReport.preparedSendCount() : 0} send-prep actions</li>
                            <li>${handoffReport != null ? handoffReport.markedSentCount() : 0} confirmed send day(s)</li>
                            <li>${handoffReport != null ? handoffReport.recipientOpenCount() : 0} recipient open day(s)</li>
                            <li>${handoffReport != null ? handoffReport.publicBriefDownloadedCount() : 0} public PDF download(s)</li>
                        </ul>
                        <p class="detail-meta">This stays focused on the free wedge: office-created customer briefs, not full vendor SaaS operations.</p>
                    </article>

                    <article class="detail-card">
                        <h2>Session</h2>
                        <form method="post" action="/admin/logout" class="inline-form">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <button class="button button--primary" type="submit">Sign out</button>
                        </form>
                    </article>
                </div>
            </section>

            <section class="section">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Vendor workflow</span>
                        <h2>Free handoff channel signals</h2>
                    </div>

                    <div class="utility-grid">
                        <article class="detail-card">
                            <h3>Creation and send prep</h3>
                            <ul class="detail-list">
                                <li>${handoffReport != null ? handoffReport.createdCount() : 0} handoff create event(s)</li>
                                <li>${handoffReport != null ? handoffReport.preparedSendCount() : 0} copy or draft prep action(s)</li>
                                <li>${handoffReport != null ? handoffReport.markedSentCount() : 0} confirmed send day(s)</li>
                                <li>${handoffReport != null ? handoffReport.repeatVendorUtilityRows().size() : 0} repeat vendor and utility loop(s)</li>
                            </ul>
                        </article>

                        <article class="detail-card">
                            <h3>Recipient response</h3>
                            <ul class="detail-list">
                                <li>${handoffReport != null ? handoffReport.recipientOpenCount() : 0} public brief open day(s)</li>
                                <li>${handoffReport != null ? handoffReport.publicBriefDownloadedCount() : 0} public brief PDF download(s)</li>
                                <li>${handoffReport != null ? handoffReport.ctaClickCount() : 0} CTA click(s) after open</li>
                                <li>${handoffReport != null ? handoffReport.sourceDrawerClickCount() : 0} full-rule or help follow-on click(s)</li>
                            </ul>
                        </article>

                        <article class="detail-card">
                            <h3>Internal visibility</h3>
                            <ul class="detail-list">
                                <li>${handoffReport != null ? handoffReport.officePreviewOpenedCount() : 0} office preview open day(s)</li>
                                <li>${handoffReport != null ? handoffReport.internalResultOpenedCount() : 0} internal result open day(s)</li>
                                <li>${handoffReport != null ? handoffReport.archivePacketOpenedCount() : 0} archive packet open day(s)</li>
                                <li>${handoffReport != null ? handoffReport.archivePacketDownloadedCount() : 0} archive PDF download(s)</li>
                            </ul>
                        </article>
                    </div>

                    @if (recentHandoffs.isEmpty())
                        <article class="detail-card">
                            <h3>No vendor handoffs yet</h3>
                            <p>Create the first customer brief and the office details, source page, and workflow state will appear here.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Created</th>
                                    <th>Vendor office</th>
                                    <th>Customer and site</th>
                                    <th>Utility</th>
                                    <th>Workflow</th>
                                    <th>Source</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var handoff : recentHandoffs)
                                    <tr>
                                        <td>${handoff.createdAt() != null ? handoff.createdAt().toString() : ""}</td>
                                        <td>
                                            <strong>${handoff.vendorCompanyName() != null && !handoff.vendorCompanyName().isBlank() ? handoff.vendorCompanyName() : "(no company)"}</strong>
                                            @if (handoff.vendorContactName() != null && !handoff.vendorContactName().isBlank())
                                                <div class="detail-meta">${handoff.vendorContactName()}</div>
                                            @endif
                                            @if (handoff.vendorEmail() != null && !handoff.vendorEmail().isBlank())
                                                <div class="detail-meta">${handoff.vendorEmail()}</div>
                                            @endif
                                            @if (handoff.vendorPhone() != null && !handoff.vendorPhone().isBlank())
                                                <div class="detail-meta">${handoff.vendorPhone()}</div>
                                            @endif
                                        </td>
                                        <td>
                                            <strong>${handoff.propertyLabel() != null && !handoff.propertyLabel().isBlank() ? handoff.propertyLabel() : "(no property label)"}</strong>
                                            @if (handoff.customerFirstName() != null && !handoff.customerFirstName().isBlank())
                                                <div class="detail-meta">Customer: ${handoff.customerFirstName()}</div>
                                            @endif
                                            @if (handoff.siteAddress() != null && !handoff.siteAddress().isBlank())
                                                <div class="detail-meta">${handoff.siteAddress()}</div>
                                            @endif
                                        </td>
                                        <td>
                                            ${handoff.utilityName() != null && !handoff.utilityName().isBlank() ? handoff.utilityName() : handoff.utilityId()}
                                            @if (handoff.utilityId() != null && !handoff.utilityId().isBlank())
                                                <div class="detail-meta">${handoff.utilityId()}</div>
                                            @endif
                                        </td>
                                        <td>
                                            <div>${handoff.issueLabel() != null && !handoff.issueLabel().isBlank() ? handoff.issueLabel() : handoff.issueType()}</div>
                                            <div class="detail-meta">${handoff.resultLabel() != null && !handoff.resultLabel().isBlank() ? handoff.resultLabel() : handoff.resultStatus()}</div>
                                            <div class="detail-meta">${handoff.submissionLabel() != null && !handoff.submissionLabel().isBlank() ? handoff.submissionLabel() : handoff.submissionStatus()}</div>
                                            @if (handoff.failedReason() != null && !handoff.failedReason().isBlank())
                                                <div class="detail-meta">Fail reason: ${handoff.failedReason()}</div>
                                            @endif
                                        </td>
                                        <td>
                                            ${handoff.createdFromPath()}
                                            @if (handoff.handoffId() != null && !handoff.handoffId().isBlank())
                                                <div class="detail-meta">ID: ${handoff.handoffId()}</div>
                                            @endif
                                        </td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif

                    @if (handoffReport != null && !handoffReport.repeatVendorUtilityRows().isEmpty())
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Vendor office</th>
                                    <th>Utility</th>
                                    <th>Handoffs</th>
                                    <th>Recipient opens</th>
                                    <th>Recipient downloads</th>
                                    <th>Window</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var row : handoffReport.repeatVendorUtilityRows())
                                    <tr>
                                        <td>${row.vendorCompanyName()}</td>
                                        <td>
                                            ${row.utilityName()}
                                            @if (row.utilityId() != null && !row.utilityId().isBlank())
                                                <div class="detail-meta">${row.utilityId()}</div>
                                            @endif
                                        </td>
                                        <td>${row.handoffCount()}</td>
                                        <td>${row.openedCount()}</td>
                                        <td>${row.downloadedCount()}</td>
                                        <td>
                                            ${row.firstSeen() != null ? row.firstSeen().toString() : ""}
                                            <div class="detail-meta">${row.lastSeen() != null ? row.lastSeen().toString() : ""}</div>
                                        </td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif

                    @if (handoffReport != null && !handoffReport.recentEvents().isEmpty())
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Recent vendor events</th>
                                    <th>Vendor</th>
                                    <th>Utility</th>
                                    <th>Workflow</th>
                                    <th>Origin</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var event : handoffReport.recentEvents())
                                    <tr>
                                        <td>
                                            <strong>${event.eventType()}</strong>
                                            <div class="detail-meta">${event.occurredAt() != null ? event.occurredAt().toString() : ""}</div>
                                        </td>
                                        <td>${event.vendorCompanyName()}</td>
                                        <td>${event.utilityName()}</td>
                                        <td>
                                            <div>${event.issueType()}</div>
                                            <div class="detail-meta">${event.resultStatus()}</div>
                                            <div class="detail-meta">${event.submissionStatus()}</div>
                                        </td>
                                        <td>
                                            ${event.originPath()}
                                            @if (event.requestPath() != null && !event.requestPath().isBlank())
                                                <div class="detail-meta">${event.requestPath()}</div>
                                            @endif
                                        </td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif

                    @if (handoffReport != null && !handoffReport.recentCtaClicks().isEmpty())
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Recent handoff CTA clicks</th>
                                    <th>Utility</th>
                                    <th>Source</th>
                                    <th>Destination</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var click : handoffReport.recentCtaClicks())
                                    <tr>
                                        <td>
                                            <strong>${click.ctaType()}</strong>
                                            <div class="detail-meta">${click.clickedAt() != null ? click.clickedAt().toString() : ""}</div>
                                            <div class="detail-meta">${click.pageFamily()}</div>
                                        </td>
                                        <td>${click.utilityName()}</td>
                                        <td>${click.sourcePage()}</td>
                                        <td>${click.destination()}</td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>

            <section class="section section--tight">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Raw storage</span>
                        <h2>File-backed store preview</h2>
                    </div>
                    @if (storageFiles.isEmpty())
                        <article class="detail-card">
                            <h3>No storage files yet</h3>
                            <p>The admin root exists, but there are no JSON, JSONL, or CSV files to preview yet.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>File</th>
                                    <th>Updated</th>
                                    <th>Size</th>
                                    <th>Path</th>
                                    <th>Preview</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var file : storageFiles)
                                    <tr>
                                        <td><strong>${file.name()}</strong></td>
                                        <td>${file.updatedAt()}</td>
                                        <td>${file.displaySize()}</td>
                                        <td><code>${file.absolutePath()}</code></td>
                                        <td>
                                            @if (file.preview() != null && !file.preview().isBlank())
                                                <details>
                                                    <summary>Show preview</summary>
                                                    <pre>${file.preview()}</pre>
                                                </details>
                                            @else
                                                <span class="detail-meta">No preview</span>
                                            @endif
                                        </td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>

            <section class="section section--tight">
                <div class="container utility-grid">
                    <article class="detail-card">
                        <h2>Issue buckets</h2>
                        @if (issueCounts.isEmpty())
                            <p>No issue buckets yet.</p>
                        @else
                            <ul class="detail-list">
                                @for (var entry : issueCounts.entrySet())
                                    <li>${entry.getKey()}: ${entry.getValue()}</li>
                                @endfor
                            </ul>
                        @endif
                    </article>

                    <article class="detail-card">
                        <h2>Page families</h2>
                        @if (pageFamilyCounts.isEmpty())
                            <p>No page family data yet.</p>
                        @else
                            <ul class="detail-list">
                                @for (var entry : pageFamilyCounts.entrySet())
                                    <li>${entry.getKey()}: ${entry.getValue()}</li>
                                @endfor
                            </ul>
                        @endif
                    </article>

                    <article class="detail-card">
                        <h2>Utility demand</h2>
                        @if (utilityCounts.isEmpty())
                            <p>No utility clustering yet.</p>
                        @else
                            <ul class="detail-list">
                                @for (var entry : utilityCounts.entrySet())
                                    <li>${entry.getKey()}: ${entry.getValue()}</li>
                                @endfor
                            </ul>
                        @endif
                    </article>

                    <article class="detail-card">
                        <h2>Coverage gaps</h2>
                        @if (providerCoverageGaps.isEmpty())
                            <p>Every captured lead has at least one assignable provider.</p>
                        @else
                            <ul class="detail-list">
                                @for (var entry : providerCoverageGaps.entrySet())
                                    <li>${entry.getKey()}: ${entry.getValue()} lead(s) with no assignable provider</li>
                                @endfor
                            </ul>
                        @endif
                    </article>

                    <article class="detail-card">
                        <h2>Provider inventory</h2>
                        <ul class="detail-list">
                            <li>${publicProviderCount} public provider profiles visible on the site</li>
                            <li>${sponsorOnlyProviderCount} sponsor-only providers kept off public routes</li>
                            <li>${sponsorActiveProviderCount} sponsor-only providers active for internal lead routing</li>
                            <li>${sponsorProspectProviderCount} sponsor prospects staged privately for outreach</li>
                            <li>${heldProviderCount} hold providers waiting on verification or commercial approval</li>
                        </ul>
                        <p class="detail-meta">Only PUBLIC inventory appears on public routes. The inbox can route to PUBLIC and only ACTIVE sponsor-only providers. PROSPECT and HOLD rows stay private until the commercial state changes.</p>
                    </article>

                    <article class="detail-card">
                        <h2>Email delivery</h2>
                        <ul class="detail-list">
                            <li>${deliveredLeadCount} delivery email(s) sent</li>
                            <li>${queuedDeliveryCount} delivery email(s) queued for manual send or SMTP setup</li>
                        </ul>
                        <p class="detail-meta">Lead delivery is created only when an ACTIVE sponsor-only provider with an email address covers a verified utility context and the form consent covers sponsor routing.</p>
                    </article>

                    <article class="detail-card">
                        <h2>Sponsor prospect metros</h2>
                        @if (sponsorProspectMetroCounts.isEmpty())
                            <p>No private sponsor prospect inventory is staged yet.</p>
                        @else
                            <ul class="detail-list">
                                @for (var entry : sponsorProspectMetroCounts.entrySet())
                                    <li>${entry.getKey()}: ${entry.getValue()} private prospect provider(s)</li>
                                @endfor
                            </ul>
                        @endif
                        <p class="detail-meta">These prospect rows never appear on public pages and do not enter the assignment queue until sponsor status moves to ACTIVE.</p>
                    </article>
                </div>
            </section>

            <section class="section">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Commercial inventory</span>
                        <h2>Private sponsor-only provider rows</h2>
                    </div>
                    @if (privateSponsorInventory.isEmpty())
                        <article class="detail-card">
                            <h3>No private sponsor inventory yet</h3>
                            <p>Seed sponsor-only rows first, then activate the ones that should enter lead routing.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Provider</th>
                                    <th>Status</th>
                                    <th>Coverage</th>
                                    <th>Contact</th>
                                    <th>Commercial note</th>
                                    <th>Action</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var provider : privateSponsorInventory)
                                    <tr>
                                        <td>
                                            <strong>${provider.providerName()}</strong>
                                            <div class="detail-meta">${provider.providerId()}</div>
                                        </td>
                                        <td>
                                            <strong>${provider.isSponsorActiveListing() ? "ACTIVE" : "PROSPECT"}</strong>
                                            <div class="detail-meta">${provider.coverageTargets().size()} utility targets</div>
                                        </td>
                                        <td>${String.join(", ", provider.coverageTargets())}</td>
                                        <td>
                                            @if (provider.phone() != null && !provider.phone().isBlank())
                                                <div>${provider.phone()}</div>
                                            @endif
                                            @if (provider.email() != null && !provider.email().isBlank())
                                                <div class="detail-meta">${provider.email()}</div>
                                            @endif
                                            @if (provider.siteUrl() != null && !provider.siteUrl().isBlank())
                                                <div><a href="${provider.siteUrl()}">Website</a></div>
                                            @endif
                                        </td>
                                        <td>${provider.pageLabel()}</td>
                                        <td>
                                            <form method="post" action="/admin/providers/${provider.providerId()}/sponsor-status" class="stack-form">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="sponsorStatus" value="${provider.isSponsorActiveListing() ? "PROSPECT" : "ACTIVE"}">
                                                <label class="field">
                                                    <span>Internal note</span>
                                                    <input name="note" type="text" maxlength="160" value="${provider.isSponsorActiveListing() ? "Move back to prospect" : "Activate for lead routing"}">
                                                </label>
                                                <button class="button button--secondary" type="submit">${provider.isSponsorActiveListing() ? "Move to prospect" : "Activate sponsor"}</button>
                                            </form>
                                        </td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>

            <section class="section section--tight">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Provider requests</span>
                        <h2>Manual provider inbox</h2>
                    </div>
                    @if (providerClaims.isEmpty())
                        <article class="detail-card">
                            <h3>No provider requests yet</h3>
                            <p>Claim, pricing, and listing requests from /claim-listing will appear here for manual follow-up.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Submitted</th>
                                    <th>Contact</th>
                                    <th>Company</th>
                                    <th>Request</th>
                                    <th>Service area</th>
                                    <th>Listing reference</th>
                                    <th>Notes</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var claim : providerClaims)
                                    <tr>
                                        <td>${claim.submittedAt() != null ? claim.submittedAt().toString() : ""}</td>
                                        <td>
                                            <strong>${claim.fullName()}</strong>
                                            <div class="detail-meta">${claim.email()}</div>
                                            @if (claim.phone() != null && !claim.phone().isBlank())
                                                <div class="detail-meta">${claim.phone()}</div>
                                            @endif
                                        </td>
                                        <td>
                                            ${claim.companyName()}
                                            @if (claim.website() != null && !claim.website().isBlank())
                                                <div class="detail-meta"><a href="${claim.website()}">${claim.website()}</a></div>
                                            @endif
                                        </td>
                                        <td>${claim.displayRequestType()}</td>
                                        <td>${claim.serviceArea()}</td>
                                        <td>${claim.listingReference()}</td>
                                        <td>
                                            ${claim.notes()}
                                            @if (claim.referrer() != null && !claim.referrer().isBlank())
                                                <div class="detail-meta">${claim.referrer()}</div>
                                            @endif
                                        </td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>

            <section class="section section--tight">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Recent deliveries</span>
                        <h2>Latest sponsor email delivery records</h2>
                    </div>
                    @if (recentDeliveries.isEmpty())
                        <article class="detail-card">
                            <h3>No delivery records yet</h3>
                            <p>Activate a sponsor and capture a lead to create the first queued or sent delivery record.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Created</th>
                                    <th>Status</th>
                                    <th>Provider</th>
                                    <th>Utility</th>
                                    <th>Channel</th>
                                    <th>Note</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var delivery : recentDeliveries)
                                    <tr>
                                        <td>${delivery.createdAt() != null ? delivery.createdAt().toString() : ""}</td>
                                        <td>${delivery.status()}</td>
                                        <td>${delivery.providerName()}<div class="detail-meta">${delivery.providerEmail()}</div></td>
                                        <td>${delivery.utilityId()}</td>
                                        <td>${delivery.channel()}</td>
                                        <td>${delivery.note()}</td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>

            <section class="section">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Inbox</span>
                        <h2>Most recent requests first</h2>
                    </div>
                    @if (inbox.isEmpty())
                        <article class="detail-card">
                            <h3>No leads captured yet</h3>
                            <p>Use the public lead form to capture the first request, then refresh this page.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Captured</th>
                                    <th>Name</th>
                                    <th>Phone</th>
                                    <th>Utility</th>
                                    <th>Issue</th>
                                    <th>Property</th>
                                    <th>Page family</th>
                                    <th>City</th>
                                    <th>Coverage</th>
                                    <th>Assignment</th>
                                    <th>Source</th>
                                    <th>Notes</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var item : inbox)
                                    <tr>
                                        <td>${item.lead().capturedAt().toString()}</td>
                                        <td>${item.lead().fullName()}<br><span class="detail-meta">${item.lead().email()}</span></td>
                                        <td>${item.lead().phone()}</td>
                                        <td>
                                            ${item.lead().displayUtility()}
                                            @if (!item.lead().hasTrustedUtility() && item.lead().submittedUtilityId() != null && !item.lead().submittedUtilityId().isBlank())
                                                <div class="detail-meta">Submitted utility only: ${item.lead().submittedUtilityId()}</div>
                                            @endif
                                        </td>
                                        <td>${item.lead().displayIssueType()}</td>
                                        <td>${item.lead().displayPropertyType()}</td>
                                        <td>${item.lead().displayPageFamily()}</td>
                                        <td>${item.lead().city()}</td>
                                        <td>
                                            @if (!item.lead().autoRouteEligible())
                                                <div class="detail-meta">${item.lead().displayRoutingStatus()}</div>
                                                <div class="detail-meta">${item.lead().routingReason()}</div>
                                            @endif
                                            @if (item.availableProviders().isEmpty())
                                                <span class="detail-meta">No assignable providers</span>
                                            @else
                                                ${item.availableProviders().size()} assignable
                                                <div class="detail-meta">${item.publicProviderCount()} public</div>
                                                <div class="detail-meta">${item.sponsorOnlyProviderCount()} sponsor-only</div>
                                            @endif
                                        </td>
                                        <td>
                                            @if (item.assignment() != null)
                                                <strong>${item.assignment().providerName()}</strong>
                                                <div class="detail-meta">${item.assignment().assignedAt().toString()}</div>
                                                @if (item.assignment().note() != null && !item.assignment().note().isBlank())
                                                    <div class="detail-meta">${item.assignment().note()}</div>
                                                @endif
                                            @endif
                                            @if (!item.availableProviders().isEmpty())
                                                <form method="post" action="/admin/leads/${item.lead().leadId()}/assign" class="stack-form">
                                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                                    <label class="field">
                                                        <span>Provider</span>
                                                        <select name="providerId" required>
                                                            @for (var provider : item.availableProviders())
                                                                @if (item.assignment() != null && provider.providerId().equals(item.assignment().providerId()))
                                                                    <option value="${provider.providerId()}" selected>${provider.providerName()}${provider.isSponsorOnlyListing() ? " (Sponsor-only)" : (provider.isSponsored() ? " [Featured]" : "")}</option>
                                                                @else
                                                                    <option value="${provider.providerId()}">${provider.providerName()}${provider.isSponsorOnlyListing() ? " (Sponsor-only)" : (provider.isSponsored() ? " [Featured]" : "")}</option>
                                                                @endif
                                                            @endfor
                                                        </select>
                                                    </label>
                                                    <label class="field">
                                                        <span>Internal note</span>
                                                        <input name="note" type="text" value="${item.assignment() != null ? item.assignment().note() : ""}" maxlength="160">
                                                    </label>
                                                    <button class="button button--secondary" type="submit">${item.assignment() != null ? "Reassign" : "Assign"}</button>
                                                </form>
                                            @endif
                                        </td>
                                        <td>
                                            ${item.lead().displaySourcePage()}
                                            @if (item.lead().sourcePage() != null && !item.lead().sourcePage().isBlank() && item.lead().submittedSourcePage() != null && !item.lead().submittedSourcePage().isBlank() && !item.lead().sourcePage().equals(item.lead().submittedSourcePage()))
                                                <div class="detail-meta">Submitted: ${item.lead().submittedSourcePage()}</div>
                                            @endif
                                            @if (item.lead().referrer() != null && !item.lead().referrer().isBlank())
                                                <div class="detail-meta">${item.lead().referrer()}</div>
                                            @endif
                                        </td>
                                        <td>${item.lead().notes()}</td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>
        @endif
    @endif
`)
