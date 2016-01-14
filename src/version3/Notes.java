package version3;

import javax.swing.*;
import java.awt.event.*;

public class Notes extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea notesArea;
    private JLabel notesLabel;
    private Dataset dataset;

    public Notes(Dataset data) {
        dataset = data;
        notesLabel.setText("Experimental Notes for: " + dataset.getFileName());
        this.notesArea.setText(dataset.getExperimentalNotes());

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

        this.setLocation(400,400);
    }

    private void onOK() {
// add your code here
        dataset.setExperimentalNotes(this.notesArea.getText());
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

}
