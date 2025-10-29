# Propiedades de ejecución

Este documento resume las principales propiedades del sistema que se pueden pasar al ejecutar los comandos de Maven del proyecto. Todas ellas se suministran con la sintaxis `-Dnombre=valor` y, salvo que se indique lo contrario, toman prioridad sobre las variables de entorno.

| Propiedad | Propósito | Valores habituales | Valor por defecto | Notas relevantes |
|-----------|-----------|--------------------|-------------------|------------------|
| `-Dtest` | Restringe las clases o métodos de prueba que se ejecutan mediante Surefire. | Nombre de clase (`UiTest`), patrón (`UiTest#escenario`), o lista separada por comas. | Ejecuta todo el conjunto definido en `pom.xml`. | Utilizada por los ejemplos de `getting-started` y por `scripts/run-suite.sh`. |
| `-Denv` | Define el ambiente objetivo para construir las URLs de API y Web. | `pre`, `qa`, `dev`, `pro`, u otro subdominio válido. | `pgarcia.dev` (configurado en `EnvironmentConfig`). | Puede definirse también como variable de entorno `TEST_ENV`. El valor `pro` omite el subdominio. |
| `-Duser` | Selecciona el archivo de credenciales en `src/test/resources/users`. | Identificador de usuario sin extensión, por ejemplo `rvillegas`. | `rvillegas`. | Acepta la variable de entorno alternativa `TEST_USER`. |
| `-Dkeyclient` | Indica el cliente de pruebas cuya base debe restaurarse antes de iniciar las pruebas. | Clave numérica como `10273` o alfanumérica según la cuenta configurada. | No restaura ningún cliente si se omite. | Si se usa, `EnvironmentConfig` llama al endpoint `/support-configuration-utilities/recover-test-db`. |
| `-Dbrowser` | Escoge el navegador administrado por `DriverFactory`. | `chrome` o `edge`. | `chrome`. | El script `run-suite.sh` reenvía esta propiedad. |
| `-Dheadless` | Fuerza la ejecución de navegadores en modo sin interfaz. | `true` o `false`. | `false`. | También se activa automáticamente si la variable de entorno `CI` vale `true`. |
| `-DsendEmailReport` | Envía el reporte HTML por correo al finalizar la suite. | `true` para habilitarlo. | No envía correos. | El procesamiento se realiza en `reporting.EmailReportSender`. |
| `-DsendXrayReport` | Publica los resultados en Xray tras la ejecución. | `true` para habilitarlo. | No publica resultados. | El manejo se implementa en `reporting.XrayReportUploader`. |

Para combinaciones más comunes y ejemplos de uso revisa la guía de [Getting Started](./getting-started.md) y el script [`scripts/run-suite.sh`](../../scripts/run-suite.sh), que encapsulan la ejecución estándar del proyecto.
