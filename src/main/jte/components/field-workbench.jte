@import owner.backflow.web.PageMeta
@import owner.backflow.data.model.decision.DecisionGuideRecord
@import owner.backflow.data.model.product.ModelGuideRecord
@param PageMeta page
@param DecisionGuideRecord guide
@param java.util.List<ModelGuideRecord> modelGuides

<header class="fv-hero">
    <div class="bv-shell">
        <nav class="fv-breadcrumb" aria-label="Breadcrumb"><a href="/">Home</a><span>/</span><a href="/backflow-library/">Field library</a><span>/</span><span>${guide.slug().equals("backflow-preventer-diagram") ? "Diagrams & parts" : "Installation"}</span></nav>
        <div class="fv-hero-grid">
            <div class="fv-hero-copy">
                <p class="fv-eyebrow">BackflowVerdict / Field guide ${guide.slug().equals("backflow-preventer-diagram") ? "01" : "02"}</p>
                <h1>Backflow preventer <em>${guide.slug().equals("backflow-preventer-diagram") ? "diagram." : "installation."}</em></h1>
                <p class="fv-hero-lede">${guide.slug().equals("backflow-preventer-diagram") ? "See the parts. Match your model. Find the right next step." : "A clearer plan. A better installer conversation."}</p>
                <div class="fv-hero-actions"><a class="fv-primary" href="#field-desk">${guide.slug().equals("backflow-preventer-diagram") ? "Find my repair parts" : "Build my installer brief"}<span aria-hidden="true">↗</span></a><a class="fv-text-link" href="#identify-device">Don't know the model? Start here ↓</a><a class="fv-text-link" href="${guide.slug().equals("backflow-preventer-diagram") ? "#read-the-diagram" : "#model-installation-details"}">${guide.slug().equals("backflow-preventer-diagram") ? "Compare RPZ, DCVA & PVB" : "See installation drawings"} ↓</a></div>
                <p class="fv-hero-foot">Manufacturer references. Exact-model matching.<br>No sign-up to explore.</p>
            </div>
            <figure class="fv-plate" data-reference-plate>
                <div class="fv-plate-top"><span>RPZ / Reduced pressure zone</span><span>Illustrated reference</span></div>
                <div class="fv-plate-image">
                    <img src="/images/devices/rpz-studio-v2.webp" width="1752" height="897" fetchpriority="high" alt="Illustrative brass RPZ assembly with two check-service covers, test connections and a downward relief outlet; not a specific model or installation plan">
                    <a class="fv-hotspot fv-hotspot--one" href="${guide.slug().equals("backflow-preventer-diagram") ? "#read-the-diagram" : "#model-installation-details"}" data-plate-point="covers" aria-label="01: Check-service covers" aria-current="true">01</a>
                    <a class="fv-hotspot fv-hotspot--two" href="${guide.slug().equals("backflow-preventer-diagram") ? "#read-the-diagram" : "#model-installation-details"}" data-plate-point="tests" aria-label="02: Test connections">02</a>
                    <a class="fv-hotspot fv-hotspot--three" href="${guide.slug().equals("backflow-preventer-diagram") ? "#read-the-diagram" : "#model-installation-details"}" data-plate-point="relief" aria-label="03: Relief outlet">03</a>
                </div>
                <div class="fv-plate-note" aria-live="polite"><span data-plate-number>01 /</span><div><strong data-plate-title>Check-service covers</strong><p data-plate-description>Cover shape is a clue, not a model ID. Read the complete nameplate before choosing parts.</p><a class="fv-text-link" href="#field-desk" data-plate-lookup>Match the model & open its documents ↗</a></div></div>
                <figcaption>AI-generated family illustration. Use the exact manufacturer drawing for parts, dimensions and installation.</figcaption>
            </figure>
        </div>
        <div class="fv-guide-strip"><span><b>01</b> Identify the exact model</span><span><b>02</b> Match parts or plan the work</span><span><b>03</b> Confirm local requirements</span></div>
    </div>
