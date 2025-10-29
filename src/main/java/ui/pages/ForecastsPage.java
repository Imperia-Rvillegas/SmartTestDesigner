package ui.pages;

import api.AuthenticationAPI;
import api.ForecastAPI;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.openqa.selenium.*;
import ui.base.BasePage;
import ui.manager.PageManager;
import ui.utils.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Página de Previsiones (ForecastsPage) del sistema.
 * Proporciona acciones para interactuar con la tabla de previsiones,
 * como editar celdas correspondientes al mes siguiente y validar su valor.
 */
public class ForecastsPage extends BasePage {

    private final By dropdownTrigger = By.xpath("//div[@role='button' and contains(@class,'p-dropdown-trigger') and @aria-label='dropdown trigger']");
//    private BigDecimal expectedValue;
    private String expectedValue = tableUtil.getLastCellValue();
    private String amount = "2";
//    private BigDecimal amount = BigDecimal.valueOf(2);
    private List<String> dimensionsApi;
    private String initialValue;
//    private BigDecimal finalValueBigDecimal;
//    private String initialValue = "";
    private String finalValue;
    private String currentCellText;
//    private int numberOfDecimals = 0;

    /**
     * Constructor que inicializa la página de previsiones.
     *
     * @param driver      instancia del WebDriver.
     * @param pageManager instancia del PageManager para acceso a utilidades.
     */
    public ForecastsPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    /**
     * Selecciona una opción de agrupación temporal en la interfaz de usuario según el criterio especificado.
     * <p>
     * Si el criterio es "Días", además de seleccionar la agrupación correspondiente, ajusta la fecha final
     * automáticamente al primer día del mes siguiente en formato "MM/yyyy". Esta operación implica:
     * <ul>
     *   <li>Generar la fecha del próximo mes usando {@link TableUtil#getNextMonthDate()}</li>
     *   <li>Limpiar y escribir dicha fecha en el segundo campo de tipo calendario</li>
     *   <li>Confirmar la entrada presionando Enter</li>
     * </ul>
     * Luego de esto, hace clic en la opción de agrupación y espera que la interfaz actualice la vista.
     *
     * @param criterion el nombre del criterio de agrupación deseado (por ejemplo: "Meses", "Semanas", "Días").
     *                  La comparación no distingue entre mayúsculas y minúsculas.
     *
     * @see TableUtil#getNextMonthDate()
     * @see WaitUtil#waitForTemporalGroupingChangeToComplete()
     */
    public void selectGrouping(String criterion) {
        if ("Días".equalsIgnoreCase(criterion)) {
            LogUtil.info("Se ha seleccionado la agrupación por Días. Se reduge rango de fechas");
            // Otener fecha del mes siguiente
            String text = tableUtil.getNextMonthDate();

            // Ingresar fecha
            WebElement element = waitUtil.findVisibleElement(By.xpath("(//imp-input-calendar//input[@type='text'])[2]"));
            safeClear(element, "Fecha final");
            waitUtil.sleepMillis(300, "Espera que se termine de borrar el contenido del campo");
            sendKeysByElement(element, text, "Fecha final");
            pressEnterKey(element);
        }

        // Clic en la agrupación
        clickButtonByName(criterion);

        // Esperar que cargue la agrupación
        waitUtil.waitForTemporalGroupingChangeToComplete();
    }

