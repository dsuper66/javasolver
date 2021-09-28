import java.util.List;
import java.util.Optional;


public class ModelDefService {

   public enum ElementType {
      branch,
      mktBranch,
      dirBranch,
      pnode,
      mktEnode,
      nwEnode,
      bus,
      tranche,
      enOfferTranche,
      resOfferTrancheFir,
      resOfferTrancheSir,
      bidTranche,
      mathModel,
      flowLossSegment,
      riskIsland
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
      busElecIsland,
      nwEnodeElecIsland,
      weightPnodeBus,
      mktBrLimitFwd,
      mktBrLimitRev,
      dirBranchLimit,
      //displayName,
      branchForMktBranch,
      branchForDirBranch,
      dirBranchDirMult,
      busStation,
      busKv,
      segLossFlowRatio,
      segMax,
      dirBranchForSeg,
      sixSecFlag,
      nwEnodeStation,
      riskFlag,
      pnodeRiskIsland
   }

   //Define properties
   static final List<PropertyTypeDef> propertyTypeDefs =
         List.of(
               //branch
               new PropertyTypeDef(PropertyType.fromBus,false, List.of(ElementType.branch)),
               new PropertyTypeDef(PropertyType.toBus,false, List.of(ElementType.branch)),
               new PropertyTypeDef(PropertyType.susceptance,true, List.of(ElementType.branch)),
               new PropertyTypeDef(PropertyType.resistance,true, List.of(ElementType.branch)),
               //mktEnode
               new PropertyTypeDef(PropertyType.nwEnodeForMktEnode,false, List.of(ElementType.mktEnode)),
               //nwEnode
               new PropertyTypeDef(PropertyType.busForNwEnode,false, List.of(ElementType.nwEnode)),
               new PropertyTypeDef(PropertyType.nwEnodeElecIsland,false, List.of(ElementType.nwEnode)),
               new PropertyTypeDef(PropertyType.nwEnodeStation,false, List.of(ElementType.nwEnode)),
               //pnode
               new PropertyTypeDef(PropertyType.pnodeLoad,true, List.of(ElementType.pnode)),
               new PropertyTypeDef(PropertyType.riskFlag,false, List.of(ElementType.pnode)),
               new PropertyTypeDef(PropertyType.pnodeRiskIsland,false, List.of(ElementType.pnode)),
               new PropertyTypeDef(PropertyType.factorPnodeMktEnode,true, List.of(ElementType.pnode, ElementType.mktEnode)),
               //bus
               new PropertyTypeDef(PropertyType.busElecIsland,false, List.of(ElementType.bus)),
               new PropertyTypeDef(PropertyType.busStation,false, List.of(ElementType.bus)),
               new PropertyTypeDef(PropertyType.busKv,false, List.of(ElementType.bus)),
               //offer
               new PropertyTypeDef(PropertyType.tranchePnode,false, List.of(ElementType.tranche)),
               new PropertyTypeDef(PropertyType.tranchePrice,true, List.of(ElementType.tranche)),
               new PropertyTypeDef(PropertyType.trancheLimit,true, List.of(ElementType.tranche)),
               new PropertyTypeDef(PropertyType.trancheType,false, List.of(ElementType.tranche)),
               new PropertyTypeDef(PropertyType.sixSecFlag,false, List.of(ElementType.tranche)),
               //mktBranch
               new PropertyTypeDef(PropertyType.mktBrLimitFwd,true, List.of(ElementType.mktBranch)),
               new PropertyTypeDef(PropertyType.mktBrLimitRev,true, List.of(ElementType.mktBranch)),
               new PropertyTypeDef(PropertyType.branchForMktBranch,false, List.of(ElementType.mktBranch)),
               //----Derived---
               //weights
               new PropertyTypeDef(PropertyType.weightPnodeBus,true, List.of(ElementType.pnode, ElementType.bus)),
               new PropertyTypeDef(PropertyType.weightTrancheBus,true, List.of(ElementType.tranche, ElementType.bus)),
               //dirBranch
               new PropertyTypeDef(PropertyType.fromBus,false, List.of(ElementType.dirBranch)),
               new PropertyTypeDef(PropertyType.toBus,false, List.of(ElementType.dirBranch)),
               new PropertyTypeDef(PropertyType.dirBranchLimit,true, List.of(ElementType.dirBranch)),
               new PropertyTypeDef(PropertyType.branchForDirBranch,false, List.of(ElementType.dirBranch)),
               //segment
               new PropertyTypeDef(PropertyType.dirBranchForSeg,false, List.of(ElementType.flowLossSegment)),
               new PropertyTypeDef(PropertyType.segMax,true, List.of(ElementType.flowLossSegment)),
               new PropertyTypeDef(PropertyType.segLossFlowRatio,true, List.of(ElementType.flowLossSegment))
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
      return propertyTypeDefs
            .stream()
            .filter(e -> e.propertyTypeId.equals(propertyType))
            .findFirst();
   }
}
