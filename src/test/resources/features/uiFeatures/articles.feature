@all @ui @articles
Feature: Pantalla Artículos

  Como usuario del sistema
  Quiero administrar los artículos desde la pantalla Artículos
  Para poder crear, buscar, filtrar, consultar y eliminar artículos correctamente

  @articles01 @creation @TEST_DEV-5909 @generic @regression
  Scenario: Crear un nuevo articulo exitosamente
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Rápido"
    When hace clic en el botón "Nuevo"
    And ingresa codigo y descripcion para el nuevo articulo
    And selecciona la primera unidad base disponible
    And hace clic en el botón "Guardar"
    And verifica que no se muestre ningun popup de error
    Then el sistema indica que fue guardado
    And se captura evidencia "Articulo creado exitosamente"

  @articles02 @search @TEST_DEV-5910
  Scenario: Buscar el articulos creado con el buscador (Lupa)
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Buscar"
    When busca el articulo creado
    Then el articulo creado aparece en los resultados
    And se captura evidencia "Articulo encontrado"

  @articles03 @filter_code @TEST_DEV-5911
  Scenario: Filtrar articulo por codigo
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Buscar"
    When aplica filtro por codigo de articulo
    Then el articulo filtrado por codigo aparece en los resultados
    And quita filtro "Código" para evitar errores en siguientes escenarios
    And se captura evidencia "Filtrar articulo por Codigo"

  @articles04 @filter_code @TEST_DEV-6192
  Scenario: Filtrar articulo por descripcion
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Buscar"
    When aplica filtro por descripcion de articulo
    Then el articulo filtrado por descripcion aparece en los resultados
    And quita filtro "Descripción" para evitar errores en siguientes escenarios
    And se captura evidencia "Filtrar articulo por Descripcion"

  @articles05 @units @TEST_DEV-5912
  Scenario: Acceder a la pantalla Unidades desde Articulos
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Rápido"
    When hace clic en el botón "Unidades"
    Then el sistema muestra la pantalla "Unidades"
    And se captura evidencia "Pantalla Unidades desde Articulos"

  @articles06 @listMaterials @TEST_DEV-5913
  Scenario: Acceder a la pantalla Listado de materiales desde Articulos
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Rápido"
    When hace clic en el botón "Listado de materiales"
    Then el sistema muestra la pantalla "Materiales"
    And se captura evidencia "Pantalla Listado de materiales desde Articulos"

  @articles07 @export @TEST_DEV-5914
  Scenario: Exportar articulos a Excel
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Buscar"
    When hace clic en el botón "Exportar a Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"

  @articles08 @adjustColumns @TEST_DEV-5915
  Scenario: Ajustar columnas de la tabla de articulos
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Buscar"
    When hace clic en el botón "Ajustar columnas"
    Then el sistema ajusta automáticamente las columnas
    And se captura evidencia "Columnas de la tabla de articulos ajustada"

  @articles09 @elimination @TEST_DEV-5916 @generic @regression
  Scenario: Eliminar el articulo creado
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Buscar"
    And busca el articulo creado
    When selecciona el articulo creado
    And hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then el articulo creado ya no aparece en los resultados
    And se captura evidencia "Articulo eliminado"

  @smoke @articles10 @creation @TEST_DEV-5917
  Scenario: Intentar crear un articulo sin ingresar la unidad obligatoria
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Rápido"
    When hace clic en el botón "Nuevo"
    And Ingresa el campo codigo del articulo
    And hace clic en el botón "Guardar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Unidad base"
    And se captura evidencia "Mensaje El campo es requerido."

  @articles11 @creation @TEST_DEV-5918
  Scenario: Intentar crear un articulo sin ingresar el codigo obligatorio
    Given el usuario está en la pantalla "Maestro de artículos" desde menú "Rápido"
    When hace clic en el botón "Nuevo"
    And selecciona la primera unidad base disponible
    And hace clic en el botón "Guardar"
    Then el sistema muestra el mensaje "El campo es requerido." del campo "Código"
    And se captura evidencia "Mensaje El campo es requerido."
