import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class PreProcessing {

   public static void calculateDerivedProperties(ModelDataService modelDataService) {
      System.out.println("pre-proc: calculateDerivedProperties");

      //Exclude island 0 bus, enode, branch
      excludeIsland(modelDataService);

      long startTime = System.currentTimeMillis();
      calcPnodeBusWeights(modelDataService);
      System.out.println("time taken calcPnodeBusWeights:" + (System.currentTimeMillis() - startTime) / 1000.0);

      setupBidsAndOffers(modelDataService);

      addDirBranches(modelDataService);

      //Add mathModel element for the objective
      modelDataService.addElement(ModelDefService.ElementType.mathModel, "mathModel");

      //Assign mktBranch limit to branch

      //Make the tranche name readable


   }

   //Replace busId with station~kv~busId
   private static void replaceBusId(ModelDataService modelDataService){
      HashMap<String,String> newBusNameForOld = new HashMap<>();
      //For each bus, replace the id
      for (ModelElement bus : modelDataService.getElements(ModelDefService.ElementType.bus)) {
         String busId = bus.elementId;
         String busStation = modelDataService.getStringValue(ModelDefService.PropertyType.busStation, busId);
         String busKv = modelDataService.getStringValue(ModelDefService.PropertyType.busKv, busId);

         //Replace element
         String newBusName = busStation + "~" + busKv + "~" + busId;
         modelDataService.addElement(ModelDefService.ElementType.bus,newBusName);
         modelDataService.removeElement(ModelDefService.ElementType.bus,busId);
         //Map the new name
         newBusNameForOld.put(busId,newBusName);
      }
      //Update busName in NwEnode-Bus
      for (ModelElement nwEnode : modelDataService.getElements(ModelDefService.ElementType.nwEnode)) {
         modelDataService.getProperty(
               ModelDefService.PropertyType.busForNwEnode,nwEnode.elementId).ifPresent(property -> {
               String busId = property.stringValue;
               Optional.ofNullable(newBusNameForOld.get(busId)).ifPresent(newBusName ->
               {
                  modelDataService.addProperty(ModelDefService.PropertyType.busForNwEnode, nwEnode.elementId, newBusName);
                  modelDataService.removeProperty(property);
               });
               }
         );
      }

      //Update busName in Branch-Bus
      for (ModelDefService.PropertyType pType
            : List.of(ModelDefService.PropertyType.fromBus, ModelDefService.PropertyType.toBus)) {
         for (ElementProperty property : modelDataService.getProperties(pType)) {
            String busId = property.stringValue;

         }
      }
   }

   //Add directional branches
   private  static void addDirBranches(ModelDataService modelDataService) {
      for (ModelElement branch : modelDataService.getElements(ModelDefService.ElementType.branch)) {

         String fromBusId = modelDataService.getStringValue(ModelDefService.PropertyType.fromBus, branch.elementId);
         String toBusId = modelDataService.getStringValue(ModelDefService.PropertyType.toBus, branch.elementId);
         //Map<String,Double> mult = Map.of("FWD",1.0,"REV",-1.0);
         Map<String, Double> mult = Map.of("FWD", 1.0);
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

   //Exclude islands
   public static void excludeIsland(ModelDataService modelDataService) {
      List<String> includeList = List.of("2");

      System.out.println("Exclude factorPnodeMktEnode");
      //Remove each pnode-enode factor if in excluded island
      for (ElementProperty property : modelDataService.getProperties(
            ModelDefService.PropertyType.factorPnodeMktEnode)) {

         String mktEnodeId = modelDataService.getElementId(property, ModelDefService.ElementType.mktEnode);
         //Get the nwEnodeId and electrical island for the mktEnode
         String nwEnodeId = modelDataService.getStringValue(
               ModelDefService.PropertyType.nwEnodeForMktEnode, mktEnodeId);
         String elecIsland = modelDataService.getStringValue(
               ModelDefService.PropertyType.nwEnodeElecIsland, nwEnodeId);

         if (!includeList.contains(elecIsland)) {
            System.out.println("removing property: " + property.propertyTypeId + " " + property.elementIds);
            modelDataService.removeProperty(property);
            //No need to remove enodes because they have no associated constraints
         }
      }

      System.out.println("Exclude branch with dead bus");
      //Remove branch and bus if bus is in excluded island
      for (ModelDefService.PropertyType pType
            : List.of(ModelDefService.PropertyType.fromBus, ModelDefService.PropertyType.toBus)) {

         for (ElementProperty property : modelDataService.getProperties(pType)) {
            String busId = property.stringValue;
            //Get the island for the bus
            String elecIsland = modelDataService.getStringValue(
                  ModelDefService.PropertyType.busElecIsland, busId);
            if (!includeList.contains(elecIsland)) {
               //Delete the property, the bus and the branch
               System.out.println(">>> Deleting branch:" +  " and bus:" + busId);
               modelDataService.removeProperty(property);
               modelDataService.removeElement(ModelDefService.ElementType.bus,busId);
               String branchId = modelDataService.getElementId(property, ModelDefService.ElementType.branch);
               modelDataService.removeElement(ModelDefService.ElementType.branch,branchId);
            }
         }
      }

      //Remove bus if in excluded island
      for (ModelElement bus : modelDataService.getElements(ModelDefService.ElementType.bus)) {
         //Get the island for the bus
         String elecIsland = modelDataService.getStringValue(
               ModelDefService.PropertyType.busElecIsland, bus.elementId);
         if (!includeList.contains(elecIsland)) {
            //Delete the bus
            System.out.println(">>> Deleting bus:" + bus.elementId);
            modelDataService.removeElement(ModelDefService.ElementType.bus,bus.elementId);
         }
      }
   }

   //Bids and Offers
   private static void setupBidsAndOffers(ModelDataService modelDataService) {
      //---OFFERS---
      //enOfferTranche elements as tranche where trancheType = "ENOF"
      //This is effectively just a set of elements that is a subset of the tranche elements
      //the properties are unchanged because the i.d. is the same
      String enOfferType = "ENOF";
      for (String enOfferTrancheId
            : modelDataService.getElementIds(
            ModelDefService.ElementType.tranche,
            ModelDefService.PropertyType.trancheType,
            enOfferType
      )) {

         //weightTrancheBus... if the tranche has non-zero weight then it clears at the bus
         String pnodeId = modelDataService.getStringValue(
               ModelDefService.PropertyType.tranchePnode, List.of(enOfferTrancheId));

         //Get bus weights for the pnode and assign them to the OFFER tranche
         //For each bus-weight
         double sumWeights = 0.0; //only add tranch if weights > 0
         for (ElementProperty weightPnodeBusProperty
               : modelDataService.getProperties(
               ModelDefService.PropertyType.weightPnodeBus,
               ModelDefService.ElementType.pnode,
               pnodeId)) {

            //This should be updated so that the bus id is extracted by index from the def
            String busId = weightPnodeBusProperty.elementIds.get(1);

            //Only add the tranch if the pnode has a mapping i.e. bus not removed
            if (weightPnodeBusProperty.doubleValue > 0.0) {
               sumWeights += weightPnodeBusProperty.doubleValue;
               modelDataService.addProperty(
                     ModelDefService.PropertyType.weightTrancheBus,
                     List.of(enOfferTrancheId, busId),
                     weightPnodeBusProperty.doubleValue);
            }
         }
         //Add the tranch if there are weights
         if (sumWeights > 0.0) {
            modelDataService.addElement(ModelDefService.ElementType.enOfferTranche, enOfferTrancheId);
         }


         //debug... price and limit
         Double limit = modelDataService.getDoubleValue(ModelDefService.PropertyType.trancheLimit, List.of(enOfferTrancheId));
         Double price = modelDataService.getDoubleValue(ModelDefService.PropertyType.tranchePrice, List.of(enOfferTrancheId));

         //System.out.println(">>>" + pnode + " " + limit + " $" + price);
      }

      //---BIDS--- Energy bids from pnodeload
      for (ElementProperty pnodeLoadProperty
            : modelDataService.getProperties(ModelDefService.PropertyType.pnodeLoad)) {

         //System.out.println(">>>" + pnodeLoadProperty.elementIds.get(0) + " " + pnodeLoadProperty.doubleValue);
         double pnodeLoad = pnodeLoadProperty.doubleValue * 1.0;
         if (pnodeLoad > 0) {
            //Create the bidTranche element using the pnode id
            String pnodeId = pnodeLoadProperty.elementIds.get(0);
            String tranchId = pnodeId;

            //Get all bus weights for the pnode and assign them to the tranche
            double sumWeights = 0.0;
            for (ElementProperty weightPnodeBusProperty
                  : modelDataService.getProperties(
                  ModelDefService.PropertyType.weightPnodeBus,
                  ModelDefService.ElementType.pnode,
                  pnodeId)) {

               Double busWeight = weightPnodeBusProperty.doubleValue;
               if (busWeight > 0.0) {
                  sumWeights += busWeight;
                  String busId = weightPnodeBusProperty.elementIds.get(1);
                  modelDataService.addProperty(
                        ModelDefService.PropertyType.weightTrancheBus,
                        List.of(pnodeId, busId),
                        busWeight);
               }
            }

            //Add the tranch (if there are non-zero weights)
            if (sumWeights > 0.0) {
               //Add the tranch, using the pnode Id
               modelDataService.addElement(ModelDefService.ElementType.bidTranche, tranchId);
               //Create a tranch from the pnode load
               modelDataService.addProperty(
                     ModelDefService.PropertyType.trancheLimit, List.of(tranchId), pnodeLoad);
               Double bidPrice = 20000.0;
               modelDataService.addProperty(
                     ModelDefService.PropertyType.tranchePrice, List.of(tranchId), bidPrice);
            }
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

      //get the properties factorPnodeMktEnode(pnode,mktEnode)
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

            //Calculate the weight
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

            /*
            System.out.println(
                  enodeFactor + "," + pn.elementId + "," + mktEnodeId
                  + ",nwEnode(" + nwEnodeId + "),"
                  + "bus(" + busId + ")," + weight);

             */
         }
      }

      //Timing
      //System.out.println(">>>" + (time1.doubleValue() / 1000.0));
      //System.out.println(">>>" + (time2.doubleValue() / 1000.0));
      //System.out.println(">>>" + (time3.doubleValue() / 1000.0));
   }
}
