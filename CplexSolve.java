import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.List;
import java.util.Map;

public class CplexSolve {

   public ConstraintDataService constraintDataService;

   public void doCplexSolve() {

      // Create the modeler/solver object
      try (IloCplex cplex = new IloCplex()) {

         IloNumVar[][] cplexVars = new IloNumVar[1][];
         IloRange[][] cplexConstraints = new IloRange[1][];

         populateByRow(
               cplex,
               cplexVars,
               cplexConstraints,
               constraintDataService.varIdList,
               constraintDataService.upperBounds,
               constraintDataService.constraints,
               constraintDataService.objectiveFn);

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
                             IloRange[][] cplexConstraints,
                             List<String> varIds,
                             Map<String,Double> upperBounds,
                             List<Constraint> constraints,
                             Constraint objectiveFn
   ) throws IloException {

      //Variables
      int varCount = varIds.size();
      //https://www.ibm.com/docs/en/icos/12.10.0?topic=cm-numvar-method-1
      cplexVars[0] = new IloNumVar[varCount];
      int varIndex = 0;
      for (String varId : varIds) {
         cplexVars[0][varIndex] = model.numVar(0.0, upperBounds.getOrDefault(varId,Double.MAX_VALUE), varId);
         //System.out.println(">>>CPLEX var:[" + varIndex + "]" + var.varId);
         varIndex++;
      }

      //https://www.ibm.com/docs/api/v1/content/SSSA5P_12.8.0/ilog.odms.cplex.help/refdotnetcplex/html/T_ILOG_Concert_ILinearNumExpr.htm
      //Objective
      IloLinearNumExpr objective = model.linearNumExpr();
      //Map<Integer, Double> varFactors = varFactorMap.get(objectiveFn.constraintId);
      for (varIndex = 0; varIndex < varCount; varIndex++) {
         objective.addTerm(objectiveFn.varFactorMap.getOrDefault(varIndex, 0.0), cplexVars[0][varIndex]);
         //System.out.println(">>>CPLEX obj varFactor:[" + varIndex + "]" + varFactor);
      }
      model.addMaximize(objective);

      //https://www.ibm.com/docs/en/icos/12.8.0.0?topic=technology-adding-constraints-iloconstraint-ilorange
      //https://kunlei.github.io/cplex/cplex-java-constraints/
      //https://stackoverflow.com/questions/2279030/type-list-vs-type-arraylist-in-java
      //https://stackoverflow.com/questions/5207162/define-a-fixed-size-list-in-java

      //Constraints
      int constraintCount = constraints.size();
      cplexConstraints[0] = new IloRange[constraintCount];
      int constraintIndex = 0;
      for (Constraint constraint : constraints) {
         System.out.printf(">>>cplex constraint[%d]:%s rhs:%f%n",
               constraintIndex, constraint.constraintId, constraint.rhsValue);

         //LHS
         IloLinearNumExpr lhs = model.linearNumExpr();
         //varFactors = varFactorMap.get(constraint.constraintId);
         for (varIndex = 0; varIndex < varCount; varIndex++) {
            lhs.addTerm(constraint.varFactorMap.getOrDefault(varIndex, 0.0), cplexVars[0][varIndex]);
         }

         //GE LT EQ
         if (constraint.inequality.equalsIgnoreCase("le")) {
            IloRange cplexConstraint
                  = model.addLe(lhs, constraint.rhsValue, constraint.constraintId);
            cplexConstraints[0][constraintIndex] = cplexConstraint;
         } else if (constraint.inequality.equalsIgnoreCase("eq")) {
            IloRange cplexConstraint
                  = model.addEq(lhs, constraint.rhsValue, constraint.constraintId);
            cplexConstraints[0][constraintIndex] = cplexConstraint;
         }

         constraintIndex++;
      }
   }
}
