@import owner.backflow.web.PageMeta
@import owner.backflow.service.HandoffRecord
@param PageMeta page
@param HandoffRecord handoff
@param String shareUrl
@param String pdfPath = ""
@param String submissionGuidance = ""
@param String fullRuleTrackedPath = ""
@param String officialProgramTrackedPath = ""

@template.layouts.document(page = page, content = @`
    <section class="document-shell">
        <div class="document-toolbar" data-print-hide="true">
            @if (pdfPath != null && !pdfPath.isBlank())
                <a class="button button--primary" href="${pdfPath}">Download PDF</a>
            @endif
        </div>
        @template.components.handoffBriefSheet(
            handoff = handoff,
            submissionGuidance = submissionGuidance,
            fullRuleTrackedPath = fullRuleTrackedPath,
            officialProgramTrackedPath = officialProgramTrackedPath
        )
    </section>
`)
