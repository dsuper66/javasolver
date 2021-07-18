import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModelElementDataService {
    private ModelElementDefService modelElementDefService = new ModelElementDefService();
    private ArrayList<ModelElement> modelElements = new ArrayList<>();

    //Add element with properties
    public void addElement(
            String elementId,
            String elementType,
            Map<String, String> properties) {

        this.modelElements.add(
                new ModelElement(elementId,elementType,properties));
    }

    //Add element but we don't know the properties yet
    public void addElement(
            String elementId,
            String elementType) {

        Map<String, String> properties = Map.of();
        List<String> elementTypeProperties = modelElementDefService.getPropertiesForElementType(elementType);
        for (String propertyType : elementTypeProperties) {
            properties =  MyUtilities.AddMapToMap(properties, Map.of(propertyType,"empty"));
        }

        this.modelElements.add(
                new ModelElement(elementId,elementType,properties));
    }

    //Set the property of the element (if it exists)
    //https://www.baeldung.com/java-optional
    public void assignPropertyValue(String elementId, String propertyType, String value) {
        getElement(elementId).ifPresent(modelElement ->
            System.out.println("elementId:" + elementId + " propertyType:" + propertyType + " propertyValue:" + value)
        );
    }

    public Optional<ModelElement> getElement(String elementId) {
        Optional<ModelElement> opt =
                modelElements
                        .stream()
                        .filter(e -> e.elementId.equals(elementId))
                        .findFirst();
        //opt.ifPresent(modelElement ->
        //    System.out.println(elementId + " properties:" + modelElement.properties));
        return opt;
    }

}
