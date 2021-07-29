import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PreProcessing {

    static ModelDefService modelDefService = new ModelDefService();

    public static void calculateDerivedProperties(ModelDataService modelDataService) {
        System.out.println("pre-proc: calculateDerivedProperties");
        HashMap<String, Double> sumPnodeFactors = new HashMap<>();

        List<ModelElement> pnodes = modelDataService.getElements("pnode");
        List<ElementProperty> pnodeEnodeFactors = modelDataService.getProperties("factorPnodeMktEnode");

        //https://stackoverflow.com/questions/33606014/collect-stream-into-a-hashmap-with-lambda-in-java-8
        pnodes
                .stream()
                .forEach(pn -> sumPnodeFactors.put(
                        pn.elementId,
                        pnodeEnodeFactors
                                .stream()
                                .filter(pef -> pef.elementIds.contains(pn.elementId))
                                .collect(Collectors.summingDouble(pef -> Double.parseDouble(pef.value))
                                )
                        )
                );

        System.out.println(sumPnodeFactors);
    }
}
