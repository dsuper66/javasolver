import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstraintBuilder {

   public static List<ConstraintComp> constraintComps;
   public static List<ConstraintDef> constraintDefs;

   public static List<Constraint> constraints;
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
                  String constraintString = String.format("%s\n", constraintId);

                  //              var msgForThisConstraint = s"\n\n${parentElement.elementId} " +
//                s"has constraint: ${constraintDef.constraintType}\n with components:\n"

                  //===Components of the Constraint===

                  //--Var Factor from parent--
                  //Check if parent element has var in the constraint components
                  if (constraintDef.varType != "") {
                     //Add the variable
                     String variableId = createVariable(parentElement.elementId, constraintDef.varType)
                     //Add its factor
                     Double varFactor = constraintDef.factorValue;
                     //TODO... add factor from property (if there ever is one)
                     setVarFactor(variableId, constraintId, varFactor);

                     //constraintString += s " ${if (varFactor > 0) " + " else " "}$varFactor * $variableId\n"
                     constraintString += String.format("+ $1.2f * %s\n", varFactor, variableId);
                  }

                  //--Components--
                  constraintComps
                        .stream()
                        .filter(cc -> cc.constraintType.equals(constraintDef.constraintType))
                        .forEach(constraintComp -> {

                           //Get component elements where their elementType matches AND their property
                           //as specified by propertyMap matches the constraintDef parent element
                           //(case classes are especially useful for pattern matching...)

                                    /*
                            val childrenMatchingElementType = modelElements.filter(
                                    _.elementType == constraintComp.elementType
                            )*/
                           //elements where elementType matches constraint component
                           modelDataService.getElements(constraintComp.elementType)
                                 .stream()
                                 //then check for property map from parent to child, or child to parent, or to self
                                 .filter(childMatchingType -> {
                                    return (
                                          //all bids and offers are in objective
                                          constraintComp.propertyMap.equals("all")
                                          ||
                                          //parentElement matches constraintComp.propertyMap
                                          //e.g. nodeBal... propertyMap is fromBus,
                                          // child is dirBranch matching parent bus
                                          modelDataService.getStringValue(
                                                constraintComp.propertyMap, childMatchingType.elementId).equals
                                                (parentElement.elementId)
                                          ||
                                          //or child matches propertyMap from parent
                                          //e.g. power flow... propertyMap is fromBus,
                                          // child is bus matching child branch
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
                                    varFactor = (varFactor
                                                 * getPropertyAsDoubleElseDefault(
                                          childElement,
                                          constraintComp.factorProperty, 1.0
                                    )
                                                 * getPropertyAsDoubleElseDefault(
                                          parentElement,
                                          constraintComp.factorParentProperty, 1.0
                                    )
                                                 * getPropertyAsDoubleElseDefault(
                                          parentElement,
                                          constraintDef.factorProperty, 1.0
                                    ));

                                    //VariableId for constraint component
                                    String variableId = createVariable(childElement.elementId, constraintComp.varType);
                                    //The varFactor relates the variable to the particular constraint
                                    setVarFactor(variableId, constraintId, varFactor);

                                    constraintString += s " ${if (varFactor > 0) " + " else "
                                    "}$varFactor * $variableId \n";

                                    constraintString += String.format("+ $1.2f * %s\n", varFactor, variableId);

                                 });
                        });//done components

                  //Inequality RHS
                  //constraintString += s " $inEquality $rhsValue"
                  constraintString += String.format("%s %1.2f", inEquality, rhsValue);

                  addConstraint(
                        constraintId,
                        constraintDef.constraintType,
                        parentElement.elementId,
                        inEquality,
                        rhsValue,
                        constraintString);

                  msg += s "$constraintString\n\n" //msgForThisConstraint

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
      } else if (!constraints.exists(c = > c.constraintId == constraintId)){
         constraints = constraints :+newC
      }
   }

   public static String createVariable(String elementId, String varType) {
      String varId = String.format("%s.%s", elementId, varType);
      if (!variables.exists(v = > v.varId == varId)){
         variables = variables :+Variable(varId, varType, elementId)
      }
      return varId;
   }

   //Create a varFactor and add it if not already
   public static void setVarFactor(
         String varId,
         String constraintId,
         Double value) {
      VarFactor varFactor = new VarFactor(varId, constraintId, value);
      if (!varFactorInputs.contains(varFactor))
         varFactorInputs = varFactorInputs :+varFactor
   }

   //Report
   def varFactorsString:String =

   {
      varFactorInputs.map(_.toString).mkString("\n")
   }

   def varFactorsForConstraint(c:Constraint):Seq[Double]=

   {
      //If there is a varFactor for this constraint+var then add it otherwise add zero
      variables.map(v = >
      varFactorInputs.find(vF = >
      (vF.varId, vF.constraintId) ==(v.varId, c.constraintId)
      )match {
      case Some(optVF) =>optVF.value
      case None =>0.0
   }
    )
   }


   class VarFactor {
      VarFactor(String varId, String constraintId, Double value) {
         this.varId = varId;
         this.constraintId = constraintId;
         this.value = value;
      }

      String varId;
      String constraintId;
      Double value;
   }


   class Variable {
      String varId;
      String varType;
      String elementId;
      Double quantity = 0.0; //result
   }

   class Constraint {
      String constraintId;
      String constraintType;
      String elementId;
      String inequality;
      Double rhsValue;
      String constraintString;
      Double shadowPrice = 0.0;

      public Constraint(String constraintId,
                        String constraintType,
                        String elementId,
                        String inequality,
                        Double rhsValue,
                        String constraintString) {
         this.constraintId = constraintId;
         this.constraintType = constraintType;
         this.elementId = elementId;
         this.inequality = inequality;
         this.rhsValue = rhsValue;
         this.constraintString = constraintString;
      }
   }

   class Results {
      List<Variable> variables;
      List<Constraint> constraints;
   }
}
