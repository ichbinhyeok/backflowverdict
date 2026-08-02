@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.decision.DecisionGuideRecord
@import owner.backflow.data.model.product.ModelGuideRecord
@param PageMeta page
@param java.util.List<DecisionGuideRecord> guides
@param java.util.List<ModelGuideRecord> modelGuides = java.util.List.of()

@template.layouts.verdict(page = page, content = @`
    <header class="bv-library-hero">
        <div class="bv-shell bv-library-hero__grid">
            <div class="bv-reveal">
                <p class="bv-kicker">Backflow field library</p>
                <h1>Find the exact device, failure, or job.</h1>
            </div>
            <div class="bv-library-hero__intro bv-reveal" style="--delay:90ms">
                <p>Start broad, then narrow by device family, visible symptom, and the work that must happen next.</p>
                <a class="bv-text-link" href="#browse">Browse every decision path <span aria-hidden="true">↓</span></a>
            </div>
        </div>
        <div class="bv-shell bv-library-stats" aria-label="Library scope">
            <span><strong>${guides.size()}</strong> live decision paths</span>
            <span><strong>4</strong> task lanes</span>
            <span><strong>0</strong> city-name duplicates</span>
        </div>
    </header>

    <main class="bv-library" id="browse">
        <section class="bv-library-group bv-library-group--paper">
            <div class="bv-shell bv-library-group__grid">
                <header><p class="bv-kicker">Official model layer</p><h2>Repair kits by exact model</h2><p>Size-band and service-scope compatibility mapped from manufacturer records, not retailer snippets.</p><a class="bv-button" href="/backflow-preventer-repair-kits/">Open repair-kit finder</a></header>
                <div class="bv-library-index">
                    @for (var modelGuide : modelGuides)
                        <a href="${modelGuide.canonicalPath()}"><span>${modelGuide.manufacturer()} / ${modelGuide.deviceType()}</span><strong>${modelGuide.model()} model passport</strong><b aria-hidden="true">&rarr;</b></a>
                    @endfor
                </div>
            </div>
        </section>

        <section class="bv-library-group">
            <div class="bv-shell bv-library-group__grid">
                <header><p class="bv-kicker">01 / Identify</p><h2>Devices and systems</h2><p>Know what is on the pipe before using repair or test advice.</p></header>
                <div class="bv-library-index">
                    @for (var guide : guides)
                        @if (guide.pageType().equals("device") || guide.pageType().equals("system"))
                            <a href="${guide.canonicalPath()}"><span>${guide.eyebrow()}</span><strong>${guide.title()}</strong><b aria-hidden="true">→</b></a>
                        @endif
                    @endfor
                </div>
            </div>
        </section>

        <section class="bv-library-group bv-library-group--paper">
            <div class="bv-shell bv-library-group__grid">
                <header><p class="bv-kicker">02 / Diagnose</p><h2>Leaks, failures, and pressure symptoms</h2><p>Route by what the device is doing, not by a guessed cause.</p></header>
                <div class="bv-library-index">
                    @for (var guide : guides)
                        @if (guide.pageType().equals("problem"))
                            <a href="${guide.canonicalPath()}"><span>${guide.eyebrow()}</span><strong>${guide.title()}</strong><b aria-hidden="true">→</b></a>
                        @endif
                    @endfor
                </div>
            </div>
        </section>

        <section class="bv-library-group">
            <div class="bv-shell bv-library-group__grid">
                <header><p class="bv-kicker">03 / Act</p><h2>Install, adjust, repair, or replace</h2><p>Compare scope, measurements, access, parts, and stop conditions before work begins.</p></header>
                <div class="bv-library-index">
                    @for (var guide : guides)
                        @if (guide.pageType().equals("installation") || guide.pageType().equals("installation-decision") || guide.pageType().equals("procedure") || guide.pageType().equals("cost") || guide.pageType().equals("decision"))
                            <a href="${guide.canonicalPath()}"><span>${guide.eyebrow()}</span><strong>${guide.title()}</strong><b aria-hidden="true">→</b></a>
                        @endif
                    @endfor
                </div>
            </div>
        </section>

        <section class="bv-library-group bv-library-group--ink">
            <div class="bv-shell bv-library-group__grid">
                <header><p class="bv-kicker">04 / Close the loop</p><h2>Testing and compliance workflows</h2><p>Connect the physical work to the tester, report, deadline, and governing utility.</p></header>
                <div class="bv-library-index">
                    @for (var guide : guides)
                        @if (guide.pageType().equals("process") || guide.pageType().equals("workflow"))
                            <a href="${guide.canonicalPath()}"><span>${guide.eyebrow()}</span><strong>${guide.title()}</strong><b aria-hidden="true">→</b></a>
                        @endif
                    @endfor
                </div>
            </div>
        </section>
    </main>
`)
