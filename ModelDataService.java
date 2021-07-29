import java.util.*;
import java.util.stream.Collectors;

public class ModelDataService {
    private ModelDefService modelDefService = new ModelDefService();
    private ArrayList<ModelElement> modelElements = new ArrayList<>();
    public ArrayList<ElementProperty> properties = new ArrayList<>();

    //--------Elements-----------
    //Add element with properties
    /*
    public void addElement(
            String elementId,
            String elementType,
            Map<String, List<String>> properties) {

        this.modelElements.add(
                new ModelElement(elementId,elementType,properties));
    }*/

    //Add element but we don't know the properties yet
    public void addElement(
            String elementId,
            String elementType) {

        /*
        HashMap<String, List<String>> properties = new HashMap<>();
        List<String> elementTypeProperties = modelDefService.getPropertiesForElementType(elementType);
        //Add an empty property list for each property
        for (String propertyType : elementTypeProperties) {
            properties.putIfAbsent(propertyType,List.of(""));
        }*/

        this.modelElements.add(
                new ModelElement(elementId,elementType));
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

    public List<ModelElement> getElements(String elementType) {
        return modelElements
                .stream()
                .filter(me -> me.elementType.equals(elementType))
                .collect(Collectors.toList());
    }

    //--------Properties-----------
    public void addProperty(
            String propertyType,
            List<String> elementIds,
            String value) {

        this.properties.add(
                new ElementProperty(propertyType,elementIds,value));
    }

    public List<ElementProperty> getProperties(String propertyType) {

        return properties
                .stream()
                .filter(p -> p.propertyType.equals(propertyType))
                .collect(Collectors.toList());
    }


}
