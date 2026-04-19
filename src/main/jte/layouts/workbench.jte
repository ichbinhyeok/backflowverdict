@import owner.backflow.web.PageMeta
@import owner.backflow.config.SiteRuntimeConfig
@import gg.jte.Content
@param PageMeta page
@param Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${page.title()}</title>
    <meta name="description" content="${page.description()}">
    <meta name="robots" content="${page.robots()}">
    <link rel="canonical" href="${page.canonicalUrl()}">
    <meta property="og:site_name" content="${SiteRuntimeConfig.siteName()}">
    <meta property="og:title" content="${page.title()}">
    <meta property="og:description" content="${page.description()}">
    <meta property="og:url" content="${page.canonicalUrl()}">
    <meta property="og:type" content="${page.openGraphType()}">
    @if (page.socialImageUrl() != null)
        <meta property="og:image" content="${page.socialImageUrl()}">
    @endif
    <meta name="twitter:card" content="${page.twitterCard()}">
    <meta name="twitter:title" content="${page.title()}">
    <meta name="twitter:description" content="${page.description()}">
    @if (page.socialImageUrl() != null)
        <meta name="twitter:image" content="${page.socialImageUrl()}">
    @endif
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Inter:wght@400;500;600;700&family=Space+Grotesk:wght@500;700&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/app.css">
    @if (SiteRuntimeConfig.googleAnalyticsMeasurementId() != null && !SiteRuntimeConfig.googleAnalyticsMeasurementId().isBlank())
        <script async src="https://www.googletagmanager.com/gtag/js?id=${SiteRuntimeConfig.googleAnalyticsMeasurementId()}"></script>
        <script>
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${SiteRuntimeConfig.googleAnalyticsMeasurementId()}');
        </script>
    @endif
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var handoffEventHost = document.querySelector('[data-handoff-event-endpoint]');
            var handoffEventEndpoint = handoffEventHost ? handoffEventHost.getAttribute('data-handoff-event-endpoint') : '';

            function pulseButton(button, activeLabel, restoreDelay) {
                if (!button) {
                    return;
                }
                var originalLabel = button.dataset.originalLabel || button.textContent;
                button.dataset.originalLabel = originalLabel;
                button.textContent = activeLabel;
                window.setTimeout(function () {
                    button.textContent = originalLabel;
                }, restoreDelay || 1200);
            }

            async function sendHandoffEvent(eventType) {
                if (!handoffEventEndpoint || !eventType) {
                    return false;
                }
                try {
                    var body = new URLSearchParams();
                    body.set('eventType', eventType);
                    var response = await window.fetch(handoffEventEndpoint, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                        },
                        body: body.toString()
                    });
                    return response.ok;
                } catch (error) {
                    return false;
                }
            }

            document.addEventListener('click', async function (event) {
                var markButton = event.target.closest('[data-handoff-mark-sent]');
                if (markButton) {
                    event.preventDefault();
                    var marked = await sendHandoffEvent(markButton.getAttribute('data-handoff-mark-sent'));
                    pulseButton(markButton, marked ? 'Confirmed' : 'Confirm failed', marked ? 1200 : 1500);
                    return;
                }

                var feedbackButton = event.target.closest('[data-handoff-feedback-event]');
                if (feedbackButton) {
                    event.preventDefault();
                    var feedbackSaved = await sendHandoffEvent(feedbackButton.getAttribute('data-handoff-feedback-event'));
                    pulseButton(feedbackButton, feedbackSaved ? 'Saved' : 'Save failed', feedbackSaved ? 1200 : 1500);
                    var feedbackStatusTargetId = feedbackButton.getAttribute('data-handoff-feedback-status-target');
                    if (feedbackStatusTargetId) {
                        var feedbackStatus = document.getElementById(feedbackStatusTargetId);
                        if (feedbackStatus) {
                            feedbackStatus.textContent = feedbackSaved
                                    ? (feedbackButton.getAttribute('data-handoff-feedback-response') || 'Feedback saved.')
                                    : 'Could not save feedback right now.';
                        }
                    }
                    return;
                }

                var button = event.target.closest('[data-copy-target]');
                if (!button) {
                    return;
                }

                var targetId = button.getAttribute('data-copy-target');
                if (!targetId) {
                    return;
                }

                var target = document.getElementById(targetId);
                if (!target) {
                    return;
                }

                var text = '';
                if (typeof target.value === 'string') {
                    text = target.value;
                } else {
                    text = target.textContent || '';
                }

                if (!text) {
                    return;
                }

                try {
                    if (navigator.clipboard && navigator.clipboard.writeText) {
                        await navigator.clipboard.writeText(text);
                    } else {
                        target.focus();
                        if (typeof target.select === 'function') {
                            target.select();
                        }
                        document.execCommand('copy');
                    }
                    pulseButton(button, 'Copied', 1200);
                    await sendHandoffEvent(button.getAttribute('data-handoff-event-type'));
                } catch (error) {
                    pulseButton(button, 'Copy failed', 1500);
                }
            });

            var form = document.querySelector('[data-handoff-builder-form]');
            if (!form || !window.localStorage) {
                return;
            }

            var legacyDefaultsKey = 'backflowpath.handoff.vendor-defaults.v1';
            var defaultsKeyPrefix = 'backflowpath.handoff.vendor-defaults.v2.';
            var assemblyPresetKeyPrefix = 'backflowpath.handoff.assembly-presets.v2.';
            var lastJobKeyPrefix = 'backflowpath.handoff.last-job.v5.';
            var assemblyPresetLimit = 5;
            var vendorProfileFields = ['vendorCompanyName', 'vendorContactName', 'vendorPhone', 'vendorEmail', 'testerLicenseNumber'];
            var assemblyPresetFields = ['propertyType', 'propertyLabel', 'accountIdentifier', 'siteAddress', 'deviceLocation', 'assemblyMakeModel', 'assemblySize', 'assemblyType', 'assemblySerial'];
            var lastBriefFields = ['propertyLabel', 'accountIdentifier', 'siteAddress', 'resultStatus', 'submissionStatus', 'dueDate', 'noticeSummary', 'failedReason'];
            var lastOfficeRecordFields = ['propertyType', 'testDate', 'submissionDate', 'deviceLocation', 'customerFirstName', 'assemblyMakeModel', 'assemblyType', 'assemblySize', 'assemblySerial', 'submissionReference', 'permitOrReportNumber', 'checkValveOneReading', 'checkValveTwoReading', 'openingPointReading', 'repairSummary', 'vendorContactName', 'testerLicenseNumber', 'technicianName', 'gaugeId', 'calibrationReference', 'testReadingSummary', 'internalNote'];
            var lastJobFields = lastBriefFields.concat(lastOfficeRecordFields);
            var utilityField = form.elements.namedItem('utilityId');
            var note = document.querySelector('[data-handoff-storage-state]');
            var saveDefaultsButton = document.querySelector('[data-handoff-save-defaults]');
            var applyDefaultsButton = document.querySelector('[data-handoff-apply-defaults]');
            var loadLastButton = document.querySelector('[data-handoff-load-last]');
            var saveAssemblyButton = document.querySelector('[data-handoff-save-assembly]');
            var clearAssembliesButton = document.querySelector('[data-handoff-clear-assemblies]');
            var vendorProfileHost = document.querySelector('[data-handoff-vendor-profile]');
            var assemblyPresetHost = document.querySelector('[data-handoff-assembly-presets]');
            var officeRecordDisclosure = document.querySelector('[data-handoff-office-record]');
            var sendPreviewHosts = document.querySelectorAll('[data-handoff-send-preview], [data-handoff-send-preview-inline]');
            var issueTypeField = form.elements.namedItem('issueType');
            var resultStatusField = form.elements.namedItem('resultStatus');
            var dueDateField = form.elements.namedItem('dueDate');
            var dueDateLabel = document.querySelector('[data-handoff-due-date-label]');
            var noticeSummaryField = form.elements.namedItem('noticeSummary');
            var failedReasonField = document.querySelector('[data-handoff-failed-reason-input]');
            var failedReasonLabel = document.querySelector('[data-handoff-failed-reason-label]');
            var failedReasonHelp = document.querySelector('[data-handoff-failed-reason-help]');
            var failedReasonWrapper = document.querySelector('[data-handoff-failed-reason-field]');
            var intakeTextField = document.querySelector('[data-handoff-intake-text]');
            var intakeApplyButton = document.querySelector('[data-handoff-intake-apply]');
            var intakeToNoteButton = document.querySelector('[data-handoff-intake-to-note]');
            var intakeToFailedReasonButton = document.querySelector('[data-handoff-intake-to-failed-reason]');
            var intakeSampleAnnualButton = document.querySelector('[data-handoff-intake-sample-annual]');
            var intakeSampleFailedButton = document.querySelector('[data-handoff-intake-sample-failed]');

            function setNote(message, tone) {
                if (!note) {
                    return;
                }
                note.textContent = message;
                note.dataset.tone = tone || 'neutral';
            }

            function setFieldValue(fieldName, value) {
                var field = form.elements.namedItem(fieldName);
                if (!field) {
                    return;
                }
                field.value = value || '';
            }

            function defaultNoteMessage() {
                if (!selectedUtilityId()) {
                    return 'Fast path: choose the utility, add a site anchor and sender line, then generate the brief.';
                }
                return 'Fast path: lock the site, next date, and sender line, then generate the brief.';
            }

            function restoreDefaultNote() {
                setNote(defaultNoteMessage(), 'neutral');
            }

            function selectedUtilityId() {
                if (!utilityField || typeof utilityField.value !== 'string') {
                    return '';
                }
                return utilityField.value.trim();
            }

            function selectedUtilityLabel() {
                if (!utilityField) {
                    return 'this utility';
                }
                if (utilityField.dataset && utilityField.dataset.selectedLabel) {
                    return utilityField.dataset.selectedLabel;
                }
                if (!utilityField.options || utilityField.selectedIndex < 0) {
                    return 'this utility';
                }
                var option = utilityField.options[utilityField.selectedIndex];
                var label = option && option.textContent ? option.textContent.trim() : '';
                return label || 'this utility';
            }

            function selectedFieldLabel(fieldName, fallback) {
                var field = form.elements.namedItem(fieldName);
                if (!field || !field.value) {
                    return fallback || '';
                }
                if (field.dataset && field.dataset.selectedLabel) {
                    return field.dataset.selectedLabel;
                }
                if (!field.options || field.selectedIndex < 0) {
                    return fallback || '';
                }
                var option = field.options[field.selectedIndex];
                var label = option && option.textContent ? option.textContent.trim() : '';
                return label || fallback || '';
            }

            function selectedIssueTypeCode() {
                var field = form.elements.namedItem('issueType');
                if (!field || typeof field.value !== 'string') {
                    return '';
                }
                return field.value.trim();
            }

            function loadDemoScenario(scenario) {
                if (!scenario) {
                    return;
                }
                if (utilityField && utilityField.tagName === 'SELECT') {
                    utilityField.value = scenario.utilityId || '';
                }
                if (issueTypeField && issueTypeField.tagName === 'SELECT') {
                    issueTypeField.value = scenario.issueType || '';
                }
                if (resultStatusField) {
                    resultStatusField.value = scenario.resultStatus || '';
                }
                setFieldValue('submissionStatus', scenario.submissionStatus || '');
                setFieldValue('propertyLabel', scenario.propertyLabel || '');
                setFieldValue('vendorCompanyName', scenario.vendorCompanyName || '');
                setFieldValue('vendorPhone', scenario.vendorPhone || '');
                setFieldValue('vendorContactName', scenario.vendorContactName || '');
                if (intakeTextField) {
                    intakeTextField.value = scenario.intakeText || '';
                }
                applyIntakeText();
                if (scenario.issueType === 'failed-test-repair') {
                    var suggestedReason = suggestedFailedReason(scenario.intakeText || '');
                    if (suggestedReason) {
                        failedReasonField.value = suggestedReason;
                    }
                } else {
                    var suggestedNote = suggestedNoticeSummary(scenario.intakeText || '');
                    if (suggestedNote) {
                        noticeSummaryField.value = suggestedNote;
                    }
                }
                updateOutcomeRequirements();
                renderSendPreview();
                setNote(scenario.loadedMessage || 'Demo loaded. Review the sample, then generate the customer brief.', 'success');
            }

            function normalizedBlob(text) {
                return String(text || '')
                        .replace(/\r\n/g, '\n')
                        .replace(/\r/g, '\n')
                        .replace(/[ \t]+/g, ' ')
                        .replace(/\n{3,}/g, '\n\n')
                        .trim();
            }

            function clipText(text, maxLength) {
                var normalized = normalizedBlob(text);
                if (!normalized) {
                    return '';
                }
                if (normalized.length <= maxLength) {
                    return normalized;
                }
                return normalized.slice(0, Math.max(0, maxLength - 3)).trim() + '...';
            }

            function escapeRegExp(text) {
                return String(text || '').replace(/[|\\{}()[\]^$+*?.]/g, '\\$&');
            }

            function cleanCustomerFacingSuggestion(sourceText, suggestion) {
                var cleaned = normalizedBlob(suggestion);
                if (!cleaned) {
                    return '';
                }

                var account = extractAccountFromText(sourceText);
                var address = extractAddressFromText(sourceText);
                if (account) {
                    cleaned = cleaned.replace(
                            new RegExp('\\b(?:for\\s+)?(?:account|acct|customer|site|reference)\\s*(?:number|no\\.?|#|id)?\\s*[:#-]?\\s*' + escapeRegExp(account) + '\\b', 'ig'),
                            ''
                    );
                }
                if (address) {
                    cleaned = cleaned.replace(new RegExp('\\bat\\s+' + escapeRegExp(address), 'ig'), '');
                    cleaned = cleaned.replace(new RegExp(escapeRegExp(address), 'ig'), '');
                }

                cleaned = cleaned
                        .replace(/\s{2,}/g, ' ')
                        .replace(/\s+([,.;:!?])/g, '$1')
                        .replace(/^[,.;:\s-]+|[,.;:\s-]+$/g, '')
                        .trim();
                return cleaned;
            }

            function setSuggestedFieldValue(field, text, fieldLabel) {
                if (!field || !text) {
                    return false;
                }
                var current = typeof field.value === 'string' ? field.value.trim() : '';
                if (current === text) {
                    setNote(fieldLabel + ' already matches the pasted suggestion.', 'neutral');
                    return false;
                }
                if (current) {
                    setNote(fieldLabel + ' already has text. Review it before replacing it.', 'warning');
                    return false;
                }
                field.value = text;
                return true;
            }

            function intakeSegments(text) {
                var normalized = normalizedBlob(text);
                if (!normalized) {
                    return [];
                }

                var segments = [];
                normalized.split('\n').forEach(function (line) {
                    var trimmedLine = line.trim();
                    if (!trimmedLine) {
                        return;
                    }
                    var parts = trimmedLine.match(/[^.!?]+[.!?]?/g) || [trimmedLine];
                    parts.forEach(function (part) {
                        var segment = String(part || '').replace(/\s+/g, ' ').trim();
                        if (segment) {
                            segments.push(segment);
                        }
                    });
                });
                return segments;
            }

            function isIntakeMetadata(segment) {
                if (!segment || segment.length < 12) {
                    return true;
                }
                if (/^(subject|from|to|cc|sent|date|phone|email)\s*:/i.test(segment)) {
                    return true;
                }
                var lowerSegment = segment.toLowerCase();
                if (lowerSegment.indexOf('http://') >= 0 || lowerSegment.indexOf('https://') >= 0 || segment.indexOf(String.fromCharCode(64)) >= 0) {
                    return true;
                }
                if (/^(?:account|acct|customer|site|reference)\b/i.test(segment)) {
                    return true;
                }
                if (/^\d{2,}\s+[A-Za-z0-9.'#\- ]+\b(?:street|st|road|rd|avenue|ave|drive|dr|boulevard|blvd|lane|ln|way|parkway|pkwy|court|ct|circle|cir|trail|trl)\b/i.test(segment)) {
                    return true;
                }
                if (/^(?:due(?: date)?|deadline|action by|retest by|repair by)\b/i.test(segment)) {
                    return true;
                }
                return false;
            }

            function intakeSuggestion(text, keywordPattern, maxLength, requireKeywordMatch) {
                var segments = intakeSegments(text).filter(function (segment) {
                    return !isIntakeMetadata(segment);
                });
                if (!segments.length) {
                    return '';
                }
                var matching = segments.filter(function (segment) {
                    return keywordPattern.test(segment);
                });
                if (requireKeywordMatch && !matching.length) {
                    return '';
                }
                var picked = matching.length ? matching : [segments[0]];
                return clipText(picked.slice(0, 2).join(' '), maxLength);
            }

            function suggestedNoticeSummary(text) {
                var suggestion = intakeSuggestion(text, /\b(annual|notice|due|submission|filing|result|keep|handling|posted|required|service)\b/i, 220, false);
                var cleaned = cleanCustomerFacingSuggestion(text, suggestion);
                return cleaned ? clipText(cleaned, 220) : suggestion;
            }

            function suggestedFailedReason(text) {
                var suggestion = intakeSuggestion(text, /\b(fail|failed|failure|repair|retest|unable to test|could not test|blocked|did not pass)\b/i, 240, true);
                var cleaned = cleanCustomerFacingSuggestion(text, suggestion);
                return cleaned ? clipText(cleaned, 240) : suggestion;
            }

            function fillIfBlank(fieldName, value) {
                if (!value) {
                    return false;
                }
                var field = form.elements.namedItem(fieldName);
                if (!field || typeof field.value !== 'string' || field.value.trim() !== '') {
                    return false;
                }
                field.value = value;
                return true;
            }

            function toIsoDate(year, month, day) {
                var yyyy = String(year || '').padStart(4, '0');
                var mm = String(month || '').padStart(2, '0');
                var dd = String(day || '').padStart(2, '0');
                return yyyy + '-' + mm + '-' + dd;
            }

            function extractDateFromText(text) {
                var normalized = normalizedBlob(text);
                if (!normalized) {
                    return '';
                }

                var keywordMatch = normalized.match(/(?:due(?: date)?|deadline|action by|retest by|return visit by|repair by)\s*[:\-]?\s*([A-Za-z0-9,\/\- ]{6,24})/i);
                if (keywordMatch && keywordMatch[1]) {
                    var keywordDate = extractDateFromText(keywordMatch[1]);
                    if (keywordDate) {
                        return keywordDate;
                    }
                }

                var isoMatch = normalized.match(/\b(20\d{2})-(0?[1-9]|1[0-2])-(0?[1-9]|[12]\d|3[01])\b/);
                if (isoMatch) {
                    return toIsoDate(isoMatch[1], isoMatch[2], isoMatch[3]);
                }

                var numericMatch = normalized.match(/\b(0?[1-9]|1[0-2])[\/\-](0?[1-9]|[12]\d|3[01])[\/\-](20\d{2})\b/);
                if (numericMatch) {
                    return toIsoDate(numericMatch[3], numericMatch[1], numericMatch[2]);
                }

                var monthNames = {
                    january: 1, jan: 1,
                    february: 2, feb: 2,
                    march: 3, mar: 3,
                    april: 4, apr: 4,
                    may: 5,
                    june: 6, jun: 6,
                    july: 7, jul: 7,
                    august: 8, aug: 8,
                    september: 9, sep: 9, sept: 9,
                    october: 10, oct: 10,
                    november: 11, nov: 11,
                    december: 12, dec: 12
                };
                var longMatch = normalized.match(/\b(january|jan|february|feb|march|mar|april|apr|may|june|jun|july|jul|august|aug|september|sep|sept|october|oct|november|nov|december|dec)\s+(\d{1,2}),?\s+(20\d{2})\b/i);
                if (longMatch) {
                    var month = monthNames[String(longMatch[1] || '').toLowerCase()];
                    if (month) {
                        return toIsoDate(longMatch[3], month, longMatch[2]);
                    }
                }

                return '';
            }

            function extractAccountFromText(text) {
                var normalized = normalizedBlob(text);
                if (!normalized) {
                    return '';
                }
                var match = normalized.match(/(?:account|acct|customer|site|reference)\s*(?:number|no\.?|#|id)?\s*[:#-]?\s*([A-Z0-9-]{4,})/i);
                return match && match[1] ? match[1].trim() : '';
            }

            function extractAddressFromText(text) {
                var normalized = normalizedBlob(text);
                if (!normalized) {
                    return '';
                }
                var addressPattern = /\b\d{2,}\s+(?:[A-Za-z#][A-Za-z0-9.'#\-]*\s+){0,4}(?:street|st|road|rd|avenue|ave|drive|dr|boulevard|blvd|lane|ln|way|parkway|pkwy|court|ct|circle|cir|trail|trl)\b[^,\n]*(?:,\s*[A-Za-z .'-]+,\s*[A-Z]{2})?/i;
                var lines = normalized.split('\n');
                for (var index = 0; index < lines.length; index++) {
                    var line = lines[index].trim();
                    var lineMatch = line.match(addressPattern);
                    if (lineMatch) {
                        return lineMatch[0].trim();
                    }
                }
                var inlineMatch = normalized.match(addressPattern);
                return inlineMatch ? inlineMatch[0].trim() : '';
            }

            function applyIntakeText() {
                if (!intakeTextField) {
                    return false;
                }
                var raw = intakeTextField.value;
                var normalized = normalizedBlob(raw);
                if (!normalized) {
                    setNote('Paste the annual notice or failed result text first.', 'warning');
                    return false;
                }

                var applied = [];
                if (fillIfBlank('dueDate', extractDateFromText(normalized))) {
                    applied.push('due date');
                }
                if (fillIfBlank('accountIdentifier', extractAccountFromText(normalized))) {
                    applied.push('account or site ID');
                }
                if (fillIfBlank('siteAddress', extractAddressFromText(normalized))) {
                    applied.push('service address');
                }

                if (issueTypeField && !issueTypeField.value && /\b(failed?|retest|repair)\b/i.test(normalized)) {
                    issueTypeField.value = 'failed-test-repair';
                    applied.push('job type');
                }

                if (resultStatusField && !resultStatusField.value) {
                    if (/\b(unable to test|could not test|test could not be completed)\b/i.test(normalized)) {
                        resultStatusField.value = 'unable-to-test';
                        applied.push('result status');
                    } else if (/\b(failed|failure|did not pass)\b/i.test(normalized)) {
                        resultStatusField.value = 'fail';
                        applied.push('result status');
                    }
                }

                updateOutcomeRequirements();
                renderSendPreview();
                if (!applied.length) {
                    setNote('The pasted text did not map cleanly. Review the customer-facing copy manually before sending.', 'neutral');
                    return false;
                }
                setNote('Quick intake filled ' + applied.join(', ') + '. Review any customer-facing copy before sending.', 'success');
                return true;
            }

            function defaultsKeyForUtility() {
                var utilityId = selectedUtilityId();
                if (!utilityId) {
                    return '';
                }
                return defaultsKeyPrefix + utilityId;
            }

            function assemblyPresetKeyForUtility() {
                var utilityId = selectedUtilityId();
                if (!utilityId) {
                    return '';
                }
                return assemblyPresetKeyPrefix + utilityId;
            }

            function lastJobKeyForContext() {
                var utilityId = selectedUtilityId();
                var issueType = selectedIssueTypeCode();
                if (!utilityId || !issueType) {
                    return '';
                }
                return lastJobKeyPrefix + utilityId + '.' + issueType;
            }

            function parseStored(key) {
                if (!key) {
                    return null;
                }
                var raw = window.localStorage.getItem(key);
                if (!raw) {
                    return null;
                }
                try {
                    return JSON.parse(raw);
                } catch (error) {
                    return null;
                }
            }

            function collectFormValues() {
                var formData = new window.FormData(form);
                var values = {};
                formData.forEach(function (value, key) {
                    values[key] = typeof value === 'string' ? value : '';
                });
                return values;
            }

            function collectVendorDefaults() {
                var values = collectFormValues();
                var picked = {};
                vendorProfileFields.forEach(function (fieldName) {
                    picked[fieldName] = values[fieldName] || '';
                });
                return picked;
            }

            function collectScopedFields(fieldNames) {
                var values = collectFormValues();
                var picked = {};
                fieldNames.forEach(function (fieldName) {
                    picked[fieldName] = values[fieldName] || '';
                });
                return picked;
            }

            function collectAssemblyPreset() {
                var values = collectFormValues();
                var picked = {};
                assemblyPresetFields.forEach(function (fieldName) {
                    picked[fieldName] = values[fieldName] || '';
                });
                picked.savedAt = new Date().toISOString();
                return picked;
            }

            function defaultsForSelectedUtility(useLegacyFallback) {
                var defaults = parseStored(defaultsKeyForUtility());
                if (!defaults && useLegacyFallback) {
                    defaults = parseStored(legacyDefaultsKey);
                }
                return sanitizeVendorDefaults(defaults);
            }

            function sanitizeVendorDefaults(defaults) {
                if (!defaults) {
                    return null;
                }
                var picked = {};
                vendorProfileFields.forEach(function (fieldName) {
                    if (defaults[fieldName] != null) {
                        picked[fieldName] = String(defaults[fieldName]);
                    }
                });
                return picked;
            }

            function recentAssembliesForSelectedUtility() {
                var stored = parseStored(assemblyPresetKeyForUtility());
                return Array.isArray(stored) ? stored : [];
            }

            function pickStoredValues(values, fieldNames) {
                if (!values) {
                    return null;
                }
                var picked = {};
                fieldNames.forEach(function (fieldName) {
                    picked[fieldName] = values[fieldName] || '';
                });
                return picked;
            }

            function applyValues(values, onlyIfBlank) {
                if (!values) {
                    return false;
                }

                var changed = false;
                Object.keys(values).forEach(function (key) {
                    var field = form.elements.namedItem(key);
                    if (!field || typeof field.value !== 'string') {
                        return;
                    }
                    var incoming = values[key] == null ? '' : String(values[key]);
                    if (onlyIfBlank && field.value && field.value.trim() !== '') {
                        return;
                    }
                    field.value = incoming;
                    changed = true;
                });

                if (officeRecordDisclosure) {
                    var needsDisclosure = lastOfficeRecordFields
                            .some(function (fieldName) {
                                var field = form.elements.namedItem(fieldName);
                                return field && typeof field.value === 'string' && field.value.trim() !== '';
                            });
                    if (needsDisclosure) {
                        officeRecordDisclosure.open = true;
                    }
                }
                if (changed) {
                    renderSendPreview();
                }
                return changed;
            }

            function hasValues(values, fields) {
                return fields.some(function (fieldName) {
                    return values[fieldName] && String(values[fieldName]).trim() !== '';
                });
            }

            function assemblyIdentity(values) {
                var parts = [
                    values.propertyLabel || '',
                    values.siteAddress || '',
                    values.deviceLocation || '',
                    values.assemblyMakeModel || '',
                    values.assemblySize || '',
                    values.assemblyType || '',
                    values.assemblySerial || ''
                ].map(function (part) {
                    return String(part || '').trim().toLowerCase();
                }).filter(Boolean);
                return parts.join('|');
            }

            function saveRecentAssemblyPreset(showMessage) {
                var key = assemblyPresetKeyForUtility();
                if (!key) {
                    if (showMessage) {
                        setNote('Choose the utility first so assembly presets stay tied to the right workflow.', 'warning');
                    }
                    return false;
                }

                var preset = collectAssemblyPreset();
                if (!hasValues(preset, assemblyPresetFields)) {
                    if (showMessage) {
                        setNote('Add at least a site or assembly record before saving an assembly preset.', 'warning');
                    }
                    return false;
                }

                var identity = assemblyIdentity(preset);
                if (!identity) {
                    if (showMessage) {
                        setNote('Assembly presets need at least one site or assembly anchor.', 'warning');
                    }
                    return false;
                }

                var presets = recentAssembliesForSelectedUtility()
                        .filter(function (item) {
                            return assemblyIdentity(item) !== identity;
                        });
                presets.unshift(preset);
                window.localStorage.setItem(key, JSON.stringify(presets.slice(0, assemblyPresetLimit)));
                renderAssemblyPresets();
                if (showMessage) {
                    setNote('Assembly preset saved for ' + selectedUtilityLabel() + ' in this browser.', 'success');
                }
                return true;
            }

            function appendTextLine(parent, value) {
                if (!value) {
                    return;
                }
                var line = document.createElement('span');
                line.textContent = value;
                parent.appendChild(line);
            }

            function appendPreviewBadge(parent, value, state) {
                var badge = document.createElement('span');
                badge.className = 'handoff-preview__badge';
                badge.textContent = value;
                if (state) {
                    badge.dataset.state = state;
                }
                parent.appendChild(badge);
            }

            function appendPreviewMeta(parent, label, value, isMissing) {
                var card = document.createElement('div');
                card.className = 'handoff-preview__meta-card';
                if (isMissing) {
                    card.dataset.state = 'missing';
                }

                var labelNode = document.createElement('span');
                labelNode.textContent = label;
                card.appendChild(labelNode);

                var valueNode = document.createElement('strong');
                valueNode.textContent = value;
                card.appendChild(valueNode);

                parent.appendChild(card);
            }

            function renderSendPreview() {
                if (!sendPreviewHosts || sendPreviewHosts.length === 0) {
                    return;
                }

                var values = collectFormValues();
                var requiresFailureReason = values.resultStatus === 'fail' || values.resultStatus === 'unable-to-test';
                var siteName = values.propertyLabel || values.siteAddress || 'Add the site or facility';
                var utilityName = selectedFieldLabel('utilityId', 'Choose utility');
                var issueName = selectedFieldLabel('issueType', '');
                var resultName = selectedFieldLabel('resultStatus', 'Choose result');
                var filingName = selectedFieldLabel('submissionStatus', 'Choose filing state');
                var customerReason = values.failedReason || values.noticeSummary || (requiresFailureReason
                        ? 'Explain why the device failed or why the return visit is still open.'
                        : 'Explain why the annual notice is still open and what the next step means.');
                var customerContext = [values.accountIdentifier, values.siteAddress]
                        .filter(function (value) {
                            return value && value.trim() !== '';
                        })
                        .join(' | ');
                var vendorContact = values.vendorPhone || values.vendorEmail || '';
                var vendorLine = [values.vendorCompanyName, vendorContact]
                        .filter(function (value) {
                            return value && value.trim() !== '';
                        })
                        .join(' | ');
                var nextAction = values.dueDate || 'Set the repair or due date';
                var nextActionCopy = values.dueDate
                        ? 'Action by ' + values.dueDate + '. Keep this on the customer copy so the next step is obvious.'
                        : 'Set the due or retest date before sending the first brief.';

                Array.prototype.forEach.call(sendPreviewHosts, function (host) {
                    host.replaceChildren();

                    var sheet = document.createElement('div');
                    sheet.className = 'handoff-preview__sheet';

                    var hero = document.createElement('div');
                    hero.className = 'handoff-preview__hero';

                    var heroTop = document.createElement('div');
                    heroTop.className = 'handoff-preview__hero-top';

                    var eyebrow = document.createElement('span');
                    eyebrow.className = 'handoff-preview__eyebrow';
                    eyebrow.textContent = 'Customer brief';
                    heroTop.appendChild(eyebrow);

                    var proof = document.createElement('span');
                    proof.className = 'handoff-preview__proof';
                    proof.textContent = resultName;
                    heroTop.appendChild(proof);
                    hero.appendChild(heroTop);

                    var title = document.createElement('h3');
                    title.className = 'handoff-preview__title';
                    title.textContent = siteName;
                    hero.appendChild(title);

                    var summary = document.createElement('p');
                    summary.className = 'handoff-preview__summary';
                    summary.textContent = [utilityName, issueName].filter(function (value) {
                        return value && value.trim() !== '';
                    }).join(' | ') || 'Choose the utility and issue type first.';
                    hero.appendChild(summary);
                    sheet.appendChild(hero);

                    var callout = document.createElement('div');
                    callout.className = 'handoff-preview__callout';

                    var calloutLabel = document.createElement('span');
                    calloutLabel.className = 'handoff-preview__callout-label';
                    calloutLabel.textContent = 'Why the customer got this';
                    callout.appendChild(calloutLabel);

                    var calloutText = document.createElement('p');
                    calloutText.textContent = customerReason;
                    callout.appendChild(calloutText);
                    sheet.appendChild(callout);

                    var meta = document.createElement('div');
                    meta.className = 'handoff-preview__meta';
                    appendPreviewMeta(meta, 'Site anchor', customerContext || 'Add the site address or account ID', !customerContext);
                    appendPreviewMeta(meta, 'Action by', nextAction, !values.dueDate);
                    appendPreviewMeta(meta, 'Sender line', vendorLine || 'Add vendor company and phone or email', !vendorLine);
                    appendPreviewMeta(meta, 'Filing status', filingName, !values.submissionStatus);
                    if (requiresFailureReason) {
                        appendPreviewMeta(
                                meta,
                                values.resultStatus === 'fail' ? 'Failure reason' : 'Return-visit reason',
                                values.failedReason || 'Add the reason the customer will receive',
                                !values.failedReason
                        );
                    }
                    sheet.appendChild(meta);

                    var nextCard = document.createElement('div');
                    nextCard.className = 'handoff-preview__next';

                    var nextLabel = document.createElement('span');
                    nextLabel.className = 'handoff-preview__callout-label';
                    nextLabel.textContent = 'What happens next';
                    nextCard.appendChild(nextLabel);

                    var nextText = document.createElement('strong');
                    nextText.textContent = nextActionCopy;
                    nextCard.appendChild(nextText);
                    sheet.appendChild(nextCard);

                    var noteLine = document.createElement('p');
                    noteLine.className = 'handoff-preview__note';
                    noteLine.textContent = 'Send this first. Keep the office record behind it.';
                    sheet.appendChild(noteLine);
                    host.appendChild(sheet);
                });
            }

            function createEmptyMemory(message) {
                var empty = document.createElement('p');
                empty.className = 'workbench-memory-empty';
                empty.textContent = message;
                return empty;
            }

            function presetTitle(preset, index) {
                return preset.propertyLabel
                        || preset.siteAddress
                        || preset.assemblySerial
                        || preset.assemblyMakeModel
                        || ('Recent assembly ' + (index + 1));
            }
            function renderAssemblyPresets() {
                if (!assemblyPresetHost) {
                    return;
                }

                assemblyPresetHost.replaceChildren();

                if (!selectedUtilityId()) {
                    assemblyPresetHost.appendChild(createEmptyMemory('Choose a utility to load recent assemblies for that workflow.'));
                    if (clearAssembliesButton) {
                        clearAssembliesButton.hidden = true;
                    }
                    return;
                }

                var presets = recentAssembliesForSelectedUtility();
                if (clearAssembliesButton) {
                    clearAssembliesButton.hidden = presets.length === 0;
                }

                if (!presets.length) {
                    assemblyPresetHost.appendChild(createEmptyMemory('No assembly preset saved for this utility yet. Save one from the current form when the site and assembly record are set.'));
                    return;
                }

                var list = document.createElement('div');
                list.className = 'workbench-memory-list';

                presets.forEach(function (preset, index) {
                    var card = document.createElement('div');
                    card.className = 'workbench-memory-card';

                    var title = document.createElement('strong');
                    title.textContent = presetTitle(preset, index);
                    card.appendChild(title);

                    presetMeta(preset).forEach(function (line) {
                        appendTextLine(card, line);
                    });

                    var actions = document.createElement('div');
                    actions.className = 'workbench-memory-actions';

                    var button = document.createElement('button');
                    button.type = 'button';
                    button.className = 'button button--secondary button--compact';
                    button.textContent = 'Use this assembly';
                    button.addEventListener('click', function () {
                        applyValues(preset, false);
                        setNote('Assembly preset applied for ' + selectedUtilityLabel() + '.', 'success');
                    });
                    actions.appendChild(button);

                    card.appendChild(actions);
                    list.appendChild(card);
                });

                assemblyPresetHost.appendChild(list);
            }

            function syncDefaultsForSelectedUtility(onlyIfBlank, useLegacyFallback, quiet) {
                if (!selectedUtilityId()) {
                    if (!quiet) {
                        setNote('Choose the utility first so saved browser memory stays scoped to the right workflow.', 'warning');
                    }
                    return;
                }

                var storedDefaults = defaultsForSelectedUtility(useLegacyFallback);
                if (!storedDefaults) {
                    if (!quiet) {
                        setNote('No saved vendor profile for ' + selectedUtilityLabel() + ' yet.', 'warning');
                    }
                    return;
                }

                applyValues(storedDefaults, onlyIfBlank);
                if (!quiet) {
                    setNote('Saved vendor profile applied for ' + selectedUtilityLabel() + '. Review the sender line, then generate the brief.', 'success');
                }
            }

            function presetMeta(preset) {
                return [
                    [preset.assemblyMakeModel, preset.assemblySize, preset.assemblyType].filter(Boolean).join(' | '),
                    preset.assemblySerial || '',
                    preset.deviceLocation || '',
                    preset.accountIdentifier || ''
                ].filter(function (value) {
                    return value && value.trim() !== '';
                });
            }

            function renderVendorProfile() {
                if (!vendorProfileHost) {
                    return;
                }

                vendorProfileHost.replaceChildren();

                if (!selectedUtilityId()) {
                    vendorProfileHost.appendChild(createEmptyMemory('Choose a utility to see saved vendor info for this workflow.'));
                    return;
                }

                var defaults = defaultsForSelectedUtility(false);
                if (!defaults || !hasValues(defaults, vendorProfileFields)) {
                    vendorProfileHost.appendChild(createEmptyMemory('No vendor profile saved for this utility yet.'));
                    return;
                }

                var card = document.createElement('div');
                card.className = 'workbench-memory-card';

                var title = document.createElement('strong');
                title.textContent = defaults.vendorCompanyName || selectedUtilityLabel();
                card.appendChild(title);

                appendTextLine(card, [defaults.vendorContactName, defaults.vendorPhone].filter(Boolean).join(' | '));
                appendTextLine(card, [defaults.vendorEmail, defaults.testerLicenseNumber].filter(Boolean).join(' | '));

                vendorProfileHost.appendChild(card);
            }

            function updateOutcomeRequirements() {
                var resultCode = resultStatusField && typeof resultStatusField.value === 'string'
                        ? resultStatusField.value.trim()
                        : '';
                var requiresFailureReason = resultCode === 'fail' || resultCode === 'unable-to-test';

                if (dueDateLabel) {
                    if (resultCode === 'fail') {
                        dueDateLabel.textContent = 'Repair or retest by';
                    } else if (resultCode === 'unable-to-test') {
                        dueDateLabel.textContent = 'Return visit by';
                    } else {
                        dueDateLabel.textContent = 'Next due or filing by';
                    }
                }

                if (dueDateField) {
                    dueDateField.required = true;
                }

                if (failedReasonField) {
                    failedReasonField.required = requiresFailureReason;
                    if (resultCode === 'fail') {
                        failedReasonField.placeholder = 'Required for failed jobs. Explain what failed and what still has to be repaired or retested.';
                    } else if (resultCode === 'unable-to-test') {
                        failedReasonField.placeholder = 'Required when the test could not be completed. Explain what blocked the test and what must change before the return visit.';
                    } else {
                        failedReasonField.placeholder = 'Optional for passed jobs. Use this only when the customer needs a short result note.';
                    }
                }

                if (failedReasonLabel) {
                    if (resultCode === 'fail') {
                        failedReasonLabel.textContent = 'Failure reason shown to the customer';
                    } else if (resultCode === 'unable-to-test') {
                        failedReasonLabel.textContent = 'Why the test could not be completed';
                    } else {
                        failedReasonLabel.textContent = 'Result note shown to the customer';
                    }
                }

                if (failedReasonHelp) {
                    if (resultCode === 'fail') {
                        failedReasonHelp.textContent = 'Required for failed jobs. This should explain what failed and what still has to happen next.';
                    } else if (resultCode === 'unable-to-test') {
                        failedReasonHelp.textContent = 'Required when the test could not be completed. This should explain the block before the return visit.';
                    } else {
                        failedReasonHelp.textContent = 'Optional for passed jobs. Leave blank unless the customer needs a short explanatory note.';
                    }
                }

                if (failedReasonWrapper) {
                    failedReasonWrapper.dataset.required = requiresFailureReason ? 'true' : 'false';
                }
            }

            restoreDefaultNote();
            if (selectedUtilityId()) {
                syncDefaultsForSelectedUtility(true, true, true);
            }
            renderVendorProfile();
            renderAssemblyPresets();
            updateOutcomeRequirements();
            renderSendPreview();

            if (utilityField) {
                utilityField.addEventListener('change', function () {
                    syncDefaultsForSelectedUtility(true, false, true);
                    renderVendorProfile();
                    renderAssemblyPresets();
                    renderSendPreview();
                    restoreDefaultNote();
                });
            }

            if (resultStatusField) {
                resultStatusField.addEventListener('change', function () {
                    updateOutcomeRequirements();
                    renderSendPreview();
                });
            }

            if (saveDefaultsButton) {
                saveDefaultsButton.addEventListener('click', function () {
                    var key = defaultsKeyForUtility();
                    if (!key) {
                        setNote('Choose the utility first so defaults are saved to the right workflow.', 'warning');
                        return;
                    }
                    window.localStorage.setItem(key, JSON.stringify(collectVendorDefaults()));
                    setNote('Vendor profile saved for ' + selectedUtilityLabel() + ' in this browser.', 'success');
                    renderVendorProfile();
                });
            }

            if (applyDefaultsButton) {
                applyDefaultsButton.addEventListener('click', function () {
                    var defaults = defaultsForSelectedUtility(true);
                    if (!defaults) {
                        setNote('No saved vendor profile for ' + selectedUtilityLabel() + ' yet. Save it once from this form.', 'warning');
                        return;
                    }
                    applyValues(defaults, false);
                    setNote('Saved vendor profile applied for ' + selectedUtilityLabel() + '. Review the sender line, then generate the brief.', 'success');
                });
            }

            if (loadLastButton) {
                loadLastButton.addEventListener('click', function () {
                    var key = lastJobKeyForContext();
                    if (!key) {
                        setNote('Choose the utility and the issue path first so the last job stays tied to the right workflow.', 'warning');
                        return;
                    }
                    var lastJob = parseStored(key);
                    if (!lastJob) {
                        setNote('No saved job is stored for this utility and issue path yet.', 'warning');
                        return;
                    }
                    applyValues(pickStoredValues(lastJob, lastJobFields), false);
                    renderVendorProfile();
                    renderAssemblyPresets();
                    setNote('Last job loaded. Review the brief, readings, and filing detail before generating again.', 'success');
                });
            }

             if (intakeApplyButton) {
                 intakeApplyButton.addEventListener('click', function () {
                     applyIntakeText();
                 });
             }

             if (intakeToNoteButton) {
                 intakeToNoteButton.addEventListener('click', function () {
                     var text = suggestedNoticeSummary(intakeTextField ? intakeTextField.value : '');
                     if (!text) {
                         setNote('Paste a notice first so BackflowPath can suggest a clean customer note.', 'warning');
                         return;
                     }
                     if (setSuggestedFieldValue(noticeSummaryField, text, 'Customer note')) {
                         renderSendPreview();
                         setNote('Suggested customer note added. Review it before sending.', 'success');
                     }
                 });
             }

             if (intakeToFailedReasonButton) {
                 intakeToFailedReasonButton.addEventListener('click', function () {
                     var text = suggestedFailedReason(intakeTextField ? intakeTextField.value : '');
                     if (!text) {
                         setNote('Paste the failed-result text first so BackflowPath can suggest a clean failed reason.', 'warning');
                         return;
                     }
                     if (resultStatusField && !resultStatusField.value) {
                         resultStatusField.value = 'fail';
                     }
                     if (issueTypeField && !issueTypeField.value) {
                         issueTypeField.value = 'failed-test-repair';
                     }
                     if (setSuggestedFieldValue(failedReasonField, text, 'Failed reason')) {
                         updateOutcomeRequirements();
                         renderSendPreview();
                         setNote('Suggested failed reason added. Review it before sending.', 'success');
                     }
                 });
             }

             if (intakeSampleAnnualButton) {
                 intakeSampleAnnualButton.addEventListener('click', function () {
                     loadDemoScenario({
                         utilityId: 'gptx-water',
                         issueType: 'general-testing',
                         resultStatus: 'pass',
                         submissionStatus: 'pending-submission',
                         propertyLabel: 'Main Street Retail Center',
                         vendorCompanyName: 'DFW Backflow Services',
                         vendorContactName: 'Jordan Lee',
                         vendorPhone: '972-555-0144',
                         intakeText: 'Annual backflow test due by 2026-05-01 for account ACC-2049 at 120 Main Street, Grand Prairie, TX. Grand Prairie Water Utilities still needs the passing annual result filed. We will send the city update after the office review.',
                         loadedMessage: 'Annual sample loaded. Review the quick demo, then generate the customer brief.'
                     });
                 });
             }

             if (intakeSampleFailedButton) {
                 intakeSampleFailedButton.addEventListener('click', function () {
                     loadDemoScenario({
                         utilityId: 'gptx-water',
                         issueType: 'failed-test-repair',
                         resultStatus: 'fail',
                         submissionStatus: 'pending-submission',
                         propertyLabel: 'Main Street Retail Center',
                         vendorCompanyName: 'DFW Backflow Services',
                         vendorContactName: 'Jordan Lee',
                         vendorPhone: '972-555-0144',
                         intakeText: 'Backflow assembly failed on 2026-04-20 for account ACC-2049 at 120 Main Street, Grand Prairie, TX. Repair by 2026-05-03. The relief valve and check readings did not hold, so repair and a passing retest are still required before the utility file can close.',
                         loadedMessage: 'Failed-test sample loaded. Review the brief and failed reason, then generate the customer brief.'
                     });
                 });
             }

            if (saveAssemblyButton) {
                saveAssemblyButton.addEventListener('click', function () {
                    saveRecentAssemblyPreset(true);
                });
            }

            if (clearAssembliesButton) {
                clearAssembliesButton.addEventListener('click', function () {
                    var key = assemblyPresetKeyForUtility();
                    if (!key) {
                        setNote('Choose the utility first before clearing assembly history.', 'warning');
                        return;
                    }
                    window.localStorage.removeItem(key);
                    renderAssemblyPresets();
                    setNote('Recent assembly history cleared for ' + selectedUtilityLabel() + '.', 'success');
                });
            }

            form.addEventListener('submit', function () {
                var lastJobKey = lastJobKeyForContext();
                if (lastJobKey) {
                    window.localStorage.setItem(lastJobKey, JSON.stringify(collectScopedFields(lastJobFields)));
                }
                var key = defaultsKeyForUtility();
                if (key) {
                    window.localStorage.setItem(key, JSON.stringify(collectVendorDefaults()));
                }
            });

            form.addEventListener('input', renderSendPreview);
            form.addEventListener('change', function () {
                updateOutcomeRequirements();
                renderSendPreview();
            });
        });
    </script>
</head>
<body class="workbench-body">
    <main class="workbench-main-shell">
        ${content}
    </main>
</body>
</html>


