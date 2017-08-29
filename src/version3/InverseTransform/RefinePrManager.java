package version3.InverseTransform;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import version3.*;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RefinePrManager extends SwingWorker<Void, Void> {

    private int numberOfCPUs;
    private RealSpace dataset;
    private int refinementRounds;
    private int roundsPerCPU;
    private int bins;
    private IndirectFT keptIFTModel;
    private double rejectionCutOff;
    private double lambda;
    private double s_o;
    private double dmax;
    private double dmax_pi;
    private double pi_invDmax;
    private double upperq;
    private final boolean useL1;
    private final boolean useDirectFT;
    private JProgressBar bar;
    private final boolean useBackgroundInFit;
    private JLabel statusLabel;
    private boolean useLabels = false;

    private boolean isFinished = false;

    private int cBoxValue;
    private XYSeries keptSeries;
    private XYSeries keptErrorSeries;
    private XYSeries standardizedSeries;
    private XYSeries fittedErrors;
    private double standardizedMin;
    private double standardizedScale;
    private AtomicInteger counter = new AtomicInteger(0);
    private double median;

    public RefinePrManager(RealSpace dataset, int numberOfCPUs, int refinementRounds, double rejectionCutOff, double lambda, int cBoxValue, boolean useL1, boolean includeBackground, boolean useDirect){

        this.numberOfCPUs = numberOfCPUs;
        this.dataset = dataset;
        this.refinementRounds = refinementRounds;
        this.roundsPerCPU = (int)(this.refinementRounds/(double)this.numberOfCPUs);
        this.rejectionCutOff = rejectionCutOff;
        this.lambda = lambda;
        this.useL1 = useL1;
        this.useBackgroundInFit = includeBackground;
        this.useDirectFT = useDirect;
        this.cBoxValue = cBoxValue;

        this.upperq = dataset.getfittedqIq().getMaxX();
        this.bins = dataset.getTotalFittedCoefficients();
        this.dmax = dataset.getDmax();
        this.dmax_pi = Math.PI*dmax;
        this.pi_invDmax = Math.PI/dmax;

        this.standardizedSeries = new XYSeries("Standard set");
        this.fittedErrors = new XYSeries("fitted errors");
        this.standardizeData();

        //dataset.setStandardizationMean(this.standardizedMin, this.standardizedScale);
        this.median = dataset.getIndirectFTModel().calculateMedianResidual(standardizedSeries);
    }


    @Override
    protected Void doInBackground() throws Exception {
        if (useLabels){
            statusLabel.setText("Starting median residual " + median);
        }

        //bar.setMaximum(100);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);
        for (int i=0; i < numberOfCPUs; i++){
            Runnable bounder = new RefinePrManager.Refiner(
                    this.standardizedSeries,
                    this.fittedErrors,
                    roundsPerCPU,
                    statusLabel);
            executor.execute(bounder);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            bar.setValue(0);
            bar.setStringPainted(false);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException " + e.getMessage());
            notifyUser("Failed settings limits, exceeded thread time");
        }

        //update realspaceModel
        //dataset.setIndirectFTModel(keptIFTModel);

        notifyUser("Finished Refining");
        createKeptSeries();

        synchronized (this) {
            isFinished = true;
            notifyAll();
        }
        notifyUser("Final Kept Series Created");
        //this.dataset.chi_estimate();
        return null;
    }

    /**
     *
     * @return
     */
    public synchronized double getMedian() {
        return this.median;
    }

    private synchronized void setMedian(double value) {
        median = value;
    }

    private synchronized void setIFTObject(IndirectFT iftObject, double value) {
        keptIFTModel = iftObject;
        setMedian(value);
    }

    public void setBar(JProgressBar bar, JLabel prStatusLabel){
        this.bar = bar;
        this.bar.setValue(0);
        this.bar.setStringPainted(true);
        this.bar.setMaximum(refinementRounds);
        this.statusLabel = prStatusLabel;
        useLabels = true;
    }

    public boolean getIsFinished() {
        return isFinished;
    }

    void updateText(final String text){
        statusLabel.setText(text);
    };

    private synchronized void notifyUser(final String text){
        if (useLabels){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateText(text);
                }
            });
        } else {
            System.out.println(text);
        }
    }

    /**
     *
     * standardize data
     */
    private void standardizeData(){
        XYDataItem tempData;
        XYSeries fitteddqIqRegion = dataset.getfittedqIq();

        int totalItems = fitteddqIqRegion.getItemCount();
        standardizedMin = dataset.getStandardizationMean();
        standardizedScale = dataset.getStandardizationStDev();
        double invstdev = 1.0/standardizedScale;

        for(int r=0; r<totalItems; r++){
            tempData = fitteddqIqRegion.getDataItem(r);
            fittedErrors.add(dataset.getErrorAllData().getDataItem(dataset.getErrorAllData().indexOf(tempData.getX())));
            standardizedSeries.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
        }
    }



    public class Refiner implements Runnable {

        private XYSeries activeSet;
        private XYSeries errorActiveSet;
        private XYSeries relativeErrorSeries;
        private int totalRuns;

        private ArrayList<Integer> countsPerBin;
        private ArrayList<Double> averageStoNPerBin;

        private JLabel status;

        public Refiner(XYSeries qIq, XYSeries allError, int totalruns, JLabel status){

            int totalItems = qIq.getItemCount() ;
            // create standardized dataset
            try {
                activeSet = qIq.createCopy(0, totalItems-1); // possibly scaled by rescaleFactor
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            //
            errorActiveSet = new XYSeries("Error qIq");
            relativeErrorSeries = new XYSeries("Error qIq");

            this.totalRuns = totalruns;

            for (int i=0; i<totalItems; i++){
                XYDataItem tempData = allError.getDataItem(i);
                errorActiveSet.add(tempData.getXValue(), tempData.getXValue() * tempData.getYValue());
                relativeErrorSeries.add(tempData.getXValue(), Math.abs(activeSet.getY(i).doubleValue() / (tempData.getXValue() * tempData.getYValue())));
            }

            countsPerBin = new ArrayList<>();
            averageStoNPerBin = new ArrayList<>();
            this.averageSignalToNoisePerBin();

            this.status = status;
        }


        @Override
        public void run() {

            int startbb, upperbb, samplingNumber, locale;
            double samplingLimit, tempMedian;

            Random randomGenerator = new Random();
            int[] randomNumbers;

            int size = activeSet.getItemCount();
            double half_delta_ns = 0.5*pi_invDmax;

            XYSeries randomSeries = new XYSeries("Random-");
            //System.out.println("TOTAL RUNS " + totalRuns);
            for (int i=0; i < totalRuns; i++){

                randomSeries.clear();

                // randomly grab from each bin
                startbb = 0;
                upperbb = 0;

                for (int b=1; b <= bins; b++){
                    // find upper q in bin
                    // if s-to-n of bin < 1.5, sample more points in bin
                    // if countPerBin < 5, randomly pick 1
                    // what if countPerBin = 0?
                    if (countsPerBin.get(b-1) > 0){

                        if (averageStoNPerBin.get(b-1) < 1.25){ // increase more points if S-to-N is less than
                            samplingNumber = 23;
                        } else {
                            samplingNumber = 23;
                        }

                        samplingLimit = (1.0 + randomGenerator.nextInt(samplingNumber))/100.0;  // return a random percent up to ...

                        binloop:
                        for (int bb=startbb; bb < size; bb++){
                            // b*pi_invDmax is cardinal point in reciprocal space
                            if (activeSet.getX(bb).doubleValue() >= (half_delta_ns + b*pi_invDmax) ){ // what happens on the last bin?
                                upperbb = bb;
                                break binloop;
                            } else {
                                upperbb = size-1;
                            }
                        }

                        // grab indices inbetween startbb and upperbb
                        randomNumbers = this.randomIntegersBounded(startbb, upperbb, samplingLimit);
                        //System.out.println("bin : " + b + " => startbb " + startbb + " upper " + upperbb + " " + size + " " + samplingLimit + " length " + randomNumbers.length);

                        startbb = upperbb;

                        for(int h=0; h < randomNumbers.length; h++){
                            locale = randomNumbers[h];
                            randomSeries.add(activeSet.getDataItem(locale));
                        }
                    } // end of checking if bin is empty
                }

                // calculate PofR using standardized dataset
                IndirectFT tempIFT;
                if (useDirectFT){
                    tempIFT = new SineIntegralTransform(randomSeries, errorActiveSet, dmax, upperq, lambda, useL1, cBoxValue, useBackgroundInFit, standardizedMin, standardizedScale);
                } else {  // use Moore Method
                    tempIFT = new MooreTransform(randomSeries, errorActiveSet, dmax, upperq, lambda, useL1, cBoxValue, useBackgroundInFit, standardizedMin, standardizedScale);
                }


                // Calculate Residuals
                tempMedian = tempIFT.calculateMedianResidual(activeSet); // squared residual using standardized data

                if (tempMedian < getMedian()){ // minimize on medium residual in activeset
                    // keep current values
                    notifyUser(String.format("New median residual %1.5E < %1.4E at round %d", tempMedian, median, i));
                    setIFTObject(tempIFT, tempMedian);
                }

                this.increment();
            }
        }


        /**
         *
         */
        private void averageSignalToNoisePerBin(){
            // for each bin determine average signal to noise
            int startbb = 0;
            double sumError, sumCount, tempCurrent, upperBound;
            int size = relativeErrorSeries.getItemCount();

            for (int b=1; b <= bins; b++) {
                sumError = 0;
                sumCount = 0;
                upperBound = b*pi_invDmax + 0.5*pi_invDmax;

                binloop:
                for (int bb=startbb; bb < size; bb++){

                    tempCurrent = relativeErrorSeries.getX(bb).doubleValue();

                    //if ( (tempCurrent >= (delta_q*(b-1)) ) && (tempCurrent < upperBound) ){
                    if ( (tempCurrent < upperBound) ){
                        sumError += relativeErrorSeries.getY(bb).doubleValue();
                        sumCount += 1.0;
                    } else if (tempCurrent >= upperBound ) {
                        startbb = bb;
                        break binloop;
                    }
                }

                // you want a better algorithm, by a bad computer
                // greens theorem surface integral
                countsPerBin.add((int)sumCount);
                if (sumCount > 0){
                    averageStoNPerBin.add(sumError/sumCount);
                } else {
                    averageStoNPerBin.add(0.0d);
                }
                //System.out.println("Signal-to-Noise per Bin " + b + " " +  averageStoNPerBin.get(b-1) + " " + countsPerBin.get(b-1));
            }
        }


        private void increment() {
            int currentCount = counter.incrementAndGet();

            if (currentCount%100==0){
                bar.setValue((int)(currentCount/refinementRounds*100));
            }
        }

        public AtomicInteger getValue() {
            return counter;
        }

        /**
         *
         * @param lower int
         * @param upper int
         * @param percent double
         * @return
         */
        private int[] randomIntegersBounded(int lower, int upper, double percent){
            List<Integer> numbers = new ArrayList<Integer>();

            int total = 0;
            for(int i = lower; i < upper; i++) {
                numbers.add(i);
                total++;
            }

            // Shuffle them
            Collections.shuffle(numbers);
            int count = (int)Math.ceil(total*percent);
            // Pick count items.
            List<Integer> tempNumbers = numbers.subList(0, count);
            int[] values = new int[count];

            for(int i=0; i < count; i++){
                values[i] = tempNumbers.get(i);
            }

            Arrays.sort(values);
            return values;
        }
    } // end of runnable class


    public void createKeptSeries(){
        // go over selected values in active set
        keptSeries=new XYSeries("reduced refinedset");
        keptErrorSeries=new XYSeries("reduced refinedset");

        double xObsValue, yObsValue;
        double residual;
        List residualsList = new ArrayList();

        XYDataItem tempData;

        double invStd = 1.0/standardizedScale;


        int size = standardizedSeries.getItemCount();
        s_o = 1.4826*(1.0 + 5.0/(size - keptIFTModel.getTotalFittedCoefficients() - 1))*Math.sqrt(getMedian());

        for (int i=0; i < size; i++){
            tempData = standardizedSeries.getDataItem(i);
            xObsValue = tempData.getXValue();
            yObsValue = tempData.getYValue(); // standardized in form of qIq
            //
            // data has been standardized, so no adjustment necessary before residual calculation.
            residual = yObsValue - (keptIFTModel.calculateQIQ(xObsValue)-standardizedMin)*invStd;

            if (Math.abs(residual/this.getS_o()) < rejectionCutOff){
                residualsList.add(residual*residual);
            } else {
               // System.out.println(xObsValue +  " REJECTED " + residual + " => " + Math.abs(residual/this.getS_o()) + " > " + rejectionCutOff);
            }
        }

        int sizeResiduals = residualsList.size();
        double sigmaM = 1.0/Math.sqrt(Statistics.calculateMean(residualsList)*sizeResiduals/(sizeResiduals - bins));

        XYSeries keptqIq = new XYSeries("keptqIq-"+dataset.getId());
        XYSeries keptNonStandardizedqIq = new XYSeries("keptqIq-nonstd"+dataset.getId());
        // Select final values for fitting
        for (int i=0; i < size; i++){
            tempData = standardizedSeries.getDataItem(i);
            xObsValue = tempData.getXValue();
            yObsValue = tempData.getYValue();
            residual = yObsValue - (keptIFTModel.calculateQIQ(xObsValue)-standardizedMin)*invStd;

            if ( Math.abs(residual*sigmaM) < rejectionCutOff){
                keptSeries.add(dataset.getfittedIq().getDataItem(i));
                keptqIq.add(tempData);
                keptNonStandardizedqIq.add(xObsValue, dataset.getfittedIq().getY(i).doubleValue()*xObsValue);
                keptErrorSeries.add(xObsValue, fittedErrors.getY(i).doubleValue());
            }
        }

        // if keptqIq > bin*2 , then calculate PofR
        // otherwise use activeqIq
        if (keptqIq.getItemCount() > 2*bins){ // do final fitting against kepSeries

            dataset.updatedRefinedSets(keptSeries, keptErrorSeries);
            dataset.getCalcIq().clear();
            IndirectFT tempIFT;
            if (useDirectFT){ // unstandardized datasets
                tempIFT = new SineIntegralTransform(keptqIq, keptErrorSeries, dmax, upperq, lambda, useL1, cBoxValue, useBackgroundInFit, standardizedMin, standardizedScale);
            } else {  // use Moore Method
                tempIFT = new MooreTransform(keptqIq, keptErrorSeries, dmax, upperq, lambda, useL1, cBoxValue, useBackgroundInFit, standardizedMin, standardizedScale);
            }
            // for chi_estimate calculations, nonStandardized datasets must be specified
            //btempIFT.setNonStandardizedData(keptNonStandardizedqIq);

            this.dataset.setIndirectFTModel(tempIFT); // also calculate chi2 using data passed in on the instance
            //this.dataset.updateIndirectFTModel(tempIFT);
            this.dataset.setPrDistribution(tempIFT.getPrDistribution());
            this.dataset.calculateIntensityFromModel(true);

            try {
                // requires (q, Iq) not (q, q*Iq)
                // need chi specific to keptSeries
                int totalKept = keptSeries.getItemCount();

                for(int i=0; i < totalKept;i++){
                    tempData = keptSeries.getDataItem(i);
                    //keptSeries.update(tempData.getX(), (tempData.getYValue()));
                    if (tempData.getYValue() > 0) { // only positive values
                        xObsValue = tempData.getXValue();
                        dataset.getCalcIq().add(xObsValue, Math.log10( tempIFT.calculateIQ(xObsValue) ) ); //plotted data is unscaled, as is from Analysis
                    }
                }
                this.dataset.calculateQIQ();
                //prDataset.calculateIntensityFromModel(qIqFit.isSelected());
                //dataset.chi_estimate(keptSeries, keptErrorSeries);  //

            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            int totalrejected =  size - keptSeries.getItemCount();
            double percentRejected = (double)totalrejected/(double)size*100;
            notifyUser(String.format("Rejected %d points (%.1f %%) using cutoff: %.4f => files written to working directory", totalrejected, percentRejected, rejectionCutOff));

            RefinedPlot refinedPlot = new RefinedPlot(dataset);
            refinedPlot.makePlot(String.format("Rejected %d points (%.1f %%) using cutoff: %.4f => files written to working directory", totalrejected, percentRejected, rejectionCutOff));

        } else { // do nothing if unacceptable, so clear and reload a original

            for (int i=0; i<size; i++){
                tempData = standardizedSeries.getDataItem(i);
                xObsValue = tempData.getXValue();
                yObsValue = tempData.getYValue();

                //dataset.getfittedIq().add(tempData);
            }

            dataset.calculateIntensityFromModel(useL1);
            updateText(String.format("Refinement failed check data for unusual numbers"));
        }
    }


    private synchronized double getS_o() {
        return this.s_o;
    }
}