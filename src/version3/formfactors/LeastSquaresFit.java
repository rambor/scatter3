package version3.formfactors;

import org.apache.commons.math3.linear.*;

import java.util.ArrayList;

/**
 * Created by robertrambo on 20/11/2016.
 */
public class LeastSquaresFit {

    private double scale;
    private double baseline;
    private double score;

    public LeastSquaresFit(int totalq, ArrayList<Double> calculatedIntensities, ArrayList<Double> transformedObservedIntensities, ArrayList<Double> transformedErrors, Double[] qvalues){

        RealMatrix designMatrix = new Array2DRowRealMatrix(totalq, 2);

        for(int j=0; j<totalq; j++){
            designMatrix.setEntry(j,0, calculatedIntensities.get(j).doubleValue());
            designMatrix.setEntry(j,1, qvalues[j]);
        }


        double[] dataForFit = new double[totalq];

        for (int index=0; index<totalq; index++){
            dataForFit[index] = transformedObservedIntensities.get(index);
        }

        RealVector dataVector = new ArrayRealVector(dataForFit, false);
        DecompositionSolver solver = new SingularValueDecomposition(designMatrix).getSolver();

        RealVector solution = solver.solve(dataVector);

        scale = solution.getEntry(0);
        baseline = solution.getEntry(1);

        double chiSq = 0.0d;
        for (int index=0; index<totalq; index++){
            double thing = (transformedObservedIntensities.get(index)-scale*calculatedIntensities.get(index)-baseline*qvalues[index])/transformedErrors.get(index);
            chiSq+= thing*thing;
        }
        score = chiSq/(double)totalq;

    }

    public double getScale(){return scale;}
    public double getBaseline(){return baseline;}
    public double getScore(){return score;}
}
