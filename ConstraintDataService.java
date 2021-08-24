

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConstraintDataService {
   //Defs are read in
   public static List<ConstraintComp> constraintComps = new ArrayList<>();
   public static List<ConstraintDef> constraintDefs = new ArrayList<>();

   //Created from defs
   public final List<Constraint> constraints = new ArrayList<>();
   public Constraint objectiveFn = new Constraint(
         "", "", "", "", 0.0);
   public final List<String> varIdList = new ArrayList<>(); //Order is important
   public final HashMap<String, Double> upperBounds = new HashMap<>();
   public final HashMap<String, Integer> varIndexes = new HashMap<>();

   //public final List<VarFactor> varFactors = new ArrayList<>();

   public void readConstraints() {
      String dir = "/Users/davidbullen/java/";
      String defFile = "constraint-defs2.json";
      String compFile = "constraint-comps2.json";
      //https://attacomsian.com/blog/jackson-read-json-file
      //ObjectMapper mapper = new ObjectMapper();
      try {
         //https://stackoverflow.com/questions/29965764/how-to-parse-json-file-with-gson
         Gson gson = new Gson();

         JsonReader reader = new JsonReader(new FileReader(dir + defFile));
         //final List<ConstraintDef> constraintDefs = Arrays.asList(gson.fromJson(reader, ConstraintDef[].class));
         constraintDefs = Arrays.asList(gson.fromJson(reader, ConstraintDef[].class));
         //constraintDefs.forEach(cd -> System.out.println(">>>constraint def:" + cd.constraintType));

         reader = new JsonReader(new FileReader(dir + compFile));
         //final List<ConstraintComp> constraintComps = Arrays.asList(gson.fromJson(reader, ConstraintComp[].class));
         constraintComps = Arrays.asList(gson.fromJson(reader, ConstraintComp[].class));
         //constraintComps.forEach(cc -> System.out.println(">>>constraint comp:" + cc.constraintType));

      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public void processConstraintDefs(ModelDataService modelDataService) {
      //noinspection MismatchedReadAndWriteOfArray
      final String[] msg = {""};
      //Constraint Defs
      for (ConstraintDef constraintDef : constraintDefs) {
         System.out.println(">>>ConstraintDef:" + constraintDef.constraintType);
         //===Define the Constraint===
         //Get the parent elements that match the ConstraintDef elementType
         //e.g. for node balance, do each bus
         for (ModelElement parentElement
               : modelDataService.getElements(constraintDef.elementType)) {

            System.out.println(">>>con:" + constraintDef.constraintType
                               + " " + parentElement.elementType + " " + parentElement.elementId);
            boolean createTheConstraint = true; //Constraint is not created if it can be replaced by bounds on the Var

            //LE or EQ
            String inEquality = constraintDef.inEquality;

            //RHS (from parent or value)
            Double rhsValue;
            //RHS from parent
            //check if RHS is a property of the parent element, e.g., brLimit for branch
            if (!constraintDef.rhsProperty.equals("")) {
               rhsValue =
                     modelDataService.getDoubleValue(
                           constraintDef.rhsProperty, List.of(parentElement.elementId));
            } else { //RHS is from specified value
               rhsValue = constraintDef.rhsValue;
            }

            String constraintId = String.format("con_%s.%s",
                  constraintDef.constraintType, parentElement.elementId);
            final String[] constraintString = {String.format("%s\n", constraintId)};

            HashMap<String,Double> varFactorVals = new HashMap<>();

            //Check for Var Factor from parent
            if (!constraintDef.varType.equals("")) {

               //If this constraint has no components
               //then the limit on this var can be applied directly to the bounds of the variable
               //and no constraint is added
               String varId = makeVarId(parentElement.elementId, constraintDef.varType);
               if (constraintComps.stream()
                     .noneMatch(cc -> cc.constraintType.equals(constraintDef.constraintType))) {
                  //Add the variable with bounds
                  //***For now ASSUME it is LE***
                  //addVariable(parentElement.elementId, constraintDef.varType, 0.0, rhsValue);
                  upperBounds.put(varId,rhsValue); //assumes var will be added by a constraint
                  createTheConstraint = false;
               } else { //Add VarFactor to the constraint
                  Double varFactor = constraintDef.factorValue;
                  varFactorVals.put(varId,varFactor);
                  //String variableId = addVariable(parentElement.elementId, constraintDef.varType);
                  //Add the VarFactor
                  //Double varFactor = constraintDef.factorValue;
                  //setVarFactor(variableId, constraintId, varFactor);
                  constraintString[0] += String.format("+ %1.2f * %s\n", varFactor, varId);
               }
            }

            //--Components--
            if (createTheConstraint) {
               //Create the constraint
               Constraint newConstraint = new Constraint(
                     constraintId,
                     constraintDef.constraintType,
                     parentElement.elementId,
                     inEquality,
                     rhsValue);
               if (parentElement.elementId.equals("mathModel")) {
                  this.objectiveFn = newConstraint;
               }
               else {
                  constraints.add(newConstraint);
               }

               //Set the Var Factors which define the LHS
               for (ConstraintComp cc : constraintComps) {
                  if (cc.constraintType.equals(constraintDef.constraintType)) {
                     //Get component elements where their elementType matches AND their property
                     //as specified by propertyMap matches the constraintDef parent element

                     //elements where elementType matches constraint component
                     //then check for property map from parent to child, or child to parent, or to self
                     for (ModelElement childMatchingType : modelDataService.getElements(cc.elementType)) {
                        if ((
                              //e.g. all bids and offers are in objective constraint
                              cc.propertyMap.equals("all")
                              //parentElement matches constraintComp.propertyMap
                              //e.g. nodeBal... propertyMap is fromBus,
                              // child is dirBranch matching parent bus
                              || modelDataService.getStringValue(
                                    cc.propertyMap, childMatchingType.elementId).equals
                                    (parentElement.elementId)
                              //or map via non-zero factor, e.g., weightTrancheBus(t,b) > 0
                              || modelDataService.getDoubleValue(
                                    cc.propertyMap,
                                    List.of(childMatchingType.elementId, parentElement.elementId))
                                 > 0
                              //or child matches propertyMap from parent
                              //e.g. power flow... propertyMap is fromBus,
                              // child elements are buses matching parent branch
                              || modelDataService.getStringValue(
                                    cc.propertyMap, parentElement.elementId).equals
                                    (childMatchingType.elementId)
                              || cc.propertyMap.equals("self")
                                 && parentElement.elementId.equals(childMatchingType.elementId)
                        )) {
                           //VarFactor for component
                           Double varFactorVal = cc.factorValue;

                           //and potentially from the factorProperty of the parent or child to themselves
                           //or the parent property from the factorParentProperty of the child
                           //(if no factor found then these default to 1.0)

                           //System.out.println(">>>" + cc.factorProperty);
                           varFactorVal = varFactorVal
                                       //factorProperty of the child
                                       // e.g., dirBranch direction applies to dirBranch
                                       * modelDataService.getDoubleValueElseOne
                                 (cc.factorProperty, childMatchingType.elementId)
                                       //factorProperty of the parent applied to child
                                       // e.g., bus child of powerflow has susceptance of parent
                                       * modelDataService.getDoubleValueElseOne
                                 (cc.factorParentProperty, parentElement.elementId)
                                       //factorProperty of the child applied to child ??
                                       //* modelDataService.getDoubleValueElseOne
                                       //(constraintComp.factorProperty, parentElement.elementId)
                                       //and tranche can map to more than one bus, via factor
                                       * modelDataService.getDoubleValueElseOne
                                 (cc.factorProperty,
                                       List.of(childMatchingType.elementId, parentElement.elementId));

                           //VariableId for constraint component
                           String varId = makeVarId(childMatchingType.elementId, cc.varType);
                           Integer varIndex = addVariable(varId);
                           //The varFactor relates the variable to the particular constraint
                           //setVarFactor(variableId, constraintId, varFactorVal);
                           newConstraint.varFactorMap.put(varIndex,varFactorVal);

                           constraintString[0] = constraintString[0]
                                                 + String.format("+ %1.2f * %s\n", varFactorVal, varId);
                        }
                     }
                  }
               }
               constraintString[0] += String.format("%s %1.2f", inEquality, rhsValue);
               newConstraint.constraintString = constraintString[0];

               msg[0] = msg[0] + constraintString[0] + "\n";
            }
         }
      }

      //System.out.println(">>>Constraints:\n" + msg[0]);
      //this.variables.forEach(v -> System.out.println(">>>var:" + v.varId));
   }

   /*
   public Constraint addConstraint(
         String constraintId,
         String constraintType,
         String elementId,
         String inequality,
         Double rhsValue) {

      Constraint newC = new Constraint(
            constraintId,
            constraintType,
            elementId,
            inequality,
            rhsValue);

      //System.out.println("Adding constraint:" + constraintId + " " + constraintType);
      if (elementId.equals("mathModel")) {
         this.objectiveFn = newC;
      } else if (constraints.stream().noneMatch(c -> c.constraintId.equals(constraintId))) {
         //add the constraint if not already there
         constraints.add(newC);
      }
      return newC;
   }*/


   private String makeVarId(String elementId, String varType) {
      return String.format("var_%s.%s", elementId, varType);
   }

   //Variable assign LB and UB (create if necessary)
   /*
   private void addVariable(String elementId, String varType, Double lowerBound, Double upperBound) {
      String varId = addVariable(elementId, varType);
      Variable var = varIdList.get(varIndexes.get(varId));
      var.lowerBound = lowerBound;
      var.upperBound = upperBound;
   }*/

   //Create variable if not already
   private Integer addVariable(String varId) {
      //add the variable if it is new and this is not the objective constraint
      //if (!elementId.equals("mathModel")) {
         Integer existingIndex = varIndexes.get(varId);
         if (existingIndex == null) {
            varIdList.add(varId);
            Integer newIndex = varIdList.indexOf(varId);
            varIndexes.put(varId, newIndex);
            return newIndex;
         }
        else {
            return existingIndex;
         }
      //}
   }

   //Create a varFactor and add it if not already
   /*
   public void setVarFactor(
         String varId,
         String constraintId,
         Double value) {
      VarFactor varFactor = new VarFactor(varId, constraintId, value);
      if (!varFactors.contains(varFactor)) {
         varFactors.add(varFactor);
      }
   }*/


   //Get the row of var factor values for the constraint
   //https://stackoverflow.com/questions/45793226/cannot-make-filter-foreach-collect-in-one-stream
   //Ordering: https://stackoverflow.com/questions/29216588/how-to-ensure-order-of-processing-in-java8-streams
   /*
   public List<Double> getVarFactorValsRow(String constraintId) {
      //System.out.println("getVarFactorValsRow:" + constraintId);
      //If there is a varFactor for this constraint+var then add it otherwise add zero
      return varIdList
            .stream()
            .map(v ->
                  varFactors
                        .stream()
                        .filter(vf -> vf.varId.equals(v.varId) && vf.constraintId.equals(constraintId))
                        .findFirst().map(vf -> vf.value)
                        .orElse(0.0))
            .collect(Collectors.toList());
   }*/

}
