package owner.backflow.service;

import java.time.LocalDateTime;
import java.util.Locale;

public record LeadRecord(
        String leadId,
        LocalDateTime capturedAt,
        String fullName,
        String phone,
        String email,
        String city,
        String utilityId,
        String utilityName,
        String propertyType,
        String issueType,
        String pageFamily,
        String notes,
        String sourcePage,
        String referrer,
        String submittedUtilityId,
        String submittedUtilityName,
        String submittedPageFamily,
        String submittedSourcePage,
        String routingStatus,
        String routingReason
) {
    public boolean hasTrustedUtility() {
        return utilityId != null && !utilityId.isBlank();
    }

    public boolean autoRouteEligible() {
        return "AUTO_ROUTE_ELIGIBLE".equalsIgnoreCase(routingStatus);
    }

    public String displayUtility() {
        if (utilityName != null && !utilityName.isBlank()) {
            return utilityName;
        }
        if (utilityId != null && !utilityId.isBlank()) {
            return utilityId;
        }
        if (submittedUtilityName != null && !submittedUtilityName.isBlank()) {
            return submittedUtilityName;
        }
        return submittedUtilityId == null ? "" : submittedUtilityId;
    }

    public String displayPropertyType() {
        return humanize(propertyType);
    }

    public String displayIssueType() {
        return humanize(issueType);
    }

    public String displayPageFamily() {
        return humanize(pageFamily != null && !pageFamily.isBlank() ? pageFamily : submittedPageFamily);
    }

    public String displaySourcePage() {
        if (sourcePage != null && !sourcePage.isBlank()) {
            return sourcePage;
        }
        return submittedSourcePage == null ? "" : submittedSourcePage;
    }

    public String displayRoutingStatus() {
        return humanize(routingStatus);
    }

    private String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "unspecified";
        }
        String spaced = value.replace('-', ' ').replace('_', ' ').toLowerCase(Locale.US);
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
