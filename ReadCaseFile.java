import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
                "/Users/davidbullen/java/examples/cplex_demo/src/MSS_51112021071200687_0X/MSS_51112021071200687_0X.DAILY";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String curLine;
        while ((curLine = bufferedReader.readLine()) != null){
            //process the line as required
            if (curLine.startsWith("I")) {
                System.out.println(curLine);
                String[] fieldNames = curLine.split(",");
            }
        }
        bufferedReader.close();
    }
}
