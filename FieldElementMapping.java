import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //Return matching elements and the orderNum,fieldNum that hold their component(s)
    public HashMap<String,List<Map<Integer,Integer>>> elementTypeFieldMap (List<String> fieldNames) {
        HashMap<String,List<Map<Integer,Integer>>> fieldNumElementMap = new HashMap<>();
        Integer thisFieldNum = 0;
        for (String thisFieldName : fieldNames) {
            thisFieldNum++;
            //Get the mappings (if any) for this fieldname
            List<FieldElementMap> matchingFeildElementMaps =
                    (List<FieldElementMap>) fieldElementMaps
                            .stream()
                            .filter(fem -> fem.fieldName.toUpperCase().equals(thisFieldName.toUpperCase()))
                            .collect(Collectors.toList());

            //Create a mapping from the elementType to array of orderNum,fieldNum
            //https://stackoverflow.com/questions/63349403/how-to-efficiently-merge-two-lists-in-java
            for (FieldElementMap thisFieldElementMap : matchingFeildElementMaps) {
                String thisElementType = thisFieldElementMap.elementType;
                Integer orderForThisFieldNum = thisFieldElementMap.order;
                List<Map<Integer,Integer>> foundOrderNumFieldNums = fieldNumElementMap.get(thisElementType);
                List<Map<Integer,Integer>> thisOrderNumFieldNum = List.of(Map.of(orderForThisFieldNum,thisFieldNum));
                if (foundOrderNumFieldNums == null) {
                    fieldNumElementMap.put(thisElementType,thisOrderNumFieldNum);
                }
                else {
                    List<Map<Integer,Integer>> updatedList =
                            Stream.concat(foundOrderNumFieldNums.parallelStream(), thisOrderNumFieldNum.parallelStream())
                            .collect(Collectors.toList());
                    fieldNumElementMap.put(thisElementType,updatedList);
                }
            }
        }
        return fieldNumElementMap;
        /*
        return fieldNames
                .stream()
                .map(fn -> elementTypeForFieldName(fn))
                .collect(Collectors.toList());*/
    }
}
