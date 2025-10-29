#!/usr/bin/env node
/**
 * @file sync-xray-scenarios.js
 * @description GitHub-friendly utility that compares local Cucumber scenarios against
 *              the corresponding Xray Cloud issues and optionally updates the remote
 *              definitions when a difference is detected.
 */

const fs = require('fs/promises');
const path = require('path');

const DEFAULT_FEATURE_DIR = 'src/test/resources/features';
const XRAY_DEFAULT_BASE_URL = 'https://xray.cloud.getxray.app/api/v2';
const CHUNK_SIZE = 50;

/**
 * Entry point for the synchronization workflow.
 */
async function main() {
  const options = parseArguments(process.argv.slice(2));
  const featureDir = path.resolve(options.featureDir ?? DEFAULT_FEATURE_DIR);

  const xrayClientId = process.env.XRAY_CLIENT_ID;
  const xrayClientSecret = process.env.XRAY_CLIENT_SECRET;

  if (!xrayClientId || !xrayClientSecret) {
    console.error('[ERROR] XRAY_CLIENT_ID and XRAY_CLIENT_SECRET environment variables are required.');
    process.exit(1);
  }

  const xrayBaseUrl = (process.env.XRAY_API_BASE_URL || XRAY_DEFAULT_BASE_URL).replace(/\/?$/, '/');

  const featureFiles = await collectFeatureFiles(featureDir);
  if (featureFiles.length === 0) {
    console.warn(`[WARN] No feature files found under ${featureDir}.`);
    return;
  }

  const scenarios = await readScenarios(featureFiles);
  const duplicates = findDuplicateKeys(scenarios);
  if (duplicates.length > 0) {
    console.error('[ERROR] Duplicated Xray keys detected in feature files:');
    duplicates.forEach((dup) => {
      console.error(`  - ${dup.issueKey} (${dup.filePaths.join(', ')})`);
    });
    process.exit(1);
  }

  const token = await authenticate(xrayBaseUrl, xrayClientId, xrayClientSecret);

  const xrayScenarios = await fetchXrayScenarios(xrayBaseUrl, token, scenarios.map((item) => item.issueKey));
  const comparison = compareScenarios(scenarios, xrayScenarios);

  logComparison(comparison);
  await writeStepSummary(comparison);

  const filesToUpdate = determineFilesToUpdate(comparison);

  if (filesToUpdate.size === 0) {
    console.log('[INFO] No differences detected. Xray is already in sync.');
    return;
  }

  if (options.dryRun) {
    console.log('[INFO] Dry run enabled. The following files require updates:');
    filesToUpdate.forEach((filePath) => console.log(`  - ${filePath}`));
    return;
  }

  await uploadFeatures(xrayBaseUrl, token, filesToUpdate, process.env.XRAY_PROJECT_KEY);
}

/**
 * Parses command line arguments for the script.
 * @param {string[]} args raw CLI arguments.
 * @returns {{ featureDir?: string, dryRun: boolean }} parsed options.
 */
function parseArguments(args) {
  const options = { dryRun: false };
  for (let i = 0; i < args.length; i += 1) {
    const arg = args[i];
    if (arg === '--feature-dir') {
      options.featureDir = args[i + 1];
      i += 1;
    } else if (arg === '--dry-run') {
      options.dryRun = true;
    } else if (arg === '--help' || arg === '-h') {
      printUsage();
      process.exit(0);
    } else {
      console.error(`[ERROR] Unknown argument: ${arg}`);
      printUsage();
      process.exit(1);
    }
  }
  return options;
}

/**
 * Prints the CLI usage help to stdout.
 */
function printUsage() {
  console.log('Usage: node scripts/sync-xray-scenarios.js [--feature-dir <path>] [--dry-run]');
  console.log('');
  console.log('Environment variables:');
  console.log('  XRAY_CLIENT_ID (required)');
  console.log('  XRAY_CLIENT_SECRET (required)');
  console.log('  XRAY_PROJECT_KEY (optional for feature import fallback)');
  console.log('  XRAY_API_BASE_URL (optional, defaults to https://xray.cloud.getxray.app/api/v2)');
}

/**
 * Recursively collects all feature files under a directory.
 * @param {string} dir base directory.
 * @returns {Promise<string[]>} list of feature file paths.
 */
