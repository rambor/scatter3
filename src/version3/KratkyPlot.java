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
 * Created by robertrambo on 08/01/2016.
 */
public class KratkyPlot {

    static JFreeChart chart;
    static boolean crosshair= true;

    private static Color titlePaint = Constants.SteelBlue;
    private static Collection inUseCollection;

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    private static XYSeriesCollection collectionToPlot = new XYSeriesCollection();

    public static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 KRATKY PLOT", chart);
    static JFrame jframe = new JFrame("SC\u212BTTER \u2263 KRATKY PLOT");

    private static XYLineAndShapeRenderer renderer1;
    private static double upper;
    private static ValueMarker yMarker = new ValueMarker(1.1);
    private static ValueMarker xMarker = new ValueMarker(1.7320508);
    private static String chartTitle;
    private static Point locationOfWindow;

    private static KratkyPlot singleton = new KratkyPlot();

    private KratkyPlot() {
        chartTitle = "Kratky Plot";

        jframe.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.out.println("Kratky Plot closed");
            }
        });


    }



    /* Static 'instance' method */
    public static KratkyPlot getInstance() {
        return singleton;
    }

    public static void plot(Collection collection, String workingDirectoryName) {

        inUseCollection = collection;
        int totalSets = collection.getDatasetCount();

        collectionToPlot.removeAllSeries();
        // create collection of series to plot

        for (int i = 0; i < totalSets; i++){
            Dataset temp = collection.getDataset(i);
            temp.clearPlottedKratkyData();
            if (temp.getInUse()){
                temp.scalePlottedKratkyData();
            }
            collectionToPlot.addSeries(temp.getPlottedKratkyDataSeries());  // should add an empty Series
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

        //if (normal){
        //    chart.setTitle("SC\u212BTTER \u2263 " + chartTitle);
       // }

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
        //if (normal){
        //    quote  = "q, \u212B \u207B\u00B9";
        //    plot.addDomainMarker(xMarker);
        //    plot.addRangeMarker(yMarker);
        //}
        domainAxis.setLabel(quote);
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);
        quote = "q\u00B2 \u00D7 I(q)";
        //if (normal){
        //    quote  = "q\u00B2 \u00D7 I(q)";
        //}
        rangeAxis.setLabel(quote);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);

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

        frame.getChartPanel().setChart(chartPanel.getChart());
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.pack();

        jframe.addWindowListener(new WindowAdapter() {
            public void WindowClosing(WindowEvent e) {
                locationOfWindow = jframe.getLocation();
                jframe.dispose();
            }
        });

        jframe.setMinimumSize(new Dimension(640,480));
        Container content = jframe.getContentPane();
        content.add(frame.getChartPanel());
        jframe.setLocation(locationOfWindow);
        jframe.setVisible(true);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public boolean isVisible(){
        return jframe.isVisible();
    }

    public void clearAll(){
        collectionToPlot.removeAllSeries();
        frame.removeAll();
    }

    public void changeVisibleSeries(int index, boolean flag){

        boolean isVisible = renderer1.isSeriesVisible(index);

        if (isVisible){
            renderer1.setSeriesVisible(index, flag);
        } else {
            Dataset temp = inUseCollection.getDataset(index);
            temp.clearPlottedKratkyData();
            temp.scalePlottedKratkyData();
            renderer1.setSeriesVisible(index, flag);
        }
    }

    private void setPlotStatus(){
        int totalSets = inUseCollection.getDatasetCount();

        for (int i = 0; i < totalSets; i++){
            Dataset temp = inUseCollection.getDataset(i);
            temp.clearPlottedKratkyData();
            if (temp.getInUse()){
                temp.scalePlottedKratkyData();
            }
            collectionToPlot.addSeries(temp.getPlottedKratkyDataSeries());  // should add an empty Series
        }
    }

    public void closeWindow(){
        locationOfWindow = jframe.getLocation();
        jframe.dispatchEvent(new WindowEvent(jframe, WindowEvent.WINDOW_CLOSING));
    }

    public void setNotify(boolean state){
        frame.getChartPanel().getChart().setNotify(state);
    }

    public void changeColor(int id, Color newColor, float thickness, int pointsize){
        renderer1.setSeriesPaint(id, newColor);

        double offset = -0.5*pointsize;
        renderer1.setSeriesShape(id, new Ellipse2D.Double(offset, offset, pointsize, pointsize));
        renderer1.setSeriesOutlineStroke(id, new BasicStroke(thickness));

    }

}
