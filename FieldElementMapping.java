import java.util.ArrayList;
import java.util.List;
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

    //If a fieldName has a matching elementType then return the elementType else empty string
    public List<String> elementTypesForFieldNums (List<String> fieldNames) {
        return fieldNames
                .stream()
                .map(fn -> elementTypeForFieldName(fn))
                .collect(Collectors.toList());
    }
}
