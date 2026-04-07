@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">Request received</span>
            <h1>Thanks, your request is in</h1>
            <p class="lede">BackflowPath will review the utility context and the details you submitted before any follow-up.</p>
            <div class="content-grid" style="margin-top: 1.5rem;">
                <article class="detail-card">
                    <span class="eyebrow">What helps most</span>
                    <h2>Useful details</h2>
                    <p>If you included the utility name, a notice deadline, or failed-test context, that gives the review a clearer starting point.</p>
                </article>
                <article class="detail-card">
                    <span class="eyebrow">While you wait</span>
                    <h2>Use the official rule page</h2>
                    <p>Keep using the local utility page for deadlines, reporting steps, and official tester requirements. Provider help should always come second to the authority workflow.</p>
                </article>
            </div>
            <div class="hero__actions">
                <a class="button button--primary" href="/states">Browse local rules</a>
                <a class="button button--secondary" href="/">Back to home</a>
            </div>
        </div>
    </section>
`)
