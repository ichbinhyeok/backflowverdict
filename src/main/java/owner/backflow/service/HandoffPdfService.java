package owner.backflow.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import owner.backflow.config.AppSiteProperties;
import org.springframework.stereotype.Service;

@Service
public class HandoffPdfService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.US);

    private final AppSiteProperties siteProperties;

    public HandoffPdfService(AppSiteProperties siteProperties) {
        this.siteProperties = siteProperties;
    }

    public byte[] renderCustomerResultSheet(HandoffRecord handoff, String submissionGuidance) {
        String whyGotThis = joinedParagraphs(
                handoff.noticeSummary(),
                handoff.failedReason(),
                customerReasonFallback(handoff)
        );
        String whatYouNeedToDo = handoff.customerActions().isEmpty()
                ? "Review the result, follow the next step, and keep the due date in view if one is shown."
                : handoff.customerActions().get(0);
        String nextStepSupport = handoff.customerActions().size() > 1
                ? handoff.customerActions().get(1)
                : "This job is not fully closed until the utility side no longer shows the site as open.";
        String whoHandlesFiling = !normalize(handoff.vendorCompanyName()).isBlank()
                ? handoff.vendorCompanyName() + " is coordinating the filing or follow-up tied to this brief."
                : "Your vendor is coordinating the filing or follow-up tied to this brief.";
        String body = pageShell(
                "Customer brief",
                handoff,
                metaTable(List.of(
                        metaCell("Utility filing status", handoff.submissionLabel(), true),
                        metaCell("Utility", handoff.utilityName(), false),
                        metaCell("Test date", formatDate(handoff.testDate()), false),
                        metaCell("Action by", formatDate(handoff.dueDate()), false),
                        metaCell("Site", handoff.propertyLabel(), false),
                        metaCell("Prepared by", handoff.vendorCompanyName(), false)
                )),
                priorityBanner("What to do next", whatYouNeedToDo, nextStepSupport)
                        + summaryTable(List.of(
                        summaryCell("Why you got this", whyGotThis),
                        summaryCell("What you need to do", whatYouNeedToDo),
                        summaryCell("Who is handling the filing", whoHandlesFiling)
                ))
                        + banner("Utility filing status", escape(submissionGuidance))
                        + banner("Official source", "Use the local rule or official program page for the governing source behind the job.")
                        + twoUp(
                                section(
                                        "What you need to know",
                                        "Next step",
                                        list(handoff.customerActions(), false)
                                ),
                                section(
                                        "Vendor follow-up",
                                        "What the vendor is handling",
                                        list(handoff.vendorActions(), true)
                                )
                        )
                        + twoUp(
                                section(
                                        "Job details",
                                        "Site and device",
                                        detailList(List.of(
                                                detailRow("Customer or contact", handoff.customerFirstName()),
                                                detailRow("Account or site ID", handoff.accountIdentifier()),
                                                detailRow("Site", handoff.propertyLabel()),
                                                detailRow("Address", handoff.siteAddress()),
                                                detailRow("Device location", handoff.deviceLocation()),
                                                detailRow("Assembly make and model", handoff.assemblyMakeModel()),
                                                detailRow("Assembly size", handoff.assemblySize()),
                                                detailRow("Assembly type", handoff.assemblyType()),
                                                detailRow("Assembly serial", handoff.assemblySerial())
                                        ))
                                ),
                                section(
                                        "Who to contact",
                                        "Vendor contact",
                                        detailList(List.of(
                                                detailRow("Company", handoff.vendorCompanyName()),
                                                detailRow("Contact", handoff.vendorContactName()),
                                                detailRow("Phone", handoff.vendorPhone()),
                                                detailRow("Email", handoff.vendorEmail())
                                        ))
                                )
                        )
                        + optionalSection(
                                (handoff.checkValveOneReading() != null && !handoff.checkValveOneReading().isBlank())
                                        || (handoff.checkValveTwoReading() != null && !handoff.checkValveTwoReading().isBlank())
                                        || (handoff.openingPointReading() != null && !handoff.openingPointReading().isBlank())
                                        || (handoff.permitOrReportNumber() != null && !handoff.permitOrReportNumber().isBlank()),
                                section(
                                        "Recorded field values",
                                        "What was logged on the test record",
                                        detailList(List.of(
                                                detailRow("Check valve 1 held at", handoff.checkValveOneReading()),
                                                detailRow("Check valve 2 held at", handoff.checkValveTwoReading()),
                                                detailRow("Relief or air inlet opened at", handoff.openingPointReading()),
                                                detailRow("Permit or report number", handoff.permitOrReportNumber())
                                        ))
                                )
                        )
                        + optionalSection(
                                !handoff.commonMistakes().isEmpty(),
                                section("Watchouts", "What leaves the file open", list(handoff.commonMistakes(), false))
                        )
                        + footer("For job follow-up, contact the vendor listed on this result. For the governing requirement, use the official rule or program linked from the brief.")
        );
        return renderPdf(handoff.headline() + " result sheet", body);
    }

    public byte[] renderCloseoutPacket(HandoffRecord handoff, String submissionGuidance) {
        String body = pageShell(
                "Office record",
                handoff,
                metaTable(List.of(
                        metaCell("Utility filing status", handoff.submissionLabel(), true),
                        metaCell("Utility", handoff.utilityName(), false),
                        metaCell("Test date", formatDate(handoff.testDate()), false),
                        metaCell("Action by", formatDate(handoff.dueDate()), false),
                        metaCell("Submission date", formatDate(handoff.submissionDate()), false),
                        metaCell("Portal confirmation", handoff.submissionReference(), false)
                )),
                banner("Utility filing status", escape(submissionGuidance))
                        + banner("Result note", joinedParagraphs(handoff.noticeSummary(), handoff.failedReason()))
                        + twoUp(
                                section(
                                        "Device record",
                                        "Assembly details",
                                        detailList(List.of(
                                                detailRow("Device location", handoff.deviceLocation()),
                                                detailRow("Technician", handoff.technicianName()),
                                                detailRow("Gauge ID", handoff.gaugeId()),
                                                detailRow("Calibration reference", handoff.calibrationReference()),
                                                detailRow("Assembly make and model", handoff.assemblyMakeModel()),
                                                detailRow("Assembly size", handoff.assemblySize()),
                                                detailRow("Assembly type", handoff.assemblyType()),
                                                detailRow("Assembly serial", handoff.assemblySerial()),
                                                detailRow("Test date", formatDate(handoff.testDate())),
                                                detailRow("Portal confirmation or receipt", handoff.submissionReference()),
                                                detailRow("Permit or report number", handoff.permitOrReportNumber())
                                        ))
                                ),
                                section(
                                        "Field readings",
                                        "Recorded test readings",
                                        detailList(List.of(
                                                detailRow("Check valve 1 held at", handoff.checkValveOneReading()),
                                                detailRow("Check valve 2 held at", handoff.checkValveTwoReading()),
                                                detailRow("Relief or air inlet opened at", handoff.openingPointReading())
                                        ))
                                )
                        )
                        + optionalSection(
                                hasOfficeNarrative(handoff),
                                section(
                                        "Field notes",
                                        "Repair or reading context",
                                        joinHtml(
                                                paragraphWithLabel("Repairs or materials used", handoff.repairSummary()),
                                                paragraphWithLabel("Additional reading notes", handoff.testReadingSummary())
                                        )
                                )
                        )
                        + twoUp(
                                section(
                                        "Prepared for",
                                        "Customer and site",
                                        detailList(List.of(
                                                detailRow("Customer or contact", handoff.customerFirstName()),
                                                detailRow("Account or site ID", handoff.accountIdentifier()),
                                                detailRow("Site", handoff.propertyLabel()),
                                                detailRow("Address", handoff.siteAddress()),
                                                detailRow("Property type", handoff.propertyType())
                                        ))
                                ),
                                section(
                                        "Prepared by",
                                        "Vendor contact",
                                        detailList(List.of(
                                                detailRow("Company", handoff.vendorCompanyName()),
                                                detailRow("Contact", handoff.vendorContactName()),
                                                detailRow("Phone", handoff.vendorPhone()),
                                                detailRow("Email", handoff.vendorEmail()),
                                                detailRow("Tester license or cert", handoff.testerLicenseNumber())
                                        ))
                                )
                        )
                        + twoUp(
                                section(
                                        "Customer next step",
                                        "What happens now",
                                        list(handoff.customerActions(), false)
                                ),
                                section(
                                        "Office workflow",
                                        "What the office is still finishing",
                                        list(handoff.vendorActions(), true)
                                )
                        )
                        + optionalSection(
                                !handoff.commonMistakes().isEmpty(),
                                section("Watchouts", "What keeps the file open", list(handoff.commonMistakes(), false))
                        )
                        + section(
                                "Source drawer",
                                "Verified local sources",
                                paragraph("Use the full local rule or the official program only when someone needs the governing source behind this office record.")
                                        + linkList(List.of(
                                                linkRow("Customer result sheet", absoluteUrl("/handoffs/brief/" + tokenOrFallback(handoff.publicToken(), handoff.handoffId()))),
                                                linkRow("Full local rule", absoluteUrl(handoff.fullRulePath())),
                                                linkRow("Official program page", handoff.officialProgramUrl())
                                        ))
                        )
        );
        return renderPdf(handoff.headline() + " office record", body);
    }

    private byte[] renderPdf(String documentTitle, String body) {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        @page { size: A4; margin: 12mm; }
                        body {
                            margin: 0;
                            font-family: Arial, Helvetica, sans-serif;
                            color: #17212a;
                            font-size: 9.6pt;
                            line-height: 1.45;
                            background: #ffffff;
                        }
                        .sheet {
                            border: 1px solid #dbe4ee;
                            border-top: 3px solid #131b2e;
                            background: #ffffff;
                            padding: 14px;
                            box-sizing: border-box;
                        }
                        .kicker {
                            font-size: 8pt;
                            font-weight: 700;
                            letter-spacing: 1.3px;
                            text-transform: uppercase;
                            color: #5b6472;
                        }
                        .brandline {
                            margin-bottom: 8px;
                        }
                        .brandmark {
                            display: inline-block;
                            margin-right: 10px;
                            color: #17212a;
                            font-size: 9pt;
                            font-weight: 800;
                            letter-spacing: 1.2px;
                            text-transform: uppercase;
                        }
                        .header-row {
                            width: 100%%;
                            border-collapse: collapse;
                        }
                        .header-row td {
                            vertical-align: top;
                        }
                        .proof {
                            text-align: right;
                            font-size: 8pt;
                            font-weight: 700;
                            letter-spacing: 1.3px;
                            text-transform: uppercase;
                            color: #5b6472;
                            background: #f3f6fc;
                            border: 1px solid #dbe4ee;
                            border-radius: 999px;
                            padding: 5px 8px;
                        }
                        .proof.proof-success {
                            color: #0f766e;
                            background: #f4fbf8;
                            border-color: #bfded4;
                        }
                        .proof.proof-danger {
                            color: #b91c1c;
                            background: #fee2e2;
                            border-color: #fecaca;
                        }
                        .proof.proof-warning {
                            color: #b45309;
                            background: #fef3c7;
                            border-color: #fcd34d;
                        }
                        h1 {
                            margin: 8px 0 8px;
                            font-size: 20pt;
                            line-height: 1.05;
                            page-break-after: avoid;
                        }
                        .intro {
                            margin: 0 0 14px;
                            font-size: 10.4pt;
                            color: #52606d;
                        }
                        .meta-table,
                        .columns {
                            width: 100%%;
                            border-collapse: separate;
                            border-spacing: 8px;
                            margin: 0 -8px;
                            table-layout: fixed;
                        }
                        .meta-card,
                        .summary-card,
                        .section,
                        .banner {
                            border: 1px solid #dbe4ee;
                            border-radius: 8px;
                            background: #f8f9ff;
                            page-break-inside: avoid;
                            break-inside: avoid;
                            overflow-wrap: anywhere;
                        }
                        .meta-card {
                            padding: 10px 12px;
                            vertical-align: top;
                            width: 50%%;
                        }
                        .priority-banner {
                            margin: 0 0 10px;
                            padding: 12px 13px;
                            border-radius: 8px;
                            border: 1px solid #131b2e;
                            background: #131b2e;
                            color: #f8fafc;
                        }
                        .priority-banner .label {
                            color: #dbe4ee;
                            margin-bottom: 7px;
                        }
                        .priority-banner h2 {
                            margin: 0 0 7px;
                            color: #ffffff;
                            font-size: 14pt;
                            line-height: 1.08;
                        }
                        .priority-banner p {
                            margin: 0;
                            color: #dbe4ee;
                            font-size: 9.4pt;
                        }
                        .summary-table {
                            width: 100%%;
                            border-collapse: separate;
                            border-spacing: 8px;
                            margin: 0 -8px 4px;
                            table-layout: fixed;
                        }
                        .summary-card {
                            width: 33.333%%;
                            padding: 10px 12px;
                            vertical-align: top;
                            background: #ffffff;
                        }
                        .summary-card p {
                            margin: 0;
                            color: #2f3f49;
                        }
                        .meta-card.emphasis {
                            background: #eff4ff;
                            border-color: #d3e4fe;
                        }
                        .label {
                            display: block;
                            margin-bottom: 5px;
                            font-size: 7.6pt;
                            font-weight: 700;
                            letter-spacing: 1.3px;
                            text-transform: uppercase;
                            color: #5b6472;
                        }
                        .value {
                            font-size: 10.2pt;
                            font-weight: 700;
                            color: #17212a;
                        }
                        .banner {
                            margin-top: 12px;
                            padding: 11px 12px;
                            background: #eff4ff;
                            border-left: 4px solid #131b2e;
                        }
                        .banner p {
                            margin: 0 0 10px;
                            color: #2f3f49;
                        }
                        .banner p:last-child {
                            margin-bottom: 0;
                        }
                        .section {
                            margin-top: 12px;
                            padding: 12px;
                            vertical-align: top;
                        }
                        .columns td,
                        .meta-table td,
                        .header-row td,
                        .columns tr,
                        .meta-table tr,
                        .header-row tr {
                            page-break-inside: avoid;
                            break-inside: avoid;
                            vertical-align: top;
                        }
                        .columns td {
                            width: 50%%;
                        }
                        .section-dark {
                            background: #131b2e;
                            border-color: #131b2e;
                            color: #f8fafc;
                        }
                        .section-dark .label,
                        .section-dark h2,
                        .section-dark li,
                        .section-dark p {
                            color: #f8fafc;
                        }
                        .section-warm {
                            background: #fff7ed;
                            border-color: #fdba74;
                        }
                        h2 {
                            margin: 5px 0 9px;
                            font-size: 13.2pt;
                            line-height: 1.1;
                            page-break-after: avoid;
                        }
                        p {
                            margin: 0 0 8px;
                            orphans: 3;
                            widows: 3;
                            page-break-inside: avoid;
                            break-inside: avoid;
                        }
                        ul, ol {
                            margin: 0;
                            padding-left: 18px;
                        }
                        li {
                            margin: 0 0 6px;
                            orphans: 2;
                            widows: 2;
                            page-break-inside: avoid;
                            break-inside: avoid;
                        }
                        .detail-list {
                            list-style: none;
                            padding-left: 0;
                        }
                        .detail-list li {
                            margin: 0 0 6px;
                        }
                        .detail-list strong {
                            color: #5b6472;
                        }
                        .narrative-block + .narrative-block {
                            margin-top: 12px;
                        }
                        .narrative-label {
                            display: block;
                            margin-bottom: 6px;
                            font-size: 8pt;
                            font-weight: 700;
                            letter-spacing: 1.1px;
                            text-transform: uppercase;
                            color: #5b6472;
                        }
                        .links {
                            margin-top: 8px;
                            padding-left: 16px;
                        }
                        .links li {
                            word-break: break-all;
                        }
                        .muted-note {
                            margin-top: 12px;
                            padding-top: 10px;
                            border-top: 1px solid #dbe4ee;
                            font-size: 9pt;
                            color: #64748b;
                        }
                        a {
                            color: #131b2e;
                            text-decoration: underline;
                        }
                        .footer {
                            margin-top: 14px;
                            padding-top: 10px;
                            border-top: 1px solid #dbe4ee;
                            font-size: 9pt;
                            color: #64748b;
                            page-break-inside: avoid;
                            break-inside: avoid;
                        }
                    </style>
                </head>
                <body>
                    <div class="sheet">%s</div>
                </body>
                </html>
                """.formatted(body);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to render PDF for " + documentTitle, exception);
        }
    }

    private String pageShell(String kicker, HandoffRecord handoff, String metaTable, String body) {
        return """
                <table class="header-row">
                    <tr>
                        <td>
                            <div class="brandline">
                                <span class="brandmark">%s</span>
                                <span class="kicker">%s</span>
                            </div>
                            <h1>%s</h1>
                            <p class="intro">%s</p>
                        </td>
                        <td class="proof %s">%s</td>
                    </tr>
                </table>
                %s
                %s
                """.formatted(
                escape(siteProperties.siteName()),
                escape(kicker),
                escape(handoff.headline()),
                escape(handoff.publicSummary()),
                proofClass(handoff),
                escape(handoff.resultLabel()),
                metaTable,
                body
        );
    }

    private String metaTable(List<String> cells) {
        List<String> filtered = cells.stream().filter(cell -> !cell.isBlank()).toList();
        if (filtered.isEmpty()) {
            return "";
        }
        List<String> rows = new ArrayList<>();
        for (int index = 0; index < filtered.size(); index += 2) {
            String left = filtered.get(index);
            String right = index + 1 < filtered.size() ? filtered.get(index + 1) : "";
            rows.add("<tr>" + left + right + "</tr>");
        }
        return "<table class=\"meta-table\">" + String.join("", rows) + "</table>";
    }

    private String metaCell(String label, String value, boolean emphasis) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return """
                <td class="meta-card%s">
                    <span class="label">%s</span>
                    <span class="value">%s</span>
                </td>
                """.formatted(
                emphasis ? " emphasis" : "",
                escape(label),
                escape(value)
        );
    }

    private String summaryTable(List<String> cells) {
        List<String> filtered = cells.stream().filter(cell -> !cell.isBlank()).toList();
        if (filtered.isEmpty()) {
            return "";
        }
        List<String> padded = new ArrayList<>(filtered);
        while (padded.size() < 3) {
            padded.add("<td></td>");
        }
        return """
                <table class="summary-table">
                    <tr>
                        %s%s%s
                    </tr>
                </table>
                """.formatted(padded.get(0), padded.get(1), padded.get(2));
    }

    private String priorityBanner(String label, String title, String text) {
        if ((title == null || title.isBlank()) && (text == null || text.isBlank())) {
            return "";
        }
        return """
                <section class="priority-banner">
                    <span class="label">%s</span>
                    <h2>%s</h2>
                    %s
                </section>
                """.formatted(
                escape(label),
                escape(title == null ? "" : title),
                text == null || text.isBlank() ? "" : prose(text)
        );
    }

    private String summaryCell(String label, String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return """
                <td class="summary-card">
                    <span class="label">%s</span>
                    %s
                </td>
                """.formatted(escape(label), prose(text));
    }

    private String banner(String label, String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return """
                <div class="banner">
                    <span class="label">%s</span>
                    %s
                </div>
                """.formatted(escape(label), prose(text));
    }

    private String section(String kicker, String title, String content) {
        return """
                <div class="section">
                    <span class="label">%s</span>
                    <h2>%s</h2>
                    %s
                </div>
                """.formatted(escape(kicker), escape(title), content);
    }

    private String twoUp(String left, String right) {
        if ((left == null || left.isBlank()) && (right == null || right.isBlank())) {
            return "";
        }
        return """
                <table class="columns">
                    <tr>
                        <td width="50%%" valign="top">%s</td>
                        <td width="50%%" valign="top">%s</td>
                    </tr>
                </table>
                """.formatted(left == null ? "" : left, right == null ? "" : right);
    }

    private String list(List<String> items, boolean ordered) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        String tag = ordered ? "ol" : "ul";
        return "<" + tag + ">" + items.stream()
                .map(this::escape)
                .filter(item -> !item.isBlank())
                .map(item -> "<li>" + item + "</li>")
                .reduce("", String::concat) + "</" + tag + ">";
    }

    private String detailList(List<String> rows) {
        List<String> filtered = rows.stream().filter(row -> !row.isBlank()).toList();
        if (filtered.isEmpty()) {
            return "";
        }
        return "<ul class=\"detail-list\">" + String.join("", filtered) + "</ul>";
    }

    private String detailRow(String label, String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return "<li><strong>" + escape(label) + ":</strong> " + escape(value) + "</li>";
    }

    private String paragraph(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return "<p>" + escape(text) + "</p>";
    }

    private String paragraphWithLabel(String label, String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return """
                <div class="narrative-block">
                    <span class="narrative-label">%s</span>
                    %s
                </div>
                """.formatted(escape(label), prose(text));
    }

    private String joinHtml(String... parts) {
        List<String> filtered = new ArrayList<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                filtered.add(part);
            }
        }
        return String.join("", filtered);
    }

    private String prose(String text) {
        String normalized = normalize(text).replace("\r\n", "\n");
        if (normalized.isBlank()) {
            return "";
        }
        List<String> paragraphs = new ArrayList<>();
        for (String block : normalized.split("\\n\\s*\\n")) {
            String trimmed = normalize(block.replace('\n', ' '));
            if (trimmed.isBlank()) {
                continue;
            }
            paragraphs.addAll(chunkNarrative(trimmed, 260));
        }
        return paragraphs.stream()
                .map(paragraph -> "<p>" + escape(paragraph) + "</p>")
                .reduce("", String::concat);
    }

    private List<String> chunkNarrative(String text, int targetLength) {
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return List.of();
        }
        String[] sentences = normalized.split("(?<=[.!?])\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            String trimmedSentence = normalize(sentence);
            if (trimmedSentence.isBlank()) {
                continue;
            }
            if (current.isEmpty()) {
                current.append(trimmedSentence);
                continue;
            }
            if (current.length() + 1 + trimmedSentence.length() <= targetLength) {
                current.append(' ').append(trimmedSentence);
                continue;
            }
            chunks.add(current.toString());
            current = new StringBuilder(trimmedSentence);
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private String linkList(List<String> rows) {
        List<String> filtered = rows.stream().filter(row -> !row.isBlank()).toList();
        if (filtered.isEmpty()) {
            return "";
        }
        return "<ul class=\"links\">" + String.join("", filtered) + "</ul>";
    }

    private String linkRow(String label, String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String safeUrl = escape(url);
        return "<li><strong>" + escape(label) + ":</strong> <a href=\"" + safeUrl + "\">" + safeUrl + "</a></li>";
    }

    private String footer(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return "<div class=\"footer\">" + escape(text) + "</div>";
    }

    private String mutedNote(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return "<div class=\"muted-note\">" + escape(text) + "</div>";
    }

    private String joinedParagraphs(String first, String second) {
        return joinedParagraphs(first, second, "");
    }

    private String joinedParagraphs(String first, String second, String fallback) {
        String firstValue = normalize(first);
        String secondValue = normalize(second);
        if (firstValue.isBlank() && secondValue.isBlank()) {
            return fallback;
        }
        if (firstValue.isBlank()) {
            return secondValue;
        }
        if (secondValue.isBlank()) {
            return firstValue;
        }
        return firstValue + "\n\n" + secondValue;
    }

    private String customerReasonFallback(HandoffRecord handoff) {
        String issueType = normalize(handoff.issueType());
        if ("failed-test-repair".equals(issueType)) {
            return "This brief tracks the site's failed-test repair workflow and the repair, retest, or closeout step tied to that failed device.";
        }
        if ("general-testing".equals(issueType)) {
            return "This brief tracks the site's annual backflow testing requirement and the testing or filing step tied to that notice.";
        }
        return "This result was created from an active backflow job for this site.";
    }

    private String proofClass(HandoffRecord handoff) {
        String resultStatus = normalize(handoff.resultStatus());
        if ("fail".equals(resultStatus)) {
            return "proof-danger";
        }
        if ("unable-to-test".equals(resultStatus)) {
            return "proof-warning";
        }
        return "proof-success";
    }

    private boolean hasOfficeNarrative(HandoffRecord handoff) {
        return !normalize(handoff.repairSummary()).isBlank()
                || !normalize(handoff.testReadingSummary()).isBlank();
    }

    private String optionalSection(boolean include, String content) {
        return include ? content : "";
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private String absoluteUrl(String path) {
        String normalizedPath = normalize(path);
        String baseUrl = normalize(siteProperties.baseUrl()).replaceAll("/+$", "");
        if (normalizedPath.isBlank()) {
            return "";
        }
        if (baseUrl.isBlank()) {
            return normalizedPath;
        }
        if (normalizedPath.startsWith("/")) {
            return baseUrl + normalizedPath;
        }
        return baseUrl + "/" + normalizedPath;
    }

    private String tokenOrFallback(String token, String fallback) {
        String normalizedToken = normalize(token);
        if (!normalizedToken.isBlank()) {
            return normalizedToken;
        }
        return normalize(fallback);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String escape(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }
        return normalized
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
