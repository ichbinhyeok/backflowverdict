@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.service.LeadRoutingService
@param PageMeta page
@param UtilityRecord utility
@param GuideRecord failedGuide = null
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()
@param String testerPath = null
@param String testerLabel = null

@template.layouts.app(page = page, content = @`
    <div class="failure-workflow-surface">
    <section class="page-hero failure-workflow-hero">
        <div class="container page-frame">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Failed-test workflow</span>
                <h1>${utility.utilityName()} failed backflow test next steps</h1>
                <p class="lede">Use the governing utility workflow first, then move through repair, retest, and reporting in the order the utility expects.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="#repair-path">Repair / retest help</a>
                    <a class="button button--secondary" href="${utility.utilityUrl()}">Official program page</a>
                    <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Back to utility page</a>
                </div>
            </div>
            <div class="side-stack">
                <article class="panel panel--status">
                    <span class="stat-card__label">Priority risk</span>
                    <div class="stat-card__value">Water shutoff risk</div>
                    <p class="stat-card__meta">A failed device means the water authority can escalate quickly if repair and retest are not completed on time.</p>
                </article>
                <article class="detail-card">
                    <span class="stat-card__label">Legal status</span>
                    <div class="stat-card__value" style="font-size: 2rem;">Notice of violation</div>
                    <p class="stat-card__meta">Use the utility page and source notes below before treating any repair offer as sufficient proof of compliance.</p>
                </article>
            </div>
        </div>
    </section>

    <section class="section failure-workflow-compliance">
        <div class="container">
            <article class="panel panel--dark">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(197, 234, 223, 0.92);">The path to compliance</span>
                    <h2 style="color: #ffffff;">The path to compliance</h2>
                    <p>Follow these steps in order. Repair alone is not enough. The retest and the accepted report are what close the failure.</p>
                </div>
                <div class="process-grid">
                    <article class="process-card" style="background: transparent; color: #ffffff;">
                        <div class="process-card__index" style="color: rgba(197, 234, 223, 0.26);">
                            <span>01</span>
                            <span class="material-symbols-outlined">build</span>
                        </div>
                        <h3 style="color: #ffffff;">Repair device</h3>
                        <p style="color: rgba(239, 241, 243, 0.72);">Use a tester or repair company that fits the local utility workflow and device type.</p>
                    </article>
                    <article class="process-card" style="background: transparent; color: #ffffff;">
                        <div class="process-card__index" style="color: rgba(197, 234, 223, 0.26);">
                            <span>02</span>
                            <span class="material-symbols-outlined">assignment_turned_in</span>
                        </div>
                        <h3 style="color: #ffffff;">Retest</h3>
                        <p style="color: rgba(239, 241, 243, 0.72);">Schedule the passing retest after repairs are complete and before the utility deadline passes.</p>
                    </article>
                    <article class="process-card" style="background: transparent; color: #ffffff;">
                        <div class="process-card__index" style="color: rgba(197, 234, 223, 0.26);">
                            <span>03</span>
                            <span class="material-symbols-outlined">cloud_upload</span>
                        </div>
                        <h3 style="color: #ffffff;">Submit report</h3>
                        <p style="color: rgba(239, 241, 243, 0.72);">The corrected report and proof of acceptance are what actually restore compliance.</p>
                    </article>
                </div>
            </article>
        </div>
    </section>

    <section class="section section--tight failure-workflow-dual-lane">
        <div class="container content-grid">
            <article class="detail-card detail-card--key" id="office-send-path">
                <span class="eyebrow">Failure overview</span>
                <h2>What the failed notice means</h2>
                <p>A failed result is not closed until the repair, retest, and accepted submission all happen. Use this page to understand the sequence before anyone treats the job as complete.</p>
                <ul class="detail-list">
                    <li>The failure notice means the utility still considers the device out of compliance</li>
                    <li>Repair alone does not close the file</li>
                    <li>The passing retest has to happen before the deadline slips</li>
                    <li>The accepted report is what actually closes the utility record</li>
                </ul>
            </article>

            <article class="detail-card detail-card--warm" id="repair-path">
                <span class="eyebrow">Repair path</span>
                <h2>Need repair, retest, or provider coordination?</h2>
                <p>Use this when the next job is field action, not explanation. Repair by itself does not close the failure.</p>
                <div class="hero__actions">
                    @if (testerPath != null && testerLabel != null)
                        <a class="button button--primary" href="${testerPath}">${testerLabel}</a>
                    @endif
                    <a class="button button--secondary" href="${LeadRoutingService.requestHelpPath(utility.utilityId(), "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/failed-test", "failed-test-repair", "failed-test")}">Request local help</a>
                </div>
                <ul class="detail-list">
                    <li>Repair the failed assembly or blocked components first</li>
                    <li>Schedule the passing retest before the deadline slips</li>
                    <li>Get the accepted report into the utility workflow last</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight failure-workflow-support">
        <div class="container content-grid">
            <article class="detail-card" id="local-help">
                <div class="visual-card">
                    <img src="/images/design/failed-repair.jpg" alt="Repair specialist working on a backflow prevention assembly">
                    <div class="visual-card__overlay">
                        <strong>Repair and retest context</strong>
                        <p>Use provider routing only after you understand the utility's exact submission and retest expectations.</p>
                    </div>
                </div>
                <span class="eyebrow">Local failure notes</span>
                <h2>Do not stop at the repair</h2>
                <ul class="detail-list">
                    @for (var item : utility.failureHighlights())
                        <li>${item}</li>
                    @endfor
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Support resources</span>
                <h2>Support guides</h2>
                @if (relatedGuides.isEmpty())
                    <p>No related support guides are mapped yet.</p>
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

    @if (failedGuide != null)
        <section class="section">
            <div class="container">
                <article class="trust-box">
                    <span class="trust-box__label">Evergreen support</span>
                    <h2>Evergreen support</h2>
                    <p>${failedGuide.description()}</p>
                    <div class="card-actions">
                        <a class="button button--secondary" href="/guides/${failedGuide.slug()}">Open the full failed-test guide</a>
                    </div>
                </article>
            </div>
        </section>
    @endif
    </div>
`)
