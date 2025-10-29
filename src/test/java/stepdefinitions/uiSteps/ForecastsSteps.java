package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import testmodel.forecastingConceptsData;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.pages.ForecastingConceptsPage;
import ui.pages.ForecastsPage;
import ui.pages.HomePage;
import ui.utils.TestDataLoader;

/**
 * Clase de definición de pasos (Step Definitions) para las pruebas UI del módulo de Previsiones (Forecasts).
 * Implementa pasos de Cucumber que interactúan con la interfaz de usuario a través de la clase {@link ForecastsPage}.
 *
 * Estos pasos permiten seleccionar agrupaciones, navegar entre módulos, modificar celdas dinámicamente
 * y validar el resultado de las operaciones realizadas.
 */
public class ForecastsSteps {

    private final PageManager pageManager = Hooks.getPageManager();
    private final ForecastsPage forecastsPage = pageManager.getForecastsPage();
    private final HomePage homePage = pageManager.getHomePage();
    private final BasePage basePage = pageManager.getBasePage();
    private final ForecastingConceptsPage forecastingConceptsPage = pageManager.getForecastingConceptsPage();
    private final String newConceptName;

    public ForecastsSteps() {
        // ForecastingConcepts
        forecastingConceptsData forecastingConceptsData = TestDataLoader.load("testdata/page/forecastingConcepts.yaml", forecastingConceptsData.class);
        this.newConceptName = forecastingConceptsData.getNewConceptName();
    }

    /**
     * Paso que selecciona una agrupación específica dentro del módulo de previsiones.
     *
     * @param group nombre visible de la agrupación (por ejemplo, "Meses", "Años", etc.).
     */
    @And("selecciona la agrupación {string}")
    public void selectTheGrouping(String group) {
        forecastsPage.selectGrouping(group);
    }

    /**
     * Paso que modifica la celda correspondiente al período siguiente (mes, semana o día)
     * dependiendo del valor de agrupación actualmente seleccionado.
     * <ul>
     *   <li>Si agrupación = "Meses", edita la celda del próximo mes.</li>
     *   <li>Si agrupación = "Semanas", edita la celda de la próxima semana.</li>
     *   <li>Si agrupación = "Días", edita la celda del próximo día.</li>
     * </ul>
     * Lanza {@link AssertionError} si el resultado de la operación no es el esperado.
     *
     * @param group tipo de agrupación seleccionada por el usuario.
     */
    @And("modifica la celda del periodo siguiente según la agrupación {string}")
    public void modifyCellAccordingToGrouping(String group) {
        forecastsPage.editNextPeriodValue(group);
    }

    /**
     * Paso que valida que el valor visible en la celda del próximo periodo
     * (mes, semana o día) corresponde con el valor modificado.
     *
     * @param group tipo de agrupación: "Meses", "Semanas" o "Días".
     */
    @And("la celda del periodo siguiente muestra el valor nuevo para la agrupación {string}")
    public void cellDisplaysNewValue(String group) {
        forecastsPage.validateUpdatedValue(group);
    }

    /**
     * Paso de Cucumber que refresca la página actual del navegador.
     *
     * <p>Este paso está mapeado a la instrucción en lenguaje natural:
     * <strong>"el usuario refresca la página"</strong>.</p>
     *
     * <p>Acciona la recarga completa de la página web utilizando el metodo
     * {@link ForecastsPage#refreshPageAndWait()}, el cual asegura que:</p>
     * <ul>
     *   <li>El navegador realice un refresh de la URL actual.</li>
     *   <li>Se apliquen esperas explícitas para asegurar que los elementos principales estén cargados nuevamente.</li>
     * </ul>
     *
     * <p>Útil en pruebas donde se requiere forzar el re-render de componentes dinámicos,
     * limpiar posibles estados inconsistentes, o reiniciar condiciones de prueba.</p>
     */
    @When("el usuario refresca la página")
    public void refreshPage() {
        //Esto permite validar que el valor persiste y tambien deja la tabla en su estado inicial ya que si se le aplicó scroll anteriormente la posicion de las celdas cambiara
        forecastsPage.refreshPageAndWait();
    }

