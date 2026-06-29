package owner.backflow.ops;

import java.time.LocalDate;
import owner.backflow.config.AppOpsProperties;

public final class OpsDates {
    private OpsDates() {
    }

    public static LocalDate today(AppOpsProperties opsProperties) {
        if (opsProperties == null || opsProperties.currentDate() == null || opsProperties.currentDate().isBlank()) {
            return LocalDate.now();
        }
        return LocalDate.parse(opsProperties.currentDate().trim());
    }
}
