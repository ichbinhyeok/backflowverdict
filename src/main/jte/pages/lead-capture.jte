@import owner.backflow.web.PageMeta
@param PageMeta page
@param String utilityId = ""
@param String utilityName = ""
@param String sourcePage = ""
@param String selectedIssueType = ""
@param String pageFamily = ""
@param String routingToken = ""
@param String formError = ""

@template.layouts.verdict(page = page, content = @`
    <section class="bv-form-hero">
        <div class="bv-shell">
            <p class="bv-kicker">Noindex service handoff</p>
            <h1>Get help with the next step</h1>
            <p class="bv-lede">Send the utility, device, and task together so the request does not lose the rule context.</p>
            @if (utilityName != null && !utilityName.isBlank())
                <p class="bv-context-line"><strong>Utility in context</strong><span>${utilityName}</span></p>
            @endif
        </div>
    </section>
    <section class="bv-section bv-section--paper">
        <div class="bv-shell bv-form-grid">
            <div class="bv-form-aside">
                <p class="bv-kicker">What happens</p>
                <ol><li><span>01</span>We keep the utility and source page with the request.</li><li><span>02</span>The issue is reviewed against the official workflow.</li><li><span>03</span>If provider help fits, follow-up can use that context.</li></ol>
                <p class="bv-form-note">An official approved-tester list, when available, should be used before a general provider route.</p>
            </div>
            <form method="post" action="/leads" class="bv-form">
                <input type="hidden" name="utilityId" value="${utilityId}">
                <input type="hidden" name="utilityName" value="${utilityName}">
                <input type="hidden" name="sourcePage" value="${sourcePage}">
                <input type="hidden" name="pageFamily" value="${pageFamily}">
                <input type="hidden" name="rt" value="${routingToken}">
                <label class="bv-honeypot" aria-hidden="true">Company website<input name="companyWebsite" tabindex="-1" autocomplete="off"></label>
                @if (formError != null && !formError.isBlank())<p class="bv-form-error" role="alert">${formError}</p>@endif
                <fieldset><legend><span>01</span>Location and task</legend>
                    <label>City or utility<input name="city" type="text" autocomplete="address-level2" placeholder="City, utility, or ZIP"></label>
                    <div class="bv-form-row">
                        <label>Property<select name="propertyType"><option value="">Select one</option><option value="residential">Residential</option><option value="commercial">Commercial</option><option value="hoa">HOA or multifamily</option><option value="restaurant-medical">Restaurant or medical</option><option value="other">Other</option></select></label>
                        <label>Next step<select name="issueType"><option value="">Select one</option><option value="general-testing" selected="${"general-testing".equals(selectedIssueType)}">Testing</option><option value="failed-test-repair" selected="${"failed-test-repair".equals(selectedIssueType)}">Failed test / repair / retest</option><option value="irrigation" selected="${"irrigation".equals(selectedIssueType)}">Irrigation device</option><option value="fire-line" selected="${"fire-line".equals(selectedIssueType)}">Fire line</option><option value="tester-search" selected="${"tester-search".equals(selectedIssueType)}">Find an eligible tester</option><option value="other" selected="${"other".equals(selectedIssueType)}">Other</option></select></label>
                    </div>
                    <label>What happened?<textarea name="notes" rows="5" placeholder="Device or model, leak location, failure code, notice deadline, or portal name"></textarea></label>
                </fieldset>
                <fieldset><legend><span>02</span>Contact</legend>
                    <div class="bv-form-row"><label>Name<input name="fullName" autocomplete="name" required></label><label>Phone<input name="phone" type="tel" autocomplete="tel" required></label></div>
                    <label>Email <small>optional</small><input name="email" type="email" autocomplete="email"></label>
                </fieldset>
                <label class="bv-consent"><input name="consentToRouting" type="checkbox" value="yes" required><span>I agree that BackflowVerdict may store and review this request and follow up about relevant next steps or provider options. See <a href="/privacy">privacy</a>.</span></label>
                <button class="bv-submit" type="submit">Send my request</button>
            </form>
        </div>
    </section>
`)
