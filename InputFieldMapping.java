import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InputFieldMapping {

    //-------Element mapping-------
    private static class FieldElementMap {
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
    }
    private final ArrayList<FieldElementMap> fieldElementMaps = new ArrayList<>();

    public void addFieldElementMap(String sectionName, String fieldName, String elementType, Integer order) {
        this.fieldElementMaps.add(
              new FieldElementMap(sectionName, fieldName, elementType, order));
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

    private static class FieldPropertyMap {
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

    private final ArrayList<FieldPropertyMap> fieldPropertyMaps = new ArrayList<>();

    public void addFieldPropertyMap(String sectionName, String fieldName, String propertyType) {
        this.fieldPropertyMaps.add(
              new FieldPropertyMap(sectionName, fieldName, propertyType));
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

}
