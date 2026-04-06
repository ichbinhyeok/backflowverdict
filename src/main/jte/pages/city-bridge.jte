@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param CityAliasRecord alias
@param UtilityRecord utility

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">City alias bridge</span>
            <h1>${alias.city()} backflow testing routes through ${utility.utilityName()}</h1>
            <p class="lede">${alias.justification()}</p>
            <div class="hero__actions">
                <a class="button button--primary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open the governing utility page</a>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="container content-grid">
            <article class="detail-card">
                <h2>Why this bridge exists</h2>
                <p>This page exists only because city-level search demand or enforcement language justified a bridge route. It should not replace the governing utility page.</p>
            </article>

            <article class="detail-card">
                <h2>Mapped utility</h2>
                <ul class="detail-list">
                    <li>${utility.utilityName()}</li>
                    <li>Mode: ${alias.aliasMode()}</li>
                    <li>Last reviewed: ${alias.lastReviewed().toString()}</li>
                </ul>
            </article>
        </div>
    </section>
`)
