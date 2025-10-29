package ui.utils;

import io.cucumber.datatable.DataTable;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import ui.manager.PageManager;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Utilidad para interactuar con tablas HTML dinámicas usando Selenium WebDriver.
 * Facilita la lectura, localización, edición y validación de celdas en tablas del DOM,
 * con soporte para encabezados dinámicos, celdas editables e interacciones específicas.
 *
 * Se basa en selectores estándar para encabezados (thead), filas del cuerpo (tbody),
 * y componentes interactivos como inputs y botones personalizados.
 */
public class TableUtil {

    private final WebDriver driver;
    private final WaitUtil waitUtil;
    private final By tableLocator = By.xpath("//*[@id='calculation-history-table']");
    private final By inputDecimal = By.cssSelector("input.p-inputtext.p-inputnumber-input");

    private int lastRowIndex;
    private int lastColumnIndex;
//    private String lastCellValue;
    private String lastMonth;
    private String sumCurrentValues;
//    private String amount;
    private String lastCellValue;

    // Último producto encontrado
    private String lastProductCode;

    // Detalles completos de celdas válidas encontradas (una lista de objetos)
    private final List<CellData> lastMatchingCells = new ArrayList<>();

    // Clase interna o externa para representar los datos de una celda
    public static class CellData {
        private final int rowIndex;
        private final int columnIndex;
        private String value;

        public CellData(int rowIndex, int columnIndex, String value) {
            this.rowIndex = rowIndex;
            this.columnIndex = columnIndex;
            this.value = value;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public int getLastRowIndex() {
        return lastRowIndex;
    }

    public int getLastColumnIndex() {
        return lastColumnIndex;
    }

    public String getLastCellValue() {
        return lastCellValue;
    }

    public String getLastProductCode() {
        return lastProductCode;
    }

    public String getLastMonth() {
        return lastMonth;
    }

    public String getSumCurrentValues() {
        return sumCurrentValues;
    }

//    public String getAmount() {
//        return amount;
//    }

//    public String getLastCellValue() {
//        return lastCellValueString;
//    }

    /**
     * Constructor que inicializa la utilidad con el WebDriver activo y utilidades de espera explícita.
     *
     * @param driver   instancia actual de WebDriver.
     * @param waitUtil utilidad personalizada para esperas explícitas.
     */
    public TableUtil(PageManager pageManager) {
        this.driver = pageManager.getDriver();
        this.waitUtil = pageManager.getWaitUtil();
    }

    /**
     * Retorna la primera tabla que aparece después de un encabezado (span) con el texto visible especificado.
     *
     * @param tableTitle Texto exacto del encabezado (por ejemplo, "Maestro de artículos").
     * @return {@link WebElement} de la tabla encontrada debajo del título.
     */
    public WebElement getTable(String tableTitle) {
        String xpath = "//*[self::div or self::span][normalize-space(text())='" + tableTitle + "']/following::table[1]";
        LogUtil.info("Buscando tabla ubicada debajo del título: '" + tableTitle + "'");
        return waitUtil.findVisibleElement(By.xpath(xpath));
    }

//    /**
//     * Obtiene el texto de una celda específica de la tabla.
//     *
//     * @param rowIndex índice de la fila (empezando en 0).
//     * @param colIndex índice de la columna (empezando en 0).
//     * @return texto de la celda localizada.
//     * @throws IndexOutOfBoundsException si el índice está fuera de los límites válidos.
//     */
//    public String getCellText(int rowIndex, int colIndex, String tableTitle) {
//        WebElement table = getTable(tableTitle);
//        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
//        if (rowIndex >= rows.size()) throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex);
//
//        WebElement row = rows.get(rowIndex);
//        List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td"));
//        if (colIndex >= cells.size()) throw new IndexOutOfBoundsException("Column index out of bounds: " + colIndex);
//
//        return cells.get(colIndex).getText().trim();
//    }

    /**
     * Retorna el {@link WebElement} de una celda específica, asegurando su visibilidad.
     *
     * @param rowIndex índice de la fila.
     * @param colIndex índice de la columna (ignorando celdas con clase 'spacer').
     * @return {@link WebElement} de la celda localizada.
     */
    public WebElement getCellElement(int rowIndex, int colIndex, String tableTitle) {
        WebElement table = getTable(tableTitle);
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
        if (rowIndex >= rows.size()) throw new IndexOutOfBoundsException("Índice de fila fuera de rango: " + rowIndex);

        WebElement row = rows.get(rowIndex);
        List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));
        if (colIndex >= cells.size())
            throw new IndexOutOfBoundsException("Índice de columna fuera de rango: " + colIndex);

        return cells.get(colIndex);
    }

//    /**
//     * Busca el índice de una fila que contiene el valor especificado en una columna dada.
//     *
//     * @param value       valor de la celda a buscar.
//     * @param columnIndex índice de la columna donde buscar.
//     * @return índice de la fila si se encuentra, o -1 si no se encuentra.
//     */
//    public int findRowIndexByCellValue(String value, int columnIndex, String tableTitle) {
//        WebElement table = getTable(tableTitle);
//        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
//
//        for (int i = 0; i < rows.size(); i++) {
//            String cellValue = getCellText(i, columnIndex, tableTitle);
//            if (cellValue.equalsIgnoreCase(value)) return i;
//        }
//
//        return -1;
//    }

