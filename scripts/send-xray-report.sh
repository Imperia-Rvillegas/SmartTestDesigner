#!/bin/bash
# -----------------------------------------------------------------------------
# Nombre: send-xray-report.sh
# Descripción:
#   Sube un reporte de resultados de pruebas (formato Cucumber JSON) a Xray Cloud,
#   creando automáticamente una ejecución de prueba (Test Execution) con metadatos
#   y asociando la información del pipeline de CI.
#
# Uso:
#   ./send-xray-report.sh
#
# Variables de entorno requeridas:
#   SUITE                Nombre de la suite ejecutada (usado para ubicar el JSON).
#   XRAY_CLIENT_ID       Client ID de la API de Xray Cloud.
#   XRAY_CLIENT_SECRET   Client Secret de la API de Xray Cloud.
#
# Variables de entorno opcionales:
#   ENVIRONMENT          Entorno de ejecución (pre, prod, etc.).
#   BROWSER              Navegador utilizado en las pruebas.
#   USER                 Usuario lógico utilizado en las pruebas.
#   KEYCLIENT            Identificador de cliente.
#   CI_PIPELINE_NUMBER   Número de ejecución del pipeline (GitHub, Bitbucket, etc.).
#   CI_PIPELINE_URL      URL detallada del pipeline.
#   BITBUCKET_BUILD_NUMBER  Compatibilidad heredada con pipelines de Bitbucket.
#
# Funcionamiento:
#   1) Localiza el archivo de resultados JSON generado por Cucumber en target/.
#   2) Construye la URL del pipeline (si se proporcionó).
#   3) Obtiene un token de acceso a Xray Cloud mediante la API de autenticación.
#   4) Crea un archivo temporal con metadatos del Test Execution (proyecto, tipo,
#      resumen y descripción).
#   5) Envía el reporte JSON y los metadatos a Xray Cloud mediante una solicitud
#      multipart/form-data a la API de importación de ejecuciones Cucumber.
#   6) Valida el código HTTP de respuesta y muestra el resultado.
#
# Dependencias:
#   - `curl` para llamadas HTTP.
#   - `jq` para parsear el token JSON de autenticación.
#   - Acceso a internet hacia https://xray.cloud.getxray.app.
#
# Artefactos:
#   - target/<SUITE>.json  Archivo de resultados Cucumber a enviar.
#   - Archivo temporal INFO_FILE con los metadatos del Test Execution.
#
# Notas:
#   - El script se detiene si ocurre un error (`set -e`).
#   - Si el archivo JSON no existe, se aborta la ejecución.
#   - El proyecto de Jira y el tipo de issue están fijos como:
#       "project.key": "DEV"
#       "issuetype.name": "Test Execution"
#     (modificar según necesidades).
# -----------------------------------------------------------------------------

set -e

echo "Subiendo resultados a Xray (Cloud)..."

JSON_REPORT="target/${SUITE}.json"
echo "Archivo JSON: $JSON_REPORT"

if [ ! -f "$JSON_REPORT" ]; then
  echo "No se encontró el archivo '$JSON_REPORT'"
  exit 1
fi

PIPELINE_NUMBER="${CI_PIPELINE_NUMBER:-${BITBUCKET_BUILD_NUMBER:-}}"
PIPELINE_NUMBER="${PIPELINE_NUMBER:-N/A}"

if [[ "$PIPELINE_NUMBER" == "N/A" ]]; then
  DEFAULT_PIPELINE_URL="N/A"
else
  DEFAULT_PIPELINE_URL="https://bitbucket.org/imperia-scm/qa-automation-bot/pipelines/results/$PIPELINE_NUMBER"
fi

PIPELINE_URL="${CI_PIPELINE_URL:-$DEFAULT_PIPELINE_URL}"

SUMMARY="${SUITE:-N/A}/${KEYCLIENT:-N/A}/${BROWSER:-N/A}/${ENVIRONMENT:-N/A}/${USER:-N/A}"

echo "Número de pipeline: $PIPELINE_NUMBER"
echo "URL del pipeline: $PIPELINE_URL"

# Obtener token de acceso
ACCESS_TOKEN=$(curl -s -X POST https://xray.cloud.getxray.app/api/v2/authenticate \
  -H "Content-Type: application/json" \
  -d @<(cat <<EOF
{
  "client_id": "$XRAY_CLIENT_ID",
  "client_secret": "$XRAY_CLIENT_SECRET"
}
EOF
) | jq -r .)

echo "Token obtenido: $ACCESS_TOKEN"

# Crear archivo temporal con metadatos
INFO_FILE=$(mktemp)
cat > "$INFO_FILE" <<EOF
{
  "fields": {
    "project": {
      "key": "DEV"
    },
    "issuetype": {
      "name": "Test Execution"
    },
    "summary": "$SUMMARY",
    "description": "Este test fue ejecutado automáticamente por qa-automation-bot."
  }
}
EOF

# Subir resultados con metadatos
echo "Subiendo resultados con metadatos..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST https://xray.cloud.getxray.app/api/v2/import/execution/cucumber/multipart \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "results=@$JSON_REPORT;type=application/json" \
  -F "info=@$INFO_FILE;type=application/json")

# Limpiar archivo temporal
rm -f "$INFO_FILE"

# Parsear respuesta y código HTTP
HTTP_BODY=$(echo "$RESPONSE" | sed '$d')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)

echo "Respuesta de Xray:"
echo "$HTTP_BODY"

if [[ "$HTTP_CODE" -ge 200 && "$HTTP_CODE" -lt 300 ]]; then
  echo "Reporte subido correctamente a Xray con metadatos."
else
  echo "Error al subir el reporte a Xray (código $HTTP_CODE)."
  exit 1
fi
