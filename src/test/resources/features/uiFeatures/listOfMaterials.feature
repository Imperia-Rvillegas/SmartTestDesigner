@all @ui @listOfMaterials
Feature: Pantalla Listado de Materiales

  Como usuario de la herramienta
  Quiero consultar, filtrar, editar y exportar la información de materiales
  Para gestionar correctamente la fabricación y aprovisionamiento de productos

  @listOfMaterials01 @TEST_DEV-6309
  Scenario: Visualizar listado completo de materiales
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    When se carga la tabla de materiales
    Then la tabla de Listado de Materiales muestra las columnas siguientes:
      | Código padre           |
      | Descripción padre      |
      | Tipo de material padre |
      | Cantidad padre         |
      | Unidad padre           |
      | Padre descontinuado    |
      | Código hijo            |
      | Descripción hijo       |
      | Tipo de material hijo  |
      | Cantidad hijo          |
      | Unidad hijo            |
      | Hijo descontinuado     |
    And se captura evidencia "listado completo de materiales"

  @listOfMaterials02 @TEST_DEV-6310
  Scenario: Acceder a la ficha del producto desde el codigo padre
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    When hago clic en el hipervinculo del codigo padre del primer registro de la tabla
    Then debo ser redirigido a la ficha del producto correspondiente
    And se captura evidencia "Ficha del producto desde el codigo padre"

  @listOfMaterials03 @TEST_DEV-6311
  Scenario: Acceder a la ficha del producto desde el codigo hijo
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    When hago clic en el hipervinculo del código hijo del primer registro de la tabla
    Then debo ser redirigido a la ficha del producto correspondiente
    And se captura evidencia "Ficha del producto desde el codigo hijo"

  @listOfMaterials04 @generic @regression @TEST_DEV-6317
  Scenario: Crear una nueva relacion entre productos (Padre e Hijo)
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And selecciona producto padre para nuevo producto
    And selecciona producto hijo para nuevo producto
    And confirma la creacion
    And verifica que no se muestre ningun popup de error
    Then debe registrarse la nueva relacion en la tabla
    And se captura evidencia "Nueva relacion"

  @listOfMaterials05 @generic @regression @TEST_DEV-6312
  Scenario: Editar un registro existente
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    When hace clic en el botón "Editar"
    And modifica el primer registro de la columna cantidad hijo
    Then el registro de la columna cantidad hijo debe actualizarse correctamente
    And se captura evidencia "El valor se actualiza correcamente"

  @listOfMaterials06 @generic @regression @TEST_DEV-7050
  Scenario: Eliminar un registro existente
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    And que selecciono el primer registro en la tabla de materiales
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    And espera a que la tabla cargue los datos completamente
    Then el registro debe eliminarse de la tabla materiales
    And se captura evidencia "El registro ya no aparece en la tabla"

  @listOfMaterials07 @export @generic @regression @TEST_DEV-6316
  Scenario: Exportar informacion a Excel
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    And hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado correctamente"

  @listOfMaterials08 @TEST_DEV-6314
  Scenario: Ajustar automaticamente el ancho de las columnas
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    When hace clic en el botón "Ajustar columnas"
    Then las columnas deben reajustarse automáticamente para mejorar la visualización
    And se captura evidencia "Las columnas se ajustan correctamente"

  @listOfMaterials09 @TEST_DEV-6315
  Scenario: Filtrar por descripcion hijo
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    And toma el primer valor de la columna descripcion hijo
    And aplica filtro por descripcion hijo en la tabla materiales
    Then el registro filtrado por descripcion hijo aparece en los resultados
    And se captura evidencia "La descripcion hijo aparece en los resultados"

  @listOfMaterials10 @TEST_DEV-7957
  Scenario: Acceder a páginas externas desde la cabecera de la pantalla "Listado de materiales"
    Given el usuario está en la pantalla "Listado de materiales" desde menú "Buscar"
    And verifica que el hipervinculo "Árbol de materiales" redirige a la url "materials/tree"
    Then se captura evidencia "Árbol de materiales"
    And el usuario va atrás en el navegador
    And verifica que el hipervinculo "Maestro de artículos" redirige a la url "products/list"
    Then se captura evidencia "Maestro de artículos"
    And el usuario va atrás en el navegador
    And verifica que el hipervinculo "Lista de atributos" redirige a la url "attributes"
    Then se captura evidencia "Lista de atributos"
    And el usuario va atrás en el navegador
    And verifica que el hipervinculo "Valores de atributos" redirige a la url "attributes/values"
    Then se captura evidencia "Valores de atributos"
    And el usuario va atrás en el navegador
    And verifica que el hipervinculo "Unidades" redirige a la url "units"
    Then se captura evidencia "Unidades"