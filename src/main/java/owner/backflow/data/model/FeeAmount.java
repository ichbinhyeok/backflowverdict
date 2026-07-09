package owner.backflow.data.model;

import java.util.List;

public record FeeAmount(
        String amount,
        String currency,
        String appliesTo,
        List<String> sourceRefs
) {
    public FeeAmount {
        currency = currency == null || currency.isBlank() ? "USD" : currency;
        sourceRefs = sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
    }

    public boolean hasContent() {
        return !isBlank(amount) || !isBlank(appliesTo);
    }

    public String display() {
        if (isBlank(amount)) {
            return appliesTo == null ? "" : appliesTo;
        }
        if (isBlank(appliesTo)) {
            return amount;
        }
        return amount + " - " + appliesTo;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
