package version3.formfactors;

import org.apache.commons.math3.util.FastMath;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import version3.Dataset;
import version3.Functions;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by robertrambo on 27/10/2016.
 */
public class FormFactorEngine extends SwingWorker<Void, Void> {

    private int cpuCores=1;
    private Dataset dataset;
    private double alpha = 0.63;
    private double percentData;
    private ModelType model;
    private int rounds;
    private int topN; // top N models to select out of total trials
    private int trials; // number of random trials per round
    private int bins;
    private int totalSearchSpace=0;
    private int modelsPerTrial;
    private ArrayList<Model> models;
    private Double[] qvalues;
    private ArrayList<Double> fittedIntensities;
    private ArrayList<Double> transformedIntensities;
    private ArrayList<Double> fittedErrors;
    private ArrayList<Double> transformedErrors;

    private ArrayList<Double> workingSetIntensities;
    private ArrayList<Double> workingSetErrors;

    private ArrayList<Double> fittedQValues;
    private ArrayList<Integer> qIndicesToFit;
    private int totalQValues;
    private int totalQIndicesToFit;
    private double[] minParams;
    private double[] maxParams;
    private double[] delta;
    private double solventContrast;
    private double[] particleContrasts;
    private double qmax, qmin, epsilon;
    private HashMap<Integer, Double> probabilities;
    //private TreeMap<Double, Integer> cdf;
    private ConcurrentNavigableMap cdf;
    private ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList;  // strictly positive

    private JProgressBar progressBar;
    private boolean barSet = false;
    private boolean useNoBackground = false;

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel residualsPanel;
    private JPanel heatMapPanel;
    private JPanel heatMap1Panel;
    private JPanel heatMap2Panel;
    private JPanel dataPlotPanel;

    private JPanel crossSection1Panel;
    private JPanel crossSection2Panel;
    private JPanel crossSection3Panel;
    private JPanel crossSection4Panel;


    private XYSeries scorePerRound;

    private JFreeChart progressChart;

    /**
     * Per Round, we will do N trials where each trial contains m random models per trial
     * @param dataset
     * @param model
     * @param cores
     * @param rounds
     * @param minParams
     * @param maxParams
     * @param delta
     * @param qmin
     * @param qmax
     * @param epsilon
     * @param top
     * @param solventContrast
     * @param particleContrasts
     * @param trials
     * @param randomModelsPerTrial
     */
    public FormFactorEngine(Dataset dataset,
                            ModelType model,
                            int cores,
                            int rounds,
                            double[] minParams,
                            double[] maxParams,
                            double[] delta,
                            double qmin,
                            double qmax,
                            double epsilon,
                            double percentData,
                            int top,
                            double solventContrast,
                            double[] particleContrasts,
                            int trials,
                            int randomModelsPerTrial,
                            boolean useNoBackground){

        this.dataset = dataset;
        this.model = model;
        cpuCores = cores;
        this.rounds = rounds;
        this.trials = trials;
        this.modelsPerTrial = randomModelsPerTrial;
        this.percentData = percentData;
        models = new ArrayList<>();

        this.minParams=minParams;
        this.maxParams=maxParams;
        this.delta=delta;
        this.qmax = qmax;

        // smallest possible value is PI/qmax
        if (Math.PI/qmax < minParams[0]){
            this.minParams[0] = Math.floor(Math.PI/qmax);
        }

        this.qmin = qmin;
        this.topN = top;
        this.epsilon = epsilon;
        this.useNoBackground = useNoBackground;

        // index -> intensity curve
        // arraylist of models
        XYSeries alldata = dataset.getAllData();
        XYSeries allError = dataset.getAllDataError();
        totalQValues=0;
        ArrayList<Double> qvalueslist = new ArrayList<>();
        fittedIntensities = new ArrayList<>();
        transformedIntensities = new ArrayList<>();
        fittedErrors = new ArrayList<>();
        transformedErrors = new ArrayList<>();

        // extract dataset to fit from allData that is bounded
        for (int i=0; i<alldata.getItemCount(); i++){
            double xvalue = alldata.getX(i).doubleValue();
            if (xvalue >= this.qmin && xvalue <= this.qmax){
                fittedIntensities.add(alldata.getY(i).doubleValue());
                fittedErrors.add(allError.getY(i).doubleValue());
                qvalueslist.add(xvalue);
                totalQValues++;
            }
        }

        // array of qvalues
        qvalues = new Double[totalQValues];
        qvalues = qvalueslist.toArray(qvalues);
        fittedQValues = new ArrayList<>();

        this.solventContrast = solventContrast;
        this.particleContrasts = particleContrasts;
        // minParams[]
        // sphere :
        // 0: lower radii
        // ellipse :
        // 0: lower minor axis
        // 1: lower major axis
        //

        // maxParams[]
        // sphere :
        // 0: upper radii
        // ellipse :
        // 0: upper minor axis
        // 1: upper major axis

    }

