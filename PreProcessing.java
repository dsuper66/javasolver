import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class PreProcessing {

   public static void doPreProcessing(ModelDataService modelDataService) {
      System.out.println("pre-proc: doPreProcessing");

      //Exclude bus, enode, branch
      excludedData(modelDataService);

      updateBusName(modelDataService);

      long startTime = System.currentTimeMillis();
      prepPnodes(modelDataService);
      System.out.println("time taken calcPnodeBusWeights:" + (System.currentTimeMillis() - startTime) / 1000.0);

      prepBidsAndOffers(modelDataService);

      prepBranches(modelDataService);

      //Add mathModel element for the objective
      modelDataService.addElement(ModelDefService.ElementType.mathModel, "mathModel");

   }

   //Replace busId with station~kv~busId
   private static void updateBusName(ModelDataService modelDataService) {
      HashMap<String, String> newBusIdForOld = new HashMap<>();
      //For each bus, replace the id
      for (ModelElement bus : modelDataService.getElements(ModelDefService.ElementType.bus)) {
         //Make the new Id
         String busId = bus.elementId;
         String busStation = modelDataService.getStringValue(ModelDefService.PropertyType.busStation, busId);
         String busKv = modelDataService.getStringValue(ModelDefService.PropertyType.busKv, busId);
         String newBusId = busStation + "~" + busKv + "~" + busId;

         //Replace element
         modelDataService.addElement(ModelDefService.ElementType.bus, newBusId);
         modelDataService.removeElement(ModelDefService.ElementType.bus, busId);
         //Map the new name
         newBusIdForOld.put(busId, newBusId);

         //Update id in bus-island property
         modelDataService.getProperty(
               ModelDefService.PropertyType.busElecIsland, busId).ifPresent(property -> {
            modelDataService.addProperty(ModelDefService.PropertyType.busElecIsland, newBusId, property.stringValue);
            modelDataService.removeProperty(property);
         });

      }
      //Update busName in NwEnode-Bus
      for (ModelElement nwEnode : modelDataService.getElements(ModelDefService.ElementType.nwEnode)) {
         renameProperty(modelDataService, nwEnode.elementId, ModelDefService.PropertyType.busForNwEnode, newBusIdForOld);
         /*
         modelDataService.getProperty(
               ModelDefService.PropertyType.busForNwEnode,nwEnode.elementId).ifPresent(property -> {
               String busId = property.stringValue;
               Optional.ofNullable(newBusIdForOld.get(busId)).ifPresent(newBusName ->
               {
                  modelDataService.addProperty(ModelDefService.PropertyType.busForNwEnode, nwEnode.elementId, newBusName);
                  modelDataService.removeProperty(property);
               });
               }
         );*/
      }

      //Update busName in Branch-Bus
      for (ModelElement branch : modelDataService.getElements(ModelDefService.ElementType.branch)) {
         for (ModelDefService.PropertyType pType
               : List.of(ModelDefService.PropertyType.fromBus, ModelDefService.PropertyType.toBus)) {

            renameProperty(modelDataService, branch.elementId, pType, newBusIdForOld);
            /*
            modelDataService.getProperty(
                  pType, branch.elementId).ifPresent(property -> {
               String busId = property.stringValue;
               Optional.ofNullable(newBusIdForOld.get(busId)).ifPresent(newBusName -> {
                  System.out.println(">>>replace " + pType.name() + ":" + busId + "  with:" + newBusName);
                  modelDataService.addProperty(pType, branch.elementId, newBusName);
                  modelDataService.removeProperty(property);
               });
            });*/
         }
      }
   }

   //Rename property, e.g., rename BusId
   private static void renameProperty(ModelDataService modelDataService,
                                      String elementId,
                                      ModelDefService.PropertyType pType,
                                      HashMap<String, String> mapOldNew) {
      modelDataService.getProperty(
            pType, elementId).ifPresent(property -> {
         String oldId = property.stringValue;
         Optional.ofNullable(mapOldNew.get(oldId)).ifPresent(newId -> {
            System.out.println(">>>replace " + pType.name() + " of " + elementId + ":" + oldId + "  with:" + newId);
            modelDataService.removeProperty(property);
            modelDataService.addProperty(pType, elementId, newId);
         });
      });
   }

   //Add directional branches
   private static void prepBranches(ModelDataService modelDataService) {

      //Get branch max from mkt branch
      HashMap<String,Double> branchFwdMaxForBranch = new HashMap<>();
      for (ModelElement mktBranch : modelDataService.getElements(ModelDefService.ElementType.mktBranch)){
         String branchId = modelDataService.getStringValue(
               ModelDefService.PropertyType.branchForMktBranch,mktBranch.elementId);

         Double branchFwdMax = modelDataService.getDoubleValue(
               ModelDefService.PropertyType.mktBrLimitFwd,mktBranch.elementId);
         branchFwdMaxForBranch.put(branchId,branchFwdMax);
      }

      //Create a fwd and rev dirBranch for each branch
      for (ModelElement branch : modelDataService.getElements(ModelDefService.ElementType.branch)) {

         String fromBusId = modelDataService.getStringValue(ModelDefService.PropertyType.fromBus, branch.elementId);
         String toBusId = modelDataService.getStringValue(ModelDefService.PropertyType.toBus, branch.elementId);
         Double resistance = modelDataService.getDoubleValue(ModelDefService.PropertyType.resistance, branch.elementId);
         resistance = resistance / 100.0 / 100.0; //resistance in the file is percent per-unit

         Map<String,Double> mult = Map.of("FWD",1.0,"REV",-1.0);
         //Map<String, Double> mult = Map.of("FWD", 1.0);
         for (String dirString : mult.keySet()) {
            //dirBranchId
            String dirBranchId = branch.elementId + dirString;
            modelDataService.addElement(ModelDefService.ElementType.dirBranch, dirBranchId);
            //Properties
            modelDataService.addProperty(ModelDefService.PropertyType.branchForDirBranch, dirBranchId, branch.elementId);
            Double dirMult = mult.get(dirString);
            modelDataService.addProperty(ModelDefService.PropertyType.dirBranchDirMult, dirBranchId, dirMult);
            //From/to bus are reversed for the reverse branch
            modelDataService.addProperty(ModelDefService.PropertyType.fromBus, dirBranchId,
                  (dirMult == 1.0) ? fromBusId : toBusId);
            modelDataService.addProperty(ModelDefService.PropertyType.toBus, dirBranchId,
                  (dirMult == 1.0) ? toBusId : fromBusId);

            //Add segments
            Double flowMax = branchFwdMaxForBranch.get(branch.elementId);
            if (flowMax != null) {
               int totalSegs = 3;

               double segMax = flowMax / totalSegs;
               double flowAtStartOfSegment = 0.0;
               double flowAtEndOfSegment = segMax;
               for (int segNum = 1; segNum <= totalSegs; segNum++) {
                  //Add the segment
                  String segId = dirBranchId + "~seg" + segNum;
                  modelDataService.addElement(ModelDefService.ElementType.flowLossSegment, segId);
                  //Seg max
                  modelDataService.addProperty(ModelDefService.PropertyType.segMax, segId, segMax);
                  //Loss-flow ratio
                  //Loss = P^2 * R
                  Double lossAtStart = Math.pow(flowAtStartOfSegment, 2) * resistance;
                  Double lossAtEnd = Math.pow(flowAtEndOfSegment, 2) * resistance;
                  Double lossFlowRatio = (lossAtEnd - lossAtStart) / segMax;
                  modelDataService.addProperty(ModelDefService.PropertyType.segLossFlowRatio, segId, lossFlowRatio);
                  System.out.printf("added seg: %s  r: %1.4f flow: %1.2f loss1: %1.2f loss2: %1.2f ratio: %1.4f%n",
                        segId, resistance, segMax, lossAtStart, lossAtEnd, lossFlowRatio);
                  flowAtStartOfSegment += segMax;
                  flowAtEndOfSegment += segMax;

                  //Map seg to branch
                  modelDataService.addProperty(ModelDefService.PropertyType.dirBranchForSeg, segId, dirBranchId);
                  System.out.printf("segId: %s, dirBranchId: %s\n", segId, dirBranchId);
               }
            }
         }
      }
   }

   //Exclude islands
   public static void excludedData(ModelDataService modelDataService) {
      List<String> includeIsland = List.of("1");
      List<String> includeRiskIslands = List.of("NI");
      List<String> includeStations = List.of("ARA","WRK");

      System.out.println("Exclude riskIsland");
      //Remove each riskIsland if not included
      for (ModelElement modelElement :
            modelDataService.getElements(ModelDefService.ElementType.riskIsland)) {

         String riskIslandId = modelElement.elementId;
         if (!includeRiskIslands.contains(riskIslandId)) {
            modelDataService.removeElement(ModelDefService.ElementType.riskIsland,riskIslandId);
         }
      }

      System.out.println("Exclude factorPnodeMktEnode");
      //Remove each pnode-enode factor if not included
      for (ElementProperty property : modelDataService.getProperties(
            ModelDefService.PropertyType.factorPnodeMktEnode)) {

         //Get the mktEnode for the pnode
         String mktEnodeId = modelDataService.getElementId(property, ModelDefService.ElementType.mktEnode);
         //Get the network details for the mktEnode to check for inclusion
         String nwEnodeId = modelDataService.getStringValue(
               ModelDefService.PropertyType.nwEnodeForMktEnode, mktEnodeId);
         String elecIsland = modelDataService.getStringValue(
               ModelDefService.PropertyType.nwEnodeElecIsland, nwEnodeId);
         String station = modelDataService.getStringValue(
               ModelDefService.PropertyType.nwEnodeStation, nwEnodeId);

         //if (!includeIsland.contains(elecIsland)) {
         if (!includeStations.contains(station)) {
            //System.out.println("removing property: " + property.propertyTypeId + " " + property.elementIds);
            modelDataService.removeProperty(property);
            //No need to remove enodes because they have no associated constraints
         }
      }

      //If a pnode has no factors then remove its tranches
      //(specifically to remove reserve tranches, because energy removed by removal of mapping)
      for (ModelElement pnode : modelDataService.getElements(ModelDefService.ElementType.pnode)) {
         List<ElementProperty> pnodeMktEnodeFactors =
               modelDataService.getProperties(
                     ModelDefService.PropertyType.factorPnodeMktEnode, ModelDefService.ElementType.pnode, pnode.elementId);
         if (pnodeMktEnodeFactors.isEmpty()) {
            for (String tranchId
                  : modelDataService.getElementIds(
                  ModelDefService.ElementType.tranche,
                  ModelDefService.PropertyType.tranchePnode,
                  pnode.elementId)) {
               modelDataService.removeElement(ModelDefService.ElementType.tranche, tranchId);
            }
         }
      }

      System.out.println("Exclude branch with bus not in included island/station");
      //Remove branch and bus if bus is not included
      for (ModelDefService.PropertyType pType
            : List.of(ModelDefService.PropertyType.fromBus, ModelDefService.PropertyType.toBus)) {

         for (ElementProperty property : modelDataService.getProperties(pType)) {
            String busId = property.stringValue;
            //Get the details for the bus
            String elecIsland = modelDataService.getStringValue(ModelDefService.PropertyType.busElecIsland, busId);
            String station = modelDataService.getStringValue(ModelDefService.PropertyType.busStation, busId);
            //if (!includeIsland.contains(elecIsland)) {
            if (!includeStations.contains(station)) {
               //Delete the property, the bus and the branch
               //System.out.println(">>> Deleting branch:" + " and bus:" + busId + " (island " + elecIsland + ")");
               modelDataService.removeProperty(property);
               modelDataService.removeElement(ModelDefService.ElementType.bus, busId);
               String branchId = modelDataService.getElementId(property, ModelDefService.ElementType.branch);
               modelDataService.removeElement(ModelDefService.ElementType.branch, branchId);
            }
         }
      }

      //Remove bus if in excluded island, or not map to station
      for (ModelElement bus : modelDataService.getElements(ModelDefService.ElementType.bus)) {
         //Get the island for the bus
         String busId = bus.elementId;
         //String elecIsland = modelDataService.getStringValue(ModelDefService.PropertyType.busElecIsland, busId);
         String station = modelDataService.getStringValue(ModelDefService.PropertyType.busStation, busId);
         //if (!includeIsland.contains(elecIsland)) {
         if (!includeStations.contains(station)) {
            //Delete the bus
            //System.out.println(">>> Deleting bus:" + bus.elementId + " (island " + elecIsland + ")");
            modelDataService.removeElement(ModelDefService.ElementType.bus, bus.elementId);
         }
      }
   }

   //Bids and Offers
   private static void prepBidsAndOffers(ModelDataService modelDataService) {
      //---OFFERS---
      //enOfferTranche elements as tranche where trancheType = "ENOF"
      //This is effectively just a set of elements that is a subset of the tranche elements
      //the properties are unchanged because the i.d. is the same
      for (String offerType : new String[]{"ENOF", "PLRO", "TWRO"}) {
         for (String offerTrancheId : modelDataService.getElementIds(
               ModelDefService.ElementType.tranche,
               ModelDefService.PropertyType.trancheType,
               offerType
         )) {

            //Get the pnodeId
            String pnodeId = modelDataService.getStringValue(
                  ModelDefService.PropertyType.tranchePnode, offerTrancheId);

            System.out.println("processing offer:" + offerTrancheId);

            //enOfferTranche
            if (offerType.equals("ENOF")) {
               //Get bus weights for the pnode and assign them to the tranche
               //For each bus-weight
               double sumWeights = 0.0; //only add tranch if weights > 0
               for (ElementProperty weightPnodeBusProperty
                     : modelDataService.getProperties(
                     ModelDefService.PropertyType.weightPnodeBus,
                     ModelDefService.ElementType.pnode,
                     pnodeId)) {

                  //This has been updated so that the bus id is extracted by index from the property def
                  //String busId = weightPnodeBusProperty.elementIds.get(1);
                  String busId = modelDataService.getElementId(weightPnodeBusProperty, ModelDefService.ElementType.bus);

                  //Only add the energy tranche-bus weight PROPERTY if not zero
                  if (weightPnodeBusProperty.doubleValue > 0.0) {
                     sumWeights += weightPnodeBusProperty.doubleValue;
                     modelDataService.addProperty(
                           ModelDefService.PropertyType.weightTrancheBus,
                           List.of(offerTrancheId, busId),
                           weightPnodeBusProperty.doubleValue);
                  }
               }
               //Add the energy tranche ELEMENT if there are weights
               //offerTrancheId is the link to the original properties
               if (sumWeights > 0.0) {
                  modelDataService.addElement(ModelDefService.ElementType.enOfferTranche, offerTrancheId);
               }
            }

            //FIR and SIR
            //Add fir and sir elements and properties (price, limit, island), remove the existing tranche
            String sixSecFlag = modelDataService.getStringValue(ModelDefService.PropertyType.sixSecFlag, offerTrancheId);
            Double tranchePrice = modelDataService.getDoubleValue(ModelDefService.PropertyType.tranchePrice, offerTrancheId);
            Double trancheLimit = modelDataService.getDoubleValue(ModelDefService.PropertyType.trancheLimit, offerTrancheId);
            String trancheRiskIsland = modelDataService.getStringValue(ModelDefService.PropertyType.pnodeRiskIsland, pnodeId);
            if (offerType.equals("PLRO") || offerType.equals("TWRO")) {
               String newOfferTrancheId = "";
               if (sixSecFlag.equals("1")) { //FIR
                  newOfferTrancheId = offerTrancheId.replace(offerType,offerType.charAt(0) + "FIR");
                  modelDataService.addElement(ModelDefService.ElementType.firOfferTranche,newOfferTrancheId);
               }
               else { //SIR
                  newOfferTrancheId = offerTrancheId.replace(offerType,offerType.charAt(0) + "SIR");
                  modelDataService.addElement(ModelDefService.ElementType.sirOfferTranche,newOfferTrancheId);
               }
               //Add properties for new id

               //Remove properties for the old offer id
            }

            //debug... price and limit
            Double limit = modelDataService.getDoubleValue(ModelDefService.PropertyType.trancheLimit, List.of(offerTrancheId));
            Double price = modelDataService.getDoubleValue(ModelDefService.PropertyType.tranchePrice, List.of(offerTrancheId));
            System.out.println("offer>>>" + offerTrancheId + " " + sixSecFlag + " " + limit + " $" + price);
         }
      }

      //---BIDS--- Energy bids from pnodeload
      for (ElementProperty pnodeLoadProperty
            : modelDataService.getProperties(ModelDefService.PropertyType.pnodeLoad)) {

         //System.out.println(">>>" + pnodeLoadProperty.elementIds.get(0) + " " + pnodeLoadProperty.doubleValue);
         double pnodeLoad = pnodeLoadProperty.doubleValue * 1.0;
         if (pnodeLoad > 0) {
            //Create the bidTranche element using the pnode id
            //String pnodeId = pnodeLoadProperty.elementIds.get(0);
            String pnodeId = modelDataService.getElementId(pnodeLoadProperty, ModelDefService.ElementType.pnode);
            String tranchId = pnodeId + "~bid";

            //Get all bus weights for the pnode and assign them to the tranche
            double sumWeights = 0.0;
            for (ElementProperty weightPnodeBusProperty : modelDataService.getProperties(
                  ModelDefService.PropertyType.weightPnodeBus,
                  ModelDefService.ElementType.pnode,
                  pnodeId)) {

               Double busWeight = weightPnodeBusProperty.doubleValue;
               if (busWeight > 0.0) {
                  sumWeights += busWeight;
                  String busId = weightPnodeBusProperty.elementIds.get(1);
                  modelDataService.addProperty(
                        ModelDefService.PropertyType.weightTrancheBus,
                        List.of(tranchId, busId),
                        busWeight);
               }
            }

            //Add the tranch (if there are non-zero weights)
            if (sumWeights > 0.0) {
               //Add the tranch element
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
   private static void prepPnodes(ModelDataService modelDataService) {

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
      //For each pnode
      for (ModelElement pn : pnodes) {
         //Pnode can potentially map to more than one bus
         //Bus weight is sum of enode weights
         HashMap<String, Double> busWeightMap = new HashMap<>();

         //for each enode for this pnode
         for (ElementProperty property : modelDataService.getProperties(
               ModelDefService.PropertyType.factorPnodeMktEnode,
               ModelDefService.ElementType.pnode,
               pn.elementId)) {

            long startTime = System.currentTimeMillis();
            String mktEnodeId = modelDataService.getElementId(property, "mktEnode");
            time1.addAndGet(System.currentTimeMillis());
            time1.addAndGet(-startTime);

            //Calculate the pnode weight for this factor
            Double sumFactors = sumPnodeFactors.get(pn.elementId);
            Double enodeFactor = property.doubleValue;
            //uncomment the following to test getDoubleValue
            //modelDataService.getDoubleValue("factorPnodeMktEnode",List.of(pn.elementId, mktEnodeId));
            Double thisWeight =
                  (sumFactors == 0.0) ? 0.0 : //don't div by zero
                        enodeFactor / sumFactors;

            //Get the nwEnodeId for the mktEnode
            String nwEnodeId = modelDataService.getStringValue(
                  ModelDefService.PropertyType.nwEnodeForMktEnode, mktEnodeId);

            //Get the busId for the nwEnodeId
            String busId = modelDataService.getStringValue(
                  ModelDefService.PropertyType.busForNwEnode, nwEnodeId);

            busWeightMap.put(busId,
                  Optional.ofNullable(busWeightMap.get(busId))
                        .map(existingWeight -> existingWeight + thisWeight)
                        .orElse(thisWeight));
         }
         //Assign the total weights
         for (var busWeightEntry : busWeightMap.entrySet()) {
            modelDataService.addProperty(
                  ModelDefService.PropertyType.weightPnodeBus,
                  List.of(pn.elementId, busWeightEntry.getKey()),
                  busWeightEntry.getValue());

            System.out.println(
                  "pnode," + pn.elementId + ",bus(" + busWeightEntry.getKey()
                  + "),bus weight," + busWeightEntry.getValue());
         }
      }

      //Timing
      //System.out.println(">>>" + (time1.doubleValue() / 1000.0));
      //System.out.println(">>>" + (time2.doubleValue() / 1000.0));
      //System.out.println(">>>" + (time3.doubleValue() / 1000.0));
   }
}
