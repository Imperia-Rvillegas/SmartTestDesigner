@all @ui @production
Feature: Pantalla Producción

  Como usuario del sistema
  Quiero administrar la producción desde la pantalla Producción
  Para poder exportar, buscar, filtrar y consultar registros correctamente

  @production01 @export @TEST_DEV-7488
  Scenario: Exportar lista Producción
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And el usuario accede a la pantalla "Producción" desde menu "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"
