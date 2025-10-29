#!/bin/bash
# -----------------------------------------------------------------------------
# Nombre: send-email-report.sh
# Descripción:
#   Envía un correo electrónico con el resumen de la ejecución de una suite
#   de pruebas, utilizando `mutt` y `msmtp` como cliente SMTP.
#   Incluye detalles de la suite, entorno, navegador, usuario y número de pipeline.
#
# Uso:
#   ./send-email-report.sh
#
# Variables de entorno requeridas:
#   EMAIL_FROM       Dirección de correo del remitente (cuenta SMTP).
#   EMAIL_ME         Direcciones de destinatario(s) TO. Puede ser UNA o VARIAS.
#                    Separadores permitidos: coma (,), punto y coma (;) o espacios.
#   SMTP_PASS        Contraseña o token de aplicación para la cuenta SMTP.
#   SUITE            Nombre de la suite ejecutada.
#   ENVIRONMENT      Nombre del entorno (pre, prod, etc.) (opcional).
#   BROWSER          Navegador utilizado en la ejecución.
#   USER             Usuario lógico utilizado en las pruebas.
#   KEYCLIENT        Identificador de cliente (opcional).
#
# Variables de entorno opcionales:
#   EMAIL_CC         Direcciones CC (mismos separadores que EMAIL_ME).
#   EMAIL_BCC        Direcciones BCC (mismos separadores que EMAIL_ME).
#   CI_PIPELINE_NUMBER  Número de ejecución del pipeline (GitHub, Bitbucket, etc.).
#   CI_PIPELINE_URL     URL detallada del pipeline.
#   BITBUCKET_BUILD_NUMBER  Compatibilidad heredada con pipelines de Bitbucket.
#
# Funcionamiento:
#   1) Muestra por consola los valores de las variables clave para depuración.
#   2) Construye el asunto (SUBJECT) y el cuerpo (BODY) con info de la ejecución
#      y un enlace al pipeline de CI disponible.
#   3) Configura ~/.msmtprc con credenciales y parámetros SMTP (host, puerto, TLS).
#   4) Configura ~/.muttrc para que `mutt` use msmtp como transportador de correo.
#   5) Envía el correo a múltiples destinatarios (TO), con soporte para CC/BCC.
#
# Dependencias:
#   - `mutt` instalado y accesible en PATH.
#   - `msmtp` instalado y accesible en PATH.
#   - Certificados TLS actualizados (/etc/ssl/certs/ca-certificates.crt).
#
# Artefactos:
#   - ~/.msmtprc  (configuración temporal de msmtp).
#   - ~/.muttrc   (configuración temporal de mutt).
#
# Notas:
#   - El script está en modo debug (`set -x`) para mostrar cada comando ejecutado.
#   - El asunto del correo incluye suite, ambiente, navegador, usuario, cliente
#     y número de pipeline.
#   - No adjunta reportes; envía un resumen textual con enlace al pipeline.
# -----------------------------------------------------------------------------

set -x  # Modo debug para ver cada comando que se ejecuta

echo "Enviando correo con mutt + msmtp..."

# Mostrar valores de las variables para depuración
echo "De: $EMAIL_FROM"
echo "Para (TO): $EMAIL_ME"
echo "CC: ${EMAIL_CC:-}"
echo "BCC: ${EMAIL_BCC:-}"
echo "Suite: $SUITE"
echo "Ambiente: ${ENVIRONMENT:-N/A}"
echo "Navegador: $BROWSER"
echo "Usuario: $USER"
echo "KeyClient: $KEYCLIENT"

PIPELINE_NUMBER="${CI_PIPELINE_NUMBER:-${BITBUCKET_BUILD_NUMBER:-}}"
PIPELINE_NUMBER="${PIPELINE_NUMBER:-N/A}"

if [[ "$PIPELINE_NUMBER" == "N/A" ]]; then
  DEFAULT_PIPELINE_URL="N/A"
else
  DEFAULT_PIPELINE_URL="https://bitbucket.org/imperia-scm/qa-automation-bot/pipelines/results/$PIPELINE_NUMBER"
fi

PIPELINE_URL="${CI_PIPELINE_URL:-$DEFAULT_PIPELINE_URL}"

echo "Pipeline N°: $PIPELINE_NUMBER"
echo "Pipeline URL: $PIPELINE_URL"

FROM="$EMAIL_FROM"
SUBJECT="Resultados $SUITE | Ambiente: ${ENVIRONMENT:-N/A} | Navegador: $BROWSER | Usuario: $USER | Cliente: ${KEYCLIENT:-N/A} | N° Pipeline: $PIPELINE_NUMBER"

BODY=$(cat <<EOF
Suite: "$SUITE"
Ambiente: "${ENVIRONMENT:-N/A}"
Navegador: "$BROWSER"
Usuario: "$USER"
KeyClient: "${KEYCLIENT:-N/A}"
N° Pipeline: "$PIPELINE_NUMBER"

Este test fue ejecutado automáticamente por qa-automation-bot.
Ver detalles del pipeline:
$PIPELINE_URL
EOF
)

# Configurar msmtp
echo "Configurando msmtp..."
cat <<EOF > ~/.msmtprc
defaults
auth on
tls on
tls_trust_file /etc/ssl/certs/ca-certificates.crt
account outlook
host smtp.office365.com
port 587
from $FROM
user $FROM
password $SMTP_PASS
account default : outlook
EOF

chmod 600 ~/.msmtprc

# Configurar mutt
echo "Configurando mutt..."
{
  echo "set sendmail=\"/usr/bin/msmtp\""
  echo "set use_from=yes"
  echo "set realname=\"CI Pipeline\""
  echo "set from=\"$FROM\""
} > ~/.muttrc

# === NUEVO: preparar listas TO/CC/BCC a partir de variables ===
# Admite separadores: coma, punto y coma y espacios.
to_list="${EMAIL_ME:-}"
cc_list="${EMAIL_CC:-}"
bcc_list="${EMAIL_BCC:-}"

# Validación básica
if [[ -z "$to_list" ]]; then
  echo "[email] ERROR: EMAIL_ME está vacío; no hay destinatarios." >&2
  exit 1
fi

# Normalizar separadores a espacios
to_list="${to_list//,/ }";   to_list="${to_list//;/ }"
cc_list="${cc_list//,/ }";   cc_list="${cc_list//;/ }"
bcc_list="${bcc_list//,/ }"; bcc_list="${bcc_list//;/ }"

# Convertir a arrays
read -r -a TO_ARRAY  <<< "$to_list"
read -r -a CC_ARRAY  <<< "$cc_list"
read -r -a BCC_ARRAY <<< "$bcc_list"

# Construir argumentos para mutt
ARGS=(-s "$SUBJECT")
for cc in "${CC_ARRAY[@]}";  do [[ -n "$cc" ]] && ARGS+=(-c "$cc"); done
for bc in "${BCC_ARRAY[@]}"; do [[ -n "$bc" ]] && ARGS+=(-b "$bc"); done
ARGS+=(-- "${TO_ARRAY[@]}")

# Enviar el correo sin adjunto, siempre
echo "Enviando correo sin adjunto..."
echo "$BODY" | mutt "${ARGS[@]}"
