# What is done, and what only you can do

## Done — no action needed

**Software**
- `data-qa` sites no longer receive an unmatchable `[data-test='…']` locator; the attribute name travels with its value.
- Component roles whose card *is* the interactive element are addressed through the root instead of searched inside it (this was failing on 100 % of instances).
- Page text is now escaped for both languages it lands in — the Java string literal and the XPath expression — including the both-quote-kinds case, which falls back to `concat()`.
- Component role naming is invariance-aware: text that is identical across every detected instance names a role; text that varies names nothing and the element is addressed by position. This is the remedy Chapter VI proposed for its own defect, now implemented.
- `By.id`/attribute selectors used in place of `#id` fragments, so ids containing `.` or `(` — SauceDemo ships one — cannot produce an invalid selector.
- Menu option 2 now reviews the same extraction option 1 emits (it previously reviewed a different, flat one).
- `gson` declared explicitly in `pom.xml` instead of arriving transitively.
- 37 offline unit tests over extraction, locator selection and code emission. Each defect above has a regression test named after the property it violated. All 37 pass.
- CI split into a fast deterministic gate (`testng-offline.xml`) and the browser-driven suite, so a red build tells you which kind of failure it is.
- Generated experiment artefacts moved out of `src/main/java` into `generated-output/`, where evidence belongs and where it cannot affect the build.

**Evaluation**
- Every number in Chapter VI re-derived from the raw artefacts. All of them reproduce; the digests, line counts, field counts and token counts are exact.
- Recall and precision, which had never been measured, now are — counted against the captured DOM.
- Locator resolution measured in a real browser engine for all six artefacts, plus the corrected output (5/7 → 7/7).
- Compilability re-verified against a stub of the real Selenium `By` API (A: 9/9, B: 1/3).
- Cost converted to currency from Google's published price.
- `evaluation/` holds scripts that recompute all of this offline; `evaluation/results/RESULTS.md` is a captured run.
- Workbook filled for P1 and extended with defects D8–D12.

**Report**
- 22 real references added, all verified against the source; `Bibliography` chapter added; the "Annex A — Bibliography checklist" to-do list removed.
- All 19 `⟦REF⟧` markers replaced with citations.
- Figures renumbered into document order (they ran 1, 2, 3, 7, 5, 6, 8, 4, 9, 10, 11).
- All 18 tables captioned and numbered.
- Table of Contents, List of Figures and List of Tables added as real Word fields.
- New section VI.8 reporting the post-campaign audit, including the defect that favours the baseline.
- New section V.3.7 on testing the generator, which removes the "no unit tests" limitation.
- `NFR-20` → `FR-20`; `Section VI.10` → `VI.11`; "Methods generated" relabelled to what was actually counted.
- Chapter II repositioned against Karagöz et al. (2026), the closest published work — found during the literature search the report asked for.
- Abstract and résumé now report the audit's findings in both directions.

## Only you can do these

1. **Title page** — defence date, your full name, your supervisor's name(s), and the ENSET logo.
2. **Dédicace** — two to six lines.
3. **Remerciements** — ENSET administration and faculty, your supervisor by name, the jury, your family, your promotion.
4. **Host organisation** (§I.2) — if there was one, describe it; if the work was purely academic, say so explicitly. A stated context beats an absent one.
5. **Personal assessment** (end of the general conclusion) — four to six sentences in your own voice. Write it last, write it yourself; a jury reads this paragraph closely and it must not sound like the rest of the report.
6. **Three screenshots** — the ExtentReports run, a green GitHub Actions run, the Jenkins pipeline view. Note that the retained ExtentReport (15 July, 11 tests) shows no failures, so if you want the failure-handling screenshot the report describes, re-run with one assertion deliberately inverted.
7. **Update the three field lists** — open the report in Word, select all, press F9. The document is flagged to refresh fields on open, but confirm it worked before printing.

## Three small housekeeping steps on your machine

1. **Delete `_to_delete/`.** It holds the six generated experiment artefacts that moved out of `src/main/java/pages/generated/` into `generated-output/pipeline-a/`, plus the transfer archive. I can write to your disk but not delete from it, so they were moved there rather than removed. Check the contents, then delete the folder.
2. **Move `ci.yml.updated` into place**: `mv ci.yml.updated .github/workflows/ci.yml`. That directory is write-protected for remote tools, so the new workflow could not be written directly.
3. **Make the evaluation script executable if git drops the bit**: `chmod +x evaluation/check-compilability.sh`.

## Two things to check before you hand anything in

- **`.env` is in your project folder and holds live `OPENAI_API_KEY` and `GEMINI_API_KEY` values.** It is correctly git-ignored and was never committed, but exclude it from any archive you submit, and rotate both keys after the defence.
- **Run `mvn -B test -Dsurefire.suiteXmlFiles=testng-offline.xml` once on your machine.** The 37 unit tests were executed and pass here, but this environment could not reach Maven Central, so they have not run against the real jsoup, TestNG and Selenium jars. That single command confirms it.

## Two questions a jury is likely to ask

**"Your evaluation covers one page. Why should I believe any of it?"**
The findings that rest on one page are categorical, not statistical: the baseline is non-deterministic, it invoked a Selenium method that does not exist, and the structured pipeline escalated nothing and so cost nothing. Each is established by direct observation and would not be strengthened by more pages. The report says exactly this, and says explicitly that "compiles one time in three" describes those three runs and is not a rate. Section VI.11 is where you point.

**"You designed the metrics, built one of the systems, and did the measuring."**
Acknowledged in §III.6.5, and answered by three things you can point at: the metric set was fixed before the campaign; the audit in §VI.8 reports a defect in your own pipeline that is worse than anything you reported in the baseline, and a result that favours the baseline; and every artefact is retained with scripts that let anyone recompute the numbers offline.
