#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# PFE evaluation — Experiment E2: confidence-gated escalation (RQ2)
#
# Purpose
#   The main campaign evaluated one page (SauceDemo inventory). That page is
#   fully instrumented with data-test attributes, so the deterministic layer
#   found a stable locator for every element and escalated nothing. The
#   escalation mechanism was therefore never exercised by a measurement.
#
#   This script exercises it. It runs Pipeline A with AI review (option 2) and
#   Pipeline B (option 3) over the same set of automationexercise.com pages —
#   a deliberately messier, real-world site — and captures the full console
#   output of every run, including the [GEMINI-METRICS] telemetry lines that
#   record prompt tokens, output tokens and latency for each model call.
#
#   The comparison it produces, per page:
#       Pipeline A : N low-confidence elements -> N small model calls -> T_a tokens
#       Pipeline B : 1 whole-page call                                -> T_b tokens
#   Across pages of differing size and differing ambiguity, this is the
#   evidence RQ2 requires.
#
# Usage
#   bash evaluation/run-escalation-experiment.sh
#
# It writes everything under evaluation/results/escalation-<timestamp>/ and
# changes nothing else in the repository. Generated .java files are moved out
# of src/main/java/pages/generated/ as soon as they are produced, so the build
# tree is left exactly as it was found.
# ---------------------------------------------------------------------------

set -uo pipefail

# Repository root = parent of the directory holding this script.
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 1
ROOT="$(pwd)"
echo "Repository root: $ROOT"

# ---------------------------------------------------------------------------
# 1. Load the API key.
#
# .env in this project is written as "GEMINI_API_KEY =value" — note the space
# before the '=' — and nothing in the Java code reads .env, so the key has to
# be exported into the environment here. GeminiClient reads it with
# System.getenv("GEMINI_API_KEY").
# ---------------------------------------------------------------------------
if [ -f .env ]; then
  while IFS= read -r line || [ -n "$line" ]; do
    case "$line" in ''|\#*) continue ;; esac
    case "$line" in *=*) : ;; *) continue ;; esac
    key="${line%%=*}"
    val="${line#*=}"
    key="$(printf '%s' "$key" | tr -d '[:space:]')"
    val="$(printf '%s' "$val" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' -e 's/^"//' -e 's/"$//')"
    [ -n "$key" ] && export "$key=$val"
  done < .env
fi

if [ -z "${GEMINI_API_KEY:-}" ]; then
  echo "ERROR: GEMINI_API_KEY is not set and could not be read from .env"
  exit 1
fi
echo "GEMINI_API_KEY loaded (${#GEMINI_API_KEY} characters)."

# ---------------------------------------------------------------------------
# 2. Output directory.
# ---------------------------------------------------------------------------
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="evaluation/results/escalation-$STAMP"
mkdir -p "$OUT"
echo "Output directory: $OUT"

# ---------------------------------------------------------------------------
# 3. Build once, and resolve the runtime classpath.
#
# cli.Main is run with plain `java` rather than `mvn exec:java` for two
# reasons: exec-maven-plugin is not declared in pom.xml (so it would be
# resolved from the network on first use), and piping answers into an
# interactive Scanner is more reliable outside the Maven JVM.
# ---------------------------------------------------------------------------
echo
echo "=== Compiling ==="
mvn -q -B -DskipTests compile || { echo "ERROR: compile failed"; exit 1; }

echo "=== Resolving classpath ==="
mvn -q -B dependency:build-classpath -Dmdep.outputFile=target/cp.txt \
  || { echo "ERROR: could not resolve the classpath"; exit 1; }
CP="target/classes:$(cat target/cp.txt)"

GEN_DIR="src/main/java/pages/generated"
mkdir -p "$GEN_DIR"

# ---------------------------------------------------------------------------
# 4. Run helpers.
#
# cli.Main prompts, in order:
#     option (1/2/3), URL, class name, "requires login?" (y/n)
# then, for option 2 only: "Gemini (g) or Ollama (o)?"
# and,  for option 1 only: section choice (b/a/h/f)
# ---------------------------------------------------------------------------

# run_pipeline_a_with_review <label> <url> <classname>
run_pipeline_a_with_review() {
  local label="$1" url="$2" cls="$3"
  local log="$OUT/${label}-pipelineA-review.log"
  echo
  echo "=== [$label] Pipeline A + AI review of low-confidence selectors ==="
  echo "    URL: $url"
  printf '2\n%s\n%s\nn\ng\n' "$url" "$cls" \
    | java -cp "$CP" cli.Main 2>&1 | tee "$log"
  # Move the generated artefact out of the compiled source tree.
  if [ -f "$GEN_DIR/$cls.java" ]; then
    mv "$GEN_DIR/$cls.java" "$OUT/${label}-pipelineA-$cls.java"
    echo "    generated artefact moved to $OUT/${label}-pipelineA-$cls.java"
  fi
  # page_dump.html is overwritten on every run; keep this page's copy.
  [ -f page_dump.html ] && cp page_dump.html "$OUT/${label}-page.html"
}

