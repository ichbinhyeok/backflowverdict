package owner.backflow.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import owner.backflow.config.AppSiteProperties;
import org.junit.jupiter.api.Test;

class HandoffPdfServiceTest {
    private final HandoffPdfService handoffPdfService = new HandoffPdfService(
            new AppSiteProperties("https://backflowpath.com", "BackflowPath", "", "", "")
    );

    @Test
    void closeoutPacketRendersPdfWhenNarrativeNotesAreLong() {
        HandoffRecord handoff = new HandoffRecord(
                "handoff-123",
                "public-123",
                "internal-123",
                LocalDateTime.of(2026, 4, 18, 10, 30),
                "arlington-water",
                "Arlington Water Utilities",
                "Texas",
                "arlington-water",
                "failed-test-repair",
                "Failed test repair or retest",
                "commercial",
                "Stress Test Commerce Plaza",
                "Morgan Alvarez",
                "ACC-2049",
                "Stress QA Backflow Office",
                "office-stress-qa",
                "office-stress-qa-arlington-water",
                "Jordan Lee",
                "972-555-0177",
                "stress@example.com",
                "BPAT-7781",
                LocalDate.of(2026, 5, 28),
                repeat(
                        "This office notice explains why the annual backflow test is still open, what the customer should expect next, and why a follow-up is required before the utility can consider the file complete.",
                        8
                ),
                "Internal note stays inside the office record only.",
                "/vendors/customer-briefs",
                "Failed backflow test for Stress Test Commerce Plaza",
                "This brief is tracking a failed-test repair workflow.",
                List.of(
                        "Approve the repair and retest work for the failed device before 2026-05-28.",
                        "Keep site access clear until the passing retest is complete."
                ),
                List.of(
                        "Document that this is still an active failed-test repair workflow, not a closed job.",
                        "Submit the passing follow-up once the repair work is complete."
                ),
                List.of(
                        "Approve the repair and retest work for the failed device before 2026-05-28.",
                        "Keep site access clear until the passing retest is complete."
                ),
                List.of(
                        "Repair approval does not close a failed-test file by itself.",
                        "A passed retest does not finish the failed-device job until the accepted result reaches the utility record."
                ),
                "/utilities/texas/arlington-water-utilities/failed-test",
                "https://www.arlingtontx.gov/city_hall/departments/water_utilities",
                "Official program page",
                "/guides/failed-backflow-test-next-steps",
                LocalDate.of(2026, 4, 18),
                "Repair and retest still come first. After that, the passing result still has to be sent through the utility record.",
                "Failed backflow test for Stress Test Commerce Plaza",
                "Here is the backflow result update for Stress Test Commerce Plaza.",
                "4800 Longform Drive, Grand Prairie, TX",
                "Rear riser room beside loading dock wall",
                "Jordan Lee",
                "G-3148",
                "Calibration due 2027-01-15",
                repeat(
                        "Field note: access was tight, the office should keep the customer aware of the re-entry window, and the meter-side condition should be rechecked during the next visit.",
                        10
                ),
                "Watts 909-QA Long Body Assembly",
                "2 in.",
                "RP",
                "RP-STRESS-88421",
                "5.8 psid",
                "2.4 psid",
                "3.1 psid",
                repeat(
                        "Repair summary: spring kit reviewed, shutoff isolated, test cocks cleaned, relief assembly checked, and return visit is still required after parts verification.",
                        10
                ),
                "fail",
                "Failed test",
                LocalDate.of(2026, 4, 18),
                "pending-submission",
                "Pending utility submission",
                LocalDate.of(2026, 4, 19),
                "BSI-STRESS-2049",
                "RPT-STRESS-88421",
                repeat(
                        "The device failed the field test because the relief valve and check assembly readings did not hold within the expected range, so repair and a passing retest are still required before the utility record can be closed.",
                        10
                )
        );

        byte[] pdf = handoffPdfService.renderCloseoutPacket(
                handoff,
                "Repair and retest still come first. After that, the passing result still has to be sent through the utility record."
        );

        assertThat(pdf).isNotEmpty();
        assertThat(pdf.length).isGreaterThan(2_000);
        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    private static String repeat(String sentence, int count) {
        return String.join(" ", java.util.Collections.nCopies(count, sentence));
    }
}
