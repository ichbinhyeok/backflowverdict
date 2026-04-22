@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Editorial standards</span>
                <h1>Writing rules for a source-backed compliance site</h1>
                <p class="lede">BackflowPath is not supposed to sound like a generic SEO education site. The public copy should clarify the local rule, preserve the official source, and keep provider-directory language visibly separate from utility authority. Editorial quality on this site is defined by trust, legibility, and restraint, not by word count alone.</p>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Core rule</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Do not blur the authority layer</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">The site can help users move faster, but it cannot pretend to be the utility, invent approval, or quietly reshape the official workflow for conversion.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Language</span>
                <h2>Use searcher language, not internal shorthand</h2>
                <p>Headlines and summaries should describe what the user is trying to do: annual testing, approved testers, failed test next steps, reporting portal, or penalties. Internal categorization terms should stay out of public titles unless they match how people actually search.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Claims</span>
                <h2>Never overstate authority status</h2>
                <p>BackflowPath should only call a tester approved, certified, or official when the governing authority itself uses that status. If the site is showing a discovery path instead, it must be labeled as non-official or directory-only.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Support actions</span>
                <h2>Provider routing is downstream</h2>
                <p>Request help and provider profiles can exist, but they should appear after the official workflow and source block. They are support actions, not the definition of the rule.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Title and summary rules</span>
                <h2>What a page should sound like</h2>
                <ul class="detail-list">
                    <li>Use the utility or authority name in the main title when that is the governing entity.</li>
                    <li>Lead with the actual user task: annual testing, approved testers, failed test next steps, fire line, irrigation, reporting portal, or due date.</li>
                    <li>Keep summaries concrete enough that a searcher can tell what the page covers before clicking.</li>
                    <li>Avoid internal taxonomy language unless it matches common search behavior.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Source usage</span>
                <h2>How sources should appear in the copy</h2>
                <p>The site should not bury the source block at the bottom and then ask the user to convert. The official route, source excerpt, submission path, and contact channel should be visible early enough that a cautious user can verify the page before taking the next action.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Required page elements</span>
                <h2>What every strong local page should include</h2>
                <ul class="detail-list">
                    <li>Last verified date and verification code</li>
                    <li>Official source URLs and source excerpt</li>
                    <li>Submission method and utility contact path</li>
                    <li>Tester routing labeled by authority status</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Failure conditions</span>
                <h2>When a page should not stay indexed</h2>
                <ul class="detail-list">
                    <li>The source can no longer be verified.</li>
                    <li>The page no longer adds utility-specific value.</li>
                    <li>The route is a thin bridge that should be canonicalized elsewhere.</li>
                    <li>The page implies authority or approval it cannot support.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card">
                <span class="eyebrow">Support content rule</span>
                <h2>How guides should behave</h2>
                <p>Guides can explain repeated patterns such as anniversary dates, reporting portals, or tester classifications, but they should still route readers back to the exact utility page before a practical decision is made. Support content is a translation layer, not a replacement layer.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Provider content rule</span>
                <h2>How provider pages should behave</h2>
                <p>Provider profiles should read like inspectable public records, not promotional landers. They need real entity signals, route classification, and explicit separation from official program status.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Editorial posture</span>
                <h2>Preferred failure mode</h2>
                <p>When the tradeoff is between being broader and being clearer, BackflowPath should choose clarity. When the tradeoff is between indexing a questionable page and waiting for stronger source support, it should choose patience.</p>
            </article>
        </div>
    </section>
`)
