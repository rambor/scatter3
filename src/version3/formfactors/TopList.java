package version3.formfactors;

import org.apache.commons.math3.special.Gamma;
import org.ejml.simple.SimpleMatrix;
import version3.*;

import javax.xml.bind.SchemaOutputResolver;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by robertrambo on 04/11/2016.
 */
// make threadsafe object to update topList
public class TopList{

    //private ConcurrentSkipListMap<Double, Integer> scoreList;  // strictly positive
    //private ConcurrentSkipListSet<ScoreObject> scoreList;
    private ArrayList<ScoreObject> scoreList;

    private ConcurrentSkipListMap<Integer, ArrayList<Integer>> modelList;
    private List<Double> probabilities;
    //private ArrayList<SortedSet<Double>> setofValuesInParam;
    private int totalProbabilities;
    private double alpha, invStepSize2, invStepSize;
    private int totalParamsFitted;
    // Integer in scoreList corresponds to Integer in ArrayList
    private AtomicInteger counter = new AtomicInteger(0);
    private double smoothnessScore=0;
    private ArrayList<Model> models;
    private boolean useEntropy=false;
    private final int sizeOfList;
    private double lambda;
    private double gap;

    private final double logePI = 0.5*(1.0+Math.log(Math.PI));

    /**
     *
     * @param sizeOfList
     * @param probabilities
     * @param alpha smoothness parameter for updating probabilities, controls weights for updating
     * @param stepsize
     * @param models
     * @param lambda regularizaion parameter
     */
    public TopList(int sizeOfList,
                   ArrayList<Double> probabilities,
                   double alpha,
                   double stepsize,
                   ArrayList<Model> models,
                   double lambda,
                   boolean entropy
    ){
        this.sizeOfList = sizeOfList;
        //scoreList = new ConcurrentSkipListMap();   // sorted by keys
        //scoreList = new ConcurrentSkipListSet<>(new ScoreComp());
        scoreList = new ArrayList<>();
        modelList = new ConcurrentSkipListMap<>(); // sorted by keys

        totalProbabilities = probabilities.size();
        //double degree = Math.log10((double)totalProbabilities);

        ArrayList<Double> temp = new ArrayList<>(totalProbabilities);

        for(int i=0; i<totalProbabilities; i++){
            temp.add(probabilities.get(i));
        }

        //this.probabilities = Collections.synchronizedList(new ArrayList<>(temp));
        this.probabilities = Collections.unmodifiableList(probabilities);

        this.alpha = alpha;
        this.lambda = lambda;
        //double stepsize = stepsize;
        this.invStepSize2 = 1.0/(stepsize*stepsize);
        this.invStepSize = 1.0/stepsize;

        this.models = models;

        totalParamsFitted = this.models.get(0).getTotalFittedParams();

        //totalParamsFitted= 1;
        this.useEntropy = entropy;
    }


    private synchronized void calculateSmoothnessUsing2ndDerivative(int numberOfModelsPerTrial){

        double[] tempProbabilities = calculateTempProbabilities(numberOfModelsPerTrial);

        // calculate second derivative for each parameter based on the models
        smoothnessScore=0;
        int totalModels = models.size();
        double smoothtemp=0;

        for(int p=0; p<totalParamsFitted; p++){
            double secondDerivative=0;
            SortedMap<Double, Double> modelParamsCount = new TreeMap<>();

            // create the projection
            for(int j=0; j<totalModels; j++){
                Model model = models.get(j);
                double param = model.getFittedParamByIndex(p);

                if (modelParamsCount.containsKey(param)){
                    modelParamsCount.put(param, modelParamsCount.get(param) + 1.0d*tempProbabilities[j]);
                } else {
                    modelParamsCount.put(param, 1.0d*tempProbabilities[j]);
                }
            }

           // double invTotalModels = 1.0/(double)modelParamsCount.size();

            // calculate smoothness of the selected distribution using 2nd derivative
            Object[] parameterValues = modelParamsCount.keySet().toArray();
            int totalValues = parameterValues.length-2;
            double f_xminus_h, f_xplus_h, diff;
            double tempStepSize = (double)parameterValues[1] - (double)parameterValues[0];
            double invfactor = 1.0/(12.0*tempStepSize*tempStepSize);

            // calculate diff
            for (int i=2; i<totalValues; i++){
                // calculate derivative for each entry
                f_xminus_h = 16*modelParamsCount.get(parameterValues[i-1]) - modelParamsCount.get(parameterValues[i-2]);
                f_xplus_h = 16*modelParamsCount.get(parameterValues[i+1]) - modelParamsCount.get(parameterValues[i+2]);
                diff = (f_xminus_h-30*modelParamsCount.get(parameterValues[i]) + f_xplus_h);
                secondDerivative += diff*diff;
            }
            //System.out.println(p + " => " + totalParamsFitted + " : " + parameterValues.length);
            //secondDerivative += forward2ndderivative(parameterValues, modelParamsCount);
            //secondDerivative += reverse2ndderivative(parameterValues, modelParamsCount);
            smoothtemp += secondDerivative*invfactor*invfactor/(double)totalValues;
        }

        smoothnessScore = lambda*10000*smoothtemp;
        //smoothnessScore = lambda*secondDerivative/(double)totalParamsFitted;
    }





