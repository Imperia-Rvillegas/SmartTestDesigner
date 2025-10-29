package ui.pages;

import io.cucumber.datatable.DataTable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;
import org.openqa.selenium.WebDriver;

/**
 * Clase que representa la página "Listado de materiales" en la aplicación web.
 * Encapsula la lógica de interacción con la tabla de materiales y permite navegar
 * hacia las fichas de productos padre o hijo desde los hipervínculos disponibles.
 * <p>
 * Esta clase hereda de {@link BasePage} y utiliza utilidades comunes como
 * {@code TableUtil}, {@code WaitUtil} y {@code ValidationUtil} proporcionadas por {@link PageManager}.
 * </p>
 */
public class ListOfMaterialsPage extends BasePage {

    By productLocator = By.xpath("//imp-input-table-v2[@formcontrolname='IdProduct']//a[contains(@class, 'button-container')]");
    By productMaerialLocator = By.xpath("//imp-input-table-v2[@formcontrolname='IdProductMaterial']//a[contains(@class, 'button-container')]");
    By searchIconButton = By.cssSelector("imperia-icon-button[icon='search'] a.button-container");
    By inputTextSearch = By.cssSelector("div.input-container.expanded input[type='text']");

    String code;
    String currentParentProductDescription;
    String newParentProductDescription;
    int currentChildAmount;
    int newChildAmount;
    String fatherDescription;
    String sonDescription;

    /**
     * Constructor que inicializa la página con el WebDriver y el PageManager.
     *
     * @param driver       Instancia activa de {@link WebDriver}.
     * @param pageManager  Gestor centralizado de páginas y utilidades.
     */
    public ListOfMaterialsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Verifica que la tabla de materiales esté correctamente cargada en pantalla,
     * validando el título de la pantalla.
     */
    public void theMaterialsTableIsLoaded() {
        validationUtil.assertCurrentScreen("Materiales");
    }

    /**
     * Hace clic en el hipervínculo del primer registro en la columna "Código padre".
     * Almacena el código en la variable interna {@code code}.
     */
    public void clickOnTheParentCodeHyperlink() {
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Código padre", "Materiales");
        WebElement hyperlink = element.findElement(By.tagName("a"));
        code = element.getText();
        clickByElement(hyperlink, "Código padre del primer registro de la tabla");
    }

    /**
     * Verifica que la ficha del producto haya sido cargada correctamente,
     * validando el título y la presencia del código.
     */
    public void theProductTableIsLoaded() {
        boolean theTitleIsVisible = waitUtil.isElementVisible(By.xpath("//div[contains(@class, 'title') and normalize-space(text())='Producto']"), 10000, 500);
        validationUtil.assertTrue(theTitleIsVisible, "El título Producto está visible");
        boolean isTheParentCodePresent = waitUtil.isElementVisible(By.xpath("//div[contains(@class, 'title') and contains(text(), '" + code + "')]"), 10000, 500);
        validationUtil.assertTrue(isTheParentCodePresent, "La ficha del producto correspondiente está visible");
    }

    /**
     * Hace clic en el hipervínculo del primer registro en la columna "Código hijo".
     * Almacena el código en la variable interna {@code code}.
     */
    public void clickOnTheChildCodeHyperlink() {
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Código hijo", "Materiales");
        WebElement hyperlink = element.findElement(By.tagName("a"));
        code = element.getText();
        clickByElement(hyperlink, "Código hijo del primer registro de la tabla");
    }

    /**
     * Selecciona el primer producto padre disponible en la lista emergente y confirma la selección.
     */
    public void selectParentProduct() {
        clickByLocator(productLocator, "Lista producto padre");
        waitUtil.sleepMillis(300, "Espera que cargue completamente la tabla");
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Descripción", "Seleccionar valores");
        currentParentProductDescription = element.getText();
        clickByElement(element, "Primera producto padre");
        clickButtonByNameAndPosition("Aceptar", 2);
    }

    /**
     * Verifica que las columnas visibles de la tabla coincidan con las esperadas.
     *
     * @param expectedColumns columnas esperadas listadas en un DataTable.
     */
    public void iMustDisplayTheColumnsAmongOthers(DataTable expectedColumns) {
        tableUtil.verifyTableColumns(expectedColumns, "Materiales");
    }

    /**
     * Selecciona el primer producto hijo disponible en la lista emergente y confirma la selección.
     */
    public void selectChildProduct() {
        clickByLocator(productMaerialLocator, "Lista producto hijo");
        WebElement firstCell = tableUtil.getFirstCellElementByHeaderName("Descripción", "Seleccionar valores");
        clickByElement(firstCell, "Primera fila");
        clickButtonByNameAndPosition("Aceptar", 2);
    }

