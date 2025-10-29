package api;

import config.EnvironmentConfig;
import ui.utils.LogUtil;
import config.TestContext;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Clase que agrupa los métodos necesarios para consumir los endpoints del perfil de usuario.
 */
public class UserProfileAPI {

    // --- Endpoints base ---
    private static final String baseUrl = EnvironmentConfig.getApiUrl();
    private static final String endpointGetUserProfile = "/user-profile/get-user-profile";

    /**
     * Realiza una petición POST al endpoint de perfil de usuario utilizando el token Bearer
     * previamente almacenado en el contexto de pruebas.
     *
     * @return Objeto {@link Response} que contiene la respuesta de la API.
     * @throws RuntimeException si ocurre un error al realizar la petición.
     */
    public static Response getUserProfile() {
        AuthenticationAPI.getToken();

        LogUtil.start("Obtener perfil de usuario");

        String token = TestContext.getValue("token");
        String fullUrl = baseUrl + endpointGetUserProfile;

        try {
            // Logging de la petición
            LogUtil.logRequest("POST", fullUrl, "Body vacío");

            // Ejecutar la petición
            Response response = RestAssured.given()
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "text/plain")
                    .header("Origin", "https://scp.imperiascm.com")
                    .header("Referer", "https://scp.imperiascm.com/materials")
                    .post(fullUrl);

            // Logging de la respuesta
            LogUtil.logResponse(response);
            LogUtil.end("Obtener perfil de usuario");

            return response;

        } catch (Exception e) {
            LogUtil.error("Error al obtener el perfil del usuario", e);
            throw new RuntimeException("Error al obtener el perfil del usuario: " + e.getMessage(), e);
        }
    }
}
