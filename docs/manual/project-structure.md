# Estructura del Proyecto

La automatización se organiza en capas que permiten separar responsabilidades y facilitar el mantenimiento.

```plaintext
scripts/
├── run-suite/                  # Ejecuta una suite de pruebas específica
├── send-email-report/          # Envía un correo con el resumen de una ejecución
├── send-xray-report/           # Publica resultados Cucumber JSON en Xray Cloud
├── main/
src/
├── main/java/
│   ├── config/                 # Configuración global, WebDriver, cargadores YAML, contexto
│   ├── api/                    # Lógica para autenticación y consumo de APIs
│   ├── hooks/                  # Acciones previas y posteriores a los escenarios
│   └── ui/
│       ├── base/               # Clase BasePage con acciones reutilizables
│       ├── manager/            # PageManager que centraliza las páginas
│       ├── enums/              # Enumeraciones usadas en el proyecto
│       ├── pages/              # PageObjects específicos (LoginPage, UnitPage, etc.)
│       └── utils/              # Utilidades para navegación, ventanas emergentes y esperas
├── test/java/
│   ├── config/                 # Configuración y pruebas auxiliares para CI/CD
│   ├── testmodel/              # Clases de modelo (POJO)
│   ├── runners/                # Runners JUnit por suite
│   └── stepdefinitions/
│       ├── apiSteps/           # Steps de pruebas de servicios REST
│       └── uiSteps/            # Steps de pruebas de interfaz
└── resources/
    ├── testdata/
    │   ├── bodyRequest/        # JSON con cuerpos dinámicos para pruebas API
    │   ├── expectedResponse/   # JSON con respuestas esperadas
    │   └── page/               # YAML con datos de prueba usados en Steps
    ├── user/                   # YAML con credenciales de prueba
    └── features/               # Escenarios Gherkin
        ├── uiFeatures/         # Escenarios UI
        └── apiFeatures/        # Escenarios API
```

## Ejemplo de Step UI con PageManager

```java
public class UnitSteps {
  private final PageManager pageManager = Hooks.getPageManager();
  private final UnitPage unitPage = pageManager.getUnitPage();

  @When("hace clic en el botón Nuevo")
  public void clickNew() {
    unitPage.clickNew();
  }
}
```
