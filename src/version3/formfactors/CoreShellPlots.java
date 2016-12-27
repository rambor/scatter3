package version3.formfactors;

import javafx.scene.chart.Chart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import version3.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by robertrambo on 03/12/2016.
 */
public class CoreShellPlots {

    private JPanel residualsPanel;
    private JPanel distributionRaPanel;
    private JPanel shellThicknessPanel;
    private JPanel distributionRcPanel;
    private JPanel dataPlotPanel;
    private JPanel geometryPanel;

    private ArrayList<Model> models;

    private ArrayList<Double> probabilities;
    private XYSeries radiiEmpty;
    private XYSeries radiiFull;
    private ArrayList<Double> probabilitiesPerModelEmpty;
    private ArrayList<Double> probabilitiesPerModelFull;
    private XYSeries probabilitiesPerRadiiA;
    private XYSeries probabilitiesPerShell;
    private XYSeries probabilitiesPerRadiiC;
    private ArrayList<Double> calculatedIntensities;
    private ArrayList<Double> transformedObservedIntensities;
    private ArrayList<Double> transformedObservedErrors;
    private XYSeriesCollection heatMapCollection;
    private XYSeriesCollection residualsCollection;
    private XYSeriesCollection averageCollection;
    private double minRc;
    private double maxRc;
    private boolean useNoBackGround;

    private Double[] qvalues;
    private DefaultXYZDataset xyzdataset;
    private DefaultXYZDataset shellThicknessCaxisDataset;
    //private XYSeriesCollection shellThicknessCaxisDataset;
    private final KeptModels keptList;

    public CoreShellPlots(JPanel residualsPanel,
                          JPanel geometryPanel,
                          JPanel raPanel,
                          JPanel shellPanel,
                          JPanel rcPanel,
                          JPanel dataFitPlot,
                          ArrayList<Model> models,
                          KeptModels keptList,
                          ArrayList<Double> probabilities,
                          Double[] qvalues,
                          ArrayList<Double> transformedObservedIntensities,
                          ArrayList<Double> transformedObservedErrors,
                          boolean useNoBackground, double min, double max){

        this.residualsPanel = residualsPanel;
        this.distributionRaPanel = raPanel;
        this.shellThicknessPanel = shellPanel;
        this.distributionRcPanel = rcPanel;
        this.dataPlotPanel = dataFitPlot;
        this.geometryPanel = geometryPanel;
        this.useNoBackGround = useNoBackground;

        this.models = models;
        this.keptList = keptList;
        this.probabilities = probabilities;
        this.qvalues = qvalues;
        this.transformedObservedIntensities = transformedObservedIntensities;
        this.transformedObservedErrors = transformedObservedErrors;

        heatMapCollection = new XYSeriesCollection();
        residualsCollection = new XYSeriesCollection();
        averageCollection = new XYSeriesCollection();
        minRc = min;
        maxRc = max;
    }


