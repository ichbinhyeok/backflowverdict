@import owner.backflow.config.SiteRuntimeConfig
@import owner.backflow.service.HandoffRecord
@import owner.backflow.web.PageMeta
@param PageMeta page
@param HandoffRecord annualDemo
@param String annualSubmissionGuidance
@param String annualFullRuleTrackedPath
@param String annualOfficialProgramTrackedPath
@param String annualStartPath
@param HandoffRecord failedDemo
@param String failedSubmissionGuidance
@param String failedFullRuleTrackedPath
@param String failedOfficialProgramTrackedPath
@param String failedStartPath
@param String workflowPath

@template.layouts.app(page = page, content = @`
    <div class="vendor-demo-surface">
    <section class="page-hero page-hero--focus vendor-demo-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Cold email demo</span>
                <h1>Show the customer brief first. Explain the office workflow second.</h1>
                <p class="lede">This is the first-click demo for vendor outreach. It shows the exact customer-facing surface a small backflow office can send after an annual notice or failed test before anyone sees the builder, office record, or utility workflow.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="#annual-notice-sample">View annual notice sample</a>
                    <a class="button button--secondary" href="#failed-test-sample">View failed test sample</a>
                    <a class="button button--secondary" href="${workflowPath}">See office workflow</a>
                </div>
                <div class="hero__stats">
                    <span>Customer brief first</span>
                    <span>Office workflow second</span>
                    <span>Cold email demo</span>
                </div>
            </div>
            <aside class="panel panel--status vendor-demo-rail">
                <div class="vendor-demo-rail__topline">
                    <span class="stat-card__label">Why this page exists</span>
                    <span class="vendor-demo-rail__proof">Demo before workflow</span>
                </div>
                <h2 class="vendor-demo-rail__title">Lead with the sendable surface</h2>
                <p class="vendor-demo-rail__intro">If the brief feels useful on this page, the next step is the office workflow page where the vendor picks the utility, opens the builder, and aligns the office setup.</p>
                <div class="vendor-demo-rail__list">
                    <div>
                        <span>What this page proves</span>
                        <strong>The customer can understand why they were contacted and what happens next.</strong>
                    </div>
                    <div>
                        <span>What it hides</span>
                        <strong>The office record, deeper proof, and utility filing detail stay behind the first send.</strong>
                    </div>
                    <div>
                        <span>What comes next</span>
                        <strong>Move to the vendor workflow only after the sample format feels right.</strong>
                    </div>
                </div>
                <div class="card-actions">
                    <a class="button button--primary" href="${workflowPath}">See the office workflow</a>
                    @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                        <a class="button button--secondary" href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup">Email about office setup</a>
                    @endif
                </div>
            </aside>
        </div>
    </section>

    <section class="section section--tight vendor-demo-jump">
        <div class="container">
            <div class="vendor-demo-jump__bar">
                <a class="vendor-demo-jump__chip" href="#annual-notice-sample">Annual notice sample</a>
                <a class="vendor-demo-jump__chip" href="#failed-test-sample">Failed test sample</a>
                <a class="vendor-demo-jump__chip" href="${workflowPath}">Office workflow</a>
            </div>
        </div>
    </section>

    <section class="section section--tight vendor-demo-bridge">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">How to read this</span>
                <h2>Start with the customer-facing artifact</h2>
                <p>Each sample below is written the same way a customer sees it. The demo is intentionally narrow: one annual-notice case and one failed-test case. If the artifact feels clear, move to the office workflow page and build the live version from the right utility path.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">What to do after this</span>
                <h2>Three possible next clicks</h2>
                <ul class="detail-list">
                    <li>Stay on this page and inspect the two sample briefs.</li>
                    <li>Open the office workflow once the format makes sense.</li>
                    <li>Jump straight into the annual or failed-test builder if you already know the wedge.</li>
                </ul>
                <div class="card-actions">
                    <a class="button button--secondary" href="${annualStartPath}">Start annual notice builder</a>
                    <a class="button button--secondary" href="${failedStartPath}">Start failed test builder</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section vendor-demo-sample" id="annual-notice-sample">
        <div class="container">
            <div class="vendor-demo-sample__header">
                <div class="section-heading">
                    <span class="eyebrow">Annual notice sample</span>
                    <h2>After a passing annual test, before the utility filing is closed</h2>
                    <p>This is the common “the test passed, but the notice is not truly closed yet” explanation surface.</p>
                </div>
                <div class="vendor-demo-sample__actions">
                    <a class="button button--secondary" href="${annualStartPath}">Use this annual path</a>
                    <a class="button button--secondary" href="${workflowPath}">See the full office workflow</a>
                </div>
            </div>
            @template.components.handoffBriefSheet(
                handoff = annualDemo,
                submissionGuidance = annualSubmissionGuidance,
                fullRuleTrackedPath = annualFullRuleTrackedPath,
                officialProgramTrackedPath = annualOfficialProgramTrackedPath
            )
        </div>
    </section>

    <section class="section vendor-demo-sample" id="failed-test-sample">
        <div class="container">
            <div class="vendor-demo-sample__header">
                <div class="section-heading">
                    <span class="eyebrow">Failed test sample</span>
                    <h2>When the customer needs the repair and retest explained in plain English</h2>
                    <p>This is the “it failed, what now?” surface. It keeps the first send understandable without exposing the whole office packet.</p>
                </div>
                <div class="vendor-demo-sample__actions">
                    <a class="button button--secondary" href="${failedStartPath}">Use this failed-test path</a>
                    <a class="button button--secondary" href="${workflowPath}">See the full office workflow</a>
                </div>
            </div>
            @template.components.handoffBriefSheet(
                handoff = failedDemo,
                submissionGuidance = failedSubmissionGuidance,
                fullRuleTrackedPath = failedFullRuleTrackedPath,
                officialProgramTrackedPath = failedOfficialProgramTrackedPath
            )
        </div>
    </section>

    <section class="section section--tight vendor-demo-close">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Decision point</span>
                <h2>If the sample feels right, move into the workflow page</h2>
                <p>The next page is where the vendor chooses the exact utility, picks annual notice or failed test, and creates the real office brief. Keep the demo as the cold-email entry and the workflow page as the second step.</p>
                <div class="card-actions">
                    <a class="button button--primary" href="${workflowPath}">Open vendor workflow</a>
                </div>
            </article>

            <article class="panel panel--dark">
                <div class="section-heading section-heading--vendor-dark">
                    <span class="eyebrow">Setup layer</span>
                    <h2>Need this aligned to one real office?</h2>
                    <p>The next paid layer is still setup-led, not a full vendor SaaS. The free workflow proves the artifact. Setup aligns the sender line, annual note, failed-test wording, and sample PDFs for one office.</p>
                </div>
                <ol class="step-list">
                    <li>Approve the artifact style on this page</li>
                    <li>Move into the office workflow page</li>
                    <li>Ask for office setup only after the free flow feels useful</li>
                </ol>
                @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                    <div class="card-actions">
                        <a class="button button--secondary" href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup">Email about office setup</a>
                    </div>
                @endif
            </article>
        </div>
    </section>
    </div>
`)
