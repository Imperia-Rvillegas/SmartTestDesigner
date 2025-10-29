#!/usr/bin/env bash
# Ejecuta una suite de regresión Maven dentro del contenedor de browsers
set -euo pipefail

SUITE_NAME="${SUITE_NAME:-}"
if [[ -z "${SUITE_NAME}" ]]; then
  echo "::error::SUITE_NAME no definido dentro del contenedor."
  exit 1
fi

GITHUB_RUN_ID="${GITHUB_RUN_ID:-0}"
GITHUB_RUN_NUMBER="${GITHUB_RUN_NUMBER:-0}"

mkdir -p target/logs
LOG_FILE="target/logs/${SUITE_NAME}-${GITHUB_RUN_ID}.log"

set +e
mvn clean test \
  -Dtest="${SUITE_NAME}" \
  -Denv="${ENVIRONMENT:-}" \
  -Duser="${USER:-}" \
  -Dbrowser="${BROWSER:-}" \
  -Dcucumber.plugin="pretty, html:target/${SUITE_NAME}.html, json:target/${SUITE_NAME}.json" \
  2>&1 | tee "${LOG_FILE}"
MVN_EXIT=${PIPESTATUS[0]}
set -e

exit "${MVN_EXIT}"
