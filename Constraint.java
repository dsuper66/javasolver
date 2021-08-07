public class Constraint {

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
