import java.util.*;

public class ModelDataService {
    private ModelDefService modelDefService = new ModelDefService();
    private ArrayList<ModelElement> modelElements = new ArrayList<>();
    private ArrayList<Property> properties = new ArrayList<>();

    //--------Elements-----------
    //Add element with properties
    public void addElement(
            String elementId,
            String elementType,
            Map<String, List<String>> properties) {

        this.modelElements.add(
                new ModelElement(elementId,elementType,properties));
    }

    //Add element but we don't know the properties yet
    public void addElement(
            String elementId,
            String elementType) {

        HashMap<String, List<String>> properties = new HashMap<>();
        List<String> elementTypeProperties = modelDefService.getPropertiesForElementType(elementType);
        //Add an empty property list for each property
        for (String propertyType : elementTypeProperties) {
            properties.putIfAbsent(propertyType,List.of(""));
        }

        this.modelElements.add(
                new ModelElement(elementId,elementType,properties));
    }

    //Set the property of the element (if it exists)
    //https://www.baeldung.com/java-optional
    public void assignElementProperty(String elementId, String propertyType, String value) {
        getElement(elementId).ifPresent(modelElement ->
                {
                    if (modelElement.elementType.equals("bus")) {
                        //System.out.println("elementId:" + elementId + " propertyType:" + propertyType + " propertyValue:" + value);
                    }
                }
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

    //--------Properties-----------



}
