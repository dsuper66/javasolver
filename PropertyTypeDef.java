import java.util.List;

public class PropertyTypeDef {
    String propertyTypeId;
    List<String> elementTypes;
    String valueType;

    /*
    public enum PropertyTypeId {
        fromBus,
        toBus,
        susceptance,
        resistance,
        nwEnodeForMktEnode,
        busForNwEnode,
        factorPnodeMktEnode,
        electricalIsland,
        tranchePnode,
        tranchePrice,
        trancheLimit,
        tradeType,
        //Derived
        weightPnodeMktEnode,
        weightPnodeBus
    }*/

    public PropertyTypeDef(
            String propertyTypeId,
            List<String> elementTypes,
            String valueType) {
        this.propertyTypeId = propertyTypeId;
        this.elementTypes = elementTypes;
        this.valueType = valueType;
    }
}
