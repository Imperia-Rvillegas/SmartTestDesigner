package ui.pages;

import config.EnvironmentConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.LogUtil;
import ui.utils.ValidationUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Página que representa la gestión de ámbitos de importación.
 * <p>
 * Permite abrir un ámbito específico, seleccionar el origen de datos, mapear columnas
 * y ejecutar la carga de información, validando que el proceso finalice correctamente.
 * </p>
 */
public class ScopesPage extends BasePage {

    /**
     * Localizador para el desplegable de selección de origen.
     */
    private final By originDropdownLocator = By.xpath("//origin-compound-selector[@formcontrolname='IdBaseSource']//div[contains(@class,'p-dropdown-trigger')]");

    /**
     * Localizador para el enlace que reinicia la asignación de columnas.
     */
    private final By resetColumnMapping = By.xpath("//a[@role='button' and .//span[normalize-space()='Reiniciar asignación de columnas']]");

    /**
     * Localizador para el badge de confirmación de guardado.
     */
    private final By guardadoBadge = By.xpath("//span[normalize-space(.)='Guardado']");

    /**
     * Constructor de la página de ámbitos.
     *
     * @param driver      instancia de {@link WebDriver} para interactuar con la aplicación.
     * @param pageManager gestor de páginas que administra la navegación entre módulos.
     */
    public ScopesPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Abre el ámbito indicado haciendo clic sobre su nombre en el listado disponible.
     *
     * @param scopeName nombre visible del ámbito a abrir.
     */
    public void openScope(String scopeName) {
        LogUtil.info("Abriendo el ámbito de importación: " + scopeName);
        clickByLocator(By.xpath("//span[normalize-space(.)='" + scopeName + "']"), "Ambito: " + scopeName);
    }

    /**
     * Selecciona el origen previamente creado en el desplegable de selección.
     *
     * @param originName nombre del origen que será utilizado en el ámbito.
     */
    public void selectOrigin(String originName) {
        LogUtil.info("Seleccionando origen de carga: " + originName);
        clickByLocator(originDropdownLocator, "Origen de carga");
        clickDropdownOptionByText(originName);
    }

    /**
     * Mapea las columnas del ámbito utilizando los datos proporcionados.
     * <p>
     * Si el estado actual del ámbito ya está guardado, reinicia la asignación de columnas
     * antes de aplicar el nuevo mapeo.
     * </p>
     *
     * @param mappings lista de pares clave-valor que relacionan columnas "Imperia" con columnas de "Fichero".
     */
    public void mapColumns(List<Map<String, String>> mappings) {
        // Validar si ya está guardado
        restartAssignmentIfSavedVisible();

        for (Map<String, String> row : mappings) {
            String imperia = row.getOrDefault("Imperia", row.getOrDefault("imperia", ""));
            String fileColumn = row.getOrDefault("Fichero", row.getOrDefault("fichero", ""));
            if (imperia == null || imperia.isBlank() || fileColumn == null || fileColumn.isBlank()) {
                LogUtil.warn("Fila de mapeo inválida: " + row);
                continue;
            }
            mapColumn(imperia, fileColumn);
        }
        // Clic en el botón Guardar
        clickButtonByName("Guardar");
    }

    /**
     * Reinicia la asignación de columnas solo si el estado actual está en "Guardado".
     * <p>
     * Si el badge "Guardado" es visible, se espera a que el enlace esté habilitado
     * y se hace clic en "Reiniciar asignación de columnas".
     * </p>
     */
    public void restartAssignmentIfSavedVisible() {
        try {
            boolean visible = waitUtil.isElementVisible(guardadoBadge, 2000, 200);
            if (visible) {
                LogUtil.info("Estado 'Guardado' visible. Reiniciando asignación de columnas...");
                waitUtil.waitForClassToDisappear(resetColumnMapping, "disabled", 5000, 200);
                clickButtonByName("Reiniciar asignación de columnas");
                LogUtil.info("Click en 'Reiniciar asignación de columnas' realizado.");
            } else {
                LogUtil.info("Estado 'Guardado' no visible. No se realiza ninguna acción.");
            }
        } catch (Exception e) {
            LogUtil.error("Error al intentar reiniciar asignación de columnas.", e);
            throw e;
        }
    }

    /**
     * Inicia el proceso de carga de datos para el ámbito actual.
     * <p>
     * Hace clic en el botón "Iniciar carga" y espera a que la tabla
     * cargue completamente para continuar.
     * </p>
     */
    public void startImport() {
        LogUtil.info("Iniciando la carga de datos para el ámbito");
        clickButtonByName("Iniciar carga");
        waitUtil.waitForTableToLoadCompletely();
    }

    /**
     * Valida que la última ejecución de carga haya finalizado exitosamente.
     * <p>
     * Se espera que el semáforo de estado aparezca en verde brillante (lightGreen).
     * </p>
     */
    public void verifyImportSucceeded() {
        String color = trafficLightUtil.waitForTrafficLightColorNameByTabIndex(0);
        ValidationUtil.assertEquals(color, "lightGreen", "El semáforo debe estar en verde brillante");
    }

    /**
     * Realiza el mapeo de una columna de Imperia con su correspondiente en el fichero.
     *
     * @param imperiaColumn nombre de la columna en Imperia.
     * @param fileColumn    nombre de la columna en el fichero de importación.
     */
    private void mapColumn(String imperiaColumn, String fileColumn) {
        LogUtil.info(String.format(Locale.ROOT, "Mapeando columna '%s' con '%s'", imperiaColumn, fileColumn));
        By dropdownLocator = By.xpath("//span[normalize-space(.)='" + imperiaColumn + "']/ancestor::div[@class='form-control-container']//a[@role='button']");
        clickByLocator(dropdownLocator, "Campo " + imperiaColumn);
        clickDropdownOptionByText(fileColumn);
    }
}