#!/usr/bin/env bash
#
# upload_xray_results.sh
# -----------------------
# Script to authenticate against Xray Cloud and upload a JSON results file.
#
# The script follows the official Xray Cloud REST API workflow:
#   1. Authenticate with the Client ID and Client Secret to obtain a bearer token.
#   2. Use the token to POST the test execution results JSON to the import endpoint.
#
# Prerequisites (set as environment variables before invoking the script):
#   XRAY_CLIENT_ID       - Xray Cloud Client ID (GitHub secret)
#   XRAY_CLIENT_SECRET   - Xray Cloud Client Secret (GitHub secret)
#   XRAY_PROJECT_KEY     - Optional, Jira project key (GitHub variable) to target when the
#                          results payload does not include the project information.
#   XRAY_API_BASE_URL    - Optional, overrides the default base URL for the Xray Cloud API.
#
# Usage:
#   ./scripts/upload_xray_results.sh <results-json-path> [--test-execution <KEY>] [--test-plan <KEY>]
#
# Arguments:
#   <results-json-path>    Path to the JSON file containing the test execution results.
#   --test-execution <KEY> Optional, injects/overrides the Test Execution issue key in the payload.
#   --test-plan <KEY>      Optional, injects/overrides the Test Plan issue key in the payload.
#
# The optional flags are provided for convenience when the JSON file does not already contain the
# relevant metadata. When used, the script will merge the provided keys into the top-level "info"
# object inside a temporary copy of the JSON prior to uploading it.
#
# Exit codes:
#   0 - Success
#   1 - Validation or usage error
#   >1 - Underlying curl/jq errors
#
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $0 <results-json-path> [--test-execution <KEY>] [--test-plan <KEY>]

Environment variables:
  XRAY_CLIENT_ID (required)
  XRAY_CLIENT_SECRET (required)
  XRAY_PROJECT_KEY (optional)
  XRAY_API_BASE_URL (optional, defaults to https://xray.cloud.getxray.app/api/v2)
USAGE
}

if [[ $# -lt 1 ]]; then
  usage >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "[ERROR] curl is required but not installed." >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "[ERROR] jq is required but not installed." >&2
  exit 1
fi

RESULTS_FILE="$1"
shift

if [[ ! -f "$RESULTS_FILE" ]]; then
  echo "[ERROR] Results file not found: $RESULTS_FILE" >&2
  exit 1
fi

# Determine the JSON root type in order to select the appropriate Xray endpoint and
# to know whether metadata can be injected into the payload.
if ! JSON_ROOT_TYPE=$(jq -r 'type' "$RESULTS_FILE" 2>/dev/null); then
  echo "[ERROR] Failed to parse JSON payload in $RESULTS_FILE" >&2
  exit 1
fi

TEST_EXECUTION_KEY=""
TEST_PLAN_KEY=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --test-execution)
      TEST_EXECUTION_KEY="$2"
      shift 2
      ;;
    --test-plan)
      TEST_PLAN_KEY="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[ERROR] Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

: "${XRAY_CLIENT_ID:?Environment variable XRAY_CLIENT_ID is required}"
: "${XRAY_CLIENT_SECRET:?Environment variable XRAY_CLIENT_SECRET is required}"

XRAY_API_BASE_URL="${XRAY_API_BASE_URL:-https://xray.cloud.getxray.app/api/v2}"
AUTH_URL="${XRAY_API_BASE_URL%/}/authenticate"

# Select the Xray import endpoint based on the JSON payload type. Cucumber results
# are represented as an array at the root level and must be sent to the dedicated
# Cucumber import endpoint.
case "$JSON_ROOT_TYPE" in
  array)
    IMPORT_URL="${XRAY_API_BASE_URL%/}/import/execution/cucumber/multipart"
    ;;
  object)
    IMPORT_URL="${XRAY_API_BASE_URL%/}/import/execution/multipart"
    ;;
  *)
    echo "[WARN] Unhandled JSON root type '$JSON_ROOT_TYPE'. Defaulting to the generic execution multipart import endpoint." >&2
    IMPORT_URL="${XRAY_API_BASE_URL%/}/import/execution/multipart"
    ;;
esac

# Create a temporary copy of the results file if we need to augment metadata.
TMP_RESULTS_FILE="$RESULTS_FILE"
CLEANUP_TEMP_FILE="false"
TMP_INFO_FILE=""
CLEANUP_INFO_FILE="false"

declare -A QUERY_PARAMS=()

