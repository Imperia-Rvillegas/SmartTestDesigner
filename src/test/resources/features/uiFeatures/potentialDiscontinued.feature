@all @ui @potentialDiscontinued
Feature: Pantalla Productos descontinuados potenciales

  Como usuario del sistema
  Quiero administrar los productos descontinuados potenciales desde la pantalla Productos descontinuados potenciales
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @potentialDiscontinued01 @export @TEST_DEV-7382
  Scenario: Exportar informe de productos descontinuados potenciales
    Given el usuario está en la pantalla "Productos descontinuados potenciales" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"