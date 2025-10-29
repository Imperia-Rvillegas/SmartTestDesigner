# Guía de Inicio Rápido

Esta guía explica cómo preparar el entorno de trabajo y ejecutar las suites principales.

## Requisitos previos

- Java 24 o superior.
- Apache Maven.
- Git.
- Un IDE con soporte para Java (IntelliJ IDEA o VS Code recomendado).

## Clonar el repositorio

```bash
git clone https://tu-usuario@bitbucket.org/imperia-scm/qa-automation-bot.git
cd qa-automation-bot
```

## Ejecutar pruebas

Consulta también la referencia de [propiedades de ejecución](./system-properties.md) para un resumen de los parámetros disponibles.

### Pruebas de interfaz de usuario

```bash
mvn clean test -Dtest=UiTest
```

### Pruebas de servicios REST

```bash
mvn clean test -Dtest=ApiTest
```

### Seleccionar ambiente

La propiedad `-Denv` define el ambiente objetivo y modifica dinámicamente las URLs utilizadas durante la ejecución.

- Valor omitido: usa el ambiente configurado por defecto en `EnvironmentConfig` (actualmente `pgarcia.dev`).
- Valor `pro`: utiliza el dominio `scp.imperiascm.com`.
- Cualquier otro valor (como `qa` o `dev`): compone la URL como `<valor>.scp.imperiascm.com`.

Ejemplo:

```bash
mvn clean test -Denv=qa
```

### Seleccionar navegador

```bash
mvn clean test -Dtest=UiTest -Dbrowser=edge
```

### Ejecutar con un usuario específico

```bash
mvn clean test -Dtest=UiTest -Duser=rvillegas
```

### Restaurar datos de un cliente

```bash
mvn clean test -Dtest=UiTest -Dkeyclient=10273
```

Si `keyclient` no se define, las pruebas se ejecutan sin restaurar datos.

### Modo sin interfaz gráfica

```bash
mvn clean test -Dtest=UiTest -Dheadless=true
```

Al activar `headless`, el navegador se ejecuta en segundo plano. Si no se define o se establece en `false`, se mostrará la ventana del navegador.
