package version3.sasCIF;

import version3.Constants;
import version3.Dataset;

import javax.swing.*;

public class SASCIFForm {
    private JTextField detailsThicknessField;
    private JTextField detailTitleField;
    private JTextField detailsExposureTimeField;
    private JTextField detailsTemperatureField;
    private JTextField beamWavelengthField;
    private JTextField beamNameField;
    private JComboBox beamSourceComboBox;
    private JTextField beamCityField;
    private JTextField beamCountryField;
    private JLabel titleLabel;
    private JButton cancelButton;
    private JButton upateButton;
    private JTextField detailsNumberOfFrames;
    private JCheckBox inverseAngstromsCheckBox;
    private JTextArea detailsCommentsArea;
    private JTextField beamWidthAtSample;
    private JTextField beamHeightAtSample;
    private JButton addComponentButton;
    private JPanel bufferComponentsPanel;

    public SASCIFForm(Dataset dataset){
        SasDetails details = dataset.getSasDetails();
        detailTitleField.setText(details.getTitle());
        detailsTemperatureField.setText(Constants.TwoDecPlace.format(details.getTemperature()));
    }
}
