package version3;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by xos81802 on 26/06/2017.
 */
public class ResidualDifferences {

    private double qmin, qmax;
    private XYSeries residuals;
    private XYSeries reference;
    private XYSeries targetSeries;
    private XYSeries targetError;
    private double average;
    private double variance, skewness;
    private double scaleFactor;
    private double durbinWatsonStatistic;
    private double ljungBoxStatistic;
    private double shapiroWilkStatistic;
    private int totalResiduals;
    private ArrayList<Double> sortedResiduals;
    private XYSeries binnedData;
    private XYSeries modelData;
    private double invGaussianNormalizationFactor;
    private double inv2variance ;
    private HistogramDataset histogram;

    private int lags; // should approximate the degrees of freedom of the data ??? // arbitrary bin size?

    private double[] shapiroWilkTable = {
            0.3770,0.2589,0.2271,0.2038,0.1851,0.1692,0.1553,0.1427,0.1312,0.1205,
            0.1105,0.1010,0.0919,0.0832,0.0748,0.0667,0.0588,0.0511,0.0436,0.0361,
            0.0288,0.0215,0.0143,0.0071
    };

    public ResidualDifferences(XYSeries referenceSet, XYSeries targetSet, XYSeries targetError, Number qmin, Number qmax, int lags) {

        this.reference = referenceSet;
        this.targetSeries = targetSet;
        this.qmax = qmax.doubleValue();
        this.qmin = qmin.doubleValue();
        this.targetError = targetError;
        this.lags = lags;
        this.makeResiduals();
        this.calculateDurbinWatson();
        this.calculateLjungBoxTest();
        this.estimateShapiroWilksTest(7);

        // perform some tests on the residuals
        // distribution should be random
        this.createBinnedData();
    }


    private void makeResiduals(){

        // for each dataset, compute pairwise ratios and quantitate
        int totalInReference = reference.getItemCount();
        int startPt=0;
        int endPt=totalInReference-1;

        for(int m=0; m<totalInReference; m++){
            if (reference.getX(m).doubleValue() >= qmin){
                startPt =m;
                break;
            }
        }

        for(int m=endPt; m>0; m--){
            if (reference.getX(m).doubleValue() <= qmax){
                endPt=m;
                break;
            }
        }


        int locale;
        XYDataItem tarXY, refItem;


        XYSeries tempvalues = new XYSeries("tempvalues");
        // compute ratio within qmin and qmax
        int totalInRatio=0;
        double valueE;
        double scale_numerator=0, scale_denominator=0;
        for (int q=startPt; q<=endPt; q++){
            refItem = reference.getDataItem(q);
            double xValue = refItem.getXValue();
            locale = targetSeries.indexOf(refItem.getX());

            if (locale > -1){
                tarXY = targetSeries.getDataItem(locale);
                tempvalues.add(tarXY);
                // reference/target
                valueE = targetError.getY(locale).doubleValue();
                scale_numerator += tarXY.getYValue()*refItem.getYValue()/(valueE*valueE);
                scale_denominator += tarXY.getYValue()*tarXY.getYValue()/(valueE*valueE);
            } else { // interpolate

                if ( (xValue > targetSeries.getX(1).doubleValue()) || (xValue < targetSeries.getX(targetSeries.getItemCount()-2).doubleValue()) ){
                    Double[] results =  Functions.interpolateOriginal(targetSeries, targetError, xValue);
                    //target.add(xValue, results[1]);
                    tempvalues.add(refItem.getX(), results[1]);
                    scale_numerator += results[1]*refItem.getYValue()/(results[2]*results[2]);
                    scale_denominator += results[1]*results[1]/(results[2]*results[2]);
                }
            }
        }

        scaleFactor = scale_numerator/scale_denominator;

        residuals = new XYSeries("residuals");
        sortedResiduals = new ArrayList<>();
        double diff, sum=0, sumqsquared=0, sumcubed=0;
        double counter=0;

        for (int q=startPt; q<=endPt; q++){
            refItem = reference.getDataItem(q);
            tarXY = tempvalues.getDataItem(tempvalues.indexOf(refItem.getX()));
            diff = (refItem.getYValue() - scaleFactor*tarXY.getYValue());
            residuals.add(refItem.getX(), diff);
            sortedResiduals.add(diff);
            sum += diff;
            sumqsquared += diff*diff;
            sumcubed += diff*diff*diff;
            counter += 1.0;
        }

        average = sum/counter;
        variance = sumqsquared/counter - average*average;
        invGaussianNormalizationFactor = 1.0/Math.sqrt(2.0*Math.PI*variance);
        inv2variance = 1.0/(2.0*variance);
        skewness = (sumcubed/counter - 3*average*variance - average*average*average)/(variance*Math.sqrt(variance));

        totalResiduals = residuals.getItemCount();

        Collections.sort(sortedResiduals);
    }

