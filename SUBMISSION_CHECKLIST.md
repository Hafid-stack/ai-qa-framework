# Submission checklist

Defence: **25 September 2026**. Updated 14 September 2026.

---

## 1. Only you can do these

| # | What | Where | Notes |
|---|------|-------|-------|
| 1 | **Your full name** | report cover, and the footer of all 50 slides | Currently `⟦ VOTRE PRÉNOM ET NOM ⟧` / `[ YOUR NAME ]`. Send it to me and I'll do every occurrence in one pass. |
| 2 | **Supervisor's name** | report cover, Remerciements, §I.2, slide 1 | Currently `⟦ NOM DE VOTRE ENCADRANT ⟧`. Four places in the report, one on the slides. |
| 3 | **Jury members** | slide 1 | Optional — if you don't know them, I'll remove the block rather than leave placeholders. |
| 4 | **Screenshot 1** — ExtentReports | §V.2.5 | Run `mvn -B clean test`, open `reports/ExtentReport.html`, screenshot the summary. For a failure screenshot, invert one assertion and re-run. |
| 5 | **Screenshot 2** — GitHub Actions | §V.4 | A green run on github.com/Hafid-stack/ai-qa-framework → Actions. |
| 6 | **Screenshot 3** — Jenkins | §V.4 | `docker start jenkins`, then the pipeline view with the three stages. |
| 7 | **Personal assessment** | end of General Conclusion | Four to six sentences in your own voice. A draft is below — rewrite it, don't submit mine. |

Send me 1–3 and I'll apply them. For 4–6, send me the image files and I'll place them under the
right captions.

---

## 2. Draft personal assessment — rewrite this in your own words

> I began this project able to write Selenium tests but without much sense of why test suites
> decay, and I finish it believing the answer is almost entirely about locators. The part I
> found hardest was not writing the parser but diagnosing what it produced: the browser's
> password dialog that stole focus mid-test was invisible to Selenium because it is not part
> of the page, and I spent far longer on that than on any code I wrote. What I would do
> differently is test the generator itself from the beginning rather than testing only what it
> generated — two of the defects in Chapter VI were sitting in output I had read and approved.
> If I continued this work I would send the model the structured skeleton rather than raw
> markup, because the one thing the measurements show it is genuinely good at is naming, and
> that is the cheapest thing to ask it for.

Every claim in that paragraph corresponds to something that actually happened in the project,
so it is safe ground — but a jury reads this paragraph closely and it should sound like you.

---

## 3. Done — no action needed

**Document**
- [x] A4, 2.5 cm margins (the file previously specified no page size at all)
- [x] Page numbers in the footer, suppressed on the cover
- [x] Table of Contents, List of Figures, List of Tables — all three built as real text with
      real page numbers. **All 125 entries verified against the rendered PDF.**
- [x] Cover: ENSET logo, "Master d'Université", "Ingénierie Informatique – Big Data et Cloud
      Computing (II-BDCC)", new title, defence date
- [x] Dédicace and Remerciements written
- [x] §I.2 academic-context paragraph written
- [x] All 12 figures and 23 tables referenced in the prose (there were zero references before)
- [x] Figures 1, 4 and 7 redrawn — Figure 7 had been 13.4 in tall, taller than an A4 page

**Academic**
- [x] **§VI.10 — Experiment E2**, the escalation experiment, with Tables 18–19 and Figure 12
- [x] RQ2 answer rewritten on real evidence
- [x] Abstract, Résumé, threats-to-validity and both conclusions updated to match
- [x] Bibliography renumbered so "order of first citation" is actually true
- [x] Reference [7] updated from arXiv preprint to the ICST 2026 conference paper
- [x] Defects D1–D13 all defined; Appendix C consolidates them
- [x] Appendix A — requirements traceability, every FR/NFR traced to a file and a test
- [x] Appendix B — the 49-test suite, class by class
- [x] §V.6 — security and secret management
- [x] FR-6 / NFR-7 credential contradiction resolved

**Slides**
- [x] Title, degree, filière, defence date
- [x] Slide 39 rebuilt on Experiment E2 — numbers, narrative and chart
- [x] Slide 42 RQ2 line corrected
- [x] Speaker notes for both rewritten

**Repo**
- [x] `.gitignore` rewritten; `.env` still ignored and still uncommitted
- [ ] Run the `git rm --cached` block (see §5) to stop pushing 6 MB of documents

---

## 4. Security — do this after the defence

`.env` in the project folder holds **live** `OPENAI_API_KEY` and `GEMINI_API_KEY` values.

- It is git-ignored and has never been committed — the repository history contains no key.
- **Do not** include `.env` in anything you hand in.
- **Rotate both keys after the defence**, since the folder will have been copied around.

---

## 5. Repo cleanup

```
cd ~/PFE-QA-AI/ai-qa-framework/ai-qa-framework
git rm --cached PFE_Report.docx PFE_Report.pdf PFE_Defence.pptx PFE_Defence.pdf \
                PFE_Cover_Page.docx PFE_Cover_Page.pdf PFE_Evaluation_Results.xlsx
git add -A
git commit -m "Untrack submission deliverables; add Experiment E2 evaluation artefacts"
git push
```

`--cached` removes them from git's tracking only. The files stay on your disk.

---

## 6. Checking the project still works

```
mvn -B clean test
```
49 tests, 0 failures. That is the authoritative check.

```
mvn -B test -Dsurefire.suiteXmlFiles=testng-offline.xml
```
37 tests, no browser, no network — a few seconds. Note it is `-Dsurefire.suiteXmlFiles`, not
`-DsuiteXmlFile`, which is silently ignored.

```
bash evaluation/run-escalation-experiment.sh
```
Re-runs Experiment E2 end to end. Costs a few cents of Gemini credit.

---

## 7. What the folders are

| Folder | What it is |
|--------|-----------|
| `src/main/java` | The framework and the generation pipeline — the project itself |
| `src/test/java` | 49 tests: 37 for the pipeline, 9 UI, 2 API, 1 end-to-end on generated page objects |
| `evaluation/` | Scripts that recompute every number in Chapter VI offline, the Selenium API stub used to check compilability, and the Experiment E2 runner with its results |
| `generated-output/` | Raw experimental evidence: what each pipeline actually emitted, and the captured page both were given. Kept outside `src/` so unvalidated model output can never break the build |
| `reports/` | ExtentReports HTML, regenerated on every run (git-ignored) |
| `target/` | Maven build output (git-ignored) |
| `page_dump.html` | Debug dump the CLI writes on every run (git-ignored) |
