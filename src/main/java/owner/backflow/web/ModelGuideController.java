package owner.backflow.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
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
        model.addAttribute("page", pageMeta(
                "Backflow preventer repair kit finder | BackflowVerdict",
                "Match a backflow preventer manufacturer, model, size band, and repair task to an official repair-kit path.",
                "/backflow-preventer-repair-kits/",
                false,
                null,
                "2026-08-02"
        ));
        model.addAttribute("modelGuides", modelGuideService.listPublished());
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
}
