@import owner.backflow.web.PageMeta
@param PageMeta page
@param String message = "The requested page is unavailable."

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">404</span>
            <h1>That page is not available.</h1>
            <p class="lede">${message}</p>
            <div class="hero__actions">
                <a class="button button--primary" href="/">Go back home</a>
            </div>
        </div>
    </section>
`)
