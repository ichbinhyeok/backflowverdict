@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.config.SiteRuntimeConfig
@param PageMeta page
@param java.util.List<UtilityRecord> featuredUtilities
@param java.util.Map<String, String> annualBriefPaths
@param java.util.Map<String, String> failedBriefPaths
@param java.util.Map<String, String> utilityPaths
@param String demoPath
@param int texasUtilityCount
@param int dfwUtilityCount

@template.layouts.app(page = page, content = @`
    <div class="vendor-briefs-surface">
    <section class="page-hero page-hero--focus vendor-briefs-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Vendor workflow</span>
                <h1>Turn an annual notice or failed test into a customer brief in 2 minutes.</h1>
                <p class="lede">Paste the notice or failed result, choose the utility, and send one customer-ready brief from the office. Built for small busy backflow offices that do not want to forward portal screenshots or rewrite the same explanation on every call.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="#vendor-start-grid">Start with a utility</a>
                    <a class="button button--secondary" href="${demoPath}">View sample demo</a>
                </div>
                <div class="hero__stats">
                    <span>Annual notice</span>
                    <span>Failed test follow-up</span>
                    <span>Customer brief first</span>
                </div>
            </div>
            <aside class="panel panel--status vendor-briefs-briefing">
                <div class="vendor-briefs-briefing__topline">
                    <span class="stat-card__label">Office lane</span>
                    <span class="vendor-briefs-briefing__proof">2-minute brief</span>
                </div>
                <h2 class="vendor-briefs-briefing__title">What the office sends first</h2>
                <p class="vendor-briefs-briefing__intro">This is not the office dump. It is the front surface a coordinator can send when a customer asks why they were contacted and what happens next.</p>

                <div class="vendor-briefs-briefing__sheet" aria-hidden="true">
                    <div class="vendor-briefs-briefing__sheet-header">
                        <span class="eyebrow">Customer brief</span>
                        <span class="vendor-briefs-briefing__status">Pending filing</span>
                    </div>
                    <strong class="vendor-briefs-briefing__headline">Backflow result for the site</strong>
                    <p class="vendor-briefs-briefing__copy">Why the customer got the message, what they need to do next, and who is handling the utility filing.</p>
                    <div class="vendor-briefs-briefing__meta">
                        <div>
                            <span>Use case</span>
                            <strong>Annual notice or failed test</strong>
                        </div>
                        <div>
                            <span>Send first</span>
                            <strong>Link or customer PDF</strong>
                        </div>
                        <div>
                            <span>Behind it</span>
                            <strong>Office record and proof</strong>
                        </div>
                    </div>
                </div>

                <div class="metadata-grid">
                    <div class="metadata-block">
                        <strong>Best use case</strong>
                        <span>Annual notice and failed test</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Send first</strong>
                        <span>Customer brief, not the office record</span>
                    </div>
                    <div class="metadata-block">
                        <strong>Success signal</strong>
                        <span>Repeat use and customer opens</span>
                    </div>
                    <div class="metadata-block">
                        <strong>See the artifact</strong>
                        <span><a href="${demoPath}">Open the cold-email demo</a></span>
                    </div>
                    <div class="metadata-block">
                        <strong>Setup contact</strong>
                        <span>
                            @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                                <a href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup">${SiteRuntimeConfig.supportEmail()}</a>
                            @else
                                Ask through the contact page
                            @endif
                        </span>
                    </div>
                </div>
            </aside>
        </div>
    </section>

    <section class="section vendor-briefs-start-grid" id="vendor-start-grid">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">DFW starting points</span>
                <h2>Start with a utility and the exact job type</h2>
                <p>This first wedge is intentionally narrow: annual notices and failed tests in live Texas utility workflows.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : featuredUtilities)
                    <article class="detail-card detail-card--quiet vendor-briefs-utility-card">
                        <span class="pill">${utility.testingFrequency()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <ul class="detail-list">
                            <li>${utility.dueBasis()}</li>
                            <li>Last verified ${utility.lastVerified().toString()}</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--primary" href="${annualBriefPaths.get(utility.utilityId())}">Annual notice brief</a>
                            <a class="button button--secondary" href="${failedBriefPaths.get(utility.utilityId())}">Failed test brief</a>
                        </div>
                        <a class="vendor-briefs-utility-card__link" href="${utilityPaths.get(utility.utilityId())}">Open utility rule</a>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section vendor-briefs-flow" id="why-this-flow">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Why this exists</span>
                <h2>Use the brief when the customer needs the explanation, not the office dump</h2>
                <p>BackflowPath already holds the utility rule, failed-test logic, and filing context. This page turns that into one narrow office lane: choose the utility, choose annual or failed, then send one customer-facing artifact instead of forwarding a notice, portal screenshot, or internal record.</p>
            </div>
            <div class="process-grid">
                <article class="process-card">
                    <div class="process-card__index">
                        <span>01</span>
                        <span class="material-symbols-outlined">apartment</span>
                    </div>
                    <h3>Pick the utility</h3>
                    <p>Start from the exact local workflow. The brief is only strong if it is tied to the governing utility, not a generic plumbing explanation.</p>
                </article>
                <article class="process-card">
                    <div class="process-card__index">
                        <span>02</span>
                        <span class="material-symbols-outlined">description</span>
                    </div>
                    <h3>Create the brief</h3>
                    <p>Use the annual notice or failed-test path so the builder starts with the right reason, local rule context, and next action in under two minutes.</p>
                </article>
                <article class="process-card">
                    <div class="process-card__index">
                        <span>03</span>
                        <span class="material-symbols-outlined">forward_to_inbox</span>
                    </div>
                    <h3>Send the front surface first</h3>
                    <p>Send the brief first. Keep the office record, deeper proof, and filing detail behind that first send unless the customer asks for more.</p>
                </article>
            </div>
        </div>
    </section>

    <section class="section section--tight" id="setup-package">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Manual office setup</span>
                <h2>Need this aligned to one real office, not just used once?</h2>
                <p>The paid setup layer is not a generic branding job. It is a narrow office-alignment offer for a small busy backflow shop that wants annual-notice and failed-test briefs ready in its own sender style.</p>
            </div>
            <div class="content-grid" style="margin-bottom: 0.9rem;">
                <article class="detail-card detail-card--key">
                    <span class="eyebrow">Pilot setup</span>
                    <h3>$149 one-time for one office</h3>
                    <p>This pilot offer covers one office, one annual-notice default brief, one failed-test default brief, sender defaults, PDF wording cleanup, about 1 to 3 starter utilities, and one revision round.</p>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">How it works</span>
                    <h3>Email first, then a short manual setup</h3>
                    <ul class="detail-list">
                        <li>Email in with your office and utility mix</li>
                        <li>We confirm fit for the annual / failed workflow</li>
                        <li>You send the short intake and payment</li>
                        <li>First draft comes back within 3 business days</li>
                    </ul>
                </article>
            </div>
            <div class="card-grid card-grid--three">
                <article class="detail-card detail-card--key">
                    <span class="eyebrow">What setup does</span>
                    <h3>Turn the free workflow into your office version</h3>
                    <ul class="detail-list">
                        <li>Align the sender line with your company, phone, and email</li>
                        <li>Set one annual-notice default brief</li>
                        <li>Set one failed-test default brief</li>
                        <li>Clean up the customer PDF header and wording</li>
                        <li>Prepare sample briefs for the utilities you handle most</li>
                    </ul>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Best fit</span>
                    <h3>Small but busy office, not a giant rollout</h3>
                    <ul class="detail-list">
                        <li>Owner plus admin, coordinator, or CSR</li>
                        <li>Repeated annual notices and failed-test jobs</li>
                        <li>Still explaining the same situation by phone, text, or screenshot</li>
                        <li>Wants one office-ready starting point, not a new operating system</li>
                    </ul>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Out of scope</span>
                    <h3>What this setup does not try to replace</h3>
                    <ul class="detail-list">
                        <li>It is not a CRM or team-seat rollout</li>
                        <li>It does not send email or SMS for your office</li>
                        <li>It does not replace BSI, VEPO, SwiftComply, or utility filing</li>
                        <li>It is not default full white-label</li>
                    </ul>
                    <p class="detail-meta">There is no public subscription yet. Recurring only comes later if repeated office use proves that memory and reuse are strong enough to sell honestly.</p>
                    <div class="card-actions">
                        <a class="button button--secondary" href="#setup-intake">See the intake checklist</a>
                        @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                            <a class="button button--secondary" href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup&amp;body=Company%20name%3A%0AUtilities%3A%0AWorkflow%3A%20annual%20notice%20/%20failed%20test%20/%20both%0AWhat%20you%20want%20aligned%3A%0A">Email for setup</a>
                        @else
                            <a class="button button--secondary" href="/contact">Ask about setup</a>
                        @endif
                    </div>
                </article>
            </div>
        </div>
    </section>

    <section class="section" id="setup-intake">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Setup intake</span>
                <h2>What we need from one office to set this up well</h2>
                <p>Keep the intake short. The goal is to align one office fast, not turn the setup into a discovery-heavy consulting project.</p>
            </div>
            <div class="content-grid">
                <article class="detail-card">
                    <span class="eyebrow">Company defaults</span>
                    <h3>Sender identity and office line</h3>
                    <ul class="detail-list">
                        <li>Company name</li>
                        <li>Main office phone</li>
                        <li>Main office email</li>
                        <li>Preferred sender line or contact name</li>
                    </ul>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Workflow examples</span>
                    <h3>What your office handles most</h3>
                    <ul class="detail-list">
                        <li>The first 1 to 3 utilities to support</li>
                        <li>One recent annual notice or pasted notice text</li>
                        <li>One recent failed-test result or pasted failed-result text</li>
                        <li>Any next-step wording your office always wants included</li>
                    </ul>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Review and handoff</span>
                    <h3>What closes the setup</h3>
                    <ul class="detail-list">
                        <li>One office reviewer for sign-off</li>
                        <li>One revision round</li>
                        <li>A short call or a recorded walkthrough</li>
                        <li>A starter link and sample brief/PDF your staff can use right away</li>
                    </ul>
                </article>

                <article class="panel panel--dark">
                    <div class="section-heading section-heading--vendor-dark">
                        <span class="eyebrow">What you get back</span>
                        <h2>Ready-to-send, not just discussed</h2>
                        <p>The point of setup is not visual polish alone. The point is that your office can open one starter path on Monday and send the first annual-notice or failed-test brief without rebuilding the wording from scratch.</p>
                    </div>
                    <ol class="step-list">
                        <li>Annual-notice starter path</li>
                        <li>Failed-test starter path</li>
                        <li>Aligned sender defaults</li>
                        <li>Sample brief and sample PDF</li>
                    </ol>
                    @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                        <div class="card-actions">
                            <a class="button button--secondary" href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup&amp;body=Company%20name%3A%0AUtilities%3A%0AWorkflow%3A%20annual%20notice%20/%20failed%20test%20/%20both%0ARecent%20notice%20or%20failed%20result%3A%0A">Start setup by email</a>
                        </div>
                    @endif
                </article>
            </div>
        </div>
    </section>

    <section class="section section--tight vendor-briefs-flow-end">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Message discipline</span>
                <h2>What goes in the first send</h2>
                <ul class="detail-list">
                    <li>What happened on this site or assembly</li>
                    <li>What the customer needs to do next</li>
                    <li>What the vendor is still handling with the utility</li>
                    <li>How to reach the vendor if they need proof or coordination</li>
                </ul>
            </article>

            <article class="panel panel--dark">
                <div class="section-heading section-heading--vendor-dark">
                    <span class="eyebrow">What this replaces</span>
                    <h2>Do not lead with the office record</h2>
                    <p>The office record is for the office file, deeper proof, and filing record. The customer brief replaces the awkward first send where the office would otherwise forward a notice, portal screenshot, or too much back-office detail.</p>
                </div>
                <ol class="step-list">
                    <li>Customer brief first</li>
                    <li>PDF attachment only when someone asks for one</li>
                    <li>Office record stays as the office copy</li>
                </ol>
            </article>
        </div>
    </section>
    </div>
`)
