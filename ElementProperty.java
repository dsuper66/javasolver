import java.util.List;

public class ElementProperty {
    String propertyType;
    List<String> elementIds;
    String value;

    public ElementProperty(
            String propertyType,
            List<String> elementIds,
            String value) {
        this.propertyType = propertyType;
        this.elementIds = elementIds;
        this.value = value;
    }
}

