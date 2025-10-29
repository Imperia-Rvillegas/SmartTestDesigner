@all @ui @associatedUnitArticleFlow
Feature: Flujo para validar restricción de eliminación de unidad asociada a un artículo

  Como usuario del sistema
  Quiero validar que no se puede eliminar una unidad asociada a un artículo
  Y posteriormente eliminar el artículo y la unidad correctamente
  Para garantizar la integridad de los datos y el ciclo completo de gestión

  @associatedUnitArticleFlow01 @TEST_DEV-6284
#    Crear la unidad
  Scenario: Crear unidad, asociarla a un articulo, validar que no puede ser eliminada, eliminar el articulo y eliminar la unidad
    Given el usuario está en la pantalla "Unidades" desde menú "Buscar"
    When hace clic en el botón "Nuevo"
    And ingresa el nombre de la nueva unidad que va a ser asociada al articulo
    And ingresa la descripción de la nueva unidad que va a ser asociada al articulo
    And ingresa el número de decimales de la nueva unidad
    And hace clic en el botón "Aceptar"
    And verifica que no se muestre ningun popup de error
    Then la unidad creada para ser asociada al articulo aparece en la lista de unidades
    And se captura evidencia "Unidad creada para ser asociada a un articulo"

#   Crear producto con la unidad creada
    Given navega hasta la pantalla "Maestro de artículos"
    When hace clic en el botón "Nuevo"
    And ingresa codigo y descripcion para el nuevo articulo
    And selecciona la unidad creada como unidad base
    And hace clic en el botón "Guardar"
    And verifica que no se muestre ningun popup de error
    Then el sistema indica que fue guardado
    And se captura evidencia "Articulo creado con unidad"

#    Intentar eliminar la unidad
    Given navega hasta la pantalla "Unidades"
    And selecciona la unidad asociada al articulo
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then el sistema muestra un mensaje indicando que una de las unidades esta asociada a un articulo
    And hace clic en el botón "Aceptar"
    And se captura evidencia "Unidad no eliminada por asociación a artículo"

#   Eliminar el articulo
    Given navega hasta la pantalla "Maestro de artículos"
    And busca el articulo creado
    When selecciona el articulo creado
    And hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then el articulo creado ya no aparece en los resultados
    And se captura evidencia "Articulo eliminado en flujo"

#   Eliminar la unidad
    Given navega hasta la pantalla "Unidades"
    And selecciona la unidad asociada al articulo
    When hace clic en el botón "Eliminar"
    And acepta la confirmación
    Then la unidad creada para ser asociada al articulo no aparece en la lista de unidades
    And se captura evidencia "Unidad eliminada tras eliminar artículo"
