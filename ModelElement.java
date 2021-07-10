import java.util.Map;

public class ModelElement {
    String elementId;
    String elementType;
    Map<String, String> properties;

    public ModelElement(
            String elementId,
            String elementType,
            Map<String, String> properties) {
        this.elementId = elementId;
        this.elementType = elementType;
        this.properties = properties;
    }

    public ModelElement(
            String elementId,
            String elementType) {
        this.elementId = elementId;
        this.elementType = elementType;
    }
}
