#!/usr/bin/env python3
"""
Reproducible measurement of the Chapter VI metrics for page P1
(SauceDemo inventory page, https://www.saucedemo.com/inventory.html).

Every number this script prints is derived from artefacts committed to this repository:

  generated-output/page-snapshot/inventory-page.html   the rendered DOM both pipelines read
  generated-output/pipeline-a/*.java                   Pipeline A output, five runs
  generated-output/naive/Pip3Run*.java                 Pipeline B output, three runs

Nothing here contacts a network or a live site, so the measurements can be reproduced at
any time, including after the target site has changed.

  M1 recall              counted against the DOM snapshot
  M2 precision           each locator evaluated in a real browser engine against the snapshot
  M3 locator quality     strategy distribution, parsed from the emitted source
  M4 compilability       javac against a faithful stub of the Selenium By API (separate script)
  M6 duplication         parsed from the emitted source
  M9 determinism         md5 after class-name normalisation

Usage:  python3 evaluation/lib/measure.py [--json]

Requires: lxml, playwright (with Chromium). See evaluation/README.md.
"""
from __future__ import annotations

import hashlib
import json
import pathlib
import re
import sys
from collections import Counter, OrderedDict

ROOT = pathlib.Path(__file__).resolve().parents[2]
SNAPSHOT = ROOT / "generated-output/page-snapshot/inventory-page.html"
PIPELINE_A = ROOT / "generated-output/pipeline-a"
PIPELINE_B = ROOT / "generated-output/naive"

# The determinism sub-experiment compares runs of the SAME page. homePageBody.java in the
# same folder is a run against a different site and is excluded here by name, not silently.
SAUCEDEMO_PIPELINE_A_RUNS = [PIPELINE_A / n for n in (
    "SauceDemOInventoryPagePIPABody.java",
    "SauceDemoInventoryPagePIPBBody.java",
    "SauceDemoInventoryPagePIPBBodyRun2Body.java",
    "SauceDemoInventoryPagePIPBBodyRun3Body.java",
    "testinglOGINBody.java",
)]

# The eight strategies org.openqa.selenium.By actually declares (Selenium 4 Java API).
REAL_BY_STRATEGIES = {
    "id", "linkText", "partialLinkText", "name", "tagName", "xpath", "className", "cssSelector",
}

BY_CALL = re.compile(
    r'By\.([a-zA-Z]+)\("((?:[^"\\]|\\.)*)"\)'
)
FIELD = re.compile(r'private final By (\w+)\s*=\s*(By\.[^;]+);')
LIST_GETTER = re.compile(
    r'public List<(\w+)> get\w+List\(\)\s*\{\s*return driver\.findElements\(By\.cssSelector\("([^"]+)"\)\)'
)
NESTED_CLASS = re.compile(r'public class (\w+) \{(.*?)\n    \}', re.S)
ROOT_FIND = re.compile(r'public void (\w+)\(\) \{.*?root\.(findElements?)\(')


# ----------------------------------------------------------------------------------
# Ground truth (M1)
# ----------------------------------------------------------------------------------

def ground_truth():
    from lxml import html as LH

    doc = LH.parse(str(SNAPSHOT)).getroot()

    def region(tag, role):
        found = doc.xpath(f"//{tag}")
        if found:
            return found[0]
        found = doc.xpath(f'//*[@role="{role}"]')
        return found[0] if found else None

    footer = region("footer", "contentinfo")
    header = region("header", "banner")

    def counts(scope):
        inputs = [e for e in scope.xpath(".//input") if e.get("type", "").lower() != "hidden"]
        return {
            "input": len(inputs),
            "button": len(scope.xpath(".//button")),
            "a": len(scope.xpath(".//a")),
            "select": len(scope.xpath(".//select")),
        }

    page = counts(doc)

    # Reproduce the pipeline's own scoping: remove header and footer, body is what remains.
    doc2 = LH.parse(str(SNAPSHOT)).getroot()
    for tag, role in (("header", "banner"), ("footer", "contentinfo")):
        found = doc2.xpath(f"//{tag}") or doc2.xpath(f'//*[@role="{role}"]')
        if found:
            found[0].getparent().remove(found[0])
    main = doc2.xpath("//main")
    body_scope = main[0] if main else doc2
    body = counts(body_scope)

    return {
        "page": page,
        "page_total": sum(page.values()),
        "page_parser_visible": page["input"] + page["button"] + page["a"],
        "header_present": header is not None,
        "footer": counts(footer) if footer is not None else None,
        "body": body,
        "body_total": sum(body.values()),
        "body_parser_visible": body["input"] + body["button"] + body["a"],
    }


