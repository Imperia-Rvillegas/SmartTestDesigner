@all @api @userProfile
Feature: Pruebas de API de usuario

  @getProfile
  Scenario: Obtener perfil de usuario
    Given que obtengo el token de autenticación
    When hago la petición para obtener el perfil del usuario
    Then la respuesta debería tener código 200