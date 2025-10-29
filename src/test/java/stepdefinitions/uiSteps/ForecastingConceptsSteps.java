package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import testmodel.forecastingConceptsData;
import ui.manager.PageManager;
import ui.pages.ForecastingConceptsPage;
import ui.utils.TestDataLoader;

/**
 * Clase que define los pasos de prueba relacionados con los conceptos de previsión (Forecasting Concepts),
 * utilizando anotaciones de Cucumber para ejecutar pruebas automatizadas de interfaz de usuario.
 * <p>
 * Esta clase accede a la página {@link ForecastingConceptsPage} a través del {@link PageManager},
 * y utiliza los datos cargados desde un archivo YAML mediante {@link TestDataLoader}.
 * </p>
 *
 */
public class ForecastingConceptsSteps {

    /** Gestor centralizado de páginas para obtener instancias de Page Object */
    private final PageManager pageManager = Hooks.getPageManager();

    /** Página de conceptos de previsión */
    private final ForecastingConceptsPage forecastingConceptsPage = pageManager.getForecastingConceptsPage();

    /** Nombre del nuevo concepto de previsión a utilizar durante las pruebas */
    private final String newConceptName;

    /**
     * Constructor que inicializa los datos del concepto de previsión
     * leyendo el archivo de datos YAML correspondiente.
     */
    public ForecastingConceptsSteps() {
        forecastingConceptsData forecastingConceptsData = TestDataLoader.load("testdata/page/forecastingConcepts.yaml", forecastingConceptsData.class);
        this.newConceptName = forecastingConceptsData.getNewConceptName();
    }

    /**
     * Paso de Cucumber que crea un nuevo concepto de previsión en la interfaz.
     * Utiliza el nombre definido en el archivo YAML.
     */
    @When("crea un nuevo concepto")
    public void createsANewConcept() {
        forecastingConceptsPage.createsANewConcept(newConceptName);
    }

    /**
     * Paso de Cucumber que valida que el nuevo concepto fue creado correctamente.
     */
    @Then("valida que el concepto fue creado")
    public void validatesThatTheConceptWasCreated() {
        forecastingConceptsPage.validatesThatTheConceptWasCreated(newConceptName);
    }

    /**
     * Paso de Cucumber que selecciona el nuevo concepto de previsión creado anteriormente.
     */
    @When("selecciona un concepto de previsiones nuevo")
    public void selectNewConcept() {
        forecastingConceptsPage.selectConcept(newConceptName);
    }

    /**
     * Paso de Cucumber que valida que el concepto de previsión fue eliminado correctamente.
     */
    @Then("valida que el concepto fue eliminado")
    public void validatesThatTheConceptWasEliminated() {
        forecastingConceptsPage.validatesThatTheConceptWasEliminated();
    }
}