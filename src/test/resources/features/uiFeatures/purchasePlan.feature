@all @ui @purchasePlan
Feature: Pantalla Plan de compras

  Como usuario del sistema
  Quiero calcular el plan de compras
  Para obtener una planificación actualizada

  @purchasePlan01 @calculation @TEST_DEV-5690
  Scenario: Calcular plan de compras con semaforo verde
    Given el usuario está en la pantalla "Plan de aprovisionamiento" desde menú "Buscar"
    When hace clic en el botón "Calcular"
    And acepta la confirmación
    Then el color del semáforo debería ser verde en la tabla "Histórico de cálculos de compras"
    And se captura evidencia "Semaforo verde"

  @purchasePlan02 @export @TEST_DEV-7474
  Scenario: Exportar Plan de compras
    Given se comprueba que el plugin "Planificación de Requerimientos de Material (MRP)" esta activo
    And el usuario accede a la pantalla "Plan de aprovisionamiento" desde menu "Buscar"
    When hace clic en el botón "Exportar a Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"

  @purchasePlan03 @creation @TEST_DEV-8283
  Scenario: Crear una nueva orden de compra en Plan de compras
    Given se comprueba que el plugin "Planificación de Requerimientos de Material (MRP)" esta activo
    And el usuario accede a la pantalla "Plan de aprovisionamiento" desde menu "Buscar"
    When hace clic en el botón "Nuevo"
    And selecciona un producto no descontinuado en el formulario de nuevo Plan de compras
    And selecciona el primer proveedor disponible en el formulario de nuevo Plan de compras
    And selecciona el primer almacén disponible en el formulario de nuevo Plan de compras si existe
    And completa los campos numéricos del formulario de nuevo Plan de compras
    And selecciona la fecha actual como fecha de pedido en el formulario de nuevo Plan de compras
    And selecciona la fecha actual como fecha de recepción en el formulario de nuevo Plan de compras
    And hace clic en el botón "Aceptar"
    Then el producto seleccionado aparece en la tabla de Plan de compras
    And se captura evidencia "Orden de compra creada"
