package reporting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ui.utils.LogUtil;

/**
 * Representa los metadatos mínimos de una ejecución de pruebas que se
 * comparten tanto por correo electrónico como por la integración con Xray.
 * Contiene información de la suite, ambiente, navegador, usuario, cliente,
 * keyClient, sprint y detalles del pipeline de CI.
 *
 * <p><strong>Registro (logging):</strong> esta clase utiliza {@code LogUtil}
 * para informar cuando se aplican valores por defecto y para emitir un
 * resumen final de los metadatos recopilados.</p>
 */
public final class ExecutionReportMetadata {

    private final String suite;
    private final String environment;
    private final String browser;
    private final String user;
    private final String cliente;
    private final String keyClient;
    private final String sprint;
    private final String pipelineNumber;
    private final String pipelineUrl;

    /**
     * Crea una instancia inmutable de metadatos de ejecución.
     *
     * @param suite           nombre de la suite de pruebas (obligatorio).
     * @param environment     ambiente de ejecución (obligatorio).
     * @param browser         navegador utilizado (obligatorio).
     * @param user            usuario que ejecuta o contexto (obligatorio).
     * @param cliente         cliente o proyecto asociado (obligatorio).
     * @param keyClient       clave/identificador del cliente (obligatorio).
     * @param sprint          sprint o iteración (obligatorio).
     * @param pipelineNumber  número del pipeline de CI (obligatorio).
     * @param pipelineUrl     URL del pipeline de CI (obligatorio).
     * @throws NullPointerException si algún parámetro es {@code null}.
     */
    private ExecutionReportMetadata(String suite,
                                    String environment,
                                    String browser,
                                    String user,
                                    String cliente,
                                    String keyClient,
                                    String sprint,
                                    String pipelineNumber,
                                    String pipelineUrl) {
        this.suite = Objects.requireNonNull(suite, "suite");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.browser = Objects.requireNonNull(browser, "browser");
        this.user = Objects.requireNonNull(user, "user");
        this.cliente = Objects.requireNonNull(cliente, "cliente");
        this.keyClient = Objects.requireNonNull(keyClient, "keyClient");
        this.sprint = Objects.requireNonNull(sprint, "sprint");
        this.pipelineNumber = Objects.requireNonNull(pipelineNumber, "pipelineNumber");
        this.pipelineUrl = Objects.requireNonNull(pipelineUrl, "pipelineUrl");
    }

    /**
     * Construye los metadatos tomando como fuente las propiedades del sistema,
     * las variables de entorno y, en última instancia, valores definidos en el
     * archivo de configuración.
     *
     * <p>Este metodo registra (vía {@code LogUtil}) un aviso si se aplican
     * valores por defecto y un resumen final de los metadatos resueltos.</p>
     *
     * @param settings configuración cargada desde {@link ReportSettings}.
     * @return metadatos listos para enviarse por correo o a Xray.
     * @throws NullPointerException si {@code settings} es {@code null}.
     */
    public static ExecutionReportMetadata collect(ReportSettings settings) {
        Objects.requireNonNull(settings, "settings");
        LogUtil.info("Recopilando metadatos de ejecución...");

        String suite = firstNonBlank(
                System.getProperty("suite"),
                System.getenv("SUITE"),
                settings.get("suite").orElse("suite"));

        String environment = firstNonBlank(
                System.getProperty("environment"),
                System.getenv("TEST_ENV"),
                "N/A");

        String browser = firstNonBlank(
                System.getProperty("browser"),
                System.getenv("BROWSER"),
                "N/A");

        String user = firstNonBlank(
                System.getProperty("user"),
                System.getenv("TEST_USER"),
                "N/A");

        String keyClient = firstNonBlank(
                System.getProperty("keyclient"),
                System.getenv("KEYCLIENT"),
                "N/A");

        String pipelineNumber = firstNonBlank(
                System.getProperty("ci.pipeline.number"),
                System.getenv("CI_PIPELINE_NUMBER"),
                System.getenv("BITBUCKET_BUILD_NUMBER"),
                "N/A");

        String pipelineUrl = firstNonBlank(
                System.getProperty("ci.pipeline.url"),
                System.getenv("CI_PIPELINE_URL"),
                buildDefaultPipelineUrl(settings, pipelineNumber));

        String cliente = firstNonBlank(
                System.getProperty("cliente"),
                System.getenv("CLIENTE"),
                "N/A");

        String sprint = firstNonBlank(
                System.getProperty("sprint"),
                System.getenv("SPRINT"),
                "N/A");

        // Aviso si se usaron valores por defecto.
        List<String> defaults = new ArrayList<>();
        if ("N/A".equalsIgnoreCase(environment)) defaults.add("environment");
        if ("N/A".equalsIgnoreCase(browser)) defaults.add("browser");
        if ("N/A".equalsIgnoreCase(user)) defaults.add("user");
        if ("N/A".equalsIgnoreCase(cliente)) defaults.add("cliente");
        if ("N/A".equalsIgnoreCase(keyClient)) defaults.add("keyClient");
        if ("N/A".equalsIgnoreCase(sprint)) defaults.add("sprint");
        if ("N/A".equalsIgnoreCase(pipelineNumber)) defaults.add("pipelineNumber");
        if ("N/A".equalsIgnoreCase(pipelineUrl)) defaults.add("pipelineUrl");
        if (!defaults.isEmpty()) {
            LogUtil.info("Valores por defecto aplicados para: " + String.join(", ", defaults));
        }

        // Resumen final para diagnóstico.
        LogUtil.info(String.format(
                "Metadatos: suite='%s', ambiente='%s', navegador='%s', usuario='%s', cliente='%s', keyClient='%s', sprint='%s', pipelineNumber='%s', pipelineUrl='%s'",
                displayValue(suite), displayValue(environment), displayValue(browser), displayValue(user),
                displayValue(cliente), displayValue(keyClient), displayValue(sprint),
                displayValue(pipelineNumber), displayValue(pipelineUrl)));

        return new ExecutionReportMetadata(
                suite,
                environment,
                browser,
                user,
                cliente,
                keyClient,
                sprint,
                pipelineNumber,
                pipelineUrl
        );
    }

