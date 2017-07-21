package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by xos81802 on 20/07/2017.
 */
public class RatioSimilarityTest {

    private Collection inUse;
    private double lowerQLimit, upperQLimit;
    private JPanel panel1;
    private JButton startButton;
    private JButton cancelButton;
    private JProgressBar progressBar1;
    private JPanel plotPanel;
    private JTextField qminField;
    private JTextField qmaxField2;
    private JLabel statusLabel;
    public ChartFrame frame;
    public ChartPanel chartPanel;
    private JFreeChart chart;
    private ArrayList<Ratio> ratioModels;
    private int totalDatasets, totalDatasetsInUse, totalToCalculate;

    public RatioSimilarityTest(Collection collectionInUse, double qmin, double qmax) throws Exception {
        inUse = collectionInUse;
        this.lowerQLimit = qmin;
        this.upperQLimit = qmax;
        qminField.setText(String.format(Locale.US, "%.3f", qmin));
        qmaxField2.setText(String.format(Locale.US, "%.3f", qmax));

        totalDatasets = inUse.getDatasetCount();
        totalDatasetsInUse = 0;

        for(int i=0; i<totalDatasets; i++){
            if (inUse.getDataset(i).getInUse()){
                totalDatasetsInUse += 1;
            }
        }

        setLowAndHighQ();

        totalToCalculate = totalDatasetsInUse*(totalDatasetsInUse-1)/2;
        progressBar1.setMaximum(totalToCalculate);
        progressBar1.setValue(0);
        progressBar1.setString("Calculating");


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateRatios();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });





    }

    // calculate pairwise ratio comparision for each in collection
    // make plot

    // find common low-q within limit
    // find common upper-q within limit
    private void setLowAndHighQ() throws Exception {

        int totalDatasets = inUse.getDatasetCount();
        Dataset refDataset = inUse.getDataset(0);

        for(int i=0; i<totalDatasets; i++){
            refDataset = inUse.getDataset(i);
            if (refDataset.getInUse()){
                break;
            }
        }

        //set low-q
        int totalInRef = refDataset.getAllData().getItemCount();
        boolean allclear = false;
        int stopIndex=0;

        for(int i=0; i<totalInRef; i++){

            Number xvalue = refDataset.getAllData().getX(i);

            if (xvalue.doubleValue() >= lowerQLimit){ // check if xvalue is in all other datasets

                int startJ=1;
                boolean isOK = true;

                for(; startJ<totalDatasets; startJ++){
                    Dataset dataset = inUse.getDataset(startJ);

                    if (dataset.getInUse()){
                        if (dataset.getAllData().indexOf(xvalue) == -1){
                            isOK = false;
                            break; // go to next value
                        }
                    }
                }

                if (startJ == totalDatasets && isOK){ // made it to end
                    allclear =true;
                    stopIndex = i;
                    lowerQLimit = xvalue.doubleValue();
                    break;
                }
            }
        }


        if (!allclear){
                throw new Exception(" No common q-value found amongst selected datasets");
        }

        allclear = false;


        for(int i=(totalInRef-1); i>stopIndex; i--){

            Number xvalue = refDataset.getAllData().getX(i);

            if (xvalue.doubleValue() <= upperQLimit){ // check if xvalue is in all other datasets

                int startJ=1;
                boolean isOK = true;

                for(; startJ<totalDatasets; startJ++){
                    Dataset dataset = inUse.getDataset(startJ);

                    if (dataset.getInUse()){
                        if (dataset.getAllData().indexOf(xvalue) == -1){
                            isOK = false;
                            break; // go to next value
                        }
                    }
                }

                if (startJ == totalDatasets && isOK){ // made it to end
                    allclear =true;
                    upperQLimit = xvalue.doubleValue();
                    break;
                }
            }
        }


        if (!allclear){
            throw new Exception(" No common upper q-value found amongst selected datasets");
        }
    }


    // perform ratio calculation
    private void calculateRatios(){

        ratioModels = new ArrayList<>();

        SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>(){

            @Override
            protected Boolean doInBackground() throws Exception {

                ScheduledExecutorService ratioExecutor = Executors.newScheduledThreadPool(4);
                List<Future<Ratio>> ratioFutures = new ArrayList<>();


                for(int i=0; i<totalDatasets; i++){

                    if (inUse.getDataset(i).getInUse()){
                        XYSeries ref = inUse.getDataset(i).getAllData();
                        int refIndex = inUse.getDataset(i).getId();

                        int next = i+1;
                        for(int j=next; j<totalDatasets; j++){

                            Dataset tar = inUse.getDataset(j);
                            if (tar.getInUse()){
                                Future<Ratio> future = ratioExecutor.submit(new CallableRatio(
                                        ref,
                                        tar.getAllData(),
                                        tar.getAllDataError(),
                                        lowerQLimit,
                                        upperQLimit,
                                        refIndex,
                                        tar.getId()
                                ));
                                ratioFutures.add(future);
                            }
                        }
                    }
                }

                System.out.println("ratiofutures " + ratioFutures.size());

                int completed=0;
                for(Future<Ratio> fut : ratioFutures){
                    try {
                        // because Future.get() waits for task to get completed
                        ratioModels.add(fut.get());
                        //update progress bar
                        completed++;
                        publish(completed);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                ratioExecutor.shutdown();
                return true;

            }

            @Override
            protected void done() {
                boolean status;
                try {
                    // Retrieve the return value of doInBackground.
                    status = get();
                    statusLabel.setText("Status : " + status);
                    progressBar1.setValue(0);
                } catch (InterruptedException e) {
                    // This is thrown if the thread's interrupted.
                } catch (ExecutionException e) {
                    // This is thrown if we throw an exception
                    // from doInBackground.
                }
            }

            @Override
            protected void process(List<Integer> chunks) {
                // Here we receive the values that we publish().
                // They may come grouped in chunks.
                int mostRecentValue = chunks.get(chunks.size()-1);
                statusLabel.setText(Integer.toString(mostRecentValue));
                progressBar1.setValue(mostRecentValue);
            }

        };

        worker.execute();

        for(int i=0;i<ratioModels.size(); i++){
            ratioModels.get(i).printTests(Integer.toString(i));
        }


//        plotPanel.removeAll();
//        plotPanel.add(chartPanel);

//        frame = new ChartFrame("S(q) PLOT", chart);
//        frame.setContentPane(this.panel1);
//        frame.setPreferredSize(new Dimension(800,600));
//        frame.getChartPanel().setDisplayToolTips(true);
//        //frame.getChartPanel().setDefaultDirectoryForSaveAs(new File(workingDirectoryName));
//        frame.pack();
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    private void createChart(){
        chart = ChartFactory.createXYLineChart(
                "",                         // chart title
                "q",                   // domain axis label
                "S(q)",                // range axis label
                new XYSeriesCollection(new XYSeries("temp")),                 // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,
                false
        );
    }


    public void makePlot(){

        JFrame frame = new JFrame("Plot");
        frame.setContentPane(this.panel1);
        frame.setPreferredSize(new Dimension(800,600));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

}




