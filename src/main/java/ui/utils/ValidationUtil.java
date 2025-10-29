package ui.utils;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.manager.PageManager;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

public class ValidationUtil {


    private final WebDriver driver;
    private final WaitUtil waitUtil;
    public final TrafficLightUtil trafficLightUtil;

    /**
     * Constructor que inicializa la utilidad con el WebDriver activo y utilidades de espera explícita.
     *
     * @param driver   instancia actual de WebDriver.
     * @param waitUtil utilidad personalizada para esperas explícitas.
     */
    public ValidationUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.waitUtil = pageManager.getWaitUtil();
        this.trafficLightUtil = pageManager.getTrafficLightUtil();
    }

    /**
     * Verifica que la pantalla actual coincida con el nombre esperado,
     * reintentando automáticamente si la validación falla.
     *
     * <p>Este metodo busca un encabezado visible que contenga el texto esperado.
     * Realiza varios intentos, esperando un tiempo entre ellos, hasta que el texto coincida
     * o se agote el tiempo máximo de espera.</p>
     *
     * @param expectedScreenName Nombre visible esperado de la pantalla.
     */
    public void assertCurrentScreen(String expectedScreenName) {
        final int maxAttempts = 3; // Número máximo de intentos
        final long waitBetweenAttemptsMillis = 300; // Tiempo entre intentos
        final long timeoutAbsolutoMillis = 1000; // Tiempo máximo total

        long startTime = System.currentTimeMillis();
        int attempt = 1;

        while (attempt <= maxAttempts) {
            try {
                WebElement titlePageElement = waitUtil.findVisibleElement(By.cssSelector("div.title-container > span[tooltip-on-hover]"));
                String currentTitle = titlePageElement.getText().trim();

                LogUtil.info("Intento " + attempt + " - Validando pantalla actual. Esperada: '" + expectedScreenName + "', Actual: '" + currentTitle + "'");

                assertEquals(currentTitle, expectedScreenName, "La pantalla actual no es la esperada");
                LogUtil.info("Pantalla validada correctamente en el intento " + attempt);
                return;
            } catch (AssertionError e) {
                LogUtil.warn("Intento " + attempt + " fallido: la pantalla aún no es la esperada. Reintentando...");
            } catch (Exception e) {
                LogUtil.warn("Error inesperado durante el intento " + attempt + " al validar pantalla: " + e.getMessage(), e);
            }

            if (System.currentTimeMillis() - startTime > timeoutAbsolutoMillis) {
                LogUtil.error("Se alcanzó el tiempo máximo permitido sin validar la pantalla: " + expectedScreenName);
                throw new AssertionError("No se pudo validar la pantalla dentro del tiempo límite: " + expectedScreenName);
            }

            waitUtil.sleepMillis(waitBetweenAttemptsMillis, "Tiempo entre intentos");
            attempt++;
        }

        throw new AssertionError("La pantalla '" + expectedScreenName + "' no se validó correctamente después de " + maxAttempts + " intentos.");
    }

    /**
     * Valida que dos cadenas de texto sean iguales.
     *
     * @param current   Valor actual.
     * @param expected Valor esperado.
     * @param message  Mensaje descriptivo para el log.
     */
    public static void assertEquals(String current, String expected, String message) {
        try {
            LogUtil.info("Validando igualdad: " + message);
            Assert.assertEquals(expected, current);
            LogUtil.info("Validación exitosa: '" + current + "' == '" + expected + "'");
        } catch (AssertionError e) {
            LogUtil.error("Error de validación (equals): " + message, e);
            throw e;
        }
    }

    /**
     * Valida que dos valores numéricos (en formato String) sean iguales dentro de una tolerancia permitida.
     *
     * @param current     Valor actual en formato String.
     * @param expected    Valor esperado en formato String.
     * @param message     Mensaje descriptivo para el log.
     * @param tolerance   Diferencia numérica máxima permitida (por ejemplo, 1).
     */
    public static void assertEqualsWithTolerance(String current, String expected, String message, double tolerance) {
        try {
            LogUtil.info("Validando igualdad con tolerancia (" + tolerance + "): " + message);

            double currentVal = Double.parseDouble(current);
            double expectedVal = Double.parseDouble(expected);
            double diff = Math.abs(currentVal - expectedVal);

            if (diff <= tolerance) {
                LogUtil.info("Validación exitosa: '" + current + "' ≈ '" + expected + "' (diferencia " + diff + " <= tolerancia " + tolerance + ")");
            } else {
                throw new AssertionError("Diferencia fuera de tolerancia: " + diff + " > " + tolerance);
            }

        } catch (NumberFormatException e) {
            LogUtil.error("Error al convertir valores numéricos: " + message, e);
            throw new AssertionError("Valores no numéricos en comparación: '" + current + "' y '" + expected + "'");
        } catch (AssertionError e) {
            LogUtil.error("Error de validación (equals con tolerancia): " + message, e);
            throw e;
        }
    }

    /**
     * Valida que dos valores {@link BigDecimal} sean iguales numéricamente.
     *
     * @param current   Valor actual.
     * @param expected  Valor esperado.
     * @param message   Mensaje descriptivo para el log.
     */
    public void assertEquals(BigDecimal current, BigDecimal expected, String message) {
        try {
            LogUtil.info("Validando igualdad numérica: " + message);

            if (expected == null && current == null) {
                LogUtil.info("Ambos valores son nulos. Validación exitosa.");
                return;
            }

            if (expected == null || current == null) {
                Assert.fail("Uno de los valores es nulo → actual: " + current + ", esperado: " + expected);
            }

            if (expected.compareTo(current) == 0) {
                LogUtil.info("Validación exitosa: " + current + " == " + expected);
            } else {
                Assert.fail("Los valores no son iguales → actual: " + current + ", esperado: " + expected);
            }

        } catch (AssertionError e) {
            LogUtil.error("Error de validación numérica: " + message, e);
            throw e;
        }
    }

    /**
     * Valida que dos valores enteros sean iguales.
     *
     * @param current   Valor actual.
     * @param expected  Valor esperado.
     * @param message   Mensaje descriptivo para el log.
     */
    public void assertEquals(int current, int expected, String message) {
        try {
            LogUtil.info("Validando igualdad: " + message);
            Assert.assertEquals(expected, current);
            LogUtil.info("Validación exitosa: '" + current + "' == '" + expected + "'");
        } catch (AssertionError e) {
            LogUtil.error("Error de validación (equals): " + message, e);
            throw e;
        }
    }

    /**
     * Valida que dos valores decimales sean iguales.
     *
     * @param current   Valor actual.
     * @param expected  Valor esperado.
     * @param message   Mensaje descriptivo para el log.
     */
    public void assertEquals(double current, double expected, String message) {
        try {
            LogUtil.info("Validando igualdad: " + message);
            Assert.assertEquals(expected, current, 0.0001); // margen de error para comparación de doubles
            LogUtil.info("Validación exitosa: '" + current + "' == '" + expected + "'");
        } catch (AssertionError e) {
            LogUtil.error("Error de validación (equals - double): " + message, e);
            throw e;
        }
    }

    /**
     * Valida que una condición sea verdadera.
     *
     * @param condition Condición a evaluar.
     * @param message   Descripción de la validación.
     */
    public void assertTrue(boolean condition, String message) {
        try {
            LogUtil.info("Validando que sea verdadero: " + message);
            Assert.assertTrue(message, condition);
            LogUtil.info("Condición verdadera validada correctamente: " + message);
        } catch (AssertionError e) {
            LogUtil.error("Validación fallida (true): " + message, e);
            throw e;
        }
    }

    /**
     * Valida que una condición sea falsa.
     *
     * @param condition Condición a evaluar.
     * @param message   Descripción de la validación.
     */
    public void assertFalse(boolean condition, String message) {
        try {
            LogUtil.info("Validando que sea falso: " + message);
            Assert.assertFalse(message, condition);
            LogUtil.info("Condición falsa validada correctamente: " + message);
        } catch (AssertionError e) {
            LogUtil.error("Validación fallida (false): " + message, e);
            throw e;
        }
    }

    /**
     * Valida que el texto visible de un elemento sea exactamente igual al texto esperado.
     *
     * Este metodo espera que el elemento sea visible y que su texto coincida exactamente
     * (sin diferencias de espacios al inicio o final) con el texto esperado.
     * Si los textos no coinciden, se lanza una aserción y se registra el error.
     *
     * @param element      Elemento Web cuyo texto se desea validar.
     * @param expectedText Texto exacto que se espera encontrar en el elemento.
     * @param elementName  Nombre descriptivo del elemento para fines de logging y trazabilidad.
     */
    public void assertElementTextEquals(WebElement element, String expectedText, String elementName) {
        try {
            LogUtil.info("Esperando visibilidad de: " + elementName);
            waitUtil.waitForVisibilityByElement(element);
            waitUtil.waitForText(element, expectedText);

            String actualText = element.getText().trim();
            LogUtil.info("Comparando texto exacto de '" + elementName + "': esperado = '" + expectedText + "', actual = '" + actualText + "'");

            Assert.assertEquals("El texto del elemento no coincide exactamente.", expectedText, actualText);
            LogUtil.info("Validación exitosa: el texto coincide exactamente con '" + expectedText + "'");

        } catch (Exception e) {
            LogUtil.error("Error al validar texto exacto en el elemento: " + elementName, e);
            throw new RuntimeException("Error al validar texto exacto en: " + elementName, e);
        }
    }

    /**
     * Verifica el mensaje de error por campo espesifico requerido.
     */
    public void messageFieldRequired(String message, String field) {

        WebElement messageErrorNameElement = waitUtil.findVisibleElement(By.xpath("//span[contains(@class, 'error-description') and normalize-space(.)='" + message + "']"));
        assertElementTextEquals(messageErrorNameElement, message, "Mensaje de error.");

        WebElement fieldElement = waitUtil.findVisibleElement(By.xpath("(//imp-label//div[@class='label-container']//span[normalize-space(text())='" + field + "'])[last()]"));
        assertElementTextEquals(fieldElement, field, "Nombre del campo");
    }

    /**
     * Verifica que un mensaje de error específico esté visible en pantalla.
     *
     * <p>Este metodo busca un elemento <code>span</code> con clase <code>error-description</code>
     * y cuyo texto coincida exactamente con el mensaje esperado.</p>
     *
     * @param expectedMessage Mensaje de error que se espera validar.
     */
    public void verifyOnlyErrorMessage(String expectedMessage) {
        try {
            WebElement messageElement = waitUtil.findVisibleElement(
                    By.xpath("//span[contains(@class, 'error-description') and normalize-space(.)='" + expectedMessage + "']")
            );
            assertElementTextEquals(messageElement, expectedMessage, "Mensaje de error");
        } catch (Exception e) {
            LogUtil.error("Error al verificar solo el mensaje de error: " + expectedMessage, e);
            throw new RuntimeException("El mensaje de error no se encontró o no coincide: " + expectedMessage, e);
        }
    }

    /**
     * Verifica que un mensaje de error específico esté visible en pantalla
     * y que su contenido coincida exactamente con el texto esperado.
     *
     * <p>Ejemplo de uso: verificar límites, validaciones, errores del sistema.</p>
     *
     * @param expectedMessage El mensaje de error que se espera ver.
     * @throws AssertionError si el mensaje no aparece o no coincide con el esperado.
     */
    public void verifyErrorMessage(String expectedMessage) {
        By errorMessageLocator = By.cssSelector("div.message-container");

        WebElement errorElement = waitUtil.waitForVisibilityByLocator(errorMessageLocator);
        String actualMessage = errorElement.getText().trim();

        LogUtil.info("Mensaje de error visible: '" + actualMessage + "'");
        if (!actualMessage.equals(expectedMessage)) {
            throw new AssertionError("El mensaje de error no coincide. Esperado: '"
                    + expectedMessage + "' pero fue: '" + actualMessage + "'");
        }
    }

    /**
     * Realiza el ajuste automático de las columnas de la tabla.
     * (Actualmente solo realiza clic, validación futura pendiente).
     */
    public void checkColumnAlignment() {
        LogUtil.error("Validación de ajuste de columnas pendiente de implementación.");
    }

    /**
     * Verifica si un botón con la etiqueta especificada está presente y visible.
     *
     * @param buttonLabel Texto visible del botón (por ejemplo: "Nuevo", "Aceptar").
     * @return true si el botón está visible, false en caso contrario.
     */
    public boolean isButtonVisible(String buttonLabel) {
        try {
            return waitUtil.findVisibleElement(
                    By.xpath("//*[self::a or self::button][normalize-space(.)='" + buttonLabel + "']")
            ).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Verifica que un registro con el valor esperado esté presente en la tabla.
     *
     * @param filter nombre de la columna por la que se filtró.
     * @param record  valor esperado en la primera celda de la columna.
     */
    public void assertRecordListed(String record) {
        By locator = By.xpath("//td[.//span[normalize-space(text())='" + record + "']]");
        waitUtil.scrollUntilElementIsVisible(locator);
        boolean exists = waitUtil.isElementVisible(locator, 30000, 500);
        assertTrue(exists, "El registro está en la tabla: " + record);
    }

    /**
     * Valida que el objeto proporcionado no sea nulo.
     *
     * <p>Registra en el log el inicio de la validación, ejecuta la aserción y,
     * dependiendo del resultado, deja constancia de éxito o del error capturado.</p>
     *
     * @param value    Objeto a validar (no debe ser {@code null}).
     * @param message  Mensaje descriptivo para el log.
     * @throws AssertionError si {@code value} es {@code null}.
     */
    public void assertNotNull(Object value, String message) {
        try {
            LogUtil.info("Validando no nulo: " + message);
            // Puedes añadir el mensaje dentro de la aserción si lo prefieres:
            // Assert.assertNotNull(value, "Se esperaba un valor no nulo. " + message);
            Assert.assertNotNull(value);
            LogUtil.info("Validación exitosa: el valor no es nulo. Valor: '" + String.valueOf(value) + "'");
        } catch (AssertionError e) {
            LogUtil.error("Error de validación (notNull): " + message + " | Valor recibido: " + String.valueOf(value), e);
            throw e;
        }
    }

    /**
     * Valida que la ruta de la URL actual coincida con la ruta esperada.
     *
     * <p>Este metodo espera a que la URL del navegador alcance la ruta especificada y,
     * posteriormente, compara únicamente la ruta (sin dominio, parámetros ni fragmentos)
     * con el valor esperado.</p>
     *
     * @param expectedPath Ruta esperada (por ejemplo, {@code "materials/tree"}).
     * @throws AssertionError si la ruta no coincide en el tiempo de espera configurado.
     */
    public void assertCurrentUrlPath(String expectedPath) {
        String normalizedExpectedPath = normalizePath(expectedPath);

        LogUtil.info("Validando ruta de la URL actual. Ruta esperada: '" + normalizedExpectedPath + "'");
        try {
            waitForUrlPath(normalizedExpectedPath);

            String currentPath = extractPath(driver.getCurrentUrl());
            LogUtil.info("Ruta actual obtenida: '" + currentPath + "'");

            Assert.assertEquals("La ruta actual no coincide con la esperada.", normalizedExpectedPath, currentPath);
            LogUtil.info("Validación exitosa: la ruta de la URL coincide con '" + normalizedExpectedPath + "'");
        } catch (AssertionError e) {
            LogUtil.error("La ruta actual no coincide con la esperada: '" + normalizedExpectedPath + "'", e);
            throw e;
        } catch (TimeoutException e) {
            String currentPath = getCurrentPathSafely();
            LogUtil.error("Tiempo de espera agotado al validar la ruta de la URL. Esperada: '"
                    + normalizedExpectedPath + "' | Actual: '" + currentPath + "'", e);
            throw new AssertionError("La ruta de la URL no coincidió dentro del tiempo esperado. Esperada: '"
                    + normalizedExpectedPath + "' pero fue: '" + currentPath + "'", e);
        } catch (Exception e) {
            LogUtil.error("Error inesperado al validar la ruta de la URL.", e);
            throw new RuntimeException("No se pudo validar la ruta de la URL esperada.", e);
        }
    }

    private void waitForUrlPath(String expectedPath) {
        LogUtil.info("Esperando a que la ruta de la URL sea: '" + expectedPath + "'");

        WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        urlWait.pollingEvery(Duration.ofMillis(500));
        urlWait.until(webDriver -> expectedPath.equals(extractPath(webDriver.getCurrentUrl())));

        String matchedPath = extractPath(driver.getCurrentUrl());
        LogUtil.info("Ruta de la URL encontrada: '" + matchedPath + "'");
    }

    private String extractPath(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path == null) {
                return "";
            }
            return normalizePath(path);
        } catch (URISyntaxException e) {
            LogUtil.error("No se pudo interpretar la URL: " + url, e);
            throw new RuntimeException("No se pudo interpretar la URL: " + url, e);
        }
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }

        String normalized = path.trim();
        if (normalized.isEmpty()) {
            return "";
        }

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
            if (normalized.isEmpty()) {
                return "";
            }
        }

        while (normalized.endsWith("/") && !normalized.isEmpty()) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String getCurrentPathSafely() {
        try {
            return extractPath(driver.getCurrentUrl());
        } catch (RuntimeException ex) {
            LogUtil.warn("No se pudo obtener la ruta actual de la URL durante la validación.", ex);
            try {
                return driver != null ? driver.getCurrentUrl() : "";
            } catch (Exception inner) {
                LogUtil.warn("No se pudo obtener la URL actual del navegador.", inner);
                return "";
            }
        }
    }

}
