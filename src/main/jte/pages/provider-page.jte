@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.ProviderRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.web.CtaPaths
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
@param java.util.Map<String, String> noticeIdentifierHints = java.util.Map.of()
@param java.util.Map<String, String> reportAcceptanceHints = java.util.Map.of()
@param java.util.Map<String, String> portalNamesByUtility = java.util.Map.of()
@param java.util.Map<String, String> portalHubPathsByUtility = java.util.Map.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container content-grid">
            <article class="detail-card">
                <img src="/images/design/provider-logo.jpg" alt="Provider identity illustration" style="width: 7rem; height: 7rem; border-radius: 1rem; object-fit: cover; margin-bottom: 1rem;">
                <span class="eyebrow kicker-rule">Public provider profile</span>
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
                    @if (provider.phone() != null && !provider.phone().isBlank())
                        <a class="button button--primary" href="tel:${provider.phone().replace(" ", "")}">Call provider</a>
                    @endif
                    @if (provider.siteUrl() != null && !provider.siteUrl().isBlank())
                        @if (provider.phone() != null && !provider.phone().isBlank())
                            <a class="button button--secondary" href="${CtaPaths.trackedPath(provider.siteUrl(), "provider-profile", "", provider.providerId(), "provider-website", "/providers/" + provider.providerId() + "/")}">Open website</a>
                        @else
                            <a class="button button--primary" href="${CtaPaths.trackedPath(provider.siteUrl(), "provider-profile", "", provider.providerId(), "provider-website", "/providers/" + provider.providerId() + "/")}">Open website</a>
                        @endif
                    @endif
                    @if ((provider.phone() == null || provider.phone().isBlank()) && (provider.siteUrl() == null || provider.siteUrl().isBlank()) && primaryUtility != null)
                        <a class="button button--primary" href="/utilities/${primaryUtility.state()}/${primaryUtility.canonicalSlug()}/">Open utility workflow</a>
                    @endif
                    @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                        <a class="button button--secondary" href="${CtaPaths.trackedPath(provider.officialApprovalSourceUrl(), "provider-profile", "", provider.providerId(), "authority-source", "/providers/" + provider.providerId() + "/")}">Authority source</a>
                    @endif
                    <a class="button button--secondary" href="${page.requestHelpPathOrDefault()}">Request local help</a>
                    <a class="button button--secondary" href="/contact">Report a profile issue</a>
                </div>
            </article>

            <article class="panel panel--hero">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Mission statement</span>
                    <h2 style="color: #ffffff;">Provider narrative and compliance posture</h2>
                </div>
                <p>${provider.licenseOrCertificationNotes()}</p>
                <p>This profile does not override the utility's official rules. It exists to make the public provider layer inspectable after the governing workflow is clear.</p>
            </article>
        </div>
    </section>

    @if (primaryUtility != null)
        <section class="section section--tight">
            <div class="container">
                <article class="detail-card detail-card--key">
                    <span class="eyebrow">Before booking this provider</span>
                    <h2>Match ${provider.providerName()} to the ${primaryUtility.utilityName()} workflow</h2>
                    <p>The strongest local provider pages do not stop at a phone number. Confirm the authority source, the utility's tester gate, the report route, and the proof trail before you schedule or send a failed device back for retest.</p>
                    <div class="metadata-grid">
                        <div class="metadata-block">
                            <strong>Official source</strong>
                            <span>${provider.pageLabel()}</span>
                        </div>
                        <div class="metadata-block">
                            <strong>Due trigger</strong>
                            @if (primaryUtility.deadlinePolicy().reportDueDaysAfterTest() != null)
                                <span>${primaryUtility.deadlinePolicy().reportDueLabel()}</span>
                            @else
                                <span>${primaryUtility.testingFrequency()} - ${primaryUtility.dueBasis()}</span>
                            @endif
                        </div>
                        <div class="metadata-block">
                            <strong>Notice clue</strong>
                            <span>${noticeIdentifierHints.getOrDefault(primaryUtility.utilityId(), "Match the utility name, address, device, and due date on the notice.")}</span>
                        </div>
                        <div class="metadata-block">
                            <strong>Report proof</strong>
                            <span>${reportAcceptanceHints.getOrDefault(primaryUtility.utilityId(), "Keep proof that the accepted report was filed with the governing utility.")}</span>
                        </div>
                        <div class="metadata-block">
                            <strong>Submission route</strong>
                            @if (primaryUtility.reportWorkflow().hasContent())
                                <span>${primaryUtility.reportWorkflow().portalName() != null && !primaryUtility.reportWorkflow().portalName().isBlank() ? primaryUtility.reportWorkflow().portalName() : "Utility reporting workflow"}</span>
                            @else
                                <span>${primaryUtility.submissionMethods().isEmpty() ? "Confirm the filing path before booking." : primaryUtility.submissionMethods().get(0).label()}</span>
                            @endif
                        </div>
                        @if (primaryUtility.testerGate().hasContent())
                            <div class="metadata-block">
                                <strong>Credential gate</strong>
                                <span>${primaryUtility.testerGate().credentialSummary()}</span>
                            </div>
                        @endif
                        @if (primaryUtility.reportWorkflow().filingFee() != null && primaryUtility.reportWorkflow().filingFee().hasContent())
                            <div class="metadata-block">
                                <strong>Filing fee</strong>
                                <span>${primaryUtility.reportWorkflow().filingFee().display()}</span>
                            </div>
                        @endif
                        <div class="metadata-block">
                            <strong>Failed-test branch</strong>
                            <span>Ask whether repair, retest, and final submission are all handled under the same utility workflow.</span>
                        </div>
                    </div>
                    <div class="card-actions">
                        <a class="button button--secondary" href="/utilities/${primaryUtility.state()}/${primaryUtility.canonicalSlug()}/">Open utility workflow</a>
                        @if (primaryUtility.supportsApprovedTestersPage())
                            <a class="button button--secondary" href="/utilities/${primaryUtility.state()}/${primaryUtility.canonicalSlug()}/approved-testers">Official tester route</a>
                        @elseif (primaryUtility.supportsFindATesterPage())
                            <a class="button button--secondary" href="/utilities/${primaryUtility.state()}/${primaryUtility.canonicalSlug()}/find-a-tester">Directory route</a>
                        @endif
                        @if (portalHubPathsByUtility.containsKey(primaryUtility.utilityId()))
                            <a class="button button--secondary" href="${portalHubPathsByUtility.get(primaryUtility.utilityId())}">${portalNamesByUtility.getOrDefault(primaryUtility.utilityId(), "Reporting portal")} portal context</a>
                        @endif
                        <a class="button button--primary" href="${page.requestHelpPathOrDefault()}">Request local help</a>
                    </div>
                </article>
            </div>
        </section>
    @endif

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
                        <li><strong>Website:</strong> <a href="${CtaPaths.trackedPath(provider.siteUrl(), "provider-profile", "", provider.providerId(), "provider-website", "/providers/" + provider.providerId() + "/")}">${provider.siteUrl()}</a></li>
                    @else
                        <li><strong>Website:</strong> No public website is stored for this provider profile yet.</li>
                    @endif
                    @if (provider.officialApprovalSourceUrl() != null && !provider.officialApprovalSourceUrl().isBlank())
                        <li><strong>Authority source:</strong> <a href="${CtaPaths.trackedPath(provider.officialApprovalSourceUrl(), "provider-profile", "", provider.providerId(), "authority-source", "/providers/" + provider.providerId() + "/")}">${provider.officialApprovalSourceUrl()}</a></li>
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
                        <ul class="detail-list">
                            <li><strong>Due basis:</strong> ${utility.dueBasis()}</li>
                            <li><strong>Notice clue:</strong> ${noticeIdentifierHints.getOrDefault(utility.utilityId(), "Match the utility, address, assembly, and due date before booking.")}</li>
                            <li><strong>Submission route:</strong> ${utility.submissionMethods().isEmpty() ? "Confirm with the utility before scheduling." : utility.submissionMethods().get(0).label()}</li>
                            <li><strong>Proof to keep:</strong> ${reportAcceptanceHints.getOrDefault(utility.utilityId(), "Keep proof that the report was submitted through the accepted route.")}</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility page</a>
                            @if (utility.supportsApprovedTestersPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/approved-testers">Official tester route</a>
                            @elseif (utility.supportsFindATesterPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/find-a-tester">Directory route</a>
                            @endif
                            @if (portalHubPathsByUtility.containsKey(utility.utilityId()))
                                <a class="button button--secondary" href="${portalHubPathsByUtility.get(utility.utilityId())}">${portalNamesByUtility.getOrDefault(utility.utilityId(), "Portal")} context</a>
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
                <span class="eyebrow">Profile maintenance</span>
                <h2>Need a correction to this profile?</h2>
                <p>If a phone number, website, or approval source is wrong, report it for manual review. BackflowVerdict updates public provider profiles only after checking the source record.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/corrections">Report a correction</a>
                    <a class="button button--secondary" href="/contact">Contact BackflowVerdict</a>
                </div>
            </article>
        </div>
    </section>
`)
