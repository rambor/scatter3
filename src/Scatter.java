import org.jfree.chart.plot.Plot;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import version3.*;
import version3.Collection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private JPanel miniGuinierPanel;
    private JPanel miniVcPanel;
    private JPanel miniNormalizedKratkyPanel;
    private JPanel mini1;
    private JPanel mini2;
    private JPanel mini3;
    private JPanel mini4;
    private JPanel analysisPane;
    private JLabel status;
    private JButton intensityPlotButton;
    private JButton normalizedKratkyButton;
    private JButton guinierPeakAnalysisButton;
    private JTabbedPane tabbedPane1;
    private JButton kratkyPlotButton;
    private JButton qIqPlotButton;
    private JTabbedPane tabbedPane2;
    private JButton errorPlotButton;
    private JButton powerLawPlotButton;
    private JButton volumeButton;
    private JButton vcPlotButton;
    private JButton flexibilityPlotsButton;
    private JButton ratioPlotButton;
    private JButton complexButton;
    private JButton rcXSectionalButton;
    private JButton scaleButton;
    private JButton averageButton;

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

    public static JTable analysisTable;
    public static AnalysisModel analysisModel;

    // singleton plots
    public PlotDataSingleton log10IntensityPlot;
    public KratkyPlot kratky;
    public QIQPlot qIqPlot;
    public ErrorPlot errorPlot;
    public PowerLawPlot powerLawPlot;

    public NormalizedKratkyPlot normalKratkyRg;
    public NormalizedKratkyPlot normalKratkyRgReal;
    public NormalizedKratkyPlot normalKratkyVc;
    public NormalizedKratkyPlot normalKratkyVcReal;

    private boolean isCtrlC = false;
    private boolean isCtrlB = false;

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
        }

        // Mini Collections for Drag-N-Drop on Files Tab
        for (int i = 0; i < totalPanels; i++){
            minis.get(i).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            minis.get(i).add(miniPlots.get(i).frame.getChartPanel());
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
        leftRenderer.setHorizontalAlignment( JLabel.LEFT );

        //Analysis Table
        analysisTable = new JTable(new AnalysisModel(status));
        TableColumnModel tcm = analysisTable.getColumnModel();

        TableColumn tc = tcm.getColumn(4);
        tc.setCellEditor(new SpinnerEditor());

        tc = tcm.getColumn(5);
        tc.setCellEditor(new SpinnerEditor());

        tc = tcm.getColumn(13);
        tc.setCellEditor(new ButtonEditorRenderer());
        tc.setCellRenderer(new ButtonEditorRenderer());

        analysisTable.setRowHeight(30);
        analysisTable.setBackground(Color.WHITE);
        analysisTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        analysisTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        analysisTable.getColumnModel().getColumn(2).setPreferredWidth(35);
        analysisTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        analysisTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        analysisTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        //analysisTable.getColumnModel().getColumn(6).setPreferredWidth(35);

        tc = analysisTable.getColumnModel().getColumn(2);
        tc.setCellEditor(new CheckBoxCellEditorRenderer());
        tc.setCellRenderer(new CheckBoxCellEditorRenderer());

//        tc = analysisTable.getColumnModel().getColumn(6);
//        tc.setCellEditor(new CheckBoxCellEditorRenderer());
//        tc.setCellRenderer(new CheckBoxCellEditorRenderer());

        final Thread[] lowerBoundThread = new Thread[1];
/*
        class SetLowerBound implements Runnable {
            double limit;
            int upperlower;
            public SetLowerBound(double limit, int uorl){
                this.limit = limit;
                this.upperlower=uorl;
            }
            @Override
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
                mainStatus.setValue(0);
                mainStatus.setStringPainted(true);
                task = new Task("bound");
                task.setQValue(this.limit);
                task.setUpperLower(this.upperlower);
                task.execute();
                task.done();
            }
        }
*/
        tc = analysisTable.getColumnModel().getColumn(0);
        tc.setCellEditor(new ColorEditor());
        tc.setCellRenderer(new ColorRenderer(true));

        analysisTable.getColumnModel().getColumn(1).setCellRenderer( rightRenderer );
        analysisTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(7).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(8).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(9).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(10).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(11).setCellRenderer( centerRenderer );
        analysisTable.getColumnModel().getColumn(12).setCellRenderer(centerRenderer);

        analysisTable.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
                    isCtrlC = true;
                }

                if ((e.getKeyCode() == KeyEvent.VK_B) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
                    isCtrlB = true;
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                isCtrlC = false;
                isCtrlB = false;
            }
        });

        analysisTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (isCtrlC){
                    int row = analysisTable.rowAtPoint(e.getPoint());
                    Notes temp = new Notes(collectionSelected.getDataset(row));
                    temp.pack();
                    temp.setVisible(true);
                    isCtrlC = false;
                }

                if (isCtrlB){
                    int row = analysisTable.rowAtPoint(e.getPoint());
                    BufferInfo tempInfo = new BufferInfo(collectionSelected.getDataset(row));
                    tempInfo.pack();
                    tempInfo.setVisible(true);
                    isCtrlB = false;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });


        analysisTable.getTableHeader().addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent mouseEvent){
                int index = analysisTable.columnAtPoint(mouseEvent.getPoint());
                if (index == 2 ){
                    int total = collectionSelected.getDatasets().size();
                    for(int i=0; i<total; i++){
                        collectionSelected.getDataset(i).setInUse(!collectionSelected.getDataset(i).getInUse());
                    }
                    analysisModel.fireTableDataChanged();
                } else if (index == 4) {
                    // add function to replot data given qmin or qmax when user selects columns 4 or 5
                    // popup dialog box asking for qmin or qmax
                    // JOptionPane.showInputDialog(Scatter)
                    String inputValue = JOptionPane.showInputDialog("Please input a minimum q value");
                    if (inputValue != null){
                        if (isQValue(inputValue)){
                            // launch new task
                          //  lowerBoundThread[0] = new Thread(new SetLowerBound(Double.parseDouble(inputValue),4));
                          //  lowerBoundThread[0].start();
                        }
                    }

                } else if (index == 5) {
                    String inputValue = JOptionPane.showInputDialog("Please input a maximum q value");
                    if (inputValue != null){
                        if (isQValue(inputValue)){
                          //  lowerBoundThread[0] = new Thread(new SetLowerBound(Double.parseDouble(inputValue),5));
                          //  lowerBoundThread[0].start();
                        }
                    }
                }

                // check if key is depresed

            }
        });

        analysisModel = (AnalysisModel) analysisTable.getModel();

        analysisModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {

                if (e.getColumn() == 12){
                    // go through each dataset in series and see if scale factor was changed?
                    if (e.getFirstRow() == e.getLastRow()){
                        mainRescaling(e.getFirstRow());
                    }
                }
            }
        });

        JTableHeader header = analysisTable.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer(analysisTable));

        JScrollPane analysisList = new JScrollPane(analysisTable);
        analysisPane.add(analysisList);
        analysisTable.setFillsViewportHeight(false);
        analysisList.setOpaque(true);
        analysisPane.setOpaque(true);

        // define Singleton plots
        kratky = KratkyPlot.getInstance();
        log10IntensityPlot = PlotDataSingleton.getInstance();
        qIqPlot = QIQPlot.getInstance();
        errorPlot = ErrorPlot.getInstance();
        powerLawPlot = PowerLawPlot.getInstance();

        normalKratkyRg = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (GUINIER)");
        normalKratkyRgReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (Real space)");
        normalKratkyVc = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Guinier)");
        normalKratkyVcReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Real space)");

        // create plot, set attribute to true
        // on close plot, set attribute to false
        // only works on singleton classes since only one window is open

        intensityPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphData();
            }
        });


        kratkyPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kratky.clearAll();
                createKratkyPlot();
            }
        });

        qIqPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                qIqPlot.clearAll();
                createQIQPlot();
            }
        });

        normalizedKratkyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNormalizedKratkyPlot();
            }
        });

        errorPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createErrorPlot();
            }
        });

        powerLawPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createPowerLawPlot();
            }
        });

        volumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int id = collectionSelected.getSelected();
                if (id < 0){
                    status.setText("Please select only one dataset");
                    return;
                }

                createVolumePlot(collectionSelected.getDataset(id).getId());
            }
        });


        vcPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createVcPlot();
            }
        });

        flexibilityPlotsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createFlexPlots();
            }
        });

        ratioPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (collectionSelected.getTotalSelected() != 2){
                    status.setText("Please select only two datasets");
                    return;
                }
                createRatioPlot();
            }
        });

        guinierPeakAnalysisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = collectionSelected.getSelected();

                if (id < 0){
                    status.setText("Please select only one dataset");
                    return;
                }

                if (collectionSelected.getDataset(id).getGuinierRg() <= 0 && collectionSelected.getDataset(id).getGuinierIzero() <= 0){
                    status.setText("Perform Manual Guinier first, auto-Rg failed");
                    return;
                }

                createGPAPlot(id);
            }
        });

        complexButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // make a class instance of ComplexPlot
                // create and show comboBox
                ComplexPlot complexation = new ComplexPlot(collectionSelected, WORKING_DIRECTORY_NAME, status);
            }
        });

        rcXSectionalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // if more than one file selected send warning and return
                int filesSelected=0, selected = 0;
                int limit = collectionSelected.getDatasetCount();

                for (int i=0; i<limit; i++){
                    if (collectionSelected.getDataset(i).getInUse()){
                        filesSelected++;
                        selected = i;
                    }
                }

                if (filesSelected != 1){
                    status.setText("Select only 1(one) file for Rc (x-section) determination");
                    return;
                } else {

                    RcXSectionalPlot tempPlot = new RcXSectionalPlot(collectionSelected.getDataset(selected), WORKING_DIRECTORY_NAME);
                    tempPlot.createPlots();
                }
            }
        });

        scaleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(){
                    public void run() {
                        // int numberOfCPUs, Collection collection, double lower, double upper, JProgressBar bar, JLabel label){
                        //Integer.valueOf(cpuBox.getSelectedItem().toString())
                        scaleButton.setEnabled(false);
                        if (log10IntensityPlot.isVisible()){
                            log10IntensityPlot.setNotify(false);
                        }
                        ScaleManager scaling = new ScaleManager(
                                2,
                                collectionSelected,
                                progressBar1,
                                status);

                        scaling.scaleNow(0,0);
                        if (log10IntensityPlot.isVisible()){
                            log10IntensityPlot.setNotify(true);
                        }
                        scaleButton.setEnabled(true);
                    }
                }.start();


            }
        });

        averageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (collectionSelected.getTotalSelected() < 2){
                    status.setText("Select at least two datasets for averaging");
                    return;
                } else {
                    averageButton.setEnabled(false);
                    Averager tempAverage = new Averager(collectionSelected, WORKING_DIRECTORY_NAME);

                    JFileChooser fc = new JFileChooser(WORKING_DIRECTORY_NAME);
                    int option = fc.showSaveDialog(panel1);
                    //set directory to default directory from Settings tab
                    Dataset tempDataset = new Dataset(tempAverage.getAveraged(), tempAverage.getAveragedError(), "averaged", collectionSelected.getDatasetCount());

                    int mergedIndex = log10IntensityPlot.addToMerged(tempAverage.getAveraged());

                    if(option == JFileChooser.CANCEL_OPTION) {
                        log10IntensityPlot.removeFromMerged(mergedIndex);
                        return;
                    }

                    if(option == JFileChooser.APPROVE_OPTION){
                        // remove dataset and write to file
                        log10IntensityPlot.removeFromMerged(mergedIndex);
                        // make merged data show on top of other datasets
                        File theFileToSave = fc.getSelectedFile();

                        String cleaned = cleanUpFileName(fc.getSelectedFile().getName());

                        if(fc.getSelectedFile()!=null){

                            WORKING_DIRECTORY_NAME = fc.getCurrentDirectory().toString();

                            FileObject dataToWrite = new FileObject(fc.getCurrentDirectory());
                            dataToWrite.writeSAXSFile(cleaned, tempDataset);

                            //close the output stream
                            status.setText(cleaned + ".dat written to "+fc.getCurrentDirectory());


                            collectionSelected.addDataset(tempDataset);
                            collectionSelected.getLast().setColor(Color.red);
                            collectionSelected.getLast().setFileName(cleaned);
                            log10IntensityPlot.addToBase(collectionSelected.getLast());

                            analysisModel.addDataset(collectionSelected.getLast());
                            //resultsModel.addDataset(collectionSelected.getLast());


                            int location = dataFilesModel.getSize();
                            dataFilesModel.addElement(new DataFileElement(collectionSelected.getLast().getFileName(), location));
                            analysisModel.fireTableDataChanged();
                            //resultsModel.fireTableDataChanged();

                            //Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    averageButton.setEnabled(true);
                }

            }
        });
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
        //System.exit(0);
    }


    private String cleanUpFileName(String fileName){
        String name;
        // remove the dot
        Pattern dot = Pattern.compile(".");
        Matcher expression = dot.matcher(fileName);

        if (expression.find()){
            String[] elements;
            elements = fileName.split("\\.");
            name = elements[0];
        } else {
            name = fileName;
        }

        return name;
    }

    private void graphData() {
        // rebuild miniCollection using only visible data
        //collectionSelected.getMiniCollection().removeAllSeries();
        log10IntensityPlot.plot(collectionSelected, WORKING_DIRECTORY_NAME);
    }

    /**
     * Creates Kratky plot from Singleton Class
     */
    private void createKratkyPlot(){
        kratky = KratkyPlot.getInstance();
        kratky.plot(collectionSelected, WORKING_DIRECTORY_NAME);
    }

    private void createGPAPlot(int id){

        GPAPlot gpaPlot = new GPAPlot("SC\u212BTTER \u2263 GUINIER PEAK ANALYSIS ", collectionSelected.getDataset(id), WORKING_DIRECTORY_NAME);
        gpaPlot.makePlot(analysisModel);

    }

    private void createRatioPlot(){

        RatioPlot ratioPlot = new RatioPlot(collectionSelected, WORKING_DIRECTORY_NAME);
        ratioPlot.plot();
    }

    private void createFlexPlots(){
        FlexPlots flexplot = new FlexPlots(collectionSelected, WORKING_DIRECTORY_NAME);
        flexplot.plot();
    }

    private void createVcPlot(){
        VcPlot tempPlot = new VcPlot(collectionSelected, WORKING_DIRECTORY_NAME);
        tempPlot.plot(status);
    }


    private void createVolumePlot(int id){
        VolumePlot tempPlot = new VolumePlot(collectionSelected.getDataset(id), WORKING_DIRECTORY_NAME);
        tempPlot.plot();
    }

    /**
     * Creates Kratky plot from Singleton Class
     */
    private void createQIQPlot(){
        qIqPlot.plot(collectionSelected, WORKING_DIRECTORY_NAME);
    }

    private void createNormalizedKratkyPlot(){

        //normalKratkyRg = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (GUINIER)");
        normalKratkyRg.plot(collectionSelected, "RECIRG", WORKING_DIRECTORY_NAME);

        for (int i=0; i<collectionSelected.getDatasetCount(); i++){
            Dataset temp = collectionSelected.getDataset(i);
            if (temp.getRealIzero() > 0 && temp.getRealRg() > 0){
                //normalKratkyRgReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (Real space)");
                normalKratkyRgReal.plot(collectionSelected, "REALRG", WORKING_DIRECTORY_NAME);
                break;
            }
        }

        //normalKratkyVc = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Guinier)");
        //normalKratkyVcReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Real space)");

    }

    private void createErrorPlot(){
        errorPlot.plot(collectionSelected, WORKING_DIRECTORY_NAME);
    }

    private void createPowerLawPlot(){
        powerLawPlot.plot(collectionSelected, WORKING_DIRECTORY_NAME);
    }


    private JLabel getStatus() {
        return status;
    }

    /**
     * returns the panel from Files tab for drag-n-drop
     * @param i is the panel on files tab
     * @return JPanel of the selected panel
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
     * @param convertNMtoAng
     * @return
     */
    private static LoadedFile loadDroppedFile(File file, JLabel status, int size, boolean convertNMtoAng){
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
                temp = new LoadedFile(tempFile, status, size, convertNMtoAng);

            } else if (ext.equals("dat") || ext.equals("fit") || ext.equals("Adat")) {
                temp = new LoadedFile(file, status, size, convertNMtoAng);
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

            int newIndex = ((Collection)collections.get(collectionNumber)).getDatasetCount();

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
            int newIndex = ((Collection)collections.get(collectionNumber)).getDatasetCount();

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

            analysisModel.clear();

//            resultsModel.datalist.clear();
//            resultsModel.clear();

            // multithreading will add in quasi random order

            int total = files.length;
            for( int i = 0; i < total; i++ ) {
                // call File loader function
                // if true add loaded file object to collection
                System.out.println("PANEL => " + index + " -------- DROPPED FILE: " + files[i]);

                LoadedFile temp = loadDroppedFile(files[i], status, ((Collection)Scatter.collections.get(index)).getDatasetCount(), convertNMtoAng);

                // load file into collection
                if (temp !=null){
                    addToCollection(index, temp);
                }
            }

            // update dataFilesList in dataFilesPanel (Files tab);
            // rebuild dataFilesPanel from collection.get(i)
            for(int i=0; i<((Collection)main.collections.get(index)).getDatasetCount(); i++){
                String name = ((Collection)main.collections.get(index)).getDataset(i).getFileName();
                main.dataFilesModel.addElement(new DataFileElement(name, i));
                analysisModel.addDataset(((Collection) collections.get(index)).getDataset(i));
               // resultsModel.addDataset(((Collection) collections.get(index)).getDataset(i));
            }

            main.dataFilesList.setModel(main.dataFilesModel);

            //update collection ids
            total = ((Collection)collections.get(index)).getDatasetCount();
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

    private static class HeaderRenderer implements TableCellRenderer {

        DefaultTableCellRenderer renderer;

        public HeaderRenderer(JTable table) {
            renderer = (DefaultTableCellRenderer)
                    table.getTableHeader().getDefaultRenderer();
            renderer.setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            return renderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
        }
    }



    public boolean isNumber( String input ) {
        try {
            Double.parseDouble(input);
            return true;
        }
        catch( Exception e) {
            return false;
        }
    }

    public static boolean isQValue(String str)
    {
        try {
            double d = Double.parseDouble(str);
            if ((d<0.0001) || (d>3)){
                throw new NumberFormatException();
            }
        } catch(NumberFormatException nfe) {
            System.out.println("Number is not a proper q-value: 0.0001 < q < 3");
            return false;
        }
        return true;
    }

    private void mainRescaling(int fIndex){
        //make sure user entered a number in Scale Factor Field

        double scaleFactor = collectionSelected.getDataset(fIndex).getScaleFactor();
        //update scale Factor in the Dataset;

        if (kratky.isVisible()) {
            kratky.setNotify(false);
            collectionSelected.getDataset(fIndex).scalePlottedKratkyData();
            kratky.setNotify(true);
        }

        if (qIqPlot.isVisible()) {
            qIqPlot.setNotify(false);
            collectionSelected.getDataset(fIndex).scalePlottedQIQData();
            qIqPlot.setNotify(true);
        }
        if (powerLawPlot.isVisible()) {
            powerLawPlot.setNotify(false);
            collectionSelected.getDataset(fIndex).scalePlottedPowerLaw();
            powerLawPlot.setNotify(true);
        }

        if (log10IntensityPlot.isVisible()){
            if (scaleFactor == 1.0){ //revert to original dataset
                status.setText("Dataset "+(fIndex+1) + " at original intensities");
            }
            //collectionSelected.getDataset(fIndex).scalePlottedLog10IntensityData();
        }

        if (errorPlot.isVisible()){
            collectionSelected.getDataset(fIndex).scalePlottedLogErrorData();
        }

        //collectionSelected.getDataset(fIndex).getData().setNotify(true);
    }

    /*
        * ColorRenderer.java (compiles with releases 1.2, 1.3, and 1.4) is used by
        * TableDialogEditDemo.java.
        */
    class ColorRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }


        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color newColor = (Color)color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", "
                    + newColor.getGreen() + ", "
                    + newColor.getBlue());
            return this;
        }
    }

    /*
    * ColorEditor.java (compiles with releases 1.3 and 1.4) is used by
    * TableDialogEditDemo.java.
    */
    /*
    class Symbol {
        private Color currentColor;
        private float stroke;
        private int pointSize;

        public Symbol(Color selected, float weight, int size){
            currentColor = selected;
            stroke = weight;
            pointSize = size;
        }

        public Color getColor(){
            return currentColor;
        }
    }
    */

    public void update_plots(int id){
/*
        public PlotDataSingleton log10IntensityPlot;
        public KratkyPlot kratky;
        public QIQPlot qIqPlot;
        public ErrorPlot errorPlot;
        public PowerLawPlot powerLawPlot;
*/
        if (KratkyPlot.frame.isVisible()){

        }
    }

    //Analysis Spinners
    public static class SpinnerEditor extends DefaultCellEditor implements ChangeListener {
        private JSpinner spinner;
        JSpinner.DefaultEditor editor;
        JTextField textField;
        boolean valueSet;
        private int rowID;
        private int colID;
        private int priorValue;

        // Initializes the spinner - Constructor.
        public SpinnerEditor() {
            super(new JTextField());
            spinner = new JSpinner();
            editor = ((JSpinner.DefaultEditor)spinner.getEditor());
            textField = editor.getTextField();

            textField.addFocusListener( new FocusListener() {
                public void focusGained( FocusEvent fe ) {
                    System.err.println("Got focus");

                }
                public void focusLost( FocusEvent fe ) {
                    System.out.println("FocusLost " + collectionSelected.getDataset(rowID).getData().getX(0) + " | value " + spinner.getValue());
                }
            });

            textField.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    stopCellEditing();
                }
            });

            spinner.addChangeListener((ChangeListener) this);
        }

        public void stateChanged(ChangeEvent e){
            int temp = (Integer)this.spinner.getValue();
            int current = (Integer) this.spinner.getValue() - 1;
            int direction = temp - this.priorValue;
            int limit;
            Dataset dataset = collectionSelected.getDataset(rowID);

            if (this.colID == 4){

                double test = dataset.getData().getMaxX(); // plotted data
                int valueOfSpinner = (Integer)this.spinner.getValue();
                //
                if ((valueOfSpinner < 1) || valueOfSpinner > dataset.getData().getItemCount() || ( dataset.getOriginalLog10Data().getX( valueOfSpinner ).doubleValue() >= test)){
                    this.spinner.setValue(1);
                    this.priorValue = 1;
                } else {
                    //moving up or down?
                    if (direction > 0) {
                        if (direction == 1) {
                            dataset.getData().remove(0);
                            // check other plots and update
                        } else {
                            limit = (Integer) temp - this.priorValue;
                            // keep removing first point
                            // current is the last point
                            XYSeries currentData = dataset.getData();
                            currentData.delete(0, limit);
                        }
                        this.priorValue = temp;
                    } else if (direction < 0){
                        if (direction == -1) {
                            dataset.getData().add(dataset.getScaledLog10DataItemAt(current));
                        } else {
                            // keep adding points up until currentValue
                            int start = current;
                            XYSeries tempData = dataset.getData();
                            double previousIntialValue = tempData.getMinX();
                            int indexInOriginal = dataset.getOriginalLog10Data().indexOf(previousIntialValue);

                            for (int i = start; i<=indexInOriginal; i++){
                                tempData.add(dataset.getScaledLog10DataItemAt(i));
                            }
                        }
                        this.priorValue = temp;
                    }
                }

                dataset.setStart((Integer)this.getCellEditorValue());

            } else if (colID == 5) {
                limit = dataset.getOriginalLog10Data().getItemCount();
                if ((Integer)this.spinner.getValue() > limit){
                    this.spinner.setValue(limit);
                    this.priorValue = limit;
                } else {
                    //moving up or down?
                    if (direction < 0) {
                        if (direction == -1) {
                            //remove last point
                            dataset.getData().remove(dataset.getData().getItemCount()-1);
                        } else {
                            limit = (Integer) temp - this.priorValue;
                            //current is the last point
                            int start = dataset.getData().getItemCount() - 1;
                            int stop = start + limit;
                            // keep removing last point
                            dataset.getData().delete(stop, start);
                        }

                    } else if (direction > 0){
                        if (direction == 1) {
                            dataset.getData().add(dataset.getScaledLog10DataItemAt(current));
                        } else {
                            // keep adding points up until currentValue
                            //Dataset tempDataset = collectionSelected.getDataset(rowID);
                            //XYSeries tempData = tempDataset.getData();
                            XYSeries tempData = dataset.getData();
                            int last = current;
                            double lastPlottedValue = tempData.getMaxX();
                            int indexOfLastPlottedValue = tempData.indexOf(lastPlottedValue);

                            for(int i = indexOfLastPlottedValue; i <= last; i++){
                                tempData.add(dataset.getScaledLog10DataItemAt(i));
                            }
                        }
                    }
                    this.priorValue = temp;
                }

                dataset.setEnd((Integer)this.getCellEditorValue());

            }
            // update plots
            // update_plots(rowID);
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            super.addCellEditorListener(l);    //To change body of overridden methods use File | Settings | File Templates.
        }

        // Prepares the spinner component and returns it.
        public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column) {

            rowID = row;
            colID = column;

            if (colID == 4){
                priorValue = collectionSelected.getDataset(rowID).getStart();
            } else if (colID == 5){
                priorValue = collectionSelected.getDataset(rowID).getEnd();
            }

            spinner.setValue(priorValue);

            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    textField.requestFocus();
                }
            });
            return spinner;
        }

        public boolean isCellEditable( EventObject eo )
        {
            //System.err.println("isCellEditable");
            if ( eo instanceof KeyEvent ) {
                KeyEvent ke = (KeyEvent)eo;
                System.err.println("key event: "+ke.getKeyChar());
                textField.setText(String.valueOf(ke.getKeyChar()));
                //textField.select(1,1);
                //textField.setCaretPosition(1);
                //textField.moveCaretPosition(1);
                valueSet = true;
            } else {
                valueSet = false;
            }
            return true;
        }

        // Returns the spinners current value.
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        public boolean stopCellEditing() {
            System.err.println("Stopping edit");
            try {
                editor.commitEdit();
                //spinner.commitEdit();
            } catch ( java.text.ParseException e ) {
                JOptionPane.showMessageDialog(null,
                        "Invalid value, discarding.");
            }
            return super.stopCellEditing();
        }

    } // end of spinnerEditor


    public class CheckBoxCellEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {

        private JCheckBox checkBox;

        public CheckBoxCellEditorRenderer() {
            this.checkBox = new JCheckBox();
            checkBox.addActionListener(this);
            checkBox.setOpaque(false);
            checkBox.setBackground(Color.WHITE);
            checkBox.setMaximumSize(new Dimension(30,30));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            checkBox.setSelected(Boolean.TRUE.equals(value));
            return checkBox;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

            if (table.getModel().toString().contains("Analysis")){

                if (column == 2) {

                    collectionSelected.getDataset(row).setInUse(!(Boolean)value);

                    if (log10IntensityPlot.isVisible()){
                        log10IntensityPlot.changeVisibleSeries(row, collectionSelected.getDataset(row).getInUse());
                    }

                    if (kratky.isVisible()){
                        kratky.changeVisibleSeries(row, collectionSelected.getDataset(row).getInUse());
                    }

                    if (qIqPlot.isVisible()){
                        qIqPlot.changeVisibleSeries(row, collectionSelected.getDataset(row).getInUse());
                    }

                    if (errorPlot.isVisible()){
                        errorPlot.changeVisibleSeries(row, collectionSelected.getDataset(row).getInUse());
                    }

                    if (powerLawPlot.isVisible()){
                        powerLawPlot.changeVisibleSeries(row, collectionSelected.getDataset(row).getInUse());
                    }

/*
                    if (errorPlot.frame.isVisible()){
                        errorPlot.chart.getXYPlot().getRenderer().setSeriesVisible(row, collectionSelected.getDataset(row).getInUse());
                    }

                    if (powerLawPlot.frame.isVisible()){
                        powerLawPlot.chart.getXYPlot().getRenderer().setSeriesVisible(row, collectionSelected.getDataset(row).getInUse());
                    }

                    if (normalKratkyRg.frame.isVisible()){
                        int old = normalKratkyRg.newDataset.getSeriesCount();
                        int key;
                        if (collectionSelected.getDataset(row).getInUse()){ // if true, add to plot
                            // if row is present as key, toggle visibility, if not add it
                            boolean test = false;
                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyRg.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyRg.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                    test = true;
                                }
                            }

                            if (!test){
                                // add series to plot
                                double i_zero_n = 0.0;
                                double rg_n = 0.0;
                                double invIzero, rg2, q, q2;

                                int start, stop;
                                XYDataItem tempDataItem;

                                Dataset tempData = collectionSelected.getDataset(row);
                                XYSeries tempKratkyData = tempData.getOriginalData(); // non-negative values

                                i_zero_n = tempData.getGuinierIzero();
                                invIzero = 1/i_zero_n;

                                rg_n = tempData.getGuinierRg();
                                rg2 = rg_n*rg_n*invIzero;

                                start = (Integer)analysisModel.getValueAt(row,4) - 1;
                                stop = (Integer)analysisModel.getValueAt(row,5) - 1;
                                if (i_zero_n > 0 && rg_n > 0){
                                    normalKratkyRg.newDataset.addSeries(new XYSeries(tempData.getId()));
                                    for (int j = start; j < stop; j++){
                                        tempDataItem = tempKratkyData.getDataItem(j);
                                        q = tempDataItem.getXValue();
                                        q2 = q*q;
                                        // Dimensionless via Rg
                                        normalKratkyRg.newDataset.getSeries(old).add(q*rg_n, q2*rg2*tempDataItem.getYValue());
                                    }

                                    double pointSize = tempData.getPointSize();
                                    double negativePointSize = -0.5*pointSize;

                                    normalKratkyRg.renderer1.setSeriesShape(old, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
                                    normalKratkyRg.renderer1.setSeriesLinesVisible(old, false);
                                    normalKratkyRg.renderer1.setSeriesShapesFilled(old, tempData.getBaseShapeFilled());
                                    normalKratkyRg.renderer1.setSeriesPaint(old, tempData.getColor());
                                    normalKratkyRg.renderer1.setSeriesOutlinePaint(old, tempData.getColor());
                                    normalKratkyRg.renderer1.setSeriesOutlineStroke(old, tempData.getStroke());
                                }
                            }
                        } else { //remove from plot
                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyRg.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyRg.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                }
                            }
                        }
                    }

                    if (normalKratkyVc.frame.isVisible()){
                        int old = normalKratkyVc.newDataset.getSeriesCount();
                        int key;
                        if (collectionSelected.getDataset(row).getInUse()){ // if true, add to plot
                            // if row is present as key, toggle visibility, if not add it
                            boolean test = false;
                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyVc.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyVc.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                    test = true;
                                }
                            }

                            if (!test){
                                // add series to plot
                                double i_zero_n = 0.0;
                                double invIzero, q, vc, q2, vcI;

                                int start, stop;
                                XYDataItem tempDataItem;

                                Dataset tempData = collectionSelected.getDataset(row);
                                XYSeries tempKratkyData = tempData.getOriginalData(); // non-negative values

                                i_zero_n = tempData.getGuinierIzero();
                                invIzero = 1/i_zero_n;
                                vc = tempData.getVC();

                                if (vc <= 0) {
                                    determineVc(row);
                                    vc = tempData.getVC();
                                }

                                vcI = vc*invIzero;

                                start = (Integer)analysisModel.getValueAt(row,4) - 1;
                                stop = (Integer)analysisModel.getValueAt(row,5) - 1;
                                if (i_zero_n > 0){
                                    normalKratkyVc.newDataset.addSeries(new XYSeries(tempData.getId()));
                                    for (int j = start; j < stop; j++){
                                        tempDataItem = tempKratkyData.getDataItem(j);
                                        q = tempDataItem.getXValue();
                                        q2 = q*q;
                                        // Dimensionless via Vc
                                        normalKratkyVc.newDataset.getSeries(old).add(q2*vc, q2*vcI*tempDataItem.getYValue());
                                    }

                                    double pointSize = tempData.getPointSize();
                                    double negativePointSize = -0.5*pointSize;

                                    normalKratkyVc.renderer1.setSeriesShape(old, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
                                    normalKratkyVc.renderer1.setSeriesLinesVisible(old, false);
                                    normalKratkyVc.renderer1.setSeriesShapesFilled(old, tempData.getBaseShapeFilled());
                                    normalKratkyVc.renderer1.setSeriesPaint(old, tempData.getColor());
                                    normalKratkyVc.renderer1.setSeriesOutlinePaint(old, tempData.getColor());
                                    normalKratkyVc.renderer1.setSeriesOutlineStroke(old, tempData.getStroke());
                                }
                            }
                        } else { //remove from plot

                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyVc.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyVc.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                }
                            }
                        }
                    }

                    if (normalKratkyVcReal.frame.isVisible()){
                        int old = normalKratkyVcReal.newDataset.getSeriesCount();
                        int key;
                        if (collectionSelected.getDataset(row).getInUse() && (collectionSelected.getDataset(row).getRealRg() > 0)){ // if true, add to plot
                            // if row is present as key, toggle visibility, if not add it
                            boolean test = false;
                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyVcReal.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyVcReal.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                    test = true;
                                }
                            }

                            if (!test){
                                // add series to plot
                                double i_zero_n = 0.0;
                                double invIzero, q, vc, q2, vcI;

                                int start, stop;
                                XYDataItem tempDataItem;

                                Dataset tempData = collectionSelected.getDataset(row);
                                XYSeries tempKratkyData = tempData.getOriginalData(); // non-negative values

                                i_zero_n = tempData.getRealIzero();
                                invIzero = 1/i_zero_n;
                                vc = tempData.getVCReal();

                                if (vc <= 0) {
                                    determineVc(row);
                                    vc = tempData.getVCReal();
                                }

                                vcI = vc*invIzero;

                                start = (Integer)analysisModel.getValueAt(row,4) - 1;
                                stop = (Integer)analysisModel.getValueAt(row,5) - 1;
                                if (i_zero_n > 0){
                                    normalKratkyVcReal.newDataset.addSeries(new XYSeries(tempData.getId()));
                                    for (int j = start; j < stop; j++){
                                        tempDataItem = tempKratkyData.getDataItem(j);
                                        q = tempDataItem.getXValue();
                                        q2 = q*q;
                                        // Dimensionless via Vc
                                        normalKratkyVcReal.newDataset.getSeries(old).add(q2*vc, q2*vcI*tempDataItem.getYValue());
                                    }

                                    double pointSize = tempData.getPointSize();
                                    double negativePointSize = -0.5*pointSize;

                                    normalKratkyVcReal.renderer1.setSeriesShape(old, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
                                    normalKratkyVcReal.renderer1.setSeriesLinesVisible(old, false);
                                    normalKratkyVcReal.renderer1.setSeriesShapesFilled(old, tempData.getBaseShapeFilled());
                                    normalKratkyVcReal.renderer1.setSeriesPaint(old, tempData.getColor());
                                    normalKratkyVcReal.renderer1.setSeriesOutlinePaint(old, tempData.getColor());
                                    normalKratkyVcReal.renderer1.setSeriesOutlineStroke(old, tempData.getStroke());
                                }
                            }
                        } else { //remove from plot

                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyVcReal.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyVcReal.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                }
                            }
                        }
                    }

                    if (normalKratkyRgReal.frame.isVisible() ){
                        int old = normalKratkyRgReal.newDataset.getSeriesCount();
                        int key;
                        if (collectionSelected.getDataset(row).getInUse() && (collectionSelected.getDataset(row).getRealRg() > 0)){ // if true, add to plot
                            // if row is present as key, toggle visibility, if not add it
                            boolean test = false;
                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyRgReal.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyRgReal.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                    test = true;
                                }
                            }

                            if (!test){
                                // add series to plot
                                double i_zero_n = 0.0;
                                double rg_n = 0.0;
                                double invIzero, rg2, q, q2;

                                int start, stop;
                                XYDataItem tempDataItem;

                                Dataset tempData = collectionSelected.getDataset(row);
                                XYSeries tempKratkyData = tempData.getOriginalData(); // non-negative values

                                i_zero_n = tempData.getRealIzero();
                                invIzero = 1/i_zero_n;

                                rg_n = tempData.getRealRg();
                                rg2 = rg_n*rg_n*invIzero;

                                start = (Integer)analysisModel.getValueAt(row,4) - 1;
                                stop = (Integer)analysisModel.getValueAt(row,5) - 1;
                                if (i_zero_n > 0 && rg_n > 0){
                                    normalKratkyRgReal.newDataset.addSeries(new XYSeries(tempData.getId()));
                                    for (int j = start; j < stop; j++){
                                        tempDataItem = tempKratkyData.getDataItem(j);
                                        q = tempDataItem.getXValue();
                                        q2 = q*q;
                                        // Dimensionless via Rg
                                        normalKratkyRgReal.newDataset.getSeries(old).add(q*rg_n, q2*rg2*tempDataItem.getYValue());
                                    }

                                    double pointSize = tempData.getPointSize();
                                    double negativePointSize = -0.5*pointSize;

                                    normalKratkyRgReal.renderer1.setSeriesShape(old, new Ellipse2D.Double(negativePointSize, negativePointSize, pointSize, pointSize));
                                    normalKratkyRgReal.renderer1.setSeriesLinesVisible(old, false);
                                    normalKratkyRgReal.renderer1.setSeriesShapesFilled(old, tempData.getBaseShapeFilled());
                                    normalKratkyRgReal.renderer1.setSeriesPaint(old, tempData.getColor());
                                    normalKratkyRgReal.renderer1.setSeriesOutlinePaint(old, tempData.getColor());
                                    normalKratkyRgReal.renderer1.setSeriesOutlineStroke(old, tempData.getStroke());
                                }
                            }
                        } else { //remove from plot
                            for (int i=0; i<old; i++){
                                key = (Integer)normalKratkyRgReal.newDataset.getSeries(i).getKey();
                                if (key == row){
                                    normalKratkyRgReal.chart.getXYPlot().getRenderer().setSeriesVisible(i, collectionSelected.getDataset(row).getInUse());
                                }
                            }
                        }
                    }
*/
                } else if (column == 6){ // Fit File
                    collectionSelected.getDataset(row).setFitFile(!(Boolean)value);
                    // switch Error to allData
                }
            } else if (table.getModel().toString().contains("Pr")){
                /*
                PrModel temp = (PrModel)table.getModel();
                RealSpace tempReal = temp.getDataset(row);
                tempReal.setSelected(!(Boolean)value);
                pofRWindow.chart.getXYPlot().getRenderer(0).setSeriesVisible(row, tempReal.getSelected());
                iofQPofRWindow.chart.getXYPlot().getRenderer(0).setSeriesVisible(row, tempReal.getSelected());
                iofQPofRWindow.chart.getXYPlot().getRenderer(1).setSeriesVisible(row, tempReal.getSelected());
                */
            }

            checkBox.setSelected(Boolean.TRUE.equals(value));
            return checkBox;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            stopCellEditing();
        }

        @Override
        public Object getCellEditorValue() {
            return checkBox.isSelected();
        }
    }

    class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        Color currentColor;
        JComboBox pointSizes;
        JComboBox thickBox;
        JButton button;
        JColorChooser colorChooser;
        JDialog dialog;
        String tableModel;
        int data_row;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            //Set up the editor (from the table's point of view),
            //which is a button.
            //This button brings up the color chooser dialog,
            //which is the editor from the user's point of view.
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            //Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button,
                    "Pick a Color or Change Size",
                    true,  //modal
                    colorChooser,
                    this,  //OK button handler
                    null); //no CANCEL button handler
        }

        /**
         * Handles events from the editor button and from
         * the dialog's OK button.
         */
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                //The user has clicked the cell, so
                //bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                JPanel preview = new JPanel();

                JLabel pointTitle = new JLabel("Point Size");
                String[] sizes = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "15", "17", "19", "21"};
                pointSizes = new JComboBox(sizes);
                preview.add(pointTitle);

                JLabel thicknessTitle = new JLabel(" | Line Stroke");
                String[] thicknesses = {"0.5", "1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0"};
                thickBox = new JComboBox(thicknesses);
                preview.add(thicknessTitle);

                if (tableModel.contains("Analysis")){
                    Dataset temp = collectionSelected.getDataset(data_row);
                    int index=0;
                    for (int i=0; i<sizes.length; i++){
                        if (temp.getPointSize() == Integer.parseInt(sizes[i])){
                            index = i;
                        }
                    }

                    pointSizes.setSelectedIndex(index);
                    preview.add(pointSizes);

                    index=0;
                    for (int i=0; i<thicknesses.length; i++){
                        if (temp.getStroke().getLineWidth() == Float.parseFloat(thicknesses[i])){
                            index = i;
                        }
                    }

                    thickBox.setSelectedIndex(index);
                    preview.add(thickBox);

                } else if (tableModel.contains("Pr")){
                    /*
                    RealSpace temp = prModel.getDataset(data_row);
                    int index=0;
                    for (int i=0; i<sizes.length; i++){
                        if (temp.getPointSize() == Integer.parseInt(sizes[i])){
                            index = i;
                        }
                    }

                    pointSizes.setSelectedIndex(index);
                    preview.add(pointSizes);

                    index=0;
                    for (int i=0; i<thicknesses.length; i++){
                        if (temp.getStroke().getLineWidth() == Float.parseFloat(thicknesses[i])){
                            index = i;
                        }
                    }

                    thickBox.setSelectedIndex(index);
                    preview.add(thickBox);
                    */
                }

                colorChooser.setPreviewPanel(preview);
                dialog.setVisible(true);

                //Make the renderer reappear.
                fireEditingStopped();

            } else { //User pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            int thickIndex = thickBox.getSelectedIndex();
            int pointIndex = pointSizes.getSelectedIndex();

            float thickness;
            int pointSize;

            thickness = Float.parseFloat((String) thickBox.getSelectedItem());
            pointSize = Integer.parseInt( (String)pointSizes.getSelectedItem());
            Symbol newInfo = new Symbol(currentColor, thickness, pointSize);
            return newInfo;
            //return currentColor;
        }

        //Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {


