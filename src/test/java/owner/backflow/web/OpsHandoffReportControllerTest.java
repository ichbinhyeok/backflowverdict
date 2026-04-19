package owner.backflow.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import owner.backflow.service.HandoffEventRepository;
import owner.backflow.service.HandoffChannelReportService;
import owner.backflow.service.HandoffRecord;
import owner.backflow.service.HandoffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.ops.verification-token=test-ops-token",
        "app.ops.allow-local-requests=false",
        "app.leads.root=./build/test-handoff-report-storage"
})
@AutoConfigureMockMvc
class OpsHandoffReportControllerTest {
    private static final Path ROOT = Path.of("./build/test-handoff-report-storage");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HandoffRepository handoffRepository;

    @Autowired
    private HandoffEventRepository handoffEventRepository;

    @Autowired
    private HandoffChannelReportService handoffChannelReportService;

    @BeforeEach
    void cleanStorage() throws IOException {
        if (!Files.exists(ROOT)) {
            return;
        }
        try (var paths = Files.walk(ROOT)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Failed to clean report storage " + path, exception);
                        }
                    });
        }
    }

    @Test
    void opsReportShowsRepeatUsageAndEventCounts() throws Exception {
        String firstResultPath = createHandoff(
                "gptx-water",
                "general-testing",
                "pass",
                "submitted",
                "DFW Backflow Services",
                "Jordan Lee",
                "gptx-water",
                ""
        );
        String secondResultPath = createHandoff(
                "gptx-water",
                "failed-test-repair",
                "fail",
                "pending-submission",
                "DFW Backflow Services",
                "Jordan Lee",
                "gptx-water",
                "Repair required before retest."
        );
        String thirdResultPath = createHandoff(
                "garland-water",
                "general-testing",
                "pass",
                "submitted",
                "Lakeside Backflow",
                "Avery Chen",
                "garland-water",
                ""
        );

        HandoffRecord first = handoffRepository.findByInternalToken(extractInternalToken(firstResultPath));
        HandoffRecord second = handoffRepository.findByInternalToken(extractInternalToken(secondResultPath));
        HandoffRecord third = handoffRepository.findByInternalToken(extractInternalToken(thirdResultPath));
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(third).isNotNull();

        openHandoff(firstResultPath, first.publicToken());
        openHandoff(secondResultPath, second.publicToken());
        openHandoff(thirdResultPath, third.publicToken());
        postOfficeEvent(firstResultPath, "brief_link_copied");
        postOfficeEvent(firstResultPath, "brief_link_marked_sent");
        postOfficeEvent(firstResultPath, "brief_feedback_missing_contact");
        postOfficeEvent(secondResultPath, "brief_email_draft_copied");
        postOfficeEvent(secondResultPath, "brief_feedback_testing_only");
        clickTrackedFollowOn(firstResultPath, "gptx-water", "full-rule", "/utilities/texas/grand-prairie-water-utilities");
        clickTrackedFollowOn(firstResultPath, "gptx-water", "help-request", "/guides/failed-backflow-test-next-steps");

        mockMvc.perform(get("/ops/handoffs/report")
                        .header("X-Ops-Token", "test-ops-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Robots-Tag", containsString("noindex")))
                .andExpect(content().string(containsString("Vendor-channel report")))
                .andExpect(content().string(containsString("3 handoffs")))
                .andExpect(content().string(containsString("3 created")))
                .andExpect(content().string(containsString("2 prep signals")))
                .andExpect(content().string(containsString("manual send days 1")))
                .andExpect(content().string(containsString("3 recipient open days")))
                .andExpect(content().string(containsString("2 tracked follow-on clicks")))
                .andExpect(content().string(containsString("handoff_created")))
                .andExpect(content().string(containsString("office_brief_preview_opened")))
                .andExpect(content().string(containsString("public_brief_opened")))
                .andExpect(content().string(containsString("brief_link_marked_sent")))
                .andExpect(content().string(containsString("archive_packet_pdf_downloaded")))
                .andExpect(content().string(containsString("Follow-on CTA clicks")))
                .andExpect(content().string(containsString("Send feedback")))
                .andExpect(content().string(containsString("Missing customer contact")))
                .andExpect(content().string(containsString("Still testing")))
                .andExpect(content().string(containsString("full-rule")))
                .andExpect(content().string(containsString("help-request")))
                .andExpect(content().string(containsString("handoff-packet")))
                .andExpect(content().string(containsString("/guides/failed-backflow-test-next-steps")))
                .andExpect(content().string(containsString("DFW Backflow Services")))
                .andExpect(content().string(containsString("Grand Prairie Water Utilities")))
                .andExpect(content().string(containsString("3 preview opens")))
                .andExpect(content().string(containsString("6 total")))
                .andExpect(content().string(containsString("2")))
                .andExpect(content().string(containsString("public_brief_pdf_downloaded")))
                .andExpect(content().string(containsString("archive_packet_opened")));
    }

    @Test
    void opsReportDedupesRepeatedPublicOpenIntoOneOpenDay() throws Exception {
        String resultPath = createHandoff(
                "gptx-water",
                "general-testing",
                "pass",
                "submitted",
                "DFW Backflow Services",
                "Jordan Lee",
                "gptx-water",
                ""
        );
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();

        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk());

        HandoffChannelReportService.HandoffChannelReport report = handoffChannelReportService.buildReport();
        assertThat(report.recipientOpenCount()).isEqualTo(1);
        assertThat(report.publicBriefOpenedCount()).isEqualTo(1);
    }

    @Test
    void opsReportDedupesRepeatedManualSendConfirmationIntoOneSendDay() throws Exception {
        String resultPath = createHandoff(
                "gptx-water",
                "general-testing",
                "pass",
                "submitted",
                "DFW Backflow Services",
                "Jordan Lee",
                "gptx-water",
                ""
        );

        postOfficeEvent(resultPath, "brief_link_marked_sent");
        postOfficeEvent(resultPath, "brief_email_marked_sent");

        HandoffChannelReportService.HandoffChannelReport report = handoffChannelReportService.buildReport();
        assertThat(report.markedSentCount()).isEqualTo(1);
    }

    @Test
    void reportGroupsSameOfficeWhenOnlyVendorEmailChanges() throws Exception {
        createHandoff(
                "gptx-water",
                "general-testing",
                "pass",
                "submitted",
                "DFW Backflow Services",
                "Jordan Lee",
                "972-555-0144",
                "dispatch@dfwbackflow.example",
                "gptx-water",
                ""
        );
        createHandoff(
                "gptx-water",
                "failed-test-repair",
                "fail",
                "pending-submission",
                "DFW Backflow Services",
                "Jordan Lee",
                "972-555-0144",
                "office@dfwbackflow.example",
                "gptx-water",
                "Repair required before retest."
        );

        HandoffChannelReportService.HandoffChannelReport report = handoffChannelReportService.buildReport();
        assertThat(report.repeatVendorUtilityRows()).hasSize(1);
        HandoffChannelReportService.VendorUtilityUsageRow usage = report.repeatVendorUtilityRows().getFirst();
        assertThat(usage.vendorCompanyName()).isEqualTo("DFW Backflow Services");
        assertThat(usage.utilityId()).isEqualTo("gptx-water");
        assertThat(usage.handoffCount()).isEqualTo(2);
    }

    @Test
    void reportGroupsSameOfficeWhenOnlyEmailLocalPartChangesAndPhoneIsBlank() throws Exception {
        createHandoff(
                "gptx-water",
                "general-testing",
                "pass",
                "submitted",
                "DFW Backflow Services",
                "Jordan Lee",
                "",
                "dispatch@dfwbackflow.example",
                "gptx-water",
                ""
        );
        createHandoff(
                "gptx-water",
                "failed-test-repair",
                "fail",
                "pending-submission",
                "DFW Backflow Services",
                "Jordan Lee",
                "",
                "office@dfwbackflow.example",
                "gptx-water",
                "Repair required before retest."
        );

        HandoffChannelReportService.HandoffChannelReport report = handoffChannelReportService.buildReport();
        assertThat(report.repeatVendorUtilityRows()).hasSize(1);
        HandoffChannelReportService.VendorUtilityUsageRow usage = report.repeatVendorUtilityRows().getFirst();
        assertThat(usage.vendorCompanyName()).isEqualTo("DFW Backflow Services");
        assertThat(usage.handoffCount()).isEqualTo(2);
    }

    private String createHandoff(
            String utilityId,
            String issueType,
            String resultStatus,
            String submissionStatus,
            String vendorCompanyName,
            String vendorContactName,
            String sourceUtilityId,
            String failedReason
    ) throws Exception {
        return createHandoff(
                utilityId,
                issueType,
                resultStatus,
                submissionStatus,
                vendorCompanyName,
                vendorContactName,
                "972-555-0144",
                "",
                sourceUtilityId,
                failedReason
        );
    }

    private String createHandoff(
            String utilityId,
            String issueType,
            String resultStatus,
            String submissionStatus,
            String vendorCompanyName,
            String vendorContactName,
            String vendorPhone,
            String vendorEmail,
            String sourceUtilityId,
            String failedReason
    ) throws Exception {
        return mockMvc.perform(post("/handoffs")
                        .param("utilityId", utilityId)
                        .param("issueType", issueType)
                        .param("resultStatus", resultStatus)
                        .param("submissionStatus", submissionStatus)
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", vendorCompanyName)
                        .param("vendorContactName", vendorContactName)
                        .param("vendorPhone", vendorPhone)
                        .param("vendorEmail", vendorEmail)
                        .param("dueDate", "2026-05-01")
                        .param("testDate", "2026-04-18")
                        .param("failedReason", failedReason)
                        .param("sourcePath", "/utilities/" + sourceUtilityId))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();
    }

    private void openHandoff(String resultPath, String publicToken) throws Exception {
        mockMvc.perform(get(resultPath))
                .andExpect(status().isOk());

        mockMvc.perform(get("/handoffs/brief/" + publicToken)
                        .param("viewer", "office"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/handoffs/brief/" + publicToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/handoffs/brief/" + publicToken + "/pdf"))
                .andExpect(status().isOk());

        mockMvc.perform(get(resultPath + "/packet"))
                .andExpect(status().isOk());

        mockMvc.perform(get(resultPath + "/packet.pdf"))
                .andExpect(status().isOk());
    }

    private void postOfficeEvent(String resultPath, String eventType) throws Exception {
        mockMvc.perform(post(resultPath + "/events")
                        .param("eventType", eventType))
                .andExpect(status().isNoContent());
    }

    private void clickTrackedFollowOn(
            String resultPath,
            String utilityId,
            String ctaType,
            String destination
    ) throws Exception {
        mockMvc.perform(get("/r/cta")
                        .param("next", destination)
                        .param("pageFamily", "handoff-packet")
                        .param("utilityId", utilityId)
                        .param("ctaType", ctaType)
                        .param("source", resultPath + "/packet"))
                .andExpect(status().is3xxRedirection());
    }

    private String extractInternalToken(String resultPath) {
        return resultPath.replace("/handoffs/", "");
    }
}
