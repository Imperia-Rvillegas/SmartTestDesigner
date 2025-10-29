# Workflow de regresión en GitHub Actions

Este documento describe el flujo de trabajo `Regression` definido en `.github/workflows/regression.yml`. El objetivo del workflow es ejecutar suites de pruebas automatizadas desde GitHub Actions, permitiendo seleccionar el entorno, navegador, usuario de datos, suite y runner, así como decidir si la ejecución será secuencial o paralela. También puede publicar los resultados en Xray cuando se requieren métricas de trazabilidad.

## Requisitos previos

Antes de ejecutar el workflow, confirma lo siguiente:

- La imagen de contenedor `rvillegasvalera/qa-java24-browsers-v2:latest` está disponible en el registro configurado para GitHub Actions.
- Existen los secretos de repositorio `XRAY_CLIENT_ID` y `XRAY_CLIENT_SECRET` para autenticar contra la API de Xray Cloud.
- Se definió la variable `XRAY_PROJECT_KEY` (en la organización o el repositorio) con la clave del proyecto de Jira/Xray donde se registrarán las ejecuciones.
- El `pom.xml` genera reportes de Cucumber en `target/<suite>.json` y `target/<suite>.html`.
- Los runners self-hosted cuentan con Docker instalado o con permisos para instalarlo durante la ejecución.

## Cómo lanzar el workflow

1. En GitHub, ve a **Actions → Regression**.
2. Haz clic en **Run workflow** y completa los parámetros deseados (son opcionales).
3. Presiona nuevamente **Run workflow** para iniciar la ejecución.
4. Una vez que el workflow finalice, descarga los artefactos publicados para revisar reportes HTML, JSON, logs y capturas.

## Parámetros de entrada

Los siguientes inputs permiten ajustar la ejecución:

| Parámetro | Tipo | Valores permitidos | Descripción |
|-----------|------|--------------------|-------------|
| `environment` | Texto | Libre | URL o identificador del entorno (por ejemplo `qa`, `staging`, `https://qa.miapp.com`). |
| `suite` | Texto | Nombre de suite | Ejecuta una única suite. Si se deja vacío, se lanzan todas las definidas en la matriz. |
| `browser` | Selección | `edge`, `chrome` | Navegador utilizado por Selenium. |
| `user` | Selección | `rvillegas`, `lnieves`, `staging2`, `test2` | Usuario lógico que las pruebas leerán desde los datos de prueba. |
| `sendXrayReport` | Booleano | `true`, `false` | Cuando es `true`, publica los resultados en Xray. |
| `parallel` | Booleano | `true`, `false` | Si está en `true`, permite ejecutar múltiples suites en paralelo. |
| `maxParallel` | Texto | Número entero positivo | Límite máximo de suites simultáneas al usar ejecución paralela (por defecto `10`). |
| `runner` | Selección | `self-hosted`, `ubuntu-latest` | Runner donde se ejecutarán los jobs. Usa `self-hosted` para conservar minutos de GitHub Actions. |

> **Nota:** Si `suite` tiene un valor, la ejecución ignora la matriz y lanza únicamente la suite indicada.

## Flujo de jobs

El workflow consta de un único job principal:

- **run-suites**: ejecuta cada suite definida en la matriz (o la suite individual solicitada). Este job realiza el checkout del código, restaura el caché de dependencias de Maven con `actions/cache`, configura Java 17 mediante `actions/setup-java`, asegura la presencia de Docker en runners self-hosted, lanza Maven dentro del contenedor configurado, empaqueta reportes y evidencias y, opcionalmente, importa resultados a Xray.

El parámetro `parallel` controla si las suites de la matriz se ejecutan en paralelo. Cuando está habilitado, `maxParallel` define el número máximo de suites simultáneas.

> **Nota:** La limpieza de artefactos y cachés ahora se gestiona desde el workflow [Actions Maintenance](actions-maintenance-workflow.md), lo que permite ejecutarla únicamente cuando sea necesario sin afectar la ejecución de pruebas. Si se borran los cachés, la siguiente ejecución tardará unos minutos adicionales en restaurar las dependencias de Maven desde cero.

## Suites disponibles

Si no se indica la entrada `suite`, el workflow ejecuta de forma secuencial o paralela las siguientes suites:

- `Articles`
- `AssociatedDimensions`
- `AssociatedUnitArticleFlow`
- `BusinessDimensions`
- `CommercialBudget`
- `Configuration`
- `ForecastAnalysisList`
- `ForecastedMaterialRequirements`
- `ForecastingConcepts`
- `InventoryHealth`
- `InventoryLocations`
- `Inventory`
- `ListOfMaterials`
- `MpsMrpReport`
- `PendingOrders`
- `PotentialDiscontinued`
- `PotentialLaunches`
- `ProcessConfigList`
- `ProductionAlerts`
- `ProductionPlan`
- `ProductionSequencing`
- `Production`
- `ProjectionOfStockNeeds`
- `PurchaseAlerts`
- `PurchasePlan`
- `SalesHistory`
- `Sequencer`
- `SubstituteProducts`
- `SupplierMaterialList`
- `TrackingChangesBetweenCycles`
- `TrackingChangesInTheSameCycle`
- `Unit`
- `Forecasts`
- `Forecasts02`
- `Forecasts03`
- `Forecasts05`

## Artefactos y publicación en Xray

- En todas las ejecuciones se publica un artefacto denominado `resultados-<suite>` con un archivo comprimido `target/artifacts/<suite>-report.tar.gz` que incluye los reportes HTML y JSON disponibles, los logs (`target/logs/<suite>-<runId>.log`) y las capturas almacenadas en `target/screenshots/`.
- Cuando `sendXrayReport` es `true`, el paso **Importar resultados a Xray** sube el archivo `target/<suite>.json` y crea un Test Execution con los metadatos definidos en `target/xray-testexec.json`. El resumen del Test Execution incluye la suite, el entorno, el navegador, el usuario lógico y los identificadores de ejecución de GitHub (`runId`, `runNumber`).

## Consejos de operación

- Si una suite falla, revisa el log específico en `target/logs/<suite>-<runId>.log`, descargándolo desde los artefactos del workflow.
- Al habilitar la ejecución paralela, ajusta `maxParallel` según la capacidad del entorno y las restricciones de tu plan de GitHub Actions.
- Si el paso de importación a Xray se omite, verifica que `sendXrayReport` se haya establecido en `true` y que los secretos requeridos estén presentes.
- Para runners `self-hosted`, confirma que el usuario tenga permisos de sudo para instalar y ejecutar Docker en caso de que no esté disponible.
