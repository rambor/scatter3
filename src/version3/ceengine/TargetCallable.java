package version3.ceengine;

import org.apache.commons.math3.util.FastMath;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.concurrent.Callable;
/**
 * Created by Nathan on 30/06/2015.
 *
 * Provides a way to implement multithreading of the target function calculation. Generates a list of target function
 * values for a given list of parameter sets
 */
public class TargetCallable implements Callable {

    private int noOfParams, noToBeSampled, noSamples;
    private int[] sampleCounters;
    private SortedSet<Integer> samples;
    private XYSeries data, errors;
    private PredictedIntensityCurves curves;
    private TargetType targetType;

    /**
     * Creates a new TargetCallable object to be run in a thread.
     *
     * @param targetType     The type of target function to be used
     * @param noSamples      The number of samples
     * @param noToBeSampled  The number of repeat samples
     * @param noOfParams     The number of valid parameter combinations
     * @param sampleCounters The chosen (or sampled) parameter sets
     * @param samples        The indices of the points sample from the data set
     * @param data           The data to for te target function to be evaluated against
     * @param errors         The errors associated with each point in the data
     * @param curves         All the possible curves for generating a 'best guess' curve from to used to evaluated the target function
     */
    public TargetCallable(TargetType targetType, int noSamples, int noToBeSampled, int noOfParams, int[] sampleCounters, SortedSet<Integer> samples, XYSeries data,XYSeries errors, PredictedIntensityCurves curves){
        this.targetType = targetType;
        this.noSamples = noSamples;
        this.noToBeSampled = noToBeSampled;
        this.noOfParams= noOfParams;
        this.sampleCounters= sampleCounters;
        this.samples = samples;
        this.data = data;
        this.errors = errors;
        this.curves = curves;
    }


    /**
     * Calculates the list of target functions linked with sample number.
     * Depending on the argument 'targetType' a variety of different target functions will be used. Defaults to chiSq.
     *
     * @return The list of target function values for given parameter sets
     */
    public ArrayList<Float[]> call(){
        long s = System.currentTimeMillis();
        ArrayList<Float[]> list = new ArrayList();
        switch(targetType){
            case ABSVAL:
                for (int i = 0; i < noSamples; i++) {
                    float targetVal = absVal(Arrays.copyOfRange(sampleCounters, i*noOfParams, (i+1)*noOfParams));                   //CHANGE SO NO COPY!!!!!!!
                    Float[] f = {(float) i, targetVal, 0f};
                    list.add(f);
                }
                break;
            case MEDIAN:
                for (int i = 0; i < noSamples; i++) {
                    float targetVal = median(Arrays.copyOfRange(sampleCounters, i*noOfParams, (i+1)*noOfParams));
                    Float[] f = {(float) i, targetVal, 0f};
                    list.add(f);
                }
                break;
            case NEGENTROPY:
                System.out.println("THIS DOESN'T WORK PROPERLY, CONTINUE AT YOUR OWN RISK!!!");
                for (int i = 0; i < noSamples; i++) {
                    float targetVal = negentropy(Arrays.copyOfRange(sampleCounters, i*noOfParams, (i+1)*noOfParams));
                    Float[] f = {(float) i, targetVal, 0f};
                    list.add(f);
                }
                break;
            default:
                System.out.println("TARGET FUNCTION BROKEN!!!\n Defaulting to Chi Squared");
            case CHISQ:
                for (int i = 0; i < noSamples; i++) {
                    float targetVal = chiSq(Arrays.copyOfRange(sampleCounters, i*noOfParams, (i+1)*noOfParams));
                    Float[] f = {(float) i, targetVal, 0f};
                    list.add(f);
                }
        }

        //System.out.println("Done from " + threadNo + " in " + (System.currentTimeMillis()-s));
        return list;

    }

    //Calculates a value of Chi Squared based on an array of curve indices.
    private float chiSq(int[] counters){
        float chiSq = 0;

        float[] pI = new float[samples.size()];

        float cUp = 0;
        float cDown = 0;
        int i = 0;
        for (Integer element : samples) {
            double error = (double)errors.getY(element);
            pI[i] = averageCurves(counters, element);
            cUp += ((double)data.getY(element)*pI[i])/(error*error);
            double thing = pI[i++]/(error);
            cDown += thing*thing;
        }
        float c = cUp/cDown;

        i = 0;
        for (Integer element : samples) {
            double thing = ((double)data.getY(element)-c*pI[i++])/(double)errors.getY(element);
            chiSq+= thing*thing;
        }
        //System.out.println("sum of chiSq" + chiSq);
        return chiSq/samples.size();
    }

