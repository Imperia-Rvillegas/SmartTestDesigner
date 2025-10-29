package api;

import config.EnvironmentConfig;
import ui.utils.LogUtil;
import config.TestContext;
import config.JsonTestDataReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de gestionar la autenticación del usuario mediante el consumo
 * del endpoint de login. Obtiene y almacena el token Bearer para su reutilización
 * en futuras peticiones.
 */
public class AuthenticationAPI {

    /**
     * Devuelve el token de autenticación actual. Si no existe en el contexto de pruebas
     * o si han pasado más de 30 minutos desde su obtención, se solicita uno nuevo.
     *
     * @return Token Bearer como cadena de texto.
     */
    public static String getToken() {
        String token = TestContext.getValue("token");
        String tokenTimestampStr = TestContext.getValue("token_timestamp");

        boolean shouldRefresh = false;

        if (token == null || tokenTimestampStr == null) {
            LogUtil.info("No se encontró token o ya expiró. Iniciando autenticación...");
            shouldRefresh = true;
        } else {
            long tokenTimestamp = Long.parseLong(tokenTimestampStr);
            long currentTime = System.currentTimeMillis();
            long elapsedMinutes = (currentTime - tokenTimestamp) / (1000 * 60);

            LogUtil.info("⏱ Tiempo transcurrido desde la obtención del token: " + elapsedMinutes + " minutos");

            if (elapsedMinutes >= 30) {
                LogUtil.info("🔁 Token expirado (más de 30 minutos). Se solicitará uno nuevo.");
                shouldRefresh = true;
            } else {
                LogUtil.info("🧾 Token válido encontrado. Se reutiliza.");
            }
        }

        if (shouldRefresh) {
            authenticate();
            token = TestContext.getValue("token");
            TestContext.setValue("token_timestamp", String.valueOf(System.currentTimeMillis()));
            LogUtil.info("⏱ Nuevo timestamp del token registrado.");
        }

        return token;
    }


    /**
     * Realiza una solicitud al endpoint de autenticación utilizando las credenciales
     * configuradas en el archivo YAML. El token obtenido se almacena en TestContext
     * para ser reutilizado en otras operaciones.
     */
    private static void authenticate() {
        LogUtil.start("Autenticación");

        // 1. Cargar configuración de ambiente
        String email = EnvironmentConfig.getEmail();
        String password = EnvironmentConfig.getPassword();
        String baseUrl = EnvironmentConfig.getApiUrl();
        String endpoint = "/authentication/authenticate";
        String fullUrl = baseUrl + endpoint;

        // 2. Preparar body de la solicitud con datos dinámicos
        Map<String, String> replacements = new HashMap<>();
        replacements.put("email", email);
        replacements.put("password", password);

        try {
            String body = JsonTestDataReader.getRequestBody("authenticateBody.json", replacements);

            // 3. Log de la petición HTTP
            LogUtil.logRequest("POST", fullUrl, body);

            // 4. Realizar la petición y validar respuesta
            Response response = RestAssured.given()
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Content-Type", "application/json")
                    .header("Origin", "https://scp.imperiascm.com")
                    .header("Referer", "https://scp.imperiascm.com/auth/login")
                    .body(body)
                    .post(fullUrl)
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            // 5. Log de la respuesta y almacenamiento del token
            LogUtil.logResponse(response);

            String token = response.jsonPath().getString("Token");
            TestContext.setValue("token", token);
            LogUtil.info("Token obtenido correctamente y almacenado en el contexto.");

        } catch (Exception e) {
            LogUtil.error("Error al obtener el token.", e);
            throw new RuntimeException("Error al obtener el token: " + e.getMessage(), e);
        }

        LogUtil.end("Autenticación");
    }
}
