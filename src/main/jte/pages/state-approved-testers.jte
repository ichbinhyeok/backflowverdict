@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param StateGuideRecord stateGuide
@param String stateLabel
@param java.util.List<UtilityRecord> utilities
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">${stateLabel} tester routes</span>
                <h1>${stateLabel} approved backflow tester lists by utility.</h1>
                <p class="lede">Use this page to find ${stateLabel} utility routes where BackflowVerdict has a published official tester-list path. The governing utility page remains the route that decides the list and reporting workflow.</p>
                <div class="hero__actions">
                    @if (!utilities.isEmpty())
                        <a class="button button--primary" href="/utilities/${utilities.get(0).state()}/${utilities.get(0).canonicalSlug()}/approved-testers">Open a tester list</a>
                    @endif
                    <a class="button button--secondary" href="/states/${stateGuide.state()}/backflow-testing">Back to state guide</a>
                    <a class="button button--secondary" href="/official-backflow-tester-lists">All official lists</a>
                </div>
                <div class="hero__stats">
                    <span>${utilities.size()} official tester routes</span>
                    <span>State guide: ${stateGuide.title()}</span>
                    <span>Local utility rules stay canonical</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">State context</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">${stateLabel}</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">${stateGuide.summary()}</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Official routes</span>
                <h2>Open the utility list before choosing a tester</h2>
                <p class="lede">Each route below points to a utility page that separates the official authority list from non-official provider discovery.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : utilities)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${utility.state()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.officialListLabel()}</p>
                        <ul class="detail-list">
                            <li>Last verified ${utility.lastVerified().toString()}</li>
                            <li>${utility.testingFrequency()}</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/approved-testers">Open official tester list</a>
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Utility rules</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">Approval boundary</span>
                <h2>Statewide certification is not the same as utility approval</h2>
                <p>Many searches mix certified, registered, and approved tester language. BackflowVerdict keeps these routes tied to the actual utility list or portal source shown on the local page.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Related reading</span>
                <h2>Guides that clarify tester status</h2>
                <div class="support-links">
                    @for (var guide : relatedGuides)
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
`)
