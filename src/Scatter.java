import net.iharder.dnd.FileDrop;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import version3.*;
import version3.Collection;
import version3.formfactors.FormFactorEngine;
import version3.formfactors.ModelType;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
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
    private JProgressBar mainProgressBar;
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
    private JButton plotMedianAndAverageSampleButton;
    private JButton clearSamplesButton;
    private JButton signalPlotButton;
    private JButton plotLog10AverageSampleButton;
    private JButton subtractFromSelectedBufferButton;
    private JTextField qminSubtractionField;
    private JTextField qmaxSubtractionField;
    private JComboBox comboBoxSubtractBins;
    private JCheckBox averageSampleFileCheckBox;
    private JCheckBox SVDAverageFilesCheckBox;
    private JCheckBox subtractFromMedianCheckBox;
    private JCheckBox scaleThenMergeCheckBox;
    private JCheckBox addRgToSignalCheckBox;
    private JComboBox setReferenceBox;
    private JPanel rightSubtractionPanel;
    private JPanel centerPanel;
    private JComboBox subtractionCutOff;
    private JPanel leftSubtractionPanel;
    private JScrollPane samples;
    private JScrollPane buffers;
    private JLabel samplesSubtractionLabel;
    private JTextField subtractionFileNameField;
    private JPanel covDetailsPanel;
    private JScrollPane covFilesScrollPanel;
    private JPanel covResetPanel;
    private JPanel covRightPanel;
    private JPanel covLeftPanel;
    private JButton clearSimButton;
    private JCheckBox autoRgCheckBox;
    private JButton medianButton;
    private JButton singleButton;
    private JButton scaleToIZeroButton;
    private JButton scaleMergeButton;
    private JPanel resultsPanel;
    private JButton exportButton;
    private JPanel headerPanel;
    private JButton izeroRgPlot;
    private JButton setOutputDirectoryButton;
    private JLabel subtractOutPutDirectoryLabel;
    private JButton buffersClearButton;
    private JCheckBox onDropConvertNmCheckBox;
    private JButton plotAverageAndMedianBuffers;
    private JButton plotLog10AverageBufferButton;
    private JButton calculateSimlarityButton;
    private JCheckBox excessKurtosisCheckBox;
    private JCheckBox volatilityVRCheckBox;

    private JTextField qminSimilarityField;
    private JTextField qmaxSimilarityField;
    private JLabel qmaxSimilarityLabel;
    private JLabel qminSimilarityLabel;
    private JButton realSpaceButton;
    private JButton addSetButton;
    private JTextField nameOfSetTextField;
    private JPanel simParamsPanel;
    private JButton SELECTSetsToAnalyzeButton;
    private JButton clearAboveButton;
    private JComboBox simBinsComboBox;
    private JPanel heatMapPanel;
    private JPanel simScatPlotPanel;
    private JLabel simLabel;
    private JButton atsasDirButton;
    private JButton workingDirButton;
    private JLabel workingDirLabel;
    private JLabel atsasDirLabel;
    private JTextField qmaxLimitField;
    private JTextField qminLimitField;
    private JButton runSphereModelButton;
    private JButton SVDReduceButton;

    private JPanel menuBarPrPanel;
    private JPanel prGraphPanels;
    private JCheckBox qIQCheckBox;
    private JProgressBar progressBar1;
    private JLabel prStatusLabel;
    private JPanel pr_settings;
    private JComboBox refinementRoundsBox;
    private JComboBox rejectionCutOffBox;
    private JLabel defaultDmax;
    private JLabel dmaxLabel;
    private JCheckBox l1NormCheckBox;
    private JSlider dmaxSlider;
    private JComboBox lambdaBox;
    private JComboBox cBox;
    private JPanel prPanel;
    private JScrollPane prScrollPane;
    private JPanel lowerPrPanel;
    private JPanel prDistribution;
    private JPanel prIntensity;
    private JTextField a39TextField;
    private JTextField a157TextField;
    private JComboBox ffCEFileSelectionComboBox;
    private JList completedDamminList;
    private JComboBox runsComboBox;
    private JRadioButton damminRadioButton;
    private JRadioButton dammifRadioButton;
    private JComboBox symmetryBox;
    private JRadioButton fastRadioButton;
    private JRadioButton slowRadioButton;
    private JButton GNOMOutFileButton;
    private JComboBox cpuBox;
    private JCheckBox damRefineCheckBox;
    private JLabel damstartLabel;
    private JButton damstartButton;
    private JCheckBox alignPDBModelRunsCheckBox;
    private JButton startButton;
    private JButton selectPDBDamminButton;
    private JLabel supcombLabel;
    private JScrollPane damScrollPane;
    private JTextPane damTextPane;
    private JLabel damminLabel;
    private JComboBox comboBoxSource;
    private JTextField sourceTextField;
    private JScrollPane stdOutPane;
    private JTextPane stdOutText;
    private JScrollPane generalPane;
    private JTextPane generalText;
    private JTextField lowerSphereRadius;
    private JTextField upperSphereRadius;
    private JTextField particleContrastText;
    private JTextField solventContrastText;
    private JTextField qminCEText;
    private JTextField qmaxCEText;
    private JPanel sphereDistributionPanel;
    private JPanel dataSpherePanel;
    private JTextField ellipsoidLowerRadiusText;
    private JLabel ellipsoidUpperMajorLimitText;
    private JButton ellipsoidButton;
    private JTextField ellipsoidSolventText;
    private JTextField ellipsoidParticleText;
    private JTextField minorAxisLabel;
    private JTextField majorAxisLabel;
    private JProgressBar progressBarSphere;
    private JProgressBar progressBarEllipsoid;
    private JPanel ellipsoidResidualPanel;
    private JPanel ellipsoidFitPanel;
    private JPanel ellipsoidResultsPanel;
    private JPanel ellipsoidParametersPanel;
    private JButton packageItButton;
    private JTextField qmaxForPDBText;
    private JCheckBox excludeWatersFromInputCheckBox;
    private JLabel versionLabel;
    private JTextField thresholdField;
    private JButton changeCWDButton;
    private JButton pdfButton;
    private JButton TRACEButton;
    private JButton SETBUFFERButton;
    private JButton UPDATEButton;
    private JButton SETPLOTRANGEButton;
    private JButton RESETRANGEButton;
    private JCheckBox checkBoxDirect;
    private JButton setPipelineDataDirButton;
    private JButton setPipelineOutputDirButton;
    private JButton setXMLFileButton;
    private JButton STARTPipelineButton;
    private JButton STOPButton;
    private JProgressBar progressBar2;
    private JButton DEPOSITButton;
    private JPanel panelPipelineTable;
    private JButton CLEARButton;
    private JLabel labelPipelineDir;
    private JLabel labelOutputDirPipeline;
    private JLabel labelXML;
    private JLabel labelPipelineMessages;
    private JButton SAVETODIRECTORYSubtractButton;
    private JButton SECFactorAnalysisICAButton;
    private JLabel rangeSetLabel;
    private JCheckBox excludeBackgroundInFitCheckBox;
    private JPanel subtractPanel;
    private JButton SVDButton;
    private JButton ICAButton;
    private JPanel bottomMiniPanel;
    private JButton testButton;
    private JTextField subtractThresholdField;
    private JComboBox excludeComboBox;
    private JCheckBox shapiroWilkCheckBox;
    private JComboBox ceComboBoxRounds;
    private JComboBox topNComboBox;
    private JComboBox trialsPerRoundComboBox;
    private JComboBox modelsPerRoundComboBox;
    private JTextField epsilonTextField;
    private JPanel sphereResidualsPanel;
    private JPanel leftSpherePanel;
    private JPanel rightSpherePanel;
    private JComboBox lambdaCEcomboBox;
    private JPanel crossSection1Panel;
    private JPanel crossSection2Panel;
    private JPanel crossSection3Panel;
    private JPanel crossSection4Panel;
    private JPanel scoreProgressEllipsePanel;
    private JPanel sphereDataPlotPanel;
    private JPanel ellipseDataPanel;
    private JPanel ellipsoidDistributionPanel;
    private JPanel histogramRaPanel;
    private JPanel histogramRbPanel;
    private JPanel histogramRcPanel;
    private JComboBox ellipseComboBox1;
    private JButton buttonCoreShell;
    private JProgressBar coreShellProgressBar;
    private JPanel coreShellParametersPanel;
    private JButton run3BodyButton;
    private JProgressBar progressBar3Body;
    private JTextField min3BodyTextField;
    private JTextField max3BodyTextField;
    private JTextField solventContrast3Body;
    private JTextField particleContrast3Body;
    private JTextField coreShellSolventContrast;
    private JTextField coreShellShellContrast;
    private JTextField coreShellCoreContrast;
    private JTextField radiiLowerCoreShell;
    private JTextField radiiUpperCoreShell;
    private JPanel residuals3BodyPanel;
    private JPanel dataFit3BodyPanel;
    private JPanel scorePanelCoreShell;
    private JPanel dataFitsCoreShell;
    private JCheckBox coreEmptyAndFullCheckBox;
    private JPanel scoreProgressThreeBodyPanel;
    private JPanel threeBodyGeometryPanel;
    private JPanel threeBodyDistributionPlot;
    private JPanel smallest3BodyPanel;
    private JPanel middle3BodyPanel;
    private JPanel largest3BodyPanel;
    private JButton button2;
    private JProgressBar progressBar4;
    private JTextField floryField;
    private JTextField a66TextField;
    private JComboBox coreShellEllipsoidModel;
    private JTextField shellThickness;
    private JCheckBox fitBackgroundCheckBox;
    private JPanel residualsCoreShellPanel;
    private JPanel histogramRaCoreShellPanel;
    private JPanel histogramRcCoreShellPanel;
    private JPanel histogramShellThicknessPanel;
    private JPanel geometryCoreShellPanel;
    private JRadioButton maximumEntropyRadioButton;
    private JRadioButton secondDerivativeRadioButton;
    private JTextField textField1;
    private JPanel plotPanel3Body;

    private String version = "3.0i";
    private static WorkingDirectory WORKING_DIRECTORY;
    private static WorkingDirectory PIPELINE_DATA_DIRECTORY;
    private static WorkingDirectory PIPELINE_OUTPUT_DIRECTORY;
    //private static String WORKING_DIRECTORY_NAME;
    private static String OUTPUT_DIR_SUBTRACTION_NAME="";
    private static String BEAMLINEMANUFACTURER="";
    private static String XRAYSOURCE="";
    private static String ATSAS_DIRECTORY="";
    private static String THRESHOLD="";

    private static DefaultListModel<DataFileElement> dataFilesModel;
    private DefaultListModel<DataFileElement> fitFilesModel;
    private DefaultListModel<DataFileElement> complexFilesModel;
    public DefaultListModel<SampleBufferElement> bufferFilesModel;
    public DefaultListModel<SampleBufferElement> sampleFilesModel;
    private DefaultListModel<SampleBufferElement> similarityFilesModel;
    private DefaultListModel<String> chiFilesModel;
    private DefaultListModel<String> damminfModelsModel;

    private static HashMap collections = new HashMap();
    private static Collection bufferCollections = new Collection();;
    private static Collection sampleCollections = new Collection();;
    private static Collection similarityCollection = new Collection();;
    private static Collection collectionSelected = new Collection();;
    public static ArrayList<JRadioButton> collectionButtons;
    public static ArrayList<Graph> miniPlots;
    public static ArrayList<JPanel> minis;

    public static int totalPanels;

    public static JTable analysisTable;
    public static AnalysisModel analysisModel;
    public static ResultsModel resultsModel;

    public static JTable prTable;
    public static PrModel prModel;

    private JList buffersList;
    private JList samplesList;
    private JList similarityList;
    private JList fitFilesList;

    private DoubleValue dmaxLow;
    private DoubleValue dmaxHigh;
    private DoubleValue dmaxStart;

    private int firstIndexInSignalPlot;
    private int lastIndexInSignalPlot;

    // singleton plots
    public PlotDataSingleton log10IntensityPlot;
    public KratkyPlot kratky;
    public QIQPlot qIqPlot;
    public ErrorPlot errorPlot;
    public PowerLawPlot powerLawPlot;
    public MiniPlots analysisMiniPlots;

    private static Similarity similarityObject;

    public NormalizedKratkyPlot normalKratkyRg;
    public NormalizedKratkyPlot normalKratkyRgReal;
    public NormalizedKratkyPlot normalKratkyVc;
    public NormalizedKratkyPlot normalKratkyVcReal;

    private boolean isCtrlC = false;
    private boolean isCtrlB = false;
    private boolean clearALL = false;
    private boolean damstartStatus = false;
    private File supcombFile;

    private static int cpuCores;

    public Scatter() { // constructor
        versionLabel.setText("Version : "+ version);
        MessageConsole mc = new MessageConsole(stdOutText);
        //mc.redirectOut();
        //mc.redirectErr(Color.RED, null);

        final MessageConsole info = new MessageConsole(generalText);
        //info.redirectOut();

        //int[] subtractionBins = new int[] {11, 13, 17, 23, 29};
        //comboBoxSubtractBins = new JComboBox(subtractionBins);
        refinementRoundsBox.setSelectedIndex(0);
        rejectionCutOffBox.setSelectedIndex(3);
        simBinsComboBox.setSelectedIndex(0);
        lambdaBox.setSelectedIndex(6);
        cBox.setSelectedIndex(1);
        lambdaCEcomboBox.setSelectedIndex(5);
        ceComboBoxRounds.setSelectedIndex(1);
        trialsPerRoundComboBox.setSelectedIndex(2);
        modelsPerRoundComboBox.setSelectedIndex(4);
        topNComboBox.setSelectedIndex(0);

        collections = new HashMap();
        bufferCollections = new Collection();
        sampleCollections = new Collection();
        similarityCollection = new Collection();

        cpuCores = Runtime.getRuntime().availableProcessors();
        Vector comboBoxItemsCPU = new Vector();
        for (int i=1; i<=cpuCores; i++){
            comboBoxItemsCPU.add(i);
        }

        final DefaultComboBoxModel cpuModel = new DefaultComboBoxModel(comboBoxItemsCPU);
        cpuBox.setModel(cpuModel);

        // Files Tab
        dataFilesModel = new DefaultListModel<DataFileElement>();
        dataFilesList.setCellRenderer(new DataFilesListRenderer());
        dataFilesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        buffersList = new JList();
        samplesList = new JList();
        similarityList = new JList();
        MouseAdaptorForDragging mouseAdaptorForDragging = new MouseAdaptorForDragging();
        similarityList.addMouseListener(mouseAdaptorForDragging);
        similarityList.addMouseMotionListener(mouseAdaptorForDragging);
        //fitFilesList = new JList();

        bufferFilesModel = new DefaultListModel<SampleBufferElement>();
        sampleFilesModel = new DefaultListModel<SampleBufferElement>();
        similarityFilesModel = new DefaultListModel<SampleBufferElement>();

        //fitFilesModel = new DefaultListModel<DataFileElement>();
        //chiFilesModel = new DefaultListModel<String>();
        damminfModelsModel = new DefaultListModel<String>();

        buffersList.setModel(bufferFilesModel);
        samplesList.setModel(sampleFilesModel);
        similarityList.setModel(similarityFilesModel);

        similarityList.setCellRenderer(new SampleBufferListRenderer());
        similarityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        similarityList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);    //To change body of overridden methods use File | Settings | File Templates.
                int index = similarityList.locationToIndex(e.getPoint());
                SampleBufferElement item = (SampleBufferElement)similarityList.getModel().getElementAt(index);
            }
        });


        //similarityList.setCellRenderer(new SelectedListCellRenderer());

        covFilesScrollPanel.setViewportView(similarityList);
        //fitFilesList.setModel(fitFilesModel);
        //chiValuesList.setModel(chiFilesModel);
        completedDamminList.setModel(damminfModelsModel);

        buffersList.setCellRenderer(new SampleBufferListRenderer());
        buffersList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


        samplesList.setCellRenderer(new SampleBufferListRenderer());
        samplesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //Add to JScrollPane from GUI
        buffers.setViewportView(buffersList);
        samples.setViewportView(samplesList);
        // Subtraction Tab
        String[] rejections ={"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5"};
        for (String i:rejections){
            subtractionCutOff.addItem(i);
        }

        subtractionCutOff.setSelectedIndex(3);
        comboBoxSubtractBins.setSelectedIndex(2);

        mainPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        minis = new ArrayList<JPanel>(){{add(mini1); add(mini2); add(mini3); add(mini4);}};
        miniPlots = new ArrayList<Graph>();

        collectionButtons = new ArrayList<JRadioButton>(){{add(radioButtonLoad1); add(radioButtonLoad2); add(radioButtonLoad3); add(radioButtonLoad4); }};
        totalPanels = collectionButtons.size();

        // initialize the collections in the HashMap
        for (int i=0; i< totalPanels; i++){
            collections.put(new Integer(i), new Collection()); // HashMap for new Collection
            miniPlots.add(new Graph("Set " + Integer.toString(i+1)));
            collectionButtons.get(i).setSelected(false);
            miniPlots.get(i).plot((Collection) collections.get(i));
        }

        collectionSelected = (Collection) collections.get(0);

        collections.put(69, bufferCollections);
        collections.put(96, sampleCollections);
        collections.put(29, similarityCollection);

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
        analysisTable = new JTable(new AnalysisModel(status, WORKING_DIRECTORY));
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
                // updateMiniPlots
                updateMiniPlots(analysisTable.rowAtPoint(e.getPoint()));
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
                if (index == 2 ){ // invert selection
                    int total = collectionSelected.getDatasets().size();
                    for(int i=0; i<total; i++){
                        collectionSelected.getDataset(i).setInUse(!collectionSelected.getDataset(i).getInUse());
                    }
                    analysisModel.fireTableDataChanged();
                } else if (index == 4) {
                    // add function to replot data given qmin or qmax when user selects columns 4 or 5
                    final String inputValue = JOptionPane.showInputDialog("Please input a minimum q value");

                    if (inputValue != null && isQValue(inputValue)){


                                    if (log10IntensityPlot.isVisible()){
                                        log10IntensityPlot.setNotify(false);
                                    }
                                    final LowerUpperBoundManager boundLower = new LowerUpperBoundManager(
                                            cpuCores,
                                            collectionSelected,
                                            mainProgressBar,
                                            status);

                                    boundLower.boundNow(analysisModel, 4, Double.parseDouble(inputValue));
                                    if (log10IntensityPlot.isVisible()){
                                        //update plot
                                        //log10IntensityPlot.updatePlot();
                                        log10IntensityPlot.setNotify(true);
                                    }
                                    //log10IntensityPlot.plot(collectionSelected, WORKING_DIRECTORY_NAME);

                    }

                } else if (index == 5) {
                    final String inputValue = JOptionPane.showInputDialog("Please input a maximum q value");

                    if (inputValue != null && isQValue(inputValue)){

                                if (log10IntensityPlot.isVisible()){
                                    log10IntensityPlot.setNotify(false);
                                    //log10IntensityPlot.closeWindow();
                                }
                                final LowerUpperBoundManager boundLower = new LowerUpperBoundManager(
                                        cpuCores,
                                        collectionSelected,
                                        mainProgressBar,
                                        status);

                                boundLower.boundNow(analysisModel, 5, Double.parseDouble(inputValue));
                                if (log10IntensityPlot.isVisible()){
                                    log10IntensityPlot.setNotify(true);
                                }
                                //log10IntensityPlot.plot(collectionSelected, WORKING_DIRECTORY_NAME);


                    }

                }
                // analysisModel.fireTableDataChanged();
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

        // add mouse functions, remove, select all, select none
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Select All");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Selecting All");
                int total = collectionSelected.getDatasets().size();
                for(int i=0; i<total; i++){
                    collectionSelected.getDataset(i).setInUse(true);
                }
                analysisModel.fireTableDataChanged();
            }
        });
        popupMenu.add(deleteItem);


        popupMenu.add(new JMenuItem(new AbstractAction("DeSelect All") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                System.out.println("DeSelecting All");
                int total = collectionSelected.getDatasets().size();
                for(int i=0; i<total; i++){
                    collectionSelected.getDataset(i).setInUse(false);
                }
                analysisModel.fireTableDataChanged();
            }
        }));


        popupMenu.add(new JMenuItem(new AbstractAction("Select Highlighted") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.


                int[] rowIndex = analysisTable.getSelectedRows();

                int endat = collectionSelected.getDatasetCount();
                System.out.println("Selecting Highlighted " + endat);

                int total = collectionSelected.getDatasets().size();
                for(int i=0; i<total; i++){
                    collectionSelected.getDataset(i).setInUse(false);
                }

                total = rowIndex.length;
                for(int i=0; i<total; i++){
                    collectionSelected.getDataset(rowIndex[i]).setInUse(true);
                }

                analysisModel.fireTableDataChanged();
            }
        }));

        popupMenu.add(new JMenuItem(new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.

                int[] rowIndex = analysisTable.getSelectedRows();

                int start = rowIndex.length - 1;

                for(int j = start; j >= 0; j--){ // count in reverse and remove selected dataset
                    int index = rowIndex[j];
                    collectionSelected.removeDataset(index);
                    analysisModel.remove(index);
                    resultsModel.remove(index);
                }

                //update list on Files tab
                dataFilesModel.clear();
                dataFilesList.removeAll();
                // update dataFilesList in dataFilesPanel;
                // rebuild dataFilesPanel from collection.get(i)
                for(int j=0; j<collectionSelected.getDatasets().size(); j++){
                    String name = collectionSelected.getDataset(j).getFileName();
                    dataFilesModel.addElement(new DataFileElement(name, j));
                }

                dataFilesList.setModel(dataFilesModel);

                //update collection ids
                int total = collectionSelected.getDatasets().size();
                for(int h=0; h<total; h++){
                    collectionSelected.getDataset(h).setId(h);
                }

                dataFilesList.validate();
                dataFilesList.repaint();
                analysisModel.fireTableDataChanged();
            }
        }));

        analysisTable.setComponentPopupMenu(popupMenu);

        JTableHeader header = analysisTable.getTableHeader();
        header.setDefaultRenderer(new HeaderRenderer(analysisTable));

        JScrollPane analysisList = new JScrollPane(analysisTable);
        analysisPane.add(analysisList);
        analysisTable.setFillsViewportHeight(false);
        analysisList.setOpaque(true);
        analysisPane.setOpaque(true);

        // Results Tab
        JTable resultsTable;
        resultsTable = new JTable(new ResultsModel()); // create table
        JTableHeader resultsHeader = resultsTable.getTableHeader(); // create header and render
        resultsHeader.setDefaultRenderer(new HeaderRenderer(resultsTable));
        resultsModel = (ResultsModel) resultsTable.getModel(); // make resultsModel from Table

        resultsTable.setRowHeight(30);
        resultsTable.setBackground(Color.WHITE);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(10); // file row
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(250); // filename
        resultsTable.getColumnModel().getColumn(1).setCellRenderer( leftRenderer );
        resultsTable.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
        resultsTable.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
        resultsTable.getColumnModel().getColumn(4).setCellRenderer( centerRenderer );
        resultsTable.getColumnModel().getColumn(5).setCellRenderer( centerRenderer );
        resultsTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(10).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(11).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(12).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(13).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(14).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(15).setCellRenderer(centerRenderer);

        JScrollPane resultsList = new JScrollPane(resultsTable);
        resultsPanel.add(resultsList);
        resultsTable.setFillsViewportHeight(false);
        resultsList.setOpaque(true);
        resultsPanel.setOpaque(true);

        dmaxLow = new DoubleValue(37);
        dmaxHigh = new DoubleValue(157);
        dmaxStart = new DoubleValue(97);

        //Pr Table JLabel status, WorkingDirectory cwd, Double lambda
        prTable = new JTable(new PrModel(status, WORKING_DIRECTORY, lambdaBox, dmaxLow, dmaxHigh, dmaxSlider, l1NormCheckBox, cBox, checkBoxDirect, excludeBackgroundInFitCheckBox));

        prModel = (PrModel) prTable.getModel();

        prModel.setBars(mainProgressBar, progressBar1, status, prStatusLabel);
        TableColumnModel pcm = prTable.getColumnModel();

        TableColumn pc = pcm.getColumn(4);
        pc.setCellEditor(new PrSpinnerEditor(prModel, status, qIQCheckBox, lambdaBox, l1NormCheckBox, cBox, checkBoxDirect, excludeBackgroundInFitCheckBox));
        pc = pcm.getColumn(5);
        pc.setCellEditor(new PrSpinnerEditor(prModel, status, qIQCheckBox, lambdaBox, l1NormCheckBox, cBox, checkBoxDirect, excludeBackgroundInFitCheckBox));
        pc = pcm.getColumn(9);
        pc.setCellEditor(new PrSpinnerEditor(prModel, status, qIQCheckBox, lambdaBox, l1NormCheckBox, cBox, checkBoxDirect, excludeBackgroundInFitCheckBox));

        pc = pcm.getColumn(2);
        pc.setCellEditor(new CheckBoxCellEditorRenderer());
        pc.setCellRenderer(new CheckBoxCellEditorRenderer());

        prTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        prTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        prTable.getColumnModel().getColumn(2).setPreferredWidth(35);
        prTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JTableHeader pheader = prTable.getTableHeader();
        pheader.setDefaultRenderer(new HeaderRenderer(prTable));

        //final JScrollPane prList = new JScrollPane(prTable);

        pc = prTable.getColumnModel().getColumn(0);
        pc.setCellEditor(new ColorEditor());
        pc.setCellRenderer(new ColorRenderer(true));

        pc = pcm.getColumn(12);  //Norm
        pc.setCellEditor(new PrButtonEditorRenderer("Norm"));
        pc.setCellRenderer(new PrButtonEditorRenderer("Norm"));
        pc = pcm.getColumn(13);  //Norm
        pc.setCellEditor(new PrButtonEditorRenderer("SEARCH"));
        pc.setCellRenderer(new PrButtonEditorRenderer("SEARCH"));
        pc = pcm.getColumn(14);  //Refine
        pc.setCellEditor(new PrButtonEditorRenderer("Refine"));
        pc.setCellRenderer(new PrButtonEditorRenderer("Refine"));
        pc = pcm.getColumn(15);  //toFile
        pc.setCellEditor(new PrButtonEditorRenderer("2File"));
        pc.setCellRenderer(new PrButtonEditorRenderer("2File"));

        prTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        prTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        prTable.getColumnModel().getColumn(6).setPreferredWidth(170); // I(0)
        prTable.getColumnModel().getColumn(7).setPreferredWidth(160); // Rg

        prTable.getColumnModel().getColumn(10).setPreferredWidth(100); // chi sk2
        prTable.getColumnModel().getColumn(10).setMinWidth(100); // chi sk2
       // prTable.getColumnModel().getColumn(14).setPreferredWidth(70);
       // prTable.getColumnModel().getColumn(12).setPreferredWidth(40);

        prTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // r_ave
        prTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); //
        prTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        prTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        prTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer); // r_ave
        prTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer); // dmax


        prScrollPane.add(prTable);
        prScrollPane.setViewportView(prTable);
        prScrollPane.validate();

        //prPanel.add(prScrollPane); // add pane to panel?,
        prTable.setFillsViewportHeight(false);
        prTable.setRowHeight(32);
        prScrollPane.setOpaque(true);
        prPanel.setOpaque(true);


        // define Singleton plots
        kratky = KratkyPlot.getInstance();
        log10IntensityPlot = PlotDataSingleton.getInstance();
        analysisMiniPlots = MiniPlots.getInstance();
        qIqPlot = QIQPlot.getInstance();
        errorPlot = ErrorPlot.getInstance();
        powerLawPlot = PowerLawPlot.getInstance();

        similarityObject = new Similarity(status, mainProgressBar);

        //normalKratkyRg = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (GUINIER)");
        //normalKratkyRgReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (Real space)");
        normalKratkyVc = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Guinier)");
        normalKratkyVcReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Real space)");

        // create plot, set attribute to true
        // on close plot, set attribute to false
        // only works on singleton classes since only one window is open
        // toggles selected Files in
        dataFilesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                JList list = (JList) event.getSource();
                // Get index of clicked item
                int index = list.locationToIndex(event.getPoint());
                // Toggle selected state
                if (index > -1){
                    DataFileElement item = (DataFileElement) list.getModel().getElementAt(index);
                    // Repaint cell
                    item.setSelected(! item.isSelected());
                    list.repaint(list.getCellBounds(index,index));
                }
            }
        });


        // toggles selected Files in
        buffersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                JList list = (JList) event.getSource();
                // Get index of clicked item
                int index = list.locationToIndex(event.getPoint());
                // Toggle selected state
                if (index > -1){
                    DataFileElement item = (DataFileElement) list.getModel().getElementAt(index);
                    // Repaint cell
                    item.setSelected(! item.isSelected());

                    //Collection inUse = (Collection) collections.get(69); //set dataset in the collection
                    //inUse.getDataset(index).setInUse(item.isSelected());
                    list.repaint(list.getCellBounds(index,index));
                }
            }
        });

        // toggles selected Files in
        samplesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                JList list = (JList) event.getSource();
                // Get index of clicked item
                int index = list.locationToIndex(event.getPoint());
                // Toggle selected state
                if (index > -1){
                    DataFileElement item = (DataFileElement) list.getModel().getElementAt(index);
                    // Repaint cell
                    item.setSelected(!item.isSelected());
                    //Collection inUse = (Collection) collections.get(96); // set dataset in the collection
                    //inUse.getDataset(index).setInUse(item.isSelected());
                    list.repaint(list.getCellBounds(index, index));
                }
            }
        });


        // popup mouse item for samples list
        final JPopupMenu samplesPopupMenu = new JPopupMenu();
        JMenuItem selectItemsSamplesList = new JMenuItem("Select All");
        selectItemsSamplesList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSamplesListSelection(true);
            }
        });
        samplesPopupMenu.add(selectItemsSamplesList);

        // popup mouse item for samples list
        JMenuItem deSelectItemsSamplesList = new JMenuItem("DeSelect All");
        deSelectItemsSamplesList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSamplesListSelection(false);
            }
        });
        samplesPopupMenu.add(deSelectItemsSamplesList);
        samplesList.setComponentPopupMenu(samplesPopupMenu);


        // remove file from Collection/Set
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //also update Analysis Table/Model
                int start = dataFilesList.getModel().getSize() - 1;
                int selected;

                for (int i = 0; i < totalPanels; i++){
                    //find which box is selected
                    if (collectionButtons.get(i).isSelected()) {  // locate active panel
                        selected = i;
                        for(int j = start; j >= 0; j--){ // count in reverse and remove selected dataset
                            DataFileElement m = (DataFileElement) dataFilesList.getModel().getElementAt(j);
                            if (m.isSelected()) {
                                ((Collection)collections.get(selected)).removeDataset(j);
                                analysisModel.remove(j);
                                resultsModel.remove(j);
                            }
                        }

                        //update list on Files tab
                        dataFilesModel.clear();
                        dataFilesList.removeAll();
                        // update dataFilesList in dataFilesPanel;
                        // rebuild dataFilesPanel from collection.get(i)
                        for(int j=0; j<((Collection)collections.get(selected)).getDatasets().size(); j++){
                            String name = ((Collection)collections.get(selected)).getDataset(j).getFileName();
                            dataFilesModel.addElement(new DataFileElement(name, j));
                        }

                        dataFilesList.setModel(dataFilesModel);

                        //update collection ids
                        int total = ((Collection)collections.get(selected)).getDatasets().size();

                        for(int h=0; h<total; h++){
                            ((Collection) collections.get(selected)).getDataset(h).setId(h);
                        }

                        break;
                    }
                }

                //prModel.clear();

                // replot any visible frames
                //if (kratky.frame.isVisible()){
                //    kratkyPlot();
                //}

                // replot any visible frames
                //if (powerLawPlot.frame.isVisible()){
                //    plotPowerLaw();
                //}

                dataFilesList.validate();
                dataFilesList.repaint();
            }



        });

        intensityPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (log10IntensityPlot.isVisible()){
                    log10IntensityPlot.closeWindow();
                }
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
                complexButton.setEnabled(false);
                ComplexPlot complexation = new ComplexPlot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory(), status);
                complexButton.setEnabled(true);
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
                } else {
                    RcXSectionalPlot tempPlot = new RcXSectionalPlot(collectionSelected.getDataset(selected), WORKING_DIRECTORY.getWorkingDirectory());
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
                                cpuCores,
                                collectionSelected,
                                mainProgressBar,
                                status);

                        scaling.scaleNow(0,0);
                        if (log10IntensityPlot.isVisible()){
                            log10IntensityPlot.setNotify(true);
                        }
                        mainProgressBar.setValue(0);
                        mainProgressBar.setStringPainted(false);
                        scaleButton.setEnabled(true);
                    }
                }.start();

            }
        });

        averageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                averageButton.setEnabled(false);
                if (collectionSelected.getTotalSelected() < 2){
                    averageButton.setEnabled(true);
                    status.setText("Select at least two datasets for averaging");
                    return;
                } else {

                    Averager tempAverage = new Averager(collectionSelected);

                    JFileChooser fc = new JFileChooser(WORKING_DIRECTORY.getWorkingDirectory());
                    int option = fc.showSaveDialog(panel1);
                    //set directory to default directory from Settings tab
                    Dataset tempDataset = new Dataset(tempAverage.getAveraged(), tempAverage.getAveragedError(), "averaged", collectionSelected.getDatasetCount(), false);

                    // update notes info

                    tempDataset.setAverageInfo(collectionSelected);

                    int mergedIndex = log10IntensityPlot.addToMerged(tempAverage.getAveraged());

                    if(option == JFileChooser.CANCEL_OPTION) {
                        log10IntensityPlot.removeFromMerged(mergedIndex);
                        averageButton.setEnabled(true);
                        return;
                    }

                    if(option == JFileChooser.APPROVE_OPTION){
                        // remove dataset and write to file
                        log10IntensityPlot.removeFromMerged(mergedIndex);
                        // make merged data show on top of other datasets
                        File theFileToSave = fc.getSelectedFile();

                        String cleaned = cleanUpFileName(fc.getSelectedFile().getName());
                        System.out.println("CLEANED FILE NAME " + cleaned);
                        if(fc.getSelectedFile()!=null){

                            WORKING_DIRECTORY.setWorkingDirectory(fc.getCurrentDirectory().toString());

                            FileObject dataToWrite = new FileObject(fc.getCurrentDirectory(), version);
                            dataToWrite.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                            dataToWrite.writeSAXSFile(cleaned, tempDataset);


                            //close the output stream
                            status.setText(cleaned + ".dat written to "+fc.getCurrentDirectory());

                            collectionSelected.addDataset(tempDataset);
                            collectionSelected.getLast().setColor(Color.red);
                            collectionSelected.getLast().setFileName(cleaned);
                            log10IntensityPlot.addToBase(collectionSelected.getLast());

                            analysisModel.addDataset(collectionSelected.getLast());
                            resultsModel.addDataset(collectionSelected.getLast());

                            int location = dataFilesModel.getSize();
                            dataFilesModel.addElement(new DataFileElement(collectionSelected.getLast().getFileName(), location));
                            analysisModel.fireTableDataChanged();
                            resultsModel.fireTableDataChanged();

                            //Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    averageButton.setEnabled(true);
                }

            }
        });

        medianButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (collectionSelected.getTotalSelected() < 3){
                    status.setText("Select at least three datasets for median");
                } else {
                    medianButton.setEnabled(false);
                    Medianer tempMedian = new Medianer(collectionSelected);

                    JFileChooser fc = new JFileChooser(WORKING_DIRECTORY.getWorkingDirectory());
                    int option = fc.showSaveDialog(panel1);
                    //set directory to default directory from Settings tab
                    Dataset tempDataset = new Dataset(tempMedian.getMedianSet(), tempMedian.getMedianSetError(), "median", collectionSelected.getDatasetCount(), false);

                    // update notes info

                    tempDataset.setMedianInfo(collectionSelected);

                    int mergedIndex = log10IntensityPlot.addToMerged(tempMedian.getMedianSet());

                    if(option == JFileChooser.CANCEL_OPTION) {
                        log10IntensityPlot.removeFromMerged(mergedIndex);
                        medianButton.setEnabled(true);
                        return;
                    }

                    if(option == JFileChooser.APPROVE_OPTION){
                        // remove dataset and write to file
                        log10IntensityPlot.removeFromMerged(mergedIndex);
                        // make merged data show on top of other datasets
                        //File theFileToSave = fc.getSelectedFile();

                        String cleaned = cleanUpFileName(fc.getSelectedFile().getName());

                        if(fc.getSelectedFile()!=null){

                            WORKING_DIRECTORY.setWorkingDirectory(fc.getCurrentDirectory().toString());
                            FileObject dataToWrite = new FileObject(fc.getCurrentDirectory(), version);
                            dataToWrite.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                            dataToWrite.writeSAXSFile(cleaned, tempDataset);

                            //close the output stream
                            status.setText(cleaned + ".dat written to "+fc.getCurrentDirectory());


                            collectionSelected.addDataset(tempDataset);
                            collectionSelected.getLast().setColor(Color.red);
                            collectionSelected.getLast().setFileName(cleaned);
                            log10IntensityPlot.addToBase(collectionSelected.getLast());

                            analysisModel.addDataset(collectionSelected.getLast());
                            resultsModel.addDataset(collectionSelected.getLast());

                            int location = dataFilesModel.getSize();
                            dataFilesModel.addElement(new DataFileElement(collectionSelected.getLast().getFileName(), location));
                            analysisModel.fireTableDataChanged();
                            resultsModel.fireTableDataChanged();

                            //Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    medianButton.setEnabled(true);
                }
            }
        });

        singleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (collectionSelected.getTotalSelected() > 1){
                    status.setText("Select only one file!");
                    return;
                }

                Dataset tempData = collectionSelected.getDataset(collectionSelected.getSelected());

                singleButton.setEnabled(false);

                JFileChooser fc = new JFileChooser(WORKING_DIRECTORY.getWorkingDirectory());
                int option = fc.showSaveDialog(panel1);
                //set directory to default directory from Settings tab

                if(option == JFileChooser.CANCEL_OPTION) {
                    singleButton.setEnabled(true);
                    return;
                }

                if(option == JFileChooser.APPROVE_OPTION){
                    // remove dataset and write to file
                    // make merged data show on top of other datasets
                    File theFileToSave = fc.getSelectedFile();

                    String cleaned = cleanUpFileName(fc.getSelectedFile().getName());

                    if(fc.getSelectedFile()!=null){

                        WORKING_DIRECTORY.setWorkingDirectory(fc.getCurrentDirectory().toString());
                        System.out.println("WORKING DIRECTORY SET TO : " + WORKING_DIRECTORY.getWorkingDirectory());

                        FileObject dataToWrite = new FileObject(fc.getCurrentDirectory(), version);
                        dataToWrite.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                        dataToWrite.writeSingleSAXSFile(cleaned, tempData);

                        //close the output stream
                        status.setText(cleaned + ".dat written to " + fc.getCurrentDirectory());

                        //Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                singleButton.setEnabled(true);
            }
        });

        scaleToIZeroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaleToIZeroButton.setEnabled(false);
                if (collectionSelected.getTotalSelected() < 2){
                    status.setText("Select more than one file!");
                    return;
                }

                int ref=0;
                for(int i=0; i<collectionSelected.getDatasets().size(); i++){
                    if (collectionSelected.getDataset(i).getInUse()){
                        ref = i;
                    }
                }

                collectionSelected.getDataset(ref).setScaleFactor(1.00d);
                mainRescaling(ref);

                double referenceScale = collectionSelected.getDataset(ref).getGuinierIzero();
                double newScale;
                for(int i=0; i<collectionSelected.getDatasetCount(); i++){
                    Dataset tempData = collectionSelected.getDataset(i);
                    if (tempData.getInUse() && i != ref){
                        newScale = referenceScale/tempData.getGuinierIzero();
                        tempData.setScaleFactor(newScale);
                        mainRescaling(i);
                    }
                }

                scaleToIZeroButton.setEnabled(true);
            }
        });

        scaleMergeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scaleMergeButton.setEnabled(false);

                // scale the data, then merge
                if (log10IntensityPlot.isVisible()){
                    log10IntensityPlot.setNotify(false);
                }
                ScaleManager scaling = new ScaleManager(
                        cpuCores,
                        collectionSelected,
                        mainProgressBar,
                        status);

                scaling.scaleNow(0.01,0.15);
                if (log10IntensityPlot.isVisible()){
                    log10IntensityPlot.setNotify(true);
                }

                // merge by averaging?
                Averager tempAverage = new Averager(collectionSelected);

                JFileChooser fc = new JFileChooser(WORKING_DIRECTORY.getWorkingDirectory());
                int option = fc.showSaveDialog(panel1);
                //set directory to default directory from Settings tab
                Dataset tempDataset = new Dataset(tempAverage.getAveraged(), tempAverage.getAveragedError(), "averaged", collectionSelected.getDatasetCount(), false);

                // update notes info

                tempDataset.setAverageInfo(collectionSelected);

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

                        WORKING_DIRECTORY.setWorkingDirectory(fc.getCurrentDirectory().toString());

                        FileObject dataToWrite = new FileObject(fc.getCurrentDirectory(), version);
                        dataToWrite.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                        dataToWrite.writeSAXSFile(cleaned, tempDataset);

                        //close the output stream
                        status.setText(cleaned + ".dat written to "+fc.getCurrentDirectory());

                        collectionSelected.addDataset(tempDataset);
                        collectionSelected.getLast().setColor(Color.red);
                        collectionSelected.getLast().setFileName(cleaned);
                        log10IntensityPlot.addToBase(collectionSelected.getLast());

                        analysisModel.addDataset(collectionSelected.getLast());
                        resultsModel.addDataset(collectionSelected.getLast());

                        int location = dataFilesModel.getSize();
                        dataFilesModel.addElement(new DataFileElement(collectionSelected.getLast().getFileName(), location));
                        analysisModel.fireTableDataChanged();
                        resultsModel.fireTableDataChanged();

                        //Logger.getLogger(Scatter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                scaleMergeButton.setEnabled(true);
            }
        });

        izeroRgPlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DoubleXYPlot izeroRgPlot = new DoubleXYPlot(WORKING_DIRECTORY.getWorkingDirectory());
                izeroRgPlot.makePlot(collectionSelected);
            }
        });



        plotMedianAndAverageSampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                plotMedianAndAverageSampleButton.setEnabled(false);
                Collection tempCollection = (Collection) collections.get(96);
                tempCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                int total = sampleFilesModel.getSize();
                int select=0;
                for(int i=0;i<total; i++){
                    if (sampleFilesModel.get(i).isSelected()){
                        tempCollection.getDataset(i).setInUse(true);
                        select++;
                    } else {
                        tempCollection.getDataset(i).setInUse(false);
                    }
                }

                if (select < 2){
                    status.setText("Too few datafiles");
                    return;
                }

                BuffersSamplesPlot tempSamples = new BuffersSamplesPlot((Collection) collections.get(96));
                try {
                    tempSamples.makePlot(false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                plotMedianAndAverageSampleButton.setEnabled(true);
            }
        });

        setOutputDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Set working directory
                File theCWD = new File(OUTPUT_DIR_SUBTRACTION_NAME);

                JFileChooser chooser = new JFileChooser(theCWD);
                chooser.setDialogTitle("Select Directory");

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION){

                    if (chooser.getSelectedFile().isDirectory()){
                        OUTPUT_DIR_SUBTRACTION_NAME = chooser.getSelectedFile().toString();
                        System.out.println("Selected: " + chooser.getSelectedFile().toString());
                    } else {
                        OUTPUT_DIR_SUBTRACTION_NAME = chooser.getCurrentDirectory().toString();
                        System.out.println("Not: " + chooser.getSelectedFile().toString());
                    }

                    subtractOutPutDirectoryLabel.setText(OUTPUT_DIR_SUBTRACTION_NAME);
                    updateProp();
                }
            }
        });

        plotLog10AverageSampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plotLog10AverageSampleButton.setEnabled(false);
                Collection tempCollection = (Collection) collections.get(96);
                tempCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                int total = sampleFilesModel.getSize();
                int select=0;
                for(int i=0;i<total; i++){
                    if (sampleFilesModel.get(i).isSelected()){
                        tempCollection.getDataset(i).setInUse(true);
                        select++;
                    } else {
                        tempCollection.getDataset(i).setInUse(false);
                    }
                }

                if (select < 2){
                    status.setText("Too few datafiles");
                    return;
                }

                BuffersSamplesPlot tempSamples = new BuffersSamplesPlot((Collection) collections.get(96));
                try {
                    tempSamples.makePlot(true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                plotLog10AverageSampleButton.setEnabled(true);
            }
        });


        signalPlotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Collection sampleCollection = (Collection) collections.get(96);
                Collection bufferCollection = (Collection) collections.get(69);
                sampleCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());
                bufferCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                int total = sampleFilesModel.getSize();
                int selectS=0;

                for(int i=0;i<total; i++){
                    if (sampleFilesModel.get(i).isSelected()){
                        sampleCollection.getDataset(i).setInUse(true);
                        selectS++;
                    } else {
                        sampleCollection.getDataset(i).setInUse(false);
                    }
                }

                System.out.println("FIRST INDEX " + firstIndexInSignalPlot);
                System.out.println(" LAST INDEX " + lastIndexInSignalPlot);

                if (lastIndexInSignalPlot <= 0){
                    System.out.println("FIRST INDEX " + firstIndexInSignalPlot);
                    System.out.println(" LAST INDEX " + lastIndexInSignalPlot);
                    lastIndexInSignalPlot = total;
                } else {
                    if (firstIndexInSignalPlot < lastIndexInSignalPlot && lastIndexInSignalPlot < total){

                    }
                }


                int totalBuffers = bufferFilesModel.getSize();
                int selectB=0;
                for(int i=0;i<totalBuffers; i++){
                    if (bufferFilesModel.get(i).isSelected()){
                        bufferCollection.getDataset(i).setInUse(true);
                        selectB++;
                    } else {
                        bufferCollection.getDataset(i).setInUse(false);
                    }
                }

                SignalPlot tempSignalPlot;
                if (selectB < 1 && selectS > 3){
                    // if no buffers, assuming subtracted and do q*Iq plot
                    tempSignalPlot = new SignalPlot(sampleCollection, status, addRgToSignalCheckBox.isSelected(), mainProgressBar, Double.parseDouble(thresholdField.getText()));
                    //return;
                } else {
                    tempSignalPlot = new SignalPlot(sampleCollection, bufferCollection, status, addRgToSignalCheckBox.isSelected(), mainProgressBar, Double.parseDouble(thresholdField.getText()));
                }

                tempSignalPlot.setFirstLastFrame(firstIndexInSignalPlot, lastIndexInSignalPlot);

                tempSignalPlot.setSampleJList(samplesList);
                tempSignalPlot.setPointsToExclude(Integer.valueOf((String)excludeComboBox.getSelectedItem()));

                Thread temp1 = new Thread(tempSignalPlot);
                temp1.start();
            }
        });



        clearSamplesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((Collection)collections.get(96)).removeAllDatasets();
                status.setText("Cleared");
                sampleFilesModel.clear();
                //samplesList.removeAll();

                if (clearALL){
                    ((Collection)collections.get(69)).removeAllDatasets();
                 bufferFilesModel.clear();
                }
                // reset range
                RESETRANGEButton.doClick();
            }
        });


        SVDAverageFilesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SVDAverageFilesCheckBox.isSelected()) {
                    averageSampleFileCheckBox.setSelected(true);
                    scaleThenMergeCheckBox.setSelected(true);
                    subtractFromMedianCheckBox.setSelected(false);
                }
            }
        });

        subtractFromMedianCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                averageSampleFileCheckBox.setSelected(false);
                SVDAverageFilesCheckBox.setSelected(false);
            }
        });

        plotAverageAndMedianBuffers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plotAverageAndMedianBuffers.setEnabled(false);
                Collection tempCollection = (Collection) collections.get(69);

                tempCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                int total = bufferFilesModel.getSize();
                int select=0;
                for(int i=0;i<total; i++){
                    if (bufferFilesModel.get(i).isSelected()){
                        tempCollection.getDataset(i).setInUse(true);
                        select++;
                    } else {
                        tempCollection.getDataset(i).setInUse(false);
                    }
                }

                if (select < 2){
                    status.setText("Too few datafiles");
                    return;
                }

                BuffersSamplesPlot tempSamples = new BuffersSamplesPlot((Collection) collections.get(69));
                try {
                    tempSamples.makePlot(false);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                plotAverageAndMedianBuffers.setEnabled(true);
            }
        });


        buffersClearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Collection)collections.get(69)).removeAllDatasets();
                // equivalent to bufferCollections.removeAllDatasets();
                status.setText("Cleared");
                bufferFilesModel.clear();
                //buffersList.removeAll();
                if (clearALL){
                    ((Collection)collections.get(96)).removeAllDatasets();
                    sampleFilesModel.clear();;
                }
            }
        });


        plotLog10AverageBufferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plotLog10AverageBufferButton.setEnabled(false);
                Collection tempCollection = (Collection) collections.get(69);
                tempCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                int total = bufferFilesModel.getSize();
                int select=0;
                for(int i=0;i<total; i++){ // update dataset to reflect checkboxes
                    if (bufferFilesModel.get(i).isSelected()){
                        tempCollection.getDataset(i).setInUse(true);
                        select++;
                    } else {
                        tempCollection.getDataset(i).setInUse(false);
                    }
                }

                if (select < 2){
                    status.setText("Too few datafiles");
                    return;
                }

                BuffersSamplesPlot tempSamples = new BuffersSamplesPlot((Collection) collections.get(69));
                try {
                    tempSamples.makePlot(true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                plotLog10AverageBufferButton.setEnabled(true);
            }
        });

        subtractFromSelectedBufferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                double qmin=0;
                double qmax=0;
                ((Collection)collections.get(69)).setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());
                ((Collection)collections.get(96)).setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                if (!isNumber(qminSubtractionField.getText()) || !isNumber(qmaxSubtractionField.getText())){
                    status.setText("q-range (qmin, qmax) is not a number ");
                    return;
                } else {
                    qmin = Double.parseDouble(qminSubtractionField.getText());
                    qmax = Double.parseDouble(qmaxSubtractionField.getText());
                    if (qmin > qmax){
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane optionPane = new JOptionPane("q-range (qmin > qmax) invalid range",JOptionPane.WARNING_MESSAGE);
                        JDialog dialog = optionPane.createDialog("Warning!");
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);

                        status.setText("q-range (qmin > qmax) invalid range ");
                        return;
                    }
                }

                final boolean mergeByAverage = averageSampleFileCheckBox.isSelected();
                boolean singles = false;
                if (!mergeByAverage && !subtractFromMedianCheckBox.isSelected()){
                    singles = true;
                }

                if (subtractionFileNameField.getText().length() < 3 && !singles){
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane optionPane = new JOptionPane("Provide a meaningful name",JOptionPane.WARNING_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Warning!");
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                    return;
                }

                // launch in separate thread
                final double finalQmin = qmin;
                final double finalQmax = qmax;

                final boolean scaleBefore = scaleThenMergeCheckBox.isSelected();
                final boolean svd = SVDAverageFilesCheckBox.isSelected();
                final boolean finalSingles = singles;

                Collection tempCollectionForSubtraction = new Collection();

                int total = sampleCollections.getDatasetCount();

                for (int i = 0; i < total; i++) {
                    if (sampleCollections.getDataset(i).getInUse()) {
                        //int newIndex = subtractedCollection.getDatasetCount();
                        tempCollectionForSubtraction.addDataset( new Dataset(sampleCollections.getDataset(i)));
                    }
                }

                new Thread() {
                    public void run() {
                        //Collection buffers, Collection samples, double tqmin, double tqmax, boolean mergeByAverage,  boolean scaleBefore, boolean svd, int cpus, JLabel status, final JProgressBar bar){
                        Subtraction subTemp = new Subtraction(bufferCollections, tempCollectionForSubtraction, finalQmin, finalQmax, mergeByAverage, finalSingles, scaleBefore, svd, cpuCores, status, mainProgressBar, version);
                        // add other attributes and then run
                        // Double.parseDouble(comboBoxSubtractBins.getSelectedItem().toString())/100.00;
                        subTemp.setBinsAndCutoff(Double.parseDouble(comboBoxSubtractBins.getSelectedItem().toString()), Double.parseDouble(subtractionCutOff.getSelectedItem().toString()));
                        subTemp.setNameAndDirectory(subtractionFileNameField.getText(), subtractOutPutDirectoryLabel.getText());
                        subTemp.setCollectionToUpdate(collectionSelected);

                        Thread temp1 = new Thread(subTemp);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // add dataset to collectionSelected

                        updateActiveModels();
                        int id = collectionSelected.getPanelID();
                        miniPlots.get(id).chart.setNotify(false);
                        miniPlots.get(id).frame.removeAll();
                        miniPlots.get(id).chart.setNotify(false);
                        miniPlots.get(id).plot(collectionSelected);
                        minis.get(id).add(miniPlots.get(id).frame.getChartPanel());
                        miniPlots.get(id).chart.setNotify(true);
                    }
                }.start();
            }
        });
        radioButtonLoad1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchCollection(0);
            }
        });
        radioButtonLoad2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchCollection(1);
            }
        });
        radioButtonLoad3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchCollection(2);
            }
        });
        radioButtonLoad4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchCollection(3);
            }
        });

        clearButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCollection(0);
            }
        });

        clearButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCollection(1);
            }
        });

        clearButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCollection(2);
            }
        });

        clearButton4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearCollection(3);
            }
        });


        calculateSimlarityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int total = similarityFilesModel.getSize();
                System.out.println("Total Dropped files");
                Collection tempCollection = new Collection();
                for (int i=0; i< total; i++){
                    System.out.println(i + " " + similarityFilesModel.get(i).getCollection_id());
                    Dataset dataset = similarityCollection.getDataset(similarityFilesModel.get(i).getCollection_id());
                    dataset.setId(i);
                    tempCollection.addDataset(dataset);
                }

                checkQminQmax(qminSimilarityField.getText(), qmaxSimilarityField.getText());

                final double qminFinal = Double.parseDouble(qminSimilarityField.getText());
                final double qmaxFinal = Double.parseDouble(qmaxSimilarityField.getText());
                System.out.println("selected box " + simBinsComboBox.getSelectedItem().toString());
                final double binsFinal = Double.parseDouble(simBinsComboBox.getSelectedItem().toString());

                new Thread() {
                    public void run() {
                        //Collection collection, double qmin, double qmax, double bins, int cpus, JLabel status, final JProgressBar bar
                        calculateSimlarityButton.setEnabled(false);
                        similarityObject.setParameters(qminFinal, qmaxFinal, binsFinal, cpuCores, simScatPlotPanel);
                        similarityObject.setDirectory(WORKING_DIRECTORY.getWorkingDirectory());
                        //Similarity simTemp = new Similarity(similarityCollection, qminFinal, qmaxFinal, binsFinal, cpuCores, status, mainProgressBar);
                        // add other attributes and then run
                        // Double.parseDouble(comboBoxSubtractBins.getSelectedItem().toString())/100.00;
                        similarityObject.setFunction(excessKurtosisCheckBox.isSelected(), volatilityVRCheckBox.isSelected(), shapiroWilkCheckBox.isSelected());
                        //simTemp.setFunction(excessKurtosisCheckBox.isSelected(), volatilityVRCheckBox.isSelected());

                        //Thread temp1 = new Thread(simTemp);
                        Thread temp1 = new Thread(similarityObject);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        calculateSimlarityButton.setEnabled(true);
                    }
                }.start();



            }
        });

        excessKurtosisCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (excessKurtosisCheckBox.isSelected()){
                    volatilityVRCheckBox.setSelected(false);
                } else {
                    volatilityVRCheckBox.setSelected(true);
                }

            }
        });

        clearSimButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (similarityObject.getTotalItemsInCollection() > 0){
                    similarityCollection.removeAllDatasets();
                    similarityObject.clearAll();
                    similarityFilesModel.clear();
                    similarityList.removeAll();
                    nameOfSetTextField.setText("name of set");
                }
            }
        });

        addSetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (nameOfSetTextField.getText().matches("name of set") || nameOfSetTextField.getText().length() < 3){
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane optionPane = new JOptionPane("Must Provide a Meaningful name for the dataset",JOptionPane.WARNING_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Warning!");
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                    status.setText("name the set  ");
                    return;
                }

                // create a new collection container to hold all the data files
                similarityObject.addCollection(nameOfSetTextField.getText());

                int total = similarityCollection.getDatasetCount();
                int index = similarityObject.getTotalItemsInCollection() - 1; // get last Collection in Object


                for (int i=0; i<total; i++){
                    similarityObject.getCollectionItemByIndex(index).addDataset(new Dataset(similarityCollection.getDataset(i)));
                    similarityObject.getCollectionItemByIndex(index).getDataset(i).setInUse(similarityFilesModel.get(i).isSelected());
                }
                // clear list
                similarityCollection.removeAllDatasets();
                similarityFilesModel.clear();
                similarityList.removeAll();
                nameOfSetTextField.setText("name of set");
                simLabel.setText("ADDED SET: " + similarityObject.getCollectionItemByIndex(index).getName());
            }
        });

        nameOfSetTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                nameOfSetTextField.setText("");
            }
        });

        SELECTSetsToAnalyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (similarityObject.getTotalItemsInCollection() > 0){
                    SimMenuItem tempSim = new SimMenuItem(similarityObject);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane optionPane = new JOptionPane("No Sets to Edit",JOptionPane.WARNING_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Warning!");
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                    return;
                }
            }
        });

        clearAboveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (similarityCollection.getDatasetCount() > 0){
                    similarityCollection.removeAllDatasets();
                    similarityFilesModel.clear();
                    similarityList.removeAll();
                    nameOfSetTextField.setText("name of set");
                }
            }
        });

        workingDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeCWD();
            }
        });


        excessKurtosisCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volatilityVRCheckBox.setSelected(!(excessKurtosisCheckBox.isSelected()));
                shapiroWilkCheckBox.setSelected(!(excessKurtosisCheckBox.isSelected()));
            }
        });

        volatilityVRCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excessKurtosisCheckBox.setSelected(!(volatilityVRCheckBox.isSelected()));
                shapiroWilkCheckBox.setSelected(!(volatilityVRCheckBox.isSelected()));
            }
        });

        shapiroWilkCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                excessKurtosisCheckBox.setSelected(!(shapiroWilkCheckBox.isSelected()));
                volatilityVRCheckBox.setSelected(!(shapiroWilkCheckBox.isSelected()));
            }
        });

        realSpaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // add to PrModel
                // create list for the PrModel?
                if (collectionSelected.getDatasetCount() == 0){
                    Toolkit.getDefaultToolkit().beep();
                    status.setText("No data to use");
                    return;
                }
                // reset box before making plot
                qIQCheckBox.setSelected(false);

                prTable.clearSelection();
                if (prTable.isEditing()){
                    prTable.getCellEditor().stopCellEditing();
                    prTable.validate();
                }


                for(int i=0; i<collectionSelected.getDatasetCount(); i++){
                    if (collectionSelected.getDataset(i).getInUse()){
                        collectionSelected.getDataset(i).getRealSpaceModel().setSelected(true);
                    } else {
                        collectionSelected.getDataset(i).getRealSpaceModel().setSelected(false);
                    }
                }


                prModel.clear();
                prModel.addDatasetsFromCollection(collectionSelected);
                prModel.fireTableDataChanged();
                mainPane.setSelectedIndex(2);


                IofQPofRPlot iofqPofRplot = IofQPofRPlot.getInstance();
                iofqPofRplot.clear();
                iofqPofRplot.plot(collectionSelected, WORKING_DIRECTORY, prIntensity, qIQCheckBox.isSelected());

                PofRPlot pofRplot = PofRPlot.getInstance();
                pofRplot.clear();
                pofRplot.plot(collectionSelected, WORKING_DIRECTORY, prDistribution);
            }
        });

        qIQCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IofQPofRPlot iofqPofRplot = IofQPofRPlot.getInstance();
                if (qIQCheckBox.isSelected()){
                    iofqPofRplot.changeToQIQ();
                } else {
                    iofqPofRplot.changeToIQ();
                }
            }
        });


        dmaxSlider.setValue((int)dmaxStart.getValue());
        dmaxLabel.setText( String.valueOf((int)(dmaxStart.getValue())) );

        dmaxSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                dmaxLabel.setText(Integer.toString(dmaxSlider.getValue()));
                dmaxStart.setValue(dmaxSlider.getValue());
            }
        });

        atsasDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Set working directory
                File theCWD = new File(System.getProperty("user.dir"));
                JFileChooser chooser = new JFileChooser(theCWD);
                chooser.setDialogTitle("Select Directory");

                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION){
                    if (chooser.getSelectedFile().isDirectory()){
                        ATSAS_DIRECTORY = chooser.getSelectedFile().toString();
                    } else {
                        ATSAS_DIRECTORY = chooser.getCurrentDirectory().toString();
                    }
                    atsasDirLabel.setText(ATSAS_DIRECTORY);
                    updateProp();
                }
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(){
                    public void run() {
                        String filename = damminLabel.getText();
                        String damstartFile = damstartLabel.getText();
                        boolean dammin = damminRadioButton.isSelected();
                        boolean fast = fastRadioButton.isSelected();

                        DammiNFManager tempModeling = new DammiNFManager( Integer.valueOf(cpuBox.getSelectedItem().toString()),
                                filename,
                                dammin,
                                Integer.valueOf(runsComboBox.getSelectedItem().toString()),
                                symmetryBox.getSelectedItem().toString(),
                                ATSAS_DIRECTORY,
                                WORKING_DIRECTORY.getWorkingDirectory(),
                                damminfModelsModel, fast, damstartFile, damstartStatus, damRefineCheckBox.isSelected());

                        if (alignPDBModelRunsCheckBox.isSelected() && supcombFile.exists()){
                            tempModeling.setSupcombFile(supcombFile);
                        }

                        tempModeling.modelNow(damTextPane, info);

                    }
                }.start();
            }
        });

        damstartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Set GNOM out file
                File theCWD = new File(WORKING_DIRECTORY.getWorkingDirectory());
                JFileChooser chooser = new JFileChooser(theCWD);
                chooser.setDialogTitle("Select File");

                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION){

                    damstartLabel.setText(chooser.getSelectedFile().toString());
                    damstartStatus = true;;
                    int test = chooser.getSelectedFile().toString().indexOf("damstart.pdb");

                    if (test < 0){
                        damRefineCheckBox.setSelected(false);
                        damstartLabel.setText("Incorrect File");
                        status.setText("Improper damstart file, must be damstart.pdb");
                        damstartStatus = false;
                    }
                }
            }
        });

        selectPDBDamminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File theCWD = new File(WORKING_DIRECTORY.getWorkingDirectory());
                JFileChooser chooser = new JFileChooser(theCWD);
                chooser.setDialogTitle("Select File");

                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setAcceptAllFileFilterUsed(false);

                supcombLabel.setFont(new Font("Arial", Font.BOLD, 12));

                if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION) {

                    int test = chooser.getSelectedFile().toString().indexOf(".pdb");

                    if (test < 0) {
                        status.setText("Improper PDB extension");
                    } else {
                        // create PofR file for plotting
                        try {
                            supcombLabel.setText(chooser.getSelectedFile().getName());
                            supcombFile = new File(chooser.getSelectedFile().getAbsolutePath());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });



        runSphereModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runSphereModelButton.setEnabled(false);
                status.setText("Calculating Modelings please wait");
                float qmin = Float.parseFloat(qminCEText.getText());
                float qmax = Float.parseFloat(qmaxCEText.getText());
                float solventContrast = Float.parseFloat(solventContrastText.getText());

                Dataset tempData = (Dataset)ffCEFileSelectionComboBox.getSelectedItem();
                double[] lowerParams = new double[1];
                lowerParams[0] = Double.parseDouble(lowerSphereRadius.getText());
                double[] upperParams = new double[1];
                upperParams[0] = Double.parseDouble(upperSphereRadius.getText());
                double[] delta = new double[1];
                // delta can be calculated from delta-q and dmax
                //delta[0] = Math.PI/qmax/4.0;
                delta[0] = 2.373;
                double[] contrasts = new double[1];
                contrasts[0] = Float.parseFloat(particleContrastText.getText());

                double percentNtoKeep = Double.parseDouble((String)topNComboBox.getSelectedItem())/100.0;
                double lambda = Double.parseDouble((String) lambdaCEcomboBox.getSelectedItem());
                int trialsPerRound = Integer.parseInt((String)trialsPerRoundComboBox.getSelectedItem());

                int topN = (int)Math.ceil(percentNtoKeep*trialsPerRound);

                Thread findIt = new Thread(){
                    public void run() {

                        final FormFactorEngine sphere = new FormFactorEngine(tempData,
                                ModelType.SPHERICAL,
                                cpuCores,           // total cpu cores to use during search
                                Integer.valueOf((String)ceComboBoxRounds.getSelectedItem()),           // total rounds of selection (hard stop)
                                lowerParams, // lower radius starting value
                                upperParams, // upper
                                delta,       // incremement to go from lower -> upper limits of params
                                qmin,        // qmin of fit
                                qmax,        // qmax of fit
                                0.0001,      // epsilon
                                lambda, //
                                topN,        // top N to select in out of M
                                solventContrast,
                                contrasts,
                                trialsPerRound,  // trials per round
                                Integer.parseInt((String)modelsPerRoundComboBox.getSelectedItem()),
                                !fitBackgroundCheckBox.isSelected(),
                                maximumEntropyRadioButton.isSelected()
                        );   // randomModelsPerTrial

                        sphere.setProgressBar(progressBarSphere);
                        sphere.setPlotPanels(sphereResidualsPanel, sphereDistributionPanel, leftSpherePanel, rightSpherePanel, sphereDataPlotPanel);
                        sphere.execute();
                        try {
                            sphere.get();
                            runSphereModelButton.setEnabled(true);
                            status.setText("Finished");
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }

                };
                findIt.start();
            }
        });

        collectionSelected.addPropertyChangeListener(new JComboBoxCEListener());

        ffCEFileSelectionComboBox.setModel(new CEComboBoxModel(collectionSelected));
        ffCEFileSelectionComboBox.setRenderer(new CECellRenderer());

        ffCEFileSelectionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                System.out.println("dataset " + ((Dataset)cb.getSelectedItem()).getFileName());
                Dataset tempDataset = (Dataset) cb.getSelectedItem();

                if (qminCEText.getText().length() > 0){
                    //double startq = tempDataset.getOriginalPositiveOnlyData().getX(tempDataset.getStart()).doubleValue();
                    double currentqMin = Double.parseDouble(qminCEText.getText());
                    double currentqMax = Double.parseDouble(qmaxCEText.getText());

                    if (currentqMin > tempDataset.getAllData().getMinX()){
                        qminCEText.setText(String.valueOf(tempDataset.getAllData().getMinX()));
                    }

                    if (currentqMax < tempDataset.getAllData().getMaxX()){
                        qmaxCEText.setText(String.valueOf(tempDataset.getAllData().getMaxX()));
                    }
                } else {
                    qminCEText.setText(String.valueOf(tempDataset.getOriginalPositiveOnlyData().getX(tempDataset.getStart()-1).doubleValue()));
                    if (tempDataset.getAllData().getMaxX() > 0.08){
                        qmaxCEText.setText("0.08");
                    } else {
                        qmaxCEText.setText(String.valueOf(tempDataset.getAllData().getMaxX()));
                    }
                }
            }
        });

        minorAxisLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // check values
                double minor = Double.parseDouble(minorAxisLabel.getText());
                double major = Double.parseDouble(majorAxisLabel.getText());
                if (minor > major ){
                    Toolkit.getDefaultToolkit().beep();
                    status.setText("Minor Axis cannot be greater than Major ");
                    double temp = major - (major)*0.5 - 1;
                    minorAxisLabel.setText(String.valueOf(temp));
                } else {
                    minorAxisLabel.setText(String.valueOf(Math.floor(minor)));
                }
            }
        });

        majorAxisLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // check values
                double minor = Double.parseDouble(minorAxisLabel.getText());
                double major = Double.parseDouble(majorAxisLabel.getText());
                if (minor > major){
                    Toolkit.getDefaultToolkit().beep();
                    status.setText("Major Axis cannot be greater than Major ");
                    double temp = minor + 60;
                    majorAxisLabel.setText(String.valueOf(temp));
                } else {
                    majorAxisLabel.setText(String.valueOf(Math.ceil(major)));
                }
            }
        });

        qmaxCEText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = ((JTextField)e.getSource()).getText();
                double value = Double.parseDouble(text);
                Dataset tempDataset = (Dataset) ffCEFileSelectionComboBox.getSelectedItem();

                if (value > tempDataset.getAllData().getMaxX()){
                    Toolkit.getDefaultToolkit().beep();
                    status.setText("Resetting to q-max of selected dataset ");
                    ((JTextField)e.getSource()).setText(String.valueOf(tempDataset.getAllData().getMaxX()));
                }
            }
        });



        ellipsoidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ellipsoidButton.setEnabled(false);
                float qmin = Float.parseFloat(qminCEText.getText());
                float qmax = Float.parseFloat(qmaxCEText.getText());
                float solventContrast = Float.parseFloat(ellipsoidSolventText.getText());

                double[] lowerParams = new double[1];
                lowerParams[0] = Double.parseDouble(minorAxisLabel.getText());
                double[] upperParams = new double[1];
                upperParams[0] = Double.parseDouble(majorAxisLabel.getText());
                double[] delta = new double[1];
                // delta can be calculated from delta-q and dmax
                delta[0] = 2.393;

                double[] contrasts = new double[1];
                contrasts[0] = Float.parseFloat(particleContrastText.getText());

                double percentNtoKeep = Double.parseDouble((String)topNComboBox.getSelectedItem())/100.0;
                double percentData = Double.parseDouble((String) lambdaCEcomboBox.getSelectedItem());
                int trialsPerRound = Integer.parseInt((String)trialsPerRoundComboBox.getSelectedItem());

                int topN = (int)Math.ceil(percentNtoKeep*trialsPerRound);

                Dataset tempData = (Dataset)ffCEFileSelectionComboBox.getSelectedItem();

                ModelType modelType;
                int selectedModelIndex = ellipseComboBox1.getSelectedIndex();
                if (selectedModelIndex==0){
                    modelType = ModelType.PROLATE_ELLIPSOID;
                } else if (selectedModelIndex==1){
                    modelType = ModelType.OBLATE_ELLIPSOID;
                } else {
                    modelType = ModelType.ELLIPSOID;
                }

                Thread findIt = new Thread(){
                    public void run() {

                        final FormFactorEngine ellipsoid = new FormFactorEngine(tempData,
                                modelType,
                                cpuCores,           // total cpu cores to use during search
                                Integer.valueOf((String)ceComboBoxRounds.getSelectedItem()),           // total rounds of selection (hard stop)
                                lowerParams, // lower radius starting value
                                upperParams, // upper
                                delta,       // incremement to go from lower -> upper limits of params
                                qmin,        // qmin of fit
                                qmax,        // qmax of fit
                                0.0001,      // epsilon
                                percentData, //
                                topN,        // top N to select in out of M
                                solventContrast,
                                contrasts,
                                trialsPerRound,  // trials per round
                                Integer.parseInt((String)modelsPerRoundComboBox.getSelectedItem()), // randomModelsPerTrial
                                !fitBackgroundCheckBox.isSelected(), // background
                                maximumEntropyRadioButton.isSelected()
                        );      // useNoBackground

                        ellipsoid.setProgressBar(progressBarEllipsoid);

                        ellipsoid.setEllipsoidPlotPanels(
                                crossSection1Panel,
                                crossSection2Panel,
                                crossSection3Panel,
                                crossSection4Panel,
                                ellipsoidResidualPanel,
                                histogramRaPanel,
                                histogramRbPanel,
                                histogramRcPanel,
                                scoreProgressEllipsePanel,
                                ellipseDataPanel);
                        ellipsoid.execute();
                        try {
                            ellipsoid.get();
                            ellipsoidButton.setEnabled(true);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                };

                findIt.start();

            }
        });

        packageItButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (collectionSelected.getTotalSelected() < 1){
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane optionPane = new JOptionPane("Need at least one file selected",JOptionPane.WARNING_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Warning!");
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                    status.setText("Select More than one file to make zip ");
                    return;
                }

                PackageIt packageIt = new PackageIt(collectionSelected, WORKING_DIRECTORY);
                packageIt.pack();
                packageIt.setVisible(true);

            }
        });

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                status.setText("Writing txt file to " + WORKING_DIRECTORY.getWorkingDirectory());
                FileObject tempObject = new FileObject(new File(WORKING_DIRECTORY.getWorkingDirectory()), version);
                tempObject.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                tempObject.exportResults(collectionSelected);
            }
        });


        changeCWDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeCWD();
            }
        });



        pdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

