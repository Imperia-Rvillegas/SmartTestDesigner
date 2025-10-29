package ui.base;

import config.ScenarioContext;
import hooks.Hooks;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ui.utils.*;
import ui.manager.PageManager;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Clase base para todas las páginas de la UI.
 *
 * <p>Esta clase proporciona acceso compartido al {@link WebDriver}, al {@link PageManager}
 * y a utilidades comunes como {@link WaitUtil}, {@link ScreenshotUtil}, {@link TableUtil},
 * {@link PopupUtil} y {@link ValidationUtil}.</p>
 *
 * <p>Contiene métodos reutilizables para realizar acciones frecuentes como clics seguros,
 * ingreso de texto en campos dinámicos, manejo de excepciones comunes (como {@link StaleElementReferenceException}),
 * y generación de evidencia visual mediante capturas de pantalla.</p>
 *
 * <p>Todas las páginas concretas del sistema deben extender esta clase para heredar su funcionalidad
 * y promover un diseño más limpio, mantenible y reutilizable.</p>
 */
public class BasePage {

    //Dependencias inyectadas vía constructor
    protected final WebDriver driver;
    protected final PageManager pageManager;

    // Acceso directo a utilidades si son comunes en todas las páginas
    protected final PopupUtil popupUtil;
    protected final WaitUtil waitUtil;
    protected final ScreenshotUtil screenshotUtil;
    protected final TableUtil tableUtil;
    protected final ValidationUtil validationUtil;
    protected final TrafficLightUtil trafficLightUtil;
    protected final ScenarioContext scenarioContext;

    /**
     * Constructor base que inicializa WebDriver y utilidades compartidas desde PageManager.
     *
     * @param driver WebDriver activo para controlar el navegador.
     * @param pageManager Instancia de PageManager que centraliza el acceso a páginas y utilidades.
     */
    public BasePage(WebDriver driver, PageManager pageManager) {
        this.driver = driver;
        this.pageManager = pageManager;
        this.popupUtil = pageManager.getPopupUtil();
        this.waitUtil = pageManager.getWaitUtil();
        this.screenshotUtil = pageManager.getScreenshotUtil();
        this.tableUtil = pageManager.getTableUtil();
        this.validationUtil = pageManager.getValidationUtil();
        this.trafficLightUtil = pageManager.getTrafficLightUtil();
        this.scenarioContext = pageManager.getScenarioContext();
    }

    /**
     * Realiza un clic único sobre un elemento web utilizando JavaScript,
     * asegurando que el elemento quede visible por debajo de cabeceras fijas.
     *
     * <p>Este metodo asume que el elemento recibido ya fue preparado mediante
     * {@link #ensureElementInteractable(WebElement, String)} o
     * {@link #ensureElementInteractable(By, String)}.</p>
     *
     * @param element     el {@link WebElement} sobre el que se debe hacer clic.
     * @param elementName Nombre descriptivo para logs y evidencias.
     */
    public void singleClick(WebElement element, String elementName) {
        //espera para asegurar que la pantalla carga completamente antes de la captura
//        waitUtil.sleepMillis(300, "Asegurar que la pantalla carga completamente antes de la captura");
        // Captura evidencia antes del clic
        screenshotUtil.capture("Evidencia antes de hacer clic en: " + elementName);
        // Log informativo
        LogUtil.info("Haciendo clic en botón: " + elementName);
        // Clic con JS
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Encapsula las esperas de visibilidad y clicabilidad, así como el ajuste de scroll,
     * para un {@link WebElement} ya localizado.
     *
     * @param element     Elemento objetivo.
     * @param elementName Nombre descriptivo usado en los mensajes de log.
     * @return El mismo elemento recibido, una vez garantizado que está listo para interactuar.
     */
    private WebElement ensureElementInteractable(WebElement element, String elementName) {
        LogUtil.info("Preparando elemento para interacción: " + elementName);
        scrollToElementIfNotVisible(element);
        waitUtil.waitForVisibilityByElement(element);
        waitUtil.waitForClickable(element);
        return element;
    }

    /**
     * Encapsula la localización, esperas explícitas y ajuste de scroll para un locator.
     *
     * @param locator     Localizador del elemento objetivo.
     * @param elementName Nombre descriptivo usado en los mensajes de log.
     * @return Un {@link WebElement} fresco y listo para interactuar.
     */
    private WebElement ensureElementInteractable(By locator, String elementName) {
        LogUtil.info("Localizando elemento interactuable: " + elementName);
        WebElement element = waitUtil.waitForPresenceOfElement(locator);
        scrollToElementIfNotVisible(element);
        waitUtil.waitForVisibilityByLocator(locator);
        waitUtil.waitUntil(
                ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(locator))
        );
        return element;
    }

