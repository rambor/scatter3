package version3;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Created by robertrambo on 24/01/2016.
 */
public class PrModel extends AbstractTableModel implements ChangeListener, PropertyChangeListener {

    private final LinkedList<RealSpace> datalist;
    private JCheckBox fitModel;
    private WorkingDirectory currentWorkingDirectory;
    private JLabel status;
    private DoubleValue dmaxLow, dmaxHigh;
    private JComboBox lambdaBox, cBox;
    private JProgressBar mainBar, prBar;
    private JSlider dmaxStart;

    DecimalFormat twoDecPlac = new DecimalFormat("0.00");
    DecimalFormat scientific = new DecimalFormat("0.00E0");
    DecimalFormat twoOneFormat = new DecimalFormat("0.0");

    private String[] columnNames = new String[]{"","", "", "", "start", "end", "<html>I(0)<font color=\"#ffA500\"> Real</font> (<font color=\"#808080\">Reci</font>)</html>", "<html>R<sub>g</sub><font color=\"#ffA500\"> Real</font> (<font color=\"#808080\">Reci</font>)</html> ", "<html>r<sub>ave</sub></html>", "<html>d<sub>max</sub></html>", "<html>Chi<sup>2</sup>(S<sub>k2</sub>)</html>", "<html>scale</html>", "", "", "",""};
    private JLabel mainStatus, prStatus;

    public PrModel(JLabel status, WorkingDirectory cwd, JComboBox lambdaBox, DoubleValue dmaxlow, DoubleValue dmaxhigh, JSlider dmaxSlider, JCheckBox fitModel, JComboBox cBox){
        this.status = status;
        this.currentWorkingDirectory = cwd;
        currentWorkingDirectory.addPropertyChangeListener(this);

        this.lambdaBox = lambdaBox;

        datalist = new LinkedList<>();
        this.fitModel = fitModel;
        dmaxLow = dmaxlow;
        dmaxHigh = dmaxhigh;
        this.dmaxStart = dmaxSlider;
        this.cBox = cBox;
    }
/*
    public void setLambda(double value){
        lambda = value;
    }

    public double getLambda(){
        return lambda;
    }
*/

    public int getRowCount() {
        return datalist.size();
    }

    public void remove(int row){
        datalist.remove(row);
        this.fireTableRowsDeleted(row, row);
    }

