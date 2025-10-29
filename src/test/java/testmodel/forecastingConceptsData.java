package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature de gestión de Ubicaciones de inventario.
 * Contiene información para escenarios de creación, edición, validaciones y búsqueda.
 */
public class forecastingConceptsData {

    @JsonProperty("newConceptName")
    private String newConceptName;

    // Getters y setters

    public String getNewConceptName() {
        return newConceptName;
    }

    public void setNewConceptName(String newConceptName) {
        this.newConceptName = newConceptName;
    }
}