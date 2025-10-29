package testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representa el conjunto de datos utilizados en el feature associatedDimensions.
 */
public class associatedDimensionsData {

    @JsonProperty("primaryDimensionName")
    private String primaryDimensionName;

    @JsonProperty("associatedDimensionName")
    private String associatedDimensionName;

    @JsonProperty("defaultValue")
    private String defaultValue;

    @JsonProperty("defaultValueEdited")
    private String defaultValueEdited;

    @JsonProperty("editedAssociatedDimension")
    private String editedAssociatedDimension;

    @JsonProperty("extraAssociatedDimension")
    private String extraAssociatedDimension;

    @JsonProperty("extraDefaultValue")
    private String extraDefaultValue;
    // Getters y setters

    public String getPrimaryDimensionName() {
        return primaryDimensionName;
    }

    public void setPrimaryDimensionName(String primaryDimensionName) {
        this.primaryDimensionName = primaryDimensionName;
    }

    public String getAssociatedDimensionName() {
        return associatedDimensionName;
    }

    public void setAssociatedDimensionName(String associatedDimensionName) {
        this.associatedDimensionName = associatedDimensionName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValueEdited() {
        return defaultValueEdited;
    }

    public void setDefaultValueEdited(String defaultValueEdited) {
        this.defaultValueEdited = defaultValueEdited;
    }

    public String getEditedAssociatedDimension() {
        return editedAssociatedDimension;
    }

    public void setEditedAssociatedDimension(String editedAssociatedDimension) {
        this.editedAssociatedDimension = editedAssociatedDimension;
    }

    public String getExtraAssociatedDimension() {
        return extraAssociatedDimension;
    }

    public void setExtraAssociatedDimension(String extraAssociatedDimension) {
        this.extraAssociatedDimension = extraAssociatedDimension;
    }

    public String getExtraDefaultValue() {
        return extraDefaultValue;
    }

    public void setExtraDefaultValue(String extraDefaultValue) {
        this.extraDefaultValue = extraDefaultValue;
    }
}