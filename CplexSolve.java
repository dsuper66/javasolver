import ilog.concert.*;
import ilog.cplex.IloCplex;

public class CplexSolve {

   public static void doCplexSolve() {

      // Create the modeler/solver object
      try (IloCplex cplex = new IloCplex()) {

         IloNumVar[][] cplexVars = new IloNumVar[1][];
         IloRange[][]  cplexConstraints = new IloRange[1][];

         populateByRow(cplex, cplexVars, cplexConstraints);

         // write model to file
         cplex.exportModel("lpex1.lp");

         // solve the model and display the solution if one was found
         if (cplex.solve()) {
            double[] x = cplex.getValues(cplexVars[0]);
            double[] dj = cplex.getReducedCosts(cplexVars[0]);
            double[] pi = cplex.getDuals(cplexConstraints[0]);
            double[] slack = cplex.getSlacks(cplexConstraints[0]);

            cplex.output().println("Solution status = " + cplex.getStatus());
            cplex.output().println("Solution value  = " + cplex.getObjValue());

            int nvars = x.length;
            for (int j = 0; j < nvars; ++j) {
               cplex.output().println("Variable " + j +
                                      ": Value = " + x[j] +
                                      " Reduced cost = " + dj[j]);
            }

            int ncons = slack.length;
            for (int i = 0; i < ncons; ++i) {
               cplex.output().println("Constraint " + i +
                                      ": Slack = " + slack[i] +
                                      " Pi = " + pi[i]);
            }
         }
      } catch (IloException e) {
         System.err.println("Concert exception '" + e + "' caught");
      }
   }


   static void populateByRow(IloMPModeler model,
                             IloNumVar[][] cplexVars,
                             IloRange[][] cplexConstraints) throws IloException {
      int varCount = 3;
      //https://www.ibm.com/docs/en/icos/12.10.0?topic=cm-numvar-method-1
      //Variables
      cplexVars[0] = new IloNumVar[varCount];
      cplexVars[0][0] = model.numVar(0.0,40.0,"x1");
      cplexVars[0][1] = model.numVar(0.0,Double.MAX_VALUE,"x2");
      cplexVars[0][2] = model.numVar(0.0,Double.MAX_VALUE,"x3");

      //https://www.ibm.com/docs/api/v1/content/SSSA5P_12.8.0/ilog.odms.cplex.help/refdotnetcplex/html/T_ILOG_Concert_ILinearNumExpr.htm
      //Objective
      IloLinearNumExpr objective = model.linearNumExpr();
      objective.addTerm(1.0,cplexVars[0][0]);
      objective.addTerm(2.0,cplexVars[0][1]);
      objective.addTerm(3.0,cplexVars[0][2]);
      model.addMaximize(objective);

      //https://www.ibm.com/docs/en/icos/12.8.0.0?topic=technology-adding-constraints-iloconstraint-ilorange
      //https://kunlei.github.io/cplex/cplex-java-constraints/
      //https://stackoverflow.com/questions/2279030/type-list-vs-type-arraylist-in-java
      //https://stackoverflow.com/questions/5207162/define-a-fixed-size-list-in-java

      //Constraints
      int constraintCount = 2;
      Double[][] varFactorsAllConstraints = new Double[constraintCount][varCount];
      String[] constraintNames = new String[constraintCount];
      Double[] constraintRhs = new Double[constraintCount];

      int constraintIndex = 0;
      Double[] varFactorsThisConstraint = new Double[varCount];
      constraintNames[constraintIndex] = "c1";
      constraintRhs[constraintIndex] = 20.0;
      varFactorsThisConstraint[0] = -1.0;
      varFactorsThisConstraint[1] = 1.0;
      varFactorsThisConstraint[2] = 1.0;
      varFactorsAllConstraints[constraintIndex] = varFactorsThisConstraint;

      constraintIndex = 1;
      varFactorsThisConstraint = new Double[varCount];
      constraintNames[constraintIndex] = "c2";
      constraintRhs[constraintIndex] = 30.0;
      varFactorsThisConstraint[0] = 1.0;
      varFactorsThisConstraint[1] = -3.0;
      varFactorsThisConstraint[2] = 1.0;
      varFactorsAllConstraints[constraintIndex] = varFactorsThisConstraint;

      cplexConstraints[0] = new IloRange[constraintCount];

      for (constraintIndex = 0; constraintIndex < constraintCount; constraintIndex++) {
         IloLinearNumExpr lhs = model.linearNumExpr();
         Double[] varFactors = varFactorsAllConstraints[constraintIndex];

         for (int varIndex = 0; varIndex < varCount; varIndex++) {
            lhs.addTerm(varFactors[varIndex], cplexVars[0][varIndex]);
         }
         IloRange cplexConstraint
               = model.addLe(lhs,constraintRhs[constraintIndex],constraintNames[constraintIndex]);
         cplexConstraints[0][constraintIndex] = cplexConstraint;
      }
   }
}
