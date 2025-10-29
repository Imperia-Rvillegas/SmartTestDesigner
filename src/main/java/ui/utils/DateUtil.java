package ui.utils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para la generación y formateo de fechas basadas en la fecha actual del sistema.
 *
 * <p>Provee el metodo {@link #getToday(String)} para retornar la fecha de hoy siguiendo
 * un patrón de formateo personalizado. Internamente se apoya en {@link DateTimeFormatter}
 * y deja trazabilidad mediante {@link LogUtil} sobre el patrón recibido, el patrón efectivo
 * utilizado y el resultado obtenido.</p>
 */
public final class DateUtil {

    private static final String DEFAULT_PATTERN = "dd-MM-yyyy";

    private DateUtil() {
        throw new IllegalStateException("Clase utilitaria, no instanciable");
    }

    /**
     * Obtiene la fecha actual formateada según el patrón indicado.
     *
     * <p>Si el patrón proporcionado es nulo o está vacío, se utiliza el patrón por defecto
     * {@value #DEFAULT_PATTERN}. Si el patrón no es válido para {@link DateTimeFormatter#ofPattern(String)},
     * se registra el error en los logs y se lanza una {@link IllegalArgumentException} detallando la causa.</p>
     *
     * @param pattern Patrón de formato compatible con {@link DateTimeFormatter#ofPattern(String)}.
     * @return Cadena representando la fecha de hoy con el patrón solicitado.
     * @throws IllegalArgumentException Cuando el patrón no es válido.
     */
    public static String getToday(String pattern) {
        LogUtil.info("Solicitando fecha actual con patrón: '" + pattern + "'");

        String effectivePattern = (pattern == null || pattern.trim().isEmpty())
                ? DEFAULT_PATTERN
                : pattern.trim();

        if (pattern == null || pattern.trim().isEmpty()) {
            LogUtil.info("Patrón recibido nulo o vacío. Se utilizará el patrón por defecto: '" + effectivePattern + "'");
        } else if (!effectivePattern.equals(pattern)) {
            LogUtil.info("Patrón recibido con espacios. Se utilizará la versión normalizada: '" + effectivePattern + "'");
        } else {
            LogUtil.info("Patrón efectivo a utilizar: '" + effectivePattern + "'");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(effectivePattern);
            String today = LocalDate.now().format(formatter);
            LogUtil.info("Fecha actual formateada: " + today);
            return today;
        } catch (IllegalArgumentException | DateTimeException e) {
            LogUtil.error("No fue posible aplicar el patrón de fecha: '" + effectivePattern + "'", e);
            throw new IllegalArgumentException(
                    "Patrón de fecha inválido: '" + effectivePattern + "'. Verifique la documentación de DateTimeFormatter.",
                    e);
        }
    }
}
