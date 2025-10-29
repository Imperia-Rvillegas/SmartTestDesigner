@all @ui @processConfigList
Feature: Pantalla Lista de configuración de procesos

  Como usuario del sistema
  Quiero administrar la lista de configuración de procesos desde la pantalla Lista de configuración de procesos
  Para poder exportar, buscar, filtrar, consultar y eliminar registros correctamente

  @processConfigList01 @export @TEST_DEV-7482
  Scenario: Exportar lista de configuración de procesos
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And el usuario accede a la pantalla "Lista de configuración de procesos" desde menu "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"
