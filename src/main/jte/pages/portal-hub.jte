@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param String portalName
@param String portalSlug
@param String intro
@param boolean overview
@param java.util.List<UtilityRecord> utilities
@param java.util.Map<String, Integer> portalCounts
@param java.util.Map<String, java.util.List<CityAliasRecord>> cityAliasesByUtility = java.util.Map.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Reporting portal hub</span>
                <h1>${portalName} for backflow testing.</h1>
                <p class="lede">${intro}</p>
                <div class="hero__actions">
                    @if (!utilities.isEmpty())
                        <a class="button button--primary" href="/utilities/${utilities.get(0).state()}/${utilities.get(0).canonicalSlug()}/">Open a utility workflow</a>
                    @endif
                    <a class="button button--secondary" href="/guides/backflow-reporting-portals">Portal guide</a>
                    <a class="button button--secondary" href="/official-backflow-tester-lists">Official tester lists</a>
                </div>
                <div class="hero__stats">
                    <span>${utilities.size()} mapped utility workflows</span>
                    <span>${portalSlug.equals("all") ? "BSI, WEIRS, SwiftComply, and VEPO families" : portalName + " utility examples"}</span>
                    <span>Reports follow utility rules</span>
                    <span>Tester action comes after the local page</span>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Portal context</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">${portalSlug.equals("all") ? "BSI / WEIRS / SwiftComply / VEPO" : portalName}</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">Portal names are discovery signals. The local utility page still controls deadlines, tester status, fees, and report acceptance.</p>
            </aside>
        </div>
    </section>

    @if (overview)
        <section class="section">
            <div class="container">
                <div class="section-heading">
                    <span class="eyebrow">Portal families</span>
                    <h2>Open the portal family, then confirm the utility workflow</h2>
                    <p class="lede">These pages group the strongest recurring portal names without pretending the portal itself replaces the local rule.</p>
                </div>
                <div class="card-grid">
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${portalCounts.getOrDefault("bsi", 0)} utilities</span>
                        <h3>BSI backflow reporting</h3>
                        <p>Utilities where BSI or Backflow Solutions appears in tester or report submission context.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/bsi">Open BSI utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${portalCounts.getOrDefault("weirs", 0)} utilities</span>
                        <h3>WEIRS tester and reporting routes</h3>
                        <p>Utilities where WEIRS appears in the official tester lookup or compliance workflow.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/weirs">Open WEIRS utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${portalCounts.getOrDefault("swiftcomply", 0)} utilities</span>
                        <h3>SwiftComply and C3Swift reporting</h3>
                        <p>Utilities where SwiftComply or C3Swift appears in the official report submission path.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/swiftcomply">Open SwiftComply utilities</a>
                        </div>
                    </article>
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${portalCounts.getOrDefault("vepo", 0)} utilities</span>
                        <h3>VEPO Envirotrax reporting</h3>
                        <p>Utilities where VEPO or Envirotrax appears in tester registration, credential review, or report submission context.</p>
                        <div class="card-actions">
                            <a class="button button--secondary" href="/backflow-reporting-portals/vepo">Open VEPO utilities</a>
                        </div>
                    </article>
                </div>
            </div>
        </section>
    @endif

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">Owner checklist</span>
                <h2>Match the notice before opening a portal</h2>
                <ul class="detail-list">
                    <li>Confirm the exact utility, service address, assembly record, and due date.</li>
                    <li>Keep any notice number, account number, CCN, or portal reference available for the tester.</li>
                    <li>Use the utility page to verify whether a filing fee, pass-only fee, or failed-test repair window applies.</li>
                </ul>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Tester checklist</span>
                <h2>Portal access still depends on accepted credentials</h2>
                <ul class="detail-list">
                    <li>Create or recover the tester account before the due date.</li>
                    <li>Upload current tester certification, license, insurance, or gauge calibration documents when required.</li>
                    <li>Submit through the named portal and confirm whether failed tests, passed tests, replacements, or removals have different handling.</li>
                </ul>
            </article>
        </div>
    </section>

    @if (!utilities.isEmpty())
        <section class="section">
            <div class="container">
                <article class="table-card">
                    <div class="section-heading">
                        <span class="eyebrow">Portal comparison</span>
                        <h2>${portalSlug.equals("all") ? "Compare portal workflows before choosing the next route" : "Compare " + portalName + " utility workflows"}</h2>
                        <p>Use this table to spot the acceptance gates that usually decide whether a backflow report is actually complete: portal submission, approved tester status, due basis, and failed-test handling.</p>
                    </div>
                    <div class="table-wrap">
                        <table class="data-table data-table--responsive">
                            <thead>
                            <tr>
                                <th>Utility and city</th>
                                <th>Portal evidence</th>
                                <th>Tester gate</th>
                                <th>Timing or fee clue</th>
                                <th>Failed-test clue</th>
                            </tr>
                            </thead>
                            <tbody>
                            @for (var utility : utilities)
                                <tr>
                                    <td data-label="Utility and city">
                                        <strong>${utility.utilityName()}</strong>
                                        @if (!cityAliasesByUtility.getOrDefault(utility.utilityId(), java.util.List.of()).isEmpty())
                                            <div class="detail-meta">
                                                @for (var alias : cityAliasesByUtility.getOrDefault(utility.utilityId(), java.util.List.of()))
                                                    <a href="/cities/${alias.state()}/${alias.aliasSlug()}/backflow-testing">${alias.city()}</a>
                                                @endfor
                                            </div>
                                        @else
                                            <div class="detail-meta">${utility.state()}</div>
                                        @endif
                                    </td>
                                    <td data-label="Portal evidence">
                                        @for (var method : utility.submissionMethods())
                                            <div>${method.label()}</div>
                                        @endfor
                                    </td>
                                    <td data-label="Tester gate">
                                        @if (utility.supportsApprovedTestersPage())
                                            <strong>${utility.officialListLabel()}</strong>
                                        @else
                                            <span>Confirm accepted tester status with the utility.</span>
                                        @endif
                                    </td>
                                    <td data-label="Timing or fee clue">
                                        <span>${utility.testingFrequency()}</span>
                                        @if (utility.costBand() != null && utility.costBand().pricingNotes() != null && !utility.costBand().pricingNotes().isBlank())
                                            <div class="detail-meta">${utility.costBand().pricingNotes()}</div>
                                        @endif
                                    </td>
                                    <td data-label="Failed-test clue">
                                        @if (!utility.failureHighlights().isEmpty())
                                            <span>${utility.failureHighlights().get(0)}</span>
                                        @else
                                            <span>Open the utility workflow before repair, retest, or resubmission.</span>
                                        @endif
                                    </td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                </article>
            </div>
        </section>
    @endif

    <section class="section">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Mapped utilities</span>
                <h2>${portalSlug.equals("all") ? "Utility workflows that mention a portal or online submission route" : portalName + " backflow portal examples by utility"}</h2>
                <p class="lede">Open the utility page first. It contains the due basis, submission method, program phone, and any tester-list route tied to the property.</p>
            </div>
            <div class="card-grid card-grid--three">
                @for (var utility : utilities)
                    <article class="detail-card detail-card--quiet">
                        <span class="pill">${utility.state()}</span>
                        <h3>${utility.utilityName()}</h3>
                        <p>${utility.verdictSummary()}</p>
                        <ul class="detail-list">
                            @for (var method : utility.submissionMethods())
                                <li>${method.label()}</li>
                            @endfor
                        </ul>
                        @if (!cityAliasesByUtility.getOrDefault(utility.utilityId(), java.util.List.of()).isEmpty())
                            <div class="support-links">
                                @for (var alias : cityAliasesByUtility.getOrDefault(utility.utilityId(), java.util.List.of()))
                                    <a class="support-link" href="/cities/${alias.state()}/${alias.aliasSlug()}/backflow-testing">
                                        <div>
                                            <strong>${alias.city()} backflow testing</strong>
                                            <span>${utility.supportsApprovedTestersPage() ? "Tester-list and portal route" : "Portal and utility workflow route"}</span>
                                        </div>
                                        <span class="material-symbols-outlined">arrow_forward</span>
                                    </a>
                                @endfor
                            </div>
                        @endif
                        <div class="card-actions">
                            <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility workflow</a>
                            @if (utility.supportsAnnualTestingPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/annual-testing">Annual testing</a>
                            @endif
                            @if (utility.supportsApprovedTestersPage())
                                <a class="button button--secondary" href="/utilities/${utility.state()}/${utility.canonicalSlug()}/approved-testers">Tester list</a>
                            @endif
                        </div>
                    </article>
                @endfor
            </div>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="trust-box">
                <span class="trust-box__label">Submission rule</span>
                <h2>Portal access does not decide tester approval by itself</h2>
                <p>A reporting portal may handle test submission while the utility, county, or separate list controls tester eligibility. Treat the utility page as the canonical workflow before scheduling or submitting a report.</p>
            </article>

            <article class="detail-card">
                <span class="eyebrow">Related reading</span>
                <h2>Guides that explain the workflow</h2>
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
