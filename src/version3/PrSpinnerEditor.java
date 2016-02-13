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
    private JCheckBox qIqFit;
    private JComboBox lambdaBox;
    private JCheckBox l1NormCheckBox;
    private JComboBox cBox;

    // Initializes the spinner.
    public PrSpinnerEditor(PrModel prModel, JLabel status,  JCheckBox qIqCheckBox, JComboBox lambdaBox, JCheckBox l1NormCheckBox, JComboBox cBox) {
        super(new JTextField());
        spinner = new JSpinner();

        this.lambdaBox = lambdaBox;
        this.l1NormCheckBox = l1NormCheckBox;
        this.cBox = cBox;

        this.prModel = prModel;

        this.status = status;
        this.qIqFit = qIqCheckBox;

        editor = ((JSpinner.DefaultEditor)spinner.getEditor());
        textField = editor.getTextField();

        textField.addFocusListener( new FocusListener() {
            public void focusGained( FocusEvent fe ) {

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

        int oldStart = prDataset.getStart();
        int oldStop = prDataset.getStop();

        int limit;
        int valueOfSpinner = (Integer)this.spinner.getValue();

        if (this.colID == 4){ // lower(start) spinner
            if (((Integer)this.spinner.getValue() < prDataset.getLowerQIndexLimit()) || (valueOfSpinner > oldStop)){
                this.spinner.setValue(prDataset.getLowerQIndexLimit());
                this.priorValue = prDataset.getLowerQIndexLimit();
            } else {
                //moving up or down?
                int direction = valueOfSpinner - oldStart;
                if (direction > 0) {
                    prDataset.decrementLow(valueOfSpinner);
                } else if (direction < 0){
                    prDataset.incrementLow(valueOfSpinner);
                }
                this.priorValue = temp;
            }

            prDataset.setStart(valueOfSpinner);

        } else if (colID == 5) {
            limit = prDataset.getMaxCount();

            if (valueOfSpinner >= limit){
                this.spinner.setValue(prDataset.getStop());
            } else {
                //moving up or down?
                int direction = valueOfSpinner - oldStop;

                if (direction < 0) {
                    prDataset.decrementHigh(valueOfSpinner);
                } else if (direction > 0){
                    prDataset.incrementHigh(valueOfSpinner);
                }
                this.priorValue = temp;
            }

        } else if (colID==9){
            temp = (Integer)this.spinner.getValue();
            prDataset.setDmax(temp);
            status.setText("Finished: d_max set to " + prDataset.getDmax());
        }
        //recalculate P(r) distributions
        status.setText("Analyzing, please wait");

        // calculte new Fit
        PrObject tempPr = new PrObject(prDataset, Double.parseDouble(lambdaBox.getSelectedItem().toString()), l1NormCheckBox.isSelected(), Integer.parseInt(cBox.getSelectedItem().toString()));
        tempPr.run();

        prDataset.calculateIntensityFromModel(qIqFit.isSelected());
        prModel.fireTableDataChanged();
    }

    // Prepares the spinner component and returns it.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        rowID = row;
        colID = column;
        lastValue = prModel.getDataset(rowID).getAllData().getItemCount();

        if (colID == 4) {
            priorValue = prModel.getDataset(rowID).getStart();
            spinner.setModel(new SpinnerNumberModel(priorValue, 1, lastValue, 10));
        } else if (colID == 5) {
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

    public boolean isCellEditable( EventObject eo ) {
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