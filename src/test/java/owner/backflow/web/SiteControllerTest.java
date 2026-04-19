package owner.backflow.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import owner.backflow.ops.FreshnessAuditService;
import owner.backflow.service.LeadRoutingService;

@SpringBootTest(properties = {
        "app.ops.verification-token=test-ops-token",
        "app.site.ga-measurement-id=G-TEST123",
        "app.site.support-email=support@backflowpath.com",
        "app.site.support-phone=+1-555-0100"
})
@AutoConfigureMockMvc
class SiteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FreshnessAuditService freshnessAuditService;

    @Test
    void homePageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("backflow testing rules before you schedule the work.")))
                .andExpect(content().string(containsString("Browse state guides")))
                .andExpect(content().string(containsString("See utility examples")))
                .andExpect(content().string(containsString("What stays official")))
                .andExpect(content().string(not(containsString("Request a quote"))))
                .andExpect(content().string(not(containsString("Protocol v4.2 compliance engine"))))
                .andExpect(content().string(not(containsString("JSON is the source of truth"))))
                .andExpect(content().string(containsString("Arizona backflow testing requirements")))
                .andExpect(content().string(containsString("Florida backflow testing requirements")))
                .andExpect(content().string(containsString("Organization")))
                .andExpect(content().string(containsString("WebSite")))
                .andExpect(content().string(containsString("support@backflowpath.com")))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")))
                .andExpect(content().string(containsString("window.dataLayer = window.dataLayer || [];")))
                .andExpect(content().string(containsString("function gtag(){dataLayer.push(arguments);}")))
                .andExpect(content().string(containsString("request_help_click")))
                .andExpect(content().string(containsString("provider_website_click")))
                .andExpect(content().string(containsString("tester_route_click")))
                .andExpect(content().string(containsString("lead_form_submit")))
                .andExpect(content().string(containsString("href=\"/about\"")))
                .andExpect(content().string(containsString("href=\"/methodology\"")))
                .andExpect(content().string(containsString("href=\"/editorial-standards\"")))
                .andExpect(content().string(containsString("href=\"/corrections\"")))
                .andExpect(content().string(containsString("href=\"/contact\"")));
    }

    @Test
    void publicIndexPagesLoad() throws Exception {
        String statesHelpPath = htmlHref(LeadRoutingService.requestHelpPath("", "/states", "", "state-index"));
        String metrosHelpPath = htmlHref(LeadRoutingService.requestHelpPath("", "/metros", "", "metro-index"));
        String guidesHelpPath = htmlHref(LeadRoutingService.requestHelpPath("", "/guides", "", "guide-index"));

        mockMvc.perform(get("/states"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Start with a state guide, then open the exact utility page.")))
                .andExpect(content().string(containsString("Browse by state")))
                .andExpect(content().string(containsString(statesHelpPath)));

        mockMvc.perform(get("/metros"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Compare nearby utilities and public provider coverage in one place.")))
                .andExpect(content().string(containsString("Browse by metro")))
                .andExpect(content().string(containsString(metrosHelpPath)));

        mockMvc.perform(get("/guides"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Answer common backflow questions without losing the local rule.")))
                .andExpect(content().string(containsString("Browse guides")))
                .andExpect(content().string(containsString(guidesHelpPath)));

        mockMvc.perform(get("/privacy"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Privacy and lead routing notice")))
                .andExpect(content().string(containsString("the same request may be shared with one or more active sponsors that cover the verified utility")))
                .andExpect(content().string(containsString("noindex,follow")))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")));

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("What BackflowPath is built to do")))
                .andExpect(content().string(containsString("Authority first")));

        mockMvc.perform(get("/methodology"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("How BackflowPath verifies local rules")))
                .andExpect(content().string(containsString("verification-code-tl")));

        mockMvc.perform(get("/editorial-standards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Writing rules for a source-backed compliance site")))
                .andExpect(content().string(containsString("Do not blur the authority layer")));

        mockMvc.perform(get("/corrections"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("How BackflowPath handles errors and stale rules")))
                .andExpect(content().string(containsString("Verify against the source")));

        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact BackflowPath")))
                .andExpect(content().string(containsString("support@backflowpath.com")))
                .andExpect(content().string(containsString("+1-555-0100")))
                .andExpect(content().string(containsString(htmlHref(
                        LeadRoutingService.requestHelpPath("", "/contact", "", "contact")
                ))));
    }

    @Test
    void providerPageEncodesTrackedAuthoritySourceLinks() throws Exception {
        mockMvc.perform(get("/providers/austin-1st-home-commercial-services/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        htmlHref(CtaPaths.trackedPath(
                                "https://services.austintexas.gov/water/weirs/index.cfm?fuseaction=report.publicWSCTechEmployer&tt=1",
                                "provider-profile",
                                "",
                                "austin-1st-home-commercial-services",
                                "authority-source",
                                "/providers/austin-1st-home-commercial-services/"
                        ))
                )));
    }

    private String htmlHref(String value) {
        return value.replace("&", "&amp;");
    }

    @Test
    void vendorCustomerBriefPageLoads() throws Exception {
        mockMvc.perform(get("/vendors/customer-briefs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Turn an annual notice or failed test into a customer brief in 2 minutes.")))
                .andExpect(content().string(containsString("View sample demo")))
                .andExpect(content().string(containsString("Office lane")))
                .andExpect(content().string(containsString("Open the cold-email demo")))
                .andExpect(content().string(containsString("Customer brief, not the office record")))
                .andExpect(content().string(containsString("portal screenshots")))
                .andExpect(content().string(containsString("Annual notice brief")))
                .andExpect(content().string(containsString("Failed test brief")))
                .andExpect(content().string(containsString("/handoffs/new?utilityId=")))
                .andExpect(content().string(containsString("Grand Prairie Water Utilities")))
                .andExpect(content().string(containsString("customer_brief_entry_click")));
    }

    @Test
    void vendorCustomerBriefDemoPageLoads() throws Exception {
        mockMvc.perform(get("/vendors/customer-brief-demo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Show the customer brief first. Explain the office workflow second.")))
                .andExpect(content().string(containsString("Cold email demo")))
                .andExpect(content().string(containsString("Annual notice sample")))
                .andExpect(content().string(containsString("Failed test sample")))
                .andExpect(content().string(containsString("See office workflow")))
                .andExpect(content().string(containsString("Backflow result for Cedar Ridge Plaza")))
                .andExpect(content().string(containsString("Failed backflow test for Willow Creek Medical Center")))
                .andExpect(content().string(containsString("Sample Backflow Office")))
                .andExpect(content().string(containsString("/vendors/customer-briefs")))
                .andExpect(content().string(containsString("/handoffs/new?utilityId=arlington-water")))
                .andExpect(content().string(containsString("/handoffs/new?utilityId=fort-worth-water")))
                .andExpect(content().string(containsString("customer_brief_entry_click")))
                .andExpect(content().string(containsString("noindex,follow")))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")));
    }

    @Test
    void sitemapAndCanonicalUseConfiguredBackflowPathBaseUrl() throws Exception {
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("https://backflowpath.com/")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("http://localhost:8080/"))));

        mockMvc.perform(get("/utilities/texas/dallas-water-utilities/").header("Host", "review.backflow.test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<link rel=\"canonical\" href=\"https://backflowpath.com/utilities/texas/dallas-water-utilities/\">")))
                .andExpect(content().string(containsString("noindex,follow")))
                .andExpect(header().string("X-Robots-Tag", "noindex,follow"))
                .andExpect(content().string(containsString("property=\"og:title\"")))
                .andExpect(content().string(containsString("name=\"twitter:card\" content=\"summary_large_image\"")))
                .andExpect(content().string(containsString("gtag/js?id=G-TEST123")))
                .andExpect(content().string(containsString("request_help_click")));
    }

    @Test
    void previewHostsStayOutOfDiscoveryFiles() throws Exception {
        mockMvc.perform(get("/robots.txt").header("Host", "review.backflow.test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Disallow: /")))
                .andExpect(content().string(not(containsString("Sitemap:"))));

        mockMvc.perform(get("/sitemap.xml").header("Host", "review.backflow.test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<urlset")))
                .andExpect(content().string(not(containsString("<url><loc>"))))
                .andExpect(content().string(not(containsString("/states/texas/backflow-testing"))));
    }

    @Test
    void faviconRouteRedirectsToSvg() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/favicon.svg"));
    }

    @Test
    void utilityPageLoadsFromJson() throws Exception {
        mockMvc.perform(get("/utilities/texas/grand-prairie-water-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Grand Prairie Water Utilities backflow testing requirements")))
                .andExpect(content().string(containsString("Start with the page that matches your situation")))
                .andExpect(content().string(containsString("Annual testing is required")))
                .andExpect(content().string(containsString("State compliance layer")))
                .andExpect(content().string(containsString("Local questions people actually ask")))
                .andExpect(content().string(containsString("FAQPage")))
                .andExpect(content().string(containsString("BreadcrumbList")))
                .andExpect(content().string(containsString("Submission methods and utility contact")))
                .andExpect(content().string(containsString("/methodology#verification-code-tl")))
                .andExpect(content().string(containsString("/corrections")));

        mockMvc.perform(get("/utilities/texas/garland-water-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Garland")))
                .andExpect(content().string(containsString("10 days of the test")));

        mockMvc.perform(get("/utilities/arizona/phoenix-water-services/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("City of Phoenix Backflow Prevention Program")))
                .andExpect(content().string(containsString("work order")));

        mockMvc.perform(get("/utilities/california/san-diego-public-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("San Diego")))
                .andExpect(content().string(containsString("approved tester list")));

        mockMvc.perform(get("/utilities/california/riverside-public-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Riverside Public Utilities")))
                .andExpect(content().string(containsString("accepted reports")));

        mockMvc.perform(get("/utilities/california/roseville-water-utility/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Roseville")))
                .andExpect(content().string(containsString("30 days")));

        mockMvc.perform(get("/utilities/california/san-jose-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("San Jose Water")))
                .andExpect(content().string(containsString("fire services")));

        mockMvc.perform(get("/utilities/colorado/denver-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Denver Water")))
                .andExpect(content().string(containsString("$250 penalty")));

        mockMvc.perform(get("/utilities/colorado/fort-collins-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fort Collins Utilities")))
                .andExpect(content().string(containsString("48 hours of installation")));

        mockMvc.perform(get("/utilities/colorado/greeley-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greeley")))
                .andExpect(content().string(containsString("Spry Backflow")));

        mockMvc.perform(get("/utilities/colorado/westminster-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Westminster")))
                .andExpect(content().string(containsString("domestic, irrigation, and fire line services")));

        mockMvc.perform(get("/utilities/florida/tampa-water-department/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tampa")))
                .andExpect(content().string(containsString("seven calendar days")));

        mockMvc.perform(get("/utilities/florida/fort-lauderdale-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fort Lauderdale")))
                .andExpect(content().string(containsString("$250 fine")));

        mockMvc.perform(get("/utilities/florida/west-palm-beach-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("West Palm Beach")))
                .andExpect(content().string(containsString("approved contractors list")));

        mockMvc.perform(get("/utilities/florida/palm-beach-county-water-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Palm Beach County Water Utilities")))
                .andExpect(content().string(containsString("compliance workflow")));

        mockMvc.perform(get("/utilities/arizona/tempe-backflow-prevention/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tempe")))
                .andExpect(content().string(containsString("anniversary date")));

        mockMvc.perform(get("/utilities/arizona/glendale-water-services/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Glendale")))
                .andExpect(content().string(containsString("one business day")));

        mockMvc.perform(get("/utilities/arizona/avondale-water-services/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Avondale")))
                .andExpect(content().string(containsString("approved tester")));

        mockMvc.perform(get("/utilities/california/sacramento-water-quality/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sacramento")))
                .andExpect(content().string(containsString("approved tester list")));

        mockMvc.perform(get("/utilities/california/santa-clara-water-sewer-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Santa Clara")))
                .andExpect(content().string(containsString("three or more stories")));

        mockMvc.perform(get("/utilities/california/fresno-water-division/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fresno")))
                .andExpect(content().string(containsString("9,700 backflow devices")));

        mockMvc.perform(get("/utilities/california/modesto-cross-connection-control/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Modesto")))
                .andExpect(content().string(containsString("certified tester list")));

        mockMvc.perform(get("/utilities/california/sacramento-suburban-water-district/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sacramento Suburban Water District")))
                .andExpect(content().string(containsString("approved tester")));

        mockMvc.perform(get("/utilities/california/san-francisco-public-utilities-commission/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("San Francisco Public Utilities Commission")))
                .andExpect(content().string(containsString("certified tester list")));

        mockMvc.perform(get("/utilities/colorado/longmont-water-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Longmont")))
                .andExpect(content().string(containsString("91 days past due")));

        mockMvc.perform(get("/utilities/colorado/thornton-water-services/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thornton")))
                .andExpect(content().string(containsString("Backflow Solutions")));

        mockMvc.perform(get("/utilities/colorado/grand-junction-water-services/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Grand Junction")))
                .andExpect(content().string(containsString("PVBAs")));

        mockMvc.perform(get("/utilities/colorado/castle-rock-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Castle Rock Water")))
                .andExpect(content().string(containsString("Only certified testers")));

        mockMvc.perform(get("/utilities/colorado/englewood-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Englewood")))
                .andExpect(content().string(containsString("new cross-connections")));

        mockMvc.perform(get("/utilities/colorado/arvada-water/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arvada")))
                .andExpect(content().string(containsString("July 31")));

        mockMvc.perform(get("/utilities/florida/tallahassee-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tallahassee")))
                .andExpect(content().string(containsString("biennial")));

        mockMvc.perform(get("/utilities/florida/orange-county-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orange County Utilities")))
                .andExpect(content().string(containsString("Registered Backflow Testers and Irrigation Contractors")));

        mockMvc.perform(get("/utilities/florida/orlando-utilities-commission/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orlando Utilities Commission")))
                .andExpect(content().string(containsString("termination of water service")));

        mockMvc.perform(get("/utilities/florida/jea-backflow-program/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("JEA Backflow Program")))
                .andExpect(content().string(containsString("$35")));

        mockMvc.perform(get("/utilities/florida/sarasota-county-public-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sarasota County Public Utilities")))
                .andExpect(content().string(containsString("hazard ID")));

        mockMvc.perform(get("/utilities/florida/manatee-county-utilities/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Manatee County Utilities")))
                .andExpect(content().string(containsString("30 days")));

        mockMvc.perform(get("/utilities/california/anaheim-cross-connection-control/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Anaheim")))
                .andExpect(content().string(containsString("Orange County Health Care Agency certified tester")));

        mockMvc.perform(get("/utilities/california/patterson-annual-test-program/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Patterson")))
                .andExpect(content().string(containsString("January")));

        mockMvc.perform(get("/utilities/colorado/aspen-cross-connection-control-program/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aspen")))
                .andExpect(content().string(containsString("BSI")));

        mockMvc.perform(get("/utilities/colorado/durango-backflow-prevention/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Durango")))
                .andExpect(content().string(containsString("within five days")));

        mockMvc.perform(get("/utilities/florida/hillsborough-county-backflow-testing/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hillsborough County")))
                .andExpect(content().string(containsString("48 hours")));

        mockMvc.perform(get("/utilities/florida/seminole-county-cross-connection-control-program/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Seminole County")))
                .andExpect(content().string(containsString("20 days")));

        mockMvc.perform(get("/utilities/arizona/prescott-backflow-prevention/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prescott")))
                .andExpect(content().string(containsString("approved backflow tester")));

        mockMvc.perform(get("/utilities/arizona/prescott-valley-backflow-prevention/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prescott Valley")))
                .andExpect(content().string(containsString("not specifically endorsed")));

        mockMvc.perform(get("/utilities/california/pasadena-water-and-power/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pasadena Water and Power")))
                .andExpect(content().string(containsString("Los Angeles County certified")));

        mockMvc.perform(get("/utilities/california/santa-rosa-backflow-prevention/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Santa Rosa")))
                .andExpect(content().string(containsString("30-day shutoff notice")));

        mockMvc.perform(get("/utilities/colorado/parker-water-and-sanitation-district/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Parker Water and Sanitation District")))
                .andExpect(content().string(containsString("10 days")));

        mockMvc.perform(get("/utilities/colorado/lafayette-backflow-compliance/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lafayette")))
                .andExpect(content().string(containsString("May 31")));

        mockMvc.perform(get("/utilities/florida/lee-county-cross-connection-control/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lee County Utilities")))
                .andExpect(content().string(containsString("customer portal")));

        mockMvc.perform(get("/utilities/florida/jupiter-backflow-prevention-details/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Jupiter")))
                .andExpect(content().string(containsString("three notices")));
    }

    @Test
    void utilityPageSurfacesSourceAndSubmissionBeforeCommercialLayer() throws Exception {
        String html = mockMvc.perform(get("/utilities/texas/dallas-water-utilities/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(
                html.indexOf("Source block") < html.indexOf("Commercial layer"),
                "Source block should appear before the commercial layer."
        );
        org.junit.jupiter.api.Assertions.assertTrue(
                html.indexOf("Submission methods and utility contact") < html.indexOf("Commercial layer"),
                "Submission path should appear before the commercial layer."
        );
    }

    @Test
    void stateGuideAndEvergreenGuideLoad() throws Exception {
        mockMvc.perform(get("/states/texas/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Texas backflow testing requirements")))
                .andExpect(content().string(containsString("Fort Worth")))
                .andExpect(content().string(containsString("All live utilities")))
                .andExpect(content().string(containsString("BreadcrumbList")));

        mockMvc.perform(get("/states/arizona/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arizona backflow testing requirements")))
                .andExpect(content().string(containsString("Phoenix")))
                .andExpect(content().string(containsString("Representative state")));

        mockMvc.perform(get("/guides/failed-backflow-test-next-steps"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Failed backflow test next steps")))
                .andExpect(content().string(containsString("Use this guide with local utility pages")))
                .andExpect(content().string(containsString("BreadcrumbList")))
                .andExpect(content().string(containsString("/methodology#verification-code-tl")))
                .andExpect(content().string(containsString("/editorial-standards")))
                .andExpect(content().string(containsString("/corrections")));

        mockMvc.perform(get("/guides/how-we-verify-backflow-rules"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("How we verify local backflow rules")))
                .andExpect(content().string(containsString("Metro clusters where this guide matters")));

        mockMvc.perform(get("/guides/backflow-reporting-portals"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backflow reporting portals")))
                .andExpect(content().string(containsString("Dallas Water Utilities")));

        mockMvc.perform(get("/guides/anniversary-date-vs-calendar-deadline"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Anniversary date vs calendar deadline")))
                .andExpect(content().string(containsString("Tempe")));

        mockMvc.perform(get("/guides/county-certified-vs-utility-approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("County-certified vs utility-approved testers")))
                .andExpect(content().string(containsString("Pasadena")));

        mockMvc.perform(get("/guides/residential-vs-commercial-backflow-rules"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Residential vs commercial backflow rules")))
                .andExpect(content().string(containsString("Fort Worth")));
    }

    @Test
    void failedTestPageLoadsForUtility() throws Exception {
        mockMvc.perform(get("/utilities/texas/fort-worth-water-utilities/failed-test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fort Worth Water Backflow Program failed backflow test next steps")))
                .andExpect(content().string(containsString("Need to explain the failure to the customer first?")))
                .andExpect(content().string(containsString("Need repair, retest, or provider coordination?")))
                .andExpect(content().string(containsString("BreadcrumbList")));
    }

    @Test
    void annualTestingAndFocusPagesLoadWhenConfigured() throws Exception {
        mockMvc.perform(get("/utilities/texas/dallas-water-utilities/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dallas Water Utilities Backflow Prevention Program annual backflow testing")))
                .andExpect(content().string(containsString("SwiftComply")))
                .andExpect(content().string(containsString("source=/utilities/texas/dallas-water-utilities/annual-testing")))
                .andExpect(content().string(containsString("State compliance layer")))
                .andExpect(content().string(containsString("BreadcrumbList")));

        mockMvc.perform(get("/utilities/texas/lewisville-water-utilities/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lewisville")))
                .andExpect(content().string(containsString("BSI Online")));

        mockMvc.perform(get("/utilities/texas/college-station-water-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fire line backflow rules")))
                .andExpect(content().string(containsString("fireline testers")))
                .andExpect(content().string(containsString("FAQPage")));

        mockMvc.perform(get("/utilities/arizona/tucson-water/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reclaimed water")));

        mockMvc.perform(get("/utilities/arizona/mesa-water-resources/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fire contractors")));

        mockMvc.perform(get("/utilities/california/san-diego-public-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fire protection")));

        mockMvc.perform(get("/utilities/california/san-jose-water/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("licensed Cross Connection Specialists")));

        mockMvc.perform(get("/utilities/california/roseville-water-utility/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fire sprinklers")));

        mockMvc.perform(get("/utilities/california/san-francisco-public-utilities-commission/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("authority-first list")));

        mockMvc.perform(get("/utilities/colorado/denver-water/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("May to September")));

        mockMvc.perform(get("/utilities/colorado/fort-collins-utilities/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pressure vacuum breakers")));

        mockMvc.perform(get("/utilities/colorado/arvada-water/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("July 31")));

        mockMvc.perform(get("/utilities/florida/jea-backflow-program/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("double checks")));

        mockMvc.perform(get("/utilities/florida/sarasota-county-public-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fire protection contractors")));

        mockMvc.perform(get("/utilities/florida/manatee-county-utilities/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("30 days")));

        mockMvc.perform(get("/utilities/colorado/greeley-water/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Spry Backflow")));

        mockMvc.perform(get("/utilities/colorado/westminster-water/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fire line services are publicly listed")));

        mockMvc.perform(get("/utilities/florida/broward-county-water-and-wastewater-services/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fire-service trained and qualified technician")));

        mockMvc.perform(get("/utilities/florida/tampa-water-department/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("seven calendar days")));

        mockMvc.perform(get("/utilities/florida/fort-lauderdale-water/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$250 fine")));

        mockMvc.perform(get("/utilities/florida/west-palm-beach-water/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("approved for fire line work")));

        mockMvc.perform(get("/utilities/florida/palm-beach-county-water-utilities/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("county compliance workflow")));

        mockMvc.perform(get("/utilities/arizona/tempe-backflow-prevention/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("45 days before the due date")));

        mockMvc.perform(get("/utilities/arizona/glendale-water-services/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hydrant meters must have an RP assembly")));

        mockMvc.perform(get("/utilities/arizona/avondale-water-services/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("double check valve assembly")));

        mockMvc.perform(get("/utilities/california/santa-clara-water-sewer-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("residential fire sprinkler systems")));

        mockMvc.perform(get("/utilities/california/fresno-water-division/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("9,700 devices")));

        mockMvc.perform(get("/utilities/colorado/longmont-water-utilities/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("two-year replacement timeline")));

        mockMvc.perform(get("/utilities/colorado/thornton-water-services/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backflow Solutions")));

        mockMvc.perform(get("/utilities/colorado/grand-junction-water-services/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("12 inches above")));

        mockMvc.perform(get("/utilities/florida/tallahassee-utilities/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("one-time fee")));

        mockMvc.perform(get("/utilities/florida/orange-county-utilities/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("reclaimed-water")));

        mockMvc.perform(get("/utilities/florida/orlando-utilities-commission/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("25 feet of the point of service")));

        mockMvc.perform(get("/utilities/colorado/aspen-cross-connection-control-program/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("double check backflow preventer")));

        mockMvc.perform(get("/utilities/colorado/durango-backflow-prevention/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fertilizer injection")));

        mockMvc.perform(get("/utilities/florida/seminole-county-cross-connection-control-program/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("residential irrigation meter")));

        mockMvc.perform(get("/utilities/florida/hillsborough-county-backflow-testing/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("registered Hillsborough County certified tester")));

        mockMvc.perform(get("/utilities/arizona/prescott-backflow-prevention/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fire-protection assemblies")));

        mockMvc.perform(get("/utilities/arizona/prescott-valley-backflow-prevention/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alternate-water setup")));

        mockMvc.perform(get("/utilities/california/pasadena-water-and-power/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fire services")));

        mockMvc.perform(get("/utilities/california/santa-rosa-backflow-prevention/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("30 days of the due date")));

        mockMvc.perform(get("/utilities/colorado/parker-water-and-sanitation-district/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("10 days")));

        mockMvc.perform(get("/utilities/colorado/lafayette-backflow-compliance/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("May 31")));

        mockMvc.perform(get("/utilities/florida/lee-county-cross-connection-control/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("county portal")));

        mockMvc.perform(get("/utilities/florida/jupiter-backflow-prevention-details/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("30 days of the due date")));
    }

    @Test
    void remainingPilotFocusPagesLoadWithStructuredContent() throws Exception {
        mockMvc.perform(get("/utilities/texas/arlington-water-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("approved fire line contractor")));

        mockMvc.perform(get("/utilities/texas/sugar-land-water-utilities/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("same date every month")));

        mockMvc.perform(get("/utilities/texas/san-antonio-water-system/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("annual irrigation checkup")));

        mockMvc.perform(get("/utilities/texas/mckinney-water-utilities/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CSS")));

        mockMvc.perform(get("/utilities/texas/leander-water-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("grandfathered")));

        mockMvc.perform(get("/utilities/texas/garland-water-utilities/irrigation"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("rain sensor")));

        mockMvc.perform(get("/utilities/texas/mesquite-water-utilities/fire-line"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("stainless-steel RP assembly")));

        mockMvc.perform(get("/utilities/texas/talty-special-utility-district/annual-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("May 1 or November 1")));
    }

    @Test
    void approvedTesterPageRequiresOfficialList() throws Exception {
        mockMvc.perform(get("/utilities/texas/grand-prairie-water-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Authority list")));

        mockMvc.perform(get("/utilities/texas/round-rock-water-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Smart Earth Backflow Testing")))
                .andExpect(content().string(containsString("Official list entry")));

        mockMvc.perform(get("/utilities/texas/college-station-water-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aqua Backflow Group LLC")));

        mockMvc.perform(get("/utilities/texas/garland-water-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Polk Mechanical")));

        mockMvc.perform(get("/utilities/texas/mesquite-water-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("All Test Service Solutions")));

        mockMvc.perform(get("/utilities/texas/talty-special-utility-district/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Action Sprinkler Repair")));

        mockMvc.perform(get("/utilities/arizona/phoenix-water-services/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Western Backflow Testing LLC")));

        mockMvc.perform(get("/utilities/arizona/tucson-water/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tucson Backflow Testing")));

        mockMvc.perform(get("/utilities/arizona/mesa-water-resources/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Next Protection")));

        mockMvc.perform(get("/utilities/arizona/chandler-water-quality/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("4 Peaks Fire Protection")));

        mockMvc.perform(get("/utilities/arizona/tempe-backflow-prevention/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Servworx Plumbing")));

        mockMvc.perform(get("/utilities/arizona/glendale-water-services/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backflow Prevention Device Inc")));

        mockMvc.perform(get("/utilities/arizona/avondale-water-services/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Affordable Fire")));

        mockMvc.perform(get("/utilities/arizona/prescott-backflow-prevention/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arizona Backflow Care LLC")));

        mockMvc.perform(get("/utilities/arizona/scottsdale-water/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("JAZ Backflow Prevention")))
                .andExpect(content().string(containsString("Western Backflow Testing LLC")));

        mockMvc.perform(get("/utilities/california/san-diego-public-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bernard Clarke")));

        mockMvc.perform(get("/utilities/california/irvine-ranch-water-district/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("American Backflow Services")))
                .andExpect(content().string(containsString("BAVCO")));

        mockMvc.perform(get("/utilities/california/riverside-public-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hovey Rooter Service")));

        mockMvc.perform(get("/utilities/california/sacramento-water-quality/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Perkins Backflow Testing")));

        mockMvc.perform(get("/utilities/california/san-francisco-public-utilities-commission/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("415 Backflow Testing")));

        mockMvc.perform(get("/utilities/california/anaheim-cross-connection-control/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Accurate Backflow Testing &amp; Valve Repair")));

        mockMvc.perform(get("/utilities/california/patterson-annual-test-program/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Same Day Backflow")));

        mockMvc.perform(get("/utilities/california/santa-rosa-backflow-prevention/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ABP Backflow Inc.")));

        mockMvc.perform(get("/utilities/colorado/fort-collins-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fort Collins Backflow Service")));

        mockMvc.perform(get("/utilities/colorado/greeley-water/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AJ's Backflow Testing")));

        mockMvc.perform(get("/utilities/colorado/castle-rock-water/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baker Backflow Testing")));

        mockMvc.perform(get("/utilities/colorado/arvada-water/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("J AND M BACKFLOW TESTING")));

        mockMvc.perform(get("/utilities/colorado/aspen-cross-connection-control-program/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Atlas Backflow")));

        mockMvc.perform(get("/utilities/colorado/durango-backflow-prevention/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No local official entries are cached yet")));

        mockMvc.perform(get("/utilities/florida/west-palm-beach-water/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("A1 Teddy Feldman Plumbing Co.")));

        mockMvc.perform(get("/utilities/florida/tallahassee-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No local official entries are cached yet")));

        mockMvc.perform(get("/utilities/florida/orange-county-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No local official entries are cached yet")));

        mockMvc.perform(get("/utilities/florida/jea-backflow-program/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bob's Backflow")));

        mockMvc.perform(get("/utilities/florida/sarasota-county-public-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ADVANCED PLUMBING SOLUTIONS")));

        mockMvc.perform(get("/utilities/florida/manatee-county-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("GREEN COAST BACKFLOWS, INC")));

        mockMvc.perform(get("/utilities/florida/hillsborough-county-backflow-testing/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No local official entries are cached yet")));

        mockMvc.perform(get("/utilities/florida/seminole-county-cross-connection-control-program/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Brad's Backflow Service")));

        mockMvc.perform(get("/utilities/texas/arlington-water-utilities/approved-testers"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/utilities/texas/mckinney-water-utilities/approved-testers"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/utilities/texas/austin-water-utilities/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("WATERCREST BACKFLOW")));

        mockMvc.perform(get("/utilities/california/modesto-cross-connection-control/approved-testers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aldrich Backflow Services")));
    }

    @Test
    void directoryOnlyTesterPagesLoadWhenInventoryExists() throws Exception {
        mockMvc.perform(get("/utilities/arizona/prescott-valley-backflow-prevention/find-a-tester"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Service 1st Fire Protection")))
                .andExpect(content().string(containsString("Non-official directory")));

        mockMvc.perform(get("/utilities/california/pasadena-water-and-power/find-a-tester"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BAVCO")))
                .andExpect(content().string(containsString("County-certified tester directory entry")));
    }

    @Test
    void findATesterPageStaysHiddenWithoutPublicProviderInventory() throws Exception {
        mockMvc.perform(get("/utilities/texas/arlington-water-utilities/find-a-tester"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cityAliasRedirectsOrBridgesBasedOnMode() throws Exception {
        mockMvc.perform(get("/cities/texas/grand-prairie/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/texas/grand-prairie-water-utilities/"));

        mockMvc.perform(get("/cities/texas/arlington/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/html")))
                .andExpect(content().string(containsString("noindex,follow")));

        mockMvc.perform(get("/cities/california/sacramento/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/california/sacramento-water-quality/"));

        mockMvc.perform(get("/cities/colorado/longmont/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/colorado/longmont-water-utilities/"));

        mockMvc.perform(get("/cities/florida/orlando/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/florida/orlando-utilities-commission/"));

        mockMvc.perform(get("/cities/california/san-francisco/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/california/san-francisco-public-utilities-commission/"));

        mockMvc.perform(get("/cities/colorado/castle-rock/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/colorado/castle-rock-water/"));

        mockMvc.perform(get("/cities/florida/jacksonville/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/florida/jea-backflow-program/"));

        mockMvc.perform(get("/cities/california/anaheim/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/california/anaheim-cross-connection-control/"));

        mockMvc.perform(get("/cities/colorado/aspen/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/colorado/aspen-cross-connection-control-program/"));

        mockMvc.perform(get("/cities/arizona/prescott/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/arizona/prescott-backflow-prevention/"));

        mockMvc.perform(get("/cities/california/pasadena/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/california/pasadena-water-and-power/"));

        mockMvc.perform(get("/cities/colorado/parker/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/colorado/parker-water-and-sanitation-district/"));

        mockMvc.perform(get("/cities/florida/fort-myers/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("noindex,follow")));

        mockMvc.perform(get("/cities/florida/jupiter/backflow-testing"))
                .andExpect(status().isMovedPermanently())
                .andExpect(redirectedUrl("/utilities/florida/jupiter-backflow-prevention-details/"));
    }

    @Test
    void sitemapAndRobotsExposeCoreRoutes() throws Exception {
        mockMvc.perform(get("/sitemap.xml"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/xml")))
                .andExpect(content().string(containsString("/about")))
                .andExpect(content().string(containsString("/methodology")))
                .andExpect(content().string(containsString("/editorial-standards")))
                .andExpect(content().string(containsString("/corrections")))
                .andExpect(content().string(containsString("/contact")))
                .andExpect(content().string(containsString("/states/texas/backflow-testing")))
                .andExpect(content().string(containsString("/states/arizona/backflow-testing")))
                .andExpect(content().string(containsString("/states/california/backflow-testing")))
                .andExpect(content().string(containsString("/states/colorado/backflow-testing")))
                .andExpect(content().string(containsString("/states/florida/backflow-testing")))
                .andExpect(content().string(containsString("/guides/backflow-test-cost")))
                .andExpect(content().string(containsString("/guides/backflow-reporting-portals")))
                .andExpect(content().string(containsString("/guides/anniversary-date-vs-calendar-deadline")))
                .andExpect(content().string(containsString("/guides/county-certified-vs-utility-approved-testers")))
                .andExpect(content().string(containsString("/guides/residential-vs-commercial-backflow-rules")))
                .andExpect(content().string(containsString("/utilities/texas/dallas-water-utilities/annual-testing")))
                .andExpect(content().string(containsString("/utilities/arizona/phoenix-water-services/approved-testers")))
                .andExpect(content().string(containsString("/utilities/california/riverside-public-utilities/approved-testers")))
                .andExpect(content().string(containsString("/utilities/california/sacramento-water-quality/approved-testers")))
                .andExpect(content().string(containsString("/utilities/california/san-francisco-public-utilities-commission/approved-testers")))
                .andExpect(content().string(containsString("/utilities/california/modesto-cross-connection-control/annual-testing")))
                .andExpect(content().string(containsString("/utilities/california/santa-clara-water-sewer-utilities/fire-line")))
                .andExpect(content().string(containsString("/utilities/california/san-diego-public-utilities/fire-line")))
                .andExpect(content().string(containsString("/utilities/colorado/fort-collins-utilities/approved-testers")))
                .andExpect(content().string(containsString("/utilities/colorado/castle-rock-water/approved-testers")))
                .andExpect(content().string(containsString("/utilities/colorado/arvada-water/annual-testing")))
                .andExpect(content().string(containsString("/utilities/colorado/longmont-water-utilities/irrigation")))
                .andExpect(content().string(containsString("/utilities/colorado/denver-water/irrigation")))
                .andExpect(content().string(containsString("/utilities/colorado/grand-junction-water-services/irrigation")))
                .andExpect(content().string(containsString("/utilities/florida/fort-lauderdale-water/annual-testing")))
                .andExpect(content().string(containsString("/utilities/florida/jea-backflow-program/approved-testers")))
                .andExpect(content().string(containsString("/utilities/florida/manatee-county-utilities/annual-testing")))
                .andExpect(content().string(containsString("/utilities/florida/sarasota-county-public-utilities/fire-line")))
                .andExpect(content().string(containsString("/utilities/florida/tallahassee-utilities/annual-testing")))
                .andExpect(content().string(containsString("/utilities/florida/orange-county-utilities/approved-testers")))
                .andExpect(content().string(containsString("/utilities/florida/orlando-utilities-commission/fire-line")))
                .andExpect(content().string(containsString("/utilities/florida/west-palm-beach-water/fire-line")))
                .andExpect(content().string(containsString("/utilities/florida/tampa-water-department/annual-testing")))
                .andExpect(content().string(containsString("/utilities/california/anaheim-cross-connection-control/approved-testers")))
                .andExpect(content().string(containsString("/utilities/california/patterson-annual-test-program/approved-testers")))
                .andExpect(content().string(containsString("/utilities/colorado/aspen-cross-connection-control-program/fire-line")))
                .andExpect(content().string(containsString("/utilities/colorado/durango-backflow-prevention/irrigation")))
                .andExpect(content().string(containsString("/utilities/florida/hillsborough-county-backflow-testing/annual-testing")))
                .andExpect(content().string(containsString("/utilities/florida/seminole-county-cross-connection-control-program/approved-testers")))
                .andExpect(content().string(containsString("/utilities/arizona/prescott-backflow-prevention/approved-testers")))
                .andExpect(content().string(containsString("/utilities/arizona/prescott-valley-backflow-prevention/find-a-tester")))
                .andExpect(content().string(containsString("/utilities/california/pasadena-water-and-power/find-a-tester")))
                .andExpect(content().string(containsString("/utilities/california/santa-rosa-backflow-prevention/approved-testers")))
                .andExpect(content().string(containsString("/utilities/colorado/parker-water-and-sanitation-district/fire-line")))
                .andExpect(content().string(containsString("/utilities/colorado/lafayette-backflow-compliance/irrigation")))
                .andExpect(content().string(containsString("/utilities/florida/lee-county-cross-connection-control/annual-testing")))
                .andExpect(content().string(containsString("/utilities/florida/jupiter-backflow-prevention-details/annual-testing")))
                .andExpect(content().string(containsString("/metros/arizona/phoenix-metro/backflow-testing")))
                .andExpect(content().string(containsString("/metros/arizona/northern-arizona-water-belt/backflow-testing")))
                .andExpect(content().string(containsString("/metros/florida/greater-orlando/backflow-testing")))
                .andExpect(content().string(containsString("/metros/florida/southwest-florida-water-utilities/backflow-testing")))
                .andExpect(content().string(containsString("/providers/phoenix-western-backflow/")))
                .andExpect(content().string(containsString("/providers/seminole-brads-backflow/")))
                .andExpect(content().string(containsString("/providers/prescott-arizona-backflow-care/")))
                .andExpect(content().string(containsString("/providers/santa-rosa-abp-backflow/")))
                .andExpect(content().string(containsString("/utilities/arizona/tempe-backflow-prevention/approved-testers")))
                .andExpect(content().string(containsString("/utilities/arizona/glendale-water-services/fire-line")))
                .andExpect(content().string(containsString("/utilities/texas/lewisville-water-utilities/irrigation")))
                .andExpect(content().string(containsString("/utilities/texas/college-station-water-utilities/fire-line")))
                .andExpect(content().string(containsString("/utilities/texas/san-antonio-water-system/failed-test")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("/utilities/texas/arlington-water-utilities/find-a-tester"))));

        mockMvc.perform(get("/robots.txt"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sitemap: https://backflowpath.com/sitemap.xml")));
    }

    @Test
    void freshnessAuditBuildsSummary() {
        var report = freshnessAuditService.buildReport();

        org.junit.jupiter.api.Assertions.assertTrue(report.summary().utilityCount() >= 80);
        org.junit.jupiter.api.Assertions.assertEquals(0, report.summary().staleUtilityCount());
        org.junit.jupiter.api.Assertions.assertTrue(report.summary().guideCount() >= 10);
        org.junit.jupiter.api.Assertions.assertTrue(report.summary().stateGuideCount() >= 5);
    }

    @Test
    void metroAndProviderPagesLoad() throws Exception {
        mockMvc.perform(get("/metros/texas/dallas-fort-worth-metroplex/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dallas-Fort Worth backflow testing")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Next Day Backflow Testing"))));

        mockMvc.perform(get("/metros/arizona/phoenix-metro/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Phoenix metro backflow testing")))
                .andExpect(content().string(containsString("Public providers already mapped to this metro")))
                .andExpect(content().string(containsString("Covers 3 utility pages in this metro")))
                .andExpect(content().string(containsString("Review mapped utilities")));

        mockMvc.perform(get("/metros/florida/greater-orlando/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greater Orlando backflow testing")))
                .andExpect(content().string(containsString("Seminole County")));

        mockMvc.perform(get("/metros/arizona/northern-arizona-water-belt/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Northern Arizona backflow testing")))
                .andExpect(content().string(containsString("Prescott Valley")));

        mockMvc.perform(get("/metros/florida/southwest-florida-water-utilities/backflow-testing"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Southwest Florida backflow testing")))
                .andExpect(content().string(containsString("Lee County")));

        mockMvc.perform(get("/providers/phoenix-western-backflow/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Western Backflow Testing LLC")))
                .andExpect(content().string(containsString("City of Phoenix Backflow Prevention Program")))
                .andExpect(content().string(containsString("Phoenix metro backflow testing")))
                .andExpect(content().string(containsString("LocalBusiness")))
                .andExpect(content().string(containsString("Area served across mapped utility pages")))
                .andExpect(content().string(containsString("Backflow testing")))
                .andExpect(content().string(containsString("mapped utilities with an official tester route")))
                .andExpect(content().string(containsString("structured submission steps")));

        mockMvc.perform(get("/providers/seminole-brads-backflow/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Brad's Backflow Service")))
                .andExpect(content().string(containsString("Seminole County Cross Connection Control Program")))
                .andExpect(content().string(containsString("Greater Orlando backflow testing")));

        mockMvc.perform(get("/providers/prescott-arizona-backflow-care/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arizona Backflow Care LLC")))
                .andExpect(content().string(containsString("City of Prescott Backflow Prevention Program")))
                .andExpect(content().string(containsString("Northern Arizona backflow testing")));

        mockMvc.perform(get("/providers/pasadena-bavco/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BAVCO")))
                .andExpect(content().string(containsString("3 mapped utility pages")))
                .andExpect(content().string(containsString("Guides that match this provider's utility mix")))
                .andExpect(content().string(containsString("Open primary utility page")));

        mockMvc.perform(get("/providers/santa-rosa-abp-backflow/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ABP Backflow Inc.")))
                .andExpect(content().string(containsString("Santa Rosa Backflow Prevention Program")))
                .andExpect(content().string(containsString("Northern California backflow testing")));

        mockMvc.perform(get("/providers/next-day-backflow-texas/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointsReturnBackendStatus() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/json")))
                .andExpect(content().string(containsString("\"status\":\"ok\"")))
                .andExpect(content().string(containsString("\"publishedUtilityCount\"")));

        mockMvc.perform(get("/readyz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ready\"")));
    }

    @Test
    void opsEndpointsRequireLocalAddressOrToken() throws Exception {
        mockMvc.perform(post("/ops/verification/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerInitials\":\"TL\",\"note\":\"blocked\"}")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/ops/verification/run")
                        .header("X-Ops-Token", "test-ops-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewerInitials\":\"TL\",\"note\":\"allowed\"}")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"status\":\"ok\"")));
    }
}
