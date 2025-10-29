package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ui.manager.PageManager;
import ui.pages.OriginsPage;

/**
 * Steps de Cucumber para automatizar el flujo de importación de orígenes y ámbitos.
 * <p>
 * Esta clase define los pasos de alto nivel usados en las pruebas BDD con Cucumber,
 * que simulan la interacción de un usuario con la interfaz de importación de orígenes.
 * </p>
 */
public class OriginsSteps {

    /**
     * Manejador de páginas para acceder a las diferentes pantallas de la aplicación.
     */
    private final PageManager pageManager = Hooks.getPageManager();

    /**
     * Página de orígenes que encapsula las acciones específicas sobre
     * el módulo de importación de orígenes.
     */
    private final OriginsPage originsPage = pageManager.getOriginsPage();

    /**
     * Nombre del origen actualmente creado, utilizado para verificar
     * su disponibilidad en la lista.
     */
    private String currentOriginName;

    /**
     * Paso de Cucumber que simula la creación de un nuevo origen de importación.
     *
     * @param tipo   el tipo de origen de importación (por ejemplo: CSV, XML, etc.)
     * @param nombre el nombre que se asignará al nuevo origen
     */
    @When("crea un nuevo origen de importación tipo {string} con nombre {string}")
    public void createNewSource(String tipo, String nombre) {
        currentOriginName = nombre;
        originsPage.createNewOrigin(tipo, nombre);
    }

    /**
     * Paso de Cucumber que carga un archivo asociado al origen de importación.
     *
     * @param archivo nombre del archivo que se subirá
     */
    @And("carga el archivo {string} para el origen")
    public void loadFileToSource(String archivo) {
        originsPage.uploadFileForOrigin(archivo);
    }

    /**
     * Paso de Cucumber que verifica que el origen creado está disponible
     * en la lista de orígenes de importación.
     */
    @Then("el origen de importación queda disponible en la lista")
    public void checkAvailableOrigin() {
        originsPage.verifyOriginCreated(currentOriginName);
    }
}
