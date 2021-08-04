import java.util.List;
import java.util.Optional;


public class ModelDefService {

    public enum ElementType {
        branch,
        pnode,
        mktEnode,
        nwEnode,
        bus,
        tranche
    }

    public enum PropertyType {
        trancheType,
        tranchePnode,
        factorPnodeMktEnode,
        nwEnodeForMktEnode,
        busForNwEnode,
        trancheLimit,
        tranchePrice
    }


    //The properties of an element type
    //e.g. elementTypeProperties['branch'] = ['fromBus', 'toBus', 'susceptance', 'resistance','flowMax'];
    /*
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
            );*/

    static List<PropertyTypeDef> propertyTypeDefDefs =
            List.of(
                    //Inputs
                    new PropertyTypeDef("fromBus", List.of("branch"), "busId"),
                    new PropertyTypeDef("toBus", List.of("branch"), "busId"),
                    new PropertyTypeDef("susceptance", List.of("branch"), "double"),
                    new PropertyTypeDef("resistance", List.of("branch"), "double"),
                    new PropertyTypeDef("nwEnodeForMktEnode", List.of("mktEnode"), "nwEnodeId"),
                    new PropertyTypeDef("busForNwEnode", List.of("nwEnode"), "busId"),
                    new PropertyTypeDef("factorPnodeMktEnode", List.of("pnode", "mktEnode"), "double"),
                    new PropertyTypeDef("electricalIsland", List.of("bus"), "integer"),
                    new PropertyTypeDef("tranchePnode", List.of("tranche"), "pnodeId"),
                    new PropertyTypeDef("tranchePrice", List.of("tranche"), "double"),
                    new PropertyTypeDef("trancheLimit", List.of("tranche"), "double"),
                    new PropertyTypeDef("trancheType", List.of("tranche"), "string"),
                    //Derived
                    //new PropertyTypeDef("weightPnodeMktEnode", List.of("pnode","mktEnode"), "double"),
                    new PropertyTypeDef("weightPnodeBus", List.of("pnode","bus"), "double"),
                    new PropertyTypeDef("weightTrancheBus", List.of("tranche","bus"), "double"),
                    new PropertyTypeDef("trancheBus", List.of("tranche"), "busId")
            );

    /*
    ,
    ,
                    "offerToBusId",List.of("enOfferBid"),
                    "offerTrancheFactor",List.of("enOfferBid"),
                    "offerTrancheLimit",List.of("enOfferBid"),
                    "offerTranchePrice",List.of("enOfferBid")
     */
    /*
    static List<String> getPropertiesForElementType(String elementType) {
        if (elementTypeProperties.get(elementType) != null) {
            return elementTypeProperties.get(elementType);
        }
        else {
            return List.of("");
        }
    }*/

    //https://x-team.com/blog/using-optional-to-transform-your-java-code/

    /*
    static Boolean elementTypeHasProperty(String elementType, String propertyType) {
        List<String> propertyTypes = elementTypeProperties.get(elementType);
        if (propertyTypes != null) {
           return propertyTypes.contains(propertyType);
        }
        else {
            return false;
        }
    }*/

    //https://stackoverflow.com/questions/41485751/java-8-optional-ifpresent-return-object-orelsethrow-exception
    //https://stackoverflow.com/questions/23773024/functional-style-of-java-8s-optional-ifpresent-and-if-not-present
    public Integer elementIndex(String propertyType,String elementType) {
        return propertyTypeDef(propertyType)
                .map(pt -> pt.elementTypes.indexOf(elementType))
                .orElse(-1);
    }

    //https://x-team.com/blog/using-optional-to-transform-your-java-code/
    //public List<String> getPropertyType(String propertyTypeId) {
    //    List<String> optName = Optional.ofNullable(propertyTypes.get(propertyType)).orElse(List.of(""));
    //    return optName;
    //}

    public Optional<PropertyTypeDef> propertyTypeDef(String propertyType) {
        Optional<PropertyTypeDef> opt =
                propertyTypeDefDefs
                        .stream()
                        .filter(e -> e.propertyTypeId.equals(propertyType))
                        .findFirst();
        return opt;
    }
}
