package owner.backflow.service;

public record LeadRoutingContext(
        String utilityId,
        String utilityName,
        String sourcePage,
        String pageFamily,
        String routingStatus,
        String routingReason
) {
    public boolean autoRouteEligible() {
        return "AUTO_ROUTE_ELIGIBLE".equalsIgnoreCase(routingStatus);
    }

    public boolean sourceContextVerified() {
        return !sourcePage.isBlank()
                && !pageFamily.isBlank()
                && !"HOLD_UNVERIFIED_CONTEXT".equalsIgnoreCase(routingStatus)
                && !"HOLD_CONTEXT_MISMATCH".equalsIgnoreCase(routingStatus);
    }
}
