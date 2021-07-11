import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        HashMap<String,Integer[]> elementTypeFieldNums = new HashMap<>();
        while ((curLine = bufferedReader.readLine()) != null){
            //process the line as required
            if (curLine.startsWith("I")) {
                //Get the headers and see which match elements and properties
                System.out.println(curLine);
                List<String> fieldNames = Arrays.asList(curLine.split(","));
                //map of fieldname to index

                HashMap<String,List<Map<Integer,Integer>>> elementTypeFieldMap
                        = fieldElementMapping.elementTypeFieldMap(fieldNames);
                System.out.println(elementTypeFieldMap);

            }
            else if (curLine.startsWith("D")) {
                //Create the elements
                List<String> fieldData = Arrays.asList(curLine.split(","));
                //Integer arrayPos = 0;

                //Use elementType to fieldNums
                //For elementType, create array, array[order] = fieldData[fieldNum]

            }
        }
        bufferedReader.close();
    }
}
