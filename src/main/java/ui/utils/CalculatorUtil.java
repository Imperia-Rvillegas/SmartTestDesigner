package ui.utils;

import api.UserProfileAPI;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Utilidad para realizar operaciones aritméticas y de conversión/formateo de valores numéricos.
 *
 * <p>Incluye métodos para:</p>
 * <ul>
 *   <li>Parsear cadenas en formatos numéricos europeos o americanos a {@link BigDecimal}.</li>
 *   <li>Formatear valores numéricos usando el separador decimal preferido del usuario.</li>
 *   <li>Operaciones aritméticas: suma, resta, multiplicación y división.</li>
 * </ul>
 *
 * <p>Los métodos son tolerantes a nulos y registran trazas mediante {@code LogUtil}.</p>
 */
public class CalculatorUtil {

    /** Separador decimal preferido del usuario: ',' o '.'. Fallback: '.' */
    private static char decimalSeparator;

    // ============================
    //        UTILIDADES
    // ============================

    /**
     * Calcula la diferencia absoluta entre dos valores numéricos en formato texto.
     * Si alguno no es numérico o es nulo, se usa 0.
     *
     * @param value1 primer valor como cadena.
     * @param value2 segundo valor como cadena.
     * @return diferencia absoluta como cadena.
     */
    public static String absoluteDifference(String value1, String value2) {
        BigDecimal v1;
        BigDecimal v2;

        try {
            v1 = (value1 != null) ? new BigDecimal(value1.trim()) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            v1 = BigDecimal.ZERO;
        }

        try {
            v2 = (value2 != null) ? new BigDecimal(value2.trim()) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            v2 = BigDecimal.ZERO;
        }

        BigDecimal result = v1.subtract(v2).abs();

        String resultStr = result.toPlainString();
        LogUtil.info("Diferencia absoluta entre valores: |" + v1 + " - " + v2 + "| = " + resultStr);

        return resultStr;
    }

    /**
     * Extrae solo la parte numérica de un texto, eliminando letras y espacios.
     *
     * <p>Ejemplos:
     * <ul>
     *   <li>"2 unidad" → "2"</li>
     *   <li>"2,1 unidad" → "2,1"</li>
     *   <li>"1.311,154 kilo" → "1.311,154"</li>
     * </ul>
     * </p>
     *
     * @param input Texto que contiene un número seguido de una unidad.
     * @return Parte numérica como String.
     */
    public static String extraerParteNumerica(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Elimina letras y espacios, conservando dígitos, comas y puntos
        return input.replaceAll("[^0-9.,]", "");
    }

    // ============================
    //   CONFIGURACIÓN DE FORMATO
    // ============================

