package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Representa la página de inicio del sistema.
 * Permite interactuar con el botón de menú y acceder a módulos desde el home.
 */
public class HomePage extends BasePage {

    private final By buttonMenuLocator = By.xpath("//imperia-icon-button[@icon='hamburguer']//a[@role='button']");
    private final By searchButton = By.cssSelector("button.btn-searcher");
    private final By searchInput = By.cssSelector("div.input-searcher-container input[type='text']");

    public HomePage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Abre el menú lateral desde el botón hamburguesa.
     */
    public void openMenu() {
        clickByLocator(buttonMenuLocator, "Menu");
    }

    /**
     * Selecciona un módulo desde el menú por su nombre.
     * @param moduleName Nombre exacto visible del módulo.
     */
    public void selectModule(String moduleName) {
        clickButtonByName(moduleName);
    }

    /**
     * Busca y selecciona un módulo dentro de la aplicación utilizando el buscador (ícono de lupa).
     *
     * <p>Este metodo realiza los siguientes pasos:
     * <ol>
     *   <li>Hace clic en el ícono de búsqueda (lupa).</li>
     *   <li>Ingresa el nombre del módulo en el campo de texto del buscador.</li>
     *   <li>Espera a que aparezca el resultado exacto y hace clic sobre él para seleccionarlo.</li>
     * </ol>
     *
     * @param screenName el nombre exacto del módulo que se desea buscar y seleccionar.
     */
    public void searchAndSelectModule(String screenName) {
        // Clic en ícono de lupa
        clickByLocator(searchButton, "Lupa de Buscar");

        // Ingresar nombre del módulo en el campo de búsqueda
        sendKeysByLocatorWithVerification(searchInput, screenName, "Buscar");

        // Esperar que aparezca el resultado correspondiente y hacer clic
        By resultSelector = By.xpath("//a[contains(@class, 'searcher-link')]//div[contains(@class,'route-link')][normalize-space(text())='" + screenName + "']");
        clickByLocator(resultSelector, screenName);
    }
}