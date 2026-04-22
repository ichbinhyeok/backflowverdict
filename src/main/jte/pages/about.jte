@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">About</span>
                <h1>What BackflowPath is built to do</h1>
                <p class="lede">BackflowPath organizes backflow compliance by utility so a property owner, facility manager, HOA, contractor, or service coordinator can find the governing authority, the testing cadence, the submission path, and the right next action without confusing that workflow with provider marketing.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/methodology">Read the methodology</a>
                    <a class="button button--secondary" href="/editorial-standards">Read editorial standards</a>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Operating stance</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Authority first</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">Official utility guidance stays primary. Provider profiles and request help are separate support layers and should never replace the governing rule.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container card-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Canonical entity</span>
                <h2>Utility pages are the core record</h2>
                <p>People often search by city, county, or metro, but the enforceable rule usually lives under a utility, district, or water authority. BackflowPath treats that governing entity as the canonical page and keeps other discovery routes subordinate to it.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Support layers</span>
                <h2>State, metro, guide, and provider pages</h2>
                <p>State guides, metro pages, and support guides exist to narrow down the correct utility workflow. Provider pages exist to make public directory information inspectable, not to imply utility approval where none is published.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Trust boundary</span>
                <h2>Official guidance and public provider browse stay separate</h2>
                <p>If a utility publishes an official tester list, BackflowPath keeps that route distinct from any non-official directory. If no official list exists, provider discovery paths stay labeled as non-official.</p>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Who this is for</span>
                <h2>Users who need the local rule fast</h2>
                <p>The site is built for people who already have a practical compliance question and need the shortest safe path to the answer.</p>
                <ul class="detail-list">
                    <li>Property owners who received a testing notice and need to confirm the exact requirement</li>
                    <li>Operations teams or facility managers coordinating annual tests across multiple addresses</li>
                    <li>Contractors trying to confirm whether a tester list is official, county-based, or utility-specific</li>
                    <li>People dealing with failed tests, due dates, penalties, or reporting portals</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">What the site is not</span>
                <h2>What BackflowPath should not pretend to be</h2>
                <ul class="detail-list">
                    <li>It is not the utility, district, or governing water authority.</li>
                    <li>It is not a legal opinion about local ordinance enforcement.</li>
                    <li>It is not a universal tester approval database.</li>
                    <li>It is not a marketplace where directory results rewrite the official workflow.</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">What appears on-page</span>
                <h2>Public evidence signals</h2>
                <ul class="detail-list">
                    <li>Last verified or last reviewed date</li>
                    <li>Verification code tied to the registry review log</li>
                    <li>Official source links and source excerpts</li>
                    <li>Submission methods, program phone, and authority workflow where available</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">What does not happen</span>
                <h2>What BackflowPath avoids</h2>
                <ul class="detail-list">
                    <li>Inventing approval or certification status for a tester</li>
                    <li>Mixing directory placement into the official source block</li>
                    <li>Keeping stale pages indexed after source evidence breaks or ages out</li>
                    <li>Replacing the utility's own rule page with generic educational copy</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">How to use the site</span>
                <h2>Recommended reading order</h2>
                <ol class="step-list">
                    <li>Start on the utility page if you know the governing authority. If you only know the city or market, start on a state or metro page and click into the utility record.</li>
                    <li>Read the official workflow, source block, submission method, and program phone before taking any provider action.</li>
                    <li>If the utility publishes an official tester list, use that route first. If no official list exists, treat any provider route as non-official discovery only.</li>
                    <li>If the rule looks stale, broken, or inconsistent with a current authority source, use the corrections path instead of guessing.</li>
                </ol>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Why these pages exist</span>
                <h2>The product shape behind the site</h2>
                <p>Backflow compliance is highly fragmented. Searchers often know the city, neighborhood, or contractor problem, but the enforceable answer lives inside a utility program page, PDF, or portal with inconsistent labeling. BackflowPath exists to compress that lookup work while keeping the trust boundary visible.</p>
                <p class="detail-meta">The goal is not to replace the authority page. The goal is to make the authority page easier to find, interpret, and act on.</p>
            </article>
        </div>
    </section>

    <section class="section section--muted">
        <div class="container card-grid">
            <article class="detail-card">
                <span class="eyebrow">Scope decisions</span>
                <h2>Why some pages are indexed and others are not</h2>
                <p>BackflowPath does not treat every generated route as worthy of indexing. Thin city bridges, stale records, or pages that fail to add real local compliance value should be canonicalized, noindexed, or removed until they earn a stronger public record.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Provider policy</span>
                <h2>How provider help fits</h2>
                <p>Provider profiles and request help can help users act, but they should never become the only visible path to the answer. The utility program page, source evidence, and submission path stay public and auditable even when a directory layer exists downstream.</p>
            </article>
        </div>
    </section>

    <section class="section section--muted">
        <div class="container">
            <div class="detail-card">
                <span class="eyebrow">Next reads</span>
                <h2>How the review system works</h2>
                <div class="card-actions">
                    <a class="button button--secondary" href="/methodology">Methodology</a>
                    <a class="button button--secondary" href="/editorial-standards">Editorial standards</a>
                    <a class="button button--secondary" href="/corrections">Corrections policy</a>
                    <a class="button button--secondary" href="/contact">Contact</a>
                </div>
            </div>
        </div>
    </section>
`)
