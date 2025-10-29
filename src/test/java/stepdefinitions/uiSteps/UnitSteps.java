package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.*;
import testmodel.unitData;
import ui.manager.PageManager;
import ui.pages.UnitPage;
import ui.utils.TestDataLoader;

/**
 * Step Definitions para los escenarios de pruebas del módulo
 * <strong>Unidades</strong> en la interfaz de usuario.
 *
 * Esta clase encapsula los pasos necesarios para:
 * <ul>
 *   <li>Navegar a la vista del módulo Unidades</li>
 *   <li>Crear, modificar y eliminar unidades</li>
 *   <li>Validar comportamiento del formulario de unidad</li>
 *   <li>Confirmar que las unidades se actualizan y reflejan correctamente en pantalla</li>
 * </ul>
 *
 * Utiliza PageManager para el acceso centralizado a páginas y utilidades.
 * Los métodos también capturan evidencia visual donde aplica.
 */
public class UnitSteps {

    private final PageManager pageManager = Hooks.getPageManager();
    private final UnitPage unitPage = pageManager.getUnitPage();

    // Datos de prueba cargadas desde YAML
    private final unitData data;
    private final String newUnitName;
    private final String descriptionOfTheNewUnit;
    private final String numberOfDecimalPlacesInTheNewUnit;
    private final String editedDescription;
    private final String unitNameWithMinimumDecimalPlaces;
    private final String minimumNumberOfDecimalsForUnit;
    private final String unitNameWithMaximumDecimalPlaces;
    private final String maximumNumberOfDecimalsForUnit;
    private final String numberOfDecimalsNotAllowedInUnit;
    private final String unitNameToAssociateWithTheArticle;
    private final String unitDescriptionToAssociateWithTheArticle;

    /**
     * Constructor que carga los datos desde el archivo YAML correspondiente
     * y los asigna a variables reutilizables para los pasos.
     */
    public UnitSteps() {
        this.data = TestDataLoader.load("testdata/page/unit.yaml", unitData.class);
        this.newUnitName = data.getNewUnitName();
        this.descriptionOfTheNewUnit = data.getDescriptionOfTheNewUnit();
        this.numberOfDecimalPlacesInTheNewUnit = data.getNumberOfDecimalPlacesInTheNewUnit();
        this.editedDescription = data.getEditedDescription();
//        this.unitAssociatedWithArticle = data.getUnitAssociatedWithArticle();
        this.unitNameWithMinimumDecimalPlaces = data.getUnitNameWithMinimumDecimalPlaces();
        this.minimumNumberOfDecimalsForUnit = data.getMinimumNumberOfDecimalsForUnit();
        this.unitNameWithMaximumDecimalPlaces = data.getUnitNameWithMaximumDecimalPlaces();
        this.maximumNumberOfDecimalsForUnit = data.getMaximumNumberOfDecimalsForUnit();
        this.numberOfDecimalsNotAllowedInUnit = data.getNumberOfDecimalsNotAllowedInUnit();
        this.unitNameToAssociateWithTheArticle = data.getUnitNameToAssociateWithTheArticle();
        this.unitDescriptionToAssociateWithTheArticle = data.getUnitDescriptionToAssociateWithTheArticle();
    }

    /**
     * Completa el formulario de creación de unidad con los valores proporcionados.
     *
     * @param name Nombre de la unidad.
     * @param description Descripción de la unidad.
     * @param decimals Número de decimales.
     */
    @And("completa los campos Nombre con {string}, Descripción con {string} y Número de decimales con {string}")
    public void completeFields(String name, String description, String decimals) {
        unitPage.completeForm(name, description, decimals);
    }

    /**
     * Valida que la unidad creada esté presente en la tabla de unidades.
     *
     * @param name Nombre de la unidad esperada.
     */
    @Then("la unidad creada aparece en la lista de unidades")
    public void unitAppears() {
        unitPage.unitAppearsInTable(newUnitName);
    }

    /**
     * Navega a la vista de unidades y abre el formulario de creación.
     */
    @Given("el usuario ingresa en el formulario de creación de nueva unidad")
    public void inNewUnitForm() {
        unitPage.clickButtonByName("Nuevo");
    }

    /**
     * Completa el formulario dejando vacío el campo Nombre y hace clic en Aceptar.
     */
    @When("deja vacio el campo nombre y hace clic en Aceptar")
    public void emptyNameField() {
        unitPage.completeForm("", descriptionOfTheNewUnit, numberOfDecimalPlacesInTheNewUnit);
        unitPage.clickAccept();
    }

    /**
     * Asegura que una unidad específica se encuentre visible en la tabla.
     *
     * @param unitName Nombre de la unidad a buscar.
     */
    @Given("encuentra una unidad {string} en la lista de unidades")
    public void findUnity(String unitName) {
        unitPage.unitAppearsInTable(unitName);
    }

