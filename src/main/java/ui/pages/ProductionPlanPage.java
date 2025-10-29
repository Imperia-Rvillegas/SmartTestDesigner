package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.DateUtil;
import ui.utils.LogUtil;

/**
 * Página que representa la vista de <strong>Plan de Produccion</strong> en la aplicación web.
 *
 * Esta clase encapsula los elementos y acciones disponibles en el módulo de Plan de Produccion,
 * tales como calcular el plan y aceptar la confirmación.
 *
 * Utiliza localizadores robustos basados en el texto visible de los botones y extiende
 * de {@link BasePage} para aprovechar métodos utilitarios comunes como `click`.
 *
 * Accedida comúnmente desde Step Definitions mediante el {@link PageManager}.
 *
 * Ejemplo de uso:
 * <pre>
 *   pageManager.getPlanProduction().clickCalculateButton();
 * </pre>
 *
 */
public class ProductionPlanPage extends BasePage {

    private final By locatorProcess = By.cssSelector("imp-input-table-v2[formcontrolname='Process'] imperia-icon-button i.pi.pi-ellipsis-h");
    private final By locatorLine = By.cssSelector("imp-input-table-v2[formcontrolname='N1'] imperia-icon-button i.pi.pi-ellipsis-h");
    private final By locatorWarehouse = By.cssSelector("imp-input-table-v2[formcontrolname='W1'] imperia-icon-button i.pi.pi-ellipsis-h");

    private static String today = null;

    /**
     * Constructor que inicializa la página con el WebDriver y el PageManager.
     *
     * @param driver WebDriver activo.
     * @param pageManager Administrador de páginas para acceder a otras instancias.
     */
    public ProductionPlanPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Selecciona el primer producto descontinuado disponible en el diálogo de búsqueda.
     * <p>
     * El flujo abre el selector del campo «Código producto», aplica el filtro «Descontinuado» con la opción «Sí»,
     * captura el valor de la columna «Código» de la primera fila, selecciona dicha fila y confirma con «Aceptar».
     * </p>
     *
     * @return Código del producto seleccionado, nunca nulo.
     */
    public String selectDiscontinuedProduct() {
        clickByLocator(By.cssSelector("imp-input-table-v2[formcontrolname='P1'] imperia-icon-button i.pi.pi-ellipsis-h"), "Campo Código producto");

        //Aplica el filtro Descontinuado = No
        filterByList("Descontinuado", "No");

        //Espera a que la tabla cargue totalmente
        waitUtil.waitForTableToLoadCompletely();

        // Captura el código del producto en la primera fila
        String productCode = tableUtil.getFirstCellElementByHeaderName("Código", "Seleccionar valores").getText();

        validationUtil.assertNotNull(productCode, "Código del producto no descontinuado");

        // Selecciona la primera fila
        clickByElement(tableUtil.getFirstCellElementByHeaderName("Código", "Seleccionar valores"), "Primera fila - Código");

        //Clic en cerrar ventanba de filtros
        clickByLocator(By.xpath("(//a[@role='button' and contains(@class, 'button-container') and .//img[contains(@src, 'back-arrow.svg')]])[last()]"), "Cerrar ventana de filtros");

        clickButtonByNameLast("Aceptar");
        return productCode;
    }

    /**
     * Selecciona la primera fila visible de un campo de búsqueda y confirma la selección.
     *
     * @param fieldTitle Texto del atributo {@code title} asociado al campo.
     */
    public void selectFirstRowFromLookup(By locator, String fieldName, String columnHeader, String dialogTitle) {
        // Abre el diálogo de selección
        clickByLocator(locator, "Campo " + fieldName);

        // Hace clic en la primera fila visible
        clickByElement(tableUtil.getFirstCellElementByHeaderName(columnHeader, dialogTitle), "Primera fila - " + fieldName);

        // Confirma la selección
        clickButtonByNameLast("Aceptar");
    }

    /**
     * Selecciona el primer proceso disponible dentro del selector de Plan de producción.
     */
    public void selectFirstProcess() {
        selectFirstRowFromLookup(locatorProcess, "Proceso", "Proceso", "Seleccionar valores");
    }

    /**
     * Selecciona la primera línea disponible dentro del selector de Plan de producción.
     */
    public void selectFirstLine() {
        selectFirstRowFromLookup(locatorLine, "Línea", "Código", "Seleccionar valores");
    }

    /**
     * Selecciona el primer almacén disponible si el campo «Almacén» está presente en el formulario.
     * Si el campo no existe, no realiza ninguna acción.
     */
    public void selectFirstWarehouseIfPresent() {
        if (!isLookupFieldPresent("Almacén")) {
            LogUtil.info("El campo 'Almacén' no está presente en el formulario. Se omite la selección.");
            return;
        }

        selectFirstRowFromLookup(locatorWarehouse, "Almacén", "Código", "Seleccionar valores");
    }

    /**
     * Completa los campos numéricos obligatorios del formulario: «Cantidad» e «Importe».
     *
     * @param quantity Valor que se debe ingresar en el campo «Cantidad».
     * @param amount   Valor que se debe ingresar en el campo «Importe».
     */
    public void fillQuantityAndAmount(String quantity, String amount) {
        // Ingresa cantidad
        sendKeysByLocator(By.xpath("//imp-input-number[@formcontrolname='Amount']//input"), quantity, "Cantidad");
        // Ingresa importe
        sendKeysByLocator(By.xpath("//imp-input-number[@formcontrolname='NetAmount']//input"), amount, "Importe");
    }

    /**
     * Selecciona la fecha actual como «Fecha máxima de fabricación».
     * El metodo abre el calendario asociado al campo y hace clic en el elemento con clase {@code p-datepicker-today}.
     */
    public void selectTodayAsMaxManufacturingDate() {
        // Obtiene la fecha de hoy en formato dd/MM/yyyy
        today = DateUtil.getToday("dd/MM/yyyy");

        // Ingresa la fecha en el campo calendario
        sendKeysByLocator(By.xpath("//imp-input-calendar[@formcontrolname='ReceptionDate']//input[@type='text']"), today, "Campo Fecha máxima de fabricación");
    }

    /**
     * Verifica que el código de producto proporcionado se encuentre visible en la tabla principal del Plan de producción.
     *
     * @param productCode Código del producto que debe aparecer en la columna «Código producto».
     */
    public void verifyProductListedInPlanTable(String productCode) {
        // Buscar el docigo en la lupa
        By locatorSearch = By.cssSelector("input[placeholder='Buscar...']");
        searchRecord(productCode, locatorSearch);

        // Esperar a que la tabla cargue totalmente
        waitUtil.waitForTableToLoadCompletely();

        // Validar que el código esté presente en la tabla
        By locator = tableUtil.buildRecordLocator(productCode);
        clickByLocator(locator, productCode);

        // Validar la fecha
        By locatorDate = tableUtil.buildRecordLocator(today);
        waitUtil.scrollUntilElementIsVisible(locatorDate);
        clickByLocator(locatorDate, today);
    }

    /**
     * Verifica si existe un campo de búsqueda identificado por su texto visible.
     *
     * @param text texto visible asociado al campo objetivo (por ejemplo "Almacén").
     * @return {@code true} si el campo está presente; {@code false} en caso contrario.
     */
    private boolean isLookupFieldPresent(String text) {
        String xpath = String.format(
                "//span[normalize-space(text())='%s']",
                text
        );
        By locator = By.xpath(xpath);
        return waitUtil.isElementPresent(locator);
    }
}
