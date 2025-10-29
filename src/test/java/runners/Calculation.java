package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner para ejecutar únicamente los escenarios de cálculo.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/uiFeatures", // Ruta a los features UI
        glue = {
                "stepdefinitions.uiSteps",  // Paquete con los step definitions UI
                "hooks"                     // Incluye hooks para manejar WebDriver
        },
        plugin = {
                "pretty",
                "html:target/Calculation.html",
                "json:target/Calculation.json"
        },
        tags = "@calculation",   // Ejecuta solo escenarios con esta etiqueta
        monochrome = true        // Limpia la salida en consola
)
public class Calculation extends BaseCucumberRunner {
}