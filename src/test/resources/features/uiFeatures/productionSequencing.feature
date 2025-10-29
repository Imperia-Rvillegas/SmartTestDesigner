@all @ui @productionSequencing
Feature: Pantalla Secuenciación de la producción

  Como usuario del sistema
  Quiero administrar la secuenciación de la producción desde la pantalla Secuenciación de la producción
  Para poder exportar, buscar, filtrar y consultar registros correctamente

  @productionSequencing01 @export @TEST_DEV-7490
  Scenario: Exportar secuenciacion de la produccion
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And tambien se comprueba que el plugin "Secuenciador de Producción" esta activo
    And el usuario accede a la pantalla "Secuenciación de la producción" desde menu "Buscar"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"