# ----------------------------------------------------------------------------------
# Locator extraction (M2, M3, M6)
# ----------------------------------------------------------------------------------

def _balanced(src: str, open_idx: int):
    """Return the parenthesised argument starting at open_idx, respecting string literals.

    A naive [^)]+ scan truncates locators that legitimately contain ')' — a:not([class]),
    normalize-space(.) — and silently mis-scopes them."""
    depth, i, in_string, escaped = 0, open_idx, False, False
    while i < len(src):
        c = src[i]
        if in_string:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif c == '"':
                in_string = False
        else:
            if c == '"':
                in_string = True
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
                if depth == 0:
                    return src[open_idx:i + 1]
        i += 1
    return None


def to_evaluable(strategy: str, value: str):
    """Map a By strategy onto something a browser can evaluate.

    By.id and By.name become attribute selectors rather than '#id' / fragment forms: an id
    may legally contain '.' or '(' — SauceDemo ships one — which is not a valid CSS id
    fragment even though By.id remains well defined."""
    value = value.replace('\\"', '"').replace("\\\\", "\\")
    if strategy not in REAL_BY_STRATEGIES:
        return None
    if strategy == "cssSelector":
        return ("css", value)
    if strategy == "xpath":
        return ("xpath", value)
    if strategy == "id":
        return ("css", '[id="%s"]' % value.replace('"', '\\"'))
    if strategy == "name":
        return ("css", '[name="%s"]' % value.replace('"', '\\"'))
    if strategy == "className":
        return ("css", "." + value)
    if strategy == "tagName":
        return ("css", value)
    if strategy == "linkText":
        return ("xpath", "//a[normalize-space(.)='%s']" % value)
    if strategy == "partialLinkText":
        return ("xpath", "//a[contains(.,'%s')]" % value)
    return None


def extract_locators(path: pathlib.Path, source_label: str):
    src = path.read_text()
    locators, invalid = [], []
    roots = {cls: sel for cls, sel in LIST_GETTER.findall(src)}

    consumed = []

    # Component-role locators, scoped to a card root.
    for cls, body in NESTED_CLASS.findall(src):
        if cls not in roots:
            continue
        for m in re.finditer(r'public void (\w+)\(\) \{', body):
            tail = body[m.end():body.find("\n        }", m.end())]
            call = re.search(r'root\.(findElements?)\(', tail)
            if not call:
                continue
            arg = _balanced(tail, call.end() - 1)
            if arg is None:
                continue
            consumed.append(arg)
            by = BY_CALL.search(arg)
            if not by:
                continue
            index = None
            if call.group(1) == "findElements":
                idx = re.search(r'\)\)\.get\((\d+)\)', tail)
                index = int(idx.group(1)) if idx else None
            ev = to_evaluable(by.group(1), by.group(2))
            entry = {
                "source": source_label, "name": f"{cls}.{m.group(1)}", "kind": "component-role",
                "strategy": by.group(1), "rootSelector": roots[cls], "index": index,
            }
            if ev is None:
                invalid.append(entry | {"value": by.group(2)})
            else:
                locators.append(entry | {"type": ev[0], "value": ev[1]})

        # Roles that act on the root element itself always resolve by construction.
        for m in re.finditer(r'public void (\w+)\(\) \{\s*(?://[^\n]*\n\s*)*WebElement el = root;', body):
            locators.append({
                "source": source_label, "name": f"{cls}.{m.group(1)}", "kind": "component-role",
                "strategy": "root", "rootSelector": roots[cls], "type": "css",
                "value": roots[cls], "actsOnRoot": True, "index": None,
            })

    # Top-level By fields.
    for fname, expr in FIELD.findall(src):
        by = BY_CALL.search(expr)
        if not by:
            continue
        ev = to_evaluable(by.group(1), by.group(2))
        entry = {"source": source_label, "name": fname, "kind": "field", "strategy": by.group(1)}
        if ev is None:
            invalid.append(entry | {"value": by.group(2)})
        else:
            locators.append(entry | {"type": ev[0], "value": ev[1]})

    # By.* built inline inside a method body rather than assigned to a field.
    stripped = src
    for arg in consumed:
        stripped = stripped.replace(arg, "", 1)
    stripped = FIELD.sub("", stripped)
    stripped = re.sub(r'return driver\.findElements\(By\.cssSelector\("[^"]+"\)\)', "", stripped)
    for m in BY_CALL.finditer(stripped):
        context = stripped[:m.start()].split("public ")[-1].split("(")[0].split()
        where = context[-1] if context else "?"
        ev = to_evaluable(m.group(1), m.group(2))
        entry = {"source": source_label, "name": f"inline in {where}()", "kind": "inline",
                 "strategy": m.group(1)}
        if ev is None:
            invalid.append(entry | {"value": m.group(2)})
        else:
            locators.append(entry | {"type": ev[0], "value": ev[1]})

    return locators, invalid


