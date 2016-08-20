package version3;

import net.jafama.FastMath;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.io.File;
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
    private XYSeries standardizedSeries;

    private double[] keptAm;
    private double s_o;
    private JLabel statusLabel;
    private boolean useLabels = false;
    private double upperq;
    private int totalToFit;
    private double dmax_pi;
    private double pi_invDmax;
    private double median;
    private double totalRoundsForRefinement;
    private double standardizedMin;
    private double standardizedRange;
    private AtomicInteger counter = new AtomicInteger(0);
    private boolean useBackgroundInFit;

    public RefineManager(RealSpace dataset, int numberOfCPUs, int refinementRounds, double rejectionCutOff, double lambda, boolean useL1, boolean excludeBackground){

        this.numberOfCPUs = numberOfCPUs;
        this.dataset = dataset;
        this.refinementRounds = refinementRounds;
        this.rejectionCutOff = rejectionCutOff;
        this.lambda = lambda;
        this.useL1 = useL1;
        this.useBackgroundInFit = excludeBackground;

        totalToFit = dataset.getfittedqIq().getItemCount();

        totalRoundsForRefinement = refinementRounds;
        roundsPerCPU = (int)(this.refinementRounds/(double)numberOfCPUs);

        bins = dataset.getTotalMooreCoefficients();
        //bins = (int) (Math.round(dataset.getfittedqIq().getMaxX() * dataset.getDmax() / Math.PI));  // excludes term related constant background

        //upperq = dataset.getAllData().getX(dataset.getStop()-1).doubleValue();
        upperq = dataset.getfittedqIq().getMaxX();
        this.dmax = dataset.getDmax();
        this.dmax_pi = Math.PI*dmax;
        this.pi_invDmax = Math.PI/dmax;

        this.standardizedSeries = new XYSeries("Standard set");

        this.standardizeData();
        dataset.setStandardizationMean(standardizedMin, standardizedRange);

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
            Runnable bounder = new Refiner(this.standardizedSeries, dataset.getfittedError(), roundsPerCPU, statusLabel);
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
        //dataset.setStandardizationMean(standardizedMean, standardizedStDev);

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
        private XYSeries errorActiveSet;
        private XYSeries relativeErrorSeries;
        //private boolean useNotify = false;
        private int totalRuns;
        private int sizeAm;

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

            //activeIqSet = iofQ.createCopy(0, totalItems-1);
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

            ArrayList<double[]> results;

            XYSeries randomSeries = new XYSeries("Random-");

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
                            samplingNumber = 13;
                        } else {
                            samplingNumber = 13;
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
                PrObject prObject = new PrObject(randomSeries, upperq, dmax, lambda);

                if (useL1){
                    // fixed q range
                    results = prObject.moore_pr_L1();
                } else {
                    if (useBackgroundInFit){
                        results = prObject.moore_coeffs_L1();
                    } else {
                        results = prObject.moore_coeffs_L1_no_bg();
                    }
                }

                // Calculate Residuals
                tempMedian = medianMooreResiduals(activeSet, results.get(0)); // squared residual

                if (tempMedian < getMedian()){ // minimize on medium residual in activeset
                    // keep current values
                    notifyUser(String.format("New median residual %1.5E < %1.4E at round %d", tempMedian, median, i));
                    //status.setText("Improved fit, new median residual: " + tempMedian + " < " + median + " at round " + i );
                    sizeAm = results.get(0).length;
                    setkeptAm(results.get(0), sizeAm);
                    setMedian(tempMedian);

                    setS_o( 1.4826*(1.0 + 5.0/(size - sizeAm - 1))*Math.sqrt(median) ); // size is number of elements in active set
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
         * @param data XYSeries of (q, qI(q))
         * @param a_m Moore Coefficients
         * @return Median residual as float
         */
        private double medianMooreResiduals(XYSeries data, double[] a_m){

            List<Double> residuals = new ArrayList<Double>();

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

                resultM = a_m[0]*q;
                for(int j=1; j < sizeAm; j++){
                    //a_matrix.set(r, c, TWO_INV_PI*pi_d * c * FastMath.pow(-1.0, c + 1) * FastMath.sin(qd) / (n_pi_squared[c] - qd2));
                    resultM = resultM + Constants.TWO_DIV_PI*a_m[j]*j*dmax_pi*Math.pow(-1,j+1)*sin_dmaxq/(j*j*Constants.PI_2 - dmax_q2);
                }
                // should this be abs(residuals) or squared?
                // data has been standardized, so no adjustment necessary before residual calculation.
                residuals.add(Math.pow((tempitem.getYValue() - resultM), 2));
            }
            return Statistics.calculateMedian(residuals);
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

        double[] keptAm = this.getKeptAM(); // scaled to standardized data
        int sizeAm = keptAm.length;

        XYDataItem tempData;
        //XYSeries activeqIqSet = dataset.getfittedqIq();      // this is scaled data
        XYSeries activeqIqSet = this.standardizedSeries;
        XYSeries errorActiveSet = dataset.getfittedError();  // these errors are unscaled

        dataset.getCalcIq().clear();

        int size = activeqIqSet.getItemCount();

        for (int i=0; i < size; i++){
            tempData = activeqIqSet.getDataItem(i); // potentially scaled data
            xObsValue = tempData.getXValue();
            yObsValue = tempData.getYValue(); // standardized in form of qIq

            double dmax_q = dmax*xObsValue;
            double dmax_q2 = dmax_q*dmax_q;
            double sin_dmaxq = Math.sin(dmax_q);

            double resultM = keptAm[0]*xObsValue;
            for(int j=1; j < sizeAm; j++){
                resultM = resultM + Constants.TWO_DIV_PI*keptAm[j]*j*dmax_pi*Math.pow(-1,j+1)*sin_dmaxq/(j*j*Constants.PI_2 - dmax_q2);
            }
            // should this be abs(residuals) or squared?
            // data has been standardized, so no adjustment necessary before residual calculation.
            residual = yObsValue - resultM;

            if (Math.abs(residual/this.getS_o()) < rejectionCutOff){
                residualsList.add(residual*residual);
            } else {
                System.out.println(xObsValue +  " REJECTED " + Math.abs(residual/this.getS_o()) + " > " + rejectionCutOff);
            }
        }

        int sizeResiduals = residualsList.size();
        double sigmaM = 1.0/Math.sqrt(Statistics.calculateMean(residualsList)*sizeResiduals/(sizeResiduals - sizeAm));

        XYSeries keptqIq = new XYSeries("keptqIq-"+dataset.getId());
        // Select final values for fitting
        for (int i=0; i < size; i++){
            tempData = activeqIqSet.getDataItem(i);
            xObsValue = tempData.getXValue();
            yObsValue = tempData.getYValue();

            double dmax_q = dmax*xObsValue;
            double dmax_q2 = dmax_q*dmax_q;
            double sin_dmaxq = Math.sin(dmax_q);
            double resultM = keptAm[0]*xObsValue;
            for(int j=1; j < sizeAm; j++){
                resultM = resultM + Constants.TWO_DIV_PI*keptAm[j]*j*dmax_pi*Math.pow(-1,j+1)*sin_dmaxq/(j*j*Constants.PI_2 - dmax_q2);
            }
            // should this be abs(residuals) or squared?
            // data has been standardized, so no adjustment necessary before residual calculation.
            residual = yObsValue - resultM;
            //System.out.println(i + " residual : " + residual + " SIGMAM " + Math.abs(residual*sigmaM) + " <=> " + rejectionCutOff);
            if ( Math.abs(residual*sigmaM) < rejectionCutOff){
                keptSeries.add(dataset.getfittedIq().getDataItem(i));
                keptqIq.add(tempData);
                keptErrorSeries.add(xObsValue, errorActiveSet.getY(i).doubleValue());
            }
        }

        // update spinner with new limits of of keptSeries
        // estimate errors on invariants using sampling algorithm off of keptSeries
        // if keptqIq > bin*2 , then calculate PofR
        // otherwise use activeqIq
        if (keptqIq.getItemCount() > 2*bins){ // do final fitting against kepSeries

            dataset.updatedRefinedSets(keptSeries, keptErrorSeries);
            ArrayList<double[]> results;

            // calculate PofR
            PrObject prObject = new PrObject(keptqIq, keptqIq.getMaxX(), dmax, lambda);
            //System.out.println("QMAX FINAL : " + upperq + " " + keptqIq.getMaxX());
            if (useL1){
                // fixed q range
                System.out.println("FINAL REFINEMENT USING L1 " + useL1);
                results = prObject.moore_pr_L1();
                //results = prObject.moore_pr_L1_noBG();
            } else {
                if (useBackgroundInFit){
                    results = prObject.moore_coeffs_L1();
                } else {
                    results = prObject.moore_coeffs_L1_no_bg();
                }
            }

            dataset.setMooreCoefficients(results.get(0));
            dataset.calculateIntensityFromModel(true);


            try {
                // requires (q, Iq) not (q, q*Iq)
                // setMooreCoefficients calculates chi over the range of data bounded by spinners
                // need chi specific to keptSeries
                int totalKept = keptSeries.getItemCount();
                keptAm = results.get(0);
                sizeAm = keptAm.length;

                for(int i=0; i < totalKept;i++){
                    tempData = keptSeries.getDataItem(i);
                    keptSeries.update(tempData.getX(), (tempData.getYValue()));
                    if (tempData.getYValue() > 0) { // only positive values
                        xObsValue = tempData.getXValue();
                        double dmax_q = dmax*xObsValue;
                        double dmax_q2 = dmax_q*dmax_q;
                        double sin_dmaxq = Math.sin(dmax_q);
                        double resultM = keptAm[0];
                        for(int j=1; j < sizeAm; j++){
                            resultM = resultM + Constants.TWO_DIV_PI*keptAm[j]*j*dmax_pi*Math.pow(-1,j+1)*sin_dmaxq/(j*j*Constants.PI_2 - dmax_q2);
                        }

                        //(resultM*standardizationStDev + standardizationMean)*invq;
                        dataset.getCalcIq().add(tempData.getXValue(), Math.log10( (resultM*standardizedRange + standardizedMin)/xObsValue ) ); //plotted data is unscaled, as is from Analysis
                        //dataset.getCalcIq().add(tempData.getXValue(), Math.log10( (resultM/xObsValue + keptAm[0])*standardizedRange ) ); //plotted data is unscaled, as is from Analysis
                    }
                }

                dataset.chi_estimate(keptSeries, keptErrorSeries);  //

            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            int totalrejected =  totalToFit - keptSeries.getItemCount();
            double percentRejected = (double)totalrejected/(double)totalToFit*100;
            notifyUser(String.format("Rejected %d points (%.1f %%) using cutoff: %.4f => files written to working directory", totalrejected, percentRejected, rejectionCutOff));

            RefinedPlot refinedPlot = new RefinedPlot(dataset);
            refinedPlot.makePlot(String.format("Rejected %d points (%.1f %%) using cutoff: %.4f => files written to working directory", totalrejected, percentRejected, rejectionCutOff));

        } else { // do nothing if unacceptable, so clear and reload a original

            for (int i=0; i<size; i++){
                tempData = activeqIqSet.getDataItem(i);
                xObsValue = tempData.getXValue();
                yObsValue = tempData.getYValue();

                //dataset.getfittedIq().add(tempData);

//                if (yObsValue > 0){ // only positive values
//                    tempData = new XYDataItem(xObsValue, Math.log10(yObsValue));
//                    dataset.getLogData().add(tempData);  // plotted I(q)
//                }
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

    /**
     *
     * standardize data
     */
    private void standardizeData(){
        XYDataItem tempData;
        XYSeries fitteddqIqRegion = dataset.getfittedqIq();
        double tempSumForStandard = 0;
        double squared = 0;
        double value;

        int totalItems = fitteddqIqRegion.getItemCount();

        for(int r=0; r<totalItems; r++){
            tempData = fitteddqIqRegion.getDataItem(r);
            value = tempData.getYValue()/tempData.getXValue();
            squared += value*value;
            tempSumForStandard += value;
        }

        //standardizedMean = tempSumForStandard/totalItems;
        //standardizedStDev = Math.sqrt(squared/totalItems - standardizedMean*standardizedMean);
        //double invstdev = 1.0/standardizedStDev;

        standardizedMin = fitteddqIqRegion.getMinY();
        standardizedRange = fitteddqIqRegion.getMaxY() - standardizedMin;
        double invstdev = 1.0/standardizedRange;

        for(int r=0; r<totalItems; r++){
            tempData = fitteddqIqRegion.getDataItem(r);
            standardizedSeries.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
        }

        //for(int r=0; r<totalItems; r++){
        //    tempData = fitteddqIqRegion.getDataItem(r);
        //    standardizedSeries.add(tempData.getX(), (tempData.getYValue()/tempData.getXValue() - standardizedMean)*invstdev*tempData.getXValue());
            //activeSet.add(tempData.getX(), (tempData.getYValue()/tempData.getXValue() - standardizedMean)*invstdev*tempData.getXValue());
        //}
    }

}
