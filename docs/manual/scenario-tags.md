# Etiquetas de escenarios

Esta referencia resume las etiquetas utilizadas en los archivos `.feature` para organizar y filtrar la ejecución de escenarios en Cucumber. Todas las etiquetas se definen en la cabecera del archivo o directamente sobre cada escenario y pueden combinarse en las ejecuciones mediante la propiedad `-Dcucumber.filter.tags`.

## Etiquetas de suite

Estas etiquetas se aplican a nivel de archivo y permiten activar subconjuntos amplios de pruebas.

| Etiqueta | Alcance | Descripción | Ejemplo de uso |
| --- | --- | --- | --- |
| `@all` | Archivo completo | Incluye el archivo `.feature` en la ejecución general por defecto. Se utiliza en todas las suites para habilitar la ejecución estándar. | `mvn test -Dcucumber.filter.tags="@all"` |
| `@ui` | Archivo completo | Identifica archivos de pruebas orientadas a interfaz gráfica. Se combina con `@all` en las features dentro de `uiFeatures`. | `mvn test -Dcucumber.filter.tags="@ui"` |
| `@api` | Archivo completo | Etiqueta exclusiva de archivos que validan servicios REST. Agrupa las features ubicadas en `apiFeatures`. | `mvn test -Dcucumber.filter.tags="@api"` |

## Etiquetas por módulo funcional

Cada archivo UI agrega una etiqueta con el nombre del módulo bajo prueba. Esto permite ejecutar únicamente los escenarios vinculados a un área específica de la aplicación.

| Etiqueta | Feature asociada | Descripción |
| --- | --- | --- |
| `@articles` | `uiFeatures/articles.feature` | Escenarios de mantenimiento de artículos y búsquedas asociadas. |
| `@associatedDimensions` | `uiFeatures/associatedDimensions.feature` | Gestión de dimensiones asociadas. |
| `@associatedUnitArticleFlow` | `uiFeatures/associatedUnitArticleFlow.feature` | Validaciones del flujo de asociación de unidades a artículos. |
| `@businessDimensions` | `uiFeatures/businessDimensions.feature` | Administración de dimensiones de negocio. |
| `@commercialBudget` | `uiFeatures/commercialBudget.feature` | Reportes del presupuesto comercial. |
| `@configuration` | `uiFeatures/configuration.feature` | Configuración general del módulo de pronósticos. |
| `@forecastAnalysisList` | `uiFeatures/forecastAnalysisList.feature` | Reportes de análisis de pronóstico. |
| `@forecastedMaterialRequirements` | `uiFeatures/forecastedMaterialRequirements.feature` | Necesidades de materiales pronosticadas. |
| `@forecastingConcepts` | `uiFeatures/forecastingConcepts.feature` | Conceptos utilizados en el módulo de pronósticos. |
| `@inventory` | `uiFeatures/inventory.feature` | Tableros de inventario consolidado. |
| `@inventoryHealth` | `uiFeatures/inventoryHealth.feature` | Indicadores de salud de inventario. |
| `@inventoryLocations` | `uiFeatures/inventoryLocations.feature` | Administración de ubicaciones de inventario. |
| `@listOfMaterials` | `uiFeatures/listOfMaterials.feature` | Listado y operaciones sobre listas de materiales. |
| `@mpsMrpReport` | `uiFeatures/mpsmrpReport.feature` | Reportes MPS/MRP. |
| `@origins` | `uiFeatures/origins.feature` | Gestión de orígenes de artículos. |
| `@pendingOrders` | `uiFeatures/pendingOrders.feature` | Consulta de órdenes pendientes. |
| `@potentialDiscontinued` | `uiFeatures/potentialDiscontinued.feature` | Reportes de potenciales descontinuados. |
| `@potentialLaunches` | `uiFeatures/potentialLaunches.feature` | Reportes de potenciales lanzamientos. |
| `@processConfigList` | `uiFeatures/processConfigList.feature` | Configuración de procesos y listas asociadas. |
| `@production` | `uiFeatures/production.feature` | Reportes y operaciones de producción. |
| `@productionAlerts` | `uiFeatures/productionAlerts.feature` | Alertas relacionadas con producción. |
| `@productionPlan` | `uiFeatures/productionPlan.feature` | Generación y seguimiento del plan de producción. |
| `@productionSequencing` | `uiFeatures/productionSequencing.feature` | Secuenciación de producción. |
| `@projectionOfStockNeeds` | `uiFeatures/projectionOfStockNeeds.feature` | Proyección de necesidades de stock. |
| `@provisioning` | `uiFeatures/provisioning.feature` | Flujos de aprovisionamiento. |
| `@purchaseAlerts` | `uiFeatures/purchaseAlerts.feature` | Alertas de compra. |
| `@purchasePlan` | `uiFeatures/purchasePlan.feature` | Planificación de compras. |
| `@salesHistory` | `uiFeatures/salesHistory.feature` | Reportes de historial de ventas. |
| `@sequencer` | `uiFeatures/sequencer.feature` | Secuenciador de órdenes. |
| `@substituteProducts` | `uiFeatures/substituteProducts.feature` | Gestión de productos sustitutos. |
| `@supplierMaterialList` | `uiFeatures/supplierMaterialList.feature` | Listas de materiales por proveedor. |
| `@trackingChangesBetweenCycles` | `uiFeatures/trackingChangesBetweenCycles.feature` | Seguimiento de cambios entre ciclos distintos. |
| `@trackingChangesInTheSameCycle` | `uiFeatures/trackingChangesInTheSameCycle.feature` | Seguimiento de cambios dentro de un mismo ciclo. |
| `@unit` | `uiFeatures/unit.feature` | Administración de unidades de medida. |
| `@userProfile` | `apiFeatures/userProfile.feature` | Validaciones sobre el perfil del usuario vía API. |

