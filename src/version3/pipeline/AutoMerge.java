package version3.pipeline;

import org.apache.commons.math3.util.MathArrays;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import version3.Collection;
import version3.Dataset;
import version3.Functions;
import version3.SubtractedSetsMerging;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by robertrambo on 07/10/2016.
 */
public class AutoMerge extends SwingWorker {

    private Collection collection;
    private XYSeries baseLine;
    private XYSeries averageRefSeries;
    private XYSeriesCollection dataSetsToPlot = new XYSeriesCollection();
    private XYSeriesCollection deltas = new XYSeriesCollection();
    private Number qmax;
    private Number qmin;
    private int binSize = 11;
    private int numberOfSeriesInBase;
    private int totalBins;
    private boolean useScale = false;

    public AutoMerge(Collection collection, int numberOfframesInBase){
        this.collection = collection;
        numberOfSeriesInBase=numberOfframesInBase;
        System.out.println("AUTO MERGE");
    }

    @Override
    protected Void doInBackground() throws Exception {

        this.setCommonQValues();
        System.out.println("QMIN : " + qmin + " QMAX " + qmax);
        // for qmin up till qmax, calculate slope
        Dataset refData = collection.getDataset(0);
        int totalInCollection = collection.getDatasetCount();
        // find first q-value in common to all?
        XYSeries refAllData = refData.getAllData();
        int totalRef = refAllData.getItemCount();

        double[] indices = new double[totalInCollection];
        double[] fitThis = new double[totalInCollection];
        for(int i=0; i<totalInCollection; i++){
            indices[i] = i*1.0;
        }

        XYSeries slopes = new XYSeries("slopes");

        for(int i=0; i<totalRef; i++){
            Number qTest = refAllData.getX(i);  //

            if (qTest.doubleValue() >= qmin.doubleValue() && qTest.doubleValue() <= qmax.doubleValue()){
                fitThis[0] = qTest.doubleValue();
                // for all other datasets, check if qmin exists
                for(int j=1; j<totalInCollection; j++){
                    XYSeries tempAllData = collection.getDataset(j).getAllData();
                    int check = tempAllData.indexOf(qTest);

                    if (check > -1){
                        fitThis[j] = tempAllData.getY(check).doubleValue();
                    } else { //interpolate

                    }
                }
                // perform fit
                double[] fit = Functions.leastSquares(indices, fitThis);
                slopes.add(qTest.doubleValue(), fit[0]);
            }
        }

        int total = slopes.getItemCount();

       // for(int i=0; i<total;i++){
       //     System.out.println(slopes.getX(i) + " " + slopes.getY(i));
       // }
        // how does slope change as a function of q
        // ratio should be one, calculate distance from line at 1 to ratio of points
        //createSquaredRatioResidual(refAllData, collection.getDataset(1).getAllData());
        //createSquaredRatioResidual(refAllData, collection.getDataset(6).getAllData());
        //createSquaredRatioResidual(refAllData, collection.getDataset(20).getAllData());

        setBaseLine();  // create XYSeries from q_min to q_max
        //
        if (useScale){
            for (int h=0; h < totalInCollection; h++){
                XYSeries target = collection.getDataset(h).getAllData();
                createAndAddSquaredRatioResidual(averageRefSeries, target);
            }
        } else {

            for (int h=0; h < totalInCollection; h++){
                XYSeries target = collection.getDataset(h).getAllData();
                createAndAddSquaredRatioResidual(averageRefSeries, target);
            }
        }

        // compare each to baseLine (baseLine is binned)

        int totalBins = baseLine.getItemCount();

        // calculate del for each in
        for (int h=0; h < totalInCollection; h++){

            deltas.addSeries(new XYSeries(h));
            XYSeries tempSeries = deltas.getSeries(h);

            for(int j=0; j<totalBins; j++){
                tempSeries.add(j, (dataSetsToPlot.getY(h, j).doubleValue() - baseLine.getY(j).doubleValue()));
            }
        }

        // baseline is the average of the 1st 3
        // if aggregation is present tar/ref > 1
        // what about concentration dependent scattering?
        // proper baseline will be at high q

        // what does the ratio of singular values do?

        for(int j=0; j<totalBins; j++){
            System.out.println(j + " " + dataSetsToPlot.getSeries(2).getY(j).doubleValue());
        }

        System.out.println("NEXT");
        for(int j=0; j<totalBins; j++){
            System.out.println(j + " " + dataSetsToPlot.getSeries(5).getY(j).doubleValue());
        }

        System.out.println("NEXT");
        for(int j=0; j<totalBins; j++){
            System.out.println(j + " " + dataSetsToPlot.getSeries(13).getY(j).doubleValue());
        }

        // radiation damage is a consistent uplift in the signal
        // make comparison of all frames
        return null;
    }


