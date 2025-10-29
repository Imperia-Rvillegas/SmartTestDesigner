package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;

import java.util.function.Function;

/**
 * Representa el menú de navegación del sistema.
 * Permite acceder a módulos mediante sus nombres visibles.
 */
public class MenuPage extends BasePage {

    public MenuPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Navega jerárquicamente por el menú lateral de la aplicación soportando hasta 3 niveles de profundidad.
     *
     * <p>El metodo interpreta la ruta recibida en {@code path}, separada por el carácter {@code '>'},
     * y localiza los elementos visibles en el menú basándose en etiquetas <span> que contienen las clases
     * {@code text} o {@code title}. Se encarga de abrir submenús colapsados y manejar los overlays generados
     * dinámicamente por Angular CDK.</p>
     *
     * <p>Comportamiento:</p>
     * <ul>
     *   <li><b>Nivel 1:</b> Busca el texto del primer nivel y lo abre si está colapsado
     *       (verifica el atributo {@code aria-expanded}).</li>
     *   <li><b>Nivel 2:</b> Una vez expandido el primer nivel, busca la opción en el overlay del CDK
     *       y hace clic en ella.</li>
     *   <li><b>Nivel 3:</b> Si existe, espera a que se renderice un nuevo overlay y selecciona la opción.</li>
     * </ul>
     *
     * <p>Ejemplos de rutas válidas:</p>
     * <ul>
     *   <li>{@code "Plan de producción"}</li>
     *   <li>{@code "Producción > Planificación"}</li>
     *   <li>{@code "Producción > Planificación > Plan de producción"}</li>
     * </ul>
     *
     * <p>Restricciones:</p>
     * <ul>
     *   <li>La ruta debe tener entre 1 y 3 niveles.</li>
     *   <li>El metodo lanza {@link IllegalArgumentException} si se proporciona un número de niveles inválido.</li>
     * </ul>
     *
     * @param path Ruta completa separada por {@code '>'} que indica la jerarquía del menú a navegar.
     *             Cada nivel corresponde al texto visible de un elemento del menú.
     *
     * @throws IllegalArgumentException si la ruta no contiene entre 1 y 3 niveles.
     * @throws org.openqa.selenium.TimeoutException si alguno de los elementos esperados no aparece
     *         dentro del tiempo máximo definido en {@code waitUtil}.
     */
    public void selectNestedModule(String path) {
        String[] levels = path.split(">");
        if (levels.length == 0 || levels.length > 3) {
            throw new IllegalArgumentException("El path debe tener entre 1 y 3 niveles separados por '>'");
        }

        String nivel1 = levels[0].trim();
        String nivel2 = levels.length >= 2 ? levels[1].trim() : null;
        String nivel3 = levels.length == 3 ? levels[2].trim() : null;

        // 0) Asegúrate de que no haya overlays activos antes de buscar el Nivel 1
        By cdkOverlayMenu = By.cssSelector(".cdk-overlay-pane .cdk-menu");
        waitUtil.waitForInvisibility(cdkOverlayMenu, "overlays activos", 10000, 500);

        // 1) Localiza el primer nivel SOLO en el menú lateral (excluye overlays)
        //    - Busca el contenedor clickable (button/div con cdk-menu-item) ANCESTOR del span de texto.
        By seccionNivel1 = By.xpath("//span[normalize-space(.)='" + nivel1 + "']/ancestor::a[1]");

        clickByLocator(seccionNivel1, nivel1);

//        WebElement seccionNivel1Elem = waitUtil.waitForVisibilityByLocator(seccionNivel1);

//        singleClick(seccionNivel1Elem, nivel1);

//        clickByLocator(seccionNivel1, "Opción de menú Nivel 1: " + nivel1);

//        WebElement primerNivel = waitUtil.findVisibleElement(seccionNivel1);
//
//        // 2) Abre el submenú si está colapsado (o haz clic si es hoja)
//        String expanded = primerNivel.getAttribute("aria-expanded");
//
//        // Intento de click normal; si intercepta o hay overlay, fallback a JS
//        try {
//            // Asegura clickeabilidad del CONTENEDOR
//            waitUtil.waitForElementToBeClickable(primerNivel).click();
//        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
//            // Quita overlays, reintenta con JS
//            waitUtil.waitForInvisibility(cdkOverlayMenu, "overlays activos", 10000, 500);
//            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", primerNivel);
//        }

        // Si NO hay nivel 2 ni 3, terminamos (era una opción de nivel 1)
        if (nivel2 == null && nivel3 == null) {
            return;
        }

        // 3) Espera a que aparezca el overlay del segundo nivel
        waitUtil.waitForVisibilityByLocator(cdkOverlayMenu);

        // Helper para buscar un item por su texto dentro de overlay CDK (exacto)
        java.util.function.Function<String, By> opcionEnOverlay = txt -> By.xpath(
                "//div[contains(@class,'cdk-overlay-pane')]//div[contains(@class,'cdk-menu')]//span" +
                        "[(contains(@class,'text') or contains(@class,'title')) and normalize-space(.)='" + txt + "']" +
                        "/ancestor::*[(self::button or self::div) and contains(@class,'cdk-menu-item')][1]"
        );

        if (nivel2 != null) {
            WebElement segundoNivel = waitUtil.waitForPresenceOfElement(opcionEnOverlay.apply(nivel2));
            try {
                clickByElement(segundoNivel, nivel2);
//                segundoNivel.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", segundoNivel);
            }

            // Si hay tercer nivel, normalmente abre otro overlay; espera de nuevo
            if (nivel3 != null) {
                WebElement tercerNivelOverlay = waitUtil.waitForPresenceOfElement(cdkOverlayMenu);
                clickByElement(tercerNivelOverlay, "overlay tercer nivel");
//                waitUtil.waitForVisibilityByLocator(cdkOverlayMenu);
            }
        }

        if (nivel3 != null) {
            WebElement tercerNivel = waitUtil.waitForElementToBeClickable(opcionEnOverlay.apply(nivel3));
            try {
                tercerNivel.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", tercerNivel);
            }
        }
    }
}