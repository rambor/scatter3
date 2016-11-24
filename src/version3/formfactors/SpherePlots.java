package version3.formfactors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by robertrambo on 14/11/2016.
 */
public class SpherePlots {

    private final ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList;
    private JPanel residualsPanel;
    private JPanel histogramPanel;
    private JPanel geometricPanel;
    private JPanel distributionPanel;
    private JPanel dataPlotPanel;
    private ArrayList<Model> models;
    private ArrayList<Shape> ellipses;
    private ArrayList<Double> zcolumn;
    private XYSeriesCollection heatMapCollection;
    private XYSeriesCollection residualsCollection;
    private XYSeriesCollection averageCollection;
    private double minDomain;
    private double maxDomain;
    private JFreeChart heatMapChart;
    private ChartPanel heatMapChartPanel;
    private ChartPanel histogramChartPanel;
    private JFreeChart histogramChart;
    private JFreeChart residualsChart;
    private ChartPanel residualsChartPanel;

    private XYLineAndShapeRenderer renderer1;
    private HashMap<Integer, Double> probabilities;
    private XYSeries probabilitiesPerRadii;
    private ArrayList<Double> calculatedIntensities;
    private ArrayList<Double> transformedObservedIntensities;
    private ArrayList<Double> transformedObservedErrors;

    private boolean useNoBackground;
    private Double[] qvalues;

    /**
     *
     * @param residualsPanel
     * @param histogramPanel
     * @param geometricPanel
     * @param distributionPanel
     * @param models
     * @param keptList top N of trials that fits the data
     */
    public SpherePlots(JPanel residualsPanel,
                       JPanel histogramPanel,
                       JPanel geometricPanel,
                       JPanel distributionPanel,
                       JPanel dataPlotPanel,
                       ArrayList<Model> models,
                       ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList,
                       HashMap<Integer, Double> probabilities,
                       Double[] qvalues,
                       ArrayList<Double> transformedObservedIntensities,
                       ArrayList<Double> transformedObservedErrors,
                       boolean useNoBackground
    ){

        this.residualsPanel = residualsPanel;
        this.histogramPanel = histogramPanel;
        this.distributionPanel = distributionPanel;
        this.geometricPanel = geometricPanel;
        this.dataPlotPanel = dataPlotPanel;
        this.models = models;
        this.keptList = keptList;
        this.probabilities = probabilities;
        this.qvalues = qvalues;
        this.transformedObservedIntensities = transformedObservedIntensities;
        this.transformedObservedErrors = transformedObservedErrors;
        this.useNoBackground = useNoBackground;

        heatMapCollection = new XYSeriesCollection();
        residualsCollection = new XYSeriesCollection();
        averageCollection = new XYSeriesCollection();
    }

    public void create(boolean makeFigures){
        makeGeomtricData();
        makeHistogramData();
        makeResiduals();

        makeGeometricPlot();
        makeHistogramPlot();
        makeResidualsPlot();
        makeDataPlot();
    }

