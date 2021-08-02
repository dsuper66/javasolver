import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PreProcessing {

    static ModelDefService modelDefService = new ModelDefService();

    public static void calculateDerivedProperties(ModelDataService modelDataService) {
        System.out.println("pre-proc: calculateDerivedProperties");

        //Pnode to bus weights

        //Sum the factors for each pnode
        System.out.println(LocalDateTime.now() + " start sum factors");
        HashMap<String, Double> sumPnodeFactors = new HashMap<>();
        List<ModelElement> pnodes = modelDataService.getElements("pnode");
        //List<ElementProperty> pnodeEnodeFactors = modelDataService.getProperties("factorPnodeMktEnode");
        //https://stackoverflow.com/questions/33606014/collect-stream-into-a-hashmap-with-lambda-in-java-8
        pnodes
                .stream()
                .forEach(pn -> sumPnodeFactors.put(
                        pn.elementId,
                        modelDataService.getProperties( //get the factor properties
                                "factorPnodeMktEnode", "pnode", pn.elementId)
                                .stream()
                                .collect(Collectors.summingDouble(p -> p.doubleValue)))

                        /*
                        pnodeEnodeFactors //get the factors for this pnode and sum them
                                .stream()
                                .filter(pef
                                        -> pef.elementIds //elements are pnode,enode... match on the pnode index
                                        .get(modelDefService.elementIndex("factorPnodeMktEnode","pnode"))
                                        .equals(pn.elementId))
                                .collect(Collectors.summingDouble(pef -> Double.parseDouble(pef.value))
                                )
                        )*/
                );
        System.out.println(sumPnodeFactors);
        System.out.println(LocalDateTime.now() + " done sum factors");

        System.out.println(LocalDateTime.now() + " start calc weights");
        //enode weight is its factor / sumFactors


        //AtomicLong startTime1 = new AtomicLong(System.nanoTime());
        //AtomicLong endTime1 = new AtomicLong(System.nanoTime());
        AtomicLong time1 = new AtomicLong();
        AtomicLong time2 = new AtomicLong();
        AtomicLong time3 = new AtomicLong();

        pnodes
                .stream()
                .forEach(pn -> modelDataService.getProperties( //get the pnode properties
                        "factorPnodeMktEnode", "pnode", pn.elementId)
                        .stream()
                        .forEach(property -> //for each enode factor property
                        {

                            Long startTime = System.currentTimeMillis();

                            String mktEnodeId = modelDataService.getElementId(property, "mktEnode");

                            time1.addAndGet(System.currentTimeMillis());
                            time1.addAndGet(-startTime);


                            Double sumFactors = sumPnodeFactors.get(pn.elementId);
                            Double enodeFactor = property.doubleValue;
                                    //uncomment the following to test getDoubleValue
                                    //modelDataService.getDoubleValue("factorPnodeMktEnode",
                                    //        List.of(pn.elementId, mktEnodeId));

                            Double weight =
                                    (sumFactors == 0.0) ? 0.0 : //don't div by zero
                                            enodeFactor / sumFactors;

                            startTime = System.currentTimeMillis();
                            String nwEnodeId = modelDataService.getStringValue(
                                    "nwEnodeForMktEnode", List.of(mktEnodeId));
                            time2.addAndGet(System.currentTimeMillis());
                            time2.addAndGet(-startTime);

                            startTime = System.currentTimeMillis();
                            String busId = modelDataService.getStringValue(
                                    "busForNwEnode", List.of(nwEnodeId));
                            time3.addAndGet(System.currentTimeMillis());
                            time3.addAndGet(-startTime);

                            modelDataService.addProperty(
                                    "weightPnodeBus",
                                    List.of(pn.elementId, busId),
                                    weight);

                            System.out.println(
                                    enodeFactor + "," + pn.elementId + "," + mktEnodeId
                                            + ",nwEnode(" + nwEnodeId + "),"
                                            + "bus(" + busId + ")," + weight);
                        })
                );
        System.out.println(">>>" + (time1.doubleValue()/1000.0));
        System.out.println(">>>" + (time2.doubleValue()/1000.0));
        System.out.println(">>>" + (time3.doubleValue()/1000.0));
        System.out.println(LocalDateTime.now() + " done calc weights");
    }
}
