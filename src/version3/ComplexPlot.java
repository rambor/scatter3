package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Created by robertrambo on 14/01/2016.
 */
public class ComplexPlot {

    public JFreeChart chart;
    public JFreeChart residualsChart;
    public JFreeChart combChart;
    public int selectedID;
    public String filename;
    private String workingDirectoryName;
    private XYSeriesCollection newDataset = new XYSeriesCollection();
    private XYSeriesCollection residualsDataset = new XYSeriesCollection();
    private Collection collection;

    CombinedDomainXYPlot combinedPlot;
    ChartFrame frame = new ChartFrame("SC\u212BTTER \u2263 Guinier Plot", chart);

    ChartPanel chartPanel = new ChartPanel(chart);
    ChartFrame residualsFrame = new ChartFrame("Residuals", residualsChart);

    JFrame f = new JFrame("Guinier");
    Container content = f.getContentPane();

    XYLineAndShapeRenderer renderer1;
    XYLineAndShapeRenderer renderer2;


    public ComplexPlot(Collection collection, String dir) {
        this.collection = collection;
        this.workingDirectoryName = dir;


    }

    public ChartFrame plot(XYSeriesCollection dataset, XYSeriesCollection residualsData, Color datasetColor, int id, String name, BasicStroke stroke) {
        filename = name;
        selectedID = id;

        newDataset = dataset;
        chart = ChartFactory.createXYLineChart(
                "SC\u212BTTER \u2263 Guinier fit",                // chart title
                "",                    // domain axis label
                "ln I(q)",                  // range axis label
                newDataset,               // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,
                false
        );

        residualsDataset = residualsData;
        residualsChart = ChartFactory.createXYLineChart(
                "Residuals",                // chart title
                "",                    // domain axis label
                "residuals",                  // range axis label
                residualsDataset,               // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,
                false
        );

        final XYPlot residuals = residualsChart.getXYPlot();
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("");
        final NumberAxis rangeAxis = new NumberAxis("log [I(q)]");


        Font fnt = new Font("SansSerif", Font.BOLD, 15);
        domainAxis.setLabelFont(fnt);
        rangeAxis.setLabelFont(fnt);
        domainAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRangeStickyZero(false);
        //String quote = "q (\u212B \u207B)";
        String quote = "q (\u212B\u207B\u00B9)";
        domainAxis.setLabel(quote);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        residuals.setDomainAxis(domainAxis);
        plot.setBackgroundPaint(null);
        residuals.setBackgroundPaint(null);
        renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(false);

        renderer1.setSeriesShapesVisible(0, true);
        renderer1.setSeriesLinesVisible(0, false);
        renderer1.setSeriesShapesFilled(0, false);
        renderer1.setSeriesShape(0, new Ellipse2D.Double(0, 0, 4.0, 4.0));
        renderer1.setSeriesPaint(0, Color.RED);

        //renderer1.setSeriesStroke(0, stroke);

        // Series
        renderer1.setSeriesShapesVisible(1, true);
        renderer1.setSeriesLinesVisible(1, false);
        renderer1.setSeriesShapesFilled(1, false);
        renderer1.setSeriesPaint(1, datasetColor);
        renderer1.setSeriesShape(1, new Ellipse2D.Double(0, 0, 8.0, 8.0));
        renderer1.setSeriesOutlineStroke(1, stroke);

        // renderer1.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

        renderer2 = (XYLineAndShapeRenderer) residuals.getRenderer();
        renderer2.setBaseShapesVisible(true);
        renderer2.setBaseShapesFilled(false);

//        renderer2.setSeriesShapesVisible(1, true);
//        renderer2.setSeriesShapesVisible(1, false);
//        renderer2.setSeriesPaint(1, Color.red);
//        renderer2.setSeriesStroke(1, stroke);
        renderer2.setSeriesLinesVisible(0, false);
        renderer2.setSeriesPaint(0, Color.BLACK);
        renderer2.setSeriesShape(0, new Ellipse2D.Double(0, 0, 8.0, 8.0));

        plot.getAnnotations().size();

        //plot.setBackgroundAlpha(0.0f);

        combinedPlot = new CombinedDomainXYPlot(new NumberAxis("2"));
        combinedPlot.setDomainAxis(domainAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.add(plot, 1);
        combinedPlot.add(residuals, 1);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        combChart = new JFreeChart("Complexation Determination Chart", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

        combChart.removeLegend();
        combChart.setBackgroundPaint(Color.WHITE);

        frame.setBackground(Color.WHITE);

        frame.getChartPanel().setSize(600, 400);
        frame.getChartPanel().setChart(combChart);
        frame.getChartPanel().setDisplayToolTips(true);
        frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
        frame.pack();

        return frame;
    }

}