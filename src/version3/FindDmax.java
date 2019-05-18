package version3;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import version3.InverseTransform.*;
import version3.RealSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FindDmax extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JProgressBar progressBar1;
    private JTextField dmaxLowerBoundField;
    private JTextField dmaxUpperBoundField;
    private JTextField lambdaLowerField;
    private JTextField lambdaUpperField;
    private JTextField qmaxField;
    private JLabel jInfoLabel;
    private JPanel likelihoodPanel;
    private JLabel bestLabel;
    private JTextField trailsTextField;
    private JPanel vcPanel;
    private JCheckBox legendreCheckBox;
    private JCheckBox useBkgrndCheckBox;
    private JCheckBox l1NormCheckBox;
    private JCheckBox mooreCheckBox;
    private JLabel statusLabel;
    private JFreeChart chart;
    private  ChartPanel outPanel;
    private  boolean crosshair = true, useLegendre = false, useMoore = true, useL1Norm = false;
    private  WorkingDirectory workingDirectory;
    private double maximumMaximumLikelihood=0;

    private AtomicInteger counter = new AtomicInteger(0);
    RealSpace dataset;

    double lowerDmax, upperDmax;
    double lowerQmax, upperQmax;
    double lowerLambda, upperLambda;
    double standardizedMin, standardizedScale, suggestqmax;

    int totalTrialsCE = 17, qbins;
    double stepSize = 0.5;

    ArrayList<XYSeries> dmaxVsChi;
    XYSeries dmaxVsDW, standardizedSeries, scaled_q_times_Iq_Errors, likelihood;

    public FindDmax(RealSpace dataset, double currentqmax, WorkingDirectory workingDirectory) {

        dataset.resetStartStop();
        this.dataset = dataset;
        qmaxField.setText(Double.toString(currentqmax));
        upperQmax = currentqmax;
        suggestqmax = upperQmax;
        this.workingDirectory = workingDirectory;

        lowerDmax = Double.parseDouble(dmaxLowerBoundField.getText());
        upperDmax = Double.parseDouble(dmaxUpperBoundField.getText());

        lowerLambda = Double.parseDouble(lambdaLowerField.getText());
        upperLambda = Double.parseDouble(lambdaUpperField.getText());

       // dmaxVsChi = new XYSeries("Chi2");
        dmaxVsDW = new XYSeries("DW");


        this.standardizeData();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        JFrame frame = new JFrame("Plot");
        frame.setContentPane(this.contentPane);
        frame.setPreferredSize(new Dimension(800,600));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        lambdaLowerField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    double value = Double.parseDouble(lambdaLowerField.getText());
                    if (value < 0){
                        lambdaLowerField.setText("0.0001");
                        lowerLambda = 0.0001;
                    } else if (value > (Double.parseDouble(lambdaUpperField.getText()))) {
                        lambdaLowerField.setText("0.0001");
                        lowerLambda = 0.0001;
                        lambdaUpperField.setText("50");
                        upperLambda = 50;
                    } else {
                        lowerLambda = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        lambdaUpperField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    double value = Double.parseDouble(lambdaUpperField.getText());
                    if (value < 0){
                        lambdaUpperField.setText("50");
                        upperLambda = 50;
                    } else if (value < (Double.parseDouble(lambdaLowerField.getText()))) {
                        lambdaLowerField.setText("0.0001");
                        lowerLambda = 0.0001;
                        lambdaUpperField.setText("50");
                        upperLambda = 50;
                    } else {
                        upperLambda = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        dmaxLowerBoundField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    double value = Double.parseDouble(dmaxLowerBoundField.getText());
                    if (value < 0){
                        dmaxLowerBoundField.setText("50");
                        lowerDmax = 50;
                    } else if (value > (Double.parseDouble(dmaxUpperBoundField.getText()))) {
                        dmaxLowerBoundField.setText("50");
                        lowerDmax = 50;
                        dmaxUpperBoundField.setText("200");
                        upperDmax = 200;
                    } else {
                        lowerDmax = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        dmaxUpperBoundField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    double value = Double.parseDouble(dmaxUpperBoundField.getText());
                    if (value < 0){
                        dmaxUpperBoundField.setText("50");
                        upperDmax = 200;
                    } else if (value < (Double.parseDouble(dmaxLowerBoundField.getText()))) {
                        dmaxLowerBoundField.setText("50");
                        lowerDmax = 50;
                        dmaxUpperBoundField.setText("200");
                        upperDmax = 200;
                    } else {
                        upperDmax = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        qmaxField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    double value = Double.parseDouble(qmaxField.getText());
                    if (value < 0){
                        qmaxField.setText(Double.toString(upperQmax));
                    } else if (value > 3) {
                        qmaxField.setText(Double.toString(upperQmax));
                    } else {
                        upperQmax = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        qmaxField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                try {
                    double value = Double.parseDouble(qmaxField.getText());
                    if (value < 0){
                        qmaxField.setText(Double.toString(upperQmax));
                    } else if (value > 3) {
                        qmaxField.setText(Double.toString(upperQmax));
                    } else {
                        upperQmax = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        dmaxUpperBoundField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                try {
                    double value = Double.parseDouble(dmaxUpperBoundField.getText());
                    if (value < 0){
                        dmaxUpperBoundField.setText("50");
                        upperDmax = 200;
                    } else if (value < (Double.parseDouble(dmaxLowerBoundField.getText()))) {
                        dmaxLowerBoundField.setText("50");
                        lowerDmax = 50;
                        dmaxUpperBoundField.setText("200");
                        upperDmax = 200;
                    } else {
                        upperDmax = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        dmaxLowerBoundField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                try {
                    double value = Double.parseDouble(dmaxLowerBoundField.getText());
                    if (value < 0){
                        dmaxLowerBoundField.setText("50");
                        lowerDmax = 50;
                    } else if (value > (Double.parseDouble(dmaxUpperBoundField.getText()))) {
                        dmaxLowerBoundField.setText("50");
                        lowerDmax = 50;
                        dmaxUpperBoundField.setText("200");
                        upperDmax = 200;
                    } else {
                        lowerDmax = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });


        lambdaLowerField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                try {
                    double value = Double.parseDouble(lambdaLowerField.getText());
                    if (value < 0){
                        lambdaLowerField.setText("0.0001");
                        lowerLambda = 0.0001;
                    } else if (value > (Double.parseDouble(lambdaUpperField.getText()))) {
                        lambdaLowerField.setText("0.0001");
                        lowerLambda = 0.0001;
                        lambdaUpperField.setText("50");
                        upperLambda = 50;
                    } else {
                        lowerLambda = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        lambdaUpperField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                try {
                    double value = Double.parseDouble(lambdaUpperField.getText());
                    if (value < 0){
                        lambdaUpperField.setText("50");
                        upperLambda = 50;
                    } else if (value < (Double.parseDouble(lambdaLowerField.getText()))) {
                        lambdaLowerField.setText("0.0001");
                        lowerLambda = 0.0001;
                        lambdaUpperField.setText("50");
                        upperLambda = 50;
                    } else {
                        upperLambda = value;
                    }
                    updateInfo();
                }
                catch (NumberFormatException ee) {
                    //Not an integer
                    jInfoLabel.setText("Not a proper number, should be positive and only numbers.");
                }
            }
        });

        trailsTextField.setText(Integer.toString(totalTrialsCE));
        trailsTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int value = Integer.parseInt(trailsTextField.getText());
                if (value < 0){
                    trailsTextField.setText("17");
                    totalTrialsCE = 17;
                } else {
                    totalTrialsCE = value;
                }
            }
        });

        makeVcPlot();
        qmaxField.setText(Double.toString(suggestqmax));
        updateInfo();

        legendreCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (legendreCheckBox.isSelected()){

                } else {
                    legendreCheckBox.setSelected(true);
                }
                mooreCheckBox.setSelected(false);
                l1NormCheckBox.setSelected(false);
                useLegendre = true;
                useMoore = false;
                useL1Norm = false;
                lambdaLowerField.setText("1");
                lambdaUpperField.setText("331");
            }
        });


        l1NormCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (l1NormCheckBox.isSelected()){

                } else {
                    l1NormCheckBox.setSelected(true);
                }
                mooreCheckBox.setSelected(false);
                legendreCheckBox.setSelected(false);
                useLegendre = true;
                useMoore = false;
                useL1Norm = true;
                lambdaLowerField.setText("0.01");
                lambdaUpperField.setText("10");
            }
        });

        mooreCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mooreCheckBox.isSelected()){

                } else {
                    mooreCheckBox.setSelected(true);
                }
                legendreCheckBox.setSelected(false);
                l1NormCheckBox.setSelected(false);
                useBkgrndCheckBox.setSelected(true);
                useLegendre = false;
                useMoore = true;
                useL1Norm = false;
                lambdaLowerField.setText("1");
                lambdaUpperField.setText("100");
            }
        });

        l1NormCheckBox.setSelected(false);
        legendreCheckBox.setSelected(false);
        mooreCheckBox.setSelected(true);
    }

    private void onOK() {
        buttonOK.setEnabled(false);
        // add your code here
        dataset.resetStartStop();

        int startQmaxIndex = 0;
        lowerQmax = 0.17;

        XYSeries allData = dataset.getfittedqIq();
        for(int i=0; i<allData.getItemCount(); i++){
            if (allData.getX(i).doubleValue() > lowerQmax){
                startQmaxIndex = i-1;
                break;
            }
        }

        int lastIndex = allData.getItemCount()-1;

        for(int i=0; i<allData.getItemCount(); i++){
            if (allData.getX(i).doubleValue() > upperQmax){
                lastIndex = i-1;
                break;
            }
        }

        // set qmax
        dataset.decrementHigh(lastIndex);
        this.standardizeData();

        double topN = 0.0031;

        qbins = 3*(int)(upperDmax/Math.PI*dataset.getfittedqIq().getMaxX());


//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);
//        for (int i=0; i < numberOfCPUs; i++){
//            Runnable bounder = new RefinePrManager.Refiner(
//                    this.standardizedSeries,
//                    this.scaled_q_times_Iq_Errors,
//                    roundsPerCPU,
//                    statusLabel);
//            executor.execute(bounder);
//        }
//
//        executor.shutdown();

//        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
//        for (int i=0; i < 4; i++){
//            Runnable bounder = new FindDmax.Refiner(
//                    this.standardizedSeries,
//                    this.scaled_q_times_Iq_Errors,
//                    totalTrialsCE,
//                    topN,
//                    statusLabel,
//                    useLegendre);
//
//            executor.execute(bounder);
//        }

        FindDmax.Refiner runnable = new FindDmax.Refiner(
                this.standardizedSeries,
                this.scaled_q_times_Iq_Errors,
                totalTrialsCE,
                topN,
                statusLabel,
                useLegendre,
                useL1Norm,
                useMoore
                );


        runnable.execute();


        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private class DMax {
        private Number dmax;
        private double count;
        public DMax(double dmax){
            this.dmax = (Number)dmax;
            this.count = 0.0d;
        }
        public void update(double value) { this.count += value;}
        public double getCount(){ return this.count;}
    }


    private class DmaxLambda implements Comparable {
        private final double dmax, lambda;
        private final Number number;
        private double probability;
        private double score, averagingBinWidth;
        private double dw, chi, prscore;
        private double score_sum;
        private int index, increment, disIncrement;
        private int binCount;
        private XYSeries tempDistribution, averagedDistribution;

        public DmaxLambda(double dmax, double lambda, int index){
            this.dmax = dmax;
            this.number = (Number)dmax;
            this.lambda = lambda;
            this.probability = 0.50;
            this.index = index;
            this.score = 0;
            this.increment = 0;
        }

        public DmaxLambda(double dmax, double lambda, double score, int index){
            this.dmax = dmax;
            this.number = (Number)dmax;
            this.lambda = lambda;
            this.score = score;
            this.index = index;
            this.increment = 1;
        }

        public int getIndex() { return index;}
        public double getDmax() { return dmax;}
        public Number getNumber() { return number;}
        public double getLambda() { return lambda;}
        public void updateProbability(double value){ this.probability = value;}
        public double getProbability(){ return probability;}
        public void setProbability(double value){ this.probability = value;}

        public double getScore(){ return score;}
        public void setDWChi(double dw, double chi, double prs){ this.dw = dw; this.chi=chi; this.prscore = prs;}
        public void printDWChi(){
            System.out.println(dmax + " " + dw + " " + chi + " " + prscore);
        }
        public String getStatString(){
            return String.format("dmax %.1f DW %.1E AIC %.2f quality %.3f lambda %.2E", (double)dmax, dw, chi, prscore, lambda); }


        public void initializeDistribution(XYSeries dis, int totalBinsInSearch){
            binCount = dis.getItemCount();
            tempDistribution = new XYSeries("temp", true, true);
            /*
             * fill
             */
            int total = dis.getItemCount()-1; // ignore dmax
            for(int i=1; i<total; i++){
                XYDataItem item = dis.getDataItem(i);
                tempDistribution.add(item);
            }

            disIncrement = 1;
        }

        /*
         * averages tempDistribution into averagedDistribution
         */
        public void updateFinalDistribution(){
            averagedDistribution = new XYSeries("average", true, false);

            double sumIt = 0;
            /*
             * tempDistribution contains duplicates
             */
            int total = tempDistribution.getItemCount();

            double firstq = tempDistribution.getX(1).doubleValue();

            int startIndex = 0;

            binCount -= 2; //remove the first and last counts (r=0 and r=dmax)
            averagingBinWidth = dmax/(double)binCount;
            final double diff = 0.5*averagingBinWidth;


            double startr = diff;//tempDistribution.getX(startIndex).doubleValue();
            double upperr = averagingBinWidth;
            double lowerr = 0;

            while(startIndex < total){

                ArrayList<Double> values = new ArrayList<>();

                double sumV = 0, countsum=0.0;
                for(int i=startIndex; i<total; i++){
                    XYDataItem item1 = tempDistribution.getDataItem(i);

                    if (item1.getXValue() > lowerr && item1.getXValue() <= upperr){
                        sumV += item1.getYValue();
                        countsum +=1.0;
                        values.add(item1.getYValue());
                        startIndex = i;
                    } else {
                        startIndex = i;
                        break;
                    }
                }
                //System.out.println("VALUES " + values.size() + " " + lowerr + " < " + upperr);
                double medianv = 0;
                if (values.size() > 0){
                    Collections.sort(values);
                    int medianLocale = (values.size()-1)/2;
                    medianv = values.get(medianLocale);

                    if (values.size()%2 == 0){
                        medianv = (medianv + values.get(medianLocale+1))*0.5;
                    }
                }

                double ave = (countsum > 0) ? sumV/countsum : 0;
                //System.out.println(startIndex + " " + startr + " " + ave + " u " + upperr + " dx " + dmax + " " + averagingBinWidth);
                //averagedDistribution.add(startr, ave);
                averagedDistribution.add(startr, medianv);

                if (startIndex == (total-1)){
                    break;
                }

                startr += averagingBinWidth;//tempDistribution.getX(startIndex).doubleValue();
                upperr += averagingBinWidth;
                lowerr += averagingBinWidth;
            }

            averagedDistribution.add(0,0);
            averagedDistribution.add(dmax, 0);
        }

        public void updateScore(double value){ score_sum += value; increment += 1; }

        /**
         * Dis is the Pr Distribution from a fit, first value is [0,0] and last shoudld be [dmax, 0]
         * @param value
         * @param dis
         */
        public void update(double value, XYSeries dis){

            int total = dis.getItemCount()-1; // exclude model dmax

            if (dis.getItemCount() > binCount){
                binCount = dis.getItemCount();
            }

            for(int i=1; i<total; i++){ // exclude first point [0] since it is zero
                XYDataItem item2 = dis.getDataItem(i);
                tempDistribution.add(item2);
            }

            score_sum += value;
            increment += 1;
            disIncrement += 1;
        }

        public void averageScore(){ score = score_sum/(double)increment;}

        public XYSeries getPrDistribution(){ return averagedDistribution; }

        public void printDistribution(){
            System.out.println("DISTRIBUTION " + lambda + " " + dmax);
            for(int i=0; i<averagedDistribution.getItemCount(); i++){
                System.out.println(i + " " + averagedDistribution.getX(i) + " " + averagedDistribution.getY(i));
            }
        }

        @Override
        public int compareTo(Object o) {
            return this.getScore() <  ((DmaxLambda) o).getScore() ? -1 : (this.getScore() == ((DmaxLambda) o).getScore() ? 0 : 1) ;
        }
    }


    public class Refiner extends SwingWorker<Void, Void> {

        private XYSeries activeSet;
        private XYSeries errorActiveSet;
        private XYSeries relativeErrorSeries;
        private int totalTrials, totalRuns, topN, last;
        private double qBinWidth, qmin, topNpercent;
        private double llambda, ulambda;
        private boolean useLegendreFunction=false, useL1=false, useMoore=false;

        private JLabel status;

        /**
         *
         * @param qIq
         * @param allError (error must be in the form of q*I(q))
         * @param totalTrials
         * @param topNPercent
         * @param status
         */
        public Refiner(XYSeries qIq, XYSeries allError, int totalTrials, double topNPercent, JLabel status, boolean useLegendre, boolean useL1, boolean useMoore){

            int totalItems = qIq.getItemCount() ;
            this.topNpercent = topNPercent;
            this.totalTrials = totalTrials;
            this.useLegendreFunction = useLegendre;
            this.useL1 = useL1;
            this.useMoore = useMoore;
            // create standardized dataset
            try {
                activeSet = qIq.createCopy(0, totalItems-1); // possibly scaled by rescaleFactor
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            errorActiveSet = new XYSeries("Error qIq");

            for (int i=0; i<totalItems; i++){
                XYDataItem tempData = allError.getDataItem(i);
                errorActiveSet.add(tempData.getXValue(), tempData.getYValue()/standardizedScale); // transformed as q*I(q)
            }

            qmin = activeSet.getMinX();
            this.status = status;
        }


        @Override
        public Void doInBackground() {
            counter.set(0);

            Random randomGenerator = new Random();

            int startbb, upperbb, samplingNumber, locale, samplingLimit;
            double tempMedian;

            /*
             * prepare the parameter container holding all possible dmax and lambda value combinations
             */
            ArrayList<DmaxLambda> parameters = new ArrayList<>();

            HashMap<Number, DMax> dmaxes = new HashMap<>();

            ArrayList<Integer>  paramIndices = new ArrayList<Integer>();
            int indexTracker = 0;

            double startDmax = lowerDmax;
            for (; startDmax < upperDmax; startDmax += 0.5){

                double lambda = lowerLambda;
                dmaxes.put((Number)startDmax, new DMax(startDmax));

                while(lambda < upperLambda){
                    parameters.add(new DmaxLambda(startDmax, lambda, indexTracker));
                    lambda *= 1.7;
                    paramIndices.add(indexTracker);
                    indexTracker++;
                }
            }

            initializeBar(indexTracker);

            int totalParameters = parameters.size();
            topN = (int)(topNpercent*totalParameters);
            totalRuns = 7*totalParameters;
            this.last = topN-1;
            DmaxLambda[] topNModels = new DmaxLambda[topN];
            /*
             * partition the active set into bins for sampling
             */
            int size = activeSet.getItemCount();
            qBinWidth = (activeSet.getMaxX() - qmin)/(double)qbins;
            double averagingbinWidth = Math.PI/activeSet.getMaxX();

            final int totalBinsInModel = (int)(Math.floor(startDmax/averagingbinWidth) + 1);

            ArrayList<ArrayList<Integer>> binIndices = new ArrayList<>();

            startbb = 0;

            for(int i=0; i<qbins; i++){
                binIndices.add(new ArrayList<>());

                for (int bb=startbb; bb < size; bb++){
                    // b*pi_invDmax is cardinal point in reciprocal space
                    if (activeSet.getX(bb).doubleValue() < ((i+1)*qBinWidth + qmin) ){ // what happens on the last bin?
                        binIndices.get(i).add(bb);
                    } else {
                        startbb = bb;
                        break;
                    }
                }
            }

            int maxElementsInLastBins = (int)(0.25*(binIndices.get(qbins-1).size() + binIndices.get(qbins-2).size() + binIndices.get(qbins-3).size() + binIndices.get(qbins-4).size()));

            double slopeN = (2 + 0.55*maxElementsInLastBins)/(double)qbins;
            double interceptN = 2.0;



            /*
             * perform CE optimization
             */
            XYSeries randomSeries = new XYSeries("Random-");
            XYSeries randomSeriesError = new XYSeries("Random-Error");
            double current_score, tempDmax=100, tempLambda=0.01;

            for(int trial=0; trial<totalTrialsCE; trial++){

                randomSeries.clear();
                randomSeriesError.clear();

                for(int i=0; i<qbins; i++){ // grab random indices from each q-bin
                    ArrayList<Integer> arrayList = new ArrayList<>(binIndices.get(i));
                    samplingNumber = arrayList.size();
                    // use increasing number of points as we go to higher q

                    if (samplingNumber > 0){

                        Collections.shuffle(arrayList);
                        int getThis = (int)(i*slopeN + interceptN);
                        if (getThis > samplingNumber){
                            samplingLimit = (int)(samplingNumber*0.7);
                        } else {
                            samplingLimit = getThis;
                        }

//                        if (samplingNumber < 10 ){
//                            samplingLimit = (1 + randomGenerator.nextInt(arrayList.size()));
//                        } else {
//                            samplingLimit = (1 + randomGenerator.nextInt(10));
//                        }

                        int swapTo = samplingNumber-1;
                        for(int h=0; h < samplingLimit; h++){
//                            int getIndex = randomGenerator.nextInt(samplingNumber);
//                            locale = arrayList.get(getIndex);
                            locale = arrayList.get(h);
                            randomSeries.add(activeSet.getDataItem(locale));
                            randomSeriesError.add(errorActiveSet.getDataItem(locale));
//                            Collections.swap(arrayList, getIndex, swapTo);
//                            swapTo -= 1;
//                            samplingNumber-=1;
                        }
                    }
                }

                // for the randomly chosen set, perform fits over d-max, lambda space
                for(int p=0; p<totalParameters; p++){
                    // choose dmax/lambda
                    DmaxLambda param = parameters.get(p);
                    tempDmax = param.getDmax();
                    tempLambda = param.getLambda();

                    IndirectFT tempIFT;

                    if (useL1){
                        tempIFT = new SineIntegralTransform(
                                randomSeries,
                                randomSeriesError,
                                tempDmax,
                                //randomSeries.getMaxX(),
                                activeSet.getMaxX(),
                                tempLambda,
                                standardizedMin,
                                standardizedScale,
                                useBkgrndCheckBox.isSelected());

                    } else if (useLegendre) {
                        tempIFT = new LegendreTransform(
                                randomSeries,
                                randomSeriesError,
                                tempDmax,
                                //randomSeries.getMaxX(),
                                activeSet.getMaxX(),
                                tempLambda,
                                standardizedMin,
                                standardizedScale,
                                useBkgrndCheckBox.isSelected());
                    } else {
                        tempIFT = new MooreTransformApache(
                                randomSeries,
                                randomSeriesError,
                                tempDmax,
                                //randomSeries.getMaxX(),
                                activeSet.getMaxX(),
                                tempLambda,
                                standardizedMin,
                                standardizedScale,
                                true);
                    }

                    double kvalue = tempIFT.ns + 1 + 1 + 1; // lambda, dmax and noise
//                    double chi = tempIFT.getChiEstimate();
                    tempIFT.makeResiduals();

//                    double aic = tempIFT.calculateChiFromDataset(activeSet, errorActiveSet);
                    double aic = 2.0*kvalue + tempIFT.ns*tempIFT.calculateChiFromDataset(activeSet, errorActiveSet) + (2.0*kvalue*kvalue + 2*kvalue)/(randomSeries.getItemCount() - kvalue -1);
                    double kt = tempIFT.getKurtosisEstimate(0);

                    current_score = 0.1*aic + 1000*kt + 3*tempIFT.getPrScore();
                    /*
                     * aic is a large number, in the 10s to 100s.  KT will be small like less than 1, and PrScore is also around 1
                     *
                     */

                    param.setDWChi(kt, aic, tempIFT.getPrScore());

                    if (trial == 0){ // variations in qmax will lead to variaions in shannon point, so we use Pi/qmax to define binwith
                        param.updateScore(current_score);
                        param.initializeDistribution(tempIFT.getPrDistribution(), totalBinsInModel);
                    } else {
                        param.update(current_score, tempIFT.getPrDistribution());
                    }

                    this.increment();
                }
            }

            /*
             * average the scores to prepare for sorting
             */
            for(int i=0; i<totalParameters; i++){
                DmaxLambda temp = parameters.get(i);
                temp.averageScore();
            }
            Collections.sort(parameters);

            /*
             * average the score
             * set probability weight
             * initialize the first model in sorted list as best
             * models are sorted from lowest score
             */
            double expSum=0;
            double best = parameters.get(0).getScore();
            parameters.get(0).setProbability(1.0d);
            parameters.get(0).updateFinalDistribution();

            dmaxes.get(parameters.get(0).getDmax()).update(1.0d);

            for(int i=1; i<totalParameters; i++){
                DmaxLambda temp = parameters.get(i);
                temp.setProbability(Math.exp((best-temp.getScore())*0.5));
                // boltzman weighting
                expSum += temp.getProbability();

                dmaxes.get(temp.getDmax()).update(temp.getProbability());
                temp.updateFinalDistribution();
            }

            /*
             * Prepare KDE estimate
             * Sum over all values to determine normalization constant
             */
            double normalization=0;
            Set set = dmaxes.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                normalization+=((DMax)mentry.getValue()).getCount();
            }

            // how many bins do I need?
            XYSeries finalModel = new XYSeries("finalModel", true, true);
            /*
             * Kernel Density Estimate
             */
            likelihood = new XYSeries("likelihood");
            double bandwith = 1.0/2.5;
            maximumMaximumLikelihood=0;
            double inv2PI = 1.0/(Math.sqrt(2*Math.PI));
            Number mostlikelyDmax=0;
            for (double iDmax = lowerDmax; iDmax < upperDmax; iDmax += 0.5){

                set = dmaxes.entrySet();
                iterator = set.iterator();
                double sumKDE = 0.0d;
                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    ((DMax)mentry.getValue()).getCount();
                    double diff = (iDmax-(double)mentry.getKey())*bandwith;
                    sumKDE += ((DMax)mentry.getValue()).getCount()*Math.exp(-0.5*diff*diff);
                }

                double value=sumKDE*inv2PI/normalization;
                if (value > maximumMaximumLikelihood){ // which DMAX has the highest likelihood
                    maximumMaximumLikelihood = value;
                    mostlikelyDmax = (Number)iDmax;
                }

                likelihood.add(iDmax, value);
            }

            bestLabel.setText("Best : " + parameters.get(0).getStatString() + " :: Most Likely Dmax : " + mostlikelyDmax);
            //plot the most likely model
            /*
             * perform weighted averaging on top 30
             */
            try {
                finalModel = parameters.get(0).getPrDistribution().createCopy(0, parameters.get(0).getPrDistribution().getItemCount()-1);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            XYSeriesCollection totalBest = new XYSeriesCollection();

            int addedCounter = 0;
            ArrayList<Color> shades = new ArrayList<>();

            for(int i=1; i<totalParameters; i++){
                DmaxLambda temp = parameters.get(i);
                if (temp.number.equals(mostlikelyDmax)){
                    XYSeries tempDis = temp.getPrDistribution();
                    int totalIn = tempDis.getItemCount();

                    totalBest.addSeries(new XYSeries ("Added " + Integer.toString(addedCounter)));
                    XYSeries nextSeries = totalBest.getSeries(addedCounter);
                    shades.add(new Color(1, 0, 0, (float)temp.getProbability()));

                    for(int j=0; j<totalIn; j++){ // first bin is always 0 from IFT model
                        XYDataItem item = tempDis.getDataItem(j);
                        //averageModel.add(item);
                        nextSeries.add(item);
                    }
                    addedCounter += 1;
                }
            }

            // do final fit with all data
//            IndirectFT tempIFT;
//            if (!useLegendreFunction){
//                tempIFT = new SineIntegralTransform(
//                        activeSet,
//                        errorActiveSet,
//                        parameters.get(0).getDmax(),
//                        activeSet.getMaxX(),
//                        parameters.get(0).getLambda(),
//                        standardizedMin,
//                        standardizedScale,
//                        useBkgrndCheckBox.isSelected());
//
//            } else {
//                tempIFT = new LegendreTransform(
//                        activeSet,
//                        errorActiveSet,
//                        parameters.get(0).getDmax(),
//                        activeSet.getMaxX(),
//                        parameters.get(0).getLambda(),
//                        standardizedMin,
//                        standardizedScale,
//                        useBkgrndCheckBox.isSelected());
//            }


            totalBest.addSeries(finalModel);
//            totalBest.addSeries(tempIFT.getPrDistribution());
            //System.out.println("FINAL MODEL " + finalModel.getItemCount());
            //updatePr(finalModel);

            makeLikelihoodPlot();
            makePlot(totalBest, shades);
            //makeVcPlot();
            progressBar1.setValue(0);
            progressBar1.setStringPainted(false);
            buttonOK.setEnabled(true);
            return null;
        }

        private void makeLikelihoodPlot(){
            XYSeriesCollection plotMe = new XYSeriesCollection();
            plotMe.addSeries(likelihood);

            JFreeChart chartX = ChartFactory.createXYLineChart(
                    "",                     // chart title
                    "dmax",                        // domain axis label
                    "likelihood",                // range axis label
                    plotMe,                 // data
                    PlotOrientation.VERTICAL,
                    false,                       // include legend
                    false,
                    false
            );

            final XYPlot plot = chartX.getXYPlot();

            final NumberAxis domainAxis = new NumberAxis("dmax, \u212B");
            final NumberAxis rangeAxis = new NumberAxis("Likelihood Score");
            domainAxis.setAutoRangeIncludesZero(false);
            domainAxis.setAutoRange(true);
            rangeAxis.setAutoRange(true);
            rangeAxis.setAxisLineVisible(true);

            ChartPanel outPanelX = new ChartPanel(chartX){
                @Override
                public void restoreAutoBounds(){
                    super.restoreAutoDomainBounds();
                    super.restoreAutoRangeBounds();
                    super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);

                    int seriesCount = super.getChart().getXYPlot().getDataset(0).getSeriesCount();
                    super.getChart().getXYPlot().getRangeAxis().setRange(-0.00001, (maximumMaximumLikelihood + 0.1*maximumMaximumLikelihood));
                    super.getChart().getXYPlot().getDomainAxis().setRange(lowerDmax, upperDmax);
                }
            };

            plot.setDomainAxis(domainAxis);
            plot.setRangeAxis(rangeAxis);
            plot.configureDomainAxes();
            plot.configureRangeAxes();
            plot.setBackgroundAlpha(0.0f);

            plot.setDomainCrosshairLockedOnData(true);
            //plot.setBackgroundAlpha(0.0f);
            plot.setRangeZeroBaselineVisible(true);
            plot.setOutlineVisible(false);

            //make crosshair visible
            plot.setDomainCrosshairVisible(true);
            plot.setRangeCrosshairVisible(true);

            XYSplineRenderer splineRend = new XYSplineRenderer();
            XYLineAndShapeRenderer renderer1 = new XYSplineRenderer();

            splineRend.setBaseShapesVisible(false);
            renderer1.setBaseShapesVisible(true);

            renderer1.setBaseStroke(new BasicStroke(2.0f));

            // renderer1.setBaseLinesVisible(false);
            int locale = 0;
            splineRend.setSeriesStroke(0, dataset.getStroke());
            splineRend.setSeriesPaint(0, dataset.getColor().darker()); // make color slight darker

            //plot.setDataset(0, splineCollection);  //Moore Function
            plot.setRenderer(0, splineRend);       //render as a line

            chartX.getXYPlot().setDomainCrosshairVisible(false);
            chartX.getXYPlot().setRangeCrosshairVisible(false);

            outPanelX.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
            likelihoodPanel.removeAll();
            likelihoodPanel.add(outPanelX);
        }




        private void makePlot(XYSeriesCollection plottedCollection, ArrayList<Color> colors){
            chart = ChartFactory.createXYLineChart(
                    "Composite Distribution",                     // chart title
                    "r",                        // domain axis label
                    "P(r)",                // range axis label
                    plottedCollection,                 // data
                    PlotOrientation.VERTICAL,
                    false,                       // include legend
                    false,
                    false
            );

            final XYPlot plot = chart.getXYPlot();

            final ValueMarker yMarker = new ValueMarker(0.0d);
            plot.addRangeMarker(yMarker);

            final NumberAxis domainAxis = new NumberAxis("r, \u212B");
            final NumberAxis rangeAxis = new NumberAxis("P(r)");
            domainAxis.setAutoRangeIncludesZero(true);
            domainAxis.setAutoRange(true);
            rangeAxis.setAutoRange(true);
            rangeAxis.setAxisLineVisible(true);

            rangeAxis.setTickLabelsVisible(false);

            //org.jfree.data.Range domainBounds = dataset.getDomainBounds(true);
            //org.jfree.data.Range rangeBounds = dataset.getRangeBounds(true);

            outPanel = new ChartPanel(chart){
                @Override
                public void restoreAutoBounds(){
                    super.restoreAutoDomainBounds();
                    super.restoreAutoRangeBounds();
                    super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);

                    int seriesCount = super.getChart().getXYPlot().getDataset(0).getSeriesCount();
                    super.getChart().getXYPlot().getRangeAxis().setRange(-0.01, plottedCollection.getSeries(0).getMaxY() + 0.15*plottedCollection.getSeries(0).getMaxY());
                    super.getChart().getXYPlot().getDomainAxis().setRange(0, upperDmax);
                }
            };

            plot.setDomainAxis(domainAxis);
            plot.setRangeAxis(rangeAxis);
            plot.configureDomainAxes();
            plot.configureRangeAxes();
            plot.setBackgroundAlpha(0.0f);

            plot.setDomainCrosshairLockedOnData(true);
            //plot.setBackgroundAlpha(0.0f);
            plot.setRangeZeroBaselineVisible(true);

            //make crosshair visible
            plot.setDomainCrosshairVisible(true);
            plot.setRangeCrosshairVisible(true);

            plot.setOutlineVisible(false);

            XYSplineRenderer splineRend = new XYSplineRenderer();
            splineRend.setBaseShapesVisible(false);

            for(int i=0; i<colors.size(); i++){
                splineRend.setSeriesStroke(i, new BasicStroke(4.0f));
                splineRend.setSeriesPaint(i, colors.get(i));
            }

            int lastLocale = plottedCollection.getSeriesCount()-1; // color all the data
            splineRend.setSeriesOutlineStroke(lastLocale, dataset.getStroke());
            splineRend.setSeriesPaint(lastLocale, dataset.getColor().darker()); // make color slight darker
            splineRend.setSeriesShapesVisible(lastLocale, true);
            splineRend.setSeriesStroke(lastLocale, new BasicStroke(5.0f));
            splineRend.setSeriesShape(lastLocale,new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
            splineRend.setSeriesShapesFilled(lastLocale, true);

//            lastLocale -= 1; // color the compositive "averaged" dataset
//            splineRend.setSeriesOutlineStroke(lastLocale, dataset.getStroke());
//            splineRend.setSeriesPaint(lastLocale, Color.cyan); // make color slight darker
//            splineRend.setSeriesShapesVisible(lastLocale, true);
//            splineRend.setSeriesStroke(lastLocale, new BasicStroke(5.0f));
//            splineRend.setSeriesShape(lastLocale,new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));
//            splineRend.setSeriesShapesFilled(lastLocale, true);


            plot.setRenderer(0, splineRend);       //render as a line

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
            leftPanel.removeAll();
            leftPanel.add(outPanel);
        }

//        private synchronized void updatePr(XYSeries dis){
//            try {
//                averagedPr = dis.createCopy(0, dis.getItemCount()-1);
//            } catch (CloneNotSupportedException e) {
//                e.printStackTrace();
//            }
//        }

        private synchronized void initializeBar(int total){
            progressBar1.setStringPainted(true);
            progressBar1.setValue(0);
            progressBar1.setMaximum(total*totalTrialsCE);
        }


        private synchronized void increment() {
            int currentCount = counter.incrementAndGet();
            progressBar1.setValue((int)(currentCount));
        }


        /**
         *
         * @param lower int
         * @param upper int
         * @param percent double
         * @return
         */
        private int[] randomIntegersBounded(int lower, int upper, double percent){
            java.util.List<Integer> numbers = new ArrayList<Integer>();

            int total = 0;
            for(int i = lower; i < upper; i++) {
                numbers.add(i);
                total++;
            }

            // Shuffle them
            Collections.shuffle(numbers);
            int count = (int)Math.ceil(total*percent);
            // Pick count items.
            List<Integer> tempNumbers = numbers.subList(0, count);
            int[] values = new int[count];

            for(int i=0; i < count; i++){
                values[i] = tempNumbers.get(i);
            }

            Arrays.sort(values);
            return values;
        }
    } // end of runnable class



    /**
     *
     * standardize data
     * create error dataset that matches q for fitted region
     */
    private void standardizeData(){
        this.standardizedSeries = new XYSeries("Standard set");
        this.scaled_q_times_Iq_Errors = new XYSeries("fitted errors");

        XYDataItem tempData, tempError;
        XYSeries fitteddqIqRegion = dataset.getfittedqIq();

        int totalItems = fitteddqIqRegion.getItemCount();
        double sum = 0, minavg=10000000, tempminavg;
        int windowSize = 7;
        double invwindow = 1.0/(double)windowSize;
        for(int r=0; r<(totalItems-windowSize); r++){
            tempminavg = 0;
            for (int w=0; w<windowSize; w++){
                tempminavg += fitteddqIqRegion.getDataItem(r).getYValue();
            }

            if (tempminavg < minavg){
                minavg = tempminavg;
            }
        }

        standardizedMin = minavg*invwindow;//nonData.getMinY();
        standardizedScale = Math.abs(fitteddqIqRegion.getMaxY() - standardizedMin);

        double invstdev = 1.0/standardizedScale;

        for(int r=0; r<totalItems; r++){
            tempData = fitteddqIqRegion.getDataItem(r);
            //scaled_q_times_Iq_Errors.add(dataset.getErrorAllData().getDataItem(dataset.getErrorAllData().indexOf(tempData.getX())));
            tempError = dataset.getErrorAllData().getDataItem(dataset.getErrorAllData().indexOf(tempData.getX()));
            scaled_q_times_Iq_Errors.add(tempData.getX(), tempError.getYValue()*tempError.getXValue());  // q*sigma/scale
            standardizedSeries.add(tempData.getX(), (tempData.getYValue() - standardizedMin)*invstdev);
        }
    }

    private void updateInfo(){
        bestLabel.setText(String.format("Search | dmax => %.1f to %.1f | lambda => %.1E to %.1E | qmax : %.4f", lowerDmax, upperDmax, lowerLambda,upperLambda, upperQmax));
    }


    private void makeVcPlot(){
        /*
         * use the first point in fitted region as starting q
         * Prepare the data
         */
        XYSeries fitteddqIqRegion = dataset.getfittedqIq();
        XYSeries originalData = dataset.getOriginalqIq();

        int startIndex = dataset.getOriginalqIq().indexOf(fitteddqIqRegion.getX(0));

        double base = originalData.getX(startIndex).doubleValue();
        double height = originalData.getY(startIndex).doubleValue();

        double area = 0.5*base*height;

        XYSeries vcData = new XYSeries("Vc");
        vcData.add(originalData.getX(startIndex), area);
        startIndex+=1;

        double tempsum;
        int limit = originalData.getItemCount()-1;
        for (int i = startIndex; i < limit; i++){
            XYDataItem tempXY = originalData.getDataItem(i);
            if (i == limit -1){ //last point for trapezoid rule
                tempsum = area + tempXY.getYValue();
                vcData.add(tempXY.getX(), tempXY.getXValue()/(2.0*(i+1))*tempsum);

            } else {
                tempsum = area + tempXY.getYValue();
                vcData.add(tempXY.getX(), tempXY.getXValue()/(2.0*(i+1))*tempsum);
                area = area + 2.0*tempXY.getYValue();
            }
        }

        // find the smallest positive slope
        int locale = 1;
        double slope = 10000, intercept=0, diff, h_value;
        int window = 30;
        int half = window/2;
        int totalInVc = vcData.getItemCount()-window;
        double[] xvalues = new double[window];
        double[] yvalues = new double[window];
        XYDataItem winItem;

        XYSeries slopes = new XYSeries("slopes");
        double slopeSum = 0;
        for(int i=window; i<totalInVc; i++){

            int startHere = i - half;
            for(int j=0; j<window; j++){
                winItem = vcData.getDataItem(startHere + j);
                xvalues[j] = winItem.getXValue();
                yvalues[j] = winItem.getYValue();
            }

            double[] params = Functions.leastSquares(xvalues, yvalues);
            slopes.add(vcData.getX(i), params[0]);
            slopeSum += params[0];

            if (params[0] < slope && params[0] > 0){
                slope = params[0];
                intercept=params[1];
                locale = i;
            }
        }

        // find region of slopes with smallest variance
        int totalSlopes = slopes.getItemCount();
        double averageSlope = slopeSum/(double)slopes.getItemCount();
        int startHere = 0;

        // find where average slope in the window is < averageSlope
        for(int j=startHere; j<(totalSlopes - window); j++){

            double tempSum=0;
            for(int h=0; h<window; h++){
                tempSum += slopes.getY(h+j).doubleValue();
            }
            if (tempSum/(double)window < averageSlope){
                averageSlope = tempSum/(double)window;
                startHere =j;
                break;
            }
        }

        //Interpolator
//        int totalInSlopes = slopes.getItemCount();
//        LoessInterpolator loessInterpolator=new LoessInterpolator(
//                67.0/totalInSlopes,//bandwidth,
//                2//robustnessIters
//        );
//
//        double x[]=new double[totalInSlopes];
//        double y[]=new double[totalInSlopes];
//        for(int i=0; i<totalInSlopes; i++){
//            XYDataItem tempItem = slopes.getDataItem(i);
//            x[i] = tempItem.getXValue();
//            y[i] = tempItem.getYValue();
//        }
//
//        double y2[]=loessInterpolator.smooth(x, y);
//        XYSeries newSlopes = new XYSeries("New Data");
//        for(int i=0; i<totalInSlopes; i++){
//            newSlopes.add(x[i], y2[i]);
//        }

        window = (int)(slopes.getItemCount()*0.1);
        // find longest stretch with smallest variance
        double tempvar, variance = 1000, sum_sq, val, tempSlope;
        int lastqIndex = 0;
        totalSlopes -= window;
        while (window > 34){
            /*
             * walk the window down and determine variance with lowest slope
             */
            double invWindow = 1.0/(double)window;
            for(int j=startHere; j< totalSlopes; j++){
                sum_sq=0;
                slopeSum=0;
                for(int h=0; h<window; h++){
                    val = slopes.getY(j+h).doubleValue();
                    sum_sq += val*val;
                    slopeSum += val;
                }
                tempSlope = invWindow*slopeSum;
                tempvar = invWindow*sum_sq - tempSlope*tempSlope;
                if (tempvar < variance ){
                    variance = tempvar;
                    averageSlope = tempSlope;
                    lastqIndex = j + window;
                }
            }
            window -= 1;
            totalSlopes += 1;
            //System.out.println(window + " < " + totalSlopes + " -- " + startHere);
        }


        suggestqmax = slopes.getX(lastqIndex).doubleValue();
        final ValueMarker yMarker = new ValueMarker(suggestqmax);
        yMarker.setPaint(Color.red);

        XYDataItem localeItem = vcData.getDataItem(locale);

//        XYSeries lineSeries = new XYSeries("Line");
//        lineSeries.add(0,intercept);
//        lineSeries.add(vcData.getMaxX(),slope*vcData.getMaxX() + intercept);


        /*
         * make the plot
         */
        XYSeriesCollection plotMe = new XYSeriesCollection();
        XYSeriesCollection plotMe2 = new XYSeriesCollection();
        plotMe.addSeries(vcData);
//        plotMe.addSeries(lineSeries);
        plotMe2.addSeries(slopes);
//        plotMe2.addSeries(newSlopes);

        JFreeChart chartX = ChartFactory.createXYLineChart(
                "",                     // chart title
                "q",                        // domain axis label
                "Integral q \u00D7 I(q)",                // range axis label
                plotMe,                 // data
                PlotOrientation.VERTICAL,
                false,                       // include legend
                false,
                false
        );

        final XYPlot plot = chartX.getXYPlot();
        plot.addDomainMarker(yMarker);

        final NumberAxis domainAxis = new NumberAxis("q (\u212B \u207B\u00B9)");
        final NumberAxis rangeAxis = new NumberAxis("Integral q\u00D7I(q)");

        final NumberAxis axis2 = new NumberAxis("Slopes");
        axis2.setAutoRangeIncludesZero(true);
        axis2.setLabelPaint(Color.gray.darker());
        axis2.setTickLabelsVisible(false);

        plot.setRangeAxis(1, axis2);
        plot.setDataset(1, plotMe2);
        plot.mapDatasetToRangeAxis(1,1);

        domainAxis.setAutoRangeIncludesZero(false);
        domainAxis.setAutoRange(true);
        rangeAxis.setAutoRange(true);
        rangeAxis.setAxisLineVisible(true);
        rangeAxis.setLabelPaint(dataset.getColor().darker());
        rangeAxis.setTickLabelsVisible(false);

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.configureDomainAxes();
        plot.configureRangeAxes();
        plot.setBackgroundAlpha(0.0f);
        plot.setOutlineVisible(false);

        plot.setDomainCrosshairLockedOnData(true);

//        rangeAxis.setRange(0, vcData.getMaxY() + 0.1*vcData.getMaxY());

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setBackgroundPaint(null);
        XYSplineRenderer rendereriVc = new XYSplineRenderer();
        plot.setRenderer(0, rendereriVc);

        rendereriVc.setBaseLinesVisible(true);
        rendereriVc.setBaseShapesVisible(false);
        rendereriVc.setBaseShapesFilled(false);

        //set dot size for all series
        rendereriVc.setSeriesLinesVisible(0, true);
        //rendereriVc.setSeriesShape(count, new Ellipse2D.Double(-3.0, -3.0, 3.0, 3.0));
        rendereriVc.setSeriesShapesFilled(0, false);
        rendereriVc.setSeriesVisible(0, true);
        rendereriVc.setSeriesPaint(0, dataset.getColor());
        rendereriVc.setSeriesStroke(0, new BasicStroke(2.0f));


        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        renderer1.setSeriesPaint(0, Color.gray);
        renderer1.setSeriesShapesFilled(0, false);
        renderer1.setSeriesShapesVisible(0, false);
        renderer1.setSeriesStroke(0, new BasicStroke(3.0f));
        plot.setRenderer(1, renderer1);


        ChartPanel outPanelX = new ChartPanel(chartX){
            @Override
            public void restoreAutoBounds(){
                super.restoreAutoDomainBounds();
                super.restoreAutoRangeBounds();
                super.getChart().getXYPlot().getRangeAxis().setAutoRange(false);
                super.getChart().getXYPlot().getRangeAxis().setRange(0, (vcData.getMaxY() + 0.1*vcData.getMaxY()));
//                    super.getChart().getXYPlot().getDomainAxis().setRange(lowerDmax, upperDmax);
            }
        };

        chartX.getXYPlot().setDomainCrosshairVisible(false);
        chartX.getXYPlot().setRangeCrosshairVisible(false);


        outPanelX.setDefaultDirectoryForSaveAs(new File(workingDirectory.getWorkingDirectory()));
        vcPanel.removeAll();
        vcPanel.add(outPanelX);
    }


}




