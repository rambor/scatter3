package version3;

import org.jfree.chart.*;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by robertrambo on 05/08/2016.
 */
public class FactorAnalysis extends SwingWorker<String, Object> {


    private JPanel panel1;

    private JButton setPeakButton;
    private JButton SVDButton;
    private JPanel intensityPanel;
    private JPanel secPanel;
    private JComboBox comboBoxSVD;
    private JTextField minQValueLabel;
    private JTextField maxQValueLabel;
    private JTextField textField3;
    private JButton ICAButton;
    private JProgressBar progressBar1;
    private JLabel messagesLabel;
    private JButton EFAButton;
    private JLabel qminLabel;
    private JLabel qmaxLabel;
    private JLabel svdLabel;
    private JLabel efaLabel;
    private JLabel componentLabel;
    private JLabel deltaLabel;
    private JLabel label2;
    private JPanel label1;
    private JPanel leftPanel;
    private JPanel rightPanel;

    private ArrayList<Double> means;
    private double minQValueOfSet;
    private double maxQValueOfSet;
    private XYSeriesCollection signalPlotCollection;
    private XYSeriesCollection saxsPlotCollection;

    private ChartPanel signalChartPanel;
    private CombinedDomainXYPlot combinedPlot;

    private JFreeChart chart;
    private XYPlot efaplot;

    private ChartPanel saxsChartPanel;
    private JFreeChart saxsChart;

    private TextTitle saxsChartTitle;

//    private Color outlineColor = new Color(70, 130, 180, 100);
//    private Color fillColor = new Color(70, 130, 180, 70);

    private Color outlineColor = new Color(0, 170, 255, 100);
    private Color fillColor = new Color(0, 170, 255, 70);

    private MouseMarker signalPlotMouseMarker;
    private ArrayList<Color> colors;


    //private ChartFrame frame;

    //private Collection samplesCollection;
    //private Collection buffersCollection;
    private Collection subtractedDataCollection;
    private ArrayList<XYSeries> standardizedDataSet;
    private XYLineAndShapeRenderer signalRenderer;
    private XYLineAndShapeRenderer reverseEFARenderer;