    /**
     * Valida que el valor mostrado en la **primera celda** del período siguiente
     * (según la agrupación actual) sea igual al valor esperado tras una edición:
     * <pre>valor_esperado = initialValue + amount</pre>
     *
     * <p><b>Cómo funciona:</b></p>
     * <ol>
     *   <li>Resuelve el nombre de la columna del <i>siguiente</i> período en función de {@code criterio}:
     *       "Meses" → {@link tableUtil#getColumnNameNextMonth()},
     *       "Semanas" → {@link tableUtil#getColumnNameNextWeek()},
     *       "Días" → {@link tableUtil#getColumnNameNextDay()}.</li>
     *   <li>Ubica la primera celda de esa columna en la sección "Previsiones".</li>
     *   <li>Parsea el texto de la celda a {@link java.math.BigDecimal}.</li>
     *   <li>Calcula el valor esperado como {@code initialValue + amount} y lo compara con el valor actual.</li>
     *   <li>Registra en el log los valores inicial, modificación, actual y esperado.</li>
     * </ol>
     *
     * <p><b>Precondiciones:</b></p>
     * <ul>
     *   <li>Los campos de instancia {@code initialValue} y {@code amount} deben estar definidos
     *       previamente con los valores tomados antes y durante la edición.</li>
     *   <li>La tabla y la sección "Previsiones" deben estar visibles y accesibles vía {@code tableUtil}.</li>
     * </ul>
     *
     * <p><b>Efectos colaterales:</b> actualiza el campo {@code expectedValue} con el valor esperado
     * y escribe trazas mediante {@code LogUtil}.</p>
     *
     * @param criterio Agrupación activa de la vista. Valores válidos: {@code "Meses"}, {@code "Semanas"} o {@code "Días"}.
     *
     * @throws IllegalArgumentException si {@code criterio} no es uno de los valores válidos.
     * @throws AssertionError si el valor actual de la celda <i>no</i> coincide con el valor esperado.
     *
     * @implNote Este metodo no realiza esperas explícitas ni reintentos. Cualquier excepción en llamadas
     *           subyacentes (p. ej., al parsear valores o ubicar elementos) puede propagarse.
     * @see tableUtil#getColumnNameNextMonth()
     * @see tableUtil#getColumnNameNextWeek()
     * @see tableUtil#getColumnNameNextDay()
     * @see tableUtil#getFirstCellElementByHeaderName(String, String)
     * @see CalculatorUtil#parseToBigDecimal(String)
     * @see CalculatorUtil#addValues(java.math.BigDecimal, java.math.BigDecimal)
     */
    public void validateUpdatedValue(String criterio) {
        String nextColumn;

        switch (criterio) {
            case "Meses":
                nextColumn = tableUtil.getColumnNameNextMonth();
                break;
            case "Semanas":
                nextColumn = tableUtil.getColumnNameNextWeek();
                break;
            case "Días":
                nextColumn = tableUtil.getColumnNameNextDay();
                break;
            default:
                throw new IllegalArgumentException("Agrupación no reconocida: " + criterio);
        }

        WebElement firstCell = tableUtil.getFirstCellElementByHeaderName(nextColumn, "Previsiones");
        String presentValue = firstCell.getText().trim();

        LogUtil.info("Valor inicial en celda de [" + nextColumn + "]: " + initialValue);
        LogUtil.info("Cantidad de la modificacion en [" + nextColumn + "]: " + amount);
        LogUtil.info("Valor actual en celda de [" + nextColumn + "]: " + presentValue);

        //Calcular el valor esperado
        expectedValue = CalculatorUtil.addValues(initialValue, amount);

        LogUtil.info("Valor esperado: " + expectedValue);

        if (presentValue.equals(expectedValue)) {
            LogUtil.info("El valor de la celda se mantiene correctamente tras la edición.");
        } else {
            String error = "El valor de la celda no coincide con el esperado. "
                    + "Esperado: " + expectedValue + ", Actual: " + presentValue;
            LogUtil.error(error);
            throw new AssertionError(error);
        }
    }

    /**
     * Edita el valor de la celda correspondiente al período siguiente (mes, semana o día)
     * en función del criterio de agrupación especificado.
     *
     * @param criterio agrupación actual seleccionada: "Meses", "Semanas" o "Días".
     */
    public void editNextPeriodValue(String criterio) {

        String nextHeader;
        switch (criterio) {
            case "Meses":
                nextHeader = tableUtil.getColumnNameNextMonth();
                break;
            case "Semanas":
                nextHeader = tableUtil.getColumnNameNextWeek();
                break;
            case "Días":
                nextHeader = tableUtil.getColumnNameNextDay();
                break;
            default:
                throw new IllegalArgumentException("Criterio de agrupación no reconocido: " + criterio);
        }

        // Obtener el WebElement de la primera celda de esa columna
        WebElement firstCell = tableUtil.getFirstCellElementByHeaderName(nextHeader, "Previsiones");

        // Guardar el valor actual de la celda para usarlo más adelante
        initialValue = firstCell.getText().trim();
//        initialValue = CalculatorUtil.parseToBigDecimal(initialValue);
        LogUtil.info("Valor original en celda de [" + nextHeader + "]: " + initialValue);

        // Hacer clic para seleccionar
        clickByElement(firstCell, "Celda editable para el período siguiente");

        // Hacer clic en boton editar celda
        WebElement editCellButton = tableUtil.getEditButtonVisible();
        clickByElement(editCellButton, "Editar celda");

        // Ingresar cantidad en el campo de texto
        WebElement input = tableUtil.getInputDecimalVisible();
        sendKeysByElement(input, amount, "Input para ingresar nueva cantidad de la celda");

        // Hacer clic en boton sumar
        WebElement addButton = tableUtil.getAddButtonVisible();
        clickByElement(addButton, "Sumar");

        // Hacer clic en boton Aplicar
        clickButtonByName("Aplicar");
    }

    /**
     * Refresca la página para simular recarga completa del módulo.
     */
    public void refreshPageAndWait() {
        refreshPage();
        waitUtil.sleepMillis(500, "Esperar recarga de página");
    }

    /**
     * Valida que el valor editado en la celda persista tras el refresco, según el tipo de operación.
     *
     * @param operation Tipo de operación aplicada (suma, resta, multiplicación, división o reemplazo).
     */
    public void validateCorrectCellModification(String operation) {
        int row = tableUtil.getLastRowIndex();
        int column = tableUtil.getLastColumnIndex();
//        BigDecimal initialValue = tableUtil.getLastCellValue();
//        BigDecimal expectedValue;

        switch (operation.toLowerCase()) {
            case "suma":
                expectedValue = CalculatorUtil.addValues(initialValue, amount);
                break;
            case "resta":
                expectedValue = CalculatorUtil.subtractValues(initialValue, amount);
                break;
            case "multiplicacion":
                expectedValue = CalculatorUtil.multiplyValues(initialValue, amount);
                break;
            case "division":
                expectedValue = CalculatorUtil.divideValues(initialValue, amount);
                break;
            case "reemplazo":
                expectedValue = amount;
                break;
            default:
                throw new IllegalArgumentException("Operación no soportada: " + operation);
        }

        WebElement newCell = tableUtil.getCellElement(row, column, "Previsiones");
        scrollToElementIfNotVisible(newCell);
//        String newCellString = newCell.getText();
        finalValue = newCell.getText();
        waitUtil.sleepMillis(2000, "Esperar que el valor se actualice");
        ValidationUtil.assertEqualsWithTolerance(finalValue, expectedValue, "Validación de persistencia para operación: " + operation, 1);
    }