    /**
     * Ejecuta una interacción sobre un elemento identificado por un {@link By} aplicando
     * reintentos automáticos ante {@link StaleElementReferenceException}.
     *
     * @param locator       Localizador del elemento sobre el cual se realizará la acción.
     * @param elementName   Nombre descriptivo del elemento para los logs.
     * @param interaction   Acción a realizar una vez que el elemento está listo.
     */
    private void performWithRetry(By locator, String elementName, Consumer<WebElement> interaction) {
        final int maxAttempts = 3;
        StaleElementReferenceException lastStaleException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                WebElement element = ensureElementInteractable(locator, elementName);
                interaction.accept(element);
                return;
            } catch (StaleElementReferenceException stale) {
                lastStaleException = stale;
                LogUtil.warn("Elemento obsoleto al interactuar con: " + elementName +
                        ". Reintento " + attempt + " de " + maxAttempts + ".", stale);
            }
        }

        throw new RuntimeException("No fue posible interactuar con el elemento: " + elementName,
                lastStaleException);
    }

    /**
     * Desplaza la vista hasta el elemento especificado solo si este no es completamente visible
     * o se encuentra en la mitad inferior del viewport.
     * Si el desplazamiento es necesario, centra el elemento en la ventana visible.
     *
     * <p>Este metodo utiliza JavaScript para realizar el scroll de forma controlada, mejorando
     * la visibilidad del elemento sin movimientos innecesarios.</p>
     *
     * @param element El {@link WebElement} al que se desea desplazar la vista.
     */
    public void scrollToElementIfNotVisible(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "const rect = arguments[0].getBoundingClientRect();" +
                        "if (rect.top > window.innerHeight / 2 || rect.bottom < 0) {" +
                        "  arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});" +
                        "}", element);
    }

    /**
     * Hace clic en un elemento después de esperar que sea clickeable, utilizando ejecución JavaScript
     * para evitar problemas de superposición u obstrucción por otros elementos.
     *
     * Este metodo realiza los siguientes pasos:
     * <ul>
     *   <li>Espera explícitamente a que el elemento sea clickeable (visibilidad e interactividad).</li>
     *   <li>Captura una evidencia (screenshot) antes de hacer clic, útil para reportes y depuración.</li>
     *   <li>Registra logs informativos antes y después de la acción de clic.</li>
     *   <li>Desplaza el elemento al área visible del navegador mediante <code>scrollIntoView</code>.</li>
     *   <li>Ejecuta el clic mediante <code>JavascriptExecutor</code> para evitar errores como <code>ElementClickInterceptedException</code>.</li>
     *   <li>Lanza una excepción con detalles si ocurre un error durante el proceso.</li>
     * </ul>
     *
     * <strong>Nota:</strong> El nombre descriptivo del elemento (<code>buttonName</code>) se usa en logs y como nombre en la captura.
     * Se recomienda evitar el uso de acentos u otros caracteres especiales en <code>buttonName</code>, ya que pueden provocar errores
     * al guardar archivos o al procesar logs.
     *
     * @param element Elemento sobre el cual se desea hacer clic.
     * @param elementName Nombre descriptivo del botón para logging y evidencia visual.
     */
    public void clickByElement(WebElement element, String elementName) {
        try {
            WebElement readyElement = ensureElementInteractable(element, elementName);
            singleClick(readyElement, elementName);
            LogUtil.info("Clic realizado exitosamente en: " + elementName);
        } catch (Exception e) {
            LogUtil.error("Error al hacer clic en: " + elementName, e);
            throw new RuntimeException("Error al hacer clic en el elemento: " + elementName, e);
        }
    }

    /**
     * Realiza un clic lento (humanLikeClick) sobre un elemento e intenta verificar
     * que otro elemento (ubicado por un locator) se vuelva visible tras el clic.
     * Si no se vuelve visible, reintenta el clic hasta el número de intentos máximo.
     *
     * @param element           Elemento sobre el cual se hará clic.
     * @param elementName       Nombre descriptivo del elemento para logs y captura.
     * @param targetLocator     Localizador del elemento que debe volverse visible tras el clic.
     * @param clickHoldMillis   Duración del clic en milisegundos (simulando el tiempo presionado).
     * @param maxAttempts       Número máximo de intentos de clic + validación.
     * @param waitBetweenMillis Tiempo de espera entre intentos (en milisegundos).
     * @param timeoutMillis     Tiempo máximo para esperar visibilidad del target (por intento).
     * @param pollingMillis     Intervalo de sondeo para detectar visibilidad.
     *
     * @return {@code true} si el elemento se volvió visible; {@code false} si no lo hizo tras todos los intentos.
     */
    public boolean clickUntilElementVisible(WebElement element,
                                            String elementName,
                                            By targetLocator,
                                            int clickHoldMillis,
                                            int maxAttempts,
                                            int waitBetweenMillis,
                                            int timeoutMillis,
                                            int pollingMillis) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            LogUtil.info("Intento #" + attempt + ": clic lento en '" + elementName + "' y verificación de visibilidad: " + targetLocator);

            // Realiza el clic humanizado con duración personalizada
            try {
                humanLikeClick(element, elementName, clickHoldMillis);
            } catch (Exception e) {
                LogUtil.warn("Error durante clic en intento #" + attempt + ": " + e.getMessage());
            }

            // Verifica si el elemento se vuelve visible
            boolean visible = waitUtil.isElementVisible(targetLocator, timeoutMillis, pollingMillis);
            if (visible) {
                LogUtil.info("Elemento localizado correctamente tras intento #" + attempt);
                return true;
            }

            // Espera antes de volver a intentar
            if (attempt < maxAttempts) {
                try {
                    Thread.sleep(waitBetweenMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        LogUtil.error("El elemento " + targetLocator + " no se volvió visible tras " + maxAttempts + " intentos.");
        return false;
    }

    /**
     * Realiza un clic humanizado sobre un elemento web, simulando una interacción más natural.
     * <p>
     * El metodo espera a que el elemento sea visible y clickeable, realiza un desplazamiento inteligente
     * hacia su centro si es necesario, captura una evidencia previa y luego ejecuta un clic sostenido
     * durante el tiempo especificado antes de soltar.
     * </p>
     *
     * @param element        el {@link WebElement} sobre el cual se realizará el clic.
     * @param elementName    nombre descriptivo del elemento, usado para registro y evidencias.
     * @param clickHoldMillis duración en milisegundos del tiempo de presión antes de soltar el clic.
     * @throws RuntimeException si ocurre algún error durante la ejecución del clic humanizado.
     */
    public void humanLikeClick(WebElement element, String elementName, int clickHoldMillis) {
        try {
            WebElement readyElement = ensureElementInteractable(element, elementName);
            screenshotUtil.capture("Evidencia antes del clic humanizado en: " + elementName);

            LogUtil.info("Realizando clic humanizado en: " + elementName + " (presión: " + clickHoldMillis + " ms)");

            Actions actions = new Actions(driver);
            actions.moveToElement(readyElement)
                    .clickAndHold()
                    .pause(Duration.ofMillis(clickHoldMillis))
                    .release()
                    .perform();

            LogUtil.info("Clic humanizado realizado en: " + elementName);
        } catch (Exception e) {
            LogUtil.error("Error durante el clic humanizado en: " + elementName, e);
            throw new RuntimeException("Error al hacer clic humanizado en: " + elementName, e);
        }
    }

    /**
     * Hace clic en un elemento ubicado por un localizador, con protección avanzada contra
     * {@link StaleElementReferenceException}, utilizando JavaScript.
     *
     * @param locator     Localizador del elemento.
     * @param elementName Nombre del elemento para logs y evidencia.
     */
    public void clickByLocator(By locator, String elementName) {
        try {
            performWithRetry(locator, elementName, element -> singleClick(element, elementName));
            LogUtil.info("Clic realizado exitosamente en: " + elementName);
        } catch (Exception e) {
            LogUtil.error("Error al hacer clic en: " + elementName, e);
            throw new RuntimeException("Error al hacer clic en el elemento: " + elementName, e);
        }
    }

    /**
     * Realiza un doble clic en un elemento identificado por un localizador, aplicando esperas,
     * scroll inteligente, captura de evidencia y manejo de elementos obsoletos.
     *
     * <p>El metodo ejecuta los siguientes pasos:</p>
     * <ul>
     *   <li>Espera a que el elemento sea visible y clickeable usando condiciones "refreshed".</li>
     *   <li>Desplaza el elemento al centro del viewport para garantizar la interacción.</li>
     *   <li>Captura una evidencia antes de realizar el doble clic.</li>
     *   <li>Ejecuta la acción de doble clic mediante {@link Actions}.</li>
     *   <li>En caso de {@link StaleElementReferenceException}, reintenta la interacción relocalizando el elemento.</li>
     * </ul>
     *
     * @param locator     Localizador del elemento sobre el que se realizará el doble clic.
     * @param elementName Nombre descriptivo del elemento para logs y evidencia.
     */
    public void doubleClickByLocator(By locator, String elementName) {
        try {
            performWithRetry(locator, elementName, element -> {
                screenshotUtil.capture("Evidencia antes del doble clic en: " + elementName);
                new Actions(driver)
                        .moveToElement(element)
                        .doubleClick()
                        .perform();
            });
            LogUtil.info("Doble clic realizado exitosamente en: " + elementName);
        } catch (Exception e) {
            LogUtil.error("Error al realizar doble clic en: " + elementName, e);
            throw new RuntimeException("Error al realizar doble clic en: " + elementName, e);
        }
    }

    /**
     * Hace clic en un botón identificado por su texto visible exacto.
     *
     * Este metodo realiza los siguientes pasos:
     * <ul>
     *   <li>Construye dinámicamente un localizador XPath que busca un botón o enlace con el texto visible exacto proporcionado.</li>
     *   <li>Espera a que el botón sea visible en el DOM utilizando una espera explícita.</li>
     *   <li>Pasa el elemento encontrado al metodo {@link #clickByLocator(By, String)}, que gestiona esperas, captura evidencia y reintentos antes de hacer clic.</li>
     *   <li>Registra logs del proceso, incluyendo el inicio de la búsqueda y cualquier error en caso de falla.</li>
     * </ul>
     *
     * <strong>Nota:</strong> Se recomienda evitar el uso de acentos o caracteres especiales en <code>buttonName</code>,
     * ya que podrían provocar errores al generar archivos de evidencia o al registrar información en logs.
     *
     * @param buttonName Texto visible del botón (o enlace) sobre el cual se desea hacer clic.
     */
    public void clickButtonByName(String buttonName) {

        LogUtil.info("Buscando botón con texto: " + buttonName);

        // Construye el localizador basado en el texto exacto del botón o enlace
        By buttonLocator = By.xpath("//*[self::a or self::button][normalize-space(.)='" + buttonName + "']");

        // Espera que sea clickeable, toma screenshot, hace clic, y registra logs
        clickByLocator(buttonLocator, buttonName);
    }

    /**
     * Realiza un doble clic sobre un elemento cuyo texto visible coincide exactamente con el
     * nombre proporcionado.
     *
     * <p>Construye dinámicamente un XPath genérico que busca cualquier elemento cuyo contenido
     * de texto normalizado coincida con el nombre indicado y delega la interacción en
     * {@link #doubleClickByLocator(By, String)}.</p>
     *
     * <p><strong>Nota:</strong> Se recomienda evitar caracteres especiales que puedan interferir
     * con la construcción del XPath.</p>
     *
     * @param elementName Texto visible exacto del elemento sobre el que se desea hacer doble clic.
     */
    public void doubleClickElementByName(String elementName) {
        LogUtil.info("Buscando elemento para doble clic con texto: " + elementName);

        By elementLocator = By.xpath("//*[normalize-space(text())='" + elementName + "']");

        doubleClickByLocator(elementLocator, elementName);
    }

    /**
     * Hace clic en una opción de un componente tipo dropdown (como <p-dropdownitem>) usando su texto visible.
     *
     * Este metodo realiza los siguientes pasos:
     * <ul>
     *   <li>Construye dinámicamente un localizador XPath que busca un elemento tipo opción (li/span) con el texto exacto proporcionado.</li>
     *   <li>Espera a que el elemento sea visible y clickeable utilizando una espera explícita.</li>
     *   <li>Delegan la interacción en {@link #clickByLocator(By, String)} para reutilizar esperas, evidencias y reintentos.</li>
     *   <li>Registra logs detallados del proceso, incluyendo la búsqueda del texto.</li>
     * </ul>
     *
     * <strong>Nota:</strong> Asegúrate de que la lista desplegable esté visible antes de llamar a este metodo.
     *
     * @param optionText Texto visible exacto de la opción del dropdown que se desea seleccionar.
     */
    public void clickDropdownOptionByText(String optionText) {

        LogUtil.info("Buscando opción del dropdown con texto: " + optionText);

        // Localiza el elemento <li> o <span> dentro del dropdown por su texto exacto
        By optionLocator = By.xpath("//span[normalize-space()='" + optionText + "' and (ancestor::li[contains(@class,'p-dropdown-item')] or ancestor::div[contains(@class,'imp-input-help-option')])]");

        // Espera y hace clic en la opción encontrada
        clickByLocator(optionLocator, optionText);
    }


    /**
     * Limpia el contenido de un campo de texto después de esperar que sea visible y editable.
     *
     * @param element Elemento de entrada que se desea limpiar.
     * @param elementName Nombre descriptivo del elemento para logging.
     * @deprecated Este metodo será eliminado en futuras versiones.
     * Usa {@link #safeClear()} en su lugar.
     */
    @Deprecated
    public void clear(WebElement element, String elementName) {
        try {
            LogUtil.info("Esperando visibilidad para limpiar: " + elementName);
            waitUtil.waitForVisibilityByElement(element);
            waitUtil.waitForClickable(element);

            screenshotUtil.capture("Evidencia antes de limpiar: " + elementName);
            LogUtil.info("Limpiando el contenido de: " + elementName);
            element.clear();
            LogUtil.info("Contenido limpiado exitosamente en: " + elementName);
        } catch (Exception e) {
            LogUtil.error("Error al limpiar el contenido de: " + elementName, e);
            throw new RuntimeException("Error al limpiar el contenido del elemento: " + elementName, e);
        }
    }

    /**
     * Envía texto a un campo después de limpiarlo y esperar visibilidad.
     *
     * @param element Elemento de entrada (input) al que se enviará texto.
     * @param text Texto a enviar.
     * @param fieldName Nombre descriptivo del campo para logging.
     */
    public void sendKeysByElement(WebElement element, String text, String fieldName) {
        try {
            LogUtil.info("Limpiando campo: " + fieldName);
            element.clear();

            LogUtil.info("Enviando texto a campo: " + fieldName + " → '" + text + "'");
            element.sendKeys(text);
            LogUtil.info("Texto enviado correctamente a campo: " + fieldName);
        } catch (Exception e) {
            LogUtil.error("Error al enviar texto a campo: " + fieldName, e);
            throw new RuntimeException("Error al enviar texto al campo: " + fieldName, e);
        }
    }

    /**
     * Envía texto a un campo de entrada ubicado por un localizador,
     * con espera "refreshed", scroll automático y reintento si el elemento se vuelve obsoleto.
     *
     * @param locator    Localizador del campo input.
     * @param text       Texto a enviar.
     * @param fieldName  Nombre descriptivo del campo para logs y evidencia.
     */
    public void sendKeysByLocator(By locator, String text, String fieldName) {
        try {
            LogUtil.info("Esperando que el campo esté visible (con refreshed): " + fieldName);

            //Espera que el elemento este visible
            waitUtil.waitForVisibilityByLocator(locator);

            waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.visibilityOfElementLocated(locator)
            ));

            LogUtil.info("Preparando campo para ingreso de texto: " + fieldName + " → '" + text + "'");

            WebElement input = waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.presenceOfElementLocated(locator)
            ));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);

            input.clear();
            input.sendKeys(text);

            LogUtil.info("Texto enviado correctamente a campo: " + fieldName);
        } catch (StaleElementReferenceException e) {
            LogUtil.warn("Campo obsoleto al enviar texto en: " + fieldName + ". Reintentando...", e);

            try {
                WebElement retryInput = waitUtil.waitUntil(ExpectedConditions.refreshed(
                        ExpectedConditions.visibilityOfElementLocated(locator)
                ));

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", retryInput);

                retryInput.clear();
                retryInput.sendKeys(text);

                LogUtil.info("Texto enviado exitosamente en reintento a campo: " + fieldName);
            } catch (Exception retryEx) {
                LogUtil.error("Reintento fallido al enviar texto a campo: " + fieldName, retryEx);
                throw new RuntimeException("Error en reintento de envío de texto al campo: " + fieldName, retryEx);
            }
        } catch (Exception e) {
            LogUtil.error("Error al enviar texto al campo: " + fieldName, e);
            throw new RuntimeException("Error al enviar texto al campo: " + fieldName, e);
        }
    }

    /**
     * Envía un número a un campo de entrada ubicado por un localizador,
     * convirtiéndolo a texto y reutilizando la lógica de envío con espera, scroll y reintento.
     *
     * @param locator    Localizador del campo input.
     * @param number     Número a enviar como texto.
     * @param fieldName  Nombre descriptivo del campo para logs y evidencia.
     */
    public void sendKeysByLocator(By locator, int number, String fieldName) {
        sendKeysByLocator(locator, String.valueOf(number), fieldName);
    }

    /**
     * Envía texto a un campo de entrada identificado por un {@link By}, verificando que el texto se haya ingresado correctamente.
     * <p>
     * Este metodo mejora la confiabilidad del ingreso de texto en aplicaciones con frontends dinámicos (Angular, React, etc.),
     * utilizando una estrategia escalonada para garantizar que el valor se refleje correctamente en el campo:
     * </p>
     * <ul>
     *     <li>Primero, intenta ingresar el texto usando {@link #sendKeysByLocator(By, String, String)}.</li>
     *     <li>Luego valida si el texto ingresado coincide con el valor esperado mediante <code>getAttribute("value")</code>.</li>
     *     <li>Si el valor no coincide, realiza un reintento con ingreso lento, carácter por carácter, usando {@link #sendTextSlowly(WebElement, String)}.</li>
     *     <li>Si el valor aún es incorrecto, como último recurso, se fuerza el valor con JavaScript.</li>
     *     <li>Incluye reintento completo en caso de {@link org.openqa.selenium.StaleElementReferenceException}.</li>
     * </ul>
     *
     * @param locator   Localizador {@link By} del campo de entrada donde se ingresará el texto.
     * @param text      Texto que se desea ingresar en el campo.
     * @param fieldName Nombre descriptivo del campo, utilizado para propósitos de logging y reportes.
     *
     * @throws RuntimeException si ocurre un error al ingresar el texto y no puede ser recuperado.
     */
    public void sendKeysByLocatorWithVerification(By locator, String text, String fieldName) {
        try {
            sendKeysByLocator(locator, text, fieldName);

            WebElement element = waitUtil.findVisibleElement(locator);
            String value = element.getAttribute("value");
            if (!text.equals(value)) {
                LogUtil.warn("Texto no coincidente tras sendKeys. Reintentando con ingreso lento: '" + value + "' != '" + text + "'");
                element.clear();
                sendTextSlowly(element, text);
                value = element.getAttribute("value");

                if (!text.equals(value)) {
                    LogUtil.warn("Texto aún incorrecto tras ingreso lento. Forzando con JavaScript.");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", element, text);
                }
            }

            LogUtil.info("Texto enviado correctamente a campo: " + fieldName);
        } catch (StaleElementReferenceException e) {
            LogUtil.warn("Campo obsoleto al enviar texto en: " + fieldName + ". Reintentando...", e);
            sendKeysByLocatorWithVerification(locator, text, fieldName); // Reintento total recursivo
        } catch (Exception e) {
            LogUtil.error("Error al enviar texto al campo: " + fieldName, e);
            throw new RuntimeException("Error al enviar texto al campo: " + fieldName, e);
        }
    }

    /**
     * Envía texto carácter por carácter a un campo de entrada, simulando la escritura humana.
     * <p>
     * Esta técnica es útil para evitar problemas en campos de entrada sensibles,
     * donde el envío rápido con {@code sendKeys()} puede omitir espacios o caracteres
     * debido a validaciones reactivas del frontend (por ejemplo, frameworks como Angular o React).
     * </p>
     *
     * @param element Elemento {@link WebElement} del campo input al que se enviará el texto.
     * @param text    Texto completo que se desea ingresar en el campo.
     */
    private void sendTextSlowly(WebElement element, String text) {
        for (char c : text.toCharArray()) {
            element.sendKeys(Character.toString(c));
            try {
                Thread.sleep(50); // Pequeña pausa para simular ingreso humano
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restablece el estado de interrupción
            }
        }
    }

    /**
     * Acepta la confirmación.
     */
    public void acceptConfirmation() {
        clickButtonByName("Aceptar");
    }

    /**
     * Aplica un filtro en la pantalla usando el nombre del filtro y su valor.
     *
     * @param filter nombre del filtro a aplicar (ej. "Descripción", "Código").
     * @param value  valor que se desea filtrar en la columna correspondiente.
     */
    public void filterBy(String filter, String value) {
        // Hacer clic en el botón de "Filtrar"
        clickByLocator(By.xpath("//imperia-icon-button[@icon='filter']//a[contains(@class, 'button-container')]"), "Filtrar");

        // Hacer clic en "Añadir Filtros"
        clickByLocator(By.xpath("//a[contains(@class, 'button-container') and contains(., 'Añadir filtro')]"), "Añadir Filtros");

        // Buscar el filtro en el buscador
        sendKeysByLocator(By.xpath("//div[contains(@class, 'filters')]//input[@type='text']"), filter, "Añadir Filtros");

        // Seleccionar el filtro encontrado
        clickByLocator(By.xpath("//a[contains(@class, 'button-container') and normalize-space(.)='" + filter + "']"), "Filtros");

        // Ingresar el valor a buscar en el campo del filtro seleccionado
        sendKeysByLocator(By.xpath("//div[contains(@class, 'horizontalListElementEnterLeave')]//div[contains(@class,'filter')][.//a[@role='button' and contains(normalize-space(@title), '" + filter + "')]]//input[@type='text']"), value, "Valor a buscar");
    }

    public void filterByList(String filter, String value) {
        //esperar a que cargue la tabla
        waitUtil.waitForVisibilityByLocator(By.xpath("//div[normalize-space(text())='Seleccionar valores']"));

        // Hacer clic en el botón de "Filtrar"
        clickByLocator(By.xpath("(//imperia-icon-button[@icon='filter']//a[contains(@class, 'button-container')])[last()]"), "Filtrar");

        // Hacer clic en "Añadir Filtros"
        clickByLocator(By.xpath("(//a[contains(@class, 'button-container') and contains(., 'Añadir filtro')])[last()]"), "Añadir Filtros");

        // Buscar el filtro en el buscador
        sendKeysByLocator(By.xpath("(//div[contains(@class, 'filters')]//input[@type='text'])[last()]"), filter, "Añadir Filtros");

        // Seleccionar el filtro encontrado
        clickByLocator(By.xpath("(//a[contains(@class, 'button-container') and normalize-space(.)='" + filter + "'])[last()]"), "Filtros");

        // Hacer clic para desplegar la lista
        clickByLocator(By.xpath("//div[contains(@class,'horizontalListElementEnterLeave')]" + "//div[contains(@class,'filter')][.//a[@role='button' and contains(normalize-space(@title), " + filter + ")]]" + "//div[@role='button' and contains(@class,'p-dropdown-trigger')]"), "Abrir lista de valores");

        // Seleccionar el valor de la lista
        clickDropdownOptionByText(value);
    }

    /**
     * Elimina todos los filtros actualmente aplicados en la tabla de artículos.
     */
    public void removeFilters(String filter) {
        //Clic en cerrar filtro especifico
        clickByLocator(By.xpath("//span[normalize-space(text())='" + filter + "']/preceding-sibling::div"), "Quitar filtro " + filter);

        //Clic en cerrar ventanba de filtros
        clickByLocator(By.xpath("//a[@role='button' and contains(@class, 'button-container') and .//img[contains(@src, 'back-arrow.svg')]]"), "Cerrar ventana de filtros");
    }

    /**
     * Hace clic en el cuerpo (body) de la página para cerrar el campo editable y guardar los cambios.
     *
     * Este metodo se utiliza típicamente después de ingresar texto en un campo editable dentro de una tabla
     * o formulario, cuando es necesario hacer clic fuera del campo para que el sistema registre y guarde el cambio.
     */
    public void clickOutside() {
        clickByLocator(By.tagName("body"), "Body para confirmar edicion");
    }

    /**
     * Limpia de forma segura el contenido de un campo de entrada (input) utilizando la combinación
     * de teclas {@code CTRL + A} para seleccionar el texto, seguida de {@code DELETE} para eliminarlo.
     *
     * <p>Este enfoque es más confiable que {@link WebElement#clear()} en ciertos escenarios donde los campos
     * tienen eventos JavaScript personalizados (por ejemplo, inputs con máscaras, validaciones en vivo, etc.).</p>
     *
     * <p>Se recomienda acompañar esta acción con una espera explícita para verificar que el campo quedó vacío,
     * por ejemplo usando {@code waitUtil.waitForInputToBeEmpty(input)} después de llamar a este metodo.</p>
     *
     * @param input        El {@link WebElement} que representa el campo de entrada a limpiar.
     * @param elementName  Nombre descriptivo del elemento para los logs.
     */
    public void safeClear(WebElement input, String elementName) {
        input.sendKeys(Keys.CONTROL + "a", Keys.DELETE);
        LogUtil.info("Texto del input " + elementName + " eliminado con CTRL+A + DELETE");
    }

    /**
     * Ingresa texto en un campo de entrada (`input[type="text"]`) ubicado dinámicamente a partir del título visible asociado.
     * <p>
     * Este metodo construye un localizador XPath utilizando el texto visible (etiqueta) del campo —por ejemplo, "Valor por defecto"—
     * y localiza el `input` correspondiente en la misma estructura de formulario.
     * <p>
     * Realiza las siguientes acciones:
     * <ul>
     *     <li>Espera que el campo sea visible usando `ExpectedConditions`.</li>
     *     <li>Desplaza el campo al área visible mediante JavaScript.</li>
     *     <li>Limpia el contenido previo con `safeClear`.</li>
     *     <li>Escribe el texto especificado en el campo.</li>
     *     <li>En caso de `StaleElementReferenceException`, reintenta la operación una vez más.</li>
     * </ul>
     *
     * @param titleText   El texto visible del título del campo (por ejemplo, "Valor por defecto") que se usará para construir el localizador XPath.
     * @param textToSend  El texto que se desea ingresar en el campo localizado.
     * @throws RuntimeException Si el elemento no es encontrado, no es visible, o el envío de texto falla incluso tras reintento.
     */
    public void sendKeysByTitle(String titleText, String textToSend) {

        try {
            LogUtil.info("Esperando que el campo esté visible (con refreshed): " + titleText);
            By dynamicLocator = By.xpath("//span[contains(text(), '" + titleText + "')]/ancestor::div[contains(@class, 'form-control-container')]//input[@type='text']");

            waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.visibilityOfElementLocated(dynamicLocator)
            ));
            waitUtil.isElementVisible(dynamicLocator, 10000, 500);

            LogUtil.info("Preparando campo para ingreso de texto: " + titleText + " → '" + textToSend + "'");

            WebElement input = waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.presenceOfElementLocated(dynamicLocator)
            ));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);

            safeClear(input, titleText);
            input.sendKeys(textToSend);

            LogUtil.info("Texto enviado correctamente a campo: " + titleText);
        } catch (StaleElementReferenceException e) {
            LogUtil.warn("Campo obsoleto al enviar texto en: " + titleText + ". Reintentando...", e);

            try {
                By dynamicLocator = By.xpath("//div[contains(@title, '" + titleText + "')]/following-sibling::div//input[@type='text']");
                WebElement retryInput = waitUtil.waitUntil(ExpectedConditions.refreshed(
                        ExpectedConditions.visibilityOfElementLocated(dynamicLocator)
                ));

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", retryInput);

                safeClear(retryInput, titleText);
                retryInput.sendKeys(textToSend);

                LogUtil.info("Texto enviado exitosamente en reintento a campo: " + titleText);
            } catch (Exception retryEx) {
                LogUtil.error("Reintento fallido al enviar texto a campo: " + titleText, retryEx);
                throw new RuntimeException("Error en reintento de envío de texto al campo: " + titleText, retryEx);
            }
        } catch (Exception e) {
            LogUtil.error("Error al enviar texto al campo: " + titleText, e);
            throw new RuntimeException("Error al enviar texto al campo: " + titleText, e);
        }
    }

    /**
     * Ingresa un número entero en un campo de entrada (`input[type="text"]`)
     * ubicado dinámicamente a partir del título visible asociado.
     * <p>
     * Este método construye un localizador XPath utilizando el texto visible (etiqueta) del campo
     * —por ejemplo, "Cantidad máxima"— y localiza el `input` correspondiente en la misma estructura de formulario.
     * <p>
     * Realiza las siguientes acciones:
     * <ul>
     *     <li>Espera que el campo sea visible usando `ExpectedConditions`.</li>
     *     <li>Desplaza el campo al área visible mediante JavaScript.</li>
     *     <li>Limpia el contenido previo con `safeClear`.</li>
     *     <li>Escribe el número especificado en el campo.</li>
     *     <li>En caso de `StaleElementReferenceException`, reintenta la operación una vez más.</li>
     * </ul>
     *
     * @param titleText   El texto visible del título del campo (por ejemplo, "Cantidad máxima")
     *                    que se usará para construir el localizador XPath.
     * @param numberToSend El número entero que se desea ingresar en el campo localizado.
     * @throws RuntimeException Si el elemento no es encontrado, no es visible, o el envío de datos falla incluso tras reintento.
     */
    public void sendKeysByTitleInt(String titleText, int numberToSend) {

        String textToSend = String.valueOf(numberToSend);

        try {
            LogUtil.info("Esperando que el campo esté visible (con refreshed): " + titleText);
            By dynamicLocator = By.xpath("//span[contains(text(), '" + titleText + "')]/ancestor::div[contains(@class, 'form-control-container')]//input[@type='text']");

            waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.visibilityOfElementLocated(dynamicLocator)
            ));
            waitUtil.isElementVisible(dynamicLocator, 10000, 500);

            LogUtil.info("Preparando campo para ingreso de número: " + titleText + " → '" + textToSend + "'");

            WebElement input = waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.presenceOfElementLocated(dynamicLocator)
            ));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);

            safeClear(input, titleText);
            input.sendKeys(textToSend);

            LogUtil.info("Número enviado correctamente a campo: " + titleText);
        } catch (StaleElementReferenceException e) {
            LogUtil.warn("Campo obsoleto al enviar número en: " + titleText + ". Reintentando...", e);

            try {
                By dynamicLocator = By.xpath("//div[contains(@title, '" + titleText + "')]/following-sibling::div//input[@type='text']");
                WebElement retryInput = waitUtil.waitUntil(ExpectedConditions.refreshed(
                        ExpectedConditions.visibilityOfElementLocated(dynamicLocator)
                ));

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", retryInput);

                safeClear(retryInput, titleText);
                retryInput.sendKeys(textToSend);

                LogUtil.info("Número enviado exitosamente en reintento a campo: " + titleText);
            } catch (Exception retryEx) {
                LogUtil.error("Reintento fallido al enviar número a campo: " + titleText, retryEx);
                throw new RuntimeException("Error en reintento de envío de número al campo: " + titleText, retryEx);
            }
        } catch (Exception e) {
            LogUtil.error("Error al enviar número al campo: " + titleText, e);
            throw new RuntimeException("Error al enviar número al campo: " + titleText, e);
        }
    }

    /**
     * Ingresa texto en un campo de entrada (`input`) localizado dinámicamente a partir del
     * valor de su atributo <code>placeholder</code>.
     * <p>
     * Funciona igual que {@link #sendKeysByTitle(String, String)}, pero en vez de buscar la
     * etiqueta visible (“título”) construye un localizador XPath usando el texto del
     * <code>placeholder</code> —por ejemplo, "Correo electrónico".
     * <p>
     * Pasos que ejecuta:
     * <ul>
     *   <li>Construye un <code>By</code> dinámico: <code>//input[contains(@placeholder,'{placeholder}')]</code>.</li>
     *   <li>Espera a que el campo sea visible (con <code>ExpectedConditions.refreshed</code>).</li>
     *   <li>Hace scroll hasta dejar el campo en la zona visible con JavaScript.</li>
     *   <li>Limpia el contenido previo mediante <code>safeClear</code>.</li>
     *   <li>Escribe el texto deseado.</li>
     *   <li>Si se produce <code>StaleElementReferenceException</code>, reintenta una vez.</li>
     * </ul>
     *
     * @param placeholder Texto (o parte) del atributo <code>placeholder</code>,
     *                    p. ej. "Correo electrónico".
     * @param textToSend  Cadena que se quiere ingresar.
     * @throws RuntimeException Si el elemento no es encontrado, no es visible,
     *                          o el envío de texto falla incluso tras el reintento.
     */
    public void sendKeysByPlaceholder(String placeholder, String textToSend) {

        try {
            LogUtil.info("Esperando que el campo con placeholder esté visible (con refreshed): " + placeholder);
            By dynamicLocator = By.xpath("//input[@placeholder='" + placeholder + "']");//Actualiza la ruta a una mas corta para probar
//            By dynamicLocator = By.xpath("//input[contains(@placeholder, '" + placeholder + "')] | //div[contains(@class, 'input-container')]//input[@type='text']");

            waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.visibilityOfElementLocated(dynamicLocator)
            ));
            waitUtil.isElementVisible(dynamicLocator, 10000, 500);

            LogUtil.info("Preparando campo para ingreso de texto: " + placeholder + " → '" + textToSend + "'");

            WebElement input = waitUtil.waitUntil(ExpectedConditions.refreshed(
                    ExpectedConditions.presenceOfElementLocated(dynamicLocator)
            ));

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);

            safeClear(input, placeholder);
            input.sendKeys(textToSend);

            LogUtil.info("Texto enviado correctamente al campo con placeholder: " + placeholder);
        } catch (StaleElementReferenceException e) {
            LogUtil.warn("Campo obsoleto al enviar texto en placeholder: " + placeholder + ". Reintentando…", e);

            try {
                // Re-localizador alternativo por ID o name, si existiese. Ajusta según tu HTML:
                By retryLocator = By.xpath("//input[contains(@placeholder, '" + placeholder + "')]");
                WebElement retryInput = waitUtil.waitUntil(ExpectedConditions.refreshed(
                        ExpectedConditions.visibilityOfElementLocated(retryLocator)
                ));

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", retryInput);

                safeClear(retryInput, placeholder);
                retryInput.sendKeys(textToSend);

                LogUtil.info("Texto enviado exitosamente en reintento al campo con placeholder: " + placeholder);
            } catch (Exception retryEx) {
                LogUtil.error("Reintento fallido al enviar texto al campo con placeholder: " + placeholder, retryEx);
                throw new RuntimeException("Error en reintento de envío de texto al campo con placeholder: " + placeholder, retryEx);
            }
        } catch (Exception e) {
            LogUtil.error("Error al enviar texto al campo con placeholder: " + placeholder, e);
            throw new RuntimeException("Error al enviar texto al campo con placeholder: " + placeholder, e);
        }
    }

    /**
     * Modifica el valor de una celda editable en una tabla HTML localizada por un valor original.
     * <p>
     * Este metodo realiza las siguientes acciones:
     * <ol>
     *     <li>Localiza la celda correspondiente al registro original proporcionado.</li>
     *     <li>Hace clic en la celda para activarla en modo edición.</li>
     *     <li>Ingresa el nuevo valor proporcionado.</li>
     *     <li>Hace clic fuera del campo para cerrar la edición y guardar los cambios.</li>
     * </ol>
     *
     * @param column         Nombre de la columna donde se encuentra el campo editable.
     * @param originalRecord Valor actual de la celda que se desea modificar.
     * @param newRecord      Nuevo valor que se desea ingresar en la celda.
     */
    public void modifyCell(String column, String originalRecord, String newRecord) {
        // Paso 1: Localizar y hacer clic en la celda del registro
        WebElement element = waitUtil.findVisibleElement(tableUtil.buildRecordLocator(originalRecord));
        waitUtil.sleepMillis(300, "Espera a que la tabla se renderice completamente"); // realiza una pequeña espera a que la tabla se renderice completamente
        clickByElement(element, "Celda: " + originalRecord);

        // Paso 2: Ingresar el nuevo registro
        sendKeysByLocator(By.xpath("//input[contains(@class,'p-inputtext') or @pinputtext]"), newRecord, column + " editable");

        // Paso 3: Hacer clic fuera (en el body) para cerrar el campo editable y guardar los cambios
        clickOutside();
    }

    /**
     * Realiza una búsqueda de un registro en un campo de búsqueda interactivo.
     * <p>
     * Este metodo hace clic en el icono de la lupa para desplegar el campo de búsqueda,
     * luego escribe el texto proporcionado en el input asociado al localizador entregado.
     * </p>
     *
     * @param record        El texto del registro que se desea buscar.
     * @param locatorSearch El localizador {@link By} que identifica el campo de entrada donde se ingresará el texto.
     */
    public void searchRecord(String record, By locatorSearch) {
        clickByLocator(By.xpath("//imperia-icon-button[@type='button' and @icon='search']//a[@role='button']"), "Icono lupa");
        sendKeysByLocator(locatorSearch, record, "Input lupa");
    }

    /**
     * Verifica si un botón con la etiqueta especificada está presente y visible.
     *
     * @param buttonLabel Texto visible del botón (por ejemplo: "Nuevo", "Aceptar").
     * @return true si el botón está visible, false en caso contrario.
     */
    public boolean isButtonVisible(String buttonLabel) {
        return validationUtil.isButtonVisible(buttonLabel);
    }

    /**
     * Modifica el valor de un registro existente en una celda editable de una tabla.
     *
     * <p>Este metodo localiza la celda correspondiente al valor original, hace clic sobre ella para activar el modo edición,
     * reemplaza el contenido con un nuevo valor, y luego hace clic fuera del campo para confirmar y guardar el cambio.
     *
     * @param column         Nombre de la columna donde se encuentra el valor a modificar (utilizado para logging).
     * @param screen         Nombre de la pantalla o contexto (utilizado para logging).
     * @param originalRecord Valor original del registro a localizar en la tabla.
     * @param newRecord      Nuevo valor que se desea ingresar en la celda editable.
     */
    public void modifyRecord(String column, String screen, String originalRecord, String newRecord) {
        // Paso 1: Localizar y hacer clic en la celda del registro
        WebElement element = waitUtil.findVisibleElement(tableUtil.buildRecordLocator(originalRecord));
        clickByElement(element, "Celda " + column + " de " + screen + " " + originalRecord);

        // Paso 2: Ingresar el nuevo registro
        sendKeysByLocator(By.xpath("//input[contains(@class,'p-inputtext') or @pinputtext]"), newRecord, column + " editable");

        // Paso 3: Hacer clic fuera (en el body) para cerrar el campo editable y guardar los cambios
        clickOutside();
    }

    /**
     * Hace clic en el botón o enlace en la posición especificada, identificado por su texto visible exacto.
     *
     * Este metodo realiza los siguientes pasos:
     * <ul>
     *   <li>Construye dinámicamente un XPath que selecciona el botón o enlace con el texto dado y en la posición indicada.</li>
     *   <li>Espera a que el elemento sea visible y clickeable.</li>
     *   <li>Captura evidencia y realiza el clic.</li>
     *   <li>Registra logs detallados del proceso.</li>
     * </ul>
     *
     * <strong>Nota:</strong> La posición es 1-based (es decir, 1 para el primero, 2 para el segundo, etc.).
     * Asegúrate de que exista al menos esa cantidad de elementos con el texto indicado.
     *
     * @param buttonName Texto visible del botón o enlace a buscar.
     * @param position Posición del elemento a seleccionar (comenzando desde 1).
     */
    public void clickButtonByNameAndPosition(String buttonName, int position) {

        LogUtil.info("Buscando botón con texto: " + buttonName + " en la posición: " + position);

        // Construye el XPath dinámico con índice entre paréntesis
        String xpath = "(//*[self::a or self::button][normalize-space(.)='" + buttonName + "'])[" + position + "]";
        By buttonLocator = By.xpath(xpath);

        // Espera que sea clickeable, toma screenshot, hace clic, y registra logs
        clickByLocator(buttonLocator, buttonName + " [posición " + position + "]");
    }

    /**
     * Hace clic en el último botón o enlace que coincida exactamente con el texto proporcionado.
     *
     * <p>Este metodo realiza lo siguiente:</p>
     * <ul>
     *   <li>Construye un XPath dinámico que selecciona el último botón o enlace con el texto visible indicado.</li>
     *   <li>Espera que el elemento sea visible y clickeable.</li>
     *   <li>Captura evidencia, realiza clic y registra logs detallados.</li>
     * </ul>
     *
     * <strong>Nota:</strong> Si no existe ningún elemento con ese texto, se lanzará una excepción.
     *
     * @param buttonName Texto visible exacto del botón o enlace a buscar.
     */
    public void clickButtonByNameLast(String buttonName) {
        LogUtil.info("Buscando el último botón con texto exacto: " + buttonName);

        // XPath para encontrar el último botón o enlace con ese texto
        String xpath = "(//*[self::a or self::button][normalize-space(.)='" + buttonName + "'])[last()]";
        By buttonLocator = By.xpath(xpath);

        // Espera, clic, evidencia y log
        clickByLocator(buttonLocator, buttonName + " [último]");
    }

    /**
     * Envía un número entero a un campo después de limpiarlo y esperar visibilidad.
     *
     * @param element Elemento de entrada (input) al que se enviará el número.
     * @param number Número entero a enviar.
     * @param fieldName Nombre descriptivo del campo para logging.
     */
    public void sendKeysByElement(WebElement element, int number, String fieldName) {
        try {
            LogUtil.info("Limpiando campo: " + fieldName);
            element.clear();

            String text = String.valueOf(number);
            LogUtil.info("Enviando número a campo: " + fieldName + " → '" + text + "'");
            element.sendKeys(text);
            LogUtil.info("Número enviado correctamente a campo: " + fieldName);
        } catch (Exception e) {
            LogUtil.error("Error al enviar número a campo: " + fieldName, e);
            throw new RuntimeException("Error al enviar número al campo: " + fieldName, e);
        }
    }

    /**
     * Envía un número decimal a un campo de entrada después de limpiarlo y esperar visibilidad.
     *
     * @param element Elemento de entrada (input) al que se enviará el valor.
     * @param value Valor numérico (double) a enviar.
     * @param fieldName Nombre descriptivo del campo para logging.
     */
    public void sendKeysByElement(WebElement element, double value, String fieldName) {
        try {
            String text = String.valueOf(value);

            LogUtil.info("Limpiando campo: " + fieldName);
            element.clear();

            LogUtil.info("Enviando valor numérico a campo: " + fieldName + " → '" + text + "'");
            element.sendKeys(text);
            LogUtil.info("Valor enviado correctamente a campo: " + fieldName);
        } catch (Exception e) {
            LogUtil.error("Error al enviar valor numérico a campo: " + fieldName, e);
            throw new RuntimeException("Error al enviar valor numérico al campo: " + fieldName, e);
        }
    }

    /**
     * Presiona la tecla Enter sobre el elemento proporcionado.
     * <p>
     * Este metodo es útil para confirmar formularios, activar acciones o salir de campos editables
     * donde la tecla Enter tiene un comportamiento definido en la aplicación.
     * </p>
     *
     * @param element el elemento sobre el cual se enviará la tecla Enter.
     */
    public void pressEnterKey(WebElement element) {
        try {
            LogUtil.info("Presionando tecla Enter sobre el elemento: " + element);
            element.sendKeys(Keys.ENTER);
            LogUtil.info("Tecla Enter enviada correctamente.");
        } catch (Exception e) {
            LogUtil.error("Error al presionar Enter sobre el elemento.", e);
            throw new RuntimeException("No se pudo presionar Enter sobre el elemento.", e);
        }
    }

    /**
     * Aplica un filtro por el nombre de columna especificado, usando el valor proporcionado.
     *
     * @param filter Nombre de la columna por la cual se aplicará el filtro (ej. "Código").
     * @param value  Valor que se usará para filtrar los resultados.
     */
    public void applyFilterBy(String filter, String value) {
        filterBy(filter, value);
    }

    /**
     * Refresca la página actual del navegador.
     */
    public void refreshPage() {
        LogUtil.info("Refrescando la página actual.");
        driver.navigate().refresh();
    }

    /**
     * Envía un número {@link BigDecimal} a un campo después de limpiarlo y esperar visibilidad.
     *
     * @param element   Elemento de entrada (input) al que se enviará el número.
     * @param number    Número {@link BigDecimal} a enviar.
     * @param fieldName Nombre descriptivo del campo para logging.
     */
    public void sendKeysByElement(WebElement element, BigDecimal number, String fieldName) {
        try {
            LogUtil.info("Limpiando campo: " + fieldName);
            element.clear();

            String text = number != null ? number.toPlainString() : "";
            LogUtil.info("Enviando número a campo: " + fieldName + " → '" + text + "'");
            element.sendKeys(text);
            LogUtil.info("Número enviado correctamente a campo: " + fieldName);
        } catch (Exception e) {
            LogUtil.error("Error al enviar número a campo: " + fieldName, e);
            throw new RuntimeException("Error al enviar número al campo: " + fieldName, e);
        }
    }

    /**
     * Verifica que el mensaje mostrado coincida con el esperado.
     *
     * @param expectedMessage Texto exacto que se espera ver en pantalla.
     */
    public void verifyMessageDisplayed(String expectedMessage) {
        String actualMessage = popupUtil.getMessageText();
        ValidationUtil.assertEquals(actualMessage, expectedMessage, "El mensaje mostrado no coincide con el esperado");
    }

    /**
     * Hace clic en el botón indicado para guardar un archivo y espera la descarga.
     * <p>
     * Si el botón no está disponible directamente, intenta abrir el menú "Exportar" y luego reintenta el clic.
     * Finalmente, espera la descarga de un archivo con extensión xlsx, xls, csv o zip y lo registra en el contexto de escenario.
     *
     * @param buttonName Nombre visible del botón que dispara la descarga del archivo.
     * @throws IllegalStateException Si la carpeta de descargas no existe o no tiene permisos de escritura.
     * @throws RuntimeException Si no se puede hacer clic en el botón o no se detecta la descarga.
     */
    public void clickTheButtonToSaveTheFile(String buttonName) {
        String downloadsFolderPath = Hooks.getDownloadsFolderPath();
        if (downloadsFolderPath == null || downloadsFolderPath.isEmpty()) {
            throw new IllegalStateException("Carpeta de descargas no inicializada para el escenario actual.");
        }

        Path dl = Paths.get(downloadsFolderPath);
        if (!Files.isDirectory(dl) || !Files.isWritable(dl)) {
            throw new IllegalStateException("Carpeta de descargas inválida o sin permisos: " + dl);
        }

        DownloadWatcherUtil watcher = new DownloadWatcherUtil(dl);

        // 1) Intento directo sobre el botón real
        try {
            clickButtonByName(buttonName);
        } catch (RuntimeException firstError) {
            LogUtil.warn("No se pudo clicar '" + buttonName + "' directamente. Intentando abrir 'Exportar…'");

            // 2) Intentar abrir el trigger Exportar... / Exportar…
            boolean opened = false;
            try {
                clickByLocator(By.xpath("//*[self::button or self::span][normalize-space(.)='Exportar' or normalize-space(.)='Exportar…' or normalize-space(.)='Exportar...']"), "Botón Exportar");
                opened = true;
            } catch (RuntimeException e) {
                try {
                    clickByLocator(By.xpath("//*[self::button or self::span][normalize-space(.)='Exportar' or normalize-space(.)='Exportar…' or normalize-space(.)='Exportar...']"), "Botón Exportar");
//                    clickButtonByName("Exportar…"); // elipsis real
                    opened = true;
                } catch (RuntimeException ignored) {
                    // Si no se pudo abrir el trigger, relanzamos el error original del botón real
                    throw firstError;
                }
            }

            // 3) Reintento sobre el botón real dentro del menú Exportar
            LogUtil.info("Trigger 'Exportar' abierto: reintentando '" + buttonName + "'");
            clickButtonByName(buttonName);
        }

        // 4) Espera de descarga y “sellado”
        File stamped = watcher.waitNewAndStamp(".*\\.(xlsx|xls|csv|zip)", Duration.ofSeconds(50));
        ScenarioContext.DownloadContext.set(stamped);
    }

    /**
     * Verifica que el último archivo descargado en el escenario es válido.
     * <p>
     * Toma el archivo desde {@link config.ScenarioContext.DownloadContext#get()} y
     * valida, mediante {@code validationUtil}, que:
     * </p>
     * <ol>
     *   <li>Se registró un archivo (no es {@code null}).</li>
     *   <li>El archivo existe físicamente en disco.</li>
     *   <li>El tamaño es mayor que cero (no está vacío).</li>
     *   <li>El nombre cumple el patrón de sello temporal:
     *       {@code <base>_yyyyMMdd_HHmmss.(xlsx|xls|csv|zip)}.</li>
     * </ol>
     *
     * <h3>Precondiciones</h3>
     * <ul>
     *   <li>Algún paso previo debió guardar la descarga en
     *       {@link config.ScenarioContext.DownloadContext#set(java.io.File)} (p. ej.,
     *       tras hacer clic en “Guardar/Descargar”).</li>
     *   <li>El proceso de descarga y renombrado (con timestamp) se completó correctamente.</li>
     * </ul>
     *
     * <h3>Detalles del nombre</h3>
     * Se valida con la expresión regular:
     * <pre>{@code
     * .+_\d{8}_\d{6}\.(xlsx|xls|csv|zip)
     * }</pre>
     * que corresponde a un sufijo {@code _yyyyMMdd_HHmmss} antes de la extensión permitida.
     *
     * <h3>Resultados/Excepciones</h3>
     * Si cualquiera de las verificaciones falla, {@code validationUtil} debe lanzar la
     * aserción correspondiente (p. ej. {@link AssertionError} o la excepción propia del framework).
     *
     * @see config.ScenarioContext.DownloadContext
     * @see ui.utils.DownloadWatcherUtil
     */
    public void assertExcelDownloaded() {
        File f = ScenarioContext.DownloadContext.get();
        validationUtil.assertNotNull(f, "Validar que el archivo se registro");
        validationUtil.assertTrue(f.exists(), "El archivo existe: " + f);
        validationUtil.assertTrue(f.length() > 0, "El archivo no está vacío: " + f);
        validationUtil.assertTrue(f.getName().matches(".+_\\d{8}_\\d{6}\\.(xlsx|xls|csv|zip)$"), "El nombre contiene el timestamp esperado (yyyyMMdd_HHmmss): " + f.getName()
        );
    }

    public void verifyThatTheHyperlinkRedirectsToTheUrl(String hipervinculo, String url) {
        clickButtonByName(hipervinculo);
    }

    /**
     * Navega hacia la página anterior dentro del historial del navegador.
     *
     * <p>Registra logs informativos antes y después de ejecutar la acción para facilitar el
     * rastreo en reportes y depuración. En caso de presentarse un error durante la navegación,
     * captura el detalle en un log de error y relanza la excepción como {@link RuntimeException}.
     */
    public void navigateBack() {
        try {
            LogUtil.info("Navegando hacia la página anterior del navegador.");
            driver.navigate().back();
            LogUtil.info("Navegación hacia atrás completada.");
        } catch (Exception e) {
            LogUtil.error("Error al navegar hacia atrás en el navegador.", e);
            throw new RuntimeException("No fue posible navegar hacia atrás en el navegador.", e);
        }
    }
}