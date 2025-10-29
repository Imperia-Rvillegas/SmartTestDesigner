package ui.pages;

import config.ScenarioContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;

import java.io.File;

/**
 * Clase que representa la página de gestión de artículos en la aplicación web.
 * Proporciona métodos para interactuar con los elementos de la página, como crear,
 * buscar, validar, ajustar columnas y seleccionar artículos.
 */
public class ArticlesPage extends BasePage {

    private final By codeInputLocator = By.cssSelector("input[formcontrolname='Code']");
    private final By descriptionInputLocator = By.cssSelector("input[formcontrolname='Description']");
    private final By baseUnitLookupButtonLocator = By.cssSelector("imperia-input-table[formcontrolname='IdBaseUnit'] i.pi.pi-ellipsis-h");
    private final By firstRowLocator = By.cssSelector("cdk-row.cdk-row.imperia-table-body-row:first-of-type");
    private final By messageSaveLocator = By.xpath("//span[normalize-space(text())='Guardado']");
    private final By locatorSearch = By.cssSelector("div.input-container.expanded input[type='text']");

    /**
     * Constructor de la clase ArticlesPage.
     *
     * @param driver      instancia de WebDriver.
     * @param pageManager instancia de PageManager para acceder a utilidades compartidas.
     */
    public ArticlesPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Llena los campos obligatorios del formulario de creación de artículo.
     *
     * @param code        código del artículo.
     * @param description descripción del artículo.
     */
    public void fillMandatoryFields(String code, String description) {
        enterCode(code);
        enterDescription(description);
    }

    /**
     * Ingresa el valor proporcionado en el campo "Código" del formulario de artículo.
     *
     * <p>Este metodo encapsula la acción de escribir en el campo de código,
     * útil para pruebas donde se requiere completar el identificador del artículo.</p>
     *
     * @param code valor que se desea ingresar en el campo "Código".
     */
    public void enterCode(String code) {
        sendKeysByLocator(codeInputLocator, code, "Código");
    }

    /**
     * Ingresa el valor proporcionado en el campo "Descripción" del formulario de artículo.
     *
     * <p>Este metodo permite completar la descripción del artículo,
     * necesaria para la creación o edición del registro.</p>
     *
     * @param description texto que se desea ingresar en el campo "Descripción".
     */
    public void enterDescription(String description) {
        sendKeysByLocator(descriptionInputLocator, description, "Descripción");
    }

    /**
     * Selecciona la primera unidad disponible en el listado de unidades base.
     *
     * <p>Este metodo abre el selector de unidades, elige la primera unidad disponible en la tabla
     * y confirma la selección haciendo clic en "Aceptar". Es útil para cumplir con el campo obligatorio
     * "Unidad base" en formularios de artículos.</p>
     */
    public void selectUnit() {
        clickByLocator(baseUnitLookupButtonLocator, "Lista de Unidades");
        clickByLocator(firstRowLocator, "Primera unidad de la tabla");
        clickButtonByName("Aceptar");
    }

    /**
     * Valida que el mensaje "Guardado" aparezca después de guardar un artículo.
     */
    public void validateSaved() {
        boolean exists = waitUtil.isElementVisible(messageSaveLocator, 80000, 500);
        validationUtil.assertTrue(exists, "Boton Guardar cambia a Guardado");
    }

    /**
     * Verifica que un artículo con el valor dado no esté listado en la tabla.
     *
     * @param article valor del artículo que no debería estar presente.
     */
    public void assertArticleNotListed(String article) {
        By articleLocator = By.xpath("//td[.//span[normalize-space(text())='" + article + "']]");
        waitUtil.waitForInvisibility(articleLocator, "Articulo", 20000, 500);
    }

