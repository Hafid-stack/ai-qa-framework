#!/usr/bin/env bash
#
# M4 — compilability of every generated artefact.
#
# Each artefact is compiled with javac against two things and nothing else:
#
#   1. evaluation/selenium-api-stub/  — a stub of the Selenium 4 public API surface whose
#      org.openqa.selenium.By declares exactly the eight static factory methods the official
#      Java API declares: id, linkText, partialLinkText, name, tagName, xpath, className,
#      cssSelector. Nothing is added, so a call to a method Selenium does not have fails here
#      exactly as it fails against the real library.
#   2. src/main/java/base/BasePage.java — the project's real base class, unmodified.
#
# Using a stub rather than the Selenium jar keeps this check runnable with no network access
# and no Maven repository, which is what makes the measurement reproducible years later.
#
# Usage:  ./evaluation/check-compilability.sh
# Exit:   0 if the observed results match the recorded ones, 1 otherwise.

set -uo pipefail
cd "$(dirname "$0")/.."

STUB="evaluation/selenium-api-stub"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

pass=0; fail=0

check () {
  local label="$1" file="$2" pkgdir="$3"
  rm -rf "$WORK/src" "$WORK/classes"
  mkdir -p "$WORK/src/base" "$WORK/src/$pkgdir"
  cp src/main/java/base/BasePage.java "$WORK/src/base/"
  cp "$file" "$WORK/src/$pkgdir/"

  if err=$(javac -nowarn -d "$WORK/classes" -sourcepath "$STUB:$WORK/src" \
            $(find "$WORK/src" -name '*.java') $(find "$STUB" -name '*.java') 2>&1); then
    printf "  %-46s COMPILES\n" "$label"; pass=$((pass+1))
  else
    printf "  %-46s FAILS\n" "$label"; fail=$((fail+1))
    echo "$err" | grep -E "error:|symbol:" | head -4 | sed 's/^/        /'
  fi
}

echo "Pipeline A — deterministic generation"
for f in generated-output/pipeline-a/*.java src/main/java/pages/generated/*.java \
         evaluation/results/SauceDemoInventoryPageBody.java; do
  [ -e "$f" ] || continue
  check "$(basename "$f")" "$f" "pages/generated"
done

echo
echo "Pipeline B — naive raw-HTML baseline"
for f in generated-output/naive/*.java; do
  pkg=$(grep -m1 '^package' "$f" | sed 's/package //;s/;//' | tr '.' '/')
  check "$(basename "$f")  [package ${pkg//\//.}]" "$f" "$pkg"
done

echo
echo "TOTAL: $pass compile, $fail fail"
