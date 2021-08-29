

public class Variable {
   final String varId;
   final String varType;
   final String elementId;
   //If a constraint has no components then it restricts the variable
   //Double lowerBound = 0.0;
   //Double upperBound = Double.MAX_VALUE;


   //Double quantity = 0.0; //result

   public Variable(String varId, String varType, String elementId) {
      this.varId = varId;
      this.varType = varType;
      this.elementId = elementId;
   }
}
