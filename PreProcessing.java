import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PreProcessing {

    static ModelDefService modelDefService = new ModelDefService();

    public static void calculateDerivedProperties(ModelDataService modelDataService) {
        System.out.println("pre-proc: calculateDerivedProperties");

        //Sum the factors for each pnode
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

        //enode weight is its factor / sumFactors
        pnodes
                .stream()
                .forEach(pn -> modelDataService.getProperties( //get the pnode properties
                        "factorPnodeMktEnode", "pnode", pn.elementId)
                        .stream()
                        .forEach(property -> //for each enode factor property
                        {
                            String mktEnodeId = modelDataService.getElementId(property, "mktEnode");
                            Double sumFactors = sumPnodeFactors.get(pn.elementId);
                            Double enodeFactor = //property.doubleValue;
                            //uncomment the following to test getDoubleValue
                            modelDataService.getDoubleValue("factorPnodeMktEnode",List.of(pn.elementId, mktEnodeId));

                            Double weight =
                                    (sumFactors == 0.0) ? 0.0 : //don't div by zero
                                            enodeFactor / sumFactors;

                            modelDataService.addProperty(
                                    "weightPnodeMktEnode",
                                    List.of(pn.elementId, mktEnodeId),
                                    weight);

                            String nwEnodeId = modelDataService.getStringValue(
                                    "nwEnodeForMktEnode",List.of(mktEnodeId));
                            String busId = modelDataService.getStringValue(
                                    "busForNwEnode",List.of(nwEnodeId));
                            System.out.println(
                                    enodeFactor + "," + pn.elementId + "," + mktEnodeId
                                            + ",nwEnode(" + nwEnodeId + "),"
                                            + "bus(" + busId + ")," + weight);
                        })
                );


    }
}
