

public class ModelElement {

    String elementId;
    String elementType;
    //Map<String, List<String>> properties;

    /*
    public ModelElement(
            String elementId,
            String elementType,
            Map<String, List<String>> properties) {
        this.elementId = elementId;
        this.elementType = elementType;
        this.properties = properties;
    }*/

    public ModelElement(
            String elementId,
            String elementType) {
        this.elementId = elementId;
        this.elementType = elementType;
    }
}
