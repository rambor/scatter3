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
    //private TreeMap<Double, Integer> cdf;
    private ConcurrentNavigableMap<Double, Integer> cdf;
    private List<Double> modelIntensities;
    private ArrayList<Double> transformedIntensities;
    private ArrayList<Double> transformedErrors;
    private RealMatrix designMatrix;
    private double[] qValues;
    private boolean useNoBackground;

    /**
     *  @param index
     * @param totalToSelect
     * @param cdf cumulative distribution function (probability range for each model)
     * @param list
     * @param modelIntensities pre-calculated model intensities
     * @param totalq
     * @param transformedIntensities
     * @param transformedErrors
     */
    public TestModel(int index,
                     int totalToSelect,
                     ConcurrentNavigableMap<Double, Integer> cdf,
                     TopList list,
                     List<Double> modelIntensities,
                     int totalqInModelIntensities,
                     ArrayList<Double> transformedIntensities,
                     ArrayList<Double> transformedErrors,
                     Double[] qvalues,
                     boolean useNoBackground){

        this.modelIndex = index;
        this.totalToSelect = totalToSelect;
        this.selectedIndices = new ArrayList<>();  // can have redundant indices
        while(selectedIndices.size() < this.totalToSelect) selectedIndices.add(0);

        this.list = list;
        this.cdf = cdf;

        this.totalq = qvalues.length;
        //this.modelIntensities = new ArrayList<>(totalqInModelIntensities);
        this.modelIntensities = modelIntensities;
        this.transformedIntensities = new ArrayList<>(totalq);
        this.transformedErrors = new ArrayList<>(totalq);
        this.useNoBackground = useNoBackground;

        qValues = new double[totalq];

        //synchronized (modelIntensities){
        //    for (double item : modelIntensities) this.modelIntensities.add(item); // can be quite large
        //}

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

//        synchronized (this){
//            for (double item : transformedIntensities) this.transformedIntensities.add(item);
//            for (double item : transformedErrors) this.transformedErrors.add(item);
//
//            for(int i=0; i<totalq; i++){
//                qValues[i] = qvalues[i];
//            }
//        }
    }



    @Override
    public Double call() throws Exception {

        // create the random Ensemble Model
        DecimalFormat formatter = new DecimalFormat("0.#####E0");

        Random random = new Random();
        int index, startIndex;
        Map.Entry<Double, Integer> entry;
        calculatedIntensities = new ArrayList<>();
        while(calculatedIntensities.size() < totalq) calculatedIntensities.add(0.0d);

        for(int i=0; i<totalToSelect; i++){
            double rand = random.nextDouble();
            entry = cdf.floorEntry(rand); // generate random number, find closest Key in CDF
            index = entry.getValue();

            selectedIndices.set(i, index);
            // combine with
            startIndex = index*totalq;
            // sum the intensities for each model
//            synchronized (modelIntensities){
//                for(int j=0; j<totalq; j++){
//                    calculatedIntensities.set(j, calculatedIntensities.get(j).doubleValue() + modelIntensities.get(startIndex+j).doubleValue());
//                }
//            }
            for(int j=0; j<totalq; j++){
                calculatedIntensities.set(j, calculatedIntensities.get(j).doubleValue() + modelIntensities.get(startIndex+j).doubleValue());
            }
        }

        // perform the average by dividing by total
        double invTotal = 1.0/(double)totalToSelect;
        for(int j=0; j<totalq; j++){
            calculatedIntensities.set(j, invTotal*calculatedIntensities.get(j).doubleValue());
        }
        // calculate score
        calculateScore();
        // update list
        list.update(score, modelIndex, this, totalToSelect);
        //System.out.println(modelIndex + " score " + score);
        return score;
    }


    public synchronized ArrayList<Integer> getSelectedIndices(){
        return selectedIndices;
    }

    /**
     * calculate chi-squared like statistic
     *
     */
    private void calculateScore(){

        double scale_c;
        double baseline = 0;

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
        } else {

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

        //int totaldiff = totalq/2;

        // if totaldiff is even, limit = totaldiff
        // else if totaldiff is odd, limit =
//        if ( (totaldiff & 1) != 0 ) {
//            totaldiff+=1;
//        }


        ArrayList<Double> residualsToPartition = new ArrayList<>();
        double sumDiff = 0; // average should be zero for a perfect fit
        double chiSq = 0, diff;
        for (int index=0; index<totalq; index++){
            diff = (transformedIntensities.get(index)-scale_c*calculatedIntensities.get(index)-baseline);
            double thing = diff/transformedErrors.get(index);
            residualsToPartition.add(diff);
            sumDiff += diff;
            chiSq+= thing*thing;
        }

        //Shapiro Wilk calculation
//        double invTotalQ = 1.0/(double)totalq;
//        double averageResidual = invTotalQ*sumDiff;
//        //sumDiff=0;
//        double sumCubed=0, sumSquared=0, sumFourth=0, tempvalue;
//        for (int index=0; index<totalq; index++){
//            diff = (residualsToPartition.get(index) - averageResidual);
//            tempvalue = diff*diff;
//            sumCubed += tempvalue*diff;
//            sumSquared += tempvalue;
//            sumFourth += tempvalue*tempvalue;
//           // sumDiff += tempvalue; // bottom of the Shapiro Wilk statistic
//        }
//        //kurtosis
//        double kurt = (double)totalq*sumFourth/(sumSquared*sumSquared);
//
//        // skew
//        double topSkew = 1.0/(double)totalq*sumCubed;
//        double bottomHalf = sumSquared/((double)totalq - 1.0d);
//        double skew = topSkew/(Math.sqrt(bottomHalf*bottomHalf*bottomHalf));
//        //end skew
//
//        //Jarque-Bera
//        double jb = 1.0/6.0*(totalq - totalToSelect)*(skew*skew + 0.25*(kurt-3.0)*(kurt-3.0));

        // sum diff of residuals
        //Collections.sort(residualsToPartition);
        //double shapirowilk=0;
        //for (int index=0; index<totaldiff; index++){
//            //System.out.println("Index " + index + " " + residualsToPartition.get(index) + " <=> " + residualsToPartition.get(totalq-index-1));
        //    shapirowilk += residualsToPartition.get(totalq-index-1) + residualsToPartition.get(index) ;
        //}
        // END Shapiro Wilk calculation
        // System.out.println(chiSq/(double)totalq + " SW " + (shapirowilk*shapirowilk/sumDiff) + " JB " + jb);
        //score = (shapirowilk*shapirowilk/sumDiff);

        //score = chiSq/(double)totalq + (shapirowilk*shapirowilk/sumDiff);
        //score = chiSq/(double)totalq + jb;
        score = chiSq/(double)totalq;
    }





}
