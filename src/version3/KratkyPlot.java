package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Created by robertrambo on 08/01/2016.
 */
public class KratkyPlot {

    static JFreeChart chart;
    static boolean crosshair= true;

    private static Color titlePaint;
    private static String workingDirectoryName;

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    private static XYSeriesCollection collectionToPlot = new XYSeriesCollection();


    public static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 KRATKY PLOT", chart);
    private static XYLineAndShapeRenderer renderer1;
    private static double upper;
    private static ValueMarker yMarker = new ValueMarker(1.1);
    private static ValueMarker xMarker = new ValueMarker(1.7320508);
    private static String chartTitle;

    private static KratkyPlot singleton = new KratkyPlot();

    private KratkyPlot() {
        chartTitle = "Kratky Plot";
    }

    /* Static 'instance' method */
    public static KratkyPlot getInstance() {
//        workingDirectoryName = workingDirectory;
//        titlePaint = paint;
        return singleton;
    }

    protected static void plot(Collection collection, int positionX, int positionY, int width, int height, boolean normal) {

        // create collection of series to plot
        int startAt, endAt;
        for (int i = 0; i < collection.getDatasets().size(); i++){
            Dataset temp = collection.getDataset(i);
            XYDataItem tempData;
            startAt = temp.getStart();
            endAt = temp.getEnd();
            //
            // use q-values in active dataset
            // that way the intensities are always already scaled and set is truncated
            //
            collectionToPlot.addSeries(new XYSeries(temp.getFileName()));
            XYSeries tempSeries = collectionToPlot.getSeries(i);

            for(int j = startAt; j < endAt; j++){
                tempData = temp.getKratkyItem(j);
                tempSeries.add(tempData.getX(), tempData.getYValue()*temp.getScaleFactor());
            }
        }

        chart = ChartFactory.createXYLineChart(
                "",                         // chart title
                "q",                        // domain axis label
                "I(q)*q^2",                 // range axis label
                collectionToPlot,           // data
                PlotOrientation.VERTICAL,
                true,                       // include legend
                false,
                false
        );

        chart.setTitle("SC\u212BTTER \u2263 Kratky Plot");

        if (normal){
            chart.setTitle("SC\u212BTTER \u2263 " + chartTitle);
        }

        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(titlePaint);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 0, 0);

        upper = collectionToPlot.getRangeUpperBound(true);

        ChartPanel chartPanel = new ChartPanel(chart){
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
                boolean isVisible;

                for (int i=0; i < seriesCount; i++){
                    //check if visible, if visible, get min and max I value
                    // renderer 0 is merged data
                    // renderer 1 is plotted data
                    isVisible = super.getChart().getXYPlot().getRenderer(0).isSeriesVisible(i);

                    if (isVisible){
                        maxIndex = super.getChart().getXYPlot().getDataset(0).getItemCount(i)-3;

                        for (int j=0; j< maxIndex;j++){
                            tempYMin = super.getChart().getXYPlot().getDataset(0).getYValue(i, j);
                            tempXMin = super.getChart().getXYPlot().getDataset(0).getXValue(i, j);
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
                    }
                }
                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.5),max+Math.abs(0.1*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.5),maxX+Math.abs(0.1*maxX));
            }
        };


        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("I(q)*q^2");
        String quote = "q, \u212B \u207B\u00B9";
        if (normal){
            quote  = "q, \u212B \u207B\u00B9";
            plot.addDomainMarker(xMarker);
            plot.addRangeMarker(yMarker);
        }
        domainAxis.setLabel(quote);

        quote = "q\u00B2 \u00D7 I(q)";
        if (normal){
            quote  = "q\u00B2 \u00D7 I(q)";
        }
        rangeAxis.setLabel(quote);

        rangeAxis.setRange(0, upper + 0.1*upper);
        chart.getLegend().setVisible(false);
        domainAxis.setAutoRangeIncludesZero(true);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        //make crosshair visible
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        double pointSize;

        for (int i=0; i < collectionToPlot.getSeriesCount(); i++){
            Dataset temp = collection.getDataset(i);
            pointSize = temp.getPointSize();

            renderer1.setSeriesShape(i, new Ellipse2D.Double(-pointSize*0.5, -pointSize*0.5, pointSize, pointSize));
            renderer1.setSeriesShapesFilled(i,temp.getBaseShapeFilled());
            renderer1.setSeriesLinesVisible(i, false);
            renderer1.setSeriesPaint(i, temp.getColor());
            renderer1.setSeriesVisible(i, temp.getInUse());
            renderer1.setSeriesOutlineStroke(i, temp.getStroke());
        }

        if (positionX==0 && positionY==0){
            positionX=50;
            positionY=50;
        }
        frame.setLocation(positionX, positionY);
        frame.getChartPanel().setSize(600, 500);

        chartPanel.setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.getContentPane().add(chartPanel);


        JPopupMenu popup = chartPanel.getPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Toggle Crosshair") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                if (crosshair){
                    chart.getXYPlot().setDomainCrosshairVisible(false);
                    chart.getXYPlot().setRangeCrosshairVisible(false);
                    crosshair = false;
                } else {
                    chart.getXYPlot().setDomainCrosshairVisible(true);
                    chart.getXYPlot().setRangeCrosshairVisible(true);
                    crosshair = true;
                }
            }
        }));


        //frame.getChartPanel().setChart(chart);
        //frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(Scatter.workingDirectoryName));
        frame.pack();
        frame.setVisible(true);
        if(width == 0) {
            width = 688;
            height = 454;
        }
        frame.setSize(width, height);

    }

    public static void rescale(int index, double scale) {

    }
}