    private void setUseScale(boolean value){
        useScale = value;
    }

    /**
     * Using the first N frames (numberOfSeriesInBase)
     * Calculate ratio to first frame
     * Average the ratio
     * Subtract each from 1 => diff = ratio - 1
     * Sum the squared diff in a bin
     * Add to baseLine
     */
    private void setBaseLine(){
        XYSeries tempSeries;

        averageRefSeries = new XYSeries("average");

        baseLine = new XYSeries("base line");
        XYSeries ref = collection.getDataset(0).getAllData();
        int totalRef = ref.getItemCount();

        double divisor = 1.0/(double)numberOfSeriesInBase;
        double diff=0, sum=0;

        // create an average ratio of q-values
        for(int i=0; i<totalRef; i++){

            Number qTest = ref.getX(i);  //
            if (qTest.doubleValue() >= qmin.doubleValue() && qTest.doubleValue() <= qmax.doubleValue()){
                // calculate ratio and subtract from 1
                //diff=0;
                sum=0;
                for(int j=0; j<numberOfSeriesInBase; j++){
                    XYSeries target = collection.getDataset(j).getAllData();
                    int check = target.indexOf(qTest);  // remove outliers

                    if (check > -1){
                        //diff += target.getY(check).doubleValue()/ref.getY(i).doubleValue(); // ratio should be one
                        sum += target.getY(check).doubleValue(); // ratio should be one
                    } else { //interpolate

                    }
                }
                //tempSeries.add(qTest, sum*divisor); // average I(q) at given q
                averageRefSeries.add(qTest, sum*divisor);
            }
        }

        // calculate baseline bin

        totalRef = averageRefSeries.getItemCount();
        tempSeries = collection.getDataset(0).getAllData();


        binLoop:
        for(int i=0; i<totalRef; i++){

            if ((i+binSize) > totalRef){
                break binLoop;
            }

            double[] dataholder = new double[binSize];

            for(int b=0; b<binSize; b++){

                int index = i+b;

                if (index > totalRef){
                    break binLoop;
                }

                Number qTest = averageRefSeries.getX(index);  //
                if (qTest.doubleValue() >= qmin.doubleValue() && qTest.doubleValue() <= qmax.doubleValue()){
                    // calculate ratio and subtract from 1
                    diff = 1-tempSeries.getY(tempSeries.indexOf(qTest)).doubleValue()/averageRefSeries.getY(index).doubleValue();
                    dataholder[b] = diff;
                }
            }

            double tempsum = sumItWithRejection(dataholder); // sum should be close to zero if random
            // add sum to plot
            baseLine.add(1.0*i, tempsum);
            System.out.println(i + " " + tempsum);
        }
    }


