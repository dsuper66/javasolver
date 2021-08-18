

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstraintDataService {
   //Defs are read in
   public static List<ConstraintComp> constraintComps = new ArrayList<>();
   public static List<ConstraintDef> constraintDefs = new ArrayList<>();

   //Created from defs
   public static List<Constraint> constraints = new ArrayList<>();
   public static Constraint objectiveFn = new Constraint(
         "", "", "", "", 0.0, "");
   public static List<Variable> variables = new ArrayList<>();
   public static List<VarFactor> varFactors = new ArrayList<>();

   public static void readConstraints(){
      String dir = "/Users/davidbullen/java/";
      String defFile = "constraint-defs.json";
      String compFile = "constraint-comps.json";
      //https://attacomsian.com/blog/jackson-read-json-file
      //ObjectMapper mapper = new ObjectMapper();
      try {
         //https://stackoverflow.com/questions/29965764/how-to-parse-json-file-with-gson
         Gson gson = new Gson();

         JsonReader reader = new JsonReader(new FileReader(dir + defFile));
         //final List<ConstraintDef> constraintDefs = Arrays.asList(gson.fromJson(reader, ConstraintDef[].class));
         constraintDefs = Arrays.asList(gson.fromJson(reader, ConstraintDef[].class));
         for (ConstraintDef cd : constraintDefs) {
            System.out.println("constraint def:" + cd.constraintType);
         }

         reader = new JsonReader(new FileReader(dir + compFile));
         //final List<ConstraintComp> constraintComps = Arrays.asList(gson.fromJson(reader, ConstraintComp[].class));
         constraintComps = Arrays.asList(gson.fromJson(reader, ConstraintComp[].class));
         for (ConstraintComp cc : constraintComps) {
            System.out.println("constraint comp:" + cc.constraintType);
         }

      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   /*
   public static void resetMathModel() {
      constraints = new ArrayList<>();
      variables = new ArrayList<>();
      varFactorInputs = new ArrayList<>();

      varFactorRows = new ArrayList<>();
   }*/

   public static void processConstraintDefs(ModelDataService modelDataService) {
      final String[] msg = {""};
      //Constraint Defs
      for (ConstraintDef constraintDef : constraintDefs) {
         //Get the parent elements that match the ConstraintDef elementType
         //e.g. for node balance, do each bus
         modelDataService.getElements(constraintDef.elementType)
               .stream()
               .forEach(parentElement -> {

                  //===Define the Constraint===
                  //LE or EQ
                  String inEquality = constraintDef.inEquality;

                  //--RHS--
                  //RHS from parent or value
                  Double rhsValue = 0.0;
                  //RHS from parent
                  //check if RHS is a property of the parent element, e.g., brLimit for branch
                  if (constraintDef.rhsProperty != "") {
                      rhsValue =
                           modelDataService.getDoubleValue(
                                 constraintDef.rhsProperty, List.of(parentElement.elementId));
                  } else { //RHS is from specified value
                     rhsValue = constraintDef.rhsValue;
                  }

                  String constraintId = String.format("%s.%s",
                        constraintDef.constraintType, parentElement.elementId);
                  final String[] constraintString = {String.format("%s\n", constraintId)};

                  //              var msgForThisConstraint = s"\n\n${parentElement.elementId} " +
//                s"has constraint: ${constraintDef.constraintType}\n with components:\n"

                  //===Components of the Constraint===

                  //--Var Factor from parent--
                  //Check if parent element has var in the constraint components
                  if (!constraintDef.varType.equals("") ) {
                     //Add the variable
                     String variableId = createVariable(parentElement.elementId, constraintDef.varType);
                     //Add its factor
                     Double varFactor = constraintDef.factorValue;
                     setVarFactor(variableId, constraintId, varFactor);

                     //constraintString += s " ${if (varFactor > 0) " + " else " "}$varFactor * $variableId\n"
                     constraintString[0] = constraintString[0]
                                           + String.format("+ %1.2f * %s\n", varFactor, variableId);
                  }

                  //--Components--
                  constraintComps
                        .stream()
                        .filter(cc -> cc.constraintType.equals(constraintDef.constraintType))
                        .forEach(constraintComp -> {

                           //Get component elements where their elementType matches AND their property
                           //as specified by propertyMap matches the constraintDef parent element

                           //elements where elementType matches constraint component
                           modelDataService.getElements(constraintComp.elementType)
                                 .stream()
                                 //then check for property map from parent to child, or child to parent, or to self
                                 .filter(childMatchingType -> {
                                    return (
                                          //e.g. all bids and offers are in objective constraint
                                          constraintComp.propertyMap.equals("all")
                                          ||
                                          //parentElement matches constraintComp.propertyMap
                                          //e.g. nodeBal... propertyMap is fromBus,
                                          // child is dirBranch matching parent bus
                                          modelDataService.getStringValue(
                                                constraintComp.propertyMap, childMatchingType.elementId).equals
                                                (parentElement.elementId)
                                          || //or map via non-zero factor, e.g., weightTrancheBus(t,b) > 0
                                          modelDataService.getDoubleValue(
                                                constraintComp.propertyMap,
                                                List.of(childMatchingType.elementId,parentElement.elementId))
                                                > 0
                                          ||
                                          //or child matches propertyMap from parent
                                          //e.g. power flow... propertyMap is fromBus,
                                          // child elements are buses matching parent branch
                                          modelDataService.getStringValue(
                                                constraintComp.propertyMap, parentElement.elementId).equals
                                                (childMatchingType.elementId)
                                          ||
                                          constraintComp.propertyMap == "self"
                                          && parentElement.elementId == childMatchingType.elementId
                                    );
                                 })
                                 .forEach(childElement -> {
                                    //VarFactor for component
                                    Double varFactor = constraintComp.factorValue;

                                    //and potentially from the factorProperty of the parent or child to themselves
                                    //or the parent property from the factorParentProperty of the child
                                    //(if no factor found then these default to 1.0)

                                    System.out.println(">>>" + constraintComp.factorProperty);
                                    varFactor = varFactor
                                                //factorProperty of the child
                                                // e.g., dirBranch direction applies to dirBranch
                                                * modelDataService.getDoubleValueElseOne
                                          (constraintComp.factorProperty, childElement.elementId)
                                                //factorProperty of the parent applied to child
                                                // e.g., bus child of powerflow has susceptance of parent
                                                * modelDataService.getDoubleValueElseOne
                                          (constraintComp.factorParentProperty, parentElement.elementId)
                                                //factorProperty of the child applied to child ??
                                                //* modelDataService.getDoubleValueElseOne
                                          //(constraintComp.factorProperty, parentElement.elementId)
                                    //and tranche can map to more than one bus, via factor
                                                * modelDataService.getDoubleValueElseOne
                                          (constraintComp.factorProperty,
                                                List.of(childElement.elementId,parentElement.elementId));

                                    /*             * getPropertyAsDoubleElseDefault(
                                    //      childElement,
                                    //      constraintComp.factorProperty, 1.0
                                    )
                                                 * getPropertyAsDoubleElseDefault(
                                          parentElement,
                                          constraintComp.factorParentProperty, 1.0
                                    )
                                                 * getPropertyAsDoubleElseDefault(
                                          parentElement,
                                          constraintDef.factorProperty, 1.0
                                    ));*/

                                    //VariableId for constraint component
                                    String variableId = createVariable(childElement.elementId, constraintComp.varType);
                                    //The varFactor relates the variable to the particular constraint
                                    setVarFactor(variableId, constraintId, varFactor);

                                    constraintString[0] = constraintString[0]
                                                          + String.format("+ %1.2f * %s\n", varFactor, variableId);
                                 });
                        });//done components

                  //Inequality RHS
                  //constraintString += s " $inEquality $rhsValue"
                  constraintString[0] = constraintString[0] + String.format("%s %1.2f", inEquality, rhsValue);

                  addConstraint(
                        constraintId,
                        constraintDef.constraintType,
                        parentElement.elementId,
                        inEquality,
                        rhsValue,
                        constraintString[0]);

                  msg[0] = msg[0] + constraintString[0] + "\n"; //msgForThisConstraint

               });
      }
      System.out.println(">>>Constraints:\n" + msg[0]);
      variables.forEach(v -> System.out.println("var:" + v.varId));
   }

   public static void addConstraint(
         String constraintId,
         String constraintType,
         String elementId,
         String inequality,
         Double rhsValue,
         String constraintString) {

      Constraint newC = new Constraint(
            constraintId,
            constraintType,
            elementId,
            inequality,
            rhsValue,
            constraintString);

      if (constraintType == "objective") {
         objectiveFn = newC;
      } else if (constraints //add the constraint
            .stream()
            .filter(c -> c.constraintId.equals(constraintId))
            .collect(Collectors.toList()).isEmpty()) {
         constraints.add(newC);
      }
   }

   public static String createVariable(String elementId, String varType) {
      String varId = String.format("%s.%s", elementId, varType);
      if (variables //add the variable
            .stream()
            .filter(v -> v.varId.equals(varId))
            .collect(Collectors.toList()).isEmpty()) {
         variables.add(new Variable(varId, varType, elementId));
      }
      return varId;
   }

   //Create a varFactor and add it if not already
   public static void setVarFactor(
         String varId,
         String constraintId,
         Double value) {
      VarFactor varFactor = new VarFactor(varId, constraintId, value);
      if (!varFactors.contains(varFactor)) {
         varFactors.add(varFactor);
      }
   }


   //Get the row of var factor values for the constraint
   //https://stackoverflow.com/questions/45793226/cannot-make-filter-foreach-collect-in-one-stream
   public List<Double> getVarFactorRow(Constraint c) {
      //If there is a varFactor for this constraint+var then add it otherwise add zero
      return variables
            .stream()
            .map(v ->
               varFactors
                     .stream()
                     .filter(vf -> vf.varId.equals(v.varId) && vf.constraintId.equals(c.constraintId))
                     .findFirst().map(vf -> vf.value)
                     .orElse(0.0))
            .collect(Collectors.toList());
      /*
      variables.map(v = >
      varFactorInputs.find(vF = >
      (vF.varId, vF.constraintId) ==(v.varId, c.constraintId)
      )match {
         case Some(optVF) =>optVF.value
         case None =>0.0
      }
    )*/

   }

   class Results {
      List<Variable> variables;
      List<Constraint> constraints;
   }
}
