import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PreProcessing {

   public static void calculateDerivedProperties(ModelDataService modelDataService) {
      System.out.println("pre-proc: calculateDerivedProperties");

      long startTime = System.currentTimeMillis();
      calcPnodeBusWeights(modelDataService);
      System.out.println("time taken calcPnodeBusWeights:" + (System.currentTimeMillis() - startTime) / 1000.0);

      setupBidsAndOffers(modelDataService);

      //Add mathModel element for the objective
      modelDataService.addElement(ModelDefService.ElementType.mathModel, "mathModel");

      //Filter out zero factors and dead buses and branches that don't have two live buses

      //Assign mktBranch limit to branch

      //Make the tranche name readable

      //Add directional branches
      for (ModelElement branch : modelDataService.getElements(ModelDefService.ElementType.branch)) {

         String fromBusId = modelDataService.getStringValue(ModelDefService.PropertyType.fromBus,branch.elementId);
         String toBusId = modelDataService.getStringValue(ModelDefService.PropertyType.toBus,branch.elementId);
         Map<String,Double> mult = Map.of("FWD",1.0,"REV",-1.0);
         for (String dirString : mult.keySet()) {
            String dirBranchId = branch.elementId + dirString;
            modelDataService.addElement(ModelDefService.ElementType.dirBranch, dirBranchId);
            modelDataService.addProperty(ModelDefService.PropertyType.branchForDirBranch, dirBranchId, branch.elementId);
            modelDataService.addProperty(ModelDefService.PropertyType.dirBranchDirection, dirBranchId, mult.get(dirString));
            modelDataService.addProperty(ModelDefService.PropertyType.fromBus, dirBranchId, fromBusId);
            modelDataService.addProperty(ModelDefService.PropertyType.toBus, dirBranchId, toBusId);

         }
      }
   }

   //Bids and Offers
   private static void setupBidsAndOffers(ModelDataService modelDataService) {
      //enOfferTranche elements as tranche where trancheType = "ENOF"
      //This is effectively just a set of elements that is a subset of the tranche elements
      //the properties are unchanged because the i.d. is the same
      String enOfferType = "ENOF";
      for (String enOfferTrancheId : modelDataService.getElementIds(
            ModelDefService.ElementType.tranche,
            ModelDefService.PropertyType.trancheType,
            enOfferType
      )) {
         modelDataService.addElement(ModelDefService.ElementType.enOfferTranche, enOfferTrancheId);

         //weightTrancheBus... if the tranche has non-zero weight then it clears at the bus
         String pnodeId = modelDataService.getStringValue(
               ModelDefService.PropertyType.tranchePnode, List.of(enOfferTrancheId));
         //Get all bus weights for the pnode and assign them to the OFFER tranche
         for (ElementProperty weightPnodeBusProperty
               : modelDataService.getProperties(
               ModelDefService.PropertyType.weightPnodeBus,
               ModelDefService.ElementType.pnode,
               pnodeId)) {
            String busId = weightPnodeBusProperty.elementIds.get(1);
            modelDataService.addProperty(
                  ModelDefService.PropertyType.weightTrancheBus,
                  List.of(enOfferTrancheId, busId),
                  weightPnodeBusProperty.doubleValue);
         }
         //Display name
         modelDataService.addProperty(
               ModelDefService.PropertyType.displayName,
               enOfferTrancheId,
               pnodeId + "_" + enOfferType);

         //debug... price and limit
         Double limit = modelDataService.getDoubleValue(ModelDefService.PropertyType.trancheLimit, List.of(enOfferTrancheId));
         Double price = modelDataService.getDoubleValue(ModelDefService.PropertyType.tranchePrice, List.of(enOfferTrancheId));

         //System.out.println(">>>" + pnode + " " + limit + " $" + price);
      }

      //Energy bids from pnodeload
      for (ElementProperty pnodeLoadProperty : modelDataService.getProperties(ModelDefService.PropertyType.pnodeLoad)) {

         //System.out.println(">>>" + pnodeLoadProperty.elementIds.get(0) + " " + pnodeLoadProperty.doubleValue);

         //Create the bidTranche element
         //use the pnode id for the bid tranche id
         String pnodeId = pnodeLoadProperty.elementIds.get(0);
         modelDataService.addElement(ModelDefService.ElementType.bidTranche, pnodeId);

         //Create bid tranche properties from the pnode load
         modelDataService.addProperty(
               ModelDefService.PropertyType.trancheLimit, List.of(pnodeId), pnodeLoadProperty.doubleValue);
         Double bidPrice = 20000.0;
         modelDataService.addProperty(
               ModelDefService.PropertyType.tranchePrice, List.of(pnodeId), bidPrice);
         //weightTrancheBus... if the tranche has non-zero weight then it clears at the bus
         //Get all bus weights for the pnode and assign them to the OFFER tranche
         for (ElementProperty weightPnodeBusProperty
               : modelDataService.getProperties(
               ModelDefService.PropertyType.weightPnodeBus,
               ModelDefService.ElementType.pnode,
               pnodeId)) {
            String busId = weightPnodeBusProperty.elementIds.get(1);
            modelDataService.addProperty(
                  ModelDefService.PropertyType.weightTrancheBus,
                  List.of(pnodeId, busId),
                  weightPnodeBusProperty.doubleValue);
         }

      }

   }

   //Pnode to bus weights
   private static void calcPnodeBusWeights(ModelDataService modelDataService) {

      //Sum the factors for each pnode
      System.out.println(LocalDateTime.now() + " start sum factors");
      HashMap<String, Double> sumPnodeFactors = new HashMap<>();
      List<ModelElement> pnodes = modelDataService.getElements(ModelDefService.ElementType.pnode);
      //List<ElementProperty> pnodeEnodeFactors = modelDataService.getProperties("factorPnodeMktEnode");
      //https://stackoverflow.com/questions/33606014/collect-stream-into-a-hashmap-with-lambda-in-java-8
      for (ModelElement pnode : pnodes) {
         sumPnodeFactors.put(
               pnode.elementId,
               modelDataService.getProperties( //get the factor properties and sum
                           ModelDefService.PropertyType.factorPnodeMktEnode,
                           ModelDefService.ElementType.pnode,
                           pnode.elementId)
                     .stream().mapToDouble(p -> p.doubleValue).sum());
      }
      //System.out.println(sumPnodeFactors);

      //enode weight is its factor / sumFactors
      AtomicLong time1 = new AtomicLong();
      AtomicLong time2 = new AtomicLong();
      AtomicLong time3 = new AtomicLong();

      //get the properties factorPnodeMktEnode(pnode,mktEnode) where the pnode matches this pnode
      for (ModelElement pn : pnodes) {
         //for each enode factor for this pnode
         for (ElementProperty property : modelDataService.getProperties(
               ModelDefService.PropertyType.factorPnodeMktEnode,
               ModelDefService.ElementType.pnode,
               pn.elementId)) {
            long startTime = System.currentTimeMillis();
            String mktEnodeId = modelDataService.getElementId(property, "mktEnode");
            time1.addAndGet(System.currentTimeMillis());
            time1.addAndGet(-startTime);

            Double sumFactors = sumPnodeFactors.get(pn.elementId);
            Double enodeFactor = property.doubleValue;
            //uncomment the following to test getDoubleValue
            //modelDataService.getDoubleValue("factorPnodeMktEnode",List.of(pn.elementId, mktEnodeId));

            Double weight =
                  (sumFactors == 0.0) ? 0.0 : //don't div by zero
                        enodeFactor / sumFactors;

            //Get the nwEnodeId for the mktEnode
            String nwEnodeId = modelDataService.getStringValue(
                  ModelDefService.PropertyType.nwEnodeForMktEnode, mktEnodeId);

            //System.out.println("found nwEnodeId " + nwEnodeId);
            //Get the busId for the nwEnodeId
            String busId = modelDataService.getStringValue(
                  ModelDefService.PropertyType.busForNwEnode, nwEnodeId);

            //System.out.println("found busId " + busId);
            modelDataService.addProperty(
                  ModelDefService.PropertyType.weightPnodeBus,
                  List.of(pn.elementId, busId),
                  weight);

            System.out.println(
                  enodeFactor + "," + pn.elementId + "," + mktEnodeId
                  + ",nwEnode(" + nwEnodeId + "),"
                  + "bus(" + busId + ")," + weight);
         }
      }
      //Timing
      //System.out.println(">>>" + (time1.doubleValue() / 1000.0));
      //System.out.println(">>>" + (time2.doubleValue() / 1000.0));
      //System.out.println(">>>" + (time3.doubleValue() / 1000.0));
   }
}
