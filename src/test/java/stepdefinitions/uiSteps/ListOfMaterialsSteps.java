package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import ui.manager.PageManager;
import ui.pages.ListOfMaterialsPage;
import ui.utils.*;

/**
 * Clase que contiene los pasos definidos para la automatización de pruebas
 * en la pantalla "Listado de Materiales", utilizando Cucumber y Selenium WebDriver.
 * Implementa interacciones como verificación de columnas, navegación por hipervínculos,
 * filtros, modificaciones y exportaciones a Excel.
 */
public class ListOfMaterialsSteps {

    private final PageManager pageManager = Hooks.getPageManager();
    private final ListOfMaterialsPage listOfMaterialsPage = pageManager.getListOfMaterialsPage();

    /**
     * Constructor que inicializa la página de Listado de Materiales desde el PageManager.
     */
    public ListOfMaterialsSteps() {
    }

    /**
     * Verifica que la tabla de materiales haya sido correctamente cargada en pantalla.
     */
    @When("se carga la tabla de materiales")
    public void theMaterialsTableIsLoaded() {
        listOfMaterialsPage.theMaterialsTableIsLoaded();
    }

    /**
     * Verifica que la tabla muestre las columnas esperadas según el DataTable proporcionado.
     *
     * @param expectedColumns columnas esperadas listadas en el feature.
     */
    @Then("^la tabla de Listado de Materiales muestra las columnas siguientes:$")
    public void iMustDisplayTheColumnsAmongOthers(DataTable expectedColumns) {
        listOfMaterialsPage.iMustDisplayTheColumnsAmongOthers(expectedColumns);
    }

    /**
     * Hace clic en el hipervínculo del código padre del primer registro de la tabla.
     */
    @When("hago clic en el hipervinculo del codigo padre del primer registro de la tabla")
    public void clickOnTheParentCodeHyperlink() {
        listOfMaterialsPage.clickOnTheParentCodeHyperlink();
    }

    /**
     * Verifica que se haya cargado correctamente la ficha del producto correspondiente.
     */
    @Then("debo ser redirigido a la ficha del producto correspondiente")
    public void theProductTableIsLoaded() {
        listOfMaterialsPage.theProductTableIsLoaded();
    }

    /**
     * Hace clic en el hipervínculo del código hijo del primer registro de la tabla.
     */
    @When("hago clic en el hipervinculo del código hijo del primer registro de la tabla")
    public void clickOnTheChildCodeHyperlink() {
        listOfMaterialsPage.clickOnTheChildCodeHyperlink();
    }

    /**
     * Selecciona un producto padre para asociarlo como nuevo producto en la tabla.
     */
    @And("selecciona producto padre para nuevo producto")
    public void selectParentProduct() {
        listOfMaterialsPage.selectParentProduct();
    }

    /**
     * Selecciona un producto hijo para asociarlo como nuevo producto en la tabla.
     */
    @And("selecciona producto hijo para nuevo producto")
    public void selectChildProduct() {
        listOfMaterialsPage.selectChildProduct();
    }

    /**
     * Confirma la creación de la nueva relación padre-hijo entre productos.
     */
    @And("confirma la creacion")
    public void confirmsTheCreation() {
        listOfMaterialsPage.confirmsTheCreation();
    }

    /**
     * Verifica que la nueva relación se haya registrado correctamente en la tabla.
     */
    @Then("debe registrarse la nueva relacion en la tabla")
    public void registerTheNewRelationship() {
        listOfMaterialsPage.registerTheNewRelationship();
    }

    /**
     * Modifica la cantidad del primer registro en la columna de cantidad del hijo.
     */
    @And("modifica el primer registro de la columna cantidad hijo")
    public void modifyChildQuantity() {
        listOfMaterialsPage.modifyChildQuantity();
    }

    /**
     * Verifica que la cantidad modificada se haya actualizado correctamente en la tabla.
     */
    @Then("el registro de la columna cantidad hijo debe actualizarse correctamente")
    public void amountOfChildrenIsUpdated() {
        listOfMaterialsPage.amountOfChildrenIsUpdated();
    }

    /**
     * Selecciona el primer registro en la tabla de materiales.
     */
    @And("que selecciono el primer registro en la tabla de materiales")
    public void selectTheFirstMaterialRecord() {
        listOfMaterialsPage.selectTheFirstMaterialRecord();
    }

    /**
     * Verifica que el registro seleccionado haya sido eliminado de la tabla.
     */
    @Then("el registro debe eliminarse de la tabla materiales")
    public void theRecordMustBeDeletedFromTheMaterialTable() {
        listOfMaterialsPage.theRecordMustBeDeletedFromTheMaterialTable();
    }

    /**
     * Valida que las columnas de la tabla se reajusten automáticamente para mejorar la visualización.
     * (Lógica pendiente de implementación).
     */
    @Then("las columnas deben reajustarse automáticamente para mejorar la visualización")
    public void columnsAreReadjusted() {
        LogUtil.info("Pendiente de implementar logica para validar que las columnas se ajustaron");
    }

    /**
     * Aplica un filtro por la descripción del código hijo en la tabla de materiales.
     */
    @And("aplica filtro por descripcion hijo en la tabla materiales")
    public void applyFilterByChildCode() {
        listOfMaterialsPage.applyFilterByChildCode();
    }

    /**
     * Toma el primer valor visible en la columna de descripción hijo.
     */
    @And("toma el primer valor de la columna descripcion hijo")
    public void valueOfFirstChildCode() {
        listOfMaterialsPage.valueOfFirstChildCode();
    }

    /**
     * Verifica que el resultado filtrado por descripción hijo esté presente en la tabla.
     */
    @Then("el registro filtrado por descripcion hijo aparece en los resultados")
    public void filteredByChildCodeAppears() {
        listOfMaterialsPage.filteredByChildCodeAppears();
    }

    /**
     * Paso de Cucumber que verifica que no se muestre ningún popup de error en la pantalla actual.
     *
     * <p>Este paso se utiliza para garantizar que no se haya producido ningún error inesperado
     * tras realizar una acción en la interfaz de usuario. Si se detecta un popup de error visible,
     * el mensaje se registra en el log y la prueba falla mediante una aserción.</p>
     *
     * <p>Ejemplo en feature file:
     * <pre>
     *   Y verifica que no se muestre ningun popup de error
     * </pre>
     * </p>
     *
     * @throws AssertionError si un popup de error está presente con un mensaje visible.
     */
    @And("verifica que no se muestre ningun popup de error")
    public void checkErrorPopup() {
        listOfMaterialsPage.checkErrorPopup();
    }

    @And("verifica que el material {string} existe en la lista de materiales")
    public void verificaQueElMaterialExisteEnLaListaDeMateriales(String material) {
        listOfMaterialsPage.verificaQueElMaterialExisteEnLaListaDeMateriales(material);
    }
}