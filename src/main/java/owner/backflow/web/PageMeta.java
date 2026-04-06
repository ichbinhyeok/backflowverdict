package owner.backflow.web;

public record PageMeta(
        String title,
        String description,
        String canonicalUrl,
        boolean noindex,
        String structuredDataJson
) {
    public PageMeta(String title, String description, String canonicalUrl, boolean noindex) {
        this(title, description, canonicalUrl, noindex, null);
    }

    public String robots() {
        return noindex ? "noindex,follow" : "index,follow";
    }
}
