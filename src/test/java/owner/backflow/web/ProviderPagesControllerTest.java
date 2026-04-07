package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import owner.backflow.service.LeadSubmissionGuardService;
import owner.backflow.service.ProviderClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.leads.root=build/test-data/provider-claims",
        "app.site.support-email=providers@backflowpath.com",
        "app.delivery.email-enabled=true",
        "app.delivery.from-email=notifications@backflowpath.com",
        "spring.mail.password=test-app-password",
        "app.ops.verification-token=test-ops-token",
        "app.ops.write-freshness-report-on-startup=false"
})
@AutoConfigureMockMvc
class ProviderPagesControllerTest {
    private static final Path CLAIMS_ROOT = Path.of("build", "test-data", "provider-claims");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProviderClaimRepository providerClaimRepository;

    @Autowired
    private LeadSubmissionGuardService leadSubmissionGuardService;

    @Autowired
    private JavaMailSender mailSender;

    @BeforeEach
    void resetClaimsRoot() throws IOException {
        leadSubmissionGuardService.clear();
        reset(mailSender);
        if (!Files.exists(CLAIMS_ROOT)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(CLAIMS_ROOT)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(CLAIMS_ROOT))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Failed to clean provider claim root", exception);
                        }
                    });
        }
    }

    @Test
    void providerPagesLoad() throws Exception {
        mockMvc.perform(get("/for-providers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Claim, correct, or review your BackflowPath listing request")))
                .andExpect(content().string(containsString("Manual review first")));

        mockMvc.perform(get("/pricing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Featured utility placement")))
                .andExpect(content().string(containsString("$49 / month")))
                .andExpect(content().string(containsString("No instant checkout")));

        mockMvc.perform(get("/claim-listing").param("requestType", "featured-metro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No automatic registration")))
                .andExpect(content().string(containsString("providers@backflowpath.com")))
                .andExpect(content().string(containsString("Email follow-up before any featured payment step")))
                .andExpect(content().string(containsString("featured-metro")));
    }

    @Test
    void providerClaimSubmissionIsStored() throws Exception {
        mockMvc.perform(post("/claim-listing")
                        .param("fullName", "=Mina Kim")
                        .param("companyName", "Backflow Field Services")
                        .param("email", "mina@example.com")
                        .param("phone", "555-818-2121")
                        .param("website", "https://example.com")
                        .param("serviceArea", "Dallas-Fort Worth")
                        .param("requestType", "claim-existing-profile")
                        .param("listingReference", "https://backflowpath.com/providers/example/")
                        .param("notes", "@Please update our phone and website.")
                        .param("consentToReview", "yes")
                        .header("Referer", "https://backflowpath.com/for-providers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claim-listing/thanks"));

        org.junit.jupiter.api.Assertions.assertEquals(1, providerClaimRepository.count());
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(CLAIMS_ROOT.resolve("provider-claims.jsonl")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.exists(CLAIMS_ROOT.resolve("provider-claims.csv")));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(CLAIMS_ROOT.resolve("provider-claims.jsonl")).contains("=Mina Kim"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(CLAIMS_ROOT.resolve("provider-claims.csv")).contains("'=Mina Kim"));
        org.junit.jupiter.api.Assertions.assertTrue(Files.readString(CLAIMS_ROOT.resolve("provider-claims.csv")).contains("'@Please update our phone and website."));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        Assertions.assertArrayEquals(new String[] {"providers@backflowpath.com"}, message.getTo());
        Assertions.assertEquals("notifications@backflowpath.com", message.getFrom());
        Assertions.assertEquals("mina@example.com", message.getReplyTo());
        Assertions.assertTrue(message.getSubject().contains("Backflow Field Services"));
        Assertions.assertTrue(message.getText().contains("Request type: Claim existing profile"));
    }

    @Test
    void providerClaimRequiresConsentAndRateLimits() throws Exception {
        mockMvc.perform(post("/claim-listing")
                        .param("fullName", "No Consent")
                        .param("companyName", "Backflow Field Services")
                        .param("email", "mina@example.com")
                        .param("requestType", "featured-utility"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claim-listing?requestType=featured-utility&error=consent"));

        org.junit.jupiter.api.Assertions.assertEquals(0, providerClaimRepository.count());

        mockMvc.perform(post("/claim-listing")
                        .param("fullName", "Bot Claim")
                        .param("companyName", "Backflow Field Services")
                        .param("email", "mina@example.com")
                        .param("requestType", "featured-metro")
                        .param("consentToReview", "yes")
                        .param("officeFax", "spam"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claim-listing/thanks"));

        org.junit.jupiter.api.Assertions.assertEquals(0, providerClaimRepository.count());

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/claim-listing")
                            .with(request -> {
                                request.setRemoteAddr("203.0.113.90");
                                return request;
                            })
                            .param("fullName", "Claim User " + attempt)
                            .param("companyName", "Backflow Field Services")
                            .param("email", "mina@example.com")
                            .param("requestType", "pricing-question")
                            .param("consentToReview", "yes"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/claim-listing/thanks"));
        }

        mockMvc.perform(post("/claim-listing")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.90");
                            return request;
                        })
                        .param("fullName", "Claim User 4")
                        .param("companyName", "Backflow Field Services")
                        .param("email", "mina@example.com")
                        .param("requestType", "pricing-question")
                        .param("consentToReview", "yes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/claim-listing?requestType=pricing-question&error=rate-limit"));

        org.junit.jupiter.api.Assertions.assertEquals(3, providerClaimRepository.count());
    }

    @TestConfiguration
    static class MailTestConfig {
        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }
    }
}
