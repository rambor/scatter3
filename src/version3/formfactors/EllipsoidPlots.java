package version3.formfactors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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

    private HashMap<Integer, Double> probabilities;
    private XYSeries probabilitiesPerRadiiA;
    private XYSeries probabilitiesPerRadiiB;
    private XYSeries probabilitiesPerRadiiC;
    private ArrayList<Double> calculatedIntensities;
    private ArrayList<Double> transformedObservedIntensities;
    private ArrayList<Double> transformedObservedErrors;
    private XYSeriesCollection heatMapCollection;
    private XYSeriesCollection residualsCollection;
    private XYSeriesCollection averageCollection;
    private double minRc;
    private double maxRc;
    private double minRb;
    private double maxRb;
    private double minRa;
    private double maxRa;
    private boolean useNoBackGround;

    private Double[] qvalues;

    private final ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList;

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
                          ConcurrentSkipListMap<Double, ArrayList<Integer>> keptList,
                          HashMap<Integer, Double> probabilities,
                          Double[] qvalues,
                          ArrayList<Double> transformedObservedIntensities,
                          ArrayList<Double> transformedObservedErrors,
                          boolean useNoBackground){

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

        heatMapCollection = new XYSeriesCollection();
        residualsCollection = new XYSeriesCollection();
        averageCollection = new XYSeriesCollection();

    }


    private void makeHistogramData(){

        TreeMap<Double, Double> r_a = new TreeMap<>();
        TreeMap<Double, Double> r_b = new TreeMap<>();
        TreeMap<Double, Double> r_c = new TreeMap<>();

        //prolate and oblate ellipsoids don't have r_b
        probabilitiesPerRadiiA = new XYSeries("probabilitiesA");
        probabilitiesPerRadiiB = new XYSeries("probabilitiesB"); // empty series if no radii
        probabilitiesPerRadiiC = new XYSeries("probabilitiesC");

        if(models.get(0).getModelType() == ModelType.ELLIPSOID){

            for(int i=0; i<probabilities.size(); i++){
                // convert probabilities into radii
                Ellipse model =  (Ellipse) models.get(i);

                if (r_a.containsKey(model.getRadius_a())){
                    r_a.put(model.getRadius_a(), r_a.get(model.getRadius_a()) + probabilities.get(i));
                } else {
                    r_a.put(model.getRadius_a(), probabilities.get(i));
                }

                if (r_b.containsKey(model.getRadius_b())){
                    r_b.put(model.getRadius_b(), r_b.get(model.getRadius_b()) + probabilities.get(i));
                } else {
                    r_b.put(model.getRadius_b(), probabilities.get(i));
                }

                if (r_c.containsKey(model.getRadius_c())){
                    r_c.put(model.getRadius_c(), r_c.get(model.getRadius_c()) + probabilities.get(i));
                } else {
                    r_c.put(model.getRadius_c(), probabilities.get(i));
                }
            }

            for(Map.Entry<Double, Double> entry : r_b.entrySet()) {
                probabilitiesPerRadiiB.add(entry.getKey(), entry.getValue());
            }
        } else {

            for(int i=0; i<probabilities.size(); i++){
                // convert probabilities into radii
                ProlateEllipsoid model =  (ProlateEllipsoid) models.get(i);

                if (r_a.containsKey(model.getRadius_a())){
                    r_a.put(model.getRadius_a(), r_a.get(model.getRadius_a()) + probabilities.get(i));
                } else {
                    r_a.put(model.getRadius_a(), probabilities.get(i));
                }

                if (r_c.containsKey(model.getRadius_c())){
                    r_c.put(model.getRadius_c(), r_c.get(model.getRadius_c()) + probabilities.get(i));
                } else {
                    r_c.put(model.getRadius_c(), probabilities.get(i));
                }
            }
        }

        for(Map.Entry<Double, Double> entry : r_a.entrySet()) {
            probabilitiesPerRadiiA.add(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<Double, Double> entry : r_c.entrySet()) {
            probabilitiesPerRadiiC.add(entry.getKey(), entry.getValue());
        }

    }


    public void create(boolean makeFigures){
      //  makeGeomtricData();

        makeResiduals();
        makeHistogramData();

      //  makeGeometricPlot();
      //  makeHistogramPlot();
        makeResidualsPlot();
        makeDataPlot();
        createDistributionPlots();
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
        for (Map.Entry<Double, ArrayList<Integer>> entry : keptList.entrySet()) {

            ArrayList<Integer> indices = entry.getValue();
            // for each index, great XYSeries and Add
            int total = indices.size();

            for (int i=0; i<total; i++){
                Model model = models.get(indices.get(i));
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
