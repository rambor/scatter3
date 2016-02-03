package version3;

import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by robertrambo on 28/01/2016.
 */
public class RefineManager extends SwingWorker<Void, Void> {

    private int numberOfCPUs;
    private RealSpace dataset;
    private int refinementRounds;
    private int roundsPerCPU;
    private int bins;
    private double rejectionCutOff;
    private double lambda;
    private double dmax;
    private boolean useL1;
    private JProgressBar bar;

    private boolean isFinished = false;

    private XYSeries keptSeries;
    private XYSeries keptErrorSeries;
    private double[] keptAm;
    private double s_o;
    private JLabel statusLabel;
    private boolean useLabels = false;
    private double upperq;
    private int totalToFit;
    private double dmax_pi;
    private double median;
    private double invReScaleFactor;
    private double totalRoundsForRefinement;
    private AtomicInteger counter = new AtomicInteger(0);

    public RefineManager(RealSpace dataset, int numberOfCPUs, int refinementRounds, double rejectionCutOff, double lambda, boolean useL1){

        this.numberOfCPUs = numberOfCPUs;
        this.dataset = dataset;
        this.refinementRounds = refinementRounds;
        this.rejectionCutOff = rejectionCutOff;
        this.lambda = lambda;
        this.useL1 = useL1;

        totalToFit = dataset.getfittedqIq().getItemCount();

        totalRoundsForRefinement = refinementRounds;
        roundsPerCPU = (int)(this.refinementRounds/(double)numberOfCPUs);
        bins = (int) (Math.round(dataset.getfittedqIq().getMaxX() * dataset.getDmax() / Math.PI));
        //upperq = dataset.getAllData().getX(dataset.getStop()-1).doubleValue();
        upperq = dataset.getfittedqIq().getMaxX();
        this.dmax = dataset.getDmax();
        this.dmax_pi = Math.PI*dmax;

        invReScaleFactor = 1.0/dataset.getRescaleFactor();
        median = 1000; //medianMooreResidualsQIQ(dataset.getfittedqIq(), dataset.getMooreCoefficients(), dataset.getTotalMooreCoefficients());
    }


    public void setBar(JProgressBar bar, JLabel prStatusLabel){
        this.bar = bar;
        this.bar.setValue(0);
        this.bar.setStringPainted(true);
        this.statusLabel = prStatusLabel;
        useLabels = true;
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

    @Override
    protected Void doInBackground() throws Exception {
        if (useLabels){
            statusLabel.setText("Starting median residual " + median);
        }
        System.out.println("BEFORE : isFinsihed " + isFinished);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);
        for (int i=0; i < numberOfCPUs; i++){
            Runnable bounder = new Refiner(dataset.getfittedqIq(), dataset.getfittedError(), roundsPerCPU, statusLabel);
            executor.execute(bounder);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            bar.setValue(0);
            bar.setStringPainted(false);
            notifyUser("Finished Refining");
        } catch (InterruptedException e) {
            System.out.println("InterruptedException " + e.getMessage());
            notifyUser("Failed settings limits, exceeded thread time");
        }

        //update realspaceModel
        dataset.setMooreCoefficients(keptAm);
        notifyUser("Finished Refining");
        createKeptSeries();

        synchronized (this) {
            isFinished = true;
            notifyAll();
        }
        System.out.println(" AFTER : isFinsihed " + isFinished);
        return null;
    }


    public class Refiner implements Runnable {

        private XYSeries activeSet;
        //private XYSeries activeIqSet;
        private XYSeries errorActiveSet;
        private XYSeries relativeErrorSeries;
        //private boolean useNotify = false;


        //private double upperq;
        private int totalRuns;

        //private double median;

        private int sizeAm;

        private ArrayList<Integer> countsPerBin;
        private ArrayList<Double> averageStoNPerBin;

        private JLabel status;

