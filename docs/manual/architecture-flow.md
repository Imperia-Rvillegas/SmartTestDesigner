# Flujo de Llamadas del Framework

El framework implementa una arquitectura basada en Cucumber, Selenium WebDriver y Java siguiendo el patrón Page Object Model. El flujo típico desde un escenario Gherkin hasta la ejecución en navegador se representa en el siguiente diagrama.

![Diagrama del flujo de llamadas desde el feature](../../images/Diagrama%20del%20flujo%20de%20llamadas%20desde%20el%20feature.png)

1. El escenario definido en el archivo `.feature` describe el comportamiento esperado en lenguaje natural.
2. Los StepDefinitions traducen cada paso del escenario en llamadas a métodos Java.
3. Los Steps delegan la interacción en los PageObjects gestionados por `PageManager`.
4. Los PageObjects utilizan la herencia de `BasePage` y las utilidades compartidas para interactuar con el DOM mediante Selenium WebDriver.
5. El resultado de cada acción se registra y se utiliza para generar reportes y evidencias.
