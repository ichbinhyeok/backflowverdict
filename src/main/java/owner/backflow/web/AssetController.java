package owner.backflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AssetController {
    @GetMapping("/favicon.ico")
    public String favicon() {
        return "forward:/favicon.svg";
    }
}
