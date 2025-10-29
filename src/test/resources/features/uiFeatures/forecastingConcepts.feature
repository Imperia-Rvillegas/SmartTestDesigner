@all @ui @forecastingConcepts
Feature: Pantalla Conceptos de prevision

  Como usuario del sistema
  Quiero administrar los conceptos desde la pantalla conceptos de prevision
  Para poder crear, buscar, filtrar, consultar y eliminar conceptos de prevision

  @forecastingConcepts01 @TEST_DEV-6755
  Scenario: Crear un nuevo concepto de prevision
    Given se comprueba que el plugin "Conceptos de previsión" esta activo
    And el usuario accede a la pantalla "Conceptos de previsión" desde menú
    When crea un nuevo concepto
    Then valida que el concepto fue creado
    And se captura evidencia "Concepto de prevision creado correctamente"

  @forecastingConcepts02 @TEST_DEV-6756
  Scenario: Eliminar un concepto de prevision
    Given se comprueba que el plugin "Conceptos de previsión" esta activo
    And el usuario accede a la pantalla "Conceptos de previsión" desde menú
    When selecciona un concepto de previsiones nuevo
    And hace clic en el botón "Eliminar"
    And hace clic en el botón "Aceptar"
    Then valida que el concepto fue eliminado
    And se captura evidencia "El concepto de prevision fue elimnado"