//                AutoMerge temp = new AutoMerge(collectionSelected, 3);
//                temp.run();

//                Thread makeIt = new Thread(){
//                    public void run() {

                        //final PDFBuilder pdfBuilder = new PDFBuilder(collectionSelected, WORKING_DIRECTORY, mainProgressBar);
                        //pdfBuilder.setName("this is a name");
                        //pdfBuilder.execute();

//                    }

//                };

  //              makeIt.start();

            }
        });

        GNOMOutFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Set GNOM out file
                File theCWD = new File(WORKING_DIRECTORY.getWorkingDirectory());
                JFileChooser chooser = new JFileChooser(theCWD);
                chooser.setDialogTitle("Select File");

                chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION){
                    damminLabel.setText(chooser.getSelectedFile().toString());
                }
            }
        });

        TRACEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Collection sampleCollection = (Collection) collections.get(96);
                Collection bufferCollection = new Collection();
                sampleCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());
                bufferCollection.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                int total = sampleFilesModel.getSize();
                int selectS=0;
                for(int i=0;i<total; i++){
                    if (sampleFilesModel.get(i).isSelected()){
                        sampleCollection.getDataset(i).setInUse(true);
                        selectS++;
                    } else {
                        sampleCollection.getDataset(i).setInUse(false);
                    }
                }

                if (selectS < 1){
                    return;
                }

                SignalPlot tempSignalPlot = new SignalPlot(sampleCollection, bufferCollection, status, addRgToSignalCheckBox.isSelected(), mainProgressBar, Double.parseDouble(thresholdField.getText()));
                tempSignalPlot.setSampleJList(samplesList);

                Thread temp1 = new Thread(tempSignalPlot);
                temp1.start();
            }
        });


        // for frames in sampleslist, copy to bufferlist and use as buffer
        SETBUFFERButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // go through samplesList and highlight selected

                int total = sampleFilesModel.getSize();

                int newIndex = bufferCollections.getDatasetCount();
                Dataset tempDataset;

                for(int i=0;i<total; i++){

                    if (sampleFilesModel.get(i).isSelected()){ // if selected, make copy and put in bufferFilesModel

                        System.out.println("SELECTED AS BUFFER " + sampleFilesModel.get(i).getFilename());
                        // add buffers to collection.get(69)
                        tempDataset = sampleFilesModel.get(i).dataset;
                        bufferCollections.addDataset(new Dataset(tempDataset));
                        bufferFilesModel.addElement(new SampleBufferElement(tempDataset.getFileName(), newIndex, bufferCollections.getDataset(newIndex).getColor(), bufferCollections.getDataset(newIndex)));
                        newIndex++;
                    }
                }

                buffersList.removeAll();
                buffersList.setModel(bufferFilesModel);
                buffersList.updateUI();
            }


        });

        UPDATEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                BEAMLINEMANUFACTURER = sourceTextField.getText();
                XRAYSOURCE = String.valueOf(comboBoxSource.getSelectedIndex());
                updateProp();
            }
        });


        SETPLOTRANGEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // set frames to use in making signal plot
                int total = sampleFilesModel.getSize();
                boolean first = false;
                boolean last = false;

                lastIndexInSignalPlot = total-1;
                firstIndexInSignalPlot = 0;

                for(int i=0;i<total; i++){
                    if (sampleFilesModel.get(i).isSelected() && !first){
                        first = true;
                        firstIndexInSignalPlot = i;
                    } else if (!(sampleFilesModel.get(i).isSelected()) && first && !last) {
                        lastIndexInSignalPlot = i;
                        break;
                    }
                }
                System.out.println("FIRST INDEX " + firstIndexInSignalPlot);
                System.out.println(" LAST INDEX " + lastIndexInSignalPlot);
                rangeSetLabel.setText(firstIndexInSignalPlot + " : " + lastIndexInSignalPlot);
            }
        });


        RESETRANGEButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastIndexInSignalPlot = -1;
                firstIndexInSignalPlot = 0;

                int total = sampleFilesModel.getSize();

                for(int i=0;i<total; i++){
                    sampleFilesModel.get(i).setSelected(true);
                }
                samplesList.repaint();
                rangeSetLabel.setText(":");
            }
        });


        damminRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dammifRadioButton.setSelected(false);
                //damminRadioButton.setEnabled(true);
            }
        });

        dammifRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                damminRadioButton.setSelected(false);
                damRefineCheckBox.setSelected(false);
                //damminRadioButton.setEnabled(true);
            }
        });

        fastRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slowRadioButton.setSelected(false);
            }
        });

        slowRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fastRadioButton.setSelected(false);
            }
        });


        damRefineCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (damRefineCheckBox.isSelected()){
                    // uncheck dammif
                    dammifRadioButton.setSelected(false);
                    damminRadioButton.setSelected(true);
                }
            }

        });


        checkBoxDirect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //l1NormCheckBox.setSelected(true);
                if (checkBoxDirect.isSelected()){
                    l1NormCheckBox.setSelected(true);
                    excludeBackgroundInFitCheckBox.setSelected(false);
                } else {
                    l1NormCheckBox.setSelected(false);
                    excludeBackgroundInFitCheckBox.setSelected(true);
                }

