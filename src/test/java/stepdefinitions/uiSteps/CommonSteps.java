package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.By;
import ui.enums.AccessType;
import ui.manager.PageManager;
import ui.base.BasePage;
import ui.pages.HomePage;
import ui.pages.MenuPage;
import ui.pages.LoginPage;
import ui.pages.PluginStorePage;
import ui.utils.*;

import static ui.enums.AccessType.*;

/**
 * Clase de definición de pasos comunes para escenarios de pruebas UI con Cucumber.
 * <p>
 * Esta clase contiene pasos reutilizables que permiten navegar por módulos de la aplicación,
 * interactuar con botones comunes como "Calcular" y validar elementos visuales como el semáforo.
 * </p>
 * <p>
 * Utiliza el {@link PageManager} para acceder a las páginas específicas del sistema según el módulo seleccionado.
 * </p>
 */
public class CommonSteps {

    /**
     * Gestor de páginas que proporciona acceso a todas las páginas y utilidades del framework UI.
     */
    private final PageManager pageManager = Hooks.getPageManager();
    private final ValidationUtil validationUtil = pageManager.getValidationUtil();
    private final TableUtil tableUtil = pageManager.getTableUtil();
    private final WaitUtil waitUtil = pageManager.getWaitUtil();
    private final ScreenshotUtil screenshotUtil = pageManager.getScreenshotUtil();
    private final NavigationUtil navigationUtil = pageManager.getNavigationUtil();
    private final TrafficLightUtil trafficLightUtil = pageManager.getTrafficLightUtil();
    //Pages
    private final BasePage basePage = pageManager.getBasePage();
    private final HomePage homePage = pageManager.getHomePage();
    private final MenuPage menuPage = pageManager.getMenuPage();
    private final LoginPage loginPage = pageManager.getLoginPage();
    private final PluginStorePage pluginStorePage = pageManager.getPluginStorePage();


    /**
     * Navega a una vista específica de la aplicación mediante un tipo de menú determinado
     * ("Rápido", "Indice", "Buscar") y realiza configuraciones adicionales según la pantalla accedida.
     *
     * Este metodo se utiliza como paso de inicio en escenarios de prueba para posicionar al usuario
     * en el módulo correspondiente. El tipo de acceso se interpreta a partir del valor proporcionado
     * en {@code menuType} y se convierte en una instancia de {@code AccessType}.
     *
     * Además de navegar al módulo especificado, el metodo configura la posición de la tabla esperada
     * en pantalla mediante la variable {@code tablePosition}, la cual varía según la pantalla accedida.
     *
     * En caso de que el nombre del módulo no coincida con los casos predefinidos, se registra una advertencia
     * y no se configura {@code tablePosition}.
     *
     * @param modulePath el nombre visible de la pantalla a la que se desea acceder
     *                   (por ejemplo, "Plan de Compras", "Previsiones", "Unidades", "producción > planificación > plan de producción").
     * @param menuType   el tipo de menú desde el cual se accede al módulo. Valores esperados:
     *                   <ul>
     *                     <li>"Rápido"</li>
     *                     <li>"Indice"</li>
     *                     <li>"Buscar", "Buscador", o "Lupa"</li>
     *                   </ul>
     * @throws IllegalArgumentException si el tipo de menú especificado no está soportado.
     */
    @Given("el usuario está en la pantalla {string} desde menú {string}")
    public void goToView(String modulePath, String menuType) {
        AccessType accessType;

        switch (menuType.trim().toLowerCase()) {
            case "rápido":
                accessType = QUICK;
                break;
            case "indice":
                accessType = INDEX;
                break;
            case "buscar":
            case "buscador":
            case "lupa":
                accessType = SEARCH;
                break;
            default:
                throw new IllegalArgumentException("Tipo de menú no soportado: " + menuType);
        }

        LogUtil.info("Accediendo al módulo: " + modulePath + " desde menú: " + menuType + " (" + accessType + ")");
        navigationUtil.navigateToModule(modulePath, accessType);
    }

    /**
     * Acepta la confirmación que aparece después de pulsar el botón "Calcular".
     * <p>
     * Este paso simula la acción del usuario al confirmar el cálculo.
     * </p>
     */
    @And("acepta la confirmación")
    public void acceptsConfirmation() {
        basePage.acceptConfirmation();
    }

