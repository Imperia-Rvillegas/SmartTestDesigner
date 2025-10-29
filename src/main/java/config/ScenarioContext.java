package config;

import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import java.io.File;

/**
 * Contenedor de datos por escenario de Cucumber.
 *
 * <p>Esta clase permite compartir objetos y resultados entre distintos
 * step definitions durante la ejecución de un mismo escenario sin “contaminar”
 * otros escenarios. Debe inyectarse/instanciarse con un alcance de escenario
 * (scope = scenario) a través del mecanismo de DI que utilices.</p>
 *
 * <h3>Responsabilidades</h3>
 * <ul>
 *   <li>Almacenar la última {@link Response} de pruebas API (RestAssured).</li>
 *   <li>Exponer el {@link Scenario} actual de Cucumber para logging/evidencias.</li>
 *   <li>Gestionar referencia al último archivo descargado mediante {@link DownloadContext}.</li>
 * </ul>
 *
 * <h3>Uso típico</h3>
 * <pre>{@code
 * // En un step que hace una llamada API
 * Response resp = given()...when()...then().extract().response();
 * scenarioContext.setResponse(resp);
 *
 * // En otro step del mismo escenario
 * Response last = scenarioContext.getResponse();
 * assertThat(last.getStatusCode()).isEqualTo(200);
 *
 * // Guardar/leer la última descarga
 * ScenarioContext.DownloadContext.set(descargado);
 * File evidencia = ScenarioContext.DownloadContext.get();
 * }</pre>
 *
 * <h3>Notas de concurrencia</h3>
 * <ul>
 *   <li>Los campos {@code response} y {@code scenario} son de instancia y, por tanto,
 *       seguros si el {@code ScenarioContext} tiene scope de escenario.</li>
 *   <li><strong>Atención:</strong> {@link DownloadContext} mantiene un campo estático:
 *       su valor se comparte entre todos los escenarios del mismo proceso/JVM. Si ejecutas
 *       escenarios en paralelo, planifica sincronización o considera una refactorización
 *       para que sea por escenario.</li>
 * </ul>
 */
public class ScenarioContext {

    /** Última respuesta HTTP obtenida en el contexto del escenario (RestAssured). */
    private Response response;

    /** Escenario actual de Cucumber, útil para adjuntar evidencias y registrar información. */
    private Scenario scenario;

    // === REST Response ===

    /**
     * Devuelve la última {@link Response} almacenada en el contexto del escenario.
     *
     * @return respuesta o {@code null} si aún no se ha establecido.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Establece la {@link Response} asociada al escenario actual.
     *
     * @param response respuesta HTTP de una operación de prueba.
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Contexto utilitario para la última descarga detectada durante el escenario.
     * <p>
     * <strong>Alcance:</strong> Los valores se almacenan en un {@link ThreadLocal}, por lo que
     * cada hilo mantiene su propia referencia al último archivo descargado.
     * </p>
     */
    public static final class DownloadContext {
        /** Contenedor del último archivo descargado para el hilo/escenario actual. */
        private static final ThreadLocal<File> LAST_DOWNLOADED = new ThreadLocal<>();

        private DownloadContext() {
        }

        /**
         * Guarda la referencia al último archivo descargado.
         *
         * @param f archivo descargado (puede ser {@code null} para limpiar el valor).
         */
        public static void set(File f) {
            if (f == null) {
                LAST_DOWNLOADED.remove();
            } else {
                LAST_DOWNLOADED.set(f);
            }
        }

        /**
         * Devuelve la referencia al último archivo descargado.
         *
         * @return archivo o {@code null} si no se ha registrado ninguno.
         */
        public static File get() {
            return LAST_DOWNLOADED.get();
        }
    }

    // === Cucumber Scenario ===

    /**
     * Devuelve el {@link Scenario} actual para logging y adjunto de evidencias.
     *
     * @return escenario en curso o {@code null} si aún no se ha inicializado.
     */
    public Scenario getScenario() {
        return scenario;
    }

    /**
     * Establece el {@link Scenario} actual de Cucumber.
     *
     * @param scenario instancia del escenario provista por Cucumber.
     */
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
}