    //Calculates a sum of abs value of the residuals based on an array of curve indices.
    private float absVal(int[] counters){
        float absValSum = 0;
        float[] pI = new float[samples.size()];

        float cUp = 0;
        float cDown = 0;
        int i = 0;
        for (Integer element : samples) {
            double error = (double)errors.getY(element);
            pI[i] = averageCurves(counters, element);
            cUp += ((double)data.getY(element)*pI[i])/(error*error);
            double thing = pI[i++]/(error);
            cDown += thing*thing;
        }
        float c = cUp/cDown;
        //float c = 1e12f;

        //System.out.println("c: " + c);

        i = 0;
        for (Integer element : samples) {
            double residual = ((double)data.getY(element)-c*pI[i++]);
            absValSum += FastMath.abs(residual);           //The Abs value of residual
        }
        return absValSum/samples.size();
    }

    //Calculates a value of mean of the abs value of the residuals based on an array of curve indices.
    private float median(int[] counters){
        float[] pI = new float[samples.size()];

        float cUp = 0;                            //scale factor thing???
        float cDown = 0;
        int i = 0;
        for (Integer element : samples) {
            double error = (double)errors.getY(element);
            pI[i] = averageCurves(counters, element);
            cUp += ((double)data.getY(element)*pI[i])/(error*error);
            double thing = pI[i++]/(error);
            cDown += thing*thing;
        }
        float c = cUp/cDown;

        //float c = 1;

        float[] residuals = new float[samples.size()];
        i = 0;
        for (Integer element : samples) {
            double thing = ((double)data.getY(element)-c*pI[i]);
            residuals[i++] = (float) FastMath.abs(thing);                                    //Arrays of abs val of residuals, sort and find median

        }
        return findMedian(residuals);
    }

    private float findMedian(float[] array){
        Arrays.sort(array);
        float median;

        int lby2 = (int)(array.length*0.5);

        if(array.length % 2 == 0){
            median = (array[lby2] + array[lby2-1])*0.5f;
        }else{
            median = array[lby2];
        }
        return median;
    }

    // Evaluates smoothness (2nd derivative) of distribution, using finite difference then minimise on median smoothness



    //arguments are array of indices for the curves that have been sampled and the point of evaluation. Returns the average at q.
    private float averageCurves(int[] counters, int qIndex){
        float sum = 0;
        for(int i=0; i<noOfParams; i++){
            if(counters[i] != 0){
                sum+=counters[i]*curves.getValue(i, qIndex);
            }
        }
        return sum/noToBeSampled;
    }

    //BROKEN!!!
    private float negentropy(int[] counters){
        float mean, workingMean = 0, sigmaSq, workingSigma = 0;
        int i = 0;

/*        float cUp = 0;                            //scale factor thing???
        float cDown = 0;
        float[] pI = new float[samples.size()];
        for (Integer element : samples) {
            float error = errors[element];
            pI[i] = averageCurves(counters, element);
            cUp += ((double)data.getY(element)*pI[i])/(error*error);
            float thing = pI[i++]/(error);
            cDown += thing*thing;
        }
        float c = cUp/cDown;*/

        float c = 1;

        float[] residuals = new float[samples.size()];
        i = 0;
        for (Integer element : samples) {
            float thing = (float)((double)data.getY(element)-c*averageCurves(counters, element));//pI[i]);
            workingMean += thing;
            workingSigma += thing*thing;
            residuals[i++] = thing;
        }
        mean = workingMean/samples.size();
        sigmaSq = workingSigma/samples.size() - mean*mean;
        int[] binnedResiduals = binResiduals(residuals,51);

        //System.out.println("binned residuals: " + Arrays.toString(binnedResiduals));

        float negentropy = (float)(0.5*(FastMath.log(2*Math.PI*sigmaSq)+1));
        /*for(int element : binnedResiduals){
            if(element != 0){
                negentropy+=element*Math.log(element);
            }
        }*/
        return negentropy;
    }

    private int[] binResiduals(float[] residuals, int noBins){
        Arrays.sort(residuals);

        //System.out.println(Arrays.toString(residuals));

        /*float binWidth = 0.02f/(float)noBins;
        int[] counters = new int[noBins];
        float current = -0.01f + binWidth;
        int counter = 0;
        for(int i=0; i<noBins; i++){
            while(counter < residuals.length && residuals[counter++] < current){
                counters[i]++;
                //counter++;
            }
            current += binWidth;
        }*/

        float binWidth = 0.02f/(float)noBins;
        float current = -0.01f + binWidth;
        int[] counters = new int[noBins];
        int index = 0;
        Integer binNumber = 0;
        while (binNumber < noBins) {
            //ArrayList<Integer> indices = new ArrayList<>();
            Loop:
            while (index < residuals.length) {
                if(residuals[index] > current){
                    break Loop;
                }
                counters[binNumber]++;
                index++;
            }
            current += binWidth;
            binNumber++;
        }

        return counters;
    }

}