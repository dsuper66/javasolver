import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModelDataService {
   private final ModelDefService modelDefService = new ModelDefService();

   private final ArrayList<ModelElement> modelElementsArray = new ArrayList<>();
   public final HashMap<String, ModelElement> modelElementsMap = new HashMap<>();

   public final ArrayList<ElementProperty> propertiesArray = new ArrayList<>();
   public final HashMap<String, ElementProperty> propertiesMap = new HashMap<>();

   //--------Elements-----------

   //Add element
   public String makeElementKey(String elementTypeId, String elementId) {
      return elementTypeId + ":" + elementId;
   }

   //Adding from pre-processing
   public void addElement(
         ModelDefService.ElementType elementType,
         String elementId) {
      addElement(elementType.name(), elementId);
   }
   //Adding from the read
   public void addElement(
         String elementType,
         String elementId) {

      //Add to map and array
      modelElementsMap.computeIfAbsent(makeElementKey(elementType, elementId), k -> {
         ModelElement newModelElement = new ModelElement(elementId, elementType);
         this.modelElementsArray.add(newModelElement);
         return newModelElement;
      });
   }

   public void removeElement(ModelDefService.ElementType elementType, String elementId) {
      String elKey = makeElementKey(elementType.name(), elementId);
      ModelElement elementToRemove = modelElementsMap.get(elKey);
      if (elementToRemove != null) {
         modelElementsMap.remove(elKey);
         modelElementsArray.remove(elementToRemove);
      }
      //this.modelElementsArray
   }

   public List<ModelElement> getElements(ModelDefService.ElementType elementType) {
      return getElements(elementType.name());
   }

   public List<ModelElement> getElements(String elementTypeId) {
      return modelElementsArray
            .stream()
            .filter(me -> me.elementType.equals(elementTypeId))
            .collect(Collectors.toList());
   }


   //--------Properties-----------
   public String makePropertyKey(String propertyTypeId, List<String> elementIds) {
      return propertyTypeId + ":[" + String.join(",", elementIds) + "]";
   }

   //Double using property type
   public void addProperty(
         ModelDefService.PropertyType propertyType,
         List<String> elementIds,
         Double value) {
      addProperty(propertyType.name(), elementIds, value);
   }

   //Double using property type, single element
   public void addProperty(
         ModelDefService.PropertyType propertyType,
         String elementId,
         Double value) {
      addProperty(propertyType.name(), List.of(elementId), value);
   }

   //String using property type, single element
   public void addProperty(
         ModelDefService.PropertyType propertyType,
         String elementId,
         String value) {
      addProperty(propertyType.name(), List.of(elementId), value);
   }

   //Double
   public void addProperty(
         String propertyTypeId,
         List<String> elementIds,
         Double value) {
      ElementProperty newProperty =
            new ElementProperty(propertyTypeId, elementIds, "", value);
      propertiesArray.add(newProperty);
      propertiesMap.put(makePropertyKey(propertyTypeId, elementIds), newProperty);
   }

   //String... create as string or double depending on valueType
   public void addProperty(
         String propertyTypeId,
         List<String> elementIds,
         String value) {

      modelDefService.propertyTypeDef(propertyTypeId).ifPresent(propertyTypeDef -> {
               //Double value
               if (propertyTypeDef.valueType.equals("double")) {
                  addProperty(propertyTypeId, elementIds, Double.parseDouble(value));
               }
               //String value
               else {
                  ElementProperty newProperty =
                        new ElementProperty(propertyTypeId, elementIds, value, 0.0);
                  //Add
                  propertiesArray.add(newProperty);
                  propertiesMap.put(makePropertyKey(propertyTypeId, elementIds), newProperty);
               }

               //Debug
            /*
                    //if (propertyTypeId.equals("nwEnodeForMktEnode") || propertyTypeId.equals("busForNwEnode")) {
                        System.out.println(">>>adding:"
                                           + " key = " + makePropertyKey(propertyTypeId,elementIds)
                                           + " value = " + value
                                );
                    //}

             */
            }
      );
   }

   //Remove property where any element matches
   public void removeProperty(
         ModelDefService.PropertyType propertyType,
         ModelDefService.ElementType elementType,
         String elementId) {

      for (ElementProperty p : getProperties(propertyType, elementType, elementId)) {
         propertiesMap.remove(makePropertyKey(p.propertyTypeId, p.elementIds));
         propertiesArray.remove(p);
      }
   }

   public void removeProperty(ElementProperty p) {
      propertiesMap.remove(makePropertyKey(p.propertyTypeId, p.elementIds));
      propertiesArray.remove(p);
   }

   //Get all properties of a certain type
   public List<ElementProperty> getProperties(ModelDefService.PropertyType propertyType) {
      return getProperties(propertyType.name());
   }

   public List<ElementProperty> getProperties(String propertyTypeId) {
      return propertiesArray
            .stream()
            .filter(p -> p.propertyTypeId.equals(propertyTypeId))
            .collect(Collectors.toList());
   }

   //Get all elements where this property value matches
   //e.g. get all tranche elements where trancheType property = "ENOF"
   public List<String> getElementIds(
         ModelDefService.ElementType elementType,
         ModelDefService.PropertyType propertyType,
         String value) {
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
   //e.g. all factorPnodeMktEnode(pnode,mktEnode) where pnodeId matches
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
   public String getElementId(ElementProperty elementProperty, String elementTypeId) {
      return elementProperty.elementIds
            .get(modelDefService.elementIndex(elementProperty.propertyTypeId, elementTypeId));
   }
   public String getElementId(ElementProperty elementProperty, ModelDefService.ElementType elementType) {
      return elementProperty.elementIds
            .get(modelDefService.elementIndex(elementProperty.propertyTypeId, elementType.name()));
   }

   public String getStringValue(String propertyTypeId, String elementId) {
      return getStringValue(propertyTypeId, List.of(elementId));
   }

   public String getStringValue(ModelDefService.PropertyType propertyType, String elementId) {
      //System.out.println("looking for " + propertyType.name() + " of " + elementId + " in " + propertiesMap);
      return getStringValue(propertyType, List.of(elementId));
   }

   public String getStringValue(ModelDefService.PropertyType propertyType, List<String> elementIds) {
      return getStringValue(propertyType.name(), elementIds);
   }

   public String getStringValue(String propertyTypeId, List<String> elementIds) {
      Optional<ElementProperty> opt = Optional.ofNullable(
            propertiesMap.get(makePropertyKey(propertyTypeId, elementIds)));
      return opt.map(p -> p.stringValue)
            .orElse("");
   }

   //Get double value for PropertyType(ElementIds), e.g., factorPnodeMktEnode(pnodeId,mktEnodeId)
   public Double getDoubleValue(String propertyTypeId, List<String> elementIds) {
      Optional<ElementProperty> opt = Optional.ofNullable(
            propertiesMap.get(makePropertyKey(propertyTypeId, elementIds)));
      return opt.map(p -> p.doubleValue)
            .orElse(0.0);
   }

   public Double getDoubleValue(ModelDefService.PropertyType propertyType, List<String> elementIds) {
      return getDoubleValue(propertyType.name(), elementIds);
   }

   public Double getDoubleValueElseOne(String propertyTypeId, String elementId) {
      return getDoubleValueElseOne(propertyTypeId, List.of(elementId));
   }

   public Double getDoubleValueElseOne(String propertyTypeId, List<String> elementIds) {
      Optional<ElementProperty> opt = Optional.ofNullable(
            propertiesMap.get(makePropertyKey(propertyTypeId, elementIds)));
      return opt.map(p -> p.doubleValue)
            .orElse(1.0);
   }


   public Optional<ElementProperty> getProperty(ModelDefService.PropertyType propertyType, String elementId) {
      return Optional.ofNullable(
            propertiesMap.get(makePropertyKey(propertyType.name(), List.of(elementId))));
   }

   //This is the old slow way, now uses map
   /*
   public Optional<ElementProperty> getProperty(ModelDefService.PropertyType propertyType, List<String> elementIds) {
      List<ElementProperty> propertiesThisType = getProperties(propertyType);
      return propertiesThisType
            .stream()
            .filter(p -> p.elementIds.equals(elementIds))
            .findFirst();
   }*/
}
