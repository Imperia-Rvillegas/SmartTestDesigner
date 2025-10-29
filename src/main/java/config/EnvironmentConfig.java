package config;

import org.yaml.snakeyaml.Yaml;
import ui.utils.LogUtil;

import java.io.InputStream;
import java.util.Map;

/**
 * Clase encargada de cargar:
 * <ul>
 *     <li>Configuración del entorno desde un archivo YAML ubicado en <code>src/test/resources/environments</code>.</li>
 *     <li>Credenciales de usuario desde <code>src/test/resources/users</code>.</li>
 *     <li>Opcionalmente, ejecutar una restauración de base de datos para un cliente de pruebas usando el endpoint <code>/support-configuration-utilities/recover-test-db</code>.</li>
 * </ul>
 * <p>
 * El entorno y usuario se definen mediante las variables del sistema:
 * <ul>
 *     <li><code>-Denv</code> o variable de entorno <code>TEST_ENV</code></li>
 *     <li><code>-Duser</code> o variable de entorno <code>TEST_USER</code></li>
 * </ul>
 * El cliente de pruebas es opcional y se pasa mediante <code>-Dkeyclient</code>.
 */
public class EnvironmentConfig {

    private static Map<String, Object> config;
    private static Map<String, Object> userConfig;
    private static String environment;
    private static String user;
    private static String defaultUser = "rvillegas";
    private static String defaultEnvironment = "production.dev";

    static {
        loadEnvironment();
        loadUser();
        loadKeyClient();
    }

    /**
     * Configura dinámicamente las URLs de API y Web con base en el valor de `env`.
     * El dominio base se construye según las siguientes reglas:
     *
     * <ul>
     *   <li>Si no se proporciona ningún valor (ni como propiedad del sistema ni como variable de entorno), se usará "pre".</li>
     *   <li>Si el valor de `env` es exactamente "pro", se usará el dominio sin subdominio: {@code scp.imperiascm.com}.</li>
     *   <li>Para cualquier otro valor, se antepondrá como subdominio: {@code <env>.scp.imperiascm.com}.</li>
     * </ul>
     *
     * Las URLs generadas son:
     * <pre>
     *  - https://<dominio>/api
     *  - https://<dominio>/auth
     * </pre>
     *
     * Ejemplos:
     * <ul>
     *   <li>env no definido → {@code pre.scp.imperiascm.com}</li>
     *   <li>env=pro        → {@code scp.imperiascm.com}</li>
     *   <li>env=qa         → {@code qa.scp.imperiascm.com}</li>
     * </ul>
     */
    private static void loadEnvironment() {
        // Obtener entorno de variable de sistema o de entorno, usar "pre" como valor por defecto
        environment = System.getProperty("env", System.getenv("TEST_ENV"));
        if (environment == null || environment.trim().isEmpty()) {
            environment = defaultEnvironment;
        }

        LogUtil.start("Configuración dinámica de URLs de ambiente");
        LogUtil.info("Ambiente seleccionado: " + environment);

        // Determinar dominio según la lógica definida
        String domain;
        if ("pro".equalsIgnoreCase(environment)) {
            domain = "scp.imperiascm.com";
        } else {
            domain = environment + ".scp.imperiascm.com";
        }

        // Asignar URLs al mapa `config`
        config = new java.util.HashMap<>();
        config.put("api_url", "https://" + domain + "/api");
        config.put("web_url", "https://" + domain + "/auth");

        LogUtil.info("API URL: " + getApiUrl());
        LogUtil.info("WEB URL: " + getWebUrl());
        LogUtil.end("Configuración dinámica de URLs de ambiente");
    }

    /**
     * Carga el archivo YAML del usuario desde la carpeta <code>resources/users</code>,
     * basándose en el valor de <code>-Duser</code> o <code>TEST_USER</code>.
     * Si no se define, se usa <code>rvillegas</code> por defecto.
     */
    private static void loadUser() {
        user = System.getProperty("user", System.getenv("TEST_USER"));
        if (user == null || user.isEmpty()) {
            user = defaultUser;
        }

        String userFile = "users/" + user + ".yaml";

        LogUtil.start("Carga de configuración de usuario");
        LogUtil.info("Usuario seleccionado: " + user);
        LogUtil.info("Archivo de usuario: " + userFile);

        try (InputStream inputStream = EnvironmentConfig.class.getClassLoader().getResourceAsStream(userFile)) {
            if (inputStream == null) {
                throw new RuntimeException("No se encontró el archivo de usuario: " + user);
            }

            Yaml yaml = new Yaml();
            userConfig = yaml.load(inputStream);
            LogUtil.info("Configuración del usuario cargada exitosamente.");

        } catch (Exception e) {
            LogUtil.error("Error al cargar la configuración del usuario.", e);
            throw new RuntimeException("Error al cargar configuración del usuario: " + e.getMessage(), e);
        }

        LogUtil.end("Carga de configuración de usuario");
    }