    /**
     * Localiza una celda en la tabla "Previsiones" que cumpla con una condición específica
     * y aplica una operación matemática sobre ella.
     *
     * <p>El flujo completo es:
     * <ol>
     *     <li>Determina el nombre de la columna del mes actual.</li>
     *     <li>Busca la primera celda que cumpla con la condición dada (e.g. "modificada", "sin modificar").</li>
     *     <li>Hace scroll hasta esa celda y realiza clic para seleccionarla.</li>
     *     <li>Hace clic en el botón de edición de la celda.</li>
     *     <li>Selecciona el botón correspondiente a la operación indicada (suma, resta, multiplicación, división, reemplazo).</li>
     *     <li>Ingresa una cantidad en el input de la celda.</li>
     *     <li>Hace clic en "Aplicar" para confirmar la operación.</li>
     *     <li>Espera 2 segundos para que se refleje la actualización.</li>
     * </ol>
     *
     * @param condition condición que debe cumplir la celda (por ejemplo: "modificada", "sin modificar", "modificada con pedido pendiente", etc.)
     * @param operation operación a aplicar sobre la celda. Valores válidos: "suma", "resta", "multiplicacion", "division", "reemplazo".
     * @throws IllegalArgumentException si la operación especificada no es soportada.
     */
    public void locatesACellWithGivenStateAndAppliesOperation(String condition, String operation) {
        // Obtiene el nombre de la columna
        String currentMonth = tableUtil.getColumnNameCurrentMonth();

        // Obtiene la celda aplicando las condiciones
        WebElement cell = tableUtil.findFirstCellMatchingCondition(currentMonth, "Previsiones", condition);

        // Aplica la operacion
        applyOperation(cell, operation, false, null);
    }

    /**
     * Aplica una operación matemática sobre una celda editable de la tabla de previsiones.
     *
     * @param cell          {@link WebElement} que representa la celda sobre la que se aplicará la operación.
     * @param operation     Nombre de la operación a aplicar (puede ser: "suma", "resta", "multiplicacion", "division", "reemplazo").
     * @param haveConcepts Indica si el valor corresponde a un concepto.
     * @param conceptName   Nombre del concepto si aplica (requerido si {@code isConceptCell} es true).
     *
     * @throws IllegalArgumentException si la operación no está soportada o si {@code isConceptCell} es true pero {@code conceptName} es nulo o vacío.
     */
    public void applyOperation(WebElement cell, String operation, boolean haveConcepts, String conceptName) {
        if (haveConcepts && (conceptName == null || conceptName.isBlank())) {
            throw new IllegalArgumentException("El nombre del concepto es requerido cuando haveConcepts es true.");
        }

        // Obtiene la cantidad
//        amount = tableUtil.getAmount();

        // Obtiene el valor inicial del producto
        initialValue = cell.getText();
//        initialValue = CalculatorUtil.parseToBigDecimal(initialValue);

        // Scroll hasta el elemento
        scrollToElementIfNotVisible(cell);

        // Hace clic en la celda
        clickByElement(cell, "Celda correspondiente");

        // Hace clic en el botón para editar la celda
        WebElement editCellButton = tableUtil.getEditButtonVisible();
        clickByElement(editCellButton, "Editar celda");

        // Lógica específica para conceptos
        if (haveConcepts) {
            LogUtil.infof("Aplicando operación a un concepto: {}", conceptName);
            clickByLocator(dropdownTrigger, "Boton para desplegar lista de conceptos");
            clickDropdownOptionByText(conceptName);
        }

//        // Localizador XPath
//        By valueLocator = By.cssSelector("input.p-inputnumber-input");
//
//        // Obtiene el elemento
//        WebElement elementValue = waitUtil.waitForPresenceOfElement(valueLocator);
//
//        // Extrae el texto
//        String textValue = elementValue.getAttribute("value");
//
//        // Cuenta la cantidad de decimales
//        numberOfDecimals = CalculatorUtil.countDecimals(textValue);

        // Obtiene el separador de miles
//        CalculatorUtil.getDecimalSeparator();

        // Obtiene el botón de la operación
        WebElement operationButton;
        switch (operation.toLowerCase()) {
            case "suma":
                operationButton = tableUtil.getAddButtonVisible();
                expectedValue = CalculatorUtil.addValues(initialValue, amount);
                break;
            case "resta":
                operationButton = tableUtil.getSubtractButtonVisible();
                expectedValue = CalculatorUtil.subtractValues(initialValue, amount);
                break;
            case "resta por valor mayor":
                operationButton = tableUtil.getSubtractButtonVisible();
                amount = CalculatorUtil.addValues(initialValue, amount);
                expectedValue = CalculatorUtil.subtractValues(initialValue, amount);
                break;
            case "multiplicacion":
                operationButton = tableUtil.getMultiplyButtonVisible();
                expectedValue = CalculatorUtil.multiplyValues(initialValue, amount);
                break;
            case "division":
                operationButton = tableUtil.getDivideButtonVisible();
                expectedValue = CalculatorUtil.divideValues(initialValue, amount);
                break;
            case "reemplazo":
                operationButton = tableUtil.getReplaceButtonVisible();
                expectedValue = amount;
                break;
            default:
                throw new IllegalArgumentException("Operación no soportada: " + operation);
        }

        /**
         * Establece el valor esperado como "0" cuando se realiza una resta por un número mayor
         * al valor inicial.
         *
         * <p>Si la operación es "resta por valor mayor" y el monto a restar (`amount`) es mayor
         * que el valor inicial (`initialValue`), entonces `expectedValue` se asigna como la cadena "0".</p>
         *
         * <p>Esto evita representar resultados negativos cuando el valor restado excede al inicial.</p>
         */
        if ("resta por valor mayor".equals(operation) && amount.compareTo(initialValue) > 0) {
            expectedValue = "0";
        }

        // Hace clic en el botón de la operación
        clickByElement(operationButton, "Botón operación: " + operation);

        // Ingresa la cantidad
        WebElement input = tableUtil.getInputDecimalVisible();
//        safeClear(input, "Input para cantidad");
        sendKeysByElement(input, amount, "Input para ingresar cantidad");

        // Hace clic en el botón aplicar
        clickButtonByName("Aplicar");
        // esta espera debe ser explicita para asegurar que la tabla se actualice
        waitUtil.sleepMillis(2000, "Esperando que se refleje la actualización");
    }


