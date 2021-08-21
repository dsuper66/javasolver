import java.util.List;

public class ElementProperty {
    final String propertyTypeId;
    final List<String> elementIds;
    final String stringValue;
    final Double doubleValue;

    public ElementProperty(
            String propertyTypeId,
            List<String> elementIds,
            String stringValue,
            Double doubleValue) {
        this.propertyTypeId = propertyTypeId;
        this.elementIds = elementIds;
        this.stringValue = stringValue;
        this.doubleValue = doubleValue;
    }
}

