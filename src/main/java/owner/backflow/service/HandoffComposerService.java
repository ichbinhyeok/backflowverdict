package owner.backflow.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import owner.backflow.config.AppSiteProperties;
import owner.backflow.data.model.UtilityFocusContent;
import owner.backflow.data.model.UtilityRecord;
import owner.backflow.files.BackflowRegistryService;
import org.springframework.stereotype.Service;

@Service
public class HandoffComposerService {
    private final BackflowRegistryService registryService;
    private final AppSiteProperties siteProperties;

    public HandoffComposerService(BackflowRegistryService registryService, AppSiteProperties siteProperties) {
        this.registryService = registryService;
        this.siteProperties = siteProperties;
    }

    public HandoffRecord compose(
            String utilityId,
            String issueTypeValue,
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
            String resultStatusValue,
            String submissionStatusValue,
            LocalDate testDate,
            LocalDate dueDate,
            LocalDate submissionDate,
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
            String createdFromPath
    ) {
        UtilityRecord utility = registryService.findUtilityById(normalize(utilityId))
                .orElseThrow(() -> new IllegalArgumentException("Choose a published utility before building the handoff."));
        HandoffIssueType issueType = HandoffIssueType.from(issueTypeValue)
                .orElseThrow(() -> new IllegalArgumentException("Choose the customer situation before building the handoff."));
        HandoffResultStatus resultStatus = HandoffResultStatus.from(resultStatusValue)
                .orElseThrow(() -> new IllegalArgumentException("Choose the test result before building the office record."));
        HandoffSubmissionStatus submissionStatus = HandoffSubmissionStatus.from(submissionStatusValue)
                .orElseThrow(() -> new IllegalArgumentException("Choose the utility submission status before building the office record."));
        if (!issueType.supports(utility)) {
            throw new IllegalArgumentException(issueType.formLabel() + " is not published for " + utility.utilityName() + ".");
        }

        String handoffId = UUID.randomUUID().toString();
        String publicToken = UUID.randomUUID().toString();
        String internalToken = UUID.randomUUID().toString();
        UtilityFocusContent focus = focusFor(utility, issueType);
        String fullRulePath = issuePath(utility, issueType);
        String officialProgramUrl = officialProgramUrl(utility);
        String briefUrl = absoluteUrl(briefPath(publicToken));
        String vendorSlug = vendorSlug(vendorCompanyName, vendorPhone, vendorEmail);
        String officeKey = officeKey(vendorCompanyName, vendorPhone, vendorEmail);
        List<String> nextSteps = nextSteps(issueType, focus, resultStatus, submissionStatus, dueDate);
        List<String> vendorActions = vendorActions(issueType, utility, resultStatus, submissionStatus);
        List<String> customerActions = customerActions(issueType, utility, resultStatus, submissionStatus, dueDate);
        List<String> commonMistakes = commonMistakes(utility, issueType, focus, resultStatus, submissionStatus);
        String headline = headline(resultStatus, propertyLabel, siteAddress, utility.utilityName());
        String publicSummary = publicSummary(utility, issueType, focus, resultStatus, submissionStatus, dueDate);

        return new HandoffRecord(
                handoffId,
                publicToken,
                internalToken,
                LocalDateTime.now(),
                utility.utilityId(),
                utility.utilityName(),
                utility.state(),
                utility.canonicalSlug(),
                issueType.code(),
                issueType.briefLabel(),
                normalize(propertyType),
                normalize(propertyLabel),
                normalize(customerFirstName),
                normalize(accountIdentifier),
                normalize(vendorCompanyName),
                vendorSlug,
                officeKey,
                normalize(vendorContactName),
                normalize(vendorPhone),
                normalize(vendorEmail),
                normalize(testerLicenseNumber),
                dueDate,
                normalize(noticeSummary),
                normalize(internalNote),
                normalize(createdFromPath),
                headline,
                publicSummary,
                nextSteps,
                vendorActions,
                customerActions,
                commonMistakes,
                fullRulePath,
                officialProgramUrl,
                "Official program page",
                LeadRoutingService.requestHelpPath(
                        utility.utilityId(),
                        fullRulePath,
                        issueType.code(),
                        issueType.leadPageFamily()
                ),
                utility.lastVerified(),
                smsText(
                        utility,
                        resultStatus,
                        submissionStatus,
                        propertyLabel,
                        siteAddress,
                        dueDate,
                        customerFirstName,
                        vendorCompanyName,
                        briefUrl
                ),
                emailSubject(utility, resultStatus, submissionStatus, propertyLabel, siteAddress),
                emailBody(
                        utility,
                        resultStatus,
                        submissionStatus,
                        propertyLabel,
                        siteAddress,
                        customerFirstName,
                        testDate,
                        dueDate,
                        noticeSummary,
                        failedReason,
                        nextSteps,
                        briefUrl,
                        fullRulePath,
                        officialProgramUrl,
                        vendorCompanyName,
                        vendorContactName,
                        vendorPhone,
                        vendorEmail
                ),
                normalize(siteAddress),
                normalize(deviceLocation),
                normalize(technicianName),
                normalize(gaugeId),
                normalize(calibrationReference),
                normalize(testReadingSummary),
                normalize(assemblyMakeModel),
                normalize(assemblySize),
                normalize(assemblyType),
                normalize(assemblySerial),
                normalizeReading(checkValveOneReading),
                normalizeReading(checkValveTwoReading),
                normalizeReading(openingPointReading),
                normalize(repairSummary),
                resultStatus.code(),
                resultStatus.label(),
                testDate,
                submissionStatus.code(),
                submissionStatus.label(),
                submissionDate,
                normalize(submissionReference),
                normalize(permitOrReportNumber),
                normalize(failedReason)
        );
    }

