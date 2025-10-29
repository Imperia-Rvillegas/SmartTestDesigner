@all @ui @provisioning
Feature: Pantalla Aprovisionamiento

  Como usuario del sistema
  Quiero revisar el detalle del plan de aprovisionamiento
  Para validar que el cálculo del mes siguiente sea consistente

  @provisioning01 @TEST_DEV-8465 @generic @regression
  Scenario Outline: Validar el detalle del plan de aprovisionamiento del <tipoPeriodo> para el <tipoMes> (<tipoFecha> - <tipoValor>)
    Given el usuario está en la pantalla "Aprovisionamiento" desde menú "Buscar"
    And selecciona la vista de "<tipoPeriodo>" en Aprovisionamiento
    And selecciona la fecha "<tipoFecha>" en Aprovisionamiento
    And selecciona el valor "<tipoValor>" en Aprovisionamiento
    When busca un registro con cantidad distinta de cero en la columna del "<tipoMes>" en Aprovisionamiento
    And hace clic sobre el registro encontrado en la pantalla de Aprovisionamiento
    Then la suma de la columna "<columnaDetalle>" coincide con la cantidad seleccionada en Aprovisionamiento
    And las "<tipoFecha>" del detalle pertenecen al mes seleccionado en Aprovisionamiento
    And se captura evidencia "Detalle plan de aprovisionamiento"
    Examples:
      | tipoPeriodo | tipoFecha          | tipoValor | tipoMes       | columnaDetalle   |
      | Meses       | Fecha de recepción | Cantidad  | Mes siguiente | Cantidad a pedir |
      | Meses       | Fecha de recepción | Cantidad  | Mes actual    | Cantidad a pedir |
      | Meses       | Fecha de pedido    | Importe   | Mes siguiente | Importe          |
      | Meses       | Fecha de pedido    | Importe   | Mes actual    | Importe          |
      | Semanas     | Fecha de recepción | Cantidad  | Mes siguiente | Cantidad a pedir |
      | Semanas     | Fecha de recepción | Cantidad  | Mes actual    | Cantidad a pedir |
      | Semanas     | Fecha de pedido    | Importe   | Mes siguiente | Importe          |
      | Semanas     | Fecha de pedido    | Importe   | Mes actual    | Importe          |
      | Días        | Fecha de recepción | Cantidad  | Mes siguiente | Cantidad a pedir |
      | Días        | Fecha de recepción | Cantidad  | Mes actual    | Cantidad a pedir |
      | Días        | Fecha de pedido    | Importe   | Mes siguiente | Importe          |
      | Días        | Fecha de pedido    | Importe   | Mes actual    | Importe          |