# ----------------------------------------------------------------------------------
# Resolution against the snapshot (M2 / M5-by-proxy)
# ----------------------------------------------------------------------------------

JS_DOC_CSS = "(sel) => document.querySelectorAll(sel).length"
JS_DOC_XPATH = """(xp) => document.evaluate(
    xp, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null).snapshotLength"""
JS_ROOT_CSS = """({rootSel, sel}) => [...document.querySelectorAll(rootSel)]
    .map(r => r.querySelectorAll(sel).length)"""
JS_ROOT_XPATH = """({rootSel, xp}) => [...document.querySelectorAll(rootSel)].map(r =>
    document.evaluate(xp, r, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null).snapshotLength)"""


def resolve(locators):
    """Evaluate every locator in a real browser engine.

    Selenium semantics are reproduced exactly: driver.findElement searches the document,
    WebElement.findElement searches the root's DESCENDANTS only (never the root itself)."""
    from playwright.sync_api import sync_playwright

    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page()
        page.goto(SNAPSHOT.as_uri())
        for item in locators:
            if item.get("actsOnRoot"):
                n = page.evaluate(JS_DOC_CSS, item["rootSelector"])
                item["perInstanceMatches"] = [1] * n
            elif item.get("rootSelector"):
                js = JS_ROOT_CSS if item["type"] == "css" else JS_ROOT_XPATH
                key = "sel" if item["type"] == "css" else "xp"
                item["perInstanceMatches"] = page.evaluate(
                    js, {"rootSel": item["rootSelector"], key: item["value"]})
            else:
                item["matches"] = page.evaluate(
                    JS_DOC_CSS if item["type"] == "css" else JS_DOC_XPATH, item["value"])

            if "perInstanceMatches" in item:
                counts = item["perInstanceMatches"]
                need = (item["index"] + 1) if item.get("index") is not None else 1
                item["instances"] = len(counts)
                item["instancesResolving"] = sum(1 for c in counts if c >= need)
                item["resolves"] = bool(counts) and item["instancesResolving"] == len(counts)
                item["ambiguous"] = any(c > 1 for c in counts) and item.get("index") is None
            else:
                item["resolves"] = item["matches"] >= 1
                item["ambiguous"] = item["matches"] > 1
        browser.close()
    return locators


# ----------------------------------------------------------------------------------
# Determinism (M9)
# ----------------------------------------------------------------------------------

def determinism(paths):
    rows = []
    for path in paths:
        raw = path.read_bytes()
        class_name = path.stem
        normalised = raw.decode().replace(class_name, "X").encode()
        rows.append({
            "file": path.name,
            "md5_raw": hashlib.md5(raw).hexdigest(),
            "md5_normalised": hashlib.md5(normalised).hexdigest(),
            "lines": raw.decode().count("\n"),
            "by_fields": len(FIELD.findall(raw.decode())),
            # Constructor + methods; the class declaration itself is not a member.
            "public_members": len(re.findall(r'\n\s*public (?!class\b)', raw.decode())),
            "package": (re.search(r'package ([\w.]+);', raw.decode()) or [None, "?"])[1],
        })
    return rows


# ----------------------------------------------------------------------------------
# Report
# ----------------------------------------------------------------------------------