    /**
     * Valida que la descripción modificada se refleje correctamente.
     *
     * @param unitName Nombre de la unidad modificada.
     */
    @Then("la descripción actualizada se refleja en la unidad creada")
    public void validateDescriptionOfUnit() {
        unitPage.validateDescriptionOfUnit(newUnitName, editedDescription);
    }

    /**
     * Valida que el sistema muestre un mensaje de bloqueo de eliminación por estar asociada a artículos.
     */
    @Then("el sistema muestra un mensaje indicando que una de las unidades esta asociada a un articulo")
    public void messageUnitNotDeletable() {
        unitPage.messageUnitNotDeletable();
    }

    /**
     * Borra el valor del campo Nombre para una unidad editable.
     *
     * @param unitName Nombre de la unidad objetivo.
     */
    @And("borra el nombre de la unidad creada")
    public void borraElValorDelCampoNombre() {
        unitPage.deleteTheNameValue(newUnitName, "Name");
    }

    /**
     * Selecciona una unidad asociada a un artículo.
     *
     * @param unitName Nombre de la unidad.
     */
    @And("selecciona la unidad asociada al articulo")
    public void selectAssociatedUnit() {
        findUnity(unitNameToAssociateWithTheArticle);
        unitPage.selectUnit(unitNameToAssociateWithTheArticle);
    }

    /**
     * Ingresa el nombre de la nueva unidad en el campo correspondiente.
     */
    @And("ingresa el nombre de la nueva unidad")
    public void enterTheNameOfTheNewUnit() {
        unitPage.enterTheNameOfTheNewUnit(newUnitName);
    }

    @And("ingresa el nombre de la nueva unidad que va a ser asociada al articulo")
    public void ingresaelnombredelanuevaunidadquevaaserasociadaalarticulo() {
        unitPage.enterTheNameOfTheNewUnit(unitNameToAssociateWithTheArticle);
    }

    /**
     * Ingresa la descripción de la nueva unidad en el formulario.
     */
    @And("ingresa la descripción de la nueva unidad")
    public void enterTheDescriptionOfTheNewUnit() {
        unitPage.enterTheDescriptionOfTheNewUnit(descriptionOfTheNewUnit);
    }

    @And("ingresa la descripción de la nueva unidad que va a ser asociada al articulo")
    public void ingresaeldescripciondelanuevaunidadquevaaserasociadaalarticulo() {
        unitPage.enterANewUnitDescriptionToAssociateWithTheArticle(unitDescriptionToAssociateWithTheArticle);
    }

    /**
     * Ingresa el número de decimales permitido para la nueva unidad.
     */
    @And("ingresa el número de decimales de la nueva unidad")
    public void enterNumberDecimalsNewUnit() {
        unitPage.enterNumberDecimalsNewUnit(numberOfDecimalPlacesInTheNewUnit);
    }

    /**
     * Verifica que la unidad creada aparece en la lista de unidades.
     */
    @And("encuentra la unidad creada en la lista de unidades")
    public void findTheCreatedUnity() {
        unitPage.unitAppearsInTable(newUnitName);
    }

    /**
     * Modifica la descripción de la unidad previamente creada.
     */
    @And("modifica la descripcion de la unidad creada")
    public void modifyUnitDescription() {
        unitPage.modifyUnitDescription("Descripción", "Unidad", descriptionOfTheNewUnit, editedDescription);
    }

    /**
     * Selecciona una unidad que no esté asociada a ningún artículo.
     */
    @And("selecciona una unidad no asociada a ningún artículo")
    public void selectUnitNotAssociatedWithNoItem() {
        findUnity(newUnitName);
        unitPage.selectUnit(newUnitName);
    }

    /**
     * Verifica que la unidad previamente creada ya no aparece en la lista (fue eliminada).
     */
    @Then("la unidad creada no aparece en la lista de unidades")
    public void theCreatedUnitDoesNotAppear() {
        unitPage.validateUnitWasDeleted(newUnitName);
    }

    /**
     * Ingresa el nombre de una unidad con el mínimo número de decimales permitidos.
     */
    @And("ingresa el nombre de la nueva unidad con minimo de decimales")
    public void enterNewUnitNameMithMinimumDecimalPlace() {
        unitPage.enterNewUnitNameMithMinimumDecimalPlace(unitNameWithMinimumDecimalPlaces);
    }

    /**
     * Ingresa el valor mínimo de decimales permitido para una unidad.
     */
    @And("ingresa el minimo de decimales permitidos en una unidad")
    public void enterTheMinimumDecimalPlacesAllowed() {
        unitPage.enterTheMinimumDecimalPlacesAllowed(minimumNumberOfDecimalsForUnit);
    }

