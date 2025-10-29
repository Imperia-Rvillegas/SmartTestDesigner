package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import testmodel.inventoryLocationsData;
import ui.manager.PageManager;
import ui.pages.InventoryLocationsPage;
import ui.utils.*;

/**
 * Step Definitions para la funcionalidad de Ubicaciones de Inventario.
 * <p>
 * Encapsula los pasos específicos de la funcionalidad, reutilizando el Page Object `InventoryLocationsPage`
 * y utilidades compartidas como `TableUtil`, `WaitUtil` y `ValidationUtil`.
 * <p>
 * Los datos de prueba se cargan desde un archivo YAML mediante el modelo {@link inventoryLocationsData}.
 */
public class InventoryLocationsSteps {

    private final inventoryLocationsData data;
    private final PageManager pageManager = Hooks.getPageManager();
    private final InventoryLocationsPage inventoryLocationsPage = pageManager.getInventoryLocationsPage();

    // Datos de prueba cargadas desde YAML
    private final String newLocationCode;
    private final String newLocationDescription;
    private final String editedDescription;
    private final String restrictedLocation;
    private final String codeEdited;

    /**
     * Constructor que carga los datos desde el archivo YAML correspondiente
     * y los asigna a variables reutilizables para los pasos.
     */
    public InventoryLocationsSteps() {
        this.data = TestDataLoader.load("testdata/page/inventoryLocations.yaml", inventoryLocationsData.class);
        this.newLocationCode = data.getNewLocationCode();
        this.newLocationDescription = data.getNewLocationDescription();
        this.editedDescription = data.getEditedDescription();
        this.restrictedLocation = data.getRestrictedLocation();
        this.codeEdited = data.getCodeEdited();
    }

    /**
     * Verifica que la tabla muestre los encabezados de columna esperados.
     *
     * @param expectedColumns DataTable con los nombres de columnas esperados.
     */
    @Then("^la tabla de Ubicaciones muestra las columnas siguientes:$")
    public void verifyTableColumns(DataTable expectedColumns) {
        inventoryLocationsPage.verifyTableColumns(expectedColumns);
    }

    /**
     * Verifica que el sistema muestre un mensaje emergente (popup) con el texto especificado.
     *
     * @param expectedMessage Texto exacto que se espera ver en el popup.
     */
    @Then("se muestra un Popup con mensaje {string}")
    public void theSystemShowsThePopup(String expectedMessage) {
        inventoryLocationsPage.verifyErrorMessagePopup(expectedMessage);
    }

    /**
     * Ingresa el código para la nueva ubicación.
     */
    @And("Ingresa el codigo para la nueva ubicacion")
    public void enterNewLocationCode() {
        inventoryLocationsPage.enterNewLocationCode(newLocationCode);
    }

    /**
     * Ingresa la descripción para la nueva ubicación.
     */
    @And("Ingresa la descripcion para la nueva ubicacion")
    public void enterNewLocationDescription() {
        inventoryLocationsPage.enterNewLocationDescription(newLocationDescription);
    }

    /**
     * Verifica que la nueva ubicación aparece listada en la tabla.
     */
    @Then("la nueva ubicacion aparece en la tabla")
    public void newLocationAppearsInTable() {
        inventoryLocationsPage.newLocationAppearsInTable(newLocationCode);
    }

    /**
     * Aplica un filtro por el código de ubicación.
     */
    @When("aplica filtro por codigo de ubicacion")
    public void applyFilterByLocation() {
        inventoryLocationsPage.applyFilterByLocation("Código", newLocationCode);
    }

    /**
     * Verifica que el código de la ubicación aparece en los resultados filtrados.
     */
    @Then("el codigo de la ubicacion aparece en los resultados")
    public void locationCodeAppears() {
        inventoryLocationsPage.locationCodeAppears(newLocationCode);
    }

    /**
     * Aplica un filtro por la descripción de la ubicación.
     */
    @And("aplica filtro por descripcion de ubicacion")
    public void applyFilterByDescription() {
        inventoryLocationsPage.applyFilterByDescription("Descripción", newLocationDescription);
    }

    /**
     * Verifica que la descripción de la ubicación aparece en los resultados filtrados.
     */
    @Then("la descripcion de la ubicacion aparece en los resultados")
    public void locationDescriptionAppears() {
        inventoryLocationsPage.locationDescriptionAppears(newLocationDescription);
    }

    /**
     * Busca la ubicación recién creada usando su descripción.
     */
    @When("busca la descripcion de la ubicacion creada en el buscador")
    public void lookForTheDescription() {
        inventoryLocationsPage.lookForTheLocationDescription(newLocationDescription);
    }

    /**
     * Modifica la descripción de una ubicación existente.
     */
    @And("modifica la descripcion de la ubicacion")
    public void modifyTheDescription() {
        inventoryLocationsPage.modifyTheDescription("Descripción", newLocationDescription, editedDescription);
    }

    /**
     * Verifica que la ubicación modificada aparece correctamente en la tabla.
     */
    @Then("la ubicacion editada aparece en la tabla")
    public void locationEditedAppears() {
        inventoryLocationsPage.locationEditedAppears(editedDescription);
    }

    /**
     * Busca una ubicación asociada a un inventario.
     */
    @And("busca una ubicacion asociada a un inventario")
    public void searchLocationAssociatedWithInventory() {
        inventoryLocationsPage.searchLocationAssociatedWithInventory(restrictedLocation);
    }

    /**
     * Intenta modificar el código de una ubicación asociada.
     */
    @And("intenta modificar el codigo de la ubicacion")
    public void modifyTheCode() {
        inventoryLocationsPage.modifyTheCode("Código", restrictedLocation, codeEdited);
    }

    /**
     * Selecciona una ubicación que no está asociada a ningún inventario.
     */
    @And("selecciona una ubicacion que no esta asociada a ningun inventario")
    public void locationNotAssociated() {
        inventoryLocationsPage.locationNotAssociated(editedDescription);
    }

    /**
     * Verifica que la ubicación ya no aparece en la tabla (por ejemplo, tras eliminarla).
     */
    @Then("la ubicacion ya no aparece en la tabla")
    public void locationNotAppearing() {
        inventoryLocationsPage.locationNotAppearing(editedDescription);
    }

    /**
     * Selecciona una ubicación que está asociada a un inventario (por ejemplo, para validaciones de restricción).
     */
    @And("selecciona una ubicacion asociada a un inventario")
    public void selectAssociatedLocation() {
        inventoryLocationsPage.selectAssociatedLocation(restrictedLocation);
    }
}