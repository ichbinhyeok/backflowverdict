@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container content-grid">
            <article class="detail-card">
                <span class="eyebrow kicker-rule">Request handling</span>
                <h1>Privacy and request handling</h1>
                <p class="lede">BackflowPath stores contact requests so the team can review the local utility context, answer next-step questions, and follow up on public provider options when useful.</p>
            </article>

            <article class="panel panel--hero">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Plain language</span>
                    <h2 style="color: #ffffff;">What the form data is used for</h2>
                </div>
                <p>Requests are used to understand the page that generated the question, review the governing utility workflow, and respond with the clearest next step.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card">
                <span class="eyebrow">What is stored</span>
                <h2>Stored request data</h2>
                <ul class="detail-list">
                    <li>Name, phone, email, city, property type, issue type, and notes from the form.</li>
                    <li>Trusted utility routing fields when BackflowPath can validate the request against a server-issued routing context.</li>
                    <li>Submitted utility ID, utility name, page family, source page, and referring page when the browser sends them.</li>
                    <li>Assignment records when the team manually matches a request to a public provider profile.</li>
                    <li>Storage metadata needed to export or review the request history later.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">How review works</span>
                <h2>Official guidance stays separate</h2>
                <ul class="detail-list">
                    <li>Submitting a request does not change the underlying utility rule.</li>
                    <li>BackflowPath keeps official authority guidance and public provider directories separate.</li>
                    <li>Requests are stored for review first, even when the page context is verified.</li>
                        <li>BackflowPath does not sell or fan out a request through a private dispatch queue.</li>
                    <li>If the routing context cannot be verified, the request stays in BackflowPath for manual review.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container">
            <article class="detail-card">
                <span class="eyebrow">Operational details</span>
                <h2>Storage and review posture</h2>
                <p>Requests are stored in the file-backed workspace used by this application. Access is intended for internal review, export, and manual follow-up only.</p>
                <p style="margin-top: 1rem;">Questions about a stored request should be handled through the reply path used for the submission or through the utility-specific page that generated the request.</p>
            </article>
        </div>
    </section>
`)
