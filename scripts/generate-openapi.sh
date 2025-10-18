#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(
  cd "$(dirname "${BASH_SOURCE[0]}")/.." >/dev/null 2>&1
  pwd
)"

OUTPUT_DIR="${ROOT_DIR}/docs"
OUTPUT_FILE="openapi.json"

echo "Generating OpenAPI document into ${OUTPUT_DIR}/${OUTPUT_FILE}"

"${ROOT_DIR}/mvnw" -q \
  org.springdoc:springdoc-openapi-maven-plugin:1.7.0:generate \
  -Dspringdoc.outputDir="${OUTPUT_DIR}" \
  -Dspringdoc.outputFileName="${OUTPUT_FILE}" \
  -Dspringdoc.packagesToScan=com.example.taskify.web \
  -Dspringdoc.apiDocsUrl=/v3/api-docs \
  -DskipTests=true

echo "✅ OpenAPI spec updated at ${OUTPUT_DIR}/${OUTPUT_FILE}"
