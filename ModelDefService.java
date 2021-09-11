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
      bidTranche,
      mathModel,
      flowLossSegment
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
      dirBranchDirection,
      busStation,
      busKv,
      segLossFlowRatio,
      segMax,
      dirBranchForSeg
   }

   //The properties of an element type
   static final List<PropertyTypeDef> propertyTypeDefDefs =
         List.of(
               //branch
               new PropertyTypeDef(
                     PropertyType.fromBus, List.of(ElementType.branch), "busId"),
               new PropertyTypeDef(
                     PropertyType.toBus, List.of(ElementType.branch), "busId"),
               new PropertyTypeDef(
                     PropertyType.susceptance, List.of(ElementType.branch), "double"),
               new PropertyTypeDef(
                     PropertyType.resistance, List.of(ElementType.branch), "double"),
               //mktEnode
               new PropertyTypeDef(
                     PropertyType.nwEnodeForMktEnode, List.of(ElementType.mktEnode), "nwEnodeId"),
               //nwEnode
               new PropertyTypeDef(
                     PropertyType.busForNwEnode, List.of(ElementType.nwEnode), "busId"),
               new PropertyTypeDef(
                     PropertyType.nwEnodeElecIsland, List.of(ElementType.nwEnode), "string"),
               //pnode
               new PropertyTypeDef(
                     PropertyType.pnodeLoad, List.of(ElementType.pnode), "double"),
               //pnode,mktEnode
               new PropertyTypeDef(
                     PropertyType.factorPnodeMktEnode, List.of(ElementType.pnode, ElementType.mktEnode), "double"),
               //bus
               new PropertyTypeDef(
                     PropertyType.busElecIsland, List.of(ElementType.bus), "string"),
               new PropertyTypeDef(
                     PropertyType.busStation, List.of(ElementType.bus), "string"),
               new PropertyTypeDef(
                     PropertyType.busKv, List.of(ElementType.bus), "string"),
               //offer
               new PropertyTypeDef(
                     PropertyType.tranchePnode, List.of(ElementType.tranche), "pnodeId"),
               new PropertyTypeDef(
                     PropertyType.tranchePrice, List.of(ElementType.tranche), "double"),
               new PropertyTypeDef(
                     PropertyType.trancheLimit, List.of(ElementType.tranche), "double"),
               new PropertyTypeDef(
                     PropertyType.trancheType, List.of(ElementType.tranche), "string"),
               //mktBranch
               new PropertyTypeDef(
                     PropertyType.mktBrLimitFwd, List.of(ElementType.mktBranch), "double"),
               new PropertyTypeDef(
                     PropertyType.mktBrLimitRev, List.of(ElementType.mktBranch), "double"),
               new PropertyTypeDef(
                     PropertyType.branchForMktBranch, List.of(ElementType.mktBranch), "branchId"),
               //----Derived---
               new PropertyTypeDef(
                     PropertyType.weightPnodeBus, List.of(ElementType.pnode, ElementType.bus), "double"),
               new PropertyTypeDef(
                     PropertyType.weightTrancheBus, List.of(ElementType.tranche, ElementType.bus), "double"),
               //dirBranch
               new PropertyTypeDef(
                     PropertyType.fromBus, List.of(ElementType.dirBranch), "busId"),
               new PropertyTypeDef(
                     PropertyType.toBus, List.of(ElementType.dirBranch), "busId"),
               new PropertyTypeDef(
                     PropertyType.dirBranchLimit, List.of(ElementType.dirBranch), "double"),
               new PropertyTypeDef(
                     PropertyType.branchForDirBranch, List.of(ElementType.dirBranch), "branchId"),
               //segment
               new PropertyTypeDef(
                     PropertyType.dirBranchForSeg, List.of(ElementType.flowLossSegment), "dirBranchId"),
               new PropertyTypeDef(
                     PropertyType.segMax, List.of(ElementType.flowLossSegment), "double"),
               new PropertyTypeDef(
                     PropertyType.segLossFlowRatio, List.of(ElementType.flowLossSegment), "double")

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
