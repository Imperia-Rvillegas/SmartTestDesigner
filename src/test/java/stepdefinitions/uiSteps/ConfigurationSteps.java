package stepdefinitions.uiSteps;

import config.ScenarioContext;
import hooks.Hooks;
import io.cucumber.java.en.*;
import ui.manager.PageManager;
import ui.pages.ConfigurationPage;

/**
 * Step Definitions para los escenarios de pruebas del módulo
 * **Configuraciones** en la interfaz de usuario.
 *
 * Esta clase encapsula los pasos necesarios para:
 * <ul>
 *   <li>Acceder al módulo Configuraciones desde el Home</li>
 *   <li>Ejecutar el cálculo de todos los elementos</li>
 *   <li>Aceptar la confirmación</li>
 *   <li>Validar el estado del semáforo como verde</li>
 * </ul>
 *
 * Utiliza el patrón PageManager para instanciar las páginas
 * y asegura el desacoplamiento entre pasos y elementos de UI.
 *
 * @author TuNombre
 */
public class ConfigurationSteps {

    /** Instancia de PageManager para acceder a páginas y utilidades comunes. */
    private final PageManager pageManager;

    /** Página específica del módulo Configuraciones. */
    private final ConfigurationPage configurationPage;

    /**
     * Constructor que inicializa PageManager y la página ConfigurationPage.
     *
     * @param scenarioContext contexto del escenario actual (si es necesario).
     */
    public ConfigurationSteps(ScenarioContext scenarioContext) {
        this.pageManager = Hooks.getPageManager();
        this.configurationPage = pageManager.getConfigurationPage();
    }

    /**
     * Paso que acepta la confirmación del cálculo en pantalla.
     */
    @And("acepta la confirmación del cálculo")
    public void confirmCalculation() {
        configurationPage.confirmCalculation();
    }
}