    private synchronized double[] calculateTempProbabilities(int numberOfModelsPerTrial){

        Set<Map.Entry<Integer, ArrayList<Integer>>> entries = modelList.entrySet();
        SortedMap<Integer, Double> selectedModelCount = new TreeMap<>(); // organized by (parameter, occurence)

        // modelist is sorted by key, not in order of score
        // for each entry, there is an arraylist of integers
        // each integer refers to a model that is defined by a unique parameter set
        // In the next section, we iterate over the selected set and count
        // exclude the last model
        int totalModels=0;
        for(Map.Entry<Integer, ArrayList<Integer>> entry:entries){
            ArrayList<Integer> modelIndices = entry.getValue();

            for(int j=0; j<numberOfModelsPerTrial; j++){

                int keyToCheck = modelIndices.get(j);

                if (selectedModelCount.containsKey(keyToCheck)){
                    selectedModelCount.put(keyToCheck, selectedModelCount.get(keyToCheck) + 1.0d);
                } else {
                    selectedModelCount.put(keyToCheck, 1.0d);
                }
                totalModels+=1;
            }
        }

        // calculate/update temp probabilities
        double inv = 1.0/(double)totalModels;
        double oldprob;
        double[] tempProbabilities = new double[models.size()];

            for(int i=0; i<models.size(); i++){
                int index = models.get(i).getIndex();
                oldprob = (1-alpha)*probabilities.get(index); // get old probability by index

                if (selectedModelCount.containsKey(index)) {
                    tempProbabilities[index] = alpha*selectedModelCount.get(index)*inv + oldprob;
                } else {
                    tempProbabilities[index] = oldprob;
                }
            }

        return tempProbabilities;
    }


    /**
     * MaximumEntropy calculation
     * @param numberOfModelsPerTrial
     */
    private synchronized void calculateEntropy(int numberOfModelsPerTrial){
        // if scoresizeOfList
        // create HashMap
        // for each params, determine the probability distribution
        // create unique values of the parameter space once and use for the lifetime of the class
        smoothnessScore=0;
        double[] tempProbabilities = calculateTempProbabilities(numberOfModelsPerTrial);
        //
        // for each fitted parameter
        // calculate variance and then covariance
        // xyz
        // var_x, var_y, var_z, var_xy, var_yz, var_xz
        // total variances
        SimpleMatrix variances = new SimpleMatrix(totalParamsFitted, totalParamsFitted);

        for(int p=0; p<totalParamsFitted; p++){

            double outer_mean=0, outerSquared=0;
            for(int i=0; i<models.size(); i++){
                double tempvalue;
                double prob;
                Model tempModel = models.get(i);
                prob = tempModel.getProbability();
                // outer block
                tempvalue = tempModel.getFittedParamByIndex(p);
                outerSquared += tempvalue*tempvalue*prob;
                outer_mean += tempvalue*prob;
            }
            double variance_outer = outerSquared - outer_mean*outer_mean;
            variances.set(p,p, variance_outer);

            for(int c=(p+1); c<totalParamsFitted; c++){
                // for each column, calculate covariances
                double inner_mean=0, tempvalue;
                double prob;
                double innerSquared=0, cross=0;

                for(int i=0; i<models.size(); i++){
                    Model tempModel = models.get(i);
                    prob = tempModel.getProbability();
                    tempvalue = tempModel.getFittedParamByIndex(c);
                    cross += prob*tempModel.getFittedParamByIndex(p)*tempvalue;

                }
                variances.set(p,c, (cross - outer_mean*inner_mean));
            }
        }

        // covariance matrices are symmetric
        // calculate entropy
        double smoothness=0;
        int totalModels = models.size();
        if (totalParamsFitted >1 ){ // determinant
            /* a b
             * c d
             * det => ad - bc
             * var_x*var_y - cov_2
             */
            smoothness = totalParamsFitted*logePI + 0.5*Math.log(variances.determinant());
            // do digamma
            for(int j=1; j<=totalParamsFitted; j++){
                smoothness -= 0.5*Gamma.digamma(0.5*(totalModels-j));
            }

        } else {

            // calculate entropy of distribution
            double temp;
            for(int i=0; i<models.size(); i++){
                temp =tempProbabilities[i];
                smoothness += temp*Math.log(temp);
            }
        }

        smoothnessScore = lambda*10*smoothness;
    }


