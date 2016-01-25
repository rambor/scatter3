package version3;

import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class RealSpace {

    private String filename;
    private boolean selected;
    private int id;
    private int start;
    private int stop;
    private XYSeries allData;  // use for actual fit and setting spinners
    private XYSeries errorAllData;  // use for actual fit and setting spinners

    private XYSeries fittedIq; // range of data used for the actual fit, may contain negative values
    private XYSeries qIq; // range of data used for the actual fit, may contain negative values
    private XYSeries fittedError;

    private XYSeries calcIq;   // log of calculate I(q), matches logData
    private XYSeries nonLogCalcIq;   // non-log of calculate I(q), matches logData
    private XYSeries logData;  // only for plotting
    private XYSeries prDistribution;
    private double analysisToPrScaleFactor;
    private double izero;
    private double rg;
    private double[] mooreCoefficients;
    private int dmax;
    private double qmax;
    private double raverage;
    private float chi2;
    private float scale;
    private Color color;
    private boolean baseShapeFilled;
    private int pointSize;
    private BasicStroke stroke;
    private double kurtosis = 0;
    private double l1_norm = 0;
    private double kurt_l1_sum;
    private Dataset dataset;

    // rescale the data when loading analysisModel

    public RealSpace(Dataset dataset){
        this.dataset = dataset;
        this.filename = dataset.getFileName();
        this.id = dataset.getId();
        this.dmax = (int)dataset.getDmax();
        selected = true;
        analysisToPrScaleFactor = 1;

        try {
            allData = dataset.getAllData().createCopy(0,dataset.getAllData().getItemCount()-1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            errorAllData = dataset.getAllDataError().createCopy(0,dataset.getAllData().getItemCount()-1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        fittedIq = new XYSeries(Integer.toString(this.id) + "fitted-" + filename);
        fittedError = new XYSeries(Integer.toString(this.id) + "fittedError-" + filename);
        logData = new XYSeries(Integer.toString(this.id) + "log-" + filename);
        calcIq = new XYSeries(Integer.toString(this.id) + "calc-" + filename);
        nonLogCalcIq = new XYSeries(Integer.toString(this.id) + "calc-" + filename);

        qIq = new XYSeries(Integer.toString(this.id) + "qIq-" + filename);

        prDistribution = new XYSeries(Integer.toString(this.id) + "Pr-" + filename);
        this.color = dataset.getColor();
        pointSize = dataset.getPointSize();
        stroke = dataset.getStroke();
        scale = 1.0f;
        baseShapeFilled = false;
        //for spinners
        this.start = dataset.getStart(); // spinner value
        this.stop = dataset.getEnd();   // spinner value

        double xValue;
        double yValue;

        // transform all the data in allData

        for(int i=0; i < allData.getItemCount(); i++){
            XYDataItem temp = allData.getDataItem(i);

            xValue = temp.getXValue();
            yValue = temp.getYValue();

            if (i >= (start-1) && (i<stop)){
                if (temp.getYValue() > 0){
                    logData.add(xValue, Math.log10(yValue));
                }
                //fittedIq.add(temp);
                fittedIq.add(xValue, yValue);
                fittedError.add(errorAllData.getDataItem(i));
                qIq.add(xValue, xValue*yValue); // fitted data
            }
        }

        //check for negative values at start
        double qlimit = 0.05;
        XYDataItem temp;
        for (int i=0; i< allData.getItemCount(); i++){
            temp = allData.getDataItem(i);

            if (temp.getYValue() < 0){
                start = i + 1;
            }

            if (temp.getXValue() > qlimit){
                break;
            }
        }

    }

    public double getGuinierRg(){
        return dataset.getGuinierRg();
    }

    public double getGuinierIzero(){
        return dataset.getGuinierIzero();
    }


    /**
     * shoudl match spinner index
     * @param i
     */
    public void setStart(int i){
        start = i;
    }

    /**
     * set start value of the spinner
     * @return
     */
    public int getStart(){
        return start;
    }

    public void setStop(int i){
        stop = i;
    }

    public int getStop(){
        return stop;
    }

    public int getDmax(){
        return dmax;
    }

    public void setDmax(int d){
        this.dataset.setDmax(d);
        dmax = d;
    }

    public int getId(){
        return id;
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

    public float getChi2(){
        return chi2;
    }


    public void setChi2(float j){
        chi2 = j;
        this.kurtosis = Math.abs(this.max_kurtosis_shannon_sampled(301));
        this.l1_norm = this.l1_norm_pddr(11);
        this.kurt_l1_sum = 0.1*this.kurtosis + 0.9*this.l1_norm;
        // System.out.println("CHI2 " + j + " " + this.kurtosis + " L1 " + this.l1_norm);
    }

    public float getScale(){
        return scale;
    }

    public void setScale(float j){
        float oldscale = 1/this.scale;
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
        this.prDistribution = data;
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

    public XYSeries getqIq(){
        return qIq;
    }

    public XYSeries getErrorAllData(){
        return errorAllData;
    }

    public XYSeries getfittedIq(){
        return fittedIq;
    }

    public XYSeries getfittedError(){
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

    public void setMooreCoefficients(double[] values, Dataset tempData){
        this.mooreCoefficients = values;
        ArrayList<Double> temp;
        temp = Functions.saxs_invariants(this.mooreCoefficients, this.dmax);
        this.izero = temp.get(0);
        this.rg = temp.get(1);
        this.raverage = temp.get(2);
    }

    public void setRealVariants(){
        ArrayList<Double> temp;
        temp = Functions.saxs_invariants(this.mooreCoefficients, this.dmax);
        this.izero = temp.get(0);
        this.rg = temp.get(1);
        this.raverage = temp.get(2);
    }


    public double l1_norm_pddr(int multiple){
        double inv_d = 1.0/this.dmax;
        double r_value, pi_r_n_inv_d, pi_r_inv_d, cos_pi_r_n_inv_d, sin_pi_r_n_inv_d;
        double pi_inv_d = Math.PI*inv_d;
        double inv_2d = inv_d*0.5;
        double a_i;

        //int r_limit = r_vector.length;

        int coeffs_size = this.mooreCoefficients.length;

        double a_i_sum, product, l1_norm = 0.0;


        double shannon = Math.ceil(multiple*1.0/Math.PI*this.dmax*this.fittedIq.getMaxX());
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

        return l1_norm;
    }

    public double max_kurtosis_shannon_sampled(int rounds){
        /*
         * Divide scattering curve into Shannon bins
         * Determine kurtosis from a random sample of the ratio of I_calc to I_obs based on the binning
         * Take max of the kurtosis set
         */

        int total = this.fittedIq.getItemCount();
        int limit = this.getMooreCoefficients().length;
        double[] ratio = new double[total];

        for (int i=0; i<total; i++){
            XYDataItem values = this.fittedIq.getDataItem(i);
            // ratio of calc/obs
            ratio[i] = Functions.moore_Iq(this.getMooreCoefficients(), this.dmax, values.getXValue(), limit)/values.getYValue();
        }

        /*
         * bin the ratio
         * qmax*dmax/PI
         *
         */
        double qmin = this.fittedIq.getMinX();
        double qmax = this.fittedIq.getMaxX();

        int bins = (int)(Math.round(qmax*dmax/Math.PI)), locale;

        ArrayList<Double> test_ratios = new ArrayList<Double>();
        ArrayList<Double> kurtosises = new ArrayList<Double>();
        double delta_q = (qmax-qmin)/bins;

        double samplingLimit;
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

                binloop:
                for (int j = startbb; j < total; j++) {
                    if (this.fittedIq.getX(j).doubleValue() >= (delta_q * b + qmin)) {
                        upperbb = j;
                        break binloop;
                    }
                }
                // grab indices inbetween startbb and upperbb
                randomNumbers = Functions.randomIntegersBounded(startbb, upperbb, samplingLimit);
                startbb = upperbb;

                for(int h=0; h < randomNumbers.length; h++){
                    locale = randomNumbers[h];
                    test_ratios.add(ratio[locale]);
                }
            }
            // calculate kurtosis
            kurtosises.add(StatMethods.kurtosis(test_ratios));
        }

        // double returnMe = Collections.max(kurtosises);
        // double returnMe = Statistics.calculateMedian(kurtosises);

        return Statistics.calculateMedian(kurtosises);
    }
}