## Etiquetas funcionales recurrentes

Las siguientes etiquetas se asignan a escenarios individuales para clasificar el tipo de validación que realizan.

| Etiqueta | Descripción |
| --- | --- |
| `@export` | Marca escenarios que verifican la exportación de datos o reportes. |
| `@calculation` | Identifica escenarios que recalculan información o indicadores. |
| `@creation` / `@creacion` | Escenarios de creación de registros (español e inglés según el caso). |
| `@edition` / `@edicion` | Escenarios de edición o actualización de registros. |
| `@deletion` / `@eliminacion` | Escenarios de eliminación de registros. |
| `@validation` | Validaciones generales sin cambios de estado. |
| `@navigation` | Recorridos de navegación por la interfaz. |
| `@limit` | Verifica límites o restricciones específicas. |
| `@generic` | Escenarios reutilizables o representativos que se ejecutan en distintas suites. |
| `@regression` | Conjunto de pruebas incluidas en la suite de regresión. |
| `@smoke` | Escenarios críticos que forman parte de la suite de smoke testing. |
| `@positivo` | Escenarios con resultados exitosos esperados. |
| `@search`, `@filter_code`, `@adjustColumns`, `@units`, etc. | Etiquetas descriptivas que indican la funcionalidad puntual validada dentro del módulo. |

## Identificadores y trazabilidad

| Etiqueta | Descripción |
| --- | --- |
| `@<modulo><NN>` (por ejemplo, `@articles01`) | Enumera los escenarios dentro de un mismo archivo. Facilita la referencia puntual en reportes. |
| `@TEST_DEV-<id>` | Vincula el escenario con la historia o bug correspondiente en Jira Xray. |
| Otras etiquetas específicas (por ejemplo, `@getProfile`) | Señalan la operación concreta que se ejecuta sobre el servicio o módulo. |

Al combinar estas etiquetas, se pueden construir suites personalizadas sin modificar el código fuente. Por ejemplo, para ejecutar únicamente los escenarios de exportación del módulo de aprovisionamiento se puede utilizar `-Dcucumber.filter.tags="@provisioning and @export"`.
