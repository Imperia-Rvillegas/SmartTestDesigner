@all @ui @forecastAnalysisList
Feature: Pantalla Listado de análisis de previsiones

  Como usuario del sistema
  Quiero administrar el listado de análisis de previsiones desde la pantalla Listado de análisis de previsiones
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @forecastAnalysisList01 @export @TEST_DEV-7376
  Scenario: Exportar informe de listado de análisis de previsiones
    Given el usuario está en la pantalla "Listado de análisis de previsiones" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"