package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Created by robertrambo on 12/01/2016.
 */
public class PowerLawPlot {

    private static JFreeChart chart;
    private static XYPlot plot;
    private static XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    private static XYSeriesCollection plottedDatasets = new XYSeriesCollection();

    private static Collection inUseCollection;
    private static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 POWER-LAW INTENSITY PLOT", chart);
    private static JFrame jframe = new JFrame("SC\u212BTTER \u2263 POWER-LAW INTENSITY PLOT");

    private static XYLineAndShapeRenderer renderer1;
    private static boolean crosshair = true;

    private static Shape elipse6 = new Ellipse2D.Double(-3, -3, 6, 6);

    CustomXYToolTipGenerator cttGen = new CustomXYToolTipGenerator();
    private static double upper;
    private static double lower;

    private static PowerLawPlot singleton = new PowerLawPlot( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private PowerLawPlot(){

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

    /* Static 'instance' method */
    public static PowerLawPlot getInstance( ) {
        return singleton;
    }

    public static void plot(Collection collection, String workingDirectoryName) {
        //
        // previously miniCollection was being plotted
        // this is really the active datasets,
        // in the constructor for collection, miniCollection is derived from dataset (same pointer)
        //
        //newDataset = collection.getMiniCollection();  //XYSeries Collection for plotting

        // option would be to add datasets to plottedDatasets
        // datasets not checked will be addes as emptySeries

        inUseCollection = collection;
        int totalSets = collection.getDatasetCount();
        plottedDatasets = new XYSeriesCollection();  // spinners will always modify the plottedDataset series

        for (int i = 0; i < totalSets; i++){
            Dataset temp = inUseCollection.getDataset(i);
            temp.clearPlottedPowerLaw();
            if (temp.getInUse()){
                temp.scalePlottedPowerLaw();
            }
            plottedDatasets.addSeries(temp.getPlottedPowerLaw());  // should add an empty Series
        }

        chart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "log10[q]",                         // domain axis label
                "log10[I(q)]",                      // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.setTitle("SC\u212BTTER \u2263 Power-Law Intensity Plot");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);

        upper = collection.getMaxI();
        lower = collection.getMinI(); // will always be with regards to entire dataset, not just visible

        double tempMin, tempMax;

        ChartPanel chartPanel = new ChartPanel(chart){
            @Override
            public void restoreAutoBounds(){
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);

                int seriesCount = super.getChart().getXYPlot().getDataset(1).getSeriesCount();
                int maxIndex;
                double min = 10;
                double max = -100;
                double minX = 10;
                double maxX = 0;
                double tempYMin;
                double tempYmax;
                double tempXMin;
                boolean isVisible;

                for (int i=0; i < seriesCount; i++){
                    // check if visible, if visible, get min and max I value
                    isVisible = super.getChart().getXYPlot().getRenderer(1).isSeriesVisible(i);

                    if (isVisible){
                        maxIndex = super.getChart().getXYPlot().getDataset(1).getItemCount(i)-3;
                        tempYmax = super.getChart().getXYPlot().getDataset(1).getYValue(i, 0);

                        if (tempYmax > max){
                            max = tempYmax;
                        }

                        for (int j=0; j< maxIndex;j++){
                            tempYMin = super.getChart().getXYPlot().getDataset(1).getYValue(i, j);
                            tempXMin = super.getChart().getXYPlot().getDataset(1).getXValue(i, j);
                            if (tempYMin < min){
                                min = tempYMin;
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

                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.1), max+Math.abs(0.25*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.2),maxX+Math.abs(0.1*maxX));
            }
        };

        plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("log10[q]");
        final NumberAxis rangeAxis = new NumberAxis("log10 Intensity");
        String quote = "log10[q (\u212B\u207B\u00B9)]";
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);
        domainAxis.setLabel(quote);
        quote = "log10[I(q)]";

        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRange(false);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);

        //rangeAxis.setRange(lower-lower*0.03, upper+0.1*upper);
        rangeAxis.setLowerBound(plottedDatasets.getRangeLowerBound(true));
        rangeAxis.setUpperBound(plottedDatasets.getRangeUpperBound(true)*1.1);
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

        //set dot size for all series
        double offset;
        for (int i=0; i < collection.getDatasets().size(); i++){
            Dataset tempData = collection.getDataset(i);
            offset = -0.5*tempData.getPointSize();
            renderer1.setSeriesShape(i, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
            renderer1.setSeriesLinesVisible(i, false);
            renderer1.setSeriesPaint(i, tempData.getColor());
            renderer1.setSeriesShapesFilled(i, tempData.getBaseShapeFilled());
            renderer1.setSeriesVisible(i, tempData.getInUse());
            renderer1.setSeriesOutlineStroke(i, tempData.getStroke());
        }
        plot.setDomainZeroBaselineVisible(false);

        frame.getChartPanel().setChart(chartPanel.getChart());
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.getChartPanel().setDisplayToolTips(false);
        frame.pack();

        jframe.setMinimumSize(new Dimension(640,480));
        Container content = jframe.getContentPane();
        content.add(frame.getChartPanel());
        jframe.setLocation(100,100);
        jframe.setVisible(true);
    }

    public void setNotify(boolean state){
        frame.getChartPanel().getChart().setNotify(state);
    }

    public boolean isVisible(){
        return jframe.isVisible();
    }

    public void changeVisibleSeries(int index, boolean flag){

        boolean isVisible = renderer1.isSeriesVisible(index);

        if (isVisible){
            renderer1.setSeriesVisible(index, flag);
        } else {
            Dataset temp = inUseCollection.getDataset(index);
            temp.scalePlottedPowerLaw();
            renderer1.setSeriesVisible(index, flag);
        }
    }

    public void changeColor(int id, Color newColor, float thickness, int pointsize){
        renderer1.setSeriesPaint(id, newColor);

        double offset = -0.5*pointsize;
        renderer1.setSeriesShape(id, new Ellipse2D.Double(offset, offset, pointsize, pointsize));
        renderer1.setSeriesOutlineStroke(id, new BasicStroke(thickness));
    }

}