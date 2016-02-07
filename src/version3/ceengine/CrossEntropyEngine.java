package version3.ceengine;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.FastMath;
import org.jfree.data.xy.XYSeries;
import version3.ceengine.Utils.CDFThing;
import version3.ceengine.Utils.ChiSqComparator;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.DoubleStream;

/**
 * Created by Nathan on 24/06/2015.
 */
public class CrossEntropyEngine{

    private float averageTarget;
    private CDFThing cdf;
    private float dmax;
    private XYSeries data, errors;
    private ArrayList<ParameterSet> pVector;
    private PredictedIntensityCurves curves;
    private int[] counters;                                                   //Parametrises the pdf.
    private final RandomDataGenerator rand = new RandomDataGenerator();
    private HashMap<Integer, Integer[]> binnedData;
    private int[] chosenParams, top, lastCounters;

    //private ArrayList<Integer> chosenParams;                             //HERE!!!!!!!!!

    private int noOfParams;
    private boolean running;

    private final static int noThreads = 4;
    public final static ExecutorService pool = Executors.newFixedThreadPool(noThreads);

    private int maxIter, noToBeSampled;
    private int noSamples = 1, topNo = -1;
    private int noSampledFromBin;
    private int convergenceParameter;                      //The number of iterations the convergence is met before exit.
    private float convergenceLimit;                        //The limit to determine 'convergence'.
    private boolean multi, smoothnessBoolean;
    private float scale;

    public CrossEntropyEngine(){
        this(true, false, 1e3f);
    }

    public CrossEntropyEngine(int maxIter, int noToBeSampled, int noSamples, int topNo, int noSampledFromBin, int convergenceParameter, float convergenceLimit, boolean multi, boolean smoothnessBoolean, float scale){
        this.maxIter = maxIter;
        this.noToBeSampled = noToBeSampled;
        this.noSamples = noSamples;
        this.topNo = topNo;
        this.noSampledFromBin = noSampledFromBin;
        this.convergenceParameter = convergenceParameter;
        this.convergenceLimit = convergenceLimit;
        this.multi = multi;
        this.smoothnessBoolean = smoothnessBoolean;
        this.scale = scale;
    }

    public CrossEntropyEngine(boolean multi, boolean smoothnessBoolean, float scale){
        maxIter = 1000;
        noToBeSampled = 500;
        noSampledFromBin = 2;
        convergenceParameter = 5;
        convergenceLimit = 1e-4f;
        this.multi = multi;
        this.smoothnessBoolean = smoothnessBoolean;
        this.scale = scale;
    }

    //Setters used to import into the engine
    public void setDmax(float dmax){
        this.dmax = dmax;
    }

    public void setData(XYSeries data, XYSeries errors){
        this.data = scaleData(data);
        this.errors = scaleData(errors);
    }

    private XYSeries scaleData(XYSeries data){
        for(int i = 0; i < data.getItemCount(); i++){
            double y = (double)data.getY(i);
            data.updateByIndex(i,scale*y);
        }
        return data;
    }

    public void setParams(ArrayList<ParameterSet> params){
        pVector = params;
        noOfParams = params.size();
        System.out.println("no. of Params:"+noOfParams);
        //if(noSamples == -1){
        noSamples = 17*noOfParams;
        //}
        //if(topNo == -1){
        topNo = (int) (noSamples*0.11);
        //}
    }

    public void setCurves(PredictedIntensityCurves curves){
        this.curves = curves;
    }

    public void resetCounters(){
        for(int i = 0; i < counters.length; i++){
            lastCounters[i] = counters[i];
            counters[i] = 0;
        }
    }

