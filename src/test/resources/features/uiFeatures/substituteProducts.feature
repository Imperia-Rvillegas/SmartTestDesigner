@all @ui @substituteProducts
Feature: Pantalla Productos sustituidos

  Como usuario del sistema
  Quiero administrar los productos sustituidos desde la pantalla Productos sustituidos
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @substituteProducts01 @export @TEST_DEV-7339
  Scenario: Exportar informe de productos sustituidos
    Given se comprueba que el plugin "Sustitución de productos" esta activo
    When el usuario accede a la pantalla "Productos sustituidos" desde menu "Indice"
    And hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"