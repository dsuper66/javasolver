import ilog.concert.*;

import java.io.IOException;

public class SolveModel {

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!"); // Display the string.

        ModelDataService modelDataService = new ModelDataService();
        modelDataService.writeDefs();

        ReadCaseFile.readCase(modelDataService);
        PreProcessing.doPreProcessing(modelDataService);

        ConstraintDataService constraintDataService = new ConstraintDataService();
        constraintDataService.readConstraints();
        constraintDataService.processConstraintDefs(modelDataService);

        CplexSolve cplexSolve = new CplexSolve();
        cplexSolve.constraintDataService = constraintDataService;
        cplexSolve.doCplexSolve();

        //The cplex demo solve
        /*
        // Create the modeler/solver object
        try (IloCplex cplex = new IloCplex()) {

            IloNumVar[][] var = new IloNumVar[1][];
            IloRange[][]  rng = new IloRange[1][];

            populateByRow(cplex, var, rng);

            // write model to file
            cplex.exportModel("lpex1.lp");

            // solve the model and display the solution if one was found
            if ( cplex.solve() ) {
                double[] x     = cplex.getValues(var[0]);
                double[] dj    = cplex.getReducedCosts(var[0]);
                double[] pi    = cplex.getDuals(rng[0]);
                double[] slack = cplex.getSlacks(rng[0]);

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
        }
        catch (IloException e) {
            System.err.println("Concert exception '" + e + "' caught");
        }

         */
    }


    static void populateByRow(IloMPModeler model,
                              IloNumVar[][] var,
                              IloRange[][] rng) throws IloException {
        /*
        double[]    lb      = {0.0, 0.0, 0.0};
        double[]    ub      = {40.0, Double.MAX_VALUE, Double.MAX_VALUE};
        String[]    varname = {"x1", "x2", "x3"};
        IloNumVar[] x       = model.numVarArray(3, lb, ub, varname);
        var[0] = x; //the array of vars that were added, so results can be extracted*/

        int varCount = 3;
        //https://www.ibm.com/docs/en/icos/12.10.0?topic=cm-numvar-method-1
        //Variables
        IloNumVar[] cplexVariables = new IloNumVar[varCount];
        cplexVariables[0] = model.numVar(0.0,40.0,"x1");
        cplexVariables[1] = model.numVar(0.0,Double.MAX_VALUE,"x2");
        cplexVariables[2] = model.numVar(0.0,Double.MAX_VALUE,"x3");

        var[0] = new IloNumVar[3];
        var[0][0] = cplexVariables[0]; //model.numVar(0.0,40.0,"x1");
        var[0][1] = cplexVariables[1]; //model.numVar(0.0,Double.MAX_VALUE,"x2");
        var[0][2] = cplexVariables[2]; //model.numVar(0.0,Double.MAX_VALUE,"x3");
        IloNumVar[] x = var[0];

        //https://www.ibm.com/docs/api/v1/content/SSSA5P_12.8.0/ilog.odms.cplex.help/refdotnetcplex/html/T_ILOG_Concert_ILinearNumExpr.htm
        //Objective
        IloLinearNumExpr objective = model.linearNumExpr();
        objective.addTerm(1.0,var[0][0]);
        objective.addTerm(2.0,var[0][1]);
        objective.addTerm(3.0,var[0][2]);
        //double[] objvals = {1.0, 2.0, 3.0};
        //model.addMaximize(model.scalProd(x, objvals));
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

        IloRange[] cplexConstraints = new IloRange[constraintCount];

        for (constraintIndex = 0; constraintIndex < constraintCount; constraintIndex++) {
            IloLinearNumExpr lhs = model.linearNumExpr();
            Double[] varFactors = varFactorsAllConstraints[constraintIndex];

            for (int varIndex = 0; varIndex < varCount; varIndex++) {
                lhs.addTerm(varFactors[varIndex], cplexVariables[varIndex]);
            }
            IloRange cplexConstraint
                  = model.addLe(lhs,constraintRhs[constraintIndex],constraintNames[constraintIndex]);
            cplexConstraints[constraintIndex] = cplexConstraint;
        }

        rng[0] = cplexConstraints;
        /*
        rng[0] = new IloRange[2];
        rng[0][0] = model.addLe(model.sum(
              model.prod(-1.0, x[0]),
                model.prod( 1.0, x[1]),
                model.prod( 1.0, x[2])), 20.0, "c1");
        rng[0][1] = model.addLe(model.sum(
              model.prod( 1.0, x[0]),
                model.prod(-3.0, x[1]),
                model.prod( 1.0, x[2])), 30.0, "c2");
         */
    }

}