//                if (checkBoxDirect.isSelected()){
//                    checkBoxDirect.setSelected(true);
//                } else {
//                    checkBoxDirect.setSelected(false);
//                }
            }
        });


        l1NormCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (l1NormCheckBox.isSelected()){
                    checkBoxDirect.setSelected(true);
                    excludeBackgroundInFitCheckBox.setSelected(false);
                } else {
                    checkBoxDirect.setSelected(false);
                    excludeBackgroundInFitCheckBox.setSelected(true);
                }
            }
        });

        setPipelineDataDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // check if Directory exists and can be written to

                File theCWD = new File(PIPELINE_DATA_DIRECTORY.getWorkingDirectory());

                JFileChooser chooser = new JFileChooser(theCWD);
                chooser.setDialogTitle("Select Directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION){
                    //WORKING_DIRECTORY.setWorkingDirectory(chooser.getCurrentDirectory().toString());

                    if (chooser.getSelectedFile().isDirectory() ){
                        //File f = new File(chooser.getSelectedFile().isDirectory() + "/test.txt");
                        PIPELINE_DATA_DIRECTORY.setWorkingDirectory(chooser.getSelectedFile().toString());
                    } else {
                        PIPELINE_DATA_DIRECTORY.setWorkingDirectory(chooser.getCurrentDirectory().toString());
                    }

                    labelPipelineDir.setText(PIPELINE_DATA_DIRECTORY.getWorkingDirectory());
                   // updateProp();
                }


            }
        });


        SAVETODIRECTORYSubtractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Set working directory
                if (sampleCollections.getTotalSelected() < 1){
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane optionPane = new JOptionPane("Need at least one file selected",JOptionPane.WARNING_MESSAGE);
                    JDialog dialog = optionPane.createDialog("Warning!");
                    dialog.setAlwaysOnTop(true);
                    dialog.setVisible(true);
                    status.setText("Select More than one file to make zip ");
                    return;
                }

                SECArchive packageIt = new SECArchive(sampleCollections, bufferCollections, WORKING_DIRECTORY, subtractOutPutDirectoryLabel);
                packageIt.pack();
                packageIt.setVisible(true);

                updateProp();
            }
        });


        SECFactorAnalysisICAButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // buffers can not be empty
                if (bufferFilesModel.getSize() == 0){
                    status.setText("No buffer files set");
                    return;
                } else {
                    // qmin and qmax should span the range of data used for the analysis
                    // default qmin = 0.009
                    // default qmax = 0.3
                    double qmin=0;
                    double qmax=0;

                    // launch in separate thread
                    final double finalQmin = qmin;
                    final double finalQmax = qmax;

                    // collect samples that are selected
                    Collection tempCollectionForSubtraction = new Collection();
                    int total = sampleCollections.getDatasetCount();

                    for (int i = 0; i < total; i++) {
                        if (sampleCollections.getDataset(i).getInUse()) {
                            tempCollectionForSubtraction.addDataset( new Dataset(sampleCollections.getDataset(i)));
                        }
                    }
                    tempCollectionForSubtraction.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());


                    // collect buffers that are selected
                    Collection tempCollectionForBuffer = new Collection();
                    total = bufferCollections.getDatasetCount();

                    for (int i = 0; i < total; i++) {
                        if (bufferCollections.getDataset(i).getInUse()) {
                            tempCollectionForBuffer.addDataset( new Dataset(bufferCollections.getDataset(i)));
                        }
                    }
                    tempCollectionForSubtraction.setWORKING_DIRECTORY_NAME(subtractOutPutDirectoryLabel.getText());

                    FactorAnalysis tempFactorAnalysis = new FactorAnalysis(tempCollectionForSubtraction, tempCollectionForBuffer, finalQmin, finalQmax, status, mainProgressBar, subtractOutPutDirectoryLabel.getText(), version);
                    Thread temp1 = new Thread(tempFactorAnalysis);
                    temp1.start();

                }
            }
        });


        excludeBackgroundInFitCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                l1NormCheckBox.setSelected(false);
                checkBoxDirect.setSelected(false);
            }
        });

        // key binding for clearing everything in samples window
        subtractPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "buttonPressed");
        subtractPanel.getActionMap().put("buttonPressed", subtractButtonPressed);
        subtractPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released A"), "buttonReleased");
        subtractPanel.getActionMap().put("buttonReleased", subtractButtonReleased);


        SVDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // need to determine number of frames selected
                int inUseCount=0;
                XYSeriesCollection tempCollection = new XYSeriesCollection();
                double qmin=1000000;
                double qmax=-100000;
                double tempMax, tempMin;

                for(int i=0; i<collectionSelected.getDatasetCount(); i++){
                    Dataset tempData = collectionSelected.getDataset(i);
                    if (tempData.getInUse()){
                        tempCollection.addSeries(tempData.getData());
                        tempMin = tempData.getData().getMinX();
                        tempMax = tempData.getData().getMaxX();

                        if (tempMin < qmin){
                            qmin = tempMin;
                        }

                        if (tempMax > qmax){
                            qmax = tempMax;
                        }
                        inUseCount++;
                    }
                }


                if (inUseCount < 3){
                    status.setText("Too few frames, 3 or more!");
                    return;
                }

                // set common q-values (max and min)
                final double finalQmin = qmin;
                final double finalQmax = qmax;

                Thread makeIt = new Thread(){
                    public void run() {

                        final SVDCovariance svd = new SVDCovariance(finalQmin, finalQmax, tempCollection);
                        svd.execute();

                        try {
                            svd.get();
                            svd.makePlot();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                };

                makeIt.start();
            }
        });

        //analysisMiniPlots.createQIQChart();
        analysisMiniPlots.setChartPanels(miniGuinierPanel, miniVcPanel, bottomMiniPanel);

        tcm = analysisTable.getColumnModel();
        tc = tcm.getColumn(4);
        SpinnerEditor temp = (SpinnerEditor) tc.getCellEditor();
        temp.setMiniPlot(analysisMiniPlots);

        temp = (SpinnerEditor) tcm.getColumn(5).getCellEditor();
        temp.setMiniPlot(analysisMiniPlots);

        ICAButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get qmin and qmax

                Thread makeIt = new Thread(){
                    public void run() {
                        AnalysisICA icaTemp = new AnalysisICA(collectionSelected);
                        icaTemp.execute();
                    }
                };

                makeIt.start();

            }
        });


        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NonNegativeMatrixFactorization nmf = new NonNegativeMatrixFactorization();
                nmf.test();

