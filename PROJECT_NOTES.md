# Project Notes (rough, working doc — not the final README)

Keeping track of key decisions and why, so Sprint 7 (final README + report) is fast to write later.

---

## Sprint 1 — Core Framework
- Site: switched from automationexercise.com (original plan) to **SauceDemo** — cleaner HTML, no ads, industry-standard practice site, stable `data-test` attributes on every element (huge win for reliable locators).
- Structure: BasePage → Pages → Flows → Tests. Flows chain multiple pages into real user journeys (login → cart → checkout).
- Kept both raw ("noflow") tests and flow-based tests deliberately — shows progression from basic to abstracted, good story for defense.
- Added 2 API tests using **Restful-Booker** (industry-standard practice API) — ping health check + full create/retrieve booking test. SauceDemo has no public API, so this is a separate, deliberate addition.
- Config values (URLs, credentials, error messages) centralized in `config.properties` + `ConfigReader` utility — no hardcoded values in tests.

## Sprint 2 — GitHub Actions CI/CD
- `.github/workflows/ci.yml` — runs `mvn clean test` on every push to main.
- Headless Chrome required in CI (no display on GitHub's runners) — this is why `BaseTest` has environment-driven browser config from the start.

## Sprint 3 — Jenkins (local, Docker)
- Jenkins running locally via Docker, JDK 17, webhook from GitHub triggers builds automatically.
- Jenkinsfile: 3 stages (Checkout, Build, Test), plus JUnit result publishing + artifact archiving.
- Jenkins Docker container uses **Chromium**, not Chrome — env vars `CHROME_BIN` / `CHROMEDRIVER_BIN` in Jenkinsfile point `BaseTest` at the right binaries. `BaseTest` falls back to WebDriverManager auto-detection when these aren't set (i.e., locally, in GitHub Actions).

## The Chrome Popup Saga (worth remembering for the defense)
- Real, recurring issue: Chrome's password leak-detection popup ("your password was found in a data breach") would randomly appear mid-test locally, stealing focus and breaking whatever field was mid-input at that moment (different field each time — lastName, postalCode, etc. — a clear sign of a race/timing issue, not a fixed locator bug).
- Tried, in order: ChromeOptions prefs (`credentials_enable_service`, `password_manager_enabled`, `password_manager_leak_detection` → false), `--disable-features=PasswordLeakDetection`, `--incognito`, a defensive "dismiss popup if present" helper (didn't work — it's native browser UI, not part of the page DOM, so Selenium can't reliably click it).
- Root cause understanding: this is partly a **server-side check** (Chrome pings Google Safe Browsing with a hash of typed credentials) — no local flag can 100% guarantee suppression, which explains the inconsistency.
- **Fix that actually worked: `--guest` mode** (`options.addArguments("--guest")`) — a fully sandboxed guest profile avoids it, confirmed via repeated manual visible-browser testing.
- Along the way, standardized on **headless by default everywhere** (`HEADLESS` env var, default true) since headless Chrome mostly avoids this entire category of native popups anyway, and it's the industry-standard way real teams run automated suites.
- Also added **Firefox as a switchable browser** (`BROWSER` env var, chrome/firefox) as a backup path — Firefox never had this problem at all. Kept both; Chrome is default again now that `--guest` fixed it.
- Real lesson for defense: this is a known, documented Selenium/Chrome community issue, not a personal mistake. The engineering story — investigate, try multiple fixes, understand root cause, land on a working solution instead of endlessly patching symptoms — is a legitimate, tell-able narrative.

## Reporting
- ExtentReports wired in via a TestNG listener (`ExtentTestListener`) — auto-generates `reports/ExtentReport.html` after every run, no changes needed to individual test files.
- Screenshot-on-failure uses reflection to grab the `driver` field from whatever test class failed (`getDeclaredField` + `setAccessible(true)` — needed since `driver` is `protected`, not `public`).
- `ThreadLocal<ExtentTest>` used for future-proofing (safe if tests ever run in parallel).

## AI Layer — Stage 1 (in progress)
- Design doc written first, before code — reframed the whole point of the AI layer: naive "paste HTML into ChatGPT" has no real engineering value; the actual contribution is a **structured extraction pipeline** (Selenium fetch → jsoup parse → categorized JSON) that produces cleaner, more consistent AI input than raw HTML.
- Comparison experiment planned: Pipeline A (structured extraction → AI) vs Pipeline B (naive raw HTML → AI), measuring selector accuracy, uniqueness rate, token cost, manual-fix effort, consistency.
- `fetch.PageFetcher` — wraps Selenium, returns rendered HTML as a String.
- `parser.HtmlParser` + `parser.ExtractedElement` — jsoup-based, extracts inputs/buttons/links with tag, type, data-test, id, name, text.
- Confirmed working end-to-end on SauceDemo login page: correctly extracted username/password/login-button with real attributes.
- `cli.Main` — manual entry point to run and inspect each pipeline stage as it's built. Deliberately NOT a TestNG test — this is a tool, not a test, so it lives in `main`, not `test`, and doesn't extend `BaseTest`.
- Next: selector priority logic (data-test > id > name > aria-label/text > CSS path > XPath) + uniqueness checking (critical for repeated components like per-product "Add to Cart" buttons).

## Known cleanup items (not urgent)
- `CheckoutStepOnePage` had a naming collision (`isDisplayed()` overloading `BasePage`'s method) — renamed, confirmed 0 usages elsewhere via IntelliJ.
- `utils.App` — scratch file used to sanity-check `Generator`/`CustomerDetail`, should be removed before final polish.