def main():
    gt = ground_truth()

    a_files = {"SauceDemoInventoryPagePIPBBody.java": "Pipeline A run 1",
               "SauceDemoInventoryPagePIPBBodyRun2Body.java": "Pipeline A run 2",
               "SauceDemoInventoryPagePIPBBodyRun3Body.java": "Pipeline A run 3"}
    b_files = {"Pip3Run1.java": "Pipeline B run 1",
               "Pip3Run2.java": "Pipeline B run 2",
               "Pip3Run3.java": "Pipeline B run 3"}

    all_locators, all_invalid = [], []
    for fname, label in a_files.items():
        loc, inv = extract_locators(PIPELINE_A / fname, label)
        all_locators += loc
        all_invalid += inv
    for fname, label in b_files.items():
        loc, inv = extract_locators(PIPELINE_B / fname, label)
        all_locators += loc
        all_invalid += inv

    # The same page regenerated after the post-campaign defect fixes, so the effect of those
    # fixes on locator resolution is measured rather than asserted.
    fixed = ROOT / "evaluation/results/SauceDemoInventoryPageBody.java"
    if fixed.exists():
        loc, inv = extract_locators(fixed, "Pipeline A after fixes")
        all_locators += loc
        all_invalid += inv

    resolve(all_locators)

    # Raw By.dataTest occurrences, including concatenated arguments a literal-only scan misses.
    hallucinated_calls = {}
    for fname, label in b_files.items():
        text = (PIPELINE_B / fname).read_text()
        hallucinated_calls[label] = len(re.findall(r'By\.dataTest\(', text))

    report = {
        "ground_truth": gt,
        "determinism_pipeline_a": determinism(SAUCEDEMO_PIPELINE_A_RUNS),
        "determinism_pipeline_b": determinism(sorted(PIPELINE_B.glob("Pip3Run*.java"))),
        "other_pipeline_a_artefacts": determinism(
            [p for p in sorted(PIPELINE_A.glob("*.java")) if p not in SAUCEDEMO_PIPELINE_A_RUNS]),
        "locators": all_locators,
        "invalid_api_locators": all_invalid,
        "hallucinated_By_dataTest_calls": hallucinated_calls,
    }

    if "--json" in sys.argv:
        print(json.dumps(report, indent=2))
        return

    print("=" * 78)
    print("P1 — SauceDemo inventory page. All values derived from committed artefacts.")
    print("=" * 78)

    print("\n--- M1  Ground truth, counted on the DOM snapshot ---")
    print(f"  whole page : {gt['page']}  -> {gt['page_total']} interactive elements")
    print(f"  <header>   : {'present' if gt['header_present'] else 'ABSENT — no header class is generated'}")
    print(f"  <footer>   : {gt['footer']}")
    print(f"  body scope : {gt['body']}  -> {gt['body_total']} interactive, "
          f"{gt['body_parser_visible']} of the kinds the parser recognises")

    print("\n--- M9  Determinism (md5 after replacing the class name with 'X') ---")
    for label, rows in (("Pipeline A", report["determinism_pipeline_a"]),
                        ("Pipeline B", report["determinism_pipeline_b"])):
        digests = {r["md5_normalised"] for r in rows}
        print(f"  {label}: {len(rows)} runs of the same page, {len(digests)} distinct digest(s) — "
              f"{'BYTE-IDENTICAL' if len(digests) == 1 else 'EVERY RUN DIFFERS'}")
        print(f"      {'file':<44} {'md5 raw':<34}{'md5 class-name normalised':<34}"
              f"lines  By  members  package")
        for r in rows:
            print(f"      {r['file']:<44} {r['md5_raw']:<34}{r['md5_normalised']:<34}"
                  f"{r['lines']:<7}{r['by_fields']:<4}{r['public_members']:<9}{r['package']}")

    others = report["other_pipeline_a_artefacts"]
    if others:
        print("\n  Other Pipeline A artefacts (different pages, not part of this sub-experiment):")
        for r in others:
            print(f"      {r['file']:<44} {r['md5_normalised']}  lines={r['lines']:<4} By={r['by_fields']}")

    print("\n--- Hallucinated API calls (By.dataTest does not exist in org.openqa.selenium.By) ---")
    for label, n in hallucinated_calls.items():
        print(f"  {label}: {n} call(s)")

    print("\n--- M2  Locator resolution, evaluated in Chromium against the snapshot ---")
    by_source = OrderedDict()
    for item in all_locators:
        by_source.setdefault(item["source"], []).append(item)
    for source, items in by_source.items():
        ok = sum(1 for i in items if i["resolves"])
        invalid_here = [i for i in all_invalid if i["source"] == source]
        note = f"   (+{len(invalid_here)} locators using a non-existent API — artefact does not compile)" \
            if invalid_here else ""
        print(f"\n  {source}: {ok}/{len(items)} resolve{note}")
        for i in items:
            mark = "ok  " if i["resolves"] else "FAIL"
            extra = ""
            if "perInstanceMatches" in i:
                extra = f"  [{i['instancesResolving']}/{i['instances']} instances; matches {i['perInstanceMatches']}]"
            elif i.get("ambiguous"):
                extra = f"  [matches {i['matches']} nodes]"
            print(f"    {mark} {i['name']:<42} {i['strategy']}{extra}")

    print("\n--- M3  Locator strategy distribution ---")
    for source, items in by_source.items():
        counter = Counter(i["strategy"] for i in items)
        invalid_here = Counter(i["strategy"] for i in all_invalid if i["source"] == source)
        print(f"  {source}: {dict(counter)}"
              + (f"  + non-existent: {dict(invalid_here)}" if invalid_here else ""))


if __name__ == "__main__":
    main()