    /**
     * Verifica que el color del semáforo sea verde en la vista actual,
     * y captura una captura de pantalla como evidencia del resultado.
     */
    @Then("el color del semáforo debería ser verde en la tabla {string}")
    public void verifyTrafficLightGreen(String tableTitle) {
        trafficLightUtil.checkGreenLight(tableTitle);
    }

    /**
     * Paso que simula que el usuario navega a una pantalla especifica desde la interfaz principal.
     */
    @When("el usuario selecciona la pantalla {string}")
    public void userSelectModuleSettings(String screenName) {
        basePage.clickButtonByName(screenName);
    }

    /**
     * Paso que realiza clic en un botón especifico.
     */
    @When("hace clic en el botón {string}")
    public void clickOnButtom(String buttonName) {
        basePage.clickButtonByName(buttonName);
    }

    /**
     * Verifica que se muestre un mensaje indicando que un campo espesifico indica un mensaje espesifico.
     */
    @Then("el sistema muestra el mensaje {string} del campo {string}")
    public void messageFieldNameRequired(String message, String field) {
        validationUtil.messageFieldRequired(message, field);
    }

    /**
     * Verifica que se muestre un mensaje indicando un mensaje espesifico.
     */
    @Then("el sistema muestra el mensaje El campo es requerido")
    public void verifySpecificMessage() {
        validationUtil.verifyOnlyErrorMessage("El campo es requerido.");
    }

    /**
     * Verifica que se muestre un mensaje espesifico.
     */
    @Then("el sistema muestra el mensaje {string}")
    public void verifyErrorMessage(String message) {
        validationUtil.verifyErrorMessage(message);
        screenshotUtil.capture("Mensaje: " + message);
    }

    /**
     * Ajusta automáticamente las columnas visibles de la tabla.
     */
    @When("el sistema ajusta automáticamente las columnas")
    public void checkColumnAlignment() {
        validationUtil.checkColumnAlignment();
    }

    /**
     * Paso común de Cucumber para capturar una evidencia en forma de screenshot durante la ejecución del escenario.
     *
     * <p>Este paso espera brevemente antes de tomar la captura para asegurar que la página esté completamente renderizada.
     * El screenshot se guarda con el nombre especificado en el parámetro {@code description} y se registra en el log.</p>
     *
     * <p>Uso en archivo .feature:</p>
     * <pre>{@code
     * Y se captura evidencia "Formulario cargado"
     * }</pre>
     *
     * @param description Nombre descriptivo que se usará como identificador del archivo de evidencia (sin extensión).
     */
    @And("se captura evidencia {string}")
    public void captureEvidence(String description) {
        // Espera breve para asegurar que la interfaz haya terminado de cargar
        waitUtil.sleepMillis(300, "Asegurar que la interfaz haya terminado de cargar");

        // Captura y adjunta la captura de pantalla al reporte
        screenshotUtil.capture(description);

        // Registro en logs para trazabilidad
        LogUtil.info("Evidencia capturada: " + description + ".png");
    }

    /**
     * Ingresa el texto especificado en un campo de entrada identificado por su atributo placeholder.
     * <p>
     * Este paso se utiliza para completar formularios donde los campos están identificados por texto
     * de ayuda (placeholder) en lugar de etiquetas visibles.
     *
     * @param field Placeholder visible en el campo de entrada.
     * @param value Texto que se desea ingresar en el campo.
     */
    @When("ingresa en el campo con placeholder {string} el texto {string}")
    public void fillFieldByPlaceholder(String field, String value) {
        basePage.sendKeysByPlaceholder(field, value);
    }

    /**
     * Ingresa el texto especificado en un campo de entrada identificado por su título o etiqueta asociada.
     * <p>
     * Utilizado en formularios donde los campos tienen un título visible que los identifica.
     *
     * @param field Título visible asociado al campo de entrada.
     * @param value Texto a ingresar en el campo.
     */
    @When("ingresa en el campo con titulo {string} el texto {string}")
    public void fillInFieldByTitle(String field, String value) {
        basePage.sendKeysByTitle(field, value);
    }

