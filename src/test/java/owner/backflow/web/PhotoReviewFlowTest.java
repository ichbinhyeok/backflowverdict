package owner.backflow.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import owner.backflow.service.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "app.leads.root=build/test-data/photo-review",
    "app.admin.username=review-test", "app.admin.password=review-test-only",
    "app.ops.write-freshness-report-on-startup=false"
})
@AutoConfigureMockMvc
class PhotoReviewFlowTest {
    @Autowired MockMvc mvc;
    @Autowired LeadAdminService leads;
    @Autowired LeadPhotoService photos;
    @Autowired LeadReviewService reviews;
    @Autowired AdminCsrfService csrf;
    @Autowired LeadSubmissionGuardService guard;
    @org.springframework.boot.test.web.server.LocalServerPort int port;
    @BeforeEach void reset() { guard.clear(); }

    private byte[] image(String format, int width, int height) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, output);
        return output.toByteArray();
    }
    private String createWithPhoto() throws Exception {
        String email = java.util.UUID.randomUUID() + "@example.com";
        mvc.perform(multipart("/leads").file(new MockMultipartFile("photos", "private-original-name.png", "image/png", image("png", 30, 20)))
                .param("email", email).param("issueType", "model-identification").param("notes", "Read the visible label; size still unknown.")
                .param("consentToRouting", "yes"))
                .andExpect(redirectedUrl("/leads/thanks")).andExpect(flash().attribute("leadSaved", true)).andExpect(flash().attribute("photoCount", 1));
        return leads.listLeads().stream().filter(lead -> email.equals(lead.email())).findFirst().orElseThrow().leadId();
    }
    private MockHttpSession session() {
        var session = new MockHttpSession(); session.setAttribute(AdminController.ADMIN_SESSION_KEY, true); csrf.ensureToken(session); return session;
    }

    @Test void photoRequestIsPersistedAsPrivateNormalizedImageAndRequiresAdminAccess() throws Exception {
        String id = createWithPhoto(), photo = photos.list(id).getFirst();
        byte[] stored = photos.read(id, photo);
        assertEquals(255, stored[0] & 255); assertEquals(216, stored[1] & 255);
        assertEquals(30, ImageIO.read(new ByteArrayInputStream(stored)).getWidth());
        mvc.perform(get("/admin/reviews/" + id + "/photos/" + photo)).andExpect(redirectedUrl("/admin"));
        mvc.perform(get("/admin/reviews/" + id + "/photos/" + photo).session(session()))
                .andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Cache-Control", "no-store")).andExpect(header().string("X-Content-Type-Options", "nosniff"));
        mvc.perform(get("/photos/" + id + "/" + photo + ".jpg")).andExpect(status().isNotFound());
        mvc.perform(get("/admin/reviews/" + id + "/photos/not-a-photo").session(session())).andExpect(status().isNotFound());
    }

    @Test void disguisedFileAndTooManyPhotosNeverPersistAndKeepWrittenContext() throws Exception {
        int before = leads.leadCount();
        var rejected = mvc.perform(multipart("/leads").file(new MockMultipartFile("photos", "fake.jpg", "image/jpeg", "<script>bad</script>".getBytes()))
                .param("email", "invalid-photo@example.com").param("notes", "Keep these details").param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection()).andReturn();
        assertFalse(rejected.getFlashMap().containsKey("leadSaved"));
        assertTrue(rejected.getFlashMap().containsKey("uploadError"));
        assertEquals("Keep these details", ((java.util.Map<?,?>) rejected.getFlashMap().get("formValues")).get("notes"));
        var file = new MockMultipartFile("photos", "one.png", "image/png", image("png", 2, 2));
        mvc.perform(multipart("/leads").file(file).file(file).file(file).file(file).param("email", "many@example.com").param("consentToRouting", "yes"))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("uploadError"));
        assertEquals(before, leads.leadCount());
    }

    @Test void imageBoundsAreCheckedBeforeDecodingAndExifOrientationIsAppliedBeforeMetadataRemoval() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> photos.prepare(List.of(new MockMultipartFile("photos", new byte[(int) LeadPhotoService.MAX_BYTES + 1]))));
        byte[] png = image("png", 2, 2);
        var pngBuffer = java.nio.ByteBuffer.wrap(png); pngBuffer.putInt(16, 5000); pngBuffer.putInt(20, 5000);
        var crc = new java.util.zip.CRC32(); crc.update(png, 12, 17); pngBuffer.putInt(29, (int) crc.getValue());
        assertThrows(IllegalArgumentException.class, () -> photos.prepare(List.of(new MockMultipartFile("photos", "huge.png", "image/png", png))));
        byte[] jpeg = image("jpeg", 20, 10);
        // APP1: Exif + little-endian TIFF, one orientation entry (rotate 90 degrees).
        byte[] exif = new byte[]{(byte)255,(byte)225,0,34,69,120,105,102,0,0,73,73,42,0,8,0,0,0,1,0,18,1,3,0,1,0,0,0,6,0,0,0,0,0,0,0};
        var combined = new ByteArrayOutputStream(); combined.write(jpeg, 0, 2); combined.write(exif); combined.write(jpeg, 2, jpeg.length - 2);
        byte[] normalized = photos.prepare(List.of(new MockMultipartFile("photos", "phone.jpg", "image/jpeg", combined.toByteArray()))).getFirst();
        var decoded = ImageIO.read(new ByteArrayInputStream(normalized)); assertEquals(10, decoded.getWidth()); assertEquals(20, decoded.getHeight());
        assertFalse(new String(normalized, java.nio.charset.StandardCharsets.ISO_8859_1).contains("Exif"));
    }

    @Test void operatorReviewRequiresAuthenticationAndCsrfAndSavesADownloadableUnsentDraft() throws Exception {
        String id = createWithPhoto();
        var session = session();
        mvc.perform(get("/admin/reviews/" + id)).andExpect(redirectedUrl("/admin"));
        mvc.perform(post("/admin/reviews/" + id).session(session).param("confirmed", "Readable model only").param("candidates", "Manufacturer parts sheet")
                .param("unknowns", "Size and failed component").param("nextSteps", "Photograph size marking")).andExpect(status().isForbidden());
        assertTrue(reviews.find(id).savedAt().isBlank());
        mvc.perform(post("/admin/reviews/" + id).session(session).param("_csrf", csrf.ensureToken(session))
                .param("confirmed", "Readable model only").param("candidates", "Manufacturer parts sheet")
                .param("unknowns", "Size and failed component").param("nextSteps", "Photograph size marking"))
                .andExpect(redirectedUrl("/admin/reviews/" + id));
        assertEquals("Size and failed component", reviews.find(id).unknowns());
        mvc.perform(get("/admin/reviews/" + id + "/reply.txt")).andExpect(redirectedUrl("/admin"));
        mvc.perform(get("/admin/reviews/" + id + "/reply.txt").session(session)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Photograph size marking")));
        mvc.perform(get("/admin/reviews/" + id).session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Delivery is not recorded")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("src=\"/analytics.js"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("googletagmanager"))));
    }

    @Test void photosCanBeRemovedByAuthenticatedCsrfProtectedActionWithoutRemovingTheRequest() throws Exception {
        String id = createWithPhoto(); var session = session();
        mvc.perform(post("/admin/reviews/" + id + "/photos/delete").session(session)).andExpect(status().isForbidden());
        assertEquals(1, photos.list(id).size());
        mvc.perform(post("/admin/reviews/" + id + "/photos/delete").session(session).param("_csrf", csrf.ensureToken(session)))
                .andExpect(redirectedUrl("/admin/reviews/" + id));
        assertTrue(photos.list(id).isEmpty());
        assertTrue(leads.listLeads().stream().anyMatch(lead -> id.equals(lead.leadId())));
    }

    @Test void actualTomcatAcceptsTheCompleteBrowserMultipartFormRatherThanOnlyTenParts() throws Exception {
        String boundary = "BackflowReviewTestBoundary";
        String email = java.util.UUID.randomUUID() + "@example.com";
        var body = new ByteArrayOutputStream();
        var values = new java.util.LinkedHashMap<String, String>();
        values.put("fullName", "Local HTTP test"); values.put("phone", ""); values.put("email", email);
        values.put("city", "Test city"); values.put("utilityId", ""); values.put("utilityName", "");
        values.put("propertyType", "residential"); values.put("issueType", "model-identification");
        values.put("notes", "Actual HTTP multipart regression"); values.put("sourcePage", "/backflow-preventer-diagram/");
        values.put("pageFamily", "decision-guide"); values.put("rt", ""); values.put("companyWebsite", ""); values.put("consentToRouting", "yes");
        for (var entry : values.entrySet()) body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"photos\"; filename=\"label.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes());
        body.write(image("png", 30, 20)); body.write(("\r\n--" + boundary + "--\r\n").getBytes());
        var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:" + port + "/leads"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build();
        try (var client = java.net.http.HttpClient.newHttpClient()) {
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            assertEquals(302, response.statusCode(), response.body());
            assertTrue(response.headers().firstValue("Location").orElse("").contains("/leads/thanks"));
        }
        var saved = leads.listLeads().stream().filter(lead -> email.equals(lead.email())).findFirst().orElseThrow();
        assertEquals(1, photos.list(saved.leadId()).size());
    }
}
