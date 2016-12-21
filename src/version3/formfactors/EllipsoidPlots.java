package version3.formfactors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by robertrambo on 16/11/2016.
 */
public class EllipsoidPlots {

    private JPanel residualsPanel;
    private JPanel distributionRaPanel;
    private JPanel distributionRbPanel;
    private JPanel distributionRcPanel;
    private JPanel dataPlotPanel;
    private JPanel cross1Panel;
    private JPanel cross2Panel;
    private JPanel cross3Panel;
    private JPanel cross4Panel;

    private ArrayList<Model> models;

    //private HashMap<Integer, Double> probabilities;
    private ArrayList<Double> probabilities;
    private XYSeries probabilitiesPerRadiiA;
    private XYSeries probabilitiesPerRadiiB;
    private XYSeries probabilitiesPerRadiiC;
    private ArrayList<Double> calculatedIntensities;
    private ArrayList<Double> transformedObservedIntensities;
    private ArrayList<Double> transformedObservedErrors;
    private ArrayList<Double> probabilitiesPerModel;
    private XYSeriesCollection residualsCollection;
    private XYSeriesCollection averageCollection;
    private double minRc;
    private double maxRc;
    private boolean useNoBackGround;
    private DefaultXYZDataset xyzdataset;
    private DefaultXYZDataset bcdataset;

    private Double[] qvalues;

    private final KeptModels keptList;

    public EllipsoidPlots(JPanel residualsPanel,
                          JPanel cross1Panel,
                          JPanel cross2Panel,
                          JPanel cross3Panel,
                          JPanel cross4Panel,
                          JPanel raPanel,
                          JPanel rbPanel,
                          JPanel rcPanel,
                          JPanel dataFitPlot,
                          ArrayList<Model> models,
                          KeptModels keptList,
                          ArrayList<Double> probabilities,
                          //HashMap<Integer, Double> probabilities,
                          Double[] qvalues,
                          ArrayList<Double> transformedObservedIntensities,
                          ArrayList<Double> transformedObservedErrors,
                          boolean useNoBackground, double min, double max){

        this.residualsPanel = residualsPanel;
        this.cross1Panel = cross1Panel;
        this.cross2Panel = cross2Panel;
        this.cross3Panel = cross3Panel;
        this.cross4Panel = cross4Panel;
        this.distributionRaPanel = raPanel;
        this.distributionRbPanel = rbPanel;
        this.distributionRcPanel = rcPanel;
        this.dataPlotPanel = dataFitPlot;
        this.useNoBackGround = useNoBackground;

        this.models = models;
        this.keptList = keptList;
        this.probabilities = probabilities;
        this.qvalues = qvalues;
        this.transformedObservedIntensities = transformedObservedIntensities;
        this.transformedObservedErrors = transformedObservedErrors;


        residualsCollection = new XYSeriesCollection();
        averageCollection = new XYSeriesCollection();
        minRc = min;
        maxRc = max;
    }


