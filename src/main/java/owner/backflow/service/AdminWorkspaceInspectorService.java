package owner.backflow.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import owner.backflow.config.AppLeadsProperties;
import org.springframework.stereotype.Service;

@Service
public class AdminWorkspaceInspectorService {
    private static final int PREVIEW_LINE_LIMIT = 5;
    private static final int PREVIEW_CHAR_LIMIT = 1400;

    private final AppLeadsProperties leadsProperties;

    public AdminWorkspaceInspectorService(AppLeadsProperties leadsProperties) {
        this.leadsProperties = leadsProperties;
    }

    public String storageRoot() {
        return rootPath().toAbsolutePath().normalize().toString();
    }

    public List<StorageFileSummary> listStorageFiles() {
        Path root = rootPath();
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(root)) {
            return paths.filter(Files::isRegularFile)
                    .sorted(Comparator
                            .comparing(this::lastModifiedSafe, Comparator.reverseOrder())
                            .thenComparing(path -> path.getFileName().toString().toLowerCase()))
                    .map(this::toSummary)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect admin storage root " + root, exception);
        }
    }

    private StorageFileSummary toSummary(Path path) {
        try {
            long sizeBytes = Files.size(path);
            FileTime lastModified = Files.getLastModifiedTime(path);
            return new StorageFileSummary(
                    path.getFileName().toString(),
                    path.toAbsolutePath().normalize().toString(),
                    sizeBytes,
                    humanSize(sizeBytes),
                    LocalDateTime.ofInstant(lastModified.toInstant(), ZoneId.systemDefault()).toString(),
                    preview(path)
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to inspect admin storage file " + path, exception);
        }
    }

    private FileTime lastModifiedSafe(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException exception) {
            return FileTime.fromMillis(0L);
        }
    }

    private String preview(Path path) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                return "";
            }
            int fromIndex = Math.max(0, lines.size() - PREVIEW_LINE_LIMIT);
            String preview = String.join(System.lineSeparator(), lines.subList(fromIndex, lines.size())).trim();
            if (preview.length() <= PREVIEW_CHAR_LIMIT) {
                return preview;
            }
            return preview.substring(0, PREVIEW_CHAR_LIMIT).trim() + System.lineSeparator() + "...";
        } catch (IOException exception) {
            return "(preview unavailable)";
        }
    }

    private String humanSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        if (sizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        }
        return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
    }

    private Path rootPath() {
        return Path.of(leadsProperties.root());
    }

    public record StorageFileSummary(
            String name,
            String absolutePath,
            long sizeBytes,
            String displaySize,
            String updatedAt,
            String preview
    ) {
    }
}
