import java.util.HashMap;
import java.util.Map;

public class MyUtilities {

    public static Map AddMapToMap(Map<String,String> map1, Map<String,String> map2){
        Map<String, String> copy = new HashMap<>(map1);
        for (Map.Entry<String, String> entry : map2.entrySet()){
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }
}
