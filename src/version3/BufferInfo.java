package version3;

import javax.swing.*;
import java.awt.event.*;

public class BufferInfo extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel bufferLabel;
    private JTextArea bufferTextArea;
    private Dataset dataset;

    public BufferInfo(Dataset data) {
        dataset = data;
        bufferLabel.setText("Buffer Composition for: " + dataset.getFileName());
        this.bufferTextArea.setText(dataset.getBufferComposition());

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
        dataset.setBufferComposition(this.bufferTextArea.getText());
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
