package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Página de automatización correspondiente a la pantalla de Dimensiones de Negocio.
 *
 * <p>Proporciona métodos para interactuar con los campos del formulario y con la tabla
 * que muestra las dimensiones existentes, así como para realizar validaciones y operaciones
 * como creación y eliminación.</p>
 */
public class BusinessDimensionsPage extends BasePage {

    private final By nameInput = By.cssSelector("input[formcontrolname='Name']");
    private final By defaultValueInput = By.cssSelector("input[formcontrolname='DefaultValue']");

    /**
     * Constructor de la clase que inicializa la página con el WebDriver y el PageManager.
     *
     * @param driver       instancia del WebDriver.
     * @param pageManager  instancia del PageManager que gestiona las páginas y utilidades.
     */
    public BusinessDimensionsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Completa el campo "Nombre" con el valor proporcionado.
     *
     * @param name el nombre que se desea ingresar.
     */
    public void fillName(String name) {
        sendKeysByLocator(nameInput, name, "Nombre");
    }

    /**
     * Completa el campo "Valor por defecto" con el texto especificado.
     *
     * @param value el valor por defecto que se desea ingresar.
     */
    public void fillDefaultValue(String value) {
        sendKeysByLocator(defaultValueInput, value, "Valor por defecto");
    }

    /**
     * Limpia el campo "Valor por defecto", esperando hasta que esté vacío.
     */
    public void clearDefaultValue() {
        WebElement input = waitUtil.waitForVisibilityByLocator(defaultValueInput);
        // Selecciona el texto y lo borra
        safeClear(input, "Valor por defecto");

        // Espera a que el campo quede vacío
        waitUtil.waitForInputToBeEmpty(input);
    }

    /**
     * Valida que una dimensión con el nombre especificado esté visible en la tabla.
     *
     * @param name el nombre de la dimensión que se espera que esté presente.
     */
    public void validateDimensionPresent(String name) {
        waitUtil.waitForVisibilityByLocator(tableUtil.buildRecordLocator(name));
    }

    /**
     * Valida que una dimensión con el nombre especificado ya no esté presente en la tabla.
     *
     * @param name el nombre de la dimensión que se espera que haya desaparecido.
     */
    public void validateDimensionNotPresent(String name) {
        waitUtil.waitForInvisibility(tableUtil.buildRecordLocator(name), "Dimension eliminada", 10000, 500);
    }

    /**
     * Elimina una dimensión de negocio seleccionándola por nombre y confirmando las ventanas de diálogo.
     *
     * @param name el nombre de la dimensión que se desea eliminar.
     */
    public void deleteDimension(String name) {
        clickByLocator(tableUtil.buildRecordLocator(name), name);
        clickButtonByName("Eliminar");
        clickButtonByName("Aceptar"); // Confirmación 1
        clickButtonByName("Aceptar"); // Confirmación 2
    }

    /**
     * Crea dimensiones de negocio hasta alcanzar el límite de 3, si es que no existen aún.
     */
    public void ensureMaxThreeDimensionsExist(String nameOfNewBusinessDimension, String defaultValueOfNewBusinessDimension) {
        int current = tableUtil.getRenderedRowCount("Gestor de dimensiones de negocio");
        for (int i = current + 1; i <= 3; i++) {
            clickButtonByName("Nuevo");
            fillName(nameOfNewBusinessDimension + i);
            fillDefaultValue(defaultValueOfNewBusinessDimension + i);
            clickButtonByName("Aceptar");
            waitUtil.sleepMillis(300, "Espera antes de continuar con la creacion de otra dimension de negocio");
        }
    }

    /**
     * Intenta crear una dimensión adicional cuando ya se alcanzó el límite permitido.
     * Esto puede ser usado para validar la aparición de mensajes de advertencia o restricciones de negocio.
     */
    public void crearDimensionExcedente() {
        clickButtonByName("Nuevo");
    }

    /**
     * Modifica el valor por defecto de una dimensión de negocio ya existente.
     *
     * <p>Este metodo actúa como envoltorio de {@code modifyRecord}, permitiendo reutilizar
     * la lógica de edición en tabla mediante parámetros semánticos específicos de la pantalla.
     *
     * @param column                         Nombre de la columna a modificar (ej. "Valor por defecto").
     * @param screen                         Nombre de la pantalla en la que se realiza la acción (ej. "Dimensión de negocio").
     * @param defaultValueOfNewBusinessDimension Valor original actual del campo.
     * @param editedDefaultValue            Nuevo valor a ingresar en el campo editable.
     */
    public void modifiesDefaultValueOfDimension(String column, String screen, String defaultValueOfNewBusinessDimension, String editedDefaultValue) {
        modifyRecord(column, screen, defaultValueOfNewBusinessDimension, editedDefaultValue);
    }

    /**
     * Modifica el nombre de una dimensión de negocio existente en la tabla.
     *
     * @param column                    Nombre de la columna a modificar (ej. "Nombre").
     * @param screen                    Nombre de la pantalla objetivo (ej. "Dimensiones de negocio").
     * @param nameOfNewBusinessDimension Nombre original de la dimensión de negocio.
     * @param businessDimensionNameEdited Nuevo nombre que se desea asignar.
     */
    public void modifyDimensionName(String column, String screen, String nameOfNewBusinessDimension, String businessDimensionNameEdited) {
        modifyRecord(column, screen, nameOfNewBusinessDimension, businessDimensionNameEdited);
    }

    /**
     * Verifica que el nombre editado de una dimensión de negocio está presente en la tabla.
     *
     * @param businessDimensionNameEdited Nombre editado que se espera encontrar en la tabla.
     */
    public void EditedBusinessDimensionNameIsShown(String businessDimensionNameEdited) {
        validateDimensionPresent(businessDimensionNameEdited);
    }

    /**
     * Verifica que un valor editado (como el valor por defecto) esté presente en la tabla de dimensiones.
     *
     * @param editedDefaultValue Valor que se espera encontrar en la tabla.
     */
    public void validateValuePresent(String editedDefaultValue) {
        validateDimensionPresent(editedDefaultValue);
    }
}