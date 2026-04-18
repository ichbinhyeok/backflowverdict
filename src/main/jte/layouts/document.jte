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
    <link rel="stylesheet" href="/app.css">
    @if (SiteRuntimeConfig.googleAnalyticsMeasurementId() != null && !SiteRuntimeConfig.googleAnalyticsMeasurementId().isBlank())
        <script async src="https://www.googletagmanager.com/gtag/js?id=${SiteRuntimeConfig.googleAnalyticsMeasurementId()}"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${SiteRuntimeConfig.googleAnalyticsMeasurementId()}');
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
<body class="document-body">
    <main class="document-main">
        ${content}
    </main>
</body>
</html>
