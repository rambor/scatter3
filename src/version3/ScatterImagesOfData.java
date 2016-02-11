package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.HorizontalAlignment;


import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;

/**
 * Created by robertrambo on 06/02/2016.
 */
public class ScatterImagesOfData {

    private JFreeChart kratkyChart;
    private static ValueMarker yMarker = new ValueMarker(1.1);
    private static ValueMarker xMarker = new ValueMarker(1.7320508);

    private JFreeChart prChart;
    private JFreeChart qIqChart;
    private JFreeChart logIqChart;
    private JFreeChart guinierChart;

    private Dataset dataset;
    public ScatterImagesOfData(Dataset data){
        this.dataset = data;
    }

    public void createAndWriteKratkyChart(String name, String directory){

        XYSeriesCollection plottedData = new XYSeriesCollection();
        String xLabel, yLabel, chartTitle="";
        boolean dimensionless = false;

        if (dataset.getGuinierRg() > 0 && dataset.getGuinierIzero() > 0){
            xLabel = "q\u2217Rg";
            yLabel = "I(q)/I(0)\u2217(q\u2217Rg)\u00B2";

            if (dataset.getNormalizedKratkyReciRgData().getItemCount() == 0) {
                chartTitle = "Dimensionless Kratky Plot (Guinier-based)";
                dataset.createNormalizedKratkyReciRgData();
            }

            plottedData.addSeries(new XYSeries(dataset.getFileName()));
            XYSeries tempSeries = dataset.getNormalizedKratkyReciRgData();
            int totalIn = tempSeries.getItemCount();
            for (int i=0; i<totalIn; i++){
                if ( tempSeries.getX(i).doubleValue() <= 10 ){
                    plottedData.getSeries(0).add(tempSeries.getDataItem(i));
                }
            }

            dimensionless = true;
        } else {
            xLabel = "q";
            yLabel = "I(q)\u2217q\u00B2";
            chartTitle = "Kratky Plot";
            XYSeries series = new XYSeries(dataset.getFileName());

            int beginAt = dataset.getStart()-1, endAt = dataset.getEnd()-1;

            int allDataStart = dataset.getAllData().indexOf(dataset.getOriginalPositiveOnlyData().getX(beginAt));
            int allDataEnd = dataset.getAllData().indexOf(dataset.getOriginalPositiveOnlyData().getX(endAt));

            for (int i=allDataStart; i<allDataEnd; i++){
                XYDataItem tempItem = dataset.getAllData().getDataItem(i);
                series.add(tempItem.getX(), tempItem.getYValue()*tempItem.getXValue()*tempItem.getXValue());
                if (tempItem.getXValue() > 0.25){
                    break;
                }
            }

            plottedData.addSeries(series);
        }


        kratkyChart = ChartFactory.createXYLineChart(
                "",                          // chart title
                "q/Rg",                      // domain axis label
                "",                          // range axis label
                plottedData,                 // data
                PlotOrientation.VERTICAL,
                true,                       // include legend
                false,
                false
        );

        kratkyChart.setTitle("SC\u212BTTER \u2263 " + chartTitle);
        kratkyChart.getTitle().setPaint(Constants.SteelBlue);
        kratkyChart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        kratkyChart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        kratkyChart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        kratkyChart.getTitle().setMargin(10, 10, 0, 0);

        final XYPlot plot = kratkyChart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis(xLabel);
        final NumberAxis rangeAxis = new NumberAxis(yLabel);
        rangeAxis.setTickLabelFont(Constants.FONT_16);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_16);
        domainAxis.setLabelFont(Constants.BOLD_16);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);


        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        if (dimensionless) {
            plot.addDomainMarker(xMarker);
            plot.addRangeMarker(yMarker);
        }

        XYLineAndShapeRenderer renderer1;
        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        double negativePointSize, pointSize;

        pointSize = dataset.getPointSize();
        negativePointSize = -0.5*pointSize;
        renderer1.setSeriesShape(0, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
        renderer1.setSeriesLinesVisible(0, false);
        renderer1.setSeriesPaint(0, dataset.getColor());
        renderer1.setSeriesShapesFilled(0, dataset.getBaseShapeFilled());
        renderer1.setSeriesOutlinePaint(0, dataset.getColor());
        renderer1.setSeriesOutlineStroke(0, dataset.getStroke());

        String outputName = directory + "/" + name + "_kratkyPlot.png";

        try {
            ChartUtilities.saveChartAsPNG(new File(outputName), kratkyChart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void createAndWritePrChart(String name, String directory){

        XYSeriesCollection plottedCollection;;
        XYSeriesCollection splineCollection = new XYSeriesCollection();

        plottedCollection = new XYSeriesCollection();
        splineCollection.addSeries(dataset.getRealSpaceModel().getPrDistribution());

        prChart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "r",                        // domain axis label
                "P(r)",                // range axis label
                plottedCollection,                 // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,
                false
        );

        final XYPlot plot = prChart.getXYPlot();

        final NumberAxis domainAxis = new NumberAxis("r, \u212B");
        final NumberAxis rangeAxis = new NumberAxis("P(r)");

        rangeAxis.setTickLabelFont(Constants.FONT_16);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_16);
        domainAxis.setLabelFont(Constants.BOLD_16);

        domainAxis.setAutoRangeIncludesZero(true);
        domainAxis.setAutoRange(true);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAxisLineVisible(true);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setBackgroundAlpha(0.0f);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setBackgroundAlpha(0.0f);
        plot.setRangeZeroBaselineVisible(true);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYSplineRenderer splineRend = new XYSplineRenderer();
        splineRend.setBaseShapesVisible(false);

        plot.setDataset(0, splineCollection);  //Moore Function
        plot.setRenderer(0, splineRend);       //render as a line

        splineRend.setSeriesStroke(0, new BasicStroke(4.0f));
        splineRend.setSeriesPaint(0, dataset.getColor().darker()); // make color slight darker

        String outputName = directory + "/" + name + "_prPlot.png";

        try {
            ChartUtilities.saveChartAsPNG(new File(outputName), prChart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createAndWriteQIQChart(String name, String directory){
        XYSeriesCollection plottedDatasets = new XYSeriesCollection();  // spinners will always modify the plottedDataset series

        dataset.clearPlottedQIQData();
        dataset.scalePlottedQIQData();
        plottedDatasets.addSeries(dataset.getPlottedQIQDataSeries());

        qIqChart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "q",                        // domain axis label
                "I(q)*q",                // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                true,                       // include legend
                true,
                false
        );

        qIqChart.setTitle("SC\u212BTTER \u2263 Total Scattered Intensity Plot");
        qIqChart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        qIqChart.getTitle().setPaint(Constants.SteelBlue);
        qIqChart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        qIqChart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        qIqChart.getTitle().setMargin(10, 10, 0, 0);

        final XYPlot plot = qIqChart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("I(q)*q");

        String quote = "q, \u212B \u207B\u00B9";
        domainAxis.setLabel(quote);
        domainAxis.setAutoRangeStickyZero(true);
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);

        quote = "q \u00D7 I(q)";
        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRangeStickyZero(true);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);

        qIqChart.getLegend().setVisible(false);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);
        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);

        double negativePointSize = -0.5*dataset.getPointSize();
        renderer1.setSeriesShape(0, new Ellipse2D.Double(negativePointSize, negativePointSize, dataset.getPointSize(), dataset.getPointSize()));
        renderer1.setSeriesShapesFilled(0, dataset.getBaseShapeFilled());
        renderer1.setSeriesLinesVisible(0, false);
        renderer1.setSeriesVisible(0, dataset.getInUse());
        renderer1.setSeriesPaint(0, dataset.getColor());
        renderer1.setSeriesOutlinePaint(0, dataset.getColor());
        renderer1.setSeriesOutlineStroke(0, dataset.getStroke());

        String outputName = directory + "/" + name + "_qIqPlot.png";

        try {
            ChartUtilities.saveChartAsPNG(new File(outputName), qIqChart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createAndWriteLog10PlotWithErrorsChart(String name, String directory){
        YIntervalSeriesCollection plottedDatasets = new YIntervalSeriesCollection();

        dataset.clearPlottedLog10ErrorData();
        dataset.scalePlottedLogErrorData();
        plottedDatasets.addSeries(dataset.getPlottedLog10ErrorData());

        logIqChart = ChartFactory.createXYLineChart(
                "Main Plot",                     // chart title
                "q",                             // domain axis label
                "log[I(q)]",                     // range axis label
                plottedDatasets,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        logIqChart.setTitle("SC\u212BTTER \u2263 Intensity Plot");
        logIqChart.getTitle().setFont(new java.awt.Font("Times", 1, 20));
        logIqChart.getTitle().setPaint(Constants.SteelBlue);
        logIqChart.getTitle().setTextAlignment(HorizontalAlignment.LEFT);
        logIqChart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        logIqChart.getTitle().setMargin(10, 10, 4, 0);
        logIqChart.setBorderVisible(false);

        final XYPlot plot = logIqChart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("q");
        final NumberAxis rangeAxis = new NumberAxis("Log Intensity");
        String quote = "q (\u212B\u207B\u00B9)";
        domainAxis.setLabelFont(Constants.BOLD_16);
        domainAxis.setTickLabelFont(Constants.FONT_12);
        domainAxis.setLabel(quote);
        quote = "log[I(q)]";

        rangeAxis.setLabel(quote);
        rangeAxis.setAutoRange(true);
        rangeAxis.setLabelFont(Constants.BOLD_16);
        rangeAxis.setTickLabelFont(Constants.FONT_12);
        rangeAxis.setAutoRangeStickyZero(false);
        domainAxis.setAutoRangeStickyZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        plot.setBackgroundAlpha(0.0f);

        plot.setDomainCrosshairLockedOnData(false);
        plot.setOutlineVisible(false);
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);

        XYErrorRenderer renderer = new XYErrorRenderer();
        renderer.setBaseLinesVisible(false);
        renderer.setBaseShapesVisible(true);
        renderer.setErrorPaint(new GradientPaint(1.0f, 2.0f, Constants.RedGray, 3.0f, 4.0f,
                Constants.RedGray));

        plot.setDataset(plottedDatasets);
        plot.setRenderer(renderer);

        //set dot size for all series
        double offset = -0.5*dataset.getPointSize();
        renderer.setSeriesShape(0, new Ellipse2D.Double(offset, offset, dataset.getPointSize(), dataset.getPointSize()));
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesPaint(0, dataset.getColor());
        renderer.setSeriesShapesFilled(0, true);
        renderer.setSeriesVisible(0, dataset.getInUse());
        renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
        renderer.setSeriesOutlinePaint(0, Color.BLACK);
        renderer.setSeriesStroke(0, dataset.getStroke());

        plot.setDomainZeroBaselineVisible(false);

        String outputName = directory + "/" + name + "_Log10IntensityErrorPlot.png";

        try {
            ChartUtilities.saveChartAsPNG(new File(outputName), logIqChart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
