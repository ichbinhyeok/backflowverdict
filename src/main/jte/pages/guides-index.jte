@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<GuideRecord> guides
@param java.util.List<UtilityRecord> featuredUtilities
@param java.util.List<StateGuideRecord> stateGuides
@param int publishedGuideCount

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Browse guides</span>
                <h1>Answer common backflow questions without losing the local rule.</h1>
                <p class="lede">Guides explain recurring patterns such as reporting portals, tester lists, due-date logic, and residential versus commercial differences. They help users understand the issue, then route back into the exact utility page before they act.</p>
                <div class="hero__actions">
                    @if (!guides.isEmpty())
                        <a class="button button--primary" href="/guides/${guides.get(0).slug()}">Open a guide</a>
                    @endif
                    <a class="button button--secondary" href="/states">Browse state guides</a>
                    <a class="button button--secondary" href="/leads/new">Request help</a>
                </div>
                <div class="hero__stats">
                    <span>${publishedGuideCount} published guides</span>
                    <span>${stateGuides.size()} state guides</span>
                    <span>Utility pages stay canonical</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">When guides are useful</span>
                <div class="stat-grid">
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Good for</strong>
                        <span style="color: #ffffff;">Repeated questions</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Not for</strong>
                        <span style="color: #ffffff;">Replacing local authority rules</span>
                    </div>
                    <div class="metadata-block" style="background: rgba(255, 255, 255, 0.08);">
                        <strong style="color: rgba(141, 224, 196, 0.86);">Best next step</strong>
                        <span style="color: #ffffff;">Open the mapped utility page</span>
                    </div>
                </div>
                <p class="detail-meta" style="color: rgba(239, 241, 243, 0.74);">Guides reduce confusion, but the page that governs action is still the utility page tied to the property.</p>
            </aside>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Published guides</span>
                <h2>Read the pattern, then return to the local workflow</h2>
                <p class="lede">These guides explain the recurring questions people ask before they schedule a test, submit a report, or figure out whether an approved list exists.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var guide : guides)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">Guide</span>
                        <h3>${guide.title()}</h3>
                        <p>${guide.description()}</p>
                        <ul class="detail-list">
                            <li>Last reviewed ${guide.lastReviewed().toString()}</li>
                            <li>${guide.reviewerInitials()} reviewer initials</li>
                        </ul>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/guides/${guide.slug()}">Open guide</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">Support layer</span>
                <h2>Useful explanations without generic plumbing drift</h2>
                <p>These guides exist to make repeated compliance questions easier to understand. They are intentionally tied back to utility pages, state guides, and local action pages.</p>
            </article>

            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">Keep the local path in view</span>
                    <h2>Guides work best when they lead back to the authority page</h2>
                    <p>Use a guide to understand the pattern, then open the mapped utility page or state guide before acting on a deadline, tester rule, or submission requirement.</p>
                </div>
                <div class="support-links">
                    @for (var stateGuide : stateGuides)
                        <a class="support-link" href="/states/${stateGuide.state()}/backflow-testing">
                            <div>
                                <strong>${stateGuide.title()}</strong>
                                <span>${stateGuide.description()}</span>
                            </div>
                            <span class="material-symbols-outlined">arrow_forward</span>
                        </a>
                    @endfor
                </div>
            </article>
        </div>
    </section>

    <section class="section section--muted">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Featured utilities</span>
                <h2>Local utility pages these guides are meant to support</h2>
                <p class="lede">These examples show the level of local specificity the guides are designed to reinforce rather than replace.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : featuredUtilities)
                    <article class="detail-card">
                        <span class="pill">${utility.state()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility page</a>
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>
`)
