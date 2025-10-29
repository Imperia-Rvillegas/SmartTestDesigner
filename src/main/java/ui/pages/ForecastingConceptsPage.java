package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Página de objetos (Page Object) correspondiente a la pantalla de Conceptos de Previsión
 * en la interfaz de usuario del sistema.
 * <p>
 * Contiene métodos para crear, seleccionar, validar la creación y eliminación
 * de conceptos de previsión, utilizando Selenium WebDriver.
 * </p>
 *
 * Esta clase hereda de {@link BasePage}, lo cual permite reutilizar métodos
 * de interacción comunes como `clickButtonByName`, `sendKeysByLocator`, etc.
 *
 * @author
 */
public class ForecastingConceptsPage extends BasePage {

    /** Localizador del campo de texto para ingresar el nombre del nuevo concepto */
    By inputFieldConceptNameLocator = By.xpath("//div[contains(@class,'form-control-input')]//input[@type='text']");

    /** Elemento Web correspondiente al nuevo concepto creado, usado para su posterior selección o validación */
    WebElement newConceptElement;

    /**
     * Constructor de la clase.
     *
     * @param driver       instancia del WebDriver
     * @param pageManager  gestor centralizado de páginas (PageManager)
     */
    public ForecastingConceptsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Crea un nuevo concepto de previsión en la interfaz.
     * <p>
     * Este metodo hace clic en el botón "Nuevo", ingresa el nombre proporcionado en el campo de texto,
     * y luego presiona el botón "Aceptar" para guardar el nuevo concepto.
     * </p>
     *
     * @param newConceptName nombre del nuevo concepto a crear
     */
    public void createsANewConcept(String newConceptName) {
        // Hace clic en el boton Nuevo
        clickButtonByName("Nuevo");

        // Ingresa el nombre del concepto
        sendKeysByLocator(inputFieldConceptNameLocator, newConceptName, "Nombre del nuevo concepto");

        // Presiona boton aceptar
        clickButtonByName("Aceptar");
    }

    /**
     * Valida que el concepto fue creado correctamente y aparece listado en la tabla.
     *
     * @param newConceptName nombre del concepto que se espera que esté listado
     */
    public void validatesThatTheConceptWasCreated(String newConceptName) {
        // Validar que el concepoto se crea
        validationUtil.assertRecordListed(newConceptName);
    }

    /**
     * Selecciona un concepto de previsión existente en la tabla.
     * <p>
     * Utiliza el nombre del concepto para localizar su celda correspondiente
     * y realiza clic sobre ella.
     * </p>
     *
     * @param conceptName nombre del concepto a seleccionar
     */
    public void selectConcept(String conceptName) {
        //Obtener localizador de la celda por el nombre
        By cellLocator = tableUtil.buildRecordLocator(conceptName);

        //Obtener el elemento web
        newConceptElement = waitUtil.findVisibleElement(cellLocator);

        // Hace clic sobre la celda
        clickByElement(newConceptElement, "Celda del concepto");
    }

    /**
     * Valida que el concepto fue eliminado correctamente de la tabla.
     * <p>
     * Espera a que el elemento correspondiente al concepto creado previamente
     * ya no sea visible en la interfaz.
     * </p>
     */
    public void validatesThatTheConceptWasEliminated() {
        waitUtil.waitForInvisibility(newConceptElement, "Celda del concepto nuevo");
    }
}