    @Override
    protected Void doInBackground() throws Exception {

        double[] params;
        System.out.println("CALCULATING " + this.model + " MODELS");
        double major, minor, dmaxOfSet=0;
        double minLimit;
        ScheduledExecutorService ellipseExecutor;
        double deltaqr = 0.00001;

        switch(model){

            case SPHERICAL:
                // create spherical models at different radii
                params = new double[1];
                params[0] = minParams[0];
                dmaxOfSet = 2*maxParams[0];

                while(params[0] < maxParams[0]){
                    models.add(new Sphere(totalSearchSpace, solventContrast, particleContrasts, params, qvalues));
                    params[0] += delta[0];
                    totalSearchSpace++;
                }
                break;
            case THREE_BODY:

                ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
                List<Future<ThreeBody>> threeBodiesFutures = new ArrayList<>();
                minLimit = minParams[0];
                params = new double[3];   // [0] => R_1, [1] => R_2, [2] => R_3
                params[2] = maxParams[0]; //
                dmaxOfSet = 2*maxParams[0]*3.0;

                while(params[2] > minLimit){ // sphere 1
                    // set R_b
                    params[1] = params[2]; // sphere 2
                    while (params[1] >= minLimit){
                        // set R_c
                        params[0] = params[1];
                        while (params[0] >= minLimit){ // sphere 3

                            double maxr13 = (2*params[1]+params[2]+params[0]); // furthest sphere_1 and sphere_2 can be
                            double minr13 = params[2] + params[0];             //closest they can be

                            // set the distances between first and third sphere
                            for(double rad=minr13; rad<maxr13; rad += delta[0]){
                                //System.out.println(totalSearchSpace + " PARAMS " + params[0] + " " + params[1] + " " + params[2] + " rad " + rad);
                                Future<ThreeBody> future = ellipseExecutor.submit(new CallableThreeBody(
                                        totalSearchSpace,
                                        solventContrast,
                                        particleContrasts,
                                        params,
                                        rad,
                                        qvalues
                                ));
                                threeBodiesFutures.add(future);
                                totalSearchSpace++;
                            }
                            // Callable object models.get(totalSearchSpace)
                            params[0] -= delta[0];
                        }
                        params[1] -= delta[0];
                    }
                    params[2] -= delta[0];
                }

                int madeModels = 0;
                progressBar.setStringPainted(true);
                progressBar.setString("Making Models");
                progressBar.setMaximum(totalSearchSpace);
                progressBar.setValue(0);
                for(Future<ThreeBody> fut : threeBodiesFutures){
                    try {
                        //print the return value of Future, notice the output delay in console
                        // because Future.get() waits for task to get completed
                        models.add(fut.get());
                        //update progress bar
                        //System.out.println("Creating Search Space : " + models.size() + " Total Models of " + totalSearchSpace);
                        madeModels++;
                        progressBar.setValue(madeModels);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                ellipseExecutor.shutdown();
                break;
            case CORESHELL:
                break;
            case CORESHELLBINARY:
                break;
            case PROLATE_ELLIPSOID:
                // if R_a > R_c
                ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
                List<Future<ProlateEllipsoid>> epfutures = new ArrayList<>();
                params = new double[2];
                minLimit = minParams[0];  // lower limit of minor axis
                params[0] = maxParams[0]; // this is R_a
                dmaxOfSet = 2*maxParams[0];

                while(params[0] > minLimit){
                    // set R_c
                    params[1] = params[0] - delta[0];
                    while (params[1] >= minLimit){
                        Future<ProlateEllipsoid> future = ellipseExecutor.submit(new CallableProlateEllipsoid(
                                totalSearchSpace,
                                solventContrast,
                                particleContrasts,
                                params,
                                qvalues
                        ));

                        epfutures.add(future);
                        params[1] -= delta[0];
                        totalSearchSpace++;
                    }
                    params[0] -= delta[0];
                }

                int ellipses = 0;
                progressBar.setStringPainted(true);
                progressBar.setString("Making Models");
                progressBar.setMaximum(totalSearchSpace);
                progressBar.setValue(0);
                for(Future<ProlateEllipsoid> fut : epfutures){
                    try {
                        //print the return value of Future, notice the output delay in console
                        // because Future.get() waits for task to get completed
                        models.add(fut.get());
                        //((Ellipse)models.get(models.size()-1)).printParams();
                        //update progress bar
                        //System.out.println("Creating Search Space : " + models.size() + " Total Models of " + totalSearchSpace);
                        ellipses++;
                        progressBar.setValue(ellipses);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                ellipseExecutor.shutdown();
                break;

            case OBLATE_ELLIPSOID:
                // if R_c > R_a
                ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
                List<Future<ProlateEllipsoid>> obfutures = new ArrayList<>();

                params = new double[2];
                minLimit = minParams[0];  // lower limit of minor axis
                params[1] = maxParams[0]; // this is R_c
                dmaxOfSet = 2*maxParams[0];

                while(params[1] > minLimit){
                    // set R_a
                    params[0] = params[1] - delta[0];
                    while (params[0] >= minLimit){

                        Future<ProlateEllipsoid> future = ellipseExecutor.submit(new CallableProlateEllipsoid(
                                totalSearchSpace,
                                solventContrast,
                                particleContrasts,
                                params,
                                qvalues
                        ));

                        obfutures.add(future);
                        params[0] -= delta[0];
                        totalSearchSpace++;
                    }
                    params[1] -= delta[0];
                }

                ellipses = 0;
                progressBar.setStringPainted(true);
                progressBar.setString("Making Models");
                progressBar.setMaximum(totalSearchSpace);
                progressBar.setValue(0);
                for(Future<ProlateEllipsoid> fut : obfutures){
                    try {
                        // because Future.get() waits for task to get completed
                        models.add(fut.get());
                        //update progress bar
                        ellipses++;
                        progressBar.setValue(ellipses);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                ellipseExecutor.shutdown();
                break;

            case ELLIPSOID:
                // triaxial ellipsoid model
                // iterate from largest ellipsoid to smallest ellipsoid
                // largest ellipsoid = maxParams[0]
                minLimit = minParams[0];
                params = new double[3];   // [0] => R_a, [1] => R_b, [2] => R_c
                params[2] = maxParams[0]; // this is R_c
                dmaxOfSet = 2*maxParams[0];
                System.out.println("MAX " +  params[2] + " MIN " + minLimit + " " + delta[0]);
                // create atomic integer array that will hold weights
                // if interval is 0.01, then x,y is 100*100 = 10000 points
                // if interval is 0.001 then x,y is 1000*1000 = 1x10^6
                //
                ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
                List<Future<Ellipse>> futures = new ArrayList<>();

                while(params[2] > minLimit){
                    // set R_b
                    params[1] = params[2] - delta[0];
                    while (params[1] >= minLimit){
                        // set R_c
                        params[0] = params[1]-delta[0];
                        while (params[0] >= minLimit){
                            // models.add(new Ellipse(totalSearchSpace, solventContrast, particleContrasts, params, qvalues));
                            // ellipseExecutor.execute(models.get(totalSearchSpace));
                            Future<Ellipse> future = ellipseExecutor.submit(new CallableEclipse(
                                    totalSearchSpace,
                                    solventContrast,
                                    particleContrasts,
                                    params,
                                    qvalues,
                                    deltaqr
                                    ));
                            futures.add(future);
                            // Callable object models.get(totalSearchSpace)
                            params[0] -= delta[0];
                            totalSearchSpace++;
                        }
                        params[1] -= delta[0];
                    }
                    params[2] -= delta[0];
                }

                ellipses = 0;
                progressBar.setStringPainted(true);
                progressBar.setString("Making Models");
                progressBar.setMaximum(totalSearchSpace);
                progressBar.setValue(0);
                for(Future<Ellipse> fut : futures){
                    try {
                        //print the return value of Future, notice the output delay in console
                        // because Future.get() waits for task to get completed
                        models.add(fut.get());
                        //update progress bar
                        //System.out.println("Creating Search Space : " + models.size() + " Total Models of " + totalSearchSpace);
                        ellipses++;
                        progressBar.setValue(ellipses);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                ellipseExecutor.shutdown();
                break;
        }

        System.out.println("TOTAL : " + models.get(0).getTotalIntensities());
        // transform data
        transformData();
        // create working dataset for fitting
        bins = (int)(qmax*dmaxOfSet/Math.PI) + 1;

        System.out.println("Max Shannon Bins " + bins);
        int startIndex=0;
        //double deltaq = (qmax-qmin)/(double)bins;
        double deltaq = qmax/(double)bins;

        qIndicesToFit = new ArrayList<>();
        // for each bin, grab at least 3 points to fit
        // if percent is 1, grab all the indices within the specified range
        if (percentData > 0.99){
            for(int j=0; j<totalQValues; j++){
                qIndicesToFit.add(j);
            }
        } else {
            for(int i=1; i<bins; i++){
                double upper = deltaq*i;
                int upperIndex=totalQValues;

                // find first value > upper
                for(int q=startIndex; q<totalQValues; q++){
                    double test = qvalues[q];
                    if (test > upper){
                        upperIndex = q;
                        break;
                    }
                }
                //System.out.println("StartIndex " + startIndex + " " + upperIndex + " " + upper + " <= " + qmax);
                if (upperIndex > 0){
                    int[] indices = Functions.randomIntegersBounded(startIndex, upperIndex, percentData);
                    startIndex = upperIndex;
                    int totalToAdd = indices.length;
                    for(int j=0; j<totalToAdd; j++){
                        qIndicesToFit.add(indices[j]);
                    }
                }
            }
        }

        totalQIndicesToFit = qIndicesToFit.size();
        Double[] qValuesInSearch = new Double[totalQIndicesToFit];
        for(int j=0; j<totalQIndicesToFit; j++) {
            qValuesInSearch[j] = qvalues[qIndicesToFit.get(j)];
        }

        createWorkingSet();
        // create mega array
        // array will be accessed via multithreads
        int totalIntensitiesOfModels = totalQIndicesToFit*models.size();
        List<Double> modelIntensities = Collections.synchronizedList(new ArrayList<>(totalIntensitiesOfModels));
        // hash map
        probabilities = new HashMap();
        double value = 1.0/(double)models.size();
        // Double is value in CDF and Integer is index of model
        for(int i=0; i<models.size(); i++){
            Model model = models.get(i);
            probabilities.put(i, value);
            synchronized (modelIntensities){
                for(int j = 0; j< totalQIndicesToFit; j++){
                    modelIntensities.add(model.getIntensity(qIndicesToFit.get(j)));
                }
            }
        }
        // modelIntensities is read only from this point on.

        //createTestData();
        /*
         * pick top X configurations
         * update probabilities for each
         * update hash map and sort by probabilities
         * use CDF to pick
         */
        populateCDF();
        scorePerRound = new XYSeries("Score per Round");
        keptList = new ConcurrentSkipListMap<Double, ArrayList<Integer> >();
        progressBar.setMaximum(rounds);
        progressBar.setValue(0);
        progressBar.setString("CE Search");
        fitLoop:
        for(int round=0; round<rounds; round++){
            // select N models based on probabilities
            // fit model
            // if change, epsilon < 0.001, break;
            ExecutorService executor = Executors.newFixedThreadPool(cpuCores);
            ArrayList<Future<Double>> futures = new ArrayList<>();
            TopList tempTopList = new TopList(topN);
            // create random configuration based on probabilities in cdf
            // cdf is immutable per round
            for(int r=0; r < trials; r++){
                // TestModel will update the tempTopList
                futures.add(executor.submit(
                        new TestModel(
                                r,
                                modelsPerTrial,
                                cdf,
                                tempTopList,
                                modelIntensities,
                                totalIntensitiesOfModels,
                                workingSetIntensities,
                                workingSetErrors,
                                qValuesInSearch,
                                useNoBackground)));
            }

//            for(Future<Double> future : futures)
//            {
//                try
//                {
//                    System.out.println("Future result is - " + " - " + future.get() + "; And Task done is " + future.isDone());
//                }
//                catch (InterruptedException | ExecutionException e)
//                {
//                    e.printStackTrace();
//                }
//            }

            // Use TopList to update CDF
            //
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Finished Round " + round);
            // tempTopList.print();
            // update probabilities
            updateCDF(tempTopList);
            //
            // update plots
            // 1. distribution plot
            // 2. residual plot
            // print fit
            //printFit(modelIntensities);

            progressBar.setValue(round+1);
            System.out.println("Round " + round);
            scorePerRound.add(round, tempTopList.getAverageScore());
        }
        // print fit

        progressBar.setString("Finished");
        // make plots/graphs
        switch(model){

            case SPHERICAL:
                // create spherical models at different radii
                SpherePlots plots = new SpherePlots(
                        residualsPanel,
                        heatMapPanel,
                        leftPanel,
                        rightPanel,
                        dataPlotPanel,
                        models,
                        keptList,
                        probabilities,
                        qvalues,
                        transformedIntensities,
                        transformedErrors,
                        useNoBackground);
                plots.create(true);
                break;
            case CORESHELL:

                break;
            case CORESHELLBINARY:

                break;
            case ELLIPSOID:
                EllipsoidPlots eplots = new EllipsoidPlots(
                        residualsPanel,
                        crossSection1Panel,
                        crossSection2Panel,
                        crossSection3Panel,
                        crossSection4Panel,
                        heatMapPanel,
                        heatMap1Panel,
                        heatMap2Panel,
                        dataPlotPanel,
                        models,
                        keptList,
                        probabilities,
                        qvalues,
                        transformedIntensities,
                        transformedErrors,
                        useNoBackground);
                eplots.create(true);
                break;
            case PROLATE_ELLIPSOID: case OBLATE_ELLIPSOID:
                EllipsoidPlots prplots = new EllipsoidPlots(
                        residualsPanel,
                        crossSection1Panel,
                        crossSection2Panel,
                        crossSection3Panel,
                        crossSection4Panel,
                        heatMapPanel,
                        heatMap1Panel,
                        heatMap2Panel,
                        dataPlotPanel,
                        models,
                        keptList,
                        probabilities,
                        qvalues,
                        transformedIntensities,
                        transformedErrors,
                        useNoBackground);
                prplots.create(true);
                break;
        }

        makeProgressPlot();

        progressBar.setValue(0);
        return null;
    }


    /**
     * Data sets are transformed depending on the model as follows:
     *
     * Spherical I(q) => q^6*I(q)
     * Elliptical I(q) =>
     *
     *
     * Standardized based on max/min
     */
     private void transformData(){
         double value, tvalue;
         switch(model){
             case SPHERICAL:
                 // transform data as q^6*I(q)
                 for(int i=0; i<totalQValues; i++){
                     value = qvalues[i];
                     //tvalue = value*value*value*value*value*value; // q^6
                     transformedIntensities.add(value*fittedIntensities.get(i));
                     transformedErrors.add(value*fittedErrors.get(i));
//                     transformedIntensities.add(fittedIntensities.get(i));
//                     transformedErrors.add(fittedErrors.get(i));

                 }
                 break;
             case CORESHELL:
                 break;
             case CORESHELLBINARY:
                 break;
             case ELLIPSOID: case PROLATE_ELLIPSOID: case OBLATE_ELLIPSOID:
                 for(int i=0; i<totalQValues; i++){
                     value = qvalues[i];
                     transformedIntensities.add(value*fittedIntensities.get(i));
                     transformedErrors.add(value*fittedErrors.get(i));
                 }
                 break;
             default:
                 for(int i=0; i<totalQValues; i++){
                     value = qvalues[i];
                     transformedIntensities.add(value*fittedIntensities.get(i));
                     transformedErrors.add(value*fittedErrors.get(i));
                 }
                 break;
         }
    }


    private ArrayList<Double> createTestDistribution(int indexOfCenter){
        // create dataset from
        int del = 2; // let this be sigma
        int start = indexOfCenter - del;
        int endat = indexOfCenter + del + 1;
        int total = endat - start;
        ArrayList<Double> percentages = new ArrayList<>(total);

        double factor = 1.0/Math.sqrt(2.0*del*del*Math.PI);
        double factor2 = 1.0/(2*(del*del));

        double sumOfPercentages = 0;
        for(int i=0; i<total; i++) {
            percentages.add(factor*Math.exp(-(del-i)*(del-i)*factor2));
            sumOfPercentages += percentages.get(i);
        }

        ArrayList<Double> testData = new ArrayList<>();
        ArrayList<Double> testError = new ArrayList<>();

        for(int j = 0; j< qIndicesToFit.size(); j++){
            testData.add(0.0d);
            testError.add(0.0d);
        }

        //Ellipse ellipse = (Ellipse)models.get(41);
        //ellipse.printParams();
        // create Gaussian distribution of spheres
        for(int i=start; i<endat; i++){
            Model model = models.get(i);
            //System.out.println(i + " Model In Test " + ((Sphere)model).getRadius());
            int index = i-start;
            for(int j = 0; j< qIndicesToFit.size(); j++){
                testData.set(j, testData.get(j) + percentages.get(index)/sumOfPercentages*model.getIntensity(qIndicesToFit.get(j)));
            }
        }

        return testData;
    }


    private void createTestData(){
        // given the number of models, randomly select an index
        int randomIndex = ThreadLocalRandom.current().nextInt(3,models.size()-3);
        //int[] indicesToModel = {31, 76};
        System.out.println("Models.size " + models.size());
        int[] indicesToModel = {1131, 1129};
        //double[] weights = {0.61, 0.1};
        double[] weights = {0.61, 0.1};

        ArrayList<Double> testData = new ArrayList<>();
        ArrayList<Double> testError = new ArrayList<>();

        for(int j = 0; j< qIndicesToFit.size(); j++){
            testData.add(0.0d);
            testError.add(0.0d);
        }
        System.out.println("1--");
        for(int ind=0; ind<indicesToModel.length; ind++){

            ArrayList<Double> temp = createTestDistribution(indicesToModel[ind]);

            for(int j = 0; j< qIndicesToFit.size(); j++){
                temp.set(j, weights[ind]*temp.get(j));
            }

            // add to testData
            for(int j = 0; j< qIndicesToFit.size(); j++){
                testData.set(j, temp.get(j) + testData.get(j));
            }
        }
        System.out.println("2--");

        // scale intensities
        for(int j = 0; j< qIndicesToFit.size(); j++){
            testData.set(j, 10000000.0*testData.get(j));
        }

        // create model errors 20%
        Random rvalue = new Random();
        double displacement;
        for(int j = 0; j< qIndicesToFit.size(); j++){
            displacement = 0.05*(rvalue.nextDouble()*2.0 - 1.0)*testData.get(j);
            testData.set(j, testData.get(j) + displacement);
            testError.set(j, displacement);
        }

        // add data to model
        workingSetIntensities.clear();
        workingSetErrors.clear();
        for(int i = 0; i< totalQIndicesToFit; i++){
            workingSetIntensities.add(testData.get(i));
            workingSetErrors.add(testError.get(i));
            System.out.println(qvalues[qIndicesToFit.get(i)] + " " + testData.get(i));
        }
    }


    /**
     * create an ArrayList of I(q) that is selected from the random subset of binned q-values
     * the non-selected set is the cross-validated set
     */
    private void createWorkingSet(){

        workingSetIntensities = new ArrayList<>();
        workingSetErrors = new ArrayList<>();
        for(int i = 0; i< totalQIndicesToFit; i++){
            workingSetIntensities.add(transformedIntensities.get(qIndicesToFit.get(i)));
            workingSetErrors.add(transformedErrors.get(qIndicesToFit.get(i)));
        }

    }


    /**
     * Initialize cumulative distribution function
     */
    private void populateCDF() {
        //cdf = new TreeMap<>(); // not sure this is threadsafe for reading
        cdf = new ConcurrentSkipListMap();
        cdf.put(0.0d, 0);
        double value = probabilities.get(0);

        for(int i=1; i<models.size(); i++){
            cdf.put(value, i);
            value += probabilities.get(i);
        }
    }


    /**
     * update using smoothing criteria => alpha*new + (1-alpha)*old
     *
     * @param tempTopList
     */
    private void updateCDF(TopList tempTopList){
        // create HashMap
        HashMap<Integer, Double> modelIndexCount = new HashMap<>();
        Integer[] keys = tempTopList.getModelIndicesFromScore();
        int totalKeys = keys.length; // get the keys that are in the top list
        int totalModels=0;
        //
        // go through the TopNList and count each selected index
        for(int i=0; i<totalKeys; i++){
            ArrayList<Integer> modelIndices = tempTopList.getModelIndicesByKey(keys[i]);

            for(int j=0; j<modelsPerTrial; j++){
                int keyToCheck = modelIndices.get(j);
                //System.out.println(i + " " + j + " Updating KEY " + keyToCheck);
                if (modelIndexCount.containsKey(keyToCheck)){
                    modelIndexCount.put(keyToCheck, modelIndexCount.get(keyToCheck) + 1.0d);
                } else {
                    modelIndexCount.put(keyToCheck, 1.0d);
                }
                totalModels+=1;
            }
        }

        // update probabilities
        // alpha*v_t + (1-alpha)*v_(t-1)
        double inv = 1.0/(double)totalModels;
        double oldprob;

        for(int i=0; i<models.size(); i++){
            oldprob = (1-alpha)*probabilities.get(i); // get old probability

            if (modelIndexCount.containsKey(i)) {
                probabilities.put(i, alpha*modelIndexCount.get(i)*inv + oldprob);
            } else {
                probabilities.put(i, oldprob);
            }
        }
//        System.out.println("40 prob => " + probabilities.get(40));
//        System.out.println("41 prob => " + probabilities.get(41));
//        System.out.println("42 prob => " + probabilities.get(42));

        keptList.clear();
        //keptList = new ConcurrentSkipListMap<Double, ArrayList<Integer> >();
        tempTopList.copyToKeptList(keptList, modelsPerTrial);
        populateCDF();
    }



    private void printFit(List<Double> modelIntensities){

        Map.Entry<Double, ArrayList<Integer> > top = keptList.firstEntry();

        ArrayList<Double> calculatedIntensities = new ArrayList<>();
        while(calculatedIntensities.size() < totalQIndicesToFit) calculatedIntensities.add(0.0d);

        int totalModelsInTop = top.getValue().size();

        for(int i=0; i<totalModelsInTop; i++){
            int startIndex = top.getValue().get(i)*totalQIndicesToFit;

            for(int j=0; j< totalQIndicesToFit; j++){
                calculatedIntensities.set(j, calculatedIntensities.get(j).doubleValue() + modelIntensities.get(startIndex+j).doubleValue());
            }
        }

        for(int j=0; j< totalQIndicesToFit; j++){
            calculatedIntensities.set(j, (1.0/(double)totalModelsInTop)*calculatedIntensities.get(j).doubleValue());
        }

        for(int i = 0; i< totalQIndicesToFit; i++){
            System.out.println(qvalues[qIndicesToFit.get(i)] + " " + workingSetIntensities.get(i) + " " + calculatedIntensities.get(i));
        }
    }

    public void setProgressBar(JProgressBar bar){
        barSet = true;
        this.progressBar = bar;
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(rounds);
    }


    public void setPlotPanels(JPanel residualsPanel, JPanel heatMapPanel, JPanel leftSpherePanel,  JPanel rightSpherePanel, JPanel dataPanel) {
        this.rightPanel = rightSpherePanel;
        this.leftPanel = leftSpherePanel;
        this.residualsPanel = residualsPanel;
        this.heatMapPanel = heatMapPanel;
        this.dataPlotPanel = dataPanel;
    }



    public void setEllipsoidPlotPanels(JPanel crossSection1Panel,
                                       JPanel crossSection2Panel,
                                       JPanel crossSection3Panel,
                                       JPanel crossSection4Panel,
                                       JPanel residualsPanel,
                                       JPanel histoRa,
                                       JPanel histoRb,
                                       JPanel histoRc,
                                       JPanel scoreProgressEllipsePanel,
                                       JPanel dataPanel) {

        this.crossSection1Panel = crossSection1Panel;
        this.crossSection2Panel = crossSection2Panel;
        this.crossSection3Panel = crossSection3Panel;
        this.crossSection4Panel = crossSection4Panel;

        this.residualsPanel = residualsPanel;
        this.heatMapPanel = histoRa;
        this.heatMap1Panel = histoRb;
        this.heatMap2Panel = histoRc;
        this.rightPanel = scoreProgressEllipsePanel;
        this.dataPlotPanel = dataPanel;
    }


    private void makeProgressPlot(){

        progressChart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "round",                             // domain axis label
                "log10 [<score>]",                     // range axis label
                new XYSeriesCollection(scorePerRound),                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                false,
                false
        );

        ChartPanel chartPanel = new ChartPanel(progressChart);
        //LogAxis yAxis = new LogAxis("Average Score");
        //yAxis.setBase(10);
        //progressChart.getXYPlot().setRangeAxis(yAxis);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        rightPanel.removeAll();
        rightPanel.add(chartPanel);
    }

}
