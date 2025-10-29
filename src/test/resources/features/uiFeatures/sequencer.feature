@all @ui @sequencer
Feature: Pantalla Secuenciador

  Como usuario del sistema
  Quiero administrar la lista del Secuenciador desde la pantalla Secuenciador
  Para poder exportar, buscar, filtrar y consultar registros correctamente

  @sequencer01 @export @TEST_DEV-7544
  Scenario: Exportar lista de secuenciador
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And tambien se comprueba que el plugin "Secuenciador de Producción" esta activo
    And el usuario accede a la pantalla "Secuenciador" desde menu "Buscar"
    When hace clic en el botón "Exportar a Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"