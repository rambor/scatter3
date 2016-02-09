package version3;

import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class PackageIt extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea1;
    private JLabel packageInfoLabel;
    private JButton setDirectoryButton;
    private JLabel outputDirLabel;
    private JTextField archiveNameField;
    private JCheckBox zipDirectoryAfterCreationCheckBox;
    private String textForReadMe;
    private WorkingDirectory workingDirectory;

    private Collection collection;

    public PackageIt(Collection collection, WorkingDirectory workingDirectory) {
        this.collection = collection;
        this.workingDirectory = workingDirectory;
        outputDirLabel.setText(workingDirectory.getWorkingDirectory());

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

        setDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser;
                chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File(workingDirectory.getWorkingDirectory()));
                chooser.setDialogTitle("SELECT DIRECTORY");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //
                // disable the "All files" option.
                //
                chooser.setAcceptAllFileFilterUsed(false);
                //
                if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {

                    if (chooser.getSelectedFile().isDirectory()){
                        workingDirectory.setWorkingDirectory(chooser.getSelectedFile().toString());
                    } else {
                        workingDirectory.setWorkingDirectory(chooser.getCurrentDirectory().toString());
                    }

                    outputDirLabel.setText(workingDirectory.getWorkingDirectory());
                }
                else {
                    System.out.println("No Selection ");
                }
            }
        });

    }

    private void onOK() {
        // add your code here
        textForReadMe = textArea1.getText();
        if (textForReadMe.length() < 1){
            packageInfoLabel.setText("Please provide meaningful info for Zip");
            return;
        }

        if (archiveNameField.getText().length() < 1){
            packageInfoLabel.setText("Provide an file name for archive");
            return;
        }

        String archiveName = archiveNameField.getText().replaceAll("\\W","_");
        // sanitize the name
        // create directory
        String outputDirString = workingDirectory.getWorkingDirectory() + "/" + archiveName;
        File outputDirectory = new File(outputDirString);
        boolean success = outputDirectory.mkdirs();

        if (!success) {
            // Directory creation failed
            // rename directory
            outputDirectory.renameTo(new File(archiveName + "_old"));
            success = outputDirectory.mkdirs();
        }

        System.out.println("Success " + success);
        if (success){
            createArchive(outputDirString);

            FileWriter fstream;

            try{ // create P(r) file
                // Create file
                fstream = new FileWriter(outputDirString+ "/README");

                BufferedWriter out = new BufferedWriter(fstream);
                out.write(String.format("README FILE FOR %s %n", archiveName));

                //out.write("REMARK 265    P(r)-DISTRIBUTION BASED ON : " + dataset.getFileName() + "\n");
                //out.write("REMARK 265 \n");
                //out.write("REMARK 265 \n");
                //out.write("REMARK 265  SCALED P(r) DISTRIBUTION \n");

                //Close the output stream
                out.close();
            }catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

        }

        if (zipDirectoryAfterCreationCheckBox.isSelected()){
            //make zip file
        }

        dispose();

    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    private void createArchive(String outputDir){

        int total = collection.getDatasetCount();

        FileObject outputObject = new FileObject(new File(outputDir));

        for(int i=0; i<total; i++){
            if (collection.getDataset(i).getInUse()){
                Dataset temp = collection.getDataset(i);
                // makes copy of the original data
                temp.copyAndRenameDataset(temp.getFileName(), outputDir);

                ScatterImagesOfData tempImages = new ScatterImagesOfData(temp);
                // write out real space file
                if (temp.getRealSpaceModel().getRg() > 0 && temp.getRealSpaceModel().getTotalMooreCoefficients() > 1){
                    temp.getRealSpaceModel().estimateErrors();
                    outputObject.writePRFile(temp, new JLabel(""), temp.getFileName(), outputDir, false);
                    // make pr image
                    tempImages.createAndWritePrChart(temp.getFileName(), outputDir);
                }

                // Kratky
                tempImages.createAndWriteKratkyChart(temp.getFileName(), outputDir);
                // qIq plot
                tempImages.createAndWriteQIQChart(temp.getFileName(), outputDir);
                // log10 plot with errors
                tempImages.createAndWriteLog10PlotWithErrorsChart(temp.getFileName(), outputDir);
            }
        }
    }



}
