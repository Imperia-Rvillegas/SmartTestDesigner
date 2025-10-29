package ui.utils;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase utilitaria para el registro de logs durante la ejecución del framework.
 * Proporciona métodos para imprimir mensajes informativos, advertencias, errores,
 * y también para registrar peticiones y respuestas de servicios API (REST).
 *
 * Utiliza SLF4J como fachada para el manejo de logs.
 */
public class LogUtil {

    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    /**
     * Imprime un mensaje de tipo INFO en el log.
     *
     * @param message Mensaje a registrar.
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Imprime un mensaje de tipo WARN (advertencia) en el log.
     *
     * @param message Mensaje de advertencia a registrar.
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Imprime un mensaje de tipo WARN (advertencia) en el log junto con una excepción.
     *
     * @param message Mensaje de advertencia a registrar.
     * @param e       Excepción asociada a la advertencia.
     */
    public static void warn(String message, Throwable e) {
        logger.warn(message, e);
    }

    /**
     * Imprime un mensaje de tipo ERROR en el log sin necesidad de excepción.
     *
     * @param message Mensaje de error a registrar.
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Imprime un mensaje de tipo ERROR en el log junto con una excepción.
     *
     * @param message Mensaje de error a registrar.
     * @param e       Excepción asociada al error.
     */
    public static void error(String message, Throwable e) {
        logger.error(message, e);
    }

    /**
     * Registra los detalles de una petición HTTP enviada (principalmente para pruebas de API).
     *
     * @param method Metodo HTTP usado (GET, POST, etc.).
     * @param url    URL completa de la petición.
     * @param body   Cuerpo de la petición en formato JSON o texto plano.
     */
    public static void logRequest(String method, String url, String body) {
        logger.info("PETICIÓN HTTP");
        logger.info(" Método: {}", method);
        logger.info(" URL: {}", url);
        logger.info(" Body:\n{}", body);
    }

    /**
     * Registra los detalles de una respuesta HTTP recibida desde una API.
     *
     * @param response Objeto de respuesta de RestAssured.
     */
    public static void logResponse(Response response) {
        logger.info("RESPUESTA HTTP");
        logger.info("Código de estado: {}", response.getStatusCode());
        logger.info("Cuerpo de la respuesta:\n{}", response.asPrettyString());
    }

    /**
     * Marca el inicio de un escenario, sección o prueba en el log.
     *
     * @param title Título o descripción del bloque de ejecución que inicia.
     */
    public static void start(String title) {
        logger.info("INICIO: {}", title);
    }

    /**
     * Marca el final de un escenario, sección o prueba en el log.
     *
     * @param title Título o descripción del bloque de ejecución que finaliza.
     */
    public static void end(String title) {
        logger.info("FIN: {}", title);
    }

    /**
     * Imprime un mensaje de tipo INFO con formato, equivalente a System.out.printf.
     * Utiliza el mismo formato de placeholders de SLF4J: {}, {}, etc.
     *
     * @param format Mensaje con placeholders (ej: "Fila: {}, Columna: {}, Valor: {}")
     * @param args   Valores a reemplazar en los placeholders.
     */
    public static void infof(String format, Object... args) {
        logger.info(format, args);
    }


}