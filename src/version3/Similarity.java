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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.*;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by robertrambo on 20/01/2016.
 */
public class Similarity implements Runnable {

    private JFreeChart chart;
    private ChartFrame frame;
    private XYZDataset heatData;
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
    private int totalItemsInCollection=0;
    private int cpus = 1;
    private String range_type="";
    private double[] xFrameHeat;
    private double[] yBinHeat;
    private double[] zHeat;
    private int xCount;
    private int yCount;
    private int zCount;
    private int frameCount;
    private JPanel panelForChart;
    private String directory;

    public Similarity(JLabel status, final JProgressBar bar){

        this.status = status;
        this.bar = bar;
        commonQValues = new TreeSet<>();
        keptQvalues = new ArrayList<>();
        collectionOfkurtosisPerFrame = new XYSeriesCollection();
        averageValuePerBin = new XYSeriesCollection();
        collections = new ArrayList<>();


        chart = ChartFactory.createXYLineChart(
                "",                         // chart title
                "",                         // domain axis label
                "",                         // range axis label
                plottedCollection,          // data
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

            // write out file for each collection
            int totalCollections = collections.size();
            for (int i=0; i<totalCollections; i++){

            }
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

            int totalDatasetToAnalyze = collections.size();
            int count=0;
            for (int i=0; i<totalDatasetToAnalyze; i++){
                if(collections.get(i).isSelected()){
                    writeCollectionToFile(collections.get(i).getName() + "_kurtosis", collectionOfkurtosisPerFrame.getSeries(count));
                    count++;
                }
            }


        } else if (useVr){

        }

    }

    public void setDirectory(String object){
        this.directory = object;
    }

    private void writeCollectionToFile(String name, XYSeries data){

        try {
            FileWriter fw = new FileWriter(directory +"/"+name+".txt");
            BufferedWriter out = new BufferedWriter(fw);
            int total = data.getItemCount();

            out.write(String.format("REMARK\t  COLUMNS : frame, kurtosis%n"));

            for (int n=0; n < total; n++) {
                out.write( String.format("%d\t%s %n", (int)data.getX(n).doubleValue(), Constants.Scientific1dot5e2.format(data.getY(n).doubleValue()) ));
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void setParameters(double qmin, double qmax, double bins, int cpus, JPanel outsidePanel){
        this.qmin = qmin;
        this.qmax = qmax;
        this.bins = bins;
        this.binWidth = (qmax-qmin)/bins;
        System.out.println("QMIN to QMAX: " + qmin + " < " + qmax + " BINWIDTH => " + binWidth);
        this.cpus = cpus;
        this.panelForChart = outsidePanel;
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
        heatData = new DefaultXYZDataset();
        // total data points in heat map = bins x frames

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

            int useableFrames = collectionInUse.getTotalSelected();
            int totalHeatMapPts = (int)bins*useableFrames;
            xFrameHeat = new double[totalHeatMapPts];
            yBinHeat = new double[totalHeatMapPts];
            zHeat = new double[totalHeatMapPts];

            xCount=0;
            yCount=0;
            zCount=0;
            frameCount=0;

            for(int j=0; j<bins; j++){
                xFrameHeat[xCount] = frameCount; // frame 0
                yBinHeat[yCount] = j; // bin
                zHeat[zCount] = 0; // intensity of signal
                xCount++;
                yCount++;
                zCount++;
            }

            for(int s=startAt; s<totalDatasets; s++){

                Dataset targetSet = collectionInUse.getDataset(s);
                if (targetSet.getInUse()){

                    frameCount++;
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
            diff = tempK.getY(ss).doubleValue() - mean; // tempK average value per bin
            diff2 = diff*diff;
            top.addValue(diff2*diff2);
            bottom.addValue(diff2);

            xFrameHeat[xCount] = frameCount; // frame 0
            yBinHeat[yCount] = ss; // bin
            zHeat[zCount] = diff2; // intensity of signal
            xCount++;
            yCount++;
            zCount++;
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


        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);

        final NumberAxis domainAxis = new NumberAxis("frame");
        final NumberAxis rangeAxis = new NumberAxis(range_type);
        rangeAxis.setAutoRangeIncludesZero(false);

        final XYPlot plot = chart.getXYPlot();
        plot.setRangeAxis(rangeAxis);

        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);
        domainAxis.setAutoRangeStickyZero(false);
        rangeAxis.setAutoRange(false);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);
        rangeAxis.setAutoRangeStickyZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setOutlineVisible(false);

        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShape(Constants.Ellipse4);

        plot.setRenderer(0, renderer1);

//        renderer1.setSeriesLinesVisible(i, false);
//        renderer1.setSeriesPaint(i, tempData.getColor());
//        renderer1.setSeriesShapesFilled(i, tempData.getBaseShapeFilled());
//        renderer1.setSeriesVisible(i, tempData.getInUse());
//        renderer1.setSeriesOutlineStroke(i, tempData.getStroke());

        frame = new ChartFrame("SC\u212BTTER \u2263 SIMILARITY PLOT", chart);
        frame.pack();
        panelForChart.add(frame.getContentPane());
        panelForChart.validate();
        //frame.setVisible(true);

/*
        frame.getChartPanel().setDisplayToolTips(true);
        frame.getChartPanel().getChart().getXYPlot().getRenderer(0).setBaseToolTipGenerator(new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset xyDataset, int i, int i2) {
                return (String) xyDataset.getSeriesKey(i);
            }
        });
*/
        //frame.getChartPanel().setRangeZoomable(false);
        //frame.getChartPanel().setDomainZoomable(false);

        //frame.pack();
        //frame.setVisible(true);
    }

    private int getDigits(double qvalue) {
        String toText = Double.toString(qvalue);
        int integerPlaces = toText.indexOf('.');
        int decimalPlaces;

        String[] temp = toText.split("\\.0*");
        decimalPlaces = (temp.length == 2) ? temp[1].length() : (toText.length() - integerPlaces -1);

        return decimalPlaces;
    }

    public String formattedQ(double qvalue, int numberOfDigits) {
        String numberToPrint ="";
        switch(numberOfDigits){
            case 7: numberToPrint = String.format(Locale.US, "%.6E", qvalue);
                break;
            case 8: numberToPrint = String.format(Locale.US, "%.7E", qvalue);
                break;
            case 9: numberToPrint = String.format(Locale.US, "%.8E", qvalue);
                break;
            case 10: numberToPrint = String.format(Locale.US, "%.9E", qvalue);
                break;
            case 11: numberToPrint = String.format(Locale.US,"%.10E", qvalue);
                break;
            case 12: numberToPrint = String.format(Locale.US, "%.11E", qvalue);
                break;
            case 13: numberToPrint = String.format(Locale.US, "%.12E", qvalue);
                break;
            case 14: numberToPrint = String.format(Locale.US, "%.13E", qvalue);
                break;
            default: numberToPrint = String.format(Locale.US,"%.6E", qvalue);
                break;
        }
        return numberToPrint;
    }


}
