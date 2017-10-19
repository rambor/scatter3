package version3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SubtractTwo extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBoxA;
    private JComboBox comboBoxB;
    private Dataset datasetA, datasetB;
    private JLabel statusLabel;
    private Collection inUse;
    private WorkingDirectory workingDirectory;

    public SubtractTwo(Collection collectionSelected, WorkingDirectory workingDirectory) {

        this.inUse = collectionSelected;
        this.workingDirectory = workingDirectory;

        comboBoxA.setModel(new SubtractTwo.FComboBoxModel(this.inUse));
        comboBoxA.setRenderer(new SubtractTwo.CECellRenderer());

        comboBoxB.setModel(new SubtractTwo.FComboBoxModel(this.inUse));
        comboBoxB.setRenderer(new SubtractTwo.CECellRenderer());

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

        comboBoxA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                Dataset tempDataset = (Dataset) cb.getSelectedItem();


           if (datasetB != null && (tempDataset.getId() == datasetB.getId())){
                    statusLabel.setText("Datasets can not be equal");
                    statusLabel.setForeground(Color.red);
                } else {
                    datasetA = tempDataset;
                    statusLabel.setText("");
                }
            }
        });

        comboBoxB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                Dataset tempDataset = (Dataset) cb.getSelectedItem();

                if (datasetA != null && tempDataset.getId() == datasetA.getId()){
                    statusLabel.setText("Datasets can not be equal");
                    statusLabel.setForeground(Color.red);
                } else {
                    datasetB = tempDataset;
                    statusLabel.setText("");
                }
            }
        });
    }

    private void onOK() {
        // add your code here

       // check that the two datasets are not the same



        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

//    public static void main(String[] args) {
//        SubtractTwo dialog = new SubtractTwo();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    class FComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private Object selectedItem;
        private Collection collection;

        public FComboBoxModel(Collection collectioninUse){
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

        public void updateCollection(Collection collectionInUse){
            this.collection = collectionInUse;
        }
    }


    class CECellRenderer implements ListCellRenderer {

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {


            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);

            if (value instanceof Dataset) {
//            renderer.setBackground((Color) value);
                renderer.setText((((Dataset) value).getId()+1) + " - " + ((Dataset) value).getFileName());
            }

            return renderer;
        }

    }
}
