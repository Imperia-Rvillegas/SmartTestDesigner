@all @ui @supplierMaterialList
Feature: Pantalla Listado de proveedores-material

  Como usuario del sistema
  Quiero administrar el listado de proveedores-material desde la pantalla Listado de proveedores-material
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @supplierMaterialList01 @export @TEST_DEV-7472
  Scenario: Exportar listado de proveedores-material
    Given se comprueba que el plugin "Planificación de Requerimientos de Material (MRP)" esta activo
    And el usuario accede a la pantalla "Listado de proveedores-material" desde menu "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"