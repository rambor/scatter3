package version3.powerlawfits;

import version3.Collection;
import version3.WorkingDirectory;

import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;

public class PowerLawFitGui extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private Collection collection;
    private JTextField qminField;
    private JTextField qmaxField;
    private JProgressBar progressBar1;
    private JLabel dirLabel;
    private JLabel statusLabel;
    private WorkingDirectory workingDirectory;
    private int cpus;

    public PowerLawFitGui(Collection collection, WorkingDirectory workingDirectory, int cpus) {

        this.collection = collection;
        this.workingDirectory = workingDirectory;
        this.cpus = cpus;

        dirLabel.setText("Output Dir : " + workingDirectory.getWorkingDirectory());

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
    }

    private void onOK() {


        new Thread(){
            public void run() {

                PowerLawFitManager tempManager = new PowerLawFitManager(
                        collection,
                        Double.parseDouble(qminField.getText()),
                        Double.parseDouble(qmaxField.getText()),
                        1700,
                        5,
                        cpus,
                        progressBar1,
                        statusLabel,
                        workingDirectory
                );

                tempManager.execute();


                try {
                    tempManager.get();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }

            }
        }.start();


    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

}
