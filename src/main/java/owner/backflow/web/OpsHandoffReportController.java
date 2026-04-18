package owner.backflow.web;

import owner.backflow.service.HandoffChannelReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OpsHandoffReportController {
    private final HandoffChannelReportService handoffChannelReportService;

    public OpsHandoffReportController(HandoffChannelReportService handoffChannelReportService) {
        this.handoffChannelReportService = handoffChannelReportService;
    }

    @GetMapping("/ops/handoffs/report")
    public String report(Model model) {
        var report = handoffChannelReportService.buildReport();
        model.addAttribute("page", new PageMeta(
                "Vendor-channel report | BackflowPath",
                "Internal handoff funnel report with repeat vendor usage, created, opened, and downloaded counts.",
                "/ops/handoffs/report",
                true
        ));
        model.addAttribute("report", report);
        return "pages/handoff-ops-report";
    }
}
