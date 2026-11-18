package owner.backflow.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class UploadErrorAdvice {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView tooLarge() {
        var model = new ModelAndView("pages/upload-error");
        model.setStatus(HttpStatus.CONTENT_TOO_LARGE);
        model.addObject("page", new PageMeta("Choose smaller photos | BackflowVerdict", "Photo upload limits and retry instructions.", "/get-help/", true));
        return model;
    }
}
