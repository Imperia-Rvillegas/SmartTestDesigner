package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner para ejecutar suite ForecastingConcepts con Cucumber y JUnit.
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
                "html:target/ForecastingConcepts.html",
                "json:target/ForecastingConcepts.json"
        },
        tags = "@forecastingConcepts",     // Ejecuta solo escenarios con esta etiqueta
        monochrome = true     // Limpia la salida en consola
)
public class ForecastingConcepts {
}