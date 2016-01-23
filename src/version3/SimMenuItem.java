package version3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by robertrambo on 22/01/2016.
 */
public class SimMenuItem {
    private JPanel simMenuItemPanel;
    private JButton saveButton;
    private JButton cancelButton;
    private JScrollPane simScrollPane;
    private JPanel simListPanel;
    private JPanel simButtonsPanel;
    private JPanel saveCancelPanel;
    private Similarity simObject;
    private static DefaultListModel<SimilarityCollectionItem> itemListModel;
    private JList itemList;
    private JFrame frame;


    public SimMenuItem(Similarity simObject){
        this.simObject = simObject;
        itemList = new JList();
        itemList.setCellRenderer(new SimDataFilesListRenderer());
        itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        itemListModel = new DefaultListModel<SimilarityCollectionItem>();

        int totalItems = this.simObject.getTotalItemsInCollection();
        for (int i =0; i<totalItems; i++){
            itemListModel.addElement(this.simObject.getCollectionItemByIndex(i));
        }

        itemList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                JList list = (JList) event.getSource();
                // Get index of clicked item
                int index = list.locationToIndex(event.getPoint());
                // Toggle selected state
                SimilarityCollectionItem item = (SimilarityCollectionItem) list.getModel().getElementAt(index);
                // Repaint cell
                item.setSelected( !item.isSelected());
                list.repaint(list.getCellBounds(index,index));
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });


        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });


        this.makeFrame();
    }

    private void makeFrame(){

        itemList.setModel(itemListModel);
        //simScrollPane = new JScrollPane();
        simScrollPane.setViewportView(itemList);

        frame = new JFrame("Similarity Item Selection");

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });


        frame.setContentPane(this.simMenuItemPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void onCancel() {
        // add your code here if necessary
        frame.dispose();
    }


    class SimDataFilesListRenderer extends JCheckBox implements ListCellRenderer {

        Color setColor;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
            setEnabled(list.isEnabled());
            setSelected(((SimilarityCollectionItem)value).isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(((SimilarityCollectionItem)value).getName().toString());
            return this;
        }
    }


}
