package ui.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ui.manager.PageManager;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Clase de utilidades para aplicar esperas explícitas en elementos web.
 */
public class WaitUtil {

    private final WebDriver driver;
    private final WebDriverWait wait;

    /**
     * Constructor que inicializa el WebDriverWait con el tiempo por defecto.
     *
     * @param driver WebDriver activo.
     */
    public WaitUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60)); // Configurable
    }

    /**
     * Espera a que un elemento sea visible.
     *
     * @param element WebElement objetivo.
     */
    public void waitForVisibilityByElement(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Espera explícitamente hasta que un elemento ubicado por el localizador especificado
     * sea visible en el DOM y lo retorna como un {@link WebElement}.
     *
     * <p>Este metodo utiliza {@link ExpectedConditions#visibilityOfElementLocated(By)} para
     * garantizar que el elemento no solo exista en el DOM, sino que también sea visible
     * (es decir, tenga altura y anchura mayores a cero, y no esté oculto con CSS).</p>
     *
     * @param locator El localizador {@link By} del elemento a esperar.
     * @return El {@link WebElement} visible una vez que aparece.
     * @throws org.openqa.selenium.TimeoutException si el tiempo de espera se agota antes de que el elemento sea visible.
     */
    public WebElement waitForVisibilityByLocator(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Verifica si un elemento ubicado por el localizador especificado se vuelve visible en el DOM
     * dentro del tiempo de espera configurado.
     *
     * <p>Este metodo permite configurar explícitamente el tiempo máximo de espera (timeout)
     * y el intervalo de sondeo (polling) para detectar la visibilidad del elemento.</p>
     *
     * <p>Internamente, utiliza {@link ExpectedConditions#visibilityOfElementLocated(By)} con una espera explícita
     * personalizada mediante {@link WebDriverWait}.</p>
     *
     * @param locator         El localizador {@link By} del elemento a verificar.
     * @param timeoutMillis   Tiempo máximo de espera en milisegundos para que el elemento sea visible.
     * @param pollingMillis   Intervalo de tiempo entre cada intento de verificación.
     * @return {@code true} si el elemento se vuelve visible antes de que expire el tiempo de espera,
     *         {@code false} si no se vuelve visible a tiempo.
     */
    public boolean isElementVisible(By locator, int timeoutMillis, int pollingMillis) {
        LogUtil.info("Iniciando verificación de visibilidad del elemento: " + locator);
        LogUtil.info("Tiempo máximo de espera: " + timeoutMillis + " ms | Intervalo de sondeo: " + pollingMillis + " ms");

        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofMillis(timeoutMillis));
        customWait.pollingEvery(Duration.ofMillis(pollingMillis));

        long start = System.currentTimeMillis();
        try {
            customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.info("Elemento visible tras " + elapsed + " ms: " + locator);
            return true;
        } catch (TimeoutException e) {
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.warn("Elemento NO visible tras " + elapsed + " ms: " + locator);
            return false;
        }
    }

    /**
     * Espera a que un elemento sea clickeable.
     *
     * Este metodo realiza lo siguiente:
     * <ul>
     *   <li>Registra en los logs que se inició la espera para que el elemento sea clickeable.</li>
     *   <li>Utiliza una espera explícita hasta que el elemento esté disponible para hacer clic.</li>
     *   <li>Registra en los logs cuando el elemento ya es clickeable.</li>
     *   <li>En caso de error, registra el fallo en los logs y lanza una excepción con los detalles.</li>
     * </ul>
     *
     * @param element WebElement objetivo.
     */
    public void waitForClickable(WebElement element) {
        try {
            LogUtil.info("Esperando que el elemento sea clickeable: " + element.toString());
            wait.until(ExpectedConditions.elementToBeClickable(element));
            LogUtil.info("El elemento ya es clickeable: " + element.toString());
        } catch (Exception e) {
            LogUtil.error("El elemento no se volvió clickeable: " + element.toString(), e);
            throw new RuntimeException("Error al esperar que el elemento sea clickeable: " + element.toString(), e);
        }
    }

    /**
     * Espera a que un texto esté presente en el elemento.
     *
     * @param element WebElement objetivo.
     * @param text Texto esperado.
     */
    public void waitForText(WebElement element, String text) {
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    /**
     * Espera a que un elemento desaparezca (no sea visible) y lo registra en el log.
     *
     * @param element     WebElement objetivo.
     * @param elementName Nombre descriptivo del elemento para el log.
     * @throws RuntimeException si el elemento sigue siendo visible después del tiempo de espera.
     */
    public void waitForInvisibility(WebElement element, String elementName) {
        try {
            LogUtil.info("Esperando a que el elemento desaparezca: " + elementName);
            wait.until(ExpectedConditions.invisibilityOf(element));
            LogUtil.info("El elemento desapareció correctamente: " + elementName);
        } catch (TimeoutException e) {
            LogUtil.error("El elemento no desapareció en el tiempo esperado: " + elementName, e);
            throw new RuntimeException("El elemento '" + elementName + "' no desapareció dentro del tiempo límite.", e);
        } catch (Exception e) {
            LogUtil.error("Error inesperado al esperar que desaparezca: " + elementName, e);
            throw new RuntimeException("Error inesperado al esperar que el elemento '" + elementName + "' desaparezca.", e);
        }
    }

    /**
     * Espera a que un elemento identificado por un locator desaparezca (es decir, deje de ser visible o no esté en el DOM),
     * dentro del tiempo y el intervalo especificado, registrando el proceso en el log.
     *
     * <p>Este metodo utiliza una espera explícita personalizada con tiempo máximo y frecuencia de sondeo definidos por el usuario.</p>
     *
     * @param locator        Localizador {@link By} del elemento que se espera que desaparezca.
     * @param elementName    Nombre descriptivo del elemento (para fines de log).
     * @param timeoutMillis  Tiempo máximo de espera en milisegundos para que el elemento desaparezca.
     * @param pollingMillis  Intervalo de sondeo en milisegundos entre cada verificación.
     * @throws RuntimeException si el elemento no desaparece dentro del tiempo especificado.
     */
    public void waitForInvisibility(By locator, String elementName, int timeoutMillis, int pollingMillis) {
        LogUtil.info("Esperando desaparición del elemento: " + elementName);
        LogUtil.info("Timeout configurado: " + timeoutMillis + " ms | Intervalo de sondeo: " + pollingMillis + " ms");

        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofMillis(timeoutMillis));
        customWait.pollingEvery(Duration.ofMillis(pollingMillis));

        long start = System.currentTimeMillis();
        try {
            customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.info("Elemento '" + elementName + "' desapareció tras " + elapsed + " ms.");
        } catch (TimeoutException e) {
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.error("Timeout: el elemento '" + elementName + "' no desapareció tras " + elapsed + " ms.", e);
            throw new RuntimeException("El elemento '" + elementName + "' no desapareció dentro del tiempo límite.", e);
        } catch (Exception e) {
            LogUtil.error("Error inesperado esperando que desaparezca el elemento '" + elementName + "'.", e);
            throw new RuntimeException("Error inesperado al esperar que el elemento '" + elementName + "' desaparezca.", e);
        }
    }

    /**
     * Espera a que un {@link WebElement} desaparezca (es decir, deje de ser visible),
     * dentro del tiempo y el intervalo especificado, registrando el proceso en el log.
     *
     * <p>Este metodo es útil cuando ya tienes una referencia directa al elemento
     * y deseas esperar su invisibilidad con control de tiempo y sondeo personalizado.</p>
     *
     * @param element        WebElement que se espera que desaparezca.
     * @param elementName    Nombre descriptivo del elemento (para fines de log).
     * @param timeoutMillis  Tiempo máximo de espera en milisegundos.
     * @param pollingMillis  Intervalo de sondeo entre verificaciones.
     * @throws RuntimeException si el elemento no desaparece dentro del tiempo especificado.
     */
    public void waitForInvisibility(WebElement element, String elementName, int timeoutMillis, int pollingMillis) {
        LogUtil.info("Esperando desaparición del WebElement: " + elementName);
        LogUtil.info("Timeout configurado: " + timeoutMillis + " ms | Intervalo de sondeo: " + pollingMillis + " ms");

        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofMillis(timeoutMillis));
        customWait.pollingEvery(Duration.ofMillis(pollingMillis));

        long start = System.currentTimeMillis();
        try {
            customWait.until(ExpectedConditions.invisibilityOf(element));
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.info("WebElement '" + elementName + "' desapareció tras " + elapsed + " ms.");
        } catch (TimeoutException e) {
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.error("Timeout: el WebElement '" + elementName + "' no desapareció tras " + elapsed + " ms.", e);
            throw new RuntimeException("El WebElement '" + elementName + "' no desapareció dentro del tiempo límite.", e);
        } catch (Exception e) {
            LogUtil.error("Error inesperado esperando que desaparezca el WebElement '" + elementName + "'.", e);
            throw new RuntimeException("Error inesperado al esperar que el WebElement '" + elementName + "' desaparezca.", e);
        }
    }


    /**
     * Realiza una pausa fija del hilo actual durante la cantidad de milisegundos indicada.
     *
     * Este metodo utiliza {@code Thread.sleep} para detener la ejecución durante un tiempo determinado.
     * En caso de que el hilo sea interrumpido, se registra el error en el log y se restablece el estado de interrupción.
     *
     * <strong>Nota:</strong> Esta es una espera forzada (hard wait), y debe usarse con precaución.
     * Se recomienda preferir esperas explícitas con condiciones (como WebDriverWait) siempre que sea posible.
     *
     * @param millis Cantidad de milisegundos que el hilo debe pausar.
     * @param reason Descripción del motivo por el cual se realiza la espera.
     */
    public void sleepMillis(long millis, String reason) {
        LogUtil.info("Iniciando pausa de " + millis + " ms. Motivo: " + reason);
        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LogUtil.error("El hilo fue interrumpido durante el sleep de " + millis + " ms. Motivo: " + reason, e);
            Thread.currentThread().interrupt();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            LogUtil.info("Pausa finalizada. Tiempo transcurrido: " + elapsed + " ms. Motivo: " + reason);
        }
    }

    /**
     * Aplica una espera explícita hasta que se cumpla una condición personalizada definida
     * mediante una función que recibe el {@link WebDriver} y retorna un resultado.
     *
     * <p>Este metodo es útil para crear condiciones personalizadas o reutilizar condiciones
     * provistas por {@link org.openqa.selenium.support.ui.ExpectedConditions}, y encapsula
     * el manejo del timeout definido en la instancia de {@link WebDriverWait}.</p>
     *
     * @param <T> Tipo del valor retornado por la condición (puede ser {@code Boolean}, {@code WebElement}, etc).
     * @param condition La condición a evaluar, expresada como una función que recibe un {@link WebDriver}.
     * @return El valor retornado por la condición una vez que se cumple.
     * @throws org.openqa.selenium.TimeoutException si la condición no se cumple dentro del tiempo de espera configurado.
     */
    public <T> T waitUntil(Function<WebDriver, T> condition) {
        return wait.until(condition);
    }

    /**
     * Espera hasta que el elemento sea visible y lo retorna.
     * Reintenta automáticamente en caso de excepción o si el elemento no es válido tras la espera.
     *
     * @param locator localizador del elemento.
     * @return WebElement visible.
     */
    public WebElement findVisibleElement(By locator) {
        int attempts = 2; //Intentos en caso de que falle
        int time = 100; //Tiempo de espera entre intentos

        for (int i = 1; i <= attempts; i++) {
            try {
                LogUtil.info("Intento " + i + " - Esperando visibilidad del elemento con locator: " + locator.toString());
                WebElement element = waitForVisibilityByLocator(locator);

                if (element != null && element.isDisplayed()) {
                    LogUtil.info("Elemento visible encontrado: " + locator.toString());
                    return element;
                } else {
                    LogUtil.warn("Elemento no visible tras espera, reintentando...");
                }

            } catch (Exception e) {
                LogUtil.warn("Error en intento " + i + " para localizar elemento visible: " + locator.toString() + ". Reintentando...", e);
            }
            sleepMillis(time, "Tiempo de espera entre intentos");
        }

        LogUtil.error("No se pudo encontrar un elemento visible con locator: " + locator.toString());
        throw new RuntimeException("Error al encontrar el elemento visible con locator: " + locator.toString());
    }

    /**
     * Espera a que todos los elementos hijos ubicados por un localizador dentro de un elemento padre
     * sean visibles, y los retorna como una lista de {@link WebElement}.
     * Reintenta automáticamente si hay error, si la lista está vacía o si ocurre StaleElementReferenceException.
     *
     * @param parent       El elemento padre donde buscar.
     * @param childLocator El localizador para los elementos hijos.
     * @return Lista de elementos visibles encontrados.
     * @throws RuntimeException si no se encuentran elementos visibles tras reintento.
     */
    public List<WebElement> findVisibleElements(WebElement parent, By childLocator) {
        int attempts = 3; // número de intentos
        int time = 500; // tiempo de espera entre intentos (ms)

        for (int i = 1; i <= attempts; i++) {
            try {
                LogUtil.info("Intento " + i + " - Esperando visibilidad de elementos hijos: " + childLocator.toString());

                // Espera a que los elementos anidados sean visibles (si al menos uno lo es, la condición pasa)
                wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(parent, childLocator));

                // Recuperamos los elementos dentro del parent y filtramos sólo los visibles
                List<WebElement> elements = parent.findElements(childLocator)
                        .stream()
                        .filter(el -> {
                            try {
                                return el.isDisplayed();
                            } catch (StaleElementReferenceException sere) {
                                // si el elemento quedó stale al comprobar isDisplayed, lo tratamos como no visible
                                return false;
                            }
                        })
                        .collect(Collectors.toList());

                if (elements != null && !elements.isEmpty()) {
                    LogUtil.info("Se encontraron " + elements.size() + " elementos visibles.");
                    return elements;
                } else {
                    LogUtil.warn("Lista de elementos vacía tras filtrar por visibilidad. Reintentando...");
                }

            } catch (StaleElementReferenceException sere) {
                LogUtil.warn("StaleElementReferenceException en intento " + i + " para locator: " + childLocator.toString() + ". Reintentando...", sere);
            } catch (Exception e) {
                LogUtil.warn("Error en intento " + i + " para localizar elementos hijos: " + childLocator.toString() + ". Reintentando...", e);
            }

            sleepMillis(time, "Tiempo de espera entre intentos");
        }

        LogUtil.error("No se pudieron encontrar elementos visibles con locator: " + childLocator.toString());
        throw new RuntimeException("Error al encontrar elementos visibles con locator: " + childLocator.toString());
    }

    /**
     * Realiza un clic seguro sobre un elemento ubicado por el localizador especificado.
     *
     * <p>Este metodo espera explícitamente a que el elemento sea clickeable mediante
     * {@link ExpectedConditions#elementToBeClickable(By)}, y utiliza {@link ExpectedConditions#refreshed}
     * para evitar errores de tipo {@link StaleElementReferenceException} cuando el DOM ha sido redibujado.</p>
     *
     * <p>Si el elemento se vuelve obsoleto (stale) después de la espera inicial, el metodo
     * reintenta automáticamente la espera y el clic una segunda vez.</p>
     *
     * <p>Este metodo es útil en aplicaciones dinámicas como Angular o React donde el DOM
     * puede cambiar entre la localización del elemento y la acción sobre él.</p>
     *
     * @param locator El localizador {@link By} del elemento a hacer clic.
     * @throws TimeoutException si el elemento no se vuelve clickeable en el tiempo configurado.
     * @throws RuntimeException si ocurre otro error inesperado durante la interacción.
     */
    public void safeClick(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.refreshed(
                    ExpectedConditions.elementToBeClickable(locator)
            ));
            element.click();
        } catch (StaleElementReferenceException e) {
            LogUtil.warn("Elemento obsoleto, reintentando clic para: " + locator.toString(), e);
            WebElement element = wait.until(ExpectedConditions.refreshed(
                    ExpectedConditions.elementToBeClickable(locator)
            ));
            element.click();
        }
    }

    /**
     * Verifica si existen elementos presentes en el DOM usando un localizador.
     * No espera visibilidad ni lanza excepción si no existen.
     *
     * @param locator localizador del elemento.
     * @return true si existen elementos presentes, false si no.
     */
    public boolean isElementPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            LogUtil.warn("Error al verificar presencia de elemento: " + locator.toString(), e);
            return false;
        }
    }

    /**
     * Espera a que el campo input esté completamente vacío (value == "").
     *
     * @param input WebElement del campo input.
     * @throws TimeoutException si el campo no queda vacío dentro del tiempo de espera.
     */
    public void waitForInputToBeEmpty(WebElement input) {
        LogUtil.info("Esperando a que el input esté vacío: " + input.toString());
        wait.until(driver -> {
            String value = input.getAttribute("value");
            LogUtil.info("Valor actual del input: '" + value + "'");
            return value != null && value.isEmpty();
        });
        LogUtil.info("El input quedó vacío correctamente.");
    }

    /**
     * Espera hasta que el elemento especificado esté presente en el DOM.
     *
     * @param locator Localizador del elemento a esperar.
     * @return WebElement presente en el DOM.
     * @throws TimeoutException si el elemento no aparece dentro del tiempo configurado.
     */
    public WebElement waitForPresenceOfElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Busca y retorna una lista de elementos web visibles en la página para un localizador dado.
     *
     * <p>Este mtodo realiza una espera explícita hasta que al menos uno de los elementos localizados
     * sea visible en el DOM. Luego intenta recuperar todos los elementos localizados y filtra aquellos
     * que realmente están visibles utilizando {@code isDisplayed()}, manejando posibles excepciones de tipo
     * {@link StaleElementReferenceException} que pueden ocurrir si el DOM se actualiza dinámicamente.</p>
     *
     * <p>Si se detecta una excepción {@code StaleElementReferenceException}, se reintenta la operación
     * hasta tres veces con una breve pausa entre intentos.</p>
     *
     * @param locator Localizador {@link By} que identifica los elementos a buscar.
     * @return Lista de {@link WebElement} que están visibles en la página. Si no hay elementos visibles o todos fallan, se retorna una lista vacía.
     */
    public List<WebElement> findVisibleElements(By locator) {
        int attempts = 3; //Numero de intentos en caso de que falle
        int time = 100; //Tiempo de espera entre intentos en segundos

        // Reintento sencillo para manejar elementos stale
        for (int i = 0; i < attempts; i++) {
            try {
                waitForVisibilityByLocator(locator);
                List<WebElement> elements = driver.findElements(locator);
                return elements.stream()
                        .filter(e -> {
                            try {
                                return e.isDisplayed();
                            } catch (StaleElementReferenceException ex) {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            } catch (StaleElementReferenceException e) {
                // Esperar un poco y reintentar
                try {
                    Thread.sleep(time);
                } catch (InterruptedException ignored) {}
            }
        }

        // Último intento sin filtrar
        return Collections.emptyList();
    }

    /**
     * Realiza scroll en una tabla virtual con `cdk-virtual-scroll` hasta que el elemento esté visible en el DOM.
     *
     * @param targetLocator Localizador del elemento esperado (generalmente un <td>, <tr> o <span>).
     * @throws RuntimeException si el elemento no se encuentra tras scroll completo.
     */
    public void scrollUntilElementIsVisible(By targetLocator) {
        LogUtil.info("Iniciando scroll para buscar elemento: " + targetLocator);

        // Contenedor real con scroll, no el <cdk-virtual-scroll-viewport>
        By scrollableContainerLocator = By.cssSelector(".cdk-virtual-scrollable");
        WebElement scrollable = findVisibleElement(scrollableContainerLocator);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Cantidad de píxeles que se desplaza verticalmente en cada iteración del scroll.
        // Un valor menor hace scrolls más cortos, lo que mejora la precisión en la carga de elementos
        // en componentes virtuales como cdk-virtual-scroll, pero puede aumentar el tiempo total de búsqueda.
        int scrollStep = 50;

        // Tiempo de espera entre scroll que permite que se rendericen nuevos elementos
        int sleepMillis = 300;

        // Número máximo de iteraciones de scroll permitidas antes de abandonar la búsqueda.
        // Actúa como límite de seguridad para evitar ciclos infinitos en caso de que el elemento nunca aparezca.
        int maxScrolls = 50;

        for (int i = 0; i < maxScrolls; i++) {
            if (isElementPresent(targetLocator)) {
                WebElement element = findVisibleElement(targetLocator);
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                waitForVisibilityByElement(element);
                LogUtil.info("Elemento visible tras scroll: " + targetLocator);
                return;
            }

            // Ejecutar scroll vertical
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];",
                    scrollable, scrollStep);
            sleepMillis(sleepMillis, "Tiempo de espera entre scroll que permite que se rendericen nuevos elementos");
        }

        String message = "No se encontró el elemento tras scroll completo: " + targetLocator;
        LogUtil.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Realiza scroll en una tabla virtual con `cdk-virtual-scroll` hasta que el elemento esté visible en el DOM.
     *
     * @param targetElement Elemento Web esperado (generalmente un <td>, <tr> o <span>).
     * @throws RuntimeException si el elemento no se encuentra tras scroll completo.
     */
    public void scrollUntilElementIsVisible(WebElement targetElement) {
        LogUtil.info("Iniciando scroll para buscar elemento: " + targetElement);

        By scrollableContainerLocator = By.cssSelector(".cdk-virtual-scrollable");
        WebElement scrollable = findVisibleElement(scrollableContainerLocator);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        int scrollStep = 50;
        int sleepMillis = 300;
        int maxScrolls = 50;

        for (int i = 0; i < maxScrolls; i++) {
            try {
                if (targetElement.isDisplayed()) {
                    js.executeScript("arguments[0].scrollIntoView({block: 'center'});", targetElement);
                    waitForVisibilityByElement(targetElement);
                    LogUtil.info("Elemento visible tras scroll: " + targetElement);
                    return;
                }
            } catch (StaleElementReferenceException e) {
                LogUtil.warn("Elemento no disponible en este ciclo, intentando nuevamente...");
            }

            js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];",
                    scrollable, scrollStep);
            sleepMillis(sleepMillis, "Tiempo de espera entre scroll que permite que se rendericen nuevos elementos");
        }

        String message = "No se encontró el elemento tras scroll completo: " + targetElement;
        LogUtil.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Espera explícitamente a que el elemento ubicado por el localizador sea clickeable,
     * y lo retorna como un {@link WebElement}.
     *
     * <p>Este metodo es útil cuando aún no tienes el {@link WebElement} y deseas aplicar
     * una espera hasta que esté disponible para hacer clic.</p>
     *
     * @param locator Localizador del elemento a esperar.
     * @return WebElement clickeable.
     * @throws TimeoutException si el elemento no se vuelve clickeable en el tiempo configurado.
     * @throws RuntimeException si ocurre otro error inesperado durante la espera.
     */
    public WebElement waitForElementToBeClickable(By locator) {
        try {
            LogUtil.info("Esperando que el elemento sea clickeable: " + locator.toString());
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            LogUtil.info("El elemento es clickeable: " + locator.toString());
            return element;
        } catch (TimeoutException e) {
            LogUtil.error("Tiempo agotado esperando a que el elemento sea clickeable: " + locator.toString(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error("Error inesperado al esperar clickeabilidad del elemento: " + locator.toString(), e);
            throw new RuntimeException("Error al esperar clickeabilidad para el elemento: " + locator.toString(), e);
        }
    }

    /**
     * Espera explícitamente a que el elemento proporcionado sea clickeable
     * y lo retorna como un {@link WebElement}.
     *
     * <p>Este metodo es útil cuando ya tienes el {@link WebElement} localizado
     * y deseas asegurarte de que está disponible para interactuar con clic.</p>
     *
     * @param element El WebElement objetivo.
     * @return El mismo WebElement una vez que se vuelve clickeable.
     * @throws TimeoutException si el elemento no se vuelve clickeable en el tiempo configurado.
     * @throws RuntimeException si ocurre un error inesperado durante la espera.
     */
    public WebElement waitForElementToBeClickable(WebElement element) {
        try {
            LogUtil.info("Esperando que el elemento sea clickeable: " + element.toString());
            WebElement clickable = wait.until(ExpectedConditions.elementToBeClickable(element));
            LogUtil.info("El elemento es clickeable: " + element.toString());
            return clickable;
        } catch (TimeoutException e) {
            LogUtil.error("Tiempo agotado esperando a que el elemento sea clickeable: " + element.toString(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error("Error inesperado al esperar clickeabilidad del elemento: " + element.toString(), e);
            throw new RuntimeException("Error al esperar clickeabilidad para el elemento: " + element.toString(), e);
        }
    }

    /**
     * Realiza scroll horizontal hacia la derecha dentro del contenedor con clase {@code .cdk-virtual-scrollable}
     * hasta que el elemento objetivo sea visible.
     * <p>
     * Este metodo es útil para tablas u otros elementos que usan scroll virtual horizontal.
     * Realiza desplazamientos hacia la derecha en pasos pequeños hasta que el {@code targetElement}
     * esté visible y centrado horizontalmente.
     * </p>
     *
     * @param targetElement el elemento que se desea hacer visible mediante scroll horizontal.
     * @throws RuntimeException si el elemento no se encuentra visible después de agotar los intentos de scroll.
     */
    public void scrollRightUntilElementIsVisible(WebElement targetElement) {
        LogUtil.info("Iniciando scroll horizontal para buscar elemento: " + targetElement);

        // Contenedor real con scroll horizontal, no el <cdk-virtual-scroll-viewport>
        By scrollableContainerLocator = By.cssSelector(".cdk-virtual-scrollable");
        WebElement scrollable = findVisibleElement(scrollableContainerLocator);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        int scrollStep = 100;
        int sleepMillis = 300;
        int maxScrolls = 50;

        for (int i = 0; i < maxScrolls; i++) {
            if (targetElement.isDisplayed()) {
                js.executeScript("arguments[0].scrollIntoView({inline: 'center'});", targetElement);
                waitForVisibilityByElement(targetElement);
                LogUtil.info("Elemento visible tras scroll horizontal: " + targetElement);
                return;
            }

            // Ejecutar scroll hacia la derecha
            js.executeScript("arguments[0].scrollLeft = arguments[0].scrollLeft + arguments[1];",
                    scrollable, scrollStep);

            sleepMillis(sleepMillis, "Tiempo de espera entre scrolls horizontales");
        }

        String message = "No se encontró el elemento tras scroll horizontal completo: " + targetElement;
        LogUtil.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Espera a que un elemento esté presente y visible dentro de un contexto dado.
     *
     * @param context       el elemento padre desde el cual buscar.
     * @param locator       el localizador del elemento buscado.
     * @param timeoutInSec  el tiempo máximo de espera en segundos.
     * @return el WebElement visible encontrado.
     * @throws TimeoutException si no se encuentra el elemento visible en el tiempo especificado.
     */
    public WebElement waitForElement(WebElement context, By locator, int timeoutInSec) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSec));
        return wait.until(driver -> {
            try {
                WebElement element = context.findElement(locator);
                return element.isDisplayed() ? element : null;
            } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                return null;
            }
        });
    }

    /**
     * Espera a que la tabla de previsiones cargue completamente verificando la aparición
     * y desaparición del mensaje "Cargando datos...".
     *
     * <p>Este metodo ejecuta los siguientes pasos:</p>
     * <ol>
     *   <li>Define un tiempo máximo para esperar si aparece el mensaje "Cargando datos...".</li>
     *   <li>Usa {@link #isElementVisible(By, int, int)} para detectar visibilidad del mensaje durante 5 segundos con intervalos de 500 ms.</li>
     *   <li>Si el mensaje aparece, llama a {@link #waitForInvisibility(By, String, int, int)} con timeout de 20 segundos y sondeo de 500 ms.</li>
     *   <li>Si el mensaje no aparece, continúa sin espera adicional.</li>
     * </ol>
     *
     * <p>Esto garantiza que no se interactúe con la tabla hasta que haya finalizado completamente su carga.</p>
     */
    public void waitForTableToLoadCompletely() {
        // Localizador del mensaje de carga
        By messageLoading = By.xpath("//span[normalize-space(text())='Cargando datos...']");

        LogUtil.info("Esperando mensaje de carga 'Cargando datos...' si aparece.");

        // Verifica si el mensaje está visible
        if (isElementVisible(messageLoading, 1000, 100)) {
            LogUtil.info("Mensaje 'Cargando datos...' visible. Esperando que desaparezca...");
            // Espera que desaparezca
            waitForInvisibility(messageLoading, "Mensaje Cargando datos...", 10000, 100);
        } else {
            LogUtil.info("El mensaje 'Cargando datos...' no apareció. Continuando sin espera adicional.");
        }
    }

    /**
     * Espera que el mensaje "Cambiando la agregación temporal..." desaparezca si aparece.
     * Este mensaje se muestra al modificar la agrupación temporal.
     */
    public void waitForTemporalGroupingChangeToComplete() {
        // Localizador del mensaje de cambio de agregación temporal
        By messageGrouping = By.xpath("//span[normalize-space(text())='Cambiando la agregación temporal...']");

        LogUtil.info("Esperando mensaje de cambio 'Cambiando la agregación temporal...' si aparece.");

        // Verifica si el mensaje está visible (espera corta)
        if (isElementVisible(messageGrouping, 1000, 100)) {
            LogUtil.info("Mensaje 'Cambiando la agregación temporal...' visible. Esperando que desaparezca...");
            waitForInvisibility(messageGrouping, "Mensaje Cambiando la agregación temporal", 10000, 500);
        } else {
            LogUtil.info("El mensaje 'Cambiando la agregación temporal...' no apareció. Continuando sin espera adicional.");
        }
    }

    /**
     * Verifica si un elemento hijo dentro de un WebElement padre se vuelve visible dentro del tiempo de espera.
     *
     * @param parent  El WebElement padre donde buscar.
     * @param locator El localizador relativo dentro del elemento padre.
     * @return {@code true} si el elemento hijo se vuelve visible, {@code false} en caso contrario.
     */
    public boolean isElementVisibleWithinElement(WebElement parent, By locator) {
        try {
            WebDriverWait nestedWait = new WebDriverWait(driver, Duration.ofSeconds(2)); // espera corta
            nestedWait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(parent, locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Espera explícitamente a que el elemento proporcionado esté visible en el DOM.
     *
     * <p>Este metodo es útil cuando ya tienes una referencia a un {@link WebElement}
     * y deseas asegurarte de que está visible (es decir, renderizado, con tamaño y estilo visible)
     * antes de interactuar con él. Utiliza {@link ExpectedConditions#visibilityOf(WebElement)}.</p>
     *
     * @param element El {@link WebElement} que debe estar visible.
     * @throws TimeoutException si el elemento no se vuelve visible dentro del tiempo configurado.
     * @throws RuntimeException si ocurre otro error inesperado durante la espera.
     */
    public void waitForElementToBeVisible(WebElement element) {
        try {
            LogUtil.info("Esperando visibilidad del elemento: " + element.toString());
            wait.until(ExpectedConditions.visibilityOf(element));
            LogUtil.info("El elemento es visible: " + element.toString());
        } catch (TimeoutException e) {
            LogUtil.error("Tiempo agotado esperando visibilidad del elemento: " + element.toString(), e);
            throw e;
        } catch (Exception e) {
            LogUtil.error("Error inesperado al esperar visibilidad del elemento: " + element.toString(), e);
            throw new RuntimeException("Error al esperar visibilidad del elemento: " + element.toString(), e);
        }
    }

    /**
     * Realiza scroll vertical dentro de un contenedor virtual (por ejemplo, una tabla con `cdk-virtual-scroll`)
     * hasta que el elemento hijo especificado por el `targetLocator` esté visible.
     *
     * <p>Este metodo es útil para componentes con scroll interno, donde se desea encontrar dinámicamente
     * un valor que aún no se ha renderizado.</p>
     *
     * @param parent        Contenedor scrollable (por ejemplo, tabla).
     * @param targetLocator Localizador del elemento objetivo dentro del contenedor.
     * @return WebElement visible encontrado tras scroll.
     * @throws RuntimeException si el elemento no se encuentra después del scroll completo.
     */
    public WebElement scrollUntilElementIsVisible(WebElement parent, By targetLocator) {
        LogUtil.info("Iniciando scroll dentro del contenedor para buscar elemento: " + targetLocator);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        int scrollStep = 50;
        int sleepMillis = 300;
        int maxScrolls = 50;

        for (int i = 0; i < maxScrolls; i++) {
            // Buscar dentro del contenedor actual
            if (isElementVisibleWithinElement(parent, targetLocator)) {
                WebElement element = waitForElement(parent, targetLocator, 3);
                js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                waitForVisibilityByElement(element);
                LogUtil.info("Elemento encontrado y visible tras scroll: " + targetLocator);
                return element;
            }

            // Ejecutar scroll vertical
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[1];",
                    parent, scrollStep);
            sleepMillis(sleepMillis, "Esperando renderizado de nuevos elementos tras scroll");
        }

        String message = "No se encontró el elemento tras scroll completo dentro del contenedor: " + targetLocator;
        LogUtil.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Espera a que el elemento ubicado por el {@link By} deje de poseer una clase CSS específica.
     *
     * <p>La condición se considera cumplida cuando:</p>
     * <ul>
     *   <li>El elemento sigue en el DOM pero su atributo <code>class</code> ya no contiene la clase indicada, o</li>
     *   <li>El elemento desaparece del DOM (se asume que ya no posee la clase).</li>
     * </ul>
     *
     * @param locator        Localizador del elemento objetivo.
     * @param className      Nombre de la clase CSS a esperar que desaparezca.
     * @param timeoutMillis  Tiempo máximo de espera en milisegundos.
     * @param pollingMillis  Intervalo de sondeo en milisegundos entre reintentos.
     * @throws RuntimeException si la clase no desaparece dentro del tiempo especificado o ante errores inesperados.
     */
    public void waitForClassToDisappear(By locator, String className, int timeoutMillis, int pollingMillis) {
        if (className == null || className.trim().isEmpty()) {
            throw new IllegalArgumentException("className no puede ser nulo ni vacío.");
        }

        LogUtil.info("Esperando que el elemento " + locator + " deje de tener la clase '" + className +
                "'. Timeout: " + timeoutMillis + " ms | Polling: " + pollingMillis + " ms");

        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofMillis(timeoutMillis));
        customWait.pollingEvery(Duration.ofMillis(pollingMillis));
        customWait.ignoring(StaleElementReferenceException.class);

        long start = System.currentTimeMillis();
        try {
            customWait.until(d -> {
                try {
                    WebElement el = d.findElement(locator);
                    String classes = el.getAttribute("class");
                    // Coincidencia segura por token de clase (evita falsos positivos por substrings)
                    boolean hasClass = classes != null && (" " + classes + " ").contains(" " + className + " ");
                    return !hasClass; // condición: YA no tiene la clase
                } catch (NoSuchElementException e) {
                    // Si el elemento ya no está en el DOM, consideramos que la clase "desapareció"
                    return true;
                } catch (StaleElementReferenceException e) {
                    // DOM se actualizó; reintentar en el siguiente polling
                    return false;
                }
            });

            long elapsed = System.currentTimeMillis() - start;
            LogUtil.info("Condición cumplida en " + elapsed + " ms: el elemento " + locator +
                    " ya no tiene la clase '" + className + "'.");
        } catch (TimeoutException e) {
            long elapsed = System.currentTimeMillis() - start;
            LogUtil.error("Timeout tras " + elapsed + " ms: el elemento " + locator +
                    " aún posee la clase '" + className + "'.", e);
            throw new RuntimeException("El elemento " + locator +
                    " no perdió la clase '" + className + "' dentro del tiempo límite.", e);
        } catch (Exception e) {
            LogUtil.error("Error inesperado esperando que el elemento " + locator +
                    " pierda la clase '" + className + "'.", e);
            throw new RuntimeException("Error inesperado al esperar la desaparición de la clase '" +
                    className + "' en el elemento " + locator + ".", e);
        }
    }

}