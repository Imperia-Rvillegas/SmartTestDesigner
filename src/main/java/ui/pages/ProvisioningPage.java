package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.CalculatorUtil;
import ui.utils.LogUtil;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Página que modela la pantalla <strong>Aprovisionamiento</strong>.
 *
 * <p>Encapsula las acciones necesarias para identificar un registro con
 * cantidades en el mes siguiente, abrir su detalle y validar la información
 * del plan de aprovisionamiento.</p>
 */
public class ProvisioningPage extends BasePage {

    /** Posibles títulos visibles para la tabla principal. */
    private static final String[] MAIN_TABLE_TITLES = {"Aprovisionamiento", "Plan de aprovisionamiento"};

    /** Tiempo máximo de espera para detectar la visibilidad del título de tabla (en milisegundos). */
    private static final int TABLE_TITLE_TIMEOUT_MS = 5000;

    /** Intervalo de sondeo para verificar la visibilidad del título de tabla (en milisegundos). */
    private static final int TABLE_TITLE_POLLING_MS = 100;

    /** Título de la tabla de detalle. */
    private static final String DETAIL_TABLE_TITLE = "Plan de aprovisionamiento";

    /** Índice de la fila seleccionada en la tabla principal. */
    private int selectedRowIndex = -1;

    /** Índice de la columna seleccionada en la tabla principal. */
    private int selectedColumnIndex = -1;

    /** Cantidad numérica del registro seleccionado. */
    private BigDecimal selectedQuantity = BigDecimal.ZERO;

    /** Texto sin procesar de la cantidad seleccionada. */
    private String selectedQuantityLabel;

    /** Encabezado del período asociado a la cantidad seleccionada. */
    private String selectedPeriodHeader;

    /** Representación YearMonth del período cuando la vista corresponde a meses. */
    private YearMonth selectedYearMonth;

    /** Representación de semana ISO cuando la vista corresponde a semanas. */
    private WeekPeriod selectedWeekPeriod;

    /** Fecha específica seleccionada cuando la vista corresponde a días. */
    private LocalDate selectedDay;

    /** Vista temporal actualmente activa en la pantalla. */
    private PeriodView activePeriodView = PeriodView.MONTHS;

    /** Criterio de fecha actualmente seleccionado en la pantalla principal. */
    private String activeDateCriterion;

    /** Tipo de valor (cantidad o importe) seleccionado en la pantalla principal. */
    private String activeValueType;

    /** Título efectivo de la tabla principal utilizado durante la ejecución. */
    private String mainTableTitle;

    /**
     * Constructor principal.
     *
     * @param driver       WebDriver activo.
     * @param pageManager  Administrador de páginas y utilidades.
     */
    public ProvisioningPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Busca la primera celda con cantidad distinta de cero en la columna del período indicado
     * y almacena la información necesaria para validaciones posteriores.
     *
     * @param periodLabel etiqueta del período objetivo (por ejemplo, «Mes siguiente» o «Mes actual»).
     */
    public void findRecordWithQuantityInPeriod(String periodLabel) {
        waitUtil.waitForTableToLoadCompletely();

        resetSelectionState();

        PeriodTarget periodTarget = PeriodTarget.fromLabel(periodLabel);
        mainTableTitle = resolveExistingTableTitle(MAIN_TABLE_TITLES);
        PeriodMetadata periodMetadata = resolvePeriodMetadata(mainTableTitle, periodTarget);
        String periodHeader = periodMetadata.getHeader();
        int columnIndex = tableUtil.getColumnIndexByHeader(periodHeader, mainTableTitle);

        if (columnIndex == -1) {
            throw new IllegalStateException(
                    "No se encontró la columna del " + periodTarget.getDescription() + " en la tabla '" + mainTableTitle + "'."
            );
        }

        WebElement table = tableUtil.getTable(mainTableTitle);
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
        List<String> headers = tableUtil.getColumnHeaders(mainTableTitle);

        int maxColumnAttempts = 10;
        for (int offset = 0; offset < maxColumnAttempts; offset++) {
            int currentColumnIndex = columnIndex + offset;
            if (currentColumnIndex >= headers.size()) {
                break;
            }

            PeriodMetadata currentMetadata;
            if (offset == 0) {
                currentMetadata = periodMetadata;
            } else {
                String currentHeader = headers.get(currentColumnIndex);
                currentMetadata = resolveMetadataFromHeader(currentHeader);
                if (currentMetadata == null) {
                    LogUtil.warn("No se pudo interpretar el encabezado '" + currentHeader + "'. Se omite la columna.");
                    continue;
                }
            }

            if (trySelectCellWithQuantity(rows, currentColumnIndex, currentMetadata)) {
                if (offset > 0) {
                    LogUtil.warn(String.format(
                            "No se encontraron cantidades mayores a cero en '%s'. Se utilizará la columna '%s'.",
                            periodHeader,
                            currentMetadata.getHeader()
                    ));
                }
                return;
            }
        }

        throw new NoSuchElementException(
                "No se encontró una celda con cantidad distinta de cero en la columna '" + periodHeader + "' ni en las "
                        + maxColumnAttempts + " columnas siguientes."
        );
    }

    /**
     * Restablece los atributos asociados al registro actualmente seleccionado en la tabla principal.
     */
    private void resetSelectionState() {
        selectedRowIndex = -1;
        selectedColumnIndex = -1;
        selectedQuantity = BigDecimal.ZERO;
        selectedQuantityLabel = null;
        selectedPeriodHeader = null;
        selectedYearMonth = null;
        selectedWeekPeriod = null;
        selectedDay = null;
    }