    private void makeResidualsPlot(){
        residualsChart = ChartFactory.createXYLineChart(
                "",
                "q",
                "residuals",
                residualsCollection,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        // "Color Intensity   Histogram","X",false,"Y",dataset,PlotOrientation.VERTICAL,true,true,false
        //XYPlot plot = (XYPlot) residualsChart.getPlot();

        residualsChartPanel = new ChartPanel(residualsChart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        residualsPanel.removeAll();
        residualsPanel.add(residualsChartPanel);
    }


    private void makeResiduals(){

        // calculate model intensities
        calculatedIntensities = new ArrayList<>();
        int totalq = models.get(0).getTotalIntensities();
        while(calculatedIntensities.size() < totalq) calculatedIntensities.add(0.0d);

        int count=0;
        for (Map.Entry<Double, ArrayList<Integer>> entry : keptList.entrySet()) {

            ArrayList<Integer> indices = entry.getValue();
            // for each index, great XYSeries and Add
            int total = indices.size();

            for (int i=0; i<total; i++){
                Sphere model =  (Sphere)models.get(indices.get(i));

                for(int q=0; q<totalq; q++){
                    calculatedIntensities.set(q, calculatedIntensities.get(q).doubleValue() + model.getIntensity(q));
                    count++;
                }
            }
        }

        // average the calculated intensities
        double inv = 1.0/(double)count;
        for(int q=0; q<totalq; q++){
            calculatedIntensities.set(q, calculatedIntensities.get(q).doubleValue()*inv);
            count++;
        }

        double scale_c;
        double baseline =0;
        XYSeries residualsSeries = new XYSeries("residuals");
        XYSeries averagedSeries = new XYSeries("average");
        XYSeries experimentalSeries = new XYSeries("experimental");


        if (useNoBackground){
            float cUp = 0;
            float cDown = 0;

            double down, error, calc;
            // calculate scale, c
            for (int index=0; index<totalq; index++){
                error = 1.0/transformedObservedErrors.get(index);
                calc = calculatedIntensities.get(index);
                cUp += transformedObservedIntensities.get(index)*calc*error*error;

                down = calc*error;
                cDown += down*down;
            }

            scale_c = cUp/cDown;

        } else {
            LeastSquaresFit fit = new LeastSquaresFit(totalq, calculatedIntensities, transformedObservedIntensities, transformedObservedErrors, qvalues);
            scale_c = fit.getScale();
            baseline = fit.getBaseline();
            System.out.println("SCALE: " + scale_c + " bck: " + baseline);
        }


        for (int index=0; index<totalq; index++){
            double calcI = scale_c*calculatedIntensities.get(index) + baseline*qvalues[index];
            residualsSeries.add((double)qvalues[index], transformedObservedIntensities.get(index) - calcI);
            averagedSeries.add((double)qvalues[index], calcI);
            experimentalSeries.add((double)qvalues[index], transformedObservedIntensities.get(index));
        }

        residualsCollection.addSeries(residualsSeries);
        averageCollection.addSeries(experimentalSeries);
        averageCollection.addSeries(averagedSeries);
    }


    private void makeHistogramData(){
        probabilitiesPerRadii = new XYSeries("probabilities");
        for(int i=0; i<probabilities.size(); i++){
            // convert probabilities into radii
            Sphere model =  (Sphere)models.get(i);
            probabilitiesPerRadii.add(model.getRadius(), probabilities.get(i));
        }
    }

    private void makeHistogramPlot(){
        histogramChart = ChartFactory.createXYBarChart(
                "",
                "radius",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadii),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        // "Color Intensity   Histogram","X",false,"Y",dataset,PlotOrientation.VERTICAL,true,true,false
        XYPlot plot = (XYPlot) histogramChart.getPlot();

        histogramChartPanel = new ChartPanel(histogramChart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        histogramPanel.removeAll();
        histogramPanel.add(histogramChartPanel);
    }

    private void makeDataPlot(){

        JFreeChart chart = ChartFactory.createXYLineChart(
                "",                     // chart title
                "q",                             // domain axis label
                "q × I(q)",                     // range axis label
                averageCollection,                 // data
                PlotOrientation.VERTICAL,
                false,                           // include legend
                true,
                false
        );

        // "Color Intensity   Histogram","X",false,"Y",dataset,PlotOrientation.VERTICAL,true,true,false
//        XYPlot plot = (XYPlot) chart.getPlot();
//        LogAxis logAxis = new LogAxis("Log10 I(q)");
//        logAxis.setBase(10);
//        logAxis.setTickUnit(new NumberTickUnit(10));
//        logAxis.setMinorTickMarksVisible(false);
//        logAxis.setAutoRange(true);
//        plot.setRangeAxis(logAxis);

        ChartPanel chartPanel = new ChartPanel(chart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        dataPlotPanel.removeAll();
        dataPlotPanel.add(chartPanel);
    }


    /**
     * use Bubblechart
     */
    public void makeGeometricPlot(){

//        DefaultXYZDataset tempData = new DefaultXYZDataset();
        int totalInHeat = heatMapCollection.getSeriesCount();
//        double[] xvalues=new double[totalInHeat];
//        double[] zvalues=new double[totalInHeat];
//
//        for(int i=0;i<totalInHeat; i++){
//            xvalues[i] =0.0d;
//            zvalues[i] =zcolumn.get(i);
//        }
//
//        double[][] series = new double[][] { xvalues, xvalues, zvalues };
//        tempData.addSeries("Series 1", series);

//        heatMapChart = ChartFactory.createBubbleChart(
//                "",                         // chart title
//                "r",                        // domain axis label
//                "r",                        // range axis label
//                (XYZDataset)tempData,          // data
//                PlotOrientation.VERTICAL,
//                false,                      // include legend
//                false,
//                false
//        );

        heatMapChart = ChartFactory.createScatterPlot(
                "",                         // chart title
                "r",                        // domain axis label
                "r",                        // range axis label
                heatMapCollection,          // data
                PlotOrientation.VERTICAL,
                false,                      // include legend
                false,
                false
        );

        //final XYPlot heatMapPlot = heatMapChart.getXYPlot();

        final NumberAxis domainAxis = new NumberAxis("r, \u212B");
        final NumberAxis rangeAxis = new NumberAxis("r");
        domainAxis.setAutoRangeIncludesZero(true);
        domainAxis.setAutoRange(false);
        rangeAxis.setAutoRange(false);
        rangeAxis.setAxisLineVisible(true);
        int maxrange = (int)(maxDomain*0.5 + 0.1*maxDomain*0.5);
        rangeAxis.setRange(-maxrange , maxrange);
        domainAxis.setRange(-maxrange, maxrange);

        XYPlot heatMapPlot = (XYPlot)heatMapChart.getPlot();
        heatMapPlot.setRenderer(new XYLineAndShapeRenderer(){
            @Override
            public Paint getItemPaint(int row, int column) {
                try {
                    return Color.green;
                } catch (Exception e) {
                    return Color.green;
                }
            }

            @Override
            public Shape getItemShape(int row, int column) {
                try {
                    return ellipses.get(column);
                } catch (Exception e) {
                    return ellipses.get(0);
                }
            }
        });

//        heatMapPlot.setRenderer(new XYBubbleRenderer(){
//            @Override
//            public Paint getItemPaint(int row, int column) {
//                try {
//                    //return new Color(0, 128, 0);
//                    return Color.green;
//                } catch (Exception e) {
//                    return Color.blue;
//                }
//            }

//            @Override
//            public Shape getItemShape(int row, int column) {
//
//                try {
//                    System.out.println("SHAPE ROW " + row + " col " + column);
//                    return ellipses.get(column);
//                } catch (Exception e) {
//                    return ellipses.get(0);
//                }
//            }
//        });

        //XYBubbleRenderer renderer = (XYBubbleRenderer) heatMapPlot.getRenderer();
        //renderer.setSeriesPaint(0, Color.green);
        //renderer.setBaseShape(new Ellipse2D.Double(-0.5*10, -5, 4, 10) );
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) heatMapPlot.getRenderer();
        renderer.setBaseShapesFilled(false);
        BasicStroke stroke = new BasicStroke(2.0f);
        for (int i = 0; i < totalInHeat; i++) {
            renderer.addAnnotation(new XYShapeAnnotation(ellipses.get(i), stroke, Color.green));
        }

        heatMapPlot.setForegroundAlpha(0.1F);
        heatMapPlot.setBackgroundPaint(Color.white);
        heatMapPlot.setBackgroundAlpha(0.0f);

        heatMapPlot.setDomainAxis(domainAxis);
        heatMapPlot.setRangeAxis(rangeAxis);

        //renderer1 = (XYLineAndShapeRenderer) heatMapChart.getXYPlot().getRenderer();
        //renderer1.setBaseShapesFilled(false);
        //renderer1.setBaseLinesVisible(true);
        //renderer1.setSeriesOutlineStroke(count, tempData.getStroke());

        heatMapChartPanel = new ChartPanel(heatMapChart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        geometricPanel.removeAll();
        geometricPanel.add(heatMapChartPanel);
    }

    public void makeGeomtricData(){

        // convert each model into a radii
        // as a single XYSeries
        Map.Entry<Double, ArrayList<Integer> > top = keptList.firstEntry();
        //renderer1 = (XYBubbleRenderer) heatMapChart.getXYPlot().getRenderer();
        //renderer1.setBaseShapesVisible(true);
        //renderer1.setBaseShapesFilled(false);
        //renderer1.setBaseStroke(new BasicStroke(2.0f));
        // keptList -> score and array of indices
        int count=1;
        int setCount=0;
        double radius, del;
        ellipses = new ArrayList<>();

        minDomain=100000.0;
        maxDomain = -1.0;
        zcolumn = new ArrayList<>();

        XYSeries ellipseSet = new XYSeries("ALL");

        for (Map.Entry<Double, ArrayList<Integer>> entry : keptList.entrySet()) {

            ArrayList<Integer> indices = entry.getValue();
            // for each index, great XYSeries and Add
            int total = indices.size();

            for (int i=0; i<total; i++){
                Sphere model =  (Sphere)models.get(indices.get(i));
                radius =  2*model.getRadius();
                del = (-radius*0.5);
                // get radii of model specified by indices.get(i)
                ellipses.add(new Ellipse2D.Double(del, del, radius, radius));
                zcolumn.add(radius);
                //heatMapCollection.addSeries(new XYSeries(setCount + "-" + count));
                ellipseSet.add(0,0);
                //heatMapCollection.getSeries(setCount).add(0,0);

                if (radius > maxDomain){
                    maxDomain = radius;
                }

                if (radius < minDomain){
                    minDomain = radius;
                }

                count++;
            }
            setCount++;
        }

        heatMapCollection.addSeries(ellipseSet);
        System.out.println("MIN " + minDomain + " " + " MAX " + maxDomain);
        System.out.println(heatMapCollection.getSeriesCount() + " => " + heatMapCollection.getItemCount(0));
        // heatMapChart.getXYPlot().setDataset(0, heatMapCollection); //IofQ Data
        // heatMapChart.getXYPlot().setRenderer(0, renderer1);        //render as points
    }





}
