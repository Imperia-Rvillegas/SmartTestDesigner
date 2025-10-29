@all @ui @trackingChangesInTheSameCycle
Feature: Pantalla Seguimiento de cambios mismo ciclo

  Como usuario del sistema
  Quiero administrar el seguimiento de cambios desde la pantalla Seguimiento de cambios mismo ciclo
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @trackingChangesInTheSameCycle01 @export @TEST_DEV-7352
  Scenario: Exportar informe de seguimiento de cambios mismo ciclo
    Given el usuario está en la pantalla "Seguimiento de cambios mismo ciclo" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"