        public Refiner(XYSeries qIq, XYSeries allError, int totalruns, JLabel status){

            try {
                int totalItems = qIq.getItemCount() ;
                activeSet = qIq.createCopy(0, totalItems-1); // possibly scaled by rescaleFactor
                //activeIqSet = iofQ.createCopy(0, totalItems-1);
                errorActiveSet = new XYSeries("Error qIq");
                relativeErrorSeries = new XYSeries("Error qIq");

                this.totalRuns = totalruns;
                double rescalefactor = dataset.getRescaleFactor();

                for (int i=0; i<totalItems; i++){
                    XYDataItem tempData = allError.getDataItem(i);
                    errorActiveSet.add(tempData.getXValue(), tempData.getXValue() * tempData.getYValue()*rescalefactor);
                    relativeErrorSeries.add(tempData.getXValue(), Math.abs(activeSet.getY(i).doubleValue() / (tempData.getXValue() * tempData.getYValue()*rescalefactor)));
                }

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
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
            double delta_q = upperq/bins;

            XYDataItem tempData;
            ArrayList<double[]> results;

            XYSeries randomSeries = new XYSeries("Random-");
            XYSeries errorSeries = new XYSeries("RandomError-");

            for (int i=0; i < totalRuns; i++){

                randomSeries.clear();
                errorSeries.clear();

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
                            samplingNumber = 36;
                        } else {
                            samplingNumber = 5;
                        }

                        samplingLimit = (1.0 + randomGenerator.nextInt(samplingNumber))/100.0;  // return a random percent up to ...

                        binloop:
                        for (int bb=startbb; bb < size; bb++){
                            if (activeSet.getX(bb).doubleValue() >= (delta_q*b) ){ // what happens on the last bin?
                                upperbb = bb;
                                break binloop;
                            }
                        }

                        // grab indices inbetween startbb and upperbb
                        randomNumbers = this.randomIntegersBounded(startbb, upperbb, samplingLimit);

                        startbb = upperbb;

                        for(int h=0; h<randomNumbers.length; h++){
                            locale = randomNumbers[h];
                            tempData = activeSet.getDataItem(locale);
                            randomSeries.add(tempData);
                            errorSeries.add(tempData.getXValue(), errorActiveSet.getY(locale));
                        }

                    } // end of checking if bin is empty
                }

                // calculate PofR
                PrObject prObject = new PrObject(randomSeries, upperq, dmax, lambda, useL1);
                if (useL1){
                    // fixed q range
                    results = prObject.moore_pr_L1();
                } else {
                    results = prObject.moore_coeffs_L1();
                }

                // Calculate Residuals
                tempMedian = medianMooreResiduals(activeSet, results.get(0));

