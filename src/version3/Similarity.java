package version3;

import com.sun.org.glassfish.external.statistics.Statistic;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.*;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.util.*;

/**
 * Created by robertrambo on 20/01/2016.
 */
public class Similarity implements Runnable {

    private JFreeChart chart;
    private ChartFrame frame;
    //private Collection collectionInUse;
    private ArrayList<SimilarityCollectionItem> collections;
    private XYSeriesCollection averageValuePerBin;
    private XYSeriesCollection plottedCollection;
    private XYSeriesCollection collectionOfkurtosisPerFrame;
    private double qmin, qmax, bins, binWidth;
    private ArrayList<Number> keptQvalues;
    private SortedSet<Number> commonQValues;
    private JLabel status;
    private JProgressBar bar;
    private boolean useKurtosis = false;
    private boolean useVr = false;
    private int totalItemsInCollection =0;
    private int cpus = 1;
    private String range_type="";

    public Similarity(JLabel status, final JProgressBar bar){

        this.status = status;
        this.bar = bar;
        commonQValues = new TreeSet<>();
        keptQvalues = new ArrayList<>();
        collectionOfkurtosisPerFrame = new XYSeriesCollection();
        averageValuePerBin = new XYSeriesCollection();
        collections = new ArrayList<>();

        chart = ChartFactory.createXYLineChart(
                "",                  // chart title
                "",                        // domain axis label
                "",                          // range axis label
                plottedCollection,               // data
                PlotOrientation.VERTICAL,
                false,                      // include legend
                true,
                false
        );
    }

    @Override
    public void run() {

        if (useKurtosis){

            range_type="kurtosis";
            //averageValuePerBin.removeAllSeries();
            this.calculateKurtosis();

            XYPlot plot = chart.getXYPlot();
            plot.setDataset(plottedCollection);
/*
            int totalSeries = averageValuePerBin.getSeriesCount();
            for (int i=0; i<totalSeries; i++){
                int totalpoints = averageValuePerBin.getSeries(i).getItemCount();
                XYSeries temp = averageValuePerBin.getSeries(i);
                System.out.println(i + "\n ");
                for (int j=0; j<totalpoints; j++){
                    System.out.println(temp.getX(j) + " " + temp.getY(j));
                }
            }
*/


            makeKurtosisPlot();

        } else if (useVr){

        }

    }

    public void clearAll(){
        plottedCollection.removeAllSeries();
        //averageValuePerBin.removeAllSeries();
        commonQValues.clear();
        keptQvalues.clear();
        collections.clear();
        totalItemsInCollection = 0;
    }

    public void addCollection(String name){
        collections.add(new SimilarityCollectionItem(name));
        totalItemsInCollection++;
    }

    public SimilarityCollectionItem getCollectionItemByIndex(int index){
        return collections.get(index);
    }

    public int getTotalItemsInCollection(){
        return totalItemsInCollection;
    }

    public void setParameters(double qmin, double qmax, double bins, int cpus){
        this.qmin = qmin;
        this.qmax = qmax;
        this.bins = bins;
        this.binWidth = (qmax-qmin)/bins;
        System.out.println("QMIN to QMAX: " + qmin + " < " + qmax + " BINWIDTH => " + binWidth);
        this.cpus = cpus;
    };

    public void calculateCorrelation(){
        //for each q-value create XYSeries

        // go through each q value and create a common set, no interpolation
        /*
        int totalDatasets = collectionInUse.getDatasetCount();
        int totalSelected = collectionInUse.getTotalSelected();

        for(int i=0; i<totalDatasets; i++){
            Dataset dataset = collectionInUse.getDataset(i);
            if (dataset.getInUse()){
                int totaldatapoints = dataset.getAllData().getItemCount();
                for (int j=0; j<totaldatapoints; j++){
                    commonQValues.add(dataset.getAllData().getX(j));
                }
            }
        }

        // create hashmap key = qValues

        Iterator it = commonQValues.iterator();
        while (it.hasNext()) {
            Number qvalue = (Number) it.next();
            int count = 0;
            for(int i=0; i<totalDatasets; i++){
                Dataset dataset = collectionInUse.getDataset(i);
                if (dataset.getInUse()){
                    XYDataItem tempXY = dataset.getAllData().getDataItem(i);

                    if (dataset.getAllData().indexOf(qvalue) > -1 && tempXY.getXValue() > qmin && tempXY.getXValue() < qmax ){
                        count++;
                    }
                }
            }
            // should be sorted since iteration is over a sorted set
            if (count == totalSelected){ // keep q-value
                keptQvalues.add(qvalue);
            }
        }
*/
        // Matrix [row (q-value), column (frame)]
        // average across row to get average Intensity at a specific q-value

    }

