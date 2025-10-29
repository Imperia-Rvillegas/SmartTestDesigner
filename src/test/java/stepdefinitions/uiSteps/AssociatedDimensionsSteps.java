package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import testmodel.associatedDimensionsData;
import ui.manager.PageManager;
import ui.pages.AssociatedDimensionsPage;
import ui.utils.TestDataLoader;

/**
 * Definiciones de pasos (Step Definitions) para las pruebas Cucumber de la pantalla "Dimensiones asociadas".
 * Esta clase conecta los pasos en Gherkin con las acciones realizadas a través del Page Object {@link AssociatedDimensionsPage}.
 * Los datos utilizados en las pruebas se cargan automáticamente desde un archivo YAML.
 *
 * <p>Incluye pasos para la creación, edición, validación de límites, y verificación de dimensiones asociadas.
 */
public class AssociatedDimensionsSteps {
    private final PageManager pageManager = Hooks.getPageManager();
    private final AssociatedDimensionsPage associatedDimensionsPage = pageManager.getAssociatedDimensionsPage();

    // Datos de prueba cargadas desde YAML
    private final associatedDimensionsData data;
    private final String primaryDimensionName;
    private final String associatedDimensionName;
    private final String defaultValue;
    private final String defaultValueEdited;
    private final String editedAssociatedDimension;
    private final String extraAssociatedDimension;
    private final String extraDefaultValue;

    /**
     * Constructor que carga los datos desde el archivo YAML correspondiente
     * y los asigna a variables reutilizables para los pasos.
     */
    public AssociatedDimensionsSteps() {
        this.data = TestDataLoader.load("testdata/page/associatedDimensions.yaml", associatedDimensionsData.class);
        this.primaryDimensionName = data.getPrimaryDimensionName();
        this.associatedDimensionName = data.getAssociatedDimensionName();
        this.defaultValue = data.getDefaultValue();
        this.defaultValueEdited = data.getDefaultValueEdited();
        this.editedAssociatedDimension = data.getEditedAssociatedDimension();
        this.extraAssociatedDimension = data.getExtraAssociatedDimension();
        this.extraDefaultValue = data.getExtraDefaultValue();
    }

    /**
     * Verifica que la tabla muestre los encabezados de columna esperados.
     * @param expectedColumns DataTable con los nombres de columnas esperados (Dimensión, Dimensión asociada, Valor por defecto).
     */
    @Then("la tabla de Dimensiones asociadas muestra las columnas siguientes:")
    public void verifyTableColumns(DataTable expectedColumns) {
        associatedDimensionsPage.verifyTableColumns(expectedColumns);
    }

    /**
     * Verifica que el formulario de creación se muestra con los campos requeridos.
     * Comprueba la presencia de los campos "Dimensión", "Dimensión asociada" y "Valor por defecto".
     */
    @Then("^se muestra el formulario de creación con campos \"Dimensión\", \"Dimensión asociada\" y \"Valor por defecto\"$")
    public void verifyCreationFormFields() {
        associatedDimensionsPage.verifyCreationFormFields();
    }

    /**
     * Selecciona un valor en el campo desplegable "Dimensión".
     * Si el nombre proporcionado es una cadena vacía, no selecciona nada (simulando que se deja el campo sin elegir).
     * @param dimensionName Nombre de la dimensión principal a seleccionar, o cadena vacía para omitir la selección.
     */
    @When("selecciona dimensión principal")
    public void selectDimensionField() {
        associatedDimensionsPage.selectDimension(primaryDimensionName);
    }

    /**
     * Ingresa texto en el campo "Dimensión asociada".
     * Si el texto proporcionado es vacío, deja el campo en blanco (simulando falta de ingreso).
     * @param associatedName Nombre de la dimensión asociada a ingresar, o vacío para dejar sin completar.
     */
    @When("ingresa el nombre de la nueva dimension asociada")
    public void enterAssociatedDimension() {
        associatedDimensionsPage.enterAssociatedDimension(associatedDimensionName);
    }

    /**
     * Ingresa texto en el campo "Valor por defecto".
     * @param defaultValue Valor por defecto que se desea ingresar.
     */
    @When("ingresa el valor por defecto de la nueva dimension asociada")
    public void enterDefaultValue() {
        associatedDimensionsPage.enterDefaultValue(defaultValue);
    }

    /**
     * Dado que existe una dimensión asociada con determinados valores (precondición).
     * Si no existe, crea una nueva dimensión asociada usando la interfaz de usuario.
     * @param assocName Nombre de la dimensión asociada que debe existir.
     * @param mainDimension Dimensión principal a la que pertenece la dimensión asociada.
     * @param defaultVal Valor por defecto de la dimensión asociada.
     */
    @When("existe la dimensión asociada creada")
    public void givenAssociatedDimensionExists() {
        associatedDimensionsPage.givenAssociatedDimensionExists(primaryDimensionName, associatedDimensionName, defaultValue);
    }

