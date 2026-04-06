package owner.backflow.service;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.data.model.SourceLink;
import owner.backflow.data.model.SubmissionMethod;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.web.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CtaDestinationPolicyService {
    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;

    public CtaDestinationPolicyService(BackflowRegistryService registryService, AppSiteProperties siteProperties) {
        this.registryService = registryService;
        this.siteProperties = siteProperties;
    }

    public String validateDestination(String destination) {
        if (destination == null || destination.isBlank()) {
            throw new NotFoundException("CTA destination is required.");
        }

        String trimmed = destination.trim();
        if (isSafeRelativePath(trimmed)) {
            return trimmed;
        }

        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException("Unsupported CTA destination.");
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new NotFoundException("Unsupported CTA destination.");
        }
        if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
            throw new NotFoundException("Unsupported CTA destination.");
        }

        String host = normalizeHost(uri.getHost());
        if (host.isBlank() || !allowedHosts().contains(host)) {
            throw new NotFoundException("CTA destination is not allowlisted.");
        }
        return trimmed;
    }

    private boolean isSafeRelativePath(String destination) {
        return destination.startsWith("/")
                && !destination.startsWith("//")
                && !destination.contains("\\");
    }

    private Set<String> allowedHosts() {
        Set<String> hosts = new LinkedHashSet<>();
        addHost(hosts, siteProperties.baseUrl());
        for (UtilityRecord utility : registryService.listPublishedUtilities()) {
            addHost(hosts, utility.utilityUrl());
            addHost(hosts, utility.approvedTesterListUrl());
            for (String url : utility.officialProgramUrls()) {
                addHost(hosts, url);
            }
            for (SubmissionMethod submissionMethod : utility.submissionMethods()) {
                addHost(hosts, submissionMethod.url());
            }
            for (SourceLink source : utility.sources()) {
                addHost(hosts, source.url());
            }
        }
        for (ProviderRecord provider : registryService.listPublicProviders()) {
            addHost(hosts, provider.siteUrl());
            addHost(hosts, provider.officialApprovalSourceUrl());
        }
        return hosts;
    }

    private void addHost(Set<String> hosts, String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            String host = normalizeHost(URI.create(url.trim()).getHost());
            if (!host.isBlank()) {
                hosts.add(host);
            }
        } catch (IllegalArgumentException ignored) {
            // Ignore malformed source URLs so they do not break redirects.
        }
    }

    private String normalizeHost(String host) {
        return host == null ? "" : host.trim().toLowerCase(Locale.US);
    }
}
