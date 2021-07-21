import java.util.List;
import java.util.Map;

public class MyUtilities {

    public static Map AddMapToMap(Map<String, List<String>> map1, Map<String,List<String>> map2){
        //https://stackoverflow.com/questions/7194522/how-to-putall-on-java-hashmap-contents-of-one-to-another-but-not-replace-existi
        map2.forEach(map1::putIfAbsent);
        return map1;
    }
}
