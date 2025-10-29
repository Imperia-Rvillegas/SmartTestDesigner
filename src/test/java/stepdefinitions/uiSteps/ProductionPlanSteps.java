package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import ui.manager.PageManager;
import ui.pages.ProductionPlanPage;

/**
 * Step Definitions para los escenarios relacionados con la funcionalidad
 * de "Plan de Produccion" en la interfaz de usuario.
 *
 * Esta clase utiliza PageManager para acceder a los métodos de interacción
 * con la página y define los pasos necesarios para calcular el plan y validar el resultado.
 *
 * Esta clase encapsula los pasos necesarios para:
 * <ul>
 *   <li>Navegar al módulo de Plan de Produccion</li>
 *   <li>Hacer clic en el botón Calcular</li>
 *   <li>Aceptar la confirmación de Calcular</li>
 *   <li>Validar que el semáforo sea de color verde</li>
 * </ul>
 *
 * @author TuNombre
 */
public class ProductionPlanSteps {

    /** Instancia compartida del PageManager para acceder a las páginas y utilidades. */
    private final PageManager pageManager = Hooks.getPageManager();

    /** Página correspondiente al módulo de Plan de Produccion. */
    private final ProductionPlanPage productionPlanPage = pageManager.getProductionPlanPage();

    /** Código del producto seleccionado en el formulario para validarlo posteriormente. */
    private String selectedProductCode;

    /**
     * Selecciona un producto marcado como descontinuado dentro del formulario de Plan de producción.
     * <p>
     * Este paso realiza lo siguiente:
     * <ol>
     *     <li>Abre el selector asociado al campo «Código producto».</li>
     *     <li>Aplica el filtro «Descontinuado» con la opción «Sí».</li>
     *     <li>Guarda en memoria el código de la primera fila resultante.</li>
     *     <li>Selecciona dicha fila y confirma la selección.</li>
     * </ol>
     * </p>
     */
    @And("selecciona un producto no descontinuado en el formulario de nuevo Plan de producción")
    public void selectDiscontinuedProduct() {
        selectedProductCode = productionPlanPage.selectDiscontinuedProduct();
    }

    /**
     * Selecciona el primer proceso disponible del listado asociado al formulario de Plan de producción.
     * <p>
     * El metodo abre el buscador del campo «Proceso», toma la primera fila visible y confirma con «Aceptar».
     * </p>
     */
    @And("selecciona el primer proceso disponible en el formulario de nuevo Plan de producción")
    public void selectFirstProcess() {
        productionPlanPage.selectFirstProcess();
    }

    /**
     * Selecciona la primera línea disponible dentro del formulario de Plan de producción.
     * <p>
     * Al igual que en el proceso, el flujo abre el selector del campo «Línea», elige la primera fila y confirma la elección.
     * </p>
     */
    @And("selecciona la primera línea disponible en el formulario de nuevo Plan de producción")
    public void selectFirstLine() {
        productionPlanPage.selectFirstLine();
    }

    /**
     * Selecciona el primer almacén disponible en caso de que el campo esté presente en el formulario de Plan de producción.
     * <p>
     * Si el campo «Almacén» no aparece, el paso se omite silenciosamente sin generar errores.
     * </p>
     */
    @And("selecciona el primer almacén disponible en el formulario de nuevo Plan de producción si existe")
    public void selectFirstWarehouseIfPresent() {
        productionPlanPage.selectFirstWarehouseIfPresent();
    }

    /**
     * Completa los valores numéricos obligatorios del formulario: «Cantidad» e «Importe».
     * <p>
     * Se ingresan los valores requeridos respetando la convención definida en el escenario (1 para cantidad y 2 para importe).
     * </p>
     */
    @And("completa los campos numéricos del formulario de nuevo Plan de producción")
    public void fillNumericFields() {
        productionPlanPage.fillQuantityAndAmount("1", "2");
    }

    /**
     * Selecciona la fecha actual como «Fecha máxima de fabricación» desde el calendario emergente.
     * <p>
     * El flujo abre el calendario asociado al campo y elige el elemento marcado con la clase {@code p-datepicker-today}.
     * </p>
     */
    @And("selecciona la fecha máxima de fabricación actual en el formulario de nuevo Plan de producción")
    public void selectTodayAsMaxManufacturingDate() {
        productionPlanPage.selectTodayAsMaxManufacturingDate();
    }

    /**
     * Valida que el producto seleccionado previamente se muestre en la tabla principal de Plan de producción.
     * <p>
     * La validación utiliza el código capturado al seleccionar el producto descontinuado.
     * </p>
     */
    @Then("el producto seleccionado aparece en la tabla de Plan de producción")
    public void verifyProductListedInPlanTable() {
        productionPlanPage.verifyProductListedInPlanTable(selectedProductCode);
    }
}