    private void makeHistogramData(){

        TreeMap<Double, Double> r_a = new TreeMap<>();
        TreeMap<Double, Double> r_c = new TreeMap<>();
        TreeMap<Double, Double> shell_count = new TreeMap<>();

        //prolate and oblate ellipsoids don't have r_b
        probabilitiesPerRadiiA = new XYSeries("probabilitiesA");
        probabilitiesPerShell = new XYSeries("ShellProbabilities"); // empty series if no radii
        probabilitiesPerRadiiC = new XYSeries("probabilitiesC");

        //prolate and oblate ellipsoids don't have r_b
        ArrayList<Double> radiusA = new ArrayList<>();
        ArrayList<Double> radiusC = new ArrayList<>();
        ArrayList<Double> thickness = new ArrayList<>();

        HashMap<Pair, Double> caxisThickness = new HashMap<>();

        radiiEmpty = new XYSeries("radiiEmpty");
        radiiFull = new XYSeries("radiiFull");
        probabilitiesPerModelEmpty = new ArrayList<>();
        probabilitiesPerModelFull = new ArrayList<>();

        double volume=0;
        int emptyCount=0, fullCount=0;

        for(int i=0; i<models.size(); i++){
            // convert probabilities into radii
            CoreShell model =  (CoreShell) models.get(i);
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

            if(shell_count.containsKey(model.getThickness())){
                shell_count.put(model.getThickness(), shell_count.get(model.getThickness()) + currentProbability);
            } else {
                shell_count.put(model.getThickness(), currentProbability);
            }


            Pair tempPair = new Pair(model.getRadius_c(), model.getThickness());
            if (caxisThickness.containsKey(tempPair)){
                if ( caxisThickness.get(tempPair) < currentProbability){
                    caxisThickness.put(tempPair, currentProbability);
                }
            } else {
                caxisThickness.put(tempPair, currentProbability);
            }

            radiusA.add(model.getRadius_a());
            radiusC.add(model.getRadius_c());
            thickness.add(model.getThickness());

            probabilitiesPerModelFull.add(currentProbability*20);
        }


        for(Map.Entry<Double, Double> entry : r_a.entrySet()) {
            probabilitiesPerRadiiA.add(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<Double, Double> entry : shell_count.entrySet()) {
            probabilitiesPerShell.add(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<Double, Double> entry : r_c.entrySet()) {
            probabilitiesPerRadiiC.add(entry.getKey(), entry.getValue());
        }

        System.out.println("CORESHELL VOLUME " + volume);
        // create datasets to plot
        int count = radiusA.size();
        xyzdataset = new DefaultXYZDataset();
        shellThicknessCaxisDataset = new DefaultXYZDataset();

        for(int i=0; i<count; i++){
                            xyzdataset.addSeries("-" + i, new double[][]{new double[]{radiusC.get(i)}, new double[]{radiusA.get(i)}, new double[]{probabilitiesPerModelFull.get(i)}});
            shellThicknessCaxisDataset.addSeries("-" + i, new double[][]{new double[]{radiusC.get(i)}, new double[]{thickness.get(i)}, new double[]{probabilitiesPerModelFull.get(i)/2}});
//            xvalues[i] = radiusC.get(i);
//            yvalues[i] = thickness.get(i);
//            zvalues[i] = probabilitiesPerModelFull.get(i);
            //shellThicknessCaxisDataset.addSeries(new XYSeries("-" + i));
            //shellThicknessCaxisDataset.getSeries(i).add(radiusC.get(i), thickness.get(i));
        }

//        Iterator it = caxisThickness.entrySet().iterator();
//        int track=0;
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            Pair tempPair = (Pair) pair.getKey();
//            shellThicknessCaxisDataset.addSeries("-" + track, new double[][]{new double[]{(double)tempPair.getLeft()}, new double[]{(double)tempPair.getRight()}, new double[]{(double)pair.getValue()}});
//            track++;
//        }
    }


    public void create(boolean makeFigures){
        //  makeGeomtricData();

        makeResiduals();
        makeHistogramData();

        makeResidualsPlot();
        makeDataPlot();
        createDistributionPlots();
        makeGeometryPlot();
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
                "shell thickness",
                "Angstroms",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerShell),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );



        JFreeChart chartC = ChartFactory.createXYBarChart(
                "c-axis",
                "radius",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiC),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

//Chart B
        XYPlot plot = chartB.getXYPlot();

        plot.setRangeGridlinePaint(Color.white);
        plot.setBackgroundPaint(Color.white);
        chartB.setBackgroundPaint(Color.white);
        chartB.removeLegend();

        // "Color Intensity   Histogram","X",false,"Y",dataset,PlotOrientation.VERTICAL,true,true,false
        // XYPlot plot = (XYPlot) histogramChart.getPlot();

        ChartPanel histogramChartPanelA = new ChartPanel(chartA);
        ChartPanel histogramChartPanelB = new ChartPanel(chartB);
        ChartPanel histogramChartPanelC = new ChartPanel(chartC);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        distributionRaPanel.removeAll();
        distributionRaPanel.add(histogramChartPanelA);
        shellThicknessPanel.removeAll();
        shellThicknessPanel.add(histogramChartPanelB);
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
        //ArrayList<Integer> indices = entry.getValue();
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


    private void makeGeometryPlot(){

        JFreeChart chart = ChartFactory.createBubbleChart(
                "",
                "radii c",
                "radii a",
                xyzdataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false

        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer xyitemrenderer = plot.getRenderer();


        double max = Collections.max(probabilitiesPerModelFull);
        int totalcount = xyzdataset.getSeriesCount();
        for(int i=0; i<totalcount; i++){
            Color temp = giveRGB(max, probabilitiesPerModelFull.get(i));
            xyitemrenderer.setSeriesOutlinePaint(i, temp);
            xyitemrenderer.setSeriesPaint(i, temp);
        }

        final NumberAxis domainAxis = new NumberAxis("c-axis");
        final NumberAxis rangeAxis = new NumberAxis("a-axis");

        domainAxis.setAutoRange(false);
        rangeAxis.setRange(minRc, maxRc);
        rangeAxis.setAutoRange(false);
        rangeAxis.setRange(minRc, maxRc);
        rangeAxis.setAutoRangeStickyZero(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        plot.setBackgroundPaint(Color.white);

        //ChartPanel chartPanel = new ChartPanel(chart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));

        CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(new NumberAxis("2"));
        combinedPlot.setDomainAxis(domainAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.add(plot, 1);
        combinedPlot.add(makeCAxisThicknessPlot(), 1);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        JFreeChart combChart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        combChart.removeLegend();
        combChart.setBackgroundPaint(Color.WHITE);
        ChartFrame chartframe = new ChartFrame("", combChart);

        geometryPanel.removeAll();
        //geometryPanel.add(chartPanel);
        geometryPanel.add(chartframe.getContentPane());

    }

    private XYPlot makeCAxisThicknessPlot(){

        JFreeChart chartB = ChartFactory.createBubbleChart(
                "",
                "radii c",
                "shell thickness",
                shellThicknessCaxisDataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        XYPlot plot = chartB.getXYPlot();
        XYItemRenderer xyitemrenderer = plot.getRenderer();
        double max = Collections.max(probabilitiesPerModelFull);
        int totalcount = shellThicknessCaxisDataset.getSeriesCount();


        for(int i=0; i<totalcount; i++){
            Color temp = giveRGB(max, probabilitiesPerModelFull.get(i));
            //xyitemrenderer.setSeriesOutlinePaint(i, temp);
            xyitemrenderer.setSeriesPaint(i, temp);
            xyitemrenderer.setSeriesOutlinePaint(i, Color.BLACK);
        }

       NumberAxis xAxis = new NumberAxis("c-axis");
//        xAxis.setLowerMargin(0.0);
//        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("shell thickness");
//        yAxis.setLowerMargin(0.0);
//        yAxis.setUpperMargin(0.0);
plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        plot.setRangeGridlinePaint(Color.white);
        plot.setBackgroundPaint(Color.white);

        return plot;
    }


    private Color giveRGB(double maximum, double value){

        double ratio = 2 * value/maximum;

        int blue = (int)(Math.max(0, 255*(1 - ratio)));
        int red = (int)(Math.max(0, 255*(ratio - 1)));
        int green = 255 - blue - red;
        return new Color(red,green,blue, 50);
    }



    public class Pair<L,R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() { return left; }
        public R getRight() { return right; }

        @Override
        public int hashCode() { return left.hashCode() ^ right.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair pairo = (Pair) o;
            return this.left.equals(pairo.getLeft()) &&
                    this.right.equals(pairo.getRight());
        }

    }

}
