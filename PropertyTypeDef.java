import java.util.List;

public class PropertyTypeDef {
    final String propertyTypeId;
    final Boolean isDouble;
    final List<String> elementTypes;

    public PropertyTypeDef(
            ModelDefService.PropertyType propertyType,
            Boolean isDouble,
            List<ModelDefService.ElementType> elementTypes) {
        this.propertyTypeId = propertyType.name();
        this.isDouble = isDouble;
        this.elementTypes = elementTypes.stream().map(Enum::name).toList();
    }
}
