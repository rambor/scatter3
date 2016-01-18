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
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by robertrambo on 11/01/2016.
 */
public class NormalizedKratkyPlot {

    JFreeChart chart;
    boolean crosshair=true;
    private XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    private XYSeriesCollection plottedData = new XYSeriesCollection();
    ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 DIMENSIONLESS KRATKY PLOT", chart);
    XYLineAndShapeRenderer renderer1;
    double upper;
    private Pattern vctag = Pattern.compile("Vc");
    private Pattern realtag = Pattern.compile("Real");
    private static ValueMarker yMarker = new ValueMarker(1.1);
    private static ValueMarker xMarker = new ValueMarker(1.7320508);
    private Point locationOfWindow;

    String chartTitle;
    String xLabel;
    String yLabel;

    public NormalizedKratkyPlot(final String title) {
        locationOfWindow = new Point(100,100);
        chartTitle = title;
    }

    /**
     *
     * @param collectionSelected
     * @param type String (RECIRG, RECIVC, REALRG, REALVC)
     */
    public void plot(Collection collectionSelected, String type, String workingDirectoryName) {

        int totalSets = collectionSelected.getDatasetCount();
        int horizontalDisplacement = 200;

        plottedData.removeAllSeries();
        // create collection of series to plot

        if (type.equals("RECIRG")) {
            for (int i = 0; i < totalSets; i++){
                Dataset temp = collectionSelected.getDataset(i);
                temp.clearNormalizedKratkyReciRgData();
                if (temp.getInUse()){
                    temp.createNormalizedKratkyReciRgData();
                }
                plottedData.addSeries(temp.getNormalizedKratkyReciRgData());
            }
            horizontalDisplacement += 50;
        } else if (type.equals("RECIVC")) {
            horizontalDisplacement += 75;
        } else if (type.equals("REALRG")) {
            for (int i = 0; i < totalSets; i++){
                Dataset temp = collectionSelected.getDataset(i);
                temp.clearNormalizedKratkyRealRgData();
                if (temp.getInUse()){
                    temp.createNormalizedKratkyRealRgData();
                }
                plottedData.addSeries(temp.getNormalizedKratkyRealRgData());  // should add an empty Series
            }
            horizontalDisplacement += 100;
        } else if (type.equals("REALVC")) {
            horizontalDisplacement += 120;
        }

        chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "q/Rg",                        // domain axis label
                "I(q)/I(0)*qRg^2",                // range axis label
                plottedData,                 // data
                PlotOrientation.VERTICAL,
                true,                       // include legend
                false,
                false
        );
        // chart.setTitle("SC\u212BTTER \u2263 Real Space Normal Kratky Plot");

        chart.setTitle("SC\u212BTTER \u2263 " + chartTitle);
        frame.setTitle(chartTitle);
        chart.getTitle().setPaint(Constants.SteelBlue);

        if (vctag.matcher(chartTitle).find()){
            xLabel = "q\u00B2\u2217Vc";
            yLabel = "I(q)/I(0)\u2217q\u00B2\u2217Vc";
            if (realtag.matcher(chartTitle).find()){
                chart.getTitle().setPaint(Constants.DodgerBlue);
            }
        } else {
            xLabel = "q\u2217Rg";
            yLabel = "I(q)/I(0)\u2217(q\u2217Rg)\u00B2";
            if (realtag.matcher(chartTitle).find()){
                chart.getTitle().setPaint(Constants.IndianRed);
            }
        }

        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 0, 0);

        upper = plottedData.getRangeUpperBound(true);

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
                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.1), max+Math.abs(0.1*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.1), maxX+Math.abs(0.1*maxX));
            }
        };

        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis(xLabel);
        final NumberAxis rangeAxis = new NumberAxis(yLabel);

        domainAxis.setLabel(xLabel);
        rangeAxis.setLabel(yLabel);

        rangeAxis.setRange(0, plottedData.getRangeUpperBound(true) + 0.1*plottedData.getRangeUpperBound(true));
        chart.getLegend().setVisible(false);
        domainAxis.setAutoRangeIncludesZero(true);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        //make crosshair visible
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        plot.addDomainMarker(xMarker);
        plot.addRangeMarker(yMarker);

        //plot.setDataset(plottedData);

        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        double negativePointSize, pointSize;

        for (int i=0; i < totalSets; i++) {
            Dataset tempDataset = collectionSelected.getDataset(i);
            if (tempDataset.getInUse()) {
                pointSize = tempDataset.getPointSize();
                negativePointSize = -0.5*pointSize;
                renderer1.setSeriesShape(i, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
                renderer1.setSeriesLinesVisible(i, false);
                renderer1.setSeriesPaint(i, tempDataset.getColor());
                renderer1.setSeriesShapesFilled(i, tempDataset.getBaseShapeFilled());
                renderer1.setSeriesOutlinePaint(i, tempDataset.getColor());
                renderer1.setSeriesOutlineStroke(i, tempDataset.getStroke());
            }
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

        frame.addWindowListener(new WindowAdapter() {
            public void WindowClosing(WindowEvent e) {
                locationOfWindow = frame.getLocation();
                frame.dispose();
            }
        });


        //frame.setLocation(new Point(frame.getLocation().getX(), frame.getLocation().getY() + horizontalDisplacement));

        //frame.getChartPanel().setSize(600, 500);
        //chartPanel.setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.getContentPane().add(chartPanel);
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.pack();
        frame.setVisible(true);
        frame.setSize(688, 454);
    }

    public void clearAll(){
        plottedData.removeAllSeries();
        frame.removeAll();
    }


}
