package version3;

import org.apache.commons.io.comparator.NameFileComparator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by robertrambo on 18/01/2016.
 */
public class ReceivedDroppedFiles extends SwingWorker<String, Object> {

    //private DefaultListModel<DataFileElement> fileListModel;
    private JList fileList;
    private JLabel status;
    private int dropLocaleIndex;
    private boolean convertToAng;
    private boolean sortFiles;
    private boolean doGuinier;
    private JProgressBar bar;
    private File[] files;
    private Collection targetCollection;
    private String workingDirectoryName;
    private AnalysisModel analysisModel;
    private ResultsModel resultsModel;
    private DefaultListModel<SampleBufferElement> sampleBufferFilesModel;

    private DefaultListModel<DataFileElement> dataFilesModel;  // common for all files that are loaded and displayed as a Jlist
    private JList dataFilesList;  // common for all files that are loaded and displayed as a list
    private int panelIndex;

    public ReceivedDroppedFiles(File[] files, Collection targetCollection, JLabel status, int index, boolean convertNMtoAng, boolean doGuinier, boolean sort, final JProgressBar bar, String workingDirectoryName){

        this.files = files;
        this.targetCollection = targetCollection;
        this.panelIndex = index;
        this.status = status;
        this.dropLocaleIndex = index;
        this.convertToAng = convertNMtoAng;
        this.doGuinier = doGuinier;
        this.bar = bar;
        this.sortFiles = sort;
        this.workingDirectoryName = workingDirectoryName;


        if (sortFiles){
            Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
        }
    }


    @Override
    protected String doInBackground() throws Exception {

        int totalFiles = files.length;

        bar.setMaximum(100);
        bar.setStringPainted(true);
        bar.setValue(0);

        for( int i = 0; i < totalFiles; i++ ) {
            // call File loader function
            // if true add loaded file object to collection

            if (files[i].isDirectory()){
                int sizeOfCollection = targetCollection.getDatasetCount();

                File[] tempFiles = finder(files[i].getAbsolutePath());

                // sort
                if (sortFiles){
                    Arrays.sort(tempFiles, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
                }

                for (int j=0; j < tempFiles.length; j++){
                    LoadedFile temp = loadDroppedFile(tempFiles[j], targetCollection.getDatasetCount());
                    addToCollection(temp);
                }

            } else {
                LoadedFile temp = loadDroppedFile(files[i], targetCollection.getDatasetCount());
                addToCollection(temp);
                System.out.println(i + " Loaded File " + targetCollection.getLast().getFileName());
            }

            status.setText(" Loaded File " + targetCollection.getLast().getFileName());
            bar.setValue((int) (i / (double) totalFiles * 100));
        }

        bar.setValue(0);
        bar.setStringPainted(false);

        updateModels(panelIndex);

        //update collection ids
        int total = targetCollection.getDatasetCount();
        for(int h=0; h<total; h++){
            targetCollection.getDataset(h).setId(h);
        }
        
        return null;
    }

    private void updateModels(int collectionIndex) {

        if (collectionIndex <= 4) {
            dataFilesModel.clear();
            dataFilesList.removeAll();
            analysisModel.clear();
            resultsModel.getDatalist().clear();
            resultsModel.clear();

            for (int i = 0; i < targetCollection.getDatasetCount(); i++) {
                String name = targetCollection.getDataset(i).getFileName();
                dataFilesModel.addElement(new DataFileElement(name, i));
                analysisModel.addDataset(targetCollection.getDataset(i));
                resultsModel.addDataset(targetCollection.getDataset(i));
            }

            dataFilesList.setModel(dataFilesModel); 
            
        } else if (collectionIndex == 69 || collectionIndex == 96){

            sampleBufferFilesModel.clear();
            //sampleBufferFilesList.removeAll();
            //repopulate

            Color tempColor;
            for(int i=0; i< targetCollection.getDatasetCount(); i++){

                String name = targetCollection.getDataset(i).getFileName();
                name = name + "_" + i;

                targetCollection.getDataset(i).setFileName(name);
                tempColor = targetCollection.getDataset(i).getColor();
                sampleBufferFilesModel.addElement(new SampleBufferElement(name, i, tempColor, targetCollection.getDataset(i)));
            }

            //sampleBufferFilesList.setModel(sampleBufferFilesModel);
            sampleBufferFilesModel.notifyAll();
        }

    }


    private void addToCollection(LoadedFile tempFile){
        // how to update results? Use a results object
        // if new dataset is added, we will have to add a JLabel thing and rerender
        // if updating, we could probably just change the value of the object which will automatically update value

        int newIndex = targetCollection.getDatasetCount();

        targetCollection.addDataset(new Dataset(
                tempFile.allData,       //data
                tempFile.allDataError,  //original
                tempFile.filebase,
                newIndex, doGuinier ));
    }

    public File[] finder(String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.endsWith(".dat"); }
        } );

    }

    private LoadedFile loadDroppedFile(File file, int size){
        LoadedFile temp = null;
        String ext;
        // how to handle different file formats?
        // get file base and extension
        String[] currentFile;
        currentFile = file.getName().split("\\.(?=[^\\.]+$)");

        ext = (currentFile.length > 2) ? currentFile[2] : currentFile[1];

        try {
            if (ext.equals("brml")) {
                status.setText("Bruker .brml  file detected ");
                File tempFile;
                tempFile = Bruker.makeTempDataFile(file, workingDirectoryName);
                temp = new LoadedFile(tempFile, status, size, convertToAng);

            } else if (ext.equals("dat") || ext.equals("fit") || ext.equals("Adat")) {
                temp = new LoadedFile(file, status, size, convertToAng);
            } else {
                // throw exception - incorrect file format
                throw new Exception("Incorrect file format: Use either brml, dat, fit, Adat, or Bdat file formats: " + currentFile);
            }
        } catch (Exception ex) {
            status.setText(ex.getMessage().toString());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        //add to collection
        return temp;
    }


    public void setModels(AnalysisModel analModel, ResultsModel resModel, DefaultListModel<DataFileElement> dataFilesModel, JList datafileslist){
        this.analysisModel = analModel;
        this.resultsModel = resModel;
        this.dataFilesModel = dataFilesModel;
        this.dataFilesList = datafileslist;
    }

    public void setSampleBufferModels(DefaultListModel<SampleBufferElement> bufferFilesModel){
        this.sampleBufferFilesModel = bufferFilesModel;
    }

}
