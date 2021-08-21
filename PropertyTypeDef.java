import java.util.List;

public class PropertyTypeDef {
    final String propertyTypeId;
    final List<String> elementTypes;
    final String valueType;

    public PropertyTypeDef(
            ModelDefService.PropertyType propertyType,
            List<String> elementTypes,
            String valueType) {
        this.propertyTypeId = propertyType.name();
        this.elementTypes = elementTypes;
        this.valueType = valueType;
    }
}
