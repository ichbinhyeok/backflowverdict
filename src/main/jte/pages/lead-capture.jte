@import owner.backflow.web.PageMeta
@param PageMeta page
@param String utilityId = ""
@param String utilityName = ""
@param String sourcePage = ""
@param String selectedIssueType = ""
@param String pageFamily = ""
@param int activeSponsorCount = 0
@param String formError = ""

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame">
            <div class="stack">
                <div class="hero__copy">
                    <span class="eyebrow kicker-rule">Form protocol 404-B</span>
                    <h1>Request local backflow help</h1>
                    <p class="lede">This request is stored in JSONL and mirrored to CSV under the external leads root so redeploys do not wipe the inbox.</p>
                    @if (utilityName != null && !utilityName.isBlank())
                        <div class="hero__stats">
                            <span>Utility: ${utilityName}</span>
                            @if (sourcePage != null && !sourcePage.isBlank())
                                <span>Source page: ${sourcePage}</span>
                            @endif
                            <span>${activeSponsorCount} active sponsor route(s)</span>
                        </div>
                    @endif
                </div>

                <div class="content-grid">
                    <article class="detail-card">
                        <span class="eyebrow">Why this form exists</span>
                        <h2>Demand capture before full sponsor delivery</h2>
                        <p>Right now the site needs to learn where demand exists, what issue types repeat, and which utility pages create real contact intent.</p>
                        <ul class="detail-list">
                            <li>Annual testing and inspection requests</li>
                            <li>Failed test repair and retest context</li>
                            <li>Irrigation and fire-line follow-up</li>
                        </ul>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">Delivery path</span>
                        <h2>What happens after submit</h2>
                        <ul class="detail-list">
                            <li>Name, phone, email, city, issue type, and notes are stored.</li>
                            <li>Utility context, page family, and referrer are preserved for routing.</li>
                            @if (activeSponsorCount > 0)
                                <li>An email delivery record is created for active sponsor coverage.</li>
                            @else
                                <li>No active sponsor is mapped, so the request stays in follow-up mode.</li>
                            @endif
                        </ul>
                    </article>
                </div>

                <article class="detail-card">
                    <div class="mini-provider">
                        <img src="/images/design/lead-testimonial.jpg" alt="Engineering supervisor portrait">
                        <div>
                            <strong>Trust through routing clarity</strong>
                            <span>The point of this form is not to blur official guidance with provider sales. It captures demand after the user has already seen the utility rule.</span>
                        </div>
                    </div>
                </article>

                <article class="detail-card">
                    <span class="eyebrow">Routing notice</span>
                    <h2>What you are consenting to</h2>
                    <p>BackflowPath stores your request details so the team can review the utility context, triage the issue, and route follow-up when sponsor coverage exists.</p>
                    <ul class="detail-list">
                        <li>The request is stored with utility context and referrer details for internal review.</li>
                        <li>BackflowPath does not resell the same request to multiple providers without your consent.</li>
                        <li>Read the full <a href="/privacy">privacy and lead routing notice</a> before submitting.</li>
                    </ul>
                </article>
            </div>

            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">Lead form</span>
                    <h2>Lead form</h2>
                    @if (activeSponsorCount > 0)
                        <p>An email delivery record will be created for active sponsor coverage after you submit this form.</p>
                    @else
                        <p>No active sponsor is mapped to this utility yet. Your request will be stored for follow-up, but it will not auto-send to a provider.</p>
                    @endif
                </div>

                @if (formError != null && !formError.isBlank())
                    <p class="form-error">${formError}</p>
                @endif

                <form method="post" action="/leads" class="stack-form">
                    <input type="hidden" name="utilityId" value="${utilityId}">
                    <input type="hidden" name="utilityName" value="${utilityName}">
                    <input type="hidden" name="sourcePage" value="${sourcePage}">
                    <input type="hidden" name="pageFamily" value="${pageFamily}">

                    <div class="field field--honeypot" aria-hidden="true">
                        <span>Company website</span>
                        <input name="companyWebsite" type="text" tabindex="-1" autocomplete="off">
                    </div>

                    <label class="field">
                        <span>Full name</span>
                        <input name="fullName" type="text" autocomplete="name" required>
                    </label>

                    <label class="field">
                        <span>Phone</span>
                        <input name="phone" type="tel" autocomplete="tel" required>
                    </label>

                    <label class="field">
                        <span>Email</span>
                        <input name="email" type="email" autocomplete="email">
                    </label>

                    <label class="field">
                        <span>City</span>
                        <input name="city" type="text" autocomplete="address-level2">
                    </label>

                    <label class="field">
                        <span>Property type</span>
                        <select name="propertyType">
                            <option value="">Select one</option>
                            <option value="residential">Residential</option>
                            <option value="commercial">Commercial</option>
                            <option value="hoa">HOA or multifamily</option>
                            <option value="restaurant-medical">Restaurant or medical</option>
                            <option value="other">Other</option>
                        </select>
                    </label>

                    <label class="field">
                        <span>What do you need?</span>
                        <select name="issueType">
                            <option value="" selected="${selectedIssueType == null || selectedIssueType.isBlank()}">Select one</option>
                            <option value="general-testing" selected="${"general-testing".equals(selectedIssueType)}">Annual or routine testing</option>
                            <option value="failed-test-repair" selected="${"failed-test-repair".equals(selectedIssueType)}">Failed test repair or retest</option>
                            <option value="irrigation" selected="${"irrigation".equals(selectedIssueType)}">Irrigation assembly testing</option>
                            <option value="fire-line" selected="${"fire-line".equals(selectedIssueType)}">Fire line testing</option>
                            <option value="tester-search" selected="${"tester-search".equals(selectedIssueType)}">Need help finding a tester</option>
                        </select>
                    </label>

                    <label class="field">
                        <span>Notes</span>
                        <textarea name="notes" rows="5" placeholder="Add the notice, deadline, assembly type, or failed-test context."></textarea>
                    </label>

                    <label class="field field--checkbox">
                        <span>Consent</span>
                        <div class="checkbox-field">
                            <input name="consentToRouting" type="checkbox" value="yes" required>
                            <p>I agree that BackflowPath may store this request, review the utility context, and contact me about routing or follow-up. I understand the request is handled under the <a href="/privacy">privacy and lead routing notice</a>.</p>
                        </div>
                    </label>

                    <div class="hero__actions">
                        <button class="button button--primary" type="submit">Save lead request</button>
                        <a class="button button--secondary" href="/">Back to site</a>
                    </div>
                </form>
            </article>
        </div>
    </section>
`)
