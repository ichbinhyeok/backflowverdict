package owner.backflow.web;

import java.net.URI;
import owner.backflow.service.LeadRoutingService;

public final class RequestHelpPaths {
    private RequestHelpPaths() {
    }

    public static String defaultPath(PageMeta page) {
        if (page == null) {
            return "/leads/new";
        }
        String explicit = normalize(page.requestHelpPath());
        if (!explicit.isBlank()) {
            return explicit;
        }
        String sourcePath = sourcePath(page.canonicalUrl());
        String pageFamily = pageFamily(sourcePath);
        if (sourcePath.isBlank() || pageFamily.isBlank()) {
            return "/leads/new";
        }
        return LeadRoutingService.requestHelpPath("", sourcePath, "", pageFamily);
    }

    static String sourcePath(String canonicalUrl) {
        String normalized = normalize(canonicalUrl);
        if (normalized.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(normalized);
            String path = normalize(uri.getPath());
            return path.startsWith("/") ? path : "";
        } catch (IllegalArgumentException exception) {
            return normalized.startsWith("/") ? normalized : "";
        }
    }

    static String pageFamily(String sourcePath) {
        String normalized = normalize(sourcePath);
        if (normalized.isBlank()) {
            return "";
        }
        if ("/".equals(normalized)) {
            return "home";
        }
        if (normalized.startsWith("/states/") && normalized.endsWith("/backflow-testing")) {
            return "state-guide";
        }
        if ("/states".equals(normalized)) {
            return "state-index";
        }
        if (normalized.startsWith("/metros/") && normalized.endsWith("/backflow-testing")) {
            return "metro";
        }
        if ("/metros".equals(normalized)) {
            return "metro-index";
        }
        if (normalized.startsWith("/guides/")) {
            return "guide";
        }
        if ("/guides".equals(normalized)) {
            return "guide-index";
        }
        if (normalized.startsWith("/providers/")) {
            return "provider-profile";
        }
        if (normalized.startsWith("/utilities/") && normalized.endsWith("/approved-testers")) {
            return "tester-directory";
        }
        if (normalized.startsWith("/utilities/") && normalized.endsWith("/find-a-tester")) {
            return "tester-directory";
        }
        if (normalized.startsWith("/utilities/") && normalized.endsWith("/failed-test")) {
            return "failed-test";
        }
        if (normalized.startsWith("/utilities/")) {
            return "utility";
        }
        if (normalized.startsWith("/vendors/")) {
            return "vendor";
        }
        if ("/contact".equals(normalized)) {
            return "contact";
        }
        return "site";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