    /**
     * Intenta seleccionar la primera celda con cantidad mayor a cero dentro de la columna indicada.
     *
     * @param rows           filas visibles de la tabla principal.
     * @param columnIndex    índice de la columna objetivo.
     * @param periodMetadata metadatos asociados al encabezado evaluado.
     * @return {@code true} si se logró seleccionar un registro, {@code false} en caso contrario.
     */
    private boolean trySelectCellWithQuantity(List<WebElement> rows, int columnIndex, PeriodMetadata periodMetadata) {
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            WebElement row = rows.get(rowIndex);
            List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));

            if (columnIndex >= cells.size()) {
                continue;
            }

            WebElement cell = cells.get(columnIndex);
            waitUtil.waitForVisibilityByElement(cell);
            String cellText = cell.getText().trim();

            if (cellText.isEmpty()) {
                continue;
            }

            BigDecimal numericValue = parseNumericValue(cellText);
            if (numericValue.compareTo(BigDecimal.ZERO) > 0) {
                updateSelectionState(rowIndex, columnIndex, periodMetadata, numericValue, cellText);
                return true;
            }
        }

        return false;
    }

    /**
     * Busca columnas alternativas (a la derecha del período esperado) que contengan cantidades mayores a cero
     * y selecciona el primer registro disponible.
     *
     * @param rows                 filas visibles de la tabla principal.
     * @param tableTitle           título de la tabla evaluada.
     * @param initialColumnIndex   índice de la columna originalmente evaluada.
     * @param initialPeriodMetadata metadatos del período inicialmente detectado.
     * @return {@code true} si se logró seleccionar un registro en una columna alternativa.
     */
    /**
     * Determina los metadatos temporales correspondientes al encabezado proporcionado.
     *
     * @param header texto del encabezado evaluado.
     * @return metadatos derivados del encabezado o {@code null} si no representan un período válido.
     */
    private PeriodMetadata resolveMetadataFromHeader(String header) {
        switch (activePeriodView) {
            case MONTHS:
                YearMonth month = tryParseHeaderToYearMonth(header);
                if (month != null) {
                    return new PeriodMetadata(header, month, null, null);
                }
                return null;
            case WEEKS:
                WeekPeriod week = parseWeekHeader(header);
                if (week != null) {
                    return new PeriodMetadata(header, null, week, null);
                }
                return null;
            case DAYS:
                LocalDate day = parseHeaderToLocalDate(header);
                if (day != null) {
                    return new PeriodMetadata(header, null, null, day);
                }
                return null;
            default:
                throw new IllegalStateException("Vista temporal no soportada: " + activePeriodView);
        }
    }

    /**
     * Actualiza el estado interno con la información del registro seleccionado.
     *
     * @param rowIndex        índice de la fila seleccionada.
     * @param columnIndex     índice de la columna seleccionada.
     * @param periodMetadata  metadatos del período asociado.
     * @param numericValue    cantidad numérica interpretada.
     * @param rawLabel        etiqueta original mostrada en la celda.
     */
    private void updateSelectionState(
            int rowIndex,
            int columnIndex,
            PeriodMetadata periodMetadata,
            BigDecimal numericValue,
            String rawLabel
    ) {
        selectedRowIndex = rowIndex;
        selectedColumnIndex = columnIndex;
        selectedQuantity = numericValue;
        selectedQuantityLabel = rawLabel;
        selectedPeriodHeader = periodMetadata.getHeader();
        selectedYearMonth = periodMetadata.getYearMonth();
        selectedWeekPeriod = periodMetadata.getWeekPeriod();
        selectedDay = periodMetadata.getDay();

        LogUtil.info(String.format(
                "Registro seleccionado → fila: %d, columna: %d, período: %s, cantidad: %s",
                rowIndex + 1,
                columnIndex + 1,
                periodMetadata.getHeader(),
                numericValue.toPlainString()
        ));
    }

    /**
     * Abre el detalle del registro previamente identificado en la tabla principal.
     */
    public void openSelectedRecordDetail() {
        ensureRecordSelected();

        WebElement targetCell = tableUtil.getCellElement(selectedRowIndex, selectedColumnIndex, mainTableTitle);
        WebElement interactiveElement = resolveInteractiveElement(targetCell);
        clickByElement(interactiveElement, String.format("Cantidad %s (%s)", selectedQuantityLabel, selectedPeriodHeader));

        waitUtil.waitForTableToLoadCompletely();
        waitForDetailTableReady();
    }

    /**
     * Valida que la suma de la columna indicada en el detalle coincida con la cantidad
     * previamente almacenada.
     *
     * @param columnHeader encabezado de la columna a sumar.
     */
    public void validateDetailQuantityMatchesSelected(String columnHeader) {
        ensureRecordSelected();
        waitForDetailTableReady();

        BigDecimal total = sumColumnValues(DETAIL_TABLE_TITLE, columnHeader);

        LogUtil.info(String.format(
                "Suma calculada para '%s': %s | Cantidad seleccionada: %s",
                columnHeader,
                total.toPlainString(),
                selectedQuantity.toPlainString()
        ));

        validationUtil.assertEquals(
                total,
                selectedQuantity,
                String.format(
                        "Comparación de la suma '%s' con la cantidad del período '%s'",
                        columnHeader,
                        selectedPeriodHeader
                )
        );
    }

    /**
     * Verifica que todas las fechas de recepción del detalle pertenezcan al período
     * seleccionado en la tabla principal.
     */
    public void verifyDetailReceptionDatesMatchSelectedMonth(String date) {
        ensureRecordSelected();
        waitForDetailTableReady();

        int columnIndex = tableUtil.getColumnIndexByHeader(date, DETAIL_TABLE_TITLE);
        if (columnIndex == -1) {
            throw new IllegalStateException("No se encontró la columna " + date + " en el detalle de aprovisionamiento.");
        }

        WebElement table = tableUtil.getTable(DETAIL_TABLE_TITLE);
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));

        validationUtil.assertTrue(!rows.isEmpty(), "Existen registros en el detalle del plan de aprovisionamiento");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            WebElement row = rows.get(rowIndex);
            List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));

            if (columnIndex >= cells.size()) {
                LogUtil.warn(String.format("La fila %d no contiene la columna " + date + ".", rowIndex + 1));
                continue;
            }

            WebElement cell = cells.get(columnIndex);
            String text = cell.getText().trim();

            validationUtil.assertTrue(
                    !text.isEmpty(),
                    String.format("La fecha de recepción de la fila %d no debe estar vacía.", rowIndex + 1)
            );

            LocalDate reception = parseDetailDate(text);

            switch (activePeriodView) {
                case MONTHS:
                    YearMonth rowMonth = YearMonth.from(reception);
                    validationUtil.assertTrue(
                            selectedYearMonth != null && rowMonth.equals(selectedYearMonth),
                            String.format("La fecha %s pertenece al período seleccionado %s", text, selectedPeriodHeader)
                    );
                    break;
                case WEEKS:
                    boolean belongsToWeek = selectedWeekPeriod != null && selectedWeekPeriod.contains(reception);
                    String weekMessage = String.format(
                            "La fecha %s debe estar entre %s y %s (semana %s)",
                            text,
                            selectedWeekPeriod != null ? selectedWeekPeriod.getStart().format(formatter) : "?",
                            selectedWeekPeriod != null ? selectedWeekPeriod.getEnd().format(formatter) : "?",
                            selectedPeriodHeader
                    );
                    validationUtil.assertTrue(belongsToWeek, weekMessage);
                    break;
                case DAYS:
                    validationUtil.assertTrue(
                            selectedDay != null && reception.equals(selectedDay),
                            String.format("La fecha %s debe coincidir con el día seleccionado %s", text, selectedPeriodHeader)
                    );
                    break;
                default:
                    throw new IllegalStateException("Vista temporal no soportada: " + activePeriodView);
            }
        }
    }

    /**
     * Garantiza que existe un registro seleccionado antes de interactuar con el detalle.
     */
    private void ensureRecordSelected() {
        boolean hasTemporalData;
        switch (activePeriodView) {
            case MONTHS:
                hasTemporalData = selectedYearMonth != null;
                break;
            case WEEKS:
                hasTemporalData = selectedWeekPeriod != null;
                break;
            case DAYS:
                hasTemporalData = selectedDay != null;
                break;
            default:
                hasTemporalData = false;
        }

        boolean recordReady = selectedRowIndex >= 0
                && selectedColumnIndex >= 0
                && selectedQuantity != null
                && selectedPeriodHeader != null
                && hasTemporalData;

        validationUtil.assertTrue(recordReady, "Existe un registro seleccionado con información del período objetivo");
    }

    /**
     * Determina el título válido para la tabla principal según los elementos visibles.
     *
     * @param possibleTitles títulos candidatos a evaluar.
     * @return título seleccionado.
     */
    private String resolveExistingTableTitle(String... possibleTitles) {
        for (String title : possibleTitles) {
            By headerLocator = By.xpath("//*[self::div or self::span][normalize-space(text())='" + title + "']");
            if (waitUtil.isElementVisible(headerLocator, TABLE_TITLE_TIMEOUT_MS, TABLE_TITLE_POLLING_MS)) {
                LogUtil.info("Se utilizará la tabla con título: " + title);
                return title;
            }
        }

        throw new IllegalStateException(String.format(
                "No se encontró ninguna tabla con los títulos esperados: %s",
                Arrays.toString(possibleTitles)
        ));
    }

    /**
     * Identifica el encabezado correspondiente al período solicitado dentro de la tabla principal.
     *
     * @param tableTitle   título de la tabla sobre la cual se buscará el encabezado.
     * @param periodTarget indicador del período (actual o siguiente).
     * @return metadatos del período solicitado detectado en la tabla.
     */
    private PeriodMetadata resolvePeriodMetadata(String tableTitle, PeriodTarget periodTarget) {
        switch (periodTarget) {
            case NEXT:
                return resolveNextPeriodMetadata(tableTitle);
            case CURRENT:
                return resolveCurrentPeriodMetadata(tableTitle);
            default:
                throw new IllegalStateException("Período no soportado: " + periodTarget);
        }
    }

    /**
     * Identifica el encabezado correspondiente al período siguiente dentro de la tabla principal.
     *
     * @param tableTitle título de la tabla sobre la cual se buscará el encabezado.
     * @return metadatos del período siguiente detectado en la tabla.
     */
    private PeriodMetadata resolveNextPeriodMetadata(String tableTitle) {
        switch (activePeriodView) {
            case MONTHS:
                return resolveNextMonthMetadata(tableTitle);
            case WEEKS:
                return resolveNextWeekMetadata(tableTitle);
            case DAYS:
                return resolveNextDayMetadata(tableTitle);
            default:
                throw new IllegalStateException("Vista temporal no soportada: " + activePeriodView);
        }
    }

    /**
     * Identifica el encabezado correspondiente al período actual dentro de la tabla principal.
     *
     * @param tableTitle título de la tabla sobre la cual se buscará el encabezado.
     * @return metadatos del período actual detectado en la tabla.
     */
    private PeriodMetadata resolveCurrentPeriodMetadata(String tableTitle) {
        switch (activePeriodView) {
            case MONTHS:
                return resolveCurrentMonthMetadata(tableTitle);
            case WEEKS:
                return resolveCurrentWeekMetadata(tableTitle);
            case DAYS:
                return resolveCurrentDayMetadata(tableTitle);
            default:
                throw new IllegalStateException("Vista temporal no soportada: " + activePeriodView);
        }
    }

    /**
     * Resuelve el encabezado correspondiente al mes siguiente y su representación temporal.
     *
     * @param tableTitle título de la tabla evaluada.
     * @return metadatos que incluyen el encabezado detectado y el {@link YearMonth} asociado.
     */
    private PeriodMetadata resolveNextMonthMetadata(String tableTitle) {
        YearMonth targetMonth = resolveTargetNextMonth();
        String expectedLabel = tableUtil.getColumnNameNextMonth();
        return resolveMonthMetadata(tableTitle, targetMonth, expectedLabel, "mes siguiente");
    }

    /**
     * Resuelve el encabezado correspondiente al mes actual y su representación temporal.
     *
     * @param tableTitle título de la tabla evaluada.
     * @return metadatos que incluyen el encabezado detectado y el {@link YearMonth} asociado.
     */
    private PeriodMetadata resolveCurrentMonthMetadata(String tableTitle) {
        YearMonth targetMonth = resolveTargetCurrentMonth();
        String expectedLabel = tableUtil.getColumnNameCurrentMonth();
        return resolveMonthMetadata(tableTitle, targetMonth, expectedLabel, "mes actual");
    }

    /**
     * Aplica las heurísticas necesarias para localizar la columna que representa un mes específico.
     *
     * @param tableTitle    título de la tabla evaluada.
     * @param targetMonth   mes objetivo a localizar.
     * @param expectedLabel etiqueta esperada provista por utilidades de tabla (puede ser {@code null}).
     * @param logContext    descripción utilizada en los mensajes de log.
     * @return metadatos del período asociados al encabezado encontrado.
     */
    private PeriodMetadata resolveMonthMetadata(
            String tableTitle,
            YearMonth targetMonth,
            String expectedLabel,
            String logContext
    ) {
        List<String> headers = tableUtil.getColumnHeaders(tableTitle);
        Locale locale = new Locale("es", "ES");

        for (String header : headers) {
            YearMonth candidate = tryParseHeaderToYearMonth(header);
            if (candidate != null && candidate.equals(targetMonth)) {
                LogUtil.info("Columna detectada para el " + logContext + " usando YearMonth: " + header);
                return new PeriodMetadata(header, candidate, null, null);
            }
        }

        if (expectedLabel != null) {
            String normalizedExpected = normalizeForComparison(expectedLabel, locale);
            for (String header : headers) {
                if (normalizeForComparison(header, locale).equals(normalizedExpected)) {
                    LogUtil.info("Columna detectada para el " + logContext + " usando etiqueta esperada: " + header);
                    return new PeriodMetadata(header, targetMonth, null, null);
                }
            }
        }

        String normalizedShort = normalizeForComparison(
                targetMonth.getMonth().getDisplayName(TextStyle.SHORT, locale),
                locale
        );
        String normalizedFull = normalizeForComparison(
                targetMonth.getMonth().getDisplayName(TextStyle.FULL, locale),
                locale
        );
        String yearTwoDigits = String.format("%02d", targetMonth.getYear() % 100);
        String yearFull = String.valueOf(targetMonth.getYear());

        for (String header : headers) {
            String normalizedHeader = normalizeForComparison(header, locale);

            boolean containsMonth = (!normalizedShort.isEmpty() && normalizedHeader.contains(normalizedShort))
                    || (!normalizedFull.isEmpty() && normalizedHeader.contains(normalizedFull));
            boolean containsYear = normalizedHeader.contains(yearTwoDigits) || normalizedHeader.contains(yearFull);

            if (containsMonth && containsYear) {
                LogUtil.info("Columna detectada para el " + logContext + " usando heurísticas: " + header);
                return new PeriodMetadata(header, targetMonth, null, null);
            }
        }

        throw new NoSuchElementException(String.format(
                "No se encontró una columna correspondiente al %s. Encabezados actuales: %s",
                logContext,
                headers
        ));
    }

    /**
     * Resuelve el encabezado correspondiente a la semana siguiente utilizando el formato esperado.
     *
     * @param tableTitle título de la tabla evaluada.
     * @return metadatos que incluyen el encabezado detectado y su {@link WeekPeriod}.
     */
    private PeriodMetadata resolveNextWeekMetadata(String tableTitle) {
        String expectedLabel = tableUtil.getColumnNameNextWeek();
        WeekPeriod targetWeek = parseWeekHeader(expectedLabel);

        if (targetWeek == null) {
            throw new IllegalStateException("No se pudo interpretar la etiqueta esperada para la semana siguiente: " + expectedLabel);
        }

        return resolveWeekMetadata(tableTitle, targetWeek, expectedLabel, "semana siguiente");
    }

    /**
     * Resuelve el encabezado correspondiente a la semana actual utilizando el formato esperado.
     *
     * @param tableTitle título de la tabla evaluada.
     * @return metadatos que incluyen el encabezado detectado y su {@link WeekPeriod}.
     */
    private PeriodMetadata resolveCurrentWeekMetadata(String tableTitle) {
        ZoneId zone = ZoneId.of("Europe/Madrid");
        LocalDate today = LocalDate.now(zone);
        WeekPeriod targetWeek = buildWeekPeriodFromDate(today);
        String expectedLabel = formatWeekLabel(targetWeek);
        return resolveWeekMetadata(tableTitle, targetWeek, expectedLabel, "semana actual");
    }

    /**
     * Aplica las heurísticas necesarias para localizar la columna que representa una semana específica.
     *
     * @param tableTitle    título de la tabla evaluada.
     * @param targetWeek    semana objetivo a localizar.
     * @param expectedLabel etiqueta esperada provista por utilidades de tabla (puede ser {@code null}).
     * @param logContext    descripción utilizada en los mensajes de log.
     * @return metadatos del período asociados al encabezado encontrado.
     */
    private PeriodMetadata resolveWeekMetadata(
            String tableTitle,
            WeekPeriod targetWeek,
            String expectedLabel,
            String logContext
    ) {
        List<String> headers = tableUtil.getColumnHeaders(tableTitle);

        PeriodMetadata exactWeekMetadata = findWeekMetadataByPeriod(
                headers,
                targetWeek,
                "Columna detectada para la " + logContext + ": "
        );
        if (exactWeekMetadata != null) {
            return exactWeekMetadata;
        }

        if (expectedLabel != null) {
            Locale locale = new Locale("es", "ES");
            String normalizedExpected = normalizeForComparison(expectedLabel, locale);
            for (String header : headers) {
                if (normalizeForComparison(header, locale).equals(normalizedExpected)) {
                    LogUtil.info("Columna detectada para la " + logContext + " usando etiqueta esperada: " + header);
                    return new PeriodMetadata(header, null, targetWeek, null);
                }
            }
        }

        throw new NoSuchElementException(String.format(
                "No se encontró una columna correspondiente a la %s. Encabezados actuales: %s",
                logContext,
                headers
        ));
    }

    /**
     * Construye un {@link WeekPeriod} a partir de una fecha de referencia.
     *
     * @param referenceDate fecha que se utilizará para determinar la semana ISO.
     * @return representación de la semana que contiene a la fecha proporcionada.
     */
    private WeekPeriod buildWeekPeriodFromDate(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException("La fecha de referencia para la semana no puede ser nula.");
        }

        WeekFields iso = WeekFields.ISO;
        int week = referenceDate.get(iso.weekOfWeekBasedYear());
        int year = referenceDate.get(iso.weekBasedYear());
        return new WeekPeriod(week, year);
    }

    /**
     * Formatea la representación textual de una semana ISO según el estándar utilizado en la aplicación.
     *
     * @param period semana a formatear.
     * @return texto que describe la semana, por ejemplo «S. 32 del 25».
     */
    private String formatWeekLabel(WeekPeriod period) {
        if (period == null) {
            return null;
        }

        Locale locale = new Locale("es", "ES");
        return String.format(locale, "S. %d del %02d", period.getWeek(), period.getWeekYear() % 100);
    }

    /**
     * Resuelve el encabezado correspondiente al día siguiente utilizando los formatos compactos disponibles.
     *
     * @param tableTitle título de la tabla evaluada.
     * @return metadatos que incluyen el encabezado detectado y la fecha asociada.
     */
    private PeriodMetadata resolveNextDayMetadata(String tableTitle) {
        String expectedLabel = tableUtil.getColumnNameNextDay();
        LocalDate targetDay = parseHeaderToLocalDate(expectedLabel);

        if (targetDay == null) {
            throw new IllegalStateException("No se pudo interpretar la etiqueta esperada para el día siguiente: " + expectedLabel);
        }

        return resolveDayMetadata(tableTitle, targetDay, expectedLabel, "día siguiente");
    }

    /**
     * Resuelve el encabezado correspondiente al día actual utilizando los formatos compactos disponibles.
     *
     * @param tableTitle título de la tabla evaluada.
     * @return metadatos que incluyen el encabezado detectado y la fecha asociada.
     */
    private PeriodMetadata resolveCurrentDayMetadata(String tableTitle) {
        ZoneId zone = ZoneId.of("Europe/Madrid");
        LocalDate targetDay = LocalDate.now(zone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy").withLocale(new Locale("es", "ES"));
        String expectedLabel = targetDay.format(formatter);
        return resolveDayMetadata(tableTitle, targetDay, expectedLabel, "día actual");
    }

    /**
     * Aplica las heurísticas necesarias para localizar la columna que representa un día específico.
     *
     * @param tableTitle    título de la tabla evaluada.
     * @param targetDay     fecha objetivo a localizar.
     * @param expectedLabel etiqueta esperada provista por utilidades de tabla (puede ser {@code null}).
     * @param logContext    descripción utilizada en los mensajes de log.
     * @return metadatos del período asociados al encabezado encontrado.
     */
    private PeriodMetadata resolveDayMetadata(
            String tableTitle,
            LocalDate targetDay,
            String expectedLabel,
            String logContext
    ) {
        if (targetDay == null) {
            throw new IllegalArgumentException("La fecha objetivo no puede ser nula para la búsqueda del " + logContext + ".");
        }

        List<String> headers = tableUtil.getColumnHeaders(tableTitle);

        for (String header : headers) {
            LocalDate candidate = parseHeaderToLocalDate(header);
            if (candidate != null && candidate.equals(targetDay)) {
                LogUtil.info("Columna detectada para el " + logContext + ": " + header);
                return new PeriodMetadata(header, null, null, candidate);
            }
        }

        if (expectedLabel != null) {
            Locale locale = new Locale("es", "ES");
            String normalizedExpected = normalizeForComparison(expectedLabel, locale);
            for (String header : headers) {
                if (normalizeForComparison(header, locale).equals(normalizedExpected)) {
                    LogUtil.info("Columna detectada para el " + logContext + " usando etiqueta esperada: " + header);
                    return new PeriodMetadata(header, null, null, targetDay);
                }
            }
        }

        throw new NoSuchElementException(String.format(
                "No se encontró una columna correspondiente al %s. Encabezados actuales: %s",
                logContext,
                headers
        ));
    }

    /**
     * Intenta convertir un encabezado a {@link YearMonth} devolviendo {@code null} cuando no es posible.
     *
     * @param headerText texto del encabezado a evaluar.
     * @return {@link YearMonth} derivado del encabezado o {@code null} si no tiene formato de mes.
     */
    private YearMonth tryParseHeaderToYearMonth(String headerText) {
        try {
            return parseHeaderToYearMonth(headerText);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Obtiene el {@link YearMonth} objetivo para el mes siguiente empleando las utilidades de {@link ui.utils.TableUtil}.
     *
     * @return {@link YearMonth} correspondiente al próximo mes.
     */
    private YearMonth resolveTargetNextMonth() {
        String nextMonthDate = tableUtil.getNextMonthDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        try {
            return YearMonth.parse(nextMonthDate, formatter);
        } catch (DateTimeParseException ex) {
            throw new IllegalStateException(
                    "No fue posible interpretar la fecha del próximo mes proporcionada por TableUtil: " + nextMonthDate,
                    ex
            );
        }
    }

    /**
     * Obtiene el {@link YearMonth} objetivo para el mes actual.
     *
     * @return {@link YearMonth} correspondiente al mes en curso.
     */
    private YearMonth resolveTargetCurrentMonth() {
        ZoneId zone = ZoneId.of("Europe/Madrid");
        return YearMonth.now(zone);
    }

    /**
     * Normaliza valores para comparaciones insensibles a mayúsculas, espacios y diacríticos.
     *
     * @param value  texto a normalizar.
     * @param locale {@link Locale} utilizada para la transformación a minúsculas.
     * @return texto normalizado.
     */
    private String normalizeForComparison(String value, Locale locale) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\s./-]", "")
                .toLowerCase(locale);

        return normalized;
    }

    /**
     * Convierte el texto de una celda con cantidad y unidad en un {@link BigDecimal}.
     *
     * @param cellText texto bruto de la celda.
     * @return valor numérico interpretado.
     */
    private BigDecimal parseNumericValue(String cellText) {
        String numeric = CalculatorUtil.extraerParteNumerica(cellText);
        if (numeric == null || numeric.isEmpty()) {
            LogUtil.warn("No se pudo extraer un valor numérico de la celda: " + cellText);
            return BigDecimal.ZERO;
        }

        String sanitized = numeric.replace(" ", "");
        if (sanitized.contains(",")) {
            sanitized = sanitized.replace(".", "");
            sanitized = sanitized.replace(',', '.');
        } else {
            sanitized = sanitized.replace(".", "");
        }

        try {
            return new BigDecimal(sanitized);
        } catch (NumberFormatException e) {
            LogUtil.warn("Valor no numérico detectado ('" + cellText + "'). Se considerará cero.", e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Convierte el encabezado del mes a un objeto {@link YearMonth}.
     *
     * @param headerText texto del encabezado del mes.
     * @return representación {@link YearMonth} del encabezado.
     */
    private YearMonth parseHeaderToYearMonth(String headerText) {
        if (headerText == null) {
            throw new IllegalArgumentException("El encabezado no puede ser nulo.");
        }

        String sanitized = headerText.replace(".", "").trim();
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("El encabezado no puede estar vacío.");
        }

        Locale locale = new Locale("es", "ES");
        List<String> patterns = Arrays.asList(
                "MMM/yy",
                "MMM/yyyy",
                "MMMM/yy",
                "MMMM/yyyy",
                "MMM yy",
                "MMMM yy",
                "MM/yyyy",
                "MM/yy"
        );

        for (String pattern : patterns) {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern(pattern)
                    .toFormatter(locale);
            try {
                return YearMonth.parse(sanitized, formatter);
            } catch (DateTimeParseException ignored) {
                // Se intenta con el siguiente patrón
            }
        }

        throw new IllegalArgumentException("No se pudo convertir el encabezado '" + headerText + "' a YearMonth.");
    }

    /**
     * Intenta interpretar el texto de un encabezado como una semana ISO (por ejemplo, "S. 44 del 25").
     *
     * @param headerText texto del encabezado a evaluar.
     * @return representación de la semana o {@code null} si no coincide con el patrón esperado.
     */
    private WeekPeriod parseWeekHeader(String headerText) {
        if (headerText == null) {
            return null;
        }

        String sanitized = headerText.trim();
        if (sanitized.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(?i)s[.]?\s*([0-9]{1,2})\s+(?:del|de)\s+([0-9]{2,4})");
        Matcher matcher = pattern.matcher(sanitized);
        if (!matcher.find()) {
            return null;
        }

        try {
            int week = Integer.parseInt(matcher.group(1));
            int year = Integer.parseInt(matcher.group(2));
            if (year < 100) {
                year += 2000;
            }
            return new WeekPeriod(week, year);
        } catch (NumberFormatException | DateTimeException exception) {
            LogUtil.warn("No fue posible interpretar la semana del encabezado: " + headerText, exception);
            return null;
        }
    }

    /**
     * Busca una columna cuyo encabezado represente la semana ISO indicada.
     *
     * @param headers   encabezados disponibles en la tabla.
     * @param target    semana objetivo a localizar.
     * @param logPrefix prefijo utilizado para los mensajes de log cuando se encuentra coincidencia.
     * @return metadatos del período asociados al encabezado encontrado o {@code null} si no existe coincidencia.
     */
    private PeriodMetadata findWeekMetadataByPeriod(List<String> headers, WeekPeriod target, String logPrefix) {
        for (String header : headers) {
            WeekPeriod candidate = parseWeekHeader(header);
            if (candidate != null && candidate.isSameIsoWeek(target)) {
                LogUtil.info(logPrefix + header);
                return new PeriodMetadata(header, null, candidate, null);
            }
        }
        return null;
    }

    /**
     * Intenta convertir un encabezado compacto en una fecha {@link LocalDate}.
     *
     * @param headerText texto del encabezado a evaluar.
     * @return fecha interpretada o {@code null} si el formato no es reconocido.
     */
    private LocalDate parseHeaderToLocalDate(String headerText) {
        if (headerText == null) {
            return null;
        }

        String sanitized = headerText.trim();
        if (sanitized.isEmpty()) {
            return null;
        }

        Locale locale = new Locale("es", "ES");
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("d/M/yy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(sanitized, formatter.withLocale(locale));
            } catch (DateTimeParseException ignored) {
                // se intenta con el siguiente formato
            }
        }

        return null;
    }

    /**
     * Calcula la suma de todas las celdas visibles de una columna específica.
     *
     * @param tableTitle   título de la tabla donde se realizará la suma.
     * @param columnHeader encabezado de la columna objetivo.
     * @return suma total de los valores visibles.
     */
    private BigDecimal sumColumnValues(String tableTitle, String columnHeader) {
        int columnIndex = tableUtil.getColumnIndexByHeader(columnHeader, tableTitle);
        if (columnIndex == -1) {
            throw new IllegalStateException("No se encontró la columna '" + columnHeader + "' en la tabla '" + tableTitle + "'.");
        }

        WebElement table = tableUtil.getTable(tableTitle);
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
        BigDecimal total = BigDecimal.ZERO;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            WebElement row = rows.get(rowIndex);
            List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));

            if (columnIndex >= cells.size()) {
                LogUtil.warn(String.format("La fila %d no contiene la columna '%s'.", rowIndex + 1, columnHeader));
                continue;
            }

            WebElement cell = cells.get(columnIndex);
            String text = cell.getText().trim();

            if (text.isEmpty()) {
                LogUtil.info(String.format("Fila %d columna '%s' sin valor. Se omite en la suma.", rowIndex + 1, columnHeader));
                continue;
            }

            BigDecimal value = parseNumericValue(text);
            total = total.add(value);

            LogUtil.info(String.format(
                    "Fila %d columna '%s' → valor %s | suma parcial %s",
                    rowIndex + 1,
                    columnHeader,
                    value.toPlainString(),
                    total.toPlainString()
            ));
        }

        return total;
    }

    /**
     * Espera a que la tabla de detalle esté visible y lista para su lectura.
     */
    private void waitForDetailTableReady() {
        By detailTableLocator = By.xpath(String.format(
                "//*[self::div or self::span][normalize-space(text())='%s']/following::table[1]",
                DETAIL_TABLE_TITLE
        ));
        waitUtil.waitForVisibilityByLocator(detailTableLocator);
        waitUtil.waitForTableToLoadCompletely();
    }

    /**
     * Determina cuál es el elemento interactuable real dentro de una celda antes de ejecutar el clic.
     *
     * <p>En algunas versiones de la pantalla, las celdas que contienen cantidades muestran un elemento
     * hijo con la clase {@code link} que actúa como disparador del detalle. Al priorizar dicho elemento
     * evitamos depender de que el {@code td} completo sea clickeable y garantizamos la apertura del modal
     * de detalle.</p>
     *
     * @param cell celda obtenida de la tabla principal.
     * @return elemento interno interactuable o la celda original si no se identifica un hijo específico.
     */
    private WebElement resolveInteractiveElement(WebElement cell) {
        List<By> candidateLocators = Arrays.asList(
                By.cssSelector("span.link"),
                By.cssSelector("a"),
                By.cssSelector("button"),
                By.cssSelector("[role='button']")
        );

        for (By locator : candidateLocators) {
            List<WebElement> candidates = cell.findElements(locator);
            for (WebElement candidate : candidates) {
                try {
                    if (candidate.isDisplayed()) {
                        LogUtil.info("Elemento interactuable interno detectado para clic: " + locator);
                        return candidate;
                    }
                } catch (StaleElementReferenceException exception) {
                    LogUtil.warn("Elemento interno obsoleto durante la detección: " + locator, exception);
                }
            }
        }

        LogUtil.info("No se identificó un elemento interno específico. Se utilizará la celda completa para el clic.");
        return cell;
    }

    /**
     * Convierte el texto de una fecha del detalle en un {@link LocalDate}.
     *
     * @param text texto de la fecha (por ejemplo, "19/12/2025").
     * @return fecha interpretada.
     */
    private LocalDate parseDetailDate(String text) {
        String sanitized = text.trim();
        Locale locale = new Locale("es", "ES");
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(sanitized, formatter.withLocale(locale));
            } catch (DateTimeParseException ignored) {
                // se intenta con el siguiente formato
            }
        }

        throw new IllegalArgumentException("No se pudo interpretar la fecha de recepción: " + text);
    }


    /**
     * Estructura auxiliar que encapsula el encabezado y la información temporal del período siguiente.
     */
    private static class PeriodMetadata {
        private final String header;
        private final YearMonth yearMonth;
        private final WeekPeriod weekPeriod;
        private final LocalDate day;

        PeriodMetadata(String header, YearMonth yearMonth, WeekPeriod weekPeriod, LocalDate day) {
            this.header = header;
            this.yearMonth = yearMonth;
            this.weekPeriod = weekPeriod;
            this.day = day;
        }

        String getHeader() {
            return header;
        }

        YearMonth getYearMonth() {
            return yearMonth;
        }

        WeekPeriod getWeekPeriod() {
            return weekPeriod;
        }

        LocalDate getDay() {
            return day;
        }
    }

    /**
     * Representa una semana ISO mediante su número, año y rango de fechas.
     */
    private static class WeekPeriod {
        private final int week;
        private final int weekYear;
        private final LocalDate start;
        private final LocalDate end;

        WeekPeriod(int week, int weekYear) {
            this.week = week;
            this.weekYear = weekYear;

            WeekFields iso = WeekFields.ISO;
            LocalDate reference = LocalDate.of(weekYear, 1, 4)
                    .with(iso.weekOfWeekBasedYear(), week)
                    .with(iso.weekBasedYear(), weekYear);
            this.start = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            this.end = reference.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        boolean isSameIsoWeek(WeekPeriod other) {
            return other != null && week == other.week && weekYear == other.weekYear;
        }

        boolean contains(LocalDate date) {
            return (date.isEqual(start) || date.isAfter(start))
                    && (date.isEqual(end) || date.isBefore(end));
        }

        LocalDate getStart() {
            return start;
        }

        LocalDate getEnd() {
            return end;
        }

        /**
         * Obtiene el número de semana ISO representado.
         *
         * @return número de semana ISO.
         */
        int getWeek() {
            return week;
        }

        /**
         * Obtiene el año ISO asociado a la semana.
         *
         * @return año ISO de la semana.
         */
        int getWeekYear() {
            return weekYear;
        }

        /**
         * Calcula una nueva instancia desplazada una cantidad específica de semanas.
         *
         * @param weeksToAdd cantidad de semanas a sumar (puede ser negativa para retroceder).
         * @return nueva instancia que representa la semana desplazada.
         */
        WeekPeriod plusWeeks(int weeksToAdd) {
            if (weeksToAdd == 0) {
                return this;
            }

            LocalDate reference = start.plusWeeks(weeksToAdd);
            WeekFields iso = WeekFields.ISO;
            int newWeek = reference.get(iso.weekOfWeekBasedYear());
            int newYear = reference.get(iso.weekBasedYear());
            return new WeekPeriod(newWeek, newYear);
        }
    }

    /**
     * Representa los períodos disponibles para la búsqueda de cantidades en la tabla principal.
     */
    private enum PeriodTarget {
        CURRENT("actual", "período actual"),
        NEXT("siguiente", "período siguiente");

        private final String keyword;
        private final String description;

        PeriodTarget(String keyword, String description) {
            this.keyword = keyword;
            this.description = description;
        }

        /**
         * Obtiene el período representado por una etiqueta proporcionada en el feature.
         *
         * @param value texto ingresado en el escenario (por ejemplo, «Mes siguiente»).
         * @return período correspondiente a la etiqueta.
         */
        static PeriodTarget fromLabel(String value) {
            if (value == null) {
                throw new IllegalArgumentException("El período recibido no puede ser nulo.");
            }

            String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase(Locale.ROOT)
                    .trim();

            if (normalized.contains(NEXT.keyword)
                    || normalized.contains("proximo")
                    || normalized.contains("proxima")) {
                return NEXT;
            }
            if (normalized.contains(CURRENT.keyword) || normalized.contains("presente") || normalized.contains("este")) {
                return CURRENT;
            }

            throw new IllegalArgumentException("Tipo de período no soportado: " + value);
        }

        String getDescription() {
            return description;
        }
    }

    /**
     * Enumeración con las vistas temporales disponibles en la pantalla de Aprovisionamiento.
     */
    private enum PeriodView {
        MONTHS("Meses"),
        WEEKS("Semanas"),
        DAYS("Días");

        private final String label;

        PeriodView(String label) {
            this.label = label;
        }

        static PeriodView fromLabel(String value) {
            for (PeriodView view : values()) {
                if (view.label.equalsIgnoreCase(value)) {
                    return view;
                }
            }
            throw new IllegalArgumentException("Vista temporal desconocida: " + value);
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Selecciona el criterio de fecha que se utilizará en la tabla principal de Aprovisionamiento.
     *
     * @param dateOption texto visible de la opción (por ejemplo, «Fecha de recepción»).
     */
    public void selectDateCriterionInProvisioning(String dateOption) {
        activeDateCriterion = selectToolbarOption(dateOption, "el criterio de fecha");
    }

    /**
     * Selecciona el tipo de valor (cantidad o importe) a visualizar en la tabla principal de Aprovisionamiento.
     *
     * @param valueOption texto visible del control asociado (por ejemplo, «Cantidad» o «Importe»).
     */
    public void selectValueTypeInProvisioning(String valueOption) {
        activeValueType = selectToolbarOption(valueOption, "el tipo de valor");
    }

    /**
     * Intenta seleccionar una opción visible en la barra de herramientas de Aprovisionamiento.
     *
     * @param optionText          texto visible del control a seleccionar.
     * @param contextDescription  descripción legible del contexto, utilizada para logs y mensajes de error.
     * @return texto saneado que representa la opción finalmente seleccionada.
     */
    private String selectToolbarOption(String optionText, String contextDescription) {
        if (optionText == null) {
            throw new IllegalArgumentException("La opción para " + contextDescription + " no puede ser nula.");
        }

        String sanitized = optionText.trim();
        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("La opción para " + contextDescription + " no puede estar vacía.");
        }

        RuntimeException lastFailure = null;

        try {
            clickButtonByName(sanitized);
            finalizeToolbarSelection(contextDescription, sanitized);
            return sanitized;
        } catch (RuntimeException ex) {
            lastFailure = ex;
            LogUtil.info(String.format(
                    "No fue posible seleccionar %s '%s' mediante clickButtonByName. Se intentarán alternativas.",
                    contextDescription,
                    sanitized
            ));
        }

        for (By locator : buildToolbarOptionLocators(sanitized)) {
            try {
                WebElement optionElement = waitUtil.findVisibleElement(locator);
                WebElement clickableElement = resolveClickableControl(optionElement);
                if (clickableElement == null) {
                    clickableElement = optionElement;
                }
                waitUtil.waitForClickable(clickableElement);
                clickByElement(clickableElement, contextDescription + ": " + sanitized);
                finalizeToolbarSelection(contextDescription, sanitized);
                return sanitized;
            } catch (RuntimeException ex) {
                lastFailure = ex;
                LogUtil.info(String.format(
                        "El locator %s no permitió seleccionar %s '%s'. Se probará con el siguiente.",
                        locator,
                        contextDescription,
                        sanitized
                ));
            }
        }

        NoSuchElementException failure = new NoSuchElementException(
                String.format("No se encontró la opción '%s' para %s en Aprovisionamiento.", sanitized, contextDescription)
        );
        if (lastFailure != null) {
            failure.initCause(lastFailure);
        }
        throw failure;
    }

    /**
     * Completa las acciones posteriores a la selección de una opción en la barra de herramientas.
     *
     * @param contextDescription descripción legible del contexto en el que se realizó la selección.
     * @param optionText         texto seleccionado para fines de log.
     */
    private void finalizeToolbarSelection(String contextDescription, String optionText) {
        resetSelectionState();
        mainTableTitle = null;
        waitUtil.waitForTableToLoadCompletely();
        LogUtil.info(String.format("Se seleccionó %s: %s", contextDescription, optionText));
    }

    /**
     * Genera los localizadores alternativos que se utilizarán para encontrar un control por su texto visible.
     *
     * @param optionText texto visible del control.
     * @return lista de localizadores candidatos.
     */
    private List<By> buildToolbarOptionLocators(String optionText) {
        String literal = buildXPathLiteral(optionText);
        return Arrays.asList(
                By.xpath("//button[normalize-space(.)=" + literal + "]"),
                By.xpath("//a[normalize-space(.)=" + literal + "]"),
                By.xpath("//span[normalize-space(text())=" + literal + " and ancestor::button]"),
                By.xpath("//span[normalize-space(text())=" + literal + " and ancestor::a]"),
                By.xpath("//li[contains(@class,'p-dropdown-item')]//span[normalize-space(text())=" + literal + "]"),
                By.xpath("//div[contains(@class,'p-segmentedbutton') or contains(@class,'imp-segmented-button') or contains(@class,'p-togglebutton')]//*[normalize-space(text())=" + literal + "]"),
                By.xpath("//*[contains(@class,'p-buttonset')]//*[normalize-space(text())=" + literal + "]")
        );
    }

    /**
     * Determina el elemento clickeable asociado a la opción identificada.
     *
     * @param element elemento localizado inicialmente.
     * @return elemento listo para recibir el clic.
     */
    private WebElement resolveClickableControl(WebElement element) {
        if (element == null) {
            return null;
        }

        WebElement current = element;
        for (int depth = 0; depth < 4; depth++) {
            if (isClickableElement(current)) {
                return current;
            }

            List<WebElement> parents = current.findElements(By.xpath("ancestor::button[1]"));
            if (!parents.isEmpty()) {
                current = parents.getFirst();
                if (isClickableElement(current)) {
                    return current;
                }
                continue;
            }

            parents = current.findElements(By.xpath("ancestor::a[1]"));
            if (!parents.isEmpty()) {
                current = parents.getFirst();
                if (isClickableElement(current)) {
                    return current;
                }
                continue;
            }

            parents = current.findElements(By.xpath("ancestor::*[@role='button'][1]"));
            if (!parents.isEmpty()) {
                current = parents.getFirst();
                if (isClickableElement(current)) {
                    return current;
                }
                continue;
            }

            break;
        }

        return element;
    }

    /**
     * Indica si el elemento proporcionado puede recibir un clic directamente.
     *
     * @param element elemento a evaluar.
     * @return {@code true} si es un botón, enlace o elemento con rol botón.
     */
    private boolean isClickableElement(WebElement element) {
        if (element == null) {
            return false;
        }
        String tagName = element.getTagName();
        if ("button".equalsIgnoreCase(tagName) || "a".equalsIgnoreCase(tagName)) {
            return true;
        }
        String role = element.getAttribute("role");
        return role != null && role.equalsIgnoreCase("button");
    }

    /**
     * Construye un literal seguro para ser utilizado dentro de una expresión XPath.
     *
     * @param value texto original que se desea insertar en el XPath.
     * @return literal escapado compatible con XPath.
     */
    private String buildXPathLiteral(String value) {
        if (value == null) {
            return "\"\"";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        StringBuilder builder = new StringBuilder("concat(");
        char[] characters = value.toCharArray();
        for (int index = 0; index < characters.length; index++) {
            char character = characters[index];
            if (character == '\"') {
                builder.append("'\"'");
            } else if (character == '\'') {
                builder.append("\"'\"");
            } else {
                builder.append("\"").append(character).append("\"");
            }

            if (index < characters.length - 1) {
                builder.append(", ");
            }
        }

        builder.append(")");
        return builder.toString();
    }

    public void seleccionaLaVistaDeEnAprovisionamiento(String name) {
        clickButtonByName(name);
        activePeriodView = PeriodView.fromLabel(name);
        resetSelectionState();
        mainTableTitle = null;
        waitUtil.waitForTableToLoadCompletely();
        LogUtil.info("Vista temporal seleccionada en Aprovisionamiento: " + activePeriodView);
    }
}