    // use defined buffer to make signal plot
    //
    // user define full range of data to apply factor analysis
    // perform factor analysis (forward reverse)
    //
    public FactorAnalysis(Collection samples, Collection selectedBuffers, double finalQmin, double finalQmax, JLabel status, final JProgressBar bar, String outputDirectory){
        // take specified buffer and do subtraction from each frame
        // samplesCollection = samples;
        // buffersCollection = selectedBuffers;
        status.setText("Preparing data");
        colors = new ArrayList<>();
        colors.add(Color.cyan);
        colors.add(Constants.MediumRed);
        colors.add(Color.orange);
        colors.add(Constants.DodgerBlue);
        colors.add(Color.green);
        colors.add(Color.magenta);
        colors.add(Color.black);

        Subtraction subTemp = new Subtraction(selectedBuffers, samples, finalQmin, finalQmax, false, true, false, false, 4, status, bar);
        // add other attributes and then run
        // Double.parseDouble(comboBoxSubtractBins.getSelectedItem().toString())/100.00;
        subTemp.setNameAndDirectory("", outputDirectory);
        // create singly subtracted datasets
        subtractedDataCollection = new Collection();
        subTemp.setCollectionToUpdate(subtractedDataCollection);

        Thread temp1 = new Thread(subTemp);
        temp1.start();

        try {
            temp1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        means = new ArrayList<>();

        int totalSamples = subtractedDataCollection.getDatasetCount();

        minQValueOfSet = Functions.findLeastCommonQvalue(subtractedDataCollection);
        maxQValueOfSet = Functions.findMaximumCommonQvalue(subtractedDataCollection);

        if (minQValueOfSet < 0.02){
            minQValueOfSet = 0.02;
        }

        if (maxQValueOfSet > 0.29){
            maxQValueOfSet = 0.29;
        }

        minQValueLabel.setText(Double.toString(minQValueOfSet));
        maxQValueLabel.setText(Double.toString(maxQValueOfSet));

        // create mean subtracted set.
        status.setText("Creating mean subtracted set data ");

        for (int i=0; i<totalSamples; i++){
            double sum = 0;
            Dataset tempDataset = subtractedDataCollection.getDataset(i);

            int totalData = tempDataset.getAllData().getItemCount();
            XYSeries allData = tempDataset.getAllData();

            for(int t=0; t<totalData; t++){
                sum += allData.getY(i).doubleValue();
            }
            means.add(sum/(double)totalData);
        }


        standardizedDataSet = new ArrayList<>();
        for (int i=0; i<totalSamples; i++){

            Dataset tempDataset = subtractedDataCollection.getDataset(i);

            standardizedDataSet.add(new XYSeries(tempDataset.getFileName()));
            XYSeries temp = standardizedDataSet.get(i);

            int totalData = tempDataset.getAllData().getItemCount();
            XYSeries allData = tempDataset.getAllData();

            for(int t=0; t<totalData; t++){
                temp.add(allData.getX(i), allData.getY(i).doubleValue() - means.get(i));
            }
        }

        bar.setValue(0);
        bar.setIndeterminate(false);
        bar.setStringPainted(false);

        status.setText("Performing iterative PCA analysis");

        // create signal plot
        SignalPlot signal = new SignalPlot(samples, selectedBuffers, status, bar);
        signal.setFirstLastFrame(0, samples.getDatasetCount());
        signalPlotCollection = signal.createSignalPlotData();

        saxsPlotCollection = new XYSeriesCollection();

        saxsPlotCollection.addSeries(new XYSeries(subtractedDataCollection.getDataset(0).getFileName()));

        int totalInSet = subtractedDataCollection.getDataset(0).getOriginalLog10Data().getItemCount();
        for(int i=0; i<totalInSet; i++){
            saxsPlotCollection.getSeries(0).add(subtractedDataCollection.getDataset(0).getOriginalLog10Data().getDataItem(i));
        }

        makeSAXSPlot();
        makeSignalPlot();

        setPeakButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // signalPlotMouseMarker.markerStart
                // sliders to define peak?
            }
        });


        /**
         * use subtracted signals defined by MouseMarkers in signal plot and resolution limits
         * perform SVD on covariance matrix
         */
        SVDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // need to determine number of frames selected
                int startFrame = signalPlotMouseMarker.markerStart.intValue();
                int endFrame = signalPlotMouseMarker.markerEnd.intValue();
                XYSeriesCollection tempCollection = new XYSeriesCollection();

                if (endFrame - startFrame < 3){
                    messagesLabel.setText("Too few frames, 3 or more!");
                    return;
                }

                System.out.println("Start " + startFrame);
                System.out.println("  End " + endFrame);

                int totalInCollection = subtractedDataCollection.getDatasetCount();
                for (int i=0; i<totalInCollection; i++){
                    if (i >= startFrame && i <= endFrame){
                        tempCollection.addSeries(subtractedDataCollection.getDataset(i).getAllData());
                    }
                }
                System.out.println("Total frames in SVD " + tempCollection.getSeriesCount());

                double finalQmin = Double.parseDouble(minQValueLabel.getText());
                double finalQmax = Double.parseDouble(maxQValueLabel.getText());

                SVDCovariance svd = new SVDCovariance(finalQmin, finalQmax, tempCollection);
                svd.execute();

                svd.done();
                svd.makePlot();

            }
        });


        EFAButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decompositionAnalysis(true);
                //double qmin, double qmax, XYSeriesCollection datasets, int startIndexOfFrame, int numberOfEigenValuesToPlot, JProgressBar bar)
