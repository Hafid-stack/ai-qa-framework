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

---

## AI Layer — Stage 1 complete, real bugs found and fixed

Tested selector-priority logic against two real, messy pages (SauceDemo checkout-step-one and inventory), not just the simple login page. Found and fixed two genuine, evidence-based bugs along the way — good material for the report/defense, since each was found by testing against real data, not theorized in advance:

**Bug 1 — naming collision from using element `type` instead of a specific identifier.** Original `buildVariableName` only looked at `element.getType()`, so multiple `type="text"` inputs (firstName, lastName, postalCode on checkout) would all generate the same Java variable name — a real compile-breaking collision, not just a style issue. Fixed by preferring `data-test` > `id` > `name` > `type` (in that priority order) when building the variable name, not just the selector.

**Bug 2 — invalid Java identifiers from real-world messy `data-test` values.** SauceDemo has a product literally named `test.allTheThings() T-Shirt (Red)`, producing a `data-test` value with periods and parentheses. Initial fix only stripped hyphens/underscores/whitespace; broadened the regex to `[^a-zA-Z0-9]+` (split on any non-alphanumeric run) to handle arbitrary punctuation safely. This is a good real-world example for the report: production HTML is messier than clean tutorial examples, and the pipeline needed to handle that.

**Design decision — priority-list pattern over nested if/else.** `SelectorPriorityFinder` uses a `List<Function<ExtractedElement, WebElementSelector>>` of small strategy methods (`tryDataTest`, `tryId`, `tryName`, `tryLinkText`), tried in order, first match wins. Chosen over a hand-written if/else-if chain specifically because priority order becomes "position in a list" rather than nested logic — adding a new priority level later means one new method + one line, not restructuring a conditional chain. (Started as a nested if/else-if from an earlier draft; refactored once the pattern's real benefit — extensibility — became clear.)

**Design decision — deferred, not built: repeated-component / duplicate detection.** SauceDemo's own `data-test` values happen to bake in uniqueness per product (`add-to-cart-sauce-labs-backpack` vs `-bike-light`), so this specific site never actually triggered the "same selector pattern repeated across a tile" problem the design doc flagged as the hardest part. Deliberately chose not to build full parent-container-scoped duplicate detection given timeline — documented as a known limitation / future work, not silently skipped. If it comes up in defense: real production sites without per-item unique attributes would need this; SauceDemo's test-friendly design happened to sidestep it.

## AI Layer — Stage 2: Page Object generation, working end to end

`generator.PageObjectGenerator` takes the `List<WebElementSelector>` from Stage 1 and generates a complete, real, compilable Java Page Object class, written to disk.

**Key design decisions:**
- Generated classes extend the **existing** `BasePage` (not a separate one) — reuses `click()`, `type()`, `getText()`, `isDisplayed()` already built and proven throughout the framework. Keeps generated and hand-written pages structurally consistent, avoids maintaining two parallel hierarchies.
- Generated classes live in a dedicated package, `pages/generated/`, with an `...AI` suffix (e.g. `InventoryPageAI`) — physically separate from hand-written pages so generated/draft code is never confused with production-reviewed code.
- Method generation is type-aware: `input` → `type...(String)`, `button` → `click...()`, `a` (link) → `click...()` plus conditionally `getText...()`. Mirrors the same method-naming conventions already used in hand-written pages throughout the project — generated code looks like the project's own style, not foreign boilerplate.
- `WebElementSelector` carries an `elementCategory` (input/button/a) and a `hasVisibleText` flag, both computed during Stage 1 extraction, so Stage 2's generation logic doesn't need to re-inspect raw HTML — clean separation between "figure out what this element is" (Stage 1) and "decide what code to generate for it" (Stage 2).

**Real bug found and fixed (good debugging story for the report):** generated file showed every `getText...()` method duplicated verbatim, back to back. Root cause hunt: ruled out `HtmlParser` producing duplicate elements (verified with explicit count logging — 28 elements in, 28 out, no duplication) and ruled out `writeToFile` appending instead of overwriting (confirmed `Files.writeString` defaults to `CREATE`+`TRUNCATE_EXISTING`). Actual cause: in `PageObjectGenerator.buildMethods()`, the `"a"` case had two separate `getText(...)` code blocks back to back — one unconditional (leftover from before the `hasVisibleText` feature was added), one correctly gated behind `if (selector.hasVisibleText())`. The unconditional block was dead code left over from an incomplete edit, not a logic error in the loop or extraction — a good example of why "add a targeted print statement and verify with real numbers at each stage" beats guessing when a bug's symptoms (duplicate output) suggest a very different cause (duplicate data) than the real one (duplicate code path).

**Deterministic vs. AI — where the line was actually drawn.** Considered using AI even for "is this an image-only link" type decisions; concluded that's a clean, binary, rule-based check (`text` field empty + tag is `<a>` → skip `getText()`) that doesn't need AI at all — cheaper, more predictable, more defensible than an API call for something a one-line condition solves. Reserving AI specifically for genuine judgment calls without a clean rule (e.g., "should an icon-only button with no visible text still get a `getText()` method, given some future use might read an aria-label instead"). This distinction — knowing which sub-problems need AI vs. which are better solved deterministically — is itself a deliberate design point worth stating explicitly in the report, not just implementation detail.

**Where things stand on the design doc's build order:**
- ✅ Selenium → retrieve rendered HTML
- ✅ Parse with jsoup, categorize by type
- ✅ Selector-generation + uniqueness logic (priority-based, Java-identifier-safe)
- ✅ Page Object generation from structured data → real compilable `.java` files on disk
- ⏳ Repeated-component detection — deferred, documented as future work (see above)
- ⏳ AI call for genuine judgment-call refinement (single agent, narrow scope — planned next)
- ⏳ Pipeline B (naive raw-HTML-to-AI baseline) for comparison
- ⏳ Run both, collect metrics, visualize for report

**Next planned step:** wire in one focused AI agent call — reviewing generated methods/names for genuine judgment calls the deterministic rules can't confidently resolve — before attempting any multi-agent split (considered, deliberately deferred as a possible stretch-goal comparison: does splitting the refinement task across multiple narrow agents improve consistency over one agent handling it all? Not yet built.).

---

## Second real test site: automationexercise.com — found real gaps, fixed with evidence

Before wiring the AI layer, tested the deterministic pipeline against a second, real, uncontrolled site (the one from the original Selenium project, before this PFE switched to SauceDemo) — deliberately, to see what a site *without* SauceDemo's clean `data-test` conventions would expose. This produced several genuine findings, each fixed with a scoped, evidence-based change rather than over-engineering:

**Finding 1 — site uses `data-qa`, not `data-test`.** Pipeline was hardcoded to only check `data-test`, so a real, purpose-built automation attribute was invisible to it. Fixed by generalizing: `automation.attributes` config key (in `config.properties`) lists known conventions (`data-test,data-qa`) checked in priority order via a new `HtmlParser.getAutomationAttribute()` helper — rather than hardcoding either name. Verified the fix directly: email/password/name fields on login and signup forms went from generic `name`-based fallback selectors to precise, correct `data-qa`-based matches.

**Finding 2 — `buildVariableName` never checked `element.getText()`, only fell through name → type.** This meant elements without `data-test`/`id`/`name` (common on this site — nav links, submit buttons) collapsed to identical generic names based on shared `type` (`"Login"` and `"Signup"` buttons both became `Button`; all 8 nav links became `Link`) — a real, compile-breaking collision. Fixed by adding a `text` fallback tier between `name` and `type` in the priority chain. Verified: every button/link now gets a distinct, meaningful name (`byLogin`, `bySignup`, `byHome`, `byContactUs`, etc.).

**Finding 3 — hidden CSRF token fields were being extracted as if they were real interactive elements.** `<input type="hidden" name="csrfmiddlewaretoken">` appeared 3 times (once per form on the page) with the same `name`, producing genuine, unavoidable duplicate fields — not a naming bug, a real structural duplicate. Root-caused correctly: CSRF tokens are server-issued security values, never meant for user interaction (no typing, no clicking, no assertions) — filtering them out is standard, defensible practice, not corner-cutting. Fixed by skipping any `<input type="hidden">` entirely in `HtmlParser`. Result: final generated class is fully clean, zero duplicates, 17 real interactive elements, all distinct and compilable.

**Why this sequence matters for the report:** each fix was driven by running the real pipeline against real HTML and observing an actual failure/limitation, not guessed in advance — data-test-only assumption, missing text fallback, and hidden-field noise were all invisible until tested against a second site with different conventions. This is a legitimate before/after comparison: SauceDemo (clean, `data-test` everywhere) needed zero fixes; automationexercise.com (real, inconsistent conventions) needed three scoped, evidence-based fixes — a good demonstration that the pipeline's design (deterministic-first, config-driven attribute list, explicit noise filtering) generalizes reasonably well across differently-structured real sites, not just the one it was originally built against.

**Known, accepted limitation restated with fresh evidence:** true structural duplicates (same selector legitimately appearing more than once, like the CSRF tokens before filtering, or a hypothetical repeated "Add to Cart" button without a unique per-item attribute) still are not automatically scoped to a parent container — this remains deliberately deferred, documented future work, now demonstrated with a second real example beyond the original inventory-page case.