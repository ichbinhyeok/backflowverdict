@import owner.backflow.web.PageMeta
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
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <link rel="icon" href="/favicon.svg" type="image/svg+xml">
    <link rel="shortcut icon" href="/favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="/app.css">
    @if (page.structuredDataJson() != null)
        <script type="application/ld+json">${page.structuredDataJson()}</script>
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
