@import owner.backflow.web.PageMeta

@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Pricing</span>
                <h1>Simple pricing, handled manually after review</h1>
                <p class="lede">BackflowPath reviews every provider request before publication or billing. Right now pricing is a guide for manual follow-up, not an instant self-serve checkout flow.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/claim-listing">Start a review request</a>
                    <a class="button button--secondary" href="/for-providers">How provider listings work</a>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Billing posture</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">No instant checkout</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">Claim requests and fit checks come first. If a featured placement is approved, BackflowPath can send the payment link afterward by email.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card detail-card--quiet">
                <span class="pill">Free</span>
                <h2>Claim or correct a listing</h2>
                <p class="lede">$0</p>
                <ul class="detail-list">
                    <li>Claim an existing BackflowPath profile</li>
                    <li>Request edits to name, website, contact details, or service area</li>
                    <li>Manual review before public changes</li>
                </ul>
                <div class="card-actions">
                    <a class="button button--secondary" href="/claim-listing?requestType=claim-existing-profile">Claim a profile</a>
                </div>
            </article>

            <article class="detail-card detail-card--key">
                <span class="pill">Featured</span>
                <h2>Featured utility placement</h2>
                <p class="lede">$49 / month</p>
                <ul class="detail-list">
                    <li>Featured placement tied to one utility or tester route where commercial placement fits</li>
                    <li>Public provider profile with utility context</li>
                    <li>Clearly labeled as provider help, not authority status</li>
                    <li>Manual approval first, then payment link if accepted</li>
                </ul>
                <div class="card-actions">
                    <a class="button button--primary" href="/claim-listing?requestType=featured-utility">Ask for review</a>
                </div>
            </article>

            <article class="detail-card">
                <span class="pill">Metro</span>
                <h2>Featured metro placement</h2>
                <p class="lede">$79 / month</p>
                <ul class="detail-list">
                    <li>Featured placement on one metro page plus the provider profile</li>
                    <li>Best fit for companies covering several mapped utilities in one market</li>
                    <li>Manual review of service-area fit before activation</li>
                    <li>If approved, billing and setup are handled afterward by email</li>
                </ul>
                <div class="card-actions">
                    <a class="button button--secondary" href="/claim-listing?requestType=featured-metro">Ask for review</a>
                </div>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Important notes</span>
                <h2>What pricing does and does not mean</h2>
                <ul class="detail-list">
                    <li>Pricing is for visibility and listing management, not for being labeled as utility-approved.</li>
                    <li>BackflowPath can refuse placement that would blur the official workflow.</li>
                    <li>Utilities with official tester lists keep that authority list first.</li>
                    <li>Coverage requests are reviewed against public website evidence and mapped service area.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Best next step</span>
                <h2>Start with the review form</h2>
                <p>If you already have a BackflowPath listing, claim that profile first. If you do not, submit a new listing request and choose the placement you want to ask about. BackflowPath reviews the request, replies by email, and only then handles billing if needed.</p>
                <div class="card-actions">
                    <a class="button button--secondary" href="/claim-listing">Open the review form</a>
                </div>
            </article>
        </div>
    </section>
`)
