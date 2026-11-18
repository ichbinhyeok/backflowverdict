@import owner.backflow.web.PageMeta
@param PageMeta page
@param String utilityId = ""
@param String utilityName = ""
@param String sourcePage = ""
@param String selectedIssueType = ""
@param String pageFamily = ""
@param String routingToken = ""
@param String formError = ""
@param java.util.Map<String, String> formValues = java.util.Map.of()

@template.layouts.verdict(page = page, content = @`
    <div class="fv-support">
    <section class="bv-form-hero">
        <div class="bv-shell">
            <p class="bv-kicker">BackflowVerdict / Manual review</p>
            <h1>${"model-identification".equals(selectedIssueType) ? "Start with three clear photos." : "installation".equals(selectedIssueType) ? "Get a second look at your installation scope." : "parts-check".equals(selectedIssueType) ? "Check the part before you order." : "Get help with the next step."}</h1>
            <p class="bv-lede">${"installation".equals(selectedIssueType) ? "Share the job and your questions. We review the scope, reference documents and local next steps." : "parts-check".equals(selectedIssueType) ? "Share the full model, nominal size and part reference. We review the published information and what still needs confirmation." : "Send the device, location and task so we can review the relevant next steps."}</p>
            <p class="fv-support-context" data-support-context hidden></p>
            @if (utilityName != null && !utilityName.isBlank())
                <p class="bv-context-line"><strong>Utility in context</strong><span>${utilityName}</span></p>
            @endif
        </div>
    </section>
    <section class="bv-section bv-section--paper">
        <div class="bv-shell bv-form-grid">
            <div class="bv-form-aside">
                @template.components.review-format()
                <p class="bv-form-note">No payment is required to send this request. Add an email address or phone number for follow-up.</p>
                <p class="bv-form-note">This is guidance and reference review, not an on-site diagnosis, installation quote or booked service. The governing authority and qualified professional confirm the work.</p>
                <p class="bv-form-note">Requests are reviewed against the official workflow. Use an official approved-tester list, when available, before a general provider route.</p>
            </div>
            <form method="post" action="/leads" enctype="multipart/form-data" class="bv-form" data-support-form>
                <input type="hidden" name="utilityId" value="${utilityId}">
                <input type="hidden" name="utilityName" value="${utilityName}">
                <input type="hidden" name="sourcePage" value="${sourcePage}">
                <input type="hidden" name="pageFamily" value="${pageFamily}">
                <input type="hidden" name="rt" value="${routingToken}">
                <label class="bv-honeypot" aria-hidden="true">Company website<input name="companyWebsite" tabindex="-1" autocomplete="off"></label>
                @if (formError != null && !formError.isBlank())<p class="bv-form-error" role="alert">${formError}</p>@endif
                <fieldset><legend><span>01</span>Location and task</legend>
                    <label>City or utility<input name="city" type="text" value="${formValues.getOrDefault("city", "")}" autocomplete="address-level2" placeholder="City, utility, or ZIP"></label>
                    <div class="bv-form-row">
                        <label>Property<select name="propertyType"><option value="">Select one</option><option value="residential" selected="${"residential".equals(formValues.get("propertyType"))}">Residential</option><option value="commercial" selected="${"commercial".equals(formValues.get("propertyType"))}">Commercial</option><option value="hoa" selected="${"hoa".equals(formValues.get("propertyType"))}">HOA or multifamily</option><option value="restaurant-medical" selected="${"restaurant-medical".equals(formValues.get("propertyType"))}">Restaurant or medical</option><option value="other" selected="${"other".equals(formValues.get("propertyType"))}">Other</option></select></label>
                        <label>Next step<select name="issueType"><option value="">Select one</option><option value="model-identification" selected="${"model-identification".equals(selectedIssueType)}">Identify model / label from photos</option><option value="parts-check" selected="${"parts-check".equals(selectedIssueType)}">Model / repair-part check</option><option value="installation" selected="${"installation".equals(selectedIssueType)}">Installation scope review</option><option value="general-testing" selected="${"general-testing".equals(selectedIssueType)}">Testing</option><option value="failed-test-repair" selected="${"failed-test-repair".equals(selectedIssueType)}">Failed test / repair / retest</option><option value="irrigation" selected="${"irrigation".equals(selectedIssueType)}">Irrigation device</option><option value="fire-line" selected="${"fire-line".equals(selectedIssueType)}">Fire line</option><option value="tester-search" selected="${"tester-search".equals(selectedIssueType)}">Find an eligible tester</option><option value="other" selected="${"other".equals(selectedIssueType)}">Other</option></select></label>
                    </div>
                    <label>What happened?<textarea name="notes" rows="5" maxlength="10000" placeholder="Device or model, leak location, failure code, notice deadline, or portal name">${formValues.getOrDefault("notes", "")}</textarea></label>
                </fieldset>
                <fieldset class="fv-photo-field"><legend><span>02</span>Photos <small>optional</small></legend><p>Send a wide device view, a readable label or cast marking, and the area you have a question about. Keep addresses, people and account details out of frame.</p><label class="fv-photo-picker">Choose up to 3 JPEG or PNG photos<input name="photos" type="file" accept="image/jpeg,image/png" multiple data-photo-input></label><p class="fv-photo-limit">5 MB and 16 megapixels per photo. Export HEIC to JPEG first. Photos upload only when you send the request.</p><p role="status" data-photo-status></p><div class="fv-photo-previews" data-photo-previews></div></fieldset>
                <fieldset><legend><span>03</span>Contact</legend>
                    <p class="bv-form-note">Enter an email address or phone number so we can reply. A phone number is optional when you provide email.</p>
                    <label>Email<input name="email" type="email" value="${formValues.getOrDefault("email", "")}" autocomplete="email" maxlength="254"></label>
                    <div class="bv-form-row"><label>Name <small>optional</small><input name="fullName" autocomplete="name" value="${formValues.getOrDefault("fullName", "")}" maxlength="150"></label><label>Phone <small>optional with email</small><input name="phone" type="tel" value="${formValues.getOrDefault("phone", "")}" autocomplete="tel" maxlength="80"></label></div>
                </fieldset>
                <label class="bv-consent"><input name="consentToRouting" type="checkbox" value="yes" required><span>I agree that BackflowVerdict may store and review this request and follow up about relevant next steps or provider options. See <a href="/privacy">privacy</a>.</span></label>
                <button class="bv-submit" type="submit">Send my review request</button>
            </form>
        </div>
    </section>
    </div>
`)
