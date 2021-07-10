import java.util.ArrayList;

public class FieldPropertyMapping {

    String fieldName;
    String propertyType;

    private class FieldPropertyMap {
        String fieldName;
        String propertyType;
        Integer order;

        private FieldPropertyMap(
                String fieldName,
                String propertyType){
            this.fieldName = fieldName;
            this.propertyType = propertyType;
        }
    }

    private ArrayList<FieldPropertyMap> fieldPropertyMapArrayList = new ArrayList<>();

    public void addFieldPropertyMap(String fieldName, String propertyType) {
        this.fieldPropertyMapArrayList.add(
                new FieldPropertyMap(fieldName, propertyType));
    }

}
