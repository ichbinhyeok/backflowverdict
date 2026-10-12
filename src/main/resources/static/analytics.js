(function () {
    'use strict';
    var path = window.location.pathname;
    var guide = document.querySelector('[data-guide-slug]');
    var model = document.querySelector('[data-model-guide]');
    var referral = new URL(window.location.href).searchParams.get('ref');
    referral = referral && /^[a-z0-9-]{1,48}$/i.test(referral) ? referral : undefined;
    var group = guide ? 'decision_guides' : model ? 'model_guides' : path === '/backflow-preventer-repair-kits/' ? 'repair_kit_finder' : /^\/utilities\//.test(path) ? 'utility_records' : /^\/cities\//.test(path) ? 'city_pages' : /^\/states\//.test(path) ? 'state_pages' : /^\/providers\//.test(path) ? 'provider_directory' : /submit-backflow-report|notice-finder/.test(path) ? 'submission_routes' : path === '/' ? 'home' : 'support';
    function track(name, params) {
        if (typeof window.gtag !== 'function') return;
        window.gtag('event', name, Object.assign({content_group: group, page_path: path, referral_code: referral, transport_type: 'beacon'}, params || {}));
    }
    function destination(raw) {
        try {
            var url = new URL(raw, window.location.origin);
            if (!/^https?:$/.test(url.protocol)) return null;
            return {destination_host: url.hostname, destination_path: url.pathname};
        } catch (error) { return null; }
    }
    track('page_context', {guide_slug: guide ? guide.dataset.guideSlug : undefined, guide_type: guide ? guide.dataset.guideType : undefined, model_slug: model ? model.dataset.modelGuide : undefined});
    document.addEventListener('click', function (event) {
        var anchor = event.target.closest('a[href]');
        if (!anchor) {
            var custom = event.target.closest('[data-funnel-event]');
            if (custom && custom.tagName === 'BUTTON') track(custom.dataset.funnelEvent);
            return;
        }
        if (anchor.dataset.funnelEvent) { track(anchor.dataset.funnelEvent, {role: anchor.dataset.shareRole}); return; }
        var url;
        try { url = new URL(anchor.href, window.location.origin); } catch (error) { return; }
        var params = destination(url.href);
        if (!params) return;
        var internal = url.origin === window.location.origin;
        var kind = anchor.dataset.linkKind;
        if (kind === 'merchant') {
            track('merchant_outbound_click', Object.assign(params, {part_number: anchor.dataset.partNumber}));
        } else if (kind === 'manufacturer' || (!internal && /(^|\.)(watts\.com|zurn\.com)$/.test(url.hostname))) {
            track('manufacturer_source_click', params);
        } else if (kind === 'official' || (!internal && (/(^|\.)epa\.gov$/.test(url.hostname) || anchor.classList.contains('resource-item') || anchor.closest('.bv-evidence, .bv-source-ledger, .source-block') || /official/i.test(anchor.textContent)))) {
            track('official_source_click', params);
        } else if (internal && url.pathname === '/r/cta') {
            var target = destination(url.searchParams.get('next'));
            if (target) track('provider_website_click', target);
        } else if (kind === 'provider') {
            track('provider_website_click', params);
        } else if (internal && /^\/(get-help\/?|leads\/new)$/.test(url.pathname)) {
            track('request_help_click', params);
        } else if (internal && /approved.*tester|official.*tester|find-a-tester/.test(url.pathname)) {
            track('tester_route_click', Object.assign(params, {route_type: /approved|official/.test(url.pathname) ? 'official' : 'directory'}));
        } else if (internal && /submit-backflow-report/.test(url.pathname)) {
            track('report_submission_route_click', params);
        } else if (internal && /^\/(utilities\/|notice-finder)/.test(url.pathname)) {
            track('official_route_click', params);
        } else if (!internal) {
            track('reference_outbound_click', params);
        }
    });
    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!(form instanceof HTMLFormElement)) return;
        var action = new URL(form.action, window.location.origin).pathname;
        if (action === '/leads') track('lead_form_attempt');
        if (action === '/notice-finder') track('notice_finder_search');
    });
    // The server sets this one-request flash flag only after the record is saved.
    if (document.querySelector('[data-lead-saved="true"]')) track('lead_form_success');
}());