    /**
     * Obtiene el índice (posición) de una columna dentro de la tabla identificada por {@code tableTitle},
     * buscando por el texto exacto de su cabecera.
     *
     * <p><b>Flujo:</b></p>
     * <ol>
     *   <li>Intenta hasta 4 veces localizar la cabecera de la tabla.</li>
     *   <li>Filtra la fila de cabecera "real" (excluyendo las filas de selectores de columnas).</li>
     *   <li>Toma dos snapshots rápidos de los textos de cabecera con una breve pausa entre ellos
     *       (100 ms) para comprobar que los valores sean estables.</li>
     *   <li>Si los snapshots difieren, asume que la cabecera estaba en transición y reintenta
     *       después de una pausa (200 ms).</li>
     *   <li>Cuando obtiene un snapshot estable, busca el texto normalizado de la cabecera
     *       que coincida con {@code headerText}.</li>
     *   <li>Si lo encuentra, devuelve su índice (basado en 0, de izquierda a derecha).</li>
     *   <li>Si no lo encuentra tras todos los intentos, devuelve {@code -1}.</li>
     * </ol>
     *
     * <p><b>Notas:</b></p>
     * <ul>
     *   <li>Devuelve {@code -1} cuando la columna no se encuentra.</li>
     *   <li>Controla internamente {@link org.openqa.selenium.StaleElementReferenceException}
     *       para reintentar sin fallar de inmediato.</li>
     *   <li>El texto buscado se normaliza con {@code norm()} antes de compararlo con las cabeceras.</li>
     *   <li>Los logs detallan cada intento, incluyendo detección de inestabilidad en las cabeceras.</li>
     * </ul>
     *
     * @param headerText Texto visible de la cabecera de la columna a buscar.
     * @param tableTitle Título de la tabla donde se debe realizar la búsqueda.
     * @return Índice basado en 0 de la columna encontrada, o {@code -1} si no se encuentra tras los intentos.
     *
     * @throws RuntimeException si ocurre un error inesperado al acceder al DOM (diferente de los manejados).
     *
     * @see #getTable(String)
     * @see #snapshotHeaderTexts(WebElement)
     * @see waitUtil#sleepMillis(long, String)
     */
    public int getColumnIndexByHeader(String headerText, String tableTitle) {
        final int attempts = 4;
        final int pauseBetweenAttemptsMillis = 200;
        final int stabilityCheckMillis = 100; // pausa corta para comprobar estabilidad

        String target = norm(headerText);

        for (int i = 1; i <= attempts; i++) {
            try {
                LogUtil.info("Intento " + i + " - Buscando '" + headerText + "' en tabla '" + tableTitle + "'");
                WebElement table = getTable(tableTitle);

                // Filtra la fila de cabecera "real" (evita la de selectors)
                WebElement headerRow = waitUtil.waitUntil(drv ->
                        table.findElement(By.cssSelector("thead tr.imperia-table-header-row:not(.columns-selectors)"))
                );

                // Tomamos dos snapshots rápidos de los textos de cabecera y los comparamos
                List<String> snap1 = snapshotHeaderTexts(headerRow);
                waitUtil.sleepMillis(stabilityCheckMillis, "Pausa corta para chequeo de estabilidad");
                List<String> snap2 = snapshotHeaderTexts(headerRow);

                if (!snap1.equals(snap2)) {
                    LogUtil.warn("Cabeceras inestables entre snapshots (intento " + i + "), reintentando");
                    waitUtil.sleepMillis(pauseBetweenAttemptsMillis, "Tiempo de espera entre intentos");
                    continue; // reintenta si no fue estable
                }

                // Busca el índice en el snapshot estable
                for (int colIndex = 0; colIndex < snap1.size(); colIndex++) {
                    if (snap1.get(colIndex).equals(target)) {
                        LogUtil.info("Columna encontrada: '" + headerText + "' en índice " + colIndex);
                        return colIndex;
                    }
                }

                LogUtil.warn("No se encontró la columna '" + headerText + "' en intento " + i);
            } catch (StaleElementReferenceException sere) {
                // Si algo queda stale, volvemos a intentar (no propagamos)
                LogUtil.warn("StaleElementReferenceException buscando columna '" + headerText + "': " + sere.getMessage());
            } catch (Exception e) {
                LogUtil.warn("Error al buscar columna '" + headerText + "': " + e.getMessage());
            }

            waitUtil.sleepMillis(pauseBetweenAttemptsMillis, "Tiempo de espera entre intentos");
        }

        return -1;
    }

    /**
     * Lee y normaliza (con {@code norm(...)}) el textContent de las celdas de cabecera
     * excluyendo .spacer. Lanza StaleElementReferenceException si ocurre durante la lectura.
     */
    private List<String> snapshotHeaderTexts(WebElement headerRow) {
        List<WebElement> headers = headerRow.findElements(By.cssSelector("th.imperia-table-header-cell:not(.spacer)"));
        List<String> texts = new ArrayList<>(headers.size());
        for (WebElement th : headers) {
            // usamos textContent para incluir NBSPs antes de normalizar
            String raw = th.getAttribute("textContent");
            texts.add(norm(raw));
        }
        return texts;
    }