async function collectFeatureFiles(dir) {
  const resolved = path.resolve(dir);
  let entries;
  try {
    entries = await fs.readdir(resolved, { withFileTypes: true });
  } catch (error) {
    console.error(`[ERROR] Unable to read feature directory ${resolved}: ${error.message}`);
    process.exit(1);
  }

  const files = [];
  for (const entry of entries) {
    const entryPath = path.join(resolved, entry.name);
    if (entry.isDirectory()) {
      const nested = await collectFeatureFiles(entryPath);
      nested.forEach((item) => files.push(item));
    } else if (entry.isFile() && entry.name.endsWith('.feature')) {
      files.push(entryPath);
    }
  }
  return files.sort();
}

/**
 * Reads every feature file and extracts the tagged scenario definitions.
 * @param {string[]} featureFiles list of feature paths.
 * @returns {Promise<object[]>} parsed scenario descriptors.
 */
async function readScenarios(featureFiles) {
  const scenarios = [];
  for (const filePath of featureFiles) {
    const content = await fs.readFile(filePath, 'utf8');
    const parsed = parseFeatureFile(filePath, content);
    parsed.forEach((scenario) => scenarios.push(scenario));
  }
  return scenarios;
}

/**
 * Parses an individual feature file and extracts Xray-linked scenarios.
 * @param {string} filePath path to the feature file.
 * @param {string} content contents of the feature file.
 * @returns {object[]} scenario descriptors containing key, name and gherkin.
 */
function parseFeatureFile(filePath, content) {
  const lines = content.split(/\r?\n/);
  const scenarios = [];
  let pendingTags = [];
  let pendingTagStart = null;

  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i];
    const trimmed = line.trim();
    if (trimmed.startsWith('@')) {
      if (pendingTagStart === null) {
        pendingTagStart = i;
      }
      const tags = trimmed.split(/\s+/).filter(Boolean);
      pendingTags.push(...tags);
      continue;
    }

    const scenarioMatch = trimmed.match(/^(Scenario Outline|Scenario):\s*(.+)$/i);
    if (scenarioMatch) {
      const scenarioName = scenarioMatch[2].trim();
      const startIndex = pendingTagStart !== null ? pendingTagStart : i;
      scenarios.push({
        filePath,
        name: scenarioName,
        startIndex,
        scenarioIndex: i,
        tags: pendingTags.slice(),
      });
      pendingTags = [];
      pendingTagStart = null;
      continue;
    }

    if (trimmed === '' || trimmed.startsWith('#')) {
      // Allow tags to survive blank/comment lines between tag and scenario.
      continue;
    }

    pendingTags = [];
    pendingTagStart = null;
  }

  const enrichedScenarios = [];
  for (let i = 0; i < scenarios.length; i += 1) {
    const scenario = scenarios[i];
    const nextStart = i + 1 < scenarios.length ? scenarios[i + 1].startIndex : lines.length;
    const block = lines.slice(scenario.startIndex, nextStart).join('\n');
    const issueKey = extractIssueKey(scenario.tags);
    if (!issueKey) {
      continue;
    }
    enrichedScenarios.push({
      issueKey,
      name: scenario.name,
      filePath: scenario.filePath,
      gherkin: block,
      normalizedGherkin: normalizeGherkin(block),
    });
  }

  return enrichedScenarios;
}

/**
 * Extracts the Jira/Xray issue key from a list of tags.
 * @param {string[]} tags cucumber tags for a scenario.
 * @returns {string|null} the detected issue key or null.
 */
function extractIssueKey(tags) {
  for (const tag of tags) {
    const keyMatch = tag.match(/^@([A-Z][A-Z0-9_]*-\d+)$/);
    if (keyMatch) {
      return keyMatch[1];
    }
  }
  return null;
}

/**
 * Detects duplicated scenario keys.
 * @param {object[]} scenarios parsed scenario descriptors.
 * @returns {{ issueKey: string, filePaths: string[] }[]} duplicates metadata.
 */
function findDuplicateKeys(scenarios) {
  const map = new Map();
  for (const scenario of scenarios) {
    if (!map.has(scenario.issueKey)) {
      map.set(scenario.issueKey, new Set());
    }
    map.get(scenario.issueKey).add(scenario.filePath);
  }

  const duplicates = [];
  for (const [issueKey, files] of map.entries()) {
    if (files.size > 1) {
      duplicates.push({ issueKey, filePaths: Array.from(files) });
    }
  }
  return duplicates;
}

/**
 * Authenticates with Xray Cloud and returns a bearer token.
 * @param {string} baseUrl base API URL.
 * @param {string} clientId Xray client id.
 * @param {string} clientSecret Xray client secret.
 * @returns {Promise<string>} authentication token.
 */
