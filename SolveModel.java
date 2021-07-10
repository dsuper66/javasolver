import ilog.concert.IloException;
import ilog.concert.IloMPModeler;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.util.Map;

public class SolveModel {

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!"); // Display the string.

        ReadCaseFile readCaseFile = new ReadCaseFile();
        ReadCaseFile.readCase();

        ModelElementDataService modelElementDataService = new ModelElementDataService();
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

        modelElementDataService.getElement("offer01");
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
        double[]    lb      = {0.0, 0.0, 0.0};
        double[]    ub      = {40.0, Double.MAX_VALUE, Double.MAX_VALUE};
        String[]    varname = {"x1", "x2", "x3"};
        IloNumVar[] x       = model.numVarArray(3, lb, ub, varname);
        var[0] = x;

        double[] objvals = {1.0, 2.0, 3.0};
        model.addMaximize(model.scalProd(x, objvals));

        rng[0] = new IloRange[2];
        rng[0][0] = model.addLe(model.sum(model.prod(-1.0, x[0]),
                model.prod( 1.0, x[1]),
                model.prod( 1.0, x[2])), 20.0, "c1");
        rng[0][1] = model.addLe(model.sum(model.prod( 1.0, x[0]),
                model.prod(-3.0, x[1]),
                model.prod( 1.0, x[2])), 30.0, "c2");
    }

}
