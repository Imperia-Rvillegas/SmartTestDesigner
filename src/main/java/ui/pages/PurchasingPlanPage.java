package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.DateUtil;
import ui.utils.LogUtil;

import java.util.Optional;

/**
 * Página que representa la vista de <strong>Plan de Compras</strong> en la aplicación web.
 *
 * Esta clase encapsula los elementos y acciones disponibles en el módulo de Plan de Compras,
 * tales como calcular el plan y aceptar la confirmación.
 *
 * Utiliza localizadores robustos basados en el texto visible de los botones y extiende
 * de {@link BasePage} para aprovechar métodos utilitarios comunes como `click`.
 *
 * Accedida comúnmente desde Step Definitions mediante el {@link PageManager}.
 *
 * Ejemplo de uso:
 * <pre>
 *   pageManager.getPlanPurchasePage().clickCalculateButton();
 * </pre>
 *
 */
public class PurchasingPlanPage extends BasePage {

    /**
     * Constructor que inicializa la página con el WebDriver y el PageManager.
     *
     * @param driver WebDriver activo.
     * @param pageManager Administrador de páginas para acceder a otras instancias.
     */
    public PurchasingPlanPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /** Fecha seleccionada como «Fecha de pedido». */
    private String orderDate;

    /** Fecha seleccionada como «Fecha de recepción». */
    private String receptionDate;

    /**
     * Selecciona un producto marcado como no descontinuado desde el formulario de Plan de compras.
     *
     * @return código del producto elegido.
     */
    public String selectNonDiscontinuedProduct() {
        By locator = buildLookupIconLocator("Producto");
        clickByLocator(locator, "Campo Producto");

        filterByList("Descontinuado", "No");

        WebElement firstCell = tableUtil.getFirstCellElementByHeaderName("Código", "Seleccionar valores");
        String productCode = Optional.ofNullable(firstCell.getText()).map(String::trim).orElse("");

        validationUtil.assertNotNull(productCode, "Código del producto no descontinuado");

        clickByElement(firstCell, "Primera fila - Producto");

        closeFilterPanelIfVisible();

        clickButtonByNameLast("Aceptar");
        return productCode;
    }

    /**
     * Selecciona el primer proveedor disponible en el formulario.
     */
    public void selectFirstSupplier() {
        By locator = buildLookupIconLocator("Proveedor");
        selectFirstRowFromLookup(locator, "Proveedor", "Code", "Seleccionar valores");
    }

    /**
     * Selecciona el primer almacén disponible si el campo se encuentra presente.
     */
    public void selectFirstWarehouseIfPresent() {
        if (!isLookupFieldPresent("Almacén")) {
            LogUtil.info("El campo 'Almacén' no está presente en el formulario de Plan de compras. Se omite la selección.");
            return;
        }

        By locator = buildLookupIconLocator("Almacén");
        selectFirstRowFromLookup(locator, "Almacén", "Code", "Seleccionar valores");
    }

    /**
     * Completa los campos numéricos requeridos del formulario: «Cantidad» e «Importe».
     *
     * @param quantity valor a ingresar en el campo «Cantidad».
     * @param amount   valor a ingresar en el campo «Importe».
     */
    public void fillQuantityAndAmount(int quantity, int amount) {
        sendKeysByTitleInt("Cantidad", quantity);
        sendKeysByTitleInt("Importe", amount);
    }

    /**
     * Selecciona la fecha actual como «Fecha de pedido».
     */
    public void selectTodayAsOrderDate() {
        orderDate = DateUtil.getToday("dd/MM/yyyy");
        sendKeysByTitle("Fecha de pedido", orderDate);
    }

    /**
     * Selecciona la fecha actual como «Fecha de recepción».
     */
    public void selectTodayAsReceptionDate() {
        receptionDate = DateUtil.getToday("dd/MM/yyyy");
        sendKeysByTitle("Fecha de recepción", receptionDate);
    }

    /**
     * Verifica que el producto previamente seleccionado aparezca en la tabla principal.
     *
     * @param productCode código del producto que debe visualizarse.
     */
    public void verifyProductListedInPlanTable(String productCode) {
        By searchLocator = By.cssSelector("input[placeholder='Buscar...']");
        searchRecord(productCode, searchLocator);

        waitUtil.waitForTableToLoadCompletely();

        By productLocator = tableUtil.buildRecordLocator(productCode);
        clickByLocator(productLocator, productCode);

        if (orderDate != null) {
            By orderLocator = tableUtil.buildRecordLocator(orderDate);
            waitUtil.scrollUntilElementIsVisible(orderLocator);
            clickByLocator(orderLocator, orderDate);
        }

        if (receptionDate != null) {
            By receptionLocator = tableUtil.buildRecordLocator(receptionDate);
            waitUtil.scrollUntilElementIsVisible(receptionLocator);
            clickByLocator(receptionLocator, receptionDate);
        }
    }

    /**
     * Construye el locator del icono de búsqueda asociado a un campo de formulario.
     *
     * @param fieldTitle texto visible del campo (por ejemplo, «Producto»).
     * @return locator del icono de lupa correspondiente.
     */
    private By buildLookupIconLocator(String fieldTitle) {
        return By.xpath("//imp-label[.//span[normalize-space(text())='" + fieldTitle
                + "']]//imp-input-table-v2//i[contains(@class,'pi-ellipsis-h')]");
    }

    /**
     * Selecciona la primera fila disponible dentro de un diálogo de búsqueda y confirma con «Aceptar».
     *
     * @param locator      localizador del icono de búsqueda a abrir.
     * @param fieldName    nombre del campo utilizado para logging.
     * @param columnHeader columna a partir de la cual se obtiene el valor de la primera fila.
     * @param dialogTitle  título del diálogo que contiene la tabla de selección.
     */
    private void selectFirstRowFromLookup(By locator, String fieldName, String columnHeader, String dialogTitle) {
        clickByLocator(locator, "Campo " + fieldName);

        WebElement firstCell = tableUtil.getFirstCellElementByHeaderName(columnHeader, dialogTitle);
        clickByElement(firstCell, "Primera fila - " + fieldName);

        clickButtonByNameLast("Aceptar");
    }

    /**
     * Cierra la ventana de filtros si está visible para evitar que interfiera con la selección.
     */
    private void closeFilterPanelIfVisible() {
        By closeFilterLocator = By.xpath("(//a[@role='button' and contains(@class, 'button-container') and .//img[contains(@src, 'back-arrow.svg')]])[last()]");
        if (waitUtil.isElementPresent(closeFilterLocator)) {
            clickByLocator(closeFilterLocator, "Cerrar ventana de filtros");
        }
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
