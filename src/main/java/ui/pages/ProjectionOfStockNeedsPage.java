package ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.CalculatorUtil;
import ui.utils.LogUtil;
import ui.utils.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Page Object que modela la pantalla de <strong>Proyección de stock de necesidades</strong>.
 * <p>
 * Centraliza las acciones habituales de la vista, como lanzar cálculos de proyección
 * y verificar el estado del semáforo en el historial de ejecuciones. Al extender de
 * {@link BasePage} reutiliza utilidades compartidas (botonera dinámica, confirmaciones
 * y validaciones de semáforo) para mantener los pasos de prueba simples y expresivos.
 * </p>
 */
public class ProjectionOfStockNeedsPage extends BasePage {

    private static final String REVIEW_PANEL_TABLE_TITLE = "Panel de revisión";
    private static final String DETAIL_QUANTITY_COLUMN = "Cantidad";
    private static final String MAX_MANUFACTURING_DATE_COLUMN = "Fecha máxima de fabricación";
    private static final String PRODUCT_CODE_COLUMN = "Código producto";
    private static final By PRODUCT_CODE_HEADER_LOCATOR = By.xpath("(//span[normalize-space()='" + PRODUCT_CODE_COLUMN + "'])[last()]");

    private int storedRowIndex = -1;
    private BigDecimal storedQuantity;
    private String storedDateLabel;
    private String storedProductLabel;
    private String storedOrderColumn;

    /**
     * Constructor que inicializa la página con el {@link WebDriver} activo y el {@link PageManager}.
     *
     * @param driver      instancia de WebDriver que controla el navegador.
     * @param pageManager gestor centralizado de páginas y utilidades.
     */
    public ProjectionOfStockNeedsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Ejecuta un cálculo de proyección utilizando el botón indicado y acepta la confirmación del sistema.
     *
     * @param buttonLabel texto exacto del botón que dispara el cálculo requerido.
     */
    public void calculateProjection(String buttonLabel) {
        clickButtonByName(buttonLabel);
        acceptConfirmation();
    }

    /**
     * Verifica que el semáforo del historial de cálculos muestre el color verde al finalizar la ejecución.
     *
     * @param tableTitle título de la tabla donde se refleja el estado del proceso.
     */
    public void verifyTrafficLightGreen(String tableTitle) {
        trafficLightUtil.checkGreenLight(tableTitle);
    }

    /**
     * Asegura que el panel de revisión esté desplegado para visualizar los registros asociados a la proyección.
     * Si la columna «Código producto» ya es visible, evita hacer clic nuevamente sobre el botón solicitado.
     *
     * @param panelName texto visible del botón que despliega el panel.
     */
    public void openReviewPanel(String panelName) {
        Objects.requireNonNull(panelName, "El nombre del panel no puede ser nulo");

        if (!isProductCodeColumnVisible()) {
            LogUtil.info("El panel de revisión no está desplegado. Se hará clic en: " + panelName);
            clickButtonByName(panelName);
            waitUtil.waitForTableToLoadCompletely();
            waitUtil.waitForVisibilityByLocator(PRODUCT_CODE_HEADER_LOCATOR);
        } else {
            LogUtil.info("El panel de revisión ya se encuentra visible. No es necesario hacer clic en: " + panelName);
        }

        resetStoredSelection();
    }

