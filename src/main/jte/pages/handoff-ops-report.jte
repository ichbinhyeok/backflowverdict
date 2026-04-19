@import owner.backflow.web.PageMeta
@import owner.backflow.service.HandoffChannelReportService.HandoffChannelReport
@param PageMeta page
@param HandoffChannelReport report

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Internal ops</span>
                <h1>Vendor-channel report</h1>
                <p class="lede">Track repeat vendor usage, prep signals, manual send confirmations, open activity, and follow-on clicks from the handoff store without widening the product.</p>
                <div class="hero__stats">
                    <span>${report.totalHandoffs()} handoffs</span>
                    <span>${report.createdCount()} created</span>
                    <span>${report.preparedSendCount()} prep signals</span>
                    <span>manual send days ${report.markedSentCount()}</span>
                    <span>${report.recipientOpenCount()} recipient open days</span>
                    <span>${report.ctaClickCount()} tracked follow-on clicks</span>
                    <span>${report.repeatVendorUtilityRows().size()} repeat vendor/utilities</span>
                </div>
            </div>
            <div class="side-stack">
                <article class="panel panel--status">
                    <span class="stat-card__label">Recipient loop</span>
                    <div class="stat-card__value">${report.publicBriefOpenedCount()} public open days</div>
                    <p class="stat-card__meta">${report.preparedSendCount()} prep signals and ${report.markedSentCount()} confirmed-send day(s) before that deduped open activity.</p>
                </article>
                <article class="detail-card">
                    <span class="stat-card__label">Office preview</span>
                    <div class="stat-card__value" style="font-size: 2rem;">${report.officePreviewOpenedCount()} preview opens</div>
                    <p class="stat-card__meta">${report.internalResultOpenedCount()} internal result opens and ${report.archivePacketOpenedCount()} office-record opens sit behind it.</p>
                </article>
                <article class="detail-card">
                    <span class="stat-card__label">Downloads</span>
                    <div class="stat-card__value" style="font-size: 2rem;">${report.downloadedCount()} total</div>
                    <p class="stat-card__meta">${report.publicBriefDownloadedCount()} recipient PDF downloads, ${report.officePreviewPdfDownloadedCount()} office preview PDFs, and ${report.archivePacketDownloadedCount()} office-record PDFs.</p>
                </article>
                <article class="detail-card">
                    <span class="stat-card__label">Source drawer</span>
                    <div class="stat-card__value" style="font-size: 2rem;">${report.sourceDrawerClickCount()} clicks</div>
                    <p class="stat-card__meta">Full local rule, official program, and help-request clicks after the handoff is opened.</p>
                </article>
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container utility-grid">
            <article class="detail-card">
                <h2>Event summary</h2>
                @if (report.eventTypeCounts().isEmpty())
                    <p>No handoff events yet.</p>
                @else
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Event</th>
                                <th>Count</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var item : report.eventTypeCounts())
                                <tr>
                                    <td><strong>${item.eventType()}</strong></td>
                                    <td>${item.count()}</td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </article>

            <article class="detail-card">
                <h2>Follow-on CTA clicks</h2>
                @if (report.ctaTypeCounts().isEmpty())
                    <p>No tracked follow-on clicks yet.</p>
                @else
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>CTA type</th>
                                <th>Count</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var item : report.ctaTypeCounts())
                                <tr>
                                    <td><strong>${item.ctaType()}</strong></td>
                                    <td>${item.count()}</td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </article>

            <article class="detail-card">
                <h2>Send feedback</h2>
                @if (report.sendFeedbackCounts().isEmpty())
                    <p>No send feedback captured yet.</p>
                @else
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Feedback</th>
                                <th>Count</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var item : report.sendFeedbackCounts())
                                <tr>
                                    <td><strong>${item.label()}</strong></td>
                                    <td>${item.count()}</td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container utility-grid">
            <article class="detail-card">
                <h2>Repeat vendor usage</h2>
                @if (report.repeatVendorUtilityRows().isEmpty())
                    <p>No repeat vendor and utility pair yet.</p>
                @else
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Vendor</th>
                                <th>Utility</th>
                                <th>Handoffs</th>
                                <th>Recipient opens</th>
                                <th>Recipient PDFs</th>
                                <th>First seen</th>
                                <th>Last seen</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var item : report.repeatVendorUtilityRows())
                                <tr>
                                    <td>
                                        <strong>${item.vendorCompanyName()}</strong>
                                        <div class="detail-meta">${item.utilityId()}</div>
                                    </td>
                                    <td>${item.utilityName()}</td>
                                    <td>${item.handoffCount()}</td>
                                    <td>${item.openedCount()}</td>
                                    <td>${item.downloadedCount()}</td>
                                    <td>${item.firstSeen() != null ? item.firstSeen().toString() : ""}</td>
                                    <td>${item.lastSeen() != null ? item.lastSeen().toString() : ""}</td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container utility-grid">
            <article class="detail-card">
                <h2>Recent events</h2>
                @if (report.recentEvents().isEmpty())
                    <p>No recent events captured yet.</p>
                @else
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>When</th>
                                <th>Event</th>
                                <th>Vendor</th>
                                <th>Utility</th>
                                <th>Issue</th>
                                <th>Result</th>
                                <th>Submission</th>
                                <th>Source</th>
                                <th>Request</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var item : report.recentEvents())
                                <tr>
                                    <td>${item.occurredAt() != null ? item.occurredAt().toString() : ""}</td>
                                    <td><strong>${item.eventType()}</strong></td>
                                    <td>${item.vendorCompanyName()}</td>
                                    <td>${item.utilityName()}</td>
                                    <td>${item.issueType()}</td>
                                    <td>${item.resultStatus()}</td>
                                    <td>${item.submissionStatus()}</td>
                                    <td>${item.originPath()}</td>
                                    <td>${item.requestPath()}</td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </article>

            <article class="detail-card">
                <h2>Recent CTA clicks</h2>
                @if (report.recentCtaClicks().isEmpty())
                    <p>No tracked CTA clicks yet.</p>
                @else
                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>When</th>
                                <th>Page family</th>
                                <th>CTA type</th>
                                <th>Utility</th>
                                <th>Source</th>
                                <th>Destination</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var item : report.recentCtaClicks())
                                <tr>
                                    <td>${item.clickedAt() != null ? item.clickedAt().toString() : ""}</td>
                                    <td><strong>${item.pageFamily()}</strong></td>
                                    <td>${item.ctaType()}</td>
                                    <td>${item.utilityName()}</td>
                                    <td>${item.sourcePage()}</td>
                                    <td>${item.destination()}</td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </article>
        </div>
    </section>
`)
