import java.util.Map;
import java.util.Set;

public class ModelElementDefService {

    //The properties of an element type
    //e.g. elementTypeProperties['branch'] = ['fromBus', 'toBus', 'susceptance', 'resistance','flowMax'];
    static Map<String, Set<String>> elementTypeProperties =
            Map.of(
                    "branch", Set.of("fromBus","toBus"),
                    "enode", Set.of("enodePnode","enodePnodeFactor")
                    );

    static Set<String> getPropertiesForElementType(String elementType) {
        if (elementTypeProperties.get(elementType) != null) {
            return elementTypeProperties.get(elementType);
        }
        else {
            return Set.of("");
        }
    }

    static Boolean elementTypeHasProperty(String elementType, String propertyType) {
        Set<String> propertyTypes = elementTypeProperties.get(elementType);
        if (propertyTypes != null) {
            return propertyTypes.contains(elementType);
        }
        else {
            return false;
        }
    }
}
