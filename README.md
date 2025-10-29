# QA Automation Bot

QA Automation Bot es un framework modular para automatizar pruebas funcionales de interfaz de usuario y servicios API utilizando Java, Cucumber, Selenium y RestAssured. El proyecto adopta una arquitectura basada en Page Object Model, integra utilidades para manejo de datos y reportes, y está preparado para ejecutarse en distintos ambientes dentro de pipelines de integración continua.

## Documentación

La documentación completa se encuentra en la carpeta [`docs/manual`](docs/manual):

- [Características principales](docs/manual/features.md)
- [Estructura del proyecto](docs/manual/project-structure.md)
- [Guía de inicio rápido](docs/manual/getting-started.md)
- [Creación de casos de prueba UI](docs/manual/ui-test-case-guide.md)
- [Etiquetas de escenarios de Cucumber](docs/manual/scenario-tags.md)
- [Uso de datos de prueba con POJO y YAML](docs/manual/test-data.md)
- [Buenas prácticas de implementación](docs/manual/best-practices.md)
- [Reportes y publicación de resultados](docs/manual/reporting.md)
- [Generación de JavaDoc](docs/manual/javadoc.md)
- [Flujo de llamadas del framework](docs/manual/architecture-flow.md)
- [Propiedades de ejecución](docs/manual/system-properties.md)
- [Workflow de regresión en GitHub Actions](docs/manual/regression-workflow.md)
- [Workflow de mantenimiento en GitHub Actions](docs/manual/actions-maintenance-workflow.md)
- [Sincronización de escenarios con Xray](docs/manual/xray-synchronization.md)
- [Configuración del entorno Codex](docs/manual/codex-environment.md)

## Tecnologías clave

- Java 24+
- Apache Maven
- Selenium WebDriver
- Cucumber
- RestAssured
- Jira Xray
