import java.util.List;

public class Property {
    String propertyType;
    List<String> elementIds;
    String value;

    public Property(
            String propertyType,
            List<String> elementIds,
            String value) {
        this.propertyType = propertyType;
        this.elementIds = elementIds;
        this.value = value;
    }
}

