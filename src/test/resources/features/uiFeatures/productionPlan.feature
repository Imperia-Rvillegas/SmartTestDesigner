@all @ui @productionPlan
Feature: Pantalla Plan de producción

  Como usuario del sistema
  Quiero calcular el plan de producción
  Para obtener una planificación actualizada

  @productionPlan01 @calculation @generic @regression @TEST_DEV-5691
  Scenario: Calcular plan de produccion con semaforo verde
    Given el usuario está en la pantalla "Plan de producción" desde menú "Buscar"
    When hace clic en el botón "Calcular"
    And acepta la confirmación
    Then el color del semáforo debería ser verde en la tabla "Histórico de cálculos de producción"
    And se captura evidencia "Semaforo verde"

  @productionPlan02 @export @TEST_DEV-7484
  Scenario: Exportar Plan de produccion
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And el usuario accede a la pantalla "Plan de producción" desde menu "Buscar"
    When hace clic en el botón "Exportar a Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"

  @productionPlan03 @creation @TEST_DEV-8004 @generic @regression
  Scenario: Crear un nuevo registro en Plan de producción con producto no descontinuado
    Given se comprueba que el plugin "Plan maestro de producción (MPS)" esta activo
    And el usuario accede a la pantalla "Plan de producción" desde menu "Buscar"
    When hace clic en el botón "Nuevo"
    And selecciona un producto no descontinuado en el formulario de nuevo Plan de producción
    And selecciona el primer proceso disponible en el formulario de nuevo Plan de producción
    And selecciona la primera línea disponible en el formulario de nuevo Plan de producción
    And selecciona el primer almacén disponible en el formulario de nuevo Plan de producción si existe
    And completa los campos numéricos del formulario de nuevo Plan de producción
    And selecciona la fecha máxima de fabricación actual en el formulario de nuevo Plan de producción
    And hace clic en el botón "Aceptar"
    Then el producto seleccionado aparece en la tabla de Plan de producción
    And se captura evidencia "Plan de producción creado con producto descontinuado"