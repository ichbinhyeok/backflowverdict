@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.product.ModelGuideRecord
@param PageMeta page
@param ModelGuideRecord modelGuide

@template.layouts.verdict(page = page, content = @`
    <article class="bv-model" data-model-guide="${modelGuide.slug()}">
        <header class="bv-model-hero">
            <div class="bv-shell bv-model-hero__grid">
                <div class="bv-model-hero__copy bv-reveal">
                    <nav class="bv-breadcrumb" aria-label="Breadcrumb"><a href="/">Home</a><span>/</span><a href="/backflow-preventer-repair-kits/">Repair kits</a><span>/</span><span>${modelGuide.model()}</span></nav>
                    <p class="bv-kicker">Verified model passport / ${modelGuide.manufacturer()}</p>
                    <h1>${modelGuide.model()}</h1>
                    <p class="bv-model-type">${modelGuide.deviceType()}</p>
                    <p class="bv-lede">${modelGuide.summary()}</p>
                    <div class="bv-model-status"><span>${modelGuide.status()}</span><p>${modelGuide.statusNote()}</p></div>
                </div>
                <figure class="bv-model-figure bv-reveal" style="--delay:90ms">
                    <span class="bv-model-figure__stamp">MODEL RECORD</span>
                    <img src="/images/devices/${modelGuide.diagram()}.svg" alt="${modelGuide.diagramAlt()}">
                    <figcaption>Diagram shows the device family, not a substitute for the cast or printed model label.</figcaption>
                </figure>
            </div>
            <div class="bv-shell bv-model-facts">
                <div><span>Manufacturer</span><strong>${modelGuide.manufacturer()}</strong></div>
                <div><span>Published sizes</span><strong>${modelGuide.sizes().size()} bands</strong></div>
                <div><span>Mapped kits</span><strong>${modelGuide.kits().size()}</strong></div>
                <div><span>Source review</span><strong>${modelGuide.lastReviewed()}</strong></div>
            </div>
        </header>

        <section class="bv-section bv-section--paper" id="kit-finder">
            <div class="bv-shell bv-model-workbench" data-model-kit-tool>
                <header>
                    <p class="bv-kicker">Compatibility workstation</p>
                    <h2>Match the size and repair scope.</h2>
                    <p>Use the complete model label, nominal body size, and failed component. The tool only exposes combinations found in the cited manufacturer material.</p>
                </header>
                <div class="bv-kit-controls">
                    <label>Nominal size<select data-kit-size><option value="">Choose a size band</option>@for (var size : modelGuide.sizes())<option value="${size}">${size}</option>@endfor</select></label>
                    <label>Repair scope<select data-kit-task disabled><option value="">Choose size first</option></select></label>
                </div>
                <p class="bv-kit-count" data-kit-count aria-live="polite">Choose a size band to reveal compatible published kits.</p>
                <div class="bv-kit-results">
                    @for (var kit : modelGuide.kits())
                        <article class="bv-kit-row" data-kit-option data-size="${kit.sizeBand()}" data-task="${kit.task()}" hidden>
                            <div class="bv-kit-row__part"><span>Manufacturer part reference</span><strong>${kit.partNumber()}</strong></div>
                            <div><span>${kit.task()}</span><h3>${kit.kitName()}</h3><p>${kit.contents()}</p></div>
                            <aside><span>Compatibility boundary</span><p>${kit.compatibility()}</p></aside>
                        </article>
                    @endfor
                </div>
                <div class="bv-kit-empty" data-kit-empty hidden>No published match is stored for that combination. Recheck the full label and use the official source below.</div>
            </div>
        </section>

        <section class="bv-section">
            <div class="bv-shell bv-document-grid">
                <div><p class="bv-kicker">Identity check</p><h2>Confirm this model before ordering.</h2><p>Similar-looking assemblies can use different internals. A search result or pipe size alone is not enough.</p></div>
                <ol class="bv-model-identifiers">@for (var identifier : modelGuide.identifiers())<li><span>${String.format("%02d", modelGuide.identifiers().indexOf(identifier) + 1)}</span><p>${identifier}</p></li>@endfor</ol>
            </div>
        </section>

        <section class="bv-section bv-section--ink">
            <div class="bv-shell bv-model-handoff">
                <div><p class="bv-kicker">Before return to service</p><h2>A matching kit is not a passed assembly.</h2></div>
                <div class="bv-model-handoff__notes">
                    <p><strong>Stop for body damage.</strong> Cracks, distortion, severe corrosion, or freeze damage can move the job from internal service to replacement.</p>
                    <p><strong>Test after repair.</strong> Where the assembly is regulated, the governing utility or authority decides tester approval, reporting, and deadlines.</p>
                    <p><strong>Use the current instructions.</strong> Part references can change. Confirm availability and supersession with the manufacturer or an authorized supplier.</p>
                    <a class="bv-button" href="/notice-finder">Find the governing utility</a>
                </div>
            </div>
        </section>

        <section class="bv-section" id="sources">
            <div class="bv-shell bv-model-sources">
                <header><p class="bv-kicker">Official source ledger</p><h2>Trace every compatibility claim.</h2><p>No retailer copy is used as the authority for the kit map.</p></header>
                <ol>@for (var source : modelGuide.sources())<li><span>${String.format("%02d", modelGuide.sources().indexOf(source) + 1)}</span><div><strong>${source.publisher()}</strong><a href="${source.url()}" rel="noopener noreferrer">${source.title()}</a><small>Verified ${source.lastVerified()}</small></div></li>@endfor</ol>
            </div>
        </section>
    </article>
`)