    /**
     * Elimina dinámicamente todas las dimensiones de la tabla "Jerarquía actual", excepto la última.
     *
     * <p>Este metodo realiza las siguientes acciones:
     * <ol>
     *     <li>Hace clic en el botón "Agrupación" para abrir la tabla de jerarquía actual.</li>
     *     <li>Accede a la tabla usando {@link TableUtil#getTable(String)} con título "Jerarquía actual".</li>
     *     <li>Obtiene la lista de elementos visibles de la columna "Value" (dimensiones).</li>
     *     <li>Itera sobre las dimensiones y elimina una a una haciendo clic primero en la celda y luego en el botón "Eliminar".</li>
     *     <li>Repite hasta que solo quede una dimensión (no se puede eliminar la última).</li>
     * </ol>
     *
     * <p>La tabla usa scroll virtual, por lo tanto se asegura el renderizado con scroll dinámico
     * y esperas explícitas mediante {@link WaitUtil#scrollUntilElementIsVisible(By)} y {@link WaitUtil#waitForElementToBeVisible(WebElement)}.</p>
     */
    public void deletePreviousHierarchy() {
        // Paso 1: Abrir sección de agrupación si aplica
        clickButtonByName("Agrupación");

        String tableTitle = "Jerarquía actual";
        By valueCellsLocator = By.cssSelector("td.imperia-table-column-Value span");

        // Paso 2: Esperar tabla visible y obtener referencia
        WebElement table = tableUtil.getTable(tableTitle);

        // Paso 3: Obtener y eliminar todas las dimensiones, menos una
        while (true) {
            // Refrescar lista actual de dimensiones visibles
            List<WebElement> dimensions = waitUtil.findVisibleElements(table, valueCellsLocator);

            if (dimensions.size() <= 1) {
                LogUtil.info("Solo queda una dimensión. Finalizando eliminación.");
                break;
            }

            // Seleccionar la primera dimensión visible (evita problemas con scroll)
            WebElement firstDimension = dimensions.getFirst();
            waitUtil.waitForElementToBeVisible(firstDimension);

            String dimensionName = firstDimension.getText().trim();
            LogUtil.info("Eliminando dimensión: " + dimensionName);

            // Paso 4: Hacer clic sobre la celda de la dimensión
            clickByElement(firstDimension, "Dimension: " + dimensionName);

            // Paso 5: Hacer clic en botón "Eliminar"
            clickButtonByName("Eliminar");

            // Esperar que desaparezca la fila eliminada antes de continuar
            waitUtil.sleepMillis(300, "Esperando que desaparezca la dimensión eliminada");
        }
    }

