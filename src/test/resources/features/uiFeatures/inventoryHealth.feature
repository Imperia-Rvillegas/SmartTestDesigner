@all @ui @inventoryHealth
Feature: Pantalla Salud de inventario

  Como usuario del sistema
  Quiero consultar la salud del inventario
  Para identificar productos con riesgo de obsolescencia, rotura o sobrestock y tomar decisiones oportunas de gestión

  @inventoryHealth01 @calculation @TEST_DEV-7263
  Scenario: Actualizar el informe de salud de inventario con semaforo verde
    Given el usuario está en la pantalla "Salud de inventario" desde menú "Buscar"
    When hace clic en el botón "Actualizar"
    And acepta la confirmación
    Then el color del semáforo en la tabla "Histórico de cálculo de salud de inventario" cumple la secuencia esperada
    And se captura evidencia "Semaforo verde"

  @inventoryHealth02 @export @TEST_DEV-7465
  Scenario: Exportar informe de Salud de inventario
    Given se comprueba que el plugin "Módulo de gestión de inventarios" esta activo
    And el usuario accede a la pantalla "Salud de inventario" desde menu "Indice"
    When hace clic en el botón "Excel" para guardar el archivo
    Then el excel aparece en la carpeta de descargas
    And se captura evidencia "Excel exportado"