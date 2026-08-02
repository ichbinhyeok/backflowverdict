@import owner.backflow.web.PageMeta
@import owner.backflow.config.SiteRuntimeConfig
@import gg.jte.Content
@param PageMeta page
@param Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${page.title()}</title>
    <meta name="description" content="${page.description()}">
    <meta name="robots" content="${page.robots()}">
    <link rel="canonical" href="${page.canonicalUrl()}">
    <meta property="og:site_name" content="BackflowVerdict">
    <meta property="og:title" content="${page.title()}">
    <meta property="og:description" content="${page.description()}">
    <meta property="og:url" content="${page.canonicalUrl()}">
    <meta property="og:type" content="website">
    @if (page.socialImageUrl() != null)
        <meta property="og:image" content="${page.socialImageUrl()}">
        <meta property="og:image:secure_url" content="${page.socialImageUrl()}">
        <meta property="og:image:width" content="512">
        <meta property="og:image:height" content="512">
        <meta property="og:image:alt" content="${page.socialImageAltText()}">
        <meta name="twitter:image" content="${page.socialImageUrl()}">
        <meta name="twitter:image:alt" content="${page.socialImageAltText()}">
    @endif
    @if (page.dateModified() != null)
        <meta property="article:modified_time" content="${page.dateModified().toString()}">
    @endif
    <meta name="twitter:card" content="${page.twitterCard()}">
    <link rel="stylesheet" href="/verdict.css?v=20260802">
    <script defer src="/verdict.js?v=20260802c"></script>
    @if (SiteRuntimeConfig.googleAnalyticsMeasurementId() != null && !SiteRuntimeConfig.googleAnalyticsMeasurementId().isBlank())
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${SiteRuntimeConfig.googleAnalyticsMeasurementId()}');
            window.addEventListener('load', function () {
                var loadAnalytics = function () {
                    var script = document.createElement('script');
                    script.async = true;
                    script.src = 'https://www.googletagmanager.com/gtag/js?id=${SiteRuntimeConfig.googleAnalyticsMeasurementId()}';
                    document.head.appendChild(script);
                };
                if ('requestIdleCallback' in window) window.requestIdleCallback(loadAnalytics, {timeout: 2500});
                else window.setTimeout(loadAnalytics, 1200);
            }, {once: true});
        </script>
    @endif
    @if (SiteRuntimeConfig.organizationStructuredData() != null)
        <script type="application/ld+json">$unsafe{SiteRuntimeConfig.organizationStructuredData()}</script>
    @endif
    @if (SiteRuntimeConfig.websiteStructuredData() != null)
        <script type="application/ld+json">$unsafe{SiteRuntimeConfig.websiteStructuredData()}</script>
    @endif
    @if (page.structuredDataJson() != null)
        <script type="application/ld+json">$unsafe{page.structuredDataJson()}</script>
    @endif
</head>
<body class="bv-body">
    <a class="bv-skip" href="#main">Skip to main content</a>
    <header class="bv-header">
        <div class="bv-shell bv-header__inner">
            <a class="bv-brand" href="/">
                <svg viewBox="0 0 40 40" aria-hidden="true"><path d="M8 9h14v7h10v15H18v-7H8z"/><path d="M14 12v16M24 19v9"/></svg>
                <span><strong>BackflowVerdict</strong><small>Identify · decide · verify</small></span>
            </a>
            <nav class="bv-nav" aria-label="Primary navigation">
                <a href="/backflow-library/">Library</a>
                <a href="/backflow-preventer-repair-kits/">Repair kits</a>
                <a href="/backflow-preventer-leaking/">Problems</a>
                <a href="/water-pressure-regulator/">Water pressure</a>
                <a href="/backflow-test/">Testing</a>
                <a href="/submit-backflow-report">Submit report</a>
            </nav>
            <a class="bv-header__action" href="/notice-finder">Find my utility</a>
            <details class="bv-menu">
                <summary>Menu</summary>
                <nav aria-label="Mobile navigation">
                    <a href="/backflow-library/">Browse the library</a>
                    <a href="/backflow-preventer-repair-kits/">Find a repair kit</a>
                    <a href="/backflow-preventer-leaking/">Diagnose a leak</a>
                    <a href="/water-pressure-regulator/">Check water pressure</a>
                    <a href="/failed-backflow-test/">Failed test</a>
                    <a href="/backflow-test/">Testing</a>
                    <a href="/notice-finder">Find my utility</a>
                    <a href="/submit-backflow-report">Submit report</a>
                    <a href="/get-help/">Get help</a>
                </nav>
            </details>
        </div>
    </header>
    <main id="main" class="bv-main">${content}</main>
    <footer class="bv-footer">
        <div class="bv-shell bv-footer__grid">
            <div><strong>BackflowVerdict</strong><p>National decision guidance connected to source-backed local utility records.</p></div>
            <div><span>Decide</span><a href="/backflow-library/">Browse library</a><a href="/backflow-preventer-repair-kits/">Find repair kit</a><a href="/backflow-preventer/">Identify device</a><a href="/water-pressure-regulator/">Check pressure</a><a href="/backflow-repair-or-replace/">Repair or replace</a><a href="/failed-backflow-test/">Failed test</a></div>
            <div><span>Verify</span><a href="/notice-finder">Find utility</a><a href="/official-backflow-tester-lists">Official tester lists</a><a href="/submit-backflow-report">Report submission</a></div>
            <div><span>Trust</span><a href="/about">About</a><a href="/methodology">Methodology</a><a href="/editorial-standards">Editorial standards</a><a href="/corrections">Corrections</a><a href="/contact">Contact</a><a href="/privacy">Privacy</a></div>
        </div>
        <div class="bv-shell bv-footer__legal">Guidance is not a remote diagnosis. Local requirements come from the governing utility.</div>
    </footer>
</body>
</html>