</header>

<section class="fv-desk" id="field-desk" data-field-desk data-default-mode="${guide.slug().equals("backflow-preventer-diagram") ? "parts" : "installation"}" data-source="${guide.canonicalPath()}" data-help-url="${page.requestHelpPathOrDefault()}">
    <div class="bv-shell">
        <div class="fv-desk-heading"><div><p class="fv-eyebrow">From reference to action</p><h2>Your device. Your next move.</h2></div><div class="fv-tabs" role="tablist" aria-label="Choose a task" data-desk-tabs hidden><button type="button" role="tab" id="parts-tab" aria-controls="parts-panel" data-desk-tab="parts">Find repair parts <span>↗</span></button><button type="button" role="tab" id="installation-tab" aria-controls="installation-panel" data-desk-tab="installation">Plan installation <span>↗</span></button></div></div>
        <div id="parts-panel" role="tabpanel" aria-labelledby="parts-tab" data-desk-panel="parts">
            <div class="fv-desk-grid">
                <div class="fv-controls">
                    <p class="fv-step">01 / Read the nameplate</p><h3>Match your model.</h3><p>Use the full model suffix and the nominal size printed on the body.</p>
                    <label for="desk-model">Manufacturer & model<select id="desk-model" data-desk-model><option value="">Choose your model</option>@for (var model : modelGuides)<option value="${model.slug()}">${model.manufacturer()} ${model.model()}</option>@endfor</select></label>
                    <label for="desk-size">Nominal size<select id="desk-size" data-desk-size disabled><option value="">Choose a model first</option></select></label>
                    <label for="desk-scope">Repair scope<select id="desk-scope" data-desk-scope disabled><option value="">All published scopes</option></select></label>
                    <a class="fv-text-link" href="#identify-device">I can't read or find the model →</a>
                    <a class="fv-text-link" data-desk-help="parts-check" href="${page.requestHelpPathOrDefault()}&issueType=parts-check">Request a parts check with my selection ↗</a>
                    <noscript><p><a href="/backflow-preventer-repair-kits/">Browse all model and size references without JavaScript →</a></p></noscript>
                </div>
                <div class="fv-results">
                    <p class="fv-observation" data-observation-note hidden></p>
                    <div class="fv-model-documents" data-desk-documents hidden></div>
                    <div class="fv-empty" data-desk-empty><span class="fv-crosshair" aria-hidden="true">+</span><p class="fv-eyebrow">The right part starts here</p><h3>Similar device.<br>Different internals.</h3><p>Choose your model and size to see published part references, what each kit includes, and available retailer links.</p><a href="/backflow-preventer-repair-kits/">Browse the complete parts library →</a></div>
                    <div data-desk-results hidden><div class="fv-result-heading" aria-live="polite"><p class="fv-eyebrow" data-desk-count></p><h3 data-desk-model-name></h3><p>Published compatibility is not a diagnosis. Confirm the failed component before ordering.</p></div><div data-desk-matches></div><div class="fv-result-help"><strong>Need a second look before ordering?</strong><p>Send your selected model, size and part references for manual review.</p><a class="fv-primary" data-desk-help="parts-check" href="${page.requestHelpPathOrDefault()}&issueType=parts-check">Request a parts check <span>↗</span></a><small>Manual guidance; no inspection or guaranteed supplier match.</small></div></div>
                </div>
            </div>
            @for (var model : modelGuides)
                <template data-model-documents="${model.slug()}"><div class="fv-doc-heading"><p class="fv-eyebrow">Exact-model evidence</p><h3>${model.manufacturer()} ${model.model()}</h3><p>${model.deviceType()}</p></div><details open><summary>Open manufacturer documents</summary><ul>@for (var section : model.fieldSections())@for (var source : section.sources())<li><a href="${source.href()}" data-link-kind="manufacturer" rel="noopener noreferrer">${source.title()} ↗</a></li>@endfor@endfor@for (var source : model.sources())<li><a href="${source.url()}" data-link-kind="manufacturer" rel="noopener noreferrer">${source.title()} ↗</a><small>Source checked ${source.lastVerified()}</small></li>@endfor</ul></details><p class="fv-doc-boundary">Read the diagram and matching size/revision table together. A part reference is not a diagnosis. <a href="${model.canonicalPath()}">Open full model record →</a></p></template>
                @for (var kit : model.kits())
                    <template data-desk-kit data-model="${model.slug()}" data-model-label="${model.manufacturer()} ${model.model()}" data-size="${kit.sizeBand()}" data-scope="${kit.task()}" data-part="${kit.partNumber()}">
                        <article class="fv-part-row"><div class="fv-part-top"><span>${kit.task()}</span><a href="${model.canonicalPath()}">Model & sources ↗</a></div><h4>${kit.partNumber()}</h4><p class="fv-part-name">${kit.kitName()}</p><p>${kit.contents()}</p><p class="fv-fit-note">${kit.compatibility()}</p>@template.components.merchant-links(merchants = kit.merchants(), partNumber = kit.partNumber())@if (kit.merchants().isEmpty())<p class="fv-part-availability">No verified retailer link stored for this part. Use the exact reference when contacting a supplier.</p>@endif</article>
                    </template>
                @endfor
            @endfor
        </div>
        <div id="installation-panel" role="tabpanel" aria-labelledby="installation-tab" data-desk-panel="installation">
            <div class="fv-desk-grid">
                <form class="fv-controls" data-brief-form>
                    <p class="fv-step">01 / Define the job</p><h3>Give the installer<br>a better starting point.</h3><p>Build a brief you can copy or print. Your choices stay in this browser until you send a request.</p>
                    <label for="brief-job">What are you planning?<select id="brief-job" name="job"><option value="Replace an existing assembly">Replace an existing assembly</option><option value="Install a new assembly">Install a new assembly</option><option value="Compare an installation quote">Compare an installation quote</option><option value="Identify what is required">Identify what is required</option></select></label>
                    <label for="brief-system">Protected system<select id="brief-system" name="system"><option value="Irrigation">Irrigation</option><option value="Domestic or commercial water">Domestic or commercial water</option><option value="Fire protection">Fire protection</option><option value="Not yet confirmed">Not yet confirmed</option></select></label>
                    <label for="brief-place">Location & exposure<select id="brief-place" name="place"><option value="Outdoors, exposed">Outdoors, exposed</option><option value="Outdoors, in an enclosure">Outdoors, in an enclosure</option><option value="Indoors or in a mechanical room">Indoors or in a mechanical room</option><option value="Not yet confirmed">Not yet confirmed</option></select></label>
                    <button class="fv-primary" type="submit">Build my brief <span aria-hidden="true">↗</span></button>
                    <noscript><p>Use the <a href="#installation-detail">installation detail checklist</a> below and <a href="${page.requestHelpPathOrDefault()}">request manual help</a>.</p></noscript>
                </form>
                <div class="fv-brief-preview"><div class="fv-brief-top"><span>INSTALLER BRIEF</span><span>BackflowVerdict</span></div><p class="fv-eyebrow">${guide.slug().equals("backflow-preventer-installation") ? "Installation planning" : "Prepare for the work"}</p><h3>A complete scope.<br>Fewer loose ends.</h3><p class="fv-brief-summary" data-brief-summary>Choose the job, system and setting. Get a practical checklist to discuss with a qualified installer.</p><ol class="fv-brief-list" data-brief-list><li>Confirm protection level and exact model</li><li>Document space, drainage and exposure</li><li>Separate equipment, labor and closeout costs</li><li>Confirm permits, testing and report ownership</li></ol><div class="fv-brief-output" data-brief-output hidden tabindex="-1" aria-live="polite"><p class="fv-eyebrow">Your brief is ready</p><div class="fv-brief-actions"><button type="button" data-copy-brief>Copy brief</button><button type="button" data-print-brief>Print brief</button><span data-copy-status role="status"></span></div><p>Want help reviewing this scope?</p><a class="fv-primary" data-desk-help="installation" href="${page.requestHelpPathOrDefault()}&issueType=installation">Request an installation review <span>↗</span></a><small>Manual scope review. Installation, quotes and local approval are not provided by this tool.</small></div><p class="fv-brief-boundary">Planning checklist, not an engineered installation plan. Confirm the exact model and governing authority.</p></div>
            </div>
        </div>
        <div class="fv-working-verdict"><span>Working verdict</span><p>${guide.answer()}</p></div>
    </div>