    /**
     * Confirma la creación del nuevo vínculo producto padre-hijo presionando "Aceptar".
     */
    public void confirmsTheCreation() {
        clickButtonByName("Aceptar");
    }

    /**
     * Verifica que la nueva relación padre-hijo se haya registrado correctamente en la tabla.
     */
    public void registerTheNewRelationship() {
        // Hace clic en la lupa
        clickByLocator(searchIconButton, "Icono buscar");

        // Envia la descripcion del registro a buscar
        WebElement elementInputSearch = waitUtil.findVisibleElement(inputTextSearch);
        sendKeysByElement(elementInputSearch, currentParentProductDescription, "Buscar");

        // Espera que cargue completamente la tabla
        waitUtil.waitForTableToLoadCompletely();

        // Obtiene la primera fila de la tabla
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Descripción padre", "Materiales");
        newParentProductDescription = element.getText();

        // Hace la comparacion
        validationUtil.assertEquals(currentParentProductDescription, newParentProductDescription, "El nuevo producto padre se muestra en la lista de materiales");
    }

    /**
     * Modifica el valor de la columna "Cantidad hijo" del primer registro,
     * incrementándolo en uno.
     */
    public void modifyChildQuantity() {
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Cantidad hijo", "Materiales");
        waitUtil.scrollRightUntilElementIsVisible(element);
        currentChildAmount = Integer.parseInt(element.getText().trim());
        newChildAmount = currentChildAmount + 1;
        clickByElement(element, "Primer registro de la columna Cantidad hijo");

        WebElement input = element.findElement(By.cssSelector("input"));
        safeClear(input, "Celda de columna Cantidad hijo");
        sendKeysByElement(input, newChildAmount, "Input editable de la columna Cantidad hijo");
        waitUtil.sleepMillis(300, "Espera a que se ingrese el valor correctamente en la celda antes de presionar enter");
        pressEnterKey(input);
    }

    /**
     * Verifica que el valor actualizado en "Cantidad hijo" se haya reflejado correctamente.
     */
    public void amountOfChildrenIsUpdated() {
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Cantidad hijo", "Materiales");
        currentChildAmount = Integer.parseInt(element.getText().trim());
        validationUtil.assertEquals(currentChildAmount, newChildAmount, "El valor se actualizó correctamente");
    }

    /**
     * Selecciona el primer registro de la tabla de materiales y guarda sus valores padre e hijo.
     */
    public void selectTheFirstMaterialRecord() {
        WebElement fatherElement = tableUtil.getFirstCellElementByHeaderName("Descripción padre", "Materiales");
        fatherDescription = fatherElement.getText().trim();
        WebElement sonElement = tableUtil.getFirstCellElementByHeaderName("Descripción hijo", "Materiales");
        sonDescription = sonElement.getText().trim();
        clickByElement(fatherElement, "Primer registro de la tabla Materiales");
    }

    /**
     * Verifica que el registro previamente seleccionado haya sido eliminado de la tabla.
     */
    public void theRecordMustBeDeletedFromTheMaterialTable() {
        tableUtil.assertFirstRowValuesNotMatchTwoColumns("Descripción padre", "Descripción hijo", fatherDescription, sonDescription, "Materiales");
    }

    /**
     * Aplica un filtro en la columna "Descripción hijo" con el valor previamente capturado.
     */
    public void applyFilterByChildCode() {
        applyFilterBy("Descripción hijo", sonDescription);
    }

    /**
     * Toma el valor del primer registro en la columna "Descripción hijo" y lo guarda en {@code sonDescription}.
     */
    public void valueOfFirstChildCode() {
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Descripción hijo", "Materiales");
        sonDescription = element.getText().trim();
    }

    /**
     * Verifica que el resultado filtrado por "Descripción hijo" se muestre en la tabla.
     */
    public void filteredByChildCodeAppears() {
        validationUtil.assertRecordListed(sonDescription);
    }

    public void checkErrorPopup() {
        popupUtil.assertNoErrorPopupPresent();
    }

    public void verificaQueElMaterialExisteEnLaListaDeMateriales(String material) {
        // Hace clic en la lupa
        clickByLocator(searchIconButton, "Icono buscar");

        // Envia la descripcion del registro a buscar
        WebElement elementInputSearch = waitUtil.findVisibleElement(inputTextSearch);
        sendKeysByElement(elementInputSearch, material, "Buscar");

        // Espera que cargue completamente la tabla
        waitUtil.waitForTableToLoadCompletely();

        // Obtiene la primera fila de la tabla
        WebElement element = tableUtil.getFirstCellElementByHeaderName("Descripción padre", "Materiales");
        newParentProductDescription = element.getText();

        // Hace la comparacion
        validationUtil.assertEquals(material, newParentProductDescription, "El nuevo producto padre se muestra en la lista de materiales");
    }
}