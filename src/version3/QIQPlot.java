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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Created by robertrambo on 11/01/2016.
 */
public class QIQPlot {

    private static JFreeChart chart;
    private XYSeriesCollection plottedDatasets = new XYSeriesCollection();
    private Collection inUseCollection;

    public static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 q \u00D7 I(q) vs q PLOT", chart);

    XYLineAndShapeRenderer renderer1;
    public boolean crosshair = true;

    private static QIQPlot singleton = new QIQPlot( );
    private static Point locationOfWindow;
    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private QIQPlot(){
        locationOfWindow = new Point(200,250);
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

        final ValueMarker yMarker = new ValueMarker(0.0);
        yMarker.setPaint(Color.black);
        plot.addRangeMarker(yMarker);


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

        frame = new ChartFrame("SC\u212BTTER \u2263 LOG10 INTENSITY PLOT", chart);

        JPopupMenu popup = frame.getChartPanel().getPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Export Plotted Data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                ExportData temp = new ExportData(plottedDatasets, workingDirectoryName, "QIQ");
                temp.pack();
                temp.setVisible(true);
            }
        }));

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

        frame.getChartPanel().setDisplayToolTips(true);
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.pack();

        frame.addWindowListener(new WindowAdapter() {
            public void WindowClosing(WindowEvent e) {
                locationOfWindow = frame.getLocation();
                frame.dispose();
            }
        });

        frame.setMinimumSize(new Dimension(640,480));
        frame.setLocation(locationOfWindow);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void setNotify(boolean state){
        frame.getChartPanel().getChart().setNotify(state);
    }

    public boolean isVisible(){

        if (frame != null){
            return frame.isVisible();
        }
        return false;
    }

    public void clearAll(){
        plottedDatasets.removeAllSeries();
        if (frame != null){
            frame.removeAll();
        }
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

    public void closeWindow(){
        this.clearAll();
        locationOfWindow = frame.getLocation();
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void changeColor(int id, Color newColor, float thickness, int pointsize){
        renderer1.setSeriesPaint(id, newColor);

        double offset = -0.5*pointsize;
        renderer1.setSeriesShape(id, new Ellipse2D.Double(offset, offset, pointsize, pointsize));
        renderer1.setSeriesOutlineStroke(id, new BasicStroke(thickness));
    }

}