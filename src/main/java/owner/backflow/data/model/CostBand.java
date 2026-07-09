package owner.backflow.data.model;

import java.util.List;

public record CostBand(
        String testingRange,
        String repairRetestRange,
        String pricingNotes,
        FeeAmount filingFee,
        FeeAmount portalFee,
        FeeAmount lateFee,
        FeeAmount fineMin,
        FeeAmount fineMax,
        String feeNotes,
        List<String> sourceRefs
) {
    public CostBand {
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
    }

    public CostBand(String testingRange, String repairRetestRange, String pricingNotes) {
        this(testingRange, repairRetestRange, pricingNotes, null, null, null, null, null, null, List.of());
    }

    public boolean hasStructuredFees() {
        return feeHasContent(filingFee)
                || feeHasContent(portalFee)
                || feeHasContent(lateFee)
                || feeHasContent(fineMin)
                || feeHasContent(fineMax)
                || (feeNotes != null && !feeNotes.isBlank());
    }

    private boolean feeHasContent(FeeAmount fee) {
        return fee != null && fee.hasContent();
    }
}
