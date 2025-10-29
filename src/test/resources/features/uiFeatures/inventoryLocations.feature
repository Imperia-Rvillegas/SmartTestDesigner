@all @ui @inventoryLocations
Feature: Pantalla Ubicaciones

  Como usuario del sistema
  Quiero crear, buscar, editar y eliminar Ubicaciones de inventario
  Para administrar correctamente los lugares físicos de almacenaje

  @inventoryLocations01 @TEST_DEV-6083
  Scenario: Visualizacion inicial de la pantalla Ubicaciones
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    Then la pantalla muestra los botones:
      | Nuevo            |
      | Editar           |
      | Eliminar         |
      | Ajustar columnas |
    And la tabla de Ubicaciones muestra las columnas siguientes:
      | Código      |
      | Descripción |
    And se captura evidencia "Pantalla Ubicaciones visible"

  @inventoryLocations02 @TEST_DEV-6084
  Scenario: Crear una nueva ubicacion exitosamente
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And Ingresa el codigo para la nueva ubicacion
    And Ingresa la descripcion para la nueva ubicacion
    And hace clic en el botón "Aceptar"
    Then la nueva ubicacion aparece en la tabla
    And se captura evidencia "Ubicación creada"

  @inventoryLocations03 @TEST_DEV-6085
  Scenario: Filtrar ubicacion por codigo
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When aplica filtro por codigo de ubicacion
    Then el codigo de la ubicacion aparece en los resultados
    And quita filtro "Código" para evitar errores en siguientes escenarios
    And se captura evidencia "Filtrar ubicacion por codigo"

  @inventoryLocations04 @TEST_DEV-6177
  Scenario: Filtrar ubicacion por descripcion
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    And aplica filtro por descripcion de ubicacion
    Then la descripcion de la ubicacion aparece en los resultados
    And quita filtro "Descripción" para evitar errores en siguientes escenarios
    And se captura evidencia "Filtrar ubicacion por descripcion"

  @inventoryLocations05 @TEST_DEV-6086
  Scenario: Validacion de campo obligatorio Codigo en la creacion de ubicacion
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And Ingresa la descripcion para la nueva ubicacion
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje El campo es requerido
    And se captura evidencia "Validacion de campo obligatorio Codigo"

  @inventoryLocations06 @TEST_DEV-6178
  Scenario: Validacion de campo obligatorio Descripcion en la creacion de ubicacion
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And Ingresa el codigo para la nueva ubicacion
    And hace clic en el botón "Aceptar"
    Then el sistema muestra el mensaje El campo es requerido
    And se captura evidencia "Validacion de campo obligatorio Descripcion"

  @inventoryLocations07 @TEST_DEV-6087
    #Precondicion: Primero ejecutar el escenario que crea la ubicacion
  Scenario: Intentar crear una ubicacion con codigo duplicado
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And Ingresa el codigo para la nueva ubicacion
    And Ingresa la descripcion para la nueva ubicacion
    And hace clic en el botón "Aceptar"
    Then se muestra un Popup con mensaje "Localización duplicada"
    And se captura evidencia "Localización duplicada"

  @inventoryLocations08 @TEST_DEV-6088
  Scenario: Buscar una ubicacion existente utilizando el buscador
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When busca la descripcion de la ubicacion creada en el buscador
    Then la descripcion de la ubicacion aparece en los resultados
    And se captura evidencia "Ubicacion encontrada"

  @inventoryLocations09 @TEST_DEV-6089
  Scenario: Editar la descripcion de una ubicacion existente
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    And busca la descripcion de la ubicacion creada en el buscador
    When hace clic en el botón "Editar"
    And modifica la descripcion de la ubicacion
    Then la ubicacion editada aparece en la tabla
    And se captura evidencia "Ubicación editada"

  @inventoryLocations10 @TEST_DEV-6090
    #Este escenario requiere un Código de ubicación relacionado a un inventario (Se usa uno de TEST CITEL)
  Scenario: Intentar editar el codigo de una ubicacion asociada a inventario
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    And busca una ubicacion asociada a un inventario
    When hace clic en el botón "Editar"
    And intenta modificar el codigo de la ubicacion
    Then el sistema muestra el mensaje "No ha sido posible editar el código de ubicación seleccionado. Existen inventarios con este código de ubicación. Elimine estos inventarios para poder editar el código seleccionado."
    And se captura evidencia "No ha sido posible editar el código de ubicación seleccionado"

  @ñ @TEST_DEV-6091
  Scenario: Eliminar una ubicacion sin dependencias
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    And selecciona una ubicacion que no esta asociada a ningun inventario
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then la ubicacion ya no aparece en la tabla
    And se captura evidencia "Ubicación eliminada"

  @inventoryLocations12 @TEST_DEV-6092
    #SD creada por que actualmente no se muestra el mensaje esperado SD-3786
  Scenario: Intentar eliminar una ubicacion asociada a inventario
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    And selecciona una ubicacion asociada a un inventario
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then el sistema muestra el mensaje "La ubicación está siendo utilizada y no puede eliminarse."
    And se captura evidencia "La ubicación está siendo utilizada y no puede eliminarse"

  @inventoryLocations13 @TEST_DEV-6093
  Scenario: Ajustar automaticamente las columnas de la tabla de Ubicaciones
    Given el usuario está en la pantalla "Ubicaciones" desde menú "Buscar"
    When hace clic en el botón "Ajustar columnas"
    Then el sistema ajusta automáticamente las columnas
    And se captura evidencia "El sistema ajusta automáticamente las columnas"