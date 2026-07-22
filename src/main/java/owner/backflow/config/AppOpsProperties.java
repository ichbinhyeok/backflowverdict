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
        @DefaultValue("7") int brokenLinkSuppressionDays,
        @DefaultValue("") String currentDate,
        @DefaultValue("./storage/search-console/pages.csv") String searchConsolePagesPath,
        @DefaultValue("./storage/search-console/queries.csv") String searchConsoleQueriesPath
) {
    public static AppOpsProperties defaults() {
        return new AppOpsProperties(
                "./build/ops/freshness_report.json",
                "0 15 3 * * *",
                true,
                "./build/ops/verification_report.json",
                false,
                "",
                7,
                "",
                "./storage/search-console/pages.csv",
                "./storage/search-console/queries.csv"
        );
    }
}
