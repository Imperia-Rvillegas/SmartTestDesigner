package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.*;
import testmodel.businessDimensionsData;
import ui.manager.PageManager;
import ui.pages.BusinessDimensionsPage;
import ui.utils.TestDataLoader;

/**
 * Clase de definiciones de pasos para los escenarios relacionados con la gestión
 * de Dimensiones de Negocio en la interfaz de usuario.
 *
 * <p>Estos pasos están escritos para ser utilizados con Cucumber en pruebas
 * automatizadas de UI. La clase interactúa con la página de Dimensiones de Negocio
 * y utiliza utilidades de captura de pantalla para evidenciar los resultados.</p>
 */
public class BusinessDimensionsSteps {

    private final PageManager pageManager = Hooks.getPageManager();
    private final BusinessDimensionsPage businessDimensionsPage = pageManager.getBusinessDimensionsPage();

    // Variables de prueba cargadas desde YAML
    private final businessDimensionsData data;
    private final String nameOfNewBusinessDimension;
    private final String defaultValueOfNewBusinessDimension;
    private final String editedDefaultValue;
    private final String businessDimensionNameEdited;

    /**
     * Constructor que carga los datos desde el archivo YAML correspondiente
     * y los asigna a variables reutilizables para los pasos.
     */
    public BusinessDimensionsSteps() {
        this.data = TestDataLoader.load("testdata/page/businessDimensions.yaml", businessDimensionsData.class);
        this.nameOfNewBusinessDimension = data.getNameOfNewBusinessDimension();
        this.defaultValueOfNewBusinessDimension = data.getDefaultValueOfNewBusinessDimension();
        this.editedDefaultValue = data.getEditedDefaultValue();
        this.businessDimensionNameEdited = data.getBusinessDimensionNameEdited();
    }

    /**
     * Completa el campo "Nombre" de la dimensión con el valor especificado.
     *
     * @param name el nombre a ingresar en el campo.
     */
    @And("ingresa el nombre de la nueva dimension de negocio")
    public void completeName() {
        businessDimensionsPage.fillName(nameOfNewBusinessDimension);
    }

    /**
     * Completa el campo "Valor por defecto" con el valor proporcionado.
     *
     * @param value el valor por defecto a ingresar.
     */
    @And("ingresa el valor por defecto de la nueva dimension de negocio")
    public void completeDefaultValue() {
        businessDimensionsPage.fillDefaultValue(defaultValueOfNewBusinessDimension);
    }

    /**
     * Limpia el contenido del campo "Valor por defecto".
     */
    @And("limpia el campo Valor por defecto")
    public void clearDefaultValue() {
        businessDimensionsPage.clearDefaultValue();
    }

    /**
     * Valida que se muestre en la interfaz la dimensión de negocio con el nombre especificado
     * y captura una evidencia visual del resultado.
     *
     * @param name el nombre de la dimensión que se espera ver.
     */
    @And("la nueva dimension de negocio se muestra en los resultados")
    public void validateBusinessDimensionPresent() {
        businessDimensionsPage.validateDimensionPresent(nameOfNewBusinessDimension);
    }

    /**
     * Valida que se muestre en la interfaz el nuevo valor por defecto proporcionado
     * y captura una evidencia visual del resultado.
     *
     * @param value el valor por defecto que se espera ver.
     */
    @And("el sistema muestra el valor por defecto de la dimension de negocio editado")
    public void validateValuePresent() {
        businessDimensionsPage.validateValuePresent(editedDefaultValue);
    }

    /**
     * Verifica que la dimensión de negocio con el nombre dado ya no se encuentra visible en la interfaz.
     *
     * @param name el nombre de la dimensión que ya no debe estar presente.
     */
    @And("la dimension de negocio editada ya no aparece en los resultados")
    public void validateBusinessDimensionNotPresent() {
        businessDimensionsPage.validateDimensionNotPresent(businessDimensionNameEdited);
    }

    /**
     * Elimina la dimensión de negocio con el nombre especificado.
     *
     * @param name el nombre de la dimensión que se desea eliminar.
     */
    @When("elimina la dimension de negocio con nombre editado")
    public void deleteBusinessDimension() {
        businessDimensionsPage.deleteDimension(businessDimensionNameEdited);
    }

    /**
     * Crea dimensiones de negocio hasta alcanzar el límite máximo permitido de 3.
     */
    @When("crea dimensiones hasta completar el limite de 3")
    public void createDimensionsUpToLimit() {
        businessDimensionsPage.ensureMaxThreeDimensionsExist(nameOfNewBusinessDimension, defaultValueOfNewBusinessDimension);
    }

    /**
     * Intenta crear una dimensión de negocio adicional por encima del límite permitido.
     */
    @And("intenta crear una nueva dimension adicional")
    public void createSurplusDimension() {
        businessDimensionsPage.crearDimensionExcedente();
    }

    /**
     * Modifica el valor por defecto de una dimensión de negocio existente usando los datos cargados desde YAML.
     * Utiliza la lógica de edición en celda de la tabla, reemplazando el valor original por uno nuevo.
     */
    @And("modifica el valor por defecto de la dimension de negocio")
    public void modifiesDefaultValueOfDimension() {
        businessDimensionsPage.modifiesDefaultValueOfDimension("Valor por defecto", "Dimension de negocio", defaultValueOfNewBusinessDimension, editedDefaultValue);
    }

    /**
     * Modifica el nombre de una dimensión de negocio existente.
     * Localiza el nombre original en la tabla y lo reemplaza por uno nuevo definido en el YAML.
     */
    @And("modifica el nombre de la dimension de negocio")
    public void modifyDimensionName() {
        businessDimensionsPage.modifyDimensionName("Nombre", "Dimensiones de negocio", nameOfNewBusinessDimension, businessDimensionNameEdited);
    }

    /**
     * Verifica que el nombre editado de la dimensión de negocio se muestra correctamente en la tabla.
     * Comprueba que la modificación se refleje en la interfaz.
     */
    @Then("el sistema muestra el nombre de la dimension de negocio editado")
    public void EditedBusinessDimensionNameIsShown() {
        businessDimensionsPage.EditedBusinessDimensionNameIsShown(businessDimensionNameEdited);
    }
}