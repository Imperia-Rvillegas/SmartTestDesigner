# Workflow de mantenimiento en GitHub Actions

El workflow `Actions Maintenance` definido en `.github/workflows/actions-maintenance.yml` está diseñado para liberar espacio de
almacenamiento en GitHub Actions. Permite eliminar artefactos antiguos del repositorio y, de forma opcional, limpiar los cachés
de Actions (incluido el caché de dependencias de Maven) cuando sea necesario.

## Cómo ejecutarlo

1. Accede a **Actions → Actions Maintenance** en el repositorio.
2. Selecciona **Run workflow** y configura los siguientes parámetros:
   - `runner`: elige `self-hosted` (valor por defecto) para ejecutar en tus propios runners sin consumir minutos de GitHub Actions o `ubuntu-latest` si prefieres usar la infraestructura hospedada por GitHub.
   - `daysToKeepArtifacts`: número de días a conservar. Se eliminarán los artefactos con una fecha de creación anterior.
   - `removeCaches`: marca esta casilla si quieres borrar todos los cachés almacenados en GitHub Actions para el repositorio.
3. Confirma con **Run workflow** para iniciar la depuración.

## Pasos principales del workflow

1. **Checkout**: descarga el contenido del repositorio para acceder a los archivos de configuración (por ejemplo, `pom.xml`).
2. **Eliminar artefactos antiguos**: recorre todos los artefactos del repositorio y elimina los que superen el umbral de días
   indicados. Los artefactos expirados automáticamente se omiten.
3. **Vaciar cachés de GitHub Actions** *(opcional)*: cuando `removeCaches` es verdadero, elimina todos los cachés asociados al
   repositorio (incluido el de Maven).

## Consideraciones

- El parámetro `daysToKeepArtifacts` debe ser mayor o igual que cero. Úsalo para conservar los artefactos recientes que aún se
necesitan para auditorías o descargas manuales.
- El parámetro `runner` es opcional; si lo omites, el workflow utilizará un runner `self-hosted`.
- Si activas `removeCaches`, también se eliminará el caché de Maven. En la siguiente ejecución de pruebas, Maven tardará un poco
más porque tendrá que descargar nuevamente todas las dependencias antes de reutilizar el caché.
- Ejecuta este workflow de manera periódica para evitar que el repositorio alcance los límites de almacenamiento impuestos por
GitHub Actions.
