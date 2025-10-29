package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ui.manager.PageManager;
import ui.pages.ProvisioningPage;

/**
 * Step Definitions para los escenarios de la pantalla "Aprovisionamiento".
 */
public class ProvisioningSteps {

    /** Administrador de páginas compartido. */
    private final PageManager pageManager = Hooks.getPageManager();

    /** Página de Aprovisionamiento. */
    private final ProvisioningPage provisioningPage = pageManager.getProvisioningPage();

    /**
     * Busca y almacena un registro cuya cantidad del mes siguiente sea mayor a cero.
     */
    @When("busca un registro con cantidad distinta de cero en la columna del {string} en Aprovisionamiento")
    public void findRecordWithQuantityInPeriod(String periodLabel) {
        provisioningPage.findRecordWithQuantityInPeriod(periodLabel);
    }

    /**
     * Abre el detalle del registro previamente identificado.
     */
    @And("hace clic sobre el registro encontrado en la pantalla de Aprovisionamiento")
    public void openSelectedProvisioningRecord() {
        provisioningPage.openSelectedRecordDetail();
    }

    /**
     * Valida que la suma de la columna especificada coincida con la cantidad del registro seleccionado.
     *
     * @param columnHeader encabezado de la columna a sumar dentro del detalle.
     */
    @Then("la suma de la columna {string} coincide con la cantidad seleccionada en Aprovisionamiento")
    public void validateDetailQuantityMatchesSelection(String columnHeader) {
        provisioningPage.validateDetailQuantityMatchesSelected(columnHeader);
    }

    /**
     * Verifica que las fechas de recepción correspondan al mes almacenado al seleccionar el registro.
     */
    @And("las {string} del detalle pertenecen al mes seleccionado en Aprovisionamiento")
    public void verifyReceptionDatesMatchSelectedMonth(String date) {
        provisioningPage.verifyDetailReceptionDatesMatchSelectedMonth(date);
    }

    @And("selecciona la vista de {string} en Aprovisionamiento")
    public void seleccionaLaVistaDeEnAprovisionamiento(String name) {
        provisioningPage.seleccionaLaVistaDeEnAprovisionamiento(name);
    }

    /**
     * Selecciona el criterio de fecha que se utilizará en la pantalla de Aprovisionamiento.
     *
     * @param dateOption opción de fecha visible en la interfaz (por ejemplo, «Fecha de recepción»).
     */
    @And("selecciona la fecha {string} en Aprovisionamiento")
    public void selectDateCriterionInProvisioning(String dateOption) {
        provisioningPage.selectDateCriterionInProvisioning(dateOption);
    }

    /**
     * Selecciona el tipo de valor (cantidad o importe) que se analizará en la pantalla de Aprovisionamiento.
     *
     * @param valueOption texto visible del botón o selector correspondiente (por ejemplo, «Cantidad»).
     */
    @And("selecciona el valor {string} en Aprovisionamiento")
    public void selectValueTypeInProvisioning(String valueOption) {
        provisioningPage.selectValueTypeInProvisioning(valueOption);
    }
}