    // ratio of two random processes should fit a distribution (not Gaussian)
    public XYSeries getResiduals() {
        return residuals;
    }

    /**
     * Location is the average or mean of the distribution
     * @return
     */
    public double getLocation(){return average;}

    /**
     * scale is the variance of the distribution (square of sigma)
     * @return
     */
    public double getScale(){return variance;}

    // calculate statistics of the ratio
    // no signal, should be flat line (randomly distributed)

    /**
     * d = 2 means no autocorrelation
     * d-value should always lie between 0 and 4
     */
    private void calculateDurbinWatson(){

        double numerator=0, value, diff;
        double denominator = residuals.getY(0).doubleValue()*residuals.getY(0).doubleValue();

        for(int i=1; i<totalResiduals; i++){
          value = residuals.getY(i).doubleValue();
          diff = value - residuals.getY(i-1).doubleValue();
            numerator += diff*diff;
            denominator += value*value;
        }

        durbinWatsonStatistic = numerator/denominator;
    }


    private void calculateLjungBoxTest(){

        double sum = 0, temp;
        double invGammeAtZero = 1.0/calculateAutoCorrelation(0);

        for(int k=1; k<lags; k++){
            temp = calculateAutoCorrelation(k)*invGammeAtZero;
            sum += temp*temp/(double)(totalResiduals-k);
        }

        ljungBoxStatistic = totalResiduals*(totalResiduals+2)*sum;
    }


    /**
     * Sample autoCovariance, if lag is 0, this is variance
     * @param lag
     * @return
     */
    private double calculateAutoCorrelation(int lag){
        double gammaSum =0;
        int limit = totalResiduals - lag;
        for(int t=0; t<limit; t++){
            gammaSum += (residuals.getY(t+lag).doubleValue()-average)*(residuals.getY(t).doubleValue() - average);
        }

        return gammaSum*1.0/(double)totalResiduals;
    }

    public void printTests(String text){
        System.out.println(text + " : DW " + durbinWatsonStatistic + " LB " + ljungBoxStatistic + " SW " + shapiroWilkStatistic + " " + average);
    }


    // Assume residuals are Gaussian, use Kolmogorov–Smirnov to test for normality
    private double calculateKolmogorovSmirnovTest(){

        return 0;
    }



    private void estimateShapiroWilksTest(int rounds){

        ArrayList<Double> estimates = new ArrayList<>(rounds);

        for(int i=0; i<rounds; i++){
            estimates.add(calculateShapiroWilksTest());
        }

        Collections.sort(estimates);

        shapiroWilkStatistic = estimates.get((rounds-1)/2);
    }


