# ---- Base: Java 24 (Temurin) ----
FROM eclipse-temurin:24-jdk-noble AS base

ENV DEBIAN_FRONTEND=noninteractive \
    TZ=UTC \
    LANG=C.UTF-8 \
    MAVEN_VERSION=3.9.11 \
    MAVEN_HOME=/opt/maven

# ---- Paquetes base y dependencias de navegadores (sin recomendaciones) ----
# En noble, el paquete correcto de sonido es libasound2t64
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
      ca-certificates curl gnupg unzip \
      libasound2t64 libatk-bridge2.0-0 libatk1.0-0 libc6 libdrm2 \
      libgbm1 libgtk-3-0 libnss3 libx11-6 libx11-xcb1 libxcb1 \
      libxcomposite1 libxdamage1 libxext6 libxi6 libxrandr2 \
      libxrender1 libxtst6 xdg-utils fonts-liberation \
      tini; \
    rm -rf /var/lib/apt/lists/*

# ---- Google Chrome estable (repo oficial con signed-by) ----
RUN set -eux; \
    install -m 0755 -d /etc/apt/keyrings; \
    curl -fsSL https://dl.google.com/linux/linux_signing_key.pub \
      | gpg --dearmor -o /etc/apt/keyrings/google-linux.gpg; \
    echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/google-linux.gpg] https://dl.google.com/linux/chrome/deb/ stable main" \
      > /etc/apt/sources.list.d/google-chrome.list; \
    apt-get update; \
    apt-get install -y --no-install-recommends google-chrome-stable; \
    rm -rf /var/lib/apt/lists/*

# ---- Microsoft Edge estable (repo oficial con signed-by) ----
RUN set -eux; \
    install -m 0755 -d /etc/apt/keyrings; \
    curl -fsSL https://packages.microsoft.com/keys/microsoft.asc \
      | gpg --dearmor -o /etc/apt/keyrings/microsoft.gpg; \
    echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/microsoft.gpg] https://packages.microsoft.com/repos/edge stable main" \
      > /etc/apt/sources.list.d/microsoft-edge.list; \
    apt-get update; \
    apt-get install -y --no-install-recommends microsoft-edge-stable; \
    rm -rf /var/lib/apt/lists/*

# ---- Maven 3.9.11 (binario oficial) ----
RUN set -eux; \
    curl -fsSL "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
      -o /tmp/maven.tar.gz; \
    tar -xzf /tmp/maven.tar.gz -C /opt; \
    ln -s "/opt/apache-maven-${MAVEN_VERSION}" "${MAVEN_HOME}"; \
    ln -s "${MAVEN_HOME}/bin/mvn" /usr/local/bin/mvn; \
    rm /tmp/maven.tar.gz

# ---- Usuario no root (idempotente: solo crea si no existen) ----
ARG UID=1000
ARG GID=1000
RUN set -eux; \
    # Grupo: usa el existente si GID ya está ocupado
    if getent group "${GID}" >/dev/null; then \
        EXISTING_GRP="$(getent group "${GID}" | cut -d: -f1)"; \
        echo "GID ${GID} ya existe como grupo '${EXISTING_GRP}'"; \
    else \
        groupadd -g "${GID}" seluser; \
        EXISTING_GRP="seluser"; \
    fi; \
    # Usuario: crea solo si el UID no existe aún
    if getent passwd "${UID}" >/dev/null; then \
        EXISTING_USR="$(getent passwd "${UID}" | cut -d: -f1)"; \
        echo "UID ${UID} ya existe como usuario '${EXISTING_USR}'"; \
        # Asegura home y permisos (por si no es 'seluser')
        HOME_DIR="$(getent passwd "${UID}" | cut -d: -f6)"; \
        mkdir -p "${HOME_DIR}/project" "${HOME_DIR}/.m2"; \
        chown -R "${UID}:${GID}" "${HOME_DIR}"; \
        ln -snf "${HOME_DIR}" /home/seluser; \
    else \
        useradd -m -u "${UID}" -g "${GID}" -s /bin/bash seluser; \
        mkdir -p /home/seluser/project /home/seluser/.m2; \
        chown -R seluser:"${GID}" /home/seluser; \
    fi

# ---- Selenium Manager + tuning JVM/Maven ----
ENV GOOGLE_CHROME=/usr/bin/google-chrome \
    MSEDGE=/usr/bin/microsoft-edge \
    CHROME_BIN=/usr/bin/google-chrome \
    EDGE_BIN=/usr/bin/microsoft-edge \
    _JAVA_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -XX:+UseZGC -XX:+UseStringDeduplication" \
    MAVEN_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# ---- tini como entrypoint (manejo correcto de señales) ----
ENTRYPOINT ["/usr/bin/tini", "--"]

# ---- Directorio de trabajo y usuario ----
WORKDIR /home/seluser/project
USER ${UID}:${GID}

# ---- Comando por defecto (forma JSON para evitar la advertencia) ----
CMD ["/bin/bash","-lc","java -version && mvn -version && google-chrome --version && microsoft-edge --version"]