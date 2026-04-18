@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.data.model.UtilityFocusContent
@param PageMeta page
@param UtilityRecord utility
@param String eyebrow
@param String heading
@param String intro
@param UtilityFocusContent focus
@param java.util.List<String> residentialNotes
@param java.util.List<String> commercialNotes
@param String testerPath = null
@param String testerLabel = null
@param String requestHelpPath
@param String handoffBuilderPath = ""
@param java.util.List<owner.backflow.web.FaqItem> faqItems
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()
@param StateGuideRecord stateGuide = null
@param int activeSponsorCount = 0

@template.layouts.app(page = page, content = @`
    <section class="page-hero page-hero--focus">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">${eyebrow}</span>
                <h1>${heading}</h1>
                <p class="lede">${intro}</p>
                <div class="hero__stats">
                    <span>Utility: ${utility.utilityName()}</span>
                    <span>Last verified: ${utility.lastVerified().toString()}</span>
                    <span>Due basis: ${utility.dueBasis()}</span>
                </div>
                <div class="hero__actions">
                    <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Back to utility requirements</a>
                    <a class="button button--primary" href="${utility.utilityUrl()}">Open the official program page</a>
                    @if (handoffBuilderPath != null && !handoffBuilderPath.isBlank())
                        <a class="button button--secondary" href="${handoffBuilderPath}">Office staff: create annual brief</a>
                    @endif
                    <a class="button button--secondary" href="#local-follow-up">Local follow-up options</a>
                </div>
            </div>
            <aside class="panel panel--status">
                <span class="stat-card__label">Focus status</span>
                <div class="stat-card__value" style="font-size: clamp(2rem, 4vw, 2.8rem);">${eyebrow}</div>
                <p class="stat-card__meta">${focus.summary()}</p>
                <div class="metadata-grid" style="margin-top: 1rem;">
                    <div class="metadata-block">
                        <strong>Testing cadence</strong>
                        <span>${utility.testingFrequency()}</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Program phone</strong>
                        <span>${utility.phone()}</span>
                    </div>
                </div>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container page-frame">
            <div class="stack">
                <div class="content-grid">
                    <article class="detail-card">
                        <span class="eyebrow">Direct answer</span>
                        <h2>What matters here</h2>
                        <p>${focus.summary()}</p>
                        <p class="detail-meta"><strong>Due basis:</strong> ${utility.dueBasis()}</p>
                    </article>

                    <article class="detail-card${handoffBuilderPath != null && !handoffBuilderPath.isBlank() ? " detail-card--key" : ""}">
                        <span class="eyebrow">Highlights</span>
                        @if (handoffBuilderPath != null && !handoffBuilderPath.isBlank())
                            <h2>Need to explain this to a customer?</h2>
                            <p>If the office already has the annual notice in hand, create the customer brief here instead of rewriting the same explanation again.</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="${handoffBuilderPath}">Create annual customer brief</a>
                            </div>
                            <ul class="detail-list">
                                @for (var highlight : focus.highlights())
                                    <li>${highlight}</li>
                                @endfor
                            </ul>
                        @else
                            <h2>Highlights</h2>
                            <ul class="detail-list">
                                @for (var highlight : focus.highlights())
                                    <li>${highlight}</li>
                                @endfor
                            </ul>
                        @endif
                    </article>
                </div>

                <article class="panel panel--dark">
                    <div class="section-heading">
                        <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Workflow</span>
                        <h2 style="color: #ffffff;">Workflow</h2>
                    </div>
                    <ol class="step-list">
                        @for (var step : focus.workflowSteps())
                            <li>${step}</li>
                        @endfor
                    </ol>
                </article>

                <div class="card-grid">
                    @if (!residentialNotes.isEmpty())
                        <article class="detail-card">
                            <span class="eyebrow">Residential</span>
                            <h2>Residential</h2>
                            <ul class="detail-list">
                                @for (var item : residentialNotes)
                                    <li>${item}</li>
                                @endfor
                            </ul>
                        </article>
                    @endif

                    @if (!commercialNotes.isEmpty())
                        <article class="detail-card">
                            <span class="eyebrow">Commercial focus</span>
                            <h2>Commercial and managed properties</h2>
                            <ul class="detail-list">
                                @for (var item : commercialNotes)
                                    <li>${item}</li>
                                @endfor
                            </ul>
                        </article>
                    @endif
                </div>

                <article class="trust-box trust-box--warning" id="local-follow-up">
                    <span class="trust-box__label">After the official step</span>
                    <h2>Need local follow-up?</h2>
                    @if (testerPath != null && testerLabel != null)
                        @if (utility.supportsApprovedTestersPage())
                            <p>Use the governing authority's official tester list after you confirm the rule, due basis, and submission method for this exact topic.</p>
                        @else
                            <p>This utility does not publish an official approved list in the registry. Use the non-official tester route only after confirming the exact utility workflow for this page topic.</p>
                        @endif
                        <div class="hero__actions">
                            <a class="button button--primary" href="${testerPath}">${testerLabel}</a>
                            <a class="button button--secondary" href="${requestHelpPath}">Request local help</a>
                        </div>
                    @else
                        <p>No public tester route is live for this utility yet. Stay on the official program path and confirm the scope directly with the utility.</p>
                        <div class="hero__actions">
                            <a class="button button--primary" href="${requestHelpPath}">Request local help</a>
                        </div>
                    @endif
                    <p class="detail-meta">If you still need help after checking the official workflow, you can submit a request for local follow-up.</p>
                </article>

                <section class="section section--tight">
                    <div class="section-heading">
                        <span class="eyebrow">FAQ</span>
                        <h2>Local questions people actually ask</h2>
                    </div>
                    <div class="faq-list">
                        @for (var faqItem : faqItems)
                            <details class="faq-item">
                                <summary>${faqItem.question()}</summary>
                                <p>${faqItem.answer()}</p>
                            </details>
                        @endfor
                    </div>
                </section>

                <article class="detail-card">
                    <span class="eyebrow">Official sources</span>
                    <h2>Official sources</h2>
                    <div class="source-list">
                        @for (var source : utility.sources())
                            <a class="source-item" href="${source.url()}">
                                <div>
                                    <strong>${source.label()}</strong>
                                    <span>${focus.summary()}</span>
                                </div>
                                <em>${source.kind()}</em>
                            </a>
                        @endfor
                    </div>
                </article>
            </div>

            <aside class="side-stack sticky-rail">
                <article class="detail-card">
                    <span class="eyebrow">Submission methods</span>
                    <h2>Submission methods</h2>
                    <div class="resource-list">
                        @for (var submissionMethod : utility.submissionMethods())
                            <a class="resource-item" href="${submissionMethod.url()}">
                                <div>
                                    <strong>${submissionMethod.label()}</strong>
                                    <span>${submissionMethod.kind()}</span>
                                </div>
                                <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
                        @endfor
                    </div>
                    <p class="detail-meta"><strong>Program phone:</strong> ${utility.phone()}</p>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">State compliance layer</span>
                    <h2>State compliance layer</h2>
                    @if (stateGuide != null)
                        <p><a href="/states/${stateGuide.state()}/backflow-testing">${stateGuide.title()}</a></p>
                        <p>${stateGuide.authoritySummary()}</p>
                    @else
                        <p>No published state hub is mapped to this utility yet.</p>
                    @endif
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Support guides</span>
                    <h2>Support guides</h2>
                    @if (relatedGuides.isEmpty())
                        <p>No support guides are mapped yet.</p>
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

                <article class="visual-card">
                    <img src="/images/design/utility-provider-2.jpg" alt="Pressure vacuum breaker and irrigation backflow assembly">
                    <div class="visual-card__overlay">
                        <strong>Route back to the authority page</strong>
                        <p>Subpage intent should sharpen the answer, then push the user back into the official utility workflow or a clearly labeled tester route.</p>
                    </div>
                </article>
            </aside>
        </div>
    </section>
`)
