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
            String propertyTypeId,
            List<String> elementIds,
            String value) {
        modelDefService.propertyTypeDef(propertyTypeId).ifPresent(propertyTypeDef -> {
                    if (propertyTypeDef.valueType.equals("string")) {
                        this.properties.add(
                                new ElementProperty(propertyTypeId, elementIds,value, 0.0));
                    }
                    else {
                        addProperty(propertyTypeId,elementIds,Double.parseDouble(value));
                    }
                }
        );
    }

    public void addProperty(
            String propertyTypeId,
            List<String> elementIds,
            Double value) {
        this.properties.add(
                new ElementProperty(propertyTypeId, elementIds,"", value));
    }

    public List<ElementProperty> getProperties(String propertyTypeId) {
        return properties
                .stream()
                .filter(p -> p.propertyTypeId.equals(propertyTypeId))
                .collect(Collectors.toList());
    }

    public List<ElementProperty> getProperties(String propertyTypeId, String elementType, String elementId) {
        List<ElementProperty> properties = getProperties(propertyTypeId);

        return properties
                .stream()
                .filter(p
                -> p.elementIds //elements are pnode,enode... match on the pnode index
                .get(modelDefService.elementIndex(propertyTypeId,elementType))
                .equals(elementId))
                .collect(Collectors.toList());
    }

    public String getElementId(ElementProperty elementProperty, String elementType) {
        return elementProperty.elementIds
                .get(modelDefService.elementIndex(elementProperty.propertyTypeId,elementType));
    }

    public Double getDoubleValue(String propertyTypeId,List<String> elementIds) {
        Optional<ElementProperty> opt =
                properties
                        .stream()
                        .filter(p -> p.propertyTypeId.equals(propertyTypeId))
                        .findFirst();
        return opt.map(p -> p.doubleValue)
                .orElse(0.0);
    }

}
