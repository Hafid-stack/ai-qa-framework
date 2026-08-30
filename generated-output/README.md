# Experimental artefacts

Raw, unmodified output from the two generation pipelines, retained as the evidence base for
Chapter VI. Nothing here is compiled: these files are measurements, not code. Keeping them
outside `src/main/java` is what stops a non-compiling model response from breaking the build
(NFR-5) and keeps machine-produced output visibly separate from reviewed code (NFR-4).

## `pipeline-a/` — structured deterministic generation

Five runs against the SauceDemo inventory page (Body scope), 31 July 2026.

| File | Run |
|---|---|
| `SauceDemOInventoryPagePIPABody.java` | preliminary |
| `SauceDemoInventoryPagePIPBBody.java` | determinism run 1 |
| `SauceDemoInventoryPagePIPBBodyRun2Body.java` | determinism run 2 |
| `SauceDemoInventoryPagePIPBBodyRun3Body.java` | determinism run 3 |
| `testinglOGINBody.java` | additional run (the class name was typed at the prompt and does not describe the page; the page was the SauceDemo inventory page) |

`homePageBody.java` is a separate run against `automationexercise.com`. It is retained because
it is the source of the naming examples quoted in Chapter VI — `click6Polo()`, `getText5HM()` —
and because every one of its locators fell back to a text match, which is the opposite end of
the confidence scale from the SauceDemo page.

The five SauceDemo runs are byte-identical once the class name is normalised. To reproduce:

```bash
for f in generated-output/pipeline-a/*.java; do
  n=$(basename "$f" .java); sed "s/$n/X/g" "$f" | md5sum
done
# every line: 420c015aece99c3da5f12330a49934a0
```

## `naive/` — Pipeline B baseline

Three consecutive runs, same page, same session, identical prompt.

```bash
md5sum generated-output/naive/Pip3Run*.java
# 19ea0a03e15f29a27597d1037a04cb89  Pip3Run1.java
# dedd5d2156b53a5b8d5400a90aa4fecc  Pip3Run2.java
# 65662d40ba9231f22536578f36ffcdcb  Pip3Run3.java
```

`Pip3Run1` and `Pip3Run2` do not compile: both call `By.dataTest(...)`, which is not a method
of `org.openqa.selenium.By`.

## `page-snapshot/`

`inventory-page.html` is the rendered DOM both pipelines consumed, captured by `PageFetcher`
during the campaign. It is the input against which every measurement in `evaluation/` is
reproducible without network access.
