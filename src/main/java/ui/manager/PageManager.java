package ui.manager;

import config.ScenarioContext;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.pages.*;
import ui.utils.*;

/**
 * Clase centralizadora de todas las páginas (Page Objects) y utilidades.
 * Implementa patrón singleton por ejecución de prueba.
 */
public class PageManager {

    private final WebDriver driver;
    private ScenarioContext scenarioContext;
    private WaitUtil waitUtil;
    private TableUtil tableUtil;
    private ValidationUtil validationUtil;
    private NavigationUtil navigationUtil;
    private PopupUtil popupUtil;
    private ScreenshotUtil screenshotUtil;
    private TrafficLightUtil trafficLightUtil;

    private BasePage basePage;
    private LoginPage loginPage;
    private HomePage homePage;
    private MenuPage menuPage;
    private UnitPage unitPage;
    private ConfigurationPage configurationPage;
    private PurchasingPlanPage purchasingPlanPage;
    private ProvisioningPage provisioningPage;
    private ProductionPlanPage productionPlanPage;
    private ForecastsPage forecastsPage;
    private ArticlesPage articlesPage;
    private BusinessDimensionsPage businessDimensionsPage;
    private AssociatedDimensionsPage associatedDimensionsPage;
    private InventoryLocationsPage inventoryLocationsPage;
    private ListOfMaterialsPage listOfMaterialsPage;
    private ForecastingConceptsPage forecastingConceptsPage;
    private PluginStorePage pluginStorePage;
    private InventoryHealthPage inventoryHealthPage;
    private OriginsPage originsPage;
    private ScopesPage scopesPage;
    private ProjectionOfStockNeedsPage projectionOfStockNeedsPage;

    /**
     * Constructor que centraliza acceso a páginas y utilidades.
     * @param driver WebDriver activo
     * @param scenarioContext contexto de escenario Cucumber
     */
    public PageManager(WebDriver driver, ScenarioContext scenarioContext) {
        this.driver = driver;
        this.scenarioContext = scenarioContext;
    }

    // === Core ===

    public WebDriver getDriver() {
        return driver;
    }

    public ScenarioContext getScenarioContext() {
        return scenarioContext;
    }

    // === Páginas ===

    public BasePage getBasePage() {
        if (basePage == null) {
            basePage = new BasePage(driver, this);
        }
        return basePage;
    }

    public LoginPage getLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(driver, this);
        }
        return loginPage;
    }

    public HomePage getHomePage() {
        if (homePage == null) {
            homePage = new HomePage(driver, this);
        }
        return homePage;
    }

    public MenuPage getMenuPage() {
        if (menuPage == null) {
            menuPage = new MenuPage(driver, this);
        }
        return menuPage;
    }

    public UnitPage getUnitPage() {
        if (unitPage == null) {
            unitPage = new UnitPage(driver, this);
        }
        return unitPage;
    }

    public ConfigurationPage getConfigurationPage() {
        if (configurationPage == null) {
            configurationPage = new ConfigurationPage(driver, this);
        }
        return configurationPage;
    }

    public PurchasingPlanPage getPurchasePlanPage() {
        if (purchasingPlanPage == null) {
            purchasingPlanPage = new PurchasingPlanPage(driver, this);
        }
        return purchasingPlanPage;
    }

    public ProvisioningPage getProvisioningPage() {
        if (provisioningPage == null) {
            provisioningPage = new ProvisioningPage(driver, this);
        }
        return provisioningPage;
    }

    public ProductionPlanPage getProductionPlanPage() {
        if (productionPlanPage == null) {
            productionPlanPage = new ProductionPlanPage(driver, this);
        }
        return productionPlanPage;
    }

    public ForecastsPage getForecastsPage() {
        if (forecastsPage == null) {
            forecastsPage = new ForecastsPage(driver, this);
        }
        return forecastsPage;
    }

    public ArticlesPage getArticlesPage() {
        if (articlesPage == null) {
            articlesPage = new ArticlesPage(driver, this);
        }
        return articlesPage;
    }

    public AssociatedDimensionsPage getAssociatedDimensionsPage() {
        if (associatedDimensionsPage == null) {
            associatedDimensionsPage = new AssociatedDimensionsPage(driver, this);
        }
        return associatedDimensionsPage;
    }

    public BusinessDimensionsPage getBusinessDimensionsPage() {
        if (businessDimensionsPage == null) {
            businessDimensionsPage = new BusinessDimensionsPage(driver, this);
        }
        return businessDimensionsPage;
    }

    public InventoryLocationsPage getInventoryLocationsPage() {
        if (inventoryLocationsPage == null) {
            inventoryLocationsPage = new InventoryLocationsPage(driver, this);
        }
        return inventoryLocationsPage;
    }

    public ListOfMaterialsPage getListOfMaterialsPage() {
        if (listOfMaterialsPage == null) {
            listOfMaterialsPage = new ListOfMaterialsPage(driver, this);
        }
        return listOfMaterialsPage;
    }

    public ForecastingConceptsPage getForecastingConceptsPage() {
        if (forecastingConceptsPage == null) {
            forecastingConceptsPage = new ForecastingConceptsPage(driver, this);
        }
        return forecastingConceptsPage;
    }

    public PluginStorePage getPluginStorePage() {
        if (pluginStorePage == null) {
            pluginStorePage = new PluginStorePage(driver, this);
        }
        return pluginStorePage;
    }

    public InventoryHealthPage getInventoryHealthPage() {
        if (inventoryHealthPage == null) {
            inventoryHealthPage = new InventoryHealthPage(driver, this);
        }
        return inventoryHealthPage;
    }

    public OriginsPage getOriginsPage() {
        if (originsPage == null) {
            originsPage = new OriginsPage(driver, this);
        }
        return originsPage;
    }

    public ScopesPage getScopesPage() {
        if (scopesPage == null) {
            scopesPage = new ScopesPage(driver, this);
        }
        return scopesPage;
    }

    /**
     * Devuelve la instancia de {@link ProjectionOfStockNeedsPage}, inicializándola si es necesario.
     *
     * @return page object reutilizable para Proyección de stock de necesidades.
     */
    public ProjectionOfStockNeedsPage getProjectionOfStockNeedsPage() {
        if (projectionOfStockNeedsPage == null) {
            projectionOfStockNeedsPage = new ProjectionOfStockNeedsPage(driver, this);
        }
        return projectionOfStockNeedsPage;
    }

    // === Utilidades ===

    public NavigationUtil getNavigationUtil() {
        if (navigationUtil == null) {
            navigationUtil = new NavigationUtil(this);
        }
        return navigationUtil;
    }

    public PopupUtil getPopupUtil() {
        if (popupUtil == null) {
            popupUtil = new PopupUtil(this);
        }
        return popupUtil;
    }

    public ScreenshotUtil getScreenshotUtil() {
        if (screenshotUtil == null) {
            screenshotUtil = new ScreenshotUtil(this);
        }
        return screenshotUtil;
    }

    public WaitUtil getWaitUtil() {
        if (waitUtil == null) {
            waitUtil = new WaitUtil(this);
        }
        return waitUtil;
    }

    public TableUtil getTableUtil() {
        if (tableUtil == null) {
            tableUtil = new TableUtil(this);
        }
        return tableUtil;
    }

    public ValidationUtil getValidationUtil() {
        if (validationUtil == null) {
            validationUtil = new ValidationUtil(this);
        }
        return validationUtil;
    }

    public TrafficLightUtil getTrafficLightUtil() {
        if (trafficLightUtil == null) {
            trafficLightUtil = new TrafficLightUtil(this);
        }
        return trafficLightUtil;
    }
}