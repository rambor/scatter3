package version3;

import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimilarityPlot extends SwingWorker<Void, Integer> {

    private JFreeChart chart;
    private ChartFrame frame;
    private ChartPanel chartPanel;
    private RatioSimilarityTest similarityPlotObject;
    private Collection simCollection;

    private DefaultXYZDataset residualDataset;
    public ArrayList<ResidualDifferences> residualDifferencesModel;
//    public ArrayList<XYTextAnnotation> residualsLabels;
    public ArrayList<Integer> frameIndices;
    private double maxResidualDataset;
    private double minResidualDataset;
    private int totalDatasetsInUse;
    int min, max;

    public JList samplesList;
    private JLabel status;

    private Collection samplesCollection;
    private Collection buffersCollection;
    private AtomicInteger counter = new AtomicInteger(0);

    private JProgressBar mainStatus;
    private XYSeries buffer = new XYSeries("buffer");
    private XYSeries bufferError = new XYSeries("buffer error");


    public SimilarityPlot(Collection sampleCollection, Collection bufferCollection, JLabel status, JProgressBar bar){

        this.samplesCollection = sampleCollection;
        this.buffersCollection = bufferCollection;

        mainStatus = bar;
        this.status = status;
    }


    @Override
    protected Void doInBackground() throws Exception {

        int select = buffersCollection.getTotalSelected();
        int sampleSize = samplesCollection.getTotalSelected();

        status.setText("compiling buffers");
        this.makeBuffer();

        status.setText("compiling samples");
        this.makeSamples();

        calculateResiduals();

        mainStatus.setValue(0);
        mainStatus.setIndeterminate(true);
        status.setText("Making Plot");
        this.makeChart();
        mainStatus.setIndeterminate(false);
        return null;
    }


    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size()-1);
        //mainStatus.setValue((int) (i / (double) totalInSamples * 100));
        mainStatus.setValue(i);
        super.process(chunks);
    }


    @Override
    protected void done() {
        // make similarity plot
        try {
            get();
            status.setText("FINISHED");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * residuals chart
     */
    private void makeChart(){
        this.createResidualsDataset();

        min = frameIndices.get(0);
        max = frameIndices.get(frameIndices.size()-1);

        NumberAxis xAxis = new NumberAxis("frame");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setRange(min, max);

        NumberAxis yAxis = new NumberAxis("frame");
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setRange(min, max);

        XYPlot plot = new XYPlot(residualDataset, xAxis, yAxis, null);
        XYBlockRenderer r = new XYBlockRenderer();

//        for(int i=0; i<residualsLabels.size(); i++){
//            plot.addAnnotation(residualsLabels.get(i));
//        }

        // max for DurbinWatson is 4
        SpectrumPaintScale ps = new SpectrumPaintScale(minResidualDataset - (0.07*minResidualDataset), maxResidualDataset+0.1*maxResidualDataset);
        r.setPaintScale(ps);
        r.setBlockHeight(1.0f);
        r.setBlockWidth(1.0f);
        plot.setRenderer(r);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
//        plot.setDomainCrosshairVisible(false);
//        plot.setRangeCrosshairVisible(false);


        chart = new JFreeChart(
                "Residuals Similarity Plot",
                JFreeChart.DEFAULT_TITLE_FONT,
                plot,
                false
        );

        NumberAxis scaleAxis = new NumberAxis("Durbin-Watson Scale");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);

        PaintScaleLegend legend = new PaintScaleLegend(ps, scaleAxis);
        legend.setSubdivisionCount(128);
        legend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(20);
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setBackgroundPaint(Color.WHITE);
        chart.addSubtitle(legend);
        chart.setBackgroundPaint(Color.white);
    }


    private void makeBuffer(){
        mainStatus.setIndeterminate(true);
        mainStatus.setStringPainted(true);
        mainStatus.setString("Reducing Buffer");
        try {
            //total = buffersCollection.getDatasetCount();
            int select = buffersCollection.getTotalSelected();

            if (select == 1){
                buffer = buffersCollection.getLast().getAllData();
                bufferError = buffersCollection.getLast().getAllDataError();
            } else if (select > 1) {
                ArrayList<XYSeries> stuff = createMedianAverageXYSeries(buffersCollection);
                buffer = stuff.get(0);
                bufferError = stuff.get(1);
            } else {
                throw new Exception("Must have at least one buffer selected: ");
            }
        } catch (Exception ex) {
            status.setText("Must have at least one buffer" + ex.getMessage().toString());
        }
        mainStatus.setIndeterminate(false);
        mainStatus.setStringPainted(false);
    }

    public void createFrame(){


        chartPanel = new ChartPanel(chart){
            @Override
            public void restoreAutoBounds(){
                System.out.println("min " + min + " max " + max);
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);
                super.getChart().getXYPlot().getRangeAxis().setRange(min, max);
                super.getChart().getXYPlot().getDomainAxis().setRange(min, max);
            }
        };

        frame = new ChartFrame("SC\u212BTTER \u2263 SIMILARITY PLOT", chartPanel.getChart());
        frame.getChartPanel().setDisplayToolTips(true);


//        frame.getChartPanel().getChart().getXYPlot().getRenderer(0).setBaseToolTipGenerator(new XYToolTipGenerator() {
//            @Override
//            public String generateToolTip(XYDataset xyDataset, int i, int i2) {
//                return (String) xyDataset.getSeriesKey(i);
//            }
//        });

//        frame.getChartPanel().setRangeZoomable(false);
//        frame.getChartPanel().setDomainZoomable(false);
        frame.getChartPanel().setMouseZoomable(false);
        frame.getChartPanel().setHorizontalAxisTrace(true);
        frame.getChartPanel().setVerticalAxisTrace(true);
        // add mouse listener for getting values

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void makeSamples(){
        simCollection = new Collection();
        frameIndices = new ArrayList<>();

        Dataset tempDataset;
        XYDataItem tempXY;

        XYSeries ratio = new XYSeries("ratio"), tempData;

        int total = samplesCollection.getDatasetCount();

        int totalXY, bufferIndex;

        mainStatus.setMaximum(100);
        mainStatus.setValue(0);
        mainStatus.setStringPainted(true);
        mainStatus.setString("Processing");

        for(int i=0; i < total; i++){ // iterate over all frames

            tempDataset = samplesCollection.getDataset(i);
            if (tempDataset.getInUse()){
                ratio.clear();
                tempData = tempDataset.getAllData();
                totalXY = tempData.getItemCount();

                // create XY Series using ratio to buffer then integrate
                for(int q=0; q < totalXY; q++){
                    tempXY = tempData.getDataItem(q);
                    bufferIndex = buffer.indexOf(tempXY.getX());
                    if (bufferIndex >= 0){
                        ratio.add(tempXY.getX(), tempXY.getYValue()/buffer.getY(bufferIndex).doubleValue());
                    }
                }

                // if number of points is less than 100, skip
                if (ratio.getItemCount() > 100){
                    // integrate
                    Dataset dataInUse = samplesCollection.getDataset(i);

                    ArrayList<XYSeries> subtraction = subtract(dataInUse.getAllData(), dataInUse.getAllDataError(), buffer, bufferError);
                    simCollection.addDataset(new Dataset(subtraction.get(0), subtraction.get(1), Integer.toString(i), i+1, false));
                    frameIndices.add(i);
                } else {
                    System.out.println("TOO FEW IN RATIO : Frame => " + i);
                }

            }
            mainStatus.setValue((int) (i / (double) total * 100));
        }

        totalDatasetsInUse = simCollection.getDatasetCount();

        Toolkit.getDefaultToolkit().beep();
        mainStatus.setMinimum(0);
        mainStatus.setStringPainted(false);
        mainStatus.setValue(0);
    }


    private ArrayList<XYSeries> subtract(XYSeries sample, XYSeries sampleError, XYSeries buffer, XYSeries bufferError){
        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();
        // for each element in sample collection, do subtraction

        XYSeries subData;
        XYSeries subError;
        XYDataItem tempDataItem;

        int tempTotal, indexOf;
        double qValue, yValue, eValue;

        tempTotal = sample.getItemCount();

        subData = new XYSeries("subtracted");
        subError = new XYSeries("errorSubtracted");
        //Subtract and add to new data
        double maxQValueInBuffer = buffer.getMaxX();

        QLOOP:
        for(int q=0; q<tempTotal; q++){
            tempDataItem = sample.getDataItem(q);
            qValue = tempDataItem.getXValue();
                     /*
                      * check to see if in buffer
                      */
            indexOf = buffer.indexOf(qValue);
            yValue = sampleError.getY(q).doubleValue();

            if (indexOf > -1){
                subData.add(qValue, tempDataItem.getYValue() - buffer.getY(indexOf).doubleValue() );

                eValue = bufferError.getY(indexOf).doubleValue();
                subError.add(qValue, Math.sqrt(yValue*yValue + eValue*eValue));

            } else { // interpolate
                // interpolation requires at least two values on left or right side of value of interest
                // if not, skip value
                if (qValue < maxQValueInBuffer) {
                    Double[] results = Functions.interpolate(buffer, qValue, 1);
                    Double[] sigmaResults = Functions.interpolateSigma(bufferError, qValue);
                    //returns unlogged data
                    eValue = sigmaResults[1];

                    subData.add(qValue, results[1]);
                    subError.add(qValue, Math.sqrt(yValue * yValue + eValue * eValue));
                }
            }
        }

        returnMe.add(subData);
        returnMe.add(subError);

        return returnMe;
    }


    /**
     * create median and average dataset only on the qmin and max that are in common
     * all data contains all positive values.
     *
     * @param collection
     * @return
     */
    private ArrayList<XYSeries> createMedianAverageXYSeries(Collection collection){
        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();

        // calculate Average and Median for set

        ArrayList<XYSeries> median_reduced_set = StatMethods.medianDatasets(collection);
        ArrayList<XYSeries> averaged = StatMethods.weightedAverageDatasets(collection);

        String name = "median_set";

        XYSeries medianAllData = null;
        XYSeries medianAllDataError = null;

        try {
            medianAllData = (XYSeries) median_reduced_set.get(0).clone();
            medianAllData.setKey(name);
            medianAllDataError = (XYSeries) median_reduced_set.get(1).clone();
            medianAllDataError.setKey(name);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        returnMe.add(medianAllData);          // 0
        returnMe.add(medianAllDataError);     // 1
        returnMe.add(averaged.get(0));        // 2
        returnMe.add(averaged.get(1));        // 3

        return returnMe;
    }



    private void createResidualsDataset() {

        residualDataset = new DefaultXYZDataset();

        maxResidualDataset = 0;
        minResidualDataset = 100;

        int N = totalDatasetsInUse;

        int index = 0;
        for (int i = 0; i < N; i++) { // row
            double[][] data = new double[3][N];

            for (int j = 0; j < N; j++) { // column
                data[0][j] = frameIndices.get(i)*1;
                data[1][j] = frameIndices.get(j)*1;
                data[2][j] = 0;
            }

            int nonZeroCol = i + 1;
            for(int j=nonZeroCol; j < N; j++){

                ResidualDifferences temp = residualDifferencesModel.get(index);
                double tempStat = temp.getStatistic();
                data[2][j] = tempStat;


                if (tempStat > maxResidualDataset){
                    maxResidualDataset = temp.getStatistic();
                }

                if (tempStat < minResidualDataset){
                    minResidualDataset = temp.getStatistic();
                }
                index++;
            }

            residualDataset.addSeries("Series" + i, data);
        }
    }


    private static class SpectrumPaintScale implements PaintScale {

        private static final float H1 = 0f;
        private static final float H2 = 1f;
        private final double lowerBound;
        private final double upperBound;

        public SpectrumPaintScale(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public double getLowerBound() {
            return lowerBound;
        }

        @Override
        public double getUpperBound() {
            return upperBound;
        }

        @Override
        public Paint getPaint(double value) {
            float saturation =1f;
            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
            float scaledH = H1 + scaledValue * (H2 - H1);

            // HSB white is s: 0, b: 1
            if (value == getLowerBound() || value == getUpperBound()){
                saturation = 0;
                //scaledH = H1 + (float) (getLowerBound() / (getUpperBound() - getLowerBound())) * (H2 - H1);
                scaledH = 0;
            } else if (value < getLowerBound() || value > getUpperBound()){
                saturation = 0;
                scaledH = 0;
            }

            //Color tempColor = Color.getHSBColor(scaledH, saturation, 1f);
            return Color.getHSBColor(scaledH, saturation, 1f);
        }
    }

//    private void calculateResiduals(){
//
//        //ratioModels = new ArrayList<>();
//        residualDifferencesModel = new ArrayList<>();
//        int totalDatasets = simCollection.getDatasetCount();
//        mainStatus.setValue(0);
//        mainStatus.setMaximum(totalDatasets*(totalDatasets-1)/2);
//        //mainStatus.setIndeterminate(true);
//                ScheduledExecutorService diffExecutor = Executors.newScheduledThreadPool(3);
//                status.setText("Status : Calculating Residual Differences...Please wait");
//
//                List<Future<ResidualDifferences>> residualFutures = new ArrayList<>();
//
//                // Residual Differences
//                int order=0;
//
//
//                for(int i=0; i < totalDatasets; i++){
//
//                    if (simCollection.getDataset(i).getInUse()){
//                        XYSeries ref = simCollection.getDataset(i).getAllData();
//                        int refIndex = simCollection.getDataset(i).getId();
//
//                        List<XYDataItem> refIntensities = Collections.synchronizedList(new ArrayList<>(ref.getItemCount()));
//                        // all models are initialized with same value
//                        for(int j = 0; j< ref.getItemCount(); j++){
//                            refIntensities.add(ref.getDataItem(j));
//                        }
//                        final List<XYDataItem> THREAD_SAFE_LIST=Collections.unmodifiableList(refIntensities);
//
//                        int next = i+1;
//                        for(int j=next; j<totalDatasets; j++){
//
//                            Dataset tar = simCollection.getDataset(j);
//                            if (tar.getInUse()){
//                                Future<ResidualDifferences> dfuture = diffExecutor.submit(new CallableResidualDifferences(
//                                        THREAD_SAFE_LIST,
//                                        tar.getAllData(),
//                                        tar.getAllDataError(),
//                                        0.0131,
//                                        0.178,
//                                        refIndex,
//                                        tar.getId(),
//                                        12,
//                                        order
//                                ));
//
//                                residualFutures.add(dfuture);
//                                order++;
//                            }
//                        }
//                    }
//                }
//
//
//                // int completed=0;
//                for(Future<ResidualDifferences> fut : residualFutures){
//                    try {
//                        // because Future.get() waits for task to get completed
//                        residualDifferencesModel.add(fut.get());
//                        //update progress bar
//                        // completed++;
//                        // publish(completed);
//                        this.increment();
//                    } catch (InterruptedException | ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                diffExecutor.shutdown();
//
//                try {
//                    diffExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//                Collections.sort(residualDifferencesModel, new Comparator<ResidualDifferences>() {
//                    @Override public int compare(ResidualDifferences p1, ResidualDifferences p2) {
//                        return p1.order - p2.order; // Ascending
//                    }
//                });
//
//    }

    private void calculateResiduals(){

        //ratioModels = new ArrayList<>();
        residualDifferencesModel = new ArrayList<>();
        int totalDatasets = simCollection.getDatasetCount();
        mainStatus.setValue(0);
        mainStatus.setMaximum(totalDatasets*(totalDatasets-1)/2);
        //mainStatus.setIndeterminate(true);

        status.setText("Status : Calculating Residual Differences...Please wait");

        // Residual Differences
        int order=0;

        for(int i=0; i < totalDatasets; i++){

            if (simCollection.getDataset(i).getInUse()){
                XYSeries ref = simCollection.getDataset(i).getAllData();
                int refIndex = simCollection.getDataset(i).getId();

                List<XYDataItem> refIntensities = Collections.synchronizedList(new ArrayList<>(ref.getItemCount()));
                // all models are initialized with same value
                for(int j = 0; j< ref.getItemCount(); j++){
                    refIntensities.add(ref.getDataItem(j));
                }
                final List<XYDataItem> THREAD_SAFE_LIST=Collections.unmodifiableList(refIntensities);

                int next = i+1;
                for(int j=next; j<totalDatasets; j++){

                    Dataset tar = simCollection.getDataset(j);

                    residualDifferencesModel.add(new ResidualDifferences(
                            ref,
                            tar.getAllData(),
                            tar.getAllDataError(),
                            0.0131,
                            0.178,
                            12,
                            refIndex,
                            tar.getId(),
                            order));
                    this.increment();
                }
            }
        }


        Collections.sort(residualDifferencesModel, new Comparator<ResidualDifferences>() {
            @Override public int compare(ResidualDifferences p1, ResidualDifferences p2) {
                return p1.order - p2.order; // Ascending
            }
        });

    }

    public synchronized void increment() {
        mainStatus.setValue(counter.incrementAndGet());
    }
}
