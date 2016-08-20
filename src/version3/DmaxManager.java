package version3;

import net.jafama.FastMath;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by robertrambo on 19/05/2016.
 */
public class DmaxManager extends SwingWorker<Void, Void> {

    private final double piinv2 = Math.PI*0.5;
    private final boolean useL1;
    private RealSpace realSet;
    private XYSeries fittedqIq;                // range of data used for the actual fit, may contain negative values
    private XYSeries errorFittedqIq;           // range of data used for the actual fit, may contain negative values
    private XYSeries dmaxQmax;
    private XYSeries dmaxChi2;
    private XYSeries qIq;
    private int startAt=0;
    private XYSeriesCollection prDistributions;
    private int numberOfCPUs=1;
    private double extrapolatedArea;
    double lambda;


    public DmaxManager(RealSpace dataset, int numberOfCPUs, double lambda, boolean useL1){

        this.realSet = dataset;

        this.fittedqIq = new XYSeries("fitted qIq");
        errorFittedqIq = realSet.getfittedError();

        int totalinfitted = realSet.getfittedqIq().getItemCount();
        XYSeries tempXY = realSet.getfittedqIq();
        for(int i=0; i<totalinfitted; i++){
            this.fittedqIq.add(tempXY.getX(i), tempXY.getY(i).doubleValue());
        }

        this.lambda = lambda;
        this.useL1 = useL1;
        this.numberOfCPUs = numberOfCPUs;
    }


    @Override
    protected Void doInBackground() throws Exception {
        // create window to plot data
        //
        dmaxChi2 = new XYSeries("DmaxVsChi2");
        dmaxQmax = new XYSeries("DmaxVsQMAX");
        //qIq = new XYSeries("qIQ for integral transform");
        prDistributions = new XYSeriesCollection();

        // 1. make extrapolated dataset
        extrapolateDataSet();
        extrapolateDataSetQ2I2();
        // unextrapolated data
        System.out.println("FINISHED EXTRAPOLATING");
        double qmax = qIq.getMaxX();
        double qmin = qIq.getX(startAt).doubleValue();
        double delta = (qmax-qmin)/(double)numberOfCPUs;
        int totalqiq = qIq.getItemCount();

        // 1. total indices = totalqiq - startAt
        // 2. divide the input dataset in to chunks for processing on multiple threads
        // 3. launch each and update XYSeries
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);
        int startHere = startAt;
        int nextStart = startHere;


        float tempChi2 = 100000;









        System.out.println("OPTIMIZING DMAX PLEASE WAIT");
        for (int i=0; i < numberOfCPUs; i++){

            // make XYSeries range to fit
            double upperq = delta*(1+i) + qmin;
            XYSeries tempXYBlock = new XYSeries("block");

            for(int j=0; j < totalqiq; j++){
                if (qIq.getX(j).doubleValue() > upperq){
                    nextStart = j;
                    break;
                }
                tempXYBlock.add(qIq.getDataItem(j));
            }


            XYSeries unextrapolatedqIq = new XYSeries("Unextrapolated");
            XYSeries unextrapolatedqIqError = new XYSeries("UnextrapolatedError");

            for(int j=0; j < fittedqIq.getItemCount(); j++){
                if (fittedqIq.getX(j).doubleValue() > upperq){
                    nextStart = j;
                    break;
                }
                unextrapolatedqIq.add(fittedqIq.getX(j),  fittedqIq.getY(j).doubleValue() );
                unextrapolatedqIqError.add(errorFittedqIq.getDataItem(j));
            }

            // startAt index
            Runnable findDmax = new Finder(startHere, tempXYBlock, unextrapolatedqIq, unextrapolatedqIqError);
            executor.execute(findDmax);

            startHere = nextStart;
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            //bar.setValue(0);
            //bar.setStringPainted(false);
            PrDistributionSearch plot = new PrDistributionSearch(prDistributions);
            plot.makePlot();

        } catch (InterruptedException e) {
            System.out.println("InterruptedException " + e.getMessage());
        }

