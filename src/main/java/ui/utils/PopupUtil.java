package ui.utils;

import org.junit.Assert;
import org.openqa.selenium.*;
import ui.manager.PageManager;

/**
 * Clase utilitaria para manejar popups o cuadros de diálogo modales dentro de la aplicación web.
 *
 * Proporciona métodos para detectar, interactuar y validar mensajes en ventanas emergentes.
 * Se utiliza en flujos donde pueden aparecer confirmaciones, errores u otros mensajes importantes que el usuario debe
 * gestionar.
 */
public class PopupUtil {

    private final WebDriver driver;
    private final WaitUtil waitUtil;

    /**
     * Constructor que inicializa la clase con el WebDriver y el PageManager.
     *
     * @param driver       Instancia del navegador WebDriver.
     * @param pageManager  Instancia del administrador de páginas del framework.
     */
    public PopupUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.waitUtil = pageManager.getWaitUtil();
    }

    /**
     * Verifica que el mensaje de error dentro del contenedor de errores coincida con el esperado.
     *
     * <p>Este metodo busca el mensaje en el componente con clase <code>title-container</code>,
     * que se encuentra dentro de un <code>div.error-container</code> mostrado en la UI.</p>
     *
     * @param expectedMessage Texto del mensaje de error que se espera validar.
     */
    public void verifyErrorTitleMessage(String expectedMessage) {
        try {
//            waitUtil.sleepMillis(300, "Espera que se muestre el mensaje");
//            By errorTitleLocator = By.cssSelector("div.error-container .title-container");
//            Nuevo localizador
            By errorMsgLocator = By.xpath("(//div[contains(@class,'log') and contains(@class,'http-error')]//div[contains(@class,'message')])[last()]");
            WebElement errorMsgElement = waitUtil.findVisibleElement(errorMsgLocator);

            LogUtil.info("Esperando visibilidad de: " + errorMsgElement);
            waitUtil.waitForVisibilityByElement(errorMsgElement);
            waitUtil.waitForText(errorMsgElement, expectedMessage);

            String actualText = errorMsgElement.getText().trim();
            LogUtil.info("Comparando texto exacto de Mensaje de error en contenedor esperado = '" + expectedMessage + "', actual = '" + actualText + "'");

            Assert.assertEquals("El texto del elemento no coincide exactamente.", expectedMessage, actualText);
            LogUtil.info("Validación exitosa: el texto coincide exactamente con '" + expectedMessage + "'");
        } catch (Exception e) {
            LogUtil.error("Error al verificar el mensaje de error del contenedor: " + expectedMessage, e);
            throw new RuntimeException("El mensaje de error no se encontró o no coincide: " + expectedMessage, e);
        }
    }

    /**
     * Verifica si se muestra un popup de error y lanza una aserción si está presente.
     *
     * <p>Este metodo se puede usar al final de un flujo para validar que **no** aparecieron errores inesperados.
     * Si el popup está presente, el test falla con el mensaje mostrado.</p>
     */
    public void assertNoErrorPopupPresent() {
        try {
            By errorPopupLocator = By.cssSelector("div.errors-container");
            By errorMessageLocator = By.cssSelector("div.errors-container .title-container");

            waitUtil.sleepMillis(200, "Esperando brevemente antes de verificar popup de error");

            WebElement popup = driver.findElement(errorPopupLocator);
            if (popup.isDisplayed()) {
                WebElement messageElement = popup.findElement(errorMessageLocator);
                String message = messageElement.getText().trim();
                LogUtil.error("Se detectó un popup de error inesperado: '" + message + "'");
                Assert.fail("Se detectó un popup de error inesperado: '" + message + "'");
            }
        } catch (NoSuchElementException e) {
            // No hay popup, no hacemos nada
        } catch (Exception e) {
            LogUtil.error("Error al verificar el popup de error", e);
            throw new RuntimeException("Error inesperado al verificar popup de error", e);
        }
    }

    /**
     * Obtiene el texto del mensaje visible en el contenedor de mensajes del sistema.
     *
     * @return Texto del mensaje si se encuentra, o cadena vacía en caso contrario.
     */
    public String getMessageText() {
        try {
            WebElement messageElement = driver.findElement(By.xpath("//div[contains(@class,'message-container')]"));
            return messageElement.getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}