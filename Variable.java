public class Variable {
   String varId;
   String varType;
   String elementId;
   Double quantity = 0.0; //result

   public Variable(String varId, String varType, String elementId) {
      this.varId = varId;
      this.varType = varType;
      this.elementId = elementId;
   }
}
