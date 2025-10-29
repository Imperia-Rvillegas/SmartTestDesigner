package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ui.manager.PageManager;
import ui.pages.ProjectionOfStockNeedsPage;

/**
 * Step Definitions específicos para la pantalla "Proyección de stock de necesidades".
 * <p>
 * Conecta los pasos escritos en Gherkin con el {@link ProjectionOfStockNeedsPage},
 * encapsulando los textos visibles relevantes en constantes para mantenerlos en un
 * único punto de mantenimiento.
 * </p>
 */
public class ProjectionOfStockNeedsSteps {

    private static final String PROJECTION_HISTORY_TABLE_TITLE = "Histórico de cálculos de la proyección de stock";
    private static final String CALCULATE_PROJECTION_BUTTON = "Calcular proyección de stock";
    private static final String CALCULATE_CURRENT_PROJECTION_BUTTON = "Calcular proyección actual";

    private final PageManager pageManager = Hooks.getPageManager();
    private final ProjectionOfStockNeedsPage projectionPage = pageManager.getProjectionOfStockNeedsPage();

    /**
     * Lanza el cálculo completo de proyección de stock y acepta la confirmación del sistema.
     */
    @When("el usuario solicita calcular la proyección de stock")
    public void requestStockProjection() {
        projectionPage.calculateProjection(CALCULATE_PROJECTION_BUTTON);
    }

    /**
     * Lanza el cálculo de la proyección actual y confirma la ejecución.
     */
    @When("el usuario solicita calcular la proyección de stock actual")
    public void requestCurrentProjection() {
        projectionPage.calculateProjection(CALCULATE_CURRENT_PROJECTION_BUTTON);
    }

    /**
     * Verifica que el semáforo del historial de proyecciones termine en verde.
     */
    @Then("el semáforo de la proyección de stock se muestra en verde")
    public void verifyTrafficLightGreen() {
        projectionPage.verifyTrafficLightGreen(PROJECTION_HISTORY_TABLE_TITLE);
    }

    /**
     * Abre el panel de revisión de la proyección para visualizar los registros disponibles.
     *
     * @param panelName texto visible del botón que muestra el panel.
     */
    @When("el usuario hace clic en {string} en Proyección de stock de necesidades")
    public void openReviewPanel(String panelName) {
        projectionPage.openReviewPanel(panelName);
    }

    /**
     * Localiza un producto con información en la columna indicada y almacena la cantidad encontrada.
     *
     * @param columnHeader encabezado de la columna donde se deben encontrar registros.
     */
    @And("busca un producto con registros de {string} y guarda su cantidad en Proyección de stock de necesidades")
    public void findProductWithRecords(String columnHeader) {
        projectionPage.findProductWithRecordsAndStoreQuantity(columnHeader);
    }

    /**
     * Selecciona el registro almacenado previamente para visualizar su detalle.
     */
    @And("selecciona el registro de Proyección de stock de necesidades")
    public void selectStoredRecord() {
        projectionPage.selectStoredReviewPanelRecord();
    }

    /**
     * Valida que la suma de la columna indicada coincida con la cantidad previamente almacenada.
     *
     * @param columnHeader nombre de la columna a sumar dentro del panel de revisión.
     */
    @Then("la suma de la columna {string} en el panel de revisión coincide con la cantidad guardada en Proyección de stock de necesidades")
    public void validateQuantitySum(String columnHeader) {
        projectionPage.validateReviewPanelDetailQuantity(columnHeader);
    }

    /**
     * Verifica que todas las fechas del detalle correspondan a la fecha registrada al seleccionar el producto.
     *
     * @param columnHeader nombre de la columna que contiene las fechas a validar.
     */
    @And("las fechas de la columna {string} coinciden con la fecha del registro seleccionado en Proyección de stock de necesidades")
    public void verifyDetailDates(String columnHeader) {
        projectionPage.verifyReviewPanelDetailDates(columnHeader);
    }
}