    /**
     * Normaliza una cadena para comparaciones robustas en UI.
     * <p>Transformaciones aplicadas, en orden:</p>
     * <ol>
     *   <li>Si {@code s} es {@code null}, devuelve cadena vacía.</li>
     *   <li>Normaliza Unicode a <strong>NFKC</strong> (homogeneiza caracteres visualmente similares).</li>
     *   <li>Reemplaza <em>non-breaking space</em> (NBSP, {@code \u00A0}) por espacio normal.</li>
     *   <li>Elimina <em>zero-width</em> {@code \u200B}, {@code \u200C}, {@code \u200D}.</li>
     *   <li>Colapsa espacios en blanco consecutivos a uno solo y hace {@code trim()}.</li>
     *   <li>Convierte a minúsculas con {@link java.util.Locale#ROOT}.</li>
     * </ol>
     *
     * <p>Útil para comparar textos de cabeceras/celdas que contienen NBSP, formatos Unicode
     * distintos o espacios “raros”.</p>
     *
     * <h3>Ejemplos</h3>
     * <pre>{@code
     * norm("  Precio Unitario ") -> "precio unitario"
     * norm("A\u200BBC")          -> "abc"
     * }</pre>
     *
     * @param s texto de entrada (puede ser {@code null}).
     * @return cadena normalizada apta para comparación exacta.
     */
    private String norm(String s) {
        if (s == null) return "";
        // Normaliza a NFKC para homogeneizar puntos/espacios
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC);
        // Reemplaza NBSP y zero-width por espacio normal
        s = s.replace('\u00A0',' ')
                .replace("\u200B","")
                .replace("\u200C","")
                .replace("\u200D","");
        // Colapsa espacios
        s = s.replaceAll("\\s+", " ").trim().toLowerCase(java.util.Locale.ROOT);
        return s;
    }

    /**
     * Devuelve el nombre completo del próximo mes en español (e.g., "Julio").
     *
     * @return nombre del próximo mes con la primera letra en mayúscula.
     */
    public String getColumnNameNextMonth() {
        Locale localeEs = new Locale("es", "ES");
        ZoneId zone = ZoneId.of("Europe/Madrid");

        YearMonth target = YearMonth.now(zone).plusMonths(1);

        String rawAbbreviation = target
                .getMonth()
                .getDisplayName(TextStyle.SHORT, localeEs)
                .trim();

        if (rawAbbreviation.isEmpty()) {
            throw new IllegalStateException("No se pudo obtener la abreviatura del mes siguiente.");
        }

        String sanitized = rawAbbreviation.endsWith(".")
                ? rawAbbreviation.substring(0, rawAbbreviation.length() - 1)
                : rawAbbreviation;
        sanitized = sanitized.toLowerCase(localeEs);
        String capitalized = sanitized.substring(0, 1).toUpperCase(localeEs) + sanitized.substring(1);
        String monthWithDot = capitalized + ".";

        int yearTwoDigits = target.getYear() % 100;
        String formattedYear = String.format(localeEs, "%02d", yearTwoDigits);

        String formatted = monthWithDot + "/" + formattedYear;
        LogUtil.info("Nombre de columna (próximo mes): " + formatted);
        return formatted;
    }

    /**
     * Obtiene la fecha del próximo mes en formato "MM/yyyy".
     *
     * @return String con el mes y año del próximo mes en formato "MM/yyyy".
     */
    public String getNextMonthDate() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        String result = nextMonth.format(formatter);

        LogUtil.info("Fecha generada para el próximo mes: " + result);
        return result;
    }

    /**
     * Retorna la primera celda visible de una columna en una tabla HTML, localizándola mediante el texto visible del encabezado.
     *
     * <p>Este metodo identifica el índice de la columna que coincide con el encabezado visible proporcionado y luego obtiene
     * la primera fila visible de la tabla especificada. A partir de esa fila, retorna la celda correspondiente a dicha columna.</p>
     *
     * <p>Este metodo excluye celdas con clase {@code spacer}, ya que estas no contienen datos reales.</p>
     *
     * @param headerText     El texto visible del encabezado de la columna a buscar.
     * @param tablePosition  La posición (índice base 0) de la tabla dentro del DOM, útil cuando hay múltiples tablas.
     * @return El {@link WebElement} correspondiente a la celda ubicada en la primera fila visible y en la columna especificada.
     *
     * @throws IllegalArgumentException si:
     * <ul>
     *   <li>No se encuentra una columna que coincida con el encabezado proporcionado.</li>
     *   <li>No hay filas visibles en la tabla especificada.</li>
     *   <li>El índice de columna está fuera del rango de celdas visibles de la fila.</li>
     * </ul>
     */
    public WebElement getFirstCellElementByHeaderName(String headerText, String tableTitle) {
        int columnIndex = getColumnIndexByHeader(headerText, tableTitle);
        if (columnIndex == -1)
            throw new IllegalArgumentException("No se encontró la columna con encabezado: '" + headerText + "'");

        WebElement table = getTable(tableTitle);
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
        if (rows.isEmpty()) throw new IllegalArgumentException("No hay filas visibles en la tabla.");

        WebElement firstRow = rows.getFirst();
        List<WebElement> cells = waitUtil.findVisibleElements(firstRow, By.cssSelector("td:not(.spacer)"));
        if (columnIndex >= cells.size()) {
            throw new IllegalArgumentException("El índice de columna (" + columnIndex + ") excede el número de celdas visibles.");
        }

        return cells.get(columnIndex);
    }

    /**
     * Retorna el botón de edición visible en la tabla.
     *
     * @return {@link WebElement} del ícono de editar.
     */
    public WebElement getEditButtonVisible() {
        By editButton = By.xpath("(//img[@src='assets/icons/list-edit.svg'])[2]");
        return waitUtil.findVisibleElement(editButton);
    }

    /**
     * Retorna el input decimal visible actualmente en la pantalla.
     *
     * @return {@link WebElement} del input decimal.
     */
    public WebElement getInputDecimalVisible() {
//        By inputDecimal = By.cssSelector("input.p-inputtext.p-inputnumber-input");
        return waitUtil.findVisibleElement(inputDecimal);
    }

    /**
     * Retorna el botón de suma visible, ubicado dentro de <code>div.imp-select-button</code>.
     *
     * @return {@link WebElement} del botón "Sumar".
     */
    public WebElement getAddButtonVisible() {
        By addButton = By.xpath("//div[contains(@class,'imp-select-button')]//img[@src='assets/icons/plus.svg']/ancestor::a");
        return waitUtil.findVisibleElement(addButton);
    }

    /**
     * Retorna el botón de resta visible.
     */
    public WebElement getSubtractButtonVisible() {
        By subtractButton = By.xpath("//img[@src='assets/icons/minus.svg']/ancestor::a");
        return waitUtil.findVisibleElement(subtractButton);
    }

    /**
     * Retorna el botón de multiplicación visible.
     */
    public WebElement getMultiplyButtonVisible() {
        By multiplyButton = By.xpath("//img[@src='assets/icons/multiply.svg']/ancestor::a");
        return waitUtil.findVisibleElement(multiplyButton);
    }

    /**
     * Retorna el botón de división visible.
     */
    public WebElement getDivideButtonVisible() {
        By divideButton = By.xpath("//img[@src='assets/icons/divide.svg']/ancestor::a");
        return waitUtil.findVisibleElement(divideButton);
    }

    /**
     * Retorna el botón de reemplazo visible.
     */
    public WebElement getReplaceButtonVisible() {
        By replaceButton = By.xpath("//i[contains(@class,'pi-reply')]/ancestor::a");
        return waitUtil.findVisibleElement(replaceButton);
    }

    /**
     * Devuelve el nombre de la columna para la semana siguiente en formato "(S. NN)".
     * Ejemplo: si la semana actual es 21, retorna "(S. 22)".
     * Usa el locale del sistema y maneja correctamente el cambio de año.
     *
     * @return Etiqueta de la semana siguiente, p. ej. "(S. 22)".
     */
    public String getColumnNameNextWeek() {
        ZoneId zone = ZoneId.of("Europe/Madrid");
        LocalDate nextMonday = LocalDate.now(zone)
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        int weekIso = nextMonday.get(WeekFields.ISO.weekOfWeekBasedYear());
        int weekYear = nextMonday.get(WeekFields.ISO.weekBasedYear());
        String formatted = String.format(new Locale("es", "ES"), "S. %d del %02d", weekIso, weekYear % 100);
        LogUtil.info("Nombre de columna (próxima semana): " + formatted);
        return formatted;
    }

    /**
     * Devuelve el nombre de columna para el **día siguiente** en formato breve español.
     * <p>Formato resultante: {@code "Jue. 12"} (abreviatura capitalizada + punto + espacio + día del mes),
     * usando zona horaria {@code Europe/Madrid} y locale español de España.</p>
     *
     * <p>Detalles:</p>
     * <ul>
     *   <li>Calcula {@code nextDay} como la fecha actual en Madrid + 1 día.</li>
     *   <li>Toma la abreviatura del día de la semana en español ({@link java.time.format.TextStyle#SHORT}).</li>
     *   <li>Normaliza la abreviatura: quita punto final si viene, pone en minúsculas y capitaliza la primera letra,
     *       luego añade un punto.</li>
     * </ul>
     *
     * @return cadena tipo {@code "Lun. 3"}, {@code "Mar. 28"}, etc., según el día siguiente en Madrid.
     *
     * @implNote La zona horaria se fija a {@code Europe/Madrid} para evitar desalineaciones
     *           en entornos con TZ distinta. El locale forzado garantiza abreviaturas españolas.
     */
    public String getColumnNameNextDay() {
        Locale es = new Locale("es", "ES");
        ZoneId zone = ZoneId.of("Europe/Madrid");
        LocalDate nextDay = LocalDate.now(zone).plusDays(1);
        String formatted = nextDay.format(DateTimeFormatter.ofPattern("dd/MM/yy", es));
        LogUtil.info("Nombre de columna (próximo día): " + formatted);
        return formatted;
    }

    /**
     * Genera un localizador {@link By} para identificar un elemento <span> dentro de una fila (<tr>)
     * de una tabla HTML, donde dicha fila contiene una celda (<td>) cuyo texto coincide parcialmente
     * con el texto proporcionado.
     *
     * <p>El localizador busca una fila que contenga al menos una celda con el texto normalizado
     * que contenga el valor de {@code registeredText}, y dentro de esa fila localiza un <span> que
     * también contenga el mismo texto.</p>
     *
     * @param registeredText el texto esperado dentro de la celda y el <span> a localizar.
     * @return un objeto {@link By} que permite ubicar el <span> correspondiente dentro de la fila coincidente.
     */
    public By buildRecordLocator(String registeredText) {
        return By.xpath("//tr[.//td[contains(normalize-space(.), '" + registeredText + "')]]//td//span[contains(normalize-space(.), '" + registeredText + "')]");
    }

    /**
     * Espera a que se rendericen las filas visibles de una tabla con scroll virtual (cdk-virtual-scroll).
     *
     * <p>Este metodo detecta y espera dinámicamente a que el número de filas se estabilice,
     * para evitar errores causados por carga asincrónica o renderizado progresivo.</p>
     *
     * @return Número final de filas renderizadas visibles en el DOM.
     */
    public int getRenderedRowCount(String tableTitle) {
        WebElement table = getTable(tableTitle);
        By rowsLocator = By.cssSelector(".cdk-virtual-scroll-content-wrapper tr");

        LogUtil.info("Esperando a que se rendericen completamente las filas en la tabla localizada: " + tableLocator);

        int maxWaitMs = 500; // Tiempo máximo total de espera (en milisegundos).
        int pollingInterval = 200; // Intervalo entre verificaciones.
        int previousCount = -1;
        int currentCount = 0;
        int waited = 0;

        while (waited < maxWaitMs) {
            List<WebElement> rows = table.findElements(rowsLocator);
            currentCount = rows.size();

            if (currentCount == previousCount) {
                break; // el conteo se estabilizó
            }

            previousCount = currentCount;
            waitUtil.sleepMillis(pollingInterval, "Intervalo entre verificaciones del número de filas renderizadas.");
            waited += pollingInterval;
        }

        LogUtil.info("Se encontraron " + currentCount + " filas renderizadas en la tabla localizada.");
        return currentCount;
    }

    /**
     * Retorna la lista de textos visibles de los encabezados de columna, excluyendo celdas tipo spacer.
     *
     * @param tablePosition posición de la tabla (1 = primera, 2 = segunda, etc.)
     * @return Lista de nombres de encabezados visibles.
     */
    public List<String> getColumnHeaders(String tableTitle) {
        WebElement table = getTable(tableTitle);
        List<WebElement> headers = waitUtil.findVisibleElements(
                table,
                By.cssSelector("thead th:not(.spacer)")
        );
        return headers.stream()
                .map(h -> h.getText().trim())
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Verifica que los encabezados de una tabla HTML coincidan exactamente con los valores esperados.
     *
     * @param expectedColumns Lista de encabezados esperados obtenida desde un {@link DataTable} de Cucumber.
     * @param tableTitle      Título identificador de la tabla sobre la cual se realizará la verificación.
     *                        Este título se usa para localizar la tabla específica en el DOM.
     *
     * @throws AssertionError si los encabezados actuales de la tabla no coinciden con los esperados.
     */
    public void verifyTableColumns(DataTable expectedColumns, String tableTitle) {
        List<String> expectedHeaders = expectedColumns.asList();
        List<String> actualHeaders = getColumnHeaders(tableTitle);
        assertEquals("Los encabezados de la tabla no coinciden con los esperados", expectedHeaders, actualHeaders);
    }

    /**
     * Valida que los valores de la primera celda de dos columnas específicas NO coincidan
     * simultáneamente con los valores esperados.
     *
     * @param columnOneHeader  Nombre del encabezado de la primera columna.
     * @param columnTwoHeader  Nombre del encabezado de la segunda columna.
     * @param expectedValueOne Valor que NO debe estar en la primera celda de la primera columna.
     * @param expectedValueTwo Valor que NO debe estar en la primera celda de la segunda columna.
     * @param tableTitle       Título del contenedor de la tabla donde buscar los encabezados.
     */
    public void assertFirstRowValuesNotMatchTwoColumns(String columnOneHeader, String columnTwoHeader, String expectedValueOne, String expectedValueTwo, String tableTitle) {
        WebElement cellOne = getFirstCellElementByHeaderName(columnOneHeader, tableTitle);
        WebElement cellTwo = getFirstCellElementByHeaderName(columnTwoHeader, tableTitle);

        String actualValueOne = cellOne.getText().trim();
        String actualValueTwo = cellTwo.getText().trim();

        LogUtil.info("Verificando que los valores de la primera fila NO coincidan simultáneamente:");
        LogUtil.info("Columna '" + columnOneHeader + "': esperado ≠ '" + expectedValueOne + "', actual = '" + actualValueOne + "'");
        LogUtil.info("Columna '" + columnTwoHeader + "': esperado ≠ '" + expectedValueTwo + "', actual = '" + actualValueTwo + "'");

        if (actualValueOne.equals(expectedValueOne) && actualValueTwo.equals(expectedValueTwo)) {
            String message = String.format("""
                            Validación fallida: Ambos valores coinciden con los no esperados:
                            - '%s': no esperado = '%s', actual = '%s'
                            - '%s': no esperado = '%s', actual = '%s'""",
                    columnOneHeader, expectedValueOne, actualValueOne,
                    columnTwoHeader, expectedValueTwo, actualValueTwo);
            LogUtil.error(message);
            throw new AssertionError(message);
        } else {
            LogUtil.info("Validación exitosa: Al menos uno de los valores no coincide con el no esperado.");
        }
    }

    /**
     * Genera el nombre de la columna correspondiente al mes actual
     * devolviendo el nombre completo del mes en español.
     * <p>
     * Formato devuelto: <b>MesCompleto</b>
     * Ejemplo: {@code Enero} para enero.
     * </p>
     *
     * <ul>
     *   <li>Utiliza {@link TextStyle#FULL} con {@code Locale("es", "ES")}
     *       para obtener el nombre completo del mes en español.</li>
     *   <li>Capitaliza la primera letra y convierte el resto a minúsculas.</li>
     *   <li>Ya no se aplican ajustes especiales para septiembre, pues no hay abreviaturas.</li>
     * </ul>
     *
     * @return Nombre del mes en formato {@code MesCompleto}.
     */
    public String getColumnNameCurrentMonth() {
        // Obtener fecha actual
        LocalDate currentMonth = LocalDate.now();

        // Obtener mes completo en español, por ejemplo "enero", "septiembre", etc.
        Locale esES = new Locale("es", "ES");
        String month = currentMonth.getMonth().getDisplayName(TextStyle.FULL, esES);

        // Capitalizar la primera letra y poner el resto en minúsculas
        month = month.substring(0, 1).toUpperCase(esES) + month.substring(1).toLowerCase(esES);

        // Registrar en el log
        LogUtil.info("Nombre de columna generado para el mes actual: " + month);

        return month;
    }

    /**
     * Busca y devuelve la primera celda en una tabla que cumpla con una condición específica.
     *
     * <p>La búsqueda se realiza en una columna identificada por su encabezado, y si no se encuentra una celda
     * que cumpla con la condición, continúa iterando hacia la derecha por un número máximo de columnas (configurable).</p>
     *
     * <p>Una celda es evaluada en base a los siguientes criterios combinados:</p>
     * <ul>
     *     <li>Si está modificada (presencia de ícono con clase que contiene <code>"lock"</code>).</li>
     *     <li>Si su valor es numérico, positivo y par.</li>
     *     <li>Si tiene un pedido pendiente (detectado por estilo <code>text-decoration: underline</code>).</li>
     * </ul>
     *
     * <p>Tipos de condiciones válidas:</p>
     * <ul>
     *     <li><b>"modificada con pedido pendiente"</b></li>
     *     <li><b>"modificada sin pedido pendiente"</b></li>
     *     <li><b>"sin modificar con pedido pendiente"</b></li>
     *     <li><b>"sin modificar sin pedido pendiente"</b></li>
     * </ul>
     *
     * <p>Si se encuentra una celda que cumpla con la condición, se retorna como {@link WebElement}.
     * Si no se encuentra ninguna celda válida tras revisar todas las columnas consecutivas desde la columna base,
     * se lanza una excepción.</p>
     *
     * @param headerName    el texto del encabezado que identifica la columna inicial de búsqueda (por ejemplo: "Jul./25").
     * @param tableTitle    el título visible que identifica la tabla HTML (por ejemplo: "Previsiones").
     * @param conditionType el tipo de condición esperada en la celda. Debe coincidir exactamente con uno de los valores válidos.
     * @return el primer {@link WebElement} que representa una celda que cumple con la condición.
     *
     * @throws IllegalArgumentException si el tipo de condición especificado no es soportado.
     * @throws NoSuchElementException si no se encuentra ninguna celda válida en las columnas evaluadas.
     */
    public WebElement findFirstCellMatchingCondition(String headerName, String tableTitle, String conditionType) {
        // Espera a que la tabla carge completamente
        waitUtil.waitForTableToLoadCompletely();

        int initialColumnIndex = getColumnIndexByHeader(headerName, tableTitle);
        int maxColumnsToSearch = 10;

        for (int offset = 0; offset < maxColumnsToSearch; offset++) {
            int currentColumnIndex = initialColumnIndex + offset;
            List<WebElement> columnCells = getAllCellsByColumnIndex(currentColumnIndex, tableTitle);

            for (int rowIndex = 0; rowIndex < columnCells.size(); rowIndex++) {
                WebElement cell = columnCells.get(rowIndex);
                waitUtil.waitForElementToBeVisible(cell);
                waitUtil.sleepMillis(100, "Pausa por scroll virtual (cdk)"); // Parece que no es necesaria esta espera, se esta probando
                LogUtil.info("Evaluando celda '" + conditionType + "' fila: " + rowIndex + ", columna: " + currentColumnIndex);
                String rawText = cell.getText().trim();

                //Normalizar el valor
//                BigDecimal rawText = CalculatorUtil.parseToBigDecimal(rawText);
                List<WebElement> icons = cell.findElements(By.tagName("i"));

                boolean isItModified = isCellModified(icons);
                boolean isItPositive = isPositiveValue(rawText);
                boolean isItAnEvenNumber = isItEvenValue(rawText);
                boolean hasPendingOrder = theCellHasPendingOrder(cell);

                boolean conditionMet;
                switch (conditionType.toLowerCase()) {
                    case "modificada con pedido pendiente":
                        conditionMet = isItModified && isItPositive && isItAnEvenNumber && hasPendingOrder;
                        break;
                    case "modificada sin pedido pendiente":
                        conditionMet = isItModified && isItPositive && isItAnEvenNumber && !hasPendingOrder;
                        break;
                    case "sin modificar con pedido pendiente":
                        conditionMet = !isItModified && isItPositive && isItAnEvenNumber && hasPendingOrder;
                        break;
                    case "sin modificar sin pedido pendiente":
                        conditionMet = !isItModified && isItPositive && isItAnEvenNumber && !hasPendingOrder;
                        break;
                    default:
                        throw new IllegalArgumentException("Tipo de condición no soportada: " + conditionType);
                }

                if (!conditionMet) {
                    LogUtil.info("Celda no cumple condición '" + conditionType + "', fila omitida: " + rowIndex);
                    continue;
                }

                this.lastRowIndex = rowIndex;
                this.lastColumnIndex = currentColumnIndex;
                this.lastCellValue = rawText;
//                this.lastCellValueString = rawText;

                LogUtil.info(("Celda válida - Fila: " + rowIndex + " Columna: " + currentColumnIndex + " Valor: " + rawText));
                return getCellElement(rowIndex, currentColumnIndex, tableTitle);

            }
        }

        throw new NoSuchElementException("No se encontró una celda que cumpla la condición '" + conditionType + "' en las columnas desde '" + headerName + "' en adelante (hasta " + maxColumnsToSearch + " columnas).");
    }

    /**
     * Obtiene todas las celdas visibles (td) de una columna específica en una tabla HTML,
     * identificada por el índice absoluto de la columna (ignorando celdas con clase <code>spacer</code>).
     *
     * <p>Este metodo recorre todas las filas del cuerpo (<code>tbody</code>) de la tabla localizada por su título visible,
     * y en cada fila extrae la celda correspondiente al índice de columna proporcionado.</p>
     *
     * <p>Se utilizan únicamente celdas visibles y válidas, omitiendo aquellas con clase <code>spacer</code>
     * que se usan para espaciado estructural en la tabla.</p>
     *
     * @param columnIndex el índice cero-based de la columna a recuperar (sin contar celdas <code>spacer</code>).
     * @param tableTitle  el texto visible del encabezado que identifica la tabla dentro del DOM.
     * @return una lista de {@link WebElement} correspondientes a las celdas visibles en la columna indicada.
     *
     * @throws RuntimeException si la tabla no puede ser localizada correctamente mediante su título.
     */
    private List<WebElement> getAllCellsByColumnIndex(int columnIndex, String tableTitle) {
        WebElement table = getTable(tableTitle);
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
        List<WebElement> cells = new ArrayList<>();

        for (WebElement row : rows) {
            List<WebElement> columnas = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));
            if (columnIndex < columnas.size()) {
                cells.add(columnas.get(columnIndex));
            }
        }

        return cells;
    }

    /**
     * Verifica si una celda está marcada como modificada.
     *
     * <p>Una celda se considera modificada si contiene al menos un ícono
     * (<code>&lt;i&gt;</code>) con una clase que incluya el texto <code>"lock"</code>.</p>
     *
     * <p>Se registra un log informativo indicando si la celda fue detectada como modificada o no.</p>
     *
     * @param icons lista de elementos <code>&lt;i&gt;</code> presentes en la celda.
     * @return <code>true</code> si la celda está modificada (ícono con clase <code>"lock"</code> presente),
     *         <code>false</code> en caso contrario.
     */
    private boolean isCellModified(List<WebElement> icons) {
        boolean isModified = icons.stream().anyMatch(e -> {
            String classAttr = e.getAttribute("class");
            return classAttr != null && classAttr.contains("lock");
        });

        if (isModified) {
            LogUtil.info("Celda modificada");
        } else {
            LogUtil.info("Celda sin modificar");
        }

        return isModified;
    }

    /**
     * Verifica si un valor numérico representado como {@link String} es positivo.
     *
     * <p>Convierte el valor recibido a {@link BigDecimal} y registra logs informativos
     * indicando si el valor es positivo, cero, negativo o inválido.</p>
     *
     * @param valueStr el número en formato de texto a evaluar.
     * @return <code>true</code> si el valor es mayor que cero;
     *         <code>false</code> si es menor o igual a cero, nulo, vacío o no numérico.
     */
    private boolean isPositiveValue(String valueStr) {
        if (valueStr == null || valueStr.trim().isEmpty()) {
            LogUtil.info("Valor nulo o vacío. No se puede evaluar si es positivo.");
            return false;
        }

        try {
            BigDecimal value = new BigDecimal(valueStr.trim());
            int comparison = value.compareTo(BigDecimal.ZERO);

            if (comparison > 0) {
                LogUtil.info("Valor positivo: " + value);
                return true;
            } else {
                LogUtil.info("Valor negativo o cero: " + value);
                return false;
            }
        } catch (NumberFormatException e) {
            LogUtil.info("Valor inválido ('" + valueStr + "'). No es un número válido.");
            return false;
        }
    }

    /**
     * Verifica si un valor numérico representado como {@link String} es par.
     *
     * <p>El valor es truncado a su parte entera antes de evaluar la paridad.</p>
     *
     * <p>Se registran logs indicando si el valor es par, impar, nulo o inválido.</p>
     *
     * @param valueStr el número en formato de texto a evaluar.
     * @return <code>true</code> si el valor convertido a entero es par;
     *         <code>false</code> si es impar, nulo, vacío o no numérico.
     */
    private boolean isItEvenValue(String valueStr) {
        if (valueStr == null || valueStr.trim().isEmpty()) {
            LogUtil.info("Valor nulo o vacío. No se puede evaluar si es par.");
            return false;
        }

        try {
            BigDecimal value = new BigDecimal(valueStr.trim());
            int integer = value.intValue(); // Trunca la parte decimal
            boolean esPar = (integer % 2 == 0);

            if (esPar) {
                LogUtil.info("Valor par: " + integer);
            } else {
                LogUtil.info("Valor impar: " + integer);
            }

            return esPar;
        } catch (NumberFormatException e) {
            LogUtil.info("Valor inválido ('" + valueStr + "'). No es un número válido.");
            return false;
        }
    }

    /**
     * Verifica si una celda contiene un pedido pendiente.
     *
     * <p>Se considera con pedido pendiente si el color de fondo es rosa
     * (background-color: rgb(254, 222, 255)), que indica una modificación en la previsión.</p>
     *
     * @param cell el {@link WebElement} que representa la celda.
     * @return true si hay pedido pendiente (color rosa), false en caso contrario.
     */
    private boolean theCellHasPendingOrder(WebElement cell) {
        String styleAttr = cell.getAttribute("style");

        boolean hasPinkBackground = styleAttr != null && styleAttr.contains("rgb(254, 222, 255)");

        if (hasPinkBackground) {
            LogUtil.info("Celda con pedido pendiente");
        } else {
            LogUtil.info("Celda sin pedido pendiente");
        }

        return hasPinkBackground;
    }

    /**
     * Busca un producto con más de 2 registros en la columna "Código producto" y verifica que todas
     * las celdas correspondientes a ese producto en las columnas de los próximos 6 meses
     * cumplan una condición específica (por ejemplo: "sin modificar sin pedido pendiente").
     *
     * <p>Si se encuentra un producto válido, se almacena su código, mes, y los datos de las celdas
     * correspondientes en {@code lastProductCode}, {@code lastMonth} y {@code lastMatchingCells}.</p>
     *
     * @param conditionType el tipo de condición que deben cumplir las celdas
     *                      (por ejemplo: "sin modificar sin pedido pendiente").
     * @param tableTitle título visible de la tabla donde buscar (por ejemplo: "Previsiones").
     * @throws NoSuchElementException si no se encuentra ningún producto que cumpla la condición especificada.
     */
    public void findProductWithConditionInMonthColumns(String conditionType, String tableTitle) {
        String columnProducto = "Código producto";

        // Obtener el índice de la columna "Código producto"
        int colIndexProducto = getColumnIndexByHeader(columnProducto, tableTitle);
        if (colIndexProducto == -1) {
            throw new IllegalArgumentException("No se encontró la columna 'Código producto'");
        }

        // Obtener todas las celdas correspondientes a esa columna
        List<WebElement> productoColumnCells = getAllCellsByColumnIndex(colIndexProducto, tableTitle);

        // Agrupar las filas por código de producto
        Map<String, List<Integer>> productoIndices = new LinkedHashMap<>();
        for (int i = 0; i < productoColumnCells.size(); i++) {
            String value = productoColumnCells.get(i).getText().trim();
            if (!value.isEmpty()) {
                productoIndices.computeIfAbsent(value, k -> new ArrayList<>()).add(i);
            }
        }

        // Generar nombres completos de los próximos 6 meses (ej: Julio, Agosto, Septiembre)
        Locale esES = new Locale("es", "ES");
        List<String> nextMonths = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> LocalDate.now().withDayOfMonth(1).plusMonths(i))
                .map(d -> d.getMonth().getDisplayName(TextStyle.FULL, esES))
                .map(m -> m.substring(0, 1).toUpperCase(esES) + m.substring(1))
                .collect(java.util.stream.Collectors.toList());

        // Recorre cada producto con sus filas
        for (Map.Entry<String, List<Integer>> entry : productoIndices.entrySet()) {
            String productCode = entry.getKey();
            List<Integer> rowIndices = entry.getValue();

            if (rowIndices.size() <= 2) continue; // Se requiere más de 2 registros

            for (String month : nextMonths) {
                int colMesIndex = getColumnIndexByHeader(month, tableTitle);
                if (colMesIndex == -1) continue;

                boolean todasCumplen = true;
                List<String> values = new ArrayList<>();

                for (int row : rowIndices) {
                    WebElement cell = getCellElement(row, colMesIndex, tableTitle);
                    waitUtil.waitForElementToBeVisible(cell);
                    waitUtil.sleepMillis(100, "Esperando scroll");

                    String rawText = cell.getText().trim();
//                    BigDecimal rawText = CalculatorUtil.parseToBigDecimal(rawText);
                    List<WebElement> icons = cell.findElements(By.tagName("i"));

                    boolean cumpleCondicion;
                    switch (conditionType.toLowerCase()) {
                        case "sin modificar sin pedido pendiente":
                            cumpleCondicion = !isCellModified(icons) && !theCellHasPendingOrder(cell) && isPositiveValue(rawText);
                            break;
                        case "sin modificar con pedido pendiente":
                            cumpleCondicion = !isCellModified(icons) && theCellHasPendingOrder(cell) && isPositiveValue(rawText);
                            break;
                        case "modificada sin pedido pendiente":
                            cumpleCondicion = isCellModified(icons) && !theCellHasPendingOrder(cell) && isPositiveValue(rawText);
                            break;
                        case "modificada con pedido pendiente":
                            cumpleCondicion = isCellModified(icons) && theCellHasPendingOrder(cell) && isPositiveValue(rawText);
                            break;
                        default:
                            throw new IllegalArgumentException("Condición no soportada: " + conditionType);
                    }

                    if (!cumpleCondicion) {
                        todasCumplen = false;
                        break;
                    }

                    values.add(rawText);
                }

                if (todasCumplen) {
                    LogUtil.info("Producto encontrado: " + productCode);
                    LogUtil.info("Columna mes: " + month);

                    this.lastProductCode = productCode;
                    this.lastMonth = month;

                    this.lastMatchingCells.clear();
                    for (int i = 0; i < rowIndices.size(); i++) {
                        this.lastMatchingCells.add(new CellData(rowIndices.get(i), colMesIndex, values.get(i)));
                    }

                    printLastMatchingCells();
                    return;
                }
            }
        }

        throw new NoSuchElementException("No se encontró ningún producto con más de 2 registros que cumpla la condición: " + conditionType);
    }

    /**
     * Imprime en los logs el contenido de la lista {@code lastMatchingCells},
     * que representa las celdas válidas encontradas durante la ejecución de pruebas.
     *
     * <p>Cada celda se muestra con su índice de fila, índice de columna y valor numérico actual.</p>
     *
     * <p>Esta información es útil para depurar, verificar resultados intermedios o generar reportes
     * durante la automatización de pruebas sobre tablas dinámicas.</p>
     */
    public void printLastMatchingCells() {
        LogUtil.info("Contenido de la lista de detalles de las celdas:");
        for (CellData cell : lastMatchingCells) {
            LogUtil.infof("Fila: {}, Columna: {}, Valor: {}", cell.getRowIndex(), cell.getColumnIndex(), cell.getValue());
        }
    }

    /**
     * Calcula la suma total de los valores actuales de las celdas previamente registradas
     * en {@code lastMatchingCells}.
     *
     * <p>Este metodo utiliza {@link CalculatorUtil#addValues(String, String)} para realizar la suma
     * de forma precisa y respetando el formato numérico (separadores de miles y decimales)
     * detectado internamente en {@code CalculatorUtil}.</p>
     *
     * <p>El resultado total se almacena en {@code sumCurrentValues} para posibles
     * validaciones posteriores.</p>
     *
     * @param tableTitle título visible de la tabla donde se encuentran las celdas (por ejemplo, "Previsiones").
     */
    public void addCells(String tableTitle) {
        // Esperar a que la tabla esté completamente cargada antes de leer celdas
        waitUtil.waitForTableToLoadCompletely();

        String suma = "0";

        for (CellData cellData : lastMatchingCells) {
            int row = cellData.getRowIndex();
            int column = cellData.getColumnIndex();

            WebElement currentCell = getCellElement(row, column, tableTitle);
            String currentText = currentCell.getText().trim();

            if (currentText.isEmpty()) {
                LogUtil.warn(String.format("Celda [%d,%d]: vacía, se ignora en la suma.", row, column));
                continue;
            }

            // Intentar sumar usando tu metodo CalculatorUtil
            try {
                suma = CalculatorUtil.addValues(suma, currentText);
                LogUtil.info(String.format("Celda [%d,%d]: valor actual '%s' | suma parcial '%s'",
                        row, column, currentText, suma));
            } catch (Exception e) {
                LogUtil.warn(String.format(
                        "Celda [%d,%d]: valor no numérico o inválido '%s', se ignora en la suma. Error: %s",
                        row, column, currentText, e.getMessage()));
            }
        }

        this.sumCurrentValues = suma;
        LogUtil.info(String.format("Suma total de los valores actuales (guardada internamente): %s", suma));
    }
}