</section>

<section class="fv-identify" id="identify-device">
    <div class="bv-shell">
        <div class="fv-identify-heading"><div><p class="fv-eyebrow">Model unknown / start here</p><h2>You don't need to know<br>the part name yet.</h2><p>Start with the markings you can safely see. Keep the full suffix: 975XL, 975XL2 and 975XL3 are different labels.</p></div><figure class="fv-nameplate"><img src="/images/devices/nameplate-guide.svg" width="680" height="280" alt="Example label showing a model field, the full model suffix and nominal size; the example is not a real product identification"><figcaption>Example label layout. Look for a tag, nameplate or cast markings; locations vary by device.</figcaption></figure></div>
        <div class="fv-photo-guide"><article><span>01 / The whole device</span><h3>Step back.</h3><p>Show the full assembly, pipe connections, covers and surrounding space. Keep people and addresses out of frame.</p></article><article><span>02 / The exact marking</span><h3>Get the label sharp.</h3><p>Include manufacturer, complete model suffix and nominal size. Photograph any readable cast lettering too.</p></article><article><span>03 / The question area</span><h3>Show what caught your eye.</h3><p>Photograph the wet point, damaged area or installation constraint. Keep the full device view for context.</p></article></div>
        <div class="fv-identify-action"><div><strong>Send what you have. Say what you can't confirm.</strong><p>Attach up to three photos to a manual identification request. Readable evidence may narrow the model; unclear markings remain unknown.</p></div><a class="fv-primary" data-identify-help href="${page.requestHelpPathOrDefault()}&issueType=model-identification">Request a model check <span>↗</span></a></div>
        <p class="fv-photo-safety">Use accessible views only. Leave covers, test connections, isolation and disassembly to qualified personnel.</p>
    </div>
