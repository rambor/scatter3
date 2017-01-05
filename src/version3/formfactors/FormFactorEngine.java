package version3.formfactors;

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
    private String version;
    private String finalResults;
    private double alpha = 0.63;
    private double smallestProb;
    private Dataset dataset;
    private double shellThickness;
    private double dmaxOfSet=0;
    private double deltaqr = 0.00001;
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
    private double priorAverage=10000000;


    private ArrayList<Double> workingSetIntensities;
    private ArrayList<Double> workingSetErrors;


    private ArrayList<Integer> qIndicesToFit;
    private int totalQValues;
    private int totalQIndicesToFit;
    private double[] minParams;
    private double[] maxParams;
    private double[] delta;
    private double solventContrast;
    private double[] particleContrasts;
    private double qmax, qmin, epsilon, lambda;

    private ArrayList<Double> probabilities;
    private ConcurrentNavigableMap cdf;
    //private ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList;  // strictly positive
    private KeptModels keptList;

    private JProgressBar progressBar;
    private boolean barSet = false;
    private boolean useNoBackground = false;
    private boolean completeness = false;
    private boolean useEntropy = false;

    private JPanel leftPanel;
    private JPanel scoreProgressPanel;
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
    private boolean useVolumeScaling;


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
                            double lambda,
                            int top,
                            double solventContrast,
                            double[] particleContrasts,
                            int trials,
                            int randomModelsPerTrial,
                            boolean useNoBackground,
                            boolean entropy,
                            boolean useVolumeScaling,
                            String version
    ){

        this.dataset = dataset;
        this.model = model;
        cpuCores = cores;
        this.rounds = rounds;
        this.trials = trials;
        this.modelsPerTrial = randomModelsPerTrial;
        models = new ArrayList<>();

        this.minParams=minParams;
        this.maxParams=maxParams;
        this.delta=delta;
        this.qmax = qmax;
        this.lambda = lambda;

        this.version = version;

        // smallest possible value is PI/qmax
        if (Math.PI/qmax < minParams[0]){
            this.minParams[0] = Math.floor(Math.PI/qmax);
        }

        this.qmin = qmin;
        this.topN = top;
        this.epsilon = epsilon;

        this.useNoBackground = useNoBackground;
        this.useEntropy = entropy;
        this.useVolumeScaling = useVolumeScaling;
        // index -> intensity curve
        // arraylist of models
        XYSeries alldata = dataset.getAllData();
        XYSeries allError = dataset.getAllDataError();
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
            }
        }

        totalQValues = fittedIntensities.size();
        // array of qvalues truncated by qmin and qmax
        qvalues = new Double[totalQValues];
        qvalues = qvalueslist.toArray(qvalues);

        // for each bin, grab at least 3 points to fit
        // if percent is 1, grab all the indices within the specified range
        qIndicesToFit = new ArrayList<>();
        for(int j=0; j<totalQValues; j++){
            qIndicesToFit.add(j);
        }

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

        System.out.println("CALCULATING " + this.model + " MODELS");

        switch(model){

            case SPHERICAL:
                // create spherical models at different radii
                createSpheres();
                break;
            case THREE_BODY:
                createThreeBody();
                break;
            case CORESHELL:
                break;
            case CORESHELL_PROLATE_ELLIPSOID:
                createCoreShellProlateEllipsoid();
                break;
            case CORESHELL_OBLATE_ELLIPSOID:
                createCoreShellOblateEllipsoid();
                break;
            case PROLATE_ELLIPSOID:
                // if R_a > R_c
                createProlateEllipsoid();
                break;

            case OBLATE_ELLIPSOID:
                // if R_c > R_a
                createOblateEllipsoid();
                break;

            case ELLIPSOID:
                createTriaxialEllipsoids();
                break;
        }

        System.out.println("TOTAL : " + models.get(0).getTotalIntensities());
        // transform data
        transformData();
        // create working dataset for fitting
        bins = (int)(qmax*dmaxOfSet/Math.PI) + 1;
        System.out.println("Max Shannon Bins " + bins);
        double deltaq = qmax/(double)bins;


//        if (percentData > 0.99){
//            for(int j=0; j<totalQValues; j++){
//                qIndicesToFit.add(j);
//            }
//        } else {
//            for(int i=1; i<bins; i++){
//                double upper = deltaq*i;
//                int upperIndex=totalQValues;
//
//                // find first value > upper
//                for(int q=startIndex; q<totalQValues; q++){
//                    double test = qvalues[q];
//                    if (test > upper){
//                        upperIndex = q;
//                        break;
//                    }
//                }
//                //System.out.println("StartIndex " + startIndex + " " + upperIndex + " " + upper + " <= " + qmax);
//                if (upperIndex > 0){
//                    int[] indices = Functions.randomIntegersBounded(startIndex, upperIndex, percentData);
//                    startIndex = upperIndex;
//                    int totalToAdd = indices.length;
//                    for(int j=0; j<totalToAdd; j++){
//                        qIndicesToFit.add(indices[j]);
//                    }
//                }
//            }
//        }

        totalQIndicesToFit = qIndicesToFit.size();
