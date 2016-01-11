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
public class PlotDataSingleton {

    JFreeChart chart;
    public XYPlot plot;
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    XYLineAndShapeRenderer mergedRenderer = new XYLineAndShapeRenderer();
    //public XYSeriesCollection newDataset = new XYSeriesCollection();
    public XYSeriesCollection plottedDatasets = new XYSeriesCollection();
    public XYSeriesCollection mergedDataset = new XYSeriesCollection();

    ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 LOG10 INTENSITY PLOT", chart);
    JFrame jframe = new JFrame("SC\u212BTTER \u2263 LOG10 INTENSITY PLOT");

    public XYLineAndShapeRenderer renderer1;
    boolean crosshair = true;

    Shape elipse6 = new Ellipse2D.Double(-3, -3, 6, 6);

    CustomXYToolTipGenerator cttGen = new CustomXYToolTipGenerator();
    double upper, dupper;
    double lower, dlower;

    private static PlotDataSingleton singleton = new PlotDataSingleton( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private PlotDataSingleton(){

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
    public static PlotDataSingleton getInstance( ) {
        return singleton;
    }

    /* Other methods protected by singleton-ness */
    protected static void demoMethod( ) {
        System.out.println("demoMethod for singleton");
    }

    public void plot(Collection collection, String workingDirectoryName) {
        //
        // previously miniCollection was being plotted
        // this is really the active datasets,
        // in the constructor for collection, miniCollection is derived from dataset (same pointer)
        //
        //newDataset = collection.getMiniCollection();  //XYSeries Collection for plotting

        // option would be to add datasets to plottedDatasets
        // datasets not checked will be addes as emptySeries

        int totalSets = collection.getDatasetCount();
        plottedDatasets = new XYSeriesCollection();  // spinners will always modify the plottedDataset series
        for (int i=0; i<totalSets; i++){
            if (collection.getDataset(i).getInUse()){
                plottedDatasets.addSeries(collection.getDataset(i).getData());
            } else {
                plottedDatasets.addSeries(new XYSeries(collection.getDataset(i).getFileName()));
            }
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
        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        mergedRenderer.setBaseShapesVisible(true);
        mergedRenderer.setBasePaint(Color.RED);
        mergedRenderer.setBaseShape(elipse6);
        mergedRenderer.setBaseLinesVisible(false);

//        plot.setDataset(1,newDataset);
        plot.setDataset(1,plottedDatasets);
        plot.setRenderer(1,renderer1);
        plot.setDataset(0, mergedDataset);
        plot.setRenderer(0, mergedRenderer);       //render as a line

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


    public boolean isVisible(){
        return jframe.isVisible();
    }
}
