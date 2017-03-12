package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;

/**
 * Created by robertrambo on 28/01/2016.
 */
public class PofRPlot {
    private static JFreeChart chart;
    private static ChartFrame frame = new ChartFrame("", chart);
    private  ChartPanel outPanel;
    private  boolean crosshair = true;

    private XYSeriesCollection plottedCollection;
    private XYSeriesCollection splineCollection = new XYSeriesCollection();
    private XYSeriesCollection pdbCollection = new XYSeriesCollection();

    private XYSplineRenderer splineRend = new XYSplineRenderer();
    private XYLineAndShapeRenderer renderer1 = new XYSplineRenderer();

    private static PofRPlot singleton = new PofRPlot( );

    private PofRPlot(){

        JPopupMenu popup = frame.getChartPanel().getPopupMenu();
        //frame.getChartPanel().getChart().getXYPlot().getRangeAxis().setAxisLineStroke();
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

        plottedCollection = new XYSeriesCollection();
        splineCollection = new XYSeriesCollection();
    }

    /* Static 'instance' method */
    public static PofRPlot getInstance() {
        return singleton;
    }

    public void plot(Collection collection, WorkingDirectory workingDirectory, JPanel panelForPlot) {

        plottedCollection = new XYSeriesCollection();
        splineCollection = new XYSeriesCollection();
        //collectionInUse = collection;

        int totalC = collection.getDatasetCount();
        System.out.println("Total C " + totalC);
        for (int i=0; i<totalC; i++){
            if (collection.getDataset(i).getInUse()){
                splineCollection.addSeries(collection.getDataset(i).getRealSpaceModel().getPrDistribution());
                System.out.println(i + " TOTAL IN PR " +collection.getDataset(i).getRealSpaceModel().getPrDistribution().getItemCount());
            }
        }

        pdbCollection.removeAllSeries();

        chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "r",                        // domain axis label
                "P(r)",                // range axis label
                plottedCollection,                 // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,
                false
        );

        final XYPlot plot = chart.getXYPlot();

        final NumberAxis domainAxis = new NumberAxis("r, \u212B");
        final NumberAxis rangeAxis = new NumberAxis("P(r)");
        domainAxis.setAutoRangeIncludesZero(true);
        domainAxis.setAutoRange(true);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAxisLineVisible(true);

        //org.jfree.data.Range domainBounds = dataset.getDomainBounds(true);
        //org.jfree.data.Range rangeBounds = dataset.getRangeBounds(true);

        outPanel = new ChartPanel(chart){
            @Override
            public void restoreAutoBounds(){
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);

                int seriesCount = super.getChart().getXYPlot().getDataset(0).getSeriesCount();

                int size;
                double min = -0.00001;
                double max = -10;
                double dmax = 0;
                double tempMax;
                double tempdMax;
                boolean isVisible = false;
                for (int i=0; i < seriesCount; i++){
                    //check if visible, if visible, get min and max I value
                    //isVisible = super.getChart().getXYPlot().getRenderer().isSeriesVisible(i);
                    isVisible = super.getChart().getXYPlot().getRenderer(0).isSeriesVisible(i);
                    if (isVisible){
                        size = super.getChart().getXYPlot().getDataset(0).getItemCount(i);

                        for(int j =1; j < size; j++) {
                            tempMax = super.getChart().getXYPlot().getDataset(0).getYValue(i, j);
                            tempdMax = super.getChart().getXYPlot().getDataset(0).getXValue(i, j);
                            if (tempMax > max){
                                max = tempMax;
                            }

                            if (tempdMax > dmax){
                                dmax = tempdMax;
                            }
                        }
                        System.out.println(i + " SIZE " + size + " " + dmax + " maxI " + max);
                    }
                }

                super.getChart().getXYPlot().getRangeAxis().setRange(min, max+0.1*max);
                super.getChart().getXYPlot().getDomainAxis().setRange(0, dmax+2);
            }
        };

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

        splineRend = new XYSplineRenderer();
        renderer1 = new XYSplineRenderer();

        splineRend.setBaseShapesVisible(false);
        renderer1.setBaseShapesVisible(false);

        renderer1.setBaseStroke(new BasicStroke(2.0f));

        // renderer1.setBaseLinesVisible(false);
        int locale = 0;
        //double negativePointSize;
        for (int i=0; i < collection.getDatasets().size(); i++){
            Dataset temp = collection.getDataset(i);
            if (temp.getInUse()) {
                //splineRend.setSeriesOutlineStroke();
                splineRend.setSeriesStroke(locale, temp.getStroke());
                splineRend.setSeriesPaint(locale, temp.getColor().darker()); // make color slight darker
             //   chart.getXYPlot().getRenderer(0).setSeriesVisible(locale, true);
                locale++;
            }
        }

        plot.setDataset(0, splineCollection);  //Moore Function
        plot.setRenderer(0, splineRend);       //render as a line

        plot.setDataset(1, pdbCollection); //PDB data
        plot.setRenderer(1, renderer1);

        //frame.getChartPanel().setBorder(b);
        //frame.getContentPane().add(chartPanel);
        //frame.getChartPanel().setChart(chart);
        //cPanel.setPreferredSize( new Dimension(-1,380) );

        JPopupMenu popup = outPanel.getPopupMenu();
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

        outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        panelForPlot.removeAll();
        panelForPlot.add(outPanel);

    }

    public void changeVisible(int index, boolean state){
        chart.getXYPlot().getRenderer(0).setSeriesVisible(index, state);
    }

    public void clear(){
        this.pdbCollection.removeAllSeries();
        this.splineCollection.removeAllSeries();
    }

    public boolean inUse(){
        if (splineCollection.getSeriesCount() > 0){
            return true;
        }
        return false;
    }

    public void setPDBPofR(XYSeries pdbData){
        this.pdbCollection.removeAllSeries();
        this.pdbCollection.addSeries(pdbData);
    }

    public void removePDB(){
        this.pdbCollection.removeAllSeries();
    }

    public void changeColor(int id, Color newColor, float thickness){
        splineRend.setSeriesPaint(id, newColor);
        chart.getXYPlot().getRenderer(0).setSeriesStroke(id, new BasicStroke(thickness));
    }
}