//                Dataset temp;
//
//                for(int i=0; i<collectionSelected.getDatasetCount(); i++){
//                    Dataset tempData = collectionSelected.getDataset(i);
//                    if (tempData.getInUse()){
//                        temp = collectionSelected.getDataset(i);
//                        XYSeries tempSeries = new XYSeries("kurt");
//
//                        for (int j=temp.getStart(); j<temp.getEnd(); j++){
//                            tempSeries.add(temp.getAllData().getDataItem(j));
//                        }
//
//                        KurtosisSolver kurtsolver = new KurtosisSolver(tempSeries, 84);
//                        kurtsolver.run();
//                        System.out.println(kurtsolver.isDone());
//                        break;
//                    }
//                }
            }
        });

        l1NormCheckBox.setSelected(true);
        checkBoxDirect.setSelected(true);

        subtractThresholdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);

                String myString = subtractThresholdField.getText();
                try {
                    double newvalue = Double.parseDouble(myString);
                    if (newvalue > 0 && newvalue < 11.1){
                        thresholdField.setText(myString);
                        THRESHOLD=myString;
                        updateProp();
                    }
                }
                catch (NumberFormatException exception) {
                    //Not a number
                    subtractThresholdField.setText(thresholdField.getText());
                    System.out.println(exception.getMessage());
                }
            }
        });

        thresholdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                String myString = thresholdField.getText();
                try {
                    double newvalue = Double.parseDouble(myString);
                    if (newvalue > 0 && newvalue < 11.1){
                        subtractThresholdField.setText(myString);
                        THRESHOLD=myString;
                        updateProp();
                    }
                }
                catch (NumberFormatException exception) {
                    //Not a number
                    thresholdField.setText(subtractThresholdField.getText());
                    System.out.println(exception.getMessage());
                }
            }
        });





        run3BodyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run3BodyButton.setEnabled(false);
                float qmin = Float.parseFloat(qminCEText.getText());
                float qmax = Float.parseFloat(qmaxCEText.getText());
                float solventContrast = Float.parseFloat(solventContrast3Body.getText());

                double[] lowerParams = new double[1];
                lowerParams[0] = Double.parseDouble(min3BodyTextField.getText());
                double[] upperParams = new double[1];
                upperParams[0] = Double.parseDouble(max3BodyTextField.getText());
                double[] delta = new double[1];
                // delta can be calculated from delta-q and dmax
                delta[0] = 2.37;

                double[] contrasts = new double[1];
                contrasts[0] = Float.parseFloat(particleContrast3Body.getText());

                double percentNtoKeep = Double.parseDouble((String)topNComboBox.getSelectedItem())/100.0;
                double percentData = Double.parseDouble((String) lambdaCEcomboBox.getSelectedItem());
                int trialsPerRound = Integer.parseInt((String)trialsPerRoundComboBox.getSelectedItem());

                int topN = (int)Math.ceil(percentNtoKeep*trialsPerRound);
                Dataset tempData = (Dataset)ffCEFileSelectionComboBox.getSelectedItem();

                Thread findIt = new Thread(){
                    public void run() {

                        final FormFactorEngine body3 = new FormFactorEngine(tempData,
                                ModelType.THREE_BODY,
                                cpuCores,           // total cpu cores to use during search
                                Integer.valueOf((String)ceComboBoxRounds.getSelectedItem()),           // total rounds of selection (hard stop)
                                lowerParams, // lower radius starting value
                                upperParams, // upper
                                delta,       // incremement to go from lower -> upper limits of params
                                qmin,        // qmin of fit
                                qmax,        // qmax of fit
                                0.0001,      // epsilon
                                percentData, //
                                topN,        // top N to select in out of M
                                solventContrast,
                                contrasts,
                                trialsPerRound,  // trials per round
                                Integer.parseInt((String)modelsPerRoundComboBox.getSelectedItem()), // randomModelsPerTrial
                                !fitBackgroundCheckBox.isSelected(),      // useNoBackground
                                maximumEntropyRadioButton.isSelected());

                        body3.setProgressBar(progressBar3Body);

                        body3.setThreeBodyPlotPanels(
                                threeBodyGeometryPanel,
                                residuals3BodyPanel,
                                scoreProgressThreeBodyPanel,
                                dataFit3BodyPanel,
                                smallest3BodyPanel,
                                middle3BodyPanel,
                                largest3BodyPanel);

                        body3.execute();
                        try {
                            body3.get();
                            run3BodyButton.setEnabled(true);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                };

                findIt.start();
            }
        });



        buttonCoreShell.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonCoreShell.setEnabled(false);
                float qmin = Float.parseFloat(qminCEText.getText());
                float qmax = Float.parseFloat(qmaxCEText.getText());
                float solventContrast = Float.parseFloat(coreShellSolventContrast.getText());

                double[] lowerParams = new double[1];
                lowerParams[0] = Double.parseDouble(radiiLowerCoreShell.getText());
                double[] upperParams = new double[1];
                upperParams[0] = Double.parseDouble(radiiUpperCoreShell.getText());
                double[] delta = new double[1];
                // delta can be calculated from delta-q and dmax
                delta[0] = 2.37;

                double[] contrasts = new double[2];
                contrasts[0] = Float.parseFloat(coreShellShellContrast.getText());
                contrasts[1] = Float.parseFloat(coreShellCoreContrast.getText());

                double percentNtoKeep = Double.parseDouble((String)topNComboBox.getSelectedItem())/100.0;
                double percentData = Double.parseDouble((String) lambdaCEcomboBox.getSelectedItem());
                int trialsPerRound = Integer.parseInt((String)trialsPerRoundComboBox.getSelectedItem());

                int topN = (int)Math.ceil(percentNtoKeep*trialsPerRound);
                Dataset tempData = (Dataset)ffCEFileSelectionComboBox.getSelectedItem();

                ModelType modelType1 = ModelType.CORESHELL_PROLATE_ELLIPSOID;
                if (coreShellEllipsoidModel.getSelectedIndex()==1){
                    modelType1 = ModelType.CORESHELL_OBLATE_ELLIPSOID;
                }

                final ModelType finalModelType = modelType1;

                Thread findIt = new Thread(){
                    public void run() {

                        final FormFactorEngine csModel = new FormFactorEngine(tempData,
                                finalModelType,
                                cpuCores,           // total cpu cores to use during search
                                Integer.valueOf((String)ceComboBoxRounds.getSelectedItem()),           // total rounds of selection (hard stop)
                                lowerParams, // lower radius starting value
                                upperParams, // upper
                                delta,       // incremement to go from lower -> upper limits of params
                                qmin,        // qmin of fit
                                qmax,        // qmax of fit
                                0.0001,      // epsilon
                                percentData, //
                                topN,        // top N to select in out of M
                                solventContrast,
                                contrasts,
                                trialsPerRound,  // trials per round
                                Integer.parseInt((String)modelsPerRoundComboBox.getSelectedItem()), // randomModelsPerTrial
                                !fitBackgroundCheckBox.isSelected(),     // useNoBackground
                                maximumEntropyRadioButton.isSelected());

                        csModel.setProgressBar(coreShellProgressBar);
                        csModel.setCompleteness(coreEmptyAndFullCheckBox.isSelected(), Double.parseDouble(shellThickness.getText()));

                        csModel.setCSPlotPanels(
                                geometryCoreShellPanel,
                                residualsCoreShellPanel,
                                scorePanelCoreShell,
                                dataFitsCoreShell,
                                histogramRaCoreShellPanel,
                                histogramShellThicknessPanel,
                                histogramRcCoreShellPanel);

                        csModel.execute();
                        try {
                            csModel.get();
                            buttonCoreShell.setEnabled(true);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        } catch (ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                };

                findIt.start();
            }
        });
        maximumEntropyRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondDerivativeRadioButton.setSelected(false);
            }
        });
        secondDerivativeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maximumEntropyRadioButton.setSelected(false);
            }
        });
    }

    private void updateSamplesListSelection(boolean boo) {
        int totalSamples = sampleFilesModel.getSize();
        Collection sampleCollection = (Collection) collections.get(96);
        for(int i=0;i<totalSamples; i++){
            ((SampleBufferElement) samplesList.getModel().getElementAt(i)).setSelected(boo);
            sampleCollection.getDataset(i).setInUse(boo);
        }
        samplesList.repaint();
    }


    private void updateMiniPlots(int i) {
        analysisMiniPlots.updatePlots(collectionSelected.getDataset(i));
    }


    AbstractAction subtractButtonPressed = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //System.out.println("WHAT " + e.getActionCommand());
            clearALL = true;
        }
    };

    AbstractAction subtractButtonReleased = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearALL = false;
        }
    };

    public static void updateProp(){
        Properties prop = new Properties();
        OutputStream output = null;

        try {
            output = new FileOutputStream("scatter.config");

            // set the properties value
            prop.setProperty("workingDirectory", WORKING_DIRECTORY.getWorkingDirectory());
            prop.setProperty("atsasDirectory", ATSAS_DIRECTORY);
            prop.setProperty("subtractionDirectory", OUTPUT_DIR_SUBTRACTION_NAME);
            prop.setProperty("xraysource", XRAYSOURCE);
            prop.setProperty("beamlineOrManufacturer", BEAMLINEMANUFACTURER);
            prop.setProperty("threshold", THRESHOLD);
            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void clearCollection(int panel){

        ((Collection)collections.get(panel)).removeAllDatasets();
        ((Collection)collections.get(panel)).setNote("Drop Files in Colored Box for Set " + panel);

        prModel.clear();
        PofRPlot pofRPlot = PofRPlot.getInstance();

        pofRPlot.clear();
        IofQPofRPlot iofQPofRPlot = IofQPofRPlot.getInstance();
        iofQPofRPlot.clear();

        dataFilesModel.clear();
        dataFilesList.removeAll();

        analysisModel.clear();
        resultsModel.clear();
        analysisMiniPlots.clearMiniPlots();

        prStatusLabel.setText("");
        status.setText("Cleared");




        closeWindows();
    }

    private boolean checkQminQmax(String qminText, String qmaxText){
        if (!isNumber(qminText) || !isNumber(qmaxText)){
            Toolkit.getDefaultToolkit().beep();
            status.setText("q-range (qmin, qmax) is not a number ");
            return false;
        } else {
            double qmin = Double.parseDouble(qminText);
            double qmax = Double.parseDouble(qmaxText);
            if (qmin > qmax){
                Toolkit.getDefaultToolkit().beep();
                JOptionPane optionPane = new JOptionPane("q-range (qmin > qmax) invalid range",JOptionPane.WARNING_MESSAGE);
                JDialog dialog = optionPane.createDialog("Warning!");
                dialog.setAlwaysOnTop(true);
                dialog.setVisible(true);

                status.setText("q-range (qmin > qmax) invalid range ");
                return false;
            }
        }
        return true;
    }

    private void closeWindows(){
        // replot any visible frames
        if (powerLawPlot.isVisible()){
            powerLawPlot.closeWindow();
        }

        if (errorPlot.isVisible()){
            errorPlot.closeWindow();
        }

        if (qIqPlot.isVisible()){
            qIqPlot.closeWindow();
        }

        if (log10IntensityPlot.isVisible()){
            log10IntensityPlot.closeWindow();
        }
    }


    public void updateProgress(final int newValue) {
        mainProgressBar.setValue(newValue);
    };

    public void setPValue(final int j) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateProgress(j);
            }
        });
    }

    public static void main(String[] args) {
        //check from property file

        int totalFiles = args.length;
        // if files, add to Collection
        for(int i=0; i<totalFiles; i++){
            System.out.println(i + " Files " + args[i]);
        }

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        WORKING_DIRECTORY = new WorkingDirectory();
        File propertyFile = new File("scatter.config");

        if (propertyFile.exists() && !propertyFile.isDirectory()){
            Properties prop = new Properties();
            InputStream input = null;

            try {
                input = new FileInputStream("scatter.config");
                // load a properties file
                prop.load(input);
                if (prop.getProperty("workingDirectory") != null) {
                    //WORKING_DIRECTORY_NAME = prop.getProperty("workingDirectory");
                    WORKING_DIRECTORY.setWorkingDirectory(prop.getProperty("workingDirectory"));
                   // WORKING_DIRECTORY = new WorkingDirectory(prop.getProperty("workingDirectory"));
                }
                if (prop.getProperty("atsasDirectory") != null) {
                    ATSAS_DIRECTORY = prop.getProperty("atsasDirectory");
                }
                if (prop.getProperty("subtractionDirectory") != null) {
                    OUTPUT_DIR_SUBTRACTION_NAME = prop.getProperty("subtractionDirectory");
                }

                if (prop.getProperty("xraysource") != null) {
                    XRAYSOURCE = prop.getProperty("xraysource");
                }

                if (prop.getProperty("beamlineOrManufacturer") != null) {
                    BEAMLINEMANUFACTURER = prop.getProperty("beamlineOrManufacturer");
                }

                THRESHOLD="0.44";
                if (prop.getProperty("threshold") != null) {
                    THRESHOLD = prop.getProperty("threshold");
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
        File theDir = new File(WORKING_DIRECTORY.getWorkingDirectory());
        if (!theDir.exists()) {
            WORKING_DIRECTORY = new WorkingDirectory(System.getProperty("user.dir"));
        }

        JFrame frame = new JFrame("ScÅtter: Software for SAXS Analysis");
        final Scatter programInstance = new Scatter();
        frame.setContentPane(programInstance.panel1);

        // set source
        //programInstance.comboBoxSource.setSelectedIndex(Integer.parseInt(XRAYSOURCE));
        // Drag-n-Drop listeners attached to SWING components here
        // Create FileDrop listeners
        // Load Files from Files Tab Panel 1

        //File theDir = new File(programInstance.WORKING_DIRECTORY.getWorkingDirectory());
       // if (!theDir.exists()) {
            //WORKING_DIRECTORY_NAME = System.getProperty("user.dir");
       //     WORKING_DIRECTORY = new WorkingDirectory("user.dir");
       // }
        programInstance.workingDirLabel.setText(WORKING_DIRECTORY.getWorkingDirectory());
        System.out.println("WORKING DIR NAME => " + WORKING_DIRECTORY.getWorkingDirectory());

        theDir = new File(programInstance.ATSAS_DIRECTORY);
        if (!theDir.exists()) {
            ATSAS_DIRECTORY = System.getProperty("user.dir");

        }
        System.out.println("ATSAS DIRECTORY => " + ATSAS_DIRECTORY);
        programInstance.atsasDirLabel.setText(ATSAS_DIRECTORY);

        theDir = new File(programInstance.OUTPUT_DIR_SUBTRACTION_NAME);

        if (!theDir.exists()) {
            OUTPUT_DIR_SUBTRACTION_NAME = System.getProperty("user.dir");
        }

        System.out.println("SUBTRACTION DIRECTORY => " + OUTPUT_DIR_SUBTRACTION_NAME);
        programInstance.subtractOutPutDirectoryLabel.setText(OUTPUT_DIR_SUBTRACTION_NAME);

        programInstance.sourceTextField.setText(BEAMLINEMANUFACTURER);

        new FileDrop( programInstance.getLoadPanel(1), new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {
                collectionSelected = (Collection)collections.get(0);
                collectionSelected.setPanelID(0);
                for(int i=0; i < totalPanels; i++){
                    collectionButtons.get(i).setSelected(false);
                }

                collectionButtons.get(0).setSelected(true);
                miniPlots.get(0).chart.setNotify(false);

                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(0), programInstance.getStatus(), 0, programInstance.convertNmToAngstromCheckBox.isSelected(), programInstance.autoRgCheckBox.isSelected(), false, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        rec1.setModels(analysisModel, resultsModel, dataFilesModel, programInstance.dataFilesList);
                        rec1.setPDBParams(programInstance.excludeWatersFromInputCheckBox.isSelected(), Double.parseDouble(programInstance.qmaxForPDBText.getText()));
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        miniPlots.get(0).frame.removeAll();
                        miniPlots.get(0).chart.setNotify(false);
                        miniPlots.get(0).plot(collectionSelected);
                        minis.get(0).add(miniPlots.get(0).frame.getChartPanel());
                        miniPlots.get(0).chart.setNotify(true);
                    }
                }.start();
            }
        });

        // Load Files from Files Tab Panel 2
        new FileDrop( programInstance.getLoadPanel(2), new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {
                final int panel = 1;
                collectionSelected = (Collection)collections.get(panel);
                collectionSelected.setPanelID(panel);
                for(int i=0; i < totalPanels; i++){
                    collectionButtons.get(i).setSelected(false);
                }
                collectionButtons.get(panel).setSelected(true);

                miniPlots.get(panel).chart.setNotify(false);
                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(panel), programInstance.getStatus(), panel, programInstance.convertNmToAngstromCheckBox.isSelected(), programInstance.autoRgCheckBox.isSelected(), false, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        rec1.setModels(analysisModel, resultsModel, dataFilesModel, programInstance.dataFilesList);
                        rec1.setPDBParams(programInstance.excludeWatersFromInputCheckBox.isSelected(), Double.parseDouble(programInstance.qmaxForPDBText.getText()));
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        miniPlots.get(panel).frame.removeAll();
                        miniPlots.get(panel).chart.setNotify(false);
                        miniPlots.get(panel).plot(collectionSelected);
                        minis.get(panel).add(miniPlots.get(panel).frame.getChartPanel());
                        miniPlots.get(panel).chart.setNotify(true);
                    }
                }.start();
            }
        });
        // Load Files from Files Tab Panel 3
        new FileDrop( programInstance.getLoadPanel(3), new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {
                final int panel = 2;
                collectionSelected = (Collection)collections.get(panel);
                collectionSelected.setPanelID(panel);
                for(int i=0; i < totalPanels; i++){
                    collectionButtons.get(i).setSelected(false);
                }
                collectionButtons.get(panel).setSelected(true);

                miniPlots.get(panel).chart.setNotify(false);
                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(panel), programInstance.getStatus(), panel, programInstance.convertNmToAngstromCheckBox.isSelected(), programInstance.autoRgCheckBox.isSelected(), false, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        rec1.setModels(analysisModel, resultsModel, dataFilesModel, programInstance.dataFilesList);
                        rec1.setPDBParams(programInstance.excludeWatersFromInputCheckBox.isSelected(), Double.parseDouble(programInstance.qmaxForPDBText.getText()));
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        miniPlots.get(panel).frame.removeAll();
                        miniPlots.get(panel).chart.setNotify(false);
                        miniPlots.get(panel).plot(collectionSelected);
                        minis.get(panel).add(miniPlots.get(panel).frame.getChartPanel());
                        miniPlots.get(panel).chart.setNotify(true);
                    }
                }.start();
            }
        });
        // Load Files from Files Tab Panel 4
        new FileDrop( programInstance.getLoadPanel(4), new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {
                final int panel = 3;
                collectionSelected = (Collection)collections.get(panel);
                collectionSelected.setPanelID(panel);
                for(int i=0; i < totalPanels; i++){
                    collectionButtons.get(i).setSelected(false);
                }
                collectionButtons.get(panel).setSelected(true);

                miniPlots.get(panel).chart.setNotify(false);
                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(panel), programInstance.getStatus(), panel, programInstance.convertNmToAngstromCheckBox.isSelected(), programInstance.autoRgCheckBox.isSelected(), false, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        rec1.setModels(analysisModel, resultsModel, dataFilesModel, programInstance.dataFilesList);
                        rec1.setPDBParams(programInstance.excludeWatersFromInputCheckBox.isSelected(), Double.parseDouble(programInstance.qmaxForPDBText.getText()));
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        miniPlots.get(panel).frame.removeAll();
                        miniPlots.get(panel).chart.setNotify(false);
                        miniPlots.get(panel).plot(collectionSelected);
                        minis.get(panel).add(miniPlots.get(panel).frame.getChartPanel());
                        miniPlots.get(panel).chart.setNotify(true);
                    }
                }.start();
            }
        });

        //Buffers panel
        new FileDrop(programInstance.buffers, new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {
                final int panel = 69;
                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(panel), programInstance.getStatus(), panel, programInstance.convertNmToAngstromCheckBox.isSelected(), false, true, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        rec1.setSampleBufferModels(programInstance.bufferFilesModel);
                        rec1.useShortenedConstructor();
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        programInstance.status.setText("Total Loaded: " + programInstance.bufferFilesModel.getSize());
                        programInstance.buffersList.removeAll();
                        programInstance.buffersList.setModel(programInstance.bufferFilesModel);
                        //programInstance.buffersList.validate();
                        programInstance.buffersList.updateUI();
                    }
                }.start();
            }
        });

        //samples Panel
        new FileDrop(programInstance.samples, new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {

                final int panel = 96;
                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(panel), programInstance.getStatus(), panel, programInstance.convertNmToAngstromCheckBox.isSelected(), false, true, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        rec1.setSampleBufferModels(programInstance.sampleFilesModel);
                        rec1.useShortenedConstructor();
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        programInstance.samplesList.removeAll();  // remove model and resetModel

                        programInstance.samplesList.setModel(programInstance.sampleFilesModel);
                        programInstance.status.setText("Total Loaded: " + programInstance.sampleFilesModel.getSize());

                        // populate drop down
                        Collection thisCollection = ((Collection) collections.get(panel));

                        programInstance.thresholdField.setText(Constants.ThreeDecPlace.format(thisCollection.getMaxq()*0.05 + thisCollection.getMaxq()));

                        int totalDatasets = thisCollection.getDatasetCount();
                        programInstance.setReferenceBox.removeAllItems();

                        for(int i=0; i< totalDatasets; i++){
                            String name = thisCollection.getDataset(i).getFileName();
                            programInstance.setReferenceBox.addItem(new ReferenceItem(name, i));
                        }
                        programInstance.setReferenceBox.setSelectedIndex(programInstance.setReferenceBox.getItemCount()-1);
                        programInstance.samplesList.validate();
                        programInstance.samplesList.updateUI();
                    }
                }.start();
            }
        });


