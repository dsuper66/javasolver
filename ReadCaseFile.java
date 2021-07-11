import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ReadCaseFile {
    static InputFieldMapping inputFieldMapping = new InputFieldMapping();

    public static void readCase() throws IOException {
        inputFieldMapping.addFieldElementMap("pnodename","pnode",1);
        inputFieldMapping.addFieldElementMap("key1","enode",1);
        inputFieldMapping.addFieldElementMap("key2","enode",2);
        inputFieldMapping.addFieldElementMap("key3","enode",3);

        inputFieldMapping.addFieldPropertyMap("factor","enodePnodeFactor");

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
                //Property mapping
                propertyTypeFieldMaps = inputFieldMapping.propertyTypeFieldMap(fieldNames);

                System.out.println(elementTypeFieldMaps);
            }
            //DATA: Create the elements and properties
            else if (curLine.startsWith("D")) {
                List<String> fieldData = Arrays.asList(curLine.split(","));

                //Elements
                //The elementId is a concatenation of the fields for the elementType
                //For each element type that has a mapping
                for (String elementType : elementTypeFieldMaps.keySet()) {
                    Map<Integer,Integer> orderNumFieldNum = elementTypeFieldMaps.get(elementType);
                    String elementId = "";
                    for (Integer orderNum : orderNumFieldNum.keySet()) {
                        Integer fieldNum = orderNumFieldNum.get(orderNum);
                        elementId += " " + fieldData.get(fieldNum - 1);
                    }
                    //Create the element if not already
                    //System.out.println(elementType + ": " + elementId);
                }
                //Properties
                //For each of the element types
                //Check for elements that are properties
                //And check for properties that have mappings

            }
        }
        bufferedReader.close();
    }
}
