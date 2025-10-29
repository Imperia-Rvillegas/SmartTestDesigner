@all @ui @projectionOfStockNeeds
Feature: Pantalla Proyección de stock de necesidades

  Como usuario del sistema
  Quiero consultar la proyección de stock de necesidades
  Para visualizar de manera anticipada la evolución del inventario y garantizar la cobertura de la demanda futura

  @projectionOfStockNeeds01 @calculation @TEST_DEV-7223
  Scenario: Calcular proyección de stock con semaforo verde
    Given el usuario está en la pantalla "Proyección de stock de necesidades" desde menú "Buscar"
    When el usuario solicita calcular la proyección de stock
    Then el semáforo de la proyección de stock se muestra en verde
    And se captura evidencia "Semaforo verde"

  @projectionOfStockNeeds02 @calculation @TEST_DEV-7224
  Scenario: Calcular proyección actual con semaforo verde
    Given el usuario está en la pantalla "Proyección de stock de necesidades" desde menú "Buscar"
    When el usuario solicita calcular la proyección de stock actual
    Then el semáforo de la proyección de stock se muestra en verde
    And se captura evidencia "Semaforo verde"

  @projectionOfStockNeeds03 @validation @TEST_DEV-
  Scenario: Validar el detalle de órdenes de fabricación ERP
    Given el usuario está en la pantalla "Proyección de stock de necesidades" desde menú "Buscar"
    When el usuario hace clic en "Panel de revisión" en Proyección de stock de necesidades
    And busca un producto con registros de "Órdenes de fabricación ERP" y guarda su cantidad en Proyección de stock de necesidades
    And selecciona el registro de Proyección de stock de necesidades
    Then la suma de la columna "Cantidad" en el panel de revisión coincide con la cantidad guardada en Proyección de stock de necesidades
    And las fechas de la columna "Fecha máxima de fabricación" coinciden con la fecha del registro seleccionado en Proyección de stock de necesidades
    And se captura evidencia "Detalle panel de revisión"
