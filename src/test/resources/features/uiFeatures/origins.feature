@all @ui @origins
Feature: Pantalla Orígenes

  Como analista de datos
  Quiero cargar un origen desde un archivo Excel y procesarlo en un ámbito
  Para disponer de las relaciones de materiales actualizadas en el sistema

  @origins01 @generic @regression @TEST_DEV-7954
  Scenario: Cargar un origen Excel y procesarlo en el ámbito de Productos
    Given el usuario está en la pantalla "Orígenes" desde menú "Buscar"
    When crea un nuevo origen de importación tipo "Excel" con nombre "Productos_Regre"
    And carga el archivo "Productos_Regre.xlsx" para el origen
    And el usuario accede a la pantalla "Orígenes" desde menu "Indice"
    Then el origen de importación queda disponible en la lista
    When el usuario accede a la pantalla "Ámbitos" desde menu "Indice"
    And abre el ámbito de carga "Productos"
    And selecciona el origen "Productos_Regre"
    And mapea las columnas del ámbito con los valores:
      | Imperia     | Fichero |
      | Código      | CODIGO  |
      | Descripción | DESC    |
      | Unidad base | UNIT    |
    And inicia la carga de datos del ámbito
    Then la carga finaliza en estado exitoso para el ámbito
    And se captura evidencia "Carga exitosa"
    And el usuario accede a la pantalla "Maestro de artículos" desde menu "Buscar"
    And verifica que el articulo "QA_COMPRA_1" existe en la lista de maestro de artículos
    And se captura evidencia "Articulo en la lista"

  @origins02 @generic @regression @TEST_DEV-7917
  Scenario: Cargar un origen Excel y procesarlo en el ámbito de Listado de materiales
    Given el usuario está en la pantalla "Orígenes" desde menú "Buscar"
    When crea un nuevo origen de importación tipo "Excel" con nombre "Materiales Regre"
    And carga el archivo "Materiales_Regre.xlsx" para el origen
    And el usuario accede a la pantalla "Orígenes" desde menu "Indice"
    Then el origen de importación queda disponible en la lista
    When el usuario accede a la pantalla "Ámbitos" desde menu "Indice"
    And abre el ámbito de carga "Listado de materiales"
    And selecciona el origen "Materiales Regre"
    And mapea las columnas del ámbito con los valores:
      | Imperia             | Fichero             |
      | Código del producto | Código del producto |
      | Código del material | Código del material |
      | Cantidad            | Cantidad            |
    And inicia la carga de datos del ámbito
    Then la carga finaliza en estado exitoso para el ámbito
    And se captura evidencia "Carga exitosa"
    And el usuario accede a la pantalla "Listado de materiales" desde menu "Buscar"
    And verifica que el material "QA_DOBLE_1" existe en la lista de materiales
    And se captura evidencia "Material en la lista"

  @origins03 @generic @regression @TEST_DEV-7961
  Scenario: Cargar un origen Excel y procesarlo en el ámbito de Proveedores
    Given el usuario está en la pantalla "Orígenes" desde menú "Buscar"
    When crea un nuevo origen de importación tipo "Excel" con nombre "Proveedores_Regre"
    And carga el archivo "Proveedores_Regre.xlsx" para el origen
    And el usuario accede a la pantalla "Orígenes" desde menu "Indice"
    Then el origen de importación queda disponible en la lista
    When el usuario accede a la pantalla "Ámbitos" desde menu "Indice"
    And abre el ámbito de carga "Proveedores"
    And selecciona el origen "Proveedores_Regre"
    And mapea las columnas del ámbito con los valores:
      | Imperia     | Fichero |
      | Código      | CODIGO  |
      | Descripción | DESC    |
    And inicia la carga de datos del ámbito
    Then la carga finaliza en estado exitoso para el ámbito
    And se captura evidencia "Carga exitosa"
    And el usuario accede a la pantalla "Proveedores" desde menu "Buscar"
    And verifica que el registro "QA_PROV_1" existe en la lista
    And se captura evidencia "Proveedor en la lista"

  @origins04 @generic @regression @TEST_DEV-8014
  Scenario: Cargar un origen Excel y procesarlo en el ámbito de Proveedor-material
    Given el usuario está en la pantalla "Orígenes" desde menú "Buscar"
    When crea un nuevo origen de importación tipo "Excel" con nombre "Mat_Sup_Regre"
    And carga el archivo "Mat_Sup_Regre.xlsx" para el origen
    And el usuario accede a la pantalla "Orígenes" desde menu "Indice"
    Then el origen de importación queda disponible en la lista
    When el usuario accede a la pantalla "Ámbitos" desde menu "Indice"
    And abre el ámbito de carga "Configuraciones de proveedores"
    And selecciona el origen "Mat_Sup_Regre"
    And mapea las columnas del ámbito con los valores:
      | Imperia            | Fichero     |
      | Código de producto | CODIGO_PROD |
      | Código proveedor   | CODIGO_PROV |
      | Código almacén     | ALM         |
    And inicia la carga de datos del ámbito
    Then la carga finaliza en estado exitoso para el ámbito
    And se captura evidencia "Carga exitosa"
    And el usuario accede a la pantalla "Listado de proveedores-material" desde menu "Buscar"
    And verifica que el registro "QA_MAT_1" existe en la lista
    And se captura evidencia "Proveedor-material en la lista"

  @origins05 @generic @regression @TEST_DEV-8016
  Scenario: Cargar un origen Excel y procesarlo en el ámbito de Inventario
    Given el usuario está en la pantalla "Orígenes" desde menú "Buscar"
    When crea un nuevo origen de importación tipo "Excel" con nombre "Inventario_Regre"
    And carga el archivo "Inventario_Regre.xlsx" para el origen
    And el usuario accede a la pantalla "Orígenes" desde menu "Indice"
    Then el origen de importación queda disponible en la lista
    When el usuario accede a la pantalla "Ámbitos" desde menu "Indice"
    And abre el ámbito de carga "Stocks"
    And selecciona el origen "Inventario_Regre"
    And mapea las columnas del ámbito con los valores:
      | Imperia         | Fichero  |
      | Código producto | CODIGO   |
      | Cantidad        | CANTIDAD |
      | Código almacén  | ALM      |
    And inicia la carga de datos del ámbito
    Then la carga finaliza en estado exitoso para el ámbito
    And se captura evidencia "Carga exitosa"
    And el usuario accede a la pantalla "Maestro de artículos" desde menu "Buscar"
    And verifica que el registro "QA_COMPRA_1" existe en la lista
    And se captura evidencia "Articulo en la lista"

  @origins06 @generic @regression @TEST_DEV-8021
  Scenario: Cargar un origen Excel y procesarlo en el ámbito de Líneas de Producción
    Given el usuario está en la pantalla "Orígenes" desde menú "Buscar"
    When crea un nuevo origen de importación tipo "Excel" con nombre "Lineas_Prod_Regre"
    And carga el archivo "Lineas_Prod_Regre.xlsx" para el origen
    And el usuario accede a la pantalla "Orígenes" desde menu "Indice"
    Then el origen de importación queda disponible en la lista
    When el usuario accede a la pantalla "Ámbitos" desde menu "Indice"
    And abre el ámbito de carga "Líneas de producción"
    And selecciona el origen "Lineas_Prod_Regre"
    And mapea las columnas del ámbito con los valores:
      | Imperia     | Fichero |
      | Código      | CODIGO  |
      | Descripción | DESC    |
    And inicia la carga de datos del ámbito
    Then la carga finaliza en estado exitoso para el ámbito
    And se captura evidencia "Carga exitosa"
    And el usuario accede a la pantalla "Líneas de producción" desde menu "Buscar"
    And verifica que el registro "LINEA_QA_1" existe en la lista
    And se captura evidencia "Linea de produccion en la lista"