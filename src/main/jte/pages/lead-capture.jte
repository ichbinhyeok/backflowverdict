@import owner.backflow.web.PageMeta
@param PageMeta page
@param String utilityId = ""
@param String utilityName = ""
@param String sourcePage = ""
@param String selectedIssueType = ""
@param String pageFamily = ""
@param String routingToken = ""
@param int activeSponsorCount = 0
@param String formError = ""

@template.layouts.app(page = page, content = @`
    <section class="page-hero">
        <div class="container page-frame">
            <div class="stack">
                <div class="hero__copy">
                    <span class="eyebrow kicker-rule">Request help</span>
                    <h1>Request local backflow help</h1>
                    <p class="lede">Share your utility, deadline, failed-test details, or tester-search question and BackflowPath will review the request against the local workflow before following up.</p>
                    @if (utilityName != null && !utilityName.isBlank())
                        <div class="hero__stats">
                            <span>Utility: ${utilityName}</span>
                            <span>Use this form for testing, repair, retest, irrigation, or fire-line questions</span>
                            <span>Official guidance stays separate from provider help</span>
                        </div>
                    @endif
                </div>

                <div class="content-grid">
                    <article class="detail-card">
                        <span class="eyebrow">Why this form helps</span>
                        <h2>Get the right next step</h2>
                        <p>Backflow rules change by utility. This form gives BackflowPath the details needed to review the local rule, understand the issue, and point you toward the next practical step.</p>
                        <ul class="detail-list">
                            <li>Annual testing and inspection questions</li>
                            <li>Failed test repair and retest context</li>
                            <li>Irrigation, fire-line, or tester-search follow-up</li>
                        </ul>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">What happens next</span>
                        <h2>Reviewed against the utility workflow</h2>
                        <ul class="detail-list">
                            <li>BackflowPath reviews the utility, issue type, and notes together.</li>
                            <li>If more detail is needed, follow-up can clarify the deadline, device type, or reporting path.</li>
                            <li>If the utility context is verified and provider help fits the situation, the request can be shared with one or more active local sponsors that cover that utility.</li>
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
                    <p>BackflowPath keeps this request so the team can review the utility context, respond about next steps, and share the request with local sponsors only when the utility routing context is verified and the form consent covers sponsor routing.</p>
                    <ul class="detail-list">
                        <li>Your request is reviewed alongside the local utility context attached to the lead link or saved request.</li>
                        <li>If routing applies, the request may be shared with one or more active sponsors that cover the verified utility.</li>
                        <li>If the routing metadata cannot be verified, BackflowPath stores the request for manual review and does not auto-route it to sponsors.</li>
                        <li>Read the full <a href="/privacy">privacy and lead routing notice</a> before submitting.</li>
                    </ul>
                </article>
            </div>

            <article class="detail-card">
                <div class="section-heading">
                    <span class="eyebrow">Request details</span>
                    <h2>Tell us what is going on</h2>
                    <p>Include the notice, due date, failed-test issue, or utility name if you have it. The more specific the details, the easier it is to review the next step.</p>
                </div>

                @if (formError != null && !formError.isBlank())
                    <p class="form-error">${formError}</p>
                @endif

                <form method="post" action="/leads" class="stack-form">
                    <input type="hidden" name="utilityId" value="${utilityId}">
                    <input type="hidden" name="utilityName" value="${utilityName}">
                    <input type="hidden" name="sourcePage" value="${sourcePage}">
                    <input type="hidden" name="pageFamily" value="${pageFamily}">
                    <input type="hidden" name="rt" value="${routingToken}">

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
                            <p>I agree that BackflowPath may store this request, review the local utility context, and, when that routing context is verified, share the request with one or more active sponsors that cover the utility. I understand the request is handled under the <a href="/privacy">privacy and lead routing notice</a>.</p>
                        </div>
                    </label>

                    <div class="hero__actions">
                        <button class="button button--primary" type="submit">Request local help</button>
                        <a class="button button--secondary" href="/">Back to site</a>
                    </div>
                </form>
            </article>
        </div>
    </section>
`)
