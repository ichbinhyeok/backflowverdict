@import owner.backflow.web.PageMeta

@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">For providers</span>
                <h1>Claim, correct, or review your BackflowPath listing request</h1>
                <p class="lede">BackflowPath keeps utility rules and provider marketing separate. Providers can claim an existing profile, request a new listing, or ask about featured placement, but approval and follow-up still happen manually by email.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/claim-listing">Claim or request a listing</a>
                    <a class="button button--secondary" href="/pricing">View pricing</a>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Review policy</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Manual review first</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">Submitting a claim request does not automatically publish, upgrade, or rewrite a listing. BackflowPath reviews site details, service area, and route fit before anything goes live.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container process-grid">
            <article class="process-card">
                <div class="process-card__index">
                    <span>01</span>
                    <span class="material-symbols-outlined">fact_check</span>
                </div>
                <h3>Submit your request</h3>
                <p>Claim an existing profile, request a new listing, or ask about featured placement from one form.</p>
            </article>
            <article class="process-card">
                <div class="process-card__index">
                    <span>02</span>
                    <span class="material-symbols-outlined">policy</span>
                </div>
                <h3>BackflowPath reviews fit</h3>
                <p>The review checks public web signals, service area, and whether your requested placement fits the utility-first trust model.</p>
            </article>
            <article class="process-card">
                <div class="process-card__index">
                    <span>03</span>
                    <span class="material-symbols-outlined">mail</span>
                </div>
                <h3>Email next step</h3>
                <p>If the request is a fit, BackflowPath replies by email with the update path, approval notes, or a payment link for featured placement.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">What providers can do here</span>
                <h2>Supported requests</h2>
                <ul class="detail-list">
                    <li>Claim an existing provider profile that already appears on BackflowPath</li>
                    <li>Request a new public listing for a backflow testing company</li>
                    <li>Correct company name, website, contact details, or service area</li>
                    <li>Ask about featured utility or metro placement</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">What gets reviewed</span>
                <h2>Review criteria</h2>
                <ul class="detail-list">
                    <li>Whether the company publicly offers backflow testing, repair, or related compliance work</li>
                    <li>Whether the requested utility or metro coverage matches the public service area</li>
                    <li>Whether any approval or certification claims can be supported by a real authority source</li>
                    <li>Whether the requested placement stays clearly separate from the official utility workflow</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">What this does not do</span>
                <h2>Trust boundary</h2>
                <ul class="detail-list">
                    <li>It does not mark a provider as utility-approved unless the authority actually publishes that status.</li>
                    <li>It does not auto-publish listings just because a form was submitted.</li>
                    <li>It does not place sponsored inventory inside the official source block.</li>
                    <li>It does not guarantee coverage on every utility page in a metro.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Public listing basics</span>
                <h2>What a public provider profile can include</h2>
                <ul class="detail-list">
                    <li>Company name, website, phone, and email when public signals support them</li>
                    <li>Mapped utility pages and metro support pages</li>
                    <li>Clearly labeled route type such as public directory or sponsored profile</li>
                    <li>Links back to utility pages so users see the official rule first</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Commercial layer</span>
                <h2>Featured placement rules</h2>
                <p>Featured placement can increase visibility, but it never overrides the authority workflow. If a utility publishes an official tester list, that list stays first and featured placement remains clearly separate. There is no instant checkout or auto-publish flow here yet.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/pricing">See plans</a>
                    <a class="button button--secondary" href="/claim-listing?requestType=featured-utility">Ask about a featured utility slot</a>
                </div>
            </article>
        </div>
    </section>
`)
