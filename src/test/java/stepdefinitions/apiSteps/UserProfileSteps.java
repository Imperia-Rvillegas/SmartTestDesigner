package stepdefinitions.apiSteps;

import api.UserProfileAPI;
import config.ScenarioContext;
import hooks.Hooks;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import ui.manager.PageManager;

/**
 * Pasos para prueba de obtención del perfil del usuario.
 */
public class UserProfileSteps {

    private final PageManager pageManager = Hooks.getPageManager();
    private final ScenarioContext scenarioContext = pageManager.getScenarioContext();

    @When("hago la petición para obtener el perfil del usuario")
    public void get_a_user_profile() {
        Response response = UserProfileAPI.getUserProfile();
        scenarioContext.setResponse(response);
    }
}
