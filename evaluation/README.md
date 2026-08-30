# Evaluation — reproducing the Chapter VI measurements

Everything in this folder recomputes the results reported in Chapter VI from artefacts
committed to this repository. No network access, no live site, no API key, and no model call
is involved, so the measurements remain reproducible after the target sites have changed —
which for a study whose subject is reproducibility is not an incidental property.

## What is measured, and how

| Metric | Method |
|---|---|
| M1 Recall | Interactive elements counted directly on the captured DOM, and compared with what each pipeline addressed. |
| M2 Precision | Every emitted locator evaluated in a real Chromium engine against that DOM. Selenium's scoping rules are reproduced exactly: `driver.findElement` searches the document, `WebElement.findElement` searches the root's **descendants only**. |
| M3 Locator quality | Strategy distribution parsed from the emitted source. |
| M4 Compilability | `javac` against a stub whose `org.openqa.selenium.By` declares exactly the eight static factory methods the real Selenium 4 Java API declares, plus the project's real `BasePage`. |
| M6 Duplication | Redundant fields and methods parsed from the emitted source. |
| M7 Cost, M8 Latency | Read from the `[GEMINI-METRICS]` telemetry lines recorded during the campaign; see `PFE_Evaluation_Results.xlsx`. Not recomputable offline. |
| M9 Determinism | md5 of each artefact after replacing its class name with `X` — the class name being the only intended difference between runs. |

M5 (executability against the live page) cannot be reproduced offline. What is reproduced
instead is **locator resolution against the captured DOM**, which answers the same underlying
question — does this locator address a real element? — without depending on the site still
existing in the form it had on the day of the runs. It is reported under that name, and not
as a live smoke test.

## Running it

```bash
# M1, M2, M3, M6, M9
pip install lxml playwright
python -m playwright install chromium
python3 evaluation/lib/measure.py

# M4
./evaluation/check-compilability.sh

# M9 alone, with nothing but coreutils
for f in generated-output/pipeline-a/*.java; do
  n=$(basename "$f" .java); sed "s/$n/X/g" "$f" | md5sum
done
md5sum generated-output/naive/Pip3Run*.java
```

`results/RESULTS.md` is the captured output of a run, so the numbers quoted in Chapter VI can
be checked without executing anything.

## Inputs

| Path | What it is |
|---|---|
| `../generated-output/page-snapshot/inventory-page.html` | The rendered DOM captured by `PageFetcher` during the campaign, and the exact input both pipelines received. |
| `../generated-output/pipeline-a/` | Pipeline A output, five runs. |
| `../generated-output/naive/` | Pipeline B output, three runs. |
| `results/SauceDemoInventoryPageBody.java` | The same page regenerated after the post-campaign defect fixes, so their effect is measured rather than asserted. |
| `selenium-api-stub/` | The Selenium API surface used for the compilability check. Not part of the build. |

## Scope

These measurements cover page P1 only. The protocol in `PFE_Evaluation_Results.xlsx` defines
six pages; five of them were not run, and the workbook rows for them are empty. Chapter VI
states this limitation explicitly and confines itself to categorical claims, which one page
supports, rather than rates, which it does not.
