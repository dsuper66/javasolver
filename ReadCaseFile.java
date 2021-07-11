import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ReadCaseFile {
    static FieldElementMapping fieldElementMapping = new FieldElementMapping();
    static FieldPropertyMapping fieldPropertyMapping = new FieldPropertyMapping();

    public static void readCase() throws IOException {
        fieldElementMapping.addFieldElementMap("pnodename","pnode",1);
        fieldElementMapping.addFieldElementMap("key1","enode",1);
        fieldElementMapping.addFieldElementMap("key2","enode",2);
        fieldElementMapping.addFieldElementMap("key3","enode",3);

        fieldPropertyMapping.addFieldPropertyMap("factor","enodePnodeFactor");

        String file =
                "/Users/davidbullen/java/MSS_51112021071200687_0X/MSS_51112021071200687_0X.DAILY";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String curLine;
        HashMap<String,Map<Integer,Integer>> elementTypeFieldMaps = new HashMap<>();
        while ((curLine = bufferedReader.readLine()) != null){
            //process the line as required
            if (curLine.startsWith("I")) {
                //Get the headers and see which match elements and properties
                System.out.println(curLine);
                List<String> fieldNames = Arrays.asList(curLine.split(","));
                //map of fieldname to index

                elementTypeFieldMaps
                        = fieldElementMapping.elementTypeFieldMap(fieldNames);

                System.out.println(elementTypeFieldMaps);

            }
            else if (curLine.startsWith("D")) {
                //Create the elements
                List<String> fieldData = Arrays.asList(curLine.split(","));

                //The elementId is a concatenation of the fields for the elementType
                for (String elementType : elementTypeFieldMaps.keySet()) {
                    Map<Integer,Integer> orderNumFieldNum = elementTypeFieldMaps.get(elementType);
                    String elementId = "";
                    for (Integer orderNum : orderNumFieldNum.keySet()) {
                        Integer fieldNum = orderNumFieldNum.get(orderNum);
                        elementId += " " + fieldData.get(fieldNum - 1);
                    }
                    System.out.println(elementType + ": " + elementId);
                }
                //Integer arrayPos = 0;

                //Use elementType to fieldNums
                //For elementType, create array, array[order] = fieldData[fieldNum]

            }
        }
        bufferedReader.close();
    }
}
