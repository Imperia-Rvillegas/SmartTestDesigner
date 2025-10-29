# Creación de Nuevos Casos de Prueba UI

Este procedimiento detalla los pasos para incorporar un escenario de interfaz de usuario al framework.

## 1. Crear el archivo `.feature`

Ubicación sugerida: `src/test/resources/features/uiFeatures/nombre.feature`.

Ejemplo:

```gherkin
@ui @modulo @tipo @id
Feature: Gestión de unidades
  Como usuario del sistema
  Quiero crear una nueva unidad
  Para gestionarla correctamente

  Scenario: Crear unidad válida
    Given que el usuario está en la vista de Unidades
    When hace clic en el botón Nuevo
    And completa los campos Nombre con "KG", Descripción con "Kilos" y Número de decimales con "2"
    And hace clic en el botón Aceptar
    Then la nueva unidad "KG" aparece en la lista de unidades
```

## 2. Implementar la clase PageObject

Ubicación sugerida: `src/main/java/ui/pages/TuNuevaPagina.java`.

```java
public class ReportPage extends BasePage {

    private final By generarBtn = By.id("generate-report");

    public ReportPage(WebDriver driver, PageManager pageManager) {
        super(driver, pageManager);
    }

    public void clickGenerarReporte() {
        click(findVisibleElement(generarBtn), "Botón Generar Reporte");
    }
}
```

## 3. Crear o actualizar el StepDefinition

Ubicación sugerida: `src/test/java/stepdefinitions/uiSteps/NOMBRESteps.java`.

```java
public class UnitSteps {

  private final PageManager pageManager = Hooks.getPageManager();
  private final UnitPage unitPage = pageManager.getUnitPage();

  @Given("que el usuario está en la vista de Unidades")
  public void vistaUnidades() {
    pageManager.getNavigationUtil().goToModuleFromMenu("Unidades");
  }
}
```

## 4. Registrar la página en PageManager

Ubicación: `src/main/java/ui/manager/PageManager.java`.

```java
private ReportPage reportPage;

public ReportPage getReportPage() {
    if (reportPage == null) {
        reportPage = new ReportPage(driver, this);
    }
    return reportPage;
}
```

## 5. Crear el Runner y actualizar regresiones

Cada pantalla o módulo debe contar con un Runner en `src/test/java/runners/NOMBRETest.java` que apunte al feature correspondiente. También debe añadirse al plan de regresión del pipeline.

## 6. Registrar el caso en Xray

Si el escenario no existe en Xray, es necesario crearlo, obtener su identificador y agregarlo en la etiqueta `@TEST_DEV-` del escenario automatizado.