async function authenticate(baseUrl, clientId, clientSecret) {
  const url = new URL('authenticate', baseUrl);
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ client_id: clientId, client_secret: clientSecret }),
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`Authentication failed (${response.status}): ${text}`);
  }

  const tokenPayload = await response.text();
  const token = tokenPayload.replace(/^"|"$/g, '');
  if (!token) {
    throw new Error('Received empty authentication token from Xray.');
  }
  return token;
}

/**
 * Fetches the remote gherkin definition for the provided keys using GraphQL.
 * @param {string} baseUrl Xray base API url.
 * @param {string} token bearer token.
 * @param {string[]} issueKeys list of keys to fetch.
 * @returns {Promise<Map<string, { gherkin: string, testType: string }>>} remote scenario map.
 */
async function fetchXrayScenarios(baseUrl, token, issueKeys) {
  const graphUrl = new URL('graphql', baseUrl);
  const results = new Map();

  for (let i = 0; i < issueKeys.length; i += CHUNK_SIZE) {
    const chunk = issueKeys.slice(i, i + CHUNK_SIZE);
    if (chunk.length === 0) {
      continue;
    }
    const jql = `issueKey in (${chunk.join(',')})`;
    const payload = {
      query: `query($jql: String!, $limit: Int!) {\n        getTests(jql: $jql, limit: $limit) {\n          results {\n            issueKey\n            gherkin\n            testType { name }\n          }\n        }\n      }`,
      variables: {
        jql,
        limit: chunk.length,
      },
    };

    const response = await fetch(graphUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const text = await response.text();
      throw new Error(`Xray GraphQL request failed (${response.status}): ${text}`);
    }

    const body = await response.json();
    if (body.errors && body.errors.length > 0) {
      const message = body.errors.map((error) => error.message).join('; ');
      throw new Error(`Xray GraphQL reported errors: ${message}`);
    }

    const fetched = body?.data?.getTests?.results ?? [];
    fetched.forEach((item) => {
      results.set(item.issueKey, {
        gherkin: item.gherkin || '',
        testType: item.testType?.name || 'Unknown',
      });
    });
  }

  return results;
}

/**
 * Compares the local scenarios against the remote map.
 * @param {object[]} scenarios local scenario descriptors.
 * @param {Map<string, { gherkin: string, testType: string }>} remoteMap remote gherkin map.
 * @returns {object[]} comparison result entries.
 */
function compareScenarios(scenarios, remoteMap) {
  return scenarios.map((scenario) => {
    const remote = remoteMap.get(scenario.issueKey);
    if (!remote) {
      return {
        status: 'missing',
        issueKey: scenario.issueKey,
        name: scenario.name,
        filePath: scenario.filePath,
        localGherkin: scenario.gherkin,
        remoteGherkin: '',
        normalizedLocal: scenario.normalizedGherkin,
        normalizedRemote: '',
        details: 'No remote scenario found for this key.',
      };
    }

    if (remote.testType.toLowerCase() !== 'cucumber') {
      return {
        status: 'unsupported',
        issueKey: scenario.issueKey,
        name: scenario.name,
        filePath: scenario.filePath,
        localGherkin: scenario.gherkin,
        remoteGherkin: remote.gherkin,
        normalizedLocal: scenario.normalizedGherkin,
        normalizedRemote: normalizeGherkin(remote.gherkin),
        details: `Remote test type is ${remote.testType}`,
      };
    }

    const remoteBlock = extractScenarioBlock(remote.gherkin, scenario.name);
    const normalizedRemote = normalizeGherkin(remoteBlock ?? remote.gherkin);
    const isDifferent = normalizedRemote !== scenario.normalizedGherkin;

    return {
      status: isDifferent ? 'different' : 'match',
      issueKey: scenario.issueKey,
      name: scenario.name,
      filePath: scenario.filePath,
      localGherkin: scenario.gherkin,
      remoteGherkin: remote.gherkin,
      normalizedLocal: scenario.normalizedGherkin,
      normalizedRemote,
      details: isDifferent ? 'Content differs between repository and Xray.' : 'Scenario is up to date.',
    };
  });
}

/**
 * Extracts the matching scenario block from a remote gherkin feature string.
 * @param {string} remoteGherkin remote gherkin text.
 * @param {string} scenarioName scenario name to look for.
 * @returns {string|null} matching scenario block or null if not found.
 */
