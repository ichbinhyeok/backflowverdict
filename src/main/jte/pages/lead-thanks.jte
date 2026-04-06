@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">Stored</span>
            <h1>Lead request saved</h1>
            <p class="lede">The request is now in the private admin queue and in the JSONL plus CSV export set.</p>
            <div class="hero__actions">
                <a class="button button--primary" href="/leads/new">Capture another lead</a>
                <a class="button button--secondary" href="/admin">Open admin</a>
            </div>
        </div>
    </section>
`)
