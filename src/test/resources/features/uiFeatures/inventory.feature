@all @ui @inventory
Feature: Pantalla Inventario

  Como usuario del sistema
  Quiero administrar el inventario desde la pantalla Inventario
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @inventory01 @export @TEST_DEV-7390
  Scenario: Exportar informe de inventario
    Given se comprueba que el plugin "Módulo de gestión de inventarios" esta activo
    And el usuario accede a la pantalla "Inventario" desde menu "Indice"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"