if [[ -n "$TEST_EXECUTION_KEY" || -n "$TEST_PLAN_KEY" || -n "${XRAY_PROJECT_KEY:-}" ]]; then
  if [[ "$JSON_ROOT_TYPE" != "object" ]]; then
    echo "[WARN] Results JSON root is of type '$JSON_ROOT_TYPE'. Unable to inject Xray metadata into non-object payloads. Continuing without modifications." >&2

    # When the payload is an array (e.g. Cucumber), Xray allows metadata to be passed via
    # query parameters instead. Collect the values to append later to the upload URL.
    if [[ -n "$TEST_EXECUTION_KEY" ]]; then
      QUERY_PARAMS[testExecutionKey]="$TEST_EXECUTION_KEY"
    fi
    if [[ -n "$TEST_PLAN_KEY" ]]; then
      QUERY_PARAMS[testPlanKey]="$TEST_PLAN_KEY"
    fi
    if [[ -n "${XRAY_PROJECT_KEY:-}" ]]; then
      QUERY_PARAMS[projectKey]="$XRAY_PROJECT_KEY"
    fi
  else
    TMP_RESULTS_FILE=$(mktemp)
    CLEANUP_TEMP_FILE="true"

    # Prepare the JSON merge payload.
    MERGE_FILTER='.'
    if [[ -n "$TEST_EXECUTION_KEY" ]]; then
      MERGE_FILTER="$MERGE_FILTER | .info.testExecutionKey = \"$TEST_EXECUTION_KEY\""
    fi
    if [[ -n "$TEST_PLAN_KEY" ]]; then
      MERGE_FILTER="$MERGE_FILTER | .info.testPlanKey = \"$TEST_PLAN_KEY\""
    fi
    if [[ -n "${XRAY_PROJECT_KEY:-}" ]]; then
      MERGE_FILTER="$MERGE_FILTER | .info.project = \"$XRAY_PROJECT_KEY\""
    fi

    jq "$MERGE_FILTER" "$RESULTS_FILE" > "$TMP_RESULTS_FILE"
  fi
fi

urlencode() {
  jq -sRr @uri <<<"$1"
}

build_info_payload() {
  local summary description project_key execution_key plan_key browser environment pipeline_number pipeline_url suite issuetype

  summary="${XRAY_INFO_SUMMARY:-}"
  if [[ -z "$summary" ]]; then
    suite="${SUITE_NAME:-${SUITE:-}}"
    if [[ -n "$suite" ]]; then
      summary="Automated execution - ${suite}"
    else
      summary="Automated execution"
    fi
  fi

  description="${XRAY_INFO_DESCRIPTION:-}"
  pipeline_number="${CI_PIPELINE_NUMBER:-}"
  pipeline_url="${CI_PIPELINE_URL:-}"
  if [[ -z "$description" ]]; then
    if [[ -n "$pipeline_number" || -n "$pipeline_url" ]]; then
      description="Generated from CI pipeline"
      if [[ -n "$pipeline_number" ]]; then
        description+=$'\n'"Run number: ${pipeline_number}"
      fi
      if [[ -n "$pipeline_url" ]]; then
        description+=$'\n'"Run URL: ${pipeline_url}"
      fi
    fi
  fi

  project_key="${XRAY_PROJECT_KEY:-}"
  execution_key="$TEST_EXECUTION_KEY"
  plan_key="$TEST_PLAN_KEY"
  browser="${BROWSER:-}"
  environment="${ENVIRONMENT:-}"
  issuetype="${XRAY_INFO_ISSUETYPE:-Test Execution}"

  jq -n \
    --arg summary "$summary" \
    --arg description "$description" \
    --arg projectKey "$project_key" \
    --arg issuetype "$issuetype" \
    --arg testExecutionKey "$execution_key" \
    --arg testPlanKey "$plan_key" \
    --arg browser "$browser" \
    --arg environment "$environment" \
    '
    {
      testExecutionKey: (if $testExecutionKey != "" then $testExecutionKey else null end),
      testPlanKey: (if $testPlanKey != "" then $testPlanKey else null end),
      info: (
        {
          summary: (if $summary != "" then $summary else null end),
          description: (if $description != "" then $description else null end),
          projectKey: (if $projectKey != "" then $projectKey else null end),
          project: (if $projectKey != "" then $projectKey else null end),
          testEnvironments: ([$browser, $environment]
            | map(select(. != ""))
            | unique
            | if length > 0 then . else null end)
        }
        | with_entries(select(.value != null))
        | if length > 0 then . else null end
      ),
      fields: (
        {
          project: (if $projectKey != "" then { key: $projectKey } else null end),
          issuetype: { name: $issuetype },
          summary: (if $summary != "" then $summary else null end),
          description: (if $description != "" then $description else null end)
        }
        | with_entries(select(.value != null))
      )
    }
    | with_entries(select(.value != null))
  '
}