    /**
     *
     */
    public void calculateKurtosis(){
        // for each bin, collect values, do outlier rejection and average
        collectionOfkurtosisPerFrame.removeAllSeries();

        int totalDatasetToAnalyze = collections.size();

        System.out.println("Collections to Analyze : " + totalDatasetToAnalyze);

        Collection collectionInUse;

        for (int dd=0; dd<totalDatasetToAnalyze; dd++){

            collectionInUse = collections.get(dd).getCollection();
            // first dataset is reference to calculate ratio to
            int totalDatasets = collectionInUse.getDatasetCount();
            Dataset referenceSet =  null;  // set first checked dataset as reference

            int startAt=0;

            for(int i=0; i<totalDatasets; i++){
                referenceSet = collectionInUse.getDataset(i);
                if (referenceSet.getInUse()){
                    startAt = i+1;
                    break;
                }
            }

            int startPt=0, endPt;
            int totalPoints = referenceSet.getAllData().getItemCount();
            endPt = totalPoints - 1;
            for (int i=0; i<totalPoints; i++){
                if (referenceSet.getAllData().getX(i).doubleValue() >= qmin){
                    startPt = i;
                    break;
                }
            }

            int seriesCount=0;

            collectionOfkurtosisPerFrame.addSeries(new XYSeries("Index " +dd));
            int locale = collectionOfkurtosisPerFrame.getSeriesCount()-1;

            for(int s=startAt; s<totalDatasets; s++){

                Dataset targetSet = collectionInUse.getDataset(s);
                if (targetSet.getInUse()){

                    double kurt = calculateKurtosisPerSet(referenceSet, startPt, endPt, targetSet);

                    collectionOfkurtosisPerFrame.getSeries(locale).add(seriesCount, kurt);
                    System.out.println(s + " target set " + targetSet.getFileName() + " <=> " + seriesCount + " " + kurt);
                    seriesCount++;
                }
            }
        }


        plottedCollection = collectionOfkurtosisPerFrame;
    }

    /**
     * Returns a single value, the kurtosis of the ratio of the two datasets.
     *
     * @param referenceSet
     * @param startPt
     * @param endPt
     * @param targetSet
     * @return double representing kurtosis of the ratio
     */
    private double calculateKurtosisPerSet(Dataset referenceSet, int startPt, int endPt, Dataset targetSet){

        XYSeries referenceSeries = referenceSet.getAllData();
        XYSeries referenceError = referenceSet.getAllDataError();
        XYSeries ratioSeries;
        YIntervalSeries ratioErrorSeries;
        XYDataItem referenceXY, errorXYref, tarXY, ratioXY;

        ArrayList<Double> valuesPerBin = new ArrayList<>();
        ArrayList<Double> ratioValuesForAveraging = new ArrayList<>();

        double xValue, ratioValue, ratioError, qlower, qupper;
        int locale, seriesCount=0, intBins = (int)bins;

        //System.out.println(targetSet.getFileName() + " " + averageValuePerBin.getSeriesCount());
        //averageValuePerBin.addSeries(new XYSeries(targetSet.getFileName()));

        ratioSeries = new XYSeries("");
        ratioErrorSeries = new YIntervalSeries("RatioError", false, false);
        ratioValuesForAveraging.clear();

        XYSeries targetSeries = targetSet.getAllData();
        XYSeries targetError = targetSet.getAllDataError();

        for(int i=startPt; i< endPt; i++){

            referenceXY = referenceSeries.getDataItem(i);
            errorXYref = referenceError.getDataItem(i);

            xValue = referenceXY.getXValue();
            locale = targetSeries.indexOf(referenceXY.getX());

            if (locale >= 0){
                tarXY = targetSeries.getDataItem(locale);

                // reference/target
                ratioValue = referenceXY.getYValue()/tarXY.getYValue();

                ratioSeries.add(xValue, ratioValue);
                ratioError = ratioValue*Math.sqrt((errorXYref.getYValue()/referenceXY.getYValue()*errorXYref.getYValue()/referenceXY.getYValue()) + (tarXY.getYValue()*tarXY.getYValue()*tarXY.getYValue()*tarXY.getYValue()));
                ratioErrorSeries.add(xValue, ratioValue, ratioValue - ratioError, ratioValue+ratioError);
                ratioValuesForAveraging.add(ratioValue);
            } else {
                // interpolate value
                // make sure reference q values is greater than first two or last two points in sourceSeries
                if ( (xValue > targetSeries.getX(1).doubleValue()) || (xValue < targetSeries.getX(targetSeries.getItemCount()-2).doubleValue()) ){

                    Double[] results =  Functions.interpolateOriginal(targetSeries, targetError, xValue, 1);
                    //target.add(xValue, results[1]);
                    ratioValue = referenceXY.getYValue()/results[1];
                    ratioSeries.add(xValue, ratioValue);
                    ratioValuesForAveraging.add(ratioValue);
                }
            }
        }

        // do binning and calculate MAD average for each bin

        int startIndex = 0;
        int totalRatio = ratioSeries.getItemCount();
        double mean, diff, diff2, averageBottom;
        //XYSeries tempK = averageValuePerBin.getSeries(seriesCount);
        XYSeries tempK = new XYSeries("tempK");

        for (int b=0; b < intBins; b++){
            // fill bins
            qlower = qmin + b*binWidth;
            qupper = qlower+binWidth;
            valuesPerBin.clear();

            Innerloop:
            for (int m=startIndex; m<totalRatio; m++){
                ratioXY = ratioSeries.getDataItem(m);
                if (ratioXY.getXValue() > qlower && ratioXY.getXValue() <= qupper){
                    valuesPerBin.add(ratioXY.getYValue());
                } else if (ratioXY.getXValue() > qupper){
                    startIndex = m;
                    break Innerloop;
                }
            }

            // calculate MAD and average
            DescriptiveStatistics keptSet = averageByMAD(valuesPerBin);
            mean = keptSet.getMean();
            // average per bin
            tempK.add(0.5*binWidth + qlower, mean);
        }

        // calculate kurtosis of tempK
        DescriptiveStatistics top = new DescriptiveStatistics();
        DescriptiveStatistics bottom = new DescriptiveStatistics();
        mean = meanFromXYSeries(tempK);
        for (int ss=0; ss<bins; ss++){
            diff = tempK.getY(ss).doubleValue() - mean;
            diff2 = diff*diff;
            top.addValue(diff2*diff2);
            bottom.addValue(diff2);
        }

        averageBottom = bottom.getMean();
        return top.getMean()/(averageBottom*averageBottom) - 3;
    }

