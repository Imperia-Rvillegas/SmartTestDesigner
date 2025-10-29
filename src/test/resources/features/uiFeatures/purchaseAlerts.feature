@all @ui @purchaseAlerts
Feature: Pantalla Listado de Alertas de compras

  Como usuario del sistema
  Quiero administrar el listado de alertas de compras desde la pantalla Alertas de compras
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @purchaseAlerts01 @export @TEST_DEV-7478
  Scenario: Exportar listado de alertas de compras
    Given se comprueba que el plugin "Planificación de Requerimientos de Material (MRP)" esta activo
    And el usuario accede a la pantalla "Alertas de compras" desde menu "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"
