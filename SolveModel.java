import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolveModel {

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!"); // Display the string.

        ModelDataService modelDataService = new ModelDataService();
        //ReadCaseFile readCaseFile = new ReadCaseFile();
        ReadCaseFile.readCase(modelDataService);
        PreProcessing.calculateDerivedProperties(modelDataService);
        ConstraintBuilder.readConstraints();
        ConstraintBuilder.processConstraintDefs(modelDataService);
        //ReadConstraints.readConstraints(constraintBuilder);

        /*
        modelElementDataService.addElement(
                "bus01",
                "bus");
        modelElementDataService.addElement(
                "bid01",
                "bid",
                Map.of("fromBus","bus01",
                        "price","100",
                        "quantity","10")
        );
        modelElementDataService.addElement(
                "offer01",
                "offer",
                Map.of("toBus","bus01",
                        "price","70",
                        "quantity","200")
        );

        modelElementDataService.getElement("offer01");*/

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
        List<IloNumVar> cplexVariables = Arrays.asList(new IloNumVar[varCount]);
        cplexVariables.set(0,model.numVar(0.0,40.0,"x1"));
        cplexVariables.set(1,model.numVar(0.0,Double.MAX_VALUE,"x2"));
        cplexVariables.set(2,model.numVar(0.0,Double.MAX_VALUE,"x3"));

        var[0] = new IloNumVar[3];
        var[0][0] = cplexVariables.get(0); //model.numVar(0.0,40.0,"x1");
        var[0][1] = cplexVariables.get(1); //model.numVar(0.0,Double.MAX_VALUE,"x2");
        var[0][2] = cplexVariables.get(2); //model.numVar(0.0,Double.MAX_VALUE,"x3");
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

        //Constraints
        int constraintCount = 2;
        List<List<Double>> varFactorsAllConstraints = new ArrayList(constraintCount);
        List<Double>varFactorsThisConstraint = Arrays.asList(new Double[varCount]);
        List<String> constraintNames = Arrays.asList(new String[constraintCount]);
        List<Double>constraintRhs = Arrays.asList(new Double[constraintCount]);

        int constraintIndex = 0;
        constraintNames.set(constraintIndex,"c1");
        constraintRhs.set(constraintIndex,20.0);
        varFactorsThisConstraint.set(0,-1.0);
        varFactorsThisConstraint.set(1,1.0);
        varFactorsThisConstraint.set(2,1.0);
        varFactorsAllConstraints.set(constraintIndex,varFactorsThisConstraint);

        constraintIndex = 1;
        constraintNames.set(constraintIndex,"c2");
        constraintRhs.set(constraintIndex,30.0);
        varFactorsThisConstraint.set(0,1.0);
        varFactorsThisConstraint.set(1,-3.0);
        varFactorsThisConstraint.set(2,1.0);
        varFactorsAllConstraints.set(constraintIndex,varFactorsThisConstraint);

        List<IloRange> cplexConstraints = Arrays.asList(new IloRange[constraintCount]);

        rng[0] = new IloRange[2];
        rng[0][0] = model.addLe(model.sum(
              model.prod(-1.0, x[0]),
                model.prod( 1.0, x[1]),
                model.prod( 1.0, x[2])), 20.0, "c1");
        rng[0][1] = model.addLe(model.sum(
              model.prod( 1.0, x[0]),
                model.prod(-3.0, x[1]),
                model.prod( 1.0, x[2])), 30.0, "c2");
    }

}
