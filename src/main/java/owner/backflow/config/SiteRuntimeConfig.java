package owner.backflow.config;

import org.springframework.stereotype.Component;

@Component
public class SiteRuntimeConfig {
    private static volatile AppSiteProperties properties = new AppSiteProperties("", "BackflowPath", "", "", "");

    public SiteRuntimeConfig(AppSiteProperties properties) {
        SiteRuntimeConfig.properties = properties;
    }

    public static String siteName() {
        String configured = properties.siteName();
        return configured == null || configured.isBlank() ? "BackflowPath" : configured.trim();
    }

    public static String googleAnalyticsMeasurementId() {
        String configured = properties.gaMeasurementId();
        return configured == null ? "" : configured.trim();
    }

    public static String baseUrl() {
        String configured = properties.baseUrl();
        if (configured == null || configured.isBlank()) {
            return "";
        }
        return configured.trim().replaceAll("/+$", "");
    }

    public static String supportEmail() {
        String configured = properties.supportEmail();
        return configured == null ? "" : configured.trim();
    }

    public static String supportPhone() {
        String configured = properties.supportPhone();
        return configured == null ? "" : configured.trim();
    }

    public static String organizationStructuredData() {
        String siteBaseUrl = baseUrl();
        if (siteBaseUrl.isBlank()) {
            return null;
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"@context\":\"https://schema.org\",\"@type\":\"Organization\"")
                .append(",\"@id\":\"").append(jsonEscape(siteBaseUrl)).append("/#organization\"")
                .append(",\"name\":\"").append(jsonEscape(siteName())).append("\"")
                .append(",\"url\":\"").append(jsonEscape(siteBaseUrl)).append("\"")
                .append(",\"description\":\"")
                .append(jsonEscape("Source-backed backflow compliance guidance organized by utility, with official authority rules separated from provider routing."))
                .append("\"")
                .append(",\"publishingPrinciples\":\"").append(jsonEscape(siteBaseUrl + "/editorial-standards")).append("\"")
                .append(",\"correctionsPolicy\":\"").append(jsonEscape(siteBaseUrl + "/corrections")).append("\"")
                .append(",\"ethicsPolicy\":\"").append(jsonEscape(siteBaseUrl + "/methodology")).append("\"")
                .append(",\"contactPoint\":{")
                .append("\"@type\":\"ContactPoint\"")
                .append(",\"contactType\":\"customer support\"")
                .append(",\"url\":\"").append(jsonEscape(siteBaseUrl + "/contact")).append("\"");
        if (!supportEmail().isBlank()) {
            json.append(",\"email\":\"").append(jsonEscape(supportEmail())).append("\"");
        }
        if (!supportPhone().isBlank()) {
            json.append(",\"telephone\":\"").append(jsonEscape(supportPhone())).append("\"");
        }
        json.append("}}");
        return json.toString();
    }

    public static String websiteStructuredData() {
        String siteBaseUrl = baseUrl();
        if (siteBaseUrl.isBlank()) {
            return null;
        }

        return new StringBuilder()
                .append("{\"@context\":\"https://schema.org\",\"@type\":\"WebSite\"")
                .append(",\"@id\":\"").append(jsonEscape(siteBaseUrl)).append("/#website\"")
                .append(",\"name\":\"").append(jsonEscape(siteName())).append("\"")
                .append(",\"url\":\"").append(jsonEscape(siteBaseUrl)).append("\"")
                .append(",\"inLanguage\":\"en-US\"")
                .append(",\"publisher\":{\"@id\":\"").append(jsonEscape(siteBaseUrl)).append("/#organization\"}")
                .append(",\"about\":{\"@type\":\"WebPage\",\"url\":\"").append(jsonEscape(siteBaseUrl + "/about")).append("\"}")
                .append("}")
                .toString();
    }

    private static String jsonEscape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }
}
