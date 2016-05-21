package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

/**
 * Created by robertrambo on 20/05/2016.
 */

public class PrDistributionSearch {


    private JPanel mainPrDistributionPanel;
    private JPanel topPanel;
    private static JFreeChart chart;
    private static ChartFrame frame = new ChartFrame("", chart);
    private ChartPanel outPanel;

    private XYSeriesCollection collection;
    private XYSplineRenderer splineRend = new XYSplineRenderer();


    public PrDistributionSearch(XYSeriesCollection collection){
        this.collection = collection;
    }

    public void makePlot(){

        chart = ChartFactory.createXYLineChart(
                "",                         // chart title
                "r",                        // domain axis label
                "P(r)",                     // range axis label
                collection,                 // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                true,
                false
        );

        final XYPlot plot = chart.getXYPlot();

        final NumberAxis domainAxis = new NumberAxis("r, \u212B");
        final NumberAxis rangeAxis = new NumberAxis("P(r)");
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
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        splineRend.setBaseShapesVisible(false);
        //splineRend.setBaseToolTipGenerator(new StandardXYSeriesLabelGenerator("Series {0}"));
        splineRend.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("Series {0}"));
        plot.setRenderer(0, splineRend);       //render as a line

        //splineRend.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("Series {0}"));


        int locale = 0;
        //double negativePointSize;

        for (int i=0; i < collection.getSeriesCount(); i++){
            XYSeries temp = collection.getSeries(i);
            splineRend.setSeriesToolTipGenerator(i, new StandardXYToolTipGenerator());
                //splineRend.setSeriesOutlineStroke();
            splineRend.setSeriesStroke(i, new BasicStroke(3.0f));
        }


        outPanel = new ChartPanel(chart);
        topPanel.add(outPanel);

        JFrame frame = new JFrame("Plot");
        frame.setContentPane(this.mainPrDistributionPanel);
        frame.setPreferredSize(new Dimension(600,400));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

}
