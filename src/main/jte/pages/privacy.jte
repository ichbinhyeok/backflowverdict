@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container content-grid">
            <article class="detail-card">
                <span class="eyebrow kicker-rule">Lead handling</span>
                <h1>Privacy and lead routing notice</h1>
                <p class="lede">BackflowPath stores contact requests so the team can review utility context, route sponsor coverage where it exists, and follow up on local compliance questions.</p>
            </article>

            <article class="panel panel--hero">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">Plain language</span>
                    <h2 style="color: #ffffff;">What the form data is used for</h2>
                </div>
                <p>Lead requests are used to review demand, understand the utility page that generated the request, and route follow-up when there is a clearly labeled provider path.</p>
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
                    <li>Utility ID, utility name, page family, source page, and referring page when available.</li>
                    <li>Delivery and assignment records when a request is routed internally.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">How routing works</span>
                <h2>Official guidance stays separate</h2>
                <ul class="detail-list">
                    <li>Submitting a request does not change the underlying utility rule.</li>
                    <li>BackflowPath keeps official authority guidance and sponsor routing separate.</li>
                    <li>The same request is not resold to multiple providers without user consent.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container">
            <article class="detail-card">
                <span class="eyebrow">Operational details</span>
                <h2>Storage and review posture</h2>
                <p>Lead requests are stored in the file-backed leads workspace used by this application. Access is intended for internal review, sponsor routing operations, and manual follow-up.</p>
                <p style="margin-top: 1rem;">Questions about a stored request should be handled through the reply path used for the submission or through the utility-specific page that generated the request.</p>
            </article>
        </div>
    </section>
`)
