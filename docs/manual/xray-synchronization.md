# Sincronización de escenarios con Xray

Esta guía describe el flujo automatizado para comparar los escenarios definidos en el repositorio con sus equivalentes en Xray Cloud y, en caso de encontrar diferencias, actualizar los casos BDD de manera controlada mediante un workflow de GitHub Actions.

## Requisitos previos

1. **Credenciales de Xray**: registra los secretos `XRAY_CLIENT_ID` y `XRAY_CLIENT_SECRET` en la configuración del repositorio. Ambas credenciales deben corresponder a un cliente con permiso para consultar y actualizar pruebas BDD.
2. **Variables opcionales**: define los valores `XRAY_PROJECT_KEY` y `XRAY_API_BASE_URL` como variables del repositorio si necesitas apuntar a un proyecto predeterminado o a un entorno distinto del cloud público.
3. **Etiquetado de escenarios**: cada escenario de Cucumber debe incluir un tag con el formato `@PROYECTO-123` (por ejemplo, `@TEST_DEV-7339`). El script utiliza esa etiqueta para vincular el escenario local con el test existente en Xray.

## Script de comparación y actualización

El comando principal se encuentra en `scripts/sync-xray-scenarios.js` y también puede invocarse mediante `npm run sync:xray`. Sus responsabilidades son:

- Analizar todos los archivos `.feature` y construir un listado de escenarios con su contenido Gherkin.
- Consultar a Xray, mediante GraphQL, los escenarios asociados a cada tag `@PROYECTO-<id>`.
- Comparar el Gherkin remoto con el del repositorio y generar un resumen con el estado de cada escenario.
- Importar en Xray los archivos `.feature` que presenten diferencias o pruebas inexistentes, utilizando el endpoint oficial `/import/feature`.

### Ejecución local

```
XRAY_CLIENT_ID=<id> \
XRAY_CLIENT_SECRET=<secret> \
node scripts/sync-xray-scenarios.js --dry-run
```

- `--dry-run` permite revisar el resumen sin enviar cambios a Xray.
- Usa `--feature-dir <ruta>` si necesitas limitar la comparación a un subconjunto de funcionalidades.

## Workflow de GitHub Actions

El workflow [`sync-xray-scenarios.yml`](../../.github/workflows/sync-xray-scenarios.yml) expone el proceso anterior como una acción manual (`workflow_dispatch`). Al ejecutarlo podrás parametrizar:

- `feature-path`: ruta donde se buscarán los archivos `.feature` (por defecto `src/test/resources/features`).
- `dry-run`: cuando se establece en `true`, la acción sólo reporta las diferencias sin importar los archivos a Xray.

Cada ejecución genera un resumen en la sección **Step Summary** de GitHub Actions con el estado de los escenarios procesados y registra en los logs qué archivos se importaron. Aprovecha este informe para auditar cambios masivos antes de actualizar los casos en Xray.

## Buenas prácticas

- Mantén un único escenario por clave de Xray. El script falla de forma explícita si detecta duplicados, para evitar sobrescrituras accidentales.
- Revisa el resultado del modo `--dry-run` antes de lanzar una importación real, especialmente cuando se hayan refactorizado varias funcionalidades.
- Conserva las etiquetas funcionales y de módulos junto a la etiqueta de Xray; ambas se preservan cuando se sube el Gherkin actualizado y facilitan los filtros dentro del repositorio.
