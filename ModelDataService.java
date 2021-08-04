import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModelDataService {
    private ModelDefService modelDefService = new ModelDefService();

    private ArrayList<ModelElement> modelElementsArray = new ArrayList<>();
    public HashMap<String,ModelElement> modelElementsMap = new HashMap<>();

    public ArrayList<ElementProperty> propertiesArray = new ArrayList<>();
    public HashMap<String,ElementProperty> propertiesMap = new HashMap<>();

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

    //Add element
    public String makeElementKey(String elementTypeId,String elementId) {
        return elementTypeId + ":" + elementId;
    }

    //Adding from pre-processing
    public void addElement(
            ModelDefService.ElementType elementType,
            String elementId){
        addElement(elementType.name(),elementId);
    }

    //Adding from the read
    public void addElement(
            String elementType,
            String elementId) {

        /*
        HashMap<String, List<String>> properties = new HashMap<>();
        List<String> elementTypeProperties = modelDefService.getPropertiesForElementType(elementType);
        //Add an empty property list for each property
        for (String propertyType : elementTypeProperties) {
            properties.putIfAbsent(propertyType,List.of(""));
        }*/
        //Add to map and array
        modelElementsMap.computeIfAbsent(makeElementKey(elementType,elementId),k -> {
            ModelElement newModelElement = new ModelElement(elementId,elementType);
            this.modelElementsArray.add(newModelElement);
            return newModelElement;
        });

    }

    //Set the property of the element (if it exists)
    //https://www.baeldung.com/java-optional
    /*
    public void assignElementProperty(String elementId, String propertyType, String value) {
        getElement(elementId).ifPresent(modelElement ->
                {
                    if (modelElement.elementType.equals("bus")) {
                        //System.out.println("elementId:" + elementId + " propertyType:" + propertyType + " propertyValue:" + value);
                    }
                }
        );
    }*/
/*
    public Optional<ModelElement> getElement(String elementId) {
        Optional<ModelElement> opt =
                modelElementsArray
                        .stream()
                        .filter(e -> e.elementId.equals(elementId))
                        .findFirst();
        //opt.ifPresent(modelElement ->
        //    System.out.println(elementId + " properties:" + modelElement.properties));
        return opt;
    }*/

    public List<ModelElement> getElements(ModelDefService.ElementType elementType) {
        return modelElementsArray
                .stream()
                .filter(me -> me.elementType.equals(elementType.name()))
                .collect(Collectors.toList());
    }


    //--------Properties-----------
    public String makePropertyKey(String propertyTypeId,List<String> elementIds) {
        return propertyTypeId + ":[" + String.join(",", elementIds) +"]";
    }

    //Double
    public void addProperty(
            String propertyTypeId,
            List<String> elementIds,
            Double value) {
        ElementProperty newProperty =
                new ElementProperty(propertyTypeId, elementIds, "", value);
        propertiesArray.add(newProperty);
        propertiesMap.put(makePropertyKey(propertyTypeId,elementIds), newProperty);

    }
    //String... create as string or double depending on valueType
    public void addProperty(
            String propertyTypeId,
            List<String> elementIds,
            String value) {

        modelDefService.propertyTypeDef(propertyTypeId).ifPresent(propertyTypeDef -> {
                    //Double value
                    if (propertyTypeDef.valueType.equals("double")) {
                        addProperty(propertyTypeId,elementIds,Double.parseDouble(value));
                    }
                    //String value
                    else {
                        ElementProperty newProperty =
                                new ElementProperty(propertyTypeId, elementIds, value, 0.0);
                        propertiesArray.add(newProperty);
                        propertiesMap.put(makePropertyKey(propertyTypeId,elementIds), newProperty);
                    }

                    //Debug
                    if (propertyTypeId.equals("nwEnodeForMktEnode") || propertyTypeId.equals("busForNwEnode")) {
                        System.out.println("adding:" + elementIds + " value:" + value
                                + " key:" + makePropertyKey(propertyTypeId,elementIds));
                    }
                }
        );
    }

    //Get all properties of a certain type
    public List<ElementProperty> getProperties(ModelDefService.PropertyType propertyType) {
        return propertiesArray
                .stream()
                .filter(p -> p.propertyTypeId.equals(propertyType.name()))
                .collect(Collectors.toList());
    }

    //Get all elements where this property value matches
    //e.g. get all tranche elements where trancheType property = "ENOF"
    public List<String> getElementIds(
            ModelDefService.ElementType elementType,
            ModelDefService.PropertyType propertyType,
            String value){
        List<ElementProperty> properties =
                getProperties(propertyType)
                .stream()
                .filter(property
                        -> property.stringValue.equals(value))
                .collect(Collectors.toList());

        final Integer elementIndex = modelDefService.elementIndex(propertyType.name(), elementType.name());
        return properties
                .stream()
                .map(p -> p.elementIds.get(elementIndex))
                .collect(Collectors.toList());
    }

    //Get all properties of this type where the *specified* element type matches the provided i.d.
    //e.g. probably only used for all factorPnodeMktEnode(pnode,mktEnode) where pnodeId matches
    //this is a bit slow because we don't have the key (could add map to array to make this faster)
    public List<ElementProperty> getProperties(
            ModelDefService.PropertyType propertyType,
            ModelDefService.ElementType elementType,
            String elementId) {
        List<ElementProperty> properties = getProperties(propertyType);

        final Integer elementIndex = modelDefService.elementIndex(propertyType.name(), elementType.name());
        return properties
                .stream()
                .filter(property
                        -> property.elementIds
                        //use the index for the elementType
                        .get(elementIndex)
                        .equals(elementId))
                .collect(Collectors.toList());
    }

    //For this property, get the id of the indexing element of the specified type
    //e.g. again, probably only used to get the MktEnode i.d. from the FactorPnodeMktEnode property
    public String getElementId(ElementProperty elementProperty, String elementType) {
        return elementProperty.elementIds
                .get(modelDefService.elementIndex(elementProperty.propertyTypeId,elementType));
    }

    public String getStringValue(ModelDefService.PropertyType propertyType, List<String> elementIds) {
        /*
        if (propertyTypeId.equals("busForNwEnode")) {
            System.out.println("looking for: " + propertyTypeId + " elements:" + elementIds);
        }*/
        /*Optional<ElementProperty> opt = getProperty(propertyTypeId, elementIds);
        return opt.map(p -> p.stringValue)
                .orElse("");*/
        Optional<ElementProperty> opt = Optional.ofNullable(
                propertiesMap.get(makePropertyKey(propertyType.name(), elementIds)));
        return opt.map(p -> p.stringValue)
                .orElse("");


    }

    //Get double value for PropertyType(ElementIds), e.g., factorPnodeMktEnode(pnodeId,mktEnodeId)
    public Double getDoubleValue(ModelDefService.PropertyType propertyType, List<String> elementIds) {
        /*
        if (propertyTypeId.equals("factorPnodeMktEnode")) {
            System.out.println("looking for: " + propertyTypeId + " elements:" + elementIds);
        }*/
        Optional<ElementProperty> opt = getProperty(propertyType,elementIds);
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
    //This is the old slow way, now uses map
    public Optional<ElementProperty> getProperty(ModelDefService.PropertyType propertyType, List<String> elementIds) {
        List<ElementProperty> propertiesThisType = getProperties(propertyType);
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

        //System.out.println(opt.map(o -> "found:" + o));
        return opt;

    }


}