//        Double[] qValuesInSearch = new Double[totalQIndicesToFit];
//        for(int j=0; j<totalQIndicesToFit; j++) {
//            qValuesInSearch[j] = qvalues[qIndicesToFit.get(j)];
//        }

        createWorkingSet();
        // create mega array
        // array will be accessed via multithreads
        int totalIntensitiesOfModels = totalQIndicesToFit*models.size();
        List<Double> modelIntensities = Collections.synchronizedList(new ArrayList<>(totalIntensitiesOfModels));
        // hash map
        probabilities = new ArrayList<>();

        double value = 1.0/(double)models.size();
        // Double is value in CDF and Integer is index of model
        // all models are initialized with same value
        for(int i=0; i<models.size(); i++){
            Model model = models.get(i);
            probabilities.add(value); // should add the probability as an attribute of Model class?
            model.setProbability(value);
            for(int j = 0; j< totalQIndicesToFit; j++){
                modelIntensities.add(model.getIntensity(j));
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

        keptList = new KeptModels();
        progressBar.setMaximum(rounds);
        progressBar.setValue(0);
        progressBar.setString("CE Search");

        // initialize distribution per param
        int round = 0;

        fitLoop:
        for(; round<rounds; round++){
            // select N models based on probabilities
            // fit model
            // if change, epsilon < 0.001, break;
            ExecutorService executor = Executors.newFixedThreadPool(cpuCores);
            ArrayList<Future<Double>> futures = new ArrayList<>();
            TopList tempTopList = new TopList(topN, probabilities, alpha, delta[0], models, lambda, useEntropy);
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
                                models,
                                modelIntensities,
                                totalIntensitiesOfModels,
                                workingSetIntensities,
                                workingSetErrors,
                                qvalues,
                                useNoBackground,
                                useVolumeScaling
                        )));
            }
            // Use TopList to update CDF
            //
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // tempTopList.print();
            // update probabilities
            updateCDF(tempTopList);
            //
            // update plots
            // 1. distribution plot
            // 2. residual plot
            // print fit
            // printFit(modelIntensities);
            progressBar.setValue(round+1);

            double averageScore = tempTopList.getAverageScore(); // log 10 score
            scorePerRound.add(round, averageScore);
            if (tempTopList.getGap() < epsilon || (Math.abs(priorAverage-averageScore) < epsilon)){
                System.out.println("EARLY BREAK :   GAP => " + tempTopList.getGap());
                System.out.println("            : DELTA => " + Math.abs(priorAverage-averageScore));
                break fitLoop;
            }
            // gap may not change much if the number of trials per round is large enough
            //
            //double criteria = Math.abs(tempTopList.getGap())/(priorAverage-tempTopList.getAverageScore());
            // if either is zero break ?
            System.out.println("Round " + round + " => COMPLETED");
            priorAverage = averageScore;
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
                        scoreProgressPanel,
                        dataPlotPanel,
                        models,
                        keptList,
                        probabilities,
                        qvalues,
                        transformedIntensities,
                        transformedErrors,
                        useNoBackground,
                        useVolumeScaling);
                plots.create(true);
                break;

            case CORESHELL_OBLATE_ELLIPSOID: case CORESHELL_PROLATE_ELLIPSOID:
                CoreShellPlots csPlots = new CoreShellPlots(
                        residualsPanel,
                        leftPanel,
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
                        useNoBackground,
                        minParams[0],
                        maxParams[0]
                        );
                csPlots.create(true);
                break;
            case ELLIPSOID:
                EllipsoidPlots eplots = new EllipsoidPlots(
                        residualsPanel,
                        crossSection1Panel,
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
                        useNoBackground, useVolumeScaling, minParams[0], maxParams[0]);
                eplots.create(true);
                break;
