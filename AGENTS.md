# Project Development Guidelines

- Mantén el mismo estilo del proyecto y revisa los ejemplos existentes antes de introducir cambios.
- Utiliza las utilidades propias del proyecto (consultar carpeta `utils`) para crear esperas, interactuar con tablas, realizar operaciones matemáticas, validaciones, registro de logs y cualquier otra funcionalidad auxiliar disponible.
- Considera escenarios donde una pantalla, tabla, modal u otro proceso tarde en cargar y agrega esperas explícitas adecuadas para manejar esos tiempos.
- Define los nombres de métodos y variables usando **camelCase**.
- Agrega documentación clara y breve a todas las clases y métodos nuevos que crees, incluyendo una descripción de su propósito y detalles relevantes de su uso.
- Asegúrate de que toda la documentación en archivos `.md` sea clara, ordenada y con una estructura legible, evitando el uso de iconos como emojis.
- Cada vez que agregues documentación nueva a la carpeta `docs/manual`, incluye su referencia correspondiente en `README.md`.
- Cuando realices cualquier actualización que modifique la documentación del manual, actualiza también el contenido relevante en `docs/manual` para mantenerla al día.
- Cada vez que desarrolles un nuevo feature, asegúrate de agregar su suite correspondiente al workflow `regression.yml`y de documentarla en la documentación oficial asociada,al igual que el runner correspondiente a la suite para poder ejecutarla.
- Cada vez que agregues un paso a algún escenario, asegúrate de implementar toda la lógica necesaria para que ese paso se ejecute correctamente.
- Al aplicar cualquier actualización que afecte la ejecución de los escenarios automatizados, ejecuta un escenario de prueba que utilice los cambios para validar que funcionan correctamente.
- Si alguna instrucción o requisito no está claro, consulta con la persona solicitante antes de suponer.
- En todas las respuestas proporcionadas explica explícitamente cuál es la causa del problema y cómo se soluciona.
- Cuando crees una nueva feature de UI debes, como parte del mismo cambio: generar su Page Object, steps y runner dedicados; asegurarte de que la suite correspondiente figure explícitamente en el workflow `regression.yml`; y actualizar la documentación del workflow en `docs/manual/regression-workflow.md` (especialmente la sección **Suites disponibles**).