    /**
     * Chooses a specified number of parameter sets to analyse. Records the place in the array. Splits one array in to blocks of samples
     */
    private void sampleParams(){
        long s = System.currentTimeMillis();
        if(multi){
            int noPerThread = noSamples/noThreads;
            Set<Future> set = new HashSet<>();
            for(int i = 0; i < noThreads-1; i++){
                Runnable r = new ParamSampleRunnable(i*noPerThread*noToBeSampled, (i+1)*noPerThread*noToBeSampled, new CDFThing(cdf), chosenParams);
                Future future = pool.submit(r);
                set.add(future);
            }
            Runnable r = new ParamSampleRunnable((noThreads-1)*noPerThread*noToBeSampled, chosenParams.length, new CDFThing(cdf), chosenParams);
            Future future = pool.submit(r);
            set.add(future);

            for(Future f : set){
                try{
                    f.get();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            //No Multi
            int counter = 0;
            for(int i = 0; i < noSamples; i++){
                for(int j = 0; j < noToBeSampled; j++){
                    chosenParams[counter++] = cdf.pickRandom();
                }
            }
        }
    }


    /**
     * Splits the data into bins of equal width. Held in a hashMap - key is bin number, value is an array of indices of
     * the points in that bin in the original data series.
     */
    private void binData(){
        binnedData = new HashMap();

        double qLow = data.getMinX();
        double qMax = data.getMaxX();
        int noBins = (int) Math.ceil((dmax*qMax/Math.PI));                  //from n*PI >= qMax*rMax (SAMPLING THEOREM)   - RANDOM *300 - no rMax ?????????????
        double width = (qMax-qLow)/((double) noBins);

        System.out.println("qLow : "+qLow);
        System.out.println("qHigh : "+qMax);
        System.out.println("Bin width : "+width);
        System.out.println("Number of bins: "+noBins);

        double currentHighQ = qLow+width;
        int index = 0;
        Integer binNumber = 0;
        while(binNumber < noBins){
            ArrayList<Integer> indices = new ArrayList<>();
            Loop:
            while(index < data.getItemCount()){
                if((double) data.getX(index) > currentHighQ){
                    break Loop;
                }
                indices.add(index);
                index++;
            }
            binnedData.put(binNumber, indices.toArray(new Integer[indices.size()]));    //Converts indices to an array - necessary???
            currentHighQ += width;
            binNumber++;
        }
    }

    /*
     * Samples a number of data points from each bin.
     * Closes program ATM if more samples than points are requested.
     * No multithreading as make longer and is un-necassary
     * Returns a SortedSet contain the indices in the data XYSeries of the samples
     */
    private SortedSet<Integer> sampleFromBins(int noOfSamples){
        long s = System.currentTimeMillis();
        SortedSet<Integer> binnedSamples = new TreeSet();
        Iterator i = binnedData.entrySet().iterator();
        int counter = 0;
        while(i.hasNext()){
            Integer[] indices = (Integer[]) ((Map.Entry) i.next()).getValue();
            if(noOfSamples > indices.length){
                System.out.println("ERROR! Cannot sample "+noOfSamples+" unique points from  bin "+counter+" as there are only "+indices.length+" different points");
            }else if(noOfSamples == indices.length){
                int index = indices[0];
                for(int j = 0; j < noOfSamples; j++){
                    binnedSamples.add(index++);
                }
            }else{
                for(int j = 0; j < noOfSamples; j++){
                    int randInt = rand.nextInt(indices[0], indices[indices.length-1]);
                    if(!binnedSamples.add(randInt)){                            //ensures that points are not sampled twice
                        j--;
                    }
                }
            }
            counter++;
        }

        System.out.println("Time for bin sampling: "+(System.currentTimeMillis()-s));

        System.out.println("Bin-samples Size: " + binnedSamples.size());

        return binnedSamples;
    }

    //TODO: v3 - Make it remembers the 'failures' as well as the 'successes' - for possible Bayesian Stuff.

    /*
     * Converts the list of chosen parameters into a pdf style array containing the number of times each parameter is chosen.
     */
    private int[] sampleCounter(int[] chosenParams){
        int[] counters = new int[noSamples*noOfParams];
        int counter = 0;
        for(int i = 0; i < noSamples; i++){
            for(int j = 0; j < noToBeSampled; j++){
                counters[i*noOfParams+chosenParams[counter++]]++;
            }
        }
        return counters;
    }

    /* Produces an array containing all the members of the r samples that had the smallest chiSqs
     * Sets all chiSq into an ArrayList, then adds the 'topNo' smallest to an array
     */
    private void topTarget(SortedSet<Integer> samples, TargetType targetType){
        //long s = System.currentTimeMillis();

        ArrayList<Float[]> topList = new ArrayList();
        int[] sampleCounters = sampleCounter(chosenParams);

        if(true){                                                       //if(multi) - PUT THIS IN FOR MULTITHREADED IMPLEMENTATION CHOICE.
            System.out.println("MCS");
            int noPerThread = noSamples/noThreads;
            Set<Future<ArrayList<Float[]>>> set = new HashSet<>();
            for(int i = 0; i < noThreads-1; i++){
                Callable<ArrayList<Float[]>> callable = new TargetCallable(targetType, noPerThread, noToBeSampled, noOfParams, sampleCounters, samples, data, errors, curves);
                Future<ArrayList<Float[]>> future = pool.submit(callable);
                set.add(future);
            }
            Callable<ArrayList<Float[]>> callable = new TargetCallable(targetType, noPerThread+noSamples%noThreads, noToBeSampled, noOfParams, sampleCounters, samples, data, errors, curves);
            Future<ArrayList<Float[]>> future = pool.submit(callable);
            set.add(future);

            for(Future<ArrayList<Float[]>> f : set){
                try{
                    topList.addAll(f.get());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            //DOESN'T WORK - Included for future expansion (Don't know if non multithreaded implementation is required?)
        }
        ListIterator<Float[]> it = topList.listIterator();

        //TODO: average target things and divide by 100. use this a param for comparator. pass to comparator via constructor!!!

        if(smoothnessBoolean){                                          //Smoothness - ONLY WORKS WITH ONE DIMENSION (I.E. SPHERES)!!!
            float[] smoothness = smoothArray(sampleCounters);
            int counter = 0;
            while(it.hasNext()){
                float sum = 0;
                for(int i = 0; i < noOfParams; i++){
                    sum += sampleCounters[counter]*smoothness[counter++];
                }
                Float[] f = it.next();
                it.set(new Float[]{f[0], f[1], sum});
            }
        }

        try{
            Collections.sort(topList, new ChiSqComparator());
        }catch(IllegalArgumentException e){
            running = false;
        }

        it = topList.listIterator();
        int sampleCounter = 0;
        int arrayCounter = 0;

        averageTarget = 0;

        int noParams = pVector.get(0).getParamNumbers();

        while(it.hasNext() && sampleCounter < topNo){
            Float[] now = it.next();
            int sampleNo = (int) (float) now[0];
            for(int j = 0; j < noToBeSampled; j++){
                int index = chosenParams[sampleNo*noToBeSampled+j];
                top[arrayCounter++] = index;
            }
            sampleCounter++;
        }
    }

    //TODO: COULD BE MULTITHREADED?

    // Works out 2nd deriv for sample counters
    private float[] smoothArray(int[] sampleCounters){
        float[] smoothArray = new float[noSamples*noOfParams];
        float h = pVector.get(1).getParam(0)-pVector.get(0).getParam(0);

        for(int i = 0; i < noSamples; i++){
            int place = i*noOfParams;
            int before = sampleCounters[place];
            int now = sampleCounters[place+1], after;
            smoothArray[place] = 0;
            for(int j = 2; j < noOfParams; j++){
                after = sampleCounters[place+j];
                smoothArray[place+j-1] = Math.abs(calcSecondDeriv(before, now, after, h));
                before = now;
                now = after;
            }
            smoothArray[place+noOfParams-1] = 0;
        }

        return smoothArray;
    }

    private float calcSecondDeriv(int before, int now, int after, float h){
        return (before-2*now+after)/(h*h);
    }

    //Resets all counters to 0, updates the counters and then updates the CDF.
    private void updateCounters(){
        resetCounters();
        for(int element : top){
            counters[element]++;
        }
        cdf.update(counters);
    }

    /**
     * Solves the problem. Will not display the process graphically.
     *
     * @return An array containing the normalised PDF
     */
    public float[] solve(){
        return solve(true, false, TargetType.CHISQ);
    }

    public static boolean thing = true;

    /**
     * Solves the problem. Will normalise the distribution or display the process graphically as requested.
     *
     * @param normalised Whether the PDf is to be normalised
     * @param graphics   Whether the process is to be displayed graphically
     * @return An array containing the PDF
     */
    public float[] solve(boolean normalised, boolean graphics, TargetType tType){
        running = true;

        counters = new int[noOfParams];

        Arrays.fill(counters,1);                                                        //HERE!!!!!

        lastCounters = new int[noOfParams];
        binData();
        cdf = new CDFThing(counters);
        top = new int[topNo*noToBeSampled];
        chosenParams = new int[noToBeSampled*noSamples];
        int counter = 0;

        System.out.println();
/*
        PlotFrame f = new PlotFrame(pVector, curves.getType());
        GraphFrame gF = new GraphFrame("Curve", data, top, curves, errors, true);
        if(graphics){
            f.setVisible(true);
            f.distribution();
            f.addStopListener(new StopListener(){
                @Override
                public void handleEvent(ActionEvent e){
                    running = false;
                }
            });

            gF.pack();
            gF.setVisible(true);
        }


        JFrame startFrame = new JFrame();
        JButton startB = new JButton("START");
        startB.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                CrossEntropyEngine.thing = false;
            }
        });
        startFrame.add(startB);
        startFrame.pack();
        startFrame.setVisible(true);
        while(thing){
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e){
            }
        }
*/
        Loop:
        for(int i = 0; i < maxIter; i++){

            long start = System.currentTimeMillis();
            System.out.println(i);

            sampleParams();
            topTarget(sampleFromBins(noSampledFromBin), tType);
            updateCounters();

            float kl = KLDivergence();

//            if(graphics){
//                gF.update(top);
//                f.update(kl, i, top, noOfParams, noToBeSampled*topNo, gF.getScale());
//            }

            if(kl < convergenceLimit && i>0){
                counter++;
            }else{
                counter = 0;
            }

            System.out.println(System.currentTimeMillis()-start);

            if(counter == convergenceParameter || !running){
                break Loop;
            }
        }

//        if(graphics){
//            f.converged();
//        }

        float[] finalNumbers = new float[noOfParams];

        if(normalised){
            for(int i = 0; i < top.length; i++){
                finalNumbers[top[i]] += 1/(float) (noToBeSampled*topNo);
            }
        }else{
            for(int i = 0; i < top.length; i++){
                finalNumbers[top[i]]++;
            }
        }

        return finalNumbers;
    }

    // Calculated the KLDivergence
    private float KLDivergence(){
        float KLD = 0;
        for(int i = 0; i < noOfParams; i++){
            if(counters[i] != 0){
                KLD += (((double) lastCounters[i])/(double) topNo)*Math.log(((double) lastCounters[i])/(double) (counters[i]));
            }
        }

        System.out.println("Divergence thing: " + KLD/noOfParams);

        return KLD/noOfParams;
    }


    //SOME UTILITIES:

    /**
     * Normalises the given distribution to 1
     *
     * @param distribution the distribtuion to be normalised
     * @return             the normalised distribtuion
     */
    public static float[] normalise(int[] distribution){
        double[] dDistribution = new double[distribution.length];
        for(int i = 0; i < distribution.length; i++){
            dDistribution[i] = (double) distribution[i];
        }
        return doNormalisation(dDistribution);
    }

    /**
     * Normalises the given distribution to 1
     *
     * @param distribution the distribtuion to be normalised
     * @return             the normalised distribtuion
     */
    public static float[] normalise(float[] distribution){
        double[] dDistribution = new double[distribution.length];
        for(int i = 0; i < distribution.length; i++){
            dDistribution[i] = (double) distribution[i];
        }
        return doNormalisation(dDistribution);
    }

    //Provides the implemntation to do the normalisation
    private static float[] doNormalisation(double[] distribution){
        float[] nDist = new float[distribution.length];
        float sum = (float) DoubleStream.of(distribution).sum();
        for(int i = 0; i < distribution.length; i++){
            nDist[i] = (float) distribution[i]/sum;
        }
        return nDist;
    }

    /**
     * Multiplies a set of data by a given power of q
     *
     * @param transformThis the data to be transformed
     * @param power         the power to transform the data by
     * @return              the transformed data set
     */
    public static XYSeries transformData(XYSeries transformThis, int power, double lower, double upper){
        XYSeries data = new XYSeries("Transformed " + transformThis.getKey() + " - q^" + power + "*I(q)");

        for(int i = 0; i < transformThis.getItemCount(); i++){
            double q = (double) transformThis.getX(i);
            if (q >= lower && q <= upper){
                data.add(q, FastMath.pow(q, power)*(double) transformThis.getY(i));
            }
        }
        return data;
    }


}

