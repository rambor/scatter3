import version3.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by robertrambo on 17/12/2015.
 */
public class Scatter {
    private JTabbedPane mainPane;
    private JPanel panel1;
    private JPanel filesPanel;
    private JPanel analysisPanel;
    private JPanel statusPanel;
    private JProgressBar progressBar1;
    private JPanel buttonPanel;
    private JPanel chartsPanel;
    private JButton removeButton;
    private JTextField collectionNote;
    private JPanel collectionFileList;
    private JPanel dataFilesPanel;
    private JList dataFilesList;
    private JPanel loadPanel1;
    private JRadioButton radioButtonLoad1;
    private JButton clearButton1;
    private JPanel loadPanel2;
    private JPanel loadPanel3;
    private JPanel loadPanel4;
    private JButton clearButton2;
    private JButton clearButton4;
    private JButton clearButton3;
    private JCheckBox convertNmToAngstromCheckBox;
    private JRadioButton radioButtonLoad2;
    private JRadioButton radioButtonLoad3;
    private JRadioButton radioButtonLoad4;
    private JPanel miniIofQPanel;
    private JPanel normalizedGuinierPanel;
    private JPanel normalizedKratkyPanel;
    private JPanel mini1;
    private JPanel mini2;
    private JPanel mini3;
    private JPanel mini4;
    private JPanel analysisPane;
    private JLabel status;
    private JButton intensityPlotButton;
    private JButton normalizedKratkyButton;
    private JButton flexibilityPlotsButton;

    private String version = "3.0";
    private static String WORKING_DIRECTORY_NAME;
    private static String OUTPUT_DIR_SUBTRACTION_NAME;
    private static String ATSAS_DIRECTORY;

    private DefaultListModel<DataFileElement> dataFilesModel;
    private DefaultListModel<DataFileElement> fitFilesModel;
    private DefaultListModel<DataFileElement> complexFilesModel;

    //public static ArrayList<Collection> collections;
    private static HashMap collections;
    private static Collection bufferCollections;
    private static Collection sampleCollections;
    private static Collection collectionSelected;
    public static ArrayList<JRadioButton> collectionButtons;
    public static ArrayList<Graph> miniPlots;
    public static ArrayList<JPanel> minis;
    private static int cpuCores;
    public static int totalPanels;

    public Scatter() { // constructor

        collections = new HashMap();
        bufferCollections = new Collection();
        sampleCollections = new Collection();

        // Files Tab
        dataFilesModel = new DefaultListModel<DataFileElement>();
        dataFilesList.setCellRenderer(new DataFilesListRenderer());
        dataFilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        mainPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        minis = new ArrayList<JPanel>(){{add(mini1); add(mini2); add(mini3); add(mini4);}};
        miniPlots = new ArrayList<Graph>();

        collectionButtons = new ArrayList<JRadioButton>(){{add(radioButtonLoad1); add(radioButtonLoad2); add(radioButtonLoad3); add(radioButtonLoad4); }};
        totalPanels = collectionButtons.size();

        for (int i=0; i< totalPanels; i++){
            collections.put(new Integer(i), new Collection()); // HashMap for new Collection
            miniPlots.add(new Graph("Set " + Integer.toString(i+1)));
            collectionButtons.get(i).setSelected(false);
            miniPlots.get(i).plot((Collection) collections.get(i));
            //miniPlots.get(i).frame.setSize(100, 100);
        }

        // Mini Collections for Drag-N-Drop on Files Tab
        for (int i = 0; i < totalPanels; i++){
            minis.get(i).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            minis.get(i).add(miniPlots.get(i).frame.getChartPanel());
        }


    }


    public static void main(String[] args) {
        //check from property file
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e){
            e.printStackTrace();
        }

        File propertyFile = new File("scatter.config");
        WORKING_DIRECTORY_NAME = System.getProperty("user.dir");
        OUTPUT_DIR_SUBTRACTION_NAME = System.getProperty("user.dir");
        ATSAS_DIRECTORY = System.getProperty("user.dir");

