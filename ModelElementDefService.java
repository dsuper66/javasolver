import java.util.List;
import java.util.Map;

public class ModelElementDefService {

    //The properties of an element type
    //e.g. elementTypeProperties['branch'] = ['fromBus', 'toBus', 'susceptance', 'resistance','flowMax'];
    static Map<String, List<String>> elementTypeProperties =
            Map.of(
                    "branch", List.of("fromBus","toBus","susceptance","resistance"),
                    "enode", List.of("enodePnode","enodePnodeFactor"),
                    "pnode", List.of("actualLoad"),
                    "enOfferTranche",List.of("tranchePnode","trancheLimit","tranchePrice"),
                    "nwEnode",List.of("nwEnodeEnode","nwEnodeBus")
                    );

    static List<String> getPropertiesForElementType(String elementType) {
        if (elementTypeProperties.get(elementType) != null) {
            return elementTypeProperties.get(elementType);
        }
        else {
            return List.of("");
        }
    }

    static Boolean elementTypeHasProperty(String elementType, String propertyType) {
        List<String> propertyTypes = elementTypeProperties.get(elementType);
        if (propertyTypes != null) {
           return propertyTypes.contains(propertyType);
        }
        else {
            return false;
        }
    }
}
