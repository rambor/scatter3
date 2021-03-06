package version3;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.io.BufferedWriter;
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

    private XYSeries baseLine;
    private double qmin, qmax, bins, binWidth;
    private ArrayList<Number> keptQvalues;
    private SortedSet<Number> commonQValues;
    private JLabel status;
    private JProgressBar bar;
    private boolean useKurtosis = false;
    private boolean useShapiro = false;
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
    private int binSize = 11;
    private JPanel panelForChart;
    private String directory;

    public Similarity(){
        commonQValues = new TreeSet<>();
        keptQvalues = new ArrayList<>();
        collectionOfkurtosisPerFrame = new XYSeriesCollection();
        averageValuePerBin = new XYSeriesCollection();
        collections = new ArrayList<>();
    }


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

        //averageValuePerBin.removeAllSeries();
        this.calculate();

        XYPlot plot = chart.getXYPlot();
        plot.setDataset(plottedCollection);

        int totalDatasetToAnalyze = collections.size();
        int count=0;

        if (useKurtosis){
            range_type="excess kurtosis";
            makePlot();

            for (int i=0; i<totalDatasetToAnalyze; i++){
                if(collections.get(i).isSelected()){
                    writeCollectionToFile(collections.get(i).getName() + "_kurtosis", collectionOfkurtosisPerFrame.getSeries(count));
                    count++;
                }
            }
        } else if (useVr){
            range_type="volatility-ratio";
            makePlot();

            for (int i=0; i<totalDatasetToAnalyze; i++){
                if(collections.get(i).isSelected()){
                    writeCollectionToFile(collections.get(i).getName() + "_vr", collectionOfkurtosisPerFrame.getSeries(count));
                    count++;
                }
            }
        } else if (useShapiro){
            range_type="unweighted Shapiro-Wilk";
            makePlot();
            for (int i=0; i<totalDatasetToAnalyze; i++){
                if(collections.get(i).isSelected()){
                    writeCollectionToFile(collections.get(i).getName() + "_sw", collectionOfkurtosisPerFrame.getSeries(count));
                    count++;
                }
            }
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
        this.binSize = (int)bins;
        this.binWidth = (qmax-qmin)/bins;
        System.out.println("QMIN to QMAX: " + qmin + " < " + qmax + " BINWIDTH => " + binWidth);
        this.cpus = cpus;
        this.panelForChart = outsidePanel;
    };

    public void setParametersNoPanel(double qmin, double qmax, double bins, int cpus){
        this.qmin = qmin;
        this.qmax = qmax;
        this.bins = bins;
        this.binSize = (int)bins;
        this.binWidth = (qmax-qmin)/bins;
        System.out.println("QMIN to QMAX: " + qmin + " < " + qmax + " BINWIDTH => " + binWidth);
        this.cpus = cpus;
    };

    /**
     *
     */
    public void calculate(){
        // for each bin, collect values, do outlier rejection and average
        collectionOfkurtosisPerFrame.removeAllSeries();
        heatData = new DefaultXYZDataset();
        // total data points in heat map = bins x frames

        int totalDatasetToAnalyze = collections.size();

        System.out.println("Collections to Analyze : " + totalDatasetToAnalyze);

        Collection collectionInUse;

        for (int dd=0; dd<totalDatasetToAnalyze; dd++){

            if (collections.get(dd).isSelected()){

                collectionInUse = collections.get(dd).getCollection();
                // first dataset is reference to calculate ratio to
                int totalDatasets = collectionInUse.getDatasetCount();
                Dataset referenceSet =  null;  // set first checked dataset as reference

                // find first data to use that is true, this will be reference
                int startAt=0;
                for(int i=0; i<totalDatasets; i++){
                    referenceSet = collectionInUse.getDataset(i);
                    if (referenceSet.getInUse()){
                        startAt = i+1;
                        break;
                    }
                }

                // determine index that satisfies qmin constraint.
                int startPt=0, endPt;
                int totalPoints = referenceSet.getAllData().getItemCount();
                endPt = totalPoints - 1;
                for (int i=0; i<totalPoints; i++){
                    if (referenceSet.getAllData().getX(i).doubleValue() >= qmin){
                        startPt = i;
                        break;
                    }
                }


                for (int i=0; i<totalPoints; i++){
                    if (referenceSet.getAllData().getX(i).doubleValue() >= qmax){
                        endPt = i;
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
                        double dism = calculateSimFunctionPerSet(referenceSet, startPt, endPt, targetSet);
                        //double dism = createAndAddSquaredRatioResidual(referenceSet.getAllData(), startPt, endPt, targetSet.getAllData());
                        seriesCount++;
                        collectionOfkurtosisPerFrame.getSeries(locale).add(seriesCount, dism);
                        System.out.println(s + " target set " + targetSet.getFileName() + " <=> " + seriesCount + " " + dism);
                    }
                }
            }
        }
        plottedCollection = collectionOfkurtosisPerFrame;
    }


    /**
     * Returns a single value, the dis-similarity of the two datasets.
     *
     * @param referenceSet
     * @param startPt  index of first q-value to use in calculation
     * @param endPt
     * @param targetSet
     * @return double representing kurtosis of the ratio
     */
    public double calculateSimFunctionPerSet(Dataset referenceSet, int startPt, int endPt, Dataset targetSet){

        XYSeries referenceSeries = referenceSet.getAllData();
        XYSeries referenceError = referenceSet.getAllDataError();
        XYSeries ratioSeries;
        YIntervalSeries ratioErrorSeries;
        XYDataItem referenceXY, errorXYref, tarXY, ratioXY;

        ArrayList<Double> valuesPerBin = new ArrayList<>();
        ArrayList<Double> ratioValuesForAveraging = new ArrayList<>();

        double xValue, ratioValue, ratioError, qlower, qupper;
        int locale, intBins = (int)bins;

        //System.out.println(targetSet.getFileName() + " " + averageValuePerBin.getSeriesCount());
        //averageValuePerBin.addSeries(new XYSeries(targetSet.getFileName()));

        ratioSeries = new XYSeries("");
        ratioErrorSeries = new YIntervalSeries("RatioError", false, false);
        ratioValuesForAveraging.clear();

        XYSeries targetSeries = targetSet.getAllData();
        XYSeries targetError = targetSet.getAllDataError();

        // scale the target data to reference data


        //new stuff covariance
//        double sumRef=0, sumTar=0;
//        double countSum = 0.0;
//        for(int i=startPt; i< endPt; i++){
//            referenceXY = referenceSeries.getDataItem(i);
//            locale = targetSeries.indexOf(referenceXY.getX());
//
//            if (locale >= 0) {
//                tarXY = targetSeries.getDataItem(locale);
//                sumRef+=referenceXY.getYValue();
//                sumTar+=tarXY.getYValue();
//                countSum+=1.0;
//            }
//        }
//        double aveRef = sumRef/countSum;
//        double aveTar = sumTar/countSum;
//        sumRef = 0.0d;
//
//        for(int i=startPt; i< endPt; i++){
//            referenceXY = referenceSeries.getDataItem(i);
//            locale = targetSeries.indexOf(referenceXY.getX());
//
//            if (locale >= 0) {
//                tarXY = targetSeries.getDataItem(locale);
//                sumRef+=(referenceXY.getYValue()-aveRef)*(tarXY.getYValue()-aveTar);
//            }
//        }
//        double covariance = 1.0/countSum*sumRef;
        //end covariance
        double scaleSum =0;
        double squaredSum =0;
        for(int i=startPt; i< endPt; i++) {

            referenceXY = referenceSeries.getDataItem(i);
            locale = targetSeries.indexOf(referenceXY.getX());

            if (locale >= 0) {
                tarXY = targetSeries.getDataItem(locale);
                ratioValue = targetError.getY(locale).doubleValue();
                scaleSum += referenceXY.getYValue()*tarXY.getYValue()/(ratioValue*ratioValue);

                ratioValue = tarXY.getYValue()/targetError.getY(locale).doubleValue();
                squaredSum += ratioValue*ratioValue;
            }
        }

        double scaleFactor = scaleSum/squaredSum;
        //double inv_scaleFactor = squaredSum/scaleSum;

        // take ratio to reference state
        for(int i=startPt; i< endPt; i++){

            referenceXY = referenceSeries.getDataItem(i);
            errorXYref = referenceError.getDataItem(i);

            xValue = referenceXY.getXValue();
            locale = targetSeries.indexOf(referenceXY.getX());

            if (locale >= 0){
                tarXY = targetSeries.getDataItem(locale);

                // reference/target
                //ratioValue= (tarXY.getYValue()-referenceXY.getYValue())/referenceXY.getYValue();
                //ratioValue = referenceXY.getYValue()/tarXY.getYValue();
                ratioValue = tarXY.getYValue()/referenceXY.getYValue()*scaleFactor;

                ratioSeries.add(xValue, ratioValue);
                ratioError = ratioValue*Math.sqrt((errorXYref.getYValue()/referenceXY.getYValue()*errorXYref.getYValue()/referenceXY.getYValue()) + (tarXY.getYValue()*tarXY.getYValue()*tarXY.getYValue()*tarXY.getYValue()));
                ratioErrorSeries.add(xValue, ratioValue, ratioValue - ratioError, ratioValue+ratioError);
                ratioValuesForAveraging.add(ratioValue);
            } else {
                // interpolate value
                // make sure reference q values is greater than first two or last two points in sourceSeries
                if ( (xValue > targetSeries.getX(1).doubleValue()) || (xValue < targetSeries.getX(targetSeries.getItemCount()-2).doubleValue()) ){

                    Double[] results =  Functions.interpolateOriginal(targetSeries, targetError, xValue);
                    //target.add(xValue, results[1]);
                    //ratioValue = (results[1]-referenceXY.getYValue())/referenceXY.getYValue();
                    //ratioValue = referenceXY.getYValue()/results[1];
                    ratioValue = results[1]/referenceXY.getYValue();
                    ratioSeries.add(xValue, ratioValue);
                    ratioValuesForAveraging.add(ratioValue);
                }
            }
        }

        // do binning and calculate MAD average for each bin
        int startIndex = 0;
        int totalRatio = ratioSeries.getItemCount();
        double mean;

        XYSeries tempK = new XYSeries("tempK");
        // divide the ratio of data into bins
        // take average per bin
        // then calculate kurtosis of the binned data
        double totalInbins=0;
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
            DescriptiveStatistics keptSet = reduceByMAD(valuesPerBin);
            totalInbins+=valuesPerBin.size();
            // if identical, average per bin is 1.0
            //
            // HeatMap XYZDataset
            // XYBlockrenderer
            //
            // calculate kurtosis per bin?
            if (useKurtosis || useVr){
                mean = keptSet.getMean();
            } else {
                mean = shapiroWilk(keptSet);
            }
            tempK.add(0.5*binWidth + qlower, mean);
        }

        double averageInBin = (totalInbins/(double)intBins);
        System.out.println("average in bin "  + (int)averageInBin);
        // return kurtosis or Vr
        // return covariance;
        if (useKurtosis){
            //return sumOfXYSeries(tempK);
            return calculateKurtosis(tempK);
        } else if (useVr){
            return calculateVr(tempK);
        } else if (useShapiro){
            return sumOfDeviationsFromMedianXYSeries(tempK, averageInBin);
        }

        return 0;
    }



    //
    public double calculateKurtosis(XYSeries reducedBinData){
        double diff, diff2, averageBottom;
        // calculate kurtosis of tempK
        DescriptiveStatistics top = new DescriptiveStatistics();
        DescriptiveStatistics bottom = new DescriptiveStatistics();
        double mean = meanFromXYSeries(reducedBinData);

        for (int ss=0; ss<bins; ss++){
            diff = reducedBinData.getY(ss).doubleValue() - mean; // tempK average value per bin
            diff2 = diff*diff;
            top.addValue(diff2*diff2);
            bottom.addValue(diff2);
        }

        averageBottom = bottom.getMean();
        return (top.getMean()/(averageBottom*averageBottom) - 3);
    }

    /**
     * calculate volatility ratio
     * @param reducedBinData
     * @return
     */
    private double calculateVr(XYSeries reducedBinData){
        //for each q-value create XYSeries
        int upperLimit = (int)bins-1;
        double diff, r_i, r_i_1, denom;
        double sum=0;
        for (int ss=0; ss<upperLimit; ss++){

            r_i = reducedBinData.getY(ss).doubleValue();
            r_i_1 = reducedBinData.getY(ss+1).doubleValue();
            diff = r_i_1 - r_i; // tempK average value per bin
            denom = 0.5*(r_i + r_i_1); // tempK average value per bin
            sum += Math.abs(diff)/denom;
        }
        return sum;
    }

    /**
     *
     * @param useKurtosis
     * @param useVr
     */
    public void setFunction(boolean useKurtosis, boolean useVr, boolean useShapiro){
        if (useKurtosis){
            this.useKurtosis = true;
            this.useShapiro = false;
            this.useVr = false;

        } else if (useVr) {
            this.useKurtosis = false;
            this.useShapiro = false;
            this.useVr = true;

        } else if (useShapiro){
            this.useKurtosis = false;
            this.useShapiro = true;
            this.useVr = false;
        }
    }


    private double sumOfXYSeries(XYSeries series){
        double sum=0;
        int limit = series.getItemCount();
        for (int i=0; i<limit; i++){
            sum += series.getY(i).doubleValue();
        }
        return sum;
    }



    private double sumOfDeviationsFromMedianXYSeries(XYSeries series, double averageInBin){
        int total = series.getItemCount();
        DescriptiveStatistics stats = new SynchronizedDescriptiveStatistics();

        for (int i=0; i<total; i++) {
            stats.addValue(series.getY(i).doubleValue());
        }

        double median = stats.getPercentile(50);
        DescriptiveStatistics deviations = new SynchronizedDescriptiveStatistics();

        //ArrayList<Double> testValues = new ArrayList<>(total);
        //double baseline = 19.197*averageInBin-26.188;
        double baseline = -2.2166 + 0.71027*averageInBin;

        for (int i=0; i<total; i++){ // deviations from
            deviations.addValue(Math.abs(stats.getElement(i) - baseline));
        }
        return stats.getSum();
    }

    /**
     *
     * @param values
     * @return
     */
    private double shapiroWilk(DescriptiveStatistics values){
        //Shapiro-Wilk test equal weights

        int totalKept = (int)values.getN();
        int m_index = totalKept/2;
        if ( (totalKept & 1) == 1 ) { // check if odd
            m_index = (totalKept-1)/2;
        }

        DescriptiveStatistics tempValues = new SynchronizedDescriptiveStatistics();

        // subtract mean, since we are dealing with scaled ratio, the mean should be one
        for(int i=0; i<totalKept; i++){
            tempValues.addValue(values.getElement(i) - 1);
        }

        // squared sum
        double ssSum=0;
        for(int i=0; i<totalKept; i++){
            double value = tempValues.getElement(i);
            ssSum += value*value;
        }

        double[] sorted = tempValues.getSortedValues();
        double rangeSum=0;
        for(int i=0; i < m_index; i++){
            double value = sorted[totalKept-i-1] - sorted[i];
            rangeSum += value;
        }
        return rangeSum*rangeSum/ssSum;
    }


    /**
     * average using median based rejection method
     * @param values
     * @return
     */
    private DescriptiveStatistics reduceByMAD(ArrayList<Double> values){

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
            if (testValues.get(i)*invMAD < 5.0 ) {
                keptValues.addValue(values.get(i));
            }
        }
        // characterize the values in kept.
        return keptValues;
    }


    /**
     * calculates average value of the y-column from XYSeries
     * @param temp
     * @return double mean
     */
    private double meanFromXYSeries(XYSeries temp){
        int total = temp.getItemCount();
        double sum = 0.0;
        for (int i=0; i<total; i++){
            sum += temp.getY(i).doubleValue();
        }
        return sum/(double)total;
    }

    private void makePlot(){

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
        renderer1.setBaseShape(Constants.Ellipse4);
        renderer1.setBaseShapesVisible(true);

        int totalSeries = plottedCollection.getSeriesCount();
        for (int i=0; i<totalSeries; i++){
            renderer1.setSeriesShape(i, Constants.Ellipse8);
        }

        plot.setRenderer(0, renderer1);

//        renderer1.setSeriesLinesVisible(i, false);
//        renderer1.setSeriesPaint(i, tempData.getColor());
//        renderer1.setSeriesShapesFilled(i, tempData.getBaseShapeFilled());
//        renderer1.setSeriesVisible(i, tempData.getInUse());
//        renderer1.setSeriesOutlineStroke(i, tempData.getStroke());

        frame = new ChartFrame("SC\u212BTTER \u2263 SIMILARITY PLOT", chart);
        //frame.pack();
        panelForChart.removeAll();
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


    /**
     * take ratio to first frame
     * bin the ratio as the sum
     * In a moving window, calculate sum of squared residual (-1)
     *
     * @param ref
     * @param target
     */
    private double createAndAddSquaredRatioResidual(XYSeries ref, int startPt, int endPt, XYSeries target){

        int totalRef = ref.getItemCount();
        double sum=0;
        double count=0.0;
        double diff;
//        int currentSet = dataSetsToPlot.getSeriesCount();
//        dataSetsToPlot.addSeries(new XYSeries(currentSet));
        XYSeries binnedRatio = new XYSeries("binratio");

        binLoop:
        for(int i=startPt; i<endPt; i++){

            if ((i+binSize) > endPt){
                break binLoop;
            }

            double[] dataholder = new double[binSize];

            //sum=0;

            for(int b=0; b<binSize; b++){

                int index = i+b;

                if (index > totalRef){
                    break binLoop;
                }

                Number qTest = ref.getX(index);  //
                if (qTest.doubleValue() >= qmin && qTest.doubleValue() <= qmax){
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

            // remove outliers
            double tempsum = sumItWithRejection(dataholder);
            // add sum to plot
            binnedRatio.add(1.0*i, tempsum);
            sum += tempsum*tempsum;
            count += 1.0;
//            dataSetsToPlot.getSeries(currentSet).add(1.0*i, tempsum);
            //System.out.println(startPt + " " + i + " => " + endPt + " " + qmax + " " + sum + " " + tempsum);
        }

        return calculateKurtosis(binnedRatio);

//        return sum*1.0/count;
//        totalBins = dataSetsToPlot.getSeries(0).getItemCount();
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
            } else {
                System.out.println(i + " => " + values[i]);
            }
        }

        return sum;
    }

//    public JFreeChart createChart(XYZDataset dataset) {
//
//        NumberAxis xAxis = new NumberAxis("Theta");
//        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        xAxis.setLowerMargin(0.0);
//        xAxis.setUpperMargin(0.0);
//        xAxis.setAxisLinePaint(Color.white);
//        xAxis.setTickMarkPaint(Color.white);
//
//        NumberAxis yAxis = new NumberAxis("Phi");
//        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        yAxis.setLowerMargin(0.0);
//        yAxis.setUpperMargin(0.0);
//        yAxis.setAxisLinePaint(Color.white);
//        yAxis.setTickMarkPaint(Color.white);
//
//        XYBlockRenderer renderer = new XYBlockRenderer();
//        LookupPaintScale paintScale = new LookupPaintScale(-1, 3, Color.gray);
//
//
//        //code adding paints to paintScale
//        renderer.setPaintScale(paintScale);
//
//        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
//        plot.setBackgroundPaint(Color.lightGray);
//        //plot.setDomainGridlinesVisible(false);
//        plot.setRangeGridlinePaint(Color.white);
//        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
//        plot.setOutlinePaint(Color.blue);
//
//        JFreeChart chart = new JFreeChart(patternType.toUpperCase(), plot);
//        chart.removeLegend();
//        NumberAxis scaleAxis = new NumberAxis("Scale");
//        scaleAxis.setAxisLinePaint(Color.white);
//        scaleAxis.setTickMarkPaint(Color.white);
//        scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 7));
//        scaleAxis.setRange(min, max);
//
//        PaintScaleLegend legend = new PaintScaleLegend(paintScale,
//                scaleAxis);
//        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
//        legend.setAxisOffset(5.0);
//        legend.setMargin(new RectangleInsets(5, 5, 5, 5));
//        legend.setFrame(new BlockBorder(Color.red));
//        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
//        legend.setStripWidth(10);
//        legend.setPosition(RectangleEdge.RIGHT);
//        legend.setBackgroundPaint(new Color(120, 120, 180));
//        chart.addSubtitle(legend);
//        chart.setBackgroundPaint(new Color(180, 180, 250));
//        return chart;
//    }

}