    /**
     * Selecciona una fila en la tabla que contiene el valor indicado.
     * <p>
     * Este paso permite interactuar con registros visibles en una tabla, generalmente como
     * paso previo a editar o eliminar un elemento específico.
     *
     * @param value Valor visible en la fila que se desea seleccionar.
     */
    @And("selecciona el registro {string}")
    public void seleccionaElRegistro(String value) {
        basePage.clickByLocator(tableUtil.buildRecordLocator(value), value);
    }

    /**
     * Verifica que los botones especificados están visibles en la pantalla.
     * Se utiliza para comprobar la presencia de los botones.
     * @param buttons DataTable que contiene la lista de etiquetas de botones esperados.
     */
    @Then("^la pantalla muestra los botones:$")
    public void verifyButtonsVisible(DataTable buttons) {
        for (String buttonLabel : buttons.asList()) {
            Assert.assertTrue("El botón \"" + buttonLabel + "\" debería estar visible",
                    basePage.isButtonVisible(buttonLabel));
        }
    }

    /**
     * Hace clic fuera del campo editable (por ejemplo, en el body) para cerrar el campo y guardar.
     */
    @And("hace clic en el body para cerrar el campo editable y guardar los cambios")
    public void clickOutside() {
        basePage.clickOutside();
    }

    /**
     * Navega hasta una pantalla específica de la aplicación utilizando la ruta del módulo proporcionada.
     * <p>
     * Este paso abre el menú principal y selecciona un submódulo anidado especificado por {@code modulePath},
     * que puede incluir una jerarquía de módulos separados por " > " (por ejemplo, "Configuraciones > Unidades").
     * </p>
     *
     * Paso Gherkin asociado: {@code Dado navega hasta la pantalla "<ruta del módulo>"}
     *
     * @param modulePath la ruta del módulo hasta la pantalla deseada, en formato jerárquico.
     */
    @Given("navega hasta la pantalla {string}")
    public void navigateToScreen(String modulePath) {
        homePage.openMenu();
        menuPage.selectNestedModule(modulePath);
    }

    /**
     * Espera a que la tabla de previsiones se cargue completamente en la pantalla.
     * <p>
     * Este paso se utiliza para garantizar que todos los elementos de la tabla de previsiones estén
     * completamente renderizados y listos para interactuar antes de continuar con el siguiente paso del escenario.
     * Internamente, delega la espera a la lógica implementada en {@code forecastsPage.waitForTheForecastTableToLoad()}.
     * </p>
     *
     * Paso Gherkin asociado: {@code Y espera a que la tabla cargue los datos completamente}
     */
    @And("espera a que la tabla cargue los datos completamente")
    public void waitForTheForecastTableToLoad() {
        waitUtil.waitForTableToLoadCompletely();
    }

    /**
     * Quita todos los filtros aplicados en la pantalla de artículos
     * para evitar interferencias en los escenarios siguientes.
     */
    @And("quita filtro {string} para evitar errores en siguientes escenarios")
    public void removeFilters(String filter) {
        basePage.removeFilters(filter);
    }

    /**
     * Verifica que un mensaje específico se muestra en la pantalla.
     * Se utiliza para comprobar mensajes de error o alerta, como validaciones de campos obligatorios o el aviso de límite alcanzado.
     * @param expectedMessage El texto esperado del mensaje que debe mostrarse.
     */
    @Then("se muestra el mensaje {string}")
    public void verifyMessageDisplayed(String expectedMessage) {
        basePage.verifyMessageDisplayed(expectedMessage);
    }

    /**
     * Paso Given de Cucumber que verifica que el plugin indicado esté activo.
     * Inicia sesión y delega la verificación/activación en {@code pluginStorePage}.
     *
     * @param name Nombre visible del plugin.
     */
    @Given("se comprueba que el plugin {string} esta activo")
    public void checkThatThePluginsIsActive(String pluginName) {
        loginPage.loginAs();
        pluginStorePage.checkThatThePluginsIsActive(pluginName);
    }

    /**
     * Paso And de Cucumber que navega a la pantalla indicada desde el menú.
     * Abre el menú principal y selecciona el módulo anidado por su nombre.
     *
     * @param pantalla Nombre visible de la pantalla/módulo en el menú.
     */
    @And("el usuario accede a la pantalla {string} desde menú")
    public void theUserAccessesTheScreenFromMenu(String pantalla) {
        homePage.openMenu();
        menuPage.selectNestedModule(pantalla);
    }

