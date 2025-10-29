package ui.pages;

import io.cucumber.datatable.DataTable;
import org.junit.Assert;
import org.openqa.selenium.*;
import ui.manager.PageManager;
import ui.base.BasePage;

import java.util.List;

/**
 * Page Object correspondiente a la pantalla "Dimensiones asociadas" en Imperia Supply Chain Planning.
 *
 * <p>Esta clase proporciona métodos para interactuar con los elementos de la interfaz de usuario
 * relacionados con la gestión de dimensiones asociadas. Permite realizar acciones como seleccionar
 * dimensiones principales, ingresar dimensiones asociadas, asignar valores por defecto, validar la
 * presencia de botones o filas en la tabla, y obtener mensajes del sistema.
 */
public class AssociatedDimensionsPage extends BasePage {

    // Localizadores de elementos de la pantalla
    private final By dimensionDropdown = By.xpath("//div[contains(@class, 'input-container')]/div[contains(@class, 'input') and contains(@class, 'showing-placeholder')]");
    private final By associatedInput = By.xpath("//input[@type='text' and contains(@class, 'ng-invalid')]");
    private final By defaultInput = By.cssSelector("input[type='text'].ng-untouched.ng-pristine.ng-valid");
    private final By tituloDialogo = By.xpath("//div[contains(@class, 'dialog-title') and normalize-space()='Añadir nueva dimensión asociada']");
    private final By defaultValueInput = By.xpath("(//div[@class='form-control-input']//input[@type='text'])[last()]");

    /**
     * Constructor de la clase AssociatedDimensionsPage.
     *
     * @param driver      Instancia de WebDriver para interactuar con el navegador.
     * @param pageManager Instancia de PageManager para acceder a utilidades y otras páginas.
     */
    public AssociatedDimensionsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Selecciona una dimensión principal del desplegable personalizado.
     *
     * @param dimensionName Nombre exacto de la dimensión a seleccionar.
     */
    public void selectDimension(String dimensionName) {
        clickByLocator(dimensionDropdown, "Lista desplegable de dimensiones");
        waitUtil.sleepMillis(200, "Espera a que la lista se despliegue totalmente");
        By optionLocator = By.xpath("//div[@id='cdk-overlay-2']//span[normalize-space()='" + dimensionName + "']");
        clickByLocator(optionLocator, "Dimensión");
    }

    /**
     * Ingresa el texto en el campo de "Dimensión asociada".
     * Si ya existe texto en el campo, este será reemplazado.
     *
     * @param associatedName Nombre de la dimensión asociada a registrar.
     */
    public void enterAssociatedDimension(String associatedName) {
        sendKeysByLocator(associatedInput, associatedName, "Dimensión asociada");
    }

    /**
     * Ingresa el valor por defecto para la dimensión asociada.
     *
     * @param defaultValue Texto a ingresar como valor por defecto.
     */
    public void enterDefaultValue(String defaultValue) {
        sendKeysByTitle("Valor por defecto", defaultValue);
    }

    /**
     * Obtiene el valor actual del campo "Valor por defecto".
     *
     * @return Texto actual en el campo de valor por defecto.
     */
    public String getDefaultValue() {
        return waitUtil.findVisibleElement(defaultInput).getAttribute("value");
    }

    /**
     * Obtiene los encabezados de las columnas de la tabla de dimensiones asociadas.
     *
     * @return Lista de nombres de columnas según se presentan en la tabla.
     */
    public List<String> getTableColumnHeaders() {
        return tableUtil.getColumnHeaders("Gestor de dimensiones asociadas");
    }

    /**
     * Verifica si existe una fila en la tabla con los valores dados para dimensión, asociada y valor por defecto.
     *
     * @param dimensionName  Valor esperado en la columna "Dimensión".
     * @param associatedName Valor esperado en la columna "Dimensión asociada".
     * @param defaultVal     Valor esperado en la columna "Valor por defecto".
     * @return true si se encuentra la fila con los valores indicados, false si no existe.
     */
    public boolean isAssociatedDimensionRowPresent(String dimensionName, String associatedName, String defaultVal) {
        String xpath = "//tr[td[normalize-space()='" + dimensionName + "'] and td[normalize-space()='" + associatedName + "'] and td[normalize-space()='" + defaultVal + "']]";
        List<WebElement> rows = waitUtil.findVisibleElements(By.xpath(xpath));
        return !rows.isEmpty();
    }

    /**
     * Crea una nueva dimensión asociada completando el formulario y confirmando la acción.
     *
     * @param dimensionName  Dimensión principal a seleccionar.
     * @param associatedName Nombre de la nueva dimensión asociada.
     * @param defaultVal     Valor por defecto a asignar (opcional).
     */
    public void createAssociatedDimension(String dimensionName, String associatedName, String defaultVal) {
        clickButtonByName("Nuevo");
        selectDimension(dimensionName);
        enterAssociatedDimension(associatedName);
        if (defaultVal != null && !defaultVal.isEmpty()) {
            enterDefaultValue(defaultVal);
        }
        clickButtonByName("Aceptar");
    }

