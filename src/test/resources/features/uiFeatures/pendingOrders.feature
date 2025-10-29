@all @ui @pendingOrders
Feature: Pantalla Pedidos pendientes

  Como usuario del sistema
  Quiero administrar los pedidos pendientes desde la pantalla Pedidos pendientes
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @pendingOrders01 @export @TEST_DEV-7372
  Scenario: Exportar informe de pedidos pendientes
    Given se comprueba que el plugin "Gestor de pedidos pendientes" esta activo
    And el usuario accede a la pantalla "Pedidos pendientes" desde menu "Indice"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"