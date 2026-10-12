@import owner.backflow.data.model.decision.DecisionFieldSection
@param java.util.List<DecisionFieldSection> sections

@for (var section : sections)
    <section class="bv-section bv-field-section" id="${section.id()}" data-field-section>
        <div class="bv-shell">
            <header class="bv-section-heading"><p class="bv-kicker">Field reference</p><h2>${section.title()}</h2><p>${section.introduction()}</p></header>
            @if (section.diagram() != null && !section.diagram().isBlank())
                <figure class="bv-field-figure"><img src="/images/devices/${section.diagram()}.svg" alt="${section.diagramAlt()}" loading="lazy"><figcaption>${section.caption()} <a href="/images/devices/${section.diagram()}.svg">Open full-size diagram</a></figcaption></figure>
            @endif
            <div class="bv-reference-scroll" role="region" aria-label="${section.title()}" tabindex="0">
                <table class="bv-reference-table"><thead><tr><th scope="col">${section.firstColumn()}</th><th scope="col">${section.secondColumn()}</th><th scope="col">${section.thirdColumn()}</th></tr></thead>
                    <tbody>@for (var row : section.rows())<tr><th scope="row">${row.observation()}</th><td>${row.meaning()}</td><td>${row.nextStep()}@if (row.href() != null && !row.href().isBlank()) <a href="${row.href()}">Continue &rarr;</a>@endif</td></tr>@endfor</tbody>
                </table>
            </div>
            @if (!section.sources().isEmpty())
                <ul class="bv-field-sources" aria-label="Sources for this reference">@for (var source : section.sources())<li><span>${source.eyebrow()}</span> <a href="${source.href()}" data-link-kind="manufacturer" rel="noopener noreferrer">${source.title()}</a></li>@endfor</ul>
            @endif
        </div>
    </section>
@endfor
