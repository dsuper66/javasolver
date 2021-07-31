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
                    if (propertyTypeDef.valueType.equals("double")) {
                        addProperty(propertyTypeId,elementIds,Double.parseDouble(value));
                    }
                    else {
                        this.properties.add(
                                new ElementProperty(propertyTypeId, elementIds,value, 0.0));
                    }
            if (propertyTypeId.equals("nwEnodeForMktEnode") || propertyTypeId.equals("factorPnodeMktEnode")) {
                System.out.println("adding:" + elementIds + " value:" + value);
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
                -> p.elementIds
                .get(modelDefService.elementIndex(propertyTypeId,elementType))
                .equals(elementId))
                .collect(Collectors.toList());
    }

    public String getElementId(ElementProperty elementProperty, String elementType) {
        return elementProperty.elementIds
                .get(modelDefService.elementIndex(elementProperty.propertyTypeId,elementType));
    }

    public String getStringValue(String propertyTypeId,List<String> elementIds) {
        if (propertyTypeId.equals("nwEnodeForMktEnode")) {
            System.out.println("looking for: " + propertyTypeId + " elements:" + elementIds);
        }
        Optional<ElementProperty> opt = getProperty(propertyTypeId, elementIds);
        return opt.map(p -> p.stringValue)
                .orElse("");
    }

    //Get double value for PropertyType(ElementIds), e.g., factorPnodeMktEnode(pnodeId,mktEnodeId)
    public Double getDoubleValue(String propertyTypeId,List<String> elementIds) {
        if (propertyTypeId.equals("factorPnodeMktEnode")) {
            System.out.println("looking for: " + propertyTypeId + " elements:" + elementIds);
        }
        Optional<ElementProperty> opt = getProperty(propertyTypeId,elementIds);
        return opt.map(p -> p.doubleValue)
                .orElse(0.0);
        /*
        List<ElementProperty> propertiesThisType = getProperties(propertyTypeId);
        Optional<ElementProperty> opt =
                propertiesThisType
                        .stream()
                        .filter(p -> {
                            Boolean matched = true;
                            Integer index = 0;
                            for (String elementId : p.elementIds) {
                                if (!elementIds.get(index).equals(elementId)) {
                                    matched = false;
                                    break;
                                }
                                index++;
                            }
                            return matched;
                        })
                        .findFirst();
        return opt.map(p -> p.doubleValue)
                .orElse(0.0);*/
    }


    //Get double value for PropertyType(ElementIds), e.g., factorPnodeMktEnode(pnodeId,mktEnodeId)
    public Optional<ElementProperty> getProperty(String propertyTypeId,List<String> elementIds) {
        List<ElementProperty> propertiesThisType = getProperties(propertyTypeId);
        Optional<ElementProperty> opt =
                propertiesThisType
                        .stream()
                        .filter(p -> p.elementIds.equals(elementIds))
                        /*
                        Boolean matched = true;
                        Integer index = 0;
                        for (String elementId : elementIds) {
                            if (propertyTypeId.equals("nwEnodeForMktEnode")) {
                                System.out.println("checking:" + p.propertyTypeId + " el:" + elementId);
                            }
                            if (!elementIds.get(index).equals(elementId)) {
                                matched = false;
                                break;
                            }
                            index++;
                        }
                        return matched;*/
                        .findFirst();

        System.out.println(opt.map(o -> "found:" + o));
        return opt;

    }


}
