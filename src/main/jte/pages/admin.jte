@import owner.backflow.web.PageMeta
@import owner.backflow.service.AdminWorkspaceInspectorService.StorageFileSummary
@import owner.backflow.service.LeadInboxItem
@param PageMeta page
@param boolean authenticated = false
@param boolean adminConfigured = true
@param boolean error = false
@param String username = "admin"
@param String csrfToken = ""
@param java.util.List<LeadInboxItem> inbox = java.util.List.of()
@param int leadCount = 0
@param int assignedLeadCount = 0
@param int unassignedLeadCount = 0
@param int uncoveredLeadCount = 0
@param java.util.Map<String, Long> issueCounts = java.util.Map.of()
@param java.util.Map<String, Long> pageFamilyCounts = java.util.Map.of()
@param java.util.Map<String, Long> utilityCounts = java.util.Map.of()
@param java.util.Map<String, Long> providerCoverageGaps = java.util.Map.of()
@param int publicProviderCount = 0
@param int heldProviderCount = 0
@param String storageRoot = ""
@param java.util.List<StorageFileSummary> storageFiles = java.util.List.of()

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container">
            <span class="eyebrow">Admin</span>
            <h1>${authenticated ? "Lead control room" : "Admin login"}</h1>
            <p class="lede">${authenticated ? "Review captured requests, provider coverage gaps, and the live JSON or CSV store from one protected page." : "Use the configured admin credentials to open the private lead queue."}</p>
            @if (authenticated)
                <div class="hero__stats">
                    <span>${leadCount} leads stored</span>
                    <span>${assignedLeadCount} assigned</span>
                    <span>${unassignedLeadCount} unassigned</span>
                    <span>${uncoveredLeadCount} without provider coverage</span>
                    <span>${publicProviderCount} public provider profiles</span>
                    <span>${heldProviderCount} held provider rows</span>
                </div>
            @endif
        </div>
    </section>

    @if (!adminConfigured)
        <section class="section section--tight">
            <div class="container">
                <article class="trust-box trust-box--warning">
                    <span class="trust-box__label">Disabled</span>
                    <h2>Admin credentials are not configured</h2>
                    <p>Set APP_ADMIN_USERNAME and APP_ADMIN_PASSWORD in the runtime environment before exposing /admin.</p>
                </article>
            </div>
        </section>
    @else
        @if (!authenticated)
            <section class="section section--tight">
                <div class="container content-grid">
                    <article class="detail-card">
                        <h2>Sign in</h2>
                        @if (error)
                            <p class="form-error">Invalid username or password.</p>
                        @endif
                        <form method="post" action="/admin/login" class="stack-form">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <label class="field">
                                <span>Username</span>
                                <input name="username" type="text" value="${username}" autocomplete="username" required>
                            </label>
                            <label class="field">
                                <span>Password</span>
                                <input name="password" type="password" autocomplete="current-password" required>
                            </label>
                            <button class="button button--primary" type="submit">Sign in</button>
                        </form>
                    </article>

                    <article class="trust-box">
                        <span class="trust-box__label">Current gate</span>
                        <h2>Configured internal credentials</h2>
                        <p>This page uses a lightweight session login because the admin scope is limited to lead review, provider assignment, and export.</p>
                        <p><strong>Configured username:</strong> ${username}</p>
                    </article>
                </div>
            </section>
        @else
            <section class="section section--tight">
                <div class="container utility-grid">
                    <article class="detail-card">
                        <h2>Storage snapshot</h2>
                        <p class="detail-meta">Lead and assignment files are stored outside the build artifact and previewed here from the live storage root.</p>
                        @if (storageRoot != null && !storageRoot.isBlank())
                            <p><strong>Root:</strong> <code>${storageRoot}</code></p>
                        @endif
                        <ul class="detail-list">
                            <li>${storageFiles.size()} file(s) visible in storage</li>
                            <li>${leadCount} lead record(s) in inbox view</li>
                            <li>${assignedLeadCount} lead assignment(s) currently attached</li>
                        </ul>
                        <div class="hero__actions">
                            <a class="button button--secondary" href="/admin/export.json">Download JSON</a>
                            <a class="button button--secondary" href="/admin/export.csv">Download CSV</a>
                        </div>
                    </article>

                    <article class="detail-card">
                        <h2>Coverage watch</h2>
                        <ul class="detail-list">
                            <li>${publicProviderCount} public provider profiles</li>
                            <li>${heldProviderCount} held provider rows</li>
                            <li>${uncoveredLeadCount} lead(s) still need a matching provider</li>
                        </ul>
                        <p class="detail-meta">This page exists to review coverage gaps and assign follow-up, not to run a private sales workflow.</p>
                    </article>

                    <article class="detail-card">
                        <h2>Session</h2>
                        <form method="post" action="/admin/logout" class="inline-form">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <button class="button button--primary" type="submit">Sign out</button>
                        </form>
                    </article>
                </div>
            </section>

            <section class="section">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Breakdown</span>
                        <h2>What is entering the queue</h2>
                    </div>
                    <div class="utility-grid">
                        <article class="detail-card">
                            <h3>Issues</h3>
                            @if (issueCounts.isEmpty())
                                <p>No issue data yet.</p>
                            @else
                                <ul class="detail-list">
                                    @for (var entry : issueCounts.entrySet())
                                        <li>${entry.getKey()}: ${entry.getValue()}</li>
                                    @endfor
                                </ul>
                            @endif
                        </article>

                        <article class="detail-card">
                            <h3>Page families</h3>
                            @if (pageFamilyCounts.isEmpty())
                                <p>No page-family data yet.</p>
                            @else
                                <ul class="detail-list">
                                    @for (var entry : pageFamilyCounts.entrySet())
                                        <li>${entry.getKey()}: ${entry.getValue()}</li>
                                    @endfor
                                </ul>
                            @endif
                        </article>

                        <article class="detail-card">
                            <h3>Coverage gaps</h3>
                            @if (providerCoverageGaps.isEmpty())
                                <p>No uncovered utility queues right now.</p>
                            @else
                                <ul class="detail-list">
                                    @for (var entry : providerCoverageGaps.entrySet())
                                        <li>${entry.getKey()}: ${entry.getValue()} uncovered lead(s)</li>
                                    @endfor
                                </ul>
                            @endif
                        </article>
                    </div>

                    <div class="table-wrap">
                        <table class="data-table">
                            <thead>
                            <tr>
                                <th>Utility</th>
                                <th>Lead count</th>
                            </tr>
                            </thead>
                            <tbody>
                            @if (utilityCounts.isEmpty())
                                <tr>
                                    <td colspan="2">No utility counts yet.</td>
                                </tr>
                            @else
                                @for (var entry : utilityCounts.entrySet())
                                    <tr>
                                        <td>${entry.getKey()}</td>
                                        <td>${entry.getValue()}</td>
                                    </tr>
                                @endfor
                            @endif
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            <section class="section">
                <div class="container">
                    <div class="section-heading">
                        <span class="eyebrow">Inbox</span>
                        <h2>Most recent requests first</h2>
                    </div>
                    @if (inbox.isEmpty())
                        <article class="detail-card">
                            <h3>No leads captured yet</h3>
                            <p>Use the public lead form to capture the first request, then refresh this page.</p>
                        </article>
                    @else
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Captured</th>
                                    <th>Name</th>
                                    <th>Phone</th>
                                    <th>Utility</th>
                                    <th>Issue</th>
                                    <th>Property</th>
                                    <th>Page family</th>
                                    <th>City</th>
                                    <th>Coverage</th>
                                    <th>Assignment</th>
                                    <th>Source</th>
                                    <th>Notes</th>
                                </tr>
                                </thead>
                                <tbody>
                                @for (var item : inbox)
                                    <tr>
                                        <td>${item.lead().capturedAt().toString()}</td>
                                        <td>${item.lead().fullName()}<br><span class="detail-meta">${item.lead().email()}</span></td>
                                        <td>${item.lead().phone()}</td>
                                        <td>
                                            ${item.lead().displayUtility()}
                                            @if (!item.lead().hasTrustedUtility() && item.lead().submittedUtilityId() != null && !item.lead().submittedUtilityId().isBlank())
                                                <div class="detail-meta">Submitted utility only: ${item.lead().submittedUtilityId()}</div>
                                            @endif
                                        </td>
                                        <td>${item.lead().displayIssueType()}</td>
                                        <td>${item.lead().displayPropertyType()}</td>
                                        <td>${item.lead().displayPageFamily()}</td>
                                        <td>${item.lead().city()}</td>
                                        <td>
                                            @if (!item.lead().autoRouteEligible())
                                                <div class="detail-meta">${item.lead().displayRoutingStatus()}</div>
                                                <div class="detail-meta">${item.lead().routingReason()}</div>
                                            @endif
                                            @if (item.availableProviders().isEmpty())
                                                <span class="detail-meta">No assignable providers</span>
                                            @else
                                                ${item.availableProviders().size()} assignable
                                                <div class="detail-meta">${item.publicProviderCount()} public profile(s)</div>
                                            @endif
                                        </td>
                                        <td>
                                            @if (item.assignment() != null)
                                                <strong>${item.assignment().providerName()}</strong>
                                                <div class="detail-meta">${item.assignment().assignedAt().toString()}</div>
                                                @if (item.assignment().note() != null && !item.assignment().note().isBlank())
                                                    <div class="detail-meta">${item.assignment().note()}</div>
                                                @endif
                                            @endif
                                            @if (!item.availableProviders().isEmpty())
                                                <form method="post" action="/admin/leads/${item.lead().leadId()}/assign" class="stack-form">
                                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                                    <label class="field">
                                                        <span>Provider</span>
                                                        <select name="providerId" required>
                                                            @for (var provider : item.availableProviders())
                                                                @if (item.assignment() != null && provider.providerId().equals(item.assignment().providerId()))
                                                                    <option value="${provider.providerId()}" selected>${provider.providerName()}</option>
                                                                @else
                                                                    <option value="${provider.providerId()}">${provider.providerName()}</option>
                                                                @endif
                                                            @endfor
                                                        </select>
                                                    </label>
                                                    <label class="field">
                                                        <span>Internal note</span>
                                                        <input name="note" type="text" value="${item.assignment() != null ? item.assignment().note() : ""}" maxlength="160">
                                                    </label>
                                                    <button class="button button--secondary" type="submit">${item.assignment() != null ? "Reassign" : "Assign"}</button>
                                                </form>
                                            @endif
                                        </td>
                                        <td>
                                            ${item.lead().displaySourcePage()}
                                            @if (item.lead().sourcePage() != null && !item.lead().sourcePage().isBlank() && item.lead().submittedSourcePage() != null && !item.lead().submittedSourcePage().isBlank() && !item.lead().sourcePage().equals(item.lead().submittedSourcePage()))
                                                <div class="detail-meta">Submitted: ${item.lead().submittedSourcePage()}</div>
                                            @endif
                                            @if (item.lead().referrer() != null && !item.lead().referrer().isBlank())
                                                <div class="detail-meta">${item.lead().referrer()}</div>
                                            @endif
                                        </td>
                                        <td>${item.lead().notes()}</td>
                                    </tr>
                                @endfor
                                </tbody>
                            </table>
                        </div>
                    @endif
                </div>
            </section>
        @endif
    @endif
`)
