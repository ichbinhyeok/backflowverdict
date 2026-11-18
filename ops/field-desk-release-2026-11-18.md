# Field desk production release — 2026-11-18

Owner authorization: “배포”.

- [PR #7](https://github.com/ichbinhyeok/backflowverdict/pull/7) merged.
- Deployed runtime: `0c4022514c1a50329f5222d399b935dfd0cb0f2b`.
- [Deployment run](https://github.com/ichbinhyeok/backflowverdict/actions/runs/44288792448), job `134748287654`.
- Deployment succeeded. Server verification completed at 2026-11-18 11:00:27 UTC / 20:00:27 KST; independent public verification followed.

Release includes the two field desks, ten-model document/parts navigation, installer brief, unknown-model photo route, private image storage/review drafts and exact FEBCO retailer matches. The existing October search baseline and review dates are preserved.

The nginx updater changes only explicit BackflowPath server blocks, validates/reloads and supports restoration with the release backup. The workflow also checks configured admin/signing values, the persistent lead mount and host routing. Private local QA records and research exports are excluded from the release.

## Production verification

- The CI Java, five nginx updater and 17 Chromium contract steps passed. Full local Java suite: 100 tests.
- The server verified nonempty admin/signing configuration and a writable persistent lead volume. The updater confirmed a 16m limit across one BackflowPath server block and changed one nginx configuration file. Health passed on attempt two; canonical, robots, sitemap, existing-winner and host-routing checks passed.
- Eight existing recovery URLs returned HTTP 200, one H1, matching canonical and index/follow. The generated WebP, nameplate SVG, field-desk JavaScript, FEBCO model page and ready endpoint returned 200.
- The live diagram desk exposed manufacturer documents before size selection. Wilkins 975XL and FEBCO 765 returned the expected exact references; the new FEBCO 905211 retailer route matched. Selection and signed source metadata reached the parts-review form.
- The live installation brief included fire-system/indoor constraints and reached the installation-review form with its content intact.
- Desktop 1440px and mobile 390px fit without horizontal overflow. No page JavaScript exceptions or unhandled rejections were observed. The desktop navigation timing sample was about 703ms; this is one QA sample, not a field performance statistic. GA4 requests were blocked in the QA browser so these checks did not intentionally create business analytics events; expected blocked-network messages were excluded from application errors.
- An unauthenticated private-photo request returned 302 to /admin, without exposing a photo.
- A controlled public request sent a valid 2,080,457-byte PNG followed by an intentionally invalid JPEG. The ARM runtime processed the first image and rejected the second with the expected image-byte validation message before persistence. Typed notes were preserved and no success flag was emitted. This verified the Cloudflare/nginx/Tomcat upload path and image normalization without creating a synthetic production lead or stored photo. Complete successful persistence/review had already been verified locally and in the real-Tomcat regression.
- Screenshots: output/playwright/production-field-desk-desktop-2026-11-18.png and production-field-desk-mobile-2026-11-18.png.

The October 12 experiment baseline/day zero and December 11 / January 10 gates remain unchanged. This release is a separate intervention. No indexing request, customer message, provider dispatch, payment, earned revenue or observed conversion lift is claimed.
