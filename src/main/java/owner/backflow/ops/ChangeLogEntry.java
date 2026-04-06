package owner.backflow.ops;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangeLogEntry(
        LocalDate checkedAt,
        String action,
        String entityType,
        String entityId,
        String note,
        Map<String, Object> metadata
) {
}
