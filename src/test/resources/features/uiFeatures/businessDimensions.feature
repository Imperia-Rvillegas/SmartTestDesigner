@all @ui @businessDimensions
Feature: Pantalla Gestor de Dimensiones de Negocio

  Como usuario del sistema
  Quiero administrar dimensiones de negocio
  Para definir configuraciones que afecten a otras áreas del sistema

  @businessDimensions01 @creation @TEST_DEV-5933
  Scenario: Crear una nueva dimension de negocio exitosamente
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva dimension de negocio
    And ingresa el valor por defecto de la nueva dimension de negocio
    And hace clic en el botón "Aceptar"
    Then la nueva dimension de negocio se muestra en los resultados
    And se captura evidencia "Nueva dimension de negocio"

  @businessDimensions02 @edition @TEST_DEV-5934
  Scenario: Editar el valor por defecto de una dimension de negocio existente
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Editar"
    And modifica el valor por defecto de la dimension de negocio
    Then el sistema muestra el valor por defecto de la dimension de negocio editado
    And se captura evidencia "Valor por defecto de dimension de negocio editado"

  @businessDimensions03 @edition @TEST_DEV-6189
  Scenario: Editar el nombre de una dimension de negocio existente
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Editar"
    And modifica el nombre de la dimension de negocio
    Then el sistema muestra el nombre de la dimension de negocio editado
    And se captura evidencia "Nombre de dimension de negocio editado"

  @businessDimensions04 @deletion @TEST_DEV-5935
  Scenario: Eliminar una dimension de negocio existente
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When elimina la dimension de negocio con nombre editado
    Then la dimension de negocio editada ya no aparece en los resultados
    And se captura evidencia "Dimension de negocio eliminada"

  @businessDimensions05 @navigation @TEST_DEV-5936
  Scenario: Acceder a pantalla Valores de dimensiones asociadas
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Valores de dimensiones asociadas"
    Then el sistema muestra la pantalla "Valores de dimensiones asociadas"

  @businessDimensions06 @creation @TEST_DEV-5937
  Scenario: Intentar crear dimension sin Nombre
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el valor por defecto de la nueva dimension de negocio
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Nombre"

  @businessDimensions07 @creation @TEST_DEV-5938
  Scenario: Intentar crear dimension sin Valor por defecto
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva dimension de negocio
    And limpia el campo Valor por defecto
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Valor por defecto"

  @businessDimensions08 @limit @TEST_DEV-5939
  Scenario: Validar limite maximo de 3 dimensiones de negocio
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When crea dimensiones hasta completar el limite de 3
    And intenta crear una nueva dimension adicional
    Then el sistema muestra el mensaje "Ha llegado al límite de dimensiones creadas. No se pueden añadir más de 3 dimensiones."

  @businessDimensions09 @TEST_DEV-5940
  Scenario: Validar funcionamiento del boton Ajustar columnas
    Given el usuario está en la pantalla "Gestor de dimensiones de negocio" desde menú "Buscar"
    When hace clic en el botón "Ajustar columnas"
    Then el sistema ajusta automáticamente las columnas