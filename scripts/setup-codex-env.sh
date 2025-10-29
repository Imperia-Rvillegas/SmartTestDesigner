#!/usr/bin/env bash
# =========================================================
# Script de configuración de entorno Codex para Ubuntu 24.04
# Incluye:
#  - Java 24 (Temurin, default)
#  - Maven (por defecto 3.9.11, versionable)
#  - Google Chrome + Microsoft Edge
#  - Soporte Headless (Xvfb)
#  - Variables de entorno para CI
#  - PATH saneado para priorizar Java 24 y evitar shims de mise
# =========================================================
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive

log() { echo -e "\n🧩 $1\n"; }

log "🔧 Iniciando configuración del entorno Codex en Ubuntu 24.04..."

# ------------------------------------------------------------
# 1) Sistema base
# ------------------------------------------------------------
log "📦 Actualizando repositorios..."
sudo apt update -y && sudo apt upgrade -y
sudo apt install -y wget curl gnupg ca-certificates unzip software-properties-common apt-transport-https

# ------------------------------------------------------------
# 2) Audio/Deps previos (evita que entre OSS4 como proveedor de libasound2)
#    Instalamos el proveedor real: libasound2t64 ANTES del JDK
# ------------------------------------------------------------
log "🔊 Preparando dependencias ALSA previas..."
sudo apt install -y --no-install-recommends libasound2t64 libasound2-data
sudo apt purge -y liboss4-salsa-asound2 liboss4-salsa2 || true

# ------------------------------------------------------------
# 3) Java 24 (Temurin) por defecto (sin recommends)
# ------------------------------------------------------------
log "☕ Instalando OpenJDK 24 (Temurin)..."
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public \
  | sudo tee /etc/apt/trusted.gpg.d/adoptium.asc >/dev/null
echo "deb [signed-by=/etc/apt/trusted.gpg.d/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" \
  | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null

sudo apt update -y
sudo apt install -y --no-install-recommends temurin-24-jdk

JDK_DIR="/usr/lib/jvm/temurin-24-jdk-amd64"
if [ ! -d "$JDK_DIR" ]; then
  echo "❌ No se encontró el directorio del JDK ($JDK_DIR)"; exit 1
fi

log "🔗 Configurando Java 24 como predeterminado..."
sudo update-alternatives --set java  "${JDK_DIR}/bin/java"  || true
sudo update-alternatives --set javac "${JDK_DIR}/bin/javac" || true

# Entorno global clásico
PROFILE_SYSTEM="/etc/profile.d/java.sh"
sudo bash -c "cat > $PROFILE_SYSTEM" <<EOF
# BEGIN JAVA24
export JAVA_HOME="${JDK_DIR}"
export PATH="\$JAVA_HOME/bin:\$PATH"
# END JAVA24
EOF
# shellcheck disable=SC1090
source "$PROFILE_SYSTEM"
log "🔎 Java activo:"; java -version || true

# ------------------------------------------------------------
# 4) Parche permanente de PATH (priorizar Java 24 y filtrar mise java)
# ------------------------------------------------------------
log "🧭 Saneando PATH para priorizar Java 24 y quitar shims de mise..."
sudo bash -c 'cat > /etc/profile.d/00-java24-first.sh' <<'EOS'
# Priorizar Temurin 24 por delante y eliminar rutas de mise/java del PATH
JAVA_HOME="/usr/lib/jvm/temurin-24-jdk-amd64"
cleanup_path() {
  # quita rutas mise/java y duplicados
  printf "%s" "$1" \
  | tr ":" "\n" \
  | awk '!seen[$0]++' \
  | awk '!/\.local\/share\/mise\/installs\/java\// && !/\/mise\/shims/' \
  | paste -sd: -
}
if [ -n "$PATH" ]; then
  CLEANED_PATH="$(cleanup_path "$PATH")"
  export JAVA_HOME
  export PATH="$JAVA_HOME/bin:/usr/bin:$CLEANED_PATH"
else
  export JAVA_HOME
  export PATH="$JAVA_HOME/bin:/usr/bin"
fi
EOS

# Cargarlo también en shells no-login
sudo bash -c 'grep -q "00-java24-first.sh" /etc/bash.bashrc || echo "[ -f /etc/profile.d/00-java24-first.sh ] && . /etc/profile.d/00-java24-first.sh" >> /etc/bash.bashrc'
if [ -d /etc/zsh ]; then
  sudo bash -c 'grep -q "00-java24-first.sh" /etc/zsh/zshenv || echo "[ -f /etc/profile.d/00-java24-first.sh ] && . /etc/profile.d/00-java24-first.sh" >> /etc/zsh/zshenv'
fi

