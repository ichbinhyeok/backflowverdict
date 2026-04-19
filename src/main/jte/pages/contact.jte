@import owner.backflow.web.PageMeta
@import owner.backflow.config.SiteRuntimeConfig
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Contact</span>
                <h1>Contact BackflowPath</h1>
                <p class="lede">Use this path for corrections, source updates, broken links, vendor workflow setup questions, or questions about how a BackflowPath page was assembled. For utility-specific service help, the request form should still start after the user has seen the relevant local rule page.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="${page.requestHelpPathOrDefault()}">Request help</a>
                    @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                        <a class="button button--secondary" href="mailto:${SiteRuntimeConfig.supportEmail()}?subject=BackflowPath%20office%20setup">Email vendor setup</a>
                    @endif
                    <a class="button button--secondary" href="/corrections">Report a correction</a>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Public contact path</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Corrections and support</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">The contact route is for the site itself: sources, corrections, routing clarity, vendor setup questions, and page issues. It is not a substitute for the utility's own program contact.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Direct channels</span>
                <h2>Configured site contact</h2>
                <ul class="detail-list">
                    @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                        <li><strong>Email:</strong> <a href="mailto:${SiteRuntimeConfig.supportEmail()}">${SiteRuntimeConfig.supportEmail()}</a></li>
                    @else
                        <li><strong>Email:</strong> No direct support email is published in site configuration yet.</li>
                    @endif
                    @if (SiteRuntimeConfig.supportPhone() != null && !SiteRuntimeConfig.supportPhone().isBlank())
                        <li><strong>Phone:</strong> <a href="tel:${SiteRuntimeConfig.supportPhone()}">${SiteRuntimeConfig.supportPhone()}</a></li>
                    @else
                        <li><strong>Phone:</strong> No public support phone is published in site configuration yet.</li>
                    @endif
                </ul>
                <p class="detail-meta">For vendor-office setup, email is the preferred contact path. For public service help, keep using the request flow after the relevant local rule page.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Best uses</span>
                <h2>When to use this page</h2>
                <ul class="detail-list">
                    <li>Report a stale or broken authority link</li>
                    <li>Flag a mismatch between a public page and the source material</li>
                    <li>Ask about verification codes, freshness windows, or route labeling</li>
                    <li>Ask about manual setup for a vendor office using customer briefs</li>
                    <li>Raise issues with privacy or lead routing</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Fastest route</span>
                <h2>How to write a useful message</h2>
                <ul class="detail-list">
                    <li>Include the exact BackflowPath URL you are referencing.</li>
                    <li>Say whether the issue is a broken source, wrong requirement, stale timing, or unclear route labeling.</li>
                    <li>If the message is about vendor-office setup, include the utility names and whether the office is using annual notice, failed test, or both.</li>
                    <li>Include the authority URL if you think the public page is wrong.</li>
                    <li>If the issue is operational, say whether it involves privacy, lead routing, or a provider profile.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Scope boundary</span>
                <h2>What this inbox can and cannot answer</h2>
                <ul class="detail-list">
                    <li>It can answer questions about how the site classified a route or assembled a page.</li>
                    <li>It can route a manual vendor-office setup question for the customer-brief workflow.</li>
                    <li>It can process correction requests, source updates, and site-level contact issues.</li>
                    <li>It cannot override a utility rule or certify a provider.</li>
                    <li>It should not be treated as the utility's own compliance hotline.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Related paths</span>
                <h2>Where to go next</h2>
                <div class="card-actions">
                    <a class="button button--secondary" href="/for-providers">For providers</a>
                    <a class="button button--secondary" href="/pricing">Pricing</a>
                    <a class="button button--secondary" href="/claim-listing">Claim listing</a>
                    <a class="button button--secondary" href="/about">About</a>
                    <a class="button button--secondary" href="/methodology">Methodology</a>
                    <a class="button button--secondary" href="/corrections">Corrections</a>
                    <a class="button button--secondary" href="/privacy">Privacy</a>
                </div>
            </article>
        </div>
    </section>
`)
