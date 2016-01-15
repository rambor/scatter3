package version3;

import org.ejml.factory.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by robertrambo on 15/01/2016.
 */
public class Scaler implements Runnable {

    private Dataset reference;
    private Dataset target;
    private XYSeries referenceSet;
    private XYSeries referenceErrorSet;
    private XYSeries targetSet;
    private XYSeries targetErrorSet;
    private double lower, upper;
    private boolean useRange = false;
    private int targetID;
    //private ScaleManager manager;
    private String statustext;


    public Scaler(Dataset reference, Dataset target, double lower, double upper, int targetID) {
        this.reference = reference;
        this.referenceSet = reference.getOriginalPositiveOnlyData();
        this.referenceErrorSet = reference.getOriginalPositiveOnlyError();

        this.target = target;
        this.targetSet = target.getOriginalPositiveOnlyData();
        this.targetErrorSet = target.getOriginalPositiveOnlyError();

        this.lower = lower;
        this.upper = upper;
        //this.manager = manager;

        if (lower != upper && lower < upper && lower > 0 && upper > 0){
            useRange = true;
        }
        this.targetID = targetID;
    }

    @Override
    public void run() {

        XYSeries scaleRef = new XYSeries("ref");
        XYSeries scaleTemp = new XYSeries("temp");
        XYSeries scaleError = new XYSeries("error");
        XYDataItem refItem;

        int startRef = reference.getStart() - 1;
        int endRef = reference.getEnd() - 1;
        int ndLast = targetSet.getItemCount() - 4;

        double startRefq = reference.getOriginalPositiveOnlyData().getX(startRef).doubleValue();
        double endRefq = reference.getOriginalPositiveOnlyData().getX(endRef).doubleValue();

        // if range is specified, need to truncate data
        double startTargetq = target.getOriginalPositiveOnlyData().getX(target.getStart()-1).doubleValue();
        double endTargetq = target.getOriginalPositiveOnlyData().getX(target.getEnd()-1).doubleValue();

        double lowerq = Math.max(startRefq, startTargetq);
        double upperq = Math.min(endRefq, endTargetq);

        if (useRange) {
            lowerq = lower;
            upperq = upper;
        }

        double refXValue;
        int ssIndexOf;

        double targetStart = targetSet.getX(1).doubleValue();
        double targetEnd = targetSet.getX(ndLast).doubleValue();

        //Build common set of q values against ref DataSet
        for (int i = startRef; i <= endRef; i++){

            // Is the ref q value given by i in Target and within selected range
            refItem = referenceSet.getDataItem(i);
            refXValue = refItem.getXValue();

            if ((refXValue >= lowerq) && (refXValue <= upperq)  ) { // i must be in overlapping region
                //Is the ref q-vale in target?
                //If it is not, then we need to determine if we have to interpolate
                ssIndexOf = targetSet.indexOf(refXValue);
                if (ssIndexOf > -1) { //

                    scaleRef.add(refItem);
                    scaleTemp.add(targetSet.getDataItem(ssIndexOf));
                    scaleError.add(targetErrorSet.getDataItem(ssIndexOf));

                } else {
                    // if reference q-value is not in target, interpolate its value
                    // make sure the requested value is not in the first two or last two
                    if ((refXValue > targetStart) && (refXValue < targetEnd)){
                        /*
                         * interpolateOriginal does not requires log10 data
                         */
                        Double[] results =  Functions.interpolateOriginal(targetSet, targetErrorSet, refXValue, 1);
                        Double[] sigmaResults = Functions.interpolateSigma(targetErrorSet, refXValue);

                        if (!results[1].isNaN()){
                            scaleRef.add(refItem);
                            scaleTemp.add(refXValue, results[1]);
                            scaleError.add(refXValue, sigmaResults[1]);
                        }
                    }
                }
            }
        }


        double scale = scaleData(scaleTemp, scaleError, scaleRef); //scale uses

        if (scaleRef.getItemCount() < 3){
            statustext = "Not enough overlapping points using LMS, try I(0) scaling. Target Dataset => " + targetID;
            System.out.println(statustext);
            //status.setText("Not enough overlapping points using LMS, try I(0) scaling.");
            return;
        }

        if (Double.isNaN(scale)){
            statustext = "Not enough overlapping points using LMS, try I(0) scaling. Target Dataset => " + targetID;
            System.out.println(statustext);
            //status.setText("Something went wrong, try removing noisy points and rescale.");
            return;
        }

        target.setScaleFactor(1.0/scale);
    }

