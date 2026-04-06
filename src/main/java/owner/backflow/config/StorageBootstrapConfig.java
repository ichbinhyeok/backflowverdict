package owner.backflow.config;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageBootstrapConfig {
    private final AppDataProperties dataProperties;
    private final AppOpsProperties opsProperties;
    private final AppLeadsProperties leadsProperties;

    public StorageBootstrapConfig(
            AppDataProperties dataProperties,
            AppOpsProperties opsProperties,
            AppLeadsProperties leadsProperties
    ) {
        this.dataProperties = dataProperties;
        this.opsProperties = opsProperties;
        this.leadsProperties = leadsProperties;
    }

    @PostConstruct
    void createStorageDirectories() {
        createDirectory(Path.of(dataProperties.root()));
        createDirectory(Path.of(leadsProperties.root()));
        createParentDirectory(opsProperties.freshnessReportPath());
        createParentDirectory(opsProperties.verificationReportPath());
    }

    private void createParentDirectory(String filePath) {
        Path parent = Path.of(filePath).toAbsolutePath().normalize().getParent();
        if (parent != null) {
            createDirectory(parent);
        }
    }

    private void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create storage directory " + path, exception);
        }
    }
}
