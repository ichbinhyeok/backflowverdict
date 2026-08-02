@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.UtilityRecord
@import owner.backflow.data.model.decision.DecisionGuideRecord
@import owner.backflow.data.model.decision.DecisionEvidenceSource
@param PageMeta page
@param DecisionGuideRecord guide
@param java.util.List<UtilityRecord> utilities = java.util.List.of()
@param UtilityRecord selectedUtility = null
@param java.util.List<DecisionEvidenceSource> evidenceSources = java.util.List.of()

@template.layouts.verdict(page = page, content = @`
    <article class="bv-case" data-guide-type="${guide.pageType()}" data-guide-slug="${guide.slug()}">
        <header class="bv-case-hero">
            <div class="bv-shell bv-case-hero__grid">
                <div class="bv-case-hero__copy bv-reveal">
                    <nav class="bv-breadcrumb" aria-label="Breadcrumb"><a href="/">Home</a><span>/</span><span>${guide.eyebrow()}</span></nav>
                    <p class="bv-kicker">${guide.eyebrow()}</p>
                    <h1>${guide.title()}</h1>
                    <p class="bv-lede">${guide.description()}</p>
                    <div class="bv-verdict"><span>Working verdict</span><p>${guide.answer()}</p></div>
                </div>
                <figure class="bv-diagram bv-reveal" style="--delay: 90ms">
                    <img src="/images/devices/${guide.diagram()}.svg" alt="${guide.diagramAlt()}">
                    <figcaption><span>FIELD VIEW</span> Use the shape as a clue. Confirm the model label before service.</figcaption>
                </figure>
            </div>
        </header>

        <section class="bv-alert" aria-label="Safety note">
            <div class="bv-shell"><strong>Before you act</strong><p>${guide.urgency()}</p></div>
        </section>

        @if (guide.toolType() == null || guide.toolType().isBlank())
            <section class="bv-section" id="decision">
                <div class="bv-shell bv-document-grid">
                    <div class="bv-section-heading"><p class="bv-kicker">Choose the closest evidence</p><h2>${guide.prompt()}</h2><p>These are routing choices, not remote diagnoses.</p></div>
                    <div class="bv-choice-list">
                        @for (var choice : guide.choices())
                            <a class="bv-choice bv-choice--${choice.tone()}" href="${choice.href()}">
                                <span class="bv-choice__mark" aria-hidden="true"></span>
                                <span><strong>${choice.label()}</strong><small>${choice.detail()}</small></span>
                                <span class="bv-choice__arrow" aria-hidden="true">→</span>
                            </a>
                        @endfor
                    </div>
                </div>
            </section>
        @endif

        @if (guide.toolType() != null && !guide.toolType().isBlank())
            <section class="bv-section bv-workstation" id="decision-tool" data-decision-tool="${guide.toolType()}">
                <div class="bv-shell bv-workstation__grid">
                    <div class="bv-workstation__intro">
                        <p class="bv-kicker">Interactive check</p>
                        <h2>Turn observations into a next step</h2>
                        <p>This narrows the route. It does not replace an on-site test, manufacturer instructions, or local requirements.</p>
                    </div>

                    @if (guide.toolType().equals("diagnostic") || guide.toolType().equals("identifier"))
                        <div class="bv-tool" data-choice-tool>
                            <fieldset>
                                <legend>${guide.prompt()}</legend>
                                @for (var choice : guide.choices())
                                    <button type="button" data-tool-choice data-result-title="${choice.label()}" data-result-detail="${choice.detail()}" data-result-href="${choice.href()}">
                                        <strong>${choice.label()}</strong><span>${choice.detail()}</span>
                                    </button>
                                @endfor
                            </fieldset>
                            <div class="bv-tool-result" data-tool-result role="status" aria-live="polite" tabindex="-1" hidden>
                                <span>Suggested route</span><h3 data-result-title></h3><p data-result-detail></p><a class="bv-button" data-result-link href="#safe-checks">Continue</a>
                            </div>
                        </div>
                    @elseif (guide.toolType().equals("score"))
                        <form class="bv-tool" data-score-tool>
                            <fieldset>
                                <legend>Which statements are true?</legend>
                                <label><input type="checkbox" value="3"> The body is cracked, distorted, or freeze damaged.</label>
                                <label><input type="checkbox" value="2"> The same failure returned after a recent repair.</label>
                                <label><input type="checkbox" value="2"> An approved repair kit cannot be confirmed.</label>
                                <label><input type="checkbox" value="1"> The device is near the end of its documented service life.</label>
                                <label><input type="checkbox" value="1"> Repair plus retesting is close to the replacement quote.</label>
                            </fieldset>
                            <button class="bv-button" type="submit">Compare repair and replacement</button>
                            <div class="bv-tool-result" data-tool-result role="status" aria-live="polite" tabindex="-1" hidden><span>Working verdict</span><h3 data-result-title></h3><p data-result-detail></p></div>
                        </form>
                    @elseif (guide.toolType().equals("pressure"))
                        <form class="bv-tool" data-pressure-tool>
                            <div class="bv-tool-fields">
                                <label>Static pressure before use <span><input name="staticPsi" inputmode="decimal" type="number" min="0" max="200" required> PSI</span></label>
                                <label>Pressure with a fixture running <span><input name="flowPsi" inputmode="decimal" type="number" min="0" max="200" required> PSI</span></label>
                            </div>
                            <button class="bv-button" type="submit">Interpret the readings</button>
                            <div class="bv-tool-result" data-tool-result role="status" aria-live="polite" tabindex="-1" hidden><span>Reading pattern</span><h3 data-result-title></h3><p data-result-detail></p></div>
                        </form>
                    @elseif (guide.toolType().equals("cost"))
                        <form class="bv-tool" data-cost-tool>
                            <div class="bv-tool-fields">
                                <label>Access difficulty<select name="access"><option value="Open and accessible">Open and accessible</option><option value="Tight or partly concealed">Tight or partly concealed</option><option value="Wall, pit, or slab work">Wall, pit, or slab work</option></select></label>
                                <label>Scope<select name="scope"><option value="Single-device scope">Single-device scope</option><option value="Extra fittings or report work">Extra fittings or report work</option><option value="Commercial or permit scope">Commercial or permit scope</option></select></label>
                            </div>
                            <button class="bv-button" type="submit">Build a quote checklist</button>
                            <div class="bv-tool-result" data-tool-result role="status" aria-live="polite" tabindex="-1" hidden><span>Quote scope &mdash; no unsupported price estimate</span><h3 data-cost-range></h3><p data-cost-detail>Ask each provider to separate device, labor, access work, permit, testing, and report filing so quotes are comparable.</p></div>
                        </form>
                    @endif
                </div>
            </section>
        @endif

        <section class="bv-section bv-section--ink" id="workflow">
            <div class="bv-shell">
                <div class="bv-section-heading"><p class="bv-kicker">Evidence sequence</p><h2>Move in this order</h2></div>
                <ol class="bv-steps">
                    @for (var step : guide.steps())
                        <li><span>${step.label()}</span><div><h3>${step.title()}</h3><p>${step.body()}</p></div></li>
                    @endfor
                </ol>
            </div>
        </section>

        @if (!guide.identifiers().isEmpty())
            <section class="bv-section">
                <div class="bv-shell bv-document-grid">
                    <div class="bv-section-heading"><p class="bv-kicker">Identification notes</p><h2>What to look for</h2></div>
                    <ul class="bv-fact-lines">@for (var item : guide.identifiers())<li>${item}</li>@endfor</ul>
                </div>
            </section>
        @endif

        <section class="bv-section bv-section--paper" id="safe-checks">
            <div class="bv-shell bv-safety-grid">
                <div><p class="bv-kicker">Safe observations</p><h2>What you can check without taking it apart</h2><ul class="bv-check-list">@for (var item : guide.safeChecks())<li>${item}</li>@endfor</ul></div>
                <div class="bv-stop"><p class="bv-kicker">Stop conditions</p><h2>Move to qualified help when</h2><ul>@for (var item : guide.stopConditions())<li>${item}</li>@endfor</ul><a class="bv-button" href="/get-help/?source=${guide.canonicalPath()}">Get help with this next step</a></div>
            </div>
        </section>

        @if (!guide.notThisDevice().isEmpty())
            <section class="bv-section">
                <div class="bv-shell bv-not-this"><div><p class="bv-kicker">Boundary check</p><h2>Do not confuse this path with</h2></div><ul>@for (var item : guide.notThisDevice())<li>${item}</li>@endfor</ul></div>
            </section>
        @endif

        <section class="bv-section bv-local">
            <div class="bv-shell bv-local__grid">
                <div><p class="bv-kicker">Official local layer</p><h2>Your utility sets the rule</h2><p>${guide.evidenceNote()}</p></div>
                <form action="${guide.canonicalPath()}" method="get" class="bv-lookup bv-utility-picker" data-utility-picker>
                    <label for="utility-filter">Find the utility named on the notice</label>
                    <input id="utility-filter" type="search" value="${selectedUtility == null ? "" : selectedUtility.utilityName()}" placeholder="Start typing a utility or state" autocomplete="off" data-utility-filter>
                    <div><select id="utility-query" name="utility" required data-utility-select><option value="">Select a verified utility</option>@for (var utility : utilities)@if (selectedUtility != null && selectedUtility.utilityId().equals(utility.utilityId()))<option value="${utility.utilityId()}" selected>${utility.utilityName()} — ${utility.state()}</option>@else<option value="${utility.utilityId()}">${utility.utilityName()} — ${utility.state()}</option>@endif@endfor</select><button type="submit">Apply official rule</button></div>
                    <p class="bv-lookup-status" aria-live="polite">Verified utility matches: <span data-utility-count>${utilities.size()}</span></p>
                </form>
            </div>
            @if (selectedUtility != null)
                <div class="bv-shell bv-utility-overlay" aria-live="polite">
                    <div><span>Selected official record</span><h3>${selectedUtility.utilityName()}</h3><p>${selectedUtility.verdictSummary()}</p></div>
                    <dl>
                        <div><dt>Testing cadence</dt><dd>${selectedUtility.testingFrequency() == null || selectedUtility.testingFrequency().isBlank() ? "Confirm with the utility" : selectedUtility.testingFrequency()}</dd></div>
                        <div><dt>Due basis</dt><dd>${selectedUtility.dueBasis() == null || selectedUtility.dueBasis().isBlank() ? "See the official record" : selectedUtility.dueBasis()}</dd></div>
                        <div><dt>Last verified</dt><dd>${selectedUtility.lastVerified().toString()}</dd></div>
                    </dl>
                    <div class="bv-utility-overlay__actions"><a class="bv-button" href="/utilities/${selectedUtility.state()}/${selectedUtility.canonicalSlug()}/">Open complete utility record</a>@if (!selectedUtility.officialProgramUrls().isEmpty())<a href="${selectedUtility.officialProgramUrls().get(0)}" rel="noopener noreferrer">Open official source</a>@endif</div>
                </div>
            @endif
        </section>

        <section class="bv-section bv-section--ink" id="sources">
            <div class="bv-shell bv-source-ledger">
                <div><p class="bv-kicker">Evidence ledger</p><h2>What this decision path is built on</h2><p>${guide.evidenceNote()}</p></div>
                <ol>@for (var source : evidenceSources)<li><span>${String.format("%02d", evidenceSources.indexOf(source) + 1)}</span><div><strong>${source.publisher()} · ${source.claimId()}</strong><a href="${source.url()}" rel="noopener noreferrer">${source.title()}</a><small>${source.use()} Verified ${source.lastVerified()}.</small></div></li>@endfor</ol>
            </div>
        </section>

        <section class="bv-section">
            <div class="bv-shell">
                <div class="bv-section-heading"><p class="bv-kicker">Continue the case</p><h2>Related next steps</h2></div>
                <div class="bv-related">@for (var link : guide.related())<a href="${link.href()}"><span>${link.eyebrow()}</span><strong>${link.title()}</strong><b aria-hidden="true">→</b></a>@endfor</div>
                <p class="bv-reviewed">CONTENT REVIEWED · ${guide.lastReviewed()}</p>
            </div>
        </section>
    </article>
`)
