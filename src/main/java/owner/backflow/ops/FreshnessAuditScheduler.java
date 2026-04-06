package owner.backflow.ops;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FreshnessAuditScheduler {
    private final FreshnessAuditService freshnessAuditService;

    public FreshnessAuditScheduler(FreshnessAuditService freshnessAuditService) {
        this.freshnessAuditService = freshnessAuditService;
    }

    @Scheduled(cron = "${app.ops.freshness-audit-cron:0 15 3 * * *}")
    public void refreshAuditReport() {
        freshnessAuditService.writeReport();
    }
}