</section>

<section class="fv-outcome-section"><div class="bv-shell fv-outcome-grid">@template.components.review-format()<div class="fv-outcome-example"><p class="fv-eyebrow">Example structure / illustrative only</p><h3>“The model looks close.<br>What do I buy?”</h3><dl><dt>Confirmed from the submission</dt><dd>A readable label says 975XL2; the size marking is not visible.</dd><dt>Candidate reference</dt><dd>The manufacturer's 975XL / XL2 component matrix is relevant. A size-specific kit cannot yet be selected.</dd><dt>Still unknown</dt><dd>Nominal size, failed component and whether hard parts are damaged.</dd><dt>Next action</dt><dd>Photograph the size marking and obtain the qualified test result before ordering a kit.</dd></dl><p>Illustrative example, not a customer case or a diagnosis of your device.</p></div></div></section>

<section class="fv-model-index">
    <div class="bv-shell fv-model-index-grid"><div><p class="fv-eyebrow">Read the body label</p><h2>Start with the name<br>on your device.</h2><p>Model-specific parts, size bands and manufacturer references. Keep every suffix.</p></div><div class="fv-model-links">@for (var model : modelGuides)<a href="${model.canonicalPath()}"><span>${model.manufacturer()}</span><strong>${model.model()}</strong><span aria-hidden="true">↗</span></a>@endfor</div></div>
</section>