    private synchronized double forward2ndderivative(Object[] parameterValues, SortedMap<Double, Double> modelParamsCount){
        double diff=0;
        double first = (double)parameterValues[0];

        diff = ((double)parameterValues[1]-first);

            double f_xplus_h = 0;
            double f_xplus_2h = 0;
            if (diff*invStepSize< 2){ // reverse step
                f_xplus_h = modelParamsCount.get(parameterValues[1]);
            }

            diff = ((double)parameterValues[2]-first);
            if (diff*invStepSize< 2){ // forward step
                f_xplus_2h = modelParamsCount.get(parameterValues[2]);
            }

            diff = (f_xplus_2h-2*f_xplus_h+modelParamsCount.get(parameterValues[0]))*invStepSize2;

        //System.out.println("forward : " + diff);
        //return Math.abs(diff);
        //return Math.abs((f_xplus_h - modelParamsCount.get(parameterValues[0]))*invStepSize);
        return diff*diff;
    }



    private synchronized double reverse2ndderivative(Object[] parameterValues, SortedMap<Double, Double> modelParamsCount){
        double diff=0;
        double f_xminus_h = 0;
        double f_xminus_2h = 0;

        int total = parameterValues.length;
        Object previous = parameterValues[total-2];
        Object before = parameterValues[total-3];
        double last = (double)parameterValues[total-1];

        diff = (last - (double)previous);

            if (diff*invStepSize < 2){ // reverse step
                f_xminus_h = modelParamsCount.get(previous);
            }

            diff = (last - (double)before);
            if (diff*invStepSize < 2){ // forward step
                f_xminus_2h = modelParamsCount.get(before);
            }

            diff = (modelParamsCount.get(last) - 2*f_xminus_h + f_xminus_2h)*invStepSize2;

        //return Math.abs((modelParamsCount.get(last) - f_xminus_h)*invStepSize);
        return diff*diff;
    }




