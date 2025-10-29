package config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import ui.utils.LogUtil;

/**
 * Fábrica de instancias de WebDriver para automatizar navegadores.
 *
 * Permite seleccionar entre Chrome o Edge de forma dinámica
 * mediante la propiedad del sistema "browser".
 *
 * Se puede forzar el modo headless con la propiedad del sistema "headless=true".
 * Si se ejecuta en un entorno CI/CD (variable de entorno CI=true),
 * también se activa automáticamente el modo headless.
 *
 * Utiliza WebDriverManager para gestionar automáticamente los drivers.
 */
public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    // Resolución deseada en headless
    private static final int HEADLESS_WIDTH = 2560;
    private static final int HEADLESS_HEIGHT = 1440;

    /**
     * Obtiene (o crea si no existe) la instancia de {@link WebDriver} para el hilo actual.
     * <p>
     * La creación depende de las propiedades del sistema y del entorno:
     * </p>
     * <ul>
     *   <li><b>Navegador</b>: {@code -Dbrowser=<chrome|edge>} (por defecto {@code chrome}).</li>
     *   <li><b>Headless</b>: activo si {@code -Dheadless=true} <em>o</em> si la variable de entorno {@code CI=true}.</li>
     * </ul>
     *
     * <p><strong>Comportamiento</strong></p>
     * <ul>
     *   <li>Para Edge/Chrome configura opciones específicas:
     *     <ul>
     *       <li>En <em>headless</em>: {@code --headless=new}, {@code --no-sandbox}, {@code --disable-dev-shm-usage}, {@code --disable-gpu}, {@code --window-size=2048,1080}.</li>
     *       <li>En modo gráfico: {@code --start-maximized} + intento de {@code manage().window().maximize()}.</li>
     *     </ul>
     *   </li>
     *   <li>Usa {@code WebDriverManager} para resolver binarios del driver.</li>
     *   <li>Guarda la instancia en un {@code ThreadLocal} (vía {@code driver.set(newDriver)}), por lo que
     *       cada hilo obtiene su propio navegador.</li>
     * </ul>
     *
     * <p><strong>Ejemplos</strong></p>
     * <pre>
     * mvn test -Dbrowser=edge -Dheadless=true
     * CI=true mvn test                # fuerza headless en CI
     * </pre>
     *
     * <p><strong>Notas</strong></p>
     * <ul>
     *   <li>Si el SO/driver ignora {@code --start-maximized}, se intenta maximizar vía WebDriver; los fallos se registran pero no detienen la ejecución.</li>
     *   <li>Las excepciones de inicialización del navegador pueden propagarse en tiempo de ejecución.</li>
     * </ul>
     *
     * @return la instancia de {@link WebDriver} asociada al hilo; se crea si aún no existe.
     *
     * @see io.github.bonigarcia.wdm.WebDriverManager
     * @see org.openqa.selenium.chrome.ChromeOptions
     * @see org.openqa.selenium.edge.EdgeOptions
     */
    public static WebDriver getDriver() {
        if (driver.get() == null) {
            String browser = System.getProperty("browser", "chrome").toLowerCase();

            // Se activa headless si: -Dheadless=true o variable de entorno CI=true
            boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"))
                    || ("true".equalsIgnoreCase(System.getenv("CI")));

            WebDriver newDriver;

            switch (browser) {
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    EdgeOptions edgeOptions = new EdgeOptions();
                    edgeOptions.setAcceptInsecureCerts(true);
                    edgeOptions.addArguments(
                            "--ignore-certificate-errors",
                            "--allow-insecure-localhost"
                    );
                    if (isHeadless) {
                        edgeOptions.addArguments(
                                "--headless=new",
                                "--no-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu",
                                String.format("--window-size=%d,%d", HEADLESS_WIDTH, HEADLESS_HEIGHT)
                        );
                        LogUtil.info("Navegador: Edge | Modo: Headless | Resolución: " + HEADLESS_WIDTH + "x" + HEADLESS_HEIGHT);
                    } else {
                        edgeOptions.addArguments("--start-maximized");
                        LogUtil.info("Navegador: Edge | Modo: Gráfico (maximizado)");
                    }
                    newDriver = new EdgeDriver(edgeOptions);
                    break;

                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.setAcceptInsecureCerts(true);
                    chromeOptions.addArguments(
                            "--ignore-certificate-errors",
                            "--allow-insecure-localhost"
                    );
                    if (isHeadless) {
                        chromeOptions.addArguments(
                                "--headless=new",
                                "--no-sandbox",
                                "--disable-dev-shm-usage",
                                "--disable-gpu",
                                String.format("--window-size=%d,%d", HEADLESS_WIDTH, HEADLESS_HEIGHT)
                        );
                        LogUtil.info("Navegador: Chrome | Modo: Headless | Resolución: " + HEADLESS_WIDTH + "x" + HEADLESS_HEIGHT);
                    } else {
                        chromeOptions.addArguments("--start-maximized");
                        LogUtil.info("Navegador: Chrome | Modo: Gráfico (maximizado)");
                    }
                    newDriver = new ChromeDriver(chromeOptions);
                    break;
            }

            // Si estamos en headless, intentar forzar el tamaño de ventana también vía WebDriver
            if (isHeadless) {
                try {
                    newDriver.manage().window().setSize(new Dimension(HEADLESS_WIDTH, HEADLESS_HEIGHT));
                    LogUtil.info("Tamaño de ventana ajustado a " + HEADLESS_WIDTH + "x" + HEADLESS_HEIGHT + " en headless.");
                } catch (Exception e) {
                    LogUtil.warn("No se pudo ajustar el tamaño de ventana en headless vía WebDriver: " + e.getMessage());
                }
            } else {
                // Garantizar maximización en modo gráfico (algunos SO/drivers ignoran el flag y esto lo refuerza)
                try {
                    newDriver.manage().window().maximize();
                } catch (Exception e) {
                    LogUtil.warn("No se pudo maximizar la ventana vía WebDriver: " + e.getMessage());
                }
            }

            driver.set(newDriver);
        }
        return driver.get();
    }

    public static void setDriver(WebDriver webDriver) {
        driver.set(webDriver);
    }

    /**
     * Cierra y elimina la instancia activa de WebDriver.
     */
    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
