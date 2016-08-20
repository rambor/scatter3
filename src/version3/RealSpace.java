package version3;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import net.jafama.FastMath;

import java.awt.*;
import java.util.*;

import static org.apache.commons.math3.stat.StatUtils.mean;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class RealSpace {

    private String filename;
    private boolean selected;
    private int id;
    private int startAt;
    private int stopAt;
    private int totalMooreCoefficients;
    private XYSeries allData;       // use for actual fit and setting spinners
    private XYSeries errorAllData;  // use for actual fit and setting spinners

    private XYSeries originalqIq;         // range of data used for the actual fit, may contain negative values
    private XYSeries fittedqIq;           // range of data used for the actual fit, may contain negative values
    private XYSeries fittedIq;           // range of data used for the actual fit, may contain negative values
    private XYSeries fittedError;        //

    private XYSeries refinedIq;           // range of data used for the actual fit, may contain negative values
    private XYSeries refinedError;        //

    private XYSeries calcIq;          // log of calculate I(q), matches logData
    private XYSeries calcqIq;         // log of calculate I(q), matches logData
    private XYSeries logData;         // only for plotting
    private XYSeries prDistribution;
    //private XYSeries refinedPrDistribution;

    private double analysisToPrScaleFactor;
    private double izero;
    private double rg;
    private double[] mooreCoefficients;

    private ArrayList<Double> rValuesDirectFT;
    private ArrayList<Double> directFT;

    private int dmax;
    private double qmax;
    private double raverage;
    private double chi2;
    private float scale=1.0f;
    private Color color;
    private boolean baseShapeFilled;
    private int pointSize;
    private BasicStroke stroke;
    private double kurtosis = 0;
    private double l1_norm = 0;
    private double kurt_l1_sum;
    private Dataset dataset;
    private int lowerQIndexLimit=0;
    private final int maxCount;
    private final static double invPi = 1.0/Math.PI;
    private boolean negativeValuesInModel=false;
    private double standardizationMean;
    private double standardizationStDev;

    // rescale the data when loading analysisModel

    public RealSpace(Dataset dataset){
        this.dataset = dataset;
        this.filename = dataset.getFileName();
        this.id = dataset.getId();
        this.dmax = (int)dataset.getDmax();
        selected = true;
        analysisToPrScaleFactor = 1;
        int totalAllData = dataset.getAllData().getItemCount();

        //allData = new XYSeries("rescale data");
        try {
            allData = dataset.getAllData().createCopy(0, totalAllData-1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            errorAllData = dataset.getAllDataError().createCopy(0, totalAllData-1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // need to rescale allData for fitting?
        double digits = Math.log10(averageIntensity(allData));

        originalqIq = new XYSeries("orignal qIq data");

        for(int i=0; i<totalAllData; i++){
            XYDataItem tempItem = allData.getDataItem(i);
            originalqIq.add(tempItem.getX(), tempItem.getYValue()*tempItem.getXValue());
        }

        //System.out.println("Average I " + averageIntensity(allData) + " digits " + digits);

        fittedIq = new XYSeries(Integer.toString(this.id) + " Iq-" + filename);
        fittedqIq = new XYSeries(Integer.toString(this.id) + " qIq-" + filename);
        fittedError = new XYSeries(Integer.toString(this.id) + " fittedError-" + filename);

        logData = new XYSeries(Integer.toString(this.id) + " log-" + filename);
        calcIq = new XYSeries(Integer.toString(this.id) + " calc-" + filename);
        calcqIq = new XYSeries(Integer.toString(this.id) + " calc-" + filename);

        refinedIq = new XYSeries(Integer.toString(this.id) + " refined-" + filename);
        refinedError = new XYSeries(Integer.toString(this.id) + " refined-" + filename);

        //originalqIq = new XYSeries("qIq relaspace");
        //for (int i=0; i<dataset.getQIQData().getItemCount(); i++){
        //    XYDataItem tempqiq = dataset.getQIQData().getDataItem(i);
        //    originalqIq.add(tempqiq.getX(), tempqiq.getYValue()/10000);
        //}


        prDistribution = new XYSeries(Integer.toString(this.id) + " Pr-" + filename);
        this.color = dataset.getColor();

        pointSize = dataset.getPointSize();
        stroke = dataset.getStroke();
        scale = 1.0f;
        baseShapeFilled = false;
        //for spinners
        //this.startAt = dataset.getStart(); // spinner value in reference to non-negative data
        int startAll = dataset.getStart() - 1;
        //int startAll = dataset.getAllData().indexOf(dataset.getData().getMinX());
        int stopAll = dataset.getAllData().indexOf(dataset.getData().getMaxX());

        double xValue;
        double yValue;

        // transform all the data in allData
        //check for negative values at start
        double qlimit = 0.05;

        XYDataItem temp;
        int ii=startAll, lastNegative=-1;
        temp = allData.getDataItem(ii);
        while (temp.getXValue() < qlimit && ii < stopAll){
            if (temp.getYValue() < 0){
                lastNegative = ii;
            }
            ii++;
            temp = allData.getDataItem(ii);
        }

        lowerQIndexLimit = (lastNegative < 0) ? startAll : lastNegative+1;

        for(int i=lowerQIndexLimit; i < totalAllData; i++){
            temp = allData.getDataItem(i);
            xValue = temp.getXValue();
            yValue = temp.getYValue();

            if (temp.getYValue() > 0){
                logData.add(xValue, Math.log10(yValue)); // plotted data
            }

            fittedIq.add(temp);
            fittedError.add(errorAllData.getDataItem(i));
            fittedqIq.add(originalqIq.getDataItem(i));   // fitted data, spinners will add and remove from this XYSeries
        }

        lowerQIndexLimit++; // increment to set to value for spinner, fixed for the real-space model
        // spinners in real-space Tab are in reference to allData
        startAt=lowerQIndexLimit;
        stopAt=originalqIq.getItemCount();
        maxCount = originalqIq.getItemCount();
    }

    /**
     * max value of original dataset
     * @return
     */
    public int getMaxCount(){ return maxCount;}

    public double getGuinierRg(){
        return dataset.getGuinierRg();
    }

    public double getGuinierIzero(){
        return dataset.getGuinierIzero();
    }

    public int getLowerQIndexLimit(){ return lowerQIndexLimit;}


    public boolean isPDB(){
        return this.dataset.getIsPDB();
    }

    /**
     * shoudl match spinner index
     * @param i
     */
    public void setStart(int i){
        startAt = i;
    }

    /**
     * set start value of the spinner
     * @return
     */
    public int getStart(){
        return startAt;
    }

    public void setStop(int i){
        stopAt = i;
    }

    public int getStop(){
        return stopAt;
    }

    public int getDmax(){
        return dmax;
    }

    public void setDmax(int d){
        this.dataset.setDmax(d);
        dmax = d;
    }

    public int getId(){
        return dataset.getId();
    }

    public double getIzero(){
        return izero;
    }

    public void setIzero(double j){
        izero = j;
    }

    public double getRg(){
        return rg;
    }

    public void setRg(double j){
        rg = j;
    }

    public double getRaverage(){
        return raverage;
    }

    public void setRaverage(float j){
        raverage = j;
    }

    public double getChi2(){
        return chi2;
    }


    public void setChi2(float j){
        chi2 = j;
        this.kurtosis = Math.abs(this.max_kurtosis_shannon_sampled(0));
        this.l1_norm = this.l1_norm_pddr(11);
        this.kurt_l1_sum = 0.1*this.kurtosis + 0.9*this.l1_norm;
        // System.out.println("CHI2 " + j + " " + this.kurtosis + " L1 " + this.l1_norm);
    }

    public float getScale(){
        return scale;
    }

    public void setScale(float j){
        float oldscale = (float) (1.0/this.scale);
        this.scale = j;
        /*
         * scale Pr distribution
         */
        int sizeof = this.prDistribution.getItemCount();
        XYDataItem tempData;
        for(int i=0; i<sizeof; i++){
            tempData = this.getPrDistribution().getDataItem(i);
            this.prDistribution.updateByIndex(i, tempData.getYValue()*oldscale*this.scale);
        }
    }

    public void setPointSize(int size){
        pointSize = size;
    }

    public Color getColor(){
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public int getPointSize(){
        return pointSize;
    }

    public void setStroke(float size){
        stroke = new BasicStroke(size);
    }

    public BasicStroke getStroke(){
        return stroke;
    }

    public void setBaseShapeFilled(boolean what){
        baseShapeFilled = what;
    }

    public boolean getBaseShapeFilled(){
        return baseShapeFilled;
    }

    public void setSelected(boolean what){
        selected = what;
    }

    public void setCalcIq(XYSeries data){
        this.calcIq = data;
    }

    public void setPrDistribution(XYSeries data){
        int totalToSet = data.getItemCount();
        for (int i=0; i<totalToSet; i++){
            prDistribution.add(data.getDataItem(i));
        }
    }

    public boolean getSelected(){
        return selected;
    }

    public String getFilename(){
        return filename;
    }

    public XYSeries getAllData(){
        return allData;
    }

    public XYSeries getCalcIq(){
        return calcIq;
    }


    public XYSeries getErrorAllData(){
        return errorAllData;
    }

    /**
     * return range of data used for fitting, includes negative Intensities
     * @return
     */
    public XYSeries getfittedIq(){

        try {
            fittedIq = allData.createCopy(startAt-1, stopAt-1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return fittedIq;
    }

    /**
     * fittedqIq is actively managed by spinners, so should be always up-to-date
     * @return
     */
    public XYSeries getfittedqIq(){
        return fittedqIq;
    }

    public XYSeries getfittedError(){
        try {
            fittedError = errorAllData.createCopy(startAt-1, stopAt-1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return fittedError;
    }

    public XYSeries getLogData(){
        return logData;
    }

    public XYSeries getPrDistribution(){
        return prDistribution;
    }

    public double getAnalysisToPrScaleFactor(){
        return analysisToPrScaleFactor;
    }

    public double getKurt_l1_sum() {
        return kurt_l1_sum;
    }

    public double[] getMooreCoefficients(){
        return mooreCoefficients;
    }

    public void setPrDistributionDirectFT(double[] rvalues, double[] fittedValues, double[] modelqIq){
        directFT = new ArrayList<>();
        rValuesDirectFT = new ArrayList<>();

        int totalR = rvalues.length;
        int totalFitted = fittedValues.length;

        for(int i=0; i< totalR; i++){
            rValuesDirectFT.add(rvalues[i]);
        }

        for(int i=0; i< totalFitted; i++){
            directFT.add(scale*fittedValues[i]);
        }

        prDistribution.clear();
        prDistribution.add(0,0);
        for (int j=1; j < totalFitted; j++){
            prDistribution.add( rValuesDirectFT.get(j-1), directFT.get(j) );
        }
        prDistribution.add(dmax,0);


        calcqIq.clear();
        //iterate over each q value and calculate I(q)
        int startHere = startAt - 1;
        int count=0;
        for (int j=startHere; j < stopAt; j++){
            calcqIq.add(allData.getX(j), modelqIq[count]);
            count++;
        }

    }

    public void setMooreCoefficients(double[] values){
        totalMooreCoefficients = values.length;
        this.mooreCoefficients = new double[totalMooreCoefficients];
        System.arraycopy(values, 0, this.mooreCoefficients, 0, totalMooreCoefficients);
        negativeValuesInModel = false;

        saxs_invariants();

        // calculate P(r) distribution
        this.calculatePofR();
        this.qmax = this.fittedqIq.getMaxX();

        try {
            // calculate chi over all the data bounded by Spinners
            this.chi_estimate(allData.createCopy(startAt-1, stopAt-1), errorAllData.createCopy(startAt-1, stopAt-1));
            //this.kurt_l1_sum = l1_norm_pddr(1) + max_kurtosis_shannon_sampled(0);
            this.kurtosis = Math.abs(this.max_kurtosis_shannon_sampled(7));
            this.l1_norm = this.l1_norm_pddr(3);

            System.out.println("SETTING MOORE COEFFICIENTS " + totalMooreCoefficients + " L1 : " + this.l1_norm + " K :" + this.kurtosis);
            this.kurt_l1_sum = 0.1*this.kurtosis + 1000*this.l1_norm;
            //this.kurt_l1_sum = 0.9*this.l1_norm;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void calculatePofRDirect(){
        /*
         * create P(r) plot
         */
        prDistribution.clear();
        double totalPrPoints = (Math.ceil(fittedqIq.getMaxX()*dmax*invPi)*3);
        int r_limit = (int)totalPrPoints -1;
        double deltar = dmax/totalPrPoints;

        double resultM;
        double inv_d = 1.0/dmax;
        double pi_dmax = Math.PI*inv_d;
        double inv_2d = 0.5*inv_d;
        double pi_dmax_r;
        double r_value;
        prDistribution.add(0.0d, 0.0d);
        negativeValuesInModel = false;

        for (int j=1; j < r_limit; j++){

            r_value = j*deltar;
            pi_dmax_r = pi_dmax*r_value;
            resultM = 0;

            for(int i=1; i < totalMooreCoefficients; i++){
                resultM += mooreCoefficients[i]*FastMath.sin(pi_dmax_r*i);
            }

            prDistribution.add(r_value, inv_2d * r_value * resultM*scale);

            if (resultM < 0){
                negativeValuesInModel = true;
            }
        }

        prDistribution.add(dmax,0);
    }


    public void calculatePofR(){
        /*
         * create P(r) plot
         */
        prDistribution.clear();
        double totalPrPoints = (Math.ceil(fittedqIq.getMaxX()*dmax*invPi)*3);
        int r_limit = (int)totalPrPoints -1;
        double deltar = dmax/totalPrPoints;

        double resultM;
        double inv_d = 1.0/dmax;
        double pi_dmax = Math.PI*inv_d;
        double inv_2d = 0.5*inv_d;
        double pi_dmax_r;
        double r_value;
        prDistribution.add(0.0d, 0.0d);
        negativeValuesInModel = false;

        for (int j=1; j < r_limit; j++){

            r_value = j*deltar;
            pi_dmax_r = pi_dmax*r_value;
            resultM = 0;

            for(int i=1; i < totalMooreCoefficients; i++){
                resultM += mooreCoefficients[i]*FastMath.sin(pi_dmax_r*i);
            }

            prDistribution.add(r_value, inv_2d * r_value * resultM*scale);

            if (resultM < 0){
                negativeValuesInModel = true;
            }
        }

        prDistribution.add(dmax,0);
    }

    public double calculatePofRAtR(double r_value){
        double inv_d = 1.0/dmax;
        double pi_dmax = Math.PI*inv_d;
        double pi_dmax_r = pi_dmax*r_value;
        double resultM = 0;

        for(int i=1; i < totalMooreCoefficients; i++){
            resultM += mooreCoefficients[i]*FastMath.sin(pi_dmax_r*i);
        }
        return 0.5*inv_d * r_value * resultM*scale;
    }


    /**
     *
     * @param isqIq
     */
    public void calculateIntensityFromModel(boolean isqIq){

        if (!this.isPDB()){
            if (isqIq) {
                this.calculateQIQ();
            } else {
                this.calculateIofQ();
            }
        }
    }

    /**
     * calculate QIQ for plotting, includes negative values
     */
    public void calculateQIQ(){
        calcqIq.clear();

        XYDataItem temp;
        //iterate over each q value and calculate I(q)
        int startHere = startAt -1;
        if (this.isPDB()){

        } else {
            for (int j=startHere; j<stopAt; j++){
                temp = allData.getDataItem(j); // allData gives q-value
                calcqIq.add(temp.getX(), moore_qIq(temp.getXValue()));
            }
        }
    }

    /**
     *
     * @return calcqIq XYSeries
     */
    public XYSeries getCalcqIq(){
        return calcqIq;
    }

    /**
     * @return XYSeries of log10 Intensities
     */
    public void calculateIofQ(){
        calcIq.clear();
        double iofq;
        XYDataItem temp;
        //iterate over each q value and calculate I(q)
        int startHere = startAt -1;

        if (this.isPDB()){

        } else {

            for (int j = startHere; j < stopAt; j++) {
                temp = allData.getDataItem(j);
                iofq = moore_Iq(temp.getXValue());

                if (iofq > 0) {
                    calcIq.add(temp.getXValue(), FastMath.log10(iofq));
                }
            }
        }
    }

    /**
     * Calculates intensities from moore coefficients
     * @param q single scattering vector point
     * @return Intensity I(q)
     */
    public double moore_Iq(double q){
        double invq = 1.0/q;

        double dmaxPi = dmax*Math.PI;
        double dmaxq = dmax*q;

        double resultM = mooreCoefficients[0];
        //double resultM = 0;
        //double twodivpi = 2.0/Math.PI;

        for(int i=1; i < totalMooreCoefficients; i++){
            resultM = resultM + Constants.TWO_DIV_PI*mooreCoefficients[i]*dmaxPi*i*FastMath.pow(-1,i+1)*FastMath.sin(dmaxq)/(Constants.PI_2*i*i - dmaxq*dmaxq);
        }

        //data.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
        //return (resultM*invq + mooreCoefficients[0])*standardizationStDev + standardizationMean;
        return (resultM*standardizationStDev + standardizationMean)*invq;
    }


    /**
     * Calculates intensities from moore coefficients
     * @param q single scattering vector point
     * @return Intensity q*I(q)
     */
    public double moore_qIq(double q){

        double dmaxPi = dmax*Math.PI;
        double dmaxq = dmax*q;

        double resultM = mooreCoefficients[0];

        for(int i=1; i < totalMooreCoefficients; i++){
            //resultM = resultM + Constants.TWO_DIV_PI*mooreCoefficients[i]*dmaxPi*i*Math.pow(-1,i+1)*Math.sin(dmaxq)/(Constants.PI_2*i*i - dmaxq*dmaxq);
            resultM = resultM + Constants.TWO_DIV_PI*mooreCoefficients[i]*dmaxPi*i*FastMath.pow(-1,i+1)*FastMath.sin(dmaxq)/(Constants.PI_2*i*i - dmaxq*dmaxq);
        }

        return resultM*standardizationStDev + standardizationMean;
    }


    /**
     * calculate 2nd derivative at r-values determined by ShannonNumber + 1
     * @param multiple
     * @return
     */
    public double l1_norm_pddr(int multiple){
        double inv_d = 1.0/this.dmax;
        double r_value, pi_r_n_inv_d, pi_r_inv_d, cos_pi_r_n_inv_d, sin_pi_r_n_inv_d;
        double pi_inv_d = Math.PI*inv_d;
        double inv_2d = inv_d*0.5;
        double a_i;

        //int r_limit = r_vector.length;

        int coeffs_size = this.mooreCoefficients.length;

        double a_i_sum, product, l1_norm = 0.0;

        double shannon = Math.ceil(multiple*1.0/Math.PI*this.dmax*this.fittedqIq.getMaxX());
        double delta_r = this.dmax/shannon;
        double[] r_vector = new double[(int)shannon + 1];

        int k=0;
        while (k*delta_r < this.dmax){
            r_vector[k] = k*delta_r;
            k += 1;
        }
        r_vector[(int)shannon] = this.dmax;

        int r_limit = (int)shannon + 1;

        for (int r=0; r < r_limit; r++){
            r_value = r_vector[r];
            pi_r_inv_d = pi_inv_d*r_value;
            a_i_sum = 0;

            for(int n=1; n < coeffs_size; n++){
                //a_i = am.get(n,0);
                a_i = this.mooreCoefficients[n];
                pi_r_n_inv_d = pi_r_inv_d*n;
                cos_pi_r_n_inv_d = inv_d*n*Math.cos(pi_r_n_inv_d);
                sin_pi_r_n_inv_d = Math.sin(pi_r_n_inv_d);
                product = pi_r_inv_d*inv_2d*n*n*sin_pi_r_n_inv_d;
                a_i_sum += a_i*cos_pi_r_n_inv_d + a_i*product;
            }
            l1_norm += Math.abs(a_i_sum);
        }
        //System.out.println("l1_norm pddr " + l1_norm);
        return l1_norm/shannon;
    }

    public double max_kurtosis_shannon_sampled(int rounds){
        /*
         * Divide scattering curve into Shannon bins
         * Determine kurtosis from a random sample of the ratio of I_calc to I_obs based on the binning
         * Take max of the kurtosis set
         */
        //Random newRandom = new Random();
        int total = this.fittedqIq.getItemCount(); // fittedqIq is scaled for fitting

        int limit = this.getMooreCoefficients().length;
        double[] ratio = new double[total];
        double squared = 0;
        double sumMean = 0;
        ArrayList<Double> test_ratios = new ArrayList<Double>();

        //System.out.println("KURTOSIS");
        double tempVar;
        for (int i=0; i<total; i++){
            XYDataItem values = this.fittedqIq.getDataItem(i);  // unscale data
            // ratio of calc/obs
            tempVar = Functions.moore_Iq(this.getMooreCoefficients(), this.dmax, values.getXValue(), limit, standardizationMean, standardizationStDev) - values.getYValue()/values.getXValue();
            ratio[i] = tempVar;
            sumMean += tempVar;
            squared += tempVar*tempVar;
        }

        double[] centered = new double[total];

        double invtotal = 1.0/(double)total;
        double standardizedMean = sumMean*invtotal;
        double stdev = Math.sqrt(squared*invtotal - standardizedMean*standardizedMean);
        double invstdev = 1.0/stdev;
        // center the residuals

        for (int i=0; i<total; i++){
            centered[i] = (ratio[i] - standardizedMean)*invstdev;
        }

        // compute covariance matrix
        // calculate variance-covariance matrix
        // sum of eigen values restricted to Shannon Number





//        double tempKurtosises = StatMethods.kurtosis(test_ratios);

        /*
         * bin the ratio
         * qmax*dmax/PI
         *
         */
        //double qmin = this.fittedqIq.getMinX();
        //double qmax = this.fittedqIq.getMaxX();

        //int bins = (int)(Math.round(qmax*dmax/Math.PI)), locale;
        //ArrayList<Double> kurtosises = new ArrayList<Double>();
        double[] kurtosises = new double[rounds];
        // calculate kurtosis
        //double kurtosis = StatMethods.kurtosis(test_ratios);
        //double kurtosis = StatMethods.prunedKurtosis(test_ratios);
        double qmin = this.fittedqIq.getMinX();
        double bins = this.mooreCoefficients.length*3.0;
        double delta_q = (this.fittedqIq.getMaxX()-qmin)/bins;

        double samplingLimit, lowerLimit;
        Random randomGenerator = new Random();
        int[] randomNumbers;


        for (int i=0; i<rounds; i++){
            // for each round, get a random set of values from ratio
            int startbb = 0, upperbb = 0;
            test_ratios.clear();

            for (int b=1; b < bins; b++) {
                // find upper q in bin
                // SAMPLE randomly per bin
                samplingLimit = (0.5 + randomGenerator.nextInt(12)) / 100.0;  // return a random percent up to 12%
                lowerLimit = (delta_q * b + qmin);

                binloop:
                for (int j = startbb; j < total; j++) {
                    if (this.fittedqIq.getX(j).doubleValue() >= lowerLimit) {
                        upperbb = j;
                        break binloop;
                    }
                }

                // grab indices inbetween startbb and upperbb
                randomNumbers = Functions.randomIntegersBounded(startbb, upperbb, samplingLimit);
                startbb = upperbb;

                for(int h=0; h < randomNumbers.length; h++){
                    test_ratios.add(ratio[randomNumbers[h]]);
                }
            }
            // calculate kurtosis
            kurtosises[i] = StatMethods.kurtosis(test_ratios);
            //System.out.println(i + " KURT : " + kurtosises[i]);
        }

        // double returnMe = Collections.max(kurtosises);
        Arrays.sort(kurtosises);
        return kurtosises[3];
        //return StatUtils.mean(kurtosises);
        //System.out.println("Sample Kurtosis " + tempKurtosises);
        //return tempKurtosises;
        //return Statistics.calculateMedian(kurtosises);
    }


    private void saxs_invariants(){
        /*
         * Calculate invariants
         */
        double i_zero = 0;
        double partial_rg = 0;
        double rsum = 0;

        double am;
        double pi_sq = 9.869604401089358;
        int sizeOf = mooreCoefficients.length;

        for (int i = 1; i < sizeOf; i++) {
                am = mooreCoefficients[i];
                i_zero = i_zero + am/(i)*Math.pow(-1,(i+1));
                partial_rg = partial_rg + am/Math.pow(i,3)*(Math.pow(Math.PI*i, 2) - 6)*Math.pow(-1, (i+1));
                rsum = rsum + am/Math.pow(i,3)*((Math.pow(Math.PI*i, 2) - 2)*Math.pow(-1, (i+1)) - 2 );
        }

        double dmax2 = dmax*dmax;
        double dmax3 = dmax2*dmax;
        double dmax4 = dmax2*dmax2;
        double inv_pi_cube = 1.0/(pi_sq*Math.PI);
        double inv_pi_fourth = inv_pi_cube/Math.PI;
        double twodivPi = 2.0/Math.PI;

        izero = standardizationStDev*(twodivPi*i_zero*dmax2/Math.PI + mooreCoefficients[0]);
        //rg = Math.sqrt(dmax4*inv_pi_cube/izero*partial_rg)*0.7071067811865475; // 1/Math.sqrt(2);
        double izero_temp = (twodivPi*i_zero*dmax2/Math.PI + mooreCoefficients[0]);
        rg = Math.sqrt(2*dmax4*inv_pi_fourth/izero_temp*partial_rg)*0.7071067811865475; // 1/Math.sqrt(2);
        //raverage = dmax3*inv_pi_cube/izero*rsum;
        raverage = 2*dmax3*inv_pi_fourth/izero_temp*rsum;
        this.dataset.setRealIzeroRgParameters(izero, 0.1*izero, rg, rg*0.1, raverage);
    }

    public void decrementLow(int spinnerValue){

        Number currentQ = fittedqIq.getX(0);

        int total = spinnerValue - this.startAt;

        if (total == 1){
            if (currentQ.equals(logData.getX(0))){
                fittedqIq.remove(0);
                logData.remove(0);
            } else {
                // find where in logData is currentQ
                int stopHere = logData.indexOf(currentQ);
                fittedqIq.remove(0);
                logData.delete(0, stopHere);
            }
        } else {

            // spinner value displays the first or last value plotted
            // if spinnervalue is 10, we are showing the value in array position 9
            int stopHere = logData.indexOf(allData.getX(spinnerValue-1));

            int totalminus = total-1;
            fittedqIq.delete(0,totalminus);
            logData.delete(0,stopHere - 1);
            //int totalminus = total-1;
            //fittedqIq.delete(0,totalminus);
            //logData.delete(0,stopHere);
        }
    }



    public void resetStartStop(){
        // start should be the low-q value
        // spinner in Analysis tab follows non-negative values
        // spinner in Pr follows originalqIq but limited by lowerQIndex

        int tempStart = originalqIq.indexOf(this.dataset.getData().getMinX()) + 1;
        System.out.println("index of plotted data mapped into original " + originalqIq.indexOf(this.dataset.getData().getMinX()));
        System.out.println("lowerqlimit " + lowerQIndexLimit);
        if (tempStart < lowerQIndexLimit){
            this.startAt = lowerQIndexLimit;
        } else {
            this.startAt = tempStart;
        }

        this.stopAt = originalqIq.indexOf(this.dataset.getData().getMaxX());

        logData.clear();
        fittedIq.clear();
        fittedqIq.clear();
        fittedError.clear();

        XYDataItem temp;
        for(int i=startAt-1; i < stopAt; i++){
            temp = allData.getDataItem(i);

            if (temp.getYValue() > 0){
                logData.add(temp.getXValue(), Math.log10(temp.getYValue()));
            }

            fittedIq.add(temp);
            fittedError.add(errorAllData.getDataItem(i));
            fittedqIq.add(originalqIq.getDataItem(i));   // fitted data, spinners will add and remove from this XYSeries
        }
    }


    public void incrementLow(int target){

        // add point to fittedqiq
        int diff = this.getStart() - target;

        if (diff == 1){
            XYDataItem tempXY = originalqIq.getDataItem(target-1);
            fittedqIq.add(tempXY);

            if (tempXY.getYValue() > 0){
                logData.add(tempXY.getX(), Math.log10(allData.getY(target-1).doubleValue()));
            }
        } else { // add more than one point
            int indexStart = this.getStart()-1;

            for (int i=0; i<diff && indexStart >= 0; i++){
                indexStart--;
                XYDataItem tempXY = originalqIq.getDataItem(indexStart);

                fittedqIq.add(tempXY);
                if (tempXY.getYValue() > 0) {
                    logData.add(tempXY.getX(), Math.log10(allData.getY(indexStart).doubleValue()));
                }
            }
        }
        this.startAt = target;
    }


    public void incrementHigh(int target){

        // add point to fittedqiq
        int diff = target - this.getStop();

        if (diff == 1){
            XYDataItem tempXY = originalqIq.getDataItem(target-1);
            fittedqIq.add(tempXY);

            if (tempXY.getYValue() > 0){
                logData.add(tempXY.getX(), Math.log10(allData.getY(target-1).doubleValue()));
            }
        } else { // add more than one point
            int indexStart = this.getStop();
            int maxCount = originalqIq.getItemCount();
            int limit = (target > maxCount) ? maxCount : (target);

            for (int i=indexStart; i < limit; i++){
                XYDataItem tempXY = originalqIq.getDataItem(i);
                fittedqIq.add(tempXY);

                if (tempXY.getYValue() > 0) {
                    logData.add(tempXY.getX(), Math.log10(allData.getY(i).doubleValue()));
                }
            }

            target = limit;
        }
        this.stopAt = target;
    }


    public void decrementHigh(int spinnerValue){

        int total = this.stopAt - spinnerValue;

        if (total == 1){
            Number currentQ = fittedqIq.getMaxX();
            if (currentQ.equals(logData.getMaxX())){
                fittedqIq.remove(fittedqIq.getItemCount()-1);
                logData.remove(logData.getItemCount()-1);
            }
        } else {

            int maxFittedqiq = fittedqIq.getItemCount();
            int delta = this.stopAt - spinnerValue;
            // spinnerValue is in reference to complete original data
            fittedqIq.delete(maxFittedqiq - delta, maxFittedqiq-1);

            double lastValue = fittedqIq.getMaxX();
            if (logData.getMaxX() > lastValue) { // if true, keep deleting until logData.MaxX <= fittedqIq.maxX
                int last = logData.getItemCount();
                while (logData.getMaxX() > lastValue){
                    last--;
                    logData.remove(last);
                }
            }
        }

        this.stopAt = spinnerValue;
    }

    /**
     * should be unscaled data, as is
     * @param data
     * @param error
     */
    public void updatedRefinedSets(XYSeries data, XYSeries error){
        int total = data.getItemCount();
        refinedIq.clear();
        refinedError.clear();
        for (int i=0; i<total; i++){
            refinedIq.add(data.getDataItem(i));
            refinedError.add(error.getDataItem(i));
        }
    }

    public XYSeries getRefinedqIData(){
        return refinedIq;
    }

    /**
     * assumes I(q) and not q*I(q)
     * Estimates chi-square at the cardinal series
     * @throws Exception
     */
    public void chi_estimate(XYSeries data, XYSeries error) throws Exception{
        // how well does the fit estimate the cardinal series (shannon points)
        double chi=0;
        double inv_card;
        final double inv_dmax = 1.0/(double)dmax;
        final double dmax_inv_pi = dmax/Math.PI;
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
                if ((cardinal - priorValue.getXValue())*inv_card < 0.0001) {// check if difference is smaller pre-cardinal
                    error_value = 1.0/error.getY(count - 1).doubleValue();
                    diff = priorValue.getYValue() - moore_Iq(priorValue.getXValue());
                    chi += (diff*diff)*(error_value*error_value);
                    df += 1.0;
                } else if ( Math.abs(postValue.getXValue() - cardinal)*inv_card < 0.0001) {// check if difference is smaller post-cardinal
                    error_value = 1.0/error.getY(count).doubleValue();
                    diff = postValue.getYValue() - moore_Iq(postValue.getXValue());  // residual
                    chi += (diff*diff)*(error_value*error_value);
                    df += 1.0;
                } else { // if difference is greater than 0.1% interpolate and also the cardinal is bounded
                    values = Functions.interpolateOriginal(data, error, cardinal);
                    diff = values[1] - (standardizationStDev*(mooreCoefficients[0] + mooreCoefficients[i]*dmax_inv_pi) + standardizationMean)*inv_card;
                    //System.out.println(i + " " + cardinal + " " + values[1] + " " + (((mooreCoefficients[0] + mooreCoefficients[i]*dmax_inv_pi)*standardizationStDev + standardizationMean)*inv_card) );
                    //System.out.println(i + " " + cardinal + " obs " + values[1] + "( "+ values[2] + " )  calc => " + (standardizationStDev*(mooreCoefficients[0] + mooreCoefficients[i]*dmax_inv_pi*inv_card) + standardizationMean));
                    chi += (diff*diff)/(values[2]*values[2]);
                    df += 1.0;
                }
            }
        }

        double delta = totalMooreCoefficients - df;

        System.out.println("dmax => " + dmax + " SUM " + chi + " " + (totalMooreCoefficients - df) + " (a_m).size : " + totalMooreCoefficients + " df : " + df);
        //return chi*1.0/(df-1);
        chi2 = chi*1.0/(df - 1 - delta);
    }

    public int getTotalMooreCoefficients(){
        return totalMooreCoefficients;
    }

    private double averageIntensity(XYSeries data){
        int total = data.getItemCount();
        double sum=0;
        for (int i=0; i<total; i++){
            sum+=data.getY(i).doubleValue();
        }
        return sum/(double)total;
    }


    public double getReciprocalSpaceScaleFactor(){ return dataset.getScaleFactor();}

    public void estimateErrors(){
        // given set of Moore coefficients
        // bin the data
        // subsample fit
        double dmax2 = dmax*dmax;
        double dmax4 = dmax2*dmax2;
        double pi_sq = 9.869604401089358;
        double inv_pi = 1.0/Math.PI;
        double inv_pi_cube = 1.0/(pi_sq*Math.PI);
        double inv_pi_fourth = inv_pi_cube*inv_pi;
        double dmax4_inv_pi_fourth = 2*dmax4*inv_pi_fourth;
        double twodivPi = 2.0*inv_pi;
        double pi_k_2, am, am3, minus1;

        int size = fittedqIq.getItemCount();
        double upperq = fittedqIq.getMaxX();
        double bins = totalMooreCoefficients-1;
        double delta_q = upperq/bins;
        double samplingLimit;

        XYDataItem tempData;
        ArrayList<double[]> results;
        int totalRuns = 31;
        double[] rgValues = new double[totalRuns];
        double[] izeroValues = new double[totalRuns];
        double tempIzero, partial_rg, rsum;

        ArrayList<Integer> countsPerBin = new ArrayList<>();
        int startbb = 0;

        // determine counts per bin to draw from

        // standardize data using specified mean and stdev
        double invStdev = 1.0/standardizationStDev;


        for (int b=1; b <= bins; b++) {
            int sumCount = 0;
            double upperBound = delta_q*b;

            binloop:
            for (int bb=startbb; bb < size; bb++){
                double tempCurrent = fittedqIq.getX(bb).doubleValue();

                if ( (tempCurrent >= (delta_q*(b-1)) ) && (tempCurrent < upperBound) ){
                    sumCount += 1.0;
                } else if (tempCurrent >= upperBound ) {
                    startbb = bb;
                    break binloop;
                }
            }
            // you want a better algorithm, by a bad computer
            // greens theorem surface integral
            countsPerBin.add((int)sumCount);
        }

        XYSeries randomSeries = new XYSeries("Random-");

        int upperbb, locale;
        Random randomGenerator = new Random();
        int[] randomNumbers;

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

                    samplingLimit = (1.0 + randomGenerator.nextInt(17))/100.0;  // return a random percent up to ...

                    binloop:
                    for (int bb=startbb; bb < size; bb++){
                        if (fittedqIq.getX(bb).doubleValue() >= (delta_q*b) ){ // what happens on the last bin?
                            upperbb = bb;
                            break binloop;
                        }
                    }

                    // grab indices inbetween startbb and upperbb
                    randomNumbers = Functions.randomIntegersBounded(startbb, upperbb, samplingLimit);

                    startbb = upperbb;

                    for(int h=0; h<randomNumbers.length; h++){
                        locale = randomNumbers[h];
                        tempData = fittedqIq.getDataItem(locale);
                        randomSeries.add(tempData.getXValue(), (tempData.getYValue()/tempData.getXValue()-standardizationMean)*invStdev*tempData.getXValue());
                    }
                } // end of checking if bin is empty
            }
            // calculate PofR
            // PrObject prObject = new PrObject(this, randomSeries, upperq, dmax, 0.001, false);
            PrObject prObject = new PrObject(randomSeries, upperq, dmax, 0.01);
            results = prObject.moore_coeffs_L1();

            // calculate Izero and Rg
            tempIzero = 0;
            partial_rg = 0;
            rsum = 0;

            for (int k = 1; k < bins+1; k++) {
                am = results.get(0)[k];
                minus1 = FastMath.pow(-1, (k+1));

                tempIzero = tempIzero + am/(k)*minus1;
                pi_k_2 = pi_sq*k*k;
                am3 = am/(double)(k*k*k);

                partial_rg = partial_rg + am3*(pi_k_2 - 6)*minus1;
                rsum = rsum + am3*((pi_k_2 - 2)*minus1 - 2 );
            }

            //tempIzero = twodivPi*tempIzero*dmax2*inv_pi + results.get(0)[0];
            //tempIzero = standardizationStDev*(twodivPi*tempIzero*dmax2*inv_pi + mooreCoefficients[0]) + standardizationMean;
            double izero_temp = (twodivPi*tempIzero*dmax2/Math.PI + results.get(0)[0]);
            // rg = Math.sqrt(2*dmax4*inv_pi_fourth/izero_temp*partial_rg)*0.7071067811865475; // 1/Math.sqrt(2);
            // rgValues[i] = Math.sqrt(dmax4_inv_pi_fourth/tempIzero*partial_rg)*0.7071067811865475; // 1/Math.sqrt(2);
            rgValues[i] = Math.sqrt(dmax4_inv_pi_fourth/izero_temp*partial_rg)*0.7071067811865475;
            //izeroValues[i] = tempIzero;
            izeroValues[i] = standardizationStDev*(twodivPi*tempIzero*dmax2*inv_pi + results.get(0)[0]) + standardizationMean;
        }

        DescriptiveStatistics rgStat = new DescriptiveStatistics(rgValues);
        DescriptiveStatistics izeroStat = new DescriptiveStatistics(izeroValues);

        this.dataset.updateRealSpaceErrors(rgStat.getStandardDeviation()/rgStat.getMean(), izeroStat.getStandardDeviation()/izeroStat.getMean());
    }

    // auto-Dmax ?
    // throw exception if no Guiner region

    public void estimateDmax(double lambda, boolean usel1, int cBox, boolean useDirect){
        // make q*I(q) dataset with extrapolated values from Guinier
        // perform integral sine transform
        XYSeries qIq = new XYSeries("qIQ for integral transform");

        double startq = fittedqIq.getMinX();
        double guinierRg = dataset.getGuinierRg();
        double guinierIzero = dataset.getGuinierIzero();

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
        double scaleFactor = dataset.getScaleFactor();

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
            xyValue = tempqIqDataItem.getYValue();
            qIq.add(xValue, xyValue * scaleFactor);
        }

        double lastqvalue = qIq.getMaxX();
        int totalqIqValues = qIq.getItemCount();

        int startAt=0;
        if (lastqvalue > 0.1){
            for (int q=0; q<totalqIqValues; q++){
                if (qIq.getX(q).doubleValue() > 0.1){
                    startAt = q;
                    break;
                }
            }
        }

        double startRvalue, nextRvalue;

        XYSeries tempSeries = new XYSeries("temp");

        for(int qm=0; qm < startAt; qm++){
            tempSeries.add(qIq.getDataItem(qm));
        }

        int q = startAt, qlimit;
        boolean changed;

        ArrayList<String> score = new ArrayList<>();
        int countOfScore = 1;
        while(q < totalqIqValues){

            startRvalue = 41;
            changed = false;

            for (int round=0; round < 1000; round++){

                nextRvalue = startRvalue - integralTransform(startRvalue, tempSeries)/finiteDifferencesDerivative(startRvalue, tempSeries);
                //System.out.println(round + " " + startRvalue + " <=> " + nextRvalue);
                if (integralTransform(nextRvalue, tempSeries) < 0.000000001){
                    startRvalue = nextRvalue;
                    changed = true;
                    break;
                }

                startRvalue = nextRvalue;
            }

            // fit P(r) using startRvalue and calculate chi and sk2
            if (changed && startRvalue > 10 && startRvalue < 1000){
                this.dmax = (int)startRvalue;
                PrObject tempPr = new PrObject(this, lambda, usel1, cBox, useDirect, false);
                tempPr.run();
                //this.chi_estimate(allData.createCopy(startAt-1, stopAt-1), errorAllData.createCopy(startAt-1, stopAt-1));
                //this.kurt_l1_sum = l1_norm_pddr(1) + max_kurtosis_shannon_sampled(0);
                //this.kurtosis = Math.abs(this.max_kurtosis_shannon_sampled(0));
                //this.l1_norm = this.l1_norm_pddr(11);
                //this.kurt_l1_sum = 0.1*this.kurtosis + 0.9*this.l1_norm;
                //System.out.println(countOfScore + " dmax " + this.dmax + " chi2 => " + this.chi2 + "\t" + tempSeries.getMaxX());
                if (this.chi2 < 4 && !negativeValuesInModel){
                    score.add(countOfScore + "\t" + this.dmax  + "\t" + this.chi2 + "\t" + this.kurt_l1_sum + "\t" + tempSeries.getMaxX());
                    countOfScore++;
                }
            }

            //tempSeries.add(qIq.getDataItem(q));
            // add next 5 values
            qlimit = q + 7;
            if (qlimit < totalqIqValues){
                for(int qi=q; qi < qlimit; qi++){
                    tempSeries.add(qIq.getDataItem(qi));
                }
            }
            q = qlimit;
        }


        for(int s=0; s<score.size(); s++){
            System.out.println(score.get(s));
        }

        /*
        for(int q=startAt; q < qIq.getItemCount(); q++){

            startRvalue = 30;
            System.out.println("MAX Q : " + tempSeries.getMaxX());

            for (int round=0; round < 1000; round++){

                nextRvalue = startRvalue - integralTransform(startRvalue, qIq)/finiteDifferencesDerivative(startRvalue, qIq);

                System.out.println(round + " " + startRvalue + " <=> " + nextRvalue);

                if (integralTransform(nextRvalue, qIq) < 0.00000001){
                    System.out.println("FINISHED " + nextRvalue);
                    startRvalue = nextRvalue;
                    break;
                }

                startRvalue = nextRvalue;
            }
            // fit P(r) using startRvalue and calculate chi and sk2
            if (startRvalue > 10 && startRvalue < 1000){
                this.dmax = (int)startRvalue;
                PrObject tempPr = new PrObject(this, lambda, usel1, cBox);
                tempPr.run();
            }

            tempSeries.add(qIq.getDataItem(q));
        }
*/


        // qIq dataset is complete upto qmax with extrapolated low-q data
        //double startRvalue = 30, nextRvalue;
/*
        System.out.println("MAX Q : " + lastqvalue);

        for (int round=0; round<1000; round++){

            nextRvalue = startRvalue - integralTransform(startRvalue, qIq)/finiteDifferencesDerivative(startRvalue, qIq);

            System.out.println(round + " " + startRvalue + " <=> " + nextRvalue);

            if (integralTransform(nextRvalue, qIq) < 0.00000001){
                System.out.println("FINISHED " + nextRvalue);
                break;
            }

            startRvalue = nextRvalue;
        }
*/
    }


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


    public void setStandardizationMean(double value, double stdev){
        this.standardizationMean = value;
        this.standardizationStDev = stdev;
    }

}
