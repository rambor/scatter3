package version3.formfactors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYZDataset;
import version3.Constants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by robertrambo on 26/11/2016.
 */
public class ThreeBodyPlots {

    private JPanel geometricPanel;
    private JPanel residualsPanel;
    private JPanel dataPlotPanel;
    private JPanel distributionRSmallestPanel;
    private JPanel distributionRMiddlePanel;
    private JPanel distributionRLargestPanel;

    private ArrayList<Model> models;

    private ArrayList<Double> calculatedIntensities;
    private ArrayList<Double> transformedObservedIntensities;
    private ArrayList<Double> transformedObservedErrors;
    private XYSeriesCollection residualsCollection;
    private XYSeriesCollection averageCollection;
    private XYSeriesCollection pointsCollection;
    private boolean useNoBackGround;
    private final KeptModels keptList;
    private ArrayList<Double> probabilities;
    private XYSeries probabilitiesPerRadiiR12;
    private XYSeries probabilitiesPerRadiiR23;
    private XYSeries probabilitiesPerRadiiR13;

    private ArrayList<Double> firstRadii;
    private ArrayList<Double> secondRadii;
    private ArrayList<Double> thirdRadii;
    private double upperMostGeometry;
    private double lowerMostGeometry;
    private double leftMostGeometry;
    private double rightMostGeometry;

    private Double[] qvalues;
    private DefaultXYZDataset xyzdataset;

