@all @ui @forecastedMaterialRequirements
Feature: Pantalla Necesidades de materiales pronosticadas

  Como usuario del sistema
  Quiero administrar las necesidades de materiales desde la pantalla Necesidades de materiales pronosticadas
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @forecastedMaterialRequirements01 @export @TEST_DEV-7340
  Scenario: Exportar informe de necesidades de materiales pronosticadas
    Given el usuario está en la pantalla "Necesidades de materiales pronosticadas" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"