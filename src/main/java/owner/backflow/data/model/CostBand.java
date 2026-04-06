package owner.backflow.data.model;

public record CostBand(
        String testingRange,
        String repairRetestRange,
        String pricingNotes
) {
}