function extractScenarioBlock(remoteGherkin, scenarioName) {
  if (!remoteGherkin) {
    return null;
  }
  const normalized = remoteGherkin.replace(/\r\n/g, '\n');
  const lines = normalized.split('\n');
  const scenarioRegex = new RegExp(`^\s*Scenario(?: Outline)?:\s*${escapeRegExp(scenarioName)}\s*$`, 'i');
  for (let i = 0; i < lines.length; i += 1) {
    if (scenarioRegex.test(lines[i])) {
      let start = i;
      for (let j = i - 1; j >= 0; j -= 1) {
        const tagCandidate = lines[j].trim();
        if (tagCandidate.startsWith('@')) {
          start = j;
          continue;
        }
        if (tagCandidate === '') {
          start = j;
          continue;
        }
        break;
      }
      let end = lines.length;
      for (let k = i + 1; k < lines.length; k += 1) {
        const candidate = lines[k].trim();
        if (/^Scenario(?: Outline)?:/i.test(candidate)) {
          end = k;
          break;
        }
      }
      return lines.slice(start, end).join('\n');
    }
  }
  return null;
}

/**
 * Escapes a string so it can be used safely inside a RegExp literal.
 * @param {string} value raw string value.
 * @returns {string} escaped string.
 */
function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Normalizes gherkin text by trimming trailing whitespace and standardising line endings.
 * @param {string} value raw gherkin content.
 * @returns {string} normalized gherkin.
 */
function normalizeGherkin(value) {
  return value
    .replace(/\r\n/g, '\n')
    .split('\n')
    .map((line) => line.replace(/\s+$/u, ''))
    .join('\n')
    .trim();
}

/**
 * Logs a human readable summary of the comparison results.
 * @param {object[]} comparison comparison result entries.
 */
function logComparison(comparison) {
  const totals = comparison.reduce((acc, item) => {
    acc[item.status] = (acc[item.status] || 0) + 1;
    return acc;
  }, {});

  console.log('[INFO] Comparison summary:');
  Object.entries(totals).forEach(([status, count]) => {
    console.log(`  - ${status}: ${count}`);
  });

  comparison.forEach((item) => {
    const statusLabel = item.status.toUpperCase();
    console.log(`[${statusLabel}] ${item.issueKey} (${item.name}) -> ${item.details} [${item.filePath}]`);
  });
}

/**
 * Writes a Markdown table with the comparison results into the GitHub step summary when available.
 * @param {object[]} comparison comparison result entries.
 */
async function writeStepSummary(comparison) {
  const summaryPath = process.env.GITHUB_STEP_SUMMARY;
  if (!summaryPath) {
    return;
  }

  const lines = [
    '| Issue Key | Scenario | Status | Details | Feature File |',
    '|-----------|----------|--------|---------|---------------|',
  ];

  comparison.forEach((item) => {
    lines.push(
      `| ${escapeMarkdown(item.issueKey)} | ${escapeMarkdown(item.name)} | ${item.status} | ${escapeMarkdown(item.details)} | ${escapeMarkdown(item.filePath)} |`,
    );
  });

  lines.push('');
  await fs.appendFile(summaryPath, `${lines.join('\n')}\n`);
}

/**
 * Escapes Markdown special characters for table rendering.
 * @param {string} value raw text.
 * @returns {string} escaped value.
 */
function escapeMarkdown(value) {
  return (value || '')
    .replace(/\\/g, '\\\\')
    .replace(/\|/g, '\\|')
    .replace(/\n/g, '<br>');
}

/**
 * Calculates which feature files require an update in Xray.
 * @param {object[]} comparison comparison result entries.
 * @returns {Set<string>} set of feature file paths to update.
 */
function determineFilesToUpdate(comparison) {
  const files = new Set();
  comparison.forEach((item) => {
    if (item.status === 'different' || item.status === 'missing') {
      files.add(item.filePath);
    }
  });
  return files;
}

/**
 * Uploads the provided feature files to Xray using the import endpoint.
 * @param {string} baseUrl Xray base url.
 * @param {string} token bearer token.
 * @param {Set<string>} files feature files to upload.
 * @param {string|undefined} projectKey optional project key.
 */
async function uploadFeatures(baseUrl, token, files, projectKey) {
  for (const filePath of files) {
    const url = new URL('import/feature', baseUrl);
    if (projectKey) {
      url.searchParams.set('projectKey', projectKey);
    }
    const content = await fs.readFile(filePath);
    const form = new FormData();
    form.set('file', new Blob([content], { type: 'text/plain' }), path.basename(filePath));

    console.log(`[INFO] Uploading ${filePath} to Xray...`);
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: form,
    });

    const responseBody = await response.text();
    if (!response.ok) {
      throw new Error(`Failed to import ${filePath} (${response.status}): ${responseBody}`);
    }
    console.log(`[INFO] Xray response for ${filePath}: ${responseBody}`);
  }
}

main().catch((error) => {
  console.error(`[ERROR] ${error.message}`);
  process.exit(1);
});
