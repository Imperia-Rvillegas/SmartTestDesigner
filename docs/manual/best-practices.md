# Buenas Prácticas de Implementación

Aplicar estas recomendaciones ayuda a mantener un framework consistente y fácil de mantener.

## Principios generales

- Reutilizar métodos existentes en los PageObjects antes de crear nuevas acciones.
- Evitar `Thread.sleep`; utilizar `WaitUtil` y demás utilidades del proyecto.
- Organizar los escenarios mediante etiquetas (`@ui`, `@api`, `@unitXX`).
- Nombrar métodos y pasos de forma consistente y descriptiva.
- Agrupar los Steps en clases con sufijo `Steps`.
- Acceder a los PageObjects a través de `PageManager` (`pageManager.getUnitPage()`).
- Capturar evidencias mediante `pageManager.getScreenshotUtil().capture("nombre_paso")` cuando se requiera.

## Estrategia para crear nuevos métodos

![Estrategia de creación de métodos](../../images/Estrategia%20de%20creacion%20de%20Metodos.png)

### CommonSteps

Crear o ampliar `CommonSteps` cuando la lógica pueda reutilizarse en múltiples pantallas o funciones generales (navegación, validaciones genéricas, autenticación, etc.).

### StepDefinition específico

Mantener los Steps relacionados con una única pantalla o flujo dentro de la clase de StepDefinition correspondiente.

### PageObject

Definir métodos en el PageObject cuando la acción involucre elementos concretos de la interfaz (clics, llenado de formularios, verificaciones visuales).

### BasePage

Centralizar en `BasePage` solo las acciones comunes a todas las páginas, como desplazamientos, cierres de modales o validaciones generales.

### Clases utilitarias

Crear métodos en utilidades específicas cuando la lógica se refiera a operaciones técnicas transversales (manejo de tablas dinámicas, ventanas emergentes, capturas de pantalla, transformaciones de datos, etc.).

## Criterios por tipo de clase

### CommonSteps

- Aplica a múltiples pantallas o funcionalidades.
- Representa acciones o validaciones de uso general.
- No depende de lógica específica de un módulo.

### StepDefinition

- Solo se usa en una pantalla o flujo.
- Está asociado a una funcionalidad puntual.
- No resulta útil reutilizarlo en otros módulos.

### PageObject

- Interactúa con elementos visibles o interactivos del DOM.
- Requiere localizadores `By` y acciones de WebDriver.
- Pertenece al contexto de una vista específica.

### BasePage

- Puede ejecutarse en cualquier página del sistema.
- Se hereda por todos los PageObjects.
- Evita duplicar lógica común.

### Utilidades

- Encapsulan patrones técnicos reutilizables.
- Agrupan comportamientos compartidos (por ejemplo, `TableUtil`, `WaitUtil`, `PopupUtil`).
