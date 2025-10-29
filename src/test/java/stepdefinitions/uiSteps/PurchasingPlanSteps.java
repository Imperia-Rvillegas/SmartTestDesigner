package stepdefinitions.uiSteps;

import hooks.Hooks;
import ui.manager.PageManager;
import ui.pages.PurchasingPlanPage;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

/**
 * Step Definitions para los escenarios relacionados con la funcionalidad
 * de "Plan de Compras" en la interfaz de usuario.
 *
 * Esta clase utiliza PageManager para acceder a los métodos de interacción
 * con la página y define los pasos necesarios para calcular el plan y validar el resultado.
 *
 * Esta clase encapsula los pasos necesarios para:
 * <ul>
 *   <li>Navegar al módulo de Plan de Compras</li>
 *   <li>Hacer clic en el botón Calcular</li>
 *   <li>Aceptar la confirmación de Calcular</li>
 *   <li>Validar que el semáforo sea de color verde</li>
 * </ul>
 *
 * @author TuNombre
 */
public class PurchasingPlanSteps {

    /** Instancia compartida del PageManager para acceder a las páginas y utilidades. */
    private final PageManager pageManager = Hooks.getPageManager();

    /** Página correspondiente al módulo de Plan de Compras. */
    private final PurchasingPlanPage purchasingPlanPage = pageManager.getPurchasePlanPage();

    /** Código del producto seleccionado para validarlo posteriormente en la tabla. */
    private String selectedProductCode;

    /**
     * Selecciona un producto no descontinuado en el formulario de Plan de compras.
     */
    @And("selecciona un producto no descontinuado en el formulario de nuevo Plan de compras")
    public void selectNonDiscontinuedProduct() {
        selectedProductCode = purchasingPlanPage.selectNonDiscontinuedProduct();
    }

    /**
     * Selecciona el primer proveedor disponible en el formulario de Plan de compras.
     */
    @And("selecciona el primer proveedor disponible en el formulario de nuevo Plan de compras")
    public void selectFirstSupplier() {
        purchasingPlanPage.selectFirstSupplier();
    }

    /**
     * Selecciona el primer almacén disponible en el formulario de Plan de compras si el campo está presente.
     */
    @And("selecciona el primer almacén disponible en el formulario de nuevo Plan de compras si existe")
    public void selectFirstWarehouseIfPresent() {
        purchasingPlanPage.selectFirstWarehouseIfPresent();
    }

    /**
     * Completa los campos numéricos requeridos del formulario: «Cantidad» e «Importe».
     */
    @And("completa los campos numéricos del formulario de nuevo Plan de compras")
    public void fillNumericFields() {
        purchasingPlanPage.fillQuantityAndAmount(1, 2);
    }

    /**
     * Selecciona la fecha actual como «Fecha de pedido».
     */
    @And("selecciona la fecha actual como fecha de pedido en el formulario de nuevo Plan de compras")
    public void selectTodayAsOrderDate() {
        purchasingPlanPage.selectTodayAsOrderDate();
    }

    /**
     * Selecciona la fecha actual como «Fecha de recepción».
     */
    @And("selecciona la fecha actual como fecha de recepción en el formulario de nuevo Plan de compras")
    public void selectTodayAsReceptionDate() {
        purchasingPlanPage.selectTodayAsReceptionDate();
    }

    /**
     * Verifica que el producto seleccionado aparezca en la tabla principal de Plan de compras.
     */
    @Then("el producto seleccionado aparece en la tabla de Plan de compras")
    public void verifyProductListedInPlanTable() {
        purchasingPlanPage.verifyProductListedInPlanTable(selectedProductCode);
    }
}
