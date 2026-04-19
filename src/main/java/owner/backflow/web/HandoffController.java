package owner.backflow.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.service.HandoffEventRecord;
import owner.backflow.service.HandoffEventRepository;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import owner.backflow.service.HandoffComposerService;
import owner.backflow.service.HandoffIssueType;
import owner.backflow.service.HandoffPdfService;
import owner.backflow.service.HandoffRecord;
import owner.backflow.service.HandoffRepository;
import owner.backflow.service.HandoffResultStatus;
import owner.backflow.service.HandoffSubmissionStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class HandoffController {
    private static final String OFFICE_SCOPE_ERROR =
            "This office handoff is limited to annual notice and failed-test briefs for now.";
    private static final String OFFICE_PREVIEW_VIEWER = "office";
    private static final String HANDOFF_RESULT_SOURCE = "handoff-result";
    private static final String HANDOFF_BRIEF_SOURCE = "handoff-brief";
    private static final String HANDOFF_PACKET_SOURCE = "handoff-packet";
    private static final Set<String> LIKELY_AUTOMATED_USER_AGENT_TOKENS = Set.of(
            "bot",
            "crawler",
            "spider",
            "preview",
            "headless",
            "facebookexternalhit",
            "linkedinbot",
            "twitterbot",
            "slackbot",
            "discordbot",
            "telegrambot",
            "skypeuripreview",
            "curl/",
            "wget/",
            "python-requests",
            "go-http-client",
            "apache-httpclient",
            "okhttp",
            "node-fetch",
            "axios",
            "java/"
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
    private static final Set<String> OFFICE_EVENT_TYPES = Set.of(
            "brief_link_copied",
            "brief_text_copied",
            "brief_email_draft_copied",
            "brief_link_marked_sent",
            "brief_text_marked_sent",
            "brief_email_marked_sent",
            "brief_feedback_sent",
            "brief_feedback_needs_edit",
            "brief_feedback_missing_contact",
            "brief_feedback_waiting_approval",
            "brief_feedback_testing_only"
    );

    private final BackflowRegistryService registryService;
    private final HandoffComposerService handoffComposerService;
    private final HandoffPdfService handoffPdfService;
    private final HandoffRepository handoffRepository;
    private final HandoffEventRepository handoffEventRepository;
    private final AppSiteProperties siteProperties;

    public HandoffController(
            BackflowRegistryService registryService,
            HandoffComposerService handoffComposerService,
            HandoffPdfService handoffPdfService,
            HandoffRepository handoffRepository,
            HandoffEventRepository handoffEventRepository,
            AppSiteProperties siteProperties
    ) {
        this.registryService = registryService;
        this.handoffComposerService = handoffComposerService;
        this.handoffPdfService = handoffPdfService;
        this.handoffRepository = handoffRepository;
        this.handoffEventRepository = handoffEventRepository;
        this.siteProperties = siteProperties;
    }

    @GetMapping("/handoffs/new")
    public String newHandoff(
            @RequestParam(value = "utilityId", required = false) String utilityId,
            @RequestParam(value = "issueType", required = false) String issueType,
            @RequestParam(value = "propertyType", required = false) String propertyType,
            @RequestParam(value = "propertyLabel", required = false) String propertyLabel,
            @RequestParam(value = "siteAddress", required = false) String siteAddress,
            @RequestParam(value = "deviceLocation", required = false) String deviceLocation,
            @RequestParam(value = "technicianName", required = false) String technicianName,
            @RequestParam(value = "gaugeId", required = false) String gaugeId,
            @RequestParam(value = "calibrationReference", required = false) String calibrationReference,
            @RequestParam(value = "testReadingSummary", required = false) String testReadingSummary,
            @RequestParam(value = "assemblySize", required = false) String assemblySize,
            @RequestParam(value = "assemblyType", required = false) String assemblyType,
            @RequestParam(value = "assemblySerial", required = false) String assemblySerial,
            @RequestParam(value = "checkValveOneReading", required = false) String checkValveOneReading,
            @RequestParam(value = "checkValveTwoReading", required = false) String checkValveTwoReading,
            @RequestParam(value = "openingPointReading", required = false) String openingPointReading,
            @RequestParam(value = "repairSummary", required = false) String repairSummary,
            @RequestParam(value = "resultStatus", required = false) String resultStatus,
            @RequestParam(value = "submissionStatus", required = false) String submissionStatus,
            @RequestParam(value = "testDate", required = false) String testDateValue,
            @RequestParam(value = "dueDate", required = false) String dueDateValue,
            @RequestParam(value = "submissionDate", required = false) String submissionDateValue,
            @RequestParam(value = "customerFirstName", required = false) String customerFirstName,
            @RequestParam(value = "accountIdentifier", required = false) String accountIdentifier,
            @RequestParam(value = "vendorCompanyName", required = false) String vendorCompanyName,
            @RequestParam(value = "vendorContactName", required = false) String vendorContactName,
            @RequestParam(value = "vendorPhone", required = false) String vendorPhone,
            @RequestParam(value = "vendorEmail", required = false) String vendorEmail,
            @RequestParam(value = "testerLicenseNumber", required = false) String testerLicenseNumber,
            @RequestParam(value = "assemblyMakeModel", required = false) String assemblyMakeModel,
            @RequestParam(value = "submissionReference", required = false) String submissionReference,
            @RequestParam(value = "permitOrReportNumber", required = false) String permitOrReportNumber,
            @RequestParam(value = "noticeSummary", required = false) String noticeSummary,
            @RequestParam(value = "failedReason", required = false) String failedReason,
            @RequestParam(value = "internalNote", required = false) String internalNote,
            @RequestParam(value = "sourcePath", required = false) String sourcePath,
            Model model
    ) {
        String normalizedIssueType = normalize(issueType);
        return renderBuilder(
                model,
                normalize(utilityId),
                normalizedIssueType,
                normalize(propertyType),
                normalize(propertyLabel),
                normalize(siteAddress),
                normalize(deviceLocation),
                normalize(technicianName),
                normalize(gaugeId),
                normalize(calibrationReference),
                normalize(testReadingSummary),
                normalize(assemblySize),
                normalize(assemblyType),
                normalize(assemblySerial),
                normalize(checkValveOneReading),
                normalize(checkValveTwoReading),
                normalize(openingPointReading),
                normalize(repairSummary),
                normalize(resultStatus),
                normalize(submissionStatus),
                normalize(testDateValue),
                normalize(dueDateValue),
                normalize(submissionDateValue),
                normalize(customerFirstName),
                normalize(accountIdentifier),
                normalize(vendorCompanyName),
                normalize(vendorContactName),
                normalize(vendorPhone),
                normalize(vendorEmail),
                normalize(testerLicenseNumber),
                normalize(assemblyMakeModel),
                normalize(submissionReference),
                normalize(permitOrReportNumber),
                normalize(noticeSummary),
                normalize(failedReason),
                normalize(internalNote),
                normalize(sourcePath),
                normalizedIssueType.isBlank() || isOfficeIssueType(normalizedIssueType) ? "" : OFFICE_SCOPE_ERROR
        );
    }

    @PostMapping("/handoffs")
    public String createHandoff(
            @RequestParam String utilityId,
            @RequestParam String issueType,
            @RequestParam String resultStatus,
            @RequestParam String submissionStatus,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String propertyLabel,
            @RequestParam(required = false) String siteAddress,
            @RequestParam(required = false) String deviceLocation,
            @RequestParam(required = false) String technicianName,
            @RequestParam(required = false) String gaugeId,
            @RequestParam(required = false) String calibrationReference,
            @RequestParam(required = false) String testReadingSummary,
            @RequestParam(required = false) String assemblySize,
            @RequestParam(required = false) String assemblyType,
            @RequestParam(required = false) String assemblySerial,
            @RequestParam(required = false) String checkValveOneReading,
            @RequestParam(required = false) String checkValveTwoReading,
            @RequestParam(required = false) String openingPointReading,
            @RequestParam(required = false) String repairSummary,
            @RequestParam(required = false) String testDate,
            @RequestParam(required = false) String dueDate,
            @RequestParam(required = false) String submissionDate,
            @RequestParam(required = false) String customerFirstName,
            @RequestParam(required = false) String accountIdentifier,
            @RequestParam(required = false) String vendorCompanyName,
            @RequestParam(required = false) String vendorContactName,
            @RequestParam(required = false) String vendorPhone,
            @RequestParam(required = false) String vendorEmail,
            @RequestParam(required = false) String testerLicenseNumber,
            @RequestParam(required = false) String assemblyMakeModel,
            @RequestParam(required = false) String submissionReference,
            @RequestParam(required = false) String permitOrReportNumber,
            @RequestParam(required = false) String noticeSummary,
            @RequestParam(required = false) String failedReason,
            @RequestParam(required = false) String internalNote,
            @RequestParam(required = false) String sourcePath,
            Model model,
            HttpServletRequest request
    ) {
        String normalizedUtilityId = normalize(utilityId);
        String normalizedIssueType = normalize(issueType);
        String normalizedPropertyType = normalize(propertyType);
        String normalizedPropertyLabel = normalize(propertyLabel);
        String normalizedSiteAddress = normalize(siteAddress);
        String normalizedDeviceLocation = normalize(deviceLocation);
        String normalizedTechnicianName = normalize(technicianName);
        String normalizedGaugeId = normalize(gaugeId);
        String normalizedCalibrationReference = normalize(calibrationReference);
        String normalizedTestReadingSummary = normalize(testReadingSummary);
        String normalizedAssemblySize = normalize(assemblySize);
        String normalizedAssemblyType = normalize(assemblyType);
        String normalizedAssemblySerial = normalize(assemblySerial);
        String normalizedCheckValveOneReading = normalize(checkValveOneReading);
        String normalizedCheckValveTwoReading = normalize(checkValveTwoReading);
        String normalizedOpeningPointReading = normalize(openingPointReading);
        String normalizedRepairSummary = normalize(repairSummary);
        String normalizedResultStatus = normalize(resultStatus);
        String normalizedSubmissionStatus = normalize(submissionStatus);
        String normalizedTestDate = normalize(testDate);
        String normalizedDueDate = normalize(dueDate);
        String normalizedSubmissionDate = normalize(submissionDate);
        String normalizedCustomerFirstName = normalize(customerFirstName);
        String normalizedAccountIdentifier = normalize(accountIdentifier);
        String normalizedVendorCompanyName = normalize(vendorCompanyName);
        String normalizedVendorContactName = normalize(vendorContactName);
        String normalizedVendorPhone = normalize(vendorPhone);
        String normalizedVendorEmail = normalize(vendorEmail);
        String normalizedTesterLicenseNumber = normalize(testerLicenseNumber);
        String normalizedAssemblyMakeModel = normalize(assemblyMakeModel);
        String normalizedSubmissionReference = normalize(submissionReference);
        String normalizedPermitOrReportNumber = normalize(permitOrReportNumber);
        String normalizedNoticeSummary = normalize(noticeSummary);
        String normalizedFailedReason = normalize(failedReason);
        String normalizedInternalNote = normalize(internalNote);
        String normalizedSourcePath = normalize(sourcePath);
        if (!normalizedIssueType.isBlank() && !isOfficeIssueType(normalizedIssueType)) {
            return renderBuilder(
                    model,
                    normalizedUtilityId,
                    normalizedIssueType,
                    normalizedPropertyType,
                    normalizedPropertyLabel,
                    normalizedSiteAddress,
                    normalizedDeviceLocation,
                    normalizedTechnicianName,
                    normalizedGaugeId,
                    normalizedCalibrationReference,
                    normalizedTestReadingSummary,
                    normalizedAssemblySize,
                    normalizedAssemblyType,
                    normalizedAssemblySerial,
                    normalizedCheckValveOneReading,
                    normalizedCheckValveTwoReading,
                    normalizedOpeningPointReading,
                    normalizedRepairSummary,
                    normalizedResultStatus,
                    normalizedSubmissionStatus,
                    normalizedTestDate,
                    normalizedDueDate,
                    normalizedSubmissionDate,
                    normalizedCustomerFirstName,
                    normalizedAccountIdentifier,
                    normalizedVendorCompanyName,
                    normalizedVendorContactName,
                    normalizedVendorPhone,
                    normalizedVendorEmail,
                    normalizedTesterLicenseNumber,
                    normalizedAssemblyMakeModel,
                    normalizedSubmissionReference,
                    normalizedPermitOrReportNumber,
                    normalizedNoticeSummary,
                    normalizedFailedReason,
                    normalizedInternalNote,
                    normalizedSourcePath,
                    OFFICE_SCOPE_ERROR
            );
        }

        LocalDate parsedTestDate;
        LocalDate parsedDueDate;
        LocalDate parsedSubmissionDate;
        try {
            parsedTestDate = parseDate(normalizedTestDate, "Enter the test date as YYYY-MM-DD.");
            parsedDueDate = parseDate(normalizedDueDate, "Enter the next due or retest-by date as YYYY-MM-DD.");
            parsedSubmissionDate = parseDate(normalizedSubmissionDate, "Enter the submission date as YYYY-MM-DD.");
        } catch (IllegalArgumentException exception) {
            return renderBuilder(
                    model,
                    normalizedUtilityId,
                    normalizedIssueType,
                    normalizedPropertyType,
                    normalizedPropertyLabel,
                    normalizedSiteAddress,
                    normalizedDeviceLocation,
                    normalizedTechnicianName,
                    normalizedGaugeId,
                    normalizedCalibrationReference,
                    normalizedTestReadingSummary,
                    normalizedAssemblySize,
                    normalizedAssemblyType,
                    normalizedAssemblySerial,
                    normalizedCheckValveOneReading,
                    normalizedCheckValveTwoReading,
                    normalizedOpeningPointReading,
                    normalizedRepairSummary,
                    normalizedResultStatus,
                    normalizedSubmissionStatus,
                    normalizedTestDate,
                    normalizedDueDate,
                    normalizedSubmissionDate,
                    normalizedCustomerFirstName,
                    normalizedAccountIdentifier,
                    normalizedVendorCompanyName,
                    normalizedVendorContactName,
                    normalizedVendorPhone,
                    normalizedVendorEmail,
                    normalizedTesterLicenseNumber,
                    normalizedAssemblyMakeModel,
                    normalizedSubmissionReference,
                    normalizedPermitOrReportNumber,
                    normalizedNoticeSummary,
                    normalizedFailedReason,
                    normalizedInternalNote,
                    normalizedSourcePath,
                    exception.getMessage()
            );
        }

        String validationError = statusValidationError(
                normalizedResultStatus,
                normalizedDueDate,
                normalizedFailedReason
        );
        if (validationError.isBlank()) {
            validationError = briefIdentityValidationError(
                    normalizedPropertyLabel,
                    normalizedSiteAddress,
                    normalizedVendorCompanyName,
                    normalizedVendorPhone,
                    normalizedVendorEmail
            );
        }
        if (!validationError.isBlank()) {
            return renderBuilder(
                    model,
                    normalizedUtilityId,
                    normalizedIssueType,
                    normalizedPropertyType,
                    normalizedPropertyLabel,
                    normalizedSiteAddress,
                    normalizedDeviceLocation,
                    normalizedTechnicianName,
                    normalizedGaugeId,
                    normalizedCalibrationReference,
                    normalizedTestReadingSummary,
                    normalizedAssemblySize,
                    normalizedAssemblyType,
                    normalizedAssemblySerial,
                    normalizedCheckValveOneReading,
                    normalizedCheckValveTwoReading,
                    normalizedOpeningPointReading,
                    normalizedRepairSummary,
                    normalizedResultStatus,
                    normalizedSubmissionStatus,
                    normalizedTestDate,
                    normalizedDueDate,
                    normalizedSubmissionDate,
                    normalizedCustomerFirstName,
                    normalizedAccountIdentifier,
                    normalizedVendorCompanyName,
                    normalizedVendorContactName,
                    normalizedVendorPhone,
                    normalizedVendorEmail,
                    normalizedTesterLicenseNumber,
                    normalizedAssemblyMakeModel,
                    normalizedSubmissionReference,
                    normalizedPermitOrReportNumber,
                    normalizedNoticeSummary,
                    normalizedFailedReason,
                    normalizedInternalNote,
                    normalizedSourcePath,
                    validationError
            );
        }

        try {
            HandoffRecord handoff = handoffRepository.save(handoffComposerService.compose(
                    normalizedUtilityId,
                    normalizedIssueType,
                    normalizedPropertyType,
                    normalizedPropertyLabel,
                    normalizedSiteAddress,
                    normalizedDeviceLocation,
                    normalizedTechnicianName,
                    normalizedGaugeId,
                    normalizedCalibrationReference,
                    normalizedTestReadingSummary,
                    normalizedAssemblySize,
                    normalizedAssemblyType,
                    normalizedAssemblySerial,
                    normalizedCheckValveOneReading,
                    normalizedCheckValveTwoReading,
                    normalizedOpeningPointReading,
                    normalizedRepairSummary,
                    normalizedResultStatus,
                    normalizedSubmissionStatus,
                    parsedTestDate,
                    parsedDueDate,
                    parsedSubmissionDate,
                    normalizedCustomerFirstName,
                    normalizedAccountIdentifier,
                    normalizedVendorCompanyName,
                    normalizedVendorContactName,
                    normalizedVendorPhone,
                    normalizedVendorEmail,
                    normalizedTesterLicenseNumber,
                    normalizedAssemblyMakeModel,
                    normalizedSubmissionReference,
                    normalizedPermitOrReportNumber,
                    normalizedNoticeSummary,
                    normalizedFailedReason,
                    normalizedInternalNote,
                    normalizedSourcePath
            ));
            trackEvent("handoff_created", handoff, request);
            return "redirect:" + handoffComposerService.resultPath(requiredToken(handoff.internalToken(), "Internal handoff token missing."));
        } catch (IllegalArgumentException exception) {
            return renderBuilder(
                    model,
                    normalizedUtilityId,
                    normalizedIssueType,
                    normalizedPropertyType,
                    normalizedPropertyLabel,
                    normalizedSiteAddress,
                    normalizedDeviceLocation,
                    normalizedTechnicianName,
                    normalizedGaugeId,
                    normalizedCalibrationReference,
                    normalizedTestReadingSummary,
                    normalizedAssemblySize,
                    normalizedAssemblyType,
                    normalizedAssemblySerial,
                    normalizedCheckValveOneReading,
                    normalizedCheckValveTwoReading,
                    normalizedOpeningPointReading,
                    normalizedRepairSummary,
                    normalizedResultStatus,
                    normalizedSubmissionStatus,
                    normalizedTestDate,
                    normalizedDueDate,
                    normalizedSubmissionDate,
                    normalizedCustomerFirstName,
                    normalizedAccountIdentifier,
                    normalizedVendorCompanyName,
                    normalizedVendorContactName,
                    normalizedVendorPhone,
                    normalizedVendorEmail,
                    normalizedTesterLicenseNumber,
                    normalizedAssemblyMakeModel,
                    normalizedSubmissionReference,
                    normalizedPermitOrReportNumber,
                    normalizedNoticeSummary,
                    normalizedFailedReason,
                    normalizedInternalNote,
                    normalizedSourcePath,
                    exception.getMessage()
            );
        }
    }

    @GetMapping("/handoffs/{handoffId}")
    public String handoffResult(@PathVariable String handoffId, Model model, HttpServletRequest request) {
        HandoffRecord handoff = handoffInternal(handoffId);
        trackEvent("internal_result_opened", handoff, request);
        String internalToken = requiredToken(handoff.internalToken(), "Internal handoff token missing.");
        String publicToken = requiredToken(handoff.publicToken(), "Public handoff token missing.");
        HandoffActivity activity = handoffActivity(handoff);
        String resultPath = handoffComposerService.resultPath(internalToken);
        String briefPath = handoffComposerService.briefPath(publicToken);
        String packetPath = handoffComposerService.packetPath(internalToken);

        model.addAttribute("page", new PageMeta(
                handoff.headline() + " | BackflowPath",
                handoff.publicSummary(),
                canonical(handoff.fullRulePath()),
                true
        ));
        model.addAttribute("handoff", handoff);
        model.addAttribute("shareUrl", handoffComposerService.absoluteUrl(briefPath));
        model.addAttribute("packetUrl", handoffComposerService.absoluteUrl(packetPath));
        model.addAttribute("briefPreviewPath", officePreviewPath(briefPath));
        model.addAttribute("briefPdfPath", officePreviewPdfPath(publicToken));
        model.addAttribute("packetPdfPath", handoffComposerService.packetPdfPath(internalToken));
        model.addAttribute("handoffEventPath", resultPath + "/events");
        model.addAttribute("returnPath", returnPath(handoff));
        String briefTrackedPath = trackedPath(
                officePreviewPath(briefPath),
                "handoff-result",
                handoff.utilityId(),
                "customer-brief-preview",
                HANDOFF_RESULT_SOURCE
        );
        String packetTrackedPath = trackedPath(packetPath, "handoff-result", handoff.utilityId(), "archive-closeout", HANDOFF_RESULT_SOURCE);
        model.addAttribute("briefTrackedPath", briefTrackedPath);
        model.addAttribute("packetTrackedPath", packetTrackedPath);
        model.addAttribute("fullRuleTrackedPath", trackedPath(handoff.fullRulePath(), "handoff-result", handoff.utilityId(), "full-rule", HANDOFF_RESULT_SOURCE));
        model.addAttribute("officialProgramTrackedPath", trackedPath(handoff.officialProgramUrl(), "handoff-result", handoff.utilityId(), "official-program", HANDOFF_RESULT_SOURCE));
        model.addAttribute("helpTrackedPath", trackedPath(handoff.helpPath(), "handoff-result", handoff.utilityId(), "help-request", HANDOFF_RESULT_SOURCE));
        model.addAttribute("submissionGuidance", submissionGuidance(handoff));
        model.addAttribute("publicBriefOpenCount", activity.publicBriefOpenCount());
        model.addAttribute("publicBriefPdfDownloadCount", activity.publicBriefPdfDownloadCount());
        model.addAttribute("officePreviewOpenCount", activity.officePreviewOpenCount());
        model.addAttribute("sendPreparedCount", activity.sendPreparedCount());
        model.addAttribute("sendMarkedCount", activity.sendMarkedCount());
        model.addAttribute("lastPublicBriefOpenAt", activity.lastPublicBriefOpenAt());
        model.addAttribute("lastSendMarkedAt", activity.lastSendMarkedAt());
        return "pages/handoff-result";
    }

    @GetMapping("/handoffs/brief/{publicToken}")
    public String handoffBrief(
            @PathVariable String publicToken,
            @RequestParam(value = "viewer", required = false) String viewer,
            Model model,
            HttpServletRequest request
    ) {
        HandoffRecord handoff = handoffPublic(publicToken);
        trackEvent(publicBriefOpenEventType(viewer, request), handoff, request);
        String shareToken = requiredToken(handoff.publicToken(), "Public handoff token missing.");
        String briefPath = handoffComposerService.briefPath(shareToken);

        model.addAttribute("page", new PageMeta(
                handoff.headline() + " | BackflowPath",
                handoff.publicSummary(),
                canonical(handoff.fullRulePath()),
                true
        ));
        model.addAttribute("handoff", handoff);
        model.addAttribute("shareUrl", handoffComposerService.absoluteUrl(briefPath));
        model.addAttribute("pdfPath", isOfficePreview(viewer)
                ? officePreviewPdfPath(shareToken)
                : handoffComposerService.briefPdfPath(shareToken));
        model.addAttribute("fullRuleTrackedPath", trackedPath(handoff.fullRulePath(), "handoff-brief", handoff.utilityId(), "full-rule", HANDOFF_BRIEF_SOURCE));
        model.addAttribute("officialProgramTrackedPath", trackedPath(handoff.officialProgramUrl(), "handoff-brief", handoff.utilityId(), "official-program", HANDOFF_BRIEF_SOURCE));
        model.addAttribute("submissionGuidance", submissionGuidance(handoff));
        return "pages/handoff-public-brief";
    }

    @GetMapping("/handoffs/{handoffId}/packet")
    public String handoffPacket(@PathVariable String handoffId, Model model, HttpServletRequest request) {
        HandoffRecord handoff = handoffInternal(handoffId);
        trackEvent("archive_packet_opened", handoff, request);
        String internalToken = requiredToken(handoff.internalToken(), "Internal handoff token missing.");
        String publicToken = requiredToken(handoff.publicToken(), "Public handoff token missing.");
        String packetPath = handoffComposerService.packetPath(internalToken);

        model.addAttribute("page", new PageMeta(
                handoff.headline() + " office record | BackflowPath",
                handoff.publicSummary(),
                canonical(handoff.fullRulePath()),
                true
        ));
        model.addAttribute("handoff", handoff);
        model.addAttribute("briefUrl", handoffComposerService.absoluteUrl(officePreviewPath(handoffComposerService.briefPath(publicToken))));
        model.addAttribute("pdfPath", handoffComposerService.packetPdfPath(internalToken));
        model.addAttribute("fullRuleUrl", handoffComposerService.absoluteUrl(handoff.fullRulePath()));
        model.addAttribute("officialProgramUrl", handoff.officialProgramUrl());
        model.addAttribute("fullRuleTrackedPath", trackedPath(handoff.fullRulePath(), "handoff-packet", handoff.utilityId(), "full-rule", HANDOFF_PACKET_SOURCE));
        model.addAttribute("officialProgramTrackedPath", trackedPath(handoff.officialProgramUrl(), "handoff-packet", handoff.utilityId(), "official-program", HANDOFF_PACKET_SOURCE));
        model.addAttribute("helpTrackedPath", trackedPath(handoff.helpPath(), "handoff-packet", handoff.utilityId(), "help-request", HANDOFF_PACKET_SOURCE));
        model.addAttribute("submissionGuidance", submissionGuidance(handoff));
        return "pages/handoff-packet";
    }

    @GetMapping(value = "/handoffs/brief/{publicToken}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> handoffBriefPdf(
            @PathVariable String publicToken,
            @RequestParam(value = "viewer", required = false) String viewer,
            HttpServletRequest request
    ) {
        HandoffRecord handoff = handoffPublic(publicToken);
        trackEvent(publicBriefPdfEventType(viewer, request), handoff, request);
        byte[] pdf = handoffPdfService.renderCustomerResultSheet(handoff, submissionGuidance(handoff));
        return pdfResponse(pdfFilename("backflow-result-sheet", handoff), pdf);
    }

    @GetMapping(value = "/handoffs/{handoffId}/packet.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> handoffPacketPdf(@PathVariable String handoffId, HttpServletRequest request) {
        HandoffRecord handoff = handoffInternal(handoffId);
        trackEvent("archive_packet_pdf_downloaded", handoff, request);
        byte[] pdf = handoffPdfService.renderCloseoutPacket(handoff, submissionGuidance(handoff));
        return pdfResponse(pdfFilename("backflow-office-record", handoff), pdf);
    }

    @PostMapping("/handoffs/{handoffId}/events")
    public ResponseEntity<Void> recordOfficeEvent(
            @PathVariable String handoffId,
            @RequestParam String eventType,
            HttpServletRequest request
    ) {
        String normalizedEventType = normalize(eventType);
        if (!OFFICE_EVENT_TYPES.contains(normalizedEventType)) {
            return ResponseEntity.badRequest().build();
        }
        HandoffRecord handoff = handoffInternal(handoffId);
        trackEvent(normalizedEventType, handoff, request);
        return ResponseEntity.noContent().build();
    }

    private String renderBuilder(
            Model model,
            String utilityId,
            String issueType,
            String propertyType,
            String propertyLabel,
            String siteAddress,
            String deviceLocation,
            String technicianName,
            String gaugeId,
            String calibrationReference,
            String testReadingSummary,
            String assemblySize,
            String assemblyType,
            String assemblySerial,
            String checkValveOneReading,
            String checkValveTwoReading,
            String openingPointReading,
            String repairSummary,
            String resultStatus,
            String submissionStatus,
            String testDateValue,
            String dueDateValue,
            String submissionDateValue,
            String customerFirstName,
            String accountIdentifier,
            String vendorCompanyName,
            String vendorContactName,
            String vendorPhone,
            String vendorEmail,
            String testerLicenseNumber,
            String assemblyMakeModel,
            String submissionReference,
            String permitOrReportNumber,
            String noticeSummary,
            String failedReason,
            String internalNote,
            String sourcePath,
            String formError
    ) {
        String officeIssueType = isOfficeIssueType(issueType) ? issueType : "";
        UtilityRecord selectedUtility = registryService.findUtilityById(utilityId).orElse(null);
        model.addAttribute("page", new PageMeta(
                "Create a 2-minute customer brief | BackflowPath",
                "Build the customer-facing brief your office sends after an annual notice or failed test, then keep the office record behind it.",
                canonical("/handoffs/new"),
                true
        ));
        model.addAttribute("utilities", registryService.listPublishedUtilities());
        model.addAttribute("issueTypes", activeOfficeIssueTypes());
        model.addAttribute("resultStatuses", Arrays.asList(HandoffResultStatus.values()));
        model.addAttribute("submissionStatuses", Arrays.asList(HandoffSubmissionStatus.values()));
        model.addAttribute("selectedUtilityId", utilityId);
        model.addAttribute("selectedIssueType", officeIssueType);
        model.addAttribute("selectedPropertyType", propertyType);
        model.addAttribute("propertyLabel", propertyLabel);
        model.addAttribute("siteAddress", siteAddress);
        model.addAttribute("deviceLocation", deviceLocation);
        model.addAttribute("technicianName", technicianName);
        model.addAttribute("gaugeId", gaugeId);
        model.addAttribute("calibrationReference", calibrationReference);
        model.addAttribute("testReadingSummary", testReadingSummary);
        model.addAttribute("assemblySize", assemblySize);
        model.addAttribute("assemblyType", assemblyType);
        model.addAttribute("assemblySerial", assemblySerial);
        model.addAttribute("checkValveOneReading", checkValveOneReading);
        model.addAttribute("checkValveTwoReading", checkValveTwoReading);
        model.addAttribute("openingPointReading", openingPointReading);
        model.addAttribute("repairSummary", repairSummary);
        model.addAttribute("selectedResultStatus", resultStatus);
        model.addAttribute("selectedSubmissionStatus", submissionStatus);
        model.addAttribute("testDateValue", testDateValue);
        model.addAttribute("dueDateValue", dueDateValue);
        model.addAttribute("submissionDateValue", submissionDateValue);
        model.addAttribute("customerFirstName", customerFirstName);
        model.addAttribute("accountIdentifier", accountIdentifier);
        model.addAttribute("vendorCompanyName", vendorCompanyName);
        model.addAttribute("vendorContactName", vendorContactName);
        model.addAttribute("vendorPhone", vendorPhone);
        model.addAttribute("vendorEmail", vendorEmail);
        model.addAttribute("testerLicenseNumber", testerLicenseNumber);
        model.addAttribute("assemblyMakeModel", assemblyMakeModel);
        model.addAttribute("submissionReference", submissionReference);
        model.addAttribute("permitOrReportNumber", permitOrReportNumber);
        model.addAttribute("noticeSummary", noticeSummary);
        model.addAttribute("failedReason", failedReason);
        model.addAttribute("internalNote", internalNote);
        model.addAttribute("sourcePath", sourcePath);
        model.addAttribute("formError", formError);
        model.addAttribute("selectedUtility", selectedUtility);
        model.addAttribute("returnPath", safeRelativePath(sourcePath));
        return "pages/handoff-builder";
    }

    private String submissionGuidance(HandoffRecord handoff) {
        UtilityRecord utility = registryService.findUtilityById(handoff.utilityId()).orElse(null);
        HandoffResultStatus resultStatus = HandoffResultStatus.from(handoff.resultStatus()).orElse(HandoffResultStatus.PASS);
        HandoffSubmissionStatus submissionStatus = HandoffSubmissionStatus.from(handoff.submissionStatus()).orElse(HandoffSubmissionStatus.PENDING);
        if (utility == null) {
            return "";
        }
        return handoffComposerService.submissionGuidance(utility, resultStatus, submissionStatus);
    }

    private ResponseEntity<byte[]> pdfResponse(String filename, byte[] pdfBytes) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdfBytes);
    }

    private LocalDate parseDate(String rawValue, String errorMessage) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawValue);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(errorMessage, exception);
        }
    }

    private HandoffRecord handoffInternal(String handoffId) {
        HandoffRecord handoff = handoffRepository.findByInternalToken(handoffId);
        if (handoff == null) {
            throw new NotFoundException("Handoff not found.");
        }
        return handoff;
    }

    private HandoffRecord handoffPublic(String publicToken) {
        HandoffRecord handoff = handoffRepository.findByPublicToken(publicToken);
        if (handoff == null) {
            throw new NotFoundException("Handoff not found.");
        }
        return handoff;
    }

    private String statusValidationError(String resultStatusValue, String dueDateValue, String failedReasonValue) {
        HandoffResultStatus resultStatus = HandoffResultStatus.from(resultStatusValue).orElse(null);
        if (resultStatus == null) {
            return "";
        }
        if (resultStatus.requiresFailureNote() && failedReasonValue.isBlank()) {
            return resultStatus == HandoffResultStatus.FAIL
                    ? "Add the failed reason before generating a failed-test handoff."
                    : "Explain why the test could not be completed before generating this handoff.";
        }
        if (dueDateValue.isBlank()) {
            if (resultStatus == HandoffResultStatus.FAIL) {
                return "Add the repair or retest date before generating a failed-test handoff.";
            }
            if (resultStatus == HandoffResultStatus.UNABLE_TO_TEST) {
                return "Add the return-visit date before generating this handoff.";
            }
            return "Add the next due or filing date before generating the customer brief.";
        }
        return "";
    }

    private String briefIdentityValidationError(
            String propertyLabelValue,
            String siteAddressValue,
            String vendorCompanyNameValue,
            String vendorPhoneValue,
            String vendorEmailValue
    ) {
        if (propertyLabelValue.isBlank() && siteAddressValue.isBlank()) {
            return "Add the site name or service address before generating the customer brief.";
        }
        if (vendorCompanyNameValue.isBlank()) {
            return "Add the vendor company name before generating the customer brief.";
        }
        if (vendorPhoneValue.isBlank() && vendorEmailValue.isBlank()) {
            return "Add a vendor phone or email before generating the customer brief.";
        }
        return "";
    }

    private String trackedPath(String destination, String pageFamily, String utilityId, String ctaType, String sourcePath) {
        String normalizedDestination = normalize(destination);
        if (normalizedDestination.isBlank()) {
            return "";
        }
        return UriComponentsBuilder.fromPath("/r/cta")
                .queryParam("next", normalizedDestination)
                .queryParam("pageFamily", pageFamily)
                .queryParam("utilityId", normalize(utilityId))
                .queryParam("ctaType", ctaType)
                .queryParam("source", sourcePath)
                .build()
                .encode()
                .toUriString();
    }

    private String returnPath(HandoffRecord handoff) {
        String fromRecord = safeRelativePath(handoff.createdFromPath());
        if (!fromRecord.isBlank()) {
            return fromRecord;
        }
        return handoff.fullRulePath();
    }

    private String safeRelativePath(String value) {
        String normalized = normalize(value);
        if (normalized.startsWith("/") && !normalized.startsWith("//") && !normalized.contains("\\")) {
            return normalized;
        }
        return "";
    }

    private void trackEvent(String eventType, HandoffRecord handoff, HttpServletRequest request) {
        String userAgent = normalizeUserAgent(request == null ? "" : request.getHeader("User-Agent"));
        handoffEventRepository.save(new HandoffEventRecord(
                null,
                null,
                eventType,
                handoff.handoffId(),
                handoff.publicToken(),
                handoff.internalToken(),
                handoff.utilityId(),
                handoff.issueType(),
                handoff.resultStatus(),
                handoff.submissionStatus(),
                handoff.vendorCompanyName(),
                handoff.vendorSlug(),
                handoff.officeKey(),
                handoff.vendorEmail(),
                handoff.createdFromPath(),
                request == null ? "" : request.getRequestURI(),
                request == null ? "" : request.getHeader("Referer"),
                userAgent,
                requestTrafficClass(userAgent)
        ));
    }

    private String canonical(String relativePath) {
        String baseUrl = normalize(siteProperties.baseUrl()).replaceAll("/+$", "");
        String path = normalize(relativePath);
        if (baseUrl.isBlank()) {
            return path;
        }
        if (path.startsWith("/")) {
            return baseUrl + path;
        }
        return baseUrl + "/" + path;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String requiredToken(String token, String errorMessage) {
        String normalizedToken = normalize(token);
        if (!normalizedToken.isBlank()) {
            return normalizedToken;
        }
        throw new NotFoundException(errorMessage);
    }

    private boolean isOfficePreview(String viewer) {
        return OFFICE_PREVIEW_VIEWER.equalsIgnoreCase(normalize(viewer));
    }

    private String publicBriefOpenEventType(String viewer, HttpServletRequest request) {
        if (isOfficePreview(viewer)) {
            return "office_brief_preview_opened";
        }
        return isLikelyAutomatedRequest(request) ? "public_brief_opened_automated" : "public_brief_opened";
    }

    private String publicBriefPdfEventType(String viewer, HttpServletRequest request) {
        if (isOfficePreview(viewer)) {
            return "office_brief_preview_pdf_downloaded";
        }
        return isLikelyAutomatedRequest(request)
                ? "public_brief_pdf_downloaded_automated"
                : "public_brief_pdf_downloaded";
    }

    private boolean isLikelyAutomatedRequest(HttpServletRequest request) {
        String userAgent = normalizeUserAgent(request == null ? "" : request.getHeader("User-Agent"))
                .toLowerCase(Locale.ROOT);
        if (userAgent.isBlank()) {
            return false;
        }
        return LIKELY_AUTOMATED_USER_AGENT_TOKENS.stream().anyMatch(userAgent::contains);
    }

    private String requestTrafficClass(String userAgent) {
        String normalizedUserAgent = normalizeUserAgent(userAgent);
        if (normalizedUserAgent.isBlank()) {
            return "unknown";
        }
        return LIKELY_AUTOMATED_USER_AGENT_TOKENS.stream()
                .anyMatch(normalizedUserAgent.toLowerCase(Locale.ROOT)::contains)
                ? "likely-automated"
                : "standard";
    }

    private String normalizeUserAgent(String value) {
        String normalized = normalize(value);
        if (normalized.length() <= 240) {
            return normalized;
        }
        return normalized.substring(0, 240);
    }

    private String officePreviewPath(String relativePath) {
        return UriComponentsBuilder.fromPath(relativePath)
                .queryParam("viewer", OFFICE_PREVIEW_VIEWER)
                .build()
                .encode()
                .toUriString();
    }

    private String officePreviewPdfPath(String publicToken) {
        return UriComponentsBuilder.fromPath(handoffComposerService.briefPdfPath(publicToken))
                .queryParam("viewer", OFFICE_PREVIEW_VIEWER)
                .build()
                .encode()
                .toUriString();
    }

    private String pdfFilename(String prefix, HandoffRecord handoff) {
        String siteSlug = sanitizeFilenameSegment(handoff.propertyLabel());
        if (siteSlug.isBlank()) {
            siteSlug = "handoff";
        }
        return prefix + "-" + siteSlug + ".pdf";
    }

    private String sanitizeFilenameSegment(String rawValue) {
        String normalized = normalize(rawValue).toLowerCase(Locale.US);
        if (normalized.isBlank()) {
            return "";
        }
        String collapsed = normalized.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return collapsed;
    }

    private List<HandoffIssueType> activeOfficeIssueTypes() {
        return List.of(HandoffIssueType.GENERAL_TESTING, HandoffIssueType.FAILED_TEST_REPAIR);
    }

    private boolean isOfficeIssueType(String rawValue) {
        return HandoffIssueType.from(rawValue)
                .map(activeOfficeIssueTypes()::contains)
                .orElse(false);
    }

    private HandoffActivity handoffActivity(HandoffRecord handoff) {
        List<HandoffEventRecord> events = handoffEventRepository.findAll().stream()
                .filter(event -> normalize(event.handoffId()).equalsIgnoreCase(normalize(handoff.handoffId())))
                .toList();
        long officePreviewOpenCount = countUniqueEventDays(events, "office_brief_preview_opened");
        long publicBriefOpenCount = countUniqueEventDays(events, "public_brief_opened");
        long publicBriefPdfDownloadCount = events.stream()
                .filter(event -> "public_brief_pdf_downloaded".equalsIgnoreCase(normalize(event.eventType())))
                .count();
        long sendPreparedCount = events.stream()
                .filter(event -> PREPARED_SEND_EVENT_TYPES.contains(normalize(event.eventType())))
                .count();
        long sendMarkedCount = countUniqueEventDays(events, MARKED_SEND_EVENT_TYPES);
        String lastPublicBriefOpenAt = events.stream()
                .filter(event -> "public_brief_opened".equalsIgnoreCase(normalize(event.eventType())))
                .map(HandoffEventRecord::occurredAt)
                .filter(value -> value != null)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toString)
                .orElse("");
        String lastSendMarkedAt = events.stream()
                .filter(event -> MARKED_SEND_EVENT_TYPES.contains(normalize(event.eventType())))
                .map(HandoffEventRecord::occurredAt)
                .filter(value -> value != null)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toString)
                .orElse("");
        return new HandoffActivity(
                officePreviewOpenCount,
                publicBriefOpenCount,
                publicBriefPdfDownloadCount,
                sendPreparedCount,
                sendMarkedCount,
                lastPublicBriefOpenAt,
                lastSendMarkedAt
        );
    }

    private long countUniqueEventDays(List<HandoffEventRecord> events, String eventType) {
        String normalizedEventType = normalize(eventType);
        return events.stream()
                .filter(event -> normalizedEventType.equalsIgnoreCase(normalize(event.eventType())))
                .map(HandoffEventRecord::occurredAt)
                .filter(value -> value != null)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .count();
    }

    private long countUniqueEventDays(List<HandoffEventRecord> events, Set<String> eventTypes) {
        return events.stream()
                .filter(event -> eventTypes.contains(normalize(event.eventType()).toLowerCase(Locale.ROOT)))
                .map(HandoffEventRecord::occurredAt)
                .filter(value -> value != null)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .count();
    }

    private record HandoffActivity(
            long officePreviewOpenCount,
            long publicBriefOpenCount,
            long publicBriefPdfDownloadCount,
            long sendPreparedCount,
            long sendMarkedCount,
            String lastPublicBriefOpenAt,
            String lastSendMarkedAt
    ) {
    }
}
