package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Runner dedicado a la suite "ProjectionOfStockNeeds".
 * <p>
 * Ejecuta únicamente los escenarios etiquetados con {@code @projectionOfStockNeeds},
 * generando reportes HTML y JSON en la carpeta {@code target/}. Úsese junto con el
 * workflow de regresión para automatizar la validación de la pantalla Proyección de
 * stock de necesidades.
 * </p>
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
                "html:target/ProjectionOfStockNeeds.html",
                "json:target/ProjectionOfStockNeeds.json"
        },
        tags = "@projectionOfStockNeeds",     // Ejecuta solo escenarios con esta etiqueta
        monochrome = true     // Limpia la salida en consola
)
public class ProjectionOfStockNeeds {
}