# run_pipeline_b <label> <url> <classname>
run_pipeline_b() {
  local label="$1" url="$2" cls="$3"
  local log="$OUT/${label}-pipelineB-naive.log"
  echo
  echo "=== [$label] Pipeline B — naive whole-HTML baseline ==="
  echo "    URL: $url"
  printf '3\n%s\n%s\nn\n' "$url" "$cls" \
    | java -cp "$CP" cli.Main 2>&1 | tee "$log"
  # Pipeline B already writes outside the source tree, into generated-output/naive/.
  if [ -f "generated-output/naive/$cls.java" ]; then
    cp "generated-output/naive/$cls.java" "$OUT/${label}-pipelineB-$cls.java"
  fi
}

# ---------------------------------------------------------------------------
# 5. The evaluation set.
#
# All four are public pages on automationexercise.com requiring no login. They
# are chosen to vary along the two axes RQ2 concerns: total page size, and
# the number of elements for which no stable attribute exists.
# ---------------------------------------------------------------------------
run_pipeline_a_with_review "P2-home"     "https://automationexercise.com/"           "EvalHomeP2"
run_pipeline_b             "P2-home"     "https://automationexercise.com/"           "EvalHomeP2Naive"

run_pipeline_a_with_review "P3-products" "https://automationexercise.com/products"   "EvalProductsP3"
run_pipeline_b             "P3-products" "https://automationexercise.com/products"   "EvalProductsP3Naive"

run_pipeline_a_with_review "P4-login"    "https://automationexercise.com/login"      "EvalLoginP4"
run_pipeline_b             "P4-login"    "https://automationexercise.com/login"      "EvalLoginP4Naive"

run_pipeline_a_with_review "P5-contact"  "https://automationexercise.com/contact_us" "EvalContactP5"
run_pipeline_b             "P5-contact"  "https://automationexercise.com/contact_us" "EvalContactP5Naive"

# ---------------------------------------------------------------------------
# 6. Summary extracted from the logs.
# ---------------------------------------------------------------------------
SUMMARY="$OUT/SUMMARY.txt"
{
  echo "Experiment E2 — confidence-gated escalation"
  echo "Run at: $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
  echo "Model:  gemini-3.1-flash-lite"
  echo
  for log in "$OUT"/*-pipelineA-review.log; do
    [ -f "$log" ] || continue
    label="$(basename "$log" -pipelineA-review.log)"
    total="$(grep -m1 '^Total selectors:' "$log" | sed 's/[^0-9]*//')"
    low="$(grep -m1 '^Low-confidence selectors to review:' "$log" | sed 's/[^0-9]*//')"
    calls="$(grep -c '\[GEMINI-METRICS\]' "$log")"
    ptok="$(grep -o 'promptTokens=[0-9]*' "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    otok="$(grep -o 'outputTokens=[0-9]*' "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    ttok="$(grep -o 'totalTokens=[0-9]*'  "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    ms="$(grep -o 'elapsedMs=[0-9]*' "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    echo "PIPELINE A  $label"
    echo "  total selectors        : ${total:-?}"
    echo "  low-confidence (gated) : ${low:-?}"
    echo "  model calls made       : $calls"
    echo "  prompt tokens (sum)    : $ptok"
    echo "  output tokens (sum)    : $otok"
    echo "  total tokens  (sum)    : $ttok"
    echo "  model time    (sum ms) : $ms"
    echo
  done
  for log in "$OUT"/*-pipelineB-naive.log; do
    [ -f "$log" ] || continue
    label="$(basename "$log" -pipelineB-naive.log)"
    calls="$(grep -c '\[GEMINI-METRICS\]' "$log")"
    ptok="$(grep -o 'promptTokens=[0-9]*' "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    otok="$(grep -o 'outputTokens=[0-9]*' "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    ttok="$(grep -o 'totalTokens=[0-9]*'  "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    ms="$(grep -o 'elapsedMs=[0-9]*' "$log" | cut -d= -f2 | awk '{s+=$1} END {print s+0}')"
    echo "PIPELINE B  $label"
    echo "  model calls made       : $calls"
    echo "  prompt tokens          : $ptok"
    echo "  output tokens          : $otok"
    echo "  total tokens           : $ttok"
    echo "  model time       (ms)  : $ms"
    echo
  done
} > "$SUMMARY"

echo
echo "==========================================================="
cat "$SUMMARY"
echo "==========================================================="
echo
echo "Raw logs and generated artefacts: $OUT"
echo "Send SUMMARY.txt back to have these numbers written into Chapter VI."
echo
echo "Checking the source tree was left clean:"
ls -la "$GEN_DIR"
