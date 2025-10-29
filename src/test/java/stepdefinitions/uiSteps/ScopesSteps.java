package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import ui.manager.PageManager;
import ui.pages.ScopesPage;

import java.util.List;
import java.util.Map;

/**
 * Steps de Cucumber para automatizar el flujo de importación de orígenes y ámbitos.
 * <p>
 * Esta clase implementa los pasos definidos en los escenarios de prueba que
 * permiten abrir un ámbito, asignar un origen de importación, mapear columnas
 * y verificar el resultado de la carga de datos.
 * </p>
 */
public class ScopesSteps {

    /**
     * Manejador de páginas que permite acceder a las diferentes vistas de la aplicación.
     */
    private final PageManager pageManager = Hooks.getPageManager();

    /**
     * Página de ámbitos utilizada para interactuar con el módulo de importación de ámbitos.
     */
    private final ScopesPage scopesPage = pageManager.getScopesPage();

    /**
     * Paso de Cucumber que abre un ámbito específico en el módulo de carga.
     *
     * @param ambito nombre del ámbito a abrir.
     */
    @And("abre el ámbito de carga {string}")
    public void openScope(String ambito) {
        scopesPage.openScope(ambito);
    }

    /**
     * Paso de Cucumber que selecciona el origen de importación previamente creado
     * y lo asocia al ámbito actual.
     */
    @And("selecciona el origen {string}")
    public void selectOriginForScope(String origin) {
        scopesPage.selectOrigin(origin);
    }

    /**
     * Paso de Cucumber que mapea las columnas del ámbito con los valores proporcionados.
     * <p>
     * El mapeo se recibe en una tabla de datos de Cucumber, que se transforma en
     * una lista de mapas clave-valor para aplicar el mapeo dentro de la interfaz.
     * </p>
     *
     * @param dataTable tabla de datos que contiene el mapeo de columnas y valores.
     */
    @And("mapea las columnas del ámbito con los valores:")
    public void mapColumns(DataTable dataTable) {
        List<Map<String, String>> mappings = dataTable.asMaps(String.class, String.class);
        scopesPage.mapColumns(mappings);
    }

    /**
     * Paso de Cucumber que inicia la carga de datos para el ámbito actual.
     */
    @And("inicia la carga de datos del ámbito")
    public void startLoadScope() {
        scopesPage.startImport();
    }

    /**
     * Paso de Cucumber que valida que la carga de datos del ámbito
     * finalice en estado exitoso.
     */
    @Then("la carga finaliza en estado exitoso para el ámbito")
    public void verifyUploadSuccessful() {
        scopesPage.verifyImportSucceeded();
    }
}