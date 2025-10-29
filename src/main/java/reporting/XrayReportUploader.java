package reporting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ui.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Publica en Xray Cloud los resultados Cucumber de la suite ejecutada.
 *
 * <p>Características clave:</p>
 * <ul>
 *   <li>El envío solo se realiza cuando la propiedad de sistema {@link #SEND_XRAY_PROPERTY}
 *       se establece en {@code true} (por ejemplo, {@code -DsendXrayReport=true}).</li>
 *   <li>Espera a que el archivo JSON de resultados esté <b>estable</b> antes de subirlo,
 *       evitando condiciones de carrera (archivo truncado o aún en escritura).</li>
 *   <li>Valida que el archivo JSON tenga forma de reporte Cucumber (raíz array no vacío).</li>
 *   <li>Incluye un <b>reintento</b> automático si Xray responde con “Error parsing results file!”.</li>
 *   <li>Evita loguear el JSON completo en los logs; en su lugar, registra tamaño y mtime.</li>
 * </ul>
 *
 * <p>Toda la configuración sensible (credenciales, baseUrl y proyecto) se obtiene desde {@link ReportSettings}.</p>
 */
public final class XrayReportUploader {

    /**
     * Propiedad de sistema que habilita el envío del reporte a Xray cuando su
     * valor es {@code true}. En IntelliJ/CLI puede configurarse como
     * {@code -DsendXrayReport=true}.
     */
    public static final String SEND_XRAY_PROPERTY = "sendXrayReport";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ReportSettings settings;
    private final HttpClient httpClient;

    /**
     * Crea un publicador utilizando el cliente HTTP por defecto.
     *
     * @param settings configuración que aporta credenciales y parámetros de Xray.
     * @throws NullPointerException si {@code settings} es {@code null}.
     */
    public XrayReportUploader(ReportSettings settings) {
        this(settings, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    /**
     * Ctor visible para tests, permite inyectar un {@link HttpClient} propio.
     *
     * @param settings   configuración con credenciales y parámetros.
     * @param httpClient cliente HTTP a utilizar para las llamadas.
     * @throws NullPointerException si algún parámetro es {@code null}.
     */
    XrayReportUploader(ReportSettings settings, HttpClient httpClient) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /**
     * Envía a Xray el archivo Cucumber JSON asociado a la suite indicada en la metadata.
     *
     * <p>Flujo:</p>
     * <ol>
     *     <li>Verifica propiedad {@value #SEND_XRAY_PROPERTY}.</li>
     *     <li>Espera a que el archivo se <b>estabilice</b> (tamaño/mtime constantes).</li>
     *     <li>Valida que el JSON sea un array no vacío (formato Cucumber).</li>
     *     <li>Autentica y sube el multipart con partes <em>results</em> (JSON) e <em>info</em> (metadata).</li>
     *     <li>Si Xray responde 400 con “Error parsing results file!”, reintenta una vez.</li>
     * </ol>
     *
     * @param metadata metadatos de la ejecución actual (ruta al JSON, suite, descripción, etc.).
     * @throws NullPointerException   si {@code metadata} es {@code null}.
     * @throws IllegalStateException  si el archivo no existe, no se estabiliza a tiempo,
     *                                el JSON no tiene el formato esperado o Xray responde con error.
     */
    public void uploadCucumberResults(ExecutionReportMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");

        if (!Boolean.parseBoolean(System.getProperty(SEND_XRAY_PROPERTY, "false"))) {
            LogUtil.infof("Propiedad {} deshabilitada. No se publicará el reporte en Xray.", SEND_XRAY_PROPERTY);
            return;
        }

        Path jsonReport = metadata.cucumberJsonPath();

        waitUntilFileExists(jsonReport, Duration.ofSeconds(30), Duration.ofMillis(200));
        if (!Files.exists(jsonReport)) {
            LogUtil.error("No se encontró el archivo de resultados: " + jsonReport, null);
            throw new IllegalStateException("No se encontró el archivo de resultados " + jsonReport);
        }

        // 1) Esperar a que el archivo esté “estable” (mitiga carreras).
        waitUntilFileIsStable(jsonReport, Duration.ofSeconds(100), Duration.ofMillis(200));

        // 2) Validar mínimo que el JSON “luzca” como Cucumber (raíz array y no vacío).
        assertCucumberJsonLooksOk(jsonReport);

        String baseUrl = settings.get("xray.baseUrl").orElse("https://xray.cloud.getxray.app");
        String clientId = settings.getRequired("xray.clientId");
        String clientSecret = settings.getRequired("xray.clientSecret");
        String projectKey = settings.getRequired("xray.projectKey");

        LogUtil.infof("Subiendo resultados de '{}' a Xray (proyecto {}).",
                jsonReport.getFileName(), projectKey);

        // Autenticación
        String accessToken = obtainAccessToken(baseUrl, clientId, clientSecret);

        // Construcción de metadata "info" (Test Execution)
        String infoPayload = buildInfoPayload(metadata, projectKey);

        // Crear y enviar multipart
        String boundary = buildBoundary();
        byte[] multipartBody = createMultipartBody(jsonReport, infoPayload, boundary);
        HttpRequest request = buildMultipartRequest(baseUrl, accessToken, boundary, multipartBody);

        HttpResponse<String> response = send(request, safeBodyPreviewForLogs(jsonReport, infoPayload, boundary));

        // 3) Reintento (p.ej. si justo se terminó de escribir el JSON y Xray lo vio truncado).
        if (isXrayParseError(response)) {
            LogUtil.info("Reintentando upload a Xray tras 1s porque el archivo podría no haber estado listo…");
            sleep(Duration.ofSeconds(1));
            waitUntilFileIsStable(jsonReport, Duration.ofSeconds(10), Duration.ofMillis(200));
            assertCucumberJsonLooksOk(jsonReport);

            boundary = buildBoundary(); // nuevo boundary por limpieza
            multipartBody = createMultipartBody(jsonReport, infoPayload, boundary);
            request = buildMultipartRequest(baseUrl, accessToken, boundary, multipartBody);

            response = send(request, safeBodyPreviewForLogs(jsonReport, infoPayload, boundary));
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            LogUtil.infof("Reporte de la suite '{}' publicado correctamente en Xray.", metadata.getSuite());
        } else {
            LogUtil.error("Error al subir el reporte a Xray (" + response.statusCode() + "): " + response.body(), null);
            throw new IllegalStateException("Error al subir el reporte a Xray (" + response.statusCode() + "): " + response.body());
        }
    }

    /**
     * Realiza la autenticación en Xray Cloud (Client Credentials).
     *
     * @param baseUrl      base URL de Xray Cloud (p.ej. {@code https://xray.cloud.getxray.app}).
     * @param clientId     {@code client_id} provisto por Xray.
     * @param clientSecret {@code client_secret} provisto por Xray.
     * @return token de acceso (JWT) como {@link String}.
     * @throws IllegalStateException si no se puede serializar el payload, fallan las llamadas
     *                               HTTP o la respuesta no es 2xx / no contiene un token válido.
     */
    private String obtainAccessToken(String baseUrl, String clientId, String clientSecret) {
        Map<String, String> payload = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret
        );

        String body;
        try {
            body = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            LogUtil.error("No fue posible serializar el payload de autenticación", e);
            throw new IllegalStateException("No fue posible serializar el payload de autenticación", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v2/authenticate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = send(request, body);

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            LogUtil.error("Error al autenticarse en Xray (" + response.statusCode() + "): " + response.body(), null);
            throw new IllegalStateException("Error al autenticarse en Xray (" + response.statusCode() + "): " + response.body());
        }

        try {
            // El servicio devuelve un string JSON con el token entre comillas.
            return OBJECT_MAPPER.readValue(response.body(), String.class);
        } catch (JsonProcessingException e) {
            LogUtil.error("No fue posible leer el token devuelto por Xray", e);
            throw new IllegalStateException("No fue posible leer el token devuelto por Xray", e);
        }
    }

    /**
     * Construye el payload {@code info} (parte del multipart) con los campos del Test Execution.
     *
     * @param metadata   metadatos de la ejecución (resumen/descr).
     * @param projectKey key del proyecto en Jira/Xray.
     * @return JSON con la estructura {@code {"fields": {...}}}.
     * @throws IllegalStateException si no es posible serializar la estructura.
     */
    private String buildInfoPayload(ExecutionReportMetadata metadata, String projectKey) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("project", Map.of("key", projectKey));
        fields.put("issuetype", Map.of("name", settings.getXrayIssueType()));
        fields.put("summary", metadata.buildXraySummary());
        fields.put("description", metadata.buildXrayDescription());

        Map<String, Object> info = Map.of("fields", fields);

        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(info);
        } catch (JsonProcessingException e) {
            LogUtil.error("No fue posible serializar la metadata de Xray", e);
            throw new IllegalStateException("No fue posible serializar la metadata de Xray", e);
        }
    }

    /**
     * Crea el cuerpo multipart con dos partes:
     * <ul>
     *   <li><b>results</b>: archivo JSON de Cucumber</li>
     *   <li><b>info</b>: JSON con campos del Test Execution</li>
     * </ul>
     *
     * @param jsonReport  ruta del archivo de resultados Cucumber.
     * @param infoPayload JSON {@code info} (ya serializado).
     * @param boundary    boundary para el multipart.
     * @return arreglo de bytes listo para enviar en la petición.
     * @throws IllegalStateException si ocurre un error de E/S al construir el cuerpo.
     */
    private byte[] createMultipartBody(Path jsonReport, String infoPayload, String boundary) {
        String CRLF = "\r\n";
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            // Parte "results" (Cucumber JSON)
            output.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            output.write(("Content-Disposition: form-data; name=\"results\"; filename=\"" +
                    jsonReport.getFileName() + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            output.write(("Content-Type: application/json" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
            Files.copy(jsonReport, output);
            output.write(CRLF.getBytes(StandardCharsets.UTF_8));

            // Parte "info" (metadata)
            output.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            output.write(("Content-Disposition: form-data; name=\"info\"; filename=\"info.json\"" + CRLF)
                    .getBytes(StandardCharsets.UTF_8));
            output.write(("Content-Type: application/json" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
            output.write(infoPayload.getBytes(StandardCharsets.UTF_8));
            output.write(CRLF.getBytes(StandardCharsets.UTF_8));

            // Cierre
            output.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));

            return output.toByteArray();

        } catch (IOException e) {
            LogUtil.error("No fue posible construir el cuerpo multipart para Xray", e);
            throw new IllegalStateException("No fue posible construir el cuerpo multipart para Xray", e);
        }
    }

    /**
     * Envía una petición HTTP y registra metodo, URL, estado y cuerpo de respuesta.
     *
     * @param request           petición ya construida.
     * @param requestBodyForLog cuerpo a registrar (usar preview seguro, no el binario real).
     * @return respuesta completa como {@code HttpResponse<String>}.
     * @throws IllegalStateException si ocurre un error de E/S o si el hilo es interrumpido.
     */
    private HttpResponse<String> send(HttpRequest request, String requestBodyForLog) {
        try {
            LogUtil.logRequest(request.method(), request.uri().toString(), requestBodyForLog);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LogUtil.info("RESPUESTA HTTP");
            LogUtil.infof("Código de estado: {}", response.statusCode());
            LogUtil.infof("Cuerpo de la respuesta:\n{}", response.body());
            return response;
        } catch (IOException e) {
            LogUtil.error("Error de comunicación con Xray", e);
            throw new IllegalStateException("Error de comunicación con Xray", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogUtil.error("La llamada HTTP a Xray fue interrumpida", e);
            throw new IllegalStateException("La llamada HTTP a Xray fue interrumpida", e);
        }
    }

    // ========= Helpers de robustez / logging =========

    /**
     * Espera (hasta {@code timeout}) a que el archivo no cambie ni de tamaño ni de mtime.
     *
     * @param path    archivo a vigilar.
     * @param timeout tiempo máximo de espera.
     * @param poll    intervalo entre comprobaciones.
     * @throws IllegalStateException si el archivo no se estabiliza a tiempo.
     */
    private void waitUntilFileIsStable(Path path, Duration timeout, Duration poll) {
        final long deadline = System.nanoTime() + timeout.toNanos();
        long lastSize = -1;
        FileTime lastMod = null;

        while (System.nanoTime() < deadline) {
            try {
                long size = Files.size(path);
                FileTime mod = Files.getLastModifiedTime(path);

                if (size > 0 && size == lastSize && Objects.equals(mod, lastMod)) {
                    // Estable (dos lecturas consecutivas iguales)
                    return;
                }

                lastSize = size;
                lastMod = mod;
                Thread.sleep(poll.toMillis());
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    LogUtil.error("Espera de estabilidad interrumpida para: " + path, e);
                    break;
                }
                // IO transitoria: seguir intentando hasta timeout
            }
        }

        LogUtil.error("El archivo no se estabilizó a tiempo: " + path, null);
        throw new IllegalStateException("El archivo " + path + " no se estabilizó a tiempo (posible escritura en curso).");
    }

    /**
     * Espera (hasta {@code timeout}) a que el archivo exista en disco.
     *
     * @param path    ruta que se espera que aparezca.
     * @param timeout tiempo máximo de espera.
     * @param poll    intervalo entre comprobaciones.
     * @throws IllegalStateException si el archivo no aparece a tiempo.
     */
    private void waitUntilFileExists(Path path, Duration timeout, Duration poll) {
        final long deadline = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadline) {
            if (Files.exists(path)) {
                return;
            }
            sleep(poll);
        }

        LogUtil.error("El archivo no existe tras esperar: " + path, null);
        throw new IllegalStateException("El archivo " + path + " no existe tras esperar " + timeout + ".");
    }

    /**
     * Valida que el JSON en {@code path} sea un array no vacío (formato Cucumber clásico).
     *
     * @param path ruta del archivo JSON a validar.
     * @throws IllegalStateException si el JSON no es un array o está vacío, o no puede parsearse.
     */
    private void assertCucumberJsonLooksOk(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            JsonNode root = OBJECT_MAPPER.readTree(in);
            if (!root.isArray()) {
                LogUtil.error("El archivo de resultados no es un array JSON (Cucumber esperado): " + path, null);
                throw new IllegalStateException("El archivo de resultados no es un array JSON (formato Cucumber esperado).");
            }
            if (root.size() == 0) {
                LogUtil.error("El archivo de resultados está vacío: " + path, null);
                throw new IllegalStateException("El archivo de resultados está vacío.");
            }
        } catch (IOException e) {
            LogUtil.error("No fue posible leer/parsear el JSON de resultados: " + path, e);
            throw new IllegalStateException("No fue posible leer/parsear el JSON de resultados.", e);
        }
    }

    /**
     * Genera un boundary único para el multipart.
     *
     * @return boundary aleatorio para separar partes del multipart.
     */
    private String buildBoundary() {
        return "----XrayBoundary" + UUID.randomUUID();
    }

    /**
     * Construye la {@link HttpRequest} con headers adecuados para el multipart.
     *
     * @param baseUrl       URL base de Xray.
     * @param accessToken   token de acceso (Bearer).
     * @param boundary      boundary del multipart.
     * @param multipartBody cuerpo ya construido en formato multipart.
     * @return petición HTTP lista para enviarse.
     */
    private HttpRequest buildMultipartRequest(String baseUrl, String accessToken, String boundary, byte[] multipartBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v2/import/execution/cucumber/multipart"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();
    }

    /**
     * Devuelve un “preview seguro” del cuerpo multipart para logs, sustituyendo el contenido
     * real del JSON por metadatos (tamaño/mtime) para no saturar los logs ni exponer datos.
     *
     * @param jsonReport  ruta del reporte JSON.
     * @param infoPayload JSON de info ya serializado (visible en logs).
     * @param boundary    boundary del multipart.
     * @return texto multi-línea con el contenido simulado para logging.
     */
    private String safeBodyPreviewForLogs(Path jsonReport, String infoPayload, String boundary) {
        try {
            long size = Files.size(jsonReport);
            FileTime mod = Files.getLastModifiedTime(jsonReport);
            String CRLF = "\r\n";

            return new StringBuilder()
                    .append("--").append(boundary).append(CRLF)
                    .append("Content-Disposition: form-data; name=\"results\"; filename=\"")
                    .append(jsonReport.getFileName()).append("\"").append(CRLF)
                    .append("Content-Type: application/json").append(CRLF).append(CRLF)
                    .append("[contenido JSON elidido] (size=").append(size)
                    .append(" bytes, mtime=").append(mod).append(")").append(CRLF)
                    .append("--").append(boundary).append(CRLF)
                    .append("Content-Disposition: form-data; name=\"info\"; filename=\"info.json\"").append(CRLF)
                    .append("Content-Type: application/json").append(CRLF).append(CRLF)
                    .append(infoPayload).append(CRLF)
                    .append("--").append(boundary).append("--").append(CRLF)
                    .toString();
        } catch (IOException e) {
            return "(no se pudo obtener preview del cuerpo multipart: " + e.getMessage() + ")";
        }
    }

    /**
     * Heurística para detectar el error típico de parseo de Xray:
     * respuesta 400 con el mensaje “Error parsing results file”.
     *
     * @param response respuesta HTTP recibida.
     * @return {@code true} si coincide con el patrón de error, en otro caso {@code false}.
     */
    private boolean isXrayParseError(HttpResponse<String> response) {
        if (response == null) return false;
        if (response.statusCode() != 400) return false;
        String body = response.body();
        return body != null && body.toLowerCase().contains("error parsing results file");
    }

    /**
     * Sleep sin checked exception. Mantiene el estado de interrupción.
     *
     * @param d duración del {@code sleep}.
     */
    private void sleep(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}