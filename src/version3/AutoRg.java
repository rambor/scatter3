package version3;

import org.apache.commons.math3.util.FastMath;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by robertrambo on 24/05/2017.
 */
public class AutoRg {

    //private XYSeries data;
    private XYSeries qSquaredData;
    private XYSeries error;

    private XYSeries inputData;
    private XYSeries inputErrors;
    private XYSeries finalDataSetForFit;

    // data Intensities should be positive only
    public AutoRg(XYSeries data, XYSeries error, int startAt){
        inputData = data;
        inputErrors = error;
        this.autoRgTransformIt(startAt);
    }

    /**
     * transforms data to q^2 vs ln I(q)
     * @param startAt index from spinner
     * @return
     */
    private void autoRgTransformIt(int startAt){
        qSquaredData = new XYSeries("qq");
        error = new XYSeries("error");

        int total = inputData.getItemCount();
        int starthere = startAt-1;
        XYDataItem tempItem;
        for (int i=starthere; i<total; i++){
            tempItem = inputData.getDataItem(i);
            if (tempItem.getYValue() > 0){  // no negative values
                qSquaredData.add(tempItem.getXValue()*tempItem.getXValue(), Math.log(tempItem.getYValue()));
                error.add(inputErrors.getDataItem(i));
            }
        }
        //return autoRg(qq, error, 1);
    }



    private void autoRg() {

        //int last = data.getItemCount()-1;
        double slope, intercept, temp_resi, tempMedian, median = 100000;
        double tempRg, rg=0;

        XYDataItem lastItem, item;
        ArrayList<Double> resList = new ArrayList<>();
        ArrayList<Double> rgList = new ArrayList<>();

        // calculate line between first and last points
        int lastAtLimit = 0;
        double lowerqlimitSquared = 0.15*0.15; // q2
        double qRgLow = 0.2*0.2;
        double qRgUp = 1.31*1.31;

        // find the index of the upper q-value that is less than lowerqlimit
        while(qSquaredData.getX(lastAtLimit).doubleValue() < lowerqlimitSquared){
            lastAtLimit++;
        }
        int last = lastAtLimit;
        // get a rough estimate of Rg using data within qmax = 0.15;
        // only use the first and last points to define the line
        // shrinking window, try all pairs up to a limit
        int first = 1;
        XYDataItem tempDataItem = qSquaredData.getDataItem(first);
        while( tempDataItem.getXValue() < lowerqlimitSquared && first < (lastAtLimit-10)){  // minimum line is defined by 7 points
            // fit line to first and last point, calculate Rg, determine gRg limit
            while (last > first+7) {
                lastItem = qSquaredData.getDataItem(last);
                // calculate line using only first and last points
                slope = (lastItem.getYValue() - tempDataItem.getYValue())/(lastItem.getXValue() - tempDataItem.getXValue());
                intercept = lastItem.getYValue() - slope*lastItem.getXValue();
                tempRg = -3.0*slope;

                if (tempRg > 0 && lastItem.getXValue()*tempRg < qRgUp){  // means we have a reasonable limit
                    resList.clear();

                    for(int i=first; i<last; i++){
                        item = qSquaredData.getDataItem(i);
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

                last--; // remove the last point
            }
            last = lastAtLimit;
            //
            first++; // set first to next point
            tempDataItem = qSquaredData.getDataItem(first);
        }

        rg = Statistics.calculateMedian(rgList, true); // rough estimate of Rg

        double errorSlope = 0.0;
        double errorIntercept = 0.0;

        //create vector
        double xvalue, yqvalue;

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
        int itemCount = qSquaredData.getItemCount();
        for(int i=0; i < itemCount; i++){
            XYDataItem dat = qSquaredData.getDataItem(i);
            xvalue = dat.getXValue();

            if (xvalue*rg2 <= qRgLow){ // exclude points that are too low, like near beamstop?
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
                    XYDataItem dat = qSquaredData.getDataItem(i+startAtLimit);
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
                        XYDataItem dat = qSquaredData.getDataItem(v);
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
            for (int i = 0; i < endAt; i++) { // get the indices of the data items to keep
                dataItem = qSquaredData.getDataItem(i);
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
            finalDataSetForFit = new XYSeries("FinalDataset");
            //double[] final_w = new double[count];

            for (int i = 0; i < count; i++) {
                dataItem=qSquaredData.getDataItem(keepers.get(i));
                finalDataSetForFit.add(dataItem);
                final_x[i] = dataItem.getXValue();
                final_y[i] = dataItem.getYValue();
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

        } else {  // not enough points, so just fit a line to what we have
            // ignore first three points, take the next 5 and fit
            int arrayLimit = 7;
            x_range = new double[arrayLimit];
            y_range = new double[arrayLimit];
            //w_range = new double[5];

            getDataLoop:
            for (int i = 3; i < itemCount; i++) {
                XYDataItem dat = qSquaredData.getDataItem(i);
                xvalue = dat.getXValue(); // q^2
             //   yvalue = dat.getYValue();
                //x^2
                if (arrayIndex < arrayLimit) {
                    //x_data[i] = Math.pow(xvalue, 2);
                    x_range[arrayIndex] = xvalue;  // q^2
                    //ln(y)
             //       y_range[arrayIndex] = yvalue;  // ln(I(q))
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
            System.out.println("AutoRg failed, too few points in Guinier Region: " + qSquaredData.getKey());
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
