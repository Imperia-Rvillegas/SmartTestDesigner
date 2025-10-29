package stepdefinitions.uiSteps;

import config.ScenarioContext;
import hooks.Hooks;
import io.cucumber.java.en.Then;
import ui.manager.PageManager;
import ui.pages.InventoryHealthPage;

/**
 * Clase que define los pasos de Cucumber relacionados con la página de
 * {@link InventoryHealthPage} en el contexto de pruebas de interfaz de usuario.
 * <p>
 * Esta clase actúa como capa intermedia entre los escenarios definidos en
 * Gherkin y las acciones que se ejecutan sobre la página de salud de inventario.
 * </p>
 */
public class InventoryHealthSteps {

    /**
     * Administrador de páginas que centraliza la creación y acceso a objetos Page.
     */
    private final PageManager pageManager;

    /**
     * Página de "Salud de inventario" que contiene las acciones y validaciones
     * específicas de dicha vista.
     */
    private final InventoryHealthPage inventoryHealthPage;

    /**
     * Constructor de la clase.
     *
     * @param scenarioContext contexto del escenario de prueba, utilizado para
     *                        compartir información entre pasos.
     *                        (Actualmente no se usa directamente en esta clase,
     *                        pero se mantiene para consistencia y futuras extensiones).
     */
    public InventoryHealthSteps(ScenarioContext scenarioContext) {
        this.pageManager = Hooks.getPageManager();
        this.inventoryHealthPage = pageManager.getInventoryHealthPage();
    }

    /**
     * Paso de Cucumber que valida que el color del semáforo en la tabla
     * "Histórico de cálculo de salud de inventario" cumple la secuencia esperada.
     * <p>
     * Se invoca al metodo correspondiente en {@link InventoryHealthPage} para
     * realizar la verificación.
     * </p>
     */
    @Then("el color del semáforo en la tabla \"Histórico de cálculo de salud de inventario\" cumple la secuencia esperada")
    public void checkTrafficLightSequence() {
        inventoryHealthPage.checkTrafficLightSequence();
    }
}
