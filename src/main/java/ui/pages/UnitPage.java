package ui.pages;

import ui.utils.*;
import org.openqa.selenium.*;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Representa la página de gestión de unidades del sistema.
 * Permite crear, editar, eliminar unidades y validar su presencia en la tabla.
 */
public class UnitPage extends BasePage {

    // Localizadores
    private final By fieldNameLocator = By.xpath("//input[@formcontrolname='Name']");
    private final By fieldDescriptionLocator = By.xpath("//input[@formcontrolname='Description']");
    private final By fieldDecimalsLocator = By.xpath("//input[@inputmode='decimal']");
    private final By messageErrorDeleteLocator = By.xpath("//div[contains(@class, 'message-container') and contains(text(), 'Una de las unidades está asociada')]");
    private final By inputLocator = By.cssSelector("input.p-inputtext");


    /**
     * Constructor de la clase UnitPage.
     *
     * @param driver Instancia de WebDriver.
     */
    public UnitPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Completa el formulario con name, descripción y decimals.
     *
     * @param name     Nombre de la unidad.
     * @param description Descripción de la unidad.
     * @param decimals  Cantidad de decimals.
     */
    public void completeForm(String name, String description, String decimals) {
        sendKeysByLocator(fieldNameLocator, name, "Nombre");
        sendKeysByLocator(fieldDescriptionLocator, description, "Descripcion");
        sendKeysByLocator(fieldDecimalsLocator, decimals, "Decimales");
    }

    /**
     * Hace clic en el botón "Aceptar" para guardar la unidad.
     */
    public void clickAccept() {
        clickButtonByName("Aceptar");
    }

    /**
     * Construye un localizador XPath para el input dentro de la celda de la columna indicada de una unidad.
     *
     * @param nombreUnidad Nombre exacto de la unidad.
     * @param columnName Nombre de la columna de la tabla (valor de atributo data-field).
     * @return Localizador By del elemento <input> correspondiente.
     */
    public By buildLocatorByUnitName(String nombreUnidad, String columnName) {
        String xpath = "//tr[contains(@class, 'imperia-table-body-row')]" +
                "[.//td[@data-field='Name']//span[normalize-space(text())='" + nombreUnidad + "']]" +
                "//td[@data-field='" + columnName + "']";
        return By.xpath(xpath);
    }

    /**
     * Elimina el valor del campo editable en la celda correspondiente a una unidad y columna específica.
     *
     * Este metodo localiza la celda especificada por el nombre de la unidad y el nombre de la columna,
     * hace clic sobre ella para activar el modo de edición, y luego limpia el contenido del campo de entrada (<input>).
     *
     * @param unitName  Nombre exacto de la unidad que se desea editar.
     * @param columName Nombre de la columna en la que se encuentra el valor a eliminar (por ejemplo, "Name").
     */
    public void deleteTheNameValue(String unitName, String columName) {
        // Paso 1: Localizar y hacer clic en la celda del Nombre de la unidad
        WebElement nameCell = waitUtil.findVisibleElement(buildLocatorByUnitName(unitName, columName));
        clickByElement(nameCell, "Celda Nombre de unidad '" + unitName + "'");

        // Paso 2: Esperar y limpiar el campo <input>
        WebElement imputName = waitUtil.findVisibleElement(inputLocator);
        safeClear(imputName, "Celda Nombre de unidad"); // Manejo de excepciones incluido
//        clear(imputName, "Celda Nombre de unidad"); // Alternativa sin manejo de excepciones
    }

    /**
     * Selecciona una unidad haciendo clic sobre su celda de nombre.
     *
     * Este metodo localiza la celda de la columna "Name" que contiene exactamente el texto del nombre de la unidad
     * y hace clic sobre ella para seleccionarla.
     *
     * @param unitName Nombre exacto de la unidad que se desea seleccionar.
     */
    public void selectUnit(String unitName) {
        WebElement unitCell = waitUtil.findVisibleElement(buildLocatorByUnitName(unitName, "Name"));
        clickByElement(unitCell, "Celda Nombre de unidad '" + unitName + "'");
    }

