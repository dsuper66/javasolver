public class Constraint {

   public final String constraintId;
   public final String constraintType;
   public final String elementId;
   public final String inequality;
   public final Double rhsValue;
   public final String constraintString;
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
