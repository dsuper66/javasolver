import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelElementDataService {
    ModelElementDefService modelElementDefService = new ModelElementDefService();
    ArrayList<ModelElement> modelElements = new ArrayList<>();

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
        Set<String> elementTypeProperties = modelElementDefService.getPropertiesForElementType(elementType);
        for (String propertyType : elementTypeProperties) {
            properties =  MyUtilities.AddMapToMap(properties, Map.of(propertyType,"empty"));
        }

        this.modelElements.add(
                new ModelElement(elementId,elementType,properties));
    }

    //Set the property of the element (if it exists)
    public void setProperty(String propertyType, String elementId, String value) {
        ModelElement modelElement =
                (ModelElement) modelElements.stream().filter(
                        e -> e.elementId.equals(elementId));

    }
    public ModelElement getElement(String elementId) {
        ModelElement modelElement =
                (ModelElement) modelElements
                        .stream()
                        .filter(e -> e.elementId.equals(elementId))
                        .collect(Collectors.toList()).get(0);
        System.out.println(elementId + " properties:" + modelElement.properties);
        return modelElement;
    }

}
