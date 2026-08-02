@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.product.ModelGuideRecord
@param PageMeta page
@param java.util.List<ModelGuideRecord> modelGuides

@template.layouts.verdict(page = page, content = @`
    <article class="bv-kit-index" data-global-kit-tool>
        <header class="bv-kit-index-hero">
            <div class="bv-shell bv-kit-index-hero__grid">
                <div class="bv-reveal"><p class="bv-kicker">Official-data repair desk</p><h1>Find the kit family.<br>Before you order.</h1></div>
                <div class="bv-kit-index-hero__copy bv-reveal" style="--delay:90ms"><p>Start with the cast or printed model label. Then narrow by nominal size and the component actually being serviced.</p><p class="bv-kit-index-hero__warning">This is a compatibility index, not a remote diagnosis or a retailer catalog.</p></div>
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
                <header><p class="bv-kicker">Verified model passports</p><h2>Current first cohort.</h2><p>Each passport consolidates size bands, tasks, kit references, model boundaries, and official sources.</p></header>
                <div>@for (var guide : modelGuides)<a href="${guide.canonicalPath()}"><span>${guide.manufacturer()}</span><strong>${guide.model()}</strong><small>${guide.deviceType()} / ${guide.kits().size()} mapped kits</small><b aria-hidden="true">&rarr;</b></a>@endfor</div>
            </div>
        </section>
    </article>
`)