    private synchronized double calculate2ndDerivativeTestModel(TestModel testmodel, int numberOfModelsPerTrial){

        //double[] tempProbabilities = calculateTempProbalitiesExcludeLast(numberOfModelsPerTrial, testmodel);
        Set<Map.Entry<Integer, ArrayList<Integer>>> entries = modelList.entrySet();
        SortedMap<Integer, Double> selectedModelCount = new TreeMap<>(); // organized by (parameter, occurence)

        int lastKey = scoreList.get(scoreList.size()-1).getIndex();
        // modelist is sorted by key, not in order of score
        // for each entry, there is an arraylist of integers
        // each integer refers to a model that is defined by a unique parameter set
        // In the next section, we iterate over the selected set and count
        // exclude the last model
        int totalModels=0;
        for(Map.Entry<Integer, ArrayList<Integer>> entry:entries){
            ArrayList<Integer> modelIndices = entry.getValue();

            if (entry.getKey() != lastKey){ // last model
                for(int j=0; j<numberOfModelsPerTrial; j++){

                    int keyToCheck = modelIndices.get(j);

                    if (selectedModelCount.containsKey(keyToCheck)){
                        selectedModelCount.put(keyToCheck, selectedModelCount.get(keyToCheck) + 1.0d);
                    } else {
                        selectedModelCount.put(keyToCheck, 1.0d);
                    }
                    totalModels+=1;
                }
            }
        }

        // add contribution from putative model
        for(int j=0; j<numberOfModelsPerTrial; j++){
            int keyToCheck = testmodel.getSelectedIndices().get(j);

            if (selectedModelCount.containsKey(keyToCheck)){
                selectedModelCount.put(keyToCheck, selectedModelCount.get(keyToCheck) + 1.0d);
            } else {
                selectedModelCount.put(keyToCheck, 1.0d);
            }
            totalModels+=1;
        }

        // calculate/update temp probabilities
        double inv = 1.0/(double)totalModels;
        double oldprob;
        double[] tempProbabilities = new double[models.size()];
        for(int i=0; i<models.size(); i++){
            int index = models.get(i).getIndex();
            oldprob = (1-alpha)*probabilities.get(index); // get old probability by index

            if (selectedModelCount.containsKey(index)) {
                tempProbabilities[index] = alpha*selectedModelCount.get(index)*inv + oldprob;
            } else {
                tempProbabilities[index] = oldprob;
            }
        }

        // checking end
        totalModels = models.size();
        double smoothtemp=0;

        for(int p=0; p<totalParamsFitted; p++){
            double secondDerivative=0;
            SortedMap<Double, Double> modelParamsCount = new TreeMap<>();

            // create the projection
            for(int j=0; j<totalModels; j++){
                Model model = models.get(j);
                double param = model.getFittedParamByIndex(p);

                if (modelParamsCount.containsKey(param)){
                    modelParamsCount.put(param, modelParamsCount.get(param) + 1.0d*tempProbabilities[j]);
                } else {
                    modelParamsCount.put(param, 1.0d*tempProbabilities[j]);
                }
            }

            double invTotalModels = 1.0/(double)modelParamsCount.size();
            // calculate smoothness of the selected distribution using 2nd derivative
            Object[] parameterValues = modelParamsCount.keySet().toArray(); // should be sorted
            int totalValues = parameterValues.length-2;
            double f_xminus_h, f_xplus_h, center, diff;
            double tempStepSize = (double)parameterValues[1] - (double)parameterValues[0];
            double invfactor = 1.0/(12.0*tempStepSize*tempStepSize);

            // calculate diff
            for (int i=2; i<totalValues; i++){
                // calculate derivative for each entry
                f_xminus_h = 16*modelParamsCount.get(parameterValues[i-1]) - modelParamsCount.get(parameterValues[i-2]);
                f_xplus_h = 16*modelParamsCount.get(parameterValues[i+1]) - modelParamsCount.get(parameterValues[i+2]);
                diff = (f_xminus_h - 30*modelParamsCount.get(parameterValues[i]) + f_xplus_h);
                secondDerivative += diff*diff;
            }

            //secondDerivative += forward2ndderivative(parameterValues, modelParamsCount);
            //secondDerivative += reverse2ndderivative(parameterValues, modelParamsCount);
            smoothtemp += secondDerivative*invfactor*invfactor/(double)totalValues;
        }

        return (lambda*10000*smoothtemp);
    }


