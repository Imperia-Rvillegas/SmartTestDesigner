package api;

import config.EnvironmentConfig;
import config.TestContext;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import ui.utils.LogUtil;

/**
 * Clase que agrupa los métodos necesarios para consumir los endpoints de unidades.
 */
public class UnitsApi {
    // --- Endpoints base ---
    private static final String baseUrl = EnvironmentConfig.getApiUrl();
    private static final String endpointGetUnitsList = "/units/get-list";

    /**
     * Realiza una petición POST al endpoint de lista de unidades utilizando el token Bearer
     * previamente almacenado en el contexto de pruebas.
     *
     * @return Objeto {@link Response} que contiene la respuesta de la API.
     * @throws RuntimeException si ocurre un error al realizar la petición.
     */
    public static Response getUnitsList() {
        AuthenticationAPI.getToken();

        LogUtil.start("Obtener lista de unidades");

        String token = TestContext.getValue("token");
        String fullUrl = baseUrl + endpointGetUnitsList;

        // Definir el body como un String en formato JSON
        String requestBody = """
                {
                  "Filters": [],
                  "Order": {
                    "Column": "",
                    "Sort": ""
                  },
                  "Pagination": {
                    "Page": 1,
                    "Size": 100
                  },
                  "Search": ""
                }
                """;

        try {
            // Logging de la petición
            LogUtil.logRequest("POST", fullUrl, requestBody);

            // Ejecutar la petición
            Response response = RestAssured.given()
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Origin", "https://scp.imperiascm.com")
                    .header("Referer", "https://scp.imperiascm.com/materials")
                    .body(requestBody)
                    .post(fullUrl);

            // Logging de la respuesta
            LogUtil.logResponse(response);
            LogUtil.end("Obtener lista de unidades");

            return response;

        } catch (Exception e) {
            LogUtil.error("Error al obtener la lista de unidades", e);
            throw new RuntimeException("Error al obtener la lista de unidades: " + e.getMessage(), e);
        }
    }

}
