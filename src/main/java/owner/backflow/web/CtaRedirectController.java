package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDateTime;
import owner.backflow.service.CtaClickRecord;
import owner.backflow.service.CtaClickRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CtaRedirectController {
    private final CtaClickRepository ctaClickRepository;

    public CtaRedirectController(CtaClickRepository ctaClickRepository) {
        this.ctaClickRepository = ctaClickRepository;
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
        String destination = validateDestination(next);
        ctaClickRepository.save(new CtaClickRecord(
                null,
                LocalDateTime.now(),
                pageFamily,
                utilityId,
                providerId,
                ctaType,
                source,
                destination,
                request.getHeader("Referer")
        ));
        RedirectView redirectView = new RedirectView(destination);
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    private String validateDestination(String destination) {
        if (destination == null || destination.isBlank()) {
            throw new NotFoundException("CTA destination is required.");
        }
        if (destination.startsWith("/")) {
            return destination;
        }
        try {
            URI uri = URI.create(destination);
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                return destination;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        throw new NotFoundException("Unsupported CTA destination.");
    }
}
