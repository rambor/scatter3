package version3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;

public class LaguerreParamsSingleton {

    private JPanel additionalInfoPrPanel;
    private JTextField additionalInfoPrTextField;
    private JCheckBox fixValue;
    private JLabel infoLabel;
    private boolean isSelected = false;
    private boolean isFixed = false;
    private double rgLimit = 20;
    private double r_ave, defaultRg = 117;
    private DecimalFormat twoOneFormat = new DecimalFormat("0.0");
    private static LaguerreParamsSingleton singleton = new LaguerreParamsSingleton( );

    private LaguerreParamsSingleton(){
        additionalInfoPrPanel = new JPanel();
        JLabel tempLabel = new JLabel("Rg");
        additionalInfoPrPanel.add(tempLabel);
        additionalInfoPrPanel.setBackground(Color.white);

        additionalInfoPrTextField = new JTextField();
        additionalInfoPrTextField.setBorder(BorderFactory.createEtchedBorder());

        additionalInfoPrTextField.setName("");
        additionalInfoPrTextField.setText(twoOneFormat.format(defaultRg));
        additionalInfoPrTextField.setMinimumSize(new Dimension(60,25));
        additionalInfoPrTextField.setPreferredSize(new Dimension(60,25));

//        additionalInfoPrTextField.setBorder(BorderFactory.createEmptyBorder());

        additionalInfoPrTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("In Focus");
            }

            @Override
            public void focusLost(FocusEvent e) {
                String content = additionalInfoPrTextField.getText();
                System.out.println("out of Focus");
                // if valid number, update default
                try {
                    double d = Double.parseDouble(content);

                    if (d > rgLimit){
                        defaultRg = d;
                        r_ave = Math.sqrt(2.0)*0.92*defaultRg;
                        // want ratio of alpha-to-beta
                        double beta  = (defaultRg*defaultRg*2 - r_ave*r_ave)/(r_ave);
                        double alpha = (r_ave*r_ave)/(defaultRg*defaultRg*2 - r_ave*r_ave);
                        // solve for r_ave
                        System.out.println("fixed ALPHA " + alpha + " | beta " + beta + " rave " + r_ave);
                    } else {
                        throw new NumberFormatException();
                    }

                } catch(NumberFormatException nfe) {
                    additionalInfoPrTextField.setText(twoOneFormat.format(defaultRg));
                    infoLabel.setText("Incorrect Rg value, setting to default");
                }
            }

        });


        additionalInfoPrPanel.add(additionalInfoPrTextField);

        fixValue = new JCheckBox("Fix?", false);

        fixValue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fixValue.isSelected()){
                    isFixed = true;
                    System.out.println("FIXED RG VALUE");
                } else {
                    isFixed = false;
                }
            }
        });
        fixValue.setBorder(BorderFactory.createEmptyBorder());
        additionalInfoPrPanel.add(fixValue);
    }

    /* Static 'instance' method */
    public static LaguerreParamsSingleton getInstance( ) {
        return singleton;
    }

    public JPanel getPanel(){

        return additionalInfoPrPanel;
    }

    public void setJLabel(JLabel info){
        this.infoLabel = info;
    }

    public void setIsSelected(boolean value){
        isSelected = value;
    }

    public boolean getIsSelected(){
        return isSelected;
    }

    public boolean getIsFixed(){ return isFixed;}

    public void setIsFixed(boolean value){
        isFixed = value;
    }

    public double getDefaultRg(){return defaultRg;}
    public double getR_ave(){return r_ave;}

    public double getRgLimit(){ return rgLimit;}
}
