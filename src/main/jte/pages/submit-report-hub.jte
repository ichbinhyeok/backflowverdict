@import owner.backflow.web.PageMeta
@import owner.backflow.web.FaqItem
@import owner.backflow.web.PriorityRoute
@import owner.backflow.data.model.CityAliasRecord
@import owner.backflow.data.model.GuideRecord
@import owner.backflow.data.model.UtilityRecord
@param PageMeta page
@param java.util.List<UtilityRecord> utilities
@param java.util.Map<String, java.util.List<CityAliasRecord>> cityAliasesByUtility = java.util.Map.of()
@param java.util.Map<String, String> noticeIdentifierHints = java.util.Map.of()
@param java.util.Map<String, String> reportAcceptanceHints = java.util.Map.of()
@param java.util.Map<String, String> portalNamesByUtility = java.util.Map.of()
@param java.util.Map<String, String> portalHubPathsByUtility = java.util.Map.of()
@param java.util.List<FaqItem> faqItems = java.util.List.of()
@param java.util.List<PriorityRoute> priorityRoutes = java.util.List.of()
@param java.util.List<GuideRecord> relatedGuides = java.util.List.of()

@template.layouts.verdict(page = page, content = @`
    <section class="bv-submit-hero">
        <div class="bv-shell bv-submit-hero__grid">
            <div><p class="bv-kicker">Official report route</p><h1>Find where your backflow test report must be submitted</h1><p class="bv-lede">A portal receives the report. Your utility sets the rule.</p></div>
            <div class="bv-submit-rule"><span>Do not stop at “passed”</span><p>Confirm who may submit, which identifier is required, and what proves acceptance.</p></div>
        </div>
    </section>
    <section class="bv-section bv-local">
        <div class="bv-shell bv-local__grid">
            <div><p class="bv-kicker">Search ${utilities.size()} official workflows</p><h2>Start with the utility</h2><p>Use the name on the notice or water bill, not only the city where the property sits.</p></div>
            <div class="bv-lookup"><label for="report-filter">Utility, city, or portal</label><div><input id="report-filter" type="search" placeholder="Dallas, WEIRS, BSI..." autocomplete="off" data-route-filter><button type="button" data-route-clear>Clear</button></div><p class="bv-lookup-status" aria-live="polite"><span data-route-count>${utilities.size()}</span> of ${utilities.size()} verified workflows shown</p></div>
        </div>
    </section>
    <section class="bv-section">
        <div class="bv-shell">
            <div class="bv-route-head"><span>Utility</span><span>Official route and next action</span></div>
            <div class="bv-route-list" data-route-list>
                @for (var utility : utilities)
                    <article class="bv-route" data-search="${utility.utilityName()} ${utility.state()} ${portalNamesByUtility.getOrDefault(utility.utilityId(), "")}">
                        <div><span>${utility.state()}</span><h2>${utility.utilityName()}</h2><small>Verified ${utility.lastVerified().toString()}</small></div>
                        <div><strong>${utility.reportWorkflow().portalName() != null && !utility.reportWorkflow().portalName().isBlank() ? utility.reportWorkflow().portalName() : portalNamesByUtility.getOrDefault(utility.utilityId(), "Official utility workflow")}</strong><p>${noticeIdentifierHints.getOrDefault(utility.utilityId(), "Match the service address, device, notice, or account identifier.")}</p><p class="bv-acceptance"><span>Keep:</span> ${reportAcceptanceHints.getOrDefault(utility.utilityId(), "Proof that the report was submitted and accepted.")}</p><div class="bv-route__actions"><a href="/utilities/${utility.state()}/${utility.canonicalSlug()}/">Open utility record</a>@if (portalHubPathsByUtility.get(utility.utilityId()) != null)<a href="${portalHubPathsByUtility.get(utility.utilityId())}">Review portal family</a>@endif</div></div>
                    </article>
                @endfor
            </div>
            <p class="bv-empty" data-route-empty hidden>No matching workflow. Try the utility name from the water bill or use the notice finder.</p>
        </div>
    </section>
`)