    /**
     * Paso de Cucumber que hace clic en el botón indicado para guardar el archivo.
     * <p>
     * Este step delega la acción en {@code basePage.clickTheButtonToSaveTheFile(buttonName)},
     * que se encarga de localizar el botón por su texto/selector y ejecutar el clic.
     * </p>
     *
     * <h3>Uso en .feature</h3>
     * <pre>
     *   When hace clic en el botón "Guardar" para guardar el archivo
     * </pre>
     *
     * <h3>Parámetros</h3>
     * @param buttonName nombre visible o identificador lógico del botón a pulsar (por ejemplo, "Guardar").
     *
     * <h3>Precondiciones</h3>
     * <ul>
     *   <li>La página y el botón objetivo están cargados y visibles.</li>
     *   <li>El usuario/flujo se encuentra en el estado en el que el botón “{@code buttonName}” está disponible.</li>
     * </ul>
     *
     * <h3>Comportamiento</h3>
     * <ul>
     *   <li>Localiza el botón que coincide con {@code buttonName}.</li>
     *   <li>Hace clic y dispara el flujo de guardado/descarga correspondiente.</li>
     * </ul>
     *
     * <h3>Posibles excepciones (propagadas desde la capa de página)</h3>
     * <ul>
     *   <li>{@link java.lang.IllegalArgumentException} si {@code buttonName} es nulo o vacío.</li>
     *   <li>{@link org.openqa.selenium.NoSuchElementException} si no se encuentra el botón.</li>
     *   <li>{@link org.openqa.selenium.TimeoutException} si el botón no es clicable a tiempo.</li>
     * </ul>
     *
     * @see BasePage#clickTheButtonToSaveTheFile(String)
     */
    @When("hace clic en el botón {string} para guardar el archivo")
    public void clickTheButtonToSaveTheFile(String buttonName) {
        basePage.clickTheButtonToSaveTheFile(buttonName);
    }

    /**
     * Step de Cucumber que navega a una pantalla desde un menú específico.
     *
     * <p>Ejemplo en Gherkin:</p>
     * <pre>
     *   And el usuario accede a la pantalla "Presupuesto comercial" desde menu "Planificación"
     * </pre>
     *
     * @param screen     Nombre visible de la pantalla o módulo (ej. "Presupuesto comercial").
     * @param accessType Menú desde el cual se accede (ej. "Planificación").
     */
    @And("el usuario accede a la pantalla {string} desde menu {string}")
    public void specificAccess(String screen, String accessType) {
        navigationUtil.navigateToTheModuleFrom(screen, accessType);
    }

    /**
     * Verifica que un plugin específico se encuentre activo en la página de plugins.
     *
     * @param pluginName nombre del plugin que se debe comprobar como activo
     */
    @And("tambien se comprueba que el plugin {string} esta activo")
    public void itIsAlsoCheckedThatThePluginIsActive(String pluginName) {
        pluginStorePage.checkThatThePluginsIsActive(pluginName);
    }

    /**
     * Valida que se haya generado y descargado un archivo Excel.
     */
    @Then("el excel aparece en la carpeta de descargas")
    public void validateDownloadExcel() {
        basePage.assertExcelDownloaded();
    }

    @And("verifica que el hipervinculo {string} redirige a la url {string}")
    public void verificaQueElHipervinculoRedirigeALaUrl(String hipervinculo, String url) {
        basePage.verifyThatTheHyperlinkRedirectsToTheUrl(hipervinculo, url);
        validationUtil.assertCurrentUrlPath(url);
    }

    @And("el usuario va atrás en el navegador")
    public void elUsuarioVaAtrasEnElNavegador() {
        basePage.navigateBack();
    }

    @And("verifica que el registro {string} existe en la lista")
    public void verificaQueElRegistroExisteEnLaLista(String record) {
        basePage.searchRecord(record, By.cssSelector("div.input-container.expanded input[type='text'], input.search-bar[type='text']"));
        validationUtil.assertRecordListed(record);
    }
}