    /**
     * Selecciona un artículo en una tabla utilizando su texto registrado como criterio de búsqueda.
     *
     * <p>Este metodo construye dinámicamente un localizador basado en el texto registrado
     * del artículo, y realiza un clic sobre la fila o celda correspondiente.</p>
     *
     * <p>Se utiliza principalmente en tablas HTML interactivas donde se debe seleccionar
     * un registro específico identificado por su nombre, código o descripción.</p>
     *
     * @param article Texto visible en la fila del artículo que se desea seleccionar.
     */
    public void selectArticle(String article) {
        clickByLocator(tableUtil.buildRecordLocator(article), article);
    }

    /**
     * Realiza la búsqueda de un artículo en la tabla según el valor y la columna especificada.
     *
     * @param record        Valor del artículo a buscar (por ejemplo, descripción o código).
     * @param column        Nombre de la columna por la cual se realizará la búsqueda (por ejemplo, "Descripción").
     * @param tablePosition Posición de la tabla en la pantalla si hay más de una (común en tablas dinámicas o modulares).
     */
    public void searchTheArticleCreated(String record) {
        searchRecord(record, locatorSearch);
    }

    /**
     * Valida que el artículo especificado aparece listado en los resultados de búsqueda.
     *
     * @param article Descripción o código del artículo que se espera esté presente en la lista.
     */
    public void ArticleAppearsInResults(String article) {
        validationUtil.assertRecordListed(article);
    }

    /**
     * Valida que el artículo especificado por código aparece en los resultados filtrados.
     *
     * @param article Código del artículo que se espera esté presente en la tabla.
     */
    public void codeAppearsInResults(String article) {
        validationUtil.assertRecordListed(article);
    }

    /**
     * Aplica un filtro por descripción utilizando el nombre de columna y el valor del artículo.
     *
     * @param filter  Nombre de la columna a filtrar (debería ser "Descripción").
     * @param article Descripción del artículo a usar como valor del filtro.
     */
    public void applyFilterByDescription(String filter, String article) {
        filterBy(filter, article);
    }

    /**
     * Valida que el artículo filtrado por descripción aparece correctamente en los resultados.
     *
     * @param article Descripción del artículo que se espera esté listado tras aplicar el filtro.
     */
    public void articleFilteredByDescriptionAppears(String article) {
        validationUtil.assertRecordListed(article);
    }

    /**
     * Valida que la pantalla actual coincide con el nombre esperado.
     *
     * @param screenName Nombre de la pantalla que se espera esté visible.
     */
    public void validateDisplayedScreen(String screenName) {
        validationUtil.assertCurrentScreen(screenName);
    }

    /**
     * Selecciona la unidad previamente creada desde la lista de unidades y la establece como unidad base.
     * <p>
     * Este metodo realiza los siguientes pasos de forma secuencial:
     * <ol>
     *   <li>Abre el selector de unidades haciendo clic en el botón de búsqueda.</li>
     *   <li>Busca la celda que contiene el nombre exacto de la unidad proporcionada.</li>
     *   <li>Hace clic sobre la unidad encontrada para seleccionarla.</li>
     *   <li>Confirma la selección pulsando el botón "Aceptar".</li>
     * </ol>
     *
     * @param unitNameToAssociateWithTheArticle el nombre de la unidad previamente creada que se desea asociar como unidad base.
     */
    public void selectTheCreatedUnit(String unitNameToAssociateWithTheArticle) {
        clickByLocator(baseUnitLookupButtonLocator, "Lista de Unidades");
        By cellLocator = By.xpath("//span[normalize-space(text())='" + unitNameToAssociateWithTheArticle + "']");
        clickByLocator(cellLocator, "Unidad " + unitNameToAssociateWithTheArticle);
        clickButtonByName("Aceptar");
    }

    /**
     * Aplica el filtro por **Código** con el valor indicado.
     *
     * @param newArticleCode código del artículo a filtrar (valor exacto o patrón según implementación).
     * @see #applyFilterBy(String, String)
     */
    public void applyFilterByCode(String newArticleCode) {
        applyFilterBy("Código", newArticleCode);
    }
}