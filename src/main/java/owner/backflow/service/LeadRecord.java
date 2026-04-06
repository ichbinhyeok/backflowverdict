package owner.backflow.service;

import java.time.LocalDateTime;

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
        String referrer
) {
    public String displayUtility() {
        return utilityName != null && !utilityName.isBlank() ? utilityName : utilityId;
    }

    public String displayPropertyType() {
        return humanize(propertyType);
    }

    public String displayIssueType() {
        return humanize(issueType);
    }

    public String displayPageFamily() {
        return humanize(pageFamily);
    }

    private String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "unspecified";
        }
        String spaced = value.replace('-', ' ').replace('_', ' ');
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