    /**
     * Calculate probability model excluding last model in list
     *
     * @param numberOfModelsPerTrial
     * @param model
     * @return
     */
    private synchronized double[] calculateTempProbalitiesExcludeLast(int numberOfModelsPerTrial, TestModel model){

        Set<Map.Entry<Integer, ArrayList<Integer>>> entries = modelList.entrySet();

        // entropy of bivariate distribution
        // variance of each parameter and then pairwise
        // sigma_x, sigma_y, sigma_xy
        // sum => probability*parameter
        SortedMap<Integer, Double> selectedModelCount = new TreeMap<>(); // organized by (parameter, occurence)

        int lastKey = scoreList.get(scoreList.size()-1).getIndex();
        // modelist is sorted by key, not in order of score
        // for each entry, there is an arraylist of integers
        // each integer refers to a model that is defined by a unique parameter set
        // In the next section, we iterate over the selected set and count
        // exclude the last model
        int totalModels=0;
        for(Map.Entry<Integer, ArrayList<Integer>> entry:entries){
            ArrayList<Integer> modelIndices = entry.getValue();

            if (entry.getKey() != lastKey){ // last model
                for(int j=0; j<numberOfModelsPerTrial; j++){

                    int keyToCheck = modelIndices.get(j);

                    if (selectedModelCount.containsKey(keyToCheck)){
                        selectedModelCount.put(keyToCheck, selectedModelCount.get(keyToCheck) + 1.0d);
                    } else {
                        selectedModelCount.put(keyToCheck, 1.0d);
                    }
                    totalModels+=1;
                }
            }
        }


        // add contribution from putative model
        for(int j=0; j<numberOfModelsPerTrial; j++){
            int keyToCheck = model.getSelectedIndices().get(j);

            if (selectedModelCount.containsKey(keyToCheck)){
                selectedModelCount.put(keyToCheck, selectedModelCount.get(keyToCheck) + 1.0d);
            } else {
                selectedModelCount.put(keyToCheck, 1.0d);
            }
            totalModels+=1;
        }

        // calculate/update temp probabilities
        double inv = 1.0/(double)totalModels;
        double oldprob;
        double[] tempProbabilities = new double[models.size()];
        for(int i=0; i<models.size(); i++){
            int index = models.get(i).getIndex();
            oldprob = (1-alpha)*probabilities.get(index); // get old probability by index

            if (selectedModelCount.containsKey(index)) {
                tempProbabilities[index] = alpha*selectedModelCount.get(index)*inv + oldprob;
            } else {
                tempProbabilities[index] = oldprob;
            }
        }

        return tempProbabilities;
    }


    /**
     * Maximum Entropy calculation
     *
     * @param model
     * @param numberOfModelsPerTrial
     * @return
     */
    private synchronized double calculateEntropyOfTestModel(TestModel model, int numberOfModelsPerTrial){
        //
        // for each fitted parameter
        // calculate variance and then covariance
        // xyz
        // var_x, var_y, var_z, var_xy, var_yz, var_xz
        // total variances
        double[] tempProbabilities = calculateTempProbalitiesExcludeLast(numberOfModelsPerTrial, model);

        SimpleMatrix variances = new SimpleMatrix(totalParamsFitted, totalParamsFitted);

        for(int p=0; p<totalParamsFitted; p++){

            double outer_mean=0, outerSquared=0;
            for(int i=0; i<models.size(); i++){
                double tempvalue;
                double prob;
                Model tempModel = models.get(i);
                prob = tempModel.getProbability();
                // outer block
                tempvalue = tempModel.getFittedParamByIndex(p);
                outerSquared += tempvalue*tempvalue*prob;
                outer_mean += tempvalue*prob;
            }

            double variance_outer = outerSquared - outer_mean*outer_mean;
            variances.set(p,p, variance_outer);

            for(int c=(p+1); c<totalParamsFitted; c++){
                // for each column, calculate covariances
                double inner_mean=0, tempvalue;
                double prob;
                double cross=0;

                for(int i=0; i<models.size(); i++){
                    Model tempModel = models.get(i);
                    prob = tempModel.getProbability();
                    tempvalue = tempModel.getFittedParamByIndex(c);
                    cross += prob*tempModel.getFittedParamByIndex(p)*tempvalue;

                }
                variances.set(p,c, (cross - outer_mean*inner_mean));
            }
        }

        // covariance matrices are symmetric
        // calculate entropy
        int totalModels = models.size();
        double smoothness = 0.0d;
        if (totalParamsFitted >1 ){ // determinant
            /* a b
             * c d
             * det => ad - bc
             * var_x*var_y - cov_2
             */
            smoothness = totalParamsFitted*logePI + 0.5*Math.log(variances.determinant());
            // do digamma
            for(int j=1; j<=totalParamsFitted; j++){
                smoothness -= 0.5*Gamma.digamma(0.5*(totalModels-j));
            }

        } else {
            // calculate entropy of distribution
            double temp;
            for(int i=0; i<models.size(); i++){
                temp =tempProbabilities[i];
                smoothness += temp*Math.log(temp);
            }
        }

        return lambda*10*smoothness;
    }


