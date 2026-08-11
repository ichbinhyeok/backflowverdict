package owner.backflow.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.List;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.product.ModelGuideRecord;
import owner.backflow.files.ModelGuideService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ModelGuideController {
    private final ModelGuideService modelGuideService;
    private final AppSiteProperties siteProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ModelGuideController(ModelGuideService modelGuideService, AppSiteProperties siteProperties) {
        this.modelGuideService = modelGuideService;
        this.siteProperties = siteProperties;
    }

    @GetMapping("/models/{slug:[a-z0-9-]+}/")
    public String modelGuide(@PathVariable String slug, Model model) {
        ModelGuideRecord guide = modelGuideService.findPublished(slug)
                .orElseThrow(() -> new NotFoundException("Model guide not found."));
        model.addAttribute("page", pageMeta(
                guide.title() + " | BackflowVerdict",
                guide.description(),
                guide.canonicalPath(),
                !guide.indexable(),
                structuredData(guide),
                guide.lastReviewed()
        ));
        model.addAttribute("modelGuide", guide);
        return "pages/model-guide";
    }

    @GetMapping("/models/{slug:[a-z0-9-]+}")
    public RedirectView modelGuideRedirect(@PathVariable String slug) {
        if (modelGuideService.findPublished(slug).isEmpty()) {
            throw new NotFoundException("Model guide not found.");
        }
        RedirectView redirect = new RedirectView("/models/" + slug + "/");
        redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirect.setExposeModelAttributes(false);
        return redirect;
    }

    @GetMapping("/backflow-preventer-repair-kits/")
    public String repairKitFinder(Model model) {
        List<ModelGuideRecord> guides = modelGuideService.listPublished();
        model.addAttribute("page", pageMeta(
                "Backflow preventer repair kits by model and size | BackflowVerdict",
                "Find backflow preventer repair kits by manufacturer, exact model, nominal size, and repair scope using official model records and part references.",
                "/backflow-preventer-repair-kits/",
                false,
                repairKitFinderStructuredData(guides),
                "2026-08-11"
        ));
        model.addAttribute("modelGuides", guides);
        return "pages/repair-kit-finder";
    }

    @GetMapping("/backflow-preventer-repair-kits")
    public RedirectView repairKitFinderRedirect() {
        RedirectView redirect = new RedirectView("/backflow-preventer-repair-kits/");
        redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirect.setExposeModelAttributes(false);
        return redirect;
    }

    private PageMeta pageMeta(String title, String description, String path, boolean noindex, String structuredData, String modified) {
        String base = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().replaceAll("/+$", "");
        return new PageMeta(title, description, base + path, noindex, structuredData)
                .withDateModified(LocalDate.parse(modified));
    }

    private String structuredData(ModelGuideRecord guide) {
        String base = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().replaceAll("/+$", "");
        ObjectNode root = objectMapper.createObjectNode();
        root.put("@context", "https://schema.org");
        ArrayNode graph = root.putArray("@graph");
        ObjectNode article = graph.addObject();
        article.put("@type", "TechArticle");
        article.put("headline", guide.title());
        article.put("description", guide.description());
        article.put("dateModified", guide.lastReviewed());
        article.put("mainEntityOfPage", base + guide.canonicalPath());
        article.putObject("about")
                .put("@type", "Product")
                .put("name", guide.manufacturer() + " " + guide.model())
                .put("manufacturer", guide.manufacturer());
        ArrayNode citations = article.putArray("citation");
        guide.sources().forEach(source -> citations.add(source.url()));
        ObjectNode breadcrumbs = graph.addObject();
        breadcrumbs.put("@type", "BreadcrumbList");
        ArrayNode items = breadcrumbs.putArray("itemListElement");
        items.addObject().put("@type", "ListItem").put("position", 1).put("name", "Home").put("item", base + "/");
        items.addObject().put("@type", "ListItem").put("position", 2).put("name", "Model library").put("item", base + "/backflow-library/");
        items.addObject().put("@type", "ListItem").put("position", 3).put("name", guide.model()).put("item", base + guide.canonicalPath());
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not build model guide structured data", exception);
        }
    }

    private String repairKitFinderStructuredData(List<ModelGuideRecord> guides) {
        String base = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().replaceAll("/+$", "");
        ObjectNode root = objectMapper.createObjectNode();
        root.put("@context", "https://schema.org");
        ArrayNode graph = root.putArray("@graph");

        ObjectNode page = graph.addObject();
        page.put("@type", "CollectionPage");
        page.put("name", "Backflow preventer repair kits by model and size");
        page.put("description", "Match official backflow preventer repair-kit references by manufacturer, model, nominal size, and repair scope.");
        page.put("dateModified", "2026-08-11");
        page.put("url", base + "/backflow-preventer-repair-kits/");
        ObjectNode itemList = page.putObject("mainEntity");
        itemList.put("@type", "ItemList");
        itemList.put("numberOfItems", guides.size());
        ArrayNode elements = itemList.putArray("itemListElement");
        for (int index = 0; index < guides.size(); index++) {
            ModelGuideRecord guide = guides.get(index);
            elements.addObject()
                    .put("@type", "ListItem")
                    .put("position", index + 1)
                    .put("name", guide.manufacturer() + " " + guide.model() + " repair kits")
                    .put("url", base + guide.canonicalPath());
        }

        ObjectNode faq = graph.addObject();
        faq.put("@type", "FAQPage");
        ArrayNode questions = faq.putArray("mainEntity");
        addFaq(questions, "How do I find the correct backflow preventer repair kit?",
                "Match the complete manufacturer and model label, nominal body size, and confirmed repair scope to the official model record before using a part number.");
        addFaq(questions, "Are backflow preventer repair kits universal?",
                "No. Similar-looking assemblies and adjacent size bands can use different checks, relief components, seats, seals, covers, and part-number prefixes.");
        addFaq(questions, "Does installing a repair kit complete the compliance work?",
                "Not necessarily. A regulated assembly may still require qualified installation, a passing retest, and report submission to the governing utility or authority.");

        ObjectNode breadcrumbs = graph.addObject();
        breadcrumbs.put("@type", "BreadcrumbList");
        ArrayNode items = breadcrumbs.putArray("itemListElement");
        items.addObject().put("@type", "ListItem").put("position", 1).put("name", "Home").put("item", base + "/");
        items.addObject().put("@type", "ListItem").put("position", 2).put("name", "Repair kits").put("item", base + "/backflow-preventer-repair-kits/");

        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not build repair-kit finder structured data", exception);
        }
    }

    private void addFaq(ArrayNode questions, String question, String answer) {
        ObjectNode item = questions.addObject();
        item.put("@type", "Question");
        item.put("name", question);
        item.putObject("acceptedAnswer").put("@type", "Answer").put("text", answer);
    }
}