//
            case THREE_BODY:
                ThreeBodyPlots threeBodyPlots = new ThreeBodyPlots(
                        leftPanel,
                        residualsPanel,
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
                        useNoBackground
                );
                threeBodyPlots.create(true);
                break;

            case PROLATE_ELLIPSOID: case OBLATE_ELLIPSOID:
                EllipsoidPlots prplots = new EllipsoidPlots(
                        residualsPanel,
                        crossSection1Panel,
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
                        useNoBackground, useVolumeScaling, minParams[0], maxParams[0]);
                prplots.create(true);
                break;
        }

        makeProgressPlot();

        // write out results
        finalResults="";
        // if P(r) distribution is determined, calculate P(r) from shape and then do diff
        System.out.println("DMAX " + dataset.getRealSpaceModel().getDmax());
        if (dataset.getRealSpaceModel().getDmax() > 1 && (model == ModelType.SPHERICAL || model == ModelType.ELLIPSOID || model == ModelType.OBLATE_ELLIPSOID || model == ModelType.PROLATE_ELLIPSOID)){
            // estimate volume of P(r)-distribution
            System.out.println("ESTIMATING FORM FACTOR PR");
            estimateVolume();
        }

        writeResults(round);


        progressBar.setValue(0);
        return null;
    }


    /**
     *
     */
    private void writeResults(int completedRound) {

        String tempHeader="REMARK 265 \n";
        tempHeader += "REMARK 265       DATA REDUCTION SOFTWARE : SCATTER (v "+version+")\n";
        tempHeader += "REMARK 265               SOFTWARE AUTHOR : RP RAMBO\n";
        tempHeader += "REMARK 265 FORM FACTOR DISTRIBUTION FITTING\n";
        tempHeader += "REMARK 265 \n";
        tempHeader += String.format("REMARK 265                          QMIN : %.4f %n", qmin);
        tempHeader += String.format("REMARK 265                          QMAX : %.4f %n", qmax);
        tempHeader += "REMARK 265\n";
        tempHeader += String.format("REMARK 265      TOP N SELECTED PER TRIAL : %d %n", topN);
        tempHeader += String.format("REMARK 265        TOTAL TRIALS PER ROUND : %d %n", trials);
        tempHeader += String.format("REMARK 265                    MAX ROUNDS : %d %n", rounds);
        tempHeader += String.format("REMARK 265        TOTAL ROUNDS COMPLETED : %d %n", completedRound);
        tempHeader += String.format("REMARK 265                       EPSILON : %.6f %n", epsilon);

        int totalScores = scorePerRound.getItemCount();
        tempHeader += "REMARK 265\n";
        tempHeader += String.format("REMARK 265              BEST CHI-SQUARED : %.4f %n", keptList.getBestScore());
        tempHeader += String.format("REMARK 265  AVERAGE CHI-SQUARED OF TOP N : %.4f %n", Math.pow(10,(double)scorePerRound.getY(totalScores-1)));
        tempHeader += "REMARK 265\n";
        tempHeader += "REMARK 265 PARAMETER DETAILS OF BEST SELECTED SET\n";
        ArrayList<Integer> list = keptList.getFirst();
        // print parameters
        int total = list.size();
        int totalParams = models.get(0).getTotalFittedParams();
        double[] averages = new double[totalParams];
        double[] variances = new double[totalParams];

        double volume=0;
        for(int i=0; i<total; i++){
            Model model = models.get(list.get(i));
            tempHeader+=model.getParamsToPrint();
            volume += model.getVolume();
            for(int p=0; p<totalParams; p++){
                double value =model.getFittedParamByIndex(p);
                averages[p]+=value;
                variances[p] += value*value;
            }
        }

        volume *= 1.0/(double)total;

        for(int p=0; p<totalParams; p++){
            averages[p] *= 1.0/(double)total;
            variances[p] *= 1.0/(double)total;
        }

        for(int p=0; p<totalParams; p++){
            variances[p] -= averages[p]*averages[p];
        }

        double max=0;
        int indexOfBest=0;
        for(int i=0; i<models.size(); i++){
            double value = models.get(i).getProbability();
            if (value > max){
                max = value;
                indexOfBest = i;
            }
        }


        tempHeader += "REMARK 265\n";
        tempHeader += "REMARK 265  IZERO => 4*PI*CONTRAST^2*SCALE*VOLUME^2\n";
        tempHeader += models.get(0).getConstrastString();
        tempHeader += String.format("REMARK 265           MOST PROBABLE MODEL : %d %n", indexOfBest);
        tempHeader += String.format("REMARK 265                  SCALE FACTOR : %.5E %n", keptList.getScaleByIndex(0));
        tempHeader += String.format("REMARK 265               WEIGHTED VOLUME : %.1f %n", volume);
        tempHeader += "REMARK 265\n";

        for(int p=0; p<totalParams; p++){
          tempHeader += String.format("REMARK 265               PARAM %d AVERAGE : %.3f %n", (p+1), averages[p]);
          tempHeader += String.format("REMARK 265                         STDEV : %.3f %n", Math.sqrt(variances[p]));
        }

        tempHeader += finalResults;
        System.out.println(tempHeader);
    }


    /**
     *
     */
    private void estimateVolume() {

        ArrayList<Integer> list = keptList.getFirst();
        // print parameters
        int total = list.size();
        int monteCarloSize = 10000;
        int totalShannonBins = dataset.getRealSpaceModel().getTotalMooreCoefficients() - 1;
        double binWidth = (double)dataset.getRealSpaceModel().getDmax()/(double)totalShannonBins;

        double squared=0;
        for(int i=0; i<total; i++){
            Model model = models.get(list.get(i));
            squared += model.getVolume()*model.getVolume();
        }
        double volume = Math.sqrt(squared/(double)total);
        double calibrationScale;
        SortedMap<Integer, Double> histogram = new TreeMap<>();

        if (model == ModelType.ELLIPSOID || model == ModelType.SPHERICAL || model == ModelType.OBLATE_ELLIPSOID || model == ModelType.PROLATE_ELLIPSOID) {

            if (model == ModelType.ELLIPSOID){

                for (int i = 0; i < total; i++) {
                    Ellipse model = (Ellipse) models.get(list.get(i));
                    double[] distances = model.calculatePr(monteCarloSize);
                    for (int j = 0; j < monteCarloSize; j++) {
                        int ratio = (int) (distances[j] / binWidth);
                        if (histogram.containsKey(ratio)) {
                            histogram.put(ratio, histogram.get(ratio) + 1.0d);
                        } else {
                            histogram.put(ratio, 1.0d);
                        }
                    }
                }

            } else if (model == ModelType.OBLATE_ELLIPSOID || model == ModelType.PROLATE_ELLIPSOID){

                for (int i = 0; i < total; i++) {
                    ProlateEllipsoid model = (ProlateEllipsoid) models.get(list.get(i));
                    double[] distances = model.calculatePr(monteCarloSize);
                    for (int j = 0; j < monteCarloSize; j++) {
                        int ratio = (int) (distances[j] / binWidth);
                        if (histogram.containsKey(ratio)) {
                            histogram.put(ratio, histogram.get(ratio) + 1.0d);
                        } else {
                            histogram.put(ratio, 1.0d);
                        }
                    }
                }

            } else if (model == ModelType.SPHERICAL ) {

                for (int i = 0; i < total; i++) {

                    Sphere model = (Sphere) models.get(list.get(i));
                    double dmaxOfModel = 2*model.getRadius();

                    for (int j = 0; j*binWidth < dmaxOfModel; j++) {

                        double rvalue = j*binWidth + 0.5*binWidth;

                        if (histogram.containsKey(j)) {
                            histogram.put(j, histogram.get(j) + model.calculatePr(rvalue));
                        } else {
                            histogram.put(j, model.calculatePr(rvalue));
                        }
                    }
                }

            }

            // calculate integral (Rectangle Method) and adjust to volume
            Object[] bins = histogram.keySet().toArray();
            double area = 0;
            for(int i=0; i<bins.length; i++){
                area += binWidth*histogram.get(bins[i]);
            }

            calibrationScale = volume/area;
            // scale the model
            String prValues = "REMARK 265 R-VALUE P(R)-EXP P(R)-MODEL\n";
            prValues += String.format("%.2f %.5E %.5E%n", 0.0,0.0,0.0);
            area=0;

            double contrast = (solventContrast - particleContrasts[0]);
            double scale = keptList.getScaleByIndex(0);

            double areaObs=0;
            double dmaxOfExp = dataset.getRealSpaceModel().getDmax();

            for(int i=0; i<bins.length; i++){

                double rvalue = i*binWidth + 0.5*binWidth;
                double prvalue = contrast*contrast*scale*volume*(calibrationScale*histogram.get(bins[i]));

                if (rvalue > dmaxOfExp){
                    prValues += String.format("%.2f %.5E %.5E%n", rvalue,0.0,prvalue);
                } else {
                    areaObs += dataset.getRealSpaceModel().calculatePofRAtR(rvalue)*binWidth;
                    prValues += String.format("%.2f %.5E %.5E%n", rvalue,dataset.getRealSpaceModel().calculatePofRAtR(rvalue),prvalue);
                }

                area += prvalue*binWidth;
            }
            prValues += String.format("%.2f %.5E %.5E %n", bins.length*binWidth,0.0,0.0);

            // assume relationship between integrated area and volume is linear
            double scaledVolume = areaObs/area*volume;

//            System.out.println("FINAL SCALED AREA => " + area + " ~= " + volume);
//            System.out.println("         EXP AREA => " + areaObs);
//            System.out.println("   CALIBRATED VOL => " + scaledVolume);
//            System.out.println("   SCALE FROM FIT => " + scale);
//            System.out.println("      CALIBRATION => " + calibrationScale);

            finalResults += "REMARK 265\n";
            finalResults += "REMARK 265 CALIBRATION CONSTANT => RATIO OF VOLUME TO AREA OF P(R) OF MODEL \n";
            finalResults += String.format("REMARK 265          CALIBRATION CONSTANT : %.4E %n", calibrationScale);
            finalResults += String.format("REMARK 265        AREA EXPERIMENTAL P(R) : %.3E %n", areaObs);
            finalResults += String.format("REMARK 265   CALIBRATED VOLUME FROM P(R) : %.1f %n", scaledVolume);
            finalResults += String.format("REMARK 265                       I(ZERO) : %.3E %n", (4*Math.PI*contrast*contrast*scale*volume*volume));

            finalResults += prValues;
            // compare to real space
        } else {

            System.out.println("Volume calibration not available for this form factor");

        }

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
         double value;
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
        int del = 1; // let this be sigma
        int start = indexOfCenter - del;
        int endat = indexOfCenter + del + 1;
        int total = endat - start;
        ArrayList<Double> percentages = new ArrayList<>(total);
        double sumOfPercentages = 0;

        if (del > 0){
            double factor = 1.0/Math.sqrt(2.0*del*del*Math.PI);
            double factor2 = 1.0/(2*(del*del));

            for(int i=0; i<total; i++) {
                percentages.add(factor*Math.exp(-(del-i)*(del-i)*factor2));
                sumOfPercentages += percentages.get(i);
            }
        } else {
            sumOfPercentages = 1;
            percentages.add(1.0);
        }

        ArrayList<Double> testData = new ArrayList<>();
        for(int j = 0; j< qIndicesToFit.size(); j++){
            testData.add(0.0d);
        }

        // create Gaussian distribution of spheres
        for(int i=start; i<endat; i++){
            Model model = models.get(i);

            int index = i-start; // maps to percentages
            for(int j = 0; j< qIndicesToFit.size(); j++){
                testData.set(j, testData.get(j) + percentages.get(index)/sumOfPercentages*model.getIntensity(qIndicesToFit.get(j)));
            }
        }

        return testData;
    }


    private void createTestData(){
        // given the number of models, randomly select an index
        //int randomIndex = ThreadLocalRandom.current().nextInt(3,models.size()-3);
        int totalRandomModels = 1;
        int[] indicesToModel = new int[totalRandomModels];
        //int[] indicesToModel = {37};
        //probabilities.set(indicesToModel[0], 0.6);
        for(int i=0; i<totalRandomModels; i++){
            indicesToModel[i] = ThreadLocalRandom.current().nextInt(5,models.size()-5);
        }

        for(int i=0; i<indicesToModel.length; i++){
            int totalFitted = models.get(i).getTotalFittedParams();
            for(int m=0; m<totalFitted; m++){
                System.out.println(" MODEL " + i + " => " + indicesToModel[i] + " PARAMS " + m + " " + models.get(indicesToModel[i]).getFittedParamByIndex(m));
            }
        }

        double[] weights = {0.71, 0.29};

        ArrayList<Double> testData = new ArrayList<>();
        ArrayList<Double> testError = new ArrayList<>();

        for(int j = 0; j< qIndicesToFit.size(); j++){
            testData.add(0.0d);
            testError.add(0.0d);
        }

        for(int ind=0; ind<indicesToModel.length; ind++){

            ArrayList<Double> temp = createTestDistribution(indicesToModel[ind]);

            for(int j = 0; j< qIndicesToFit.size(); j++){
                temp.set(j, weights[ind]*temp.get(j)); // with no weights, data is scaled by volume
            }

            // add to testData
            for(int j = 0; j< qIndicesToFit.size(); j++){
                testData.set(j, temp.get(j) + testData.get(j));
            }
        }

        // scale intensities
        for(int j = 0; j< qIndicesToFit.size(); j++){
            testData.set(j, 1000*testData.get(j));
        }

        // create model errors 20%
        Random rvalue = new Random();
        double displacement;
        double slopeerror = 0.05/(0.3-0.001);
        double intercept = 0.01-slopeerror*0.004;

        for(int j = 0; j< qIndicesToFit.size(); j++){
            double error = qvalues[j]*slopeerror + intercept;
            displacement = error*rvalue.nextGaussian()*testData.get(j);
            testData.set(j, testData.get(j) + displacement);
            testError.set(j, Math.abs(displacement));
        }


        // add data to model
        workingSetIntensities.clear();
        workingSetErrors.clear();
        for(int i = 0; i< totalQIndicesToFit; i++){
            workingSetIntensities.add(testData.get(i));
            workingSetErrors.add(testError.get(i));
            //System.out.println(qvalues[qIndicesToFit.get(i)] + " " + testData.get(i) + " " + testError.get(i));
        }
        transformedIntensities = workingSetIntensities;
        transformedErrors = workingSetErrors;
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
        cdf = new ConcurrentSkipListMap();
        cdf.put(0.0d, models.get(0).getIndex());
        double value = probabilities.get(models.get(0).getIndex());

        for(int i=1; i<models.size(); i++){
            int index = models.get(i).getIndex();
            cdf.put(value, index);
            value += probabilities.get(index);
        }
        //System.out.println("LAST " + (probabilities.size()-1) + " => " + cdf.lastEntry().getKey() + " <=> " + cdf.lastEntry().getValue() );
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

                if (modelIndexCount.containsKey(keyToCheck)){
                    modelIndexCount.put(keyToCheck, modelIndexCount.get(keyToCheck) + 1.0d);
                } else {
                    modelIndexCount.put(keyToCheck, 1.0d);
                }
                totalModels+=1;
            }
        }

        //
        // update probabilities
        // alpha*v_t + (1-alpha)*v_(t-1)
        double inv = 1.0/(double)totalModels;
        double oldprob;

        for(int i=0; i<models.size(); i++){
            int index = models.get(i).getIndex();
            // get old probability by index
            oldprob = (1-alpha)*models.get(i).getProbability();

            if (modelIndexCount.containsKey(index)) {
                probabilities.set(index, alpha*modelIndexCount.get(index)*inv + oldprob);
            } else {
                probabilities.set(index, oldprob);
            }

            models.get(i).setProbability(probabilities.get(index));
        }

        keptList.clear();
        tempTopList.copyToKeptList(keptList, modelsPerTrial);
        populateCDF();
    }


