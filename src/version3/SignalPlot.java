package version3;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import sun.misc.Signal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by robertrambo on 18/01/2016.
 */
public class SignalPlot extends SwingWorker<Void, Void> {

    private JFreeChart chart;
    private ChartFrame frame;
    private ChartPanel chartPanel;
    private XYLineAndShapeRenderer renderer1;
    private XYLineAndShapeRenderer rightRenderer;
    public JList samplesList;
    private JLabel status;

    private Collection samplesCollection;
    private Collection buffersCollection;
    private XYSeriesCollection plotMe;
    private XYSeriesCollection plotRg;

    private JProgressBar mainStatus;
    private XYSeries buffer = new XYSeries("buffer");
    private XYSeries bufferError = new XYSeries("buffer error");
    private boolean useRg = false;
    private double threshold;

    public SignalPlot(Collection sampleCollection, Collection bufferCollection, JLabel status, boolean useRg, JProgressBar bar, double threshold){
        this.samplesCollection = sampleCollection;
        this.buffersCollection = bufferCollection;
        plotMe = new XYSeriesCollection();
        plotRg = new XYSeriesCollection();

        this.useRg = useRg;
        mainStatus = bar;
        this.status = status;
        this.threshold = threshold;

    }


    private void writeData(){
        try {
            if (plotMe.getSeriesCount() == 0){
                throw new Exception("Not enough points in ratio, check q-vales - must match: ");
            }
        } catch (Exception ex) {
            status.setText("Error " + ex.getMessage().toString());
        }

        // write signal plot to file
        ArrayList<String> linesToFile = new ArrayList<String>();
        linesToFile.add("# REMARK file_index integrated_area_ratio file_name");
        // create plot using plotMe

        if (useRg){
            linesToFile.set(0,"# REMARK file_index integrated_area_ratio R_g file_name");
        }

        // go over each series and write out
        for(int w=0; w<plotMe.getSeriesCount(); w++){
            XYSeries tempSeries = plotMe.getSeries(w);
            if (useRg){

                XYSeries tempRgSeries = plotRg.getSeries(w);
                linesToFile.add(String.format("%d %.5f %.3f %s", w, tempSeries.getY(0).doubleValue(), tempRgSeries.getY(0).doubleValue(), tempSeries.getKey()));

            } else {
                linesToFile.add(String.format("%d %.5f %s", w, tempSeries.getY(0).doubleValue(), tempSeries.getKey()));
            }
        }

        Functions.writeLinesToFile(linesToFile, "signal_plot.txt", samplesCollection.getWORKING_DIRECTORY_NAME());
    }


    /**
     * creates XYSeries collections used for plotting
     */
    private void makeSamples(){
        Dataset tempDataset;
        XYDataItem tempXY;

        XYSeries ratio = new XYSeries("ratio"), tempData;

        int total = samplesCollection.getDatasetCount();
        int select=0, totalXY, bufferIndex;
        int seriesCount=0;
        double area;
        double[] izeroRg;

        mainStatus.setValue(0);
        mainStatus.setStringPainted(true);
        mainStatus.setString("Processing");

        for(int i=0;i<total; i++){

            tempDataset = samplesCollection.getDataset(i);
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
                plotMe.addSeries(new XYSeries(dataInUse.getFileName()));
                area = Functions.trapezoid_integrate(ratio);
                plotMe.getSeries(seriesCount).add(i, area);

                if (useRg){ // make double plot if checked

                    status.setText("auto-Rg for : " + dataInUse.getFileName());
                    plotRg.addSeries(new XYSeries(dataInUse.getFileName()));
                    if (area > threshold){
                        ArrayList<XYSeries> subtraction = subtract(dataInUse.getAllData(), dataInUse.getAllDataError(), buffer, bufferError);
                        izeroRg = Functions.autoRgTransformIt(subtraction.get(0), subtraction.get(1), 1);
                        plotRg.getSeries(seriesCount).add(i,izeroRg[1]);
                    } else {
                        plotRg.getSeries(seriesCount).add(i,0);
                    }

                }
                seriesCount++;
            }

            mainStatus.setValue((int) (i / (double) total * 100));
        }

