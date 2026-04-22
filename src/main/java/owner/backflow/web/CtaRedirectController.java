package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import owner.backflow.service.CtaClickRecord;
import owner.backflow.service.CtaDestinationPolicyService;
import owner.backflow.service.CtaClickRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CtaRedirectController {
    private final CtaClickRepository ctaClickRepository;
    private final CtaDestinationPolicyService ctaDestinationPolicyService;

    public CtaRedirectController(
            CtaClickRepository ctaClickRepository,
            CtaDestinationPolicyService ctaDestinationPolicyService
    ) {
        this.ctaClickRepository = ctaClickRepository;
        this.ctaDestinationPolicyService = ctaDestinationPolicyService;
    }

    @GetMapping("/r/cta")
    public RedirectView redirect(
            @RequestParam String next,
            @RequestParam(required = false) String pageFamily,
            @RequestParam(required = false) String utilityId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String ctaType,
            @RequestParam(required = false) String source,
            HttpServletRequest request
    ) {
        String destination = ctaDestinationPolicyService.validateDestination(next);
        ctaClickRepository.save(new CtaClickRecord(
                null,
                LocalDateTime.now(),
                pageFamily,
                utilityId,
                providerId,
                ctaType,
                sanitizeSource(source),
                destination,
                request.getHeader("Referer")
        ));
        RedirectView redirectView = new RedirectView(destination);
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    private String sanitizeSource(String source) {
        return source == null ? "" : source.trim();
    }
}