    /**
     * Update Method maintains the top ten list and must be synchronized since it will
     * be accessed from multiple cores
     *
     * @param score
     * @param index
     * @param model
     * @param numberOfModelsPerTrial
     */
    public synchronized void update(double score, int index, TestModel model, int numberOfModelsPerTrial, double scale){
        // iterate over the list
        // start in reverse
        // and if score is better than
        // insert and remove last
        //
        if (counter.get() < sizeOfList){

            modelList.put(index, new ArrayList<Integer>(numberOfModelsPerTrial));
            for (Integer item : model.getSelectedIndices()) modelList.get(index).add(item);


            if (useEntropy){
                this.calculateEntropy(numberOfModelsPerTrial);
            } else {
                this.calculateSmoothnessUsing2ndDerivative(numberOfModelsPerTrial);
            }

            scoreList.add(new ScoreObject(score, index, scale));
            counter.set(scoreList.size());
            Collections.sort(scoreList, new ScoreComp());

        } else {
            // double currentBest = scoreList.lastKey() + smoothnessScore;
            int lastIndex = scoreList.size()-1;
            double currentBest = scoreList.get(lastIndex).getScorevalue() + smoothnessScore;
            // new configuration has to be better than the last and have a better overall smoothness

            double testSmooth = (useEntropy) ? this.calculateEntropyOfTestModel(model, numberOfModelsPerTrial) : this.calculate2ndDerivativeTestModel(model, numberOfModelsPerTrial);

            if ((score + testSmooth)< currentBest) {
                // popoff last
                // insert new entry
                modelList.remove(scoreList.get(lastIndex).getIndex());
                scoreList.remove(lastIndex);

                scoreList.add(new ScoreObject(score, index, scale));
                modelList.put(index, new ArrayList<Integer>(numberOfModelsPerTrial));
                // add indices from model to newly created entry
                for (Integer item : model.getSelectedIndices()) modelList.get(index).add(item);

                Collections.sort(scoreList, new ScoreComp());

                smoothnessScore = testSmooth;
            }
        }
    }

    /**
     *
     * ConcurrentSkipListMap<Double, Integer> scoreList;  // strictly positive
     * Integer is the index of the random trial set
     * @return List of models used in the randomized selection
     */
    public Integer[] getModelIndicesFromScore(){

        int size = scoreList.size();
        Integer[] returnthis = new Integer[size];

        int index=0;
        for(ScoreObject s : scoreList){
            returnthis[index] = s.getIndex();
            index++;
        }

        return returnthis;
    }


    /**
     * Calculate average score in list
     * @return
     */
    public double getAverageScore(){

        double value = 0.0d;

        int index=0;
        for(ScoreObject s : scoreList){
            value += s.getScorevalue();
            index++;
        }

        double average = (value/(double)index);

        double first = scoreList.get(0).getScorevalue();
        double last = scoreList.get(scoreList.size()-1).getScorevalue();
        gap = last-first;

        System.out.println("FIRST: " + first + " LAST: " + last +  "  AVG: " + average + " GAP: "+ gap + " SIZE: " + scoreList.size() + " " +counter.get());
        return Math.log10(average);
    }


    public ArrayList<Integer> getModelIndicesByKey(int key){
        return modelList.get(key);
    }

    /**
     * returns the difference between first and last member of the top N list
     * @return
     */
    public double getGap(){return gap;}


    public void print(){
        int count=0;
        System.out.println("SIZE OF SCORELIST => " + scoreList.size() + " CNT " + counter.get());

        for(ScoreObject s : scoreList){
            System.out.println(count + " SCORE LIST KEY : " + s.getIndex() + " => " + s.getScorevalue());
            count++;
        }
    }


    /**
     * copies top list
     * key is score
     *
     * @param keptList
     * @param numberOfModelsPerTrial
     */
    public void copyToKeptList(KeptModels keptList, int numberOfModelsPerTrial){

        for(ScoreObject s : scoreList){ // should be sorted
            keptList.addModel(s.getScorevalue(), modelList.get(s.getIndex()), s.getScale());
        }
    }


    class ScoreComp implements Comparator<ScoreObject>{

        @Override
        public int compare(ScoreObject o1, ScoreObject o2) {
            if (o1.getScorevalue() > o2.getScorevalue()) return 1;
            if (o1.getScorevalue() < o2.getScorevalue()) return -1;
            return 0;
        }
    }

}