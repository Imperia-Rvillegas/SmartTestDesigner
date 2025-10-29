package ui.pages;

import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Página de UI que representa la vista de
 * <b>"Salud de inventario"</b> dentro de la aplicación.
 * <p>
 * Esta clase encapsula las acciones y validaciones que se
 * pueden realizar sobre dicha pantalla, siguiendo el
 * patrón de diseño Page Object.
 * </p>
 */
public class InventoryHealthPage extends BasePage {

    /**
     * Constructor de la clase {@code InventoryHealthPage}.
     *
     * @param driver      instancia de {@link WebDriver} utilizada para interactuar con el navegador.
     * @param pageManager instancia de {@link PageManager} que gestiona los objetos Page y utilidades compartidas.
     */
    public InventoryHealthPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Verifica que la secuencia de colores del semáforo en la tabla
     * <b>"Histórico de cálculo de salud de inventario"</b> cumpla con lo esperado.
     * <p>
     * Internamente, delega la validación en {@code trafficLightUtil}.
     * </p>
     */
    public void checkTrafficLightSequence() {
        trafficLightUtil.checkTrafficLightSequence("Histórico de cálculo de salud de inventario");
    }
}