package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature de gestión de Ubicaciones de inventario.
 * Contiene información para escenarios de creación, edición, validaciones y búsqueda.
 */
public class articlesData {

    @JsonProperty("newArticleCode")
    private String newArticleCode;

    @JsonProperty("newArticleDescription")
    private String newArticleDescription;

    // Getters y setters

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