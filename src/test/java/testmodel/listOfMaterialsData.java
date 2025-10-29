package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature de gestión de Ubicaciones de inventario.
 * Contiene información para escenarios de creación, edición, validaciones y búsqueda.
 */
public class listOfMaterialsData {

    @JsonProperty("newChildAmount")
    private String newChildAmount;

    // Getters y setters

    public String getNewChildAmount() {
        return newChildAmount;
    }

    public void setNewChildAmount(String newChildAmount) {
        this.newChildAmount = newChildAmount;
    }
}