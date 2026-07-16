@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <section class="page-hero page-hero--compact-mobile">
        <div class="container page-frame page-frame--hero">
            <div class="hero__copy">
                <span class="eyebrow kicker-rule">Free partner kit</span>
                <h1>Give customers one clear link for a backflow notice.</h1>
                <p class="lede">For testers, contractors, HOA teams, and property managers: send a notice clue, city, or portal name to the source-backed BackflowPath lookup flow before you explain the next step.</p>
                <div class="hero__actions">
                    <a class="button button--primary" href="/notice-finder?ref=partner">Open the notice finder</a>
                    <a class="button button--secondary" href="/partners/sample/customer-guide">See a customer page</a>
                    <a class="button button--secondary" href="#partner-link-builder">Create a tracked link</a>
                </div>
            </div>
            <aside class="panel panel--hero">
                <span class="stat-card__label" style="color: rgba(141, 224, 196, 0.9);">Use it when</span>
                <div class="stat-card__value" style="color: #ffffff; font-size: clamp(2rem, 4vw, 2.7rem);">A customer sends a notice</div>
                <p class="stat-card__meta" style="color: rgba(239, 241, 243, 0.74);">Keep the official utility source primary. This link helps people find the right local route faster.</p>
            </aside>
        </div>
    </section>

    <section class="section section--tight">
        <div class="container content-grid">
            <article class="detail-card detail-card--key">
                <span class="eyebrow">Copy and send</span>
                <h2>Customer message</h2>
                <p id="partner-message">Add the customer’s notice clue, then copy the ready-to-send message.</p>
                <label class="field" for="notice-clue">
                    <span>Customer notice clue</span>
                    <input id="notice-clue" type="text" maxlength="180" placeholder="Dallas SwiftComply annual notice">
                </label>
                <textarea class="partner-copy" id="partner-copy" rows="6" readonly>Send the customer’s city, utility, portal name, or exact notice clue here: /notice-finder?ref=partner</textarea>
                <div class="card-actions">
                    <button class="button button--primary" type="button" data-copy-target="partner-copy">Copy customer message</button>
                    <a class="button button--secondary" id="partner-message-open" href="/notice-finder?ref=partner">Test the flow</a>
                </div>
            </article>
            <article class="detail-card">
                <span class="eyebrow">Safe handoff</span>
                <h2>What the customer should include</h2>
                <ul class="detail-list">
                    <li>City and utility name</li>
                    <li>Portal name such as SwiftComply, BSI, WEIRS, VEPO, Aqua, Tokay, or SpryBackflow</li>
                    <li>Due date, annual notice, failed-test, repair, or retest wording</li>
                    <li>Hazard ID, Site ID, account clue, device ID, or assembly serial when present</li>
                </ul>
            </article>
        </div>
    </section>

    <section class="section" id="partner-link-builder">
        <div class="container">
            <div class="section-heading">
                <span class="eyebrow">Partner tracking</span>
                <h2>Create your reusable referral link</h2>
                <p class="lede">Use a short label for your company or team. It is only used as a referral code in the URL; no customer information is stored by this tool.</p>
            </div>
            <article class="detail-card partner-link-builder">
                <label class="field" for="partner-code">
                    <span>Partner label</span>
                    <input id="partner-code" type="text" value="partner" maxlength="48" inputmode="text" placeholder="your-company">
                </label>
                <label class="field" for="partner-link">
                    <span>Reusable link</span>
                    <input id="partner-link" type="text" readonly value="/notice-finder?ref=partner">
                </label>
                <div class="card-actions">
                    <button class="button button--primary" type="button" data-copy-target="partner-link">Copy referral link</button>
                    <a class="button button--secondary" id="partner-link-open" href="/notice-finder?ref=partner">Open referral link</a>
                </div>
                <p class="share-feedback__status" id="partner-link-status" aria-live="polite"></p>
            </article>
        </div>
    </section>

    <script>
        (function () {
            var code = document.getElementById('partner-code');
            var link = document.getElementById('partner-link');
            var open = document.getElementById('partner-link-open');
            var clue = document.getElementById('notice-clue');
            var message = document.getElementById('partner-copy');
            var messageOpen = document.getElementById('partner-message-open');
            if (!code || !link || !open || !clue || !message || !messageOpen) {
                return;
            }
            function refresh() {
                var normalized = code.value.toLowerCase().replace(/[^a-z0-9_-]/g, '').slice(0, 48) || 'partner';
                var destination = new URL('/notice-finder', window.location.origin);
                destination.searchParams.set('ref', normalized);
                if (clue.value.trim()) {
                    destination.searchParams.set('q', clue.value.trim());
                }
                var value = destination.pathname + destination.search;
                link.value = value;
                open.href = value;
                messageOpen.href = value;
                message.value = clue.value.trim()
                    ? 'Use this BackflowPath route for the notice clue "' + clue.value.trim() + '": ' + destination.toString()
                    : 'Send the customer’s city, utility, portal name, or exact notice clue here: ' + destination.toString();
            }
            code.addEventListener('input', refresh);
            clue.addEventListener('input', refresh);
            refresh();
        }());
    </script>
`)
