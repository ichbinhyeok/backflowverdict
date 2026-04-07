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
    @if (page.socialImageUrl() != null)
        <meta property="og:image" content="${page.socialImageUrl()}">
    @endif
    <meta name="twitter:card" content="${page.twitterCard()}">
    <meta name="twitter:title" content="${page.title()}">
    <meta name="twitter:description" content="${page.description()}">
    @if (page.socialImageUrl() != null)
        <meta name="twitter:image" content="${page.socialImageUrl()}">
    @endif
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link rel="shortcut icon" href="/favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="/app.css">
    @if (!page.noindex() && SiteRuntimeConfig.googleAnalyticsMeasurementId() != null && !SiteRuntimeConfig.googleAnalyticsMeasurementId().isBlank())
        <script async src="https://www.googletagmanager.com/gtag/js?id=${SiteRuntimeConfig.googleAnalyticsMeasurementId()}"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${SiteRuntimeConfig.googleAnalyticsMeasurementId()}');

            (function () {
                function clean(value) {
                    if (value == null) {
                        return undefined;
                    }
                    var normalized = String(value).replace(/\s+/g, ' ').trim();
                    return normalized === '' ? undefined : normalized;
                }

                function withPageContext(params) {
                    var next = Object.assign({
                        page_path: window.location.pathname,
                        page_title: document.title,
                        transport_type: 'beacon'
                    }, params || {});

                    Object.keys(next).forEach(function (key) {
                        if (next[key] == null || next[key] === '') {
                            delete next[key];
                        }
                    });
                    return next;
                }

                function track(eventName, params) {
                    if (!eventName || typeof gtag !== 'function') {
                        return;
                    }
                    gtag('event', eventName, withPageContext(params));
                }

                function classifyLink(anchor) {
                    try {
                        var url = new URL(anchor.href, window.location.origin);
                        var sameOrigin = url.origin === window.location.origin;
                        var text = clean(anchor.textContent);

                        if (sameOrigin && url.pathname === '/leads/new') {
                            return {
                                eventName: 'request_help_click',
                                params: {
                                    utility_id: clean(url.searchParams.get('utilityId')),
                                    issue_type: clean(url.searchParams.get('issueType')),
                                    page_family: clean(url.searchParams.get('pageFamily')),
                                    source_page: clean(url.searchParams.get('source')),
                                    cta_text: text
                                }
                            };
                        }

                        if (sameOrigin && url.pathname === '/r/cta') {
                            return {
                                eventName: 'provider_website_click',
                                params: {
                                    provider_id: clean(url.searchParams.get('providerId')),
                                    utility_id: clean(url.searchParams.get('utilityId')),
                                    page_family: clean(url.searchParams.get('pageFamily')),
                                    cta_type: clean(url.searchParams.get('ctaType')),
                                    destination: clean(url.searchParams.get('next')),
                                    cta_text: text
                                }
                            };
                        }

                        if (sameOrigin && (url.pathname.indexOf('/approved-testers') >= 0 || url.pathname.indexOf('/find-a-tester') >= 0)) {
                            return {
                                eventName: 'tester_route_click',
                                params: {
                                    route_type: url.pathname.indexOf('/approved-testers') >= 0 ? 'approved' : 'directory',
                                    destination_path: url.pathname,
                                    cta_text: text
                                }
                            };
                        }

                        if (anchor.classList.contains('resource-item')) {
                            return {
                                eventName: 'submission_method_click',
                                params: {
                                    destination: url.href,
                                    cta_text: text
                                }
                            };
                        }

                        if (!sameOrigin && text && text.toLowerCase().indexOf('official') >= 0) {
                            return {
                                eventName: 'official_program_click',
                                params: {
                                    destination: url.href,
                                    cta_text: text
                                }
                            };
                        }

                        if (sameOrigin && url.pathname.indexOf('/utilities/') === 0) {
                            return {
                                eventName: 'utility_page_click',
                                params: {
                                    destination_path: url.pathname,
                                    cta_text: text
                                }
                            };
                        }

                        if (sameOrigin && url.pathname.indexOf('/states/') === 0) {
                            return {
                                eventName: 'state_guide_click',
                                params: {
                                    destination_path: url.pathname,
                                    cta_text: text
                                }
                            };
                        }

                        if (sameOrigin && url.pathname.indexOf('/metros/') === 0) {
                            return {
                                eventName: 'metro_page_click',
                                params: {
                                    destination_path: url.pathname,
                                    cta_text: text
                                }
                            };
                        }

                        if (sameOrigin && url.pathname.indexOf('/guides/') === 0) {
                            return {
                                eventName: 'guide_click',
                                params: {
                                    destination_path: url.pathname,
                                    cta_text: text
                                }
                            };
                        }
                    } catch (error) {
                        return null;
                    }
                    return null;
                }

                document.addEventListener('click', function (event) {
                    var anchor = event.target.closest('a[href]');
                    if (!anchor) {
                        return;
                    }
                    var tracked = classifyLink(anchor);
                    if (tracked) {
                        track(tracked.eventName, tracked.params);
                    }
                });

                document.addEventListener('submit', function (event) {
                    if (!(event.target instanceof HTMLFormElement)) {
                        return;
                    }
                    var form = event.target;
                    var action = clean(form.getAttribute('action'));
                    if (action !== '/leads') {
                        return;
                    }

                    var formData = new FormData(form);
                    track('lead_form_submit', {
                        utility_id: clean(formData.get('utilityId')),
                        issue_type: clean(formData.get('issueType')),
                        page_family: clean(formData.get('pageFamily')),
                        property_type: clean(formData.get('propertyType')),
                        has_email: clean(formData.get('email')) ? 'yes' : 'no',
                        has_phone: clean(formData.get('phone')) ? 'yes' : 'no'
                    });
                });
            }());
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
<body class="site-body">
    <header class="site-header">
        <div class="container site-header__inner">
            <a class="brand" href="/">
                <span class="brand__copy">
                    <strong>BackflowPath</strong>
                    <span>Local backflow requirements</span>
                </span>
            </a>
            <div class="site-header__desktop">
                <nav class="site-nav" aria-label="Primary">
                    <a href="/">Home</a>
                    <a href="/states">States</a>
                    <a href="/metros">Metros</a>
                    <a href="/guides">Guides</a>
                </nav>
                <div class="site-header__actions">
                    <a class="button button--primary button--compact" href="/leads/new">Request help</a>
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
                    </nav>
                    <a class="button button--primary button--compact site-menu__cta" href="/leads/new">Request help</a>
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
                <strong>BackflowPath</strong>
                <p>Find utility-specific backflow testing requirements, official tester routes, and clearly labeled provider options across representative states.</p>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Explore</span>
                <a href="/states">Browse state guides</a>
                <a href="/metros">Browse metro pages</a>
                <a href="/guides">Browse all guides</a>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Popular help</span>
                <a href="/guides/approved-testers-vs-find-a-tester">Approved vs directory</a>
                <a href="/guides/backflow-reporting-portals">Reporting portals</a>
                <a href="/guides/residential-vs-commercial-backflow-rules">Residential vs commercial</a>
            </div>
            <div class="footer-column">
                <span class="footer-column__label">Get help</span>
                <a href="/leads/new">Request help</a>
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
            <span>BackflowPath</span>
            <span>Source-backed utility pages with official guidance and provider options kept separate.</span>
        </div>
    </footer>
</body>
</html>
