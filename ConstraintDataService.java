

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.util.*;

public class ConstraintDataService {
   //Defs are read in
   public static List<ConstraintComp> constraintComps = new ArrayList<>();
   public static List<ConstraintDef> constraintDefs = new ArrayList<>();

   //Created from defs
   public final List<Constraint> constraints = new ArrayList<>();
   public Constraint objectiveFn = new Constraint(
         "", "", "", "", 0.0);
   public final List<String> varIdList = new ArrayList<>(); //Order is important
   public final HashMap<String, Double> lowerBounds = new HashMap<>();
   public final HashMap<String, Double> upperBounds = new HashMap<>();
   public final HashMap<String, Integer> varIdIndexMap = new HashMap<>();

   //For reporting
   public final HashMap<String,ModelVar> modelVars = new HashMap<>();
   public final HashMap<String,String> fromBusMap = new HashMap<>();
   public final HashMap<String,String> toBusMap = new HashMap<>();


   //public final List<VarFactor> varFactors = new ArrayList<>();

   record ModelVar (
         String varId,
         String varType,
         String elementId){}

   private class ConstraintDef {
      String constraintType;
      String elementType;
      String varType;
      String inEquality;
      Double rhsValue;
      String rhsProperty;
      Double factorValue;
   }
   private class ConstraintComp {
      String constraintType;
      String elementType;
      String propertyMap;
      String varType;
      String factorParentProperty;
      Double factorValue;
      String factorProperty;
   }