                if (tempMedian < getMedian()){
                    // keep current values
                    notifyUser(String.format("Improved fit, new median residual %1.7E < %1.7E at round %d", tempMedian, median, i));
                    //status.setText("Improved fit, new median residual: " + tempMedian + " < " + median + " at round " + i );

                    sizeAm = results.get(0).length;
                    setkeptAm(results.get(0), sizeAm);
                    setMedian(tempMedian);
                    setS_o(1.4826*(1.0+5.0/(size - sizeAm))*Math.sqrt(median));
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
            double delta_q = upperq/bins;
            int size = relativeErrorSeries.getItemCount();

            for (int b=1; b <= bins; b++) {
                sumError = 0;
                sumCount = 0;
                upperBound = delta_q*b;

                binloop:
                for (int bb=startbb; bb < size; bb++){

                    tempCurrent = relativeErrorSeries.getX(bb).doubleValue();

                    if ( (tempCurrent >= (delta_q*(b-1)) ) && (tempCurrent < upperBound) ){
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
                System.out.println("Signal-to-Noise per Bin " + b + " " +  averageStoNPerBin.get(b-1) + " " + countsPerBin.get(b-1));
            }
        }

        private void increment() {
            int currentCount = counter.incrementAndGet();

            if (currentCount%100==0){
                bar.setValue((int)(currentCount/totalRoundsForRefinement*100));
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

        /**
         * Calculate the median residual of Moore function and dataset
         * Requires anti-log10 q*I(q) data
         * @param data XYSeries of (q, I(q))
         * @param a_m Moore Coefficients
         * @return Median residual as float
         */
        private double medianMooreResiduals(XYSeries data, double[] a_m){
            double medianResidual;
            ArrayList<Double> residuals = new ArrayList<Double>();

            int dataLimit = data.getItemCount();
            double q;
            double dmax_q2, dmax_q;
            double sin_dmaxq;
            double resultM;
            XYDataItem tempitem;

            for (int i = 0; i < dataLimit; i++){
                tempitem = data.getDataItem(i);

                q = tempitem.getXValue();
                dmax_q = dmax*q;
                dmax_q2 = dmax_q*dmax_q;
                sin_dmaxq = Math.sin(dmax_q);

                resultM = a_m[0];
                for(int j=1; j < sizeAm; j++){
                    resultM = resultM + Constants.TWO_DIV_PI*a_m[j]*j*dmax_pi*Math.pow(-1,j+1)*sin_dmaxq/(j*j*Constants.PI_2 - dmax_q2);
                }
                // should this be abs(residuals) or squared?
                residuals.add( Math.pow(tempitem.getYValue() - resultM, 2) );  //squaring the difference
                //residuals.add( Math.abs(tempitem.getYValue() - resultM) );  //squaring the difference
            }

            medianResidual = Statistics.calculateMedian(residuals);
            return medianResidual;
        }

    } // end of runnable class

    private synchronized void setkeptAm(double[] values, int sizeAm) {
        this.keptAm = Arrays.copyOf(values, sizeAm);
    }

    /** @return a copy of the array */
    public double[] getKeptAM() {
        return Arrays.copyOf(keptAm, keptAm.length);
    }

    private synchronized void setS_o(double value) {
        this.s_o = value;
    }

    private synchronized double getS_o() {
        return this.s_o;
    }

    public void createKeptSeries(){
        // go over selected values in active set
        keptSeries=new XYSeries("reduced refinedset");
        keptErrorSeries=new XYSeries("reduced refinedset");

        double xObsValue, yObsValue;
        double residual;
        List residualsList = new ArrayList();
        double[] keptAm = this.getKeptAM();
        int sizeAm = keptAm.length;

        XYDataItem tempData;
        XYSeries activeIqSet = dataset.getfittedIq();
        XYSeries errorActiveSet = dataset.getfittedError();
        int size = activeIqSet.getItemCount();

        for (int i=0; i < size; i++){
            tempData = activeIqSet.getDataItem(i);
            xObsValue = tempData.getXValue();
            yObsValue = tempData.getYValue();

            residual = invReScaleFactor*Functions.moore_Iq(keptAm, dmax, xObsValue, sizeAm)-yObsValue;

            if (Math.abs(residual/this.getS_o()) <= rejectionCutOff){
                residualsList.add(residual*residual);
            }
        }

        int sizeResiduals = residualsList.size();
        double sigmaM = 1.0/Math.sqrt(Statistics.calculateMean(residualsList)*sizeResiduals/(sizeResiduals - sizeAm));

        XYSeries keptqIq = new XYSeries("keptqIq-"+dataset.getId());
        // Select final values for fitting
        dataset.getLogData().clear();

        double iCalc;
        dataset.getCalcIq().clear();
        //dataset.getfittedIq().clear();

        for (int i=0; i<size; i++){
            tempData = activeIqSet.getDataItem(i);
            xObsValue = tempData.getXValue();
            yObsValue = tempData.getYValue();

            iCalc = Functions.moore_Iq(keptAm, dmax, xObsValue, sizeAm)*invReScaleFactor;
            residual = iCalc-yObsValue;

            if (Math.abs(residual*sigmaM) <= rejectionCutOff){
                keptSeries.add(tempData);
                //dataset.getfittedIq().add(tempData);

                keptqIq.add(xObsValue, xObsValue*yObsValue*dataset.getRescaleFactor());  // used in actual fitting must be rescaled if too low or high
                keptErrorSeries.add(xObsValue, errorActiveSet.getY(i).doubleValue());

                if (yObsValue > 0){ // only positive values
                    tempData = new XYDataItem(xObsValue, Math.log10(yObsValue));
                    dataset.getCalcIq().add(xObsValue, Math.log10(iCalc));
                    dataset.getLogData().add(tempData);  // plotted I(q)
                }
            }
        }
        // update spinner with new limits of of keptSeries
        // estimate errors on invariants using sampling algorithm off of keptSeries
        // if keptqIq > bin*2 , then calculate PofR
        // otherwise use activeqIq
        if (keptqIq.getItemCount() > 2*bins){ // do final fitting against kepSeries

            dataset.updatedRefinedSets(keptSeries, keptErrorSeries);
            System.out.println("kept qIq Count => " + keptqIq.getItemCount() + " dmax => " + dmax);
            ArrayList<double[]> results;
            // calculate PofR
            PrObject prObject = new PrObject(keptqIq, upperq , dmax, lambda, useL1);

            if (useL1){
                // fixed q range
                results = prObject.moore_pr_L1();
            } else {
                results = prObject.moore_coeffs_L1();
            }

            dataset.setMooreCoefficients(results.get(0));

            try {
                dataset.chi_estimate(keptSeries, keptErrorSeries);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            int totalrejected =  totalToFit - keptSeries.getItemCount();
            double percentRejected = (double)totalrejected/(double)totalToFit*100;
            notifyUser(String.format("Rejected %d points (%.1f %%) using cutoff: %.4f => files written to working directory", totalrejected, percentRejected, rejectionCutOff));
        } else { // do nothing if unacceptable, so clear and reload a original

            for (int i=0; i<size; i++){
                tempData = activeIqSet.getDataItem(i);
                xObsValue = tempData.getXValue();
                yObsValue = tempData.getYValue();

                //dataset.getfittedIq().add(tempData);

                if (yObsValue > 0){ // only positive values
                    tempData = new XYDataItem(xObsValue, Math.log10(yObsValue));
                    dataset.getLogData().add(tempData);  // plotted I(q)
                }
            }

            dataset.calculateIntensityFromModel(useL1);
            updateText(String.format("Refinement failed check data for unusual numbers"));
        }
    }


    public boolean getIsFinished() {
        return isFinished;
    }

    /**
     *
     * @param value
     * @return
     */
    private synchronized void setMedian(double value) {
            this.median = value;
    }


    /**
     *
     * @return
     */
    public synchronized double getMedian() {
        return this.median;
    }

    /**
     * Calculate the median residual of Moore function and dataset
     * Requires anti-log10 I(q) data
     * @param data XYSeries of (q, I(q))
     * @param a_m Moore Coefficients
     * @return Median residual as float
     */
    private double medianMooreResidualsQIQ(XYSeries data, double[] a_m, int sizeAm){
        double medianResidual;
        ArrayList<Double> residuals = new ArrayList<Double>();

        int dataLimit = data.getItemCount();
        double q;
        double dmax_q2, dmax_q;
        //double dmaxPi = dmax*Math.PI;
        //double pi2 = Math.PI*Math.PI;
        double sin_dmaxq;
        double resultM;
        XYDataItem tempitem;

        for (int i = 0; i < dataLimit; i++){
            tempitem = data.getDataItem(i);

            q = tempitem.getXValue();
            dmax_q = dmax*q;
            dmax_q2 = dmax_q*dmax_q;
            sin_dmaxq = Math.sin(dmax_q);

            resultM = a_m[0];
            for(int j=1; j < sizeAm; j++){
                resultM = resultM + Constants.TWO_DIV_PI*a_m[j]*j*dmax_pi*Math.pow(-1,j+1)*sin_dmaxq/(j*j*Constants.PI_2 - dmax_q2);
            }
            // should this be abs(residuals) or squared?
            residuals.add( Math.pow(tempitem.getYValue() - resultM, 2) );  //squaring the difference
            //residuals.add( Math.abs(tempitem.getYValue() - resultM) );  //squaring the difference
        }

        medianResidual = Statistics.calculateMedian(residuals);
        return medianResidual;
    }

}
