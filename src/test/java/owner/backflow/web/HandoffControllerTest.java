package owner.backflow.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import owner.backflow.service.HandoffEventRecord;
import owner.backflow.service.HandoffEventRepository;
import owner.backflow.service.HandoffRecord;
import owner.backflow.service.HandoffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "app.ops.verification-token=test-ops-token",
        "app.site.ga-measurement-id=G-TEST123",
        "app.site.support-email=support@backflowpath.com",
        "app.site.support-phone=+1-555-0100",
        "app.leads.root=./build/test-handoff-storage"
})
@AutoConfigureMockMvc
class HandoffControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HandoffRepository handoffRepository;

    @Autowired
    private HandoffEventRepository handoffEventRepository;

    @BeforeEach
    void cleanStorage() throws IOException {
        Path root = Path.of("./build/test-handoff-storage");
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Failed to clean test storage " + path, exception);
                        }
                    });
        }
    }

    @Test
    void builderPageLoadsWithPrefill() throws Exception {
        mockMvc.perform(get("/handoffs/new")
                .param("utilityId", "gptx-water")
                .param("issueType", "general-testing")
                .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create a 2-minute customer brief")))
                .andExpect(content().string(containsString("Load last job")))
                .andExpect(content().string(containsString("Customer brief preview")))
                .andExpect(content().string(containsString("Grand Prairie Water Utilities")))
                .andExpect(content().string(containsString("Annual or routine testing")))
                .andExpect(content().string(containsString("Optional browser memory")))
                .andExpect(content().string(containsString("Saved profile and recent assemblies")))
                .andExpect(content().string(containsString("restores the customer brief and any saved office-only detail")))
                .andExpect(content().string(containsString("var lastOfficeRecordFields = ['propertyType'")))
                .andExpect(content().string(containsString("Save assembly preset")))
                .andExpect(content().string(containsString("Generate customer brief")))
                .andExpect(content().string(containsString("Back to current rule")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Pilot office setup is $149 one-time."))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Irrigation assembly testing"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Fire line testing"))))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")));
    }

    @Test
    void genericBuilderShowsQuickSampleButtons() throws Exception {
        mockMvc.perform(get("/handoffs/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Need a fast demo first?")))
                .andExpect(content().string(containsString("Try annual sample")))
                .andExpect(content().string(containsString("Try failed-test sample")));
    }

    @Test
    void builderDoesNotLockUnsupportedIssueTypeIntoUi() throws Exception {
        mockMvc.perform(get("/handoffs/new")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "fire-line")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("This office handoff is limited to annual notice and failed-test briefs for now.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Fire line testing"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("data-selected-label=\"Fire line testing\""))));
    }

    @Test
    void createAndRenderHandoff() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "submitted")
                        .param("propertyType", "commercial")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("siteAddress", "120 Main Street, Grand Prairie, TX")
                        .param("deviceLocation", "Rear mechanical room")
                        .param("assemblyType", "RP")
                        .param("assemblySize", "2 in.")
                        .param("assemblySerial", "RP-88421")
                        .param("assemblyMakeModel", "Watts 909")
                        .param("testReadingSummary", "CV1 5.8 psid, RV opened at 3.1 psid, CV2 tight.")
                        .param("checkValveOneReading", "5.8")
                        .param("checkValveTwoReading", "2.4")
                        .param("openingPointReading", "3.1")
                        .param("repairSummary", "No repair needed.")
                        .param("testDate", "2026-04-20")
                        .param("submissionDate", "2026-04-21")
                        .param("submissionReference", "BSI-2049")
                        .param("permitOrReportNumber", "RPT-88421")
                        .param("customerFirstName", "Morgan")
                        .param("accountIdentifier", "ACC-2049")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorContactName", "Jordan Lee")
                        .param("vendorPhone", "972-555-0144")
                        .param("vendorEmail", "jordan@dfwbackflow.example")
                        .param("testerLicenseNumber", "BPAT-7781")
                        .param("dueDate", "2026-05-01")
                        .param("noticeSummary", "Annual notice letter says the assembly is due in May.")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/handoffs/*"))
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();
        assertThat(handoff.vendorSlug()).startsWith("office-dfw-backflow-services");
        assertThat(handoff.vendorSlug()).doesNotStartWith("email-");
        assertThat(handoff.officeKey()).startsWith("office-dfw-backflow-services-");
        String publicBriefPath = "/handoffs/brief/" + handoff.publicToken();

        mockMvc.perform(get("/handoffs/" + handoff.handoffId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/handoffs/brief/" + handoff.handoffId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(resultPath))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Customer brief ready")))
                .andExpect(content().string(containsString("Send the customer brief from your office")))
                .andExpect(content().string(containsString("Open customer brief")))
                .andExpect(content().string(containsString("customer-brief-preview")))
                .andExpect(content().string(containsString("source=handoff-result")))
                .andExpect(content().string(containsString("BackflowPath does not deliver the message for you in this free tool.")))
                .andExpect(content().string(containsString("Confirm link sent")))
                .andExpect(content().string(containsString("Customer brief URL")))
                .andExpect(content().string(containsString("Did this reach a real customer?")))
                .andExpect(content().string(containsString("Needs wording changes")))
                .andExpect(content().string(containsString("Still testing")))
                .andExpect(content().string(containsString("Office record")))
                .andExpect(content().string(containsString("Morgan")))
                .andExpect(content().string(containsString("Main Street Retail Center")))
                .andExpect(content().string(containsString("Rear mechanical room")))
                .andExpect(content().string(containsString("RP-88421")))
                .andExpect(content().string(containsString("Watts 909")))
                .andExpect(content().string(containsString("2 in.")))
                .andExpect(content().string(containsString("ACC-2049")))
                .andExpect(content().string(containsString("BPAT-7781")))
                .andExpect(content().string(containsString("BSI-2049")))
                .andExpect(content().string(containsString("RPT-88421")))
                .andExpect(content().string(containsString("CV1 5.8 psid, RV opened at 3.1 psid, CV2 tight.")))
                .andExpect(content().string(containsString("Check valve 1 held at: 5.8 psid")))
                .andExpect(content().string(containsString("Check valve 2 held at: 2.4 psid")))
                .andExpect(content().string(containsString("Relief or air inlet opened at: 3.1 psid")))
                .andExpect(content().string(containsString("No repair needed.")))
                .andExpect(content().string(containsString("DFW Backflow Services")))
                .andExpect(content().string(containsString("Backflow result for Main Street Retail Center")))
                .andExpect(content().string(containsString("Here is the backflow result update for Main Street Retail Center.")))
                .andExpect(content().string(containsString("Annual notice letter says the assembly is due in May.")))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")));

        mockMvc.perform(get(publicBriefPath).param("viewer", "office"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Customer brief")));

        mockMvc.perform(get(publicBriefPath))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Customer brief")))
                .andExpect(content().string(containsString("Plain-language summary")))
                .andExpect(content().string(containsString("What happens on the vendor side")))
                .andExpect(content().string(containsString("Next step")))
                .andExpect(content().string(containsString("DFW Backflow Services")))
                .andExpect(content().string(containsString("View full local rule")))
                .andExpect(content().string(containsString("Official program page")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Request local help"))))
                .andExpect(content().string(containsString("noindex,follow")))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")));

        mockMvc.perform(get("/handoffs/" + handoff.publicToken() + "/packet"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(resultPath + "/packet"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("viewer=office")))
                .andExpect(content().string(containsString("Download PDF")))
                .andExpect(content().string(containsString("Prepared by")))
                .andExpect(content().string(containsString("DFW Backflow Services")))
                .andExpect(content().string(containsString("Portal confirmation or receipt")))
                .andExpect(content().string(containsString("Permit or report number")))
                .andExpect(content().string(containsString("Recorded test readings")))
                .andExpect(content().string(containsString("Check valve 1 held at: 5.8 psid")))
                .andExpect(content().string(containsString("Check valve 2 held at: 2.4 psid")))
                .andExpect(content().string(containsString("Relief or air inlet opened at: 3.1 psid")))
                .andExpect(content().string(containsString("No repair needed.")))
                .andExpect(content().string(containsString("What the office is still finishing")));

        MvcResult briefPdfResult = mockMvc.perform(get(publicBriefPath + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("backflow-result-sheet-main-street-retail-center.pdf")))
                .andExpect(header().string("Content-Disposition", not(containsString(handoff.handoffId()))))
                .andReturn();
        byte[] briefPdfBytes = briefPdfResult.getResponse().getContentAsByteArray();
        assertThat(new String(briefPdfBytes, StandardCharsets.ISO_8859_1))
                .startsWith("%PDF");
        assertA4Pdf(briefPdfBytes);

        MvcResult packetPdfResult = mockMvc.perform(get(resultPath + "/packet.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("backflow-office-record-main-street-retail-center.pdf")))
                .andExpect(header().string("Content-Disposition", not(containsString(handoff.handoffId()))))
                .andReturn();
        byte[] packetPdfBytes = packetPdfResult.getResponse().getContentAsByteArray();
        assertThat(new String(packetPdfBytes, StandardCharsets.ISO_8859_1))
                .startsWith("%PDF");
        assertA4Pdf(packetPdfBytes);

        mockMvc.perform(post(resultPath + "/events")
                        .param("eventType", "brief_link_marked_sent"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post(resultPath + "/events")
                        .param("eventType", "brief_feedback_testing_only"))
                .andExpect(status().isNoContent());

        List<HandoffEventRecord> events = handoffEventRepository.findAll().stream()
                .filter(event -> handoff.handoffId().equals(event.handoffId()))
                .toList();
        assertThat(events)
                .extracting(HandoffEventRecord::eventType)
                .contains(
                        "handoff_created",
                        "internal_result_opened",
                        "office_brief_preview_opened",
                        "public_brief_opened",
                        "archive_packet_opened",
                        "public_brief_pdf_downloaded",
                        "archive_packet_pdf_downloaded",
                        "brief_link_marked_sent",
                        "brief_feedback_testing_only"
                );
        assertThat(events)
                .extracting(HandoffEventRecord::originPath)
                .contains("/utilities/texas/grand-prairie-water-utilities/annual-testing");
        assertThat(events)
                .extracting(HandoffEventRecord::officeKey)
                .containsOnly(handoff.officeKey());
    }

    @Test
    void repeatedPublicOpenSameDayCountsAsOneOpenDayOnResultSurface() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "submitted")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144")
                        .param("dueDate", "2026-05-01")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();

        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get(resultPath))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Open tracking and send notes")))
                .andExpect(content().string(containsString("Recipient open activity landed on 1 day(s)")));
    }

    @Test
    void automatedPreviewDoesNotCountAsRecipientOpenOnResultSurface() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "submitted")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144")
                        .param("dueDate", "2026-05-01")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();

        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken())
                        .header("User-Agent", "Slackbot-LinkExpanding 1.0 (+https://api.slack.com/robots)"))
                .andExpect(status().isOk());

        List<HandoffEventRecord> events = handoffEventRepository.findAll().stream()
                .filter(event -> handoff.handoffId().equals(event.handoffId()))
                .toList();
        assertThat(events)
                .extracting(HandoffEventRecord::eventType)
                .contains("public_brief_opened_automated")
                .doesNotContain("public_brief_opened");

        mockMvc.perform(get(resultPath))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No recipient open yet. This is the signal to watch before you widen the workflow.")));
    }

    @Test
    void repeatedManualSendConfirmationSameDayCountsAsOneSendDayOnResultSurface() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "submitted")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144")
                        .param("dueDate", "2026-05-01")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();

        mockMvc.perform(post(resultPath + "/events")
                        .param("eventType", "brief_link_marked_sent"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post(resultPath + "/events")
                        .param("eventType", "brief_email_marked_sent"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(resultPath))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Office confirmed a send on 1 day(s)")));
    }

    @Test
    void publicBriefDoesNotLeakInternalNote() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144")
                        .param("dueDate", "2026-05-01")
                        .param("noticeSummary", "Customer received an annual compliance notice.")
                        .param("internalNote", "Internal vendor note: awaiting technician routing.")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/handoffs/*"))
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();

        mockMvc.perform(get(resultPath))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vendor-only note:")))
                .andExpect(content().string(containsString("Internal vendor note: awaiting technician routing.")));

        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Plain-language summary")))
                .andExpect(content().string(containsString("Customer received an annual compliance notice.")))
                .andExpect(content().string(containsString("View full local rule")))
                .andExpect(content().string(containsString("Official program page")))
                .andExpect(content().string(containsString("For the governing requirement, use the official rule or program links above.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Internal note"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Internal vendor note: awaiting technician routing."))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Request local help"))));

        mockMvc.perform(get(resultPath + "/packet"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Customer received an annual compliance notice.")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Internal note"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Internal vendor note: awaiting technician routing."))));
    }

    @Test
    void publicBriefUsesAnnualFallbackWhenOptionalReasonFieldsAreBlank() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144")
                        .param("dueDate", "2026-05-01")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/annual-testing"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();

        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("This brief is tied to the site's annual backflow testing requirement")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("active backflow job"))));
    }

    @Test
    void publicBriefUsesFailedWorkflowFallbackWhenReasonFieldsAreBlank() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "failed-test-repair")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144")
                        .param("dueDate", "2026-05-01")
                        .param("sourcePath", "/utilities/texas/grand-prairie-water-utilities/failed-test"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String resultPath = createResult.getResponse().getRedirectedUrl();
        HandoffRecord handoff = handoffRepository.findByInternalToken(extractInternalToken(resultPath));
        assertThat(handoff).isNotNull();

        mockMvc.perform(get("/handoffs/brief/" + handoff.publicToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("This brief is tied to a failed backflow test for this site")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("active backflow job"))));
    }

    @Test
    void failedHandoffRequiresFailureReason() throws Exception {
        mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "failed-test-repair")
                        .param("resultStatus", "fail")
                        .param("submissionStatus", "pending-submission")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("dueDate", "2026-05-01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add the failed reason before generating a failed-test handoff.")));
    }

    @Test
    void customerBriefRequiresDueDateForPassedResult() throws Exception {
        mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add the next due or filing date before generating the customer brief.")));
    }

    @Test
    void customerBriefRequiresSiteAnchor() throws Exception {
        mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("dueDate", "2026-05-01")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("vendorPhone", "972-555-0144"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add the site name or service address before generating the customer brief.")));
    }

    @Test
    void customerBriefRequiresVendorSenderLine() throws Exception {
        mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("dueDate", "2026-05-01")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add a vendor phone or email before generating the customer brief.")));
    }

    @Test
    void unsupportedIssueReturnsBuilderError() throws Exception {
        mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "fire-line")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("This office handoff is limited to annual notice and failed-test briefs for now.")));
    }

    @Test
    void rejectsUnknownOfficeEventType() throws Exception {
        String resultPath = mockMvc.perform(post("/handoffs")
                        .param("utilityId", "gptx-water")
                        .param("issueType", "general-testing")
                        .param("resultStatus", "pass")
                        .param("submissionStatus", "pending-submission")
                        .param("propertyLabel", "Main Street Retail Center")
                        .param("vendorCompanyName", "DFW Backflow Services")
                        .param("dueDate", "2026-05-01")
                        .param("vendorPhone", "972-555-0144"))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        mockMvc.perform(post(resultPath + "/events")
                        .param("eventType", "not-real"))
                .andExpect(status().isBadRequest());
    }

    private void assertA4Pdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isGreaterThan(0);
            PDRectangle mediaBox = document.getPage(0).getMediaBox();
            assertThat(Math.abs(mediaBox.getWidth() - PDRectangle.A4.getWidth())).isLessThan(2.0f);
            assertThat(Math.abs(mediaBox.getHeight() - PDRectangle.A4.getHeight())).isLessThan(2.0f);
        }
    }

    private String extractInternalToken(String resultPath) {
        return resultPath.replace("/handoffs/", "");
    }
}