    private double calculateShapiroWilksTest(){
        // divide into 11 bins, sample 5 from each bin and calculate
        double lqmin = residuals.getMinX();
        double lqmax = residuals.getMaxX();

        int bins = 11;
        int[] perBin = {4,6,6,5,4,4,4,4,4,4,4}; // odd number of elements => 49

        double increment = (lqmax-lqmin)/(double)bins;

        ArrayList<Double> selectFrom = new ArrayList<>();
        ArrayList<Double> keptResiduals = new ArrayList<>(49);
        int start=0;
        double qvalue;
        double lowerq = lqmin, upperq = lowerq+increment;

        for(int i=0; i<bins; i++){

            for(int j=start; j<totalResiduals; j++){
                qvalue = residuals.getX(j).doubleValue();
                if(qvalue >= lowerq && qvalue < upperq){
                    selectFrom.add(residuals.getY(j).doubleValue());
                } else {
                    start = j;
                    break;
                }
            }

            lowerq = upperq;
            upperq += increment;
            // randomly grab
            Collections.shuffle(selectFrom, new Random(System.nanoTime()));
            for (int m=0; m<perBin[i]; m++){
                keptResiduals.add(selectFrom.get(m).doubleValue());
            }
            selectFrom.clear();
        }

        // throw excepction if keptResiduals is not 49 in lenght
        int totalInKept = keptResiduals.size();
        Collections.sort(keptResiduals);

        double ss=0, temp;
        for(int s=0; s<totalInKept; s++){
            temp = keptResiduals.get(s) - average;
            ss+= temp*temp;
        }

        // calculate distance between extremes
        int limit = (totalInKept-1)/2;
        double b_factor=0;
        for(int s=0; s < limit; s++){
            b_factor += shapiroWilkTable[s]*(keptResiduals.get(totalInKept-1-s) - keptResiduals.get(s));
        }

        return b_factor*b_factor/ss;
    }


    private void createBinnedData(){
        // binning
        ArrayList<Double> bins = new ArrayList<>();
        ArrayList<Integer> binned = new ArrayList<>();

        // Scott's Normal Reference Rule
        //final double binWidth = 3.5*Math.sqrt(variance)/Math.pow(totalResiduals,1.0/3.0d);
        final double binWidth = 0.25*Math.sqrt(variance);

        // move left for 10 bins
        final int startIndex = 17;
        binnedData = new XYSeries("Binned Data");
        modelData = new XYSeries("Modeled Data");
        double midpoint;
        int counted = 1;
        for(int binIndex=0; binIndex < startIndex; binIndex++){
            double lower = this.average - (startIndex-binIndex)*binWidth;
            double upper = lower + binWidth;

            int count=0;
            for(int j_index=0; j_index<totalResiduals; j_index++){
                double value = sortedResiduals.get(j_index);

                if (binIndex == 0 && (value < upper)){
                    count++;
                } else if (value >= lower && (value < upper)){
                    count++;
                }
            }
            bins.add(lower);
            binned.add(count);
            binnedData.add(counted, count);

            midpoint = lower+0.5*binWidth;
            modelData.add(midpoint, calculateModel(midpoint));
            counted++;
        }


        for(int binIndex=0; binIndex < startIndex; binIndex++){
            double lower = this.average + binWidth*binIndex;
            double upper = lower + binWidth;

            int count=0;
            for(int j_index=0; j_index<totalResiduals; j_index++){
                double value = sortedResiduals.get(j_index);

                if (binIndex == (startIndex-1) && (value > lower)){
                    count++;
                } else if (value >= lower && (value < upper)){
                    count++;
                }
            }
            bins.add(lower);
            binned.add(count);
            binnedData.add(counted, count);
            midpoint = lower+0.5*binWidth;
            modelData.add(midpoint,calculateModel(midpoint));
            counted++;
        }

        double invln2 = 1.0/Math.log(2);
        double sigmaG = Math.sqrt(6.0*(totalResiduals-2.0)/((totalResiduals+1.0)*(totalResiduals+3.0)));
        int doanes = (int)(1 + Math.log(totalResiduals)*invln2 + Math.log(1 + Math.abs(skewness)/sigmaG)*invln2);

        double[] histoDataArray = new double[totalResiduals];
        for(int j_index=0; j_index<totalResiduals; j_index++){
            histoDataArray[j_index] = sortedResiduals.get(j_index);
        }

        histogram = new HistogramDataset();
        histogram.setType(HistogramType.SCALE_AREA_TO_1);
        histogram.addSeries("Gauss", histoDataArray, doanes);

        // remove first and last points form binnedData
        // these bins will have too many counts since I put all the extremes in one block
        binnedData.remove(0);
        binnedData.remove(binnedData.getItemCount()-1);
    }

    public XYSeries getBinnedData(){
        return binnedData;
    }

    private double calculateModel(double value){
        double diff = value - average;
        return invGaussianNormalizationFactor*Math.exp(-(diff*diff)*inv2variance);
    }

    public XYSeries getModelData() {
        return modelData;
    }

    public HistogramDataset getHistogram(){ return histogram;}
}
