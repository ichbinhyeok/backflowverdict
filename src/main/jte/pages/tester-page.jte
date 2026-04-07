@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.data.model.ProviderRecord
@import owner.backflow.service.LeadRoutingService
@param PageMeta page
@param UtilityRecord utility
@param java.util.List<ProviderRecord> providers
@param boolean official
@param int activeSponsorCount = 0

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">${official ? "Official authority list" : "Non-official directory"}</span>
                <h1>${official ? utility.utilityName() + " approved testers" : utility.utilityName() + " find a tester"}</h1>
                <p class="lede">
                    @if (official)
                        This page mirrors cached entries from the governing authority's published tester list. Any paid placement stays clearly separate from the official list.
                    @else
                        This is a provider discovery page, not an authority list. Use the official utility page for the compliance rule and reporting workflow first.
                    @endif
                </p>
                <div class="hero__actions">
                    <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Back to utility requirements</a>
                    @if (official)
                        <a class="button button--primary" href="${utility.approvedTesterListUrl()}">${utility.officialListLabel()}</a>
                    @endif
                    <a class="button button--secondary" href="${utility.utilityUrl()}">Official program page</a>
                    <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/" + (official ? "approved-testers" : "find-a-tester"), "tester-search", "tester-directory")}">Request local help</a>
                </div>
            </div>
            <aside class="detail-card">
                <span class="stat-card__label">Route context</span>
                <div class="metadata-grid">
                    <div class="metadata-block">
                        <strong>Utility</strong>
                        <span>${utility.utilityName()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Last verified</strong>
                        <span>${utility.lastVerified().toString()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Freshness window</strong>
                        <span>${utility.staleAfterDays()} days</span>
                    </div>
                </div>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container page-frame">
            <div class="stack">
                @if (official)
                    <article class="table-card">
                        <div class="section-heading">
                            <span class="eyebrow">Official list entries</span>
                            <h2>Authority-first tester table</h2>
                            <p>Use the governing authority list first. Provider profiles and sponsor routing remain secondary to the official approval context.</p>
                        </div>
                        @if (!providers.isEmpty())
                            <div class="table-wrap">
                                <table class="data-table data-table--responsive">
                                    <thead>
                                    <tr>
                                        <th>Full name</th>
                                        <th>License notes</th>
                                        <th>Company or label</th>
                                        <th>Review date</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    @for (var provider : providers)
                                        <tr>
                                            <td data-label="Full name">
                                                <strong>${provider.providerName()}</strong>
                                                <div class="detail-meta">Official list entry</div>
                                            </td>
                                            <td data-label="License notes">${provider.licenseOrCertificationNotes()}</td>
                                            <td data-label="Company or label">${provider.pageLabel()}</td>
                                            <td data-label="Review date">${provider.lastReviewed().toString()}</td>
                                        </tr>
                                    @endfor
                                    </tbody>
                                </table>
                            </div>
                        @else
                            <div class="empty-state">
                                <strong>No local official entries are cached yet</strong>
                                <p>Use the utility's official program page to confirm the latest authority list directly while BackflowPath expands this page.</p>
                            </div>
                        @endif
                    </article>
                @else
                    <article class="detail-card">
                        <div class="section-heading">
                            <span class="eyebrow">Provider inventory</span>
                            <h2>Directory entries mapped to this utility</h2>
                            <p>No official approved tester list is published here in BackflowPath, so this page only presents clearly labeled provider options.</p>
                        </div>
                        @if (!providers.isEmpty())
                            <div class="card-grid">
                                @for (var provider : providers)
                                    <article class="panel">
                                        <span class="pill">${provider.isSponsored() ? "Featured directory" : "Directory"}</span>
                                        <h3><a href="/providers/${provider.providerId()}/">${provider.providerName()}</a></h3>
                                        <p>${provider.pageLabel()}</p>
                                        <ul class="detail-list">
                                            @if (provider.licenseOrCertificationNotes() != null && !provider.licenseOrCertificationNotes().isBlank())
                                                <li>${provider.licenseOrCertificationNotes()}</li>
                                            @endif
                                            @if (provider.phone() != null && !provider.phone().isBlank())
                                                <li>${provider.phone()}</li>
                                            @endif
                                            @if (provider.email() != null && !provider.email().isBlank())
                                                <li>${provider.email()}</li>
                                            @endif
                                        </ul>
                                        <div class="card-actions">
                                            <a class="button button--secondary" href="/providers/${provider.providerId()}/">Open provider profile</a>
                                            @if (provider.siteUrl() != null && !provider.siteUrl().isBlank())
                                                <a class="button button--secondary" href="/r/cta?next=${provider.siteUrl()}&pageFamily=tester-directory&utilityId=${utility.utilityId()}&providerId=${provider.providerId()}&ctaType=provider-website&source=/utilities/${utility.state()}/${utility.canonicalSlug()}/${official ? "approved-testers" : "find-a-tester"}">Open provider website</a>
                                            @endif
                                        </div>
                                    </article>
                                @endfor
                            </div>
                        @else
                            <div class="empty-state">BackflowPath does not yet show mapped provider options for this utility. Use the official program page while this route is expanded.</div>
                        @endif
                    </article>
                @endif

                <article class="panel panel--dark">
                    <div class="section-heading">
                        <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Utility workflow</span>
                        <h2 style="color: #ffffff;">Stay on the official compliance path</h2>
                        <p>The utility page remains canonical. This tester surface should only help the user take the next step after the exact reporting and deadline path is clear.</p>
                    </div>
                    <ol class="step-list">
                        @for (var step : utility.workflowSteps())
                            <li>${step}</li>
                        @endfor
                    </ol>
                </article>
            </div>

            <aside class="side-stack sticky-rail">
                @if (official)
                    <article class="trust-box">
                        <span class="trust-box__label">Official first</span>
                        <h2>Authority list context</h2>
                        <p>Always confirm tester status on the governing utility list before treating a provider as approved. Paid placements are never blended into this public list.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="${utility.approvedTesterListUrl()}">${utility.officialListLabel()}</a>
                        </div>
                    </article>
                @else
                    <article class="trust-box trust-box--warning">
                        <span class="trust-box__label">Non-official</span>
                        <h2>Directory page</h2>
                        <p>This route is intentionally labeled as discovery, not official approval. Use the utility page for deadlines, penalties, and required submission mechanics.</p>
                    </article>
                @endif

                @if (!providers.isEmpty())
                    <article class="panel panel--dashed">
                        <div class="section-heading">
                            <span class="eyebrow">Commercial layer</span>
                            <h2>Featured providers</h2>
                        </div>
                        @for (var provider : providers)
                            @if (providers.indexOf(provider) < 2)
                                <div class="mini-provider" style="margin-top: 0.9rem;">
                                    <img src="${providers.indexOf(provider) == 0 ? "/images/design/approved-featured.jpg" : "/images/design/utility-provider-1.jpg"}" alt="Backflow provider featured image">
                                    <div>
                                        <strong>${provider.providerName()}</strong>
                                        <span>${provider.pageLabel()}</span>
                                    </div>
                                </div>
                            @endif
                        @endfor
                        <p class="detail-meta">If you need help beyond the list, you can still submit a request for local follow-up.</p>
                    </article>
                @endif

                <article class="detail-card">
                    <span class="eyebrow">Utility notes</span>
                    <h2>Local compliance context</h2>
                    <ul class="detail-list">
                        <li>${utility.testingFrequency()}</li>
                        <li>${utility.dueBasis()}</li>
                        <li>${utility.penalties()}</li>
                    </ul>
                </article>

                <article class="detail-card">
                    <div class="visual-card">
                        <img src="/images/design/approved-map.jpg" alt="Service territory map visualization">
                        <div class="visual-card__overlay">
                            <strong>Service territory</strong>
                            <p>Use this page to compare nearby providers only after the utility rule and reporting path are clear.</p>
                        </div>
                    </div>
                </article>
            </aside>
        </div>
    </section>
`)
