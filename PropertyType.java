import java.util.List;

public class PropertyType {
    String propertyTypeId;
    List<String> elementTypes;
    String valueType;

    public PropertyType(
            String propertyTypeId,
            List<String> elementTypes,
            String valueType) {
        this.propertyTypeId = propertyTypeId;
        this.elementTypes = elementTypes;
        this.valueType = valueType;
    }
}
