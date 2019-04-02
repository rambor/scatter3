package version3;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.event.*;

public class PofRHistogram extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel panel1;
    private JPanel plotPanel;
    private JLabel plotLabel;

    private XYSeriesCollection plottedCollection;
    private XYSeriesCollection splineCollection;

    HistogramDataset primaryDataset;

    JFreeChart jfreechart;

    private XYSplineRenderer splineRend = new XYSplineRenderer();
    private XYLineAndShapeRenderer renderer1 = new XYSplineRenderer();

    public PofRHistogram(Collection collection, WorkingDirectory workingDirectory) {

        plottedCollection = new XYSeriesCollection();
        splineCollection = new XYSeriesCollection();
        primaryDataset = new HistogramDataset();

        // build files to plot
        int totalC = collection.getDatasetCount();
        for (int i=0; i<totalC; i++){
            if (collection.getDataset(i).getInUse()){
                XYSeries temp = collection.getDataset(i).getRealSpaceModel().getPrDistribution();
                double[] tempArray = new double[temp.getItemCount()];

                for (int j=0; j<temp.getItemCount(); j++){
                    tempArray[j]=temp.getY(j).doubleValue();
                }
                primaryDataset.addSeries(collection.getDataset(i).getFileName(), tempArray, temp.getItemCount());
            }
        }


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

        this.plot();
    }


    public void plot(){
        jfreechart = ChartFactory.createHistogram(
                "P(R) Distributions",
                null,
                null,
                primaryDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        XYPlot xyplot = (XYPlot) jfreechart.getPlot();

        xyplot.setForegroundAlpha(0.85f);
        XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
        xybarrenderer.setBarPainter(new StandardXYBarPainter());

        ChartPanel outPanel = new ChartPanel(jfreechart);

        plotPanel.add(outPanel);

        JFrame frame = new JFrame("Plot");
        frame.setContentPane(this.contentPane);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
