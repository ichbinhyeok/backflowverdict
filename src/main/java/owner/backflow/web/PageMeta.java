package owner.backflow.web;

import java.net.URI;

public record PageMeta(
        String title,
        String description,
        String canonicalUrl,
        boolean noindex,
        String structuredDataJson,
        String socialImageUrl,
        String openGraphType,
        String requestHelpPath
) {
    public PageMeta(String title, String description, String canonicalUrl, boolean noindex) {
        this(title, description, canonicalUrl, noindex, null, null, null, null);
    }

    public PageMeta(String title, String description, String canonicalUrl, boolean noindex, String structuredDataJson) {
        this(title, description, canonicalUrl, noindex, structuredDataJson, null, null, null);
    }

    public PageMeta(
            String title,
            String description,
            String canonicalUrl,
            boolean noindex,
            String structuredDataJson,
            String socialImageUrl,
            String openGraphType
    ) {
        this(title, description, canonicalUrl, noindex, structuredDataJson, socialImageUrl, openGraphType, null);
    }

    public String robots() {
        return noindex ? "noindex,follow" : "index,follow";
    }

    public String socialImageUrl() {
        if (socialImageUrl != null && !socialImageUrl.isBlank()) {
            return socialImageUrl;
        }
        if (canonicalUrl == null || canonicalUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(canonicalUrl);
            if (uri.getScheme() == null || uri.getAuthority() == null) {
                return null;
            }
            return uri.getScheme() + "://" + uri.getAuthority() + "/images/design/home-hero-industrial.jpg";
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public String openGraphType() {
        return openGraphType == null || openGraphType.isBlank() ? "website" : openGraphType;
    }

    public String twitterCard() {
        return socialImageUrl() == null ? "summary" : "summary_large_image";
    }

    public String requestHelpPathOrDefault() {
        return RequestHelpPaths.defaultPath(this);
    }

    public PageMeta withRequestHelpPath(String value) {
        return new PageMeta(
                title,
                description,
                canonicalUrl,
                noindex,
                structuredDataJson,
                socialImageUrl,
                openGraphType,
                value
        );
    }

    public PageMeta withNoindex(boolean forcedNoindex) {
        return new PageMeta(
                title,
                description,
                canonicalUrl,
                forcedNoindex,
                structuredDataJson,
                socialImageUrl,
                openGraphType,
                requestHelpPath
        );
    }
}
