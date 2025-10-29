#!/bin/bash
# -----------------------------------------------------------------------------
# Nombre: run-suite.sh
# Descripción:
#   Ejecuta una suite de pruebas Maven/Cucumber, genera reportes (HTML/JSON),
#   y luego envía un correo resumen y sube los resultados a Xray.
#
# Uso:
#   ./run-suite.sh <NOMBRE_DE_LA_SUITE>
#
# Ejemplos:
#   ./run-suite.sh UiTest
#   ENVIRONMENT=pre USER=qa BROWSER=edge ./run-suite.sh ApiTest
#
# Parámetros:
#   <NOMBRE_DE_LA_SUITE>  Nombre de la suite/clase de test a ejecutar (obligatorio).
#
# Variables de entorno (opcional):
#   ENVIRONMENT  Entorno de ejecución (p. ej. pre, prod).
#   USER         Usuario lógico para las pruebas (se pasa a Maven con -Duser).
#   BROWSER      Navegador objetivo (p. ej. chrome, edge, firefox).
#
# Flujo:
#   1) Valida que se reciba el nombre de la suite.
#   2) Ejecuta `mvn clean test` con los parámetros y plugins de Cucumber.
#      - La ejecución de Maven no detiene el script en caso de fallo (usa `|| true`).
#   3) Ejecuta `scripts/send-email-report.sh` para enviar un resumen por correo.
#   4) Ejecuta `scripts/send-xray-report.sh` para subir resultados a Xray.
#
# Artefactos generados:
#   target/<SUITE>.html   Reporte HTML de Cucumber.
#   target/<SUITE>.json   Reporte JSON de Cucumber.
#
# Dependencias:
#   - Maven instalado y accesible en PATH.
#   - Proyecto con configuración de Cucumber y plugins.
#   - scripts/send-email-report.sh
#   - scripts/send-xray-report.sh
#
# Códigos de salida:
#   0  Ejecución del script completada (aunque los tests fallen).
#   1  Falta el argumento <NOMBRE_DE_LA_SUITE>.
#
# Notas:
#   - Este script **no** pasa `-Dkeyclient`. Si necesitas restaurar la DB de pruebas,
#     ejecútalo una sola vez en el paso de "Preparar entorno" de la pipeline.
#   - El `set -e` detiene el script ante errores, excepto en la línea de Maven
#     que usa `|| true` para permitir la continuación del flujo (envío de reportes).
#   - Asegúrate de exportar/definir ENVIRONMENT, USER y BROWSER según tu pipeline.
# -----------------------------------------------------------------------------

set -e

SUITE=$1

if [ -z "$SUITE" ]; then
  echo "Debes proporcionar el nombre de la suite como argumento"
  exit 1
fi

export SUITE="$SUITE"
echo "Ejecutando pruebas para suite: $SUITE"

mvn clean test \
  -Dtest="$SUITE" \
  -Denv="$ENVIRONMENT" \
  -Duser="$USER" \
  -Dcucumber.plugin="pretty, html:target/${SUITE}.html, json:target/${SUITE}.json" \
  -Dbrowser="$BROWSER" || true

echo "Enviando correo con resumen..."
bash scripts/send-email-report.sh

echo "Subiendo resultados a Xray..."
bash scripts/send-xray-report.sh

echo "Suite $SUITE finalizada."