package version3;

import org.apache.commons.math3.util.FastMath;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Created by robertrambo on 08/09/2016.
 */
public class SubtractedSetsMerging extends SwingWorker {
    private final Collection subtractedSets;
    private final int totalsets;
    private final int numberOfCPUs;
    private JPanel panel1;
    private JButton button1;
    private JPanel izeroRgPanel;
    private JPanel intensityPanel;
    private JPanel textPanel;
    private JList samplesList;
    private AtomicIntegerArray radii_of_gyrations;
    private AtomicIntegerArray izeroes;


    public SubtractedSetsMerging (Collection subtractedSets, int cpus) {

        this.subtractedSets = subtractedSets;
        // perform autoRg and get Rg and I(0) for each

        this.totalsets = subtractedSets.getDatasetCount();
        radii_of_gyrations = new AtomicIntegerArray(totalsets);
        izeroes = new AtomicIntegerArray(totalsets);


        this.numberOfCPUs = cpus;

    }


    @Override
    protected Object doInBackground() throws Exception {

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);
        for (int i=0; i < totalsets; i++){
            //Runnable bounder = new AutoRg()
            //executor.execute(bounder);
        }

        executor.shutdown();
        return null;
    }


    public void setSampleJList(JList list){
        this.samplesList = list;
    }

    private synchronized void updateRgIzeros(int index) {

    }

    /**
     * launch autoRg on multiple cores
     * pass in index of arrayist to update,
     */
    public class AutoRg implements Runnable{

        private final int indexToUpdate;
        private final int startAt;
        private XYSeries data;
        private XYSeries errors;

        public AutoRg(XYSeries data, XYSeries errors, int startAt, int indexToUpdate){
            this.startAt = startAt;
            this.data = data;
            this.errors = errors;
            this.indexToUpdate = indexToUpdate;
        }

        @Override
        public void run() {

        }

        /**
         * AutoRg algorithm for calculating I(zero) and Rg
         * @return array of doubles
         */
        private void autoRg() {

            int first = startAt;
            //int last = data.getItemCount()-1;
            double slope, intercept, temp_resi, tempMedian, median = 100000;
            double tempRg, rg=0;

            XYDataItem tempDataItem = data.getDataItem(first);
            XYDataItem lastItem, item;
            ArrayList<Double> resList = new ArrayList<>();
            ArrayList<Double> rgList = new ArrayList<>();
            // calculate line between first and last points
            int lastAtLimit = 0;
            double lowerqlimit = 0.12*0.12;
            double qRgLow = 0.2*0.2;
            double qRgUp = 1.3*1.3;

            while(data.getX(lastAtLimit).doubleValue() < lowerqlimit){
                lastAtLimit++;
            }
            int last = lastAtLimit;

            while( tempDataItem.getXValue() < lowerqlimit && first < (lastAtLimit-10)){  // minimum line is defined by 5 points
                // fit line to first and last point, calculate Rg, determine gRg limit
                while (last > first+7) {
                    lastItem = data.getDataItem(last);
                    // calculate line
                    slope = (lastItem.getYValue() - tempDataItem.getYValue())/(lastItem.getXValue() - tempDataItem.getXValue());
                    intercept = lastItem.getYValue() - slope*lastItem.getXValue();
                    tempRg = -3.0*slope;

                    if (tempRg > 0 && lastItem.getXValue()*tempRg < qRgUp){  // means we have a reasonable limit
                        resList.clear();

                        for(int i=first; i<last; i++){
                            item = data.getDataItem(i);
                            temp_resi = item.getYValue() - (slope*item.getXValue() + intercept);
                            resList.add(temp_resi*temp_resi);
                        }
                        // get median
                        tempMedian = Statistics.calculateMedian(resList, true);
                        if (tempMedian < median){
                            rgList.add(FastMath.sqrt(tempRg));
                            median = tempMedian;
                        }
                    }

                    last--;
                }
                last = lastAtLimit;
                //
                first++;
                tempDataItem = data.getDataItem(first);
            }

            rg = Statistics.calculateMedian(rgList, true); // rough estimate of Rg

            double errorSlope = 0.0;
            double errorIntercept = 0.0;
            int itemCount = data.getItemCount();

            //create vector
            double xvalue, yvalue;

            int count=0;
            double c0, c1;

            double minResidual = 10000000000.0;
            double[] x_range;
            double[] y_range;
            //double[] w_range;
            double r2_coeff = 0.0;

            double[] residuals3;
            //rg = Math.sqrt(-3.0*slope);
            double rg2 = rg*rg;
            double i_zero;

            int endAt = 0;
            int startAtLimit = 0;
            // how many points are within upperlimit?
            for(int i=0; i < itemCount; i++){
                XYDataItem dat = data.getDataItem(i);
                xvalue = dat.getXValue();

                if (xvalue*rg2 <= qRgLow){
                    startAtLimit++;
                }

                if ((xvalue*rg2 <= qRgUp)) {
                    endAt++;
                } else {
                    break;
                }
            }

            int sizeOfArray = endAt - startAtLimit + 1;
            // perform least median square fitting
            int arrayIndex = 0;

            double tempqmaxLimit, qmax13Limit =0;

//        long startTime = System.nanoTime();

            if (sizeOfArray > 5){
                int window = 13;
                x_range = new double[window];
                y_range = new double[window];
                double[] keptResiduals = new double[0];
                int keptStartAt=0;
                double keptSlope=0, keptIntercept=0;

                int upTO = startAtLimit + window;
                residuals3 = new double[endAt];
                keptResiduals = new double[endAt];

                while(upTO < endAt){

                    for (int i = 0; i < window; i++) {
                        XYDataItem dat = data.getDataItem(i+startAtLimit);
                        x_range[i] = dat.getXValue();  // q^2
                        y_range[i] = dat.getYValue();  // ln(I(q))
                    }

                    double[] param3 = Functions.leastSquares(x_range, y_range);
                    c1 = param3[0];
                    c0 = param3[1];

                    if (c1 < 0){ // slope has to be negative
                        //double tempRgat = Math.sqrt(-3*c1);
                        //tempqmaxLimit = 1.3/tempRgat;

                        //  if (tempqmaxLimit > qmax13Limit && (tempqmaxLimit*tempqmaxLimit) < x_range[window-1]){

                        for (int v = 0; v < endAt; v++) {
                            XYDataItem dat = data.getDataItem(v);
                            residuals3[v] = Math.pow((dat.getYValue() - (c1 * dat.getXValue() + c0)), 2);
                        }

                        Arrays.sort(residuals3);
                        double median_test = Functions.median(residuals3);

                        if (median_test < minResidual) {
                            minResidual = median_test;
                            System.arraycopy(residuals3, 0, keptResiduals, 0, endAt);
                            //keptStartAt = startAtLimit;
                            keptSlope = c1;
                            keptIntercept = c0;
                            //         qmax13Limit = tempqmaxLimit;
                        }
                        //   }

                    }

                    startAtLimit++;
                    upTO = startAtLimit + window;
                }

                double s_o = 1.4826 * (1.0 + 5.0 / (endAt - 2 - 1)) * Math.sqrt(minResidual);
                double inv_s_o = 1.0/s_o;

                // create final dataset for final fit
                count = 0;
                ArrayList<Integer> keepers = new ArrayList<Integer>();

                XYDataItem dataItem;
                for (int i = 0; i < endAt; i++) {
                    //residualAt = Math.pow((data.getY(i).doubleValue() - (keptSlope * data.getX(i).doubleValue() + keptIntercept)), 2);
                    dataItem = data.getDataItem(i);
                    //if (Math.abs((dataItem.getYValue() - (keptSlope * dataItem.getXValue() + keptIntercept))) * inv_sigma < 2.5) {
                    if (Math.abs((dataItem.getYValue() - (keptSlope * dataItem.getXValue() + keptIntercept))) * inv_s_o < 2.0) {
                        // decide which ln[I(q)] values to keep
                        keepers.add(i);
                        count++;
                    }
                }

                // determines values to keep for fitting to determine Rg and I(zero)
                double[] final_x = new double[count];
                double[] final_y = new double[count];
                //double[] final_w = new double[count];
                int keep;

                XYDataItem dat;
                for (int i = 0; i < count; i++) {
                    keep = keepers.get(i);
                    dat=data.getDataItem(keep);
                    final_x[i] = dat.getXValue();
                    final_y[i] = dat.getYValue();
                    //final_w[i] = errors.getY(keep).doubleValue();
                }

                arrayIndex = count;

                double[] param3 = Functions.leastSquares(final_x, final_y);
                slope = param3[0];
                intercept = param3[1];
                errorSlope = param3[2];
                errorIntercept = param3[3];
                rg = Math.sqrt(-3.0 * slope);
                i_zero = Math.exp(intercept);

            } else {
                // ignore first three points, take the next 5 and fit
                int arrayLimit = 7;
                x_range = new double[arrayLimit];
                y_range = new double[arrayLimit];
                //w_range = new double[5];

                getDataLoop:
                for (int i = 3; i < itemCount; i++) {
                    XYDataItem dat = data.getDataItem(i);
                    xvalue = dat.getXValue(); // q^2
                    yvalue = dat.getYValue();
                    //x^2
                    if (arrayIndex < arrayLimit) {
                        //x_data[i] = Math.pow(xvalue, 2);
                        x_range[arrayIndex] = xvalue;  // q^2
                        //ln(y)
                        y_range[arrayIndex] = yvalue;  // ln(I(q))
                        //ln(y)*error*y
                        //w_range[arrayIndex] = yvalue * errors.getY(i).doubleValue() * Math.exp(yvalue);
                        //original data
                        arrayIndex++;
                    } else {
                        break getDataLoop;
                    }
                }
                // fit line and use as default fit
                double[] param3 = Functions.leastSquares(x_range, y_range);
                slope = param3[0];
                intercept = param3[1];
                errorSlope = param3[2];
                errorIntercept = param3[3];

                rg = Math.sqrt(-3.0 * slope);
                i_zero = Math.exp(intercept);
            }

            //procedure for calculating Izero and Rg
            System.out.println("Determined Rg " + rg);
            double[] parameters = new double[6];

            if ((arrayIndex <= 7) || (Double.isNaN(rg) || (Double.isNaN(i_zero)))){
                System.out.println("AutoRg failed, too few points in Guinier Region: " + data.getKey());
                parameters[0]=0;
                parameters[1]=0;
                parameters[2]=0;
                parameters[3]=0;
                parameters[4]=0;
                parameters[5]=1; // percent rejected
            } else {
                parameters[0]=i_zero;
                parameters[1]=rg;
                parameters[2]=i_zero*errorIntercept;  //Izero Error
                parameters[3]=1.5*errorSlope*Math.sqrt(1/3.0*1/rg);  //Rg Error
                parameters[4]=r2_coeff;
                parameters[5]=(arrayIndex - count)/(double)arrayIndex; // percent rejected
            }

        }

    }





}