        if (propertyFile.exists() && !propertyFile.isDirectory()){
            Properties prop = new Properties();
            InputStream input = null;

            try {
                input = new FileInputStream("scatter.config");
                // load a properties file
                prop.load(input);
                if (prop.getProperty("workingDirectory") != null) {
                    Scatter.WORKING_DIRECTORY_NAME = prop.getProperty("workingDirectory");
                }
                if (prop.getProperty("atsasDirectory") != null) {
                    Scatter.ATSAS_DIRECTORY = prop.getProperty("atsasDirectory");
                }
                if (prop.getProperty("subtractionDirectory") != null) {
                    Scatter.OUTPUT_DIR_SUBTRACTION_NAME = prop.getProperty("subtractionDirectory");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //
        JFrame frame = new JFrame("ScÅtter: Software for SAXS Analysis");
        final Scatter programInstance = new Scatter();
        frame.setContentPane(programInstance.panel1);

        // Drag-n-Drop listeners attached to SWING components here
        // Create FileDrop listeners
        // Load Files from Files Tab Panel 1

        new FileDrop( programInstance.getLoadPanel(1), new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                // add the new file to the collection
                receivedDroppedFiles(files, programInstance.getStatus(), programInstance, 0, programInstance.convertNmToAngstromCheckBox.isSelected());
            }
        });

        // Load Files from Files Tab Panel 2
        new FileDrop( programInstance.getLoadPanel(2), new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                receivedDroppedFiles(files, programInstance.getStatus(), programInstance, 1, programInstance.convertNmToAngstromCheckBox.isSelected());
            }
        });
        // Load Files from Files Tab Panel 3
        new FileDrop( programInstance.getLoadPanel(3), new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                receivedDroppedFiles(files, programInstance.getStatus(), programInstance, 2, programInstance.convertNmToAngstromCheckBox.isSelected());
            }
        });
        // Load Files from Files Tab Panel 4
        new FileDrop( programInstance.getLoadPanel(4), new FileDrop.Listener() {
            @Override
            public void filesDropped(File[] files) {
                receivedDroppedFiles(files, programInstance.getStatus(), programInstance, 3, programInstance.convertNmToAngstromCheckBox.isSelected());
            }
        });




        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

    private JLabel getStatus() {
        return status;
    }

    /**
     * returns the panel from Files tab for drag-n-drop
     * @param i
     * @return
     */
    private JPanel getLoadPanel(int i){
        JPanel jpanel = new JPanel();
        if (i == 1) {
            jpanel = loadPanel1;
        } else if (i == 2){
            jpanel = loadPanel2;
        } else if (i == 3) {
            jpanel = loadPanel3;
        } else if (i == 4) {
            jpanel = loadPanel4;
        }
        return jpanel;
    }


    /**
     * Creates LoadedFile from dropped file
      * @param file
     * @param status
     * @param size
     * @param toPlot
     * @param convertNMtoAng
     * @return
     */
    private static LoadedFile loadDroppedFile(File file, JLabel status, int size, int toPlot, boolean convertNMtoAng){
        LoadedFile temp = null;
        String filebase, ext;
        // how to handle different file formats?
        // get file base and extension
        String[] currentFile;
        currentFile = file.getName().split("\\.(?=[^\\.]+$)");
        filebase = currentFile[0];
        ext = (currentFile.length > 2) ? currentFile[2] : currentFile[1];

        try {
            if (ext.equals("brml")) {
                status.setText("Bruker .brml  file detected ");
                File tempFile;
                tempFile = Bruker.makeTempDataFile(file, Scatter.WORKING_DIRECTORY_NAME);
                temp = new LoadedFile(tempFile, status, size, toPlot, convertNMtoAng);

            } else if (ext.equals("dat") || ext.equals("fit") || ext.equals("Adat")) {
                temp = new LoadedFile(file, status, size, toPlot, convertNMtoAng);
            } else {
                // throw exception - incorrect file format
                throw new Exception("Incorrect file format: Use either brml, dat, fit, Adat, or Bdat file formats: " + currentFile);
            }
        } catch (Exception ex) {
            status.setText(ex.getMessage().toString());
            Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
        }
        //add to collection
        return temp;
    }

    /**
     *
     * @param collectionNumber
     * @param tempFile
     */
    private static void addToCollection(int collectionNumber, LoadedFile tempFile){
        // how to update results? Use a results object
        // if new dataset is added, we will have to add a JLabel thing and rerender
        // if updating, we could probably just change the value of the object which will automatically update value
        if (collectionNumber < 69){

            int newIndex = ((Collection)collections.get(collectionNumber)).getDatasets().size();

            ((Collection)collections.get(collectionNumber)).addDataset(new Dataset(
                    tempFile.allData,       //data
                    tempFile.allDataError,  //original
                    tempFile.filebase,
                    newIndex ));

            // update Analysis Tab
            //analysisModel.addDataset(((Collection)collections.get(collectionNumber)).getLast());
            //resultsModel.addDataset(((Collection)collections.get(collectionNumber)).getLast());

        } else if (collectionNumber == 69 || collectionNumber == 96) {
            // buffers
            int newIndex = ((Collection)collections.get(collectionNumber)).getDatasets().size();

            ((Collection)collections.get(collectionNumber)).addDataset(new Dataset(
                    tempFile.allData,       //data
                    tempFile.allDataError,  //original
                    tempFile.filebase,
                    newIndex ));
        }
    }


    /**
     * This is a static method, it is not an instance of the Scatter class
     * @param files
     * @param status
     * @param main
     * @param index 1 through 4 refers to panels on Files tab
     * @param convertNMtoAng
     */
    private static void receivedDroppedFiles(File[] files, JLabel status, Scatter main, int index, boolean convertNMtoAng){

        if (index <= 4){

            main.dataFilesModel.clear();
            main.dataFilesList.removeAll();

//            prModel.clear();

//            analysisModel.datalist.clear();
//            analysisModel.clear();

//            resultsModel.datalist.clear();
//            resultsModel.clear();

            int total = files.length;
            for( int i = 0; i < total; i++ ) {
                // call File loader function
                // if true add loaded file object to collection
                System.out.println("PANEL => " + index + " -------- DROPPED FILE: " + files[i]);

                LoadedFile temp = loadDroppedFile(files[i], status, ((Collection)Scatter.collections.get(index)).getDatasets().size(), 1, convertNMtoAng);

                // load file into collection
                if (temp !=null){
                    addToCollection(index, temp);
                }
            }

            // update dataFilesList in dataFilesPanel (Files tab);
            // rebuild dataFilesPanel from collection.get(i)
            for(int i=0; i<((Collection)main.collections.get(index)).getDatasets().size(); i++){
                String name = ((Collection)main.collections.get(index)).getDataset(i).getFileName();
                main.dataFilesModel.addElement(new DataFileElement(name, i));
               // analysisModel.addDataset(((Collection) collections.get(index)).getDataset(i));
               // resultsModel.addDataset(((Collection) collections.get(index)).getDataset(i));
            }

            main.dataFilesList.setModel(main.dataFilesModel);

            //update collection ids
            total = ((Collection)collections.get(index)).getDatasets().size();
            for(int h=0; h<total; h++){
                ((Collection) collections.get(index)).getDataset(h).setId(h);
            }

            for(int i=0; i < totalPanels; i++){
                collectionButtons.get(i).setSelected(false);
            }

            collectionButtons.get(index).setSelected(true);
            collectionSelected = (Collection)main.collections.get(index);

            // replot miniCollection
            miniPlots.get(index).frame.removeAll();
            miniPlots.get(index).chart.setNotify(false);
            miniPlots.get(index).plot((Collection)main.collections.get(index));
            minis.get(index).add(miniPlots.get(index).frame.getChartPanel());
            miniPlots.get(index).chart.setNotify(true);

        }  else if (index < 97) {
/*
            if (index == 69){
                //main.bufferFileElementArrayList.clear();
                main.bufferFilesModel.clear();
                main.buffersList.removeAll();
            } else if (index == 96) {
                //main.sampleFileElementArrayList.clear();
                main.sampleFilesModel.clear();
                main.samplesList.removeAll();
                main.setReferenceBox.removeAllItems();
            }

            // incase directories are dropped
            Arrays.sort(files, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

            for( int i = 0; i < files.length; i++ ) {
                // call File loader function
                // if true add loaded file object to collection


                if (files[i].isDirectory()){
                    int sizeOfCollection = ((Collection)main.collections.get(index)).getDatasets().size();

                    File[] tempFiles = finder(files[i].getAbsolutePath());

                    // sort
                    Arrays.sort(tempFiles, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);

                    for (int j=0; j < tempFiles.length; j++){
                        LoadedFile temp = loadDroppedFile(tempFiles[j], status, ((Collection)main.collections.get(index)).getDatasets().size(), 1, convertNMtoAng);
                        addToCollection(index,temp);
                    }
                } else {
                    LoadedFile temp = loadDroppedFile(files[i], status, ((Collection) main.collections.get(index)).getDatasets().size(), 1, convertNMtoAng);
                    addToCollection(index,temp);
                }
            }
            // update dataFilesList in dataFilesPanel;
            // rebuild dataFilesPanel from collection.get(i)

            Color tempColor;
            for(int i=0; i< ((Collection)main.collections.get(index)).getDatasets().size(); i++){

                String name = ((Collection)main.collections.get(index)).getDataset(i).getFileName();
                name = name.replaceAll("sample_[0-9]+", "sample_"+ i);
                ((Collection)main.collections.get(index)).getDataset(i).setFileName(name);

                tempColor = ((Collection)main.collections.get(index)).getDataset(i).getColor();

                if (index == 69) {
                    main.bufferFilesModel.addElement(new SampleBufferElement(name, i, tempColor, ((Collection)main.collections.get(index)).getDataset(i)));
                } else if (index == 96) {
                    main.sampleFilesModel.addElement(new SampleBufferElement(name, i, tempColor, ((Collection)main.collections.get(index)).getDataset(i)));
                    main.setReferenceBox.addItem(new ReferenceItem(name, i));
                }
            }

            if (index == 69) {
                main.buffersList.setModel(main.bufferFilesModel);

            } else if (index == 96) {
                main.samplesList.setModel(main.sampleFilesModel);
                // auto set reference to last entry
                main.setReferenceBox.setSelectedIndex(main.setReferenceBox.getItemCount()-1);
            }
*/
        } else if (index == 138) {
/*
            main.fitFilesModel.clear();
            main.fitFilesList.removeAll();
            String absolutePath, filebase, ext;
            String[] currentFile;
            int count=0;

            for( int i = 0; i < files.length; i++ ) {
                // call File loader function
                // if true add loaded file object to collection
                absolutePath = files[i].getAbsolutePath();
                currentFile = files[i].getName().split("\\.(?=[^\\.]+$)");
                filebase = currentFile[0];
                ext = (currentFile.length > 2) ? currentFile[2] : currentFile[1];

                if (ext.matches("fit")){
                    main.fitFilesModel.addElement(new DataFileElement(filebase, i));
                    main.fitFilesModel.getElementAt(count).setFullpath(absolutePath);
                    count++;
                }
            }

            main.fitFilesList.setModel(main.fitFilesModel);
            */
        }

    }

}

class DataFilesListRenderer extends JCheckBox implements ListCellRenderer {
    Color setColor;
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        setEnabled(list.isEnabled());
        setSelected(((DataFileElement)value).isSelected());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setText(value.toString());
        return this;
    }
}