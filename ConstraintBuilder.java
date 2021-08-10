

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConstraintBuilder {

   public static List<ConstraintComp> constraintComps;
   public static List<ConstraintDef> constraintDefs;

   public static List<Constraint> constraints;
   public static Constraint objectiveFn = new Constraint(
         "", "", "", "", 0.0, "");
   public static List<Variable> variables;
   //Var factor inputs are used to create varFactor rows
   //which are then related to c and v by row and col
   public static List<VarFactor> varFactorInputs;
   public static List<List<Double>> varFactorRows;

   public static void resetMathModel() {
      constraints = new ArrayList<>();
      variables = new ArrayList<>();
      varFactorInputs = new ArrayList<>();

      varFactorRows = new ArrayList<>();
        /*
        reducedCosts = new ArrayList<>();
        rhsValues = new ArrayList<>();
        objectiveFn = Constraint("", "", "", "", 0.0, "");
        objectiveRhs = 0.0;*/
   }

   public static void processConstraintDefs(ModelDataService modelDataService) {
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
                     Double rhsValueFromProperty =
                           modelDataService.getDoubleValue(
                                 constraintDef.rhsProperty, List.of(parentElement.elementId));
                    /*
                    val rhsValueFromProperty = parentElement.properties.find {
                        case (name, _) => name == constraintDef.rhsProperty
                    }
                    if (rhsValueFromProperty.isDefined) {
                        rhsValue = rhsValueFromProperty.get._2.toString.toDouble
                    }*/

                  } else { //RHS is from specified value
                     rhsValue = constraintDef.rhsValue;
                  }


                  //String constraintId = s"${constraintDef.constraintType}.${parentElement.elementId}"
                  //String constraintString = s"$constraintId:\n"

                  String constraintId = String.format("%s.%s",
                        constraintDef.constraintType, parentElement.elementId);
                  //String constraintString = String.format("%s\n", constraintId);
                  //AtomicReference<String> constraintString = new AtomicReference<>();
                  final String[] constraintString = {String.format("%s\n", constraintId)};
                  //constraintString.set(String.format("%s\n", constraintId));

                  //              var msgForThisConstraint = s"\n\n${parentElement.elementId} " +
//                s"has constraint: ${constraintDef.constraintType}\n with components:\n"

                  //===Components of the Constraint===

                  //--Var Factor from parent--
                  //Check if parent element has var in the constraint components
                  if (constraintDef.varType != "") {
                     //Add the variable
                     String variableId = createVariable(parentElement.elementId, constraintDef.varType);
                     //Add its factor
                     Double varFactor = constraintDef.factorValue;
                     //TODO... add factor from property (if there ever is one)
                     setVarFactor(variableId, constraintId, varFactor);

                     //constraintString += s " ${if (varFactor > 0) " + " else " "}$varFactor * $variableId\n"
                     constraintString[0] = constraintString[0] + String.format("+ $1.2f * %s\n", varFactor, variableId);
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

                                    //Tranche can map to more than one bus, via factor
                                    varFactor = varFactor
                                                * modelDataService.getDoubleValueElseOne(
                                          constraintComp.factorProperty, childElement.elementId)
                                                * modelDataService.getDoubleValueElseOne(
                                          constraintComp.factorParentProperty, parentElement.elementId)
                                                * modelDataService.getDoubleValueElseOne(
                                          constraintComp.factorProperty, parentElement.elementId);
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

                                    //constraintString += s " ${if (varFactor > 0) " + " else "
                                    //"}$varFactor * $variableId \n";

                                    constraintString[0] = constraintString[0]
                                                          + String.format("+ $1.2f * %s\n", varFactor, variableId);
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

                  //msg += s "$constraintString\n\n" //msgForThisConstraint

               });
      }

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
      /*
      if (!variables.exists(v = > v.varId == varId)){
         variables = variables :+Variable(varId, varType, elementId)
      }*/
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
      if (!varFactorInputs.contains(varFactor)) {
         varFactorInputs.add(varFactor);
      }
   }


   //Get the row of var factor values for the constraint
   //https://stackoverflow.com/questions/45793226/cannot-make-filter-foreach-collect-in-one-stream
   public List<Double> getVarFactorRow(Constraint c) {
      //If there is a varFactor for this constraint+var then add it otherwise add zero
      return variables
            .stream()
            .map(v ->
               varFactorInputs
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
