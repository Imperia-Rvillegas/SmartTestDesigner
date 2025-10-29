package hooks;

import config.DriverFactory;
import config.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import ui.manager.PageManager;
import ui.utils.LogUtil;
import ui.utils.ScreenshotUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Hooks de Cucumber para pruebas de interfaz {@code @ui}.
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Inicializar y cerrar el {@link WebDriver} a través de {@link DriverFactory}.</li>
 *   <li>Propagar el {@link Scenario} al {@link ScenarioContext} para uso en steps y pages.</li>
 *   <li>Inicializar el {@link PageManager} y utilidades asociadas (p. ej., {@link ScreenshotUtil}).</li>
 *   <li>Configurar carpetas por escenario para screenshots y descargas.</li>
 *   <li>Intentar forzar la carpeta de descargas del navegador mediante CDP (Chromium).</li>
 *   <li>Registrar inicio y fin de cada escenario, y capturar evidencia en fallos.</li>
 * </ul>
 *
 * <p><strong>Ámbito</strong>: cada ejecución de escenario crea su propio subdirectorio
 * bajo {@code target/screenshots/} y {@code target/downloads/} con un timestamp,
 * lo que facilita la trazabilidad de evidencias y archivos descargados.</p>
 *
 * <p><strong>Nota</strong>: estos hooks asumen un entorno Chromium (Chrome/Edge) para
 * poder forzar la ruta de descargas vía CDP. En otros navegadores, la ruta de descargas
 * no se modifica desde código y deberá configurarse externamente.</p>
 */
public class Hooks {

    /** Utilidad para gestionar capturas de pantalla asociadas al escenario. */
    private ScreenshotUtil screenshotUtil;

    /**
     * Instancia compartida del {@link WebDriver} para el escenario actual.
     * <p>Se crea en {@link #setUp(Scenario)} y se cierra en {@link #tearDown(Scenario)}.</p>
     */
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    /**
     * Instancia compartida de {@link PageManager} para acceder a objetos de página durante el escenario.
     * <p>Se inicializa en {@link #setUp(Scenario)} con el driver y el {@link ScenarioContext}.</p>
     */
    private static final ThreadLocal<PageManager> PAGE_MANAGER = new ThreadLocal<>();

    /**
     * Ruta del directorio donde se almacenarán las capturas de pantalla del escenario actual.
     * <p>Formato: {@code target/screenshots/<nombre_escenario>_<timestamp>/}</p>
     */
    private static final ThreadLocal<String> SCENARIO_FOLDER_PATH = new ThreadLocal<>();

    /**
     * Ruta del directorio donde se almacenarán las descargas del escenario actual.
     * <p>Formato: {@code target/downloads/<nombre_escenario>_<timestamp>/}</p>
     * <p>Cuando se ejecuta en Chromium, se intenta forzar esta ruta en el navegador vía CDP.</p>
     */
    private static final ThreadLocal<String> DOWNLOADS_FOLDER_PATH = new ThreadLocal<>();

    /**
     * Contexto del escenario para compartir datos entre hooks/steps/pages.
     * <p>El {@link Scenario} activo se inyecta en este contexto durante {@link #setUp(Scenario)}.</p>
     */
    private final ScenarioContext scenarioContext;

