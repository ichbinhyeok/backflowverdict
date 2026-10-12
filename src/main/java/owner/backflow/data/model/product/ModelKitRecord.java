package owner.backflow.data.model.product;

public record ModelKitRecord(
        String sizeBand,
        String task,
        String partNumber,
        String kitName,
        String contents,
        String compatibility,
        java.util.List<ModelMerchantLink> merchants
) {
    public ModelKitRecord {
        merchants = merchants == null ? java.util.List.of() : java.util.List.copyOf(merchants);
    }
}
