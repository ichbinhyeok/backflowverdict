package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class SiteExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NotFoundException exception, HttpServletRequest request, Model model) {
        model.addAttribute("page", new PageMeta(
                "Page not found | BackflowPath",
                "The requested BackflowPath page could not be found.",
                request.getRequestURL().toString(),
                true
        ));
        model.addAttribute("message", exception.getMessage());
        return "pages/not-found";
    }
}
