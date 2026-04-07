@import owner.backflow.web.PageMeta
@import owner.backflow.config.SiteRuntimeConfig

@param PageMeta page
@param String selectedRequestType = ""
@param String formError = ""

@template.layouts.app(page = page, content = @`
    <section class="page-hero page-hero--claim">
        <div class="container page-frame page-frame--claim">
            <div class="stack stack--claim">
                <div class="hero__copy">
                    <span class="eyebrow kicker-rule">Claim listing</span>
                    <h1>Claim or request a provider listing</h1>
                    <p class="lede">Use this form to claim an existing profile, request a new listing, or ask about featured placement. BackflowPath reviews the request first and replies by email with the next step.</p>
                    <div class="hero__stats">
                        <span>Manual review before publication</span>
                        <span>Manual review before billing</span>
                        <span>Email follow-up before any featured payment step</span>
                    </div>
                </div>

                <div class="content-grid content-grid--claim">
                    <article class="detail-card">
                        <span class="eyebrow">How claims work</span>
                        <h2>No automatic registration</h2>
                        <ul class="detail-list">
                            <li>BackflowPath checks public company details, service area, and route fit first.</li>
                            <li>Claiming a listing does not auto-publish edits or featured placement.</li>
                            <li>If featured placement makes sense, BackflowPath sends the payment step later by email.</li>
                            <li>Provider requests stay separate from official tester-list claims.</li>
                        </ul>
                    </article>

                    <article class="detail-card">
                        <span class="eyebrow">What helps most</span>
                        <h2>Useful details to include</h2>
                        <ul class="detail-list">
                            <li>Your company website and the metros or utilities you actually serve</li>
                            <li>The BackflowPath profile URL if one already exists</li>
                            <li>Whether you want a free claim review, a new listing, or featured placement</li>
                        </ul>
                    </article>
                </div>
            </div>

            <article class="detail-card claim-form-card sticky-rail">
                <div class="section-heading">
                    <span class="eyebrow">Request form</span>
                    <h2>Tell BackflowPath what you need</h2>
                    <p>This request is reviewed manually. If the fit is clear, BackflowPath can update an existing listing, create a new profile, or reply by email with featured placement next steps.</p>
                </div>

                @if (formError != null && !formError.isBlank())
                    <p class="form-error">${formError}</p>
                @endif

                <form method="post" action="/claim-listing" class="stack-form claim-form-grid">
                    <div class="field field--honeypot" aria-hidden="true">
                        <span>Office fax</span>
                        <input name="officeFax" type="text" tabindex="-1" autocomplete="off">
                    </div>

                    <label class="field">
                        <span>Full name</span>
                        <input name="fullName" type="text" autocomplete="name" required>
                    </label>

                    <label class="field">
                        <span>Company name</span>
                        <input name="companyName" type="text" autocomplete="organization" required>
                    </label>

                    <label class="field">
                        <span>Email</span>
                        <input name="email" type="email" autocomplete="email" required>
                    </label>

                    <label class="field">
                        <span>Phone</span>
                        <input name="phone" type="tel" autocomplete="tel">
                    </label>

                    <label class="field">
                        <span>Website</span>
                        <input name="website" type="url" placeholder="https://example.com">
                    </label>

                    <label class="field">
                        <span>Primary service area</span>
                        <input name="serviceArea" type="text" placeholder="Example: Dallas-Fort Worth, Austin, Phoenix metro">
                    </label>

                    <label class="field">
                        <span>What do you need?</span>
                        <select name="requestType">
                            <option value="" selected="${selectedRequestType == null || selectedRequestType.isBlank()}">Select one</option>
                            <option value="claim-existing-profile" selected="${"claim-existing-profile".equals(selectedRequestType)}">Claim an existing profile</option>
                            <option value="request-new-profile" selected="${"request-new-profile".equals(selectedRequestType)}">Request a new listing</option>
                            <option value="featured-utility" selected="${"featured-utility".equals(selectedRequestType)}">Ask about featured utility placement</option>
                            <option value="featured-metro" selected="${"featured-metro".equals(selectedRequestType)}">Ask about featured metro placement</option>
                            <option value="pricing-question" selected="${"pricing-question".equals(selectedRequestType)}">General pricing question</option>
                        </select>
                    </label>

                    <label class="field field--span-2">
                        <span>Existing BackflowPath URL or listing reference</span>
                        <input name="listingReference" type="text" placeholder="Paste the BackflowPath URL if a profile already exists">
                    </label>

                    <label class="field field--span-2">
                        <span>Notes</span>
                        <textarea name="notes" rows="4" placeholder="Share the utilities or metros you want covered, what should be corrected, or what plan you want to discuss."></textarea>
                    </label>

                    <label class="field field--checkbox field--span-2">
                        <span>Consent</span>
                        <div class="checkbox-field">
                            <input name="consentToReview" type="checkbox" value="yes" required>
                            <p>I agree that BackflowPath may store and review this provider request, contact me about listing details or placement, and handle the request under the <a href="/privacy">privacy and lead routing notice</a>.</p>
                        </div>
                    </label>

                    <div class="hero__actions field--span-2">
                        <button class="button button--primary" type="submit">Send request for review</button>
                        <a class="button button--secondary" href="/pricing">View pricing</a>
                    </div>
                </form>

                @if (SiteRuntimeConfig.supportEmail() != null && !SiteRuntimeConfig.supportEmail().isBlank())
                    <p class="detail-meta" style="margin-top: 1rem;">Prefer email? Send provider questions to <a href="mailto:${SiteRuntimeConfig.supportEmail()}">${SiteRuntimeConfig.supportEmail()}</a>. This is also the reply path BackflowPath uses for manual follow-up.</p>
                @endif
            </article>
        </div>
    </section>
`)
