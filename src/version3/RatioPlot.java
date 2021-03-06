package version3;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by robertrambo on 13/01/2016.
 */
public class RatioPlot {

    private final XYSeriesCollection ratioCollection;
    private final XYSeriesCollection differenceCollection;
    private YIntervalSeries ratioErrorSeries;
    private Collection inUse;
    private Dataset referenceDataset, targetDataset;
    private String refName;
    private String targetName;
    private ResidualDifferences differences;
    //private double yMarker;
    private ValueMarker yMarker; //= new ValueMarker(1.1);
    private ValueMarker yMarkerLog; //= new ValueMarker(1.1);

    JFreeChart chart;
    JFreeChart chartLogRatio;
    JFreeChart combChart;
    private Ratio ratioObject;

    private int target_id, ref_id;
    JFrame f = new JFrame("SC\u212BTTER \u2263 Intensity Ratio Plot");
    Container content = f.getContentPane();
    //label to be changed
    JLabel label;

    String title;
    private String workingDirectoryName;

    ChartFrame frame = new ChartFrame("Ratio", chart);
    CombinedDomainXYPlot combinedPlot;

    public RatioPlot(Collection collection, String workingDirectoryName) {
        inUse = collection;
        this.workingDirectoryName = workingDirectoryName;

        ratioCollection = new XYSeriesCollection();
        differenceCollection = new XYSeriesCollection();

        ratioErrorSeries = new YIntervalSeries("RatioError", false, false);

        //XYSeries target;
        this.setTargetAndReference();
        this.createRatioDataSets();
    }





    private void createRatioDataSets(){


        differences = new ResidualDifferences(referenceDataset.getAllData(),
                targetDataset.getAllData(),
                targetDataset.getAllDataError(),
                referenceDataset.getData().getMinX(),
                referenceDataset.getData().getMaxX(),
                12, referenceDataset.getId(), targetDataset.getId(), 0);

        differences.printTests(" DIFF");

        ratioObject = new Ratio(referenceDataset.getAllData(),
                targetDataset.getAllData(),
                targetDataset.getAllDataError(),
                referenceDataset.getData().getMinX(),
                referenceDataset.getData().getMaxX(), referenceDataset.getId(), targetDataset.getId(),0);

        ratioObject.printTests("RATIO");

        //double mark = averageByMAD(valuesPerBin).getMean();
        ratioCollection.addSeries(ratioObject.getTestSeries());
        differenceCollection.addSeries(differences.getTestSeries());
    }


    /**
     *
     */
    private void setTargetAndReference(){
        boolean first = false;
        for(int i=0; i<inUse.getDatasets().size(); i++){
            if (inUse.getDataset(i).getInUse() && !first){
                target_id = i;
                first = true;
            } else if (inUse.getDataset(i).getInUse() && first) {
                ref_id = i;
                break;
            }
        }

        referenceDataset = inUse.getDataset(ref_id);
        refName =referenceDataset.getFileName();

        targetDataset = inUse.getDataset(target_id);
        targetName = targetDataset.getFileName();

        title = "Ratio: " + refName+" / "+targetName;
    }

    public void plot() {


        chart = ChartFactory.createXYLineChart(
                title,                // chart title
                "q",                    // domain axis label
                "I\u2081(q)/I\u2082(q)",                  // range axis label
                ratioCollection,               // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,
                false
        );

        chartLogRatio = ChartFactory.createXYLineChart(
                "Intensity Differences",                // chart title
                "q",                    // domain axis label
                "(I_ref - c\u00D7I_target)",                  // range axis label
                differenceCollection,               // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,
                false
        );
        String yLabel = "I₁(q)/I₂(q)";
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("");
        final NumberAxis rangeAxis = new NumberAxis(yLabel);

        domainAxis.setAutoRangeIncludesZero(false);
        Font fnt = new Font("SansSerif", Font.BOLD, 15);
        domainAxis.setLabelFont(fnt);
        rangeAxis.setLabelFont(fnt);
        rangeAxis.setAutoRange(true);
        domainAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);

        double average = ratioObject.getAverage();
        double stdev = Math.sqrt(ratioObject.getVariance());
        rangeAxis.setRange(average-2*stdev,2*stdev+average);
        plot.setRangeAxis(rangeAxis);
        yMarker = new ValueMarker(average);
        yMarker.setPaint(Color.RED);
        yMarker.setStroke(new BasicStroke(
                2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {6.0f, 6.0f}, 0.0f
        ));
        plot.addRangeMarker(yMarker);

        plot.setDomainAxis(domainAxis);
        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(false);
        renderer1.setBaseLinesVisible(false);
        renderer1.setSeriesShape(0, new Ellipse2D.Double(-3.6, -3.6, 3.6, 3.6));
        renderer1.setSeriesPaint(0, Constants.DarkGray);
        plot.getAnnotations().size();
        plot.setBackgroundAlpha(0.0f);
        plot.setBackgroundPaint(Color.WHITE);

        final XYPlot plotLogRatio = chartLogRatio.getXYPlot();

        XYSplineRenderer renderer2 = new XYSplineRenderer();
        renderer2.setBaseLinesVisible(false);
        renderer2.setBaseShapesFilled(false);
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-3.6, -3.6, 3.6, 3.6));
        renderer2.setSeriesPaint(0,  Constants.DarkGray);

        plotLogRatio.setRenderer(renderer2);
        plotLogRatio.setBackgroundAlpha(0.0f);
        final NumberAxis rangeAxisLog = new NumberAxis(" [I\u2081(q) - c\u00D7I\u2082(q)]");

        average = differences.getLocation();
        stdev = Math.sqrt(differences.getScale());
        yMarkerLog = new ValueMarker(0);
        yMarkerLog.setPaint(Color.RED);
        yMarkerLog.setStroke(new BasicStroke(
                2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {6.0f, 6.0f}, 0.0f
        ));
        plotLogRatio.addRangeMarker(yMarkerLog);

        rangeAxisLog.setRange(average - 2*stdev,2*stdev+average);
        rangeAxisLog.setLabelFont(fnt);
        rangeAxisLog.setAutoRangeIncludesZero(false);
        plotLogRatio.setRangeAxis(rangeAxisLog);

        combinedPlot = new CombinedDomainXYPlot(new NumberAxis("q"));
        combinedPlot.setGap(10.0);
        combinedPlot.add(plot, 1);
        combinedPlot.add(plotLogRatio, 1);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedPlot.setBackgroundAlpha(0.0f);
        combinedPlot.setBackgroundPaint(Color.WHITE);

        combChart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        combChart.setBackgroundPaint(Color.WHITE);
        combChart.removeLegend();

        f.setLocation(300, 300);
        frame.getChartPanel().setChart(combChart);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.getChartPanel().setBackground(Color.WHITE);
        frame.getChartPanel().setDisplayToolTips(true);
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.pack();

        content.add(frame.getChartPanel(), BorderLayout.CENTER);
        f.setSize(600, 300);
        f.pack();

        f.setVisible(true);
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
}