@all @ui @unit
Feature: Pantalla Unidades

  Como usuario del sistema
  Quiero poder crear, editar y eliminar unidades
  Para gestionar adecuadamente las unidades asociadas a los artículos

  @create @unit01 @TEST_DEV-5653
  Scenario: Crear una nueva unidad con datos validos
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva unidad
    And ingresa la descripción de la nueva unidad
    And ingresa el número de decimales de la nueva unidad
    And hace clic en el botón "Aceptar"
#    And verifica que se cierre la ventana
    Then la unidad creada aparece en la lista de unidades
    And se captura evidencia "Nueva unidad creada"

  @create @unit02 @TEST_DEV-5654
  Scenario: Intentar crear una unidad sin completar los campos requeridos
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    And el usuario ingresa en el formulario de creación de nueva unidad
    When deja vacio el campo nombre y hace clic en Aceptar
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Nombre"
    And se captura evidencia "El sistema muestra el mensaje"

  @edit @unit03 @TEST_DEV-5655
  Scenario: Editar los datos de una unidad
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    And encuentra la unidad creada en la lista de unidades
    When hace clic en el botón "Editar"
    And modifica la descripcion de la unidad creada
    Then la descripción actualizada se refleja en la unidad creada
    And se captura evidencia "Unidad editada"

  @edit @unit04 @TEST_DEV-5656
  Scenario: Intentar editar una unidad sin completar campos requeridos
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    And encuentra la unidad creada en la lista de unidades
    When hace clic en el botón "Editar"
    And borra el nombre de la unidad creada
    And hace clic en el body para cerrar el campo editable y guardar los cambios
    Then la unidad creada aparece en la lista de unidades
    And  se captura evidencia "La unidad continua apareciendo"

  @eliminate @unit05 @TEST_DEV-5657
  Scenario: Eliminar una unidad no asociada a articulos
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    And selecciona una unidad no asociada a ningún artículo
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then la unidad creada no aparece en la lista de unidades
    And se captura evidencia "Unidad eliminada"

  @create @unit07 @TEST_DEV-5659
  Scenario: Crear unidad con el minimo permitido de decimales
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva unidad con minimo de decimales
    And ingresa la descripción de la nueva unidad
    And ingresa el minimo de decimales permitidos en una unidad
    And hace clic en el botón "Aceptar"
    Then la nueva unidad con minimo de decimales aparece en la lista de unidades
    And se captura evidencia "Nueva unidad con minimo de decimales"

  @create @unit08 @TEST_DEV-6175
  Scenario: Crear unidad con el maximo permitido de decimales
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva unidad con maximo de decimales
    And ingresa la descripción de la nueva unidad
    And ingresa el maximo de decimales permitidos en una unidad
    And hace clic en el botón "Aceptar"
    Then la nueva unidad con maximo de decimales aparece en la lista de unidades
    And se captura evidencia "Nueva unidad con maximo de decimales"

  @create @unit09 @TEST_DEV-5661
  Scenario: Eliminar unidad con el minimo de decimales permitidos
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    And selecciona la unidad con minimo de decimales permitidos
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then la nueva unidad con minimo de decimales no aparece en la lista de unidades
    And se captura evidencia "Unidad con minimo de decimales no aparece"

  @create @unit10 @TEST_DEV-6176
  Scenario: Eliminar unidad con el maximo de decimales permitidos
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    And selecciona la unidad con maximo de decimales permitidos
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then la nueva unidad con maximo de decimales no aparece en la lista de unidades
    And se captura evidencia "Unidad con maximo de decimales no aparece"

  @create @unit11 @TEST_DEV-5907
  Scenario: Intentar crear unidad con 7 decimales
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva unidad
    And ingresa la descripción de la nueva unidad
    And ingresa numero de decimales no permitidos en una unidad
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje "El valor del campo no puede ser mayor de 6, valor actual 7." del campo "Número de decimales"
    And se captura evidencia "El sistema muestra el mensaje"