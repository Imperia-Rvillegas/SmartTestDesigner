package api;

import config.EnvironmentConfig;
import config.TestContext;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import ui.utils.LogUtil;

/**
 * Clase que agrupa los métodos necesarios para consumir los endpoints de previsiones.
 */
public class ForecastAPI {

    // --- Endpoints base ---
    private static final String baseUrl = EnvironmentConfig.getApiUrl();
    private static final String endpointGetMinimunLevelAgregation = "/configurations/get-minimun-level-agregation";

    /**
     * Realiza una petición POST al endpoint de nivel minimo de agregacion de usuario utilizando el token Bearer
     * previamente almacenado en el contexto de pruebas.
     *
     * @return Objeto {@link Response} que contiene la respuesta de la API.
     * @throws RuntimeException si ocurre un error al realizar la petición.
     */
    public static Response getMinimunLevelAgregation() {
        AuthenticationAPI.getToken();

        LogUtil.start("Obtener nivel minimo de agregacion ");

        String token = TestContext.getValue("token");
        String fullUrl = baseUrl + endpointGetMinimunLevelAgregation;

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
            LogUtil.end("Obtener nivel minimo de agregacion ");

            return response;

        } catch (Exception e) {
            LogUtil.error("Error al obtener el nivel minimo de agregacion ", e);
            throw new RuntimeException("Error al obtener el nivel minimo de agregacion : " + e.getMessage(), e);
        }
    }
}