    /**
     * Verifica que la tabla muestre una fila con los valores especificados para Dimensión, Dimensión asociada y Valor por defecto.
     * @param mainDimension Dimensión principal esperada en la fila.
     * @param assocName Dimensión asociada esperada en la fila.
     * @param defaultVal Valor por defecto esperado en la fila.
     */
    @Then("la nueva dimension asociada se muestra en los resultados")
    public void verifyTableRowPresent() {
        associatedDimensionsPage.verifyTableRowPresent(primaryDimensionName, associatedDimensionName, defaultValue);
    }

    /**
     * Garantiza que una dimensión principal dada tenga ya 10 dimensiones asociadas creadas (prepara el límite).
     * Si hay menos de 10, crea dimensiones asociadas adicionales hasta alcanzar 10.
     * @param mainDimension La dimensión principal que debe tener 10 dimensiones asociadas.
     */
    @And("la dimensión principal ya tiene 10 dimensiones asociadas creadas")
    public void ensureTenAssociatedDimensionsExist() {
        associatedDimensionsPage.ensureTenAssociatedDimensionsExist(associatedDimensionName, defaultValue, primaryDimensionName);
    }

    /**
     * Limpia el campo "Valor por defecto" del formulario de creación o edición.
     * Se utiliza para validar el comportamiento al dejar este campo vacío.
     */
    @And("limpia el campo Valor por defecto de la nueva dimension asociada")
    public void clearsTheDefaultValueField() {
        associatedDimensionsPage.clearsTheDefaultValueField();
    }

    /**
     * Modifica el valor por defecto de una dimensión asociada ya existente.
     * Utiliza los valores originales y editados previamente cargados desde YAML.
     */
    @And("modifica el valor por defecto de la dimension asociada existente")
    public void changeTheDefaultValue() {
        associatedDimensionsPage.changeTheDefaultValue("Valor por defecto", "Dimensión asociada", defaultValue, defaultValueEdited);
    }

    /**
     * Modifica el nombre de una dimensión asociada existente.
     * Cambia el nombre original al valor nuevo definido en el archivo YAML.
     */
    @And("modifica el nombre de la dimensión asociada existente")
    public void modifyTheNameOfTheDimension() {
        associatedDimensionsPage.modifyTheNameOfTheDimension("Dimensión asociada", "Dimensión asociada", associatedDimensionName, editedAssociatedDimension);
    }

    /**
     * Verifica que la dimensión asociada editada se muestre correctamente en la tabla de resultados.
     * Comprueba los valores actualizados de nombre y valor por defecto.
     */
    @Then("la dimension asociada editada se muestra en los resultados")
    public void dimensionEditedShows() {
        associatedDimensionsPage.dimensionEditedShows(primaryDimensionName, editedAssociatedDimension, defaultValueEdited);
    }

    /**
     * Asegura que existe una dimensión asociada con nombre y valor por defecto ya editados.
     * Si no existe, la crea con los datos definidos en el YAML.
     */
    @When("existe la dimensión asociada editada")
    public void theEditedAssociatedDimensionExists() {
        associatedDimensionsPage.theEditedAssociatedDimensionExists(primaryDimensionName, editedAssociatedDimension, defaultValueEdited);
    }

    /**
     * Selecciona una dimensión asociada ya editada desde la interfaz.
     * Utiliza el nombre editado definido en el YAML.
     */
    @And("selecciona la dimension asociada editada")
    public void selectTheEditedDimension() {
        associatedDimensionsPage.selectTheEditedDimension(editedAssociatedDimension);
    }

    /**
     * Verifica que la dimensión asociada editada ya no se muestre en la tabla de resultados.
     * Esto puede usarse para validar que fue eliminada correctamente o desasociada.
     */
    @Then("la dimension asociada editada no se muestra en los resultados")
    public void editedDimensionNotShown() {
        associatedDimensionsPage.editedDimensionNotShown(primaryDimensionName, editedAssociatedDimension, defaultValueEdited);
    }

    /**
     * Ingresa el nombre de una dimensión asociada adicional (extra), generalmente para pruebas de límite.
     * Utiliza un nombre definido adicionalmente en el archivo YAML.
     */
    @And("ingresa el nombre de una dimension asociada extra")
    public void enterNameOfAnExtraDimension() {
        associatedDimensionsPage.enterNameOfAnExtraDimension(extraAssociatedDimension);
    }

    /**
     * Verifica que la dimensión asociada extra no se muestre en los resultados.
     * Esto puede validar el cumplimiento de reglas de límite o condiciones de exclusión.
     */
    @And("la dimension asociada extra no se muestra en los resultados")
    public void ExtraDimensionNotShown() {
        associatedDimensionsPage.ExtraDimensionNotShown(primaryDimensionName, extraAssociatedDimension, extraDefaultValue);
    }
}