//    private void printFit(List<Double> modelIntensities){
//
//        Map.Entry<Double, ArrayList<Integer> > top = keptList.firstEntry();
//
//        ArrayList<Double> calculatedIntensities = new ArrayList<>();
//        while(calculatedIntensities.size() < totalQIndicesToFit) calculatedIntensities.add(0.0d);
//
//        int totalModelsInTop = top.getValue().size();
//
//        for(int i=0; i<totalModelsInTop; i++){
//            int startIndex = top.getValue().get(i)*totalQIndicesToFit;
//
//            for(int j=0; j< totalQIndicesToFit; j++){
//                calculatedIntensities.set(j, calculatedIntensities.get(j).doubleValue() + modelIntensities.get(startIndex+j).doubleValue());
//            }
//        }
//
//        for(int j=0; j< totalQIndicesToFit; j++){
//            calculatedIntensities.set(j, (1.0/(double)totalModelsInTop)*calculatedIntensities.get(j).doubleValue());
//        }
//
//        for(int i = 0; i< totalQIndicesToFit; i++){
//            System.out.println(qvalues[qIndicesToFit.get(i)] + " " + workingSetIntensities.get(i) + " " + calculatedIntensities.get(i));
//        }
//    }

    public void setProgressBar(JProgressBar bar){
        barSet = true;
        this.progressBar = bar;
        this.progressBar.setMinimum(0);
        this.progressBar.setMaximum(rounds);
    }


    public void setPlotPanels(JPanel residualsPanel, JPanel heatMapPanel, JPanel leftSpherePanel,  JPanel rightSpherePanel, JPanel dataPanel) {
        this.scoreProgressPanel = rightSpherePanel;
        this.leftPanel = leftSpherePanel;
        this.residualsPanel = residualsPanel;
        this.heatMapPanel = heatMapPanel;
        this.dataPlotPanel = dataPanel;
    }

    public void setCSPlotPanels(
            JPanel geometryPanel,
            JPanel residualsPanel,
            JPanel scoreProgressPanel,
            JPanel dataPanel,
            JPanel smallestPanel,
            JPanel middlePanel,
            JPanel largestPanel) {

        this.leftPanel = geometryPanel;
        this.residualsPanel = residualsPanel;
        this.scoreProgressPanel = scoreProgressPanel;
        this.dataPlotPanel = dataPanel;
        this.heatMapPanel = smallestPanel;
        this.heatMap1Panel = middlePanel;
        this.heatMap2Panel = largestPanel;
    }


    public void setThreeBodyPlotPanels(
                                       JPanel geometryPanel,
                                       JPanel residualsPanel,
                                       JPanel scoreProgressPanel,
                                       JPanel dataPanel,
                                       JPanel smallestPanel,
                                       JPanel middlePanel,
                                       JPanel largestPanel) {

        this.leftPanel = geometryPanel;
        this.residualsPanel = residualsPanel;
        this.heatMapPanel = smallestPanel;
        this.heatMap1Panel = middlePanel;
        this.heatMap2Panel = largestPanel;
        this.scoreProgressPanel = scoreProgressPanel;
        this.dataPlotPanel = dataPanel;
    }



    public void setEllipsoidPlotPanels(JPanel crossSection1Panel,
                                       JPanel residualsPanel,
                                       JPanel histoRa,
                                       JPanel histoRb,
                                       JPanel histoRc,
                                       JPanel scoreProgressEllipsePanel,
                                       JPanel dataPanel) {

        this.crossSection1Panel = crossSection1Panel;
        this.residualsPanel = residualsPanel;
        this.heatMapPanel = histoRa;
        this.heatMap1Panel = histoRb;
        this.heatMap2Panel = histoRc;
        this.scoreProgressPanel = scoreProgressEllipsePanel;
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
        scoreProgressPanel.removeAll();
        scoreProgressPanel.add(chartPanel);
    }

    public void setCompleteness(boolean value, double shellthickness){
        this.completeness = value;
        this.shellThickness = shellthickness;
    }


    private void createSpheres(){

        double[] params = new double[1];
        params[0] = minParams[0];

        dmaxOfSet = 2*maxParams[0];
        totalSearchSpace=0;
        while(params[0] < maxParams[0]){
            models.add(new Sphere(totalSearchSpace, solventContrast, particleContrasts, params, qvalues));
            params[0] += delta[0];
            totalSearchSpace++;
        }
    }

    private void createOblateEllipsoid(){
        // if R_c > R_a
        ScheduledExecutorService ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
        List<Future<ProlateEllipsoid>> obfutures = new ArrayList<>();

        double[] params = new double[2];
        double minLimit = minParams[0];  // lower limit of minor axis
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

        int ellipses = 0;
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
    }


    private void createProlateEllipsoid(){
        // if R_a > R_c
        ScheduledExecutorService ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
        List<Future<ProlateEllipsoid>> epfutures = new ArrayList<>();
        double[] params = new double[2];

        dmaxOfSet = 2*maxParams[0];
        // fill parameter arrays
        ArrayList<Double> outer = new ArrayList<>();

        outer.add(maxParams[0]);
        int count=0;
        while (outer.get(count) >= minParams[0] ){
            count++;
            outer.add(maxParams[0] - count*delta[0]);
        }

        int totalOuterLoop = outer.size();

        // create models
        for(int i=0; i<(totalOuterLoop-1); i++){
            params[0] = outer.get(i);
            int startHere = i+1;
            for(int j=startHere; j<totalOuterLoop; j++){

                params[1] = outer.get(j);
                Future<ProlateEllipsoid> future = ellipseExecutor.submit(new CallableProlateEllipsoid(
                        totalSearchSpace, // index
                        solventContrast,
                        particleContrasts,
                        params,
                        qvalues
                ));

                epfutures.add(future);
                totalSearchSpace++;
            }
        }

        int ellipses = 0;
        progressBar.setStringPainted(true);
        progressBar.setString("Making Models");
        progressBar.setMaximum(totalSearchSpace);
        progressBar.setValue(0);
        for(Future<ProlateEllipsoid> fut : epfutures){
            try {
                //print the return value of Future, notice the output delay in console
                models.add(fut.get());
                //System.out.println("Creating Search Space : " + models.size() + " Total Models of " + totalSearchSpace);
                ellipses++;
                progressBar.setValue(ellipses);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        ellipseExecutor.shutdown();
    }


    private void createTriaxialEllipsoids(){

        // iterate from largest ellipsoid to smallest ellipsoid
        // largest ellipsoid = maxParams[0]
        double minLimit = minParams[0];
        double[] params = new double[3];   // [0] => R_a, [1] => R_b, [2] => R_c
        params[2] = maxParams[0]; // this is R_c
        dmaxOfSet = 2*maxParams[0];
        //System.out.println("MAX " +  params[2] + " MIN " + minLimit + " " + delta[0]);
        // create atomic integer array that will hold weights
        // if interval is 0.01, then x,y is 100*100 = 10000 points
        // if interval is 0.001 then x,y is 1000*1000 = 1x10^6
        //
        ScheduledExecutorService ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
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

        int ellipses = 0;
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
    }

    private void createThreeBody(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(cpuCores);
        List<Future<ThreeBody>> threeBodiesFutures = new ArrayList<>();
        double minLimit = minParams[0];
        double[] params = new double[3];   // [0] => R_1, [1] => R_2, [2] => R_3
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
                    double minr13 = params[2] + params[0];             // closest they can be

                    // set the distances between first and third sphere
                    for(double rad=minr13; rad<=maxr13; rad += delta[0]){
                        //System.out.println(totalSearchSpace + " PARAMS " + params[0] + " " + params[1] + " " + params[2] + " rad " + rad);
                        Future<ThreeBody> future = executor.submit(new CallableThreeBody(
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

        executor.shutdown();
    }

    private void createCoreShellOblateEllipsoid() {
        // if R_c > R_a
        ScheduledExecutorService ellipseExecutor = Executors.newScheduledThreadPool(cpuCores);
        List<Future<ProlateEllipsoid>> obfutures = new ArrayList<>();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(cpuCores);
        List<Future<CoreShell>> coreOblatefutures = new ArrayList<>();
        double[] params = new double[2];
        double minLimit = minParams[0];    // lower limit of minor axis
        params[1] = maxParams[0];   // this is R_a
        dmaxOfSet = 2*maxParams[0];

        double deltaShell = shellThickness*0.2;// set shell thickness
        double shellStart = shellThickness - 3*deltaShell;

        while(params[1] > minLimit){ // major
            // set R_a
            params[0] = params[1] - delta[0];

            while (params[0] >= minLimit){ // minor
                // for each r_major and r_minor axis, set shell thickness
                for(int s=0; s<7; s++){ // vary the shell thickness
                    double shell = s*deltaShell+shellStart;

                    Future<CoreShell> future = executor.submit(new CallableCoreShellEllipsoid(
                            totalSearchSpace,
                            solventContrast,
                            particleContrasts,
                            shell,
                            params,
                            qvalues,
                            completeness
                    ));
                    coreOblatefutures.add(future);
                    totalSearchSpace++;

                    if (completeness){ // alternate core contrast with solvent
                        double[] empty = new double[2];
                        empty[0] = particleContrasts[0]; // shell
                        empty[1] = solventContrast; // core
                        Future<CoreShell> emptyFuture = executor.submit(new CallableCoreShellEllipsoid(
                                totalSearchSpace,
                                solventContrast,
                                empty,
                                shell,
                                params,
                                qvalues,
                                completeness
                        ));
                        coreOblatefutures.add(emptyFuture);
                        totalSearchSpace++;
                    }
                }
                params[0] -= delta[0];
            }
            params[1] -= delta[0];
        }

        int csellipses = 0;
        progressBar.setStringPainted(true);
        progressBar.setString("Making Models");
        progressBar.setMaximum(totalSearchSpace);
        progressBar.setValue(0);
        for(Future<CoreShell> fut : coreOblatefutures){
            try {
                //print the return value of Future, notice the output delay in console
                models.add(fut.get());
                csellipses++;
                progressBar.setValue(csellipses);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
    }


    /**
     *
     */
    private void createUnilamellar(){


        double maxamplitude=10;
        double minamplitude = -1.0;


        double deltaAmp = (maxamplitude - minamplitude)/0.1;

        //int totalBins = 400.0/


    }

    private void createCoreShellProlateEllipsoid() {
        // if R_a > R_c
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(cpuCores);
        List<Future<CoreShell>> coreProlatefutures = new ArrayList<>();
        double[] params = new double[2];

        double minLimit = minParams[0];    // lower limit of minor axis
        params[0] = maxParams[0];   // this is R_a
        dmaxOfSet = 2*maxParams[0];
        double deltaShell = shellThickness*0.2;// set shell thickness
        double shellStart = shellThickness - 2*deltaShell;

        while(params[0] > minLimit){
            // set R_c
            params[1] = params[0] - delta[0];

            while (params[1] >= minLimit){
                // for each r_major and r_minor axis, set shell thickness
                for(int s=0; s<5; s++){ // vary the shell thickness
                    double shell = s*deltaShell+shellStart;

                    Future<CoreShell> future = executor.submit(new CallableCoreShellEllipsoid(
                            totalSearchSpace,
                            solventContrast,
                            particleContrasts,
                            shell,
                            params,
                            qvalues,
                            completeness
                    ));
                    coreProlatefutures.add(future);

                    totalSearchSpace++;
                    if (completeness){ // alternate core contrast with solvent
                        double[] empty = new double[2];
                        empty[0] = particleContrasts[0];
                        empty[1] = solventContrast;
                        Future<CoreShell> emptyFuture = executor.submit(new CallableCoreShellEllipsoid(
                                totalSearchSpace,
                                solventContrast,
                                empty,
                                shell,
                                params,
                                qvalues,
                                completeness
                        ));
                        coreProlatefutures.add(emptyFuture);
                        totalSearchSpace++;
                    }
                }
                params[1] -= delta[0];
            }
            params[0] -= delta[0];
        }

        int csellipses = 0;
        progressBar.setStringPainted(true);
        progressBar.setString("Making Models");
        progressBar.setMaximum(totalSearchSpace);
        progressBar.setValue(0);
        for(Future<CoreShell> fut : coreProlatefutures){
            try {
                //print the return value of Future, notice the output delay in console
                models.add(fut.get());
                csellipses++;
                progressBar.setValue(csellipses);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
    }


}