        return null;
    }


    @Override
    protected void process(List<Void> chunks) {
        super.process(chunks);
    }


    public class Finder implements Runnable {

        private XYSeries dataBlock;
        private XYSeries qIqUnextrapolated;
        private XYSeries unextrapolatedError;
        private int startHere;
        private double invPi = 1.0/Math.PI;
        private double qmaxOfBlock;
        private XYSeries prDistribution;

        public Finder(int startHere, XYSeries qIqBlock, XYSeries qIqUnextrapolated, XYSeries qIqUnextrapolatedError) {
            this.startHere = startHere;
            dataBlock = qIqBlock;

            this.qIqUnextrapolated = qIqUnextrapolated;
            this.unextrapolatedError = qIqUnextrapolatedError;

            qmaxOfBlock = qIqBlock.getMaxX();
            prDistribution = new XYSeries("PR");
        }

        @Override
        public void run() {

            double startRvalue, nextRvalue, chi2;

            XYSeries tempSeries = new XYSeries("temp");

            XYSeries fittedSeries = new XYSeries("qIq");
            XYSeries fittedErrorSeries = new XYSeries("error");

            double checkq = dataBlock.getX(startHere).doubleValue();

            int stoppedAt=0;
            for(int i=0; i<qIqUnextrapolated.getItemCount(); i++){
                if (qIqUnextrapolated.getX(i).doubleValue() > checkq){
                    stoppedAt=i;
                    break;
                }
                fittedSeries.add(qIqUnextrapolated.getDataItem(i));
                fittedErrorSeries.add(unextrapolatedError.getDataItem(i));
            }

            for(int qm=0; qm < startHere; qm++){
                tempSeries.add(dataBlock.getDataItem(qm));
            }


            int q = startHere, qlimit, totalqIqValues = dataBlock.getItemCount();
            boolean changed;

            while(q < totalqIqValues){

//                startRvalue = 41;
//                changed = false;
//
//                // perform Newton root search
//                for (int round=0; round < 1000; round++){
//
//                    nextRvalue = startRvalue - integralTransform(startRvalue, tempSeries)/finiteDifferencesDerivative(startRvalue, tempSeries);
//
//                    if (integralTransform(nextRvalue, tempSeries) < 0.000000001){
//                        startRvalue = nextRvalue;
//                        changed = true;
//                        break;
//                    }
//
//                    startRvalue = nextRvalue;
//                }

                // start at 21
                double tempDmax = 21;

                while (tempDmax < 650){
                        // PrObject tempPr = new PrObject(this, lambda, usel1, cBox);
                        // fit un-interpolated data
                        // tempSeries must be q*Iq
                        // fit un-interpolated datasets
                        double tempLambda = 0.00001;
                        while (tempLambda < 0.0001){

                            PrObject tempPr = new PrObject(fittedSeries, tempSeries.getMaxX(), tempDmax, tempLambda);
                            ArrayList<double[]> results;

                            if (useL1){
                                // fixed q range
                                results = tempPr.moore_pr_L1();
                            } else {
                                results = tempPr.moore_coeffs_L1();
                            }

                            // calculate chi
                            try {

                                chi2 = this.chi_estimate(results.get(0), tempDmax, fittedSeries, fittedErrorSeries);
                                //System.out.println("CHI2 : " + chi2 + "  => " + tempDmax + " qmax : " + tempSeries.getMaxX() + " lambda " + tempLambda);
                                if (results.get(0).length > 3 && chi2 > 0.9 && chi2 < 1.5 && !calculatePofR(tempDmax, results.get(0))){
                                    // if true add prDistribution to collection
                                    // update plots in thread safe way so use process?
                                    System.out.println("CHI2 : " + chi2 + "  => " + tempDmax + " qmax : " + tempSeries.getMaxX() + " lambda : " + tempLambda);
                                    addPrDistributions(prDistribution, (int)tempDmax, tempSeries.getMaxX());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            tempLambda *= 1.4;
                        }

                    tempDmax += 1;
                }


                // fit P(r) using startRvalue and calculate chi and sk2
//                if (changed && startRvalue > 10 && startRvalue < 1000){
//                    // PrObject tempPr = new PrObject(this, lambda, usel1, cBox);
//                    // fit un-interpolated data
//                    // tempSeries must be q*Iq
//                    // PrObject tempPr = new PrObject(tempSeries, tempSeries.getMaxX(), startRvalue, lambda);
//                    // fit un-interpolated datasets
//                    double tempLambda = 0.00001;
//                    while (tempLambda < 0.0001){
//
//                        PrObject tempPr = new PrObject(fittedSeries, tempSeries.getMaxX(), startRvalue, tempLambda);
//                        ArrayList<double[]> results;
//
//                        if (useL1){
//                            // fixed q range
//                            results = tempPr.moore_pr_L1();
//                        } else {
//                            results = tempPr.moore_coeffs_L1();
//                        }
//
//                        // calculate chi
//                        try {
//
//                            //chi2 = this.chi_estimate(results.get(0), startRvalue, fittedSeries, fittedErrorSeries);
//                            chi2 = this.chi_estimate(results.get(0), startRvalue, fittedSeries, fittedErrorSeries);
//                            //System.out.println("CHI2 : " + chi2 + "  => " + startRvalue + " qmax : " + tempSeries.getMaxX() + " lambda " + tempLambda);
//
//                            if (results.get(0).length > 3 && chi2 > 0.9 && chi2 < 1.5 && !calculatePofR(startRvalue, results.get(0)) && !testParsevalsTheorem(startRvalue, results.get(0), fittedSeries)){
//                                // if true add prDistribution to collection
//                                // update plots in thread safe way so use process?
//                                System.out.println("CHI2 : " + chi2 + "  => " + startRvalue + " qmax : " + tempSeries.getMaxX() + " lambda : " + tempLambda);
//                                //testParsevalsTheorem(startRvalue, results.get(0), fittedSeries);
//                                addPrDistributions(prDistribution, (int)startRvalue, tempSeries.getMaxX());
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        tempLambda *= 1.4;
//                    }
//                }

                // add next 5 values
                qlimit = q + 7;
                if (qlimit < totalqIqValues){
                    for(int qi=q; qi < qlimit; qi++){
                        tempSeries.add(dataBlock.getDataItem(qi));
                    }
                }

                checkq = tempSeries.getMaxX();
                for(int i=stoppedAt; i < qIqUnextrapolated.getItemCount(); i++){
                    if (qIqUnextrapolated.getX(i).doubleValue() > checkq){
                        stoppedAt=i;
                        break;
                    }

                    fittedSeries.add(qIqUnextrapolated.getDataItem(i));
                    fittedErrorSeries.add(unextrapolatedError.getDataItem(i));
                }

                q = qlimit;
            }
        }


        /**
         * Data should be interpolated to zero q
         * Integrated data should be >= sum of squared coefficients (Bessel's inequality)
         * @param dmax
         * @param mooreCoefficients
         * @param data
         * @return
         */
        private boolean testParsevalsTheorem(double dmax, double[] mooreCoefficients, XYSeries data){
            boolean testFailed = false;

            XYSeries q2I2 = new XYSeries("q2I2");
            int totalData = data.getItemCount();
            XYDataItem tempItem;

            double y;
            for(int i=0; i<totalData; i++){
                tempItem = data.getDataItem(i);
                y = tempItem.getYValue(); // q*I(q)
                q2I2.add(tempItem.getX(), y*y);
            }

            double integration = piinv2*(1.0/dmax*(Functions.trapezoid_integrate(q2I2) + extrapolatedArea));
            int totalMoore = mooreCoefficients.length;

            double squaredSum=0;
            for (int i=1; i<totalMoore; i++){
                squaredSum += mooreCoefficients[i]*mooreCoefficients[i];
            }

            squaredSum = 0.25*mooreCoefficients[0]*mooreCoefficients[0] + 0.5*squaredSum;

            double diff = (integration - squaredSum)/(integration + squaredSum)*0.5;
            if (diff < 0){
                testFailed = true;
            }

            //System.out.println("Integrated: " + integration + " => squared coefficients " + squaredSum + " DIFF " + diff);

            return testFailed;

        }


        /**
         *
         * @param dmax
         * @param mooreCoefficients
         * @return
         */
        private boolean calculatePofR(double dmax, double[] mooreCoefficients){
            prDistribution.clear();
            prDistribution.add(0.0d, 0.0d);

            double totalPrPoints = ( Math.ceil( qmaxOfBlock * dmax * invPi)*3 );

            int r_limit = (int)totalPrPoints - 1, totalMooreCoefficients = mooreCoefficients.length;
            double deltar = dmax/totalPrPoints;

            double resultM;
            double inv_d = 1.0/dmax;
            double pi_dmax = Math.PI*inv_d;
            double inv_2d = 0.5*inv_d;
            double pi_dmax_r;
            double r_value;

            boolean negativeValuesInModel = false;

            for (int j=1; j < r_limit; j++){

                r_value = j*deltar;
                pi_dmax_r = pi_dmax*r_value;
                resultM = 0;

                for(int i=1; i < totalMooreCoefficients; i++){
                    resultM += mooreCoefficients[i]*FastMath.sin(pi_dmax_r*i);
                }

                prDistribution.add(r_value, inv_2d * r_value * resultM);

                if (resultM < 0){
                    negativeValuesInModel = true;
                }
            }

            prDistribution.add(dmax,0);
            return negativeValuesInModel;
        }


        /**
         * assumes I(q) and not q*I(q)
         * Estimates chi-square at the cardinal series
         * @throws Exception
         */
        private double chi_estimate(double[] results, double dmax, XYSeries data, XYSeries error) throws Exception{
            // how well does the fit estimate the cardinal series (shannon points)
            int totalMooreCoefficients = results.length;

            double chi=0;
            double inv_card;
            final double inv_dmax = 1.0/dmax;
            // n*PI/qmax
            final double pi_dmax = inv_dmax*Math.PI;
            double error_value;
            // Number of Shannon bins, excludes the a_o
            int bins = totalMooreCoefficients - 1, count=0, total = data.getItemCount();

            double df = 0; //1.0/(double)(bins-1);
            double cardinal, test_q = 0, diff;
            XYDataItem priorValue, postValue;
            Double[] values;
            // see if q-value exists but to what significant figure?
            // if low-q is truncated and is missing must exclude from calculation
            for (int i=1; i <= bins; i++){

                cardinal = i*pi_dmax;
                inv_card = 1.0/cardinal;

                // what happens if a bin is empty?
                searchLoop:
                while ( (test_q < cardinal) && (count < total) ){
                    test_q = data.getX(count).doubleValue();
                    // find first value >= shannon cardinal value
                    if (test_q >= cardinal){
                        break searchLoop;
                    }
                    count++;
                }

                // if either differences is less than 0.1% use the measured value
                if (count > 0 && count < total){
                    priorValue = data.getDataItem(count-1);
                    postValue = data.getDataItem(count);
                    if (Math.abs(priorValue.getXValue() - cardinal)*inv_card < 0.001) {// check if difference is smaller pre-cardinal

                        error_value = 1.0/error.getY(count - 1).doubleValue();
                        diff = priorValue.getYValue()/postValue.getXValue() - moore_Iq(postValue.getXValue(), dmax, results);//*invRescaleFactor;
                        chi += (diff*diff)*(error_value*error_value); // errors are unscaled
                        df += 1.0;
                    } else if ( Math.abs(postValue.getXValue() - cardinal)*inv_card < 0.001) {// check if difference is smaller post-cardinal

                        error_value = 1.0/error.getY(count).doubleValue();
                        diff = postValue.getYValue()/postValue.getXValue() - moore_Iq(postValue.getXValue(), dmax, results);//*invRescaleFactor;  // residual
                        chi += (diff*diff)*(error_value*error_value);
                        df += 1.0;
                    } else { // if difference is greater than 0.1% interpolate and also the cardinal is bounded

                        values = Functions.interpolateOriginal(data, error, cardinal);
                        //System.out.println("VALUE " + values[1] + " " + (results[0] + results[i]*dmax*0.5) + " " + values[2]);
                        // results is q*I(q)
                        diff = values[1] - (results[0] + results[i]*dmax*0.5); //*invRescaleFactor;
                        chi += (diff*diff)/(values[2]*values[2]);
                        df += 1.0;

                    }
                }
            }

            //System.out.println("dmax => " + dmax + " SUM " + chi + " " + (totalMooreCoefficients - df) + " (a_m).size : " + totalMooreCoefficients + " df : " + df);
            //return chi*1.0/(df-1);
            return chi*1.0/(totalMooreCoefficients-1);
        }


        /**
         *
         * @param rvalue
         * @param extrapolatedQIqSeries
         * @return
         */
        private double integralTransform(double rvalue, XYSeries extrapolatedQIqSeries){

            // create XYSeries
            XYSeries transformedSeries = new XYSeries("Sine Transformed");
            XYDataItem tempqIqDataItem;

            for (int i=0; i< extrapolatedQIqSeries.getItemCount(); i++){
                tempqIqDataItem = extrapolatedQIqSeries.getDataItem(i);
                transformedSeries.add(tempqIqDataItem.getX(), tempqIqDataItem.getYValue()*Math.sin(tempqIqDataItem.getXValue()*rvalue));
            }

            // trapezoid rule integration
            return Functions.trapezoid_integrate(transformedSeries);
        }



        /**
         * Use forward difference approximation to 2nd order to approximate derivative of P(r)-distribution
         * @param rvalue
         * @param extrapolatedQIqSeries
         * @return
         */
        private double finiteDifferencesDerivative(double rvalue, XYSeries extrapolatedQIqSeries){
            double h_increment = 0.7;
            // Centered
            //double sumIt = -integralTransform(rvalue+2*h_increment, extrapolatedQIqSeries) + 8*integralTransform(rvalue+h_increment, extrapolatedQIqSeries) - 8*integralTransform(rvalue-h_increment, extrapolatedQIqSeries) + integralTransform(rvalue-2*h_increment, extrapolatedQIqSeries);
            // Forward
            // double sumIt = -integralTransform(rvalue+2*h_increment, extrapolatedQIqSeries) + 4*integralTransform(rvalue+h_increment, extrapolatedQIqSeries) - 3*integralTransform(rvalue, extrapolatedQIqSeries);
            // Reverse
            double sumIt = 3*integralTransform(rvalue, extrapolatedQIqSeries) - 4*integralTransform(rvalue - h_increment, extrapolatedQIqSeries) + integralTransform(rvalue - 2*h_increment, extrapolatedQIqSeries);

            //return sumIt/(12*h_increment);
            return sumIt/(2*h_increment);
        }


        /**
         * Calculates intensities from moore coefficients
         * @param q single scattering vector point
         * @return Intensity I(q)
         */
        public double moore_Iq(double q, double dmax, double[] mooreCoefficients){
            double invq = 1.0/q;
            int totalMooreCoefficients = mooreCoefficients.length;

            double dmaxPi = dmax*Math.PI;
            double dmaxq = dmax*q;
            //double pi2 = Math.PI*Math.PI;

            double resultM = mooreCoefficients[0];
            //double twodivpi = 2.0/Math.PI;

            for(int i=1; i < totalMooreCoefficients; i++){
                resultM = resultM + Constants.TWO_DIV_PI*mooreCoefficients[i]*dmaxPi*i* FastMath.pow(-1,i+1)*FastMath.sin(dmaxq)/(Constants.PI_2*i*i - dmaxq*dmaxq);
            }
            return resultM*invq;
        }
    }


    private synchronized void addPrDistributions(XYSeries prToAdd, int dmaxInUse, double qmaxInUse){

        prDistributions.addSeries(new XYSeries("dmax : " + dmaxInUse + " qmax => " + qmaxInUse));

        int distributionLocale = prDistributions.getSeriesCount() - 1;

        XYSeries tempLocale = prDistributions.getSeries(distributionLocale);
        for (int k=0; k < prToAdd.getItemCount(); k++){
            tempLocale.add(prToAdd.getDataItem(k));
        }

        //System.out.println("Adding Distribution: " + prDistributions.getSeriesCount() + " DMAX " + dmaxInUse + " QMAX " + qmaxInUse);
    }


    private void extrapolateDataSet(){

        double startq = fittedqIq.getMinX();
        double guinierRg = realSet.getGuinierRg();
        double guinierIzero = realSet.getGuinierIzero();

        if (startq > 1.5/guinierRg){

        }

        java.util.List delq = new ArrayList();
        int itemCount = fittedqIq.getItemCount();

        for (int j = 1; j < (itemCount-1); j++){
            delq.add(Math.abs(fittedqIq.getX(j - 1).doubleValue() - fittedqIq.getX(j).doubleValue()));
        }

        double averageq = Statistics.calculateMean(delq);
        //Use deltaq to extrapolate
        double javerageq, javerageq2;
        double rg23 = guinierRg*guinierRg/3.0;

        double scaleFactor = realSet.getReciprocalSpaceScaleFactor();

        qIq = new XYSeries("qIq-extrapolated");

        // create extrapolation using Guinier estimates
        for (int j=0; j*averageq < startq; j++){
            javerageq = j*averageq;
            javerageq2 = (javerageq)*guinierIzero*Math.exp(-rg23*javerageq*javerageq);
            qIq.add(javerageq, javerageq2*scaleFactor);
        }

        // Add rest of the values
        XYDataItem tempqIqDataItem;
        double xValue, xyValue;
        for (int j=0; j < fittedqIq.getItemCount(); j++) {
            tempqIqDataItem = fittedqIq.getDataItem(j);

            xValue = tempqIqDataItem.getXValue();
            xyValue = tempqIqDataItem.getYValue();//*invRescaleFactor;
            qIq.add(xValue, xyValue * scaleFactor);
        }

        double lastqvalue = qIq.getMaxX();
        int totalqIqValues = qIq.getItemCount();

        if (lastqvalue > 0.1){
            for (int q=0; q<totalqIqValues; q++){
                if (qIq.getX(q).doubleValue() > 0.1){
                    startAt = q;
                    break;
                }
            }
        }
    }


    /**
     * extrapolate as a triangle
     */
    private void extrapolateDataSetQ2I2(){

        double endPointq = fittedqIq.getX(0).doubleValue(); // q
        double endPointIntensity = fittedqIq.getY(0).doubleValue(); // q*I(q)

        double endPointIntensitySquared = endPointIntensity*endPointIntensity;
        extrapolatedArea = 0.5*endPointq*endPointIntensitySquared;
    }


}
