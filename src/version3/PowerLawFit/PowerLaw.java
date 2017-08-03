package version3.PowerLawFit;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Layer;
import version3.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by xos81802 on 01/08/2017.
 */
public class PowerLaw {

    private JFreeChart chart;
    public JFreeChart residualsChart;
    //private static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 POWER-LAW INTENSITY PLOT", chart);
    private JFrame jframe = new JFrame("");
    private XYPlot plot;
    private XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    private XYSeriesCollection plottedDatasets = new XYSeriesCollection();
    private XYLineAndShapeRenderer renderer1;

    private JPanel panel1;
    private JPanel plotPanel;
    private JSpinner spinnerLow;
    private JSpinner spinnerHigh;
    private JComboBox comboBox1;
    private JButton fitButton;
    private JLabel qminLabel;
    private JLabel qmaxLabel;
    private JPanel residualsPlot;
    private JLabel resultslabel;
    private WorkingDirectory workingDirectory;
    private int lowQSpinnerValue, highQSpinnerValue, upperSpinnerMaxValue;
    private Dataset dataset;
    private double slope, intercept, slopeError, interceptError;
    private XYSeries datatToPlot;
    private ArrayList<Double> qvalues;
    private ArrayList<Integer> indices;
    private XYSeries dataToFit;
    private XYSeries residuals;
    private XYSeries line;
    private boolean isFitted = false;
    private final double firstValue, lastValue;

    private Color inUseColor;