    public void clear(){
        datalist.clear();
        this.fireTableDataChanged();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setValueAt(Object obj, int row, int col){

        try {

            RealSpace dataset = (RealSpace) datalist.get(row);

            if (col == 11){
                if(!isNumber((String) obj)){
                    status.setText("Scale factor " + (dataset.getId()+1) + " is not a number. Enter a number.");
                    return;
                } else {
                    status.setText("*");
                }

                dataset.setScale(Float.parseFloat((String) obj));

            } else if (col == 4){
                dataset.setStart((Integer)obj);
            } else if (col == 5){
                dataset.setStop((Integer)obj);
            } else if (col==0) {
                Symbol temp = (Symbol)obj;

                dataset.setColor(temp.getColor());
                dataset.setStroke(temp.getStroke());
                dataset.setPointSize(temp.getPointSize());
                // rebuild plot
                // replotPr(row, dataset);
            } else if (col == 9){
                dataset.setDmax((Integer)obj);
            }
            fireTableCellUpdated(row, col);

        } catch(IndexOutOfBoundsException e) {
            System.out.println("Please wait, reindexing PrModel Class" + e);
        }
    }

    //selected
    //filename
    //start
    //end
    //I(0)
    //Rg
    //r-average
    //dmax
    //chi2
    //scale
    //refine button
    //write to file
    //norm

    @Override
    public Object getValueAt(int row, int col) {
        RealSpace dataset = (RealSpace) datalist.get(row);

        int index;
        // anytime row is clicked, this method is executed
        switch (col){
            case 0: //color
                return dataset.getColor();
            case 1:
                return dataset.getId() + 1;  //id is the id from the list on Analysis tab
            case 2: // rendered as a checkbox
                return dataset.getSelected();
            case 3:
                return dataset.getFilename();
            case 4: //spinner
                index = dataset.getStart();
                return index;
            case 5: //spinner
                index = dataset.getStop();
                return index;
            case 6: //I-zero
                double tempIzero = dataset.getGuinierIzero();
                return "<html><b><font color=\"#ffA500\">" + scientific.format(dataset.getIzero()).toString() + "</font></b> (<font color=\"#808080\">" + scientific.format(tempIzero).toString() +"</font>)</html>";
            case 7: //Rg
                String tempRg = twoDecPlac.format(dataset.getGuinierRg()).toString();
                return "<html><b><font color=\"#ffA500\">" + twoDecPlac.format(dataset.getRg()).toString()  + "</font></b> (<font color=\"#808080\">" +  tempRg +"</font>)</html>";
            case 8: //r-average
                return twoOneFormat.format(dataset.getRaverage());
            case 9:
                return dataset.getDmax();
            case 10:
                return twoDecPlac.format(dataset.getChi2()) + " ("+twoDecPlac.format(dataset.getKurt_l1_sum())+")";
            case 11:
                return scientific.format(dataset.getScale());
            case 12: //norm  Button
                return true;
            case 13: //dmax search Button
                return true;
            case 14: //refine Button
                return true;
            case 15: //toFile Button
                return true;
            default:  //
                return null;
        }
    }

    public void setBars(JProgressBar mainBar, JProgressBar prBar, JLabel mainStatus, JLabel prStatus){
        this.mainBar = mainBar;
        this.prBar = prBar;
        this.mainStatus = mainStatus;
        this.prStatus = prStatus;
    }

    public RealSpace getDataset(int i){
        return datalist.get(i);
    }

    public void addDataset(RealSpace dataset){
        datalist.add(dataset);
        fireTableRowsInserted(datalist.size()-1, datalist.size()-1);
        //fireTableDataChanged();
    }

    public void addDatasetsFromCollection(final Collection collection){

        datalist.clear();
        final int total = collection.getDatasetCount();
        mainBar.setStringPainted(true);
        mainBar.setIndeterminate(true);
        mainStatus.setText("Loading Datasets into Pr-tab");
        prStatus.setText("");

        for (int i = 0; i < total; i++) {
            if (collection.getDataset(i).getInUse()) {

                RealSpace temp = collection.getDataset(i).getRealSpaceModel();
                temp.setColor(collection.getDataset(i).getColor());

                if ((temp.getRg() == 0 && temp.getIzero() == 0) || temp.getDmax() == 0) {
                            temp.resetStartStop();
                            fireTableDataChanged();
                            temp.setDmax((int) dmaxStart.getValue());
                            //create a new PrObject and run in thrad
                            // fit model is L1-norm of coefficients or second derivative
                            PrObject tempPr = new PrObject(temp, Double.parseDouble(lambdaBox.getSelectedItem().toString()), fitModel.isSelected(), Integer.parseInt(cBox.getSelectedItem().toString()));
                            Thread tempThread = new Thread(tempPr);
                            tempThread.run();
                            // update series in plots
                }

                // don't want to use kept series here
                datalist.add(collection.getDataset(i).getRealSpaceModel());

            }
        }

        mainBar.setStringPainted(false);
        mainBar.setIndeterminate(false);
        mainStatus.setText("");
        //fireTableDataChanged();
    }


    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        //editable 0,2,4,5,6,13,14
        if (col==0 || col==2 || col==4 || col==5 || col==9 || col==11 || col==12 ||col==13 ||col==14 ||col==15) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isNumber( String input ) {
        try {
            Double.parseDouble(input);
            return true;
        } catch( Exception e) {
            return false;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // determine if the CurrentTemperature property of the temperature
        // object is the one that changed
        if (evt.getSource() == currentWorkingDirectory && evt.getPropertyName() == "WorkingDirectory") {
            WorkingDirectory t = (WorkingDirectory) evt.getSource();
            // get the new value object
            Object o = evt.getNewValue();
            String newCWD;
            if (o == null) {
                // go back to the object to get the temperature
                newCWD = t.getWorkingDirectory();
            } else {
                // get the new temperature value
                newCWD = ((String)o).toString();
            }
        } else if (evt.getSource() == dmaxLow && evt.getPropertyName() == "DoubleValue") {

            DoubleValue t = (DoubleValue) evt.getSource();
            Object o = evt.getNewValue();
            double newCWD;
            if (o == null) {
                // go back to the object to get the temperature
                newCWD = t.getValue();
            } else {
                // get the new temperature value
                newCWD = ((Double)o).doubleValue();
            }
        }
    }


    @Override
    public void stateChanged(ChangeEvent e) {

    }
}
