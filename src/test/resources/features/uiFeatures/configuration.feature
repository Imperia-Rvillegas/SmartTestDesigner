@all @ui @configuration
Feature: Pantalla Configuraciones

  Como usuario del sistema
  Quiero poder ejecutar el cálculo total de previsiones en configuraciones
  Para asegurarme de que se procesen correctamente todos los datos

  @positivo @calculation @config01 @TEST_DEV-5477
  Scenario: Ejecutar calculo completo de previsiones y verificar semaforo verde
    Given el usuario está en la pantalla "Configuraciones" desde menú "Rápido"
    When hace clic en el botón "Calcular todo"
    And acepta la confirmación del cálculo
    Then el color del semáforo debería ser verde en la tabla "Histórico de cálculos de previsión"
    And se captura evidencia "Semaforo verde"