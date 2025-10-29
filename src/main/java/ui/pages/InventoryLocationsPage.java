package ui.pages;

import io.cucumber.datatable.DataTable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Page Object que modela la pantalla de **Ubicaciones de inventario**.
 * <p>
 * Encapsula todos los elementos y acciones reutilizables de esta vista:
 * creación, edición, eliminación, búsqueda y ajuste de columnas.
 */
public class InventoryLocationsPage extends BasePage {

    private final By locatorSearch = By.cssSelector("input[placeholder='Buscar...']");

    /**
     * Constructor que inicializa la página.
     *
     * @param driver      WebDriver activo.
     * @param pageManager Gestor de páginas y utilidades.
     */
    public InventoryLocationsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Verifica que se muestre un popup con un mensaje de error que coincida exactamente con el texto esperado.
     *
     * @param expectedMessage Texto exacto del mensaje de error esperado.
     */
    public void verifyErrorMessagePopup(String expectedMessage) {
        popupUtil.verifyErrorTitleMessage(expectedMessage);
    }

    /**
     * Ingresa un nuevo valor en el campo "Código" para crear una ubicación.
     *
     * @param textToSend Texto a ingresar como código.
     */
    public void enterNewLocationCode(String textToSend) {
        sendKeysByTitle("Código", textToSend);
    }

    /**
     * Ingresa un nuevo valor en el campo "Descripción" para crear una ubicación.
     *
     * @param textToSend Texto a ingresar como descripción.
     */
    public void enterNewLocationDescription(String textToSend) {
        sendKeysByTitle("Descripción", textToSend);
    }

    /**
     * Valida que una ubicación recién creada aparece en la tabla.
     *
     * @param value Valor esperado en la tabla.
     */
    public void newLocationAppearsInTable(String value) {
        validationUtil.assertRecordListed(value);
    }

    /**
     * Aplica un filtro por código de ubicación en la tabla.
     *
     * @param filter Nombre del filtro (por ejemplo: "Código").
     * @param value  Valor a filtrar.
     */
    public void applyFilterByLocation(String filter, String value) {
        filterBy(filter, value);
    }

    /**
     * Valida que un código de ubicación específico esté listado en la tabla.
     *
     * @param record Código de ubicación esperado.
     */
    public void locationCodeAppears(String record) {
        validationUtil.assertRecordListed(record);
    }

    /**
     * Aplica un filtro por descripción de ubicación en la tabla.
     *
     * @param filter Nombre del filtro (por ejemplo: "Descripción").
     * @param value  Valor a filtrar.
     */
    public void applyFilterByDescription(String filter, String value) {
        filterBy(filter, value);
    }

    /**
     * Valida que una descripción de ubicación específica esté listada en la tabla.
     *
     * @param record Descripción esperada.
     */
    public void locationDescriptionAppears(String record) {
        validationUtil.assertRecordListed(record);
    }

    /**
     * Modifica la celda de la columna indicada para una ubicación específica.
     *
     * @param column     Nombre de la columna a modificar (por ejemplo: "Descripción").
     * @param location   Valor actual de la ubicación (clave para encontrar el registro).
     * @param newRecord  Nuevo valor a establecer.
     */
    public void modifyTheDescription(String column, String location, String newRecord) {
        modifyCell(column, location, newRecord);
    }

    /**
     * Valida que el valor editado aparezca correctamente en la tabla.
     *
     * @param value Valor esperado tras la edición.
     */
    public void locationEditedAppears(String value) {
        validationUtil.assertRecordListed(value);
    }

    /**
     * Realiza una búsqueda en la tabla por el campo "Código".
     *
     * @param location       Valor a buscar.
     * @param tablePosition  Posición esperada en la tabla (útil para tablas virtualizadas).
     */
    public void searchLocationAssociatedWithInventory(String location) {
        searchRecord(location, locatorSearch);
    }

    /**
     * Modifica el código de una ubicación específica.
     *
     * @param column       Nombre de la columna a modificar.
     * @param originalCode Código actual.
     * @param newCode      Nuevo código.
     */
    public void modifyTheCode(String column, String originalCode, String newCode) {
        modifyCell(column, originalCode, newCode);
    }

    /**
     * Selecciona una ubicación no asociada, validando que esté visible en la tabla y haciendo clic sobre ella.
     *
     * @param description Descripción de la ubicación no asociada.
     */
    public void locationNotAssociated(String description) {
        By locator = tableUtil.buildRecordLocator(description);
        waitUtil.scrollUntilElementIsVisible(locator);
        clickByLocator(locator, description);
    }

    /**
     * Valida que una ubicación específica no aparece en la tabla.
     *
     * @param location Valor que no debe estar presente.
     */
    public void locationNotAppearing(String location) {
        waitUtil.sleepMillis(300, "Espera breve para permitir la actualización de la tabla");
        By locator = By.xpath("//td[.//span[normalize-space(text())='" + location + "']]");
        validationUtil.assertFalse(waitUtil.isElementVisible(locator, 10000, 500), "La ubicacion no está listada: " + location);
    }

    /**
     * Selecciona una ubicación ya asociada desde la tabla.
     *
     * @param restrictedLocation Descripción de la ubicación asociada.
     */
    public void selectAssociatedLocation(String restrictedLocation) {
        By locator = tableUtil.buildRecordLocator(restrictedLocation);
        waitUtil.scrollUntilElementIsVisible(locator);
        clickByLocator(locator, restrictedLocation);
    }

    /**
     * Realiza una búsqueda por la descripción de la ubicación.
     *
     * @param location       Descripción a buscar.
     * @param tablePosition  Posición esperada del registro.
     */
    public void lookForTheLocationDescription(String location) {
        searchRecord(location, locatorSearch);
    }

    /**
     * Verifica que las columnas visibles de la tabla de Listado de ubicaciones inventario coincidan con las columnas esperadas.
     * <p>
     * Este metodo utiliza un {@code DataTable} de Cucumber que contiene la lista de nombres de columnas
     * esperadas, y delega la verificación a la utilidad {@code tableUtil}, especificando la tabla "Listado de ubicaciones inventario".
     * </p>
     *
     * @param expectedColumns tabla de datos de Cucumber que contiene los nombres de las columnas esperadas.
     */
    public void verifyTableColumns(DataTable expectedColumns) {
        tableUtil.verifyTableColumns(expectedColumns, "Listado de ubicaciones inventario");
    }
}