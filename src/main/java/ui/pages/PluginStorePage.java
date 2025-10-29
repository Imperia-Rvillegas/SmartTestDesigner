package ui.pages;

import api.UserProfileAPI;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.LogUtil;

import java.util.Map;

/**
 * Representa la página de plugin store.
 */
public class PluginStorePage extends BasePage {

    // Localizadores de elementos
    private final By avatarImperia = By.cssSelector("imp-avatar.cdk-menu-trigger[role='button'][aria-haspopup='menu']");
    private final By searchInput = By.cssSelector("input[type='text'][placeholder^='Buscar']");
    private final By btnClose = By.cssSelector(".header-buttons-wrapper imperia-icon-button[icon='x'] a[role='button']");

    public PluginStorePage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Verifica si un plugin está activo y, si no lo está, ejecuta los pasos necesarios para habilitarlo.
     *
     * <p><strong>Flujo</strong></p>
     * <ol>
     *   <li>Registra en log el inicio de la verificación.</li>
     *   <li>Obtiene el perfil del usuario vía {@link UserProfileAPI#getUserProfile()}.</li>
     *   <li>Evalúa si el plugin indicado está activo con {@link #checkActivePlugin(io.restassured.response.Response, String)}.</li>
     *   <li>Si <em>no</em> está activo:
     *     <ul>
     *       <li>Registra advertencia en el log.</li>
     *       <li>Lo contrata/habilita con {@link #hirePlugin(String)}.</li>
     *       <li>Refresca la página con {@link #refreshPage()} para aplicar cambios.</li>
     *     </ul>
     *   </li>
     *   <li>Si ya está activo, no realiza ninguna acción adicional (idempotente).</li>
     * </ol>
     *
     * <p><strong>Precondiciones</strong></p>
     * <ul>
     *   <li>La sesión del usuario tiene permisos para contratar/habilitar plugins.</li>
     *   <li>El nombre visible del plugin coincide con el esperado por las APIs/UI.</li>
     * </ul>
     *
     * <p><strong>Efectos colaterales</strong></p>
     * <ul>
     *   <li>Puede modificar la configuración de plugins del usuario.</li>
     *   <li>Fuerza un refresco de la UI para reflejar el cambio de estado.</li>
     * </ul>
     *
     * <p><strong>Excepciones</strong></p>
     * <ul>
     *   <li>Este metodo no captura ni transforma errores de las dependencias; cualquier
     *       excepción de red/API/UI puede propagarse en tiempo de ejecución.</li>
     * </ul>
     *
     * @param pluginName nombre visible del plugin a verificar/activar (por ejemplo, {@code "Conceptos de previsión"}).
     *
     * @see UserProfileAPI#getUserProfile()
     * @see #checkActivePlugin(io.restassured.response.Response, String)
     * @see #hirePlugin(String)
     * @see #refreshPage()
     */
    public void checkThatThePluginsIsActive(String pluginName) {
        LogUtil.info("Verificar si el plugin '" + pluginName + "' está activo");

        Response response = UserProfileAPI.getUserProfile();

        boolean isActive = checkActivePlugin(response, pluginName);

        if (!isActive) {
            // Instala el plugin
            LogUtil.warn("Instalando plugin: " + pluginName);

            // Contrata el plugin
            hirePlugin(pluginName);

            // Refresca la página para actualizar los cambios
            refreshPage();
        }
    }

    /**
     * Contrata/habilita un plugin desde la tienda de plugins de la aplicación.
     * <p><strong>Flujo UI:</strong></p>
     * <ol>
     *   <li>Clic en el avatar de Imperia (abre menú de usuario).</li>
     *   <li>Clic en <em>Plugin store</em>.</li>
     *   <li>Clic en <em>Ver todos</em> para listar todos los plugins.</li>
     *   <li>Escribe el nombre del plugin en el buscador.</li>
     *   <li>Clic en <em>Contratar</em> dentro de la tarjeta del plugin cuyo título coincide con {@code namePlugin}.</li>
     *   <li>Clic en <em>Aceptar</em> para confirmar.</li>
     *   <li>Cierra el modal/ventana con el botón de cierre.</li>
     * </ol>
     *
     * <p><strong>Precondiciones:</strong> sesión iniciada con permisos para contratar plugins;
     * elementos visibles/alcanzables en el flujo descrito.</p>
     *
     * <p><strong>Excepciones:</strong> Puede propagar {@link org.openqa.selenium.NoSuchElementException},
     * {@link org.openqa.selenium.TimeoutException} o errores de interacción si la UI cambia, hay latencias
     * o el nombre no coincide exactamente.</p>
     *
     * @param namePlugin nombre visible del plugin a contratar (coincidencia exacta con el título de la tarjeta).
     *
     * @implNote El localizador para el botón <em>Contratar</em> usa un XPath relativo a la tarjeta del plugin:
     * <pre>{@code
     * //p[normalize-space(.)='NAME']/ancestor::div[contains(@class,'plugin-card-container')]
     *   //span[normalize-space(.)='Contratar']
     * }</pre>
     * Si hay cambios de i18n/etiquetas, extrae los textos a constantes o usa selectores más robustos.
     *
     * @see #clickByLocator(org.openqa.selenium.By, String)
     * @see #clickButtonByName(String)
     * @see #sendKeysByLocator(org.openqa.selenium.By, String, String)
     */
    private void hirePlugin(String namePlugin) {
        // Clic en el avatar de imperia
        clickByLocator(avatarImperia, "Avatar de imperia");

        // Clic en Plugin store
        clickButtonByName("Plugin store");

        // Clic en ver todos
        clickButtonByName("Ver todos");

        // Buscar el plugin
        sendKeysByLocator(searchInput, namePlugin, "Buscar");

        // Clic en contratar
        clickByLocator(
                By.xpath("//p[normalize-space(.)='" + namePlugin + "']"
                        + "/ancestor::div[contains(@class,'plugin-card-container')]"
                        + "//span[normalize-space(.)='Contratar']"),
                "Boton contratar"
        );

        // Clic en aceptar
        clickButtonByName("Aceptar");

        // Clic en cerrar la ventana
        clickByLocator(btnClose, "Boton cerrar ventana");
    }

    /**
     * Verifica si un plugin está activo consultando la sección <code>$.ActivePlugins</code>
     * del JSON devuelto por el perfil de usuario.
     * <p>
     * Funcionamiento:
     * <ol>
     *   <li>Convierte el cuerpo de la respuesta en cadena.</li>
     *   <li>Mapea el nombre visible del plugin (insensible a mayúsculas) a su clave de API.</li>
     *   <li>Lee <code>$.ActivePlugins</code> con JsonPath.</li>
     *   <li>Devuelve {@code true} si existe la clave del plugin dentro de <code>ActivePlugins</code>;
     *       en caso contrario, {@code false}.</li>
     *   <li>Si la ruta <code>$.ActivePlugins</code> no existe, registra un <b>warn</b> y devuelve {@code false}.</li>
     * </ol>
     * </p>
     *
     * <p><b>Notas:</b>
     * <ul>
     *   <li>Si el nombre visible no está contemplado en el mapeo, se lanza
     *       {@link IllegalArgumentException}.</li>
     *   <li>El metodo escribe mensajes informativos en el log sobre la presencia del plugin.</li>
     * </ul>
     * </p>
     *
     * @param response           Respuesta HTTP (RestAssured) que contiene el JSON con <code>ActivePlugins</code>.
     * @param visiblePluginName  Nombre visible del plugin (por ejemplo, "Conceptos de previsión").
     * @return {@code true} si el plugin está presente en <code>ActivePlugins</code>; {@code false} en caso contrario
     *         o si no existe la sección.
     * @throws IllegalArgumentException si el nombre visible del plugin no está mapeado.
     */
    public boolean checkActivePlugin(Response response, String visiblePluginName) {
        // Convertir el cuerpo de la respuesta en String
        String jsonResponse = response.getBody().asString();

        // Convertir el nombre visible del plugin a la clave API
        String keyPluginApi;

        switch (visiblePluginName.toLowerCase()) {
            case "conector api":
                keyPluginApi = "API_CONNECTOR";
                break;
            case "almacenes":
                keyPluginApi = "WAREHOUSES";
                break;
            case "múltiples proveedores por configuración":
                keyPluginApi = "MULTIPLE_SUPPLIERS_BY_CONFIG";
                break;
            case "recalcular línea base":
                keyPluginApi = "RECALCULATE_BASELINE";
                break;
            case "planificación de requerimientos de material (mrp)":
                keyPluginApi = "SHOW_PURCHASE_MODULE";
                break;
            case "módulo de gestión de inventarios":
                keyPluginApi = "INVENTORY_MODULE";
                break;
            case "productos nuevos sin historial":
                keyPluginApi = "NEW_PRODUCTS_REPORT_WITHOUT_HISTORY";
                break;
            case "gestor de pedidos pendientes":
                keyPluginApi = "CONFIRMED_ORDERS_FORECAST_CALCULATION";
                break;
            case "sustitución de productos":
                keyPluginApi = "SIMILAR_PRODUCTS_MULTIPLIER";
                break;
            case "plan maestro de producción (mps)":
                keyPluginApi = "SHOW_PRODUCTION_MODULE";
                break;
            case "meses personalizados para previsión":
                keyPluginApi = "CUSTOM_FORECASTS_MONTHS";
                break;
            case "informe mps/mrp":
                keyPluginApi = "MRP_MPS_REPORT";
                break;
            case "secuenciador de producción":
                keyPluginApi = "PRODUCTION_SCHEDULING";
                break;
            case "tiempo de cambio en proceso de producción matriz":
                keyPluginApi = "MATRIX_PRODUCTION_PROCESS_CHANGE_TIME";
                break;
            case "truncar antes de cargar":
                keyPluginApi = "TRUNCATE_BEFORE_LOAD";
                break;
            case "periodo de producción del producto":
                keyPluginApi = "PRODUCT_PRODUCTION_PERIOD";
                break;
            case "carga de datos con múltiples plantillas":
                keyPluginApi = "MULTI_TEMPLATES_DATA_LOAD";
                break;
            case "exportación de datos":
                keyPluginApi = "DATA_EXPORT";
                break;
            case "conceptos de previsión":
                keyPluginApi = "FORECAST_CONCEPTS";
                break;
            case "presupuesto comercial":
                keyPluginApi = "LOAD_BUDGETS_DIFFERENT_HIERARCHY";
                break;
            default:
                throw new IllegalArgumentException("Nombre visible del plugin no reconocido: " + visiblePluginName);
        }

        try {
            Map<String, Object> plugins = JsonPath.read(jsonResponse, "$.ActivePlugins");

            if (plugins.containsKey(keyPluginApi)) {
                LogUtil.info("El plugin '" + visiblePluginName + "' está presente.");
                return true;
            } else {
                LogUtil.info("El plugin '" + visiblePluginName + "' no se encuentra activo.");
                return false;
            }
        } catch (PathNotFoundException e) {
            LogUtil.warn("No se encontró la sección 'ActivePlugins' en la respuesta JSON.");
            return false;
        }
    }
}