    /**
     * Paso de Cucumber que valida que la celda modificada mantiene su valor después de refrescar la página.
     *
     * <p>Este paso está mapeado a la instrucción en lenguaje natural:
     * <strong>"la celda modificada mantiene el nuevo valor después del refresco"</strong>.</p>
     *
     * <p>Este metodo invoca {@link ForecastsPage#validatePersistenceAfterRefresh()}, el cual debe encargarse de:</p>
     * <ul>
     *   <li>Recuperar el valor previamente ingresado o modificado en una celda de la tabla.</li>
     *   <li>Forzar un refresh completo de la página.</li>
     *   <li>Esperar a que la tabla se recargue correctamente.</li>
     *   <li>Verificar que la celda conserva el mismo valor tras el refresco.</li>
     * </ul>
     *
     * <p>Este tipo de validación es útil para probar persistencia de datos,
     * sincronización con el backend y comportamiento correcto del almacenamiento temporal/local.</p>
     */
    @Then("la celda modificada mantiene el nuevo valor después del refresco según el tipo de operación {string}")
    public void validatePersistenceAfterRefresh(String operation) {
        forecastsPage.validateCorrectCellModification(operation);
    }

    /**
     * Paso de Cucumber que ubica una celda con state (modificada o sin modificar) y le aplica una operación aritmética.
     *
     * @param condition    Estado de la celda: "modificada" (con candado) o "sin modificar" (sin candado).
     * @param operation Operación a aplicar: suma, resta, multiplicación, división, reemplazo.
     */
    @And("ubica una celda {string} y aplica la operacion {string}")
    public void locateCellWithStateAndApplyOperation(String condition, String operation) {
        forecastsPage.locatesACellWithGivenStateAndAppliesOperation(condition, operation);
    }

    /**
     * Paso de Cucumber que configura el último nivel de agrupación en la jerarquía.
     *
     * <p>Este paso está mapeado a la instrucción en lenguaje natural:
     * <strong>"configura el ultimo nivel de agrupacion"</strong>.</p>
     *
     * <p>Realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Elimina la jerarquía actual existente mediante {@link ForecastsPage#deletePreviousHierarchy()}.
     *       Esto asegura que no haya residuos de configuraciones anteriores, dejando la tabla lista para una nueva definición.</li>
     *   <li>Obtiene desde un endpoint la lista de dimensiones válidas para la agrupación y configura el nuevo orden jerárquico
     *       utilizando {@link ForecastsPage#configureTheLastGroupingLevel()}.</li>
     * </ol>
     *
     * <p>El flujo completo simula la configuración del último nivel de agrupación en la UI de forma dinámica,
     * utilizando datos reales del backend para mantener consistencia funcional entre interfaz y lógica de negocio.</p>
     */
    @And("configura el ultimo nivel de agrupacion")
    public void configureTheLastGroupingLevel() {
        // Eliminar jerarquía anterior
        forecastsPage.deletePreviousHierarchy();

        // Seleccionar jerarquía según respuesta de endpoint
        forecastsPage.configureTheLastGroupingLevel();
    }

    /**
     * Paso de Cucumber que ubica un producto con más de 2 registros y valida que las celdas
     * en las columnas de los próximos 6 meses cumplan con una condición específica.
     *
     * @param condition tipo de condición que deben cumplir las celdas, por ejemplo:
     *                  "sin modificar sin pedido pendiente", "modificada con pedido pendiente", etc.
     * @throws AssertionError si no se encuentra ningún producto que cumpla la condición.
     */
    @And("ubica un producto con mas de 2 entradas y celdas {string}")
    public void locateProductWithMultipleRowsAndCondition(String condition) {
        forecastsPage.findProductWithConditionInMonthColumns(condition);
    }

    /**
     * Paso de Cucumber que cambia la agrupación en la interfaz de previsiones
     * para dejar solo la dimensión "Código producto".
     */
    @And("cambia la agrupacion a solo codigo producto")
    public void changeTheGroupingToProductCodeOnly() {
        forecastsPage.deleteHierarchyExceptCodigoProducto();
    }

    /**
     * Paso de Cucumber que aplica una operación de modificación sobre las celdas
     * correspondientes al producto previamente localizado.
     *
     * @param operation nombre de la operación a aplicar (ej: "multiplicar por 2", "sumar", "reemplazar", etc.).
     */
    @And("modifica el producto aplicando {string}")
    public void modifyTheProduct(String operation) {
        forecastsPage.modifyTheProductByApplying(operation);
    }

