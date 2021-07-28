import java.util.List;
import java.util.Map;
import java.util.Optional;


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

    static List<PropertyType> propertyTypes =
            List.of(
                    //Inputs
                    new PropertyType("fromBus", List.of("branch"), "id"),
                    new PropertyType("toBus", List.of("branch"), "id"),
                    new PropertyType("susceptance", List.of("branch"), "double"),
                    new PropertyType("resistance", List.of("branch"), "double"),
                    new PropertyType("nwEnodeForMktEnode", List.of("mktEnode"), "id"),
                    new PropertyType("busForNwEnode", List.of("nwEnode"), "id"),
                    new PropertyType("factorPnodeEnode", List.of("pnode", "mktEnode"), "double"),
                    new PropertyType("electricalIsland", List.of("bus"), "integer"),
                    new PropertyType("tranchePnode", List.of("tranche"), "id"),
                    new PropertyType("tranchePrice", List.of("tranche"), "double"),
                    new PropertyType("trancheLimit", List.of("tranche"), "double"),
                    new PropertyType("tradeType", List.of("tranche"), "string"),
                    //Derived
                    new PropertyType("factorPnodeBus", List.of("pnode","bus"), "double"),
                    new PropertyType("factorTrancheBus", List.of("tranche","bus"), "double")
            );

    /*
    ,
    ,
                    "offerToBusId",List.of("enOfferBid"),
                    "offerTrancheFactor",List.of("enOfferBid"),
                    "offerTrancheLimit",List.of("enOfferBid"),
                    "offerTranchePrice",List.of("enOfferBid")
     */
    static List<String> getPropertiesForElementType(String elementType) {
        if (elementTypeProperties.get(elementType) != null) {
            return elementTypeProperties.get(elementType);
        }
        else {
            return List.of("");
        }
    }

    //https://x-team.com/blog/using-optional-to-transform-your-java-code/

    static Boolean elementTypeHasProperty(String elementType, String propertyType) {
        List<String> propertyTypes = elementTypeProperties.get(elementType);
        if (propertyTypes != null) {
           return propertyTypes.contains(propertyType);
        }
        else {
            return false;
        }
    }

    //https://x-team.com/blog/using-optional-to-transform-your-java-code/
    //public List<String> getPropertyType(String propertyTypeId) {
    //    List<String> optName = Optional.ofNullable(propertyTypes.get(propertyType)).orElse(List.of(""));
    //    return optName;
    //}

    public Optional<PropertyType> getPropertyType(String propertyTypeId) {
        Optional<PropertyType> opt =
                propertyTypes
                        .stream()
                        .filter(e -> e.propertyTypeId.equals(propertyTypeId))
                        .findFirst();
        return opt;
    }
}
