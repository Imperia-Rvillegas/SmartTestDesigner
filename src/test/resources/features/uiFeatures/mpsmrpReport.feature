@all @ui @mpsMrpReport
Feature: Pantalla Informe MPS/MRP

  Como usuario del sistema
  Quiero administrar el informe MPS/MRP desde la pantalla Informe MPS/MRP
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @mpsMrpReport01 @export @TEST_DEV-7467
  Scenario: Exportar informe MPS/MRP
    Given se comprueba que el plugin "Informe MPS/MRP" esta activo
    And el usuario accede a la pantalla "Informe MPS/MRP" desde menu "Indice"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"