    /**
     * Obtiene desde la API el conjunto de dimensiones disponibles para la configuración
     * del último nivel de agrupación jerárquica.
     *
     * <p>Este metodo realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Solicita y almacena un token de autenticación válido usando {@link AuthenticationAPI#getToken()}.</li>
     *   <li>Realiza una llamada a la API de previsiones mediante {@link ForecastAPI#getMinimunLevelAgregation()}.</li>
     *   <li>Guarda la respuesta en el {@code scenarioContext} para futuras validaciones o inspecciones.</li>
     *   <li>Extrae el nodo "Data" del JSON de respuesta, que representa un mapa clave-valor con las dimensiones disponibles.</li>
     *   <li>Filtra el mapa eliminando valores nulos, y construye una lista de nombres de dimensiones válidas.</li>
     * </ol>
     *
     * @return Lista de nombres de dimensiones no nulas obtenidas desde el nodo {@code Data} del JSON de respuesta.
     */
    public List<String> getDimensionsFromApi() {
        Response response = ForecastAPI.getMinimunLevelAgregation();

        List<String> dimensions = new ArrayList<>();

        // Extrae el nodo "Data" del JSON
        JsonPath jsonPath = response.jsonPath();
        Map<String, String> data = jsonPath.getMap("Data");

        // Recorre el mapa y agrega solo los valores no nulos
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                dimensions.add(entry.getValue());
            }
        }

        return dimensions;
    }

    /**
     * Configura los niveles de agrupación en la jerarquía actual agregando las dimensiones recibidas desde la API.
     *
     * <p>Este metodo realiza las siguientes acciones en orden:</p>
     * <ol>
     *     <li>Agrega primero la dimensión "Código producto".</li>
     *     <li>Elimina la primera fila de la tabla de jerarquía, que corresponde a la dimensión que no pudo eliminarse antes.</li>
     *     <li>Agrega el resto de dimensiones obtenidas desde la API a través de la tabla "Nuevo nivel de la jerarquía de agrupación".</li>
     * </ol>
     *
     * <p>El flujo simula acciones del usuario en la UI:</p>
     * <ul>
     *     <li>Presionar el botón "Nuevo".</li>
     *     <li>Seleccionar la dimensión en la tabla mediante scroll si es necesario.</li>
     *     <li>Presionar "Aceptar" para agregar la dimensión.</li>
     * </ul>
     */
    public void configureTheLastGroupingLevel() {
        dimensionsApi = getDimensionsFromApi();

        // Paso 1: Agregar la primera dimensión obligatoria: "Código producto"
        String firstDimension = "Código producto";
        LogUtil.info("Agregando primera dimensión fija: " + firstDimension);
        addGroupingFromTable(firstDimension);

        // Paso 2: Eliminar la primera fila de la jerarquía (residual que no se pudo eliminar antes)
        LogUtil.info("Eliminando dimensión residual de la jerarquía anterior...");
        removeFirstDimensionOfHierarchy();

        addRemainingDimensions(firstDimension);
    }

    /**
     * Agrega dinámicamente todas las dimensiones restantes a la agrupación,
     * excluyendo una dimensión principal que ya fue agregada previamente.
     *
     * <p>Este metodo recorre la lista de dimensiones disponibles (`dimensionsApi`) y,
     * por cada una distinta a la dimensión principal (`firstDimension`), la agrega a través
     * de la interfaz de usuario mediante el metodo {@code addGroupingFromTable}.</p>
     *
     * <p>Al finalizar, hace clic en el botón "Aceptar" de la tabla "Jerarquía actual"
     * para confirmar la configuración de agrupación.</p>
     *
     * @param firstDimension nombre de la dimensión que ya fue agregada manualmente y no debe repetirse.
     */
    public void addRemainingDimensions(String firstDimension) {
        // Agregar el resto de dimensiones dinámicamente
        for (String dimension : dimensionsApi) {
            if (dimension.equalsIgnoreCase(firstDimension)) {
                continue; // Ya fue agregada manualmente
            }

            LogUtil.info("Agregando dimensión dinámica a la agrupación: " + dimension);
            addGroupingFromTable(dimension);
        }

        // Paso final: Clic en aceptar en tabla Jerarquía actual
        clickButtonByNameLast("Aceptar");
    }

    /**
     * Agrega una dimensión seleccionándola desde la tabla "Nuevo nivel de la jerarquía de agrupación".
     *
     * <p>Este metodo realiza:</p>
     * <ul>
     *     <li>Click en el botón "Nuevo".</li>
     *     <li>Scroll y selección de la dimensión en la tabla correspondiente.</li>
     *     <li>Click en el botón "Aceptar".</li>
     * </ul>
     *
     * @param dimension Nombre exacto de la dimensión a agregar.
     */
    private void addGroupingFromTable(String dimension) {
        // Abrir el modal de nueva dimensión
        clickButtonByNameAndPosition("Nuevo", 2);

        // Esperar y localizar la tabla de dimensiones
        WebElement tableNewLevel = tableUtil.getTable("Nuevo nivel de la jerarquía de agrupación");

        // Buscar la fila con el nombre de la dimensión
        By cellNameDimension = By.xpath(".//td[contains(@class, 'imperia-table-column-Value')]//span[contains(normalize-space(.), '" + dimension.trim() + "')]");

        // Hace scroll hasta el elemento
        WebElement cell = waitUtil.scrollUntilElementIsVisible(tableNewLevel, cellNameDimension);
        waitUtil.waitForElementToBeClickable(cell);

        clickByElement(cell, dimension);

        // Confirmar la selección
        clickButtonByNameLast("Aceptar");
    }

    /**
     * Elimina la primera fila de la tabla "Jerarquía actual", correspondiente a la dimensión residual.
     *
     * <p>Se usa después de agregar la primera dimensión nueva, ya que la anterior no pudo eliminarse en
     * ejecuciones anteriores.</p>
     */
    private void removeFirstDimensionOfHierarchy() {
        WebElement tableHierarchy = tableUtil.getTable("Jerarquía actual");

        // Localizar la primera celda de la columna "Value"
        List<WebElement> cells = waitUtil.findVisibleElements(tableHierarchy,
                By.cssSelector("td.imperia-table-column-Value span"));

        if (cells.isEmpty()) {
            LogUtil.warn("No hay dimensiones visibles en la tabla para eliminar.");
            return;
        }

        WebElement first = cells.getFirst();
        waitUtil.waitForElementToBeVisible(first);
        clickByElement(first, "Primera dimension");

        String name = first.getText().trim();
        LogUtil.info("Eliminando dimensión residual: " + name);

        clickButtonByName("Eliminar");

        waitUtil.sleepMillis(300, "Esperando a que se actualice la tabla luego de eliminar la dimensión.");
    }

    /**
     * Busca en la tabla de previsiones un producto que tenga más de 2 registros (filas)
     * y cuyas celdas en las columnas correspondientes a los próximos 6 meses cumplan con una condición específica.
     *
     * <p>Este metodo obtiene primero el token de autenticación, luego consulta el perfil del usuario
     * mediante la API para determinar el formato numérico (separador decimal), y finalmente delega
     * la búsqueda a {@code tableUtil.findProductWithConditionInMonthColumns}.</p>
     *
     * @param condition la condición que deben cumplir las celdas, por ejemplo:
     *                  "sin modificar sin pedido pendiente", "modificada con pedido pendiente", etc.
     * @return {@code true} si se encuentra un producto que cumpla con la condición en las celdas correspondientes;
     *         {@code false} en caso contrario.
     */
    public void findProductWithConditionInMonthColumns(String condition) {
        tableUtil.findProductWithConditionInMonthColumns(condition, "Previsiones");
    }

    /**
     * Elimina dinámicamente todas las dimensiones visibles en la sección de agrupación de previsiones,
     * excepto la dimensión "Código producto".
     *
     * <p>Este metodo realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Abre la sección "Agrupación".</li>
     *   <li>Localiza la tabla "Jerarquía actual".</li>
     *   <li>Itera sobre las dimensiones visibles y elimina una por una todas aquellas distintas a "Código producto".</li>
     *   <li>Confirma la acción haciendo clic en el botón "Aceptar".</li>
     * </ol>
     *
     * <p>El flujo está diseñado para asegurar que solo quede una única dimensión activa: "Código producto".</p>
     */
    public void deleteHierarchyExceptCodigoProducto() {
        // Paso 1: Abrir sección de agrupación si aplica
        clickButtonByName("Agrupación");

        String tableTitle = "Jerarquía actual";
        By dimensionLocator = By.cssSelector("td span"); // Busca cualquier texto dentro de celdas

        // Paso 2: Esperar tabla visible
        WebElement table = tableUtil.getTable(tableTitle);

        // Paso 3: Eliminar dinámicamente todas las dimensiones distintas de "Código producto"
        while (true) {
            List<WebElement> dimensions = waitUtil.findVisibleElements(table, dimensionLocator);

            // Filtrar solo dimensiones distintas de "Código producto"
            List<WebElement> removable = dimensions.stream()
                    .filter(d -> !d.getText().trim().equalsIgnoreCase("Código producto"))
                    .toList();

            if (removable.isEmpty()) {
                LogUtil.info("Solo queda la dimensión 'Código producto'. Finalizando eliminación.");
                break;
            }

            // Eliminar la primera dimensión distinta
            WebElement first = removable.getFirst();
            waitUtil.waitForElementToBeVisible(first);

            String name = first.getText().trim();
            LogUtil.info("Eliminando dimensión: " + name);

            clickByElement(first, "Dimensión: " + name);
            clickButtonByName("Eliminar");

            waitUtil.sleepMillis(500, "Esperando que desaparezca la dimensión eliminada");
        }

        clickButtonByNameLast("Aceptar");
    }

    /**
     * Modifica el valor de una celda correspondiente a un producto previamente localizado
     * aplicando una operación matemática específica (suma, resta, multiplicación, división o reemplazo).
     *
     * <p>Este metodo realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Filtra la tabla por el código del producto almacenado previamente.</li>
     *   <li>Espera a que la tabla se renderice completamente.</li>
     *   <li>Obtiene la unidad del producto desde la columna "UnitName".</li>
     *   <li>Localiza la primera celda del mes donde se encontraron valores válidos (almacenado en {@code lastMonth}).</li>
     *   <li>Guarda el valor inicial de esa celda para referencia y validación.</li>
     *   <li>Aplica la operación matemática especificada sobre esa celda.</li>
     *   <li>Guarda el valor final de la celda para validación posterior.</li>
     * </ol>
     *
     * @param operation nombre de la operación a aplicar sobre la celda del producto.
     *                  Puede ser: "suma", "resta", "multiplicacion", "division", "reemplazo".
     */
    public void modifyTheProductByApplying(String operation) {
        // Filtrar el código producto
        String productCode = tableUtil.getLastProductCode();
        filterBy("Código producto", productCode);

        // Espera que cargue completamente la tabla
        waitUtil.waitForTableToLoadCompletely();

        // Obtiene la primera celda de la columna correspondiente al mes encontrado
        String headerName = tableUtil.getLastMonth();
        WebElement element = tableUtil.getFirstCellElementByHeaderName(headerName, "Previsiones");

        // Aplica la operación
        applyOperation(element, operation, false, null);

        // Guardar valor final del producto
        try {
            finalValue = element.getText();
//            finalValueBigDecimal = CalculatorUtil.parseToBigDecimal(finalValueString);
            LogUtil.info("El valor final del producto es: " + finalValue);
        } catch (NumberFormatException e) {
            LogUtil.error("No se pudo convertir el texto a double: " + e.getMessage());
        }

        //Validar que el valor actual de la celda es el esperado
//        expectedValue = CalculatorUtil.formatToOriginalFormat(expectedValue, initialValue);
        ValidationUtil.assertEquals(finalValue, expectedValue, "Validar que el valor actual de la celda es el esperado");
    }

    /**
     * Verifica que la suma de todos los valores actuales de las celdas modificadas
     * sea igual al valor final registrado del producto.
     *
     * <p>Esta validación asegura que la distribución del valor total entre las celdas
     * (por ejemplo, por mes) se haya realizado correctamente.</p>
     *
     * <p>Compara el valor acumulado mediante {@code tableUtil.getSumaValoresActuales()}
     * con el valor final del producto almacenado en {@code finalProductValue}.</p>
     *
     * @throws AssertionError si los valores no coinciden exactamente.
     */
    public void theSumOfAllCellsIsCorrect() {
        // Validar que cada celda fue modificada respecto a su valor original
        tableUtil.addCells("Previsiones");

        // Compara la suma de todas las celdas con el valor final del producto
        String sumOfAllCellsString = tableUtil.getSumCurrentValues();
        ValidationUtil.assertEqualsWithTolerance(finalValue, sumOfAllCellsString, "Validar que la suma de todas las celdas es igual a la cantidad total del producto", 1);
    }

    /**
     * Valida que el valor actual de la celda modificada en la tabla de previsiones
     * coincida con el valor esperado.
     *
     * <p>Este metodo obtiene la última celda editada (usando el índice de la última fila
     * y columna modificada) en la tabla identificada como "Previsiones", y compara su texto
     * visible con el valor esperado, previamente calculado y formateado.</p>
     *
     * <p>Utiliza {@code calculatorUtil} para convertir el valor esperado a su formato original,
     * y {@code validationUtil} para realizar la aserción.</p>
     *
     * <p>Si el valor actual no coincide con el esperado, se lanza una aserción indicando el fallo.</p>
     */
    public void validModifiedValue() {
        int rowIndex = tableUtil.getLastRowIndex();
        int columnIndex = tableUtil.getLastColumnIndex();

        WebElement element = tableUtil.getCellElement(rowIndex, columnIndex, "Previsiones");
        currentCellText = element.getText();
        expectedValue = tableUtil.getLastCellValue();
        ValidationUtil.assertEqualsWithTolerance(currentCellText, expectedValue, "Validar que el valor actual de la celda es el esperado",1);
    }

    /**
     * Abre la pantalla de <b>Previsiones</b> desde la interfaz de usuario.
     *
     * <p>Este metodo realiza las siguientes acciones secuenciales:</p>
     * <ol>
     *   <li>Hace clic en el botón identificado por el texto visible <b>"Previsiones"</b>.</li>
     *   <li>Valida que la pantalla cargada corresponda a <b>"Previsiones"</b>
     *       mediante {@link validationUtil#assertCurrentScreen(String)}.</li>
     * </ol>
     *
     * <p>Se utiliza para garantizar que la pantalla objetivo esté visible
     * y lista para la interacción antes de continuar con el flujo de prueba.</p>
     *
     * @throws AssertionError si la pantalla cargada no coincide con "Previsiones".
     */
    public void openTheForecastScreen() {
        clickButtonByName("Previsiones");

        // Espera que cargue la pantalla
        validationUtil.assertCurrentScreen("Previsiones");
    }

    /**
     * Localiza la primera celda que cumpla una condición específica en la pantalla <b>Previsiones</b>
     * y aplica una operación sobre ella.
     *
     * <p>Este metodo realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Obtiene el nombre de la columna correspondiente al mes actual
     *       mediante {@link tableUtil#getColumnNameCurrentMonth()}.</li>
     *   <li>Busca en la tabla <b>Previsiones</b> la primera celda que cumpla la condición indicada,
     *       utilizando {@link tableUtil#findFirstCellMatchingCondition(String, String, String)}.</li>
     *   <li>Aplica la operación especificada sobre la celda encontrada mediante
     *       {@link #applyOperation(WebElement, String, boolean, String)},
     *       pasando el nombre del concepto y habilitando la validación posterior.</li>
     * </ol>
     *
     * @param condition   la condición que debe cumplir la celda para ser seleccionada
     *                    (por ejemplo: "modificada con pedido pendiente").
     * @param operation   la operación a ejecutar sobre la celda (por ejemplo: "sumar", "restar").
     * @param conceptName el nombre del concepto asociado a la operación.
     *
     * @throws NoSuchElementException si no se encuentra ninguna celda que cumpla la condición.
     * @throws IllegalArgumentException si el tipo de condición no es soportado.
     */
    public void locateCellandApplyOperation(String condition, String operation, String conceptName) {
        // Obtiene el nombre de la columna
        String currentMonth = tableUtil.getColumnNameCurrentMonth();

        // Obtiene la celda aplicando las condiciones
        WebElement cell = tableUtil.findFirstCellMatchingCondition(currentMonth, "Previsiones", condition);

//        String Prueba = cell.getText();
//        LogUtil.info(Prueba);

        // Aplica la operacion
        applyOperation(cell, operation, true, conceptName);
    }

    /**
     * Valida que la cantidad aplicada a un concepto en la pantalla <b>Previsiones</b>
     * sea correcta en función de la diferencia esperada.
     *
     * <p>Este metodo realiza las siguientes acciones:</p>
     * <ol>
     *   <li>Construye un localizador {@link By} para identificar la celda que contiene el valor del concepto
     *       dentro de la tabla de <b>Conceptos</b>.</li>
     *   <li>Calcula la diferencia absoluta esperada entre los valores {@code expectedValue}
     *       e {@code initialValue} usando {@link CalculatorUtil#absoluteDifference(BigDecimal, BigDecimal)}.</li>
     *   <li>Convierte el valor esperado al formato de texto original mediante
     *       {@link CalculatorUtil#formatToOriginalFormat(BigDecimal, String)}.</li>
     *   <li>Obtiene la última posición de fila y columna utilizada en la tabla mediante {@link tableUtil#getLastRowIndex()}
     *       y {@link tableUtil#getLastColumnIndex()}.</li>
     *   <li>Localiza la celda en la tabla <b>Previsiones</b> y realiza un desplazamiento
     *       controlado hasta centrarla en pantalla.</li>
     *   <li>Hace clic repetidamente sobre la celda hasta que se despliegue correctamente la ventana de conceptos,
     *       verificando la visibilidad de la celda objetivo del concepto.</li>
     *   <li>Obtiene el valor actual del concepto, extrae únicamente la parte numérica mediante
     *       {@link CalculatorUtil#extraerParteNumerica(String)}, y lo compara con el valor esperado.</li>
     * </ol>
     *
     * @param conceptName nombre visible del concepto a validar.
     *
     * @throws AssertionError si el valor mostrado para el concepto no coincide con el valor esperado.
     * @throws NoSuchElementException si no se puede localizar la celda correspondiente al concepto.
     */
    public void quantityIsCorrectlyAppliedToTheConcept(String conceptName) {
        By valueTdLocator = By.xpath("//div[normalize-space(text())='Conceptos']" + "//following::table[1]//td[normalize-space(text())='" + conceptName + "']" + "/following-sibling::td[1]");
        String expectedConceptValue = CalculatorUtil.absoluteDifference(expectedValue, initialValue);
//        String expectedConceptValue = CalculatorUtil.formatToOriginalFormat(expectedConceptValue, initialValue);

        // Espera que la tabla cargue completamente
        waitUtil.waitForTableToLoadCompletely();

        //Obtener el valor actual del concepto
        //Hacer clic en la celda
        int rowIndex = tableUtil.getLastRowIndex();
        int columnIndex = tableUtil.getLastColumnIndex();

        WebElement element = tableUtil.getCellElement(rowIndex, columnIndex, "Previsiones");

        // Scroll hasta el elemento
        scrollToElementIfNotVisible(element);

        // Hace clic sobre la celda hasta que se despliega la ventana de conceptos correctamente
        clickUntilElementVisible(element, "Celda encontada", valueTdLocator, 3, 3, 300, 300, 100);

        //Obtener el texto del web ekement ejemplo "2 unidad"
        WebElement conceptCell = getConceptValueCell(conceptName);
        String conceptCellString = conceptCell.getText();

        // Quitar las letras y los espacios de la cadena de texto
        String formattedConceptValue = CalculatorUtil.extraerParteNumerica(conceptCellString);

        ValidationUtil.assertEqualsWithTolerance(formattedConceptValue, expectedConceptValue, "Validar que el valor del concepto se aplica correctamente", 1);
    }

    /**
     * Obtiene el elemento <td> siguiente al que contiene un texto específico dentro de una tabla localizada
     * a partir de un div con texto 'Conceptos'.
     *
     * Este metodo realiza:
     * <ul>
     *   <li>Busca un div con el texto exacto 'Conceptos'.</li>
     *   <li>Desde allí, encuentra una tabla descendiente.</li>
     *   <li>Dentro de esa tabla, localiza una celda <td> con el texto deseado (por ejemplo, 'CONCEPT-QA').</li>
     *   <li>Luego obtiene el siguiente <td>, que contiene el valor del concepto (por ejemplo, '2 Unidad').</li>
     * </ul>
     *
     * @param conceptCode Texto exacto del concepto a buscar (por ejemplo, 'CONCEPT-QA').
     * @return WebElement del <td> que contiene el valor del concepto específico.
     */
    public WebElement getConceptValueCell(String conceptCode) {
        LogUtil.info("Buscando valor de concepto para: " + conceptCode);

        // XPath que hace toda la búsqueda encadenada a partir del div con texto 'Conceptos'
        By valueTdLocator = By.xpath("//div[normalize-space(text())='Conceptos']" + "//following::table[1]//td[normalize-space(text())='" + conceptCode + "']" + "/following-sibling::td[1]");

        return waitUtil.findVisibleElement(valueTdLocator);
    }
}