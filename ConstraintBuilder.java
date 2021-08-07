import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConstraintBuilder {

    public static List<ConstraintComp> constraintComps = new ArrayList<>(

    );

    public static ArrayList<ConstraintDef> constraintDefs = new ArrayList<>(


    );

    public static void processConstraintDefs(ModelDataService modelDataService){
        //Constraint Defs
        for (ConstraintDef constraintDef : constraintDefs) {
            //Get the parent elements that match the ConstraintDef elementType
            //e.g. for node balance, do each bus
            /*
            for (
                    parentElement <-
                    modelElements.filter(_.elementType == constraintDef.elementType)
            ) { */
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


                String constraintId = s"${constraintDef.constraintType}.${parentElement.elementId}"
                String constraintString = s"$constraintId:\n"

                //              var msgForThisConstraint = s"\n\n${parentElement.elementId} " +
//                s"has constraint: ${constraintDef.constraintType}\n with components:\n"

                //===Components of the Constraint===

                //--Var Factor from parent--
                //Check if parent element has var in the constraint components
                if (constraintDef.varType != "") {
                    //Add the variable
                    val variableId = createVariable(parentElement.elementId, constraintDef.varType)
                    //Add its factor
                    val varFactor = constraintDef.factorValue
                    //TODO... add factor from property (if there ever is one)
                    setVarFactor(variableId, constraintId, varFactor)
                    constraintString += s" ${if (varFactor > 0) "+" else ""}$varFactor * $variableId\n"
                }

                //--Components--
                //Get the constraint components
                for ( //Get components where the constraint Id property matches
                        constraintComp <- constraintComps.filter(
                        _.constraintType == constraintDef.constraintType
                )
                ) {
                    //Get component elements where their elementType matches AND their property
                    //as specified by propertyMap matches the constraintDef parent element
                    //(case classes are especially useful for pattern matching...)

                    //elements where elementType matches constraint component
                    val childrenMatchingElementType = modelElements.filter(
                            _.elementType == constraintComp.elementType
                    )
                    //then check for property map from parent to child, or child to parent, or to self
                    for (
                            childElement <-
                            childrenMatchingElementType
                                    .filter(childMatchingType =>
                            (
                                    (constraintComp.propertyMap == "all") //all bids and offers are in objective
                                            || //parent matches propertyMap from child
                                            //e.g. nodeBal... propertyMap is fromBus, child is dirBranch matching parent bus
                                            childMatchingType.properties.exists(property =>
                                                    property._1 == constraintComp.propertyMap
                                                    && property._2 == parentElement.elementId)
                            || //or child matches propertyMap from parent
                            //e.g. power flow... propertyMap is fromBus, child is bus matching child branch
                            parentElement.properties.exists(property =>
                                    property._1 == constraintComp.propertyMap
                                    && property._2 == childMatchingType.elementId)
                            ||
                    constraintComp.propertyMap == "self"
                            && parentElement.elementId == childMatchingType.elementId
                          )
                      )
                ) {
                        //VarFactor for component
                        var varFactor = constraintComp.factorValue

                        //and potentially from the factorProperty of the parent or child to themselves
                        //or the parent property from the factorParentProperty of the child
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
                        ))

                        //VariableId for constraint component
                        val variableId = createVariable(childElement.elementId, constraintComp.varType)
                        //The varFactor relates the variable to the particular constraint
                        setVarFactor(variableId, constraintId, varFactor)

                        constraintString += s" ${if (varFactor > 0) "+" else ""}$varFactor * $variableId \n"
                    }
                } //done components

                //Inequality RHS
                constraintString += s" $inEquality $rhsValue"

                addConstraint(
                        constraintId,
                        constraintDef.constraintType,
                        parentElement.elementId,
                        inEquality,
                        rhsValue,
                        constraintString);

                msg += s"$constraintString\n\n" //msgForThisConstraint
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
            objectiveFn = newC
        } else if (!constraints.exists(c => c.constraintId == constraintId)) {
            constraints = constraints :+ newC
        }
    }

     case class VarFactor(
            varId: String,
            constraintId: String,
            value: Double
    )


  case class Variable(
            varId: String,
            varType: String,
            elementId: String,
            quantity: Double = 0.0 //result
    )

  case class Results(
            List<Variable> variables;
            List<Constraint> constraints;
    )
}
