import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PreProcessing {

    static ModelDefService modelDefService = new ModelDefService();

    public static void calculateDerivedProperties(ModelDataService modelDataService) {
        System.out.println("pre-proc: calculateDerivedProperties");

        //Sum pnode factors for each pnode
        HashMap<String, Double> sumPnodeFactors = new HashMap<>();
        List<ModelElement> pnodes = modelDataService.getElements("pnode");
        //List<ElementProperty> pnodeEnodeFactors = modelDataService.getProperties("factorPnodeMktEnode");
        //https://stackoverflow.com/questions/33606014/collect-stream-into-a-hashmap-with-lambda-in-java-8
        pnodes
                .stream()
                .forEach(pn -> sumPnodeFactors.put(
                        pn.elementId,
                        modelDataService.getProperties(
                                "factorPnodeMktEnode","pnode",pn.elementId)
                        .stream()
                        .collect(Collectors.summingDouble(p -> Double.parseDouble(p.value))))

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
        HashMap<String, Double> pnodeEnodeWeights = new HashMap<>();

    }
}
