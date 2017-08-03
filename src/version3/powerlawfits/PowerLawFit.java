package version3.powerlawfits;


import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by xos81802 on 31/07/2017.
 */
public class PowerLawFit {

    private XYSeries data;
    private XYSeries error;
    private XYSeries finalDataSetForFit;
    private double slope, intercept, errorSlope, errorIntercept;
    private int startAt, endAt;
    private ArrayList<Integer> indices;
    private int totalToFit;
    private int totalSubsetToFit;
    private int rounds, midPointForAverage;
    private boolean locale = true;
    private String outputLine;


    public PowerLawFit(XYSeries data, XYSeries error, double qmin, double qmax, int rounds, int totalToFitPerRound){
        // create powerlaw series of the data
        this.data = new XYSeries("data");
        this.error = new XYSeries("error");
        this.rounds = rounds;
        this.totalSubsetToFit = totalToFitPerRound;

        int totalInData = data.getItemCount();
        startAt = 0;
        endAt = totalInData-1;

        for(int i=0; i<totalInData; i++){
            if(data.getX(i).doubleValue() >= qmin){
                startAt = i;
                break;
            }
        }

        for(int i=endAt; i>startAt; i--){
            if (data.getX(i).doubleValue() <= qmax){
                endAt = i;
                break;
            }
        }

        // fit the data
        for(int i=startAt; i<=endAt; i++){
            XYDataItem temp = data.getDataItem(i);
            if (temp.getYValue() > 0){ // only take positive numbers
                this.data.add(Math.log(temp.getXValue()), Math.log(temp.getYValue()));
                this.error.add(error.getDataItem(i));
            }
        }

        totalToFit = this.data.getItemCount();
        // create array of integers that encapsulate the elements in data
        indices = new ArrayList<>();
        for(int i=0; i<totalToFit; i++){
            indices.add(i);
        }

        this.setMedian();
        //this.fitData();
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public void fitData(){

        double sumX, sumY, sumXxY, sumXsq, sumYsq;
        XYDataItem tempItem;
        double tempValue, tempSlope, tempIntercept, invN = 1.0/(double)totalSubsetToFit;
        double tempMedianResidual, lowestMedianResidual = 10000.0;

        for(int round=0; round<rounds; round++){

            // randomly grab three points
            int count=0;
            for(int j=0; j<totalSubsetToFit; j++){ // randomize indices up to totalSubsetToFit
                int swapping = ThreadLocalRandom.current().nextInt(count, totalToFit);
                Collections.swap(indices, count, swapping);
                count++;
            }

            sumX = 0.0;
            sumY = 0.0;
            sumXxY = 0.0;
            sumXsq = 0.0;

            // calculate line
            for (int i=0; i < totalSubsetToFit; i++) {
                tempItem = data.getDataItem(indices.get(i));
                sumXsq += (tempItem.getXValue() * tempItem.getXValue());
                sumXxY += (tempItem.getXValue() * tempItem.getYValue());
                sumX += tempItem.getXValue();
                sumY += tempItem.getYValue();
            }

            double sumX2 = sumX*sumX;
            tempSlope = ((totalSubsetToFit*sumXxY)-(sumX*sumY))/((totalSubsetToFit*sumXsq)- sumX2);
            tempIntercept = ((sumY*invN)-((tempSlope*sumX)*invN));

            // calculate residuals
            ArrayList<Double> tempResiduals = new ArrayList<>(totalToFit);
            for (int i=0; i < totalToFit; i++){
                tempItem = data.getDataItem(i);
                tempValue = tempItem.getYValue() - (tempSlope*tempItem.getXValue() + tempIntercept);
                tempResiduals.add( tempValue*tempValue );
            }

            Collections.sort(tempResiduals);
            tempMedianResidual = this.getMedian(tempResiduals);
            if(tempMedianResidual < lowestMedianResidual){ // minimize median squared residual
                lowestMedianResidual=tempMedianResidual;
                slope = tempSlope;
                intercept = tempIntercept;
            }
        }

        // calculate residuals

        double s_o = 1.4826 * (1.0 + 5.0 / (totalToFit - 2.0 - 1.0)) * Math.sqrt(lowestMedianResidual);
        double inv_s_o = 1.0/s_o;
        // create final dataset for final fit
        int count = 0;
        ArrayList<Integer> keepers = new ArrayList<Integer>();

        XYDataItem dataItem;
        for (int i = 0; i < totalToFit; i++) { // get the indices of the data items to keep
            dataItem = data.getDataItem(i);
            //if (Math.abs((dataItem.getYValue() - (keptSlope * dataItem.getXValue() + keptIntercept))) * inv_sigma < 2.5) {
            if (Math.abs((dataItem.getYValue() - (slope * dataItem.getXValue() + intercept))) * inv_s_o < 2.5) {
                // decide which ln[I(q)] values to keep
                keepers.add(i);
                count++;
            }
        }

        // determines values to keep for fitting to determine Rg and I(zero)
        finalDataSetForFit = new XYSeries("FinalDataset");
        sumX=0;
        sumY=0;
        sumXsq=0;
        sumYsq=0;
        sumXxY=0;

        for (int i = 0; i < count; i++) {
            dataItem=data.getDataItem(keepers.get(i));
            finalDataSetForFit.add(dataItem);
            sumX += dataItem.getXValue();
            sumY += dataItem.getYValue();
            sumXsq += dataItem.getXValue()*dataItem.getXValue();
            sumYsq += dataItem.getYValue()*dataItem.getYValue();
            sumXxY += dataItem.getXValue()*dataItem.getYValue();
        }

        double dcount = (double)count;
        invN = 1.0/dcount;
        double sumX2 = sumX*sumX;
        slope = ((count*sumXxY)-(sumX*sumY))/((count*sumXsq)- sumX2);
        intercept = ((sumY*invN)-((slope*sumX)*invN));

        double s_e_2 = 1.0/(dcount*(dcount-2))*(dcount*sumYsq - Math.pow(sumY,2) - Math.pow(slope,2)*(dcount*sumXsq - sumX2));
        double s_m_2 = dcount*s_e_2/(dcount*sumXsq- sumX2);
        double s_b_2 =  s_m_2/dcount*sumXsq;

        errorSlope = Math.sqrt(s_m_2);
        errorIntercept = Math.sqrt(s_b_2);
    }


    private void setMedian(){
         if ( (totalToFit & 1) == 0 ) { // even
            midPointForAverage = totalToFit/2;
            locale = false;
         } else {
             midPointForAverage = (totalToFit-1)/2;
         }
    }


    private Double getMedian(ArrayList<Double> sortedValues){
        if (locale){
            return sortedValues.get(midPointForAverage);
        } else {
            return 0.5*( sortedValues.get(midPointForAverage) + sortedValues.get(midPointForAverage-1) );
        }
    }

    public double getErrorSlope() {
        return errorSlope;
    }

    public double getErrorIntercept() {
        return errorIntercept;
    }

    public String getOutputLine() {
        return outputLine;
    }

    public void setOutputLine(String line){
        outputLine = line;
    }
}
