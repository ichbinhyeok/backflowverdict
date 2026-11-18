@import owner.backflow.web.PageMeta
@import owner.backflow.service.LeadRecord
@import owner.backflow.service.LeadReviewService.Review
@param PageMeta page
@param LeadRecord lead
@param java.util.List<String> photos
@param Review review
@param String csrfToken

@template.layouts.verdict(page = page, content = @`
    <section class="fv-review-admin bv-shell">
        <nav class="fv-breadcrumb"><a href="/admin">Private request queue</a><span>/</span><span>Reference review</span></nav>
        <p class="fv-eyebrow">${lead.displayIssueType()} / ${lead.capturedAt().toString()}</p><h1>Prepare a useful reply.</h1><p>Inspect the submitted evidence. Keep confirmed markings separate from candidates and unknowns. Saving a draft does not send a message.</p>
        <div class="fv-review-admin-grid">
            <div><h2>Submitted context</h2><dl class="fv-review-contact"><dt>Name</dt><dd>${lead.fullName()}</dd><dt>Reply contact</dt><dd>${lead.email()} ${lead.phone()}</dd><dt>Location</dt><dd>${lead.city()}</dd><dt>Source</dt><dd>${lead.displaySourcePage()}</dd><dt>Routing validation</dt><dd>${lead.displayRoutingStatus()}</dd></dl><pre class="fv-request-notes">${lead.notes()}</pre>
                <h2>Private photos (${photos.size()})</h2><p>Re-encoded copies; original filenames and embedded metadata are not retained. Exact label text still needs manual inspection.</p>
                <div class="fv-admin-photos">@for (var photo : photos)<a href="/admin/reviews/${lead.leadId()}/photos/${photo}" target="_blank" rel="noopener"><img src="/admin/reviews/${lead.leadId()}/photos/${photo}" alt="Submitted device photo ${photos.indexOf(photo) + 1}"><span>Open photo ${photos.indexOf(photo) + 1} ↗</span></a>@endfor</div>
                @if (!photos.isEmpty())<form method="post" action="/admin/reviews/${lead.leadId()}/photos/delete"><input type="hidden" name="_csrf" value="${csrfToken}"><button type="submit">Remove stored photos for this request</button></form>@endif
            </div>
            <div><form method="post" action="/admin/reviews/${lead.leadId()}" class="fv-review-editor"><input type="hidden" name="_csrf" value="${csrfToken}"><h2>Reference review draft</h2><label>01 / Confirmed information<textarea name="confirmed" rows="4" maxlength="8000" required placeholder="Readable markings and evidence links; explicitly state if identity is not confirmed.">${review.confirmed()}</textarea></label><label>02 / Part or document candidates<textarea name="candidates" rows="4" maxlength="8000" required placeholder="Part numbers or documents, compatibility conditions and source links.">${review.candidates()}</textarea></label><label>03 / Still to confirm<textarea name="unknowns" rows="4" maxlength="8000" required placeholder="Missing size, suffix, revision, diagnosis, stock or local approval.">${review.unknowns()}</textarea></label><label>04 / Next action<textarea name="nextSteps" rows="4" maxlength="8000" required placeholder="One specific next action, its owner and what evidence closes it.">${review.nextSteps()}</textarea></label><button class="fv-primary" type="submit">Save review draft <span>↗</span></button></form>
                @if (!review.savedAt().isBlank())<div class="fv-saved-review"><p>Draft saved ${review.savedAt()}. Delivery is not recorded by this tool.</p><a class="fv-text-link" href="/admin/reviews/${lead.leadId()}/reply.txt">Download reply text →</a><details><summary>Preview the saved reply</summary><pre>${review.replyText()}</pre></details></div>@endif
            </div>
        </div>
    </section>
`)
