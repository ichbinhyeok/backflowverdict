@import owner.backflow.web.PageMeta
@param PageMeta page
@param boolean leadSaved = false
@param int photoCount = 0

@template.layouts.verdict(page = page, content = @`
    <section class="fv-confirm" data-lead-saved="${leadSaved}">
        <div class="bv-shell">
            <p class="fv-eyebrow">BackflowVerdict / Manual review</p>
            <span class="fv-confirm-mark" aria-hidden="true">${leadSaved ? "✓" : "↗"}</span>
            <h1>${leadSaved ? "Your review request is saved." : "A clearer next step starts with the details."}</h1>
            <p class="bv-lede">${leadSaved ? "Your submitted details are in the review queue. We use the contact information you provided for any follow-up." : "Send your model, part reference or installation scope so we can review the relevant information."}</p>
            @if (leadSaved)<p class="fv-confirm-note">${photoCount} photo${photoCount == 1 ? "" : "s"} saved with this request. The review separates confirmed information, document or part candidates, remaining unknowns and the next action.</p>@endif
            <p class="fv-confirm-note">Manual reference review does not book an installer or establish that an assembly is safe. Keep the governing utility’s deadlines, required tests and report instructions in view.</p>
            <div class="fv-hero-actions"><a class="fv-primary" href="/backflow-preventer-diagram/">Return to diagrams & parts <span>↗</span></a><a class="fv-text-link" href="/notice-finder">Find the governing utility →</a></div>
        </div>
    </section>
`)
