@import owner.backflow.web.PageMeta
@param PageMeta page

@template.layouts.app(page = page, content = @`
    <main class="partner-guide-surface">
        <section class="partner-guide-hero partner-guide-hero--sales">
            <div class="container partner-guide-hero__inner">
                <div class="partner-guide-hero__copy">
                    <div class="partner-guide-kicker">
                        <span class="partner-guide-mark">BP</span>
                        <span>North Texas Backflow Co. · Customer portal</span>
                    </div>
                    <p class="partner-guide-eyebrow">Post-test customer guide</p>
                    <h1>Your Dallas backflow test is complete.</h1>
                    <p class="partner-guide-lede">North Texas Backflow Co. has organized the next steps below so you can finish the report without guessing.</p>
                    <div class="partner-guide-hero__actions">
                        <a class="button button--light" href="/cities/texas/dallas/submit-backflow-report">Open Dallas submission route</a>
                        <a class="partner-guide-text-link" href="#contact">Contact your inspector <span aria-hidden="true">↓</span></a>
                    </div>
                    <p class="partner-guide-hero__microcopy">Sample partner page · Dallas, Texas · SwiftComply notice</p>
                </div>
                <div class="partner-guide-demo" aria-label="Preview of a customer handoff page">
                    <div class="partner-guide-demo__bar"><span></span><span></span><span></span><strong>North Texas Backflow Co.</strong></div>
                    <div class="partner-guide-demo__body">
                        <span class="partner-guide-demo__label">Dallas Water Utilities · SwiftComply</span>
                        <h2>Your test is done.<br><em>Here is what is next.</em></h2>
                        <div class="partner-guide-demo__line"><b>01</b><span>Confirm the report details</span></div>
                        <div class="partner-guide-demo__line"><b>02</b><span>Use the required Dallas route</span></div>
                        <div class="partner-guide-demo__line"><b>03</b><span>Keep proof of filing</span></div>
                        <div class="partner-guide-demo__footer">North Texas Backflow Co. · Customer support</div>
                    </div>
                </div>
            </div>
        </section>

        <section class="partner-guide-proof" aria-label="Test status">
            <div class="container partner-guide-proof__grid">
                <div><span>01</span><strong>Test complete</strong><p>Confirm the property, device, test date, and result.</p></div>
                <div><span>02</span><strong>Submit the report</strong><p>Use the Dallas route and follow the current portal instructions.</p></div>
                <div><span>03</span><strong>Keep your proof</strong><p>Save the confirmation with your property records.</p></div>
            </div>
        </section>

        <section class="section section--tight" id="what-your-customer-sees">
            <div class="container partner-guide-content">
                <div class="partner-guide-content__heading">
                    <p class="partner-guide-eyebrow">Your next three moves</p>
                    <h2>Keep the report moving.</h2>
                    <p>Use this checklist after your inspector sends the completed test information.</p>
                </div>
                <div class="partner-guide-steps">
                    <article class="partner-guide-step partner-guide-step--active">
                        <span class="partner-guide-step__number">1</span>
                        <div><h3>Confirm the report details</h3><p>Check the property, device or assembly ID, test date, and result against the notice you received.</p></div>
                        <span class="partner-guide-step__tag">Start here</span>
                    </article>
                    <article class="partner-guide-step">
                        <span class="partner-guide-step__number">2</span>
                        <div><h3>Use the required Dallas route</h3><p>Open the source-backed route to confirm the current submission path and any tester or portal requirements.</p></div>
                        <a class="partner-guide-step__arrow" href="/cities/texas/dallas/submit-backflow-report" aria-label="Open Dallas submission route">↗</a>
                    </article>
                    <article class="partner-guide-step">
                        <span class="partner-guide-step__number">3</span>
                        <div><h3>Keep proof of filing</h3><p>Save the confirmation, receipt, or accepted-report record with your property documents.</p></div>
                        <span class="partner-guide-step__tag partner-guide-step__tag--muted">Save this</span>
                    </article>
                </div>
            </div>
        </section>

        <section class="partner-guide-callout">
            <div class="container partner-guide-callout__inner">
                <div>
                    <p class="partner-guide-eyebrow">If the test did not pass</p>
                    <h2>Do not stop at the result.</h2>
                    <p>Ask your inspector whether repair, retest, and final report submission are included in the next step. The utility’s workflow still controls what counts as complete.</p>
                </div>
                <a class="button button--dark" href="/cities/texas/dallas/failed-backflow-test">Find failed-test guidance</a>
            </div>
        </section>

        <section class="section section--tight" id="contact">
            <div class="container partner-guide-footer">
                <div>
                    <p class="partner-guide-eyebrow">Need help?</p>
                    <h2>Contact North Texas Backflow Co.</h2>
                    <p>Ask your inspector about repair, retest, or final submission if the result is not complete. This customer page keeps the handoff in one place.</p>
                </div>
                <div class="partner-guide-footer__note">
                    <span class="partner-guide-mark partner-guide-mark--small">BP</span>
                    <p><strong>Sample page</strong><br>Built with BackflowVerdict for a clearer customer handoff.</p>
                </div>
            </div>
        </section>
    </main>
`)