   //Read constraint defs
   public void readConstraintDefs() {
      String dir = "/Users/davidbullen/java/";
      String defFile = "constraint-defs4.json";
      String compFile = "constraint-comps4.json";
      //https://attacomsian.com/blog/jackson-read-json-file
      try {
         //https://stackoverflow.com/questions/29965764/how-to-parse-json-file-with-gson
         Gson gson = new Gson();
         JsonReader reader = new JsonReader(new FileReader(dir + defFile));
         constraintDefs = Arrays.asList(gson.fromJson(reader, ConstraintDef[].class));
         //constraintDefs.forEach(cd -> System.out.println(">>>constraint def:" + cd.constraintType));

         reader = new JsonReader(new FileReader(dir + compFile));
         constraintComps = Arrays.asList(gson.fromJson(reader, ConstraintComp[].class));
         //constraintComps.forEach(cc -> System.out.println(">>>constraint comp:" + cc.constraintType));

      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   public void processConstraintDefs(ModelDataService modelDataService) {
      final String[] msg = {""};
      //Constraint Defs
      for (ConstraintDef constraintDef : constraintDefs) {
         System.out.println(">>>ConstraintDef:" + constraintDef.constraintType + " elementType:" + constraintDef.elementType);
         //===Define the Constraint===
         //Get the parent elements that match the ConstraintDef elementType
         //e.g. for node balance, do each bus
         for (ModelElement parentElement
               : modelDataService.getElements(constraintDef.elementType)) {

            System.out.println(">>>con:" + constraintDef.constraintType
                               + " " + parentElement.elementType + " " + parentElement.elementId);
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

            //HashMap<String,Double> varFactorVals = new HashMap<>();

            //Create the constraint
            Constraint newConstraint = new Constraint(
                  constraintId,
                  constraintDef.constraintType,
                  parentElement.elementId,
                  inEquality,
                  rhsValue);
            //but adding it is conditional
            boolean addTheConstraint = true; //Constraint is not added if it can be replaced by bounds on the Var

            System.out.print(">>>Constraint:" + constraintId);
            //Check for Var Factor from parent
            if (!constraintDef.varType.equals("")) {

               //If this constraint has no components
               //then the limit on this var can be applied directly to the bounds of the variable
               //and the constraint will not be added
               String varId = getVarId(parentElement.elementId, constraintDef.varType);
               if (constraintComps.stream()
                     .noneMatch(cc -> cc.constraintType.equals(constraintDef.constraintType))) {
                  //Add the variable with bounds
                  System.out.print("...No components\n");
                  //***For now ASSUME it is LE***
                  upperBounds.put(varId,rhsValue); //assumes var will be added by a constraint
                  addTheConstraint = false;
               } else {
                  //Add VarFactor to the constraint
                  Double varFactorVal = constraintDef.factorValue;
                  newConstraint.varFactorMap.put(getVarIndex(varId),varFactorVal);

                  constraintString[0] += String.format("+ %1.2f * %s\n", varFactorVal, varId);
               }
            }

            //--Components--
            if (addTheConstraint) {

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
                     for (ModelElement childElement : modelDataService.getElements(cc.elementType)) {
                        if (cc.propertyMap.equals("dirBranchForSeg")) {
                           System.out.printf("check if property %s %s matches %s\n",
                                 cc.propertyMap, childElement.elementId, parentElement.elementId);
                        }
                        if ((
                              //e.g. all bids and offers are in objective constraint
                              cc.propertyMap.equals("all")
                              //parentElement matches constraintComp.propertyMap
                              //e.g. nodeBal... propertyMap is fromBus,
                              // child is dirBranch matching parent bus
                              || modelDataService.getStringValue(
                                    cc.propertyMap, childElement.elementId).equals
                                    (parentElement.elementId)
                              //or map via non-zero factor, e.g., weightTrancheBus(t,b) > 0
                              || modelDataService.getDoubleValue(
                                    cc.propertyMap,
                                    List.of(childElement.elementId, parentElement.elementId))
                                 > 0
                              //or child matches propertyMap from parent
                              //e.g. power flow... propertyMap is fromBus,
                              // child elements are buses matching parent branch
                              || modelDataService.getStringValue(
                                    cc.propertyMap, parentElement.elementId).equals
                                    (childElement.elementId)
                              //or child element is same as parent
                              //e.g. seg loss for flow... loss is "parent" and flow x ratio is "child"
                              || cc.propertyMap.equals("self")
                                 && parentElement.elementId.equals(childElement.elementId)
                        )) {
                           System.out.print("matched\n");
                           //For reporting
                           if (parentElement.elementType.equals("bus") && cc.propertyMap.equals("fromBus")){
                              fromBusMap.putIfAbsent(childElement.elementId,parentElement.elementId);
                           }
                           else if (parentElement.elementType.equals("bus") && cc.propertyMap.equals("toBus")){
                              toBusMap.putIfAbsent(childElement.elementId,parentElement.elementId);
                           }

                           //VarFactor for component
                           Double varFactorVal = cc.factorValue;

                           //and potentially from the factorProperty of the parent or child to themselves
                           //or the parent property from the factorParentProperty of the child
                           //(if no factor found then these default to 1.0)

                           System.out.println(">>>factorProperty:" + cc.factorProperty);
                           varFactorVal = varFactorVal
                                       //factorProperty of the child
                                       // e.g., dirBranch direction applies to dirBranch
                                       * modelDataService.getDoubleValueElseOne
                                 (cc.factorProperty, childElement.elementId)
                                       //factorProperty of the parent applied to child
                                       // e.g., bus child of powerflow has susceptance of parent
                                       * modelDataService.getDoubleValueElseOne
                                 (cc.factorParentProperty, parentElement.elementId)
                                       //tranche can map to more than one bus, via factor
                                       * modelDataService.getDoubleValueElseOne
                                 (cc.factorProperty,
                                       List.of(childElement.elementId, parentElement.elementId));

                           //Set the varFactor
                           String varId = getVarId(childElement.elementId, cc.varType);
                           newConstraint.varFactorMap.put(getVarIndex(varId),varFactorVal);

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

      //Check
      for(ModelElement branch : modelDataService.getElements(ModelDefService.ElementType.dirBranch) ){
         String fromBusString = fromBusMap.get(branch.elementId);
         String toBusString = toBusMap.get(branch.elementId);
         if (fromBusString == null || toBusString == null) {
            System.out.println("####Branch: " + branch.elementId + " is missing from or to bus");
         }
      }
      System.out.println(">>>Constraints:\n" + msg[0]);
      //this.variables.forEach(v -> System.out.println(">>>var:" + v.varId));
      System.out.println(">>>>>>>>>>>>>>>>>>Constraints:" + constraints.size() + " Vars:" + varIdList.size() + "<<<<<<<<<<<<<<<<<<<<");

   }

   //Get varId, add if new
   private String getVarId(String elementId, String varType) {
      //Add the var i.d. to the map if not already there
      String varId = String.format("var_%s.%s", elementId, varType);
      modelVars.putIfAbsent(varId,new ModelVar(varId,varType,elementId));
      //Unrestricted vars
      /*
      if (varType.equals("branchFlow") || varType.equals("phaseAngle")){
         lowerBounds.putIfAbsent(varId,-Double.MAX_VALUE);
      }*/
      return varId;
   }

   //Get var index, add if new
   private Integer getVarIndex(String varId) {
      //add the variable if it is new and this is not the objective constraint
      Integer existingIndex = varIdIndexMap.get(varId);
      if (existingIndex == null) {
         varIdList.add(varId);
         Integer newIndex = varIdList.indexOf(varId);
         varIdIndexMap.put(varId, newIndex);
         return newIndex;
      } else {
         return existingIndex;
      }
   }
}
