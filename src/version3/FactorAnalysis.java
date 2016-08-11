package version3;

import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

/**
 * Created by robertrambo on 05/08/2016.
 */
public class FactorAnalysis {


    private JPanel panel1;
    private JSlider slider1;
    private JSlider slider2;
    private JButton setPeakButton;
    private JButton SVDButton;
    private JPanel intensityPanel;
    private JPanel secPanel;
    private JComboBox comboBox1;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JButton addButton;

    private Collection samplesCollection;
    private Collection buffersCollection;

    // use defined buffer to make signal plot
    //
    // user define full range of data to apply factor analysis
    // perform factor analysis (forward reverse)
    //

    public FactorAnalysis(Collection samples, Collection selectedBuffers){

        // take specified buffer and do subtraction from each frame
        samplesCollection = samples;
        buffersCollection = selectedBuffers;

        // Subtraction subTemp = new Subtraction(selectedBuffers, samples, finalQmin, finalQmax, false, finalSingles, false, svd, cpuCores, status, mainProgressBar);

        // add other attributes and then run
        // Double.parseDouble(comboBoxSubtractBins.getSelectedItem().toString())/100.00;
//        subTemp.setBinsAndCutoff(Double.parseDouble(comboBoxSubtractBins.getSelectedItem().toString()), Double.parseDouble(subtractionCutOff.getSelectedItem().toString()));
//        subTemp.setNameAndDirectory(subtractionFileNameField.getText(), subtractOutPutDirectoryLabel.getText());
//        subTemp.setCollectionToUpdate(collectionSelected);


        // set sliders


    }




}
