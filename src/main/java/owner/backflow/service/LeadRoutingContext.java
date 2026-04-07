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
}