# (Opcional) symlinks "cinturón y tirantes" en /usr/local/bin
for b in java javac javadoc jar jlink jpackage jshell jps jcmd jfr; do
  sudo ln -sfn "${JDK_DIR}/bin/$b" "/usr/local/bin/$b"
done

# ------------------------------------------------------------
# 5) Maven (robusto, versionable)
# ------------------------------------------------------------
log "📦 Instalando Apache Maven..."
MAVEN_VERSION="${MAVEN_VERSION:-3.9.11}"
MAVEN_TGZ="apache-maven-${MAVEN_VERSION}-bin.tar.gz"
MAVEN_DIR="/opt/apache-maven-${MAVEN_VERSION}"
MAVEN_URLS=(
  "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/${MAVEN_TGZ}"
  "https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/${MAVEN_TGZ}"
  "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/${MAVEN_TGZ}"
)
set +e
download_ok=0
cd /tmp
for url in "${MAVEN_URLS[@]}"; do
  echo "➡️  Descargando Maven ${MAVEN_VERSION} desde: $url"
  if timeout 180s wget --progress=dot:giga --tries=3 "$url" -O "$MAVEN_TGZ"; then
    download_ok=1; break
  else
    echo "wget falló, probando curl..."
    if timeout 180s curl -fL --retry 3 --retry-delay 2 -o "$MAVEN_TGZ" "$url"; then
      download_ok=1; break
    fi
  fi
done
set -e
if [ "$download_ok" -ne 1 ]; then
  echo "❌ No se pudo descargar Maven ${MAVEN_VERSION} desde los mirrors oficiales."; exit 1
fi
sudo tar -xzf "$MAVEN_TGZ" -C /opt
sudo ln -sfn "$MAVEN_DIR" /opt/maven
PROFILE_MAVEN="/etc/profile.d/maven.sh"
sudo bash -c "cat > $PROFILE_MAVEN" <<'EOF'
# BEGIN MAVEN
export M2_HOME=/opt/maven
export MAVEN_HOME=/opt/maven
export PATH=$M2_HOME/bin:$PATH
# END MAVEN
EOF
# shellcheck disable=SC1091
source "$PROFILE_MAVEN"
log "🔎 Maven activo:"; mvn -version || { echo "❌ Maven no responde"; exit 1; }

# ------------------------------------------------------------
# 6) Deps de runtime para Chrome/Edge en CI (ya con ALSA estándar)
# ------------------------------------------------------------
log "🧰 Instalando dependencias de navegadores..."
sudo apt install -y \
  libnss3 libgbm1 libxkbcommon0 libx11-xcb1 libatk-bridge2.0-0 \
  libdrm2 libxdamage1 libxfixes3 libxrandr2 libgtk-3-0 xdg-utils

# ------------------------------------------------------------
# 7) Google Chrome
# ------------------------------------------------------------
log "🌐 Instalando Google Chrome..."
wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo gpg --dearmor -o /usr/share/keyrings/google.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google.gpg] http://dl.google.com/linux/chrome/deb stable main" \
  | sudo tee /etc/apt/sources.list.d/google-chrome.list > /dev/null
sudo apt update -y
sudo apt install -y google-chrome-stable

# ------------------------------------------------------------
# 8) Microsoft Edge
# ------------------------------------------------------------
log "🌐 Instalando Microsoft Edge..."
wget -q https://packages.microsoft.com/keys/microsoft.asc -O- | sudo gpg --dearmor -o /usr/share/keyrings/microsoft.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/microsoft.gpg] https://packages.microsoft.com/repos/edge stable main" \
  | sudo tee /etc/apt/sources.list.d/microsoft-edge.list > /dev/null
sudo apt update -y
sudo apt install -y microsoft-edge-stable

# ------------------------------------------------------------
# 9) Xvfb (headless)
# ------------------------------------------------------------
log "🖥️ Instalando Xvfb para ejecución headless..."
sudo apt install -y xvfb

# ------------------------------------------------------------
# 10) Entorno CI
# ------------------------------------------------------------
log "⚙️ Configurando variables de entorno CI..."
if ! grep -q '^CI=' /etc/environment; then
  echo 'CI=true' | sudo tee -a /etc/environment > /dev/null
fi

# ------------------------------------------------------------
# 11) Comprobaciones finales
# ------------------------------------------------------------
log "✅ Instalación completada. Versiones instaladas:"
echo "==> Java";        java -version || true
echo "==> Maven";       mvn -version || true
echo "==> Navegadores"; google-chrome --version || true; microsoft-edge --version || true
echo "==> Headless";    Xvfb -help 2>&1 | head -n 1 || true

echo ""
echo "🚀 Entorno Codex listo para CI/CD con Java 24, Maven, Chrome, Edge y Xvfb."