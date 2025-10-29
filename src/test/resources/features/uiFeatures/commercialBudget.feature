@all @ui @commercialBudget
Feature: Pantalla Presupuesto comercial

  Como usuario del sistema
  Quiero administrar el presupuesto comercial desde la pantalla Presupuesto comercial
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @commercialBudget01 @export @TEST_DEV-7353
  Scenario: Exportar informe de presupuesto comercial
    Given se comprueba que el plugin "Presupuesto comercial" esta activo
    And el usuario accede a la pantalla "Presupuesto comercial" desde menú
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"

  @commercialBudget02 @export @TEST_DEV-7354
  Scenario: Exportar informe de necesidades de materiales presupuestadas
    Given se comprueba que el plugin "Presupuesto comercial" esta activo
    And el usuario accede a la pantalla "Necesidades de materiales presupuestadas" desde menú
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"