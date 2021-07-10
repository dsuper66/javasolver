import java.util.ArrayList;

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

    private ArrayList<FieldElementMap> fieldElementMapArrayList = new ArrayList<>();

    public void addFieldElementMap(String fieldName, String elementType, Integer order) {
        this.fieldElementMapArrayList.add(
                new FieldElementMap(fieldName, elementType, order));
    }
}
