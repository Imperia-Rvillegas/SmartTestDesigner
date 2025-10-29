# Configuración del entorno Codex

Este procedimiento prepara una máquina Linux para ejecutar las suites automatizadas del proyecto mediante Codex.

1. Clona el repositorio y sitúate en la raíz del proyecto.
2. Ejecuta `scripts/setup-codex-env.sh` con un usuario que tenga privilegios de `sudo`.
3. Reinicia la sesión para que `JAVA_HOME`, `MAVEN_HOME` y `CI=true` queden disponibles.

El script instala Java 24 (Temurin), Maven 3.13.0, Google Chrome, Microsoft Edge, Xvfb y utilidades adicionales como Git, `mutt` y `msmtp`, asegurando compatibilidad con las tareas de compilación, ejecución de pruebas y envío de reportes.