    public void setFunction(boolean useKurtosis, boolean useVr){
        this.useKurtosis = useKurtosis;
        this.useVr = useVr;
    }

    public void makeHeatMap(){

    }


    private double sumOfXYSeries(XYSeries series){
        double sum=0;
        int limit = series.getItemCount();
        for (int i=0; i<limit; i++){
            sum += series.getY(i).doubleValue();
        }
        return sum;
    }


    private DescriptiveStatistics averageByMAD(ArrayList<Double> values){

        int total = values.size();
        DescriptiveStatistics stats = new SynchronizedDescriptiveStatistics();

        for (int i=0; i<total; i++) {
            stats.addValue(values.get(i));
        }

        double median = stats.getPercentile(50);
        DescriptiveStatistics deviations = new SynchronizedDescriptiveStatistics();

        ArrayList<Double> testValues = new ArrayList<>(total);

        for (int i=0; i<total; i++){
            testValues.add(Math.abs(values.get(i) - median));
            deviations.addValue(testValues.get(i));
        }

        double mad = 1.4826*deviations.getPercentile(50);
        double invMAD = 1.0/mad;

        // create
        DescriptiveStatistics keptValues = new DescriptiveStatistics();

        for (int i=0; i<total; i++){
            if (testValues.get(i)*invMAD < 2.5 ){
                keptValues.addValue(values.get(i));
            }
        }

        return keptValues;
    }

    private double meanFromXYSeries(XYSeries temp){
        int total = temp.getItemCount();
        double sum = 0.0;
        for (int i=0; i<total; i++){
            sum += temp.getY(i).doubleValue();
        }
        return sum/(double)total;
    }

    private void makeKurtosisPlot(){

        chart.setTitle("SC\u212BTTER \u2263 Plot");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);

        final NumberAxis domainAxis = new NumberAxis("Signal");
        final NumberAxis rangeAxis = new NumberAxis(range_type);
        rangeAxis.setAutoRangeIncludesZero(false);

        final XYPlot plot = chart.getXYPlot();
        plot.setRangeAxis(rangeAxis);

        frame = new ChartFrame("SC\u212BTTER \u2263 SIGNAL PLOT", chart);
        frame.getChartPanel().setDisplayToolTips(true);

        frame.getChartPanel().getChart().getXYPlot().getRenderer(0).setBaseToolTipGenerator(new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset xyDataset, int i, int i2) {
                return (String) xyDataset.getSeriesKey(i);
            }
        });

        frame.getChartPanel().setRangeZoomable(false);
        frame.getChartPanel().setDomainZoomable(false);

        frame.pack();
        frame.setVisible(true);
    }


    public void editSets(){

    }

}
