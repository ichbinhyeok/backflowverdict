package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SiteVisibilityInterceptor implements HandlerInterceptor {
    private final SiteVisibilityService siteVisibilityService;

    public SiteVisibilityInterceptor(SiteVisibilityService siteVisibilityService) {
        this.siteVisibilityService = siteVisibilityService;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView
    ) {
        if (modelAndView == null || modelAndView.getViewName() == null || modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }
        Object page = modelAndView.getModel().get("page");
        if (page instanceof PageMeta pageMeta) {
            PageMeta adjusted = siteVisibilityService.apply(pageMeta, request);
            modelAndView.addObject("page", adjusted);
            if (adjusted.noindex()) {
                response.setHeader("X-Robots-Tag", adjusted.robots());
            }
        }
    }
}