    public PowerLaw(Dataset dataset) {
        this.dataset = dataset;
        inUseColor = dataset.getColor();

        int startAt = dataset.getStart();
        int endAt = dataset.getEnd();

        qvalues = new ArrayList<>();
        datatToPlot = new XYSeries("plotted");
        dataToFit = new XYSeries("fitted");
        residuals = new XYSeries("residuals");
        line = new XYSeries("line");

        indices = new ArrayList<>();
        int counter=0;
        for(int i=startAt; i<endAt; i++){
            XYDataItem temp = dataset.getOriginalPositiveOnlyDataItem(i);
            qvalues.add(temp.getXValue());  // use this to know which qvalue we are using in power law plot
            datatToPlot.add(Math.log(temp.getXValue()), Math.log(temp.getYValue()) );
            dataToFit.add(Math.log(temp.getXValue()), Math.log(temp.getYValue()) );
            indices.add(counter);
            counter++;
        }

        lowQSpinnerValue = 1;
        spinnerLow.setValue(1);
        highQSpinnerValue = datatToPlot.getItemCount();
        upperSpinnerMaxValue = highQSpinnerValue;
        spinnerHigh.setValue(highQSpinnerValue);

        firstValue = datatToPlot.getMinX();
        lastValue = datatToPlot.getMaxX();

        qminLabel.setText("qmin : "+ Double.toString(qvalues.get(0)));
        qmaxLabel.setText("qmax : "+ Double.toString(qvalues.get(highQSpinnerValue-1)));

        fitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { // make powerlaw fit

                isFitted = true;
               fitData();

            }
        });


        spinnerLow.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int valueOfSpinner = (Integer)spinnerLow.getValue();

                if ((valueOfSpinner < 1) ){ //
                    spinnerLow.setValue(1);
                } else { // increment
                    //moving up or down?
                    int direction = valueOfSpinner - lowQSpinnerValue;
                    if (direction > 0) {
                        decrementLow(valueOfSpinner);
                    } else if (direction < 0){
                        incrementLow(valueOfSpinner);
                    }

                    lowQSpinnerValue = valueOfSpinner;
                    qminLabel.setText("qmin : "+ Double.toString(qvalues.get(lowQSpinnerValue-1)));
                    if (isFitted){
                        fitData();
                    }
                }
            }
        });


        spinnerHigh.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int valueOfSpinner = (Integer)spinnerHigh.getValue();

                if ((valueOfSpinner > upperSpinnerMaxValue) ){ //
                    spinnerHigh.setValue(upperSpinnerMaxValue);
                } else { // increment
                    //moving up or down?
                    int direction = valueOfSpinner - highQSpinnerValue;
                    if (direction > 0) {
                        incrementHigh(valueOfSpinner);
                    } else if (direction < 0){
                        decrementHigh(valueOfSpinner);
                    }

                    highQSpinnerValue = valueOfSpinner;
                    qmaxLabel.setText("qmax : "+ Double.toString(qvalues.get(valueOfSpinner-1)));
                    if (isFitted){
                        fitData();
                    }
                }
            }
        });

    }


    private void makePlot(){

        plottedDatasets = new XYSeriesCollection();  // spinners will always modify the plottedDataset series
        plottedDatasets.addSeries(line);
        plottedDatasets.addSeries(dataToFit);
        plottedDatasets.addSeries(datatToPlot);  // should add an empty Series


        chart = ChartFactory.createXYLineChart(
                "PowerLaw Plot",                     // chart title
                "log[q]",                         // domain axis label
                "log[I(q)]",                      // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.setTitle("");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);


        plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("log[q]");
        final NumberAxis rangeAxis = new NumberAxis("log Intensity");
        String quote = "log[q (\u212B\u207B\u00B9)]";
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);
        domainAxis.setLabel(quote);
        quote = "log[I(q)]";

        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRange(false);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);


        rangeAxis.setLowerBound(plottedDatasets.getRangeLowerBound(true));
        rangeAxis.setUpperBound(plottedDatasets.getRangeUpperBound(true) + 0.5);
        domainAxis.setUpperBound(plottedDatasets.getDomainUpperBound(true));
        domainAxis.setLowerBound(plottedDatasets.getDomainLowerBound(true));

        rangeAxis.setAutoRangeStickyZero(false);
        domainAxis.setAutoRangeStickyZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setBackgroundAlpha(0.0f);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setOutlineVisible(false);

        //make crosshair visible
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        plot.setDataset(plottedDatasets);
        plot.setRenderer(renderer1);

        // fitted line is Series 0
        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(false);
        renderer1.setSeriesShapesVisible(0, true);
        renderer1.setSeriesShapesVisible(0, false);
        renderer1.setSeriesPaint(0, Color.red);
        renderer1.setSeriesStroke(0, new BasicStroke(1.5f));


        // plotted data underneath data that is fitted
        double offset = -0.5*dataset.getPointSize();
        renderer1.setSeriesShape(2, new Ellipse2D.Double(offset, offset, dataset.getPointSize(), dataset.getPointSize()));
        renderer1.setSeriesLinesVisible(2, false);
        renderer1.setSeriesPaint(2, new Color(224,224,224, 100)); // some tyupe of gray color\
        renderer1.setSeriesShapesFilled(2, dataset.getBaseShapeFilled());
        renderer1.setSeriesVisible(2, dataset.getInUse());
        renderer1.setSeriesOutlineStroke(2, new BasicStroke(0.8f));

        offset = -0.5*dataset.getPointSize();
        renderer1.setSeriesShape(1, new Ellipse2D.Double(offset, offset, dataset.getPointSize(), dataset.getPointSize()));
        renderer1.setSeriesLinesVisible(1, false);
        renderer1.setSeriesPaint(1, dataset.getColor());
        renderer1.setSeriesShapesFilled(1, dataset.getBaseShapeFilled());
        renderer1.setSeriesVisible(1, dataset.getInUse());
        renderer1.setSeriesOutlineStroke(1, dataset.getStroke());

        plot.setDomainZeroBaselineVisible(false);

        this.createResidualsPlot();
    }

    public void setWorkingDirectory(WorkingDirectory dir){
        this.workingDirectory = dir;
    }

    public void makeVisible(){

        this.makePlot();

        jframe.addWindowListener(new WindowAdapter() {
            public void WindowClosing(WindowEvent e) {
                //locationOfWindow = jframe.getLocation();
                jframe.dispose();
            }
        });

//        ChartPanel chartPanel = new ChartPanel(chart);
//        plotPanel.add(new ChartPanel(chart));
//
//        frame.setContentPane(this.panel1);
//        //frame.getChartPanel().setChart(chartPanel.getChart());
//        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
//        frame.getChartPanel().setDisplayToolTips(false);
//        frame.pack();
//
//        jframe.setMinimumSize(new Dimension(640,480));
//        Container content = jframe.getContentPane();
//        content.add(frame.getChartPanel());
//        jframe.setVisible(true);

//        ChartPanel chartPanel = new ChartPanel(chart){
//            @Override
//            public void restoreAutoBounds(){
//                super.restoreAutoDomainBounds();
//                super.restoreAutoRangeBounds();
//                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);
//
//                int maxIndex;
//                double min = 10;
//                double max = -100;
//                double minX = 10;
//                double maxX = 0;
//                double tempYMin;
//                double tempYmax;
//                double tempXMin;
//
//                        maxIndex = super.getChart().getXYPlot().getDataset(2).getItemCount(2)-3;
//                        tempYmax = super.getChart().getXYPlot().getDataset(2).getYValue(2, 0);
//
//                        if (tempYmax > max){
//                            max = tempYmax;
//                        }
//
//                        for (int j=0; j< maxIndex;j++){
//                            tempYMin = super.getChart().getXYPlot().getDataset(2).getYValue(2, j);
//                            tempXMin = super.getChart().getXYPlot().getDataset(2).getXValue(2, j);
//                            if (tempYMin < min){
//                                min = tempYMin;
//                            }
//
//                            if (tempXMin < minX) {
//                                minX = tempXMin;
//                            }
//                            if (tempXMin > maxX) {
//                                maxX = tempXMin;
//                            }
//                        }
//
//                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.1), max+Math.abs(0.25*max));
//                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.2),maxX+Math.abs(0.1*maxX));
//            }
//        };

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        plotPanel.add(chartPanel);

        JFrame frame = new JFrame("PowerLaw Fitting");
        frame.setContentPane(this.panel1);
        //tempChartPanel.addMouseListener(new MouseMarker(tempChartPanel, indices));
       // frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        //frame.setPreferredSize(new Dimension(800,600));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    private void decrementHigh(int spinnerValue){
        //remove last point
        int total = Math.abs(spinnerValue - highQSpinnerValue);
        int last = dataToFit.getItemCount()-1;
        for(int i=0; i<total; i++){
            dataToFit.remove(last - i);
        }
    }

    private void incrementHigh(int spinnerValue){
        // actual value displayed by spinner is -1
        // so lowQSpinnerValue => is displaying -1 value
        int startAt = highQSpinnerValue - 1;
        int stopAt = spinnerValue-1;
        for(int i=startAt; i<stopAt; i++){
            dataToFit.add(datatToPlot.getDataItem(i-1));
        }
    }

    private void decrementLow(int spinnerValue){
        int total = spinnerValue - lowQSpinnerValue;

        for(int i=0; i<total; i++){
            dataToFit.remove(0);
        }
    }

    private void incrementLow(int value){
        // actual value displayed by spinner is -1
        // so lowQSpinnerValue => is displaying -1 value
        int startAt = lowQSpinnerValue - 1;
        int stopAt = value -1;
        for(int i=startAt; i>stopAt; i--){
            dataToFit.add(datatToPlot.getDataItem(i-1));
        }
    }

    public void changeColor(Color newColor, float thickness, int pointsize){

        renderer1.setSeriesPaint(1, newColor);
        double offset = -0.5*pointsize;
        renderer1.setSeriesShape(1, new Ellipse2D.Double(offset, offset, pointsize, pointsize));
        renderer1.setSeriesOutlineStroke(1, new BasicStroke(thickness));
    }


    private class MouseMarker extends MouseAdapter {
        private Marker marker;
        private Double markerStart = Double.NaN;
        private Double markerEnd = Double.NaN;
        private final XYPlot plot;
        private final JFreeChart chart;
        private  final ChartPanel panel;
        private ArrayList<Integer> samplesList;


        public MouseMarker(ChartPanel panel, ArrayList<Integer> indices) {
            this.samplesList = indices;
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
            System.out.println("markerEnd " + markerEnd.intValue());
//            int size = samplesList.getModel().getSize();
//            int trueStart = markerStart.intValue();
//            int trueEnd = markerEnd.intValue();
//
//            // set everything within range to true, everything else false
//            for(int i=0; i<size; i++){
//                // sampleFilesModel.get(i).isSelected()
//                if ((i < trueStart) || (i > trueEnd)){
//                    ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(false);
//                } else {
//                    ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(true);
//                }
//            }
//            samplesList.repaint();
            updateMarker();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            markerStart = getPosition(e);

            // if key pressed
        }
    }


    private void fitData(){
        int total = dataToFit.getItemCount();
        double[] xvalues = new double[total];
        double[] yvalues = new double[total];
        XYDataItem item;

        for(int i=0; i<total; i++){
            item = dataToFit.getDataItem(i);
            xvalues[i] = item.getXValue();
            yvalues[i] = item.getYValue();
        }
        double[] params = Functions.leastSquares(xvalues, yvalues);

        slope = params[0];
        intercept = params[1];
        slopeError = params[2];
        interceptError = params[3];
        resultslabel.setText(String.format("SLOPE : %.2f ( \u00B1 %.3f) INTRCPT : %.3E ( \u00B1 %.3E)", slope, slopeError, intercept, interceptError));
        // calculate residuals
        residuals.clear();
        for(int i=0; i<total; i++){
            item = dataToFit.getDataItem(i);
            residuals.add(qvalues.get(lowQSpinnerValue-1+i).doubleValue(), item.getYValue() - (slope*item.getXValue() + intercept));
        }

        double minq = datatToPlot.getX(lowQSpinnerValue-1).doubleValue();
        double maxq = datatToPlot.getX(highQSpinnerValue-1).doubleValue();
        double valueq = 0.5*(minq - firstValue) + firstValue;
        line.clear();
        line.add(valueq, slope*valueq + intercept);
        valueq = 0.5*(lastValue - maxq) + maxq;
        line.add(valueq, slope*valueq + intercept);
    }


    private void createResidualsPlot(){
        residualsChart = ChartFactory.createXYLineChart(
                "",                // chart title
                "q (\u212B\u207B\u00B9)",     // domain axis label
                "residuals",                  // range axis label
                new XYSeriesCollection(residuals),      // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        residualsChart.getXYPlot().setDomainZeroBaselineVisible(true);
        residualsChart.getXYPlot().setRangeZeroBaselineVisible(true);
        residualsChart.removeSubtitle(residualsChart.getTitle());
        residualsChart.setBackgroundPaint(null);
        residualsChart.setBackgroundPaint(Color.WHITE);
        residualsChart.getXYPlot().setBackgroundPaint(Color.WHITE);

        ChartPanel tempChartPanel = new ChartPanel(residualsChart);
        tempChartPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        residualsPlot.add(tempChartPanel);
    }
}
