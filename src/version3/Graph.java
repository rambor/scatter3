package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Graph extends ApplicationFrame {

    public JFreeChart chart;
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    private XYSeriesCollection newDataset = new XYSeriesCollection();
    public ChartFrame frame = new ChartFrame("", chart);
    JFrame f = new JFrame("");

    public double markerStart = 0.0;
    public double markerEnd = 0.0;
    public boolean markerReady = false;
    public boolean markerStartSet = false;
    public boolean markerEndSet = false;
    Container content = f.getContentPane();

    public Graph(final String title) {
        super(title);
    }

    public void plot(Collection collection) {

        newDataset = collection.getMiniCollection();
        chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "",                        // domain axis label
                "",                // range axis label
                newDataset,                 // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,
                false
        );

        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("");
        final NumberAxis rangeAxis = new NumberAxis("");

        domainAxis.setTickLabelsVisible(false);
        rangeAxis.setTickLabelsVisible(false);

        plot.setRangeGridlinesVisible(false);
        plot.setDomainGridlinesVisible(false);

        plot.setOutlineVisible(false);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setBackgroundAlpha(1.0f);
        plot.setBackgroundImageAlpha(0.0f);
        plot.setBackgroundImage(null);
        plot.setBackgroundPaint(new Color(255,255,255));

        //make crosshair visible
        plot.setDomainCrosshairVisible(false);
        plot.setRangeCrosshairVisible(false);
        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(true);

        //set dot size for all series
        for (int i=0; i < newDataset.getSeriesCount(); i++){
            renderer1.setSeriesShape(i, new Ellipse2D.Double(0, 0, 1.0, 1.0));
            renderer1.setSeriesLinesVisible(i, false);
            renderer1.setSeriesPaint(i, collection.getDataset(i).getColor());
        }

        this.chart.setBackgroundPaint(new Color(0,0,255,0));

        this.frame.setBackground(null);
        this.frame.setForeground(null);
        this.frame.getContentPane().setBackground(null);

        this.frame.getChartPanel().setChart(this.chart);
        //frame.getChartPanel().setSize(100,100);
        this.frame.pack();

        //content.add(frame.getChartPanel());
    }
}