    /**
     * Crea una instancia de hooks con el contexto de escenario compartido.
     *
     * @param scenarioContext contenedor de datos del escenario (inyectado por Cucumber/DI).
     */
    public Hooks(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    /**
     * Hook de arranque que se ejecuta antes de cada escenario etiquetado con {@code @ui}.
     *
     * <p>Pasos principales:</p>
     * <ol>
     *   <li>Obtiene el {@link WebDriver} desde {@link DriverFactory#getDriver()}.</li>
     *   <li>Guarda el {@link Scenario} en {@link ScenarioContext}.</li>
     *   <li>Inicializa {@link PageManager} y {@link ScreenshotUtil}.</li>
     *   <li>Crea carpetas específicas del escenario para screenshots y descargas (con timestamp).</li>
     *   <li>Intenta configurar la carpeta de descargas del navegador con {@link #forceBrowserDownloadDir(WebDriver, String)}.</li>
     *   <li>Registra el inicio del escenario en el log.</li>
     * </ol>
     *
     * @param scenario escenario en ejecución provisto por Cucumber.
     */
    @Before(value = "@ui", order = 0)
    public void setUp(Scenario scenario) {
        WebDriver webDriver = DriverFactory.getDriver();
        DRIVER.set(webDriver);
        scenarioContext.setScenario(scenario);
        PageManager manager = new PageManager(webDriver, scenarioContext);
        PAGE_MANAGER.set(manager);

        screenshotUtil = manager.getScreenshotUtil();

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String folderName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp;

        // Carpeta de screenshots
        String scenarioFolderPath = "target/screenshots/" + folderName + "/";
        SCENARIO_FOLDER_PATH.set(scenarioFolderPath);
        new File(scenarioFolderPath).mkdirs();

        // Carpeta de descargas para el escenario
        String downloadsFolderPath = "target/downloads/" + folderName + "/";
        DOWNLOADS_FOLDER_PATH.set(downloadsFolderPath);
        new File(downloadsFolderPath).mkdirs();
        try {
            forceBrowserDownloadDir(webDriver, downloadsFolderPath);
            LogUtil.info("Carpeta de descargas configurada: " + downloadsFolderPath);
        } catch (Exception e) {
            LogUtil.error("No se pudo configurar la carpeta de descargas vía CDP: " + e.getMessage());
        }

        LogUtil.start("Escenario: " + scenario.getName());
    }

    /**
     * Hook de finalización para escenarios etiquetados con {@code @ui}.
     *
     * <p>Si el escenario falla, captura una screenshot con contexto y la guarda en
     * {@link #scenarioFolderPath}. Tras ello, registra el fin del escenario y
     * cierra el {@link WebDriver} vía {@link DriverFactory#quitDriver()}.</p>
     *
     * @param scenario escenario que acaba de finalizar.
     */
    @After("@ui")
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && screenshotUtil != null) {
            LogUtil.error("Escenario fallido: " + scenario.getName());
            screenshotUtil.captureWithScenario(scenario, "Evidencia de error");
        }

        LogUtil.end("Escenario: " + scenario.getName());
        DriverFactory.quitDriver();
        ScenarioContext.DownloadContext.set(null);
        PAGE_MANAGER.remove();
        SCENARIO_FOLDER_PATH.remove();
        DOWNLOADS_FOLDER_PATH.remove();
        DRIVER.remove();
    }

    /**
     * Devuelve el {@link WebDriver} actual asociado al escenario en curso.
     *
     * @return instancia de {@link WebDriver}, o {@code null} si aún no fue inicializado.
     */
    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    /**
     * Devuelve el {@link PageManager} actual para interactuar con objetos de página.
     *
     * @return instancia de {@link PageManager}, o {@code null} si aún no fue inicializada.
     */
    public static PageManager getPageManager() {
        return PAGE_MANAGER.get();
    }

    /**
     * Devuelve la ruta configurada para almacenar las capturas de pantalla del escenario actual.
     *
     * @return ruta absoluta o relativa de la carpeta de screenshots del escenario.
     */
    public static String getScenarioFolderPath() {
        return SCENARIO_FOLDER_PATH.get();
    }

    /**
     * Devuelve la ruta configurada para almacenar las descargas del escenario actual.
     *
     * @return ruta absoluta o relativa de la carpeta de descargas del escenario.
     */
    public static String getDownloadsFolderPath() {
        return DOWNLOADS_FOLDER_PATH.get();
    }

    /**
     * Configura en tiempo de ejecución la carpeta de descargas del navegador empleando
     * el protocolo CDP (Chrome DevTools Protocol).
     *
     * <p>Compatibilidad:</p>
     * <ul>
     *   <li><strong>Soportado</strong>: {@link ChromeDriver}, {@link EdgeDriver} (Chromium).</li>
     *   <li><strong>No soportado</strong>: otros drivers/no Chromium. En este caso, se registra un {@code warn} y no se modifica nada.</li>
     * </ul>
     *
     * <p><strong>Requisitos</strong>:
     * el navegador debe permitir el comando {@code Page.setDownloadBehavior} y
     * la ruta de destino debe existir y ser escribible.</p>
     *
     * @param driver      instancia del navegador en uso.
     * @param downloadDir ruta de la carpeta donde se desean almacenar las descargas.
     */
    private void forceBrowserDownloadDir(WebDriver driver, String downloadDir) {
        // Solo funciona con drivers Chromium (Chrome/Edge):
        Map<String, Object> params = new HashMap<>();
        params.put("behavior", "allow");
        params.put("downloadPath", Paths.get(downloadDir).toAbsolutePath().toString());

        if (driver instanceof ChromeDriver) {
            ((ChromeDriver) driver).executeCdpCommand("Page.setDownloadBehavior", params);
        } else if (driver instanceof EdgeDriver) {
            ((EdgeDriver) driver).executeCdpCommand("Page.setDownloadBehavior", params);
        } else {
            LogUtil.warn("El driver no es Chromium; no se puede forzar carpeta de descarga vía CDP.");
        }
    }
}
