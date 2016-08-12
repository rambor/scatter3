package version3;

import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class StatMethods {

    public static double kurtosis(ArrayList<Double> values){
        double kurtosis = 0;
        int pointsToUse = values.size();

        double m2=0, m4=0, diff, mm;
        double mean = mean_array_list(values);
        double inverse_n = 1.0/pointsToUse;

        for (int i=0; i<pointsToUse; i++){
            diff = (values.get(i) - mean);
            mm = diff*diff;
            m2 += mm;
            m4 += mm*mm;
        }

        double quotient = inverse_n*m2; // standard deviation squared => variance
        kurtosis = inverse_n*m4/(quotient*quotient) - 3;

        return kurtosis;
    }


    public static double prunedKurtosis(ArrayList<Double> values){
        int pointsToUse = values.size();

        double[] meanVariance = mean_variance_array_list(values);
        double mean = meanVariance[0];
        double invStdev = 1.0/Math.sqrt(meanVariance[1]);

        ArrayList<Double> keepers = new ArrayList<>();

        double xvalue;
        for (int i=0; i<pointsToUse; i++){
            xvalue = values.get(i);
            if (Math.abs(xvalue - mean)*invStdev < 2.5){
                keepers.add(xvalue);
            }
        }

        return kurtosis(keepers);
    }


    public static double[] mean_variance_array_list(ArrayList<Double> values){

        int total = values.size();
        double sum = 0;
        double sum2 = 0;
        double xvalue;
        double[] valuesToReturn = new double[2];

        for (int i=0; i< total; i++){
            xvalue = values.get(i);
            sum2 += xvalue*xvalue;
            sum += xvalue;
        }

        double invTotal = 1.0/(double)total;
        valuesToReturn[0] = sum*invTotal;
        valuesToReturn[1] = sum2*invTotal - valuesToReturn[0]*valuesToReturn[0];

        return valuesToReturn;
    }


    public static double mean_array_list(ArrayList<Double> values){
        int total = values.size();
        double sum = 0;
        for (int i=0; i< total; i++){
            sum += values.get(i);
        }

        return (sum/total);
    }

    public static ArrayList<XYSeries> medianDatasets(Collection thisCollection){
        // Median Set
        // Determine all open files
        // Need the master list of all unique q values
        // create set of unique q values from the collection using TreeSet
        TreeSet<Double> qList;
        qList = new TreeSet<Double>();

        HashMap<Double, ArrayList<Double>> intValuesHash = new HashMap<Double, ArrayList<Double>>();
        HashMap<Double, ArrayList<Double>> errValuesHash = new HashMap<Double, ArrayList<Double>>();


        int lowerQI;
        int upperQI;

        double qValue;
        double scaleF;

        Dataset tempDataSet;

        int limit = thisCollection.getDatasets().size();
        for (int i=0; i < limit; i++){
            // only checked boxes
            if (thisCollection.getDataset(i).getInUse()){

                tempDataSet = new Dataset(thisCollection.getDataset(i));

                try {
                    XYSeries series7 = (XYSeries) tempDataSet.getData().clone(); // plotted datset
                    XYSeries series8 = (XYSeries) tempDataSet.getAllData().clone();
                    XYSeries series9 = (XYSeries) tempDataSet.getAllDataError().clone();
                    //add q-values to qList
                    lowerQI = series8.indexOf(series7.getX(0));
                    upperQI = series8.indexOf(series7.getMaxX());
                    scaleF = tempDataSet.getScaleFactor();

                    for (int j = lowerQI; j <= upperQI; j++){
                        // make entry in qList if not present
                        // if present
                        qValue = series8.getX(j).doubleValue();
                        if (qList.add(qValue)) {
                            //if true, add entry to ArrayList of Intensity Values that will be used for median
                            intValuesHash.put(qValue, new ArrayList<Double>( Arrays.asList(series8.getY(j).doubleValue() * scaleF) ));
                            errValuesHash.put(qValue, new ArrayList<Double>( Arrays.asList(series9.getY(j).doubleValue() * scaleF) ));
                        } else {
                            // if already present, find the index
                            intValuesHash.get(qValue).add(series8.getY(j).doubleValue()*scaleF);
                            errValuesHash.get(qValue).add(series9.getY(j).doubleValue()*scaleF);
                        }
                    }

                } catch (CloneNotSupportedException ex) {
                    //private static final Logger log = Logger.getLogger( ClassName.class.getName() );
                    Logger.getLogger("From StatMethods: MedianDatasets").log(Level.SEVERE, null, ex);
                    //Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }



        int qListSize;

        double median_value;
        double err_value;
        double err_valueU;
        double err_valueL;
        double lowerq;
        double upperq;
        int middlePoint;

        ArrayList<Double> tempArrayList;
        ArrayList<Double> sortList;

        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();
        XYSeries medianSet = new XYSeries("median_set_from_collection");
        XYSeries medianErrorSet = new XYSeries("median_error_set_from_collection");

        for (Double qvalue_key: intValuesHash.keySet()){

            tempArrayList = intValuesHash.get(qvalue_key);
            qListSize = tempArrayList.size();

            median_value = 0;
            err_value = 0;

            if (qListSize == 1 ){  //
                median_value = tempArrayList.get(0);
                err_value = errValuesHash.get(qvalue_key).get(0);

                //   qieSeries.add(new DataLine(tempQ, median_value, err_value, true));
            } else if (qListSize % 2 != 0) {  // odd
                middlePoint = (qListSize -1)/2;

                sortList = new ArrayList(tempArrayList); //makes a copy
                Collections.sort(sortList);
                median_value = sortList.get(middlePoint);
                err_value = errValuesHash.get(qvalue_key).get(tempArrayList.indexOf(median_value));

            } else { // even
                median_value = Statistics.calculateMedian(tempArrayList);
                sortList = new ArrayList(tempArrayList); //makes a copy
                Collections.sort(sortList);

                upperQI = tempArrayList.size()/2;
                lowerQI = upperQI-1;
                upperq = sortList.get(upperQI);
                lowerq = sortList.get(lowerQI);
                // look up values and reset to un-sorted array
                upperQI = tempArrayList.indexOf(upperq);
                lowerQI = tempArrayList.indexOf(lowerq);
                // get corresponding error values from datasets
                err_valueU = errValuesHash.get(qvalue_key).get(upperQI);
                err_valueL = errValuesHash.get(qvalue_key).get(lowerQI);
                err_value = 0.5*(err_valueL + err_valueU); // propagated error is an average
            }

            medianSet.add((double)qvalue_key, median_value);
            medianErrorSet.add((double)qvalue_key, err_value);

            tempArrayList = null;
        }

        returnMe.add(medianSet);
        returnMe.add(medianErrorSet);

        // write out new Median DataSet
        return returnMe;
    }

    public static ArrayList<XYSeries> weightedAverageDatasets(Collection selected){

        int ref = 0;
        int limit = selected.getDatasets().size();

        // use last as reference
        for(int i=0; i< limit; i++){
            if (selected.getDataset(i).getInUse()){
                ref = i;
            }
        }

        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();

        Dataset reference = selected.getDataset(ref);
        XYSeries tempRefData;

        tempRefData = reference.getData(); // log10 data used for plotting
        double referenceQmin = tempRefData.getMinX();
        double referenceQmax = tempRefData.getMaxX();

        double scale, lower, upper, referenceQ;
        int refIndex, count;

        XYSeries summedSet = new XYSeries("Summed");
        XYSeries summedSetError = new XYSeries("SummedError");

        double sigma, var, targetMax;
        Number targetMin;
        scale = reference.getScaleFactor();

        int lowerQI = reference.getAllData().indexOf(referenceQmin);
        int upperQI = reference.getAllData().indexOf(referenceQmax);
        int lowerT, upperT;
        /*
         * within limits of plotted data
         */
        for (int i=lowerQI; i < upperQI; i++){
            XYDataItem tempRefItem = reference.getAllData().getDataItem(i);
            sigma = 1.0/(scale*reference.getAllDataError().getY(i).doubleValue());
            var = sigma*sigma;

            summedSet.add(tempRefItem.getXValue(), scale * tempRefItem.getYValue()*var);
            summedSetError.add(tempRefItem.getXValue(), var);
        }

        XYDataItem tempDataItem, tempSummedItem;

        for (int i=0; i < limit; i++) {
            if (selected.getDataset(i).getInUse() && i != ref){

                Dataset tempDataset = selected.getDataset(i);

                XYSeries targetData  = tempDataset.getAllData();
                //XYSeries targetData  = tempDataset.getData();
                XYSeries targetError = tempDataset.getAllDataError();

                // get plotted domain of the target dataset
                targetMin = tempDataset.getData().getX(0);
                targetMax = tempDataset.getData().getMaxX();

                lowerT = targetData.indexOf(targetMin);
                upperT = targetData.indexOf(targetMax);

                lower = Math.max(referenceQmin, (Double)targetMin);
                upper = Math.min(referenceQmax, targetMax);

                scale = tempDataset.getScaleFactor();
                // iterate of the plotted range but within AllData (includes negative intensities)
                for(int j = lowerT; j <= upperT; j++){
                    //double target_q = targetData.getX(j).doubleValue();
                    tempDataItem = targetData.getDataItem(j);
                    double target_q = tempDataItem.getXValue();

                    if (((target_q < lower) || (target_q > upper)) && (summedSet.indexOf(target_q) < 0)) {
                        /*
                         *  if q value is outside range of reference
                         *  .indexOf() < 0 means value is not found in XYSeries
                         */
                        sigma = 1.0/(scale*targetError.getY(j).doubleValue());
                        var = sigma*sigma;

                        summedSet.add(target_q, scale * tempDataItem.getYValue()*var);
                        summedSetError.add(target_q, var );

                    } else {
                        refIndex = summedSet.indexOf(tempDataItem.getX());
                        if (refIndex >= 0) {
                         /*
                          * average the signal - first sum
                          */
                            tempSummedItem = summedSet.getDataItem(refIndex);

                            sigma = 1.0/(scale*targetError.getY(j).doubleValue());
                            var = sigma*sigma;

                            summedSet.updateByIndex(refIndex, (tempSummedItem.getYValue() + scale * targetData.getY(j).doubleValue() * var));
                            summedSetError.updateByIndex(refIndex, (summedSetError.getY(refIndex).doubleValue() + var));

                        } else if (lower < target_q && (target_q < upper)) {
                         /*
                          * interpolate
                          */
                            System.out.println("Interpolating ");
                            count = 0;
                            referenceQ = 0.0;
                            // find first value in reference greater than targetData.getX
                            while (summedSet.getX(count).doubleValue() < target_q && (count < 3000)) {
                                referenceQ = summedSet.getX(count).doubleValue();
                                count++;
                            }

                            Double[] results = Functions.interpolate(targetData, targetError, referenceQ, 1);
                            Double[] sigmaResults = Functions.interpolateSigma(targetError, referenceQ);
                            sigma = 1.0/(sigmaResults[1]*scale);
                            var = sigma*sigma;

                            //returns unlogged data
                            refIndex = summedSet.indexOf(referenceQ);

                            tempSummedItem = summedSet.getDataItem(refIndex);

                            summedSet.updateByIndex(refIndex, (tempSummedItem.getYValue() + scale * results[1] * var));
                            summedSetError.updateByIndex(refIndex, (summedSetError.getY(refIndex).doubleValue() + var ));

                        }
                    }
                }
            }
        }

        //double inv_total_n; // = 1.0/(double)total_n;
        for(int i=0; i<summedSet.getItemCount(); i++){

            var = summedSetError.getY(i).doubleValue();

            if ((Double.isNaN(var)) || (var == Double.POSITIVE_INFINITY)) {
                summedSet.updateByIndex(i, 0.0);
                summedSetError.updateByIndex(i, 1);
            } else {
                summedSet.updateByIndex(i, summedSet.getY(i).doubleValue() / var);
                summedSetError.updateByIndex(i, 1.0 / Math.sqrt(var));
            }

        }

        returnMe.add(summedSet);
        returnMe.add(summedSetError);

        return returnMe;
    }

}