public class Constraint {

   final String constraintId;
   final String constraintType;
   final String elementId;
   final String inequality;
   final Double rhsValue;
   final String constraintString;
   //Double shadowPrice = 0.0; //result

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
