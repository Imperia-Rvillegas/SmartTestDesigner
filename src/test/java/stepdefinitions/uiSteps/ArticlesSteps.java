package stepdefinitions.uiSteps;

import hooks.Hooks;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import testmodel.articlesData;
import testmodel.unitData;
import ui.manager.PageManager;
import ui.pages.ArticlesPage;
import ui.utils.ScreenshotUtil;
import ui.utils.TestDataLoader;

/**
 * Clase de definiciones de pasos para la gestión de artículos en pruebas automatizadas con Cucumber.
 *
 * <p>Contiene los pasos definidos en Gherkin relacionados con la pantalla de artículos,
 * como completar datos, buscar, aplicar filtros, seleccionar, validar guardado, descargar Excel,
 * ajustar columnas y validar presencia o ausencia de artículos.</p>
 *
 * <p>Estos pasos interactúan con la clase {@link ArticlesPage}, que encapsula la lógica de la interfaz web,
 * y con {@link ScreenshotUtil}, para capturar evidencias visuales durante la ejecución.</p>
 */
public class ArticlesSteps {

    private final PageManager pageManager = Hooks.getPageManager();
    private final ArticlesPage articlesPage = pageManager.getArticlesPage();
    private final String newArticleCode;
    private final String newArticleDescription;

    private final unitData unitData;
    private final String unitNameToAssociateWithTheArticle;

    /**
     * Constructor que carga los datos desde el archivo YAML correspondiente
     * y los asigna a variables reutilizables para los pasos.
     */
    public ArticlesSteps() {
        //Articles
        articlesData articlesData = TestDataLoader.load("testdata/page/articles.yaml", articlesData.class);
        this.newArticleCode = articlesData.getNewArticleCode();
        this.newArticleDescription = articlesData.getNewArticleDescription();
        //Unit
        this.unitData = TestDataLoader.load("testdata/page/unit.yaml", unitData.class);
        this.unitNameToAssociateWithTheArticle = unitData.getUnitNameToAssociateWithTheArticle();
    }

    /**
     * Completa los campos obligatorios del formulario de creación de artículos.
     *
     * @param code        Código del artículo.
     * @param description Descripción del artículo.
     */
    @When("ingresa codigo y descripcion para el nuevo articulo")
    public void enterCodeAndDescriptionForTheNewArticle() {
        articlesPage.fillMandatoryFields(newArticleCode, newArticleDescription);
    }

    /**
     * Valida que el artículo haya sido guardado correctamente.
     * Se captura una evidencia visual del estado resultante.
     */
    @Then("el sistema indica que fue guardado")
    public void validateSave() {
        articlesPage.validateSaved();
    }

    /**
     * Selecciona un artículo específico de la lista.
     *
     * @param article Nombre o descripción del artículo a seleccionar.
     */
    @When("selecciona el articulo creado")
    public void selectArticle() {
        articlesPage.selectArticle(newArticleDescription);
    }

    /**
     * Valida que una pantalla específica se haya mostrado correctamente.
     * Se captura evidencia visual con el nombre de la pantalla.
     *
     * @param screenName Nombre de la pantalla esperada.
     */
    @Then("el sistema muestra la pantalla {string}")
    public void validateDisplayedScreen(String screenName) {
        articlesPage.validateDisplayedScreen(screenName);
    }

    /**
     * Valida que un artículo ya no esté presente en el listado.
     * Se captura una evidencia visual de esta validación.
     *
     * @param article Nombre o descripción del artículo esperado a desaparecer.
     */
    @Then("el articulo creado ya no aparece en los resultados")
    public void validateProductNotPresent() {
        articlesPage.assertArticleNotListed(newArticleDescription);
    }

    /**
     * Selecciona la primera unidad base disponible desde el selector de unidades.
     *
     * <p>Este paso es útil para cumplir con el requisito del campo obligatorio "Unidad",
     * seleccionando automáticamente la primera unidad listada.</p>
     */
    @When("selecciona la primera unidad base disponible")
    public void selectBaseUnit() {
        articlesPage.selectUnit();
    }

    /**
     * Completa el campo "Código" del artículo con el valor especificado.
     *
     * <p>Este paso se utiliza cuando se quiere validar el comportamiento del formulario
     * completando únicamente el campo de código.</p>
     *
     * @param code valor a ingresar en el campo Código del artículo.
     */
    @And("Ingresa el campo codigo del articulo")
    public void completeCodeField() {
        articlesPage.enterCode(newArticleCode);
    }

    /**
     * Busca el artículo previamente creado utilizando su descripción.
     * Se realiza una búsqueda dentro de la tabla de artículos aplicando un filtro por descripción.
     * Utiliza la variable {@code newArticleDescription} y la columna "Descripción".
     */
    @When("busca el articulo creado")
    public void searchTheArticleCreated() {
        articlesPage.searchTheArticleCreated(newArticleDescription);
    }

    /**
     * Verifica que el artículo creado previamente aparece en los resultados de búsqueda.
     * La validación se realiza comparando la descripción del artículo visible con {@code newArticleDescription}.
     */
    @Then("el articulo creado aparece en los resultados")
    public void articleAppearsInResults() {
        articlesPage.ArticleAppearsInResults(newArticleDescription);
    }

    /**
     * Aplica un filtro en la tabla de artículos utilizando el código del artículo.
     * Se usa la columna "Código" y el valor {@code newArticleCode} como criterio de filtrado.
     */
    @When("aplica filtro por codigo de articulo")
    public void applyFilterByCode() {
        articlesPage.applyFilterByCode(newArticleCode);
    }

    /**
     * Verifica que el artículo filtrado por código aparece en los resultados.
     * La validación asegura que el código {@code newArticleCode} está presente en la tabla.
     */
    @Then("el articulo filtrado por codigo aparece en los resultados")
    public void codeAppearsInResults() {
        articlesPage.codeAppearsInResults(newArticleCode);
    }

    /**
     * Aplica un filtro en la tabla de artículos utilizando la descripción del artículo.
     * Se usa la columna "Descripción" y el valor {@code newArticleDescription} como criterio de filtrado.
     */
    @When("aplica filtro por descripcion de articulo")
    public void applyFilterByDescription() {
        articlesPage.applyFilterByDescription("Descripción", newArticleDescription);
    }

    /**
     * Verifica que el artículo filtrado por descripción aparece en los resultados.
     * Comprueba que la descripción {@code newArticleDescription} se encuentra visible en la tabla.
     */
    @Then("el articulo filtrado por descripcion aparece en los resultados")
    public void articleFilteredByDescriptionAppears() {
        articlesPage.articleFilteredByDescriptionAppears(newArticleDescription);
    }

    /**
     * Selecciona la unidad previamente creada como unidad base en la pantalla de artículos.
     * <p>
     * Este paso asume que {@code unitNameToAssociateWithTheArticle} ya ha sido definido
     * anteriormente en el flujo de prueba, y que dicha unidad se encuentra disponible
     * en la lista de selección correspondiente.
     * </p>
     *
     * Paso Gherkin asociado: {@code And selecciona la unidad creada como unidad base}
     */
    @And("selecciona la unidad creada como unidad base")
    public void selectTheCreatedUnit() {
        articlesPage.selectTheCreatedUnit(unitNameToAssociateWithTheArticle);
    }

    @And("verifica que el articulo {string} existe en la lista de maestro de artículos")
    public void verificaQueElArticuloExisteEnLaListaDeMaestroDeArtículos(String article) {
        articlesPage.searchTheArticleCreated(article);
        articlesPage.ArticleAppearsInResults(article);
    }
}