HTTP_RESPONSE_FILE=""

cleanup() {
  if [[ -n "$HTTP_RESPONSE_FILE" && -f "$HTTP_RESPONSE_FILE" ]]; then
    rm -f "$HTTP_RESPONSE_FILE"
  fi
  if [[ "$CLEANUP_TEMP_FILE" == "true" && -f "$TMP_RESULTS_FILE" ]]; then
    rm -f "$TMP_RESULTS_FILE"
  fi
  if [[ "$CLEANUP_INFO_FILE" == "true" && -f "$TMP_INFO_FILE" ]]; then
    rm -f "$TMP_INFO_FILE"
  fi
}
trap cleanup EXIT

# Authenticate to obtain a bearer token.
printf '[INFO] Authenticating with Xray Cloud...\n'
TOKEN=$(curl -sS --fail \
  -X POST "$AUTH_URL" \
  -H 'Content-Type: application/json' \
  -d "{\"client_id\":\"$XRAY_CLIENT_ID\",\"client_secret\":\"$XRAY_CLIENT_SECRET\"}") || {
  echo "[ERROR] Failed to authenticate with Xray Cloud." >&2
  exit 1
}

# The API returns the token as a JSON string literal; strip surrounding quotes.
TOKEN="${TOKEN%\"}"
TOKEN="${TOKEN#\"}"

printf '[INFO] Uploading results from %s...\n' "$RESULTS_FILE"

UPLOAD_URL="$IMPORT_URL"
# Optional query parameters can be added here if needed in the future.
if [[ ${#QUERY_PARAMS[@]} -gt 0 ]]; then
  FIRST=true
  for key in "${!QUERY_PARAMS[@]}"; do
    value="${QUERY_PARAMS[$key]}"
    if [[ -z "$value" ]]; then
      continue
    fi
    encoded_key=$(urlencode "$key")
    encoded_value=$(urlencode "$value")
    if [[ "$FIRST" == true ]]; then
      UPLOAD_URL+="?${encoded_key}=${encoded_value}"
      FIRST=false
    else
      UPLOAD_URL+="&${encoded_key}=${encoded_value}"
    fi
  done
fi

HTTP_RESPONSE_FILE=$(mktemp)

TMP_INFO_FILE=$(mktemp)
CLEANUP_INFO_FILE="true"
INFO_PAYLOAD=$(build_info_payload)
if [[ -z "$INFO_PAYLOAD" ]]; then
  INFO_PAYLOAD='{}'
fi
printf '%s\n' "$INFO_PAYLOAD" > "$TMP_INFO_FILE"

HTTP_STATUS=$(curl -sS \
  -X POST "$UPLOAD_URL" \
  -H "Authorization: Bearer $TOKEN" \
  -F "results=@${TMP_RESULTS_FILE};type=application/json" \
  -F "info=@${TMP_INFO_FILE};type=application/json" \
  -o "$HTTP_RESPONSE_FILE" \
  -w '%{http_code}') || {
    CURL_EXIT=$?
    echo "[ERROR] curl failed with exit code $CURL_EXIT" >&2
    if [[ -f "$HTTP_RESPONSE_FILE" && -s "$HTTP_RESPONSE_FILE" ]]; then
      echo "[ERROR] Response from Xray:" >&2
      cat "$HTTP_RESPONSE_FILE" >&2
    fi
    exit "$CURL_EXIT"
  }

if [[ "$HTTP_STATUS" =~ ^[0-9]+$ ]] && (( HTTP_STATUS >= 200 && HTTP_STATUS < 300 )); then
  if [[ -s "$HTTP_RESPONSE_FILE" ]]; then
    printf '[INFO] Xray response:\n'
    cat "$HTTP_RESPONSE_FILE"
    printf '\n'
  fi
  printf '[INFO] Upload completed successfully.\n'
else
  echo "[ERROR] Xray import failed with status ${HTTP_STATUS}. Response:" >&2
  if [[ -s "$HTTP_RESPONSE_FILE" ]]; then
    cat "$HTTP_RESPONSE_FILE" >&2
  else
    echo "[ERROR] (no response body)" >&2
  fi
  exit 1
fi
