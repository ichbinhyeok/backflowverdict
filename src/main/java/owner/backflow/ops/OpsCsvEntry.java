package owner.backflow.ops;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

public record OpsCsvEntry(Map<String, String> values) {
    public String value(String key) {
        return values.getOrDefault(key, "");
    }

    public Optional<LocalDate> localDate(String key) {
        String raw = value(key);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(raw.trim()));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }
}