    /**
     * Obtiene y establece el separador de decimales configurado según el formato numérico del usuario.
     * Lanza una excepción si no se puede obtener el formato.
     */
    public static void getDecimalSeparator() throws RuntimeException {
        // Validación: si ya tiene un valor asignado, no hacer nada
        if (decimalSeparator != '\u0000') {
            LogUtil.info("Separador de decimales ya establecido previamente: " + decimalSeparator);
            return;
        }

        try {
            decimalSeparator = getUserNumericFormat();
            LogUtil.info("Separador de decimales establecido: " + decimalSeparator);
        } catch (Exception e) {
            String msg = "No se pudo obtener el formato numérico del usuario. Motivo: " + e.getMessage();
            LogUtil.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    /**
     * Obtiene el formato numérico definido en el perfil del usuario desde la API de perfil.
     *
     * @return El carácter correspondiente al campo {@code NumericFormat} (',' o '.').
     * @throws RuntimeException si el campo no está presente, está vacío o ocurre un error al obtenerlo.
     */
    public static char getUserNumericFormat() {
        try {
            Response response = UserProfileAPI.getUserProfile();

            if (response == null) {
                throw new RuntimeException("La respuesta de UserProfileAPI.getUserProfile() es nula.");
            }

            JsonPath jsonPath = response.jsonPath();
            String numericFormat = jsonPath.getString("NumericFormat");

            if (numericFormat == null || numericFormat.isBlank()) {
                throw new RuntimeException("El campo 'NumericFormat' no está presente o está vacío en la respuesta.");
            }

            return numericFormat.charAt(0);

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el formato numérico del usuario: " + e.getMessage(), e);
        }
    }

    // ============================
    //   PARSEO Y FORMATEO BASE
    // ============================

    /**
     * Parsea un número con separador de decimales conocido y detección automática del separador de miles.
     *
     * @param valorCadena texto a parsear (puede incluir miles y signo).
     * @param sepDecimal  separador decimal conocido (',' o '.').
     * @return BigDecimal equivalente.
     * @throws IllegalArgumentException si el texto es nulo o vacío.
     */
    public static BigDecimal parseConSeparadorDecimal(String valorCadena, char sepDecimal) {
        if (valorCadena == null || valorCadena.isEmpty()) {
            throw new IllegalArgumentException("El valor no puede ser nulo o vacío");
        }

        // Limpia el string: deja solo dígitos, comas, puntos y signos
        valorCadena = valorCadena.trim().replaceAll("[^0-9,.-]", "");

        // Determina el separador de miles detectado (opuesto al separador decimal conocido)
        char sepMilesDetectado = (sepDecimal == ',') ? '.' : ',';

        // Si ambos separadores existen, determina cuál parece ser el separador de miles
        int ultimaComa = valorCadena.lastIndexOf(',');
        int ultimoPunto = valorCadena.lastIndexOf('.');

        if (ultimaComa >= 0 && ultimoPunto >= 0) {
            // Si el separador decimal conocido aparece después del otro, ese otro es el de miles
            if (sepDecimal == ',' && ultimoPunto < ultimaComa) {
                sepMilesDetectado = '.';
            } else if (sepDecimal == '.' && ultimaComa < ultimoPunto) {
                sepMilesDetectado = ',';
            }
        }

        // Elimina el separador de miles detectado
        String limpio = valorCadena.replace(String.valueOf(sepMilesDetectado), "");

        // Reemplaza el separador decimal conocido por punto (para BigDecimal)
        limpio = limpio.replace(sepDecimal, '.');

        return new BigDecimal(limpio);
    }

    /**
     * Formatea un BigDecimal en texto, usando únicamente el separador decimal especificado.
     *
     * @param valor      valor numérico a formatear
     * @param sepDecimal separador decimal deseado (',' o '.')
     * @param escala     número fijo de decimales a mostrar
     * @return número formateado como cadena
     */
    private static String formatear(BigDecimal valor, char sepDecimal, int escala) {
        DecimalFormatSymbols simbolos = getDecimalFormatSymbols(sepDecimal);

        // Construcción dinámica del patrón según escala
        StringBuilder pattern = new StringBuilder("#,##0");
        if (escala > 0) {
            pattern.append('.');
            for (int i = 0; i < escala; i++) {
                pattern.append('0'); // fuerza mostrar los decimales, incluso si son ceros
            }
        }

        DecimalFormat formato = new DecimalFormat(pattern.toString(), simbolos);
        formato.setMinimumFractionDigits(escala);
        formato.setMaximumFractionDigits(escala);

        return formato.format(valor);
    }

    private static DecimalFormatSymbols getDecimalFormatSymbols(char sepDecimal) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();

        if (sepDecimal == ',') {
            simbolos.setDecimalSeparator(',');
            simbolos.setGroupingSeparator('.');
        } else {
            simbolos.setDecimalSeparator('.');
            simbolos.setGroupingSeparator(',');
        }

        if (sepDecimal == 0) { // respaldo
            simbolos.setDecimalSeparator('.');
            simbolos.setGroupingSeparator(',');
        }
        return simbolos;
    }

    /**
     * Cuenta decimales en base al separador decimal indicado.
     * @param numeroCadena texto numérico original.
     * @param sepDecimal separador decimal a considerar (',' o '.').
     * @return cantidad de dígitos tras el separador; 0 si no hay parte decimal.
     */
    private static int countDecimals(String numeroCadena, char sepDecimal) {
        if (numeroCadena == null) return 0;
        String s = numeroCadena.trim();
        int idx = s.lastIndexOf(sepDecimal);
        if (idx == -1 || idx == s.length() - 1) return 0;
        return s.length() - idx - 1;
    }

//    /**
//     * Garantiza que {@link #decimalSeparator} esté configurado.
//     * Si es 0, intenta leer del perfil; si falla, usa '.'.
//     */
//    private static void ensureDecimalSeparatorConfigured() {
//        if (decimalSeparator == 0) {
//            getDecimalSeparator();
//        }
//    }

    // ============================
    //         OPERACIONES
    // ============================

    /**
     * Suma dos valores numéricos representados como cadenas.
     * <p>Escala dinámica = máx(decimales en a, decimales en b).</p>
     *
     * @param a primer valor numérico en formato String
     * @param b segundo valor numérico en formato String
     * @return resultado de la suma formateado como String
     */
    public static String addValues(String a, String b) {
        getDecimalSeparator();
        LogUtil.infof(String.format("Sumando valores: a=%s, b=%s", a, b));

        BigDecimal bdA = parseConSeparadorDecimal(a, decimalSeparator);
        BigDecimal bdB = parseConSeparadorDecimal(b, decimalSeparator);
        BigDecimal resultado = bdA.add(bdB);

        int escala = Math.max(countDecimals(a, decimalSeparator), countDecimals(b, decimalSeparator));
        String result = formatear(resultado, decimalSeparator, escala);

        LogUtil.infof(String.format("Resultado de la suma: %s", result));
        return result;
    }

    /**
     * Resta dos valores numéricos representados como cadenas (a - b).
     * <p>Escala dinámica = máx(decimales en a, decimales en b).</p>
     *
     * @param a minuendo en formato String
     * @param b sustraendo en formato String
     * @return resultado de la resta formateado como String
     */
    public static String subtractValues(String a, String b) {
        getDecimalSeparator();
        LogUtil.infof(String.format("Restando valores: a=%s, b=%s", a, b));

        BigDecimal bdA = parseConSeparadorDecimal(a, decimalSeparator);
        BigDecimal bdB = parseConSeparadorDecimal(b, decimalSeparator);
        BigDecimal resultado = bdA.subtract(bdB);

        int escala = Math.max(countDecimals(a, decimalSeparator), countDecimals(b, decimalSeparator));
        String result = formatear(resultado, decimalSeparator, escala);

        LogUtil.infof(String.format("Resultado de la resta: %s", result));
        return result;
    }

    /**
     * Multiplica dos valores numéricos representados como cadenas.
     * <p>Escala dinámica por defecto = máx(decimales en a, decimales en b)
     * para evitar crecer en exceso; si se requiere más precisión puede ajustarse luego.</p>
     *
     * @param a primer factor en formato String
     * @param b segundo factor en formato String
     * @return resultado de la multiplicación formateado como String
     */
    public static String multiplyValues(String a, String b) {
        getDecimalSeparator();
        LogUtil.infof(String.format("Multiplicando valores: a=%s, b=%s", a, b));

        BigDecimal bdA = parseConSeparadorDecimal(a, decimalSeparator);
        BigDecimal bdB = parseConSeparadorDecimal(b, decimalSeparator);
        BigDecimal resultado = bdA.multiply(bdB);

        int escala = Math.max(countDecimals(a, decimalSeparator), countDecimals(b, decimalSeparator));
        // Redondeo a la escala deseada para una salida estable (HALF_UP)
        if (escala >= 0) {
            resultado = resultado.setScale(escala, RoundingMode.HALF_UP);
        }
        String result = formatear(resultado, decimalSeparator, escala);

        LogUtil.infof(String.format("Resultado de la multiplicación: %s", result));
        return result;
    }

    /**
     * Divide dos valores numéricos representados como cadenas (a / b).
     * <p>Escala dinámica por defecto = máx(decimales en a, decimales en b).
     * Si el divisor es 0, lanza {@link ArithmeticException}.</p>
     *
     * @param a dividendo en formato String
     * @param b divisor en formato String
     * @return resultado de la división formateado como String
     * @throws ArithmeticException si el divisor es cero
     */
    public static String divideValues(String a, String b) {
        getDecimalSeparator();
        LogUtil.infof(String.format("Dividiendo valores: a=%s, b=%s", a, b));

        BigDecimal bdA = parseConSeparadorDecimal(a, decimalSeparator);
        BigDecimal bdB = parseConSeparadorDecimal(b, decimalSeparator);

        if (bdB.compareTo(BigDecimal.ZERO) == 0) {
            LogUtil.error("Error: intento de división por cero");
            throw new ArithmeticException("División por cero");
        }

        // Usamos una escala interna amplia para la operación, luego ajustamos a la escala de salida
        int escalaSalida = Math.max(countDecimals(a, decimalSeparator), countDecimals(b, decimalSeparator));
        int escalaIntermedia = Math.max(escalaSalida, 10);

        BigDecimal resultado = bdA.divide(bdB, escalaIntermedia, RoundingMode.HALF_UP);

        // Ajustamos/mostramos con la escala de salida deseada
        if (escalaSalida >= 0) {
            resultado = resultado.setScale(escalaSalida, RoundingMode.HALF_UP);
        }
        String result = formatear(resultado, decimalSeparator, escalaSalida);

        LogUtil.infof(String.format("Resultado de la división: %s", result));
        return result;
    }
}