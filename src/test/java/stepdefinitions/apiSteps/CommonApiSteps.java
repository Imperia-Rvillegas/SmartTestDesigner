package stepdefinitions.apiSteps;

import api.AuthenticationAPI;
import config.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.junit.Assert;

/**
 * Clase con pasos comunes reutilizables para múltiples features.
 */
public class CommonApiSteps {

    private final ScenarioContext scenarioContext;

    // PicoContainer inyecta automáticamente este constructor
    public CommonApiSteps(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Given("que obtengo el token de autenticación")
    public void get_token() {
        AuthenticationAPI.getToken();
    }

    @Then("la respuesta debería tener código {int}")
    public void validate_response(int statusCode) {
        Response response = scenarioContext.getResponse();
        Assert.assertEquals(statusCode, response.getStatusCode());
    }
}
