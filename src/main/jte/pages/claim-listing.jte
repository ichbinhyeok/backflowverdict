@import owner.backflow.web.PageMeta
@param PageMeta page
@param int publicProviderCount
@param int publishedUtilityCount

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Provider listing support</span>
                <h1>Claim or correct a BackflowVerdict provider listing.</h1>
                <p class="lede">Use this page when a public provider profile needs a correction, a source link should be updated, or a provider wants listing context reviewed. Utility rules and official tester routes stay separate from provider claims.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/contact">Contact BackflowVerdict</a>
                    <a class="button button--secondary" href="/corrections">Report a correction</a>
                    <a class="button button--secondary" href="/official-backflow-tester-lists">Official tester lists</a>
                </div>
                <div class="hero__stats">
                    <span>${publicProviderCount} public provider profiles</span>
                    <span>${publishedUtilityCount} utility rule pages</span>
                    <span>Authority routes stay primary</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Listing boundary</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Claim does not equal approval</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">A provider profile can be corrected without changing whether a utility publishes an official approved tester list.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Corrections</span>
                <h2>Fix public listing details</h2>
                <p>Send the provider name, profile URL, corrected phone, website, service territory, license note, or source URL that should be reviewed.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/contact">Submit listing details</a>
                </div>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Authority evidence</span>
                <h2>Update official-list context</h2>
                <p>If a provider appears on a utility, county, BSI, WEIRS, or portal list, include the public source. BackflowVerdict will keep that evidence distinct from non-official directory copy.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/corrections">Send source evidence</a>
                </div>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Local help</span>
                <h2>Route a customer request</h2>
                <p>If the issue is a property owner looking for next steps, start from the utility page or request-help path so the governing rule remains attached to the request.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/states">Find utility page</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="trust-box">
                <span class="trust-box__label">What to include</span>
                <h2>Information that speeds review</h2>
                <ul class="detail-list">
                    <li>Provider name and BackflowVerdict profile URL, if one exists</li>
                    <li>Correct website, phone, license, or service-area detail</li>
                    <li>Official list or utility source URL when approval status is involved</li>
                    <li>Whether the request is a listing correction, source update, or duplicate profile issue</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Trust rule</span>
                <h2>Provider edits cannot rewrite utility requirements</h2>
                <p>BackflowVerdict can update a public provider profile, but it will not infer utility approval from a provider claim alone. Official tester status must come from the governing authority, county, or accepted portal source.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/methodology">Read methodology</a>
                    <a class="button button--secondary" href="/editorial-standards">Editorial standards</a>
                </div>
            </article>
        </div>
    </section>
`)
