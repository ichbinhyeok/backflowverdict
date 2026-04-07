@import owner.backflow.web.PageMeta

@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">Request received</span>
            <h1>Your listing request is in review</h1>
            <p class="lede">BackflowPath received the request. Claim and placement requests are reviewed manually, and the next step is sent by email after review.</p>
            <div class="content-grid" style="margin-top: 1.5rem;">
                <article class="detail-card">
                    <span class="eyebrow">What happens next</span>
                    <h2>Manual review first</h2>
                    <p>BackflowPath checks the company details, public website, service area, and requested placement before deciding what should go live.</p>
                </article>
                <article class="detail-card">
                    <span class="eyebrow">Email follow-up</span>
                    <h2>No automatic publication or billing</h2>
                    <p>Submitting this form does not automatically create a profile, feature a listing, or start billing. If featured placement is approved, BackflowPath sends the payment step later by email.</p>
                </article>
            </div>
            <div class="hero__actions">
                <a class="button button--primary" href="/for-providers">Back to provider info</a>
                <a class="button button--secondary" href="/pricing">View pricing</a>
            </div>
        </div>
    </section>
`)
