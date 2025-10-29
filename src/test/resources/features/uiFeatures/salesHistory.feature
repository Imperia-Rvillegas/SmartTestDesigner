@all @ui @salesHistory
Feature: Pantalla Histórico de ventas

  Como usuario del sistema
  Quiero administrar el histórico de ventas desde la pantalla Histórico de ventas
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @salesHistory01 @export @TEST_DEV-7368
  Scenario: Exportar informe de histórico de ventas
    Given el usuario está en la pantalla "Histórico de ventas" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"