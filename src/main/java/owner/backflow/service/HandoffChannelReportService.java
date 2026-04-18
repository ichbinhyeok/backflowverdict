package owner.backflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class HandoffChannelReportService {
    private static final Set<String> OPEN_EVENT_TYPES = Set.of(
            "public_brief_opened",
            "internal_result_opened",
            "archive_packet_opened"
    );

    private static final Set<String> DOWNLOAD_EVENT_TYPES = Set.of(
            "office_brief_preview_pdf_downloaded",
            "public_brief_pdf_downloaded",
            "archive_packet_pdf_downloaded"
    );

    private static final Set<String> PREPARED_SEND_EVENT_TYPES = Set.of(
            "brief_link_copied",
            "brief_text_copied",
            "brief_email_draft_copied"
    );

    private static final Set<String> MARKED_SEND_EVENT_TYPES = Set.of(
            "brief_link_marked_sent",
            "brief_text_marked_sent",
            "brief_email_marked_sent"
    );

    private static final Set<String> RECIPIENT_OPEN_EVENT_TYPES = Set.of(
            "public_brief_opened"
    );

    private static final Set<String> RECIPIENT_DOWNLOAD_EVENT_TYPES = Set.of(
            "public_brief_pdf_downloaded"
    );

    private static final Set<String> SOURCE_DRAWER_CTA_TYPES = Set.of(
            "full-rule",
            "official-program",
            "help-request"
    );

    private final HandoffRepository handoffRepository;
    private final HandoffEventRepository handoffEventRepository;
    private final CtaClickRepository ctaClickRepository;

    public HandoffChannelReportService(
            HandoffRepository handoffRepository,
            HandoffEventRepository handoffEventRepository,
            CtaClickRepository ctaClickRepository
    ) {
        this.handoffRepository = handoffRepository;
        this.handoffEventRepository = handoffEventRepository;
        this.ctaClickRepository = ctaClickRepository;
    }

    public HandoffChannelReport buildReport() {
        List<HandoffRecord> handoffs = handoffRepository.findAll();
        List<HandoffEventRecord> events = handoffEventRepository.findAll();
        List<CtaClickRecord> ctaClicks = ctaClickRepository.findAll();
        List<CtaClickRecord> handoffCtaClicks = ctaClicks.stream()
                .filter(this::isHandoffClick)
                .toList();
        Map<String, List<HandoffEventRecord>> eventsByHandoffId = events.stream()
                .filter(event -> !normalize(event.handoffId()).isBlank())
                .collect(Collectors.groupingBy(event -> normalize(event.handoffId())));

        List<EventTypeCount> eventTypeCounts = events.stream()
                .collect(Collectors.groupingBy(
                        event -> normalize(event.eventType()),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new EventTypeCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(EventTypeCount::count).reversed().thenComparing(EventTypeCount::eventType))
                .toList();

        List<VendorUtilityUsageRow> repeatVendorUtilityRows = handoffs.stream()
                .collect(Collectors.groupingBy(this::vendorUtilityKey, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> summarizeUsage(entry.getValue(), eventsByHandoffId))
                .filter(row -> row.handoffCount() > 1)
                .sorted(Comparator.comparingLong(VendorUtilityUsageRow::handoffCount).reversed()
                        .thenComparing(VendorUtilityUsageRow::vendorCompanyName)
                        .thenComparing(VendorUtilityUsageRow::utilityName))
                .toList();

        List<RecentEventRow> recentEvents = events.stream()
                .sorted(Comparator.comparing(HandoffEventRecord::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(30)
                .map(event -> new RecentEventRow(
                        event.occurredAt(),
                        event.eventType(),
                        displayVendor(event.vendorCompanyName()),
                        displayUtilityName(event.utilityId(), handoffs),
                        display(event.issueType()),
                        display(event.resultStatus()),
                        display(event.submissionStatus()),
                        display(event.originPath()),
                        display(event.requestPath())
                ))
                .toList();

        List<CtaTypeCount> ctaTypeCounts = handoffCtaClicks.stream()
                .collect(Collectors.groupingBy(
                        click -> normalize(click.ctaType()),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new CtaTypeCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(CtaTypeCount::count).reversed().thenComparing(CtaTypeCount::ctaType))
                .toList();

        List<RecentCtaRow> recentCtaClicks = handoffCtaClicks.stream()
                .sorted(Comparator.comparing(CtaClickRecord::clickedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(30)
                .map(click -> new RecentCtaRow(
                        click.clickedAt(),
                        display(click.pageFamily()),
                        display(click.ctaType()),
                        displayUtilityName(click.utilityId(), handoffs),
                        display(click.sourcePage()),
                        display(click.destination())
                ))
                .toList();

        long createdCount = count(events, "handoff_created");
        long preparedSendCount = countAny(events, PREPARED_SEND_EVENT_TYPES);
        long markedSentCount = countUniqueIdentityDays(events, MARKED_SEND_EVENT_TYPES);
        long recipientOpenCount = countUniqueEventDays(events, "public_brief_opened");
        long openedCount = countUniqueWorkflowDays(events, OPEN_EVENT_TYPES);
        long downloadedCount = countAny(events, DOWNLOAD_EVENT_TYPES);
        long ctaClickCount = handoffCtaClicks.size();
        long sourceDrawerClickCount = handoffCtaClicks.stream()
                .filter(click -> SOURCE_DRAWER_CTA_TYPES.contains(normalize(click.ctaType()).toLowerCase(Locale.ROOT)))
                .count();

        return new HandoffChannelReport(
                handoffs.size(),
                events.size(),
                createdCount,
                preparedSendCount,
                markedSentCount,
                recipientOpenCount,
                openedCount,
                downloadedCount,
                ctaClickCount,
                sourceDrawerClickCount,
                countUniqueEventDays(events, "public_brief_opened"),
                countUniqueEventDays(events, "office_brief_preview_opened"),
                countUniqueEventDays(events, "internal_result_opened"),
                countUniqueEventDays(events, "archive_packet_opened"),
                count(events, "public_brief_pdf_downloaded"),
                count(events, "office_brief_preview_pdf_downloaded"),
                count(events, "archive_packet_pdf_downloaded"),
                eventTypeCounts,
                ctaTypeCounts,
                repeatVendorUtilityRows,
                recentEvents,
                recentCtaClicks
        );
    }

    private VendorUtilityUsageRow summarizeUsage(
            List<HandoffRecord> group,
            Map<String, List<HandoffEventRecord>> eventsByHandoffId
    ) {
        long createdCount = group.size();
        List<HandoffEventRecord> groupEvents = group.stream()
                .flatMap(handoff -> eventsByHandoffId.getOrDefault(normalize(handoff.handoffId()), List.of()).stream())
                .toList();
        // Keep repeat rows tied to recipient-facing loop signals so office result and packet churn
        // do not overstate whether the brief actually circulated outside the office.
        long openedCount = countUniqueWorkflowDays(groupEvents, RECIPIENT_OPEN_EVENT_TYPES);
        long downloadedCount = countUniqueWorkflowDays(groupEvents, RECIPIENT_DOWNLOAD_EVENT_TYPES);

        LocalDateTime firstSeen = group.stream()
                .map(HandoffRecord::createdAt)
                .filter(value -> value != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime lastSeen = group.stream()
                .map(HandoffRecord::createdAt)
                .filter(value -> value != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        HandoffRecord sample = group.getFirst();
        return new VendorUtilityUsageRow(
                displayVendor(sample.vendorCompanyName()),
                displayUtility(sample.utilityId(), sample.utilityName()),
                display(sample.utilityId()),
                createdCount,
                openedCount,
                downloadedCount,
                firstSeen,
                lastSeen
        );
    }

    private long count(List<HandoffEventRecord> events, String eventType) {
        String normalized = normalize(eventType).toLowerCase(Locale.ROOT);
        return events.stream()
                .filter(event -> normalize(event.eventType()).toLowerCase(Locale.ROOT).equals(normalized))
                .count();
    }

    private long countAny(List<HandoffEventRecord> events, Set<String> eventTypes) {
        return events.stream()
                .filter(event -> eventTypes.contains(normalize(event.eventType()).toLowerCase(Locale.ROOT)))
                .count();
    }

    private long countUniqueEventDays(List<HandoffEventRecord> events, String eventType) {
        String normalized = normalize(eventType).toLowerCase(Locale.ROOT);
        return events.stream()
                .filter(event -> normalize(event.eventType()).toLowerCase(Locale.ROOT).equals(normalized))
                .map(this::eventDayKey)
                .filter(key -> !key.isBlank())
                .distinct()
                .count();
    }

    private long countUniqueEventDays(List<HandoffEventRecord> events, Set<String> eventTypes) {
        return events.stream()
                .filter(event -> eventTypes.contains(normalize(event.eventType()).toLowerCase(Locale.ROOT)))
                .map(this::eventDayKey)
                .filter(key -> !key.isBlank())
                .distinct()
                .count();
    }

    private long countUniqueIdentityDays(List<HandoffEventRecord> events, Set<String> eventTypes) {
        return events.stream()
                .filter(event -> eventTypes.contains(normalize(event.eventType()).toLowerCase(Locale.ROOT)))
                .map(this::eventIdentityDayKey)
                .filter(key -> !key.isBlank())
                .distinct()
                .count();
    }

    private long countUniqueWorkflowDays(List<HandoffEventRecord> events, Set<String> eventTypes) {
        return events.stream()
                .filter(event -> eventTypes.contains(normalize(event.eventType()).toLowerCase(Locale.ROOT)))
                .map(this::eventWorkflowDayKey)
                .filter(key -> !key.isBlank())
                .distinct()
                .count();
    }

    private String vendorUtilityKey(HandoffRecord handoff) {
        return vendorIdentity(handoff)
                + "\u0000"
                + normalize(handoff.utilityId()).toLowerCase(Locale.ROOT);
    }

    private String vendorIdentity(HandoffRecord handoff) {
        String officeKey = normalize(handoff.officeKey());
        if (!officeKey.isBlank()) {
            return officeKey.toLowerCase(Locale.ROOT);
        }
        String officeIdentity = officeIdentity(handoff.vendorCompanyName(), handoff.vendorPhone());
        if (!officeIdentity.isBlank()) {
            return officeIdentity;
        }
        String vendorSlug = normalize(handoff.vendorSlug());
        if (!vendorSlug.isBlank()) {
            return vendorSlug.toLowerCase(Locale.ROOT);
        }
        String vendorEmail = normalize(handoff.vendorEmail());
        if (!vendorEmail.isBlank()) {
            return "email\u0000" + vendorEmail.toLowerCase(Locale.ROOT);
        }
        String vendorCompanyName = normalize(handoff.vendorCompanyName());
        if (!vendorCompanyName.isBlank()) {
            return "company\u0000" + vendorCompanyName.toLowerCase(Locale.ROOT);
        }
        return "unknown";
    }

    private String officeIdentity(String vendorCompanyName, String vendorPhone) {
        String companySlug = slugSegment(vendorCompanyName);
        if (!companySlug.isBlank()) {
            return "office\u0000" + companySlug;
        }
        String digits = normalize(vendorPhone).replaceAll("[^0-9]", "");
        if (!digits.isBlank()) {
            String tail = digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
            return "phone\u0000" + tail;
        }
        return "";
    }

    private String eventDayKey(HandoffEventRecord event) {
        String eventIdentity = eventIdentity(event);
        LocalDate eventDate = event.occurredAt() == null ? null : event.occurredAt().toLocalDate();
        if (eventIdentity.isBlank() || eventDate == null) {
            return "";
        }
        return eventIdentity + "\u0000" + normalize(event.eventType()).toLowerCase(Locale.ROOT) + "\u0000" + eventDate;
    }

    private String eventIdentityDayKey(HandoffEventRecord event) {
        String eventIdentity = eventIdentity(event);
        LocalDate eventDate = event.occurredAt() == null ? null : event.occurredAt().toLocalDate();
        if (eventIdentity.isBlank() || eventDate == null) {
            return "";
        }
        return eventIdentity + "\u0000" + eventDate;
    }

    private String eventIdentity(HandoffEventRecord event) {
        String publicToken = normalize(event.publicToken());
        if (!publicToken.isBlank()) {
            return "public\u0000" + publicToken.toLowerCase(Locale.ROOT);
        }
        String internalToken = normalize(event.internalToken());
        if (!internalToken.isBlank()) {
            return "internal\u0000" + internalToken.toLowerCase(Locale.ROOT);
        }
        String handoffId = normalize(event.handoffId());
        if (!handoffId.isBlank()) {
            return "handoff\u0000" + handoffId.toLowerCase(Locale.ROOT);
        }
        return "";
    }

    private String eventWorkflowDayKey(HandoffEventRecord event) {
        String workflowIdentity = eventWorkflowIdentity(event);
        LocalDate eventDate = event.occurredAt() == null ? null : event.occurredAt().toLocalDate();
        if (workflowIdentity.isBlank() || eventDate == null) {
            return "";
        }
        return workflowIdentity + "\u0000" + eventDate;
    }

    private String eventWorkflowIdentity(HandoffEventRecord event) {
        String handoffId = normalize(event.handoffId());
        if (!handoffId.isBlank()) {
            return "handoff\u0000" + handoffId.toLowerCase(Locale.ROOT);
        }
        String publicToken = normalize(event.publicToken());
        if (!publicToken.isBlank()) {
            return "public\u0000" + publicToken.toLowerCase(Locale.ROOT);
        }
        String internalToken = normalize(event.internalToken());
        if (!internalToken.isBlank()) {
            return "internal\u0000" + internalToken.toLowerCase(Locale.ROOT);
        }
        return "";
    }

    private String slugSegment(String value) {
        return normalize(value).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private String displayVendor(String vendorCompanyName) {
        String normalized = display(vendorCompanyName);
        return normalized.isBlank() ? "(no vendor name)" : normalized;
    }

    private String displayUtility(String utilityId, String utilityName) {
        String name = display(utilityName);
        if (!name.isBlank()) {
            return name;
        }
        String id = display(utilityId);
        return id.isBlank() ? "(no utility)" : id;
    }

    private String displayUtilityName(String utilityId, List<HandoffRecord> handoffs) {
        String utilityName = handoffs.stream()
                .filter(handoff -> normalize(handoff.utilityId()).equalsIgnoreCase(normalize(utilityId)))
                .map(HandoffRecord::utilityName)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("");
        return displayUtility(utilityId, utilityName);
    }

    private String display(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isHandoffClick(CtaClickRecord click) {
        String pageFamily = normalize(click.pageFamily()).toLowerCase(Locale.ROOT);
        if (pageFamily.startsWith("handoff")) {
            return true;
        }
        return normalize(click.sourcePage()).contains("/handoffs/");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record HandoffChannelReport(
            long totalHandoffs,
            long totalEvents,
            long createdCount,
            long preparedSendCount,
            long markedSentCount,
            long recipientOpenCount,
            long openedCount,
            long downloadedCount,
            long ctaClickCount,
            long sourceDrawerClickCount,
            long publicBriefOpenedCount,
            long officePreviewOpenedCount,
            long internalResultOpenedCount,
            long archivePacketOpenedCount,
            long publicBriefDownloadedCount,
            long officePreviewPdfDownloadedCount,
            long archivePacketDownloadedCount,
            List<EventTypeCount> eventTypeCounts,
            List<CtaTypeCount> ctaTypeCounts,
            List<VendorUtilityUsageRow> repeatVendorUtilityRows,
            List<RecentEventRow> recentEvents,
            List<RecentCtaRow> recentCtaClicks
    ) {
    }

    public record EventTypeCount(String eventType, long count) {
    }

    public record CtaTypeCount(String ctaType, long count) {
    }

    public record VendorUtilityUsageRow(
            String vendorCompanyName,
            String utilityName,
            String utilityId,
            long handoffCount,
            long openedCount,
            long downloadedCount,
            LocalDateTime firstSeen,
            LocalDateTime lastSeen
    ) {
    }

    public record RecentEventRow(
            LocalDateTime occurredAt,
            String eventType,
            String vendorCompanyName,
            String utilityName,
            String issueType,
            String resultStatus,
            String submissionStatus,
            String originPath,
            String requestPath
    ) {
    }

    public record RecentCtaRow(
            LocalDateTime clickedAt,
            String pageFamily,
            String ctaType,
            String utilityName,
            String sourcePage,
            String destination
    ) {
    }
}
