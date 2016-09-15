package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by robertrambo on 06/09/2016.
 */
public class AnalysisICA extends SwingWorker {
    private JComboBox componentBox;
    private JTextField qminText;
    private JTextField qmaxText;
    private JLabel qminLabel;
    private JLabel qmaxLabel;
    private JProgressBar progressBar1;
    private JButton startButton;
    private JPanel sourcesPanel;
    private JPanel percentagePanel;
    private JPanel panel1;
    private JLabel dataSetsLabel;
    private JLabel messageLabel;
    final private double qminLimit;
    final private double qmaxLimit;

    private XYSeriesCollection allData;
    private XYSeriesCollection demixedData;
    private XYSeriesCollection originals;
    private int totalSignals;

    private JFreeChart chart;

    public AnalysisICA(Collection Datasets){
        // create collection
        // use Datasets to create a collection from
        allData = new XYSeriesCollection();
        Collection tempCollection = new Collection();

        messageLabel.setText("");
        messageLabel.setForeground(Constants.MediumRed);

        int totalDatasets = Datasets.getDatasetCount();

        for(int i=0; i<totalDatasets; i++){
            if (Datasets.getDataset(i).getInUse()){
                allData.addSeries(Datasets.getDataset(i).getAllData());
                tempCollection.addDataset(new Dataset(Datasets.getDataset(i)));
            }
        }

        double qmin = Functions.findLeastCommonQvalue(tempCollection);
        double qmax = Functions.findMaximumCommonQvalue(tempCollection);

        qminLimit=qmin;
        qmaxLimit=qmax;

        // user specified values can not exceed these
        qminText.setText(Double.toString(qmin));
        qmaxText.setText(Double.toString(qmax));

        totalSignals = allData.getSeriesCount();
        dataSetsLabel.setText(totalSignals + " input signals");
        // set combobox values
        componentBox.removeAllItems();
        int totalArray = totalSignals-1;
        //Integer[] eigenValues = new Integer[totalArray]; //currentYear is an int variable
        for (int i=0; i<totalArray; i++){
          //  eigenValues[i] = i + 2;
            componentBox.addItem((i+2));
        }


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // create q-limited collection for ICA

            }
        });

        qminText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double temp = Double.parseDouble(qminText.getText());
                if (temp < qminLimit){
                    qminText.setText(Double.toString(qminLimit));
                    messageLabel.setText("Too small, reset");
                }
            }
        });

        qmaxText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double temp = Double.parseDouble(qmaxText.getText());
                if (temp < qmaxLimit){
                    qmaxText.setText(Double.toString(qmaxLimit));
                    messageLabel.setText("Too large, reset");
                }
            }
        });


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // assemble dataset
                // ICA
                final double finalQmin = Double.parseDouble(qminText.getText());
                final double finalQmax = Double.parseDouble(qmaxText.getText());
                final int eigenValues = (Integer)componentBox.getSelectedItem();

                // create test data, two sinusoids
                ArrayList<Double> tempq = new ArrayList<Double>();
                for (int i=0; i<allData.getSeries(0).getItemCount(); i++){
                    double qvalue = allData.getSeries(0).getX(i).doubleValue();
                    if (qvalue >= finalQmin && qvalue <= finalQmax){
                        tempq.add(qvalue);
                    }
                }

                // create sources
                double pseudoDmax = 43.0;
                originals = new XYSeriesCollection();
                    originals.addSeries(new XYSeries("SINE"));
                    for (int j=0; j<tempq.size(); j++){
                        originals.getSeries(0).add(tempq.get(j).doubleValue(), Math.sin(tempq.get(j)*pseudoDmax)/(tempq.get(j)*pseudoDmax));
                    }

                originals.addSeries(new XYSeries("COS"));
                for (int j=0; j<tempq.size(); j++){
                    originals.getSeries(1).add(tempq.get(j).doubleValue(), Math.cos(tempq.get(j)*pseudoDmax));
                }

                Random randomno = new Random();
                originals.addSeries(new XYSeries("NOISE"));
                for (int j=0; j<tempq.size(); j++){
                    originals.getSeries(2).add(tempq.get(j).doubleValue(), randomno.nextGaussian());
                }

                // three mixtures

                XYSeriesCollection mixtures = new XYSeriesCollection();
                double value;
                for (int i=0; i<2; i++){
                    mixtures.addSeries(new XYSeries(i));
                    double first = randomno.nextDouble();
                    double second = 1.0 - first;
                    double third = 0;

                    for (int j=0; j<tempq.size(); j++){
                        value = originals.getSeries(0).getY(j).doubleValue()*first + originals.getSeries(1).getY(j).doubleValue()*second + originals.getSeries(2).getY(j).doubleValue()*third;
                        mixtures.getSeries(i).add(tempq.get(j).doubleValue(), value);
                    }
                }

                //IndependentComponentAnalysis ica = new IndependentComponentAnalysis(finalQmin, finalQmax, mixtures, 2, progressBar1);

                IndependentComponentAnalysis ica = new IndependentComponentAnalysis(finalQmin, finalQmax, allData, eigenValues, progressBar1);
                //ica.runTest();
                ica.execute();
                ica.done();

                demixedData = ica.getSAXSICA();
                makeSourcesPlot();

//                int total = demixedData.getItemCount(0);
//                for (int i=0; i<total; i++){
//                    System.out.println(demixedData.getSeries(0).getX(i) + " " + demixedData.getSeries(0).getY(i));
//                }
//                Thread makeIt = new Thread(){
//                    public void run() {
//                        IndependentComponentAnalysis ica = new IndependentComponentAnalysis(finalQmin, finalQmax, allData, eigenValues, progressBar1);
//                        ica.execute();
//                        ica.done();
//                        demixedData = ica.getSAXSICA();
//                        makeSourcesPlot();
//                    }
//                };
//
//                makeIt.start();
            }
        });
    }

    @Override
    protected Object doInBackground() throws Exception {
        JFrame frame = new JFrame("ICA Analysis");

        frame.setContentPane(panel1);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return null;
    }

    private void makeSourcesPlot(){
        chart = ChartFactory.createXYLineChart(
                "DEMIXED SOURCES PLOT",            // chart title
                "",                 // domain axis label
                "",                 // range axis label
                demixedData,                   // data
                PlotOrientation.VERTICAL,
                false,                    // include legend
                true,                     // toolTip
                false
        );

        final XYPlot plot = chart.getXYPlot();
        plot.setDataset(0, demixedData);
        //plot.setDataset(1, originals);

        XYLineAndShapeRenderer signalRenderer = new XYLineAndShapeRenderer();
        signalRenderer.setBaseShapesVisible(true);
        signalRenderer.setBaseShape(new Ellipse2D.Double(-0.5*6, -0.5*6.0, 6, 6));
        plot.setRenderer(1, signalRenderer);

        ChartPanel outPanel = new ChartPanel(chart);
        sourcesPanel.removeAll();
        sourcesPanel.add(outPanel);

    }
}
