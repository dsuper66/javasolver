import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class ConstraintPrep {

    public static void readConstraints(){
        String dir = "/Users/davidbullen/java/";
        String defFile = "constraint-defs.json";
        String compFile = "constraint-comps.json";


//        BufferedReader bufferedReader =
//                new BufferedReader(new FileReader(fileName));
        //System.out.println("file:" + fileName);

        //https://attacomsian.com/blog/jackson-read-json-file
        ObjectMapper mapper = new ObjectMapper();

        try {
            /*
            List<ConstraintDef> constraintDefs =
                    Arrays.asList(mapper.readValue(Paths.get(fileName).toFile(), ConstraintDef[].class));

            for (ConstraintDef c : constraintDefs) {
                System.out.println("constraint:" + c.constraintType);
            }*/

            //ConstraintDef c = mapper.readValue(Paths.get(fileName).toFile(), ConstraintDef.class);

            //https://stackoverflow.com/questions/29965764/how-to-parse-json-file-with-gson
            Gson gson = new Gson();

            JsonReader reader = new JsonReader(new FileReader(dir + defFile));
            final List<ConstraintDef> constraintDefs = Arrays.asList(gson.fromJson(reader, ConstraintDef[].class));

            for (ConstraintDef cd : constraintDefs) {
                System.out.println("constraint def:" + cd.constraintType);
            }

            reader = new JsonReader(new FileReader(dir + compFile));
            final List<ConstraintComp> constraintComps = Arrays.asList(gson.fromJson(reader, ConstraintComp[].class));

            for (ConstraintComp cc : constraintComps) {
                System.out.println("constraint comp:" + cc.constraintType);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
