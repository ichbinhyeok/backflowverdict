@import owner.backflow.web.PageMeta
@param PageMeta page
@template.layouts.verdict(page = page, content = @`
    <section class="fv-confirm"><div class="bv-shell"><p class="fv-eyebrow">Your request was not saved</p><h1>Choose smaller photos.</h1><p class="bv-lede">Use up to three JPEG or PNG photos, 5 MB each and 16 megapixels or less.</p><p>Go back to your form, choose smaller copies and send again. Photos must be selected again. You can also send the request without photos.</p><a class="fv-primary" href="/get-help/?issueType=model-identification">Return to the request form →</a></div></section>
`)