    public String briefPath(String publicToken) {
        return "/handoffs/brief/" + publicToken;
    }

    public String packetPath(String internalToken) {
        return "/handoffs/" + internalToken + "/packet";
    }

    public String resultPath(String internalToken) {
        return "/handoffs/" + internalToken;
    }

    public String briefPdfPath(String publicToken) {
        return "/handoffs/brief/" + publicToken + "/pdf";
    }

    public String packetPdfPath(String internalToken) {
        return "/handoffs/" + internalToken + "/packet.pdf";
    }

    public String absoluteUrl(String path) {
        String normalizedPath = normalize(path);
        String baseUrl = normalize(siteProperties.baseUrl()).replaceAll("/+$", "");
        if (baseUrl.isBlank()) {
            return normalizedPath;
        }
        if (normalizedPath.startsWith("/")) {
            return baseUrl + normalizedPath;
        }
        return baseUrl + "/" + normalizedPath;
    }

    public String submissionGuidance(
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus
    ) {
        String route = submissionRoute(utility);
        return switch (submissionStatus) {
            case SUBMITTED -> switch (resultStatus) {
                case PASS -> utility.utilityName() + " usually receives completed test results through " + route
                        + ". This handoff shows that submission step as already sent.";
                case FAIL -> "The failed result has been documented, but the final passing retest still has to go through "
                        + route + " before the requirement is complete.";
                case UNABLE_TO_TEST -> "This utility usually receives updates through " + route
                        + ", but the workflow still needs a completed test before it can move forward.";
            };
            case PENDING -> switch (resultStatus) {
                case PASS -> utility.utilityName() + " usually receives test results through " + route
                        + ". The field test is done, but that submission step still needs to be completed.";
                case FAIL -> "Repair and retest still come first. After that, the passing result still has to be sent through "
                        + route + ".";
                case UNABLE_TO_TEST -> "This visit did not produce a completed test. Once the device is ready, the result still has to be submitted through "
                        + route + ".";
            };
        };
    }

    private UtilityFocusContent focusFor(UtilityRecord utility, HandoffIssueType issueType) {
        return switch (issueType) {
            case GENERAL_TESTING -> utility.resolvedAnnualTesting();
            case IRRIGATION -> utility.irrigation();
            case FIRE_LINE -> utility.fireLine();
            case FAILED_TEST_REPAIR -> new UtilityFocusContent(
                    "A failed device is not closed until repair, retest, and the accepted report all land in the utility workflow.",
                    utility.failureHighlights(),
                    List.of(
                            "Repair the device using the local workflow that fits the assembly and property.",
                            "Complete the passing retest before the utility deadline closes.",
                            "Make sure the accepted report reaches the utility so the failed file actually closes."
                    )
            );
        };
    }

