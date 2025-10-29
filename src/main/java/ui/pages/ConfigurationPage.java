package ui.pages;

import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Página de Configuraciones.
 * Permite al usuario ejecutar el cálculo general y validar visualmente el resultado
 * mediante el estado del semáforo de ejecución.
 */
public class ConfigurationPage extends BasePage {

    /**
     * Constructor que inicializa la página de Configuraciones con su PageManager.
     *
     * @param driver       Instancia del WebDriver actual.
     * @param pageManager  Referencia centralizada a otras páginas y utilidades.
     */
    public ConfigurationPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Hace clic en el botón de confirmación tras lanzar el cálculo.
     * Este botón aparece en un cuadro de diálogo de confirmación.
     */
    public void confirmCalculation() {
        clickButtonByName("Aceptar");
    }
}