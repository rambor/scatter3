package version3;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by robertrambo on 18/01/2016.
 */
public class ReceivedDroppedFiles extends SwingWorker<Void, Integer> {

    //private DefaultListModel<DataFileElement> fileListModel;
    private JLabel status;
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
    private double qmax;
    private boolean exclude, shortened = false;
    private JList list;


    public ReceivedDroppedFiles(File[] files, Collection targetCollection, JLabel status, int index, boolean convertNMtoAng, boolean doGuinier, boolean sort, final JProgressBar bar, String workingDirectoryName){

        this.files = files;
        this.targetCollection = targetCollection;
        this.panelIndex = index;
        this.status = status;
        this.convertToAng = convertNMtoAng;
        this.doGuinier = doGuinier;
        this.bar = bar;
        this.sortFiles = sort;
        this.workingDirectoryName = workingDirectoryName;

    }


    @Override
    protected Void doInBackground() throws Exception {

        int totalFiles = files.length;

        bar.setMaximum(totalFiles);
        bar.setStringPainted(true);
        bar.setValue(0);

       //System.out.println("TOTAL FILES " + totalFiles);
        Comparator<File> fileComparator = new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    //int s = name.indexOf('_')+1;
                    int s = name.lastIndexOf('_')+1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0; // if filename does not match the format
                    // then default to 0
                }
                return i;
            }
        };


        if (sortFiles && totalFiles > 1){ // sort the name of directories first and then proceed to loading files
            Arrays.sort(files, fileComparator);
        }

        for( int i = 0; i < totalFiles; i++ ) {
            // call File loader function
            // if true add loaded file object to collection
            if (files[i].isDirectory()){
                //int sizeOfCollection = targetCollection.getDatasetCount();
                File[] tempFiles = finder(files[i].getAbsolutePath());

                // sort
                if (sortFiles){
                    Arrays.sort(tempFiles, fileComparator);
                }

                for (int j=0; j < tempFiles.length; j++){
                    //temp = loadDroppedFile(tempFiles[j], targetCollection.getDatasetCount());
                    addToCollection(loadDroppedFile(tempFiles[j], targetCollection.getDatasetCount()));
                }

            } else {

                String[] filename = files[i].getName().split("\\.(?=[^\\.]+$)");
                String ext = filename[1];
                String filebase = filename[0];
                if (ext.equals("pdb")){
                    // make Dataset from PDB file and add to collection
                    status.setText("Reading PDB file " + filebase + " => calculating P(r) - please wait ~ 1 min");
                    System.out.println("Detected PDB file");
                    bar.setStringPainted(false);
                    bar.setIndeterminate(true);
                    PDBFile tempPDB = new PDBFile(files[i], qmax, exclude, workingDirectoryName);

                    int newIndex = targetCollection.getDatasetCount();

                    targetCollection.addDataset(new Dataset(
                            tempPDB.getIcalc(),  //data
                            tempPDB.getError(),  //original
                            filebase,
                            newIndex, true ));

                    targetCollection.getDataset(newIndex).setIsPDB(tempPDB.getPrDistribution(), (int)tempPDB.getDmax(), tempPDB.getRg(), tempPDB.getIzero());
                    bar.setIndeterminate(false);
                } else {
                    //LoadedFile temp = loadDroppedFile(files[i], targetCollection.getDatasetCount());
                    addToCollection(loadDroppedFile(files[i], targetCollection.getDatasetCount()));
                }
            }
            publish(i);
        }

        updateModels(panelIndex);

        //update collection ids
        int total = targetCollection.getDatasetCount();
        for(int h=0; h<total; h++){
            targetCollection.getDataset(h).setId(h);
        }

        System.gc();
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int i = chunks.get(chunks.size()-1);
        bar.setValue(i);
        super.process(chunks);
    }


    @Override
    protected void done() {
        try {
            //get();
            bar.setValue(0);
            bar.setStringPainted(false);
            status.setText("FINISHED");

            if (panelIndex == 96 ){
//                list.revalidate();
//                list.repaint();
                list.validate();
            }
            get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void setJList(JList list){
        this.list = list;
    }


    private void updateModels(int collectionIndex) {

        if (collectionIndex <= 4) {
            dataFilesModel.removeAllElements();
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
            //repopulate
            Color tempColor;
            for(int i=0; i< targetCollection.getDatasetCount(); i++){

                String name = targetCollection.getDataset(i).getFileName() + "_" + i;
                tempColor = targetCollection.getDataset(i).getColor();
                sampleBufferFilesModel.addElement(new SampleBufferElement(name, i, tempColor, targetCollection.getDataset(i)));
            }

            //sampleBufferFilesList.setModel(sampleBufferFilesModel);
//            sampleBufferFilesModel.notifyAll();
        }
    }

    public void useShortenedConstructor(){
        this.shortened = true;
    }

    private void addToCollection(LoadedFile tempFile){
        // how to update results? Use a results object
        // if new dataset is added, we will have to add a JLabel thing and rerender
        // if updating, we could probably just change the value of the object which will automatically update value

        int newIndex = targetCollection.getDatasetCount();

        if (shortened){
            targetCollection.addDataset(new Dataset(
                    tempFile.allData,       //data
                    tempFile.allDataError,  //original
                    tempFile.filebase,
                    newIndex));
        } else {
            targetCollection.createDataset(tempFile, newIndex, doGuinier);

//            targetCollection.addDataset(new Dataset(
//                    tempFile.allData,       //data
//                    tempFile.allDataError,  //original
//                    tempFile.filebase,
//                    newIndex, doGuinier ));
        }
    }



    public File[] finder(String dirName){
        File dir = new File(dirName);

        //System.out.println("DIRECTORY NAME IN FINDER : " + dirName + " " + dir.listFiles().length);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.endsWith(".dat"); }
        } );
    }

    private LoadedFile loadDroppedFile(File file, int size){

        // how to handle different file formats?
        // get file base and extension
        String[] currentFile;
        currentFile = file.getName().split("\\.(?=[^\\.]+$)");

        String ext = (currentFile.length > 2) ? currentFile[2] : currentFile[1];

        try {
            if (ext.equals("brml")) {
                status.setText("Bruker .brml  file detected ");
                File tempFile;
                tempFile = Bruker.makeTempDataFile(file, workingDirectoryName);
                return new LoadedFile(tempFile, status, size, convertToAng);

            } else if (ext.equals("int") || ext.equals("dat") || ext.equals("fit") || ext.equals("Adat") || ext.equals("csv")) {
                return new LoadedFile(file, status, size, convertToAng);
            } else {
                // throw exception - incorrect file format
                throw new Exception("Incorrect file format: Use either brml, dat, csv, Adat, or Bdat file formats: " + currentFile);
            }

        } catch (Exception ex) {
            status.setText(ex.getMessage().toString());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        //add to collection
        return null;
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

    public void setPDBParams(boolean exclude, double qmax){
        this.exclude = exclude;
        this.qmax = qmax;
    }
}
