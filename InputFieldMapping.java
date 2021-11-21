import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class InputFieldMapping {

    //-------Element mapping-------
    static class FieldElementMap {
        final String sectionName;
        final String fieldName;
        final String elementType;
        final Integer order;

        private FieldElementMap(
                String sectionName,
                String fieldName,
                String elementType,
                Integer order){
            this.sectionName = sectionName;
            this.fieldName = fieldName;
            this.elementType = elementType;
            this.order = order;
        }

        @Override
        public String toString() {
            return ("sectionName:"+ this.sectionName+
                    " fieldName: "+ this.fieldName +
                    " elementType: "+ this.elementType);
        }
    }
    private List<FieldElementMap> fieldElementMaps = new ArrayList<>();

    public void addFieldElementMap(String sectionName, String fieldName, String elementType) {
        addFieldElementMap(sectionName, fieldName, elementType,1);
    }
    public void addFieldElementMap(String sectionName, String fieldName, String elementType, Integer order) {
        this.fieldElementMaps.add(
              new FieldElementMap(sectionName, fieldName, elementType, order));
    }

    public List<FieldElementMap> getFieldElementMaps(){
        return fieldElementMaps;
    }

    //For the Header row, return matching ELEMENTS and the orderNum,fieldNum that hold their component(s)
    public HashMap<String,Map<Integer,Integer>> getElementFieldMapForSectionFieldNames(
            String thisSectionName,
            List<String> fieldNames) {
        //The return... elementType : [orderNum : fieldNum]
        HashMap<String,Map<Integer,Integer>> elementTypeFieldMaps = new HashMap<>();
        //Go through all the fieldNames, add those that have a mapping
        Integer thisFieldNum = 0;
        for (String thisFieldName : fieldNames) {
            thisFieldNum++;
            //Get the mappings (if any) for this fieldname in this section
            List<FieldElementMap> matchingFieldElementMaps =
                    fieldElementMaps
                            .stream()
                            .filter(fem ->
                                    fem.fieldName.equalsIgnoreCase(thisFieldName)
                                    && fem.sectionName.toUpperCase().equals(thisSectionName))
                            .collect(Collectors.toList());

            //System.out.println("thisFieldName:" + thisFieldName);
            //Create a mapping from the elementType to a map of orderNum,fieldNum
            //https://stackoverflow.com/questions/63349403/how-to-efficiently-merge-two-lists-in-java
            for (FieldElementMap thisFieldElementMap : matchingFieldElementMaps) {
                String thisElementType = thisFieldElementMap.elementType;
                Integer orderForThisFieldNum = thisFieldElementMap.order;

                //System.out.println("Element:" + thisElementType
                //        + " FieldName:" + thisFieldName + " order:" + orderForThisFieldNum);

                //Add or update map from orderNum to fieldNum
                //(because element i.d. can have more than one fieldNum)
                Map<Integer,Integer> foundOrderNumFieldNumMap = elementTypeFieldMaps.get(thisElementType);
                //New
                if (foundOrderNumFieldNumMap == null) {
                    elementTypeFieldMaps.put(thisElementType,Map.of(orderForThisFieldNum,thisFieldNum));
                }
                //There is already an orderNum map for this elementType, add another
                else {
                    HashMap<Integer,Integer> updatedMap;
                    updatedMap = new HashMap<>(foundOrderNumFieldNumMap);
                    updatedMap.put(orderForThisFieldNum,thisFieldNum);
                    //System.out.println("hashmap: " + updatedMap);
                    //Replace the existing map
                    elementTypeFieldMaps.put(thisElementType,updatedMap);
                }

            }
        }
        return elementTypeFieldMaps;
    }

    //-------Property mapping-------
    static class FieldPropertyMap {
        final String sectionName;
        final String fieldName;
        final String propertyType;

        private FieldPropertyMap(
                String sectionName,
                String fieldName,
                String propertyType){
            this.sectionName = sectionName;
            this.fieldName = fieldName;
            this.propertyType = propertyType;
        }
    }
    private List<FieldPropertyMap> fieldPropertyMaps = new ArrayList<>();

    public void addFieldPropertyMap(String sectionName, String fieldName, String propertyTypeId) {
        this.fieldPropertyMaps.add(
              new FieldPropertyMap(sectionName, fieldName, propertyTypeId));
    }
    public void addFieldPropertyMap(String sectionName, String fieldName, ModelDefService.PropertyType propertyType) {
        this.fieldPropertyMaps.add(
              new FieldPropertyMap(sectionName, fieldName, propertyType.name()));
    }

    public List<FieldPropertyMap> getFieldPropertyMaps(){
        return fieldPropertyMaps;
    }


    //For the Header row, return matching PROPERTY and associated fieldNum
    public HashMap<String,Integer> getPropertyFieldMapForSectionFieldNames(
            String thisSectionName, List<String> fieldNames) {
        //The return... propertyType : fieldNum
        HashMap<String,Integer> propertyTypeFieldMap = new HashMap<>();

        Integer thisFieldNum = 0;
        //String thisSectionName = fieldNames.get(2);
        for (String thisFieldName : fieldNames) {
            thisFieldNum++;
            //Get the PROPERTY mappings (if any) for this fieldname
            List<FieldPropertyMap> matchingFieldPropertyMaps =
                    fieldPropertyMaps
                            .stream()
                            .filter(fpm -> fpm.fieldName.equalsIgnoreCase(thisFieldName)
                                    && fpm.sectionName.equalsIgnoreCase(thisSectionName))
                            .collect(Collectors.toList());

            //Create a mapping from the elementType to a map of orderNum,fieldNum
            //https://stackoverflow.com/questions/63349403/how-to-efficiently-merge-two-lists-in-java
            for (FieldPropertyMap thisFieldPropertyMap : matchingFieldPropertyMaps) {
                String thisPropertyType = thisFieldPropertyMap.propertyType;
                //System.out.println("Property:" + thisPropertyType
                //        + " FieldName:" + thisFieldName);

                //Map the property type to the field number
                propertyTypeFieldMap.put(thisPropertyType,thisFieldNum);
            }
        }

        return propertyTypeFieldMap;
    }

    //Read the map from case file to elements and properties
    public void readInputMaps() {
        try {
        //String dir = "/Users/davidbullen/java/";
            String dir = "../../";
        //String elementsFile = "field-element-maps.json";
        //String propertiesFile = "field-property-maps.json";
        //https://attacomsian.com/blog/jackson-read-json-file
            //https://stackoverflow.com/questions/29965764/how-to-parse-json-file-with-gson
            Gson gson = new Gson();
            String fieldToElementMapFile = "field-element-maps.json";
            JsonReader reader = new JsonReader(new FileReader(dir + fieldToElementMapFile));
            fieldElementMaps = Arrays.asList(gson.fromJson(reader, FieldElementMap[].class));
            //Confirm that all the elements are defined
            List<FieldElementMap> unMatchedElementTypes = fieldElementMaps
                  .stream()
                  .filter(fem -> Arrays
                        .stream(ModelDefService.ElementType.values())
                        .noneMatch(v -> v.name().equals(fem.elementType))).collect(Collectors.toList());
            if (unMatchedElementTypes.size() > 0) {
                throw new RuntimeException("Element map file" + fieldToElementMapFile
                                           + " contain undefined element(s) " + unMatchedElementTypes);
            }

            reader = new JsonReader(new FileReader(dir + "field-property-maps.json"));
            fieldPropertyMaps = Arrays.asList(gson.fromJson(reader, FieldPropertyMap[].class));

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
