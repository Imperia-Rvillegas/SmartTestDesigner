package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Runner para trackingChangesInTheSameCycle.
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
                "html:target/TrackingChangesInTheSameCycle.html",
                "json:target/TrackingChangesInTheSameCycle.json"
        },
        tags = "@trackingChangesInTheSameCycle",     // Ejecuta solo escenarios con esta etiqueta
        monochrome = true     // Limpia la salida en consola
)
public class TrackingChangesInTheSameCycle extends BaseCucumberRunner {

    @BeforeClass
    public static void setUpHeadless() {
        System.setProperty("suite", "TrackingChangesInTheSameCycle");
    }
}