    public ThreeBodyPlots(
            JPanel leftPanel,
            JPanel residualsPanel,
            JPanel heatMapPanel,
            JPanel r13Panel,
            JPanel r23Panel,
            JPanel dataPlotPanel,
            ArrayList<Model> models,
            KeptModels keptList,
            ArrayList<Double> probabilities,
            Double[] qvalues, ArrayList<Double> transformedIntensities,
            ArrayList<Double> transformedErrors,
            boolean useNoBackground) {


        this.geometricPanel = leftPanel;
        this.residualsPanel = residualsPanel;
        this.dataPlotPanel = dataPlotPanel;
        this.useNoBackGround = useNoBackground;

        this.models = models;
        this.keptList = keptList;
        this.probabilities = probabilities;
        this.qvalues = qvalues;
        this.transformedObservedIntensities = transformedIntensities;
        this.transformedObservedErrors = transformedErrors;

        distributionRSmallestPanel = heatMapPanel;
        distributionRMiddlePanel = r13Panel;
        distributionRLargestPanel = r23Panel;

        residualsCollection = new XYSeriesCollection();
        averageCollection = new XYSeriesCollection();

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

    public void create(boolean makeFigures){
        //  makeGeomtricData();
        makeResiduals();
        makeHistogramData();
        makeGeometricPlot();
        //makeHistogramPlot();
        makeResidualsPlot();
        makeDataPlot();
        createDistributionPlots();
    }


    private void createDistributionPlots(){

        JFreeChart chartA = ChartFactory.createXYBarChart(
                "Body 1 to 2",
                "distance",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiR12),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        JFreeChart chartB = ChartFactory.createXYBarChart(
                "Body 2 to 3",
                "distance",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiR13),
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        JFreeChart chartC = ChartFactory.createXYBarChart(
                "Body 1 to 3",
                "distance",
                false,
                "",
                new XYSeriesCollection(probabilitiesPerRadiiR23),
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
        distributionRSmallestPanel.removeAll();
        distributionRSmallestPanel.add(histogramChartPanelA);

        distributionRMiddlePanel.removeAll();
        distributionRMiddlePanel.add(histogramChartPanelB);

        distributionRLargestPanel.removeAll();
        distributionRLargestPanel.add(histogramChartPanelC);

    }


    private void makeHistogramData(){

        TreeMap<Double, Double> r_12 = new TreeMap<>();
        TreeMap<Double, Double> r_23 = new TreeMap<>();
        TreeMap<Double, Double> r_13 = new TreeMap<>();

        //prolate and oblate ellipsoids don't have r_b
        probabilitiesPerRadiiR12 = new XYSeries("probabilitiesR12");
        probabilitiesPerRadiiR23 = new XYSeries("probabilitiesR23"); // empty series if no radii
        probabilitiesPerRadiiR13 = new XYSeries("probabilitiesR13");

        ArrayList<Double> firstRadii = new ArrayList<>();
        ArrayList<Double> secondRadii = new ArrayList<>();
        ArrayList<Double> thirdRadii = new ArrayList<>();

        ArrayList<Double> xvalues = new ArrayList<>();
        ArrayList<Double> yvalues = new ArrayList<>();
        ArrayList<Double> radii = new ArrayList<>();

        // model 1-2-3 == 3-2-1 == 2-3-1
        double[] triplet = new double[3];
        double minLimit = 0.01;
        double sum_x=0, sum_y=0, sumfirst=0, smallest=0, middle=0, largest=0;
        double counter = 1.0d;
        double volume=0;

        for(int i=0; i<probabilities.size(); i++){
            // convert probabilities into radii
            ThreeBody model =  (ThreeBody) models.get(i);
            volume+=probabilities.get(i)*model.getVolume();

            // sort the three radii in order o=
            // for each model, i need to determine correct order
            triplet[0] = model.getSortedDistanceByIndex(0);
            triplet[1] = model.getSortedDistanceByIndex(1);
            triplet[2] = model.getSortedDistanceByIndex(2);

            if (probabilities.get(i) > minLimit && triplet[2] > triplet[1]){
                double smallest2 = triplet[0]*triplet[0];
                double middle2 = triplet[1]*triplet[1];
                double largest2 = triplet[2]*triplet[2];

                smallest+=model.getSmallest();
                middle+=model.getMiddle();
                largest+=model.getLargest();

                firstRadii.add(model.getSmallest());
                secondRadii.add(model.getMiddle());
                thirdRadii.add(model.getLargest());

                //smallest
                xvalues.add(-1*triplet[0]);
                yvalues.add(0.0d);
                radii.add(2*model.getSmallest());
                //middle
                xvalues.add(0.0d);
                yvalues.add(0.0d);
                radii.add(2*model.getMiddle());

                double radian = (smallest2+middle2-largest2)/(2*triplet[0]*triplet[1]);
                // acos  1 => 0
                // acos -1 => PI
                if (radian > 1){
                    radian = 0;
                } else if (radian < -1){
                    radian = Math.PI;
                } else {
                    radian = Math.acos(radian);
                }

                double angle = Math.PI - radian;
                model.printOrder();
                System.out.println(i + " Angle " + (angle*180/Math.PI) + " " + (smallest2+middle2-largest2)/(2*triplet[0]*triplet[1]));
                sumfirst+=-1*triplet[0];
                //geometry1.add(-1*triplet[0],0); // along x-axis
                //firstRadii.add()
                //geometry2.add(0,0); // always at origin

                double xtemp;
                if (angle > Math.PI/2){
                    xtemp = -1*triplet[1]*Math.cos(angle);
                } else {
                    xtemp = triplet[1]*Math.cos(angle);
                }

                double ytemp =triplet[1]*Math.sin(angle);
                //largest
                xvalues.add(xtemp);
                yvalues.add(ytemp);
                radii.add(2*model.getLargest());

                sum_x+=xtemp;
                sum_y+=ytemp;
                counter++;
            }

            // distribution of smallest radii
            if (r_12.containsKey(triplet[0])){
                r_12.put(triplet[0], r_12.get(triplet[0]) + probabilities.get(i));
            } else {
                r_12.put(triplet[0], probabilities.get(i));
            }
            // distribution second smallest
            if (r_23.containsKey(triplet[1])){
                r_23.put(triplet[1], r_23.get(triplet[1]) + probabilities.get(i));
            } else {
                r_23.put(triplet[1], probabilities.get(i));
            }
            // distribution of third radii
            if (r_13.containsKey(triplet[2])){
                r_13.put(triplet[2], r_13.get(triplet[2]) + probabilities.get(i));
            } else {
                r_13.put(triplet[2], probabilities.get(i));
            }
        }


        for(Map.Entry<Double, Double> entry : r_12.entrySet()) {
            probabilitiesPerRadiiR12.add(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<Double, Double> entry : r_23.entrySet()) {
            probabilitiesPerRadiiR23.add(entry.getKey(), entry.getValue());
        }

        for(Map.Entry<Double, Double> entry : r_13.entrySet()) {
            probabilitiesPerRadiiR13.add(entry.getKey(), entry.getValue());
        }

        double[] xArray = new double[xvalues.size()];
        double[] yArray = new double[xvalues.size()];
        double[] radiiArray = new double[xvalues.size()];

        for(int i=0; i<xvalues.size(); i++){
            xArray[i] = xvalues.get(i);
            yArray[i] = yvalues.get(i);
            radiiArray[i] = radii.get(i);
        }

        double[][] series = new double[][]{xArray, yArray, radiiArray};

        System.out.println("Radii first => " + (sumfirst/counter) + " third => " + (sum_x/counter));
        double[] x = {sumfirst/counter, 0, sum_x/counter};
        double[] y = {0, 0, sum_y/counter};
        double dis = Math.sqrt(x[2]*x[2]/(counter*counter) + y[2]*y[2]/(counter*counter));

        System.out.println("first dis " + x[0] + " <= " + dis);

        double[] z = {2*smallest/counter, 2*middle/counter, 2*largest/counter}; // radii

        leftMostGeometry = x[0] - (z[0] + z[0]*0.5);
        rightMostGeometry = x[2] + z[2] + z[2]*0.5;

        lowerMostGeometry = -(z[1] + z[1]*0.3);
        upperMostGeometry = y[2] + z[2] + z[2]*0.3;

        System.out.println("first radii " + z[0] + " <= " + z[2]);
        System.out.println("TRIAXIAL VOLUME " + volume);

        xyzdataset = new DefaultXYZDataset();
        xyzdataset.addSeries("Series 1", series);

    }



    private void makeResiduals(){

        // calculate model intensities
        calculatedIntensities = new ArrayList<>();
        int totalq = models.get(0).getTotalIntensities();
        while(calculatedIntensities.size() < totalq) calculatedIntensities.add(0.0d);

        int count=0;

        //for (Map.Entry<Double, ArrayList<Integer>> entry : keptList.entrySet()) {

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

//            ArrayList<Integer> indices = entry.getValue();
//            // for each index, great XYSeries and Add
//            int total = indices.size();
//
//            for (int i=0; i<total; i++){
//                Model model = models.get(indices.get(i));
//                for(int q=0; q<totalq; q++){
//                    calculatedIntensities.set(q, calculatedIntensities.get(q).doubleValue() + model.getIntensity(q));
//                    count++;
//                }
//            }
        //}

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

    private void makeGeometricPlot(){

        JFreeChart chart = ChartFactory.createBubbleChart(
                "",
                "X",
                "Y",
                xyzdataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false

        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer xyitemrenderer = plot.getRenderer();
        xyitemrenderer.setSeriesPaint( 0 , new Color(0, 204, 0, 5) );
        xyitemrenderer.setBaseOutlinePaint(new Color(0, 204, 0, 1) );

        final NumberAxis domainAxis = new NumberAxis("x");
        final NumberAxis rangeAxis = new NumberAxis("y");
        domainAxis.setAutoRange(true);
        rangeAxis.setRange(leftMostGeometry, rightMostGeometry);
        rangeAxis.setAutoRange(true);
        rangeAxis.setRange(lowerMostGeometry, upperMostGeometry);
        rangeAxis.setAutoRangeStickyZero(false);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        plot.getRenderer().setSeriesStroke(0,new BasicStroke(2.1f));
        plot.getRenderer().setSeriesPaint(1, Color.CYAN);
        plot.getRenderer().setSeriesStroke(1,new BasicStroke(4.0f));
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setBackgroundPaint(Color.lightGray);

        ChartPanel chartPanel = new ChartPanel(chart);
        //outPanel.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        geometricPanel.removeAll();
        geometricPanel.add(chartPanel);
    }


}