    private void makeHistogramData(){

        TreeMap<Double, Double> r_a = new TreeMap<>();
        TreeMap<Double, Double> r_b = new TreeMap<>();
        TreeMap<Double, Double> r_c = new TreeMap<>();

        //prolate and oblate ellipsoids don't have r_b
        probabilitiesPerRadiiA = new XYSeries("probabilitiesA");
        probabilitiesPerRadiiB = new XYSeries("probabilitiesB"); // empty series if no radii
        probabilitiesPerRadiiC = new XYSeries("probabilitiesC");

        ArrayList<Double> radiusA = new ArrayList<>();
        ArrayList<Double> radiusB = new ArrayList<>();
        ArrayList<Double> radiusC = new ArrayList<>();
        probabilitiesPerModel = new ArrayList<>();

        double volume=0;

        if(models.get(0).getModelType() == ModelType.ELLIPSOID){

            for(int i=0; i<models.size(); i++){
                // convert probabilities into radii
                Ellipse model =  (Ellipse) models.get(i);
                int index = model.getIndex();
                double currentProbability = probabilities.get(index);
                volume += currentProbability*model.getVolume();

                if (r_a.containsKey(model.getRadius_a())){
                    r_a.put(model.getRadius_a(), r_a.get(model.getRadius_a()) + currentProbability);
                } else {
                    r_a.put(model.getRadius_a(), currentProbability);
                }

                if (r_b.containsKey(model.getRadius_b())){
                    r_b.put(model.getRadius_b(), r_b.get(model.getRadius_b()) + currentProbability);
                } else {
                    r_b.put(model.getRadius_b(), currentProbability);
                }

                if (r_c.containsKey(model.getRadius_c())){
                    r_c.put(model.getRadius_c(), r_c.get(model.getRadius_c()) + currentProbability);
                } else {
                    r_c.put(model.getRadius_c(), currentProbability);
                }

                radiusA.add(model.getRadius_a());
                radiusB.add(model.getRadius_b());
                radiusC.add(model.getRadius_c());
                probabilitiesPerModel.add(currentProbability*20); // size of bubble
            }

            for(Map.Entry<Double, Double> entry : r_b.entrySet()) {
                probabilitiesPerRadiiB.add(entry.getKey(), entry.getValue());
            }

        } else {

            //minRc=100000;
            //maxRc=0;
            int totalModels = models.size();

            for(int i=0; i<totalModels; i++){
                // add up all the probabilities at the given r_a value
                ProlateEllipsoid model = (ProlateEllipsoid) models.get(i);

                int index = model.getIndex();

                double currentProbability = probabilities.get(index);
                volume += currentProbability*model.getVolume();

                if (r_a.containsKey(model.getRadius_a())){
                    r_a.put(model.getRadius_a(), r_a.get(model.getRadius_a()) + currentProbability);
                } else {
                    r_a.put(model.getRadius_a(), currentProbability);
                }

                if (r_c.containsKey(model.getRadius_c())){
                    r_c.put(model.getRadius_c(), r_c.get(model.getRadius_c()) + currentProbability);
                } else {
                    r_c.put(model.getRadius_c(), currentProbability);
                }

                radiusA.add(model.getRadius_a());
                radiusC.add(model.getRadius_c());
                probabilitiesPerModel.add(currentProbability*20); // size of bubble
            }

            System.out.println("min " + minRc + " < " + maxRc);
        }

        for(Map.Entry<Double, Double> entry : r_a.entrySet()) {
            probabilitiesPerRadiiA.add(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<Double, Double> entry : r_c.entrySet()) {
            probabilitiesPerRadiiC.add(entry.getKey(), entry.getValue());
        }

        // create datasets to plot
        int count = radiusA.size();
        xyzdataset = new DefaultXYZDataset();
        bcdataset = new DefaultXYZDataset();
        for(int i=0; i<count; i++){
            xyzdataset.addSeries("-" + i, new double[][]{new double[]{radiusC.get(i)}, new double[]{radiusA.get(i)}, new double[]{probabilitiesPerModel.get(i)}});
            if(models.get(0).getModelType() == ModelType.ELLIPSOID){
                bcdataset.addSeries("-" + i, new double[][]{new double[]{radiusC.get(i)}, new double[]{radiusB.get(i)}, new double[]{probabilitiesPerModel.get(i)}});
            }
        }
        System.out.println("VOLUME " + volume);
    }


    public void create(boolean makeFigures){

        makeResiduals();
        makeHistogramData();

        makeGeometricPlot();
        makeResidualsPlot();
        makeDataPlot();
        createDistributionPlots();
    }

    private Color giveRGB(double maximum, double value){

        double ratio = 2 * value/maximum;

        int blue = (int)(Math.max(0, 255*(1 - ratio)));
        int red = (int)(Math.max(0, 255*(ratio - 1)));
        int green = 255 - blue - red;
        return new Color(red,green,blue);
    }


    private XYPlot makeBCGeometricPlot(){
        JFreeChart chart = ChartFactory.createBubbleChart(
                "",
                "c-axis",
                "b-axis",
                bcdataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false

        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer xyitemrenderer = plot.getRenderer();
        xyitemrenderer.setBaseOutlinePaint(new Color(105, 105, 105, 40) );

        double max = Collections.max(probabilitiesPerModel);
        int totalcount = bcdataset.getSeriesCount();

        for(int i=0; i<totalcount; i++){
            xyitemrenderer.setSeriesPaint(i, giveRGB(max, probabilitiesPerModel.get(i)));
        }

        final NumberAxis domainAxis = new NumberAxis("c-axis");
        final NumberAxis rangeAxis = new NumberAxis("b-axis");
        domainAxis.setAutoRange(true);
        domainAxis.setRange(minRc, maxRc);
        domainAxis.setAutoRangeStickyZero(false);
        domainAxis.setAutoRangeIncludesZero(false);

        rangeAxis.setAutoRange(true);
        rangeAxis.setRange(minRc, maxRc);
        rangeAxis.setAutoRangeStickyZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        plot.setBackgroundPaint(Color.white);
        chart.getXYPlot().getRangeAxis().setInverted(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        return plot;
        //cross2Panel.removeAll();
        //cross2Panel.add(chartPanel);
    }



    private void makeGeometricPlot(){
        JFreeChart chart = ChartFactory.createBubbleChart(
                "",
                "c-axis",
                "a-axis",
                xyzdataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false

        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer xyitemrenderer = plot.getRenderer();
        xyitemrenderer.setBaseOutlinePaint(new Color(105, 105, 105, 40) );

        double max = Collections.max(probabilitiesPerModel);
        int totalcount = xyzdataset.getSeriesCount();
        System.out.println("Series count " + plot.getSeriesCount());
        for(int i=0; i<totalcount; i++){
            xyitemrenderer.setSeriesPaint(i, giveRGB(max, probabilitiesPerModel.get(i)));
        }

        final NumberAxis domainAxis = new NumberAxis("c-axis");
        final NumberAxis rangeAxis = new NumberAxis("a-axis");
        domainAxis.setAutoRange(true);
        domainAxis.setRange(minRc, maxRc);
        domainAxis.setAutoRangeStickyZero(false);
        domainAxis.setAutoRangeIncludesZero(false);

        rangeAxis.setAutoRange(true);
        rangeAxis.setRange(minRc, maxRc);
        rangeAxis.setAutoRangeStickyZero(false);
        rangeAxis.setAutoRangeIncludesZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        plot.setBackgroundPaint(Color.white);

        ChartPanel chartPanel = new ChartPanel(chart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        if(models.get(0).getModelType() == ModelType.ELLIPSOID){
            //makeBCGeometricPlot();
            CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(new NumberAxis("2"));
            combinedPlot.setDomainAxis(domainAxis);
            combinedPlot.setGap(10.0);
            combinedPlot.add(plot, 1);
            combinedPlot.add(makeBCGeometricPlot(), 1);
            combinedPlot.setOrientation(PlotOrientation.VERTICAL);

            JFreeChart combChart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
            combChart.removeLegend();
            combChart.setBackgroundPaint(Color.WHITE);
            ChartFrame chartframe = new ChartFrame("", combChart);
            cross1Panel.removeAll();
            cross1Panel.add(chartframe.getContentPane());
        } else {
            cross1Panel.removeAll();
            cross1Panel.add(chartPanel);
        }
    }

    private void makeResidualsPlot(){
        JFreeChart chart = ChartFactory.createXYLineChart(
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

        ChartPanel chartPanel = new ChartPanel(chart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        residualsPanel.removeAll();
        residualsPanel.add(chartPanel);
    }


    private void createDistributionPlots(){

        JFreeChart chartA = ChartFactory.createXYBarChart(
                "a-axis",
                "radius",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiA),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        JFreeChart chartB = ChartFactory.createXYBarChart(
                "b-axis",
                "radius",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiB),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        JFreeChart chartC = ChartFactory.createXYBarChart(
                "c-axis (major)",
                "radius",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiC),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );


        // "Color Intensity   Histogram","X",false,"Y",dataset,PlotOrientation.VERTICAL,true,true,false
        // XYPlot plot = (XYPlot) histogramChart.getPlot();

        ChartPanel histogramChartPanelA = new ChartPanel(chartA);
        ChartPanel histogramChartPanelB = new ChartPanel(chartB);
        ChartPanel histogramChartPanelC = new ChartPanel(chartC);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        distributionRaPanel.removeAll();
        distributionRaPanel.add(histogramChartPanelA);

        distributionRbPanel.removeAll();
        distributionRbPanel.add(histogramChartPanelB);

        distributionRcPanel.removeAll();
        distributionRcPanel.add(histogramChartPanelC);

    }


    private void makeResiduals(){

        // calculate model intensities
        calculatedIntensities = new ArrayList<>();
        int totalq = models.get(0).getTotalIntensities();
        while(calculatedIntensities.size() < totalq) calculatedIntensities.add(0.0d);

        int count=0;
       // for (Map.Entry<Double, ArrayList<Integer>> entry : keptList.entrySet()) {
        //Map.Entry<Double, ArrayList<Integer>> entry = keptList.firstEntry();


            ArrayList<Integer> indices = keptList.getFirst();
            // for each index, great XYSeries and Add
            int total = indices.size();

            for (int i=0; i<total; i++){
                Model model = models.get(indices.get(i));
                for(int q=0; q<totalq; q++){
                    calculatedIntensities.set(q, calculatedIntensities.get(q).doubleValue() + model.getIntensity(q));
                    count++;
                }
            }
       // }

        // average the calculated intensities
        double inv = 1.0/(double)count;
        for(int q=0; q<totalq; q++){
            calculatedIntensities.set(q, calculatedIntensities.get(q).doubleValue()*inv);
        }


        double scale_c;
        double baseline =0;
        XYSeries residualsSeries = new XYSeries("residuals");
        XYSeries averagedSeries = new XYSeries("average");
        XYSeries experimentalSeries = new XYSeries("experimental");

        if (this.useNoBackGround){
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


}
