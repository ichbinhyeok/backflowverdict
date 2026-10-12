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
    <meta property="og:site_name" content="${SiteRuntimeConfig.siteName()}">
    <meta property="og:title" content="${page.title()}">
    <meta property="og:description" content="${page.description()}">
    <meta property="og:url" content="${page.canonicalUrl()}">
    <meta property="og:type" content="${page.openGraphType()}">
    <meta property="og:locale" content="en_US">
    @if (page.socialImageUrl() != null)
        <meta property="og:image" content="${page.socialImageUrl()}">
        <meta property="og:image:secure_url" content="${page.socialImageUrl()}">
        <meta property="og:image:width" content="512">
        <meta property="og:image:height" content="512">
        <meta property="og:image:alt" content="${page.socialImageAltText()}">
    @endif
    @if (page.dateModified() != null)
        <meta property="article:modified_time" content="${page.dateModified().toString()}">
    @endif
    <meta name="twitter:card" content="${page.twitterCard()}">
    <meta name="twitter:title" content="${page.title()}">
    <meta name="twitter:description" content="${page.description()}">
    @if (page.socialImageUrl() != null)
        <meta name="twitter:image" content="${page.socialImageUrl()}">
        <meta name="twitter:image:alt" content="${page.socialImageAltText()}">
    @endif
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined&icon_names=arrow_forward,assignment_turned_in,build,cloud_upload,menu,monitoring,policy,verified_user&display=block" rel="stylesheet">
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link rel="shortcut icon" href="/favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="/app.css?v=20260712">
    <script defer src="/analytics.js?v=20261012"></script>
    @if (SiteRuntimeConfig.googleAnalyticsMeasurementId() != null && !SiteRuntimeConfig.googleAnalyticsMeasurementId().isBlank())
        <script async src="https://www.googletagmanager.com/gtag/js?id=${SiteRuntimeConfig.googleAnalyticsMeasurementId()}"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${SiteRuntimeConfig.googleAnalyticsMeasurementId()}', {page_location: location.origin + location.pathname, page_referrer: document.referrer.split('?')[0].split('#')[0]});


        </script>
    @endif
    <script>
        document.addEventListener('click', async function (event) {
            var button = event.target.closest('[data-copy-target]');
            if (!button) {
                return;
            }

            var targetId = button.getAttribute('data-copy-target');
            if (!targetId) {
                return;
            }

            var target = document.getElementById(targetId);
            if (!target) {
                return;
            }

            var text = '';
            if (typeof target.value === 'string') {
                text = target.value;
            } else {
                text = target.textContent || '';
            }

            if (!text) {
                return;
            }

            var originalLabel = button.textContent;
            try {
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    await navigator.clipboard.writeText(text);
                } else {
                    target.focus();
                    if (typeof target.select === 'function') {
                        target.select();
                    }
                    document.execCommand('copy');
                }
                button.textContent = 'Copied';
                window.setTimeout(function () {
                    button.textContent = originalLabel;
                }, 1200);
            } catch (error) {
                button.textContent = 'Copy failed';
                window.setTimeout(function () {
                    button.textContent = originalLabel;
                }, 1500);
            }
        });

        document.addEventListener('click', async function (event) {
            var button = event.target.closest('[data-native-share]');
            if (!button) {
                return;
            }
            var target = document.getElementById(button.getAttribute('data-native-share'));
            if (!target) {
                return;
            }
            var message = target.value || target.textContent || '';
            var status = document.querySelector('[data-share-status]');
            try {
                if (navigator.share) {
                    await navigator.share({title: 'BackflowVerdict notice route', text: message});
                    if (status) {
                        status.textContent = 'Shared.';
                    }
                } else if (navigator.clipboard && navigator.clipboard.writeText) {
                    await navigator.clipboard.writeText(message);
                    if (status) {
                        status.textContent = 'Sharing is unavailable here, so the message was copied.';
                    }
                }
            } catch (error) {
                if (status && error.name !== 'AbortError') {
                    status.textContent = 'Could not open sharing. Use Copy message instead.';
                }
            }
        });
    </script>
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
<body class="site-body${page.canonicalUrl().endsWith("/partners/sample/customer-guide") ? " partner-demo-body" : ""}">
    <header class="site-header">
        <div class="container site-header__inner">
            <a class="brand" href="/">
                <span class="brand__copy">
                    <strong>BackflowVerdict</strong>
                    <span>Local backflow requirements</span>
                </span>
            </a>
            <div class="site-header__desktop">
                <nav class="site-nav" aria-label="Primary">
                    <a href="/">Home</a>
                    <a href="/states">States</a>
                    <a href="/metros">Metros</a>
                    <a href="/guides">Guides</a>
                    <a href="/notice-finder">Notice finder</a>
                    <a href="/submit-backflow-report">Submit reports</a>
                    <a href="/official-backflow-tester-lists">Tester lists</a>
                </nav>
                <div class="site-header__actions">
                    <a class="button button--primary button--compact" href="${page.requestHelpPathOrDefault()}">Request help</a>
                </div>
            </div>
            <details class="site-menu">
                <summary class="site-menu__toggle" aria-label="Open navigation menu">
                    <span>Menu</span>
                    <span class="material-symbols-outlined" aria-hidden="true">menu</span>
                </summary>
                <div class="site-menu__panel">
                    <nav class="site-menu__nav" aria-label="Mobile">
                        <a href="/">Home</a>
                        <a href="/states">States</a>
                        <a href="/metros">Metros</a>
                        <a href="/guides">Guides</a>
                        <a href="/notice-finder">Notice finder</a>
                        <a href="/submit-backflow-report">Submit reports</a>
                        <a href="/official-backflow-tester-lists">Tester lists</a>
                    </nav>
                    <a class="button button--primary button--compact site-menu__cta" href="${page.requestHelpPathOrDefault()}">Request help</a>
                </div>
            </details>
        </div>
    </header>

    <main class="site-main">
        ${content}
    </main>

    <footer class="site-footer">
        <div class="container footer-grid">
            <div class="footer-brand">
                <strong>BackflowVerdict</strong>
                <p>Find utility-specific backflow testing requirements, official tester routes, and clearly labeled provider options across representative states.</p>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Explore</span>
                <a href="/states">Browse state guides</a>
                <a href="/metros">Browse metro pages</a>
                <a href="/guides">Browse all guides</a>
                <a href="/notice-finder">Notice finder</a>
                <a href="/official-backflow-tester-lists">Official tester lists</a>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Popular help</span>
                <a href="/backflow-reporting-portals">Reporting portal hub</a>
                <a href="/partners/notice-kit">Free partner notice kit</a>
                <a href="/submit-backflow-report">Submit report hub</a>
                <a href="/official-backflow-tester-lists">Official tester list hub</a>
                <a href="/guides/approved-testers-vs-find-a-tester">Approved vs directory</a>
                <a href="/guides/backflow-reporting-portals">Reporting portals</a>
                <a href="/guides/residential-vs-commercial-backflow-rules">Residential vs commercial</a>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Get help</span>
                <a href="${page.requestHelpPathOrDefault()}">Request help</a>
                <a href="/notice-finder">Find from notice</a>
                <a href="/claim-listing">Claim listing</a>
                <a href="/contact">Contact</a>
                <a href="/privacy">Privacy</a>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Trust</span>
                <a href="/about">About</a>
                <a href="/methodology">Methodology</a>
                <a href="/editorial-standards">Editorial standards</a>
                <a href="/corrections">Corrections</a>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Reference</span>
                <a href="/guides/anniversary-date-vs-calendar-deadline">Due date basics</a>
                <a href="/guides/county-certified-vs-utility-approved-testers">Certification vs approval</a>
            </div>
        </div>
        <div class="container footer-legal">
            <span>BackflowVerdict</span>
            <span>Source-backed utility pages with official guidance and provider options kept separate.</span>
        </div>
    </footer>
</body>
</html>