    /**
     * Si se especifica la propiedad <code>-Dkeyclient</code>, ejecuta la restauración de la base de datos
     * para el cliente de pruebas indicado. Si no se especifica, se omite el paso.
     */
    private static void loadKeyClient() {
        String keyClient = System.getProperty("keyclient");
        if (keyClient != null && !keyClient.trim().isEmpty()) {
            LogUtil.start("Configuración de cliente de pruebas");
            LogUtil.info("Cliente de pruebas seleccionado: " + keyClient);
            recoverTestDb(keyClient);
            LogUtil.end("Configuración de cliente de pruebas");
        } else {
            LogUtil.info("No se seleccionó ningún cliente de pruebas con -Dkeyclient");
        }
    }

    /**
     * Ejecuta una autenticación en el endpoint <code>/authentication/authenticate</code> y
     * luego llama al endpoint <code>/support-configuration-utilities/recover-test-db</code>
     * con el token obtenido, para restaurar la base de datos de pruebas del cliente indicado.
     *
     * @param keyClient clave del cliente de pruebas (ejemplo: "10406", "10313").
     */
    public static void recoverTestDb(String keyClient) {
        LogUtil.start("Inicio de recuperación de base de datos de pruebas");
        try {
            String email = getEmail();
            String password = getPassword();
            String authUrl = getApiUrl() + "/authentication/authenticate";
            String recoverUrl = getApiUrl() + "/support-configuration-utilities/recover-test-db";

            // 1. Autenticación
            String authPayload = String.format("{\"Email\":\"%s\",\"Password\":\"%s\"}", email, password);
            java.net.URL url = new java.net.URL(authUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "java-http-client");
            connection.setDoOutput(true);
            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(authPayload.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Falló la autenticación. Código HTTP: " + responseCode);
            }

            java.io.InputStream is = connection.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            String responseBody = s.hasNext() ? s.next() : "";
            s.close();

            // 2. Parseo del token
            String token;
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode =
                        new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
                token = jsonNode.get("Token").asText();
            } catch (Exception e) {
                throw new RuntimeException("No se pudo extraer el token de la respuesta: " + responseBody, e);
            }

            LogUtil.info("Token de autenticación obtenido exitosamente.");

            // 3. Recuperación de base de datos
            java.net.URL recoverDbUrl = new java.net.URL(recoverUrl);
            java.net.HttpURLConnection recoverConn = (java.net.HttpURLConnection) recoverDbUrl.openConnection();
            recoverConn.setRequestMethod("POST");
            recoverConn.setRequestProperty("Authorization", "Bearer " + token);
            recoverConn.setRequestProperty("Content-Type", "application/json");
            recoverConn.setDoOutput(true);

            String recoverPayload = "\"" + keyClient + "\""; // JSON string plano

            try (java.io.OutputStream os = recoverConn.getOutputStream()) {
                os.write(recoverPayload.getBytes());
            }

            int recoverResponse = recoverConn.getResponseCode();
            if (recoverResponse != 200) {
                java.io.InputStream errorStream = recoverConn.getErrorStream();
                if (errorStream != null) {
                    java.util.Scanner scanner = new java.util.Scanner(errorStream).useDelimiter("\\A");
                    String errorBody = scanner.hasNext() ? scanner.next() : "";
                    scanner.close();
                    LogUtil.error("Error en recuperación de DB. Respuesta: " + errorBody, null);
                }
                throw new RuntimeException("Falló la recuperación de base de datos. Código HTTP: " + recoverResponse);
            }

            LogUtil.info("Recuperación de base de datos para el cliente '" + keyClient + "' completada con éxito.");
        } catch (Exception e) {
            LogUtil.error("Error durante la recuperación de base de datos de pruebas.", e);
            throw new RuntimeException("Error en recoverTestDb: " + e.getMessage(), e);
        }
        LogUtil.end("Fin de recuperación de base de datos de pruebas");
    }

    /** @return URL base del API (ej: https://pre.scp.imperiascm.com/api) */
    public static String getApiUrl() {
        return (String) config.get("api_url");
    }

    /** @return URL base de la interfaz web (ej: https://pre.scp.imperiascm.com/auth) */
    public static String getWebUrl() {
        return (String) config.get("web_url");
    }

    /** @return Email configurado para autenticación */
    public static String getEmail() {
        return (String) userConfig.get("email");
    }

    /** @return Password configurado para autenticación */
    public static String getPassword() {
        return (String) userConfig.get("password");
    }
}