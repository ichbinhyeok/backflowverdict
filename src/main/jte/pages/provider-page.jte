@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.ProviderRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param ProviderRecord provider
@param java.util.List<UtilityRecord> utilities
@param java.util.List<MetroRecord> metros = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()
@param UtilityRecord primaryUtility = null
@param java.util.List<String> coverageStates = java.util.List.of()
@param java.util.List<String> coverageCities = java.util.List.of()
@param java.util.List<String> coverageCounties = java.util.List.of()
@param java.util.List<String> providerServiceTypes = java.util.List.of()
@param int providerOfficialRouteCount = 0
@param int providerDirectoryRouteCount = 0
@param int providerSubmissionWorkflowCount = 0
@param java.time.LocalDate latestUtilityVerification = null

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container content-grid">
            <article class="detail-card">
                <img src="/images/design/provider-logo.jpg" alt="Provider identity illustration" style="width: 7rem; height: 7rem; border-radius: 1rem; object-fit: cover; margin-bottom: 1rem;">
                <span class="eyebrow kicker-rule">${provider.isSponsored() ? "Sponsored profile" : "Public provider profile"}</span>
                <h1>${provider.providerName()}</h1>
                <p class="lede">${provider.pageLabel()}</p>
                <div class="hero__stats">
                    <span>${utilities.size()} mapped utility pages</span>
                    <span>${metros.size()} metro support page(s)</span>
                    <span>Last reviewed ${provider.lastReviewed().toString()}</span>
                </div>
                <div class="metadata-grid" style="margin-top: 1rem;">
                    @if (provider.phone() != null && !provider.phone().isBlank())
                        <div class="metadata-block">
                            <strong>Direct line</strong>
                            <span>${provider.phone()}</span>
                        </div>
                    @endif
                    @if (provider.email() != null && !provider.email().isBlank())
                        <div class="metadata-block">
                            <strong>Email</strong>
                            <span>${provider.email()}</span>
                        </div>
                    @endif
                </div>
                <div class="hero__actions">
                    @if (provider.siteUrl() != null && !provider.siteUrl().isBlank())
                        <a class="button button--primary" href="/r/cta?next=${provider.siteUrl()}&pageFamily=provider-profile&providerId=${provider.providerId()}&ctaType=provider-website&source=/providers/${provider.providerId()}/">Open website</a>
                    @endif
                    @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                        <a class="button button--secondary" href="/r/cta?next=${provider.officialApprovalSourceUrl()}&pageFamily=provider-profile&providerId=${provider.providerId()}&ctaType=authority-source&source=/providers/${provider.providerId()}/">Authority source</a>
                    @endif
                    <a class="button button--secondary" href="/claim-listing?requestType=claim-existing-profile">Claim or update this listing</a>
                </div>
            </article>

            <article class="panel panel--hero">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Mission statement</span>
                    <h2 style="color: #ffffff;">Provider narrative and compliance posture</h2>
                </div>
                <p>${provider.licenseOrCertificationNotes()}</p>
                <p>This profile does not override the utility's official rules. It exists to make the local provider layer inspectable and easier to route to.</p>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container content-grid">
            <article class="detail-card">
                <div class="visual-card">
                    <img src="/images/design/provider-map.jpg" alt="Regional coverage map">
                    <div class="visual-card__overlay">
                        <strong>Jurisdictional coverage</strong>
                        <p>${metros.isEmpty() ? "Utility-level coverage only." : metros.size() + " mapped metro support page(s)."}</p>
                    </div>
                </div>
            </article>

            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">Coverage</span>
                    <h2>Utility coverage</h2>
                    <p>${utilities.size()} mapped utility pages</p>
                </div>
                <div class="stat-grid">
                    <div class="metadata-block">
                        <strong>Utility count</strong>
                        <span>${utilities.size()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Metro count</strong>
                        <span>${metros.size()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Guide links</strong>
                        <span>${relatedGuides.size()}</span>
                    </div>
                </div>
                <ul class="detail-list">
                    <li>${provider.licenseOrCertificationNotes()}</li>
                    @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                        <li><a href="${provider.officialApprovalSourceUrl()}">Authority source</a></li>
                    @endif
                    <li>Last reviewed ${provider.lastReviewed().toString()}</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Entity signals</span>
                <h2>Contact and public web signals</h2>
                <ul class="detail-list">
                    @if (provider.phone() != null && !provider.phone().isBlank())
                        <li><strong>Phone:</strong> ${provider.phone()}</li>
                    @endif
                    @if (provider.email() != null && !provider.email().isBlank())
                        <li><strong>Email:</strong> ${provider.email()}</li>
                    @endif
                    @if (provider.siteUrl() != null && !provider.siteUrl().isBlank())
                        <li><strong>Website:</strong> <a href="${provider.siteUrl()}">${provider.siteUrl()}</a></li>
                    @else
                        <li><strong>Website:</strong> No public website is stored for this provider profile yet.</li>
                    @endif
                    @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                        <li><strong>Authority source:</strong> <a href="${provider.officialApprovalSourceUrl()}">${provider.officialApprovalSourceUrl()}</a></li>
                    @endif
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Service area</span>
                <h2>Area served across mapped utility pages</h2>
                <div class="stat-grid">
                    <div class="metadata-block">
                        <strong>States</strong>
                        <span>${coverageStates.isEmpty() ? "Not mapped" : String.join(", ", coverageStates)}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Cities</strong>
                        <span>${coverageCities.isEmpty() ? "Not mapped" : String.join(", ", coverageCities)}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Counties</strong>
                        <span>${coverageCounties.isEmpty() ? "Not mapped" : String.join(", ", coverageCounties)}</span>
                    </div>
                </div>
                @if (!providerServiceTypes.isEmpty())
                    <ul class="detail-list">
                        @for (var serviceType : providerServiceTypes)
                            <li>${serviceType}</li>
                        @endfor
                    </ul>
                @endif
                <h3>Verification basis</h3>
                <ul class="detail-list">
                    <li>${utilities.size()} mapped utility pages</li>
                    <li>${providerOfficialRouteCount} mapped utilities with an official tester route</li>
                    <li>${providerDirectoryRouteCount} mapped utilities using a directory route</li>
                    <li>${providerSubmissionWorkflowCount} mapped utilities with structured submission steps</li>
                    @if (latestUtilityVerification != null)
                        <li>Latest mapped utility verification: ${latestUtilityVerification.toString()}</li>
                    @endif
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Covered utilities</span>
                <h2>Utility pages this provider is mapped to</h2>
            </div>
            <div class="card-grid">
                @for (var utility : utilities)
                    <article class="detail-card">
                        <span class="pill">${utility.state()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility page</a>
                            @if (utility.supportsApprovedTestersPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/approved-testers">Official tester route</a>
                            @elseif (utility.supportsFindATesterPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/find-a-tester">Directory route</a>
                            @endif
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Metro reach</span>
                <h2>Metro support pages connected to this provider</h2>
                @if (metros.isEmpty())
                    <p>No metro support page is mapped to this provider yet.</p>
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

            <article class="detail-card">
                <span class="eyebrow">Support guides</span>
                <h2>Guides that match this provider's utility mix</h2>
                @if (relatedGuides.isEmpty())
                    <p>No guide set is mapped to this provider yet.</p>
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
        </div>
    </section>

    @if (primaryUtility != null)
        <section class="section section--tight">
            <div class="container">
                <article class="trust-box">
                    <span class="trust-box__label">Primary utility path</span>
                    <h2>Start from the utility before you act</h2>
                    <p>This provider is most tightly associated with <a href="/utilities/${primaryUtility.state()}/${primaryUtility.canonicalSlug()}/">${primaryUtility.utilityName()}</a>. Use that page to confirm the exact local rule before booking or routing a lead.</p>
                    <div class="card-actions">
                        <a class="button button--secondary" href="/utilities/${primaryUtility.state()}/${primaryUtility.canonicalSlug()}/">Open primary utility page</a>
                    </div>
                </article>
            </div>
        </section>
    @endif

    <section class="section section--tight">
        <div class="container">
            <article class="detail-card">
                <span class="eyebrow">Provider updates</span>
                <h2>Own this company?</h2>
                <p>If this is your company, you can claim the profile, request corrections, or ask about featured placement. Listing claims are reviewed manually before anything is changed.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/claim-listing?requestType=claim-existing-profile">Claim this listing</a>
                    <a class="button button--secondary" href="/pricing">Pricing</a>
                </div>
            </article>
        </div>
    </section>
`)