//similarity Panel
        new FileDrop(programInstance.covFilesScrollPanel, new FileDrop.Listener() {
            @Override
            public void filesDropped(final File[] files) {

                final int panel = 29;

                new Thread() {
                    public void run() {
                        ReceivedDroppedFiles rec1 = new ReceivedDroppedFiles(files, (Collection)collections.get(panel), programInstance.getStatus(), panel, programInstance.convertNmToAngstromCheckBox.isSelected(), false, false, programInstance.mainProgressBar, programInstance.WORKING_DIRECTORY.getWorkingDirectory());
                        // add other attributes and then run
                        Collection thisCollection = ((Collection) collections.get(panel));
                        rec1.setSampleBufferModels(programInstance.similarityFilesModel);
                        Thread temp1 = new Thread(rec1);
                        temp1.start();
                        try {
                            temp1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // populate drop down
                        System.out.println("Populating list");
                        programInstance.similarityList.removeAll();
                        programInstance.similarityFilesModel.clear();
                        //sampleBufferFilesList.removeAll();
                        //repopulate
                        int totalDatasets = thisCollection.getDatasetCount();
                        Color tempColor;
                        for(int i=0; i< totalDatasets; i++){
                            String name = thisCollection.getDataset(i).getFileName();
                            name = name + "_" + i;
                            //thisCollection.getDataset(i).setFileName(name);
                            tempColor = ((Collection)collections.get(panel)).getDataset(i).getColor();
                            programInstance.similarityFilesModel.addElement(new SampleBufferElement(name, i, tempColor, thisCollection.getDataset(i)));
                        }

                        //sampleBufferFilesList.setModel(sampleBufferFilesModel);
                        programInstance.similarityList.setModel(programInstance.similarityFilesModel);
                        programInstance.qminSimilarityField.setText(String.valueOf(thisCollection.getDataset(0).getAllData().getMinX()));
                        programInstance.qmaxSimilarityField.setText(String.valueOf(thisCollection.getDataset(0).getAllData().getMaxX()));

                        programInstance.status.setText("Total Loaded: " + programInstance.similarityFilesModel.getSize());
                        programInstance.similarityList.revalidate();
                        programInstance.similarityList.repaint();
                    }
                }.start();
            }
        });


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        //System.exit(0);
    }


    private void updateActiveModels(){
        dataFilesModel.clear();
        dataFilesList.removeAll();
        analysisModel.clear();
        //resultsModel.getDatalist().clear();
        resultsModel.clear();
        int totalModels = analysisModel.getRowCount();
        //System.out.println("Updating Models : " + totalModels + " < " + collectionSelected.getDatasetCount());

        for (int i = 0; i < collectionSelected.getDatasetCount(); i++) {
            String name = collectionSelected.getDataset(i).getFileName();

            dataFilesModel.addElement(new DataFileElement(name, i));
            analysisModel.addDataset(collectionSelected.getDataset(i));
            resultsModel.addDataset(collectionSelected.getDataset(i));
        }

        dataFilesList.setModel(dataFilesModel);
        analysisModel.fireTableDataChanged();
    }

    private void switchCollection(int j){
        int selected = j; // toggled radioButton

        // if non are selected set toggled radioButton to true

        for(int i=0; i<totalPanels; i++){
            if (i != selected){
                collectionButtons.get(i).setSelected(false);
            } else if (i == selected) {
                collectionButtons.get(i).setSelected(true);
                //update list
                dataFilesModel.clear();
                dataFilesList.removeAll();

                analysisModel.clear();
                analysisModel.clear();

                status.setText("Switched to collection " + (j+1));

                /*
                if (iofQPofRWindow instanceof IofQPofRPlot){
                    iofQPofRWindow.getContentPane().removeAll();
                    iofQPofRWindow.removeAll();
                    iofQPofRWindow.clear();
                    iofQPofRWindow.dispose();
                }

                if (pofRWindow instanceof PofRPlot){
                    pofRWindow.getContentPane().removeAll();
                    pofRWindow.removeAll();
                    pofRWindow.clear();
                    pofRWindow.dispose();
                }

                prModel.clear();
                */
                resultsModel.clear();
//                analysisModel.addDataset(((Collection)collections.get(collectionNumber)).getLast());
//                resultsModel.addDataset(((Collection)collections.get(collectionNumber)).getLast());
                // update dataFilesList in dataFilesPanel;
                // rebuild dataFilesPanel from collection.get(i)

                for(int jj=0; jj<((Collection)collections.get(selected)).getDatasets().size(); jj++){
                    String name = ((Collection)collections.get(selected)).getDataset(jj).getFileName();
                    dataFilesModel.addElement(new DataFileElement(name, jj));
                    analysisModel.addDataset(((Collection) collections.get(selected)).getDataset(jj));
                    resultsModel.addDataset(((Collection) collections.get(selected)).getDataset(jj));
                }

                dataFilesList.setModel(dataFilesModel);
                collectionSelected = (Collection)collections.get(selected);
                collectionNote.setText(collectionSelected.getNote());

                closeWindows();
            }
        }
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
        log10IntensityPlot.plot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
    }

    /**
     * Creates Kratky plot from Singleton Class
     */
    private void createKratkyPlot(){
        kratky = KratkyPlot.getInstance();
        kratky.plot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
    }

    private void createGPAPlot(int id){

        GPAPlot gpaPlot = new GPAPlot("SC\u212BTTER \u2263 GUINIER PEAK ANALYSIS ", collectionSelected.getDataset(id), WORKING_DIRECTORY.getWorkingDirectory());
        gpaPlot.makePlot(analysisModel);

    }

    private void createRatioPlot(){

        RatioPlot ratioPlot = new RatioPlot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
        ratioPlot.plot();
    }

    private void createFlexPlots(){
        FlexibilityPlots flexplot = new FlexibilityPlots(collectionSelected, WORKING_DIRECTORY);
        flexplot.makePlot();
    }

    private void createVcPlot(){
        VcPlot tempPlot = new VcPlot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
        tempPlot.plot(status);
    }


    private void createVolumePlot(int id){
        VolumePlot tempPlot = new VolumePlot(collectionSelected.getDataset(id), WORKING_DIRECTORY.getWorkingDirectory(), analysisModel);
        tempPlot.plot();
    }

    /**
     * Creates Kratky plot from Singleton Class
     */
    private void createQIQPlot(){
        qIqPlot.plot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
    }

    private void createNormalizedKratkyPlot(){

        normalKratkyRg = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (GUINIER)");
        normalKratkyRg.plot(collectionSelected, "RECIRG", WORKING_DIRECTORY.getWorkingDirectory());


        // test if Real Space Invariants have been determined
        for (int i=0; i<collectionSelected.getDatasetCount(); i++){
            Dataset temp = collectionSelected.getDataset(i);
            if (temp.getRealIzero() > 0 && temp.getRealRg() > 0){
                normalKratkyRgReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Rg-based (Real space)");
                normalKratkyRgReal.plot(collectionSelected, "REALRG", WORKING_DIRECTORY.getWorkingDirectory());
                break;
            }
        }


        //normalKratkyVc = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Guinier)");
        //normalKratkyVcReal = new NormalizedKratkyPlot("DIMENSIONLESS KRATKY PLOT Vc-based (Real space)");

    }

    private void createErrorPlot(){
        errorPlot.plot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
    }

    private void createPowerLawPlot(){
        powerLawPlot.plot(collectionSelected, WORKING_DIRECTORY.getWorkingDirectory());
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

        if (log10IntensityPlot.isVisible()){
            log10IntensityPlot.setNotify(false);
            collectionSelected.getDataset(fIndex).scalePlottedLogErrorData();
            log10IntensityPlot.setNotify(true);
        }

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


        public Component getTableCellRendererComponent(JTable table, Object color,
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
        private MiniPlots mini;

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
            Dataset dataset = collectionSelected.getDataset(rowID);
            int temp = (Integer)this.spinner.getValue();
            int current = (Integer) this.spinner.getValue() - 1;
            int direction;// = temp - this.priorValue;
            int limit;

            if (this.colID == 4){

                double test = dataset.getData().getMaxX(); // plotted data
                int valueOfSpinner = (Integer)this.spinner.getValue();
                //
                if ((valueOfSpinner < 1) || valueOfSpinner > dataset.getData().getItemCount() || ( dataset.getOriginalLog10Data().getX( valueOfSpinner ).doubleValue() >= test)){
                    this.spinner.setValue(1);
                    this.priorValue = 1;
                } else {
                    //moving up or down?
                    direction = temp - dataset.getStart();
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
                    direction = temp - dataset.getEnd();
                    //moving up or down?
                    if (direction < 0) {
                        if (direction == -1) {
                            //remove last point
                            dataset.getData().remove(dataset.getData().getItemCount()-1);
                        } else {
                            limit = (Integer) temp - dataset.getEnd();
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
            mini.updatePlots(dataset);
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

        public void setMiniPlot(MiniPlots plot){
            this.mini = plot;
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
                    System.out.println("CheckBox row " + row);
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

                } else if (column == 6){ // Fit File
                    collectionSelected.getDataset(row).setFitFile(!(Boolean)value);
                    // switch Error to allData
                }
            } else if (table.getModel().toString().contains("Pr")){

                PrModel temp = (PrModel)table.getModel();
                RealSpace tempReal = temp.getDataset(row);
                tempReal.setSelected(!(Boolean)value);

                PofRPlot pofRPlot = PofRPlot.getInstance();
                pofRPlot.changeVisible(row, tempReal.getSelected());
                IofQPofRPlot iofQPofRPlot = IofQPofRPlot.getInstance();
                iofQPofRPlot.changeVisible(row, tempReal.getSelected());
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
            reColorPlots(data_row, currentColor, thickness, pointSize);
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



    /**
     * replot the color of the tables listed in the method
     * @param row
     * @param newColor
     * @param thickness
     * @param pointsize
     */
    private void reColorPlots(int row, Color newColor, float thickness, int pointsize) {

        if (log10IntensityPlot.isVisible()){
            log10IntensityPlot.changeColor(row, newColor, thickness, pointsize);
        }

        if (kratky.isVisible()){
            kratky.changeColor(row, newColor, thickness, pointsize);
        }

        if (qIqPlot.isVisible()){
            qIqPlot.changeColor(row, newColor, thickness, pointsize);
        }

        if (errorPlot.isVisible()){
            errorPlot.changeColor(row, newColor, thickness, pointsize);
        }

        if (powerLawPlot.isVisible()){
            powerLawPlot.changeColor(row, newColor, thickness, pointsize);
        }

        if (PofRPlot.getInstance().inUse() && IofQPofRPlot.getInstance().inUse()){
            PofRPlot.getInstance().changeColor(row, newColor, thickness);
            IofQPofRPlot.getInstance().changeColor(row, newColor, thickness, pointsize);
        }

        // add Normalized KratkyPlots
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

                PlotManualGuinier manualGuinierPlot = new PlotManualGuinier("Guinier Plot", collectionSelected.getDataset(rowID), WORKING_DIRECTORY.getWorkingDirectory());
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


    public static File[] finder(String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.endsWith(".dat"); }
        } );

    }

    public static Float getRetinaScaleFactor(){
        Object obj = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.contentScaleFactor");
        if(obj != null){
            if(obj instanceof Float)
                return (Float) obj;
        }
        return null;
    }


    public static boolean hasRetinaDisplay(){
        Float fRetinaFactor = getRetinaScaleFactor();
        if(fRetinaFactor != null){
            if(fRetinaFactor > 0){
                int nScale = fRetinaFactor.intValue();
                return (nScale == 2); // 1 indicates a regular mac display, 2 is for retina
            }
        }
        return false;
    }


    private static float getResulationFactor(){
        float fResolutionFactor = ((float) Toolkit.getDefaultToolkit().getScreenResolution() / 96f);
        if(hasRetinaDisplay()){
            fResolutionFactor = fResolutionFactor * getRetinaScaleFactor().floatValue();
        }
        return fResolutionFactor;
    }


    class LoadFiles implements Runnable {
        private double limit;
        private int upperlower;
        private JProgressBar bar;
        public LoadFiles(double limit, int uorl, JProgressBar bar){
            this.bar = bar;
            this.limit = limit;
            this.upperlower=uorl;
        }

        @Override
        public void run() {
            //To change body of implemented methods use File | Settings | File Templates.
            bar.setValue(0);
            bar.setStringPainted(true);
            for(int i=0; i<upperlower; i++){
                bar.setValue((int) (i / (double) upperlower * 100));
            }
            bar.setMinimum(0);
            bar.setStringPainted(false);
            bar.setValue(0);
        }
    }


    private class MouseAdaptorForDragging extends MouseInputAdapter {
        private boolean mouseDragging = false;
        private int dragSourceIndex;

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragSourceIndex = similarityList.getSelectedIndex();

                mouseDragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseDragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mouseDragging) {
                int currentIndex = similarityList.locationToIndex(e.getPoint());
                if (currentIndex != dragSourceIndex) {
                    int dragTargetIndex = similarityList.getSelectedIndex(); // new location
                    SampleBufferElement dragElement = similarityFilesModel.get(dragSourceIndex);
                    // reorder the collection is created on drop from received files similarityCollection
                    similarityCollection.reorderCollection(dragSourceIndex, dragTargetIndex);

                    similarityFilesModel.remove(dragSourceIndex);
                    similarityFilesModel.add(dragTargetIndex, dragElement);
                    dragSourceIndex = currentIndex;
                }
            }
        }
    }

    public class PrButtonEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        JButton button;
        private int rowID;
        private int colID;
        private String name;


        public PrButtonEditorRenderer(String name){
            this.button = new JButton();
            this.button.addActionListener(this);
            //button.setMaximumSize(new Dimension(10,10));
            //button.setPreferredSize(new Dimension(10,10));

            if (name == "Norm"){
                this.button.setText("N");
                this.button.setToolTipText("Normalize");
            } else if (name == "Refine"){
                this.button.setText("Refine");
                this.button.setToolTipText("Refine P(r) model");
            } else if (name == "2File"){
                this.button.setText("2File");
                this.button.setToolTipText("Write to File");
            } else if (name == "SEARCH"){
                this.button.setText("? d-max");
                this.button.setToolTipText("Auto-dmax search");
                //Icon warnIcon = new ImageIcon("src/dmax_logo.png");
                //this.button = new JButton(warnIcon);
            }

            this.name = name;
            this.button.setFont(new Font("Verdana", Font.PLAIN, 10));
            this.button.setBorderPainted(true);
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
            if (this.colID == 12) {
                this.button.setBackground(Color.WHITE);
                this.button.setForeground(Color.GREEN);
                //normalize - divide P(r) by I-Zero
                double invIzero = 1.0/prModel.getDataset(rowID).getIzero();
                prModel.getDataset(rowID).setScale((float) invIzero);

           //     prModel.fireTableDataChanged();
            } else if (this.colID == 13){ // dmax search

                this.button.setBackground(Color.WHITE);
                this.button.setForeground(Color.GREEN);
                status.setText("");
                prStatusLabel.setText("Starting dmax search of " + prModel.getDataset(rowID).getFilename());
                // PrObject tempPr = new PrObject(prDataset, Double.parseDouble(lambdaBox.getSelectedItem().toString()), l1NormCheckBox.isSelected(), Integer.parseInt(cBox.getSelectedItem().toString()));
                //prModel.getDataset(rowID).estimateDmax(Double.parseDouble(lambdaBox.getSelectedItem().toString()), l1NormCheckBox.isSelected(), Integer.parseInt(cBox.getSelectedItem().toString()));

                Thread refineIt = new Thread(){
                    public void run() {

                        final DmaxManager refineMe = new DmaxManager(prModel.getDataset(rowID), (cpuCores-1),
                                Double.parseDouble(lambdaBox.getSelectedItem().toString()),
                                l1NormCheckBox.isSelected());

                        prStatusLabel.setText("");

                        //refineMe.setBar(progressBar1, prStatusLabel);
                        refineMe.execute();
/*
                        synchronized (refineMe) {
                            if (!refineMe.getIsFinished()) {
                                try {
                                    refineMe.wait();
                                } catch (InterruptedException ee) {
                                    // handle it somehow
                                    System.out.println("Catch " + ee.getMessage());
                                }
                            }
                        }
                        */
                    }

                };

                refineIt.start();


            } else if (this.colID == 14){
                //refine_Pr
                this.button.setBackground(Color.WHITE);
                this.button.setForeground(Color.GREEN);
                status.setText("");
                prStatusLabel.setText("Starting refinement of " + prModel.getDataset(rowID).getFilename());
                // launch a thread
                System.out.println("Stopped in refinemananer");

                Thread refineIt = new Thread(){
                    public void run() {

                        final RefineManager refineMe = new RefineManager(prModel.getDataset(rowID), cpuCores,
                                Integer.parseInt(refinementRoundsBox.getSelectedItem().toString()),
                                Double.parseDouble(rejectionCutOffBox.getSelectedItem().toString()),
                                Double.parseDouble(lambdaBox.getSelectedItem().toString()),
                                l1NormCheckBox.isSelected(), excludeBackgroundInFitCheckBox.isSelected());

                        prStatusLabel.setText("");
                        refineMe.setBar(progressBar1, prStatusLabel);
                        refineMe.execute();

                        synchronized (refineMe) {
                            if (!refineMe.getIsFinished()) {
                                try {
                                    refineMe.wait();
                                } catch (InterruptedException ee) {
                                    // handle it somehow
                                    System.out.println("Catch " + ee.getMessage());
                                }
                            }
                        }

                        FileObject tempFile = new FileObject(new File(WORKING_DIRECTORY.getWorkingDirectory()));
                        tempFile.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                        String newname = tempFile.writePRFile(collectionSelected.getDataset(prModel.getDataset(rowID).getId()),
                                prStatusLabel,
                                collectionSelected.getDataset(prModel.getDataset(rowID).getId()).getFileName(),
                                WORKING_DIRECTORY.getWorkingDirectory(),
                                true
                        );

                        prStatusLabel.setText("Files written to " + WORKING_DIRECTORY.getWorkingDirectory() + ", ready to run DAMMIN/F");
                        runDatGnom(newname, collectionSelected.getDataset(prModel.getDataset(rowID).getId()).getRealRg());
                        // run gnom
                        prModel.fireTableDataChanged();
                    }

                };

                refineIt.start();

            } else if (this.colID == 15){
                // write Pr and Iq distributions toFile
                // create new instance of save and pass through datasets
                // get and return directory and filename
                SavePr tempSave = new SavePr();

                if (tempSave.getFileName().length() > 2){
                    FileObject tempFile = new FileObject(tempSave.getCurrentDir());
                    tempFile.setSource(comboBoxSource, BEAMLINEMANUFACTURER);
                    String newname = tempFile.writePRFile(collectionSelected.getDataset(prModel.getDataset(rowID).getId()),
                                                                       prStatusLabel,
                                                                       tempSave.getFileName(),
                                                                       tempSave.getCurrentDir().getAbsolutePath(),
                                                                       false
                    );

                    status.setText("Files written to " + WORKING_DIRECTORY.getWorkingDirectory() + ", ready to run DAMMIN/F");
                    runDatGnom(newname, collectionSelected.getDataset(prModel.getDataset(rowID).getId()).getRealRg());
                }
            }
        }

        @Override
        public Object getCellEditorValue() {
            return button.isSelected();
        }
    }



    class SavePr {
        private String name;
        private File currentDir;

        public SavePr(){
            JFileChooser c = new JFileChooser(WORKING_DIRECTORY.getWorkingDirectory());
            JTextField filename = new JTextField(), dir = new JTextField();
            // "Save" dialog:

            int rVal = c.showSaveDialog(panel1);

            if (rVal == JFileChooser.APPROVE_OPTION) {
                filename.setText(c.getSelectedFile().getName());
                this.setFileName(filename.getText());
                dir.setText(c.getCurrentDirectory().toString());
                this.setCurrentDir(c.getCurrentDirectory());
            }

            if (rVal == JFileChooser.CANCEL_OPTION) {
                status.setText("Save Cancelled");
                filename.setText("");
                dir.setText("");
            }
        }
        /*
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            JTextField filename = new JTextField(), dir = new JTextField();
            // "Save" dialog:

            int rVal = c.showSaveDialog(Scatter.this);

            if (rVal == JFileChooser.APPROVE_OPTION) {
                filename.setText(c.getSelectedFile().getName());
                this.setFileName(filename.getText());
                dir.setText(c.getCurrentDirectory().toString());
                this.setCurrentDir(dir.getText());
            }
            if (rVal == JFileChooser.CANCEL_OPTION) {
                status.setText("Save Cancelled");
                filename.setText("You pressed cancel");
                dir.setText("");
            }
        }
        */
        private void setFileName(String text){
            this.name = text;
            System.out.println("Setting name to " + text);
        }

        private void setCurrentDir(File text){
            this.currentDir = text;
        }

        public String getFileName(){
            return this.name;
        }

        public File getCurrentDir(){
            return this.currentDir;
        }
    }

    private void runDatGnom(String dat_file_name, double rg){
        // run datgnom
        String os = System.getProperty("os.name");
        String datgnom = "";
        Runtime rt = Runtime.getRuntime();

        if (os.indexOf("win") >=0 ){
            datgnom = "datgnom.exe";
        } else {
            datgnom = "datgnom";
        }

        String[] base_name = dat_file_name.split("\\.");

        try {
            System.out.println("Running datgnom: " + ATSAS_DIRECTORY+"/"+datgnom);

            ProcessBuilder pr = new ProcessBuilder(ATSAS_DIRECTORY+"/"+datgnom, "-r", Constants.Scientific1dot3e1.format(rg), "-o", base_name[0]+"_dg.out", WORKING_DIRECTORY.getWorkingDirectory()+ "/" + dat_file_name);
            pr.directory(new File(WORKING_DIRECTORY.getWorkingDirectory()));
            Process ps = pr.start();

            BufferedReader input = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String line=null;
            while((line=input.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Finished datgnom: file " + base_name[0] + "_dg.out");
            damminLabel.setText(WORKING_DIRECTORY.getWorkingDirectory() + "/"+ base_name[0] + "_dg.out");

        } catch (IOException e) {
            System.out.println("Problem running datgnom from " + ATSAS_DIRECTORY);
            status.setText("Problem running datgnom from " + ATSAS_DIRECTORY);
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    class JComboBoxCEListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            //System.out.println("SOURCE " + evt.getPropertyName());
            //update comboxbox
            ffCEFileSelectionComboBox.updateUI();
            //ffCEFileSelectionComboBox.revalidate();
            //ffCEFileSelectionComboBox.repaint();
        }
    }

    public void changeCWD(){
        status.setText("CWD : " + WORKING_DIRECTORY.getWorkingDirectory());
        File theCWD = new File(WORKING_DIRECTORY.getWorkingDirectory());
        JFileChooser chooser = new JFileChooser(theCWD);
        chooser.setDialogTitle("Select Directory");

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(panel1) == JFileChooser.APPROVE_OPTION){
            //WORKING_DIRECTORY.setWorkingDirectory(chooser.getCurrentDirectory().toString());
            if (chooser.getSelectedFile().isDirectory()){
                WORKING_DIRECTORY.setWorkingDirectory(chooser.getSelectedFile().toString());
            } else {
                WORKING_DIRECTORY.setWorkingDirectory(chooser.getCurrentDirectory().toString());
            }
            workingDirLabel.setText(WORKING_DIRECTORY.getWorkingDirectory());
            updateProp();
        }

        status.setText("CWD Changed to : " + WORKING_DIRECTORY.getWorkingDirectory());
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

class SampleBufferListRenderer extends JCheckBox implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {

        setEnabled(list.isEnabled());
        setSelected(((SampleBufferElement) value).isSelected());
        //setFont(list.getFont());
        setFont( new Font ("Sanserif", Font.BOLD, 14));
        setBackground(list.getBackground());
        setForeground(((SampleBufferElement) value).getColor());
        setText(value.toString());

        if (isSelected) {
            setBackground(Constants.LightBlueGray);
        }

        return this;
    }
}



class CEComboBoxModel extends AbstractListModel implements ComboBoxModel {

    private Object selectedItem;
    private Collection collection;

    public CEComboBoxModel(Collection collectioninUse){
        this.collection = collectioninUse;
    }

    public void setSelectedItem(Object anItem) {
        selectedItem = anItem; // to select and register an
    } // item from the pull-down list

    // Methods implemented from the interface ComboBoxModel
    public Object getSelectedItem() {
        return selectedItem; // to add the selection to the combo box
    }

    public int getSize(){
        return collection.getDatasetCount();
    }

    public Object getElementAt(int i){
        return collection.getDataset(i);
    }
}

class CECellRenderer implements ListCellRenderer {

    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {


        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);

        if (value instanceof Dataset) {

//            renderer.setBackground((Color) value);
            renderer.setText(((Dataset) value).getFileName());
        }

        //renderer.setText(((Dataset) value).getFileName());
        //if (index >= 0){
        //    renderer.setText(((Dataset) value).getFileName());
        //}

        return renderer;
    }

}

