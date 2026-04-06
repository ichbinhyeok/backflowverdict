package owner.backflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.ops")
public record AppOpsProperties(
        @DefaultValue("./build/ops/freshness_report.json") String freshnessReportPath,
        @DefaultValue("0 15 3 * * *") String freshnessAuditCron,
        @DefaultValue("true") boolean writeFreshnessReportOnStartup,
        @DefaultValue("./build/ops/verification_report.json") String verificationReportPath,
        @DefaultValue("false") boolean allowLocalRequests,
        @DefaultValue("") String verificationToken,
        @DefaultValue("7") int brokenLinkSuppressionDays
) {
}
