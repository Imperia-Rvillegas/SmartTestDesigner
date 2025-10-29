package runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Clase encargada de ejecutar los escenarios de prueba de API definidos en archivos feature con Cucumber.
 * Utiliza JUnit como runner principal y configura los parámetros necesarios para la ejecución.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        // Ruta donde se encuentran los archivos .feature que definen los escenarios de prueba
        features = "src/test/resources/features",

        // Paquete donde se encuentran las definiciones de pasos (Step Definitions)
        glue = {
                "stepdefinitions",
                "hooks"
        },

        // Etiqueta utilizada para filtrar los escenarios a ejecutar
        tags = "@api",

        // Plugins de salida para reportes en formato legible, JSON y HTML
        plugin = {
                "pretty",                               // Muestra resultados detallados en consola
                "json:target/Api.json",            // Reporte en formato JSON para integración con herramientas externas
                "html:target/Api.html"      // Reporte en formato HTML para visualización en navegador
        }
)
public class Api {// Clase vacía. La ejecución es controlada por las anotaciones @RunWith y @CucumberOptions.
}
