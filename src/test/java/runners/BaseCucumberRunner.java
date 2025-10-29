package runners;

import org.junit.BeforeClass;

/**
 * Clase base para los runners de Cucumber que configura propiedades del sistema
 * antes de la ejecución de las pruebas.
 * <p>
 * - Prioriza valores definidos con {@code -D}.<br>
 * - Si no existen, intenta usar variables de entorno (HEADLESS, TEST_ENV, TEST_USER, BROWSER, KEYCLIENT,
 *   SEND_EMAIL_REPORT, SEND_XRAY_REPORT).<br>
 * - No aplica valores por defecto.
 * <p>
 * Ejemplos:
 *   -Dheadless=true
 *   -Denv=qa
 *   -Duser=usuario1
 *   -Dbrowser=chrome
 *   -Dkeyclient=abc123
 *   -DsendEmailReport=true
 *   -DsendXrayReport=true
 */
public abstract class BaseCucumberRunner {

    /**
     * Configura las propiedades necesarias para la suite de pruebas.
     * <p>
     * Se ejecuta una sola vez antes de la suite completa.
     */
    @BeforeClass
    public static void configureSuiteProperties() {
        propagateProperty("headless", "HEADLESS");
        propagateProperty("env", "TEST_ENV");
        propagateProperty("user", "TEST_USER");
        propagateProperty("browser", "BROWSER");
        propagateProperty("keyclient", "KEYCLIENT");
        propagateProperty("sendEmailReport", "SEND_EMAIL_REPORT");
        propagateProperty("sendXrayReport", "SEND_XRAY_REPORT");
        propagateProperty("suite", "SUITE");
    }

    /**
     * Propaga una propiedad:
     * <ul>
     *   <li>Usa primero {@code -DpropertyName}.</li>
     *   <li>Si no existe, busca en la variable de entorno asociada.</li>
     *   <li>No aplica valor por defecto.</li>
     * </ul>
     *
     * @param propertyName nombre de la propiedad del sistema
     * @param envVariable  nombre de la variable de entorno asociada
     */
    private static void propagateProperty(String propertyName, String envVariable) {
        if (hasText(System.getProperty(propertyName))) {
            return;
        }

        String value = System.getenv(envVariable);
        if (hasText(value)) {
            System.setProperty(propertyName, value);
        }
    }

    /**
     * Verifica si una cadena tiene texto válido.
     *
     * @param value valor a validar
     * @return {@code true} si no es nulo ni vacío tras eliminar espacios
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
