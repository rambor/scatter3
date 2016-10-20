package version3;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class ExportData extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private XYSeriesCollection plottedData;
    private String workingDirectoryName;
    private String prefix;

    public ExportData(XYSeriesCollection data, String dirname, String prefix) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        plottedData = data;
        workingDirectoryName = dirname;
        this.prefix = prefix;

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
    }


    private void onOK() {
        // add your code here
        // sanitize text file
        String myString = textField1.getText().replaceAll(" ", "_").toLowerCase();
        int totalinplot = plottedData.getSeriesCount();
        FileWriter fstream;

        for (int i=0; i<totalinplot; i++){
            String base = myString+ "_"+(String)plottedData.getSeries(i).getKey();
            XYSeries tempSeries = plottedData.getSeries(i);
            int totalItems=tempSeries.getItemCount();

            try{ // create P(r) file
                // Create file
                fstream = new FileWriter(workingDirectoryName+ "/" + base +"_" +prefix+ ".txt");
                BufferedWriter out = new BufferedWriter(fstream);
                for(int j=0; j < totalItems; j++){
                    out.write( Constants.Scientific1dot2e1.format(tempSeries.getX(j).doubleValue()) + "\t" + Constants.Scientific1dot2e1.format(tempSeries.getY(j).doubleValue()) + "\n");
                }
                //Close the output stream
                out.close();
            }catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }
        }

        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

}