//            if (table.getModel().getClass() == AnalysisModel.class){
//                System.out.println("Analysis Table Model" + table.getModel().getClass() + " | ");
//            } else if (table.getModel().getClass() == PrModel.class){
//                System.out.println("Pr Table Model" + table.getModel().getClass() + " | ");
//            }

            tableModel = table.getModel().getClass().toString();
            currentColor = (Color)value;
            data_row = row;
            return button;
        }
    }


    private void copyDataset(String obj, Dataset dataset, String workingDirectoryName) {

        String base = obj.replaceAll("\\W","_");
        FileWriter fstream = null;
        try {
            fstream = new FileWriter(workingDirectoryName+ "/" + base + ".dat");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("# REMARK\tFile renamed " + dataset.getFileName() + " => " + base + "\n");
            out.write("# REMARK\tColumns: q, I(q), error \n");

            int numberOfDigits;
            XYSeries writeOut = dataset.getOriginalLog10Data();
            XYSeries writeError = dataset.getOriginalPositiveOnlyError();

            int totalItems = dataset.getOriginalLog10Data().getItemCount();
            for (int i=0; i< totalItems; i++){
                numberOfDigits = getDigits(writeOut.getX(i).doubleValue());
       //         out.write( String.format("%s\t%s\t%s %n", formattedQ(writeOut.getX(i).doubleValue(), numberOfDigits), scientific1dot5e2.format(writeOut.getY(i).doubleValue()),scientific1dot5e2.format(writeError.getY(i).doubleValue())));
            }

            status.setText("Renamed(copied) original file");
            dataset.setFileName(base);
            //Close the output stream
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ButtonEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        JButton button;
        private int rowID;
        private int colID;

        public ButtonEditorRenderer(){
            this.button = new JButton();
            button.addActionListener(this);
            button.setMaximumSize(new Dimension(10,10));
            button.setPreferredSize(new Dimension(10,10));
            button.setText("G");
            button.setFont(new Font("Verdana", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            button.setSelected(Boolean.TRUE.equals(value));
            this.button.setForeground(Color.BLACK);
            return button;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

            button.setSelected(Boolean.TRUE.equals(value));
            rowID = row;
            colID = column;
            return button;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.colID == 13) {
                this.button.setBackground(Color.WHITE);
                this.button.setForeground(Color.GREEN);

                //if (manualGuinierFrame instanceof JFrame){
                //    manualGuinierPlot.dispose();
                //    manualGuinierFrame.dispose();
                //}

                PlotManualGuinier manualGuinierPlot = new PlotManualGuinier("Guinier Plot", collectionSelected.getDataset(rowID), WORKING_DIRECTORY_NAME);
                System.out.println("PLOTTING GUINIER");
                manualGuinierPlot.plot(analysisModel);
                //plotGuinierRg(collectionSelected.getDataset(rowID));
            }
        }

        @Override
        public Object getCellEditorValue() {
            return button.isSelected();
        }
    }

    private int getDigits(double qvalue) {
        String toText = Double.toString(qvalue);
        int integerPlaces = toText.indexOf('.');
        int decimalPlaces;

        String[] temp = toText.split("\\.0*");
        decimalPlaces = (temp.length == 2) ? temp[1].length() : (toText.length() - integerPlaces -1);

        return decimalPlaces;
    }


} // end of Scatter class

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