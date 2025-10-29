@all @ui @trackingChangesBetweenCycles
Feature: Pantalla Seguimiento de cambios entre ciclos

  Como usuario del sistema
  Quiero administrar el seguimiento de cambios desde la pantalla Seguimiento de cambios entre ciclos
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @trackingChangesBetweenCycles01 @export @TEST_DEV-7341
  Scenario: Exportar informe de seguimiento de cambios entre ciclos
    Given el usuario está en la pantalla "Seguimiento de cambios entre ciclos" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"