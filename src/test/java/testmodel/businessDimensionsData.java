package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature businessDimensions.
 */
public class businessDimensionsData {

    @JsonProperty("nameOfNewBusinessDimension")
    private String nameOfNewBusinessDimension;

    @JsonProperty("defaultValueOfNewBusinessDimension")
    private String defaultValueOfNewBusinessDimension;

    @JsonProperty("editedDefaultValue")
    private String editedDefaultValue;

    @JsonProperty("businessDimensionNameEdited")
    private String businessDimensionNameEdited;

    // Getters y setters

    public String getNameOfNewBusinessDimension() {
        return nameOfNewBusinessDimension;
    }

    public void setNameOfNewBusinessDimension(String nameOfNewBusinessDimension) {
        this.nameOfNewBusinessDimension = nameOfNewBusinessDimension;
    }

    public String getDefaultValueOfNewBusinessDimension() {
        return defaultValueOfNewBusinessDimension;
    }

    public void setDefaultValueOfNewBusinessDimension(String defaultValueOfNewBusinessDimension) {
        this.defaultValueOfNewBusinessDimension = defaultValueOfNewBusinessDimension;
    }

    public String getEditedDefaultValue() {
        return editedDefaultValue;
    }

    public void setEditedDefaultValue(String editedDefaultValue) {
        this.editedDefaultValue = editedDefaultValue;
    }

    public String getBusinessDimensionNameEdited() {
        return businessDimensionNameEdited;
    }

    public void setBusinessDimensionNameEdited(String businessDimensionNameEdited) {
        this.businessDimensionNameEdited = businessDimensionNameEdited;
    }
}