package version3;

import org.jfree.data.xy.XYDataItem;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * Created by robertrambo on 25/01/2016.
 */
public class PrSpinnerEditor extends DefaultCellEditor implements ChangeListener {
    JSpinner spinner;
    JSpinner.DefaultEditor editor;
    JTextField textField;
    boolean valueSet;
    private int rowID;
    private PrModel prModel;
    private int colID;
    private int priorValue;
    private int lastValue;
    private JLabel status;
    private JCheckBox antiLog;
    private JCheckBox qIqFit;

    // Initializes the spinner.
    public PrSpinnerEditor(PrModel prModel, JLabel status, JCheckBox antiLogCheckBox) {
        super(new JTextField());
        spinner = new JSpinner();

        this.prModel = prModel;

        this.status = status;
        this.antiLog = antiLogCheckBox;

        editor = ((JSpinner.DefaultEditor)spinner.getEditor());
        textField = editor.getTextField();

        textField.addFocusListener( new FocusListener() {
            public void focusGained( FocusEvent fe ) {

                //textField.setSelectionStart(0);
                //textField.setSelectionEnd(1);
                    /*
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            if ( valueSet ) {
                                textField.setCaretPosition(1);
                            }
                        }
                    });
                    */
            }

            public void focusLost( FocusEvent fe ) {
                //System.out.println("FocusLost " + collectionSelected.getDataset(rowID).getData().getX(0) + " | value " + spinner.getValue());
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

        RealSpace prDataset = prModel.getDataset(rowID);

        int temp = (Integer)this.spinner.getValue();
        int current = (Integer) this.spinner.getValue() - 1;

        int direction = temp - this.priorValue;
        int limit;

        XYDataItem tempData;

        if (this.colID == 4){ // lower(start) spinner
            double test = prDataset.getAllData().getMaxX(); // plotted data
            double testSpinner = prDataset.getAllData().getX(((Integer) this.spinner.getValue()).intValue()).doubleValue();

            if (((Integer)this.spinner.getValue() < 1) || (testSpinner > test)){
                this.spinner.setValue(1);
                this.priorValue = 1;
            } else {
                //moving up or down?
                if (direction > 0) {
                    if (direction == 1) {

                        tempData = prDataset.getfittedIq().getDataItem(0);
                        // check to see if datapoint is in log plot
                        if (prDataset.getLogData().indexOf(tempData.getX()) >= 0){
                            prDataset.getLogData().remove(0);
                        }
                        prDataset.getfittedIq().remove(0);
                        prDataset.getfittedError().remove(0);
                        prDataset.getqIq().remove(0);
                    } else {

                        limit = (Integer) temp - this.priorValue;
                        for (int i = 0; i < limit; i++){
                            tempData = prDataset.getfittedIq().getDataItem(0);
                            // check to see if datapoint is in log plot
                            if (prDataset.getLogData().indexOf(tempData.getX()) >= 0){
                                prDataset.getLogData().remove(0);
                            }
                            prDataset.getfittedIq().remove(0);
                            prDataset.getfittedError().remove(0);
                            prDataset.getqIq().remove(0);
                        }

                    }
                    this.priorValue = temp;
                } else if (direction < 0){
                    if (direction == -1) {

                        tempData = prDataset.getAllData().getDataItem(current);
                        double q = tempData.getXValue();

                        if (tempData.getYValue() > 0){  // make sure Intensity is positive
                            double lnI = Math.log10(tempData.getYValue());
                            prDataset.getLogData().add(q, lnI);
                        }
                        prDataset.getfittedIq().add(tempData);
                        prDataset.getfittedError().add(prDataset.getErrorAllData().getDataItem(current));
                        prDataset.getqIq().add(q,q*tempData.getYValue());


                    } else {
                        // keep adding points up until currentValue
                        int start = current;
                        double lastq = prDataset.getfittedIq().getMaxX();
                        int last = prDataset.getAllData().indexOf(lastq);

                        double q, lnI;
                        //int start = this.priorValue - 2;
                        prDataset.getLogData().clear();
                        prDataset.getfittedIq().clear();
                        prDataset.getfittedError().clear();
                        prDataset.getqIq().clear();

                        for(int i = start; i <= last; i++){
                            tempData = prDataset.getAllData().getDataItem(i);
                            q = tempData.getXValue();

                            if (tempData.getYValue() > 0){
                                lnI = Math.log10(tempData.getYValue());
                                prDataset.getLogData().add(q, lnI);
                            }
                            prDataset.getfittedIq().add(tempData);
                            prDataset.getfittedError().add(prDataset.getErrorAllData().getDataItem(i));
                            prDataset.getqIq().add(q,q*tempData.getYValue());
                        }
                    }
                    this.priorValue = temp;
                }
            }
        } else if (colID == 5) {
            limit = prDataset.getAllData().getItemCount();
            int testLogInt;
            if ((Integer)this.spinner.getValue() > limit){
                this.spinner.setValue(limit);
                this.priorValue = limit;
            } else {
                //moving up or down?
                if (direction < 0) {
                    if (direction == -1) {
                        //remove last point
                        int last = prDataset.getfittedIq().getItemCount();
                        tempData = prDataset.getfittedIq().getDataItem(last -1);

                        // check to see if datapoint is in log plot
                        if (prDataset.getLogData().indexOf(tempData.getX()) >= 0){
                            prDataset.getLogData().remove(prDataset.getLogData().getItemCount()-1);
                        }
                        prDataset.getfittedIq().remove(last-1);
                        prDataset.getfittedError().remove(last-1);
                        prDataset.getqIq().remove(last-1);
                    } else {
                        limit = (Integer) temp - this.priorValue;
                        //current is the last point
                        int start = prDataset.getfittedIq().getItemCount() - 1;
                        int stop = start + limit;
                        // keep removing last point
                        for (int i = start; i > stop; i--){
                            tempData = prDataset.getfittedIq().getDataItem(i);
                            testLogInt = prDataset.getLogData().indexOf(tempData.getXValue());
                            if ( testLogInt >= 0){
                                prDataset.getLogData().remove(testLogInt);
                            }
                            prDataset.getfittedIq().remove(i);
                            prDataset.getfittedError().remove(i);
                            prDataset.getqIq().remove(i);
                        }
                    }

                } else if (direction > 0){
                    if (direction == 1) {
                        //XYDataItem tempXY = collectionSelected.getDataset(collectionID).getAllData().getDataItem(current);
                        XYDataItem tempXY = prDataset.getAllData().getDataItem(current);
                        double q = tempXY.getXValue();

                        if (tempXY.getYValue() > 0){
                            double lnI = Math.log10(tempXY.getYValue());
                            prDataset.getLogData().add(q,lnI);
                        }
                        prDataset.getfittedIq().add(tempXY);
                        prDataset.getfittedError().add(prDataset.getErrorAllData().getDataItem(current));
                        prDataset.getqIq().add(q, q*tempXY.getYValue());


                    } else {
                        // keep adding points up until currentValue
                        int last = current;
                        double startq = prDataset.getfittedIq().getX(0).doubleValue();
                        int start = prDataset.getAllData().indexOf(startq);
                        double q, lnI;

                        prDataset.getfittedIq().clear();
                        prDataset.getfittedError().clear();
                        prDataset.getLogData().clear();
                        prDataset.getqIq().clear();

                        for(int i = start; i <= last; i++){
                            XYDataItem tempXY = prDataset.getAllData().getDataItem(i);
                            q = tempXY.getXValue();
                            if (tempXY.getYValue() > 0){
                                lnI = Math.log10(tempXY.getYValue());
                                prDataset.getLogData().add(q,lnI);
                            }
                            prDataset.getfittedIq().add(tempXY);
                            prDataset.getfittedError().add(prDataset.getErrorAllData().getDataItem(i));
                            prDataset.getqIq().add(q, q*tempXY.getYValue());
                        }
                    }
                }
                this.priorValue = temp;
                prModel.setValueAt(temp, rowID, colID);
            }
        } else if (colID==9){

            temp = (Integer)this.spinner.getValue();
            //collectionID = prDataset.getId();
            prDataset.setDmax(temp);
        }
        //recalculate P(r) distributions
        status.setText("Analyzing, please wait");

        //recalculatePr(prDataset, antiLog.isSelected());

        status.setText("Finished: d_max set to " + prDataset.getDmax());
    }

    // Prepares the spinner component and returns it.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        rowID = row;
        colID = column;
        lastValue = prModel.getDataset(rowID).getAllData().getItemCount();

        if (colID == 4){
            priorValue = prModel.getDataset(rowID).getStart();
            spinner.setModel(new SpinnerNumberModel(priorValue, 1, lastValue, 10));

        } else if (colID == 5){
            priorValue = prModel.getDataset(rowID).getStop();
            spinner.setModel(new SpinnerNumberModel(priorValue, 1, lastValue, 10));
        } else if (colID == 9) {
            priorValue = prModel.getDataset(rowID).getDmax();
            spinner.setValue(priorValue);
        }

        //new SpinnerNumberModel(spinnerStart, 1, spinnerEnd, 10)
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
        if ( eo instanceof KeyEvent) {
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
            spinner.commitEdit();

        } catch ( java.text.ParseException e ) {
            JOptionPane.showMessageDialog(null,
                    "Invalid value, discarding.");
        }
        return super.stopCellEditing();
    }
} // end of PrSpinnerEditor