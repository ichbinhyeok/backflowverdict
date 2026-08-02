(function () {
    var choices = document.querySelectorAll('.bv-choice');
    choices.forEach(function (choice) {
        choice.addEventListener('pointerenter', function () {
            choice.setAttribute('data-active', 'true');
        });
        choice.addEventListener('pointerleave', function () {
            choice.removeAttribute('data-active');
        });
    });

    document.addEventListener('click', function (event) {
        var link = event.target.closest('a[href]');
        if (!link || typeof window.gtag !== 'function') {
            return;
        }
        var href = link.getAttribute('href') || '';
        if (href.indexOf('/get-help/') === 0 || href.indexOf('/leads/new') === 0) {
            window.gtag('event', 'request_help_click', {destination_path: href});
        } else if (href.indexOf('/submit-backflow-report') === 0) {
            window.gtag('event', 'report_submission_route_click', {destination_path: href});
        } else if (href.indexOf('/approved-backflow-tester') === 0 || href.indexOf('/official-backflow-tester') === 0) {
            window.gtag('event', 'tester_route_click', {destination_path: href});
        } else if (link.host && link.host !== window.location.host) {
            window.gtag('event', 'provider_website_click', {destination: link.href});
        } else if (href.indexOf('/utilities/') === 0 || href.indexOf('/notice-finder') === 0) {
            window.gtag('event', 'official_route_click', {destination_path: href});
        }
    });

    document.addEventListener('submit', function (event) {
        if (typeof window.gtag !== 'function') return;
        var action = event.target.getAttribute('action') || '';
        if (action === '/notice-finder') window.gtag('event', 'notice_finder_search');
        if (action === '/leads') window.gtag('event', 'lead_form_submit');
    });

    var guide = document.querySelector('[data-guide-slug]');
    var pageGroup = guide ? 'decision_guides' : document.querySelector('.bv-utility-record') ? 'utility_records' : document.querySelector('[data-route-list]') ? 'submission_routes' : document.querySelector('.bv-home') ? 'home' : 'other_verdict';
    if (typeof window.gtag === 'function') {
        window.gtag('event', 'page_context', {
            content_group: pageGroup,
            guide_slug: guide ? guide.getAttribute('data-guide-slug') : undefined,
            guide_type: guide ? guide.getAttribute('data-guide-type') : undefined
        });
    }

    document.querySelectorAll('[data-choice-tool]').forEach(function (tool) {
        var result = tool.querySelector('[data-tool-result]');
        tool.querySelectorAll('[data-tool-choice]').forEach(function (button) {
            button.addEventListener('click', function () {
                tool.querySelectorAll('[data-tool-choice]').forEach(function (item) { item.removeAttribute('aria-pressed'); });
                button.setAttribute('aria-pressed', 'true');
                result.querySelector('[data-result-title]').textContent = button.getAttribute('data-result-title');
                result.querySelector('[data-result-detail]').textContent = button.getAttribute('data-result-detail');
                result.querySelector('[data-result-link]').setAttribute('href', button.getAttribute('data-result-href') || '#safe-checks');
                result.hidden = false;
                result.focus({preventScroll: true});
                result.scrollIntoView({behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth', block: 'nearest'});
                if (typeof window.gtag === 'function') window.gtag('event', 'decision_tool_result', {tool_type: 'choice', result: button.getAttribute('data-result-title')});
            });
        });
    });

    document.querySelectorAll('[data-score-tool]').forEach(function (tool) {
        tool.addEventListener('submit', function (event) {
            event.preventDefault();
            var score = Array.prototype.slice.call(tool.querySelectorAll('input:checked')).reduce(function (sum, input) { return sum + Number(input.value); }, 0);
            var result = tool.querySelector('[data-tool-result]');
            var title = score >= 5 ? 'Replacement deserves the first quote' : score >= 2 ? 'Compare a documented repair with replacement' : 'A targeted repair may still be reasonable';
            var detail = score >= 5 ? 'Structural damage, repeat failure, or unavailable approved parts shift the decision toward replacement. Confirm device approval and required retesting.' : score >= 2 ? 'Ask for separate repair and replacement scopes, including parts, labor, shutdown, testing, and report filing.' : 'A qualified technician can confirm the failure point and whether an approved kit makes repair economical.';
            result.querySelector('[data-result-title]').textContent = title;
            result.querySelector('[data-result-detail]').textContent = detail;
            result.hidden = false;
            result.focus({preventScroll: true});
            if (typeof window.gtag === 'function') window.gtag('event', 'decision_tool_result', {tool_type: 'repair_score', score: score});
        });
    });

    document.querySelectorAll('[data-pressure-tool]').forEach(function (tool) {
        tool.addEventListener('submit', function (event) {
            event.preventDefault();
            var staticPsi = Number(tool.elements.staticPsi.value);
            var flowPsi = Number(tool.elements.flowPsi.value);
            var drop = staticPsi - flowPsi;
            var title;
            var detail;
            if (staticPsi > 80) {
                title = 'Static pressure is above the common residential code threshold';
                detail = 'Do not keep turning the adjustment bolt without the model instructions. Confirm the gauge and have the regulator, thermal expansion control, and local requirements checked.';
            } else if (staticPsi < 30) {
                title = 'The static reading is low';
                detail = 'The cause may be upstream supply, a closed valve, restriction, or regulator behavior. Compare another time and location before blaming the regulator.';
            } else if (drop > 20) {
                title = 'The pressure falls sharply under flow';
                detail = 'A restriction, undersized path, clogged strainer, or regulator condition is possible. Record fixture flow and repeat the measurement before service.';
            } else {
                title = 'The two readings do not show an obvious extreme';
                detail = 'Symptoms can still be intermittent. Record overnight pressure and the exact fixtures affected, then follow the manufacturer and local service route.';
            }
            var result = tool.querySelector('[data-tool-result]');
            result.querySelector('[data-result-title]').textContent = title;
            result.querySelector('[data-result-detail]').textContent = detail + ' Measured drop: ' + Math.max(0, drop).toFixed(0) + ' PSI.';
            result.hidden = false;
            result.focus({preventScroll: true});
            if (typeof window.gtag === 'function') window.gtag('event', 'decision_tool_result', {tool_type: 'pressure', static_band: staticPsi > 80 ? 'high' : staticPsi < 30 ? 'low' : 'common'});
        });
    });

    document.querySelectorAll('[data-cost-tool]').forEach(function (tool) {
        tool.addEventListener('submit', function (event) {
            event.preventDefault();
            var access = tool.elements.access.value;
            var scope = tool.elements.scope.value;
            tool.querySelector('[data-cost-range]').textContent = access + ' · ' + scope;
            tool.querySelector('[data-cost-detail]').textContent = 'Request separate line items for device, labor, ' + access.toLowerCase() + ', permits, testing, and report filing. Compare scope before price; this tool does not invent a national dollar range.';
            var result = tool.querySelector('[data-tool-result]');
            result.hidden = false;
            result.focus({preventScroll: true});
            if (typeof window.gtag === 'function') window.gtag('event', 'decision_tool_result', {tool_type: 'quote_scope', access: access, scope: scope});
        });
    });

    var routeFilter = document.querySelector('[data-route-filter]');
    var routeList = document.querySelector('[data-route-list]');
    if (routeFilter && routeList) {
        var routes = Array.prototype.slice.call(routeList.querySelectorAll('[data-search]'));
        var empty = document.querySelector('[data-route-empty]');
        var routeCount = document.querySelector('[data-route-count]');
        var applyFilter = function () {
            var query = routeFilter.value.trim().toLowerCase();
            var visible = 0;
            routes.forEach(function (route) {
                var match = !query || (route.getAttribute('data-search') || '').toLowerCase().indexOf(query) >= 0;
                route.hidden = !match;
                if (match) visible += 1;
            });
            if (empty) empty.hidden = visible !== 0;
            if (routeCount) routeCount.textContent = visible;
        };
        routeFilter.addEventListener('input', applyFilter);
        var clear = document.querySelector('[data-route-clear]');
        if (clear) clear.addEventListener('click', function () { routeFilter.value = ''; applyFilter(); routeFilter.focus(); });
    }

    document.querySelectorAll('[data-utility-picker]').forEach(function (picker) {
        var filter = picker.querySelector('[data-utility-filter]');
        var select = picker.querySelector('[data-utility-select]');
        var count = picker.querySelector('[data-utility-count]');
        var submit = picker.querySelector('button[type="submit"]');
        var prompt = select.options[0].cloneNode(true);
        var options = Array.prototype.slice.call(select.options, 1).map(function (option) {
            return option.cloneNode(true);
        });
        var renderUtilities = function () {
            var query = filter.value.trim().toLowerCase();
            var selectedValue = select.value;
            var matches = options.filter(function (option) {
                return !query || option.textContent.toLowerCase().indexOf(query) >= 0;
            });
            select.innerHTML = '';
            select.appendChild(prompt.cloneNode(true));
            matches.forEach(function (option) {
                var copy = option.cloneNode(true);
                copy.selected = copy.value === selectedValue;
                select.appendChild(copy);
            });
            if (count) count.textContent = matches.length;
            select.disabled = matches.length === 0;
            if (submit) submit.disabled = matches.length === 0;
        };
        filter.addEventListener('input', renderUtilities);
        renderUtilities();
    });

    var uniqueValues = function (nodes, attribute, filterAttribute, filterValue) {
        return nodes.reduce(function (values, node) {
            if (filterAttribute && node.getAttribute(filterAttribute) !== filterValue) return values;
            var value = node.getAttribute(attribute);
            if (value && values.indexOf(value) < 0) values.push(value);
            return values;
        }, []);
    };
    var replaceOptions = function (select, prompt, values) {
        select.innerHTML = '';
        var first = document.createElement('option');
        first.value = '';
        first.textContent = prompt;
        select.appendChild(first);
        values.forEach(function (value) {
            var option = document.createElement('option');
            option.value = value;
            option.textContent = value;
            select.appendChild(option);
        });
        select.disabled = values.length === 0;
    };

    document.querySelectorAll('[data-model-kit-tool]').forEach(function (tool) {
        var rows = Array.prototype.slice.call(tool.querySelectorAll('[data-kit-option]'));
        var size = tool.querySelector('[data-kit-size]');
        var task = tool.querySelector('[data-kit-task]');
        var count = tool.querySelector('[data-kit-count]');
        var empty = tool.querySelector('[data-kit-empty]');
        var render = function () {
            var visible = 0;
            rows.forEach(function (row) {
                var match = size.value && row.getAttribute('data-size') === size.value && (!task.value || row.getAttribute('data-task') === task.value);
                row.hidden = !match;
                if (match) visible += 1;
            });
            empty.hidden = !size.value || visible !== 0;
            count.textContent = !size.value ? 'Choose a size band to reveal compatible published kits.' : visible + ' published match' + (visible === 1 ? '' : 'es') + (task.value ? ' for this repair scope.' : ' in this size band.');
        };
        size.addEventListener('change', function () {
            replaceOptions(task, 'All repair scopes', uniqueValues(rows, 'data-task', 'data-size', size.value));
            render();
        });
        task.addEventListener('change', render);
    });

    document.querySelectorAll('[data-global-kit-tool]').forEach(function (tool) {
        var rows = Array.prototype.slice.call(tool.querySelectorAll('[data-global-option]'));
        var model = tool.querySelector('[data-global-model]');
        var size = tool.querySelector('[data-global-size]');
        var task = tool.querySelector('[data-global-task]');
        var status = tool.querySelector('[data-global-status]');
        var render = function () {
            var visible = 0;
            rows.forEach(function (row) {
                var match = model.value && size.value && row.getAttribute('data-model') === model.value && row.getAttribute('data-size') === size.value && (!task.value || row.getAttribute('data-task') === task.value);
                row.hidden = !match;
                if (match) visible += 1;
            });
            status.querySelector('span').textContent = visible ? visible + ' PUBLISHED MATCH' + (visible === 1 ? '' : 'ES') : 'COMPATIBILITY CHECK';
            status.querySelector('p').textContent = !model.value ? 'Read the complete body label. Do not identify a kit from appearance alone.' : !size.value ? 'Now match the nominal size marked on the assembly.' : visible ? 'Confirm the part reference against the linked official source before purchase.' : 'No stored match. Recheck the model label, size, and manufacturer source.';
        };
        model.addEventListener('change', function () {
            replaceOptions(size, 'Choose nominal size', uniqueValues(rows, 'data-size', 'data-model', model.value));
            replaceOptions(task, 'Choose size first', []);
            render();
        });
        size.addEventListener('change', function () {
            var modelRows = rows.filter(function (row) { return row.getAttribute('data-model') === model.value && row.getAttribute('data-size') === size.value; });
            replaceOptions(task, 'All repair scopes', uniqueValues(modelRows, 'data-task'));
            render();
        });
        task.addEventListener('change', render);
    });
}());
