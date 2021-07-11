import java.util.HashMap;
import java.util.Map;

public class MyUtilities {

    public static Map AddMapToMap(Map<String,String> map1, Map<String,String> map2){
        Map<String, String> mapPlusMap = new HashMap<>(map1);
        for (Map.Entry<String, String> entryFromMap2 : map2.entrySet()){
            mapPlusMap.put(entryFromMap2.getKey(), entryFromMap2.getValue());
        }
        return mapPlusMap;
    }
}
