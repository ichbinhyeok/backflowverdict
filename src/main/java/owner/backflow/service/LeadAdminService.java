package owner.backflow.service;

import java.time.LocalDateTime;
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
    private final BackflowRegistryService registryService;

    public LeadAdminService(
            LeadRepository leadRepository,
            LeadAssignmentRepository leadAssignmentRepository,
            BackflowRegistryService registryService
    ) {
        this.leadRepository = leadRepository;
        this.leadAssignmentRepository = leadAssignmentRepository;
        this.registryService = registryService;
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

    public int heldProviderCount() {
        return registryService.listHeldProviders().size();
    }

    public LeadRecord record(LeadRecord leadRecord) {
        return leadRepository.save(leadRecord);
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