    private String issuePath(UtilityRecord utility, HandoffIssueType issueType) {
        String basePath = "/utilities/" + utility.state() + "/" + utility.canonicalSlug() + "/";
        return switch (issueType) {
            case GENERAL_TESTING -> basePath + "annual-testing";
            case FAILED_TEST_REPAIR -> basePath + "failed-test";
            case IRRIGATION -> basePath + "irrigation";
            case FIRE_LINE -> basePath + "fire-line";
        };
    }

    private String headline(
            HandoffResultStatus resultStatus,
            String propertyLabel,
            String siteAddress,
            String utilityName
    ) {
        String siteReference = siteReference(propertyLabel, siteAddress, utilityName + " account");
        return switch (resultStatus) {
            case PASS -> "Backflow result for " + siteReference;
            case FAIL -> "Failed backflow test for " + siteReference;
            case UNABLE_TO_TEST -> "Backflow visit update for " + siteReference;
        };
    }

    private String publicSummary(
            UtilityRecord utility,
            HandoffIssueType issueType,
            UtilityFocusContent focus,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus,
            LocalDate dueDate
    ) {
        String actionDateLine = dueDate == null ? "" : " Keep " + dueDate + " in view while this backflow requirement is still open.";
        if (issueType == HandoffIssueType.GENERAL_TESTING) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED ->
                            "This annual backflow testing workflow now has a passing field test, and the result has been marked as submitted to "
                                    + utility.utilityName() + "." + actionDateLine;
                    case PENDING ->
                            "This annual backflow testing workflow now has a passing field test, but the annual requirement is not complete until that result reaches "
                                    + utility.utilityName() + "." + actionDateLine;
                };
                case FAIL -> "This annual backflow testing workflow reached the test step, but the device did not pass. Repair, a passing retest, and the accepted filing are still required before the annual requirement can close."
                        + actionDateLine;
                case UNABLE_TO_TEST -> "This annual backflow testing workflow could not be completed because the device could not be tested. Access, installation, or device conditions still need to be corrected before the annual requirement can move forward."
                        + actionDateLine;
            };
        }
        if (issueType == HandoffIssueType.FAILED_TEST_REPAIR) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED ->
                            "This failed-test repair workflow now has a passing retest, and the closeout result has been marked as submitted to "
                                    + utility.utilityName() + "." + actionDateLine;
                    case PENDING ->
                            "This failed-test repair workflow now has a passing retest, but the failed-device file stays open until that retest result reaches "
                                    + utility.utilityName() + "." + actionDateLine;
                };
                case FAIL -> "This brief is tracking a failed-test repair workflow. The device is still in failed status, so repair, a passing retest, and the accepted filing are still required before the file can close."
                        + actionDateLine;
                case UNABLE_TO_TEST -> "This failed-test repair workflow could not be completed on the return visit. The access, installation, or device condition blocking the retest still has to be corrected before the failed file can close."
                        + actionDateLine;
            };
        }
        return switch (resultStatus) {
            case PASS -> switch (submissionStatus) {
                case SUBMITTED ->
                        "The device passed the field test and the result has been marked as submitted to "
                                + utility.utilityName() + "." + actionDateLine;
                case PENDING ->
                        "The device passed the field test, but this backflow requirement is not complete until the accepted result reaches "
                                + utility.utilityName() + "." + actionDateLine;
            };
            case FAIL -> "The device failed the field test. Repair and a passing retest are still required before this requirement can be treated as complete."
                    + actionDateLine;
            case UNABLE_TO_TEST -> "The device could not be tested. Access, installation, or device conditions still need to be corrected before the utility workflow can move forward."
                    + actionDateLine;
        };
    }

    private List<String> nextSteps(
            HandoffIssueType issueType,
            UtilityFocusContent focus,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus,
            LocalDate dueDate
    ) {
        String actionDate = dueDate == null ? "the utility timeline" : dueDate.toString();
        if (issueType == HandoffIssueType.GENERAL_TESTING) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED -> List.of(
                            "Keep this annual test result with the site records in case the notice or portal status lags.",
                            "Use the official rule only if someone needs the governing annual testing requirement.",
                            "Watch the next annual due cycle before the utility opens the next notice."
                    );
                    case PENDING -> List.of(
                            "Finish filing the passing annual test result with the utility.",
                            "Keep this copy in hand until the annual notice shows as cleared.",
                            "Do not treat the annual workflow as closed until the utility side is updated."
                    );
                };
                case FAIL -> List.of(
                        "Approve the repair path needed to clear the annual testing requirement.",
                        "Complete the repair and passing retest before " + actionDate + ".",
                        "Expect the annual notice to stay open until the passing result reaches the utility."
                );
                case UNABLE_TO_TEST -> List.of(
                        "Fix the access or device condition that blocked the annual test.",
                        "Get the assembly ready for a return visit before " + actionDate + ".",
                        "Do not assume the annual notice moves forward without a completed test."
                );
            };
        }
        if (issueType == HandoffIssueType.FAILED_TEST_REPAIR) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED -> List.of(
                            "Keep this passing retest copy with the repair record for the failed device.",
                            "Use the official rule only if someone needs the governing failed-test requirement.",
                            "Watch for the utility record to finish clearing the old failed status."
                    );
                    case PENDING -> List.of(
                            "Finish filing the passing retest so the failed-test file can close.",
                            "Keep this copy in hand until the failed-device record shows as updated.",
                            "Do not treat the repair workflow as closed until the utility side is updated."
                    );
                };
                case FAIL -> List.of(
                        "Approve the repair path that fits the failed device.",
                        "Complete the repair and passing retest before " + actionDate + ".",
                        "Expect the failed-device file to stay open until the utility receives the passing retest."
                );
                case UNABLE_TO_TEST -> List.of(
                        "Fix the access or device condition blocking the failed-device return visit.",
                        "Get the assembly ready for a retest before " + actionDate + ".",
                        "Do not assume the failed-test file moves forward without a completed retest."
                );
            };
        }
        return switch (resultStatus) {
            case PASS -> switch (submissionStatus) {
                case SUBMITTED -> List.of(
                        "Keep this result copy with the customer records.",
                        "Use the full local rule only if someone needs the exact city requirement or exception.",
                        "Watch the next due cycle before the utility opens the next test window."
                );
                case PENDING -> List.of(
                        "Confirm the passing result is filed with the utility.",
                        "Keep this copy in hand until the submission is accepted.",
                        "Do not treat the job as closed until the utility side is actually updated."
                );
            };
            case FAIL -> List.of(
                    "Approve the repair path that fits the failed device.",
                    "Complete the repair and passing retest before " + actionDate + ".",
                    "Expect this requirement to stay open until the utility receives the passing result."
            );
            case UNABLE_TO_TEST -> List.of(
                    "Fix the access or device condition that blocked the test.",
                    "Get the assembly ready for a return visit before " + actionDate + ".",
                    "Do not assume the utility file moves forward without a completed test."
            );
        };
    }

    private List<String> vendorActions(
            HandoffIssueType issueType,
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus
    ) {
        if (issueType == HandoffIssueType.GENERAL_TESTING) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED -> List.of(
                            "Send the customer brief as the annual notice closeout copy, with the passing result already in hand.",
                            "Keep proof that the annual test result was submitted to " + utility.utilityName() + ".",
                            "Leave the customer with one clear annual-testing explanation instead of a notice or portal screenshot only."
                    );
                    case PENDING -> List.of(
                            "Complete the passing annual test record and push it into the utility workflow.",
                            "Keep the customer copy aligned with the actual annual filing status.",
                            "Close the annual notice job only after the filing step is complete."
                    );
                };
                case FAIL -> List.of(
                        "Explain that the annual test happened, but the device failed and the annual requirement stays open.",
                        "Scope the repair path and the passing retest in the right order.",
                        "Submit the passing follow-up once the repair work is complete."
                );
                case UNABLE_TO_TEST -> List.of(
                        "Document why the annual testing visit could not be completed.",
                        "Tell the customer what has to change before the return visit can clear the annual notice.",
                        "Keep the annual utility path visible so the stalled file does not drift."
                );
            };
        }
        if (issueType == HandoffIssueType.FAILED_TEST_REPAIR) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED -> List.of(
                            "Send the customer brief as the failed-test closeout copy, with the passing retest already in hand.",
                            "Keep proof that the closeout retest was submitted to " + utility.utilityName() + ".",
                            "Leave the customer with one clear repair-and-retest explanation instead of piecemeal updates."
                    );
                    case PENDING -> List.of(
                            "Push the passing retest into the utility workflow so the failed-device file can actually close.",
                            "Keep the customer copy aligned with the actual failed-test filing status.",
                            "Close the repair workflow only after the filing step is complete."
                    );
                };
                case FAIL -> List.of(
                        "Document that this is still an active failed-test repair workflow, not a closed job.",
                        "Scope the repair path and the passing retest in the right order.",
                        "Submit the passing follow-up once the repair work is complete."
                );
                case UNABLE_TO_TEST -> List.of(
                        "Document why the failed-device follow-up visit could not be completed.",
                        "Tell the customer what has to change before the retest can happen.",
                        "Keep the failed-test utility path visible so the open file does not drift."
                );
            };
        }
        return switch (resultStatus) {
            case PASS -> switch (submissionStatus) {
                case SUBMITTED -> List.of(
                        "Send the customer brief with the passed result already in hand.",
                        "Keep proof that the result was submitted to " + utility.utilityName() + ".",
                        "Leave the customer with one clean result copy instead of a verbal summary only."
                );
                case PENDING -> List.of(
                        "Complete the passing test record and push it into the utility workflow.",
                        "Keep the customer copy aligned with the actual submission status.",
                        "Close the job only after the filing step is complete."
                );
            };
            case FAIL -> List.of(
                    "Document the failed outcome clearly enough that the customer understands the job is still open.",
                    "Scope the repair path and the passing retest in the right order.",
                    "Submit the passing follow-up once the repair work is complete."
            );
            case UNABLE_TO_TEST -> List.of(
                    "Document why the test could not be completed.",
                    "Tell the customer what has to change before the return visit.",
                    "Keep the utility path visible so the stalled file does not drift."
            );
        };
    }

    private List<String> customerActions(
            HandoffIssueType issueType,
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus,
            LocalDate dueDate
    ) {
        String actionDate = dueDate == null ? "the utility timeline" : dueDate.toString();
        if (issueType == HandoffIssueType.GENERAL_TESTING) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED -> List.of(
                            "Keep this result copy as proof the annual backflow requirement was handled.",
                            "If anyone asks why you received a notice, use this result copy first and the official source second.",
                            "Watch the next annual due cycle instead of waiting for another scramble."
                    );
                    case PENDING -> List.of(
                            "Keep this result copy in hand until the annual filing is complete.",
                            "Do not assume a passed annual field test alone means the notice is cleared.",
                            "If the utility still shows the site as open, ask for the submission confirmation."
                    );
                };
                case FAIL -> List.of(
                        "Approve the repair and retest work needed to clear the annual backflow requirement before " + actionDate + ".",
                        "Keep site access clear until the passing retest is complete.",
                        "The annual notice is not resolved until the passing retest is done and filed."
                );
                case UNABLE_TO_TEST -> List.of(
                        "Fix the access or installation issue before " + actionDate + " so the annual test can be completed.",
                        "Make sure the device can be reached and tested on the return visit.",
                        "The annual requirement stays active until a completed test is recorded."
                );
            };
        }
        if (issueType == HandoffIssueType.FAILED_TEST_REPAIR) {
            return switch (resultStatus) {
                case PASS -> switch (submissionStatus) {
                    case SUBMITTED -> List.of(
                            "Keep this passing retest copy as proof the failed device was repaired, retested, and sent for closeout.",
                            "If the old failed status reappears, use this result copy first and the official source second.",
                            "Keep the repair record with your site paperwork until the utility record catches up."
                    );
                    case PENDING -> List.of(
                            "Keep this result copy in hand until the passing retest filing is complete.",
                            "Do not assume the failed-device file is closed until the utility accepts the retest.",
                            "If the utility still shows the device as failed, ask for the submission confirmation."
                    );
                };
                case FAIL -> List.of(
                        "Approve the repair and retest work for the failed device before " + actionDate + ".",
                        "Keep site access clear until the passing retest is complete.",
                        "The failed-test file is not finished until the passing retest is done and filed."
                );
                case UNABLE_TO_TEST -> List.of(
                        "Fix the access, installation, or shutdown issue before " + actionDate + " so the failed device can be retested.",
                        "Make sure the device can be reached and worked on during the return visit.",
                        "The failed-test file stays active until a completed retest is recorded."
                );
            };
        }
        return switch (resultStatus) {
            case PASS -> switch (submissionStatus) {
                case SUBMITTED -> List.of(
                        "Keep this result copy for your records.",
                        "If anyone asks for proof, use the result copy first and the official source second.",
                        "Watch the next utility due cycle instead of waiting for a scramble."
                );
                case PENDING -> List.of(
                        "Keep this result copy in hand until the utility filing is complete.",
                        "Do not assume a passed field test alone means the requirement is complete.",
                        "If the utility still shows the device as open, ask for the submission confirmation."
                );
            };
            case FAIL -> List.of(
                    "Approve the repair and retest work before " + actionDate + ".",
                    "Keep site access clear until the passing retest is complete.",
                    "The job is not finished until the passing retest is done."
            );
            case UNABLE_TO_TEST -> List.of(
                    "Fix the access or installation issue before " + actionDate + ".",
                    "Make sure the device can be reached and tested on the return visit.",
                    "The requirement stays active until a completed test is recorded."
            );
        };
    }

    private List<String> commonMistakes(
            UtilityRecord utility,
            HandoffIssueType issueType,
            UtilityFocusContent focus,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus
    ) {
        if (issueType == HandoffIssueType.GENERAL_TESTING) {
            if (resultStatus == HandoffResultStatus.FAIL) {
                return List.of(
                        "The annual notice does not clear just because the first test happened. A passing retest is still required.",
                        "Repair work alone does not close the annual requirement. The accepted passing result still has to reach the utility record."
                );
            }
            if (resultStatus == HandoffResultStatus.UNABLE_TO_TEST) {
                return List.of(
                        "A blocked annual testing visit still leaves the annual notice open.",
                        "Rescheduling without fixing access, clearance, or device issues usually leads to another incomplete visit."
                );
            }
            if (submissionStatus == HandoffSubmissionStatus.PENDING) {
                return List.of(
                        "A passing annual field test is not the same as a cleared annual notice.",
                        "Keep proof of submission close until the utility record no longer shows the annual requirement as open."
                );
            }
        }
        if (issueType == HandoffIssueType.FAILED_TEST_REPAIR) {
            if (resultStatus == HandoffResultStatus.FAIL) {
                return List.of(
                        "Repair approval does not close a failed-test file by itself. The passing retest still has to happen.",
                        "A passed retest does not finish the failed-device job until the accepted result reaches the utility record."
                );
            }
            if (resultStatus == HandoffResultStatus.UNABLE_TO_TEST) {
                return List.of(
                        "A blocked return visit still leaves the failed-test file open.",
                        "Access, shutdown, or installation problems should be corrected before the retest is booked again."
                );
            }
            if (submissionStatus == HandoffSubmissionStatus.PENDING) {
                return List.of(
                        "A passing retest is not the same as a closed failed-device file.",
                        "Keep proof of submission close until the utility record no longer shows the device as open or failed."
                );
            }
        }
        if (resultStatus == HandoffResultStatus.FAIL) {
            return List.of(
                    "Repair work does not close the file by itself. The passing retest still has to happen.",
                    "A passed retest does not finish the job until the accepted result reaches the utility record."
            );
        }
        if (resultStatus == HandoffResultStatus.UNABLE_TO_TEST) {
            return List.of(
                    "An incomplete visit still leaves the utility file open.",
                    "Access or installation problems should be corrected before the next test date is booked."
            );
        }
        if (submissionStatus == HandoffSubmissionStatus.PENDING) {
            return List.of(
                    "A passed field test is not the same as a closed utility file.",
                    "Keep proof of submission close until the customer record is updated."
            );
        }

        List<String> highlights = focus.highlights() == null ? List.of() : focus.highlights();
        if (!highlights.isEmpty()) {
            return highlights.stream()
                    .map(this::normalize)
                    .filter(item -> !item.isBlank())
                    .limit(2)
                    .toList();
        }
        return List.of(
                utility.dueBasis(),
                utility.whoIsAffected()
        ).stream().map(this::normalize).filter(item -> !item.isBlank()).limit(2).toList();
    }

    private String officialProgramUrl(UtilityRecord utility) {
        String primary = normalize(utility.utilityUrl());
        if (!primary.isBlank()) {
            return primary;
        }
        return utility.officialProgramUrls().stream()
                .map(this::normalize)
                .filter(url -> !url.isBlank())
                .findFirst()
                .orElse("");
    }

    private String submissionRoute(UtilityRecord utility) {
        List<String> labels = utility.submissionMethods().stream()
                .map(method -> normalize(method.label()))
                .filter(label -> !label.isBlank())
                .distinct()
                .limit(3)
                .toList();
        if (labels.isEmpty()) {
            return "the utility's normal submission channel";
        }
        if (labels.size() == 1) {
            return labels.get(0);
        }
        if (labels.size() == 2) {
            return labels.get(0) + " or " + labels.get(1);
        }
        return labels.get(0) + ", " + labels.get(1) + ", or " + labels.get(2);
    }

    private String vendorSlug(String vendorCompanyName, String vendorPhone, String vendorEmail) {
        String company = slugSegment(vendorCompanyName);
        if (!company.isBlank()) {
            return "office-" + company;
        }

        String digits = normalize(vendorPhone).replaceAll("[^0-9]", "");
        if (!digits.isBlank()) {
            String tail = digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
            return "phone-" + tail;
        }

        String normalizedEmail = normalize(vendorEmail).toLowerCase();
        if (!normalizedEmail.isBlank()) {
            return "email-" + normalizedEmail
                    .replace("@", "-at-")
                    .replace(".", "-")
                    .replaceAll("[^a-z0-9-]", "-")
                    .replaceAll("-{2,}", "-")
                    .replaceAll("^-|-$", "");
        }

        return "";
    }

    private String officeKey(String vendorCompanyName, String vendorPhone, String vendorEmail) {
        String company = slugSegment(vendorCompanyName);
        String phoneTail = phoneTail(vendorPhone, 7);
        String emailDomain = emailDomainSlug(vendorEmail);
        if (!company.isBlank() && !phoneTail.isBlank()) {
            return "office-" + company + "-" + phoneTail;
        }
        if (!company.isBlank() && !emailDomain.isBlank()) {
            return "office-" + company + "-" + emailDomain;
        }
        if (!company.isBlank()) {
            return "office-" + company;
        }
        if (!phoneTail.isBlank()) {
            return "phone-" + phoneTail;
        }
        if (!emailDomain.isBlank()) {
            return "domain-" + emailDomain;
        }
        return "";
    }

    private String emailDomainSlug(String vendorEmail) {
        String normalizedEmail = normalize(vendorEmail).toLowerCase();
        int atIndex = normalizedEmail.indexOf('@');
        if (atIndex < 0 || atIndex == normalizedEmail.length() - 1) {
            return "";
        }
        return slugSegment(normalizedEmail.substring(atIndex + 1));
    }

    private String phoneTail(String vendorPhone, int width) {
        String digits = normalize(vendorPhone).replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return "";
        }
        if (digits.length() <= width) {
            return digits;
        }
        return digits.substring(digits.length() - width);
    }

    private String slugSegment(String value) {
        return normalize(value).toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private String smsText(
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus,
            String propertyLabel,
            String siteAddress,
            LocalDate dueDate,
            String customerFirstName,
            String vendorCompanyName,
            String briefUrl
    ) {
        String firstName = normalize(customerFirstName);
        String sender = normalize(vendorCompanyName);
        String siteReference = siteReference(propertyLabel, siteAddress, "your property");

        StringBuilder text = new StringBuilder();
        if (!firstName.isBlank()) {
            text.append(firstName).append(", ");
        }
        if (!sender.isBlank()) {
            text.append(sender).append(" here. ");
        }
        text.append("Your backflow result for ")
                .append(siteReference)
                .append(" is ready: ")
                .append(briefUrl)
                .append(" ");
        text.append(statusSummary(utility, resultStatus, submissionStatus));
        if (dueDate != null) {
            text.append(" Action by ").append(dueDate).append(".");
        }
        return text.toString().trim();
    }

    private String emailSubject(
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus,
            String propertyLabel,
            String siteAddress
    ) {
        String siteReference = siteReference(propertyLabel, siteAddress, utility.utilityName() + " account");
        return switch (resultStatus) {
            case PASS -> switch (submissionStatus) {
                case SUBMITTED -> "Backflow result for " + siteReference;
                case PENDING -> "Backflow result and filing status for " + siteReference;
            };
            case FAIL -> "Failed backflow test next step for " + siteReference;
            case UNABLE_TO_TEST -> "Backflow visit update for " + siteReference;
        };
    }

    private String emailBody(
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus,
            String propertyLabel,
            String siteAddress,
            String customerFirstName,
            LocalDate testDate,
            LocalDate dueDate,
            String noticeSummary,
            String failedReason,
            List<String> nextSteps,
            String briefUrl,
            String fullRulePath,
            String officialProgramUrl,
            String vendorCompanyName,
            String vendorContactName,
            String vendorPhone,
            String vendorEmail
    ) {
        StringBuilder body = new StringBuilder();
        String firstName = normalize(customerFirstName);
        String siteReference = siteReference(propertyLabel, siteAddress, utility.utilityName() + " account");
        String senderCompany = normalize(vendorCompanyName);
        String senderContact = normalize(vendorContactName);
        String senderPhoneValue = normalize(vendorPhone);
        String senderEmailValue = normalize(vendorEmail);

        if (!firstName.isBlank()) {
            body.append(firstName).append(",").append(System.lineSeparator()).append(System.lineSeparator());
        }
        body.append("Here is the backflow result update for ")
                .append(siteReference)
                .append(".")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        body.append("Current status").append(System.lineSeparator());
        body.append("- Utility: ").append(utility.utilityName()).append(System.lineSeparator());
        body.append("- Test result: ").append(resultStatus.label()).append(System.lineSeparator());
        body.append("- Utility filing: ").append(submissionStatus.label()).append(System.lineSeparator());
        if (testDate != null) {
            body.append("- Test date: ").append(testDate).append(System.lineSeparator());
        }
        if (dueDate != null) {
            body.append("- Action by: ").append(dueDate).append(System.lineSeparator());
        }
        body.append(System.lineSeparator());

        body.append("Customer result sheet").append(System.lineSeparator());
        body.append(briefUrl).append(System.lineSeparator()).append(System.lineSeparator());

        String normalizedNotice = normalize(noticeSummary);
        String normalizedFailedReason = normalize(failedReason);
        if (!normalizedNotice.isBlank()) {
            body.append("Job note").append(System.lineSeparator());
            body.append(normalizedNotice).append(System.lineSeparator()).append(System.lineSeparator());
        }
        if (!normalizedFailedReason.isBlank()) {
            body.append(System.lineSeparator());
            body.append("Result note").append(System.lineSeparator());
            body.append(normalizedFailedReason).append(System.lineSeparator()).append(System.lineSeparator());
        }

        body.append("What happens next").append(System.lineSeparator());
        int index = 1;
        for (String step : nextSteps) {
            body.append(index).append(". ").append(step).append(System.lineSeparator());
            index++;
        }

        body.append(System.lineSeparator());
        body.append("If you need the governing rule behind this result, reply to this message and we can send it.")
                .append(System.lineSeparator());
        if (!senderCompany.isBlank() || !senderContact.isBlank() || !senderPhoneValue.isBlank() || !senderEmailValue.isBlank()) {
            body.append(System.lineSeparator());
            if (!senderCompany.isBlank()) {
                body.append(senderCompany).append(System.lineSeparator());
            }
            if (!senderContact.isBlank()) {
                body.append(senderContact).append(System.lineSeparator());
            }
            if (!senderPhoneValue.isBlank()) {
                body.append(senderPhoneValue).append(System.lineSeparator());
            }
            if (!senderEmailValue.isBlank()) {
                body.append(senderEmailValue).append(System.lineSeparator());
            }
        }
        return body.toString().trim();
    }

    private String siteReference(String propertyLabel, String siteAddress, String fallback) {
        String label = normalize(propertyLabel);
        if (!label.isBlank()) {
            return label;
        }
        String address = normalize(siteAddress);
        if (!address.isBlank()) {
            return address;
        }
        return normalize(fallback);
    }

    private String statusSummary(
            UtilityRecord utility,
            HandoffResultStatus resultStatus,
            HandoffSubmissionStatus submissionStatus
    ) {
        return switch (resultStatus) {
            case PASS -> switch (submissionStatus) {
                case SUBMITTED -> "Passed test. Result marked submitted to " + utility.utilityName() + ".";
                case PENDING -> "Passed test. Utility filing is still pending.";
            };
            case FAIL -> "Failed test. Repair and a passing retest are still needed.";
            case UNABLE_TO_TEST -> "Test could not be completed. A return visit is still needed.";
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeReading(String value) {
        String normalized = normalize(value).replaceAll("\\s+", " ");
        if (normalized.matches("^\\d+(?:\\.\\d+)?$")) {
            return normalized + " psid";
        }
        return normalized;
    }
}
