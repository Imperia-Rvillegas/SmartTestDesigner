package ui.pages;

import config.EnvironmentConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.LogUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Página que modela la gestión de orígenes de importación.
 * <p>
 * Esta clase permite automatizar la interacción con la interfaz de usuario
 * para crear, configurar y validar orígenes de importación de datos.
 * Se implementa el patrón Page Object para facilitar el mantenimiento y
 * la reutilización de código en pruebas automatizadas.
 * </p>
 */
public class OriginsPage extends BasePage {

    /**
     * Localizador para el desplegable de tipo de origen.
     */
    private final By typeDropdownLocator = By.cssSelector("p-dropdown[formcontrolname='Type'] .p-dropdown-trigger");

    /**
     * Localizador para el campo de entrada de nombre del origen.
     */
    private final By nameInputLocator = By.xpath("//input[@formcontrolname='Name']");

    /**
     * Localizador para el input de carga de archivos.
     */
    private final By fileInputLocator = By.xpath("//div[contains(@class,'input-file-form-container')]//input[@type='file']");

    /**
     * Localizador para el mensaje de confirmación de guardado exitoso.
     */
    private final By uploadSuccessLocator = By.xpath("//span[normalize-space(.)='Guardado']");

    /**
     * Constructor de la página de orígenes.
     *
     * @param driver      instancia de {@link WebDriver} utilizada para interactuar con la aplicación.
     * @param pageManager gestor de páginas que administra la navegación entre diferentes módulos.
     */
    public OriginsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Crea un nuevo origen seleccionando el tipo de importación y asignando un nombre.
     * <p>
     * Realiza las siguientes acciones:
     * <ul>
     *     <li>Hace clic en el botón "Nuevo".</li>
     *     <li>Introduce el nombre del origen.</li>
     *     <li>Selecciona el tipo de importación desde el desplegable.</li>
     *     <li>Confirma la creación con el botón "Aceptar".</li>
     * </ul>
     * </p>
     *
     * @param type nombre visible del tipo de origen (por ejemplo, "Excel").
     * @param name nombre del origen que se registrará.
     */
    public void createNewOrigin(String type, String name) {
        LogUtil.start("Creación de un nuevo origen de importación");
        clickButtonByName("Nuevo");
        sendKeysByLocatorWithVerification(nameInputLocator, name, "Nombre del origen");
        waitUtil.isElementVisible(typeDropdownLocator, 10000, 500);
        clickByLocator(typeDropdownLocator, "Selector de tipo de carga");
        clickDropdownOptionByText(type);
        clickButtonByName("Aceptar");
    }

    /**
     * Carga el fichero proporcionado para el origen en edición y espera a que el sistema confirme la operación.
     * <p>
     * El archivo debe estar ubicado en:
     * <code>src/test/resources/testdata/imports</code>
     * </p>
     *
     * @param fileName nombre del fichero a cargar.
     */
    public void uploadFileForOrigin(String fileName) {
        Path filePath = resolveImportFile(fileName);
        validationUtil.assertTrue(Files.exists(filePath), "El fichero a importar existe: " + filePath);
        WebElement input = waitUtil.waitForPresenceOfElement(fileInputLocator);
        LogUtil.info("Enviando fichero de importación: " + filePath);
        input.sendKeys(filePath.toString());
        clickButtonByName("Guardar");
        boolean uploadCompleted = waitUtil.isElementVisible(uploadSuccessLocator, 60000, 500);
        validationUtil.assertTrue(uploadCompleted, "El fichero se cargó correctamente para el origen");
    }

    /**
     * Verifica que el origen recién creado aparezca en la tabla de orígenes disponibles.
     * <p>
     * Espera a que la tabla se cargue completamente antes de validar la existencia del registro.
     * </p>
     *
     * @param originName nombre del origen que debe estar listado.
     */
    public void verifyOriginCreated(String originName) {
        waitUtil.waitForTableToLoadCompletely();
        validationUtil.assertRecordListed(originName);
    }

    /**
     * Resuelve la ruta absoluta del archivo de importación ubicado en el directorio de testdata.
     *
     * @param fileName nombre del fichero a importar.
     * @return ruta absoluta al fichero de importación.
     */
    private Path resolveImportFile(String fileName) {
        return Paths.get("src", "test", "resources", "testdata", "imports", fileName).toAbsolutePath();
    }
}