    public String getStatustext(){return statustext;}

    /**
     *
     * @param target
     * @param errorTarget
     * @param ref
     * @return
     */
    private double scaleData(XYSeries target, XYSeries errorTarget, XYSeries ref){
        SimpleMatrix tempRef = new SimpleMatrix(1,1);
        SimpleMatrix tempTarget =new SimpleMatrix(1,1);
        SimpleMatrix residualsVector;
        ArrayList<Double> residualsFromVector = new ArrayList<Double>();

        int samplingLimit;
        int min = (int)Math.round(ref.getItemCount()*0.045);   //minimum points 6.5% of total
        int size = ref.getItemCount();
        int[] randomNumbers;
        double startRef = ref.getX(0).doubleValue();
        double endRef = ref.getY(ref.getItemCount()-1).doubleValue();
        double scaleFactor = 1.0;
        double medianValue=0;
        float tempScale;
        int samplingRounds = 2000; // sets sampling maximum
        /*
         * For each round, pick a random set of integers from size of ref dataset
         */

        for (int i = 0; i < samplingRounds; i++) {
            //pick random integer
            samplingLimit = min + (int)(Math.random()*((size - min) + 1));
            randomNumbers = randomArray(samplingLimit, size);

            tempRef.reshape(samplingLimit, 1);
            tempTarget.reshape(samplingLimit, 1);
            try {
                for (int j = 0; j < samplingLimit; j++) {

                    // make sure selected Index is not the first 3 or last 3 in ref

                    tempRef.set(j, 0, ref.getY(randomNumbers[j]).doubleValue());

                    // determine if current q value is present in Target, is so add it, if not interpolate
                    if (target.indexOf(ref.getX(randomNumbers[j])) > -1) {
                        tempTarget.set(j, 0, target.getY(target.indexOf(ref.getX(randomNumbers[j]))).doubleValue());
                    } else { // do the interpolation
                        Double[] interpolated =  Functions.interpolateOriginal(target, errorTarget, ref.getX(randomNumbers[j]).doubleValue(), 1);
                        if (!interpolated[1].isNaN()){
                            tempTarget.set(j, 0, interpolated[1]);
                        } else {
                            tempTarget.set(j, 0, target.getY((int)Math.random()*(target.getItemCount()-1)).doubleValue());
                        }
                    }
                }
            } catch (Exception e) {

            }
            // Need an exception handler for the solver, get NaN or Inf sometimes
            // For Ax=b
            // A.solve(b) => ref*scale = target
            try {
                tempScale = (float) tempRef.solve(tempTarget).get(0,0);
            } catch ( SingularMatrixException e ) {
                scaleFactor = 1.0;
                return scaleFactor;
            }

            //calculate residuals
            residualsVector = tempRef.scale(tempScale).minus(tempTarget);
            residualsFromVector.clear();
            //Squared residual
            for (int r=0; r < residualsVector.numRows(); r++){
                residualsFromVector.add(Math.pow(residualsVector.get(r, 0),2));
            }


            if (i == 0) {
                medianValue = Statistics.calculateMedian(residualsFromVector, true);
                scaleFactor = (float)tempScale;
            } else if (Statistics.calculateMedian(residualsFromVector, true) < medianValue ){
                medianValue = Statistics.calculateMedian(residualsFromVector, true);
                scaleFactor = (float)tempScale;
            }
        }

        return scaleFactor;
    }

    private int[] randomArray(int limit, int max){
        List<Integer> sequence = new ArrayList<Integer>();
        int[] values;
        //sequence.clear();
        //create ArrayList of integers upto max value
        for (int i = 0; i < max; i++){
            sequence.add(i);
        }

        values = new int[limit];

        for (int i = 0; i < limit; i++){
            int position = (int)(Math.random()*max);
            values[i] = sequence.get(position);
            sequence.remove(position);
            max = sequence.size();
        }
        Arrays.sort(values);
        return values;
    }

}