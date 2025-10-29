@all @ui @productionAlerts
Feature: Pantalla Alertas de producción

  Como usuario del sistema
  Quiero administrar las alertas de producción desde la pantalla Alertas de producción
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @productionAlerts01 @export @TEST_DEV-7486
  Scenario: Exportar lista de alertas de producción
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And el usuario accede a la pantalla "Alertas de producción" desde menu "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"
