package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by robertrambo on 18/01/2016.
 */
public class BuffersSamplesPlot {

    private Collection collection;
    private JFreeChart chart;
    private XYPlot plot;
    private XYLineAndShapeRenderer dataRenderer = new XYLineAndShapeRenderer();
    private XYLineAndShapeRenderer mergedRenderer = new XYLineAndShapeRenderer();
    private XYSeriesCollection dataSets = new XYSeriesCollection();
    private XYSeriesCollection mergedSets = new XYSeriesCollection();

    private ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 INTENSITY PLOT", chart);
    public JFrame jframe = new JFrame("SC\u212BTTER \u2263 INTENSITY PLOT");

    private Container content = jframe.getContentPane();
    private double upper, dupper;
    private double lower, dlower;
    private XYSeries medianSet;
    private XYSeries averageSet;


    public BuffersSamplesPlot(Collection collection){
        this.collection = collection;

        ArrayList<XYSeries> stuff = createMedianAverageXYSeries();

        medianSet = stuff.get(0);
        averageSet = stuff.get(2);
    }

    public void makePlot (boolean isLog) throws IOException {
        //
        // previously miniCollection was being plotted
        // this is really the active datasets,
        // in the constructor for collection, miniCollection is derived from dataset (same pointer)
        //
        dataSets = new XYSeriesCollection();
        mergedSets = new XYSeriesCollection();

        int total = collection.getDatasets().size();
        String yquote = "I(q)";
        for (int i=0; i<total; i++){
            if (isLog){
                yquote = "log[I(q)]";
                dataSets.addSeries(collection.getDataset(i).getData()); // log10
            } else {
                dataSets.addSeries(collection.getDataset(i).getAllData());
            }
        }

        mergedSets.addSeries(medianSet);
        mergedSets.addSeries(averageSet);

        chart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "q",                        // domain axis label
                "log[I(q)]",                // range axis label
                dataSets,                 // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
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

        if (mergedSets.getSeries(1).getItemCount() > 1) {
            TextTitle legendText = new TextTitle("MEDIAN");
            legendText.setPosition(RectangleEdge.BOTTOM);

            chart.addSubtitle(legendText);
            legendText.setPaint(Color.RED);

            TextTitle legendText2 = new TextTitle("AVERAGE");
            legendText2.setPosition(RectangleEdge.BOTTOM);
            chart.addSubtitle(legendText2);

            legendText2.setPaint(Color.CYAN);
        }

        upper = dataSets.getRangeUpperBound(true);
        lower = dataSets.getRangeLowerBound(true);

        double tempMin, tempMax;
        dupper = -10000.0;
        dlower = 10000.0;

        for (int i=0; i < total; i++){
            tempMax = collection.getDataset(i).getMaxq();
            tempMin = collection.getDataset(i).getMinq();
            if (tempMax > dupper){
                dupper = tempMax;
            }
            if (tempMin < dlower){
                dlower = tempMin;
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
                double min = lower;
                double max = upper;
                double minX = 10;
                double maxX = 0;
                double tempYMin;
                double tempYmax;
                double tempXMin;
                boolean isVisible;

                for (int i=0; i < seriesCount; i++){
                    //check if visible, if visible, get min and max I value
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

                super.getChart().getXYPlot().getRangeAxis().setRange(min-Math.abs(min*0.2), max+Math.abs(0.25*max));
                super.getChart().getXYPlot().getDomainAxis().setRange(minX-Math.abs(minX*0.2),maxX+Math.abs(0.1*maxX));

            }
        };

        plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("Log Intensity");
        String quote = "q (\u212B\u207B\u00B9)";
        domainAxis.setLabel(quote);


        rangeAxis.setLabel(yquote);
        rangeAxis.setAutoRange(false);

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
        dataRenderer = (XYLineAndShapeRenderer) plot.getRenderer();
        dataRenderer.setBaseShapesVisible(true);

        plot.setDataset(1,dataSets);
        plot.setRenderer(1,dataRenderer);
        plot.setDataset(0, mergedSets);
        plot.setRenderer(0, mergedRenderer);       //render as a line

        mergedRenderer.setBaseShapesVisible(true);
        mergedRenderer.setSeriesShape(0, Constants.Ellipse4);
        mergedRenderer.setSeriesShape(1, Constants.Ellipse4);
        mergedRenderer.setBaseLinesVisible(false);
        mergedRenderer.setBaseShapesFilled(true);
        mergedRenderer.setSeriesPaint(0, Color.RED);
        mergedRenderer.setSeriesPaint(1, Color.CYAN);


        //set dot size for all series
        int locale =0;
        double offset;
        for (int i=0; i < collection.getDatasets().size(); i++){
            Dataset tempData = collection.getDataset(i);
            offset = -0.5*tempData.getPointSize();
            dataRenderer.setSeriesShape(i, new Ellipse2D.Double(offset, offset, tempData.getPointSize(), tempData.getPointSize()));
            dataRenderer.setSeriesLinesVisible(i, false);
            dataRenderer.setSeriesPaint(i, tempData.getColor());
            dataRenderer.setSeriesShapesFilled(i, tempData.getBaseShapeFilled());
            dataRenderer.setSeriesVisible(i, tempData.getInUse());
            dataRenderer.setSeriesOutlineStroke(i, tempData.getStroke());
        }
        plot.setDomainZeroBaselineVisible(false);

        frame.getContentPane().add(chartPanel);
        frame.getChartPanel().setDisplayToolTips(true);


        File theDir = new File(collection.getWORKING_DIRECTORY_NAME());

        if (!theDir.exists()) {
            throw new IOException("Directory does not exists ");
        }

        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(collection.getWORKING_DIRECTORY_NAME()));


        frame.pack();

        if (isLog){
            frame.setLocation(50, 70);
        }

        jframe.setSize(688, 454);
        content.add(frame.getChartPanel());
        jframe.setVisible(true);

    }

    private ArrayList<XYSeries> createMedianAverageXYSeries(){
        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();

        // calculate Average and Median for set

        ArrayList<XYSeries> median_reduced_set = StatMethods.medianDatasets(collection);
        ArrayList<XYSeries> averaged = StatMethods.weightedAverageDatasets(collection);

        String name = "median_set";

        XYSeries medianAllData = null;
        XYSeries medianAllDataError = null;

        try {
            medianAllData = (XYSeries) median_reduced_set.get(0).clone();
            medianAllData.setKey(name);
            medianAllDataError = (XYSeries) median_reduced_set.get(1).clone();
            medianAllDataError.setKey(name);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        returnMe.add(medianAllData);          // 0
        returnMe.add(medianAllDataError);     // 1
        returnMe.add(averaged.get(0));        // 2
        returnMe.add(averaged.get(1));        // 3

        return returnMe;
    }
}
