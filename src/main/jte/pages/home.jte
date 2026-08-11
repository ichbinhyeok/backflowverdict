@import owner.backflow.web.PageMeta
@import owner.backflow.web.PriorityRoute
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.MetroRecord
@import owner.backflow.data.model.StateGuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<UtilityRecord> utilities
@param java.util.List<GuideRecord> guides
@param java.util.List<GuideRecord> featuredGuides = java.util.List.of()
@param java.util.List<StateGuideRecord> stateGuides = java.util.List.of()
@param java.util.List<MetroRecord> metros = java.util.List.of()
@param java.util.List<UtilityRecord> featuredUtilities = java.util.List.of()
@param java.util.Map<String, String> testerPaths
@param java.util.Map<String, String> testerLabels
@param java.util.List<PriorityRoute> priorityRoutes = java.util.List.of()
@param StateGuideRecord featuredStateGuide = null
@param int publishedUtilityCount
@param int publishedStateCount
@param int publishedMetroCount
@param int publicProviderCount

@template.layouts.verdict(page = page, content = @`
    <section class="bv-home-hero">
        <div class="bv-shell bv-home-hero__grid">
            <div class="bv-home-hero__copy bv-reveal">
                <p class="bv-kicker">Backflow decision system</p>
                <h1>What happened with your water-control device?</h1>
                <p class="bv-lede">Identify the device, check the symptom, then confirm the rule for your water utility.</p>
                <a class="bv-text-link" href="#start">Start with what you can see <span>↓</span></a>
            </div>
            <div class="bv-home-visual bv-reveal" style="--delay:90ms" aria-hidden="true">
                <svg viewBox="0 0 620 480"><path class="pipe" d="M25 282h118v-92h88v92h88v-92h92v92h184"/><path class="device" d="M192 148h78v84h-78zM365 148h78v84h-78zM270 175h95v108h-95z"/><path class="flow" d="M64 255h60m360 0h70"/><circle cx="317" cy="312" r="16"/><path class="drop" d="M317 328c-18 24-20 39 0 49 20-10 18-25 0-49z"/><text x="24" y="58">FIELD NOTE / 01</text><text x="24" y="90">DEVICE → SYMPTOM → RULE</text></svg>
            </div>
        </div>
    </section>

    <section class="bv-start" id="start">
        <div class="bv-shell bv-document-grid">
            <div class="bv-section-heading"><p class="bv-kicker">Start here</p><h2>Choose the event, not a technical term</h2><p>You do not need to know the model yet.</p></div>
            <div class="bv-choice-list bv-choice-list--home">
                <a class="bv-choice" href="/backflow-test/"><span class="bv-index">01</span><span><strong>I received a test notice</strong><small>Find the test, tester, and local reporting path.</small></span><span class="bv-choice__arrow">→</span></a>
                <a class="bv-choice bv-choice--urgent" href="/backflow-preventer-leaking/"><span class="bv-index">02</span><span><strong>It is leaking or discharging</strong><small>Start with location, timing, weather, and recent work.</small></span><span class="bv-choice__arrow">→</span></a>
                <a class="bv-choice bv-choice--warning" href="/failed-backflow-test/"><span class="bv-index">03</span><span><strong>My device failed a test</strong><small>Repair, retest, and close the utility record.</small></span><span class="bv-choice__arrow">→</span></a>
                <a class="bv-choice" href="/backflow-preventer/"><span class="bv-index">04</span><span><strong>I need to identify the device</strong><small>Separate RPZ, PVB, double-check, and look-alikes.</small></span><span class="bv-choice__arrow">→</span></a>
                <a class="bv-choice" href="/backwater-valve/"><span class="bv-index">05</span><span><strong>Sewage is backing up</strong><small>Use the separate sewer backwater-valve lane.</small></span><span class="bv-choice__arrow">→</span></a>
                <a class="bv-choice" href="/water-pressure-regulator/"><span class="bv-index">06</span><span><strong>House pressure is high, low, or unstable</strong><small>Measure PSI and separate regulator symptoms from supply or fixture problems.</small></span><span class="bv-choice__arrow">→</span></a>
            </div>
        </div>
    </section>

    <section class="bv-section bv-local">
        <div class="bv-shell bv-local__grid">
            <div><p class="bv-kicker">Have a notice?</p><h2>Find the exact utility record</h2><p>Enter the city, utility, portal, or identifier printed on the notice.</p></div>
            <form class="bv-lookup" method="get" action="/notice-finder"><label for="home-query">Notice or utility clue</label><div><input id="home-query" name="q" type="search" placeholder="Example: Queen Creek BSI"><button type="submit">Find official route</button></div></form>
        </div>
        <div class="bv-shell bv-proof-line"><span>${publishedUtilityCount} published utility records</span><span>${publishedStateCount} states covered</span><span>Official and commercial routes kept separate</span></div>
    </section>

    <section class="bv-section">
        <div class="bv-shell">
            <div class="bv-section-heading"><p class="bv-kicker">Device passports</p><h2>Four systems people commonly mix up</h2></div>
            <div class="bv-passports">
                <a href="/backflow-preventer/"><img src="/images/devices/rpz.svg" alt=""><span>Potable water</span><strong>Backflow preventer</strong><p>RPZ, double-check, irrigation, and testable assemblies.</p></a>
                <a href="/vacuum-breaker/"><img src="/images/devices/pvb.svg" alt=""><span>Air-inlet protection</span><strong>Vacuum breaker</strong><p>PVB, atmospheric, and hose-bib device families.</p></a>
                <a href="/backwater-valve/"><img src="/images/devices/backwater.svg" alt=""><span>Sewer lane</span><strong>Backwater valve</strong><p>Drainage reversal protection, separate from drinking water.</p></a>
                <a href="/water-pressure-regulator/"><img src="/images/devices/regulator.svg" alt=""><span>House pressure</span><strong>Pressure regulator</strong><p>Measure static and flowing PSI before adjustment or replacement.</p></a>
            </div>
            <a class="bv-library-callout" href="/backflow-library/"><span>Browse the complete field library</span><strong>Devices, failures, repair paths, installation, and testing</strong><b aria-hidden="true">→</b></a>
        </div>
    </section>

    <section class="bv-section bv-section--ink">
        <div class="bv-shell bv-method">
            <div><p class="bv-kicker">How a verdict is built</p><h2>Observation first. Local authority last.</h2></div>
            <ol><li><span>01</span><strong>Identify</strong><p>Use the nameplate, openings, pipe system, and visible geometry.</p></li><li><span>02</span><strong>Decide</strong><p>Use safe checks and stop conditions without pretending to diagnose remotely.</p></li><li><span>03</span><strong>Verify</strong><p>Open the governing utility record for tester, deadline, and submission facts.</p></li></ol>
        </div>
    </section>

    <section class="bv-section">
        <div class="bv-shell bv-document-grid">
            <div><p class="bv-kicker">High-intent field desk</p><h2>Move from the label to the right job.</h2><p>Use the exact model, installation scope, or device family before comparing parts or work.</p></div>
            <div class="bv-related">
                <a href="/backflow-preventer-repair-kits/"><span>Parts</span><strong>Find repair kits by model and size</strong><b aria-hidden="true">&rarr;</b></a>
                <a href="/backflow-preventer-installation/"><span>Install</span><strong>Check installation requirements and detail</strong><b aria-hidden="true">&rarr;</b></a>
                <a href="/backflow-preventer/"><span>Identify</span><strong>Compare RPZ with other backflow preventers</strong><b aria-hidden="true">&rarr;</b></a>
            </div>
        </div>
    </section>

    @if (!featuredUtilities.isEmpty())
        <section class="bv-section">
            <div class="bv-shell">
                <div class="bv-section-heading"><p class="bv-kicker">Source-backed records</p><h2>See the local layer in practice</h2><p>These pages retain the official utility evidence already built in this domain.</p></div>
                <div class="bv-record-rail">
                    @for (var utility : featuredUtilities)
                        <a href="/utilities/${utility.state()}/${utility.canonicalSlug()}/"><span>${utility.state()}</span><strong>${utility.utilityName()}</strong><small>Verified ${utility.lastVerified().toString()}</small></a>
                    @endfor
                </div>
            </div>
        </section>
    @endif
`)
