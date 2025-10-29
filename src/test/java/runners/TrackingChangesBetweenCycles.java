package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Runner para trackingChangesBetweenCycles.
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
                "html:target/TrackingChangesBetweenCycles.html",
                "json:target/TrackingChangesBetweenCycles.json"
        },
        tags = "@trackingChangesBetweenCycles",     // Ejecuta solo escenarios con esta etiqueta
        monochrome = true     // Limpia la salida en consola
)
public class TrackingChangesBetweenCycles extends BaseCucumberRunner {

    @BeforeClass
    public static void setUpHeadless() {
        System.setProperty("suite", "TrackingChangesBetweenCycles");
    }
}