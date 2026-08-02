package owner.backflow.web;

import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.decision.DecisionEvidenceSource;
import owner.backflow.data.model.decision.DecisionGuideRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.files.DecisionEvidenceService;
import owner.backflow.files.DecisionGuideService;
import owner.backflow.files.ModelGuideService;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class DecisionGuideController {
    private final DecisionGuideService decisionGuideService;
    private final BackflowRegistryService registryService;
    private final DecisionEvidenceService evidenceService;
    private final AppSiteProperties siteProperties;
    private final ModelGuideService modelGuideService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DecisionGuideController(
            DecisionGuideService decisionGuideService,
            BackflowRegistryService registryService,
            DecisionEvidenceService evidenceService,
            AppSiteProperties siteProperties,
            ModelGuideService modelGuideService
    ) {
        this.decisionGuideService = decisionGuideService;
        this.registryService = registryService;
        this.evidenceService = evidenceService;
        this.siteProperties = siteProperties;
        this.modelGuideService = modelGuideService;
    }

    @GetMapping("/backflow-library/")
    public String library(Model model) {
        String base = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().replaceAll("/+$", "");
        List<DecisionGuideRecord> guides = decisionGuideService.listAvailable();
        model.addAttribute("page", new PageMeta(
                "Backflow device, problem, and repair library | BackflowVerdict",
                "Browse backflow devices, failure symptoms, testing workflows, installation decisions, and repair paths by the job you need to complete.",
                base + "/backflow-library/",
                false
        ).withDateModified(java.time.LocalDate.of(2026, 8, 2)));
        model.addAttribute("guides", guides);
        model.addAttribute("modelGuides", modelGuideService.listPublished());
        return "pages/decision-library";
    }

    @GetMapping("/backflow-library")
    public RedirectView libraryRedirect() {
        RedirectView redirect = new RedirectView("/backflow-library/");
        redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirect.setExposeModelAttributes(false);
        return redirect;
    }

    @GetMapping("/{slug:[a-z0-9-]+}/")
    public String screen(
            @PathVariable String slug,
            @RequestParam(name = "utility", required = false) String utilityId,
            Model model
    ) {
        return render(slug, utilityId, model);
    }

    @GetMapping("/{slug:[a-z0-9-]+}")
    public RedirectView canonicalRedirect(@PathVariable String slug) {
        if (decisionGuideService.findPublished(slug).isEmpty()) {
            throw new NotFoundException("Decision guide not found.");
        }
        RedirectView redirect = new RedirectView("/" + slug + "/");
        redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        redirect.setExposeModelAttributes(false);
        return redirect;
    }

    private String render(String slug, String utilityId, Model model) {
        DecisionGuideRecord guide = decisionGuideService.findPublished(slug)
                .orElseThrow(() -> new NotFoundException("Decision guide not found."));
        String base = siteProperties.baseUrl() == null ? "" : siteProperties.baseUrl().replaceAll("/+$", "");
        List<DecisionEvidenceSource> evidenceSources = evidenceService.forGuide(guide);
        model.addAttribute("page", new PageMeta(
                guide.title() + " | BackflowVerdict",
                guide.description(),
                base + guide.canonicalPath(),
                !guide.indexable(),
                structuredData(guide, evidenceSources, base)
        ).withDateModified(java.time.LocalDate.parse(guide.lastReviewed())));
        model.addAttribute("guide", guide);
        model.addAttribute("evidenceSources", evidenceSources);
        var utilities = registryService.listPublishedUtilities();
        var selectedUtility = Optional.ofNullable(utilityId)
                .flatMap(registryService::findUtilityById)
                .orElse(null);
        model.addAttribute("utilities", utilities);
        model.addAttribute("selectedUtility", selectedUtility);
        return "pages/decision-guide";
    }

    private String structuredData(DecisionGuideRecord guide, List<DecisionEvidenceSource> sources, String base) {
        ObjectNode graph = objectMapper.createObjectNode();
        graph.put("@context", "https://schema.org");
        ArrayNode items = graph.putArray("@graph");

        ObjectNode article = items.addObject();
        article.put("@type", "Article");
        article.put("headline", guide.title());
        article.put("description", guide.description());
        article.put("dateModified", guide.lastReviewed());
        article.put("mainEntityOfPage", base + guide.canonicalPath());
        article.putObject("publisher").put("@type", "Organization").put("name", siteProperties.siteName());
        ArrayNode citations = article.putArray("citation");
        sources.forEach(source -> citations.add(source.url()));

        ObjectNode breadcrumbs = items.addObject();
        breadcrumbs.put("@type", "BreadcrumbList");
        ArrayNode list = breadcrumbs.putArray("itemListElement");
        list.addObject().put("@type", "ListItem").put("position", 1).put("name", "Home").put("item", base + "/");
        list.addObject().put("@type", "ListItem").put("position", 2).put("name", guide.title()).put("item", base + guide.canonicalPath());
        try {
            return objectMapper.writeValueAsString(graph);
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalStateException("Could not build decision guide structured data", exception);
        }
    }
}