    /**
     * Verifica que una unidad aparezca en la tabla por su nombre, sin importar en qué fila esté.
     * Este metodo busca entre todas las filas visibles actualmente renderizadas en el DOM.
     *
     * Incluye una espera explícita para garantizar que la tabla esté visible antes de buscar filas.
     *
     * Nota: Si se utiliza un componente de scroll virtual (como cdk-virtual-scroll-viewport),
     * este metodo solo funcionará con las filas que estén actualmente renderizadas.
     *
     * @param name Nombre de la unidad que debe aparecer en alguna fila de la tabla.
     */
    public void unitAppearsInTable(String name) {
        validationUtil.assertRecordListed(name);
    }

    /**
     * Verifica el mensaje de error si no se puede eliminar una unidad asociada.
     */
    public void messageUnitNotDeletable() {
        boolean visible = waitUtil.isElementVisible(messageErrorDeleteLocator, 10000, 500);
        validationUtil.assertTrue(visible, "Mensaje de error es visible");
    }

    /**
     * Valida que la descripción de una unidad específica sea exactamente "Unidad de prueba editada".
     *
     * Este metodo localiza la celda de la columna "Description" correspondiente a la unidad con el nombre proporcionado
     * y verifica que su texto visible coincida exactamente con el valor esperado.
     *
     * @param unitName Nombre exacto de la unidad cuya descripción se desea validar.
     */
    public void validateDescriptionOfUnit(String unitName, String editedDescription) {
        WebElement descriptionCell = waitUtil.findVisibleElement(buildLocatorByUnitName(unitName, "Description"));
        validationUtil.assertElementTextEquals(descriptionCell, editedDescription, "Celda descripcion");
    }

    /**
     * Valida que una unidad ya no esté visible en la tabla, confirmando que fue eliminada correctamente.
     *
     * Este metodo espera un breve período para permitir que la tabla se actualice tras la acción de eliminación,
     * y luego verifica que la celda con el nombre de la unidad ya no esté presente en el DOM.
     *
     * @param unitName Nombre exacto de la unidad que se espera que haya sido eliminada.
     */
    public void validateUnitWasDeleted(String unitName) {
        WebElement element = waitUtil.findVisibleElement(buildLocatorByUnitName(unitName, "Name"));
        try {
            waitUtil.waitForInvisibility(element, "Unidad" + unitName);
            LogUtil.info("Validación exitosa: la unidad '" + unitName + "' ya no está presente en la tabla.");
        } catch (TimeoutException e) {
            LogUtil.error("La unidad '" + unitName + "' todavía aparece en la tabla después de intentar eliminarla.", e);
            throw new AssertionError("La unidad '" + unitName + "' no fue eliminada correctamente.");
        }
    }

    /**
     * Ingresa el nombre de la nueva unidad en el campo titulado "Nombre".
     *
     * @param newUnitName Nombre que se desea asignar a la nueva unidad.
     */
    public void enterTheNameOfTheNewUnit(String newUnitName) {
        sendKeysByTitle("Nombre", newUnitName);
    }

    /**
     * Ingresa la descripción de la nueva unidad en el campo titulado "Descripción".
     *
     * @param descriptionOfTheNewUnit Descripción que se desea asignar a la unidad.
     */
    public void enterTheDescriptionOfTheNewUnit(String descriptionOfTheNewUnit) {
        sendKeysByTitle("Descripción", descriptionOfTheNewUnit);
    }

    /**
     * Ingresa el número de decimales permitido para la nueva unidad.
     *
     * @param numberOfDecimalPlacesInTheNewUnit Número de decimales válidos para la unidad.
     */
    public void enterNumberDecimalsNewUnit(String numberOfDecimalPlacesInTheNewUnit) {
        sendKeysByLocator(fieldDecimalsLocator, numberOfDecimalPlacesInTheNewUnit, "Número de decimales");
    }

    /**
     * Modifica la descripción de una unidad existente.
     *
     * @param column Nombre de la columna a modificar.
     * @param screen Nombre de la pantalla donde se realiza la modificación.
     * @param descriptionOfTheNewUnit Valor actual de la descripción.
     * @param editedDescription Nuevo valor que se desea asignar a la descripción.
     */
    public void modifyUnitDescription(String column, String screen, String descriptionOfTheNewUnit, String editedDescription) {
        modifyRecord(column, screen, descriptionOfTheNewUnit, editedDescription);
    }

