import java.util.List;
import java.util.Optional;


public class ModelDefService {

   public enum ElementType {
      branch,
      pnode,
      mktEnode,
      nwEnode,
      bus,
      tranche,
      enOfferTranche,
      bidTranche,
      mathModel
   }

   public enum PropertyType {
      fromBus,
      toBus,
      trancheType,
      tranchePnode,
      factorPnodeMktEnode,
      nwEnodeForMktEnode,
      busForNwEnode,
      trancheLimit,
      tranchePrice,
      pnodeLoad,
      weightTrancheBus,
      susceptance,
      resistance,
      electricalIsland,
      weightPnodeBus,
      mktBrLimitFwd,
      mktBrLimitRev,
      displayName,
      branchForMktBranch
   }


   //The properties of an element type
   static final List<PropertyTypeDef> propertyTypeDefDefs =
         List.of(
               //Inputs
               new PropertyTypeDef(PropertyType.fromBus, List.of("branch"), "busId"),
               new PropertyTypeDef(PropertyType.toBus, List.of("branch"), "busId"),
               new PropertyTypeDef(PropertyType.susceptance, List.of("branch"), "double"),
               new PropertyTypeDef(PropertyType.resistance, List.of("branch"), "double"),
               new PropertyTypeDef(PropertyType.nwEnodeForMktEnode, List.of("mktEnode"), "nwEnodeId"),
               new PropertyTypeDef(PropertyType.busForNwEnode, List.of("nwEnode"), "busId"),
               new PropertyTypeDef(PropertyType.factorPnodeMktEnode, List.of("pnode", "mktEnode"), "double"),
               new PropertyTypeDef(PropertyType.electricalIsland, List.of("bus"), "integer"),
               new PropertyTypeDef(PropertyType.tranchePnode, List.of("tranche"), "pnodeId"),
               new PropertyTypeDef(PropertyType.tranchePrice, List.of("tranche"), "double"),
               new PropertyTypeDef(PropertyType.trancheLimit, List.of("tranche"), "double"),
               new PropertyTypeDef(PropertyType.trancheType, List.of("tranche"), "string"),
               new PropertyTypeDef(PropertyType.pnodeLoad, List.of("pnode"), "double"),
               new PropertyTypeDef(PropertyType.mktBrLimitFwd, List.of("mktBranch"), "double"),
               new PropertyTypeDef(PropertyType.mktBrLimitRev, List.of("mktBranch"), "double"),
               new PropertyTypeDef(PropertyType.branchForMktBranch, List.of("mktBranch"), "branchId"),
               //Derived
               new PropertyTypeDef(PropertyType.weightPnodeBus, List.of("pnode", "bus"), "double"),
               new PropertyTypeDef(PropertyType.weightTrancheBus, List.of("tranche", "bus"), "double")
         );

   //https://stackoverflow.com/questions/41485751/java-8-optional-ifpresent-return-object-orelsethrow-exception
   //https://stackoverflow.com/questions/23773024/functional-style-of-java-8s-optional-ifpresent-and-if-not-present
   public Integer elementIndex(String propertyType, String elementType) {
      return propertyTypeDef(propertyType)
            .map(pt -> pt.elementTypes.indexOf(elementType))
            .orElse(-1);
   }

   //https://x-team.com/blog/using-optional-to-transform-your-java-code/
   public Optional<PropertyTypeDef> propertyTypeDef(String propertyType) {
      return propertyTypeDefDefs
            .stream()
            .filter(e -> e.propertyTypeId.equals(propertyType))
            .findFirst();
   }
}
