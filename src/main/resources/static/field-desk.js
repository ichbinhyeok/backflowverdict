(function () {
    'use strict';
    var storeKey = 'bv-field-brief-v1';
    var emit = function (name, params) {
        if (typeof window.gtag === 'function') window.gtag('event', name, params);
    };
    var plate = document.querySelector('[data-reference-plate]');
    if (plate) {
        var points = {
            covers: ['01 /', 'Check-service covers', 'Cover shape is a clue, not a model ID. Read the complete nameplate before choosing parts.'],
            tests: ['02 /', 'Test connections', 'These are test access points. Do not open or adjust them to identify a repair kit.'],
            relief: ['03 /', 'Relief outlet', 'An RPZ can discharge here. Location alone does not identify a failed component; use the exact model and a qualified test.']
        };
        plate.querySelectorAll('[data-plate-point]').forEach(function (point) {
            point.addEventListener('click', function (event) {
                event.preventDefault();
                var content = points[point.dataset.platePoint];
                plate.querySelector('[data-plate-number]').textContent = content[0];
                plate.querySelector('[data-plate-title]').textContent = content[1];
                plate.querySelector('[data-plate-description]').textContent = content[2];
                plate.querySelectorAll('[data-plate-point]').forEach(function (other) { other.removeAttribute('aria-current'); });
                point.setAttribute('aria-current', 'true');
                emit('diagram_point_select', {point: point.dataset.platePoint, page_path: location.pathname});
            });
        });
    }
    var desk = document.querySelector('[data-field-desk]');
    if (desk) {
        var tabs = Array.from(desk.querySelectorAll('[data-desk-tab]'));
        var panels = desk.querySelectorAll('[data-desk-panel]');
        desk.querySelector('[data-desk-tabs]').hidden = false;
        var mode = desk.dataset.defaultMode;
        var setMode = function (next, track) {
            mode = next;
            tabs.forEach(function (tab) {
                var selected = tab.dataset.deskTab === mode;
                tab.setAttribute('aria-selected', String(selected));
                tab.tabIndex = selected ? 0 : -1;
            });
            panels.forEach(function (panel) { panel.hidden = panel.dataset.deskPanel !== mode; });
            if (track) emit('field_task_select', {task: mode, page_path: location.pathname});
        };
        tabs.forEach(function (tab, index) {
            tab.addEventListener('click', function () { setMode(tab.dataset.deskTab, true); });
            tab.addEventListener('keydown', function (event) {
                var next = event.key === 'ArrowRight' ? (index + 1) % tabs.length : event.key === 'ArrowLeft' ? (index + tabs.length - 1) % tabs.length : event.key === 'Home' ? 0 : event.key === 'End' ? tabs.length - 1 : -1;
                if (next < 0) return;
                event.preventDefault(); tabs[next].focus(); setMode(tabs[next].dataset.deskTab, true);
            });
        });
        setMode(mode, false);
        var lookup = document.querySelector('[data-plate-lookup]');
        if (lookup) lookup.addEventListener('click', function () {
            setMode('parts', true);
            var note = desk.querySelector('[data-observation-note]');
            note.textContent = 'Observed area: ' + document.querySelector('[data-plate-title]').textContent + '. Confirm the actual model before selecting a part; this observation does not diagnose a failure.';
            note.hidden = false;
        });
        var templates = Array.from(desk.querySelectorAll('[data-desk-kit]'));
        var model = desk.querySelector('[data-desk-model]');
        var size = desk.querySelector('[data-desk-size]');
        var scope = desk.querySelector('[data-desk-scope]');
        var matches = [];
        var unique = function (items, key) { return Array.from(new Set(items.map(function (item) { return item.dataset[key]; }))); };
        var options = function (select, prompt, values) {
            select.replaceChildren(new Option(prompt, ''));
            values.forEach(function (value) { select.add(new Option(value, value)); });
            select.disabled = values.length === 0;
        };
        var render = function () {
            matches = templates.filter(function (item) { return item.dataset.model === model.value && item.dataset.size === size.value && (!scope.value || item.dataset.scope === scope.value); });
            var target = desk.querySelector('[data-desk-matches]');
            target.replaceChildren();
            matches.forEach(function (item) { target.appendChild(item.content.cloneNode(true)); });
            desk.querySelector('[data-desk-empty]').hidden = !!model.value;
            desk.querySelector('[data-desk-results]').hidden = !matches.length;
            desk.querySelector('[data-desk-count]').textContent = matches.length + ' published part reference' + (matches.length === 1 ? '' : 's') + ' / ' + size.value;
            desk.querySelector('[data-desk-model-name]').textContent = model.options[model.selectedIndex].text;
        };
        model.addEventListener('change', function () {
            options(size, 'Choose nominal size', unique(templates.filter(function (item) { return item.dataset.model === model.value; }), 'size'));
            options(scope, 'All published scopes', []); render();
            var documents = desk.querySelector('[data-desk-documents]');
            documents.replaceChildren();
            var source = Array.from(desk.querySelectorAll('[data-model-documents]')).find(function (template) { return template.dataset.modelDocuments === model.value; });
            if (source) documents.appendChild(source.content.cloneNode(true));
            documents.hidden = !source;
            emit('part_model_select', {model_slug: model.value, source_page: desk.dataset.source});
        });
        size.addEventListener('change', function () {
            options(scope, 'All published scopes', unique(templates.filter(function (item) { return item.dataset.model === model.value && item.dataset.size === size.value; }), 'scope')); render();
            if (matches.length) emit('kit_match_result', {model_slug: model.value, size_band: size.value, match_count: matches.length, source_page: desk.dataset.source});
        });
        scope.addEventListener('change', function () {
            render(); emit('part_scope_select', {model_slug: model.value, size_band: size.value, repair_scope: scope.value || 'all', match_count: matches.length});
        });
        var brief = '';
        var briefReady = false;
        var briefForm = desk.querySelector('[data-brief-form]');
        var lines = [];
        var buildBrief = function () {
            var job = briefForm.elements.job.value;
            var system = briefForm.elements.system.value;
            var place = briefForm.elements.place.value;
            lines = ['Confirm the governing utility, protection level, exact model, size and permitted orientation.', 'Record the existing assembly, nameplate, flow arrow and accessible site photos.'];
            if (job === 'Replace an existing assembly') lines.push('Compare the replacement dimensions and service envelope with the existing piping; nominal size alone does not establish fit.');
            if (system === 'Fire protection') lines.push('Coordinate the fire authority, qualified designer and an approved shutdown before any work.');
            if (place === 'Indoors or in a mechanical room') lines.push('Ask the designer to verify relief-discharge capacity, air-gap arrangement, drainage and nearby flood exposure.');
            if (/Outdoors/.test(place)) lines.push('Confirm freeze exposure, enclosure suitability, drainage and service access for this exact model.');
            if (system === 'Irrigation') lines.push('Confirm whether the utility accepts the selected device family. For a PVB, check the manual’s reference to the highest downstream piping.');
            lines.push('Separate the quote into assembly, fittings, labor, enclosure/drainage, permits, initial test and report filing.', 'Name the permit/approval owner, qualified tester and report filer; keep the passing test and acceptance proof.');
            brief = 'BACKFLOW INSTALLER BRIEF\n' + job + ' / ' + system + ' / ' + place + '\n\n' + lines.map(function (line, i) { return (i + 1) + '. ' + line; }).join('\n') + '\n\nPlanning checklist only; confirm the exact manufacturer drawing and governing authority.\nReference: https://backflowpath.com' + desk.dataset.source;
            desk.querySelector('[data-brief-summary]').textContent = job + ' · ' + system + ' · ' + place;
            var list = desk.querySelector('[data-brief-list]'); list.replaceChildren();
            lines.forEach(function (line) { var li = document.createElement('li'); li.textContent = line; list.appendChild(li); });
            desk.querySelector('[data-brief-output]').hidden = false;
            briefReady = true;
        };
        briefForm.addEventListener('submit', function (event) {
            event.preventDefault(); buildBrief();
            desk.querySelector('[data-brief-output]').focus({preventScroll: true});
            emit('installation_brief_created', {system: briefForm.elements.system.value, setting: briefForm.elements.place.value, job: briefForm.elements.job.value, source_page: desk.dataset.source});
        });
        briefForm.addEventListener('change', function () { if (briefReady) buildBrief(); });
        desk.querySelector('[data-copy-brief]').addEventListener('click', async function () {
            var status = desk.querySelector('[data-copy-status]');
            try { await navigator.clipboard.writeText(brief); status.textContent = 'Copied'; emit('installation_brief_copy', {source_page: desk.dataset.source}); }
            catch (error) { status.textContent = 'Copy unavailable. Use Print brief to save a PDF.'; }
        });
        desk.querySelector('[data-print-brief]').addEventListener('click', function () {
            document.body.classList.add('fv-print-brief');
            window.print(); document.body.classList.remove('fv-print-brief');
            emit('installation_brief_print', {source_page: desk.dataset.source});
        });
        desk.querySelectorAll('[data-desk-help]').forEach(function (link) {
            var url = new URL(desk.dataset.helpUrl, location.origin);
            url.searchParams.set('issueType', link.dataset.deskHelp);
            link.href = url.pathname + url.search;
            link.addEventListener('click', function () {
                var notes = link.dataset.deskHelp === 'installation' && briefReady ? brief : 'PARTS CHECK\nModel: ' + (model.value ? model.options[model.selectedIndex].text : 'Not identified') + '\nNominal size: ' + (size.value || 'Not confirmed') + '\nRepair scope: ' + (scope.value || 'Not confirmed') + '\nPublished references to review: ' + (matches.map(function (item) { return item.dataset.part; }).join(', ') || 'None selected') + '\nThis selection is self-reported, not a confirmed diagnosis.';
                try { sessionStorage.setItem(storeKey, JSON.stringify({source: desk.dataset.source, issue: link.dataset.deskHelp, notes: notes, at: Date.now()})); } catch (error) { /* The form works without browser storage. */ }
                emit('context_help_start', {issue_type: link.dataset.deskHelp, source_page: desk.dataset.source, model_slug: link.dataset.deskHelp === 'parts-check' ? model.value : undefined});
            });
        });
    }
    document.querySelectorAll('[data-identify-help]').forEach(function (link) {
        link.addEventListener('click', function () {
            var notes = 'MODEL IDENTIFICATION REQUEST\nManufacturer: not confirmed\nFull model and suffix: not confirmed\nNominal size: not confirmed\nPlease review readable markings and relevant manufacturer references.\nPhoto checklist: full device; readable nameplate or cast markings; question area.\nQuestion or observed symptom: ';
            var source = new URL(link.href, location.origin).searchParams.get('source');
            try { sessionStorage.setItem(storeKey, JSON.stringify({source: source, issue:'model-identification', notes:notes, at:Date.now()})); } catch (error) { /* The form works without browser storage. */ }
            emit('identification_request_start', {source_page:source});
        });
    });
    document.querySelectorAll('[data-library-help]').forEach(function (link) {
        link.addEventListener('click', function () {
            var selectedModel = document.querySelector('[data-global-model]');
            var selectedSize = document.querySelector('[data-global-size]');
            var selectedScope = document.querySelector('[data-global-task]');
            var parts = Array.from(document.querySelectorAll('[data-global-option]')).filter(function (row) { return !row.hidden; }).map(function (row) { return row.querySelector('.bv-kit-row__part strong').textContent; });
            var notes = 'PARTS CHECK\nModel: ' + (selectedModel.value ? selectedModel.options[selectedModel.selectedIndex].text : 'Not identified') + '\nNominal size: ' + (selectedSize.value || 'Not confirmed') + '\nRepair scope: ' + (selectedScope.value || 'Not confirmed') + '\nPublished references to review: ' + (parts.join(', ') || 'None selected') + '\nThis selection is self-reported, not a confirmed diagnosis.';
            try { sessionStorage.setItem(storeKey, JSON.stringify({source: location.pathname, issue: 'parts-check', notes: notes, at: Date.now()})); } catch (error) { /* Optional context only. */ }
        });
    });
    var support = document.querySelector('[data-support-form]');
    if (support) {
        try {
            var saved = JSON.parse(sessionStorage.getItem(storeKey) || 'null');
            if (saved && saved.source === support.elements.sourcePage.value && saved.issue === support.elements.issueType.value && Date.now() - saved.at < 2 * 60 * 60 * 1000 && typeof saved.notes === 'string' && saved.notes.length < 10000 && !support.elements.notes.value) {
                support.elements.notes.value = saved.notes;
                var confirmation = document.querySelector('[data-support-context]');
                confirmation.hidden = false;
                confirmation.textContent = 'Your ' + (saved.issue === 'installation' ? 'installer brief' : saved.issue === 'model-identification' ? 'identification checklist' : 'model and part selection') + ' is included below. Review or edit it before sending.';
            }
        } catch (error) { /* No browser storage or invalid old context; use the normal form. */ }
        var validateContact = function () {
            support.elements.email.setCustomValidity(!support.elements.email.value.trim() && !support.elements.phone.value.trim() ? 'Enter an email address or phone number so we can reply.' : '');
        };
        support.elements.email.addEventListener('input', validateContact);
        support.elements.phone.addEventListener('input', validateContact);
        validateContact();
        var photoInput = support.querySelector('[data-photo-input]');
        if (photoInput) {
            var photoUrls = [];
            var renderPhotos = function () {
                photoUrls.forEach(function (url) { URL.revokeObjectURL(url); }); photoUrls = [];
                var files = Array.from(photoInput.files || []);
                var previews = support.querySelector('[data-photo-previews]'); previews.replaceChildren();
                var error = files.length > 3 ? 'Choose no more than three photos.' : files.some(function (file) { return file.size > 5 * 1024 * 1024; }) ? 'Each photo must be 5 MB or smaller.' : files.some(function (file) { return !['image/jpeg','image/png'].includes(file.type); }) ? 'Use JPEG or PNG photos. Export other formats first.' : '';
                photoInput.setCustomValidity(error);
                support.querySelector('[data-photo-status]').textContent = error || (files.length ? files.length + ' photo' + (files.length === 1 ? '' : 's') + ' selected. Nothing uploads until you send.' : '');
                files.slice(0,3).forEach(function (file, index) {
                    if (!['image/jpeg','image/png'].includes(file.type) || file.size > 5*1024*1024) return;
                    var url = URL.createObjectURL(file); photoUrls.push(url);
                    var figure = document.createElement('figure'); var image = document.createElement('img'); image.src = url; image.alt = 'Selected device photo ' + (index + 1);
                    var remove = document.createElement('button'); remove.type = 'button'; remove.textContent = 'Remove photo ' + (index + 1);
                    remove.addEventListener('click', function () {
                        if (typeof DataTransfer === 'undefined') { photoInput.value = ''; } else {
                            var transfer = new DataTransfer(); Array.from(photoInput.files).forEach(function (candidate, candidateIndex) { if (candidateIndex !== index) transfer.items.add(candidate); }); photoInput.files = transfer.files;
                        }
                        renderPhotos();
                    });
                    figure.append(image, remove); previews.appendChild(figure);
                });
                emit('request_photo_selection', {photo_count:Math.min(files.length,3), valid_selection:!error});
            };
            photoInput.addEventListener('change', renderPhotos);
            window.addEventListener('pagehide', function () { photoUrls.forEach(function (url) { URL.revokeObjectURL(url); }); });
        }
    }
}());
