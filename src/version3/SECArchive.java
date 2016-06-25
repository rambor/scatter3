package version3;

import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SECArchive extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea1;
    private JLabel packageInfoLabel;
    private JButton setDirectoryButton;
    private JLabel outputDirLabel;
    private JTextField archiveNameField;
    private JCheckBox zipDirectoryAfterCreationCheckBox;
    private JProgressBar progressBar1;
    private String textForReadMe;
    private WorkingDirectory workingDirectory;

    private Collection collection;
    private Collection buffersCollection;

    public SECArchive(Collection samplesCollection, Collection buffersCollection, WorkingDirectory workingDirectory) {

        this.collection = samplesCollection;
        this.buffersCollection = buffersCollection;

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
            packageInfoLabel.setText("Please provide meaningful info for the README");
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

        ProgressBarWriter progressBarWriter = new ProgressBarWriter(progressBar1, outputDirString, archiveName);
        progressBarWriter.execute();

//        File outputDirectory = new File(outputDirString);
//        boolean success = outputDirectory.mkdirs();
//
//        if (!success) {
//            // Directory creation failed
//            // rename directory
//            outputDirectory.renameTo(new File(archiveName + "_old"));
//            success = outputDirectory.mkdirs();
//        }
//
//
//        if (success){
//
//            writeSamples(outputDirString);
//
//            if (buffersCollection.getDatasetCount() > 0){
//                File outputDirectoryBuffers = new File(outputDirString+"/buffers");
//                success = outputDirectoryBuffers.mkdirs();
//                writeBuffers(outputDirString+"/buffers");
//            }
//
//            FileWriter fstream;
//
//            try{ // create P(r) file
//                // Create file
//                fstream = new FileWriter(outputDirString+ "/README");
//
//                BufferedWriter out = new BufferedWriter(fstream);
//                out.write(String.format("# REMARK README FILE FOR : %s %n", archiveName));
//                out.write(String.format("# REMARK %s %n", textForReadMe));
//                out.write(String.format("# REMARK %n"));
//                out.write(String.format("# REMARK FILES WRITTEN OUT : %n"));
//                int totalInCollection = collection.getDatasetCount();
//
//                for (int i=0; i<totalInCollection; i++){ // write out all files
//                        out.write(String.format("%3d => %s\t%s %n", (i+1), collection.getDataset(i).getOriginalFilename(), collection.getDataset(i).getFileName() ));
//                }
//                //Close the output stream
//                out.close();
//            }catch (Exception e){//Catch exception if any
//                System.err.println("Error: " + e.getMessage());
//            }
//
//        }

        if (zipDirectoryAfterCreationCheckBox.isSelected()){
            //make zip file
        }

        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }



    private void writeBuffers(String outputDir){

        int total = buffersCollection.getDatasetCount();
        int index = 1;

        for(int i=0; i<total; i++){

            Dataset temp = buffersCollection.getDataset(i);
            // makes copy of the original data
            temp.copyAndRenameDataset("buffer_"+index, outputDir);
            // Make Signal Plot?
            //tempImages.createAndWriteKratkyChart(temp.getFileName(), outputDir)
            index++;
        }
    }



    private void writeSamples(String outputDir){

        int total = collection.getDatasetCount();
        int index = 1;
        for(int i=0; i<total; i++){
            Dataset temp = collection.getDataset(i);
            // makes copy of the original data
            temp.copyAndRenameDataset("sample_"+index, outputDir);
            // Make Signal Plot?
            //tempImages.createAndWriteKratkyChart(temp.getFileName(), outputDir);
            index++;
        }
    }



    public class ProgressBarWriter extends SwingWorker<Void, Void> {

        JProgressBar bar;
        String outputDirString;
        String archiveName;

        public ProgressBarWriter(JProgressBar bar, String output, String archiveName){
            this.bar = bar;
            this.outputDirString = output;
            this.archiveName = archiveName;
        }

        @Override
        protected Void doInBackground() throws Exception {


            bar.setString("Writing Files");
            bar.setIndeterminate(true);

            File outputDirectory = new File(outputDirString);
            boolean success = outputDirectory.mkdirs();

            if (!success) {
                // Directory creation failed
                // rename directory
                outputDirectory.renameTo(new File(archiveName + "_old"));
                success = outputDirectory.mkdirs();
            }


            if (success){

                writeSamples(outputDirString);

                if (buffersCollection.getDatasetCount() > 0){
                    File outputDirectoryBuffers = new File(outputDirString+"/buffers");
                    success = outputDirectoryBuffers.mkdirs();
                    writeBuffers(outputDirString+"/buffers");
                }

                FileWriter fstream;

                try{ // create P(r) file
                    // Create file
                    fstream = new FileWriter(outputDirString+ "/README");

                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(String.format("# REMARK README FILE FOR : %s %n", archiveName));
                    out.write(String.format("# REMARK %s %n", textForReadMe));
                    out.write(String.format("# REMARK %n"));
                    out.write(String.format("# REMARK FILES WRITTEN OUT : %n"));
                    int totalInCollection = collection.getDatasetCount();

                    for (int i=0; i<totalInCollection; i++){ // write out all files
                        out.write(String.format("%3d => %s\t%s %n", (i+1), collection.getDataset(i).getOriginalFilename(), collection.getDataset(i).getFileName() ));
                    }
                    //Close the output stream
                    out.close();
                }catch (Exception e){//Catch exception if any
                    System.err.println("Error: " + e.getMessage());
                }

            }
            bar.setString("");
            bar.setIndeterminate(false);
            return null;
        }
    }

}
