@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<UtilityRecord> utilities
@param java.util.Map<String, java.util.List<UtilityRecord>> utilitiesByState
@param java.util.Map<String, String> stateLabels
@param java.util.List<StateGuideRecord> stateGuides
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Official tester list hub</span>
                <h1>Official backflow tester lists by utility.</h1>
                <p class="lede">This hub collects utility routes where the governing authority publishes an approved, registered, or certified tester list. It is intentionally separate from non-official provider directories.</p>
                <div class="hero__actions">
                    @if (!utilities.isEmpty())
                        <a class="button button--primary" href="/utilities/${utilities.get(0).state()}/${utilities.get(0).canonicalSlug()}/approved-testers">Open a tester list</a>
                    @endif
                    <a class="button button--secondary" href="/backflow-reporting-portals">Reporting portals</a>
                    <a class="button button--secondary" href="/guides/approved-testers-vs-find-a-tester">Approved vs directory</a>
                </div>
                <div class="hero__stats">
                    <span>${utilities.size()} official tester routes</span>
                    <span>${utilitiesByState.size()} states represented</span>
                    <span>Directory pages stay separate</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Route rule</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">Official source first</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">A route appears here only when the utility record points to a published authority list or equivalent official lookup.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">By state</span>
                <h2>Choose the state, then open the exact utility list</h2>
                <p class="lede">Tester approval is local. Start with the state group, then confirm the actual list on the governing utility page.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var entry : utilitiesByState.entrySet())
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${entry.getKey()}</span>
                        <h3>${stateLabels.getOrDefault(entry.getKey(), entry.getKey())} official tester lists</h3>
                        <p>${entry.getValue().size()} utility routes with official tester-list support.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/states/${entry.getKey()}/approved-backflow-testers">Open state tester hub</a>
                            <a class="button button--secondary" href="/states/${entry.getKey()}/backflow-testing">State guide</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container split-panels">
            <article class="detail-card">
                <span class="eyebrow">Utility routes</span>
                <h2>Live official tester list pages</h2>
                <div class="support-links">
                    @for (var utility : utilities)
                        <a class="support-link" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/approved-testers">
                            <div>
                                <strong>${utility.utilityName()}</strong>
                                <span>${utility.officialListLabel()}</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    @endfor
                </div>
            </article>

            <article class="panel panel--dark">
                <span class="eyebrow" style="color: rgba(141, 224, 196, 0.9);">How to read the list</span>
                <h2 style="color: #ffffff;">Approval is not universal</h2>
                <p>A tester can be valid for one utility list and not shown for another. Open the specific utility route before treating a provider as approved for a property.</p>
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
