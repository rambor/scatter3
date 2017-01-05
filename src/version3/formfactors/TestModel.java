package version3.formfactors;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by robertrambo on 04/11/2016.
 * TestModel creates an ensemble average from a set of pre-calculated intensities
 *
 */
public class TestModel implements Callable<Double> {

    private ArrayList<Integer> selectedIndices;
    private ArrayList<Double> calculatedIntensities;
    private TopList list;
    private int modelIndex;
    private int totalq;
    private int totalToSelect;
    private double score;
    private double scale_c;
    private ConcurrentNavigableMap<Double, Integer> cdf;
    private List<Double> modelIntensities;
    private ArrayList<Double> transformedIntensities;
    private ArrayList<Double> transformedErrors;
    private ArrayList<Model> models;
    private RealMatrix designMatrix;
    private double[] qValues;
    private boolean useNoBackground;
    private int totalqInModelIntensities;
    private boolean useVolumeScaling = true;

    /**
     *  @param index
     * @param totalToSelect
     * @param cdf cumulative distribution function (probability range for each model)
     * @param list
     * @param modelIntensities pre-calculated model intensities
     * @param totalqInModelIntensities
     * @param transformedIntensities
     * @param transformedErrors
     */
    public TestModel(int index,
                     int totalToSelect,
                     ConcurrentNavigableMap<Double, Integer> cdf,
                     TopList list,
                     ArrayList<Model> models,
                     List<Double> modelIntensities,
                     int totalqInModelIntensities,
                     ArrayList<Double> transformedIntensities,
                     ArrayList<Double> transformedErrors,
                     Double[] qvalues,
                     boolean useNoBackground,
                     boolean useVolumeScaling){

        this.modelIndex = index;
        this.totalToSelect = totalToSelect;
        this.selectedIndices = new ArrayList<>();  // can have redundant indices
        while(selectedIndices.size() < this.totalToSelect) selectedIndices.add(0);
        this.totalqInModelIntensities = totalqInModelIntensities;
        this.list = list;
        this.cdf = cdf;
        this.models = models;

        this.totalq = qvalues.length;

        this.transformedIntensities = new ArrayList<>(totalq);
        this.transformedErrors = new ArrayList<>(totalq);
        this.useNoBackground = useNoBackground;
        this.useVolumeScaling = useVolumeScaling;
        qValues = new double[totalq];


        this.modelIntensities = modelIntensities;

        synchronized (transformedIntensities){
            for (double item : transformedIntensities) this.transformedIntensities.add(item);
        }

        synchronized (transformedErrors){
            for (double item : transformedErrors) this.transformedErrors.add(item);
        }

        synchronized (qvalues){
            for(int i=0; i<totalq; i++){
                qValues[i] = qvalues[i];
            }
        }
    }


    @Override
    public Double call() throws Exception {

        List<Double> modelIntensitiesTemp;
//        if (totalqInModelIntensities < 1000000){
//            System.out.println("MAKING COPY : Total Model Intensities => " + totalqInModelIntensities);
//            modelIntensitiesTemp = new ArrayList<>(totalqInModelIntensities);
//            synchronized (modelIntensities){
//                for (double item : modelIntensities) modelIntensitiesTemp.add(item); // can be quite large
//            }
//        } else {
//            modelIntensitiesTemp = this.modelIntensities;
//        }

        modelIntensitiesTemp = this.modelIntensities;
        // create the random Ensemble Model
        DecimalFormat formatter = new DecimalFormat("0.#####E0");

        Random random = new Random();
        int index, startIndex;

        calculatedIntensities = new ArrayList<>();
        while(calculatedIntensities.size() < totalq) calculatedIntensities.add(0.0d);

        double totalVolume = 0;
        for(int i=0; i<totalToSelect; i++){
            double rand = random.nextDouble();

            index = cdf.floorEntry(rand).getValue();
            selectedIndices.set(i, index);
            // combine with
            startIndex = index*totalq;
            // sum the intensities for each model
            totalVolume += models.get(index).getVolume();

            double volumeScaling=1.0d;
            if (!useVolumeScaling){
                volumeScaling = 1.0/(models.get(index).getVolume());
            }
            for(int j=0; j<totalq; j++){
                calculatedIntensities.set(j, calculatedIntensities.get(j).doubleValue() + volumeScaling*modelIntensitiesTemp.get(startIndex+j).doubleValue());
            }
        }

        // perform the average by dividing by total
        double invTotal = 1.0/(double)totalToSelect;
        for(int j=0; j<totalq; j++){
            calculatedIntensities.set(j, invTotal*calculatedIntensities.get(j).doubleValue());
            // uncomment for volume weighting
        }

        // calculate score
        calculateScore();
        // update list
        list.update(score, modelIndex, this, totalToSelect, scale_c);
        return score;
    }

    /**
     * print selected indices
     */
    private void printIndices(){
        for(int i=0; i<totalToSelect; i++){
            System.out.println(modelIndex + " MODEL INDEX " + selectedIndices.get(i));
        }
    }

    public synchronized ArrayList<Integer> getSelectedIndices(){
        return selectedIndices;
    }

    /**
     * calculate chi-squared like statistic
     *
     */
    private void calculateScore(){

        double baseline = 0.0d;

        if (useNoBackground){
            float cUp = 0;
            float cDown = 0;
            double down, error, calc;
            // calculate scale, c
            for (int index=0; index<totalq; index++){
                error = 1.0/transformedErrors.get(index);
                calc = calculatedIntensities.get(index);
                cUp += transformedIntensities.get(index)*calc*error*error;

                down = calc*error;
                cDown += down*down;
            }

            scale_c = cUp/cDown;
        } else { // fit background to data
            designMatrix = new Array2DRowRealMatrix(totalq, 2);

            for(int j=0; j<totalq; j++){
                designMatrix.setEntry(j,0, calculatedIntensities.get(j).doubleValue());
                designMatrix.setEntry(j,1, qValues[j]);
            }

            double[] dataForFit = new double[totalq];
            for (int index=0; index<totalq; index++){
                dataForFit[index] = transformedIntensities.get(index);
            }

            RealVector dataVector = new ArrayRealVector(dataForFit, false);
            DecompositionSolver solver = new SingularValueDecomposition(designMatrix).getSolver();

            RealVector solution = solver.solve(dataVector);

            scale_c = solution.getEntry(0);
            baseline = solution.getEntry(1);
        }

        double chiSq = 0, diff;
        for (int index=0; index<totalq; index++){
            diff = (transformedIntensities.get(index)-scale_c*calculatedIntensities.get(index)-baseline);
            double thing = diff/transformedErrors.get(index);
            chiSq+= thing*thing;
        }

        score = chiSq/(double)(totalq - 2);
    }

    public double getScale_c(){return scale_c;}
}
