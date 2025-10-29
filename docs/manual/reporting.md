# Reportes y Publicación de Resultados

El framework genera reportes automáticos que documentan cada ejecución de pruebas.

## Archivos generados

- `target/{nombre-de-la-suite}.html`
- `target/{nombre-de-la-suite}.json`

Los reportes pueden enviarse automáticamente a Jira Xray como parte de los pipelines de integración continua.

## Envío automático de reportes

El hook `hooks.ReportingLifecycleHook#publishReports` coordina el envío de reportes al finalizar la ejecución de Cucumber. Según las propiedades de sistema definidas, es posible activar:

- Envío de un resumen por correo electrónico.
- Publicación del reporte Cucumber JSON en Xray Cloud.

### Configuración local

1. Copiar `src/main/resources/reporting/reporting-settings.example.properties` como `reporting-settings.properties` en la misma carpeta.
2. Completar las credenciales SMTP y de Xray en el archivo creado.
3. Agregar las siguientes propiedades al ejecutar Maven:
   - `-DsendEmailReport=true` para enviar el correo.
   - `-DsendXrayReport=true` para publicar en Xray.

Si no se especifican estas propiedades, los reportes solo se generan en disco.
