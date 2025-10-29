@all @ui @potentialLaunches
Feature: Pantalla Lanzamientos potenciales

  Como usuario del sistema
  Quiero administrar los lanzamientos potenciales desde la pantalla Lanzamientos potenciales
  Para poder exportar, buscar, y consultar registros correctamente

  @potentialLaunches01 @export @TEST_DEV-7383
  Scenario: Exportar informe de lanzamientos potenciales
    Given el usuario está en la pantalla "Lanzamientos potenciales" desde menú "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"