    /**
     * Paso de Cucumber que agrega nuevamente todas las dimensiones que fueron eliminadas,
     * excepto "Código producto", como parte de la agrupación.
     *
     * También elimina cualquier filtro aplicado por "Código producto" antes de reconfigurar la agrupación.
     */
    @And("agrega las dimensiones restantes")
    public void addTheRemainingDimensions() {
        //Elimina filtros
        forecastsPage.removeFilters("Código producto");

        forecastsPage.clickButtonByName("Agrupación");

        String firstDimension = "Código producto";
        forecastsPage.addRemainingDimensions(firstDimension);
    }

    /**
     * Paso de Cucumber que valida que la suma total de los nuevos valores de las celdas modificadas
     * sea correcta según la operación aplicada previamente.
     *
     * Este paso asume que la operación aplicada y el valor esperado ya están definidos.
     */
    @And("valida que la suma de todas las celdas es igual la nueva cantidad del producto")
    public void theSumOfAllCellsIsCorrect() {
        forecastsPage.theSumOfAllCellsIsCorrect();
    }

    /**
     * Paso de Cucumber que valida que el valor de una celda previamente modificada
     * en la página de previsiones sea el valor esperado.
     *
     * Este metodo delega la validación a la lógica definida en {@code forecastsPage.validModifiedValue()},
     * donde se compara el valor actual de la celda con el valor esperado según el contexto de la prueba.
     *
     * Se usa en los escenarios donde se realiza una edición de celda y posteriormente se requiere
     * verificar que el cambio fue correctamente aplicado.
     */
    @And("valida que el valor de la celda persiste")
    public void validModifiedValue() {
        forecastsPage.validModifiedValue();
    }

    /**
     * Step de Cucumber que abre la pantalla de <b>Previsiones</b>.
     * <p>Mapea el step: {@code And abre la pantalla de previsiones} y delega en
     * {@code forecastsPage.openTheForecastScreen()}.</p>
     */
    @And("abre la pantalla de previsiones")
    public void openScreenForecasts() {
        forecastsPage.openTheForecastScreen();
    }

    /**
     * Step de Cucumber que ubica una celda según la condición indicada y
     * aplica la operación para el nuevo concepto.
     * <p>Mapea el step:
     * {@code And ubica una celda "<condición>" y aplica la operacion "<operación>" para el nuevo concepto}
     * y delega en {@link ForecastsPage#locateCellandApplyOperation(String, String, String)}
     * usando {@code newConceptName}.</p>
     *
     * @param condition condición que debe cumplir la celda (p. ej., "modificada con pedido pendiente").
     * @param operation operación a aplicar sobre la celda (p. ej., "incrementar", "disminuir", "multiplicacion").
     */
    @And("ubica una celda {string} y aplica la operacion {string} para el nuevo concepto")
    public void locateaCellAndApplyTheOperationForTheNewConcept(String condition, String operation) {
        forecastsPage.locateCellandApplyOperation(condition, operation, newConceptName);
    }

    /**
     * Step de Cucumber que valida que la cantidad se aplica correctamente al concepto.
     * <p>Mapea el step: {@code And valida que la cantidad se aplica correctamente al concepto}
     * y delega en {@link ForecastsPage#quantityIsCorrectlyAppliedToTheConcept(String)}
     * usando {@code newConceptName}.</p>
     */
    @And("valida que la cantidad se aplica correctamente al concepto")
    public void validatesThatTheAmountIsCorrectlyAppliedToTheConcept() {
        forecastsPage.quantityIsCorrectlyAppliedToTheConcept(newConceptName);
    }

    /**
     * Step de Cucumber que elimina el concepto recién creado.
     * <p>Mapea el step: {@code And Eliminar el concepto} y realiza:</p>
     * <ol>
     *   <li>Abrir el menú principal.</li>
     *   <li>Navegar a <b>Conceptos de previsión</b>.</li>
     *   <li>Seleccionar {@code newConceptName}.</li>
     *   <li>Pulsar <b>Eliminar</b> y confirmar con <b>Aceptar</b>.</li>
     * </ol>
     */
    @And("Eliminar el concepto")
    public void deleteTheConcept() {
        // Hace clic en el boton del menu
        homePage.openMenu();

        // Hace clic en el enlace "Conceptos de previsión"
        basePage.clickButtonByName("Conceptos de previsión");

        // Selecciona el concepto nuevo
        forecastingConceptsPage.selectConcept(newConceptName);

        // Hace clic en el boton eliminar
        basePage.clickButtonByName("Eliminar");

        // Hace clic en aceptar
        basePage.clickButtonByName("Aceptar");
    }
}