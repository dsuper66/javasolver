import java.util.List;

public class ElementProperty {
    String propertyTypeId;
    List<String> elementIds;
    String stringValue;
    Double doubleValue;

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

