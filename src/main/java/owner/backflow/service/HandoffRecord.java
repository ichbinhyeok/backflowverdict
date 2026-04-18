package owner.backflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HandoffRecord(
        String handoffId,
        String publicToken,
        String internalToken,
        LocalDateTime createdAt,
        String utilityId,
        String utilityName,
        String utilityState,
        String utilitySlug,
        String issueType,
        String issueLabel,
        String propertyType,
        String propertyLabel,
        String customerFirstName,
        String accountIdentifier,
        String vendorCompanyName,
        String vendorSlug,
        String officeKey,
        String vendorContactName,
        String vendorPhone,
        String vendorEmail,
        String testerLicenseNumber,
        LocalDate dueDate,
        String noticeSummary,
        String internalNote,
        String createdFromPath,
        String headline,
        String publicSummary,
        List<String> nextSteps,
        List<String> vendorActions,
        List<String> customerActions,
        List<String> commonMistakes,
        String fullRulePath,
        String officialProgramUrl,
        String officialProgramLabel,
        String helpPath,
        LocalDate lastVerified,
        String smsText,
        String emailSubject,
        String emailBody,
        String siteAddress,
        String deviceLocation,
        String technicianName,
        String gaugeId,
        String calibrationReference,
        String testReadingSummary,
        String assemblyMakeModel,
        String assemblySize,
        String assemblyType,
        String assemblySerial,
        String checkValveOneReading,
        String checkValveTwoReading,
        String openingPointReading,
        String repairSummary,
        String resultStatus,
        String resultLabel,
        LocalDate testDate,
        String submissionStatus,
        String submissionLabel,
        LocalDate submissionDate,
        String submissionReference,
        String permitOrReportNumber,
        String failedReason
) {
    public HandoffRecord {
        nextSteps = nextSteps == null ? List.of() : List.copyOf(nextSteps);
        vendorActions = vendorActions == null ? List.of() : List.copyOf(vendorActions);
        customerActions = customerActions == null ? List.of() : List.copyOf(customerActions);
        commonMistakes = commonMistakes == null ? List.of() : List.copyOf(commonMistakes);
    }
}
