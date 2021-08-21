import java.util.List;

public class DoubleProperty {
    final String propertyType;
    final List<String> elementIds;
    final Double value;

    public DoubleProperty(
            String propertyType,
            List<String> elementIds,
            String value) {
        this.propertyType = propertyType;
        this.elementIds = elementIds;
        this.value = Double.parseDouble(value);
    }
}