        Toolkit.getDefaultToolkit().beep();
        mainStatus.setMinimum(0);
        mainStatus.setStringPainted(false);
        mainStatus.setValue(0);
    }


    private void makeBuffer(){
        mainStatus.setIndeterminate(true);
        mainStatus.setStringPainted(true);
        mainStatus.setString("Reducing Buffer");
        try {
            //total = buffersCollection.getDatasetCount();
            int select = buffersCollection.getTotalSelected();

            int ref=0;

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
//                    count = 0;
//                    referenceQ = buffer.getX(count).doubleValue();
//                    find first value in reference greater than targetData.getX
//                    while (referenceQ < qValue) {
//                        count++;
//                        referenceQ = buffer.getX(count).doubleValue();
//                    }
//
//                    if (count < 2) {
//                       break QLOOP;
//                    }
                System.out.println("Interpolating Value at " + qValue);
                Double[] results = Functions.interpolate(buffer, bufferError, qValue, 1);
                Double[] sigmaResults = Functions.interpolateSigma(bufferError, qValue);
                //returns unlogged data
                eValue = sigmaResults[1];

                subData.add(qValue, results[1]);
                subError.add(qValue, Math.sqrt(yValue*yValue + eValue*eValue));
            }
        }

        returnMe.add(subData);
        returnMe.add(subError);

        return returnMe;
    }

    public void setSampleJList(JList list){
        this.samplesList = list;
    }

    public void makePlot(){

        chart = ChartFactory.createXYLineChart(
                "Signal Plot",            // chart title
                "sample",                 // domain axis label
                "signal",                 // range axis label
                plotMe,                   // data
                PlotOrientation.VERTICAL,
                false,                    // include legend
                true,                     // toolTip
                false
        );

        chartPanel = new ChartPanel(chart){
            @Override
            public void restoreAutoBounds(){
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);
                int seriesCount = super.getChart().getXYPlot().getDataset(0).getSeriesCount();
                int maxIndex;
                double min = 1000;
                double max = -10;
                double minX = 10;
                double maxX = 0;
                double tempYMin;
                double tempXMin;

                for (int i=0; i < seriesCount; i++){
                    tempYMin = super.getChart().getXYPlot().getDataset(0).getYValue(i, 0);
                    tempXMin = super.getChart().getXYPlot().getDataset(0).getXValue(i, 0);

                    if (tempYMin < min){
                        min = tempYMin;
                    }
                    if (tempYMin > max){
                        max = tempYMin;
                    }
                    if (tempXMin < minX) {
                        minX = tempXMin;
                    }
                    if (tempXMin > maxX) {
                        maxX = tempXMin;
                    }
                }
                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.02), max+Math.abs(0.02*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.02), maxX+Math.abs(0.02*maxX));
            }
        };


        final XYPlot plot = chart.getXYPlot();
    /*
      set domain and range axis
     */
        final NumberAxis domainAxis = new NumberAxis("Sample ID");
        final NumberAxis rangeAxis = new NumberAxis("Integral of Ratio to Background");
        final NumberAxis rangeAxisRight = new NumberAxis("Rg");

        rangeAxis.setRange(plotMe.getRangeLowerBound(true) - 0.01*plotMe.getRangeLowerBound(true), plotMe.getRangeUpperBound(true) + 0.02*plotMe.getRangeUpperBound(true));
        rangeAxis.setAutoRangeIncludesZero(false);

        plot.setRangeAxis(rangeAxis);

        plot.setDataset(0, plotMe);
        plot.mapDatasetToRangeAxis(0, 0);//1st dataset to 1st y-axis
        double negativePointSize, pointSize;

        if (!(plotRg == null) && (plotRg.getSeriesCount() > 0)){

            String quoteR = "Rg Å";
            rangeAxisRight.setLabel(quoteR);
            rangeAxisRight.setLabelFont(new Font("Times", Font.BOLD, 20));
            rangeAxisRight.setLabelPaint(new Color(51, 153, 255));
            rangeAxisRight.setAutoRange(true);
            rangeAxisRight.setAutoRangeIncludesZero(false);
            rangeAxisRight.setAutoRangeStickyZero(false);

            //double minRg = plotRg.getRangeLowerBound(true);
            rangeAxisRight.setRange(0, plotRg.getRangeUpperBound(true) + 0.02*plotRg.getRangeUpperBound(true));

            rightRenderer = new XYLineAndShapeRenderer();

            plot.setDataset(1,plotRg);
            plot.mapDatasetToRangeAxis(1, 1); //2nd dataset to 2nd y-axis
            plot.setRangeAxis(1, rangeAxisRight);

            for (int i=0; i < plotRg.getSeriesCount(); i++) {
                // go over each series
                pointSize = 9;
                negativePointSize = -0.5*pointSize;
                rightRenderer.setSeriesShape(i, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
                rightRenderer.setSeriesLinesVisible(i, false);
                rightRenderer.setSeriesPaint(i, new Color(51, 153, 255));
                rightRenderer.setSeriesShapesFilled(i, true);
                rightRenderer.setSeriesOutlinePaint(i, new Color(51, 153, 255));
                rightRenderer.setSeriesOutlineStroke(i, new BasicStroke(1.0f));
            }

            plot.setRenderer(1, rightRenderer);       //render as a line
        }

        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShape(new Ellipse2D.Double(-0.5*6, -0.5*6.0, 6, 6));

        for (int i=0; i < plotMe.getSeriesCount(); i++) {
            // go over each series
            pointSize = 9;
            negativePointSize = -0.5*pointSize;
            renderer1.setSeriesShape(i, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
            renderer1.setSeriesLinesVisible(i, false);
            renderer1.setSeriesPaint(i, new Color(36, 46, 54));
            renderer1.setSeriesShapesFilled(i, false);
            renderer1.setSeriesOutlinePaint(i, new Color(36, 46, 54));
            renderer1.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
        }

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
        frame.getChartPanel().setHorizontalAxisTrace(true);

        // add mouse listener for getting values

        //frame.getChartPanel().addKeyListener();
        frame.getChartPanel().addMouseListener(new MouseMarker(frame.getChartPanel(), samplesList));
        frame.getChartPanel().addChartMouseListener(new ChartMouseListener() {
            private Double markerStart = Double.NaN;
            private Double markerEnd = Double.NaN;

            @Override
            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
                System.out.println("Setting frame min ");
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {

            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    protected Void doInBackground() throws Exception {

        status.setText("compiling buffers");
        this.makeBuffer();
        status.setText("compiling samples");
        this.makeSamples();
        status.setText("Writing Signal Plot");
        this.writeData();
        status.setText("Making Plot");
        this.makePlot();
        mainStatus.setIndeterminate(false);
        return null;
    }

    private final static class MouseMarker extends MouseAdapter {
        private Marker marker;
        private Double markerStart = Double.NaN;
        private Double markerEnd = Double.NaN;
        private final XYPlot plot;
        private final JFreeChart chart;
        private  final ChartPanel panel;
        private JList samplesList;


        public MouseMarker(ChartPanel panel, JList samplesList) {
            this.samplesList = samplesList;
            this.panel = panel;
            this.chart = panel.getChart();
            this.plot = (XYPlot) chart.getPlot();
        }

        private void updateMarker(){
            if (marker != null){
                plot.removeDomainMarker(marker, Layer.BACKGROUND);
            }
            if (!( markerStart.isNaN() && markerEnd.isNaN())){
                if ( markerEnd > markerStart){
                    marker = new IntervalMarker(markerStart, markerEnd);
                    marker.setPaint(new Color(0xDD, 0xFF, 0xDD, 0x80));
                    marker.setAlpha(0.5f);
                    plot.addDomainMarker(marker,Layer.BACKGROUND);
                }
            }
        }

        private Double getPosition(MouseEvent e){
            Point2D p = panel.translateScreenToJava2D( e.getPoint());
            Rectangle2D plotArea = panel.getScreenDataArea();
            XYPlot plot = (XYPlot) chart.getPlot();
            return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            markerEnd = getPosition(e);

            int size = samplesList.getModel().getSize();
            int trueStart = markerStart.intValue();
            int trueEnd = markerEnd.intValue();

            // set everything within range to true, everything else false
            for(int i=0; i<size; i++){
                // sampleFilesModel.get(i).isSelected()
                if ((i < trueStart) || (i > trueEnd)){
                    ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(false);
                } else {
                    ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(true);
                }
            }
            samplesList.repaint();
            updateMarker();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            markerStart = getPosition(e);

            // if key pressed
        }


    }


}
