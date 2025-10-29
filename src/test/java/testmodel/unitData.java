package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature de gestión de Ubicaciones de inventario.
 * Contiene información para escenarios de creación, edición, validaciones y búsqueda.
 */
public class unitData {

    @JsonProperty("newUnitName")
    private String newUnitName;

    @JsonProperty("descriptionOfTheNewUnit")
    private String descriptionOfTheNewUnit;

    @JsonProperty("numberOfDecimalPlacesInTheNewUnit")
    private String numberOfDecimalPlacesInTheNewUnit;

    @JsonProperty("editedDescription")
    private String editedDescription;

    @JsonProperty("unitAssociatedWithArticle")
    private String unitAssociatedWithArticle;

    @JsonProperty("unitNameWithMinimumDecimalPlaces")
    private String unitNameWithMinimumDecimalPlaces;

    @JsonProperty("minimumNumberOfDecimalsForUnit")
    private String minimumNumberOfDecimalsForUnit;

    @JsonProperty("unitNameWithMaximumDecimalPlaces")
    private String unitNameWithMaximumDecimalPlaces;

    @JsonProperty("maximumNumberOfDecimalsForUnit")
    private String maximumNumberOfDecimalsForUnit;

    @JsonProperty("numberOfDecimalsNotAllowedInUnit")
    private String numberOfDecimalsNotAllowedInUnit;

    @JsonProperty("unitNameToAssociateWithTheArticle")
    private String unitNameToAssociateWithTheArticle;

    @JsonProperty("unitDescriptionToAssociateWithTheArticle")
    private String unitDescriptionToAssociateWithTheArticle;

    // Getters y setters

    public String getNewUnitName() {
        return newUnitName;
    }

    public void setNewUnitName(String newUnitName) {
        this.newUnitName = newUnitName;
    }

    public String getDescriptionOfTheNewUnit() {
        return descriptionOfTheNewUnit;
    }

    public void setDescriptionOfTheNewUnit(String descriptionOfTheNewUnit) {
        this.descriptionOfTheNewUnit = descriptionOfTheNewUnit;
    }

    public String getNumberOfDecimalPlacesInTheNewUnit() {
        return numberOfDecimalPlacesInTheNewUnit;
    }

    public void setNumberOfDecimalPlacesInTheNewUnit(String numberOfDecimalPlacesInTheNewUnit) {
        this.numberOfDecimalPlacesInTheNewUnit = numberOfDecimalPlacesInTheNewUnit;
    }

    public String getEditedDescription() {
        return editedDescription;
    }

    public void setEditedDescription(String editedDescription) {
        this.editedDescription = editedDescription;
    }

    public String getUnitAssociatedWithArticle() {
        return unitAssociatedWithArticle;
    }

    public void setUnitAssociatedWithArticle(String unitAssociatedWithArticle) {
        this.unitAssociatedWithArticle = unitAssociatedWithArticle;
    }

    public String getUnitNameWithMinimumDecimalPlaces() {
        return unitNameWithMinimumDecimalPlaces;
    }

    public void setUnitNameWithMinimumDecimalPlaces(String unitNameWithMinimumDecimalPlaces) {
        this.unitNameWithMinimumDecimalPlaces = unitNameWithMinimumDecimalPlaces;
    }

    public String getMinimumNumberOfDecimalsForUnit() {
        return minimumNumberOfDecimalsForUnit;
    }

    public void setMinimumNumberOfDecimalsForUnit(String minimumNumberOfDecimalsForUnit) {
        this.minimumNumberOfDecimalsForUnit = minimumNumberOfDecimalsForUnit;
    }

    public String getUnitNameWithMaximumDecimalPlaces() {
        return unitNameWithMaximumDecimalPlaces;
    }

    public void setUnitNameWithMaximumDecimalPlaces(String unitNameWithMaximumDecimalPlaces) {
        this.unitNameWithMaximumDecimalPlaces = unitNameWithMaximumDecimalPlaces;
    }

    public String getMaximumNumberOfDecimalsForUnit() {
        return maximumNumberOfDecimalsForUnit;
    }

    public void setMaximumNumberOfDecimalsForUnit(String maximumNumberOfDecimalsForUnit) {
        this.maximumNumberOfDecimalsForUnit = maximumNumberOfDecimalsForUnit;
    }

    public String getNumberOfDecimalsNotAllowedInUnit() {
        return numberOfDecimalsNotAllowedInUnit;
    }

    public void setNumberOfDecimalsNotAllowedInUnit(String numberOfDecimalsNotAllowedInUnit) {
        this.numberOfDecimalsNotAllowedInUnit = numberOfDecimalsNotAllowedInUnit;
    }

    public String getUnitNameToAssociateWithTheArticle() {
        return unitNameToAssociateWithTheArticle;
    }

    public void setUnitNameToAssociateWithTheArticle(String unitNameToAssociateWithTheArticle) {
        this.unitNameToAssociateWithTheArticle = unitNameToAssociateWithTheArticle;
    }

    public String getUnitDescriptionToAssociateWithTheArticle() {
        return unitDescriptionToAssociateWithTheArticle;
    }

    public void setUnitDescriptionToAssociateWithTheArticle(String unitDescriptionToAssociateWithTheArticle) {
        this.unitDescriptionToAssociateWithTheArticle = unitDescriptionToAssociateWithTheArticle;
    }
}