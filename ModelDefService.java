import java.util.List;
import java.util.Map;



public class ModelDefService {

    //The properties of an element type
    //e.g. elementTypeProperties['branch'] = ['fromBus', 'toBus', 'susceptance', 'resistance','flowMax'];
    static Map<String, List<String>> elementTypeProperties =
            Map.of(
                    "branch", List.of("fromBus","toBus","susceptance","resistance"),
                    "enode", List.of("enodePnode","enodePnodeFactor"),
                    "pnode", List.of("actualLoad","enodes"),
                    "offerTranche",List.of("tranchePnode","tradeType","trancheLimit","tranchePrice"),
                    "nwEnode",List.of("nwEnodeEnode","nwEnodeBus"),
                    "bus",List.of("electricalIsland"),
                    //derived
                    "enOfferTranche",List.of("toBus","trancheFactor","trancheLimit","tranchePrice"),
                    "enOfferBid",List.of("fromBus","trancheFactor","trancheLimit","tranchePrice")
                    );

    static List<String> elementTypes =
            List.of(
                    "branch",
                    "mktEnode",
                    "pnode",
                    "nwEnode",
                    "bus",
                    "enOfferTranche",
                    "enBidTranche"
            );

    static Map<String, List<String>> propertyTypes =
            Map.of(
                    "fromBusId",List.of("branch"),
                    "toBusId",List.of("branch"),
                    "susceptance",List.of("branch"),
                    "resistance",List.of("branch"),
                    "mapPnodeMktEnode",List.of("pnode","mktEnode"),
                    "mapMktEnodeNwEnode",List.of("mktEnode","nwEnode"),
                    "mapNwEnodeBus",List.of("nwEnode","bus"),
                    "factorPnodeBus",List.of("nwEnode","bus")
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
