package config;

import org.junit.Assume;
import org.junit.Test;

/**
 * Prueba de soporte para la restauración de la base de datos de clientes.
 * <p>
 * Esta clase valida la ejecución condicional de la restauración de la base de datos
 * dependiendo de la presencia de la variable <b>KEYCLIENT</b>, que identifica al cliente
 * sobre el cual deben ejecutarse las pruebas.
 * </p>
 *
 * <h2>Comportamiento principal</h2>
 * <ul>
 *   <li>Si <b>KEYCLIENT</b> no está definido ni como propiedad del sistema
 *       ({@code -Dkeyclient}) ni como variable de entorno ({@code KEYCLIENT}),
 *       la prueba se omite de forma controlada usando
 *       {@link org.junit.Assume#assumeTrue(String, boolean)}.</li>
 *   <li>Si <b>KEYCLIENT</b> está presente, se propagan además las variables de entorno
 *       {@code TEST_ENV} y {@code TEST_USER} hacia las propiedades del sistema
 *       {@code env} y {@code user} respectivamente, en caso de no estar definidas.</li>
 *   <li>Se invoca a la clase {@link config.EnvironmentConfig}, cuyo bloque
 *       estático es responsable de ejecutar la lógica de recuperación
 *       de la base de datos de pruebas asociada al cliente.</li>
 * </ul>
 *
 * <h2>Notas</h2>
 * <ul>
 *   <li>El test nunca falla si falta la variable <b>KEYCLIENT</b>,
 *       simplemente se marca como omitido. Esto asegura que la pipeline
 *       continúe sin interrupciones.</li>
 *   <li>Existen dos opciones para disparar la recuperación:
 *       <ol>
 *           <li>Configurar la propiedad {@code -Dkeyclient} y cargar {@code EnvironmentConfig}
 *               (opción actualmente implementada).</li>
 *           <li>Llamar directamente a {@code EnvironmentConfig.recoverTestDb(key)}
 *               (comentada como alternativa).</li>
 *       </ol>
 *   </li>
 * </ul>
 *
 * <h2>Ejemplo de ejecución en pipeline</h2>
 * <pre>
 * mvn test -Dkeyclient=CLIENTE123 -Denv=pre -Duser=qa_user
 * </pre>
 *
 * @author TuNombre
 * @version 1.0
 * @since 2025
 */
public class RecoverDbTest {

    /**
     * Ejecuta la restauración de la base de datos solo si está presente la variable
     * <b>KEYCLIENT</b>, ya sea como propiedad del sistema ({@code -Dkeyclient})
     * o como variable de entorno ({@code KEYCLIENT}).
     * <p>
     * Si la variable no está definida, la prueba se omite
     * sin generar fallo en la pipeline.
     * </p>
     *
     * @throws Exception si ocurre algún problema al cargar {@link config.EnvironmentConfig}.
     */
    @Test
    public void recoverOnlyIfKeyclientPresent() throws Exception {
        // Lee KEYCLIENT desde -Dkeyclient o env KEYCLIENT
        String key = System.getProperty("keyclient");
        if (key == null || key.isBlank()) key = System.getenv("KEYCLIENT");

        // Si no hay KEYCLIENT -> se omite el test (no falla el pipeline)
        Assume.assumeTrue("Sin KEYCLIENT -> se omite restauración.", key != null && !key.isBlank());

        // Propaga ENV/USER si vienen por variables de entorno del pipeline
        if (System.getProperty("env") == null && System.getenv("TEST_ENV") != null) {
            System.setProperty("env", System.getenv("TEST_ENV"));
        }
        if (System.getProperty("user") == null && System.getenv("TEST_USER") != null) {
            System.setProperty("user", System.getenv("TEST_USER"));
        }

        // Opción A: dispara el static de EnvironmentConfig (respeta tu lógica de -Dkeyclient)
        System.setProperty("keyclient", key);           // para que loadKeyClient() lo vea
        Class.forName("config.EnvironmentConfig");      // carga la clase -> ejecuta static -> hace recover si hay keyclient

        // (Opción B alternativa: llamar directo sin tocar -Dkeyclient)
        // EnvironmentConfig.recoverTestDb(key);
    }
}