    /**
     * Ingresa el nombre de una unidad con el mínimo número de decimales permitidos.
     *
     * @param unitNameWithMinimumDecimalPlaces Nombre de la unidad con decimales mínimos.
     */
    public void enterNewUnitNameMithMinimumDecimalPlace(String unitNameWithMinimumDecimalPlaces) {
        sendKeysByTitle("Nombre", unitNameWithMinimumDecimalPlaces);
    }

    /**
     * Ingresa el valor mínimo de decimales permitido para una unidad.
     *
     * @param minimumNumberOfDecimalsForUnit Valor mínimo de decimales permitidos.
     */
    public void enterTheMinimumDecimalPlacesAllowed(String minimumNumberOfDecimalsForUnit) {
        sendKeysByLocator(fieldDecimalsLocator, minimumNumberOfDecimalsForUnit, "Número minimo de decimales permitidos");
    }

    /**
     * Ingresa el nombre de una unidad con el máximo número de decimales permitidos.
     *
     * @param unitNameWithMaximumDecimalPlaces Nombre de la unidad con decimales máximos.
     */
    public void enterTheNameOfTheNewUnitWithaMaximumOfDecimalPlaces(String unitNameWithMaximumDecimalPlaces) {
        sendKeysByTitle("Nombre", unitNameWithMaximumDecimalPlaces);
    }

    /**
     * Ingresa el valor máximo de decimales permitido para una unidad.
     *
     * @param maximunNumberOfDecimalsForUnit Valor máximo de decimales permitidos.
     */
    public void enterTheMaximumDecimalPlacesAllowed(String maximunNumberOfDecimalsForUnit) {
        sendKeysByLocator(fieldDecimalsLocator, maximunNumberOfDecimalsForUnit, "Número maximo de decimales permitidos");
    }

    /**
     * Ingresa un valor no permitido de decimales para una unidad, con el fin de validar el comportamiento ante entradas inválidas.
     *
     * @param numberOfDecimalsNotAllowedInUnit Valor de decimales no permitido por la validación del sistema.
     */
    public void enterTheNumberOfDecimalPlacesNotAllowed(String numberOfDecimalsNotAllowedInUnit) {
        sendKeysByLocator(fieldDecimalsLocator, numberOfDecimalsNotAllowedInUnit, "Número de decimales no permitido");
    }

    /**
     * Verifica que una unidad con el nombre especificado aparezca visible en la tabla de unidades.
     * <p>
     * Este metodo inspecciona las filas visibles de la tabla localizada por {@code tableLocator},
     * y comprueba si alguna celda de la columna "Name" contiene el nombre exacto (ignorando mayúsculas/minúsculas)
     * de la unidad esperada.
     * </p>
     * <p>
     * Actualmente incluye una espera temporal de 300 milisegundos mediante {@code waitUtil.sleepMillis}
     * para dar tiempo a que la tabla cargue completamente. Se recomienda reemplazar esta espera por una
     * condición explícita robusta basada en visibilidad o estado de carga.
     * </p>
     *
     * @param name el nombre de la unidad que se espera encontrar en la tabla.
     */
    public void theUnitCreatedToBeAssociated(String name) {
        validationUtil.assertRecordListed(name);
    }

    /**
     * Ingresa una nueva descripción de unidad que será asociada al artículo.
     * <p>
     * Este metodo localiza el campo de texto titulado "Descripción" y escribe el valor proporcionado
     * en {@code unitDescriptionToAssociateWithTheArticle}, que representa la descripción de la unidad.
     * </p>
     *
     * @param unitDescriptionToAssociateWithTheArticle la descripción de la unidad que se desea asociar al artículo.
     */
    public void enterANewUnitDescriptionToAssociateWithTheArticle(String unitDescriptionToAssociateWithTheArticle) {
        sendKeysByTitle("Descripción", unitDescriptionToAssociateWithTheArticle);
    }
}