    /**
     * Verifica si el formulario para añadir una nueva dimensión asociada está desplegado.
     *
     * @return true si todos los campos requeridos son visibles, false en caso contrario.
     */
    public boolean isNewAssociatedDimensionFormDisplayed() {
        try {
            waitUtil.findVisibleElement(dimensionDropdown);
            waitUtil.findVisibleElement(associatedInput);
            waitUtil.findVisibleElement(defaultInput);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Espera a que se cierre el diálogo de "Añadir nueva dimensión asociada".
     * Utilizado tras confirmar o cancelar la creación/edición.
     */
    public void waitForTheWindowToClose() {
        WebElement element = waitUtil.findVisibleElement(tituloDialogo);
        waitUtil.waitForInvisibility(element, "Título del diálogo");
    }

    /**
     * Limpia el contenido actual del campo "Valor por defecto".
     * Elimina cualquier texto ingresado previamente y espera a que el campo quede vacío.
     */
    public void clearsTheDefaultValueField() {
        WebElement input = waitUtil.waitForVisibilityByLocator(defaultValueInput);
        // Selecciona el texto y lo borra
        safeClear(input, "Valor por defecto");

        // Espera a que el campo quede vacío
        waitUtil.waitForInputToBeEmpty(input);
    }

    /**
     * Modifica el valor por defecto de una dimensión asociada existente.
     *
     * @param column          Nombre de la columna que se desea modificar.
     * @param screen          Pantalla objetivo (contextual).
     * @param originalRecord  Valor original antes de la edición.
     * @param newRecord       Nuevo valor a ingresar.
     */
    public void changeTheDefaultValue(String column, String screen, String originalRecord, String newRecord) {
        modifyRecord(column, screen, originalRecord, newRecord);
    }

    /**
     * Modifica el nombre de una dimensión asociada existente.
     *
     * @param column          Nombre de la columna a editar.
     * @param screen          Pantalla objetivo (contextual).
     * @param originalRecord  Nombre actual de la dimensión asociada.
     * @param newRecord       Nuevo nombre para la dimensión asociada.
     */
    public void modifyTheNameOfTheDimension(String column, String screen, String originalRecord, String newRecord) {
        modifyRecord(column, screen, originalRecord, newRecord);
    }

    /**
     * Selecciona una dimensión asociada editada en la tabla.
     *
     * @param value Valor exacto que representa la dimensión editada en la tabla.
     */
    public void selectTheEditedDimension(String value) {
        clickByLocator(tableUtil.buildRecordLocator(value), value);
    }

    /**
     * Verifica que una dimensión asociada editada no esté presente en la tabla.
     *
     * @param primaryDimensionName     Nombre de la dimensión principal.
     * @param editedAssociatedDimension Nombre editado de la dimensión asociada.
     * @param defaultValueEdited       Valor por defecto esperado tras la edición.
     */
    public void editedDimensionNotShown(String primaryDimensionName, String editedAssociatedDimension, String defaultValueEdited) {
        boolean exists = !getTableColumnHeaders().isEmpty()
                && !isAssociatedDimensionRowPresent(primaryDimensionName, editedAssociatedDimension, defaultValueEdited);
        Assert.assertFalse("La dimensión asociada '" + editedAssociatedDimension + "' todavía está presente para la dimensión '" + primaryDimensionName + "'", exists);
    }

    /**
     * Garantiza que una dimensión principal tenga exactamente 10 dimensiones asociadas.
     * Si hay menos de 10, crea las que faltan automáticamente.
     *
     * @param tablePosition           Posición de la tabla en la página.
     * @param associatedDimensionName Base del nombre de la dimensión asociada.
     * @param defaultValue            Base del valor por defecto.
     * @param primaryDimensionName    Dimensión principal a asociar.
     */
    public void ensureTenAssociatedDimensionsExist(String associatedDimensionName, String defaultValue, String primaryDimensionName) {
        // Crear hasta 10 dimensiones asociadas para la dimensión principal dada.
        int current = tableUtil.getRenderedRowCount("Gestor de dimensiones asociadas");
        for (int i = current + 1; i <= 10; i++) {
            String assocName = associatedDimensionName + i;
            String defaultVal = defaultValue + i;
            createAssociatedDimension(primaryDimensionName, assocName, defaultVal);
            waitForTheWindowToClose();
        }
        // Verificación opcional: comprobar que la décima asociación existe en la tabla.
        Assert.assertTrue("No se logró crear 10 dimensiones asociadas para la dimensión " + primaryDimensionName,
                isAssociatedDimensionRowPresent(primaryDimensionName, associatedDimensionName + "10", defaultValue + "10"));
    }

    /**
     * Ingresa un nombre en el campo de "Dimensión asociada" para una dimensión extra (fuera del límite).
     *
     * @param associatedName Nombre de la dimensión adicional a ingresar.
     */
    public void enterNameOfAnExtraDimension(String associatedName) {
        sendKeysByLocator(associatedInput, associatedName, "Dimensión asociada");
    }

    /**
     * Verifica que la dimensión asociada extra no esté presente en la tabla de resultados.
     *
     * @param primaryDimensionName     Dimensión principal.
     * @param extraAssociatedDimension Nombre de la dimensión adicional.
     * @param extraDefaultValue        Valor por defecto de la dimensión adicional.
     */
    public void ExtraDimensionNotShown(String primaryDimensionName, String extraAssociatedDimension, String extraDefaultValue) {
        boolean exists = !getTableColumnHeaders().isEmpty()
                && !isAssociatedDimensionRowPresent(primaryDimensionName, extraAssociatedDimension, extraDefaultValue);
        Assert.assertFalse("La dimensión asociada '" + extraAssociatedDimension + "' todavía está presente para la dimensión '" + primaryDimensionName + "'", exists);
    }

    /**
     * Verifica que los encabezados de columna en la tabla coincidan con los esperados.
     *
     * @param expectedColumns Tabla de Cucumber con los encabezados esperados.
     */
    public void verifyTableColumns(DataTable expectedColumns) {
        List<String> expectedHeaders = expectedColumns.asList();
        List<String> actualHeaders = getTableColumnHeaders();
        Assert.assertEquals("Los encabezados de la tabla no coinciden con los esperados",
                expectedHeaders, actualHeaders);
    }

    /**
     * Verifica que se muestren correctamente los campos del formulario de creación de dimensión asociada.
     */
    public void verifyCreationFormFields() {
        validationUtil.assertTrue(isNewAssociatedDimensionFormDisplayed(), "El formulario de creación no muestra los campos esperados");
    }

    /**
     * Verifica que ya exista una dimensión asociada con los valores indicados.
     *
     * @param primaryDimensionName Nombre de la dimensión principal.
     * @param associatedDimensionName Nombre de la dimensión asociada.
     * @param defaultValue Valor por defecto esperado.
     */
    public void givenAssociatedDimensionExists(String primaryDimensionName, String associatedDimensionName, String defaultValue) {
        validationUtil.assertTrue(isAssociatedDimensionRowPresent(primaryDimensionName, associatedDimensionName, defaultValue), "No existe la dimensión asociada");
    }

    /**
     * Verifica que una fila específica esté presente en la tabla.
     *
     * @param primaryDimensionName     Dimensión principal.
     * @param associatedDimensionName  Dimensión asociada.
     * @param defaultValue             Valor por defecto.
     */
    public void verifyTableRowPresent(String primaryDimensionName, String associatedDimensionName, String defaultValue) {
        validationUtil.assertTrue(isAssociatedDimensionRowPresent(primaryDimensionName, associatedDimensionName, defaultValue), "No se encontró en la tabla la fila con Dimensión '" + primaryDimensionName + "', Dimensión asociada '" + associatedDimensionName + "', Valor por defecto '" + defaultValue + "'");
    }

    /**
     * Verifica que la dimensión asociada editada esté visible con los valores actualizados.
     *
     * @param primaryDimensionName     Dimensión principal.
     * @param editedAssociatedDimension Nombre actualizado de la dimensión asociada.
     * @param defaultValueEdited       Valor por defecto actualizado.
     */
    public void dimensionEditedShows(String primaryDimensionName, String editedAssociatedDimension, String defaultValueEdited) {
        validationUtil.assertTrue(isAssociatedDimensionRowPresent(primaryDimensionName, editedAssociatedDimension, defaultValueEdited), "No se encontró en la tabla la fila con Dimensión '" + primaryDimensionName + "', Dimensión asociada '" + editedAssociatedDimension + "', Valor por defecto '" + defaultValueEdited + "'");
    }

    /**
     * Verifica que exista una dimensión asociada editada con los valores proporcionados.
     *
     * @param primaryDimensionName     Dimensión principal.
     * @param editedAssociatedDimension Nombre actualizado.
     * @param defaultValueEdited       Valor por defecto actualizado.
     */
    public void theEditedAssociatedDimensionExists(String primaryDimensionName, String editedAssociatedDimension, String defaultValueEdited) {
        validationUtil.assertTrue(isAssociatedDimensionRowPresent(primaryDimensionName, editedAssociatedDimension, defaultValueEdited), "No existe la dimensión asociada");
    }
}