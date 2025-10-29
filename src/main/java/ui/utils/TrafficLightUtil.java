package ui.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.manager.PageManager;

import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Utilidad especializada en la verificación de los semáforos de estado dentro de tablas de la UI.
 * <p>
 * Se encarga de:
 * <ul>
 *   <li>Esperar la desaparición de indicadores de progreso ("in-progress").</li>
 *   <li>Detectar el color actual del semáforo mediante estilos CSS.</li>
 *   <li>Mapear valores RGBA a nombres de color legibles.</li>
 *   <li>Validar secuencias esperadas de colores durante diferentes fases de un proceso.</li>
 *   <li>Tomar capturas de pantalla en momentos clave.</li>
 * </ul>
 * </p>
 */
public class TrafficLightUtil {

    private final WebDriver driver;
    private final WaitUtil waitUtil;
    private final TableUtil tableUtil;
    private final ScreenshotUtil screenshotUtil;

    // 🔹 Constantes reutilizables
    private static final long TIMEOUT_MILLIS = 300_000;   // Tiempo máximo de espera
    private static final long POLLING_INTERVAL = 500;     // Intervalo entre intentos
    private static final int MAX_ATTEMPTS = 600;          // Máx. intentos de espera por seguridad

    // 🔹 Selectores reutilizables
    private static final By IN_PROGRESS_SELECTOR = By.cssSelector("div.status-indicator:is(.in-progress, .loading)");
    private static final By INDICATOR_SELECTOR = By.xpath(".//div[contains(@class, 'status-indicator') and not(contains(@class, 'container'))]");