//                double finalQmin = Double.parseDouble(minQValueLabel.getText());
//                double finalQmax = Double.parseDouble(maxQValueLabel.getText());
//
//                // create collection
//                XYSeriesCollection tempCollection = new XYSeriesCollection();
//                int startFrame = signalPlotMouseMarker.markerStart.intValue();
//                int endFrame = signalPlotMouseMarker.markerEnd.intValue();
//
//                if (endFrame - startFrame < 3){
//                    messagesLabel.setText("Too few frames, 3 or more!");
//                    return;
//                }
//
//                int totalInCollection = subtractedDataCollection.getDatasetCount();
//                for (int i=0; i<totalInCollection; i++){
//                    if (i >= startFrame && i <= endFrame){
//                        tempCollection.addSeries(subtractedDataCollection.getDataset(i).getAllData());
//                    }
//                }
//
//                int totalSVD = Integer.valueOf((String)comboBoxSVD.getSelectedItem());
//                System.out.println("Total Eigenvalues to Plot: " + totalSVD);
//
//                EvolvingFactorAnalysis efa = new EvolvingFactorAnalysis(finalQmin, finalQmax,tempCollection, startFrame, totalSVD, progressBar1);
//                efa.execute();
//                efa.done();
//
//
//                XYSeriesCollection forward = efa.getForwardEigenValueSet();
//                //XYSeriesCollection reverse = efa.getReverseEigenValueSet();
//
//                int totalSeries = forward.getSeriesCount();
//                int totalInSeries = forward.getSeries(0).getItemCount();
//                System.out.println("PRINTING EFA");
//                //for(int i = 0; i<totalInSeries; i++){
//                //    System.out.println(i + " " + forward.getSeries(0).getX(i) + " " + forward.getSeries(0).getY(i));
//                //}
//
//                efaplot.setDataset(0, forward);  //Moore Function
                //efaplot.setDataset(1, reverse);
                //setReverseColors(forward.getSeriesCount());
            }
        });

        ICAButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // grab subtracted frames
                decompositionAnalysis(false);
            }
        });
    }

    private void decompositionAnalysis(boolean doEFA){
        //double qmin, double qmax, XYSeriesCollection datasets, int startIndexOfFrame, int numberOfEigenValuesToPlot, JProgressBar bar)
        double finalQmin = Double.parseDouble(minQValueLabel.getText());
        double finalQmax = Double.parseDouble(maxQValueLabel.getText());

        // create collection
        XYSeriesCollection tempCollection = new XYSeriesCollection();
        int startFrame = signalPlotMouseMarker.markerStart.intValue();
        int endFrame = signalPlotMouseMarker.markerEnd.intValue();

        if (endFrame - startFrame < 3){
            messagesLabel.setText("Too few frames, 3 or more!");
            return;
        }

        int totalInCollection = subtractedDataCollection.getDatasetCount();
        for (int i=0; i<totalInCollection; i++){
            if (i >= startFrame && i <= endFrame){
                tempCollection.addSeries(subtractedDataCollection.getDataset(i).getAllData());
            }
        }

        int totalSVD = Integer.valueOf((String)comboBoxSVD.getSelectedItem());
        System.out.println("Total Eigenvalues to Plot: " + totalSVD);

        if (doEFA){
            EvolvingFactorAnalysis efa = new EvolvingFactorAnalysis(finalQmin, finalQmax,tempCollection, startFrame, totalSVD, progressBar1);
            efa.execute();
            efa.done();


            XYSeriesCollection forward = efa.getForwardEigenValueSet();
            //XYSeriesCollection reverse = efa.getReverseEigenValueSet();

            int totalSeries = forward.getSeriesCount();
            int totalInSeries = forward.getSeries(0).getItemCount();
            System.out.println("PRINTING EFA");
            //for(int i = 0; i<totalInSeries; i++){
            //    System.out.println(i + " " + forward.getSeries(0).getX(i) + " " + forward.getSeries(0).getY(i));
            //}

            efaplot.setDataset(0, forward);  //Moore Function
        } else {
            // ICA
            System.out.println("DOING ICA");
//            Thread makeIt = new Thread(){
//                public void run() {
                    IndependentComponentAnalysis ica = new IndependentComponentAnalysis(finalQmin, finalQmax,tempCollection, startFrame, totalSVD, progressBar1);
                    ica.execute();
//                }
//            };
            //makeIt.start();
        }
    }


    @Override
    protected String doInBackground() throws Exception {

        JFrame frame = new JFrame("Factor Analysis");

        frame.setContentPane(panel1);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        return null;
    }

    /**
     * combo plot
     *
     */
    public void makeSAXSPlot(){

        saxsChart = ChartFactory.createXYLineChart(
                "SAXS Intensity Plot",            // chart title
                "q",                 // domain axis label
                "log10[I(q)]",                 // range axis label
                saxsPlotCollection,                   // data
                PlotOrientation.VERTICAL,
                false,                    // include legend
                true,                     // toolTip
                false
        );

        saxsChartPanel = new ChartPanel(saxsChart){
            @Override
            public void restoreAutoBounds(){
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);

                double min = 1000;
                double max = -10;
                double tempYMin;

                int totalItems = super.getChart().getXYPlot().getDataset(0).getItemCount(0);

                double minX = super.getChart().getXYPlot().getDataset(0).getXValue(0,0);
                double maxX = super.getChart().getXYPlot().getDataset(0).getXValue(0,totalItems-1);


                for(int i=0; i<totalItems; i++){
                    tempYMin = super.getChart().getXYPlot().getDataset(0).getYValue(0, i);
                    if (tempYMin < min){
                        min = tempYMin;
                    }
                    if (tempYMin > max){
                        max = tempYMin;
                    }
                }

                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.02), max+Math.abs(0.02*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.02), maxX+Math.abs(0.02*maxX));
            }
        };

        saxsChartTitle = saxsChart.getTitle();
        saxsChartTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        saxsChartPanel.setHorizontalAxisTrace(false);
        saxsChartPanel.setVerticalAxisTrace(false);

        final XYPlot plot = saxsChart.getXYPlot();
    /*
      set domain and range axis
     */
        //final NumberAxis domainAxis = new NumberAxis("q, \u212B \u207B\u00B9");
        final NumberAxis domainAxis = new NumberAxis("");
        domainAxis.setAutoRange(true);

        final NumberAxis rangeAxis = new NumberAxis("");
        rangeAxis.setRange(saxsPlotCollection.getRangeLowerBound(true) - 0.01*saxsPlotCollection.getRangeLowerBound(true), saxsPlotCollection.getRangeUpperBound(true) + 0.02*saxsPlotCollection.getRangeUpperBound(true));
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRange(true);


        plot.setRangeAxis(rangeAxis);
        plot.setDomainAxis(domainAxis);

        plot.setDataset(0, saxsPlotCollection);
        plot.mapDatasetToRangeAxis(0, 0);//1st dataset to 1st y-axis
        double negativePointSize, pointSize;

        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        signalRenderer = (XYLineAndShapeRenderer) plot.getRenderer();
        signalRenderer.setBaseShapesVisible(true);
        signalRenderer.setBaseShape(new Ellipse2D.Double(-0.5*2.0, -0.5*2.0, 2, 2));

        //XYSplineRenderer splineRend = new XYSplineRenderer();
        //splineRend.setBaseShapesVisible(true);
        //splineRend.setBaseShape(new Ellipse2D.Double(-0.5*2.0, -0.5*2.0, 2, 2));
        //plot.setRenderer(0, splineRend);       //render as a line

        //splineRend.setSeriesStroke(locale, temp.getStroke());
        //splineRend.setSeriesPaint(locale, temp.getColor().darker()); // make color slight darker

        pointSize = 4;
        negativePointSize = -0.5*pointSize;
        signalRenderer.setSeriesShape(0, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
        signalRenderer.setSeriesLinesVisible(0, false);

        signalRenderer.setSeriesPaint(0, new Color(70, 130, 180, 70));
        signalRenderer.setSeriesShapesFilled(0, true);

        signalRenderer.setSeriesOutlinePaint(0, new Color(70, 130, 180, 100));
        signalRenderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));


        //frame = new ChartFrame("SC\u212BTTER \u2263 SIGNAL PLOT", chart);
        saxsChartPanel.setDisplayToolTips(true);

        saxsChartPanel.getChart().getXYPlot().getRenderer(0).setBaseToolTipGenerator(new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset xyDataset, int i, int i2) {
                return (String) xyDataset.getSeriesKey(i);
            }
        });

        //saxsChartPanel.setRangeZoomable(false);
        //saxsChartPanel.setDomainZoomable(false);
        saxsChartPanel.setMouseZoomable(true);
        saxsChartPanel.setHorizontalAxisTrace(false);

        intensityPanel.add(saxsChartPanel);

    }

    private void setReverseColors(int totalPlotted){

        // first color in forward is last
        int startHere = totalPlotted-1;

        double pointSize = 3.3;
        double negativePointSize = -0.5*pointSize;

        for (int i=0; i < totalPlotted; i++){
            int inReverse = startHere - i;
            reverseEFARenderer.setSeriesPaint(inReverse, efaplot.getRenderer(0).getSeriesPaint(i));
            reverseEFARenderer.setSeriesShape(inReverse, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
            reverseEFARenderer.setSeriesLinesVisible(inReverse, false);
            reverseEFARenderer.setSeriesPaint(inReverse, efaplot.getRenderer(0).getSeriesPaint(i));
            reverseEFARenderer.setSeriesShapesFilled(inReverse, true);
            reverseEFARenderer.setSeriesOutlinePaint(inReverse, efaplot.getRenderer(0).getSeriesPaint(i));
        }
    }


    public void makeSignalPlot(){

        chart = ChartFactory.createXYLineChart(
                "Signal Plot",            // chart title
                "frame",                 // domain axis label
                "signal",                 // range axis label
                signalPlotCollection,                   // data
                PlotOrientation.VERTICAL,
                false,                    // include legend
                true,                     // toolTip
                false
        );

        JFreeChart efaChart = ChartFactory.createXYLineChart(
                "",            // chart title
                "frame",                 // domain axis label
                "eigenvalues",                 // range axis label
                new XYSeriesCollection(),                   // data
                PlotOrientation.VERTICAL,
                false,                    // include legend
                true,                     // toolTip
                false
        );

        efaplot = efaChart.getXYPlot();
        efaplot.setDataset(0, new XYSeriesCollection());  //Moore Function
        efaplot.setDataset(1, new XYSeriesCollection());  //Moore Function

        XYSplineRenderer splineRend = new XYSplineRenderer();
        splineRend.setBaseShapesVisible(false);
        splineRend.setBaseStroke(new BasicStroke(6.0f));

        float dash[] = {5.0f};

        reverseEFARenderer = new XYLineAndShapeRenderer();
        reverseEFARenderer.setBaseShapesVisible(true);

        efaplot.setRenderer(0, splineRend);          //render as a line
        efaplot.setRenderer(1, reverseEFARenderer);   //render as a line

        for (int i=0; i < colors.size(); i++){
            //splineRend.setSeriesShape(i, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
            //splineRend.setSeriesLinesVisible(i, false);
            splineRend.setSeriesPaint(i, colors.get(i));
            // splineRend.setSeriesShapesFilled(i, tempData.getBaseShapeFilled());
        }

//        signalChartPanel = new ChartPanel(chart){
//            @Override
//            public void restoreAutoBounds(){
//                super.restoreAutoDomainBounds();
//                super.restoreAutoRangeBounds();
//                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);
//                int seriesCount = super.getChart().getXYPlot().getDataset(0).getSeriesCount();
//
//                double min = 1000;
//                double max = -10;
//                double minX = 10;
//                double maxX = 0;
//                double tempYMin;
//                double tempXMin;
//
//                for (int i=0; i < seriesCount; i++){
//                    tempYMin = super.getChart().getXYPlot().getDataset(0).getYValue(i, 0);
//                    tempXMin = super.getChart().getXYPlot().getDataset(0).getXValue(i, 0);
//
//                    if (tempYMin < min){
//                        min = tempYMin;
//                    }
//                    if (tempYMin > max){
//                        max = tempYMin;
//                    }
//                    if (tempXMin < minX) {
//                        minX = tempXMin;
//                    }
//                    if (tempXMin > maxX) {
//                        maxX = tempXMin;
//                    }
//                }
//                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.02), max+Math.abs(0.02*max));
//                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.02), maxX+Math.abs(0.02*maxX));
//            }
//        };
//
//        signalChartPanel.setVerticalAxisTrace(true);

        final XYPlot plot = chart.getXYPlot();
    /*
      set domain and range axis
     */
        final NumberAxis domainAxis = new NumberAxis("frame");
        final NumberAxis rangeAxis = new NumberAxis("Integral of Ratio to Background");

        LogarithmicAxis yAxis = new LogarithmicAxis("eigenvalues");
        yAxis.setAutoRange(true);
        efaplot.setRangeAxis(yAxis);
        efaplot.setBackgroundPaint(Color.white);

        rangeAxis.setRange(signalPlotCollection.getRangeLowerBound(true) - 0.01*signalPlotCollection.getRangeLowerBound(true), signalPlotCollection.getRangeUpperBound(true) + 0.02*signalPlotCollection.getRangeUpperBound(true));
        rangeAxis.setAutoRangeIncludesZero(false);

        plot.setRangeAxis(rangeAxis);

        plot.setDataset(0, signalPlotCollection);
        plot.mapDatasetToRangeAxis(0, 0);//1st dataset to 1st y-axis
        double negativePointSize, pointSize;

        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        signalRenderer = (XYLineAndShapeRenderer) plot.getRenderer();
        signalRenderer.setBaseShapesVisible(true);
        signalRenderer.setBaseShape(new Ellipse2D.Double(-0.5*6, -0.5*6.0, 6, 6));

        for (int i=0; i < signalPlotCollection.getSeriesCount(); i++) {
            // go over each series
            pointSize = 9;
            negativePointSize = -0.5*pointSize;
            signalRenderer.setSeriesShape(i, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
            signalRenderer.setSeriesLinesVisible(i, false);
            signalRenderer.setSeriesPaint(i, fillColor);
            signalRenderer.setSeriesShapesFilled(i, true);

            signalRenderer.setSeriesOutlinePaint(i, outlineColor);
            signalRenderer.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
        }

        //frame = new ChartFrame("SC\u212BTTER \u2263 SIGNAL PLOT", chart);
//        signalChartPanel.setDisplayToolTips(true);
//
//        signalChartPanel.getChart().getXYPlot().getRenderer(0).setBaseToolTipGenerator(new XYToolTipGenerator() {
//            @Override
//            public String generateToolTip(XYDataset xyDataset, int i, int i2) {
//                return (String) xyDataset.getSeriesKey(i);
//            }
//        });
//
//        signalChartPanel.setRangeZoomable(false);
//        signalChartPanel.setDomainZoomable(false);
//        signalChartPanel.setMouseZoomable(false);
//        signalChartPanel.setHorizontalAxisTrace(true);
//        signalChartPanel.setVerticalAxisTrace(false);

        // add mouse listener for getting values
        //frame.getChartPanel().addKeyListener();
//        signalPlotMouseMarker = new MouseMarker(signalChartPanel, saxsPlotCollection, subtractedDataCollection, saxsChartTitle);
//        signalChartPanel.addMouseListener(signalPlotMouseMarker);
//
//        signalChartPanel.addChartMouseListener(new ChartMouseListener() {
//            private Double markerStart = Double.NaN;
//            private Double markerEnd = Double.NaN;
//
//            @Override
//            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
//                System.out.println("Setting frame min ");
//            }
//
//            @Override
//            public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
//
//            }
//        });

//        secPanel.add(signalChartPanel);

        combinedPlot = new CombinedDomainXYPlot(new NumberAxis("2"));
        combinedPlot.setDomainAxis(domainAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.add(plot, 1);
        combinedPlot.add(efaplot,1);

        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart combChart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        combChart.removeLegend();
        combChart.setBackgroundPaint(Color.WHITE);
        ChartFrame chartframe = new ChartFrame("", combChart); // chartpanel exists in frame

        signalChartPanel = chartframe.getChartPanel();

        signalPlotMouseMarker = new MouseMarker(signalChartPanel, (XYPlot) combinedPlot.getSubplots().get(0), saxsPlotCollection, subtractedDataCollection, saxsChartTitle);
        signalChartPanel.addMouseListener(signalPlotMouseMarker);

        signalChartPanel.addChartMouseListener(new ChartMouseListener() {
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

        signalChartPanel.setDisplayToolTips(true);

//        signalChartPanel.getChart().getXYPlot().getRenderer(0).setBaseToolTipGenerator(new XYToolTipGenerator() {
//            @Override
//            public String generateToolTip(XYDataset xyDataset, int i, int i2) {
//                return (String) xyDataset.getSeriesKey(i);
//            }
//        });

        signalChartPanel.setRangeZoomable(false);
        signalChartPanel.setDomainZoomable(false);
        signalChartPanel.setMouseZoomable(true);
        signalChartPanel.setHorizontalAxisTrace(true);
        signalChartPanel.setVerticalAxisTrace(false);

        secPanel.add(chartframe.getContentPane());
    }


    private final static class MouseMarker extends MouseAdapter {

        private Marker marker;
        private ValueMarker selectedDatasetMarker;
        private Double markerStart = Double.NaN;
        private Double markerEnd = Double.NaN;
        private int selectedDataset;
        private final XYPlot plot;
        private final JFreeChart chart;
        private  final ChartPanel panel;
        private XYSeriesCollection plottedXYSeries;
        private Collection subtractedCollection;
        private TextTitle saxsChartTitleInUse;

        public MouseMarker(ChartPanel panel, XYPlot xyplot, XYSeriesCollection samples, Collection subtractedDataCollection, TextTitle saxsChartTitle) {
            this.panel = panel;
            this.chart = panel.getChart();
            this.plot = xyplot;
            //this.plot = (XYPlot) chart.getPlot();

            this.plottedXYSeries = samples;
            this.subtractedCollection = subtractedDataCollection;
            this.saxsChartTitleInUse = saxsChartTitle;
            this.selectedDataset = 0;
            selectedDatasetMarker = new ValueMarker(selectedDataset);
            plot.addDomainMarker(selectedDatasetMarker);
        }

        private void updateMarker(){
            if (marker != null){
                plot.removeDomainMarker(marker, Layer.BACKGROUND);
            }


            plot.removeDomainMarker(selectedDatasetMarker);
            selectedDatasetMarker = new ValueMarker(selectedDataset);
            plot.addDomainMarker(selectedDatasetMarker);

            if (!( markerStart.isNaN() && markerEnd.isNaN())){
                if ( markerEnd > markerStart){
                    marker = new IntervalMarker(markerStart.intValue(), markerEnd.intValue());
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

            // int mouseX = e.getX();
            // int onscreen = e.getXOnScreen();
            // System.out.println("x = " + mouseX + " onscreen " + onscreen);
            return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            markerEnd = getPosition(e);

            int trueStart = markerStart.intValue();
            int trueEnd = markerEnd.intValue();

            int toPlot = trueStart;
            if ((trueEnd - trueStart) > 2){
                toPlot += Math.round(0.5*(trueEnd - trueStart));
            }

            selectedDataset = toPlot;
            plottedXYSeries.getSeries(0).clear();
            XYSeries tempLog10 = subtractedCollection.getDataset(toPlot).getOriginalLog10Data();
            //XYSeries tempLog10 = subtractedCollection.getDataset(toPlot).getQIQData();
            int total = tempLog10.getItemCount();
            //System.out.println("toPlot " + toPlot + " total " + total);

            for (int i=0; i<total; i++){
                plottedXYSeries.getSeries(0).add(tempLog10.getDataItem(i));
            }

            String name = subtractedCollection.getDataset(toPlot).getFileName().split("_sub")[0];
            saxsChartTitleInUse.setText(name + " (frame "+toPlot+")");

            // get coordinate of mouse start and make plot from subtractedset
            // set everything within range to true, everything else false
//            for(int i=0; i<size; i++){
//                // sampleFilesModel.get(i).isSelected()
//                if ((i < trueStart) || (i > trueEnd)){
//                    ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(false);
//                } else {
//                    ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(true);
//                }
//            }

            updateMarker();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            markerStart = getPosition(e);

            // if key pressed
        }


    }
}
