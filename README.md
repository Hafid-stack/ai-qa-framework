# AI-Assisted QA Framework with CI/CD Pipeline

A Java/Selenium test automation framework combined with an AI-assisted pipeline that generates Page Object classes automatically from a webpage's HTML — a master's PFE project.

## Problem statement

Writing automated UI test code — locators, page objects, and test methods — is repetitive and time-consuming. This project builds a modular, AI-assisted pipeline that generates these artifacts from a webpage's HTML, letting a QA engineer choose how much of the boilerplate to automate, wrapped in a CI/CD pipeline for continuous execution. It also compares this structured approach against the naive alternative — pasting raw HTML directly into an AI — to measure whether structuring the input actually produces better, more reliable results.

## Tech stack

- **Language:** Java 17, Maven
- **Test framework:** Selenium WebDriver, TestNG, RestAssured (API testing)
- **CI/CD:** GitHub Actions (cloud) + Jenkins (local, Docker)
- **Reporting:** ExtentReports with screenshots on failure
- **AI:** Gemini 3.5 Flash (cloud) and Llama 3.1 8B via Ollama (local) — both tested and compared
- **HTML parsing:** jsoup
- **Test target:** [SauceDemo](https://www.saucedemo.com), with a second real site ([automationexercise.com](https://automationexercise.com)) used to stress-test the AI pipeline

## Running the tests

```bash
mvn clean test
```

Runs headless by default (industry standard for CI). Override with environment variables:

```bash
HEADLESS=false mvn test              # watch the browser
BROWSER=firefox mvn test             # use Firefox instead of Chrome
```

## Project structure

```
src/main/java/
├── base/          — BasePage, BaseFlow: shared Selenium action methods
├── pages/         — Hand-written Page Objects
│   ├── generated/ — AI-pipeline-generated Page Objects (Pipeline A)
│   └── naive/     — Naive single-shot AI baseline output (Pipeline B)
├── flows/         — Reusable multi-page user journeys
├── fetch/         — PageFetcher: Selenium-based HTML retrieval
├── parser/        — HtmlParser, SelectorPriorityFinder, WebElementSelector
├── generator/     — PageObjectGenerator: turns selectors into real .java files
├── ai/            — GeminiClient, OllamaClient, SelectorReviewClient, NaivePageObjectGenerator
├── utils/         — ConfigReader, Generator (test data via Faker)
└── cli/           — Manual entry points for running each pipeline stage

src/test/java/
├── base/          — BaseTest (driver setup, headless/browser switching)
├── ui/            — UI test suites (login, cart, checkout, e2e)
└── api/           — API tests (Restful-Booker)
```

---

## Part 1: The test automation framework

**Structure:** BasePage → Pages → Flows → Tests. Flows chain multiple pages into real user journeys (login → cart → checkout). Both raw ("noflow") tests and flow-based tests were kept deliberately, showing the progression from basic to abstracted test design.

**API testing:** 2 tests via [Restful-Booker](https://restful-booker.herokuapp.com) (an industry-standard practice API) — a ping health check and a full create/retrieve booking test. SauceDemo has no public API, so this was added as a separate, deliberate piece of coverage.

**Configuration:** all URLs, credentials, and expected error messages are centralized in `config.properties` via a `ConfigReader` utility — no hardcoded values in test code.

### CI/CD — two independent pipelines

**GitHub Actions** (`.github/workflows/ci.yml`) runs `mvn clean test` on every push, headless (GitHub's runners have no display).

**Jenkins** runs locally via Docker (JDK 17), triggered automatically by a GitHub webhook. The Jenkinsfile has three stages (Checkout, Build, Test) plus JUnit result publishing and artifact archiving. The Jenkins container uses Chromium rather than Chrome; `BaseTest` reads `CHROME_BIN`/`CHROMEDRIVER_BIN` environment variables when set (Jenkins), falling back to WebDriverManager's auto-detection otherwise (local, GitHub Actions).

### Reporting

ExtentReports is wired in via a TestNG listener (`ExtentTestListener`), generating `reports/ExtentReport.html` automatically after every run — no changes needed to individual test files. Screenshot-on-failure uses reflection (`getDeclaredField` + `setAccessible(true)`) to grab the `driver` field from whichever test class failed.

### The Chrome popup investigation

A real, recurring issue surfaced during development: Chrome's password leak-detection popup ("your password was found in a data breach") would intermittently appear mid-test, stealing focus and corrupting whatever field was being typed into at that moment — a different field each time, which was the first clue this was a timing/race issue rather than a broken locator.

Attempted fixes, in order: ChromeOptions prefs to disable the password manager and leak detection, `--disable-features=PasswordLeakDetection`, `--incognito` mode, and a defensive "dismiss the popup if present" helper (which didn't work, since it's native browser UI, not part of the page DOM — Selenium can't reliably interact with it). Root cause: this check is partly server-side (Chrome sends a hash of typed credentials to Google's Safe Browsing service), so no local flag can fully guarantee suppression — explaining the inconsistency.

**What actually worked:** Chrome's `--guest` mode, which uses a fully sandboxed profile. Confirmed via repeated manual, visible-browser testing. Along the way, the project also standardized on headless-by-default everywhere (matching how real teams run automated suites) and added Firefox as a switchable backup browser, since it never exhibited the issue at all.

---

## Part 2: The AI-assisted generation pipeline

### Pipeline A — structured extraction and generation

**Stage 1 — extraction.** Selenium fetches the fully-rendered HTML of a live page. jsoup parses it and categorizes every interactive element (inputs, buttons, links). For each element, a priority-ordered strategy picks the best available selector: `data-test`/`data-qa` → `id` → `name` → visible text, in that order — implemented as a list of small strategy methods tried in sequence, rather than a nested if/else chain, so adding a new priority tier later means adding one method, not restructuring existing logic.

**Stage 2 — generation.** The structured selector list is turned into a complete, real, compilable Java Page Object class, written directly to disk. Generated classes extend the same `BasePage` used everywhere else in the framework (reusing `click()`, `type()`, `getText()`, `isDisplayed()`), and live in a dedicated `pages/generated/` package so they're never confused with hand-written, production-reviewed code.

**Stage 3 — AI judgment-call review.** Elements that could only be matched via visible text (the least stable strategy) are flagged as low-confidence and sent — one at a time, not the whole page — to an AI for a focused second opinion: should this element be kept, what methods should it expose, and is there a specific concern worth flagging. The AI's response is required as structured JSON, parsed defensively (handling markdown-fence wrapping, malformed responses, and API failures without crashing the pipeline).

### Real bugs found and fixed along the way

Testing against real, messy HTML (not just clean tutorial examples) surfaced genuine issues, each fixed with a scoped, evidence-based change:

- **Naming collisions from using element type instead of a specific identifier.** Multiple `type="text"` inputs (firstName, lastName, postalCode) all generated the same, colliding Java variable name. Fixed by preferring `data-test` > `id` > `name` > visible text > `type`, in that order, for naming — not just for selector matching.
- **Invalid Java identifiers from real-world punctuation.** A product literally named `test.allTheThings() T-Shirt (Red)` produced attribute values with periods and parentheses, which aren't valid in Java identifiers. Fixed by splitting on any non-alphanumeric character run (`[^a-zA-Z0-9]+`) rather than just hyphens and underscores.
- **A second real site (automationexercise.com) uses `data-qa`, not `data-test`.** The pipeline was hardcoded to one convention. Fixed by making the list of recognized automation attributes configurable (`automation.attributes=data-test,data-qa` in `config.properties`), checked in priority order.
- **Hidden CSRF token fields were being extracted as real interactive elements**, producing genuine, unavoidable duplicate fields (the same token appears once per form on a page). Root-caused correctly: these are server-issued security values, never meant for user interaction. Fixed by filtering out any `<input type="hidden">` during extraction.
- **A duplicate-method generation bug** traced back to two near-identical code blocks in `PageObjectGenerator` — one dead, leftover from an earlier edit — rather than any duplication in the underlying data. Found by explicitly logging element counts at each pipeline stage rather than guessing from symptoms.

### Deterministic rules vs. AI — where the line was drawn

Not every decision was routed to AI. Whether an image-only link should get a `getText()` method, for example, is a clean, binary, rule-based check (empty visible text + `<a>` tag → skip) that doesn't benefit from an API call. AI was reserved specifically for genuine judgment calls without a clean rule — like assessing whether a text-based selector is risky enough to flag for review. Knowing which sub-problems need AI and which are better solved deterministically was a deliberate design choice, not an afterthought.

**Known, deliberately deferred limitation:** true structural duplicates — the same selector legitimately appearing more than once, such as a repeated "Add to Cart" button without a unique per-item attribute — are not automatically scoped to a parent container. Both SauceDemo and automationexercise.com happened to have unique-enough attributes that this rarely mattered in testing, but a production version would need this. Documented here as future work rather than built under time pressure.

### AI backends — Gemini vs. local Llama, compared

Two backends were built and tested against the same 8 low-confidence elements, same prompt:

**Gemini 3.5 Flash (cloud):** caught a specific, concrete defect — a stray Unicode icon character corrupting a navigation link's selector — and correctly flagged it for review. Free tier's 5 requests/minute limit caused real, live rate-limit failures during testing, handled gracefully by falling back to deterministic output rather than crashing; upgraded to a paid tier (~$10) to remove this friction entirely.

**Llama 3.1 8B via Ollama (local, free, no rate limit):** gave plausible, mostly correct, but noticeably more generic feedback, and did not catch the icon-character issue specifically. Both models reliably returned well-formed JSON.

**Honest finding:** the larger cloud model showed better judgment-call specificity than the local 8B model on this task. Also notable: Gemini's own verdict on the same element varied between separate runs (once recommending removal, another time not), despite consistently identifying the same underlying defect — a real, reportable finding about LLM consistency on subjective judgment calls, independent of any bug in the pipeline itself.

### Pipeline B — the naive baseline

For comparison, raw HTML (the entire page, no structuring) was sent directly to an AI in one shot, asking it to generate a complete Page Object with no filtering or extraction beforehand. The prompt gave reasonable, fair guidance (prefer stable attributes, skip hidden inputs) rather than being deliberately unhelpful, to keep the comparison honest.

**Results were genuinely mixed:**

| | SauceDemo | automationexercise.com |
|---|---|---|
| Pipeline A | 28 elements, 0 duplicates, all correctly matched | 17 elements after 3 real fixes; used `linkText` for nav (flagged low-confidence) |
| Pipeline B (naive) | Correctly used `data-test`, but hallucinated 2 methods for non-interactive page-instruction text | Correctly inferred `data-qa` unprompted; used `href`-based selectors for nav — arguably more robust than Pipeline A's own fallback |

The naive baseline was not uniformly worse. On the messier site, it occasionally made better individual selector choices. Pipeline A's real advantages are not "always-better output," but **determinism and traceability** (every selector's origin is explicitly tracked, not a black-box guess), **explicit low-confidence flagging** for human review rather than a silent one-shot answer, and **proven resilience** — the pipeline degrades gracefully on API failure rather than having a single point of failure.

---

## Design decisions worth knowing

- **Headless is the default everywhere** (local, GitHub Actions, Jenkins) — matching standard industry practice, and the fix that ultimately resolved the Chrome popup issue.
- **The AI layer is intentionally human-in-the-loop.** Low-confidence elements are flagged with a concern and confidence score, not silently auto-corrected.
- **Repeated-component detection is deliberately deferred** — a real, acknowledged limitation, not an oversight.
- **Hidden inputs are explicitly filtered** during extraction, since they are not genuine interactive elements.

## Known limitations

- Repeated/duplicate selector components are not automatically scoped to a parent container.
- The naive Pipeline B baseline was tested against two pages — a larger sample would strengthen the comparison further.
- AI judgment-call consistency varies between runs and between models.

## Author

Abdelhafid Idbahamd — ENSET Mohammedia, Master's in Computer Engineering (Big Data & Cloud Computing)