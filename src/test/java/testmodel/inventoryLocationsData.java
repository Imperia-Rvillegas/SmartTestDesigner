package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature de gestión de Ubicaciones de inventario.
 * Contiene información para escenarios de creación, edición, validaciones y búsqueda.
 */
public class inventoryLocationsData {

    @JsonProperty("newLocationCode")
    private String newLocationCode;

    @JsonProperty("newLocationDescription")
    private String newLocationDescription;

    @JsonProperty("editedDescription")
    private String editedDescription;

    @JsonProperty("restrictedLocation")
    private String restrictedLocation;

    @JsonProperty("codeEdited")
    private String codeEdited;

    // Getters y setters

    public String getNewLocationCode() {
        return newLocationCode;
    }

    public void setNewLocationCode(String newLocationCode) {
        this.newLocationCode = newLocationCode;
    }

    public String getNewLocationDescription() {
        return newLocationDescription;
    }

    public void setNewLocationDescription(String newLocationDescription) {
        this.newLocationDescription = newLocationDescription;
    }

    public String getEditedDescription() {
        return editedDescription;
    }

    public void setEditedDescription(String editedDescription) {
        this.editedDescription = editedDescription;
    }

    public String getRestrictedLocation() {
        return restrictedLocation;
    }

    public void setRestrictedLocation(String restrictedLocation) {
        this.restrictedLocation = restrictedLocation;
    }

    public String getCodeEdited() {
        return codeEdited;
    }

    public void setCodeEdited(String codeEdited) {
        this.codeEdited = codeEdited;
    }
}