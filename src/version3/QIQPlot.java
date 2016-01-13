package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
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
 * Created by robertrambo on 11/01/2016.
 */
public class QIQPlot {

    static JFreeChart chart;
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    private XYSeriesCollection plottedDatasets = new XYSeriesCollection();
    private Collection inUseCollection;

    public static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 q \u00D7 I(q) vs q PLOT", chart);
    static JFrame jframe = new JFrame("SC\u212BTTER \u2263 q × I(q) vs q PLOT");

    XYLineAndShapeRenderer renderer1;
    public boolean crosshair = true;

    private static QIQPlot singleton = new QIQPlot( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private QIQPlot(){

        JPopupMenu popup = frame.getChartPanel().getPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Toggle Crosshair") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                if (crosshair) {
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
    }

    public static QIQPlot getInstance() {
        return singleton;
    }

    public void plot(Collection collection, String workingDirectoryName) {

        inUseCollection = collection;
        int totalSets = collection.getDatasetCount();
        plottedDatasets = new XYSeriesCollection();  // spinners will always modify the plottedDataset series

        for (int i = 0; i < totalSets; i++){
            Dataset temp = collection.getDataset(i);
            temp.clearPlottedQIQData();
            if (temp.getInUse()){
                temp.scalePlottedQIQData();
            }
            plottedDatasets.addSeries(temp.getPlottedQIQDataSeries());
        }

        chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "q",                        // domain axis label
                "I(q)*q",                // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                true,                       // include legend
                true,
                false
        );

        chart.setTitle("SC\u212BTTER \u2263 Total Scattered Intensity Plot");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 0, 0);

        ChartPanel chartPanel = new ChartPanel(chart){
            @Override
            public void restoreAutoBounds(){
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);

                int seriesCount = super.getChart().getXYPlot().getDataset(0).getSeriesCount();
                int maxIndex;
                double min = 10;
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
                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.1),max+Math.abs(0.1*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.1),maxX+Math.abs(0.1*maxX));
            }
        };

        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("I(q)*q");

        String quote = "q, \u212B \u207B\u00B9";
        domainAxis.setLabel(quote);
        domainAxis.setAutoRangeStickyZero(true);
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);

        //domainAxis.setAxisLineStroke(new BasicStroke(16));
        //domainAxis.setAxisLinePaint(Color.BLACK);
        //domainAxis.setAxisLineVisible(true);

        quote = "q \u00D7 I(q)";
        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRangeStickyZero(true);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);
        //rangeAxis.setAxisLineVisible(false);
        //rangeAxis.setRange(0, dataset.getRangeUpperBound(true) + 0.1*dataset.getRangeUpperBound(true));
        chart.getLegend().setVisible(false);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        //make crosshair visible
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);
        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        double negativePointSize;
        for (int i=0; i < collection.getDatasets().size(); i++){
            Dataset temp = collection.getDataset(i);
            negativePointSize = -0.5*temp.getPointSize();
            renderer1.setSeriesShape(i, new Ellipse2D.Double(negativePointSize, negativePointSize, temp.getPointSize(), temp.getPointSize()));
            renderer1.setSeriesShapesFilled(i, temp.getBaseShapeFilled());
            renderer1.setSeriesLinesVisible(i, false);
            renderer1.setSeriesVisible(i, temp.getInUse());
            renderer1.setSeriesPaint(i, temp.getColor());
            renderer1.setSeriesOutlinePaint(i, temp.getColor());
            renderer1.setSeriesOutlineStroke(i, temp.getStroke());
        }

        frame.setLocation(350, 350);
        frame.getChartPanel().setChart(chartPanel.getChart());
        frame.getChartPanel().setDisplayToolTips(true);
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.pack();

        jframe.setMinimumSize(new Dimension(640,480));
        Container content = jframe.getContentPane();
        content.add(frame.getChartPanel());
        jframe.setLocation(200,200);
        jframe.setVisible(true);

    }

    public boolean isVisible(){
        return jframe.isVisible();
    }

    public void clearAll(){
        plottedDatasets.removeAllSeries();
        frame.removeAll();
    }

    public void changeVisibleSeries(int index, boolean flag){

        boolean isVisible = renderer1.isSeriesVisible(index);

        if (isVisible){
            renderer1.setSeriesVisible(index, flag);
        } else {
            Dataset temp = inUseCollection.getDataset(index);
            temp.clearPlottedQIQData();
            temp.scalePlottedQIQData();
            renderer1.setSeriesVisible(index, flag);
        }
    }
}