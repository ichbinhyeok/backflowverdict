package owner.backflow.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import owner.backflow.data.model.ProviderRecord;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.stereotype.Service;

@Service
public class LeadAdminService {
    private final LeadRepository leadRepository;
    private final LeadAssignmentRepository leadAssignmentRepository;
    private final LeadDeliveryService leadDeliveryService;
    private final BackflowRegistryService registryService;
    private final ProviderCommercialStateRepository providerCommercialStateRepository;

    public LeadAdminService(
            LeadRepository leadRepository,
            LeadAssignmentRepository leadAssignmentRepository,
            LeadDeliveryService leadDeliveryService,
            BackflowRegistryService registryService,
            ProviderCommercialStateRepository providerCommercialStateRepository
    ) {
        this.leadRepository = leadRepository;
        this.leadAssignmentRepository = leadAssignmentRepository;
        this.leadDeliveryService = leadDeliveryService;
        this.registryService = registryService;
        this.providerCommercialStateRepository = providerCommercialStateRepository;
    }

    public List<LeadRecord> listLeads() {
        return leadRepository.findAll();
    }

    public List<LeadInboxItem> listInbox() {
        Map<String, LeadAssignmentRecord> assignmentsByLead = leadAssignmentRepository.findAll().stream()
                .collect(Collectors.toMap(
                        LeadAssignmentRecord::leadId,
                        Function.identity(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        return listLeads().stream()
                .map(lead -> new LeadInboxItem(
                        lead,
                        assignmentsByLead.get(lead.leadId()),
                        registryService.findAssignableProvidersForUtility(lead.utilityId())
                ))
                .toList();
    }

    public int leadCount() {
        return leadRepository.count();
    }

    public int assignedLeadCount() {
        return Math.toIntExact(listInbox().stream().filter(LeadInboxItem::assigned).count());
    }

    public int unassignedLeadCount() {
        return Math.toIntExact(listInbox().stream().filter(item -> !item.assigned()).count());
    }

    public int uncoveredLeadCount() {
        return Math.toIntExact(listInbox().stream().filter(item -> !item.hasProviderCoverage()).count());
    }

    public Map<String, Long> issueCounts() {
        return listLeads().stream()
                .collect(Collectors.groupingBy(
                        lead -> lead.issueType().isBlank() ? "unspecified" : lead.issueType(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> pageFamilyCounts() {
        return listLeads().stream()
                .collect(Collectors.groupingBy(
                        lead -> lead.pageFamily().isBlank() ? "unspecified" : lead.pageFamily(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> utilityCounts() {
        return listLeads().stream()
                .collect(Collectors.groupingBy(
                        lead -> lead.displayUtility(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> providerCoverageGaps() {
        return listInbox().stream()
                .filter(item -> !item.hasProviderCoverage())
                .collect(Collectors.groupingBy(
                        item -> item.lead().displayUtility(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public int publicProviderCount() {
        return registryService.listPublicProviders().size();
    }

    public int sponsorOnlyProviderCount() {
        return registryService.listSponsorOnlyProviders().size();
    }

    public int sponsorActiveProviderCount() {
        return registryService.listActiveSponsorProviders().size();
    }

    public int sponsorProspectProviderCount() {
        return registryService.listSponsorProspects().size();
    }

    public int heldProviderCount() {
        return registryService.listHeldProviders().size();
    }

    public int deliveredLeadCount() {
        return leadDeliveryService.sentCount();
    }

    public int queuedDeliveryCount() {
        return leadDeliveryService.queuedCount();
    }

    public List<LeadDeliveryRecord> recentDeliveries() {
        return leadDeliveryService.listDeliveries().stream().limit(10).toList();
    }

    public List<ProviderRecord> privateSponsorInventory() {
        return registryService.listSponsorOnlyProviders();
    }

    public Map<String, Long> sponsorProspectMetroCounts() {
        List<ProviderRecord> prospects = registryService.listSponsorProspects();
        return registryService.listPublishedMetros().stream()
                .map(metro -> Map.entry(
                        metro.title(),
                        prospects.stream()
                                .filter(provider -> metro.utilityIds().stream().anyMatch(provider::matchesUtility))
                                .count()
                ))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    public ProviderCommercialStateOverrideRecord updateSponsorStatus(
            String providerId,
            String sponsorStatus,
            String note,
            String updatedBy
    ) {
        ProviderRecord provider = registryService.listSponsorOnlyProviders().stream()
                .filter(candidate -> candidate.providerId().equalsIgnoreCase(providerId == null ? "" : providerId.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sponsor-only provider not found: " + providerId));
        String normalizedStatus = sponsorStatus == null ? "" : sponsorStatus.trim().toUpperCase();
        if (!"ACTIVE".equals(normalizedStatus) && !"PROSPECT".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Unsupported sponsor status: " + sponsorStatus);
        }
        ProviderCommercialStateOverrideRecord saved = providerCommercialStateRepository.save(new ProviderCommercialStateOverrideRecord(
                provider.providerId(),
                provider.providerName(),
                normalizedStatus,
                LocalDateTime.now(),
                updatedBy == null ? "" : updatedBy.trim(),
                note == null ? "" : note.trim()
        ));
        registryService.reload();
        return saved;
    }

    public LeadRecord record(LeadRecord leadRecord) {
        LeadRecord saved = leadRepository.save(leadRecord);
        leadDeliveryService.deliverIfPossible(saved);
        return saved;
    }

    public LeadAssignmentRecord assignLead(String leadId, String providerId, String note, String assignedBy) {
        LeadRecord lead = leadRepository.findById(leadId);
        if (lead == null) {
            throw new IllegalArgumentException("Lead not found: " + leadId);
        }
        ProviderRecord provider = registryService.findAssignableProvider(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found: " + providerId));
        if (!provider.matchesUtility(lead.utilityId())) {
            throw new IllegalArgumentException("Provider does not cover the lead utility.");
        }
        return leadAssignmentRepository.save(new LeadAssignmentRecord(
                lead.leadId(),
                lead.utilityId(),
                provider.providerId(),
                provider.providerName(),
                LocalDateTime.now(),
                assignedBy,
                note == null ? "" : note.trim()
        ));
    }

    public String exportJson() {
        return leadRepository.exportJson();
    }

    public String exportCsv() {
        return leadRepository.exportCsv();
    }
}