    /**
     * Construye una URL por defecto del pipeline usando la base configurada en
     * {@link ReportSettings#getPipelineBaseUrl()} y el número del pipeline.
     *
     * @param settings       configuración para obtener la URL base.
     * @param pipelineNumber número del pipeline; si es {@code "N/A"}, retorna {@code "N/A"}.
     * @return URL completa del pipeline o {@code "N/A"} si no aplica.
     */
    private static String buildDefaultPipelineUrl(ReportSettings settings, String pipelineNumber) {
        if ("N/A".equalsIgnoreCase(pipelineNumber)) {
            return "N/A";
        }
        return settings.getPipelineBaseUrl() + pipelineNumber;
    }

    /**
     * Retorna el primer valor no nulo y no vacío (tras {@code trim}).
     *
     * @param values secuencia de candidatos.
     * @return el primer valor no vacío, o cadena vacía si todos lo son.
     */
    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    /**
     * Devuelve {@code "N/A"} si el valor es nulo o vacío; en caso contrario, retorna el valor original.
     *
     * @param value texto a evaluar.
     * @return valor original o {@code "N/A"} si está vacío.
     */
    private static String displayValue(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    /**
     * Ruta esperada del archivo Cucumber JSON generado por la suite.
     *
     * @return ruta en {@code target/<suite>.json}.
     */
    public Path cucumberJsonPath() {
        return Path.of("target", suite + ".json");
    }

    /**
     * Construye el asunto del correo replicando el formato utilizado por los
     * scripts históricos del proyecto.
     *
     * @return asunto listo para enviar mediante correo electrónico.
     */
    public String buildEmailSubject() {
        return String.format("Resultados %s", suite);
    }

    /**
     * Construye el cuerpo del correo con la información relevante de la
     * ejecución.
     *
     * @return texto plano con los detalles de la suite.
     */
    public String buildEmailBody() {
        String lineSeparator = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append("Suite: \"").append(suite).append("\"").append(lineSeparator)
                .append("Ambiente: \"").append(displayValue(environment)).append("\"").append(lineSeparator)
                .append("Navegador: \"").append(displayValue(browser)).append("\"").append(lineSeparator)
                .append("Usuario: \"").append(displayValue(user)).append("\"").append(lineSeparator)
                .append("Cliente: \"").append(displayValue(cliente)).append("\"").append(lineSeparator)
                .append("KeyClient: \"").append(displayValue(keyClient)).append("\"").append(lineSeparator)
                .append("Sprint: \"").append(displayValue(sprint)).append("\"").append(lineSeparator)
                .append("N° Pipeline: \"").append(displayValue(pipelineNumber)).append("\"").append(lineSeparator)
                .append(lineSeparator)
                .append("Este test fue ejecutado automáticamente por qa-automation-bot.").append(lineSeparator)
                .append("Ver detalles del pipeline:").append(lineSeparator)
                .append(displayValue(pipelineUrl));
        return builder.toString();
    }

    /**
     * Construye la descripción multilinea utilizada en Xray para documentar la
     * ejecución.
     *
     * @return cadena lista para ser enviada en el campo {@code description}.
     */
    public String buildXrayDescription() {
        return buildEmailBody();
    }

    /**
     * Construye el resumen utilizado en Xray siguiendo el formato
     * suite/cliente/keyClient/navegador/ambiente/usuario/sprint.
     *
     * @return cadena formateada con los metadatos principales.
     */
    public String buildXraySummary() {
        return String.join(
                "/",
                displayValue(suite),
                displayValue(cliente),
                displayValue(keyClient),
                displayValue(browser),
                displayValue(environment),
                displayValue(user),
                displayValue(sprint)
        );
    }

    /**
     * @return nombre de la suite de pruebas.
     */
    public String getSuite() {
        return suite;
    }

    /**
     * @return ambiente de ejecución (por ejemplo, QA, UAT, PROD).
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * @return navegador utilizado (por ejemplo, Chrome, Firefox).
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * @return usuario asociado a la ejecución o contexto.
     */
    public String getUser() {
        return user;
    }

    /**
     * @return clave/identificador del cliente.
     */
    public String getKeyClient() {
        return keyClient;
    }

    /**
     * @return número del pipeline de CI.
     */
    public String getPipelineNumber() {
        return pipelineNumber;
    }

    /**
     * @return URL del pipeline de CI.
     */
    public String getPipelineUrl() {
        return pipelineUrl;
    }

    /**
     * @return cliente o proyecto asociado.
     */
    public String getCliente() {
        return cliente;
    }

    /**
     * @return sprint o iteración de trabajo.
     */
    public String getSprint() {
        return sprint;
    }
}