    /**
     * Verifica que la unidad con el mínimo número de decimales aparece en la lista.
     */
    @Then("la nueva unidad con minimo de decimales aparece en la lista de unidades")
    public void NewMinimumDecimalUnitAppears() {
        unitPage.unitAppearsInTable(unitNameWithMinimumDecimalPlaces);
    }

    /**
     * Ingresa el nombre de una unidad con el máximo número de decimales permitidos.
     */
    @And("ingresa el nombre de la nueva unidad con maximo de decimales")
    public void enterTheNameOfTheNewUnitWithaMaximumOfDecimalPlaces() {
        unitPage.enterTheNameOfTheNewUnitWithaMaximumOfDecimalPlaces(unitNameWithMaximumDecimalPlaces);
    }

    /**
     * Ingresa el valor máximo de decimales permitido para una unidad.
     */
    @And("ingresa el maximo de decimales permitidos en una unidad")
    public void enterTheMaximumDecimalPlacesAllowed() {
        unitPage.enterTheMaximumDecimalPlacesAllowed(maximumNumberOfDecimalsForUnit);
    }

    /**
     * Verifica que la unidad con el máximo número de decimales aparece en la lista.
     */
    @Then("la nueva unidad con maximo de decimales aparece en la lista de unidades")
    public void theNewUnitWithMaximumDecimalPlaces() {
        unitPage.unitAppearsInTable(unitNameWithMaximumDecimalPlaces);
    }

    /**
     * Selecciona la unidad con el mínimo número de decimales permitidos.
     */
    @And("selecciona la unidad con minimo de decimales permitidos")
    public void selectTheUnitWithTheMinimumNumberOfDecimalPlaces() {
        findUnity(unitNameWithMinimumDecimalPlaces);
        unitPage.selectUnit(unitNameWithMinimumDecimalPlaces);
    }

    /**
     * Verifica que la unidad con el mínimo número de decimales fue eliminada de la lista.
     */
    @Then("la nueva unidad con minimo de decimales no aparece en la lista de unidades")
    public void unitWithMinimumDecimalPlaceDoesNotAppear() {
        unitPage.validateUnitWasDeleted(unitNameWithMinimumDecimalPlaces);
    }

    /**
     * Selecciona la unidad con el máximo número de decimales permitidos.
     */
    @And("selecciona la unidad con maximo de decimales permitidos")
    public void selectTheUnitWithMaximumDecimalPlaces() {
        findUnity(unitNameWithMaximumDecimalPlaces);
        unitPage.selectUnit(unitNameWithMaximumDecimalPlaces);
    }

    /**
     * Verifica que la unidad con el máximo número de decimales fue eliminada de la lista.
     */
    @Then("la nueva unidad con maximo de decimales no aparece en la lista de unidades")
    public void unitWithMaximumDecimalsDoesNotAppear() {
        unitPage.validateUnitWasDeleted(unitNameWithMaximumDecimalPlaces);
    }

    /**
     * Ingresa un número de decimales no permitido para una unidad (valor inválido).
     */
    @And("ingresa numero de decimales no permitidos en una unidad")
    public void enterTheNumberOfDecimalPlacesNotAllowed() {
        unitPage.enterTheNumberOfDecimalPlacesNotAllowed(numberOfDecimalsNotAllowedInUnit);
    }

    /**
     * Verifica que la unidad creada para ser asociada al artículo aparezca en la lista de unidades disponibles.
     * <p>
     * Este paso valida que la unidad identificada por {@code unitNameToAssociateWithTheArticle} esté presente
     * en la lista de unidades, lo que confirma que se ha creado correctamente y que puede ser seleccionada
     * para su asociación con un artículo.
     * </p>
     *
     * Paso Gherkin asociado: {@code Entonces la unidad creada para ser asociada al articulo aparece en la lista de unidades}
     */
    @Then("la unidad creada para ser asociada al articulo aparece en la lista de unidades")
    public void theUnitCreatedToBeAssociated() {
        unitPage.theUnitCreatedToBeAssociated(unitNameToAssociateWithTheArticle);
    }

    /**
     * Verifica que la unidad creada para ser asociada al artículo ya no esté presente en la lista de unidades.
     * <p>
     * Este paso se utiliza para validar que la unidad identificada por {@code unitNameToAssociateWithTheArticle}
     * fue correctamente eliminada y, por lo tanto, no aparece en la lista de unidades disponibles para asociación.
     * </p>
     *
     * Paso Gherkin asociado: {@code Entonces la unidad creada para ser asociada al articulo no aparece en la lista de unidades}
     */
    @Then("la unidad creada para ser asociada al articulo no aparece en la lista de unidades")
    public void unitCreatedToBeAssociatedWithTheArticleDoesNotAppear() {
        unitPage.validateUnitWasDeleted(unitNameToAssociateWithTheArticle);
    }
}