package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
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
import java.io.File;

/**
 * Created by robertrambo on 05/01/2016.
 */
public class Graph extends ApplicationFrame {

    //public JFreeChart chart;
    //private XYSeriesCollection newDataset = new XYSeriesCollection();
    //public ChartFrame frame;// = new ChartFrame("", chart);
    private ChartPanel outPanel;

    private double markerStart = 0.0;
    private double markerEnd = 0.0;
    private boolean markerReady = false;
    private boolean markerStartSet = false;
    private boolean markerEndSet = false;

    public Graph(final String title) {
        super(title);
    }

    public void plot(Collection collection) {

        XYSeriesCollection newDataset = collection.getMiniCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "",                // domain axis label
                "",                // range axis label
                newDataset,                  // data
                PlotOrientation.VERTICAL,
                false,                // include legend
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
        plot.setDomainCrosshairLockedOnData(false);
        plot.setRangeCrosshairVisible(false);
        plot.setDomainCrosshairVisible(false);


        plot.setBackgroundAlpha(1.0f);
        plot.setBackgroundImageAlpha(0.0f);
        plot.setBackgroundImage(null);
        plot.setBackgroundPaint(new Color(255,255,255));

        XYLineAndShapeRenderer renderer1 = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer1.setBaseShapesVisible(true);
        renderer1.setBaseShapesFilled(true);

        //set dot size for all series
        for (int i=0; i < newDataset.getSeriesCount(); i++){
            renderer1.setSeriesShape(i, new Ellipse2D.Double(0, 0, 1.0, 1.0));
            renderer1.setSeriesLinesVisible(i, false);
            renderer1.setSeriesPaint(i, collection.getDataset(i).getColor());
        }

        chart.setBackgroundPaint(new Color(0,0,0,0));

        outPanel = new ChartPanel(chart);
        outPanel.setBackground(Color.white);
        //outPanel.getInsets().set(0, 0, 0, 0);

//        frame = new ChartFrame("", chart);
//        this.frame.setBackground(null);
//        this.frame.setForeground(null);
//        this.frame.getContentPane().setBackground(null);
//        this.frame.pack();
    }

    public ChartPanel getPanel(){return outPanel;}

    public void setNotify(boolean state){

        outPanel.getChart().setNotify(state);
        //frame.getChartPanel().getChart().setNotify(state);
    }

    public void clearAll(){
        outPanel.removeAll();
        //frame.getChartPanel().removeAll();
        //frame.removeAll();
    }
}
