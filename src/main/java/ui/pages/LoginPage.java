package ui.pages;

import config.EnvironmentConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;

/**
 * Página de inicio de sesión de la aplicación.
 * Permite ingresar credenciales y acceder al sistema.
 */
public class LoginPage extends BasePage {

    private final By emailInput = By.id("email");
    private final By nextButton = By.xpath("(//button[contains(@class, 'p-button') and @role='button'])[2]");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.xpath("//div[contains(@class,'flex') and contains(@class,'gap-2')]/button[contains(@class,'p-button') and @pbutton]");

    public LoginPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Realiza login usando las credenciales configuradas por entorno.
     */
    public void loginAs() {
        String email = EnvironmentConfig.getEmail();
        String password = EnvironmentConfig.getPassword();
        driver.get(EnvironmentConfig.getWebUrl() + "/login");

        sendKeysByLocator(emailInput, email, "Correo electronico");
        clickByLocator(nextButton, "Boton siguiente");
        sendKeysByLocator(passwordInput, password, "Contraseña");
        clickByLocator(loginButton, "Boton iniciar sesion");
    }
}