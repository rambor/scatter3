package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
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

/**
 * Created by robertrambo on 13/01/2016.
 */
public class RatioPlot {

    private final XYSeriesCollection ratioCollection;
    private final XYSeriesCollection logRatioCollection;
    private YIntervalSeries ratioErrorSeries;
    private Collection inUse;
    private Dataset referenceDataset, targetDataset;
    private String refName;
    private String targetName;
    JFreeChart chart;
    JFreeChart chartLogRatio;
    JFreeChart combChart;
    private XYSeries reference;
    private XYSeries ratioSeries;
    private XYSeries logRatioSeries;
    private XYSeries referenceError;
    private XYSeries targetError;

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
        logRatioCollection = new XYSeriesCollection();

        ratioSeries = new XYSeries("ratio");
        logRatioSeries = new XYSeries("log-ratio");
        ratioErrorSeries = new YIntervalSeries("RatioError", false, false);

        //XYSeries target;
        this.setTargetAndReference();
        this.createRatioDataSets();

    }

    private void createRatioDataSets(){

        int startPt = referenceDataset.getStart();
        int endPt = referenceDataset.getEnd();
        XYSeries referenceSeries = referenceDataset.getAllData();
        XYSeries targetSeries = targetDataset.getAllData();
        XYDataItem tempXY, errorXYref, errorXYtar, tarXY;

        double xValue, ratioValue, ratioError;
        int locale;

        for(int i=startPt; i< endPt; i++){
            tempXY = referenceSeries.getDataItem(i);
            errorXYref = referenceError.getDataItem(i);

            xValue = tempXY.getXValue();
            locale = targetSeries.indexOf(tempXY.getX());

            if (locale >= 0){
                tarXY = targetSeries.getDataItem(locale);

                // reference/target
                ratioValue = tempXY.getYValue()/tarXY.getYValue();

                ratioSeries.add(xValue, ratioValue);

                ratioError = ratioValue*Math.sqrt((errorXYref.getYValue()/tempXY.getYValue()*errorXYref.getYValue()/tempXY.getYValue()) + (tarXY.getYValue()*tarXY.getYValue()*tarXY.getYValue()*tarXY.getYValue()));
                ratioErrorSeries.add(xValue, ratioValue, ratioValue - ratioError, ratioValue+ratioError);

                if (ratioValue > 0) {
                    logRatioSeries.add(xValue, Math.log10(ratioValue));
                }
                // target.add(targetSeries.getDataItem(locale));
            } else {
                // interpolate value
                // make sure reference q values is greater than first two or last two points in sourceSeries
                if ( (xValue > targetSeries.getX(1).doubleValue()) || (xValue < targetSeries.getX(targetSeries.getItemCount()-2).doubleValue()) ){
                    Double[] results =  Functions.interpolateOriginal(targetSeries, targetError, xValue, 1);
                    //target.add(xValue, results[1]);
                    ratioValue = tempXY.getYValue()/results[1];
                    ratioSeries.add(xValue, ratioValue);
                    if (ratioValue > 0) {
                        logRatioSeries.add(xValue, Math.log10(ratioValue));
                    }
                }
            }
        }

        ratioCollection.addSeries(ratioSeries);
        logRatioCollection.addSeries(logRatioSeries);
    }

    private void setTargetAndReference(){
        boolean first = false;
        for(int i=0; i<inUse.getDatasets().size(); i++){
            if (inUse.getDataset(i).getInUse() && !first){
                target_id = i;
                first = true;
            } else if (inUse.getDataset(i).getInUse() && first) {
                ref_id = i;
                reference = new XYSeries(inUse.getDataset(i).getFileName());
                break;
            }
        }

        referenceDataset = inUse.getDataset(ref_id);
        refName =referenceDataset.getFileName();
        referenceError = referenceDataset.getAllDataError();

        targetDataset = inUse.getDataset(target_id);
        targetName = targetDataset.getFileName();
        targetError = targetDataset.getAllDataError();
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
                "Log Intensity Ratio",                // chart title
                "q",                    // domain axis label
                "log(I1/I2)",                  // range axis label
                logRatioCollection,               // data
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
        domainAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);

        double sum=0.0;
        int counter =0;
        for (int i=0; i<ratioSeries.getItemCount()*0.5; i++){
            sum += ratioSeries.getY(i).doubleValue();
            counter +=1;
        }

        double average = sum/((double)counter);

        rangeAxis.setRange(-0.5*average+average,0.5*average+average);

        plot.setRangeAxis(rangeAxis);

        plot.setDomainAxis(domainAxis);
        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(true);
        renderer1.setBaseLinesVisible(false);
        renderer1.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 3.0, 3.0));
        renderer1.setSeriesPaint(0, Color.BLACK);
        plot.getAnnotations().size();
        plot.setBackgroundAlpha(0.0f);
        plot.setBackgroundPaint(Color.WHITE);

        final XYPlot plotLogRatio = chartLogRatio.getXYPlot();

        XYSplineRenderer renderer2 = new XYSplineRenderer();
        renderer2.setBaseLinesVisible(false);
        renderer2.setBaseShapesFilled(false);
        renderer2.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 3.0, 3.0));
        renderer2.setSeriesPaint(0, Color.BLACK);

        plotLogRatio.setRenderer(renderer2);
        plotLogRatio.setBackgroundAlpha(0.0f);
        final NumberAxis rangeAxisLog = new NumberAxis("log [I\u2081(q)/I\u2082(q)]");

        sum=0.0;
        counter =0;
        for (int i=0; i<logRatioSeries.getItemCount()*0.5; i++){
            sum += logRatioSeries.getY(i).doubleValue();
            counter +=1;
        }

        average = sum/((double)counter);
        rangeAxisLog.setRange(-0.5*average+average,0.5*average+average);
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
}