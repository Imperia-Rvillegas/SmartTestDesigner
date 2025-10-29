package reporting;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import ui.utils.LogUtil;

/**
 * Carga y expone la configuración necesaria para enviar reportes externos.
 *
 * <p>Los valores se leen desde un archivo de propiedades llamado
 * {@code reporting-settings.properties} ubicado en el mismo paquete. El archivo
 * no forma parte del control de versiones para evitar exponer credenciales.
 * Si no está presente, se utiliza una configuración vacía y los componentes
 * consumidores deberán reaccionar en consecuencia.</p>
 *
 * <p><strong>Registro (logging):</strong> esta clase utiliza {@code LogUtil}
 * para informar si la configuración fue localizada/cargada y para registrar
 * fallos de lectura del archivo.</p>
 */
public final class ReportSettings {

    /**
     * Nombre del archivo de configuración que contiene credenciales y valores
     * sensibles. Debe ubicarse en el mismo paquete ({@code reporting}).
     */
    public static final String SETTINGS_FILE = "reporting-settings.properties";

    private static final String DEFAULT_PIPELINE_BASE_URL =
            "https://bitbucket.org/imperia-scm/qa-automation-bot/pipelines/results/";

    private final Properties properties;

    private ReportSettings(Properties properties) {
        this.properties = properties;
    }

    /**
     * Crea una instancia leyendo el archivo de propiedades local si está
     * disponible. En caso contrario se obtiene una instancia con valores vacíos.
     *
     * @return instancia de {@link ReportSettings} lista para consultar valores.
     * @throws IllegalStateException si ocurre un error de E/S al leer el archivo.
     */
    public static ReportSettings load() {
        return new ReportSettings(loadProperties());
    }

    /**
     * Lee el archivo {@value #SETTINGS_FILE} desde el classpath y construye
     * un {@link Properties} con su contenido.
     *
     * <p>Registra con {@code LogUtil} si el archivo no se encuentra (se usará
     * configuración vacía) o si fue cargado correctamente.</p>
     *
     * @return propiedades cargadas; vacías si el archivo no existe.
     * @throws IllegalStateException si no es posible leer el archivo por un error de E/S.
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = ReportSettings.class.getResourceAsStream(SETTINGS_FILE)) {
            if (in == null) {
                LogUtil.info(String.format(
                        "No se encontró %s en el classpath. Se utilizarán valores vacíos.",
                        SETTINGS_FILE));
            } else {
                properties.load(in);
                LogUtil.info(String.format(
                        "Configuración de reporting cargada desde %s",
                        SETTINGS_FILE));
            }
        } catch (IOException e) {
            LogUtil.error("No fue posible leer " + SETTINGS_FILE, e);
            throw new IllegalStateException("No fue posible leer " + SETTINGS_FILE, e);
        }
        return properties;
    }

    /**
     * Obtiene un valor textual opcional.
     *
     * @param key clave a consultar dentro del archivo.
     * @return {@link Optional} con el valor, vacío si no existe o está en blanco.
     * @throws NullPointerException si {@code key} es {@code null}.
     */
    public Optional<String> get(String key) {
        Objects.requireNonNull(key, "key");
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }

    /**
     * Obtiene el valor asociado a la clave especificada y lanza una excepción
     * si no está configurado.
     *
     * @param key clave obligatoria.
     * @return valor asociado a la clave.
     * @throws IllegalStateException si no existe o está vacío.
     */
    public String getRequired(String key) {
        return get(key).orElseThrow(() ->
                new IllegalStateException("Falta configurar la propiedad '" + key + "' en " + SETTINGS_FILE));
    }

    /**
     * Lee una propiedad booleana.
     *
     * @param key          clave a consultar.
     * @param defaultValue valor por defecto en caso de ausencia.
     * @return valor booleano resultante.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return get(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    /**
     * Lee una propiedad numérica entera.
     *
     * @param key          clave a consultar.
     * @param defaultValue valor a retornar en caso de que no exista.
     * @return entero representando la propiedad.
     * @throws IllegalStateException si el valor configurado no es un entero válido.
     */
    public int getInt(String key, int defaultValue) {
        return get(key).map(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("El valor de '" + key + "' debe ser numérico", e);
            }
        }).orElse(defaultValue);
    }

    /**
     * Obtiene una lista de valores separada por coma, punto y coma o espacios.
     *
     * @param key clave a consultar.
     * @return lista de elementos; vacía si la clave no existe o está en blanco.
     */
    public List<String> getList(String key) {
        return get(key)
                .map(value -> Arrays.stream(value.split("[,;\\s]+"))
                        .map(String::trim)
                        .filter(item -> !item.isEmpty())
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Obtiene la URL base para construir vínculos a pipelines cuando no se
     * proporciona explícitamente una. Si no se configura se utiliza el valor
     * predeterminado del repositorio QA Automation Bot.
     *
     * @return URL base para los resultados del pipeline.
     */
    public String getPipelineBaseUrl() {
        return get("pipeline.baseUrl").orElse(DEFAULT_PIPELINE_BASE_URL);
    }

    /**
     * Tipo de issue que se debe utilizar al crear ejecuciones en Xray.
     *
     * @return nombre del tipo de issue; por defecto {@code Test Execution}.
     */
    public String getXrayIssueType() {
        return get("xray.issueType").orElse("Test Execution");
    }
}