    /**
     * Busca una fila con información en la columna indicada, almacena la cantidad encontrada y registra su fecha asociada.
     *
     * @param orderColumnHeader encabezado de la columna donde deben existir registros, por ejemplo «Órdenes de fabricación ERP».
     */
    public void findProductWithRecordsAndStoreQuantity(String orderColumnHeader) {
        Objects.requireNonNull(orderColumnHeader, "El encabezado de columna no puede ser nulo");

        waitUtil.waitForTableToLoadCompletely();

        WebElement summaryTable = resolveReviewPanelTable(orderColumnHeader);
        int orderColumnIndex = findColumnIndex(summaryTable, orderColumnHeader);
        if (orderColumnIndex == -1) {
            throw new IllegalStateException("No se encontró la columna '" + orderColumnHeader + "' en el panel de revisión.");
        }

        int dateColumnIndex = findColumnIndex(summaryTable, MAX_MANUFACTURING_DATE_COLUMN);
        if (dateColumnIndex == -1) {
            throw new IllegalStateException("No se encontró la columna '" + MAX_MANUFACTURING_DATE_COLUMN + "' en el panel de revisión.");
        }

        List<WebElement> rows = waitUtil.findVisibleElements(summaryTable, By.cssSelector("tbody tr"));
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            WebElement row = rows.get(rowIndex);
            List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));
            if (orderColumnIndex >= cells.size()) {
                continue;
            }

            WebElement orderCell = cells.get(orderColumnIndex);
            String rawValue = normalizeText(orderCell.getText());
            if (rawValue.isEmpty()) {
                continue;
            }

            BigDecimal parsedValue = parseNumericValue(rawValue);
            if (parsedValue == null || parsedValue.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String dateLabel = (dateColumnIndex < cells.size())
                    ? normalizeText(cells.get(dateColumnIndex).getText())
                    : "";

            validationUtil.assertTrue(!dateLabel.isEmpty(),
                    "La fila encontrada debe contener una fecha en la columna '" + MAX_MANUFACTURING_DATE_COLUMN + "'.");

            storedRowIndex = rowIndex;
            storedQuantity = parsedValue;
            storedDateLabel = dateLabel;
            storedOrderColumn = orderColumnHeader;
            storedProductLabel = extractProductLabel(cells);

            LogUtil.info(String.format(
                    Locale.ROOT,
                    "Registro almacenado → fila %d | producto: %s | columna '%s': %s | fecha: %s",
                    rowIndex + 1,
                    storedProductLabel,
                    orderColumnHeader,
                    storedQuantity.toPlainString(),
                    storedDateLabel
            ));

            return;
        }

        throw new IllegalStateException("No se encontró un registro con valores en la columna '" + orderColumnHeader + "'.");
    }

    /**
     * Selecciona el registro almacenado previamente para visualizar el detalle del panel de revisión.
     */
    public void selectStoredReviewPanelRecord() {
        ensureRecordStored();

        WebElement summaryTable = resolveReviewPanelTable(storedOrderColumn);
        List<WebElement> rows = waitUtil.findVisibleElements(summaryTable, By.cssSelector("tbody tr"));
        if (storedRowIndex >= rows.size()) {
            throw new IllegalStateException("El índice almacenado excede la cantidad de filas actuales del panel de revisión.");
        }

        WebElement row = rows.get(storedRowIndex);
        List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));
        WebElement clickableCell = cells.isEmpty() ? row : cells.get(0);

        clickByElement(clickableCell, String.format("Registro %s - %s", storedProductLabel, storedOrderColumn));
        waitUtil.waitForTableToLoadCompletely();
        waitForDetailTableReady();
    }

    /**
     * Valida que la suma de la columna indicada en el detalle coincida con la cantidad almacenada del registro.
     *
     * @param columnHeader encabezado de la columna a sumar dentro del detalle del panel.
     */
    public void validateReviewPanelDetailQuantity(String columnHeader) {
        ensureRecordStored();
        Objects.requireNonNull(columnHeader, "El encabezado de la columna a validar no puede ser nulo");

        WebElement detailTable = obtainDetailTable();
        int columnIndex = findColumnIndex(detailTable, columnHeader);
        if (columnIndex == -1) {
            throw new IllegalStateException("No se encontró la columna '" + columnHeader + "' en el detalle del panel de revisión.");
        }

        BigDecimal total = sumColumn(detailTable, columnIndex);
        LogUtil.info(String.format(
                Locale.ROOT,
                "Total calculado en columna '%s': %s | Cantidad almacenada: %s",
                columnHeader,
                total.toPlainString(),
                storedQuantity.toPlainString()
        ));

        validationUtil.assertEquals(total, storedQuantity,
                String.format("Suma de '%s' vs cantidad de '%s'", columnHeader, storedOrderColumn));
    }

    /**
     * Verifica que todas las fechas del detalle coincidan con la fecha almacenada al seleccionar el registro.
     *
     * @param columnHeader encabezado de la columna que contiene las fechas a validar.
     */
    public void verifyReviewPanelDetailDates(String columnHeader) {
        ensureRecordStored();
        Objects.requireNonNull(columnHeader, "El encabezado de la columna de fechas no puede ser nulo");

        WebElement detailTable = obtainDetailTable();
        int columnIndex = findColumnIndex(detailTable, columnHeader);
        if (columnIndex == -1) {
            throw new IllegalStateException("No se encontró la columna '" + columnHeader + "' en el detalle del panel de revisión.");
        }

        List<WebElement> rows = waitUtil.findVisibleElements(detailTable, By.cssSelector("tbody tr"));
        validationUtil.assertTrue(!rows.isEmpty(), "El detalle del panel de revisión debe contener información");

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            WebElement row = rows.get(rowIndex);
            List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));
            if (columnIndex >= cells.size()) {
                continue;
            }

            String currentDate = normalizeText(cells.get(columnIndex).getText());
            ValidationUtil.assertEquals(currentDate, storedDateLabel,
                    String.format("Fecha del detalle fila %d coincide con %s", rowIndex + 1, storedDateLabel));
        }
    }

    private void resetStoredSelection() {
        storedRowIndex = -1;
        storedQuantity = null;
        storedDateLabel = null;
        storedProductLabel = null;
        storedOrderColumn = null;
    }

    private void ensureRecordStored() {
        if (storedRowIndex < 0 || storedQuantity == null || storedDateLabel == null) {
            throw new IllegalStateException("No existe un registro del panel de revisión almacenado para operar.");
        }
    }

    private WebElement resolveReviewPanelTable(String orderColumnHeader) {
        try {
            return tableUtil.getTable(REVIEW_PANEL_TABLE_TITLE);
        } catch (RuntimeException ex) {
            LogUtil.warn("Tabla '" + REVIEW_PANEL_TABLE_TITLE + "' no encontrada. Se usará un localizador alternativo.");
            String xpath = String.format(
                    Locale.ROOT,
                    "//table[.//th[normalize-space()='%s'] and .//th[normalize-space()='%s']]",
                    orderColumnHeader,
                    MAX_MANUFACTURING_DATE_COLUMN
            );
            return waitUtil.waitForVisibilityByLocator(By.xpath(xpath));
        }
    }

    private WebElement obtainDetailTable() {
        waitForDetailTableReady();
        String exclusion = (storedOrderColumn != null && !storedOrderColumn.isEmpty())
                ? String.format(Locale.ROOT, " and not(.//th[normalize-space()='%s'])", storedOrderColumn)
                : "";

        String xpath = String.format(
                Locale.ROOT,
                "//table[.//th[normalize-space()='%s'] and .//th[normalize-space()='%s']%s]",
                DETAIL_QUANTITY_COLUMN,
                MAX_MANUFACTURING_DATE_COLUMN,
                exclusion
        );
        return waitUtil.waitForVisibilityByLocator(By.xpath(xpath));
    }

    private void waitForDetailTableReady() {
        waitUtil.waitForTableToLoadCompletely();
        By locator = By.xpath(String.format(
                Locale.ROOT,
                "//table[.//th[normalize-space()='%s'] and .//th[normalize-space()='%s']]",
                DETAIL_QUANTITY_COLUMN,
                MAX_MANUFACTURING_DATE_COLUMN
        ));
        waitUtil.waitForVisibilityByLocator(locator);
    }

    private int findColumnIndex(WebElement table, String headerText) {
        List<WebElement> headers = waitUtil.findVisibleElements(table, By.cssSelector("thead tr th:not(.spacer)"));
        String normalizedTarget = normalizeText(headerText);
        for (int index = 0; index < headers.size(); index++) {
            String current = normalizeText(headers.get(index).getText());
            if (current.equalsIgnoreCase(normalizedTarget)) {
                return index;
            }
        }
        return -1;
    }

    private BigDecimal sumColumn(WebElement table, int columnIndex) {
        List<WebElement> rows = waitUtil.findVisibleElements(table, By.cssSelector("tbody tr"));
        BigDecimal total = BigDecimal.ZERO;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            WebElement row = rows.get(rowIndex);
            List<WebElement> cells = waitUtil.findVisibleElements(row, By.cssSelector("td:not(.spacer)"));
            if (columnIndex >= cells.size()) {
                continue;
            }

            String cellValue = normalizeText(cells.get(columnIndex).getText());
            if (cellValue.isEmpty()) {
                continue;
            }

            BigDecimal parsedValue = parseNumericValue(cellValue);
            if (parsedValue != null) {
                total = total.add(parsedValue);
                LogUtil.info(String.format(
                        Locale.ROOT,
                        "Fila %d → valor %s | acumulado %s",
                        rowIndex + 1,
                        parsedValue.toPlainString(),
                        total.toPlainString()
                ));
            }
        }

        return total;
    }

    private BigDecimal parseNumericValue(String rawValue) {
        String numeric = CalculatorUtil.extraerParteNumerica(rawValue);
        if (numeric.isEmpty()) {
            LogUtil.warn("No se pudo extraer parte numérica de: " + rawValue);
            return null;
        }

        char decimalSeparator = determineDecimalSeparator(numeric);
        try {
            if (numeric.indexOf(',') == -1 && numeric.indexOf('.') == -1) {
                return new BigDecimal(numeric);
            }
            return CalculatorUtil.parseConSeparadorDecimal(numeric, decimalSeparator);
        } catch (Exception ex) {
            LogUtil.warn("Error al parsear valor numérico '" + numeric + "': " + ex.getMessage());
            return null;
        }
    }

    private char determineDecimalSeparator(String numericValue) {
        int lastComma = numericValue.lastIndexOf(',');
        int lastDot = numericValue.lastIndexOf('.');
        if (lastComma > lastDot) {
            return ',';
        }
        return '.';
    }

    private String normalizeText(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace('\u00A0', ' ').trim();
    }

    private String extractProductLabel(List<WebElement> cells) {
        if (cells.isEmpty()) {
            return "Registro sin descripción";
        }
        return normalizeText(cells.get(0).getText());
    }

    /**
     * Verifica si el encabezado de la columna «Código producto» del panel de revisión es visible actualmente.
     *
     * @return {@code true} cuando la columna ya está visible y, por ende, el panel se encuentra desplegado.
     */
    private boolean isProductCodeColumnVisible() {
        return waitUtil.isElementVisible(PRODUCT_CODE_HEADER_LOCATOR, 2000, 200);
    }
}