    /**
     * Constructor que inicializa la utilidad con dependencias compartidas del {@link PageManager}.
     *
     * @param pageManager instancia de {@link PageManager} que provee WebDriver y utilidades auxiliares.
     */
    public TrafficLightUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.waitUtil = pageManager.getWaitUtil();
        this.tableUtil = pageManager.getTableUtil();
        this.screenshotUtil = pageManager.getScreenshotUtil();
    }

    /**
     * Espera hasta que desaparezca el indicador de progreso en la columna <b>"Estado"</b>
     * de la tabla especificada y retorna el nombre del color actual del semáforo.
     *
     * @param tableTitle título de la tabla donde se buscará la celda de estado.
     * @return nombre del color del semáforo detectado (ej: {@code "brightGreen"}, {@code "red"}).
     * @throws IllegalStateException si no desaparece el estado "in-progress" a tiempo o el color no se reconoce.
     */
    public String waitForTrafficLightColorName(String tableTitle) {
        long startTime = System.currentTimeMillis();

        try {
            LogUtil.info("Esperando a que finalice el estado 'in-progress' en la tabla antes de verificar el color del semáforo.");

            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                WebElement firstCell = tableUtil.getFirstCellElementByHeaderName("Estado", tableTitle);

                List<WebElement> progressElements = firstCell.findElements(IN_PROGRESS_SELECTOR);
                if (progressElements.isEmpty()) {
                    long duration = System.currentTimeMillis() - startTime;
                    LogUtil.info("Estado 'in-progress' finalizado en la tabla. Tiempo transcurrido: " + duration + " ms. Iniciando verificación del color.");
                    break;
                }

                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= TIMEOUT_MILLIS) {
                    LogUtil.error("Timeout esperando que termine estado 'in-progress' después de " + elapsed + " ms.");
                    throw new IllegalStateException("Tiempo de espera superado al esperar que termine estado 'in-progress'.");
                }

                LogUtil.info("Intento #" + attempt + ": el estado 'in-progress' persiste en la tabla. Reintentando en " + POLLING_INTERVAL + " ms...");
                waitUtil.sleepMillis(POLLING_INTERVAL, "Intervalo entre intentos");
            }

            WebElement firstCell = tableUtil.getFirstCellElementByHeaderName("Estado", tableTitle);
            WebElement indicatorElement = firstCell.findElement(INDICATOR_SELECTOR);

            LogUtil.info("HTML del div que contiene el color: " + indicatorElement.getAttribute("outerHTML"));
            String color = indicatorElement.getCssValue("background-color");
            LogUtil.info("Color detectado en CSS (background-color): " + color);

            return mapColorToName(color);

        } catch (Exception e) {
            LogUtil.error("Error al verificar el color del semáforo: " + e.getMessage(), e);
            throw new IllegalStateException("Error durante la verificación del semáforo: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea el valor RGBA de un color al nombre esperado del semáforo.
     *
     * @param color valor CSS en formato RGBA.
     * @return nombre del color.
     * @throws IllegalStateException si el color no es reconocido.
     */
    private String mapColorToName(String color) {
        switch (color) {
            case "rgba(166, 224, 166, 1)":
                LogUtil.info("Color reconocido: lightGreen");
                LogUtil.warn("⚠ Color incorrecto detectado, debe ser verde brillante");
                return "lightGreen";
            case "rgba(40, 199, 111, 1)":
                LogUtil.info("Color reconocido: brightGreen");
                return "brightGreen";
            case "rgba(23, 192, 242, 1)":
                LogUtil.info("Color reconocido: lightBlue");
                return "lightBlue";
            case "rgba(249, 80, 101, 1)":
                LogUtil.info("Color reconocido: red");
                LogUtil.warn("⚠ Color incorrecto detectado, debe ser rojo intenso");
                return "red";
            case "rgba(220, 38, 38, 1)":
                LogUtil.info("Color reconocido: deepRed");
                return "deepRed";
            case "rgba(244, 236, 93, 1)":
                LogUtil.info("Color reconocido: yellow");
                LogUtil.warn("⚠ Color incorrecto detectado, debe ser amarillo dorado intenso");
                return "yellow";
            case "rgba(251, 191, 36, 1)":
                LogUtil.info("Color reconocido: intenseGoldenYellow");
                return "intenseGoldenYellow";
            case "rgba(255, 230, 164, 1)":
                LogUtil.info("Color reconocido: veryLightAndSoftYellow");
                return "veryLightAndSoftYellow";
            case "rgba(255, 217, 102, 1)":
                LogUtil.info("Color reconocido: softGoldenYellow");
                LogUtil.warn("⚠ Color incorrecto detectado, debe ser amarillo dorado intenso");
                return "softGoldenYellow";
            case "rgba(241, 194, 50, 1)":
                LogUtil.info("Color reconocido: deepGoldenYellow");
                LogUtil.warn("⚠ Color incorrecto detectado, debe ser amarillo dorado intenso");
                return "deepGoldenYellow";
            case "rgba(140, 217, 238, 1)":
                LogUtil.info("Color reconocido: lightSkyBlue");
                return "lightSkyBlue";
            case "rgba(203, 203, 203, 1)":
                LogUtil.info("Color reconocido: lightGray");
                return "lightGray";
            default:
                LogUtil.error("Color no reconocido: " + color);
                throw new IllegalStateException("Color del semáforo no reconocido: " + color);
        }
    }

    /**
     * Verifica la secuencia de colores esperada en la tabla indicada, usando valores
     * por defecto de timeout (5 minutos por fase) y polling (300 ms).
     *
     * <p>Secuencia validada:
     * <ol>
     *   <li>Proyección de stock → {@code intenseGoldenYellow}</li>
     *   <li>Informe de salud de inventario → {@code veryLightAndSoftYellow}</li>
     *   <li>Finalizado → {@code brightGreen}</li>
     * </ol>
     * </p>
     *
     * @param tableTitle título de la tabla a validar.
     */
    public void checkTrafficLightSequence(String tableTitle) {
        checkTrafficLightSequence(tableTitle, Duration.ofMinutes(5), 300);
    }

    /**
     * Verifica la secuencia de colores esperada en la tabla indicada, con configuración personalizada.
     *
     * @param tableTitle      título de la tabla a validar.
     * @param perPhaseTimeout tiempo máximo de espera para cada fase.
     * @param pollingMillis   intervalo en milisegundos entre verificaciones sucesivas.
     */
    public void checkTrafficLightSequence(String tableTitle, Duration perPhaseTimeout, long pollingMillis) {
        waitUntilTrafficLightColorIs(tableTitle, "intenseGoldenYellow", perPhaseTimeout, pollingMillis, "Calculando proyeccion de stock");
        screenshotUtil.capture("Calculando proyeccion de stock");

        waitUntilTrafficLightColorIs(tableTitle, "veryLightAndSoftYellow", perPhaseTimeout, pollingMillis, "Calculando informe de salud de inventario");
        screenshotUtil.capture("Calculando informe de salud de inventario");

        waitUntilTrafficLightColorIs(tableTitle, "brightGreen", perPhaseTimeout, pollingMillis, "Proceso finalizado");
    }

    /**
     * Espera de manera periódica hasta que el semáforo muestre un color específico.
     * <p>
     * Si el color esperado no aparece dentro del tiempo límite, lanza un {@link AssertionError}.
     * </p>
     *
     * @param tableTitle        título de la tabla a inspeccionar.
     * @param expectedColorName nombre del color esperado (ej: {@code "brightGreen"}).
     * @param timeout           duración máxima de espera.
     * @param pollingMillis     intervalo en milisegundos entre verificaciones.
     * @param phaseDesc         descripción de la fase actual (para logs y reportes).
     * @throws AssertionError si el color esperado no aparece dentro del tiempo establecido.
     */
    private void waitUntilTrafficLightColorIs(String tableTitle, String expectedColorName, Duration timeout, long pollingMillis, String phaseDesc) {
        final long deadline = System.nanoTime() + timeout.toNanos();
        String lastSeen = null;

        while (System.nanoTime() < deadline) {
            String color = waitForTrafficLightColorName(tableTitle);
            lastSeen = color;
            if (expectedColorName.equals(color)) {
                ValidationUtil.assertEquals(color, expectedColorName, "El semáforo debe estar en " + expectedColorName + " durante la fase: " + phaseDesc);
                return;
            }
            waitUtil.sleepMillis(pollingMillis, "Esperando cambio a '" + expectedColorName + "' (" + phaseDesc + ")");
        }
        throw new AssertionError("Timeout esperando color '" + expectedColorName + "' (" + phaseDesc + "). Último visto: " + lastSeen);
    }

    /**
     * Verifica que el semáforo de la tabla indicada finalice en color verde claro ({@code "lightGreen"}).
     *
     * <p>Flujo:
     * <ul>
     *   <li>Espera la desaparición del estado "in-progress".</li>
     *   <li>Obtiene el color actual del semáforo.</li>
     *   <li>Valida que sea exactamente {@code "lightGreen"}.</li>
     * </ul>
     * </p>
     *
     * @param tableTitle     título de la tabla a inspeccionar.
     * @param validationUtil utilidad de validación para comparar valores.
     * @throws AssertionError si el semáforo no es de color verde claro.
     */
    public void checkGreenLight(String tableTitle) {
        String color = waitForTrafficLightColorName(tableTitle);
        ValidationUtil.assertEquals(color, "lightGreen", "El semáforo debe estar en verde brillante");
    }

    /**
     * Espera hasta que desaparezca el indicador de progreso en la columna <b>"Estado"</b>
     * de la tabla identificada por su atributo {@code tabindex} y retorna el nombre del color actual del semáforo.
     *
     * @param tableTabIndex valor del atributo tabindex de la tabla objetivo.
     * @return nombre del color del semáforo detectado (ej: {@code "brightGreen"}, {@code "red"}).
     * @throws IllegalStateException si no desaparece el estado "in-progress" a tiempo, la estructura de la tabla es inválida,
     *                               o el color no se reconoce.
     */
    public String waitForTrafficLightColorNameByTabIndex(int tableTabIndex) {
        long startTime = System.currentTimeMillis();
        String tableSelector = String.format("table[tabindex='%d']", tableTabIndex);

        try {
            LogUtil.info("Buscando tabla por tabindex: " + tableTabIndex);
            WebElement table = driver.findElement(By.cssSelector(tableSelector));

            // Localizar índice de la columna "Estado"
            int estadoColIndex = getHeaderIndexByName(table, "Estado");
            if (estadoColIndex < 0) {
                LogUtil.error("No se encontró la columna 'Estado' en la tabla con tabindex=" + tableTabIndex);
                throw new IllegalStateException("Columna 'Estado' no encontrada.");
            }

            // Obtener la primera celda de datos bajo la columna "Estado"
            WebElement firstCell = getFirstDataCellByColumnIndex(table, estadoColIndex);

            LogUtil.info("Esperando fin de 'in-progress' en la celda de la columna 'Estado' (tabindex=" + tableTabIndex + ").");
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                List<WebElement> progressElements = firstCell.findElements(IN_PROGRESS_SELECTOR);
                if (progressElements.isEmpty()) {
                    long duration = System.currentTimeMillis() - startTime;
                    LogUtil.info("Estado 'in-progress' finalizado. Tiempo: " + duration + " ms. Iniciando verificación del color.");
                    break;
                }

                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= TIMEOUT_MILLIS) {
                    LogUtil.error("Timeout esperando fin de 'in-progress' tras " + elapsed + " ms (tabindex=" + tableTabIndex + ").");
                    throw new IllegalStateException("Tiempo de espera superado al esperar que termine 'in-progress'.");
                }

                LogUtil.info("Intento #" + attempt + ": persiste 'in-progress'. Reintentando en " + POLLING_INTERVAL + " ms...");
                waitUtil.sleepMillis(POLLING_INTERVAL, "Intervalo entre intentos");
            }

            // Re-obtener la celda por si el DOM cambió durante la espera (mayor robustez)
            table = driver.findElement(By.cssSelector(tableSelector));
            firstCell = getFirstDataCellByColumnIndex(table, estadoColIndex);

            WebElement indicatorElement = firstCell.findElement(INDICATOR_SELECTOR);
            LogUtil.info("HTML del div que contiene el color: " + indicatorElement.getAttribute("outerHTML"));

            String color = indicatorElement.getCssValue("background-color"); //aqui detecta el color
            LogUtil.info("Color detectado en CSS (background-color): " + color);

            return mapColorToName(color);

        } catch (Exception e) {
            LogUtil.error("Error al verificar el color del semáforo por tabindex=" + tableTabIndex + ": " + e.getMessage(), e);
            throw new IllegalStateException("Error durante la verificación del semáforo: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna el índice (0-based) de la columna cuyo encabezado coincide con el nombre dado.
     * Busca en thead > tr > th y compara texto normalizado.
     */
    private int getHeaderIndexByName(WebElement table, String headerName) {
        List<WebElement> headers = table.findElements(By.cssSelector("thead tr th"));
        for (int i = 0; i < headers.size(); i++) {
            String text = headers.get(i).getText();
            if (text != null && text.trim().equalsIgnoreCase(headerName.trim())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Obtiene la primera celda de datos (tbody > tr:first) para el índice de columna indicado.
     * Valida que existan filas y que el índice sea válido.
     */
    private WebElement getFirstDataCellByColumnIndex(WebElement table, int colIndex) {
        WebElement firstRow = table.findElement(By.cssSelector("tbody tr"));
        List<WebElement> cells = firstRow.findElements(By.cssSelector("td"));
        if (cells.isEmpty()) {
            throw new IllegalStateException("La tabla no contiene filas de datos en <tbody>.");
        }
        if (colIndex < 0 || colIndex >= cells.size()) {
            throw new IllegalStateException("Índice de columna fuera de rango: " + colIndex + " (total celdas=" + cells.size() + ")");
        }
        return cells.get(colIndex);
    }

}