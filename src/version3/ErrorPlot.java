package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Created by robertrambo on 12/01/2016.
 */
public class ErrorPlot {

    static JFreeChart chart;
    private static XYPlot plot;

    private static YIntervalSeriesCollection plottedDatasets;

    private static Collection inUseCollection;
    static ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 LOG10 INTENSITY PLOT WITH ERROR", chart);
    static JFrame jframe = new JFrame("SC\u212BTTER \u2263 LOG10 INTENSITY PLOT WITH ERROR");

    private static XYErrorRenderer renderer;
    boolean crosshair = true;

    CustomXYToolTipGenerator cttGen = new CustomXYToolTipGenerator();
    private static double upper;
    private static double dupper;
    private static double lower;
    private static double dlower;
    private static Point locationOfWindow;

    private static ErrorPlot singleton = new ErrorPlot( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private ErrorPlot(){

        locationOfWindow = new Point(300,300);

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
    public static ErrorPlot getInstance( ) {
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

        plottedDatasets = new YIntervalSeriesCollection();

        for (int i = 0; i < totalSets; i++){
            Dataset temp = inUseCollection.getDataset(i);
            temp.clearPlottedLog10ErrorData();
            if (temp.getInUse()){
                temp.scalePlottedLogErrorData();
            }
            plottedDatasets.addSeries(temp.getPlottedLog10ErrorData());
        }

        chart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "q",                             // domain axis label
                "log[I(q)]",                     // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        chart.setTitle("SC\u212BTTER \u2263 Intensity Plot");
        chart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        chart.getTitle().setPaint(Constants.SteelBlue);
        chart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getTitle().setMargin(10, 10, 4, 0);
        chart.setBorderVisible(false);

        upper = collection.getMaxI();
        lower = collection.getMinI(); // will always be with regards to entire dataset, not just visible

        double tempMin, tempMax;
        dupper = -10000.0;
        dlower = 10000.0;
        for (int i=0; i < totalSets; i++){
            if (collection.getDataset(i).getInUse()){
                tempMax = collection.getDataset(i).getMaxq();
                tempMin = collection.getDataset(i).getMinq();
                if (tempMax > dupper){
                    dupper = tempMax;
                }
                if (tempMin < dlower){
                    dlower = tempMin;
                }
            }
        }

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
                    // renderer 0 is merged data
                    // renderer 1 is plotted data
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
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("Log Intensity");
        String quote = "q (\u212B\u207B\u00B9)";
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);
        domainAxis.setLabel(quote);
        quote = "log[I(q)]";

        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRange(false);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);
        rangeAxis.setRange(lower-lower*0.03, upper+0.1*upper);
        rangeAxis.setAutoRangeStickyZero(false);

        domainAxis.setRange(dlower, dupper);
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

        renderer = new XYErrorRenderer();
        renderer.setBaseLinesVisible(false);
        renderer.setBaseShapesVisible(true);
        renderer.setErrorPaint(new GradientPaint(1.0f, 2.0f, Constants.RedGray, 3.0f, 4.0f,
                Constants.RedGray));

        plot.setDataset(plottedDatasets);
        plot.setRenderer(renderer);

        //set dot size for all series
        double offset;
        for (int i=0; i < totalSets; i++){
            Dataset tempData = inUseCollection.getDataset(i);
            offset = -0.5*tempData.getPointSize();
            renderer.setSeriesShape(i, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
            renderer.setSeriesLinesVisible(i, false);
            renderer.setSeriesPaint(i, tempData.getColor());
            renderer.setSeriesShapesFilled(i, true);
            renderer.setSeriesVisible(i, tempData.getInUse());
            renderer.setSeriesOutlineStroke(i, tempData.getStroke());
            renderer.setSeriesStroke(i, tempData.getStroke());
        }

        plot.setDomainZeroBaselineVisible(false);

        frame.getChartPanel().setChart(chartPanel.getChart());
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.getChartPanel().setDisplayToolTips(false);
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
    }

    public void setNotify(boolean status){
        frame.getChartPanel().getChart().setNotify(status);
    }

    public boolean isVisible(){
        return jframe.isVisible();
    }

    public static void changeVisibleSeries(int index, boolean flag){

        boolean isVisible = renderer.isSeriesVisible(index);

        if (isVisible){
            renderer.setSeriesVisible(index, flag);
        } else {
            Dataset temp = inUseCollection.getDataset(index);
            temp.scalePlottedLogErrorData();
            renderer.setSeriesVisible(index, flag);
        }
    }

    public void closeWindow(){
        locationOfWindow = jframe.getLocation();
        jframe.dispatchEvent(new WindowEvent(jframe, WindowEvent.WINDOW_CLOSING));
    }

    public void changeColor(int id, Color newColor, float thickness, int pointsize){
        renderer.setSeriesPaint(id, newColor);

        double offset = -0.5*pointsize;
        renderer.setSeriesShape(id, new Ellipse2D.Double(offset, offset, pointsize, pointsize));
        renderer.setSeriesOutlineStroke(id, new BasicStroke(thickness));
    }

}