    /**
     * In a moving window, calculate sum of squared residual (-1)
     *
     * @param ref
     * @param target
     */
    private void createAndAddSquaredRatioResidual(XYSeries ref, XYSeries target){

        int totalRef = ref.getItemCount();
        double sum, diff;
        int currentSet = dataSetsToPlot.getSeriesCount();
        dataSetsToPlot.addSeries(new XYSeries(currentSet));

        binLoop:
        for(int i=0; i<totalRef; i++){

            sum=0;
            if ((i+binSize) > totalRef){
                break binLoop;
            }

            double[] dataholder = new double[binSize];

            for(int b=0; b<binSize; b++){

                int index = i+b;

                if (index > totalRef){
                    break binLoop;
                }

                Number qTest = ref.getX(index);  //
                if (qTest.doubleValue() >= qmin.doubleValue() && qTest.doubleValue() <= qmax.doubleValue()){
                    // calculate ratio and subtract from 1
                    int check = target.indexOf(qTest);  // remove outliers
                    if (check > -1){
                        diff = 1-target.getY(check).doubleValue()/ref.getY(index).doubleValue();
                        dataholder[b] = diff;
                        sum += diff;
                    } else { //interpolate

                    }
                }
            }

            double tempsum = sumItWithRejection(dataholder);
            // add sum to plot
            dataSetsToPlot.getSeries(currentSet).add(1.0*i, tempsum);
            System.out.println(currentSet + " " + i + " " + sum + " " + tempsum);
        }

        totalBins = dataSetsToPlot.getSeries(0).getItemCount();
    }


    private double sumItWithRejection(double[] values){
        Arrays.sort(values);
        double median = Functions.median(values);

        int total = values.length;
        double[] deviation = new double[total];
        // calculate MAD
        for(int i=0; i<total; i++){
            deviation[i] = Math.abs(values[i] - median);
        }

        Arrays.sort(deviation);
        double mad = 1.4826*Functions.median(deviation);
        double inv_mad = 1.0/mad;

        // reject outliers
        double sum = 0;
        for(int i=0; i<total; i++){
            if ((Math.abs(values[i] - median)*inv_mad) < 2.5){
                sum += values[i];
            }
        }

        return sum;
    }


    // create list of common q-vales
    private void setCommonQValues(){

        Dataset refData = collection.getDataset(0);
        int totalInCollection = collection.getDatasetCount();
        // find first q-value in common to all?
        XYSeries refAllData = refData.getAllData();
        int totalRef = refAllData.getItemCount();

        qmin = refAllData.getX(0);

        searchLoop:
        for(int i=0; i<totalRef; i++){
            Number qTest = refAllData.getX(i);  //

            int leave = 1;
            // for all other datasets, check if qmin exists
            for(int j=1; j<totalInCollection; j++){
                XYSeries tempAllData = collection.getDataset(j).getAllData();

                int check = tempAllData.indexOf(qTest);

                if (check > -1){
                    leave++;
                } else {
                    // can it be interpolated?
                    // are there at least 3 points before the q-value to interpolate
                    if (tempAllData.getX(3).doubleValue() < qTest.doubleValue()){
                        leave++;
                    } else { // break and move to next qmin
                        break;
                    }
                }
            }

            // break if I made it all the way
            if (leave == totalInCollection){
                qmin = qTest;
                break searchLoop;
            }
        }


        qmax = refAllData.getX(totalRef-1);

        searchLoop:
        for(int i=totalRef-1; i>0; i--){
            Number qTest = refAllData.getX(i);  //

            int leave = 1;
            // for all other datasets, check if qmin exists
            for(int j=1; j<totalInCollection; j++){
                XYSeries tempAllData = collection.getDataset(j).getAllData();

                int check = tempAllData.indexOf(qTest);

                if (check > -1){
                    leave++;
                } else {
                    // can it be interpolated?
                    // are there at least 3 points before the q-value to interpolate
                    if (tempAllData.getX(totalRef-3).doubleValue() > qTest.doubleValue()){
                        leave++;
                    } else { // break and move to next qmin
                        break;
                    }
                }
            }

            // break if I made it all the way
            if (leave == totalInCollection){
                qmax = qTest;
                break searchLoop;
            }
        }
    }


}
