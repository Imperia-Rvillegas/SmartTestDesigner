@all @ui
Feature: Pantalla Previsiones

  Como usuario del sistema
  Quiero modificar un valor de previsión del período siguiente al actual
  Para validar que se mantenga después de ejecutar el cálculo total

  @forecasts @positivo @calculation @forecasts01 @TEST_DEV-5771
  Scenario Outline: Modificar celda de prevision por "<agrupacion>", calcular todo y validar persistencia desde menu "<menuTipo>"
    Given el usuario está en la pantalla "Previsiones" desde menú "<menuTipo>"
    And selecciona la agrupación "<agrupacion>"
    And espera a que la tabla cargue los datos completamente
    And modifica la celda del periodo siguiente según la agrupación "<agrupacion>"
    When el usuario selecciona la pantalla "Configuraciones"
    And hace clic en el botón "Calcular todo"
    And acepta la confirmación
    Then el color del semáforo debería ser verde en la tabla "Histórico de cálculos de previsión"
    And el usuario selecciona la pantalla "Previsiones"
    And espera a que la tabla cargue los datos completamente
    And la celda del periodo siguiente muestra el valor nuevo para la agrupación "<agrupacion>"
    And se captura evidencia "Persistencia Validada"

    Examples:
      | agrupacion | menuTipo |
      | Meses      | Rápido   |
      | Semanas    | Indice   |
      | Días       | Buscar   |

  @forecasts02 @TEST_DEV-6458
  Scenario Outline: Editar celda <condicion> aplicando <operacion>, y validar persistencia tras refrescar
    Given el usuario está en la pantalla "Previsiones" desde menú "Rápido"
    And selecciona la agrupación "Meses"
    And configura el ultimo nivel de agrupacion
    And espera a que la tabla cargue los datos completamente
    And ubica una celda "<condicion>" y aplica la operacion "<operacion>"
    When el usuario refresca la página
    And el sistema muestra la pantalla "Previsiones"
    Then la celda modificada mantiene el nuevo valor después del refresco según el tipo de operación "<operacion>"
    And se captura evidencia "Persistencia tras refresco"

    Examples:
      | condicion                          | operacion      |
      | modificada con pedido pendiente    | suma           |
      | modificada con pedido pendiente    | resta          |
      | modificada con pedido pendiente    | multiplicacion |
      | modificada con pedido pendiente    | division       |
      | modificada con pedido pendiente    | reemplazo      |
      | modificada sin pedido pendiente    | suma           |
      | modificada sin pedido pendiente    | resta          |
      | modificada sin pedido pendiente    | multiplicacion |
      | modificada sin pedido pendiente    | division       |
      | modificada sin pedido pendiente    | reemplazo      |
      | sin modificar con pedido pendiente | suma           |
      | sin modificar con pedido pendiente | resta          |
      | sin modificar con pedido pendiente | multiplicacion |
      | sin modificar con pedido pendiente | division       |
      | sin modificar con pedido pendiente | reemplazo      |
      | sin modificar sin pedido pendiente | suma           |
      | sin modificar sin pedido pendiente | resta          |
      | sin modificar sin pedido pendiente | multiplicacion |
      | sin modificar sin pedido pendiente | division       |
      | sin modificar sin pedido pendiente | reemplazo      |

  @forecasts03 @TEST_DEV-6757
  Scenario Outline: Verificar que la cantidad editada de un producto se distribuye correctamente a niveles inferiores con celdas "<condicion>" usando la operación "<operacion>"
    Given el usuario está en la pantalla "Previsiones" desde menú "Rápido"
    And selecciona la agrupación "Meses"
    And configura el ultimo nivel de agrupacion
    And espera a que la tabla cargue los datos completamente
    And ubica un producto con mas de 2 entradas y celdas "<condicion>"
    And cambia la agrupacion a solo codigo producto
    When modifica el producto aplicando "<operacion>"
    And agrega las dimensiones restantes
    And valida que la suma de todas las celdas es igual la nueva cantidad del producto
    And se captura evidencia "La suma de todas las celdas es igual la nueva cantidad del producto"

    Examples:
      | condicion                          | operacion      |
      | modificada con pedido pendiente    | suma           |
      | modificada con pedido pendiente    | resta          |
      | modificada con pedido pendiente    | multiplicacion |
      | modificada con pedido pendiente    | division       |
      | modificada con pedido pendiente    | reemplazo      |
      | modificada sin pedido pendiente    | suma           |
      | modificada sin pedido pendiente    | resta          |
      | modificada sin pedido pendiente    | multiplicacion |
      | modificada sin pedido pendiente    | division       |
      | modificada sin pedido pendiente    | reemplazo      |
      | sin modificar con pedido pendiente | suma           |
      | sin modificar con pedido pendiente | resta          |
      | sin modificar con pedido pendiente | multiplicacion |
      | sin modificar con pedido pendiente | division       |
      | sin modificar con pedido pendiente | reemplazo      |
      | sin modificar sin pedido pendiente | suma           |
      | sin modificar sin pedido pendiente | resta          |
      | sin modificar sin pedido pendiente | multiplicacion |
      | sin modificar sin pedido pendiente | division       |
      | sin modificar sin pedido pendiente | reemplazo      |

  @forecasts @forecasts04 @TEST_DEV-6758
  Scenario Outline: Validar que una celda "<condicion>" queda en cero tras restar un valor mayor y persiste luego de refrescar
    Given el usuario está en la pantalla "Previsiones" desde menú "Rápido"
    And selecciona la agrupación "Meses"
    And configura el ultimo nivel de agrupacion
    And espera a que la tabla cargue los datos completamente
    When ubica una celda "<condicion>" y aplica la operacion "resta por valor mayor"
    Then se muestra el mensaje "Esta acción va a generar previsiones negativas, por lo que no se puede ejecutar. Por favor haga una operación válida"
    And acepta la confirmación
    And el usuario refresca la página
    And el sistema muestra la pantalla "Previsiones"
    And valida que el valor de la celda persiste
    And se captura evidencia "Persistencia tras refresco"

    Examples:
      | condicion                          |
      | sin modificar sin pedido pendiente |
      | modificada sin pedido pendiente    |

  @forecasts05 @TEST_DEV-6759
  Scenario Outline: Validar que se aplica correctamente un valor a un concepto nuevo en celda "<condicion>" aplicando "<operacion>"
    Given se comprueba que el plugin "Conceptos de previsión" esta activo
    And el usuario accede a la pantalla "Conceptos de previsión" desde menú
    When crea un nuevo concepto
    And abre la pantalla de previsiones
    And configura el ultimo nivel de agrupacion
    And ubica una celda "<condicion>" y aplica la operacion "<operacion>" para el nuevo concepto
    And el usuario refresca la página
    And el sistema muestra la pantalla "Previsiones"
    Then la celda modificada mantiene el nuevo valor después del refresco según el tipo de operación "<operacion>"
    And valida que la cantidad se aplica correctamente al concepto
    And Eliminar el concepto
    And se captura evidencia "El concepto se elimina correctamente"

    Examples:
      | condicion                          | operacion      |
      | modificada con pedido pendiente    | suma           |
      | modificada con pedido pendiente    | resta          |
      | modificada con pedido pendiente    | multiplicacion |
      | modificada con pedido pendiente    | division       |
      | modificada con pedido pendiente    | reemplazo      |
      | modificada sin pedido pendiente    | suma           |
      | modificada sin pedido pendiente    | resta          |
      | modificada sin pedido pendiente    | multiplicacion |
      | modificada sin pedido pendiente    | division       |
      | modificada sin pedido pendiente    | reemplazo      |
      | sin modificar con pedido pendiente | suma           |
      | sin modificar con pedido pendiente | resta          |
      | sin modificar con pedido pendiente | multiplicacion |
      | sin modificar con pedido pendiente | division       |
      | sin modificar con pedido pendiente | reemplazo      |
      | sin modificar sin pedido pendiente | suma           |
      | sin modificar sin pedido pendiente | resta          |
      | sin modificar sin pedido pendiente | multiplicacion |
      | sin modificar sin pedido pendiente | division       |
      | sin modificar sin pedido pendiente | reemplazo      |

  @forecasts @forecasts06 @TEST_DEV-7388
  Scenario: Exportar informe de prevision estadistica
    Given el usuario está en la pantalla "Previsiones" desde menú "Buscar"
    When hace clic en el botón "Excel"
    And hace clic en el botón "Previsión estadística" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"

  @forecasts @forecasts07 @export @TEST_DEV-7389
  Scenario: Exportar informe de prevision actual
    Given el usuario está en la pantalla "Previsiones" desde menú "Buscar"
    When hace clic en el botón "Excel"
    And hace clic en el botón "Previsión actual" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"