@all @ui @associatedDimensions
Feature: Pantalla Dimensiones asociadas

  Como usuario de Imperia Supply Chain Planning
  Quiero gestionar las dimensiones asociadas a cada dimensión principal
  Para administrar correctamente las relaciones y sus valores por defecto.

  @associatedDimensions01 @TEST_DEV-5981
  Scenario: Visualizacion inicial de la pantalla Dimensiones asociadas
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    Then la pantalla muestra los botones:
      | Nuevo            |
      | Editar           |
      | Eliminar         |
      | Ajustar columnas |
    And la tabla de Dimensiones asociadas muestra las columnas siguientes:
      | Dimensión          |
      | Dimensión asociada |
      | Valor por defecto  |
    And se captura evidencia "Visualizacion inicial de la pantalla Dimensiones asociadas correcta"

  @creacion @associatedDimensions02 @TEST_DEV-5982
  Scenario: Crear una dimension asociada exitosamente
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And se muestra el formulario de creación con campos "Dimensión", "Dimensión asociada" y "Valor por defecto"
    And selecciona dimensión principal
    And ingresa el nombre de la nueva dimension asociada
    And ingresa el valor por defecto de la nueva dimension asociada
    And hace clic en el botón "Aceptar"
    Then la nueva dimension asociada se muestra en los resultados
    And se captura evidencia "Dimension asociada creada"

  @creacion @associatedDimensions03 @TEST_DEV-5983
  Scenario: Validacion de campo obligatorio dimension en creacion de dimension asociada
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva dimension asociada
    And ingresa el valor por defecto de la nueva dimension asociada
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Dimensión"
    And se captura evidencia "Mensaje del campo obligatorio Dimension"

  @creacion @associatedDimensions04 @TEST_DEV-6190
  Scenario: Validacion de campo obligatorio dimension asociada en creacion de dimension asociada
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And selecciona dimensión principal
    And ingresa el valor por defecto de la nueva dimension asociada
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Dimensión asociada"
    And se captura evidencia "Mensaje del campo obligatorio Dimension asociada"

  @creacion @associatedDimensions05 @TEST_DEV-6191
  Scenario: Validacion de campo obligatorio valor por defecto en creacion de dimension asociada
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And selecciona dimensión principal
    And ingresa el nombre de la nueva dimension asociada
    And limpia el campo Valor por defecto de la nueva dimension asociada
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Valor por defecto"
    And se captura evidencia "Mensaje del campo obligatorio Valor por defecto"

  @edicion @associatedDimensions06 @TEST_DEV-5984
  Scenario: Editar una dimension asociada existente
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When existe la dimensión asociada creada
    And hace clic en el botón "Editar"
    And modifica el valor por defecto de la dimension asociada existente
    And modifica el nombre de la dimensión asociada existente
    Then la dimension asociada editada se muestra en los resultados
    And se captura evidencia "Dimensión asociada editada"

  @eliminacion @associatedDimensions07 @TEST_DEV-5985
  Scenario: Cancelar la eliminacion
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When existe la dimensión asociada editada
    And selecciona la dimension asociada editada
    And hace clic en el botón "Eliminar"
    And hace clic en el botón "Cancelar"
    Then la dimension asociada editada se muestra en los resultados
    And se captura evidencia "La dimension asociada no se elimina"

  @eliminacion @associatedDimensions08 @TEST_DEV-5986
  Scenario: Eliminar una dimension asociada
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    When existe la dimensión asociada editada
    And selecciona la dimension asociada editada
    And hace clic en el botón "Eliminar"
    And hace clic en el botón "Aceptar"
    Then la dimension asociada editada no se muestra en los resultados
    And se captura evidencia "Dimension asociada eliminada"

  @associatedDimensions09 @TEST_DEV-5987
    #El mensaje que se muestra no es el esperado, se crea una SD
  Scenario: Limite maximo de 10 dimensiones asociadas por dimension principal
    Given el usuario está en la pantalla "Gestor de dimensiones asociadas" desde menú "Buscar"
    And la dimensión principal ya tiene 10 dimensiones asociadas creadas
    When hace clic en el botón "Nuevo"
    And selecciona dimensión principal
    And ingresa el nombre de una dimension asociada extra
    And hace clic en el botón "Aceptar"
    Then se muestra el mensaje "Ha llegado al límite de dimensiones asociadas creadas. No se pueden añadir más de 10 dimensiones asociadas a la dimensión Cliente."
    And se captura evidencia "Se muestra el mensaje esperado"
