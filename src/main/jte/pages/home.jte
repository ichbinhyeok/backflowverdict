@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<UtilityRecord> utilities
@param java.util.List<GuideRecord> guides
@param java.util.List<GuideRecord> featuredGuides = java.util.List.of()
@param java.util.List<StateGuideRecord> stateGuides = java.util.List.of()
@param java.util.List<MetroRecord> metros = java.util.List.of()
@param java.util.List<UtilityRecord> featuredUtilities = java.util.List.of()
@param java.util.Map<String, String> testerPaths
@param java.util.Map<String, String> testerLabels
@param StateGuideRecord featuredStateGuide = null
@param int publishedUtilityCount
@param int publishedStateCount
@param int publishedMetroCount
@param int publicProviderCount

@template.layouts.app(page = page, content = @`
    <section class="hero">
        <div class="container hero__grid hero__grid--home">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Find local backflow requirements</span>
                <h1>Know your utility's backflow testing rules before you schedule the work.</h1>
                <p class="lede">BackflowPath organizes backflow requirements by utility so you can see who needs testing, how reports are filed, what happens after a failed test, and whether an official tester list exists. State, metro, and guide pages help you narrow down the right local path without mixing public guidance and provider options.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/states">Browse state guides</a>
                    <a class="button button--secondary" href="#utility-examples">See utility examples</a>
                    <a class="button button--secondary" href="/guides">Browse guides</a>
                </div>
                <div class="hero__stats">
                    <span>${publishedUtilityCount} live utility pages</span>
                    <span>${publishedStateCount} state guides</span>
                    <span>${publishedMetroCount} metro pages</span>
                    <span>${publicProviderCount} public provider profiles</span>
                </div>
            </div>
            <div class="hero__visual">
                <img src="/images/design/home-hero-industrial.jpg" alt="Industrial backflow prevention assembly in a commercial mechanical room">
                <div class="hero__visual-copy">
                    <strong>What you'll find here</strong>
                    <p>Utility pages explain due dates, testing frequency, submission steps, failed-test next steps, and tester routes. State, metro, and guide pages help you get to the right local answer faster.</p>
                </div>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">How it works</span>
                <h2>Start with the authority, then move to the next action</h2>
                <p class="lede">Every page is built to answer the next practical question: what the utility requires, whether an official tester list exists, and where to go if you still need help.</p>
            </div>
            <div class="process-grid">
                <article class="process-card">
                    <div class="process-card__index">
                        <span>01</span>
                        <span class="material-symbols-outlined">policy</span>
                    </div>
                    <h3>Find the right utility page</h3>
                    <p>Utility pages are the clearest local answer. They explain who needs testing, how often it happens, where reports go, and which follow-up pages matter.</p>
                </article>
                <article class="process-card">
                    <div class="process-card__index">
                        <span>02</span>
                        <span class="material-symbols-outlined">verified_user</span>
                    </div>
                    <h3>Open the page for your situation</h3>
                    <p>Annual testing, failed-test, irrigation, and fire-line pages sit in front of the exact question that usually comes after the main utility rule.</p>
                </article>
                <article class="process-card">
                    <div class="process-card__index">
                        <span>03</span>
                        <span class="material-symbols-outlined">monitoring</span>
                    </div>
                    <h3>Use the tester or help layer last</h3>
                    <p>Once the rule is clear, use the official tester route when it exists. If it does not, provider and help surfaces stay clearly labeled as a separate action layer.</p>
                </article>
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">Browse by state</span>
                    <h2>State guides help you narrow down the right utility</h2>
                    <p class="lede">State pages give statewide context and then route you into the utility page that actually governs the testing workflow.</p>
                </div>
                <div class="card-grid">
                    @for (var stateGuide : stateGuides)
                        <article class="panel detail-card--quiet">
                            <span class="pill">${stateGuide.state()}</span>
                            <h3>${stateGuide.title()}</h3>
                            <p>${stateGuide.description()}</p>
                            <p class="detail-meta">${stateGuide.summary()}</p>
                            <div class="card-actions">
                                <a class="button button--secondary" href="/states/${stateGuide.state()}/backflow-testing">Open state guide</a>
                            </div>
                        </article>
                    @endfor
                </div>
            </article>

            <article class="panel panel--dark">
                <div class="section-heading">
                    <span class="eyebrow" style="color: rgba(197, 234, 223, 0.92);">Popular guides</span>
                    <h2 style="color: #ffffff;">Common questions, explained clearly</h2>
                    <p>Guide pages answer recurring backflow questions, then route you back into the local utility page before you act on a rule or hire a provider.</p>
                </div>
                <div class="support-links">
                    @for (var guide : featuredGuides)
                        <a class="support-link" href="/guides/${guide.slug()}">
                            <div>
                                <strong>${guide.title()}</strong>
                                <span>${guide.description()}</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    @endfor
                </div>
            </article>
        </div>
    </section>

    @if (!metros.isEmpty())
        <section class="section">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Browse by metro</span>
                    <h2>Metro pages group nearby utilities and public provider options</h2>
                    <p class="lede">Use metro pages to compare nearby utilities and public provider profiles, then open the exact utility page before acting on a local rule.</p>
                </div>
                <div class="metric-banner">
                    <div class="metric-banner__pair">
                        <div class="metric-banner__value">${publishedMetroCount}</div>
                        <div class="metric-banner__label">live metro pages</div>
                    </div>
                    <div class="metric-banner__pair">
                        @for (var metro : metros)
                            @if (metros.indexOf(metro) < 3)
                                <span class="meta-chip">${metro.title()}</span>
                            @endif
                        @endfor
                    </div>
                </div>
            </div>
        </section>
    @endif

    <section class="section" id="utility-examples">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Popular utility pages</span>
                <h2>See how the product works at the local level</h2>
                <p class="lede">These pages show the strongest live patterns across the representative states: utility guidance first, tester routing second, and help options kept clearly separate.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : featuredUtilities)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${utility.testingFrequency()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <ul class="detail-list">
                            <li>Last verified ${utility.lastVerified().toString()}</li>
                            <li>${utility.whoIsAffected()}</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility page</a>
                        </div>
                    </article>
                @endfor
            </div>
            <div class="metric-banner metric-banner--light">
                <div class="metric-banner__pair metric-banner__pair--stacked">
                    <div class="metric-banner__value">${publishedUtilityCount}</div>
                    <div class="metric-banner__label">live utility pages</div>
                </div>
                <div class="metric-banner__pair metric-banner__pair--flow">
                    @for (var stateGuide : stateGuides)
                        <a class="meta-chip meta-chip--link" href="/states/${stateGuide.state()}/backflow-testing">${stateGuide.state()}</a>
                    @endfor
                </div>
                <div class="metric-banner__pair metric-banner__pair--actions">
                    <a class="button button--primary" href="/states">Browse state guides</a>
                    <a class="button button--secondary" href="/guides">Browse guides</a>
                </div>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="split-panels">
                <article class="split-panel split-panel--official">
                    <span class="eyebrow">What stays official</span>
                    <h3>Authority guidance comes first</h3>
                    <p>Utility rules, due dates, report portals, source excerpts, and official tester lists stay public and auditable. That public guidance is the foundation of the product.</p>
                    <ul class="detail-list">
                        <li>Official program links and submission portals</li>
                        <li>Freshness windows and verification codes</li>
                        <li>Approved tester pages only when the list is real</li>
                    </ul>
                </article>
                <article class="split-panel split-panel--commercial">
                    <span class="eyebrow">Where provider help fits</span>
                    <h3>Provider options stay clearly labeled</h3>
                    <p>Provider profiles, metro inventory, and help requests exist as a separate layer. They can help the user act, but they do not replace or blur the authority workflow.</p>
                    <ul class="detail-list">
                        <li>Public provider pages appear only where inventory is ready</li>
                        <li>Official and non-official tester routes stay clearly separated</li>
                        <li>Help requests are collected without rewriting the rule page</li>
                    </ul>
                </article>
            </div>
        </div>
    </section>

`)
