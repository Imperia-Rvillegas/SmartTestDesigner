package ui.utils;

import org.openqa.selenium.*;
import ui.enums.AccessType;
import ui.manager.PageManager;
import ui.pages.HomePage;
import ui.pages.LoginPage;
import ui.pages.MenuPage;

/**
 * Utilidad para navegar a módulos del sistema mediante flujos estándar.
 * Permite acceder desde el menú lateral o desde la vista inicial del home.
 */
public class NavigationUtil {

    private final WebDriver driver;
    private final HomePage homePage;
    private final MenuPage menuPage;
    private final LoginPage loginPage;

    /**
     * Constructor que inicializa utilidades de navegación.
     * @param driver WebDriver actual
     * @param pageManager PageManager compartido
     */
    public NavigationUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.homePage = pageManager.getHomePage();
        this.menuPage = pageManager.getMenuPage();
        this.loginPage = pageManager.getLoginPage();
    }

    /**
     * Realiza login y navega al módulo indicado según el tipo de acceso especificado.
     * Acepta rutas jerárquicas separadas por '>'.
     *
     * - QUICK: accede directamente mediante el menú rápido.
     * - INDEX: abre el menú principal y luego navega por niveles.
     * - SEARCH: usa la barra de búsqueda y selecciona el módulo.
     *
     * @param modulePath Ruta del módulo a abrir, separada por '>' para navegación jerárquica.
     * @param accessType Tipo de acceso al módulo
     */
    public void navigateToModule(String modulePath, AccessType accessType) {
        LogUtil.info("Inicio navegación a módulo: " + modulePath + " | tipo de acceso: " + accessType);
        loginPage.loginAs();

        switch (accessType) {
            case QUICK:
                LogUtil.info("Acceso por menú Rápido.");
                homePage.selectModule(modulePath);
                break;
            case INDEX:
                LogUtil.info("Acceso por menú Índice.");
                homePage.openMenu();
                menuPage.selectNestedModule(modulePath);
                break;
            case SEARCH:
                LogUtil.info("Acceso por buscador.");
                homePage.searchAndSelectModule(modulePath);
                break;
            default:
                throw new IllegalArgumentException("Tipo de acceso no soportado: " + accessType);
        }

        LogUtil.info("Navegación completada a la pantalla: " + modulePath);
    }

    /**
     * Navega al módulo especificado utilizando el tipo de acceso indicado.
     * Los tipos de acceso soportados son: "rapido", "indice" y "buscar".
     *
     * @param modulePath Ruta del módulo a abrir, puede ser jerárquica.
     * @param accessType Tipo de acceso al módulo ("rapido", "indice" o "buscar").
     * @throws IllegalArgumentException si el tipo de acceso no es soportado
     */
    public void navigateToTheModuleFrom(String modulePath, String accessType) {
        LogUtil.info("Accediendo a la pantalla: " + modulePath + " | desde el menu: " + accessType);

        switch (accessType.toLowerCase()) {
            case "rapido":
                LogUtil.info("Acceso por menú Rápido.");
                homePage.selectModule(modulePath);
                break;
            case "indice":
                LogUtil.info("Acceso por menú Índice.");
                homePage.openMenu();
                menuPage.selectNestedModule(modulePath);
                break;
            case "buscar":
                LogUtil.info("Acceso por buscador.");
                homePage.searchAndSelectModule(modulePath);
                break;
            default:
                throw new IllegalArgumentException("Tipo de acceso no soportado: " + accessType);
        }

        LogUtil.info("Acceso completado a la pantalla: " + modulePath);
    }
}