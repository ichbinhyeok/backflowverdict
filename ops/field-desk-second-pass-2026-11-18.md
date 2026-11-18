# Second pass — useful identification and a concrete review outcome

Local implementation scope authorized by the owner on 2026-11-18. No production deployment or external messages are part of this pass.

Subsequent instruction: the owner explicitly requested production deployment. The release is now being prepared through the existing OCI workflow; final production evidence will be recorded separately. This supersedes the earlier local-only deployment boundary without authorizing external customer messages.

Visual thesis: a precise equipment reference with the working document beside the selection, and a clear photographic route when the label is unknown.

Content plan: known-model lookup with manufacturer documents → unknown-model photo guide → explicit four-part review format → private operator review workspace.

Interaction thesis: model selection reveals its real source documents; unknown-model selection reveals a nameplate/photo checklist; locally previewed photos travel only with the submitted form. The operator can inspect the private photos and save/download a structured reply draft.

Technical basis: [Spring multipart requests](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/multipart-forms.html), [JDK ImageReader](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/imageio/ImageReader.html). Images are validated and normalized with Java ImageIO; originals are not published as static assets.

## Implemented and verified locally

- Selecting a model reveals its existing manufacturer references and available parts drawings immediately, before a size is selected. The hero's observed component can lead into this desk; it is never used to infer a failed part.
- Added an unknown-model route with an original instructional nameplate SVG, three-photo checklist, signed source context and a model-identification task in the review form.
- Optional JPEG/PNG upload supports up to three images, 5 MB each and at most 16 megapixels each. The server verifies image bytes and dimensions, resizes long edges to at most 2,000px, applies JPEG orientation, removes embedded source metadata and keeps only normalized JPEG copies. Browser selection/previews do not upload until submit.
- Photos live below the existing private lead storage root, not static resources. Authenticated admin endpoints return them with no-store and nosniff headers. CSRF-protected photo removal keeps the underlying request.
- Public copy and an explicitly illustrative example show the four-part review: confirmed information, document/part candidates, unknowns and next action.
- The private request queue links to a review workspace with photo inspection, four editable fields, persistent drafts and a downloadable reply text. Saving/downloading a draft is not message delivery. The review page does not load GA4 or the custom analytics script.
- Invalid images preserve typed form values through the existing redirect/flash flow. Upload-limit errors show a retry page. Production nginx reference now allows a bounded 16 MB request.

Verification: full Gradle suite **100 passing**, Chromium **17/17**. Tests cover image-byte validation, count/byte/pixel bounds, EXIF orientation/metadata removal, private access, CSRF, draft persistence/export, deletion, exact retailer matching, keyboard/mobile/no-JavaScript paths and the existing winner/recovery contracts.

A manual local browser submission with an explicitly synthetic fixture saved one photo and the correct model-identification issue, source URL and VERIFIED_PAGE_CONTEXT. The admin browser then loaded the private photo, saved and updated all four draft sections and downloaded the reply text. No real customer, production inquiry or external message was used.

The real HTTP check found that this pinned Spring Boot 4.0.3 runtime configures Tomcat max-part-count=10; the complete browser form exceeded it even with a 200 KB image. Set max-part-count=24 and added a random-port, real-Tomcat multipart regression with all browser fields. This is independently checked beyond MockMvc. Multipart configuration reference: [Spring Boot](https://docs.spring.io/spring-boot/4.0/appendix/application-properties/).

Screenshots: output/playwright/conversion-v3-identify-desktop.png, conversion-v3-photo-form.png and conversion-v3-private-review.png. The screenshots contain synthetic/local QA data. Section captures suppress the sticky header only while capturing to avoid a stitching overlay.

## Deployment and operating boundary

This remains a local, uncommitted implementation. The public October release is unchanged. Local runtime uses build/review-leads, disabled email/GA4 and disposable local-only admin credentials.

Before a production release, apply/reload the nginx 16 MB limit from ops/nginx/backflowpath.conf, verify the production admin/signing configuration and persistent lead volume, and repeat a controlled production verification within the authorized release scope. The configured volume ./leads:/app/leads persists the new photos and review subfolders.

The operator must inspect the manual queue and decide which requests are actionable. No response-time guarantee, automated identification, paid review, affiliate agreement, dispatch, customer reply or earned revenue is established by this work. Specific reply drafts can now be prepared from the evidence; sending remains a separate external action.

## Exact retailer matching

Added two ordinary, uncompensated retailer routes for FEBCO 765 in the 1/2–3/4 inch size band. No price, availability, delivery estimate or commission is promised.

| Stored reference | Manufacturer ordering code | Primary manufacturer source | Retailer source |
| --- | --- | --- | --- |
| FRK 765-BP 1/2-3/4 | 905211 | [Watts](https://www.watts.com/products/plumbing-flow-control-solutions/repair-kits/backflow-preventer-repair-kits/rk-765/frk-765-bp-12-34) | [SupplyHouse](https://www.supplyhouse.com/Febco-905211-Bonnet-Poppet-Kit-1-2-3-4-765-Pressure-Vacuum-Breaker) |
| FRK 765-CK 1/2-3/4 | 905051 | [Watts](https://www.watts.com/products/plumbing-flow-control-solutions/repair-kits/backflow-preventer-repair-kits/rk-765/frk-765-ck-12-34) | [SupplyHouse](https://www.supplyhouse.com/Febco-905051-Check-Assembly-Kit-for-1-2-3-4-765-Pressure-Vacuum-Breaker) |

Both manufacturer pages explicitly match the code, family and nominal-size band. The retailer pages identify the same SKU, model family and size. Other sellers/search snippets confused 905212 sizing; those candidates were not added. Earlier source dates and the overall model review date were preserved; the new two source entries and retailer checks are dated November 18.
