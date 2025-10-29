# Datos de Prueba con POJO y YAML

El framework utiliza objetos Java simples (POJO) para mapear los datos definidos en archivos YAML ubicados en `src/test/resources/testdata/page/`.

## Ubicación de archivos

```text
src/test/java/testmodel/            # Clases POJO que representan estructuras de datos
src/test/resources/testdata/page/   # Archivos YAML con datos de prueba
```

## Principios de uso

- Cada POJO refleja la estructura de un archivo YAML asociado.
- Los atributos se anotan con `@JsonProperty` para mapear los nombres de los campos.
- Los datos se cargan en tiempo de ejecución mediante `TestDataLoader.load`.
- Los valores se comparten entre Steps y PageObjects sin duplicar información.

## Ejemplo de POJO

```java
package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArticlesData {

    @JsonProperty("newArticleCode")
    private String newArticleCode;

    @JsonProperty("newArticleDescription")
    private String newArticleDescription;

    public String getNewArticleCode() {
        return newArticleCode;
    }

    public void setNewArticleCode(String newArticleCode) {
        this.newArticleCode = newArticleCode;
    }

    public String getNewArticleDescription() {
        return newArticleDescription;
    }

    public void setNewArticleDescription(String newArticleDescription) {
        this.newArticleDescription = newArticleDescription;
    }
}
```

## Ejemplo de YAML asociado

```yaml
newArticleCode: "ART-001"
newArticleDescription: "Artículo de prueba QA"
```

## Uso en Steps

```java
public class ArticlesSteps {

    private final ArticlesPage articlesPage;
    private final String newArticleCode;
    private final String newArticleDescription;

    public ArticlesSteps() {
        ArticlesData articlesData = TestDataLoader.load(
            "testdata/page/articles.yaml", ArticlesData.class);

        this.newArticleCode = articlesData.getNewArticleCode();
        this.newArticleDescription = articlesData.getNewArticleDescription();
        this.articlesPage = Hooks.getPageManager().getArticlesPage();
    }

    @When("ingresa codigo y descripcion para el nuevo articulo")
    public void enterCodeAndDescriptionForTheNewArticle() {
        articlesPage.fillMandatoryFields(newArticleCode, newArticleDescription);
    }
}
```

## Flujo de trabajo

1. Definir valores dinámicos en el YAML (por ejemplo, `articles.yaml`).
2. Crear o actualizar el POJO correspondiente (por ejemplo, `ArticlesData`).
3. Cargar el POJO en el Step mediante `TestDataLoader.load`.
4. Utilizar los valores en los métodos de los PageObjects.

Este patrón separa responsabilidades, facilita la reutilización de datos y mantiene la trazabilidad entre escenarios y datos de prueba.
