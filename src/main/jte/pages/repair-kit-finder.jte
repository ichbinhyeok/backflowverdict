@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.product.ModelGuideRecord
@param PageMeta page
@param java.util.List<ModelGuideRecord> modelGuides

@template.layouts.verdict(page = page, content = @`
    <article class="bv-kit-index" data-global-kit-tool>
        <header class="bv-kit-index-hero">
            <div class="bv-shell bv-kit-index-hero__grid">
                <div class="bv-reveal"><p class="bv-kicker">Official-data repair desk</p><h1>Backflow preventer repair kits.<br>By model and size.</h1></div>
                <div class="bv-kit-index-hero__copy bv-reveal" style="--delay:90ms"><p>Match the complete manufacturer and model label, nominal body size, and confirmed repair scope to an official part reference before you order.</p><p class="bv-kit-index-hero__warning">Repair kits are not universal. This is a compatibility index, not a remote diagnosis or a retailer catalog.</p></div>
            </div>
        </header>

        <section class="bv-kit-desk">
            <div class="bv-shell">
                <div class="bv-kit-desk__controls">
                    <label>01 / Model<select data-global-model><option value="">Choose manufacturer and model</option>@for (var guide : modelGuides)<option value="${guide.slug()}">${guide.manufacturer()} ${guide.model()}</option>@endfor</select></label>
                    <label>02 / Nominal size<select data-global-size disabled><option value="">Choose model first</option></select></label>
                    <label>03 / Repair scope<select data-global-task disabled><option value="">Choose size first</option></select></label>
                </div>
                <div class="bv-kit-desk__status" data-global-status aria-live="polite"><span>START HERE</span><p>Read the complete body label. Do not identify a kit from appearance alone.</p></div>
                <div class="bv-kit-results bv-kit-results--global">
                    @for (var guide : modelGuides)
                        @for (var kit : guide.kits())
                            <article class="bv-kit-row" data-global-option data-model="${guide.slug()}" data-size="${kit.sizeBand()}" data-task="${kit.task()}" hidden>
                                <div class="bv-kit-row__part"><span>Official part reference</span><strong>${kit.partNumber()}</strong><a href="${guide.canonicalPath()}">${guide.manufacturer()} ${guide.model()} passport</a></div>
                                <div><span>${kit.task()} / ${kit.sizeBand()}</span><h2>${kit.kitName()}</h2><p>${kit.contents()}</p></div>
                                <aside><span>Compatibility boundary</span><p>${kit.compatibility()}</p></aside>
                            </article>
                        @endfor
                    @endfor
                </div>
            </div>
        </section>

        <section class="bv-section bv-section--paper">
            <div class="bv-shell bv-model-catalog">
                <header><p class="bv-kicker">Visible compatibility index</p><h2>Backflow preventer repair kits by model and size.</h2><p>Each model passport exposes the published size bands, repair scopes, manufacturer part references, compatibility boundaries, and official sources.</p></header>
                <div>@for (var guide : modelGuides)<a href="${guide.canonicalPath()}"><span>${guide.manufacturer()}</span><strong>${guide.model()}</strong><small>${String.join(", ", guide.sizes())} / ${guide.kits().size()} mapped kits / starts with ${guide.kits().get(0).partNumber()}</small><b aria-hidden="true">&rarr;</b></a>@endfor</div>
            </div>
        </section>

        <section class="bv-section">
            <div class="bv-shell bv-document-grid">
                <div><p class="bv-kicker">Compatibility rules</p><h2>How to match a backflow preventer repair kit.</h2><p>A kit is only a candidate after the assembly and failed service zone are confirmed.</p></div>
                <ul class="bv-fact-lines">
                    <li><strong>Manufacturer and exact model:</strong> 975XL, 975XL2, and 975XL3 are not interchangeable names.</li>
                    <li><strong>Nominal body size:</strong> part-number prefixes and internal components can change at a size-band boundary.</li>
                    <li><strong>Repair scope:</strong> check, relief, rubber, seat, cover, and total-service kits contain different parts.</li>
                    <li><strong>Official source:</strong> confirm supersession and current availability in the cited manufacturer record.</li>
                    <li><strong>Return to service:</strong> include qualified repair, a required retest, and utility report closeout in the job.</li>
                </ul>
            </div>
        </section>

        <section class="bv-section bv-section--ink">
            <div class="bv-shell bv-document-grid">
                <div><p class="bv-kicker">Repair-kit FAQ</p><h2>What the part number does not prove.</h2></div>
                <ul class="bv-fact-lines">
                    <li><strong>Are repair kits universal?</strong> No. Visual similarity and pipe size alone do not establish compatibility.</li>
                    <li><strong>Does a kit identify the failed component?</strong> No. Use the failed test, observed location, and qualified diagnosis to define scope.</li>
                    <li><strong>Is the job complete after installation?</strong> Not always. Regulated assemblies may require a passing retest and accepted report.</li>
                </ul>
            </div>
        </section>
    </article>
`)
