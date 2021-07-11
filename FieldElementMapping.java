import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldElementMapping {

    private class FieldElementMap {
        String fieldName;
        String elementType;
        Integer order;

        private FieldElementMap(
                String fieldName,
                String elementType,
                Integer order){
            this.fieldName = fieldName;
            this.elementType = elementType;
            this.order = order;
        }
    }

    private ArrayList<FieldElementMap> fieldElementMaps = new ArrayList<>();

    public void addFieldElementMap(String fieldName, String elementType, Integer order) {
        this.fieldElementMaps.add(
                new FieldElementMap(fieldName, elementType, order));
    }

    //Get the element type for the field name
    private String elementTypeForFieldName(String fieldName) {
        List<FieldElementMap> matchingFeildElementMaps =
                (List<FieldElementMap>) fieldElementMaps
                        .stream()
                        .filter(fem -> fem.fieldName.toUpperCase().equals(fieldName.toUpperCase()))
                        .collect(Collectors.toList());
        if (!matchingFeildElementMaps.isEmpty()){
            return matchingFeildElementMaps.get(0).elementType;
        }
        else {
            return "";
        }
    }

    //For the Header row, return matching elements and the fieldNum
    public HashMap<String,Integer> propertyTypeFieldMap (List<String> fieldNames) {
        //The return
        HashMap<String,Integer> propertyTypeFieldMap = new HashMap<>();

        return propertyTypeFieldMap;
    }

    //For the Header row, return matching elements and the orderNum,fieldNum that hold their component(s)
    public HashMap<String,Map<Integer,Integer>> elementTypeFieldMap (List<String> fieldNames) {
        //The return
        HashMap<String,Map<Integer,Integer>> elementTypeFieldMaps = new HashMap<>();
        //Go through all the fieldNames, add those that have a mapping
        Integer thisFieldNum = 0;
        for (String thisFieldName : fieldNames) {
            thisFieldNum++;
            //Get the mappings (if any) for this fieldname
            List<FieldElementMap> matchingFieldElementMaps =
                    fieldElementMaps
                            .stream()
                            .filter(fem -> fem.fieldName.toUpperCase().equals(thisFieldName.toUpperCase()))
                            .collect(Collectors.toList());

            System.out.println("thisFieldName:" + thisFieldName);
            //Create a mapping from the elementType to an map of orderNum,fieldNum
            //https://stackoverflow.com/questions/63349403/how-to-efficiently-merge-two-lists-in-java
            for (FieldElementMap thisFieldElementMap : matchingFieldElementMaps) {
                String thisElementType = thisFieldElementMap.elementType;
                Integer orderForThisFieldNum = thisFieldElementMap.order;

                System.out.println("thisElementType:" + thisElementType + " orderForThisFieldNum:" + orderForThisFieldNum);
                Map<Integer,Integer> foundOrderNumFieldNumMap = elementTypeFieldMaps.get(thisElementType);

                //Add or update map from orderNum to fieldNum
                if (foundOrderNumFieldNumMap == null) {
                    elementTypeFieldMaps.put(thisElementType,Map.of(orderForThisFieldNum,thisFieldNum));
                }
                else {
                    HashMap<Integer,Integer> updatedMap = new HashMap(foundOrderNumFieldNumMap);
                    updatedMap.put(orderForThisFieldNum,thisFieldNum);
                    System.out.println("hashmap: " + updatedMap);
                    elementTypeFieldMaps.put(thisElementType,updatedMap);
                }

            }
        }
        return elementTypeFieldMaps;
        /*
        return fieldNames
                .stream()
                .map(fn -> elementTypeForFieldName(fn))
                .collect(Collectors.toList());*/
    }
}
