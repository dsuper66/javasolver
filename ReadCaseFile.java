import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ReadCaseFile {
    static InputFieldMapping inputFieldMapping = new InputFieldMapping();
    static ModelElementDefService modelElementDefService = new ModelElementDefService();
    static ModelElementDataService modelElementDataService = new ModelElementDataService();

    public static void readCase() throws IOException {
        //Map a field name to an element type
        inputFieldMapping.addFieldElementMap("pnodename","pnode",1);
        inputFieldMapping.addFieldElementMap("key1","enode",1);
        inputFieldMapping.addFieldElementMap("key2","enode",2);
        inputFieldMapping.addFieldElementMap("key3","enode",3);

        //Map a field name to a property type
        inputFieldMapping.addFieldPropertyMap("factor","enodePnodeFactor");
        inputFieldMapping.addFieldPropertyMap("pnodename","enodePnode");

        //Map an element type to a property type
        //(allows for concatenated element i.d.)
        inputFieldMapping.addElementPropertyMap("enode","pnodeEnode");

        String file =
                "/Users/davidbullen/java/MSS_51112021071200687_0X/MSS_51112021071200687_0X.DAILY";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String curLine;
        HashMap<String,Map<Integer,Integer>> elementTypeFieldMaps = new HashMap<>();
        HashMap<String,Integer> propertyTypeFieldMaps = new HashMap<>();
        while ((curLine = bufferedReader.readLine()) != null){
            //HEADER: Get the headers and see which match elements and properties
            if (curLine.startsWith("I")) {

                System.out.println(curLine);
                List<String> fieldNames = Arrays.asList(curLine.split(","));

                //Element mapping
                elementTypeFieldMaps = inputFieldMapping.elementTypeFieldMap(fieldNames);
                System.out.println(elementTypeFieldMaps);
                //Property mapping
                propertyTypeFieldMaps = inputFieldMapping.propertyTypeFieldMap(fieldNames);
                System.out.println(propertyTypeFieldMaps);
            }
            //DATA: Create the elements and properties
            else if (curLine.startsWith("D")) {
                List<String> fieldData = Arrays.asList(curLine.split(","));

                //Elements
                //The elementId is a concatenation of the fields for the elementType
                //For each element type that has a mapping from the header processing
                //create the elementId
                HashMap<String,String> elementIdsAndTypeToAdd = new HashMap<>();
                for (String elementType : elementTypeFieldMaps.keySet()) {
                    Map<Integer,Integer> orderNumFieldNum = elementTypeFieldMaps.get(elementType);
                    String elementId = "";
                    //Create the elementId from one or more fields
                    for (Integer orderNum : orderNumFieldNum.keySet()) {
                        Integer fieldNum = orderNumFieldNum.get(orderNum);
                        elementId += " " + fieldData.get(fieldNum - 1);
                    }
                    elementIdsAndTypeToAdd.put(elementId,elementType);
                }
                //System.out.println("elementTypes:" + elementIdAndTypeToAdd);

                //Add the elements
                for (var elementIdAndType : elementIdsAndTypeToAdd.entrySet()) {
                    modelElementDataService.addElement(
                            elementIdAndType.getKey(),elementIdAndType.getValue());
                }

                //Get and assign the Properties
                for (String propertyType : propertyTypeFieldMaps.keySet()) {
                    Integer fieldNum = propertyTypeFieldMaps.get(propertyType);
                    String propertyValue = fieldData.get(fieldNum - 1);
                    //System.out.println("propertyType:" + propertyType + " propertyValue:" + propertyValue);

                    //If any of the element types have this property then assign the value
                    for (var elementIdAndType : elementIdsAndTypeToAdd.entrySet()) {
                        if (modelElementDefService.elementTypeHasProperty(
                                elementIdAndType.getValue(),propertyType)) {
                            modelElementDataService.assignPropertyValue(
                                    propertyType,elementIdAndType.getKey(),propertyValue);
                        }
                    }
                }
            }
        }
        bufferedReader.close();
    }
}
