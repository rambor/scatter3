package version3;


import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by robertrambo on 16/01/2016.
 */
public class FileObject {

    private File directoryInfo;
    private String note;
    private String buffer;

    public FileObject(File directory){
        this.directoryInfo = directory;
    }

    public void writeSAXSFile(String name, Dataset data){
        int total = data.getAllData().getItemCount();
        XYSeries refData = data.getAllData();
        XYSeries errorValues =data.getAllDataError();

        try {
            FileWriter fw = new FileWriter(directoryInfo +"/"+name+".dat");
            BufferedWriter out = new BufferedWriter(fw);

            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
            out.write(this.createNotesRemark(data));
            out.write(this.createBufferRemark(data));

            if (data.getGuinierIzero() > 0 && data.getGuinierRg() > 0){
                out.write(prIqheader(data));
            }

            out.write(String.format("REMARK 265    COLUMNS : q, I(q), error%n"));
            out.write(String.format("REMARK 265          q : defined in inverse Angstroms%n"));

            int numberOfDigits;
            for (int n=0; n < total; n++) {

                numberOfDigits = getDigits(refData.getX(n).doubleValue());
                out.write( String.format("%s\t%s\t%s %n", formattedQ(refData.getX(n).doubleValue(), numberOfDigits), Constants.Scientific1dot5e2.format(refData.getY(n).doubleValue()),Constants.Scientific1dot5e2.format(errorValues.getY(n).doubleValue()) ));
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeSingleSAXSFile(String name, Dataset data){
        int total = data.getAllData().getItemCount();
        int startAt = data.getStart()-1;
        int endAt = data.getEnd();
        XYSeries refData = data.getAllData();
        XYSeries errorValues =data.getAllDataError();

        try {
            FileWriter fw = new FileWriter(directoryInfo +"/"+name+".dat");
            BufferedWriter out = new BufferedWriter(fw);
            if (data.getGuinierIzero() > 0 && data.getGuinierRg() > 0){
                out.write(prIqheader(data));
            }
            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
            out.write(this.createNotesRemark(data));
            out.write(this.createBufferRemark(data));
            out.write(String.format("REMARK 265    COLUMNS : q, I(q), error%n"));
            out.write(String.format("REMARK 265          q : defined in inverse Angstroms%n"));

            int numberOfDigits;
            for (int n=startAt; n < endAt; n++) {
                numberOfDigits = getDigits(refData.getX(n).doubleValue());
                out.write( String.format("%s\t%s\t%s %n", formattedQ(refData.getX(n).doubleValue(), numberOfDigits), Constants.Scientific1dot5e2.format(refData.getY(n).doubleValue()),Constants.Scientific1dot5e2.format(errorValues.getY(n).doubleValue()) ));
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dataset
     * @param status
     * @param filename
     * @param workingDirectoryName
     * @return file name for intensity file
     */
    public String writePRFile(Dataset dataset, JLabel status, String filename, String workingDirectoryName, boolean isRefined){
        RealSpace realspaceModel = dataset.getRealSpaceModel();
        double dmax = realspaceModel.getDmax();
        XYSeries r_pr = new XYSeries("r_pr");
        XYDataItem tempXY;

        status.setText("Writing P(r) and plotted I(q) to file: " + filename);

        double incr = dmax/101.0;
        double[] coefs = realspaceModel.getMooreCoefficients();
        int coefsSize = coefs.length;

        for (int r = 0; r*incr <= dmax; r++){
            double r_incr = r*incr;
            r_pr.add(r_incr, realspaceModel.calculatePofRAtR(r_incr));
        }

        // clean-up file name
        String[] base = filename.split("\\.");

        FileWriter fstream;

        try{ // create P(r) file
            // Create file
            if (isRefined){
                fstream = new FileWriter(workingDirectoryName+ "/" + base[0] + "_refined_pr.dat");
            } else {
                fstream = new FileWriter(workingDirectoryName+ "/" + base[0] + "_pr.dat");
            }
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(createScatterHeader());
            out.write("REMARK 265 EXPERIMENTAL REAL SPACE FILE \n");
            out.write("REMARK 265    P(r)-DISTRIBUTION BASED ON : " + dataset.getFileName() + "\n");
            out.write("REMARK 265 \n");
            out.write(prIqheader(dataset));
            out.write("REMARK 265 \n");
            out.write("REMARK 265  MOORE COEFFICIENTS (UNSCALED)\n");
            String newLine=String.format("REMARK 265      CONSTANT BACKGROUND a(0) : %.3E %n", coefs[0]);
            for (int i=1; i<coefsSize;i++){
                newLine += String.format("REMARK 265                        a_(%2d) : %.3E %n", i, coefs[i]);
            }

            out.write(newLine);

            out.write("REMARK 265 \n");
            out.write("REMARK 265  SCALED P(r) DISTRIBUTION \n");
            out.write(String.format("REMARK 265      SCALE : %.3E %n", realspaceModel.getScale()));
            out.write("REMARK 265    COLUMNS : r, P(r), error\n");
            out.write(String.format("REMARK 265          r : defined in Angstroms%n"));
            for(int i =0; i < r_pr.getItemCount(); i++){
                out.write( Constants.Scientific1dot2e1.format(r_pr.getX(i).doubleValue()) + "\t" + Constants.Scientific1dot2e1.format(r_pr.getY(i).doubleValue()*realspaceModel.getScale()) + "\t 0.00 "+ "\n");
            }
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }


        String sx_filename="";

        try{
            // Create file
            if (isRefined){
                sx_filename =  base[0]+"_refined_sx.dat";
                fstream = new FileWriter(workingDirectoryName+ "/" +sx_filename);
            } else {
                sx_filename = base[0]+"_sx.dat";
                fstream = new FileWriter(workingDirectoryName+ "/" +sx_filename);
            }

            BufferedWriter out = new BufferedWriter(fstream);
            out.write(createScatterHeader());
            out.write(String.format("REMARK 265  DATASET MAY CONTAIN FEWER POINTS THAN ORIGINAL DATA : %s %n", dataset.getFileName()));
            out.write("REMARK 265  REFINEMENT OR MANUAL TRIMMING OF SCATTERING CURVE MAY PRODUCE FEWER DATA. \n");

            if (isRefined){
                int total = dataset.getAllData().getItemCount();
                double percentRejected = dataset.getRealSpaceModel().getRefinedqIData().getItemCount()/(double)total*100.0;
                out.write(String.format("REMARK 265                  PERCENT KEPT : %.3f %n", percentRejected));
            }

            out.write("REMARK 265  \n");
            out.write(prIqheader(dataset));
            out.write(String.format("REMARK 265    COLUMNS : q, I_OBS(q), error, I_CALC(q)%n"));
            out.write(String.format("REMARK 265          q : defined in inverse Angstroms%n"));

            int tempIndex = 0;

            double invScaleFactor = 1.0/realspaceModel.getRescaleFactor();
            double iCalc;

            if (isRefined){
                XYSeries tempXYSeries = realspaceModel.getRefinedqIData();
                for(int i =0; i < tempXYSeries.getItemCount(); i++){
                    tempXY = tempXYSeries.getDataItem(i);
                    tempIndex = dataset.getAllData().indexOf(tempXY.getX());  // gets unscale SAXS curve that originated the P(r)
                    iCalc = realspaceModel.moore_Iq(tempXY.getXValue())*invScaleFactor;

                    if (tempIndex > 0) {
                        out.write(Constants.Scientific1dot5e2.format(tempXY.getXValue()) + "\t" +
                                Constants.Scientific1dot5e2.format(tempXY.getYValue()) + "\t" +
                                Constants.Scientific1dot5e2.format(dataset.getAllDataError().getY(tempIndex)) + "\t" +
                                Constants.Scientific1dot5e2.format(iCalc) +  "\n");
                    }
                }
            } else {

                for(int i =0; i < realspaceModel.getfittedqIq().getItemCount(); i++){

                    tempXY = realspaceModel.getfittedqIq().getDataItem(i);
                    tempIndex = dataset.getAllData().indexOf(tempXY.getX());  // gets unscale SAXS curve that originated the P(r)
                    iCalc = realspaceModel.moore_Iq(tempXY.getXValue())*invScaleFactor;

                    if (tempIndex > 0) {
                        out.write(Constants.Scientific1dot5e2.format(tempXY.getXValue()) + "\t" +
                                Constants.Scientific1dot5e2.format(tempXY.getYValue()) + "\t" +
                                Constants.Scientific1dot5e2.format(dataset.getAllDataError().getY(tempIndex)) + "\t" +
                                Constants.Scientific1dot5e2.format(iCalc) +  "\n");
                    }
                }
            }
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        //status.setText("Files written to " + workingDirectoryName + ", ready to run DAMMIN/F");
        //runDatGnom(base[0] + "_sx.dat", dataset.getRealRg());
        return sx_filename;
    }

    private String prIqheader(Dataset dataset){
        String newLines = String.format("REMARK 265 SAXS DERIVED PARAMETERS%n");
        newLines += String.format("REMARK 265%n");
        double scaledGuinierIzero = dataset.getGuinierIzero()*dataset.getScaleFactor();
        newLines += String.format("REMARK 265  RECI REFERS TO RECIPROCAL SPACE VALUES DERIVED FROM GUINIER ANALYSIS%n");
        newLines += String.format("REMARK 265  REAL REFERS TO REAL SPACE VALUES DERIVED FROM P(R)-DISTRIBUTION%n");
        double diff = 100*Math.abs(dataset.getRealIzero() - scaledGuinierIzero)/(0.5*(dataset.getRealIzero() + scaledGuinierIzero));
        newLines += String.format("REMARK 265                     REAL I(0) : %.3E %n", dataset.getRealIzero());
        newLines += String.format("REMARK 265                     RECI I(0) : %.3E %n", dataset.getGuinierIzero());
        newLines += String.format("REMARK 265            PERCENT DIFFERENCE : %.3f %n", diff);

        diff = 100*Math.abs(dataset.getRealRg() - dataset.getGuinierRg())/(0.5*dataset.getRealRg() + dataset.getGuinierRg());
        newLines += String.format("REMARK 265                       REAL Rg : %.3E (Angstroms)%n", dataset.getRealRg());
        newLines += String.format("REMARK 265                       RECI Rg : %.3E (Angstroms)%n", dataset.getGuinierRg());
        newLines += String.format("REMARK 265            PERCENT DIFFERENCE : %.3f %n", diff);
        newLines += String.format("REMARK 265                        VOLUME : %d (Angstroms^3) %n", dataset.getPorodVolume());
        newLines += String.format("REMARK 265          POROD EXPONENT (P_E) : %.3f %n", dataset.getPorodExponent());
        newLines += String.format("REMARK 265                      REAL <r> : %.3f (Angstroms)%n", dataset.getAverageR());
        newLines += String.format("REMARK 265                          DMAX : %d (Angstroms) %n", (int)dataset.getDmax());
        return newLines;
    }

    public void writeRefinedDataToFile(){

    }


    private int getDigits(double qvalue) {
        String toText = Double.toString(qvalue);
        int integerPlaces = toText.indexOf('.');
        int decimalPlaces;

        String[] temp = toText.split("\\.0*");
        decimalPlaces = (temp.length == 2) ? temp[1].length() : (toText.length() - integerPlaces -1);

        return decimalPlaces;
    }

    public String formattedQ(double qvalue, int numberOfDigits) {
        String numberToPrint ="";
        switch(numberOfDigits){
            case 7: numberToPrint = String.format(Locale.US, "%.6E", qvalue);
                break;
            case 8: numberToPrint = String.format(Locale.US, "%.7E", qvalue);
                break;
            case 9: numberToPrint = String.format(Locale.US, "%.8E", qvalue);
                break;
            case 10: numberToPrint = String.format(Locale.US, "%.9E", qvalue);
                break;
            case 11: numberToPrint = String.format(Locale.US,"%.10E", qvalue);
                break;
            case 12: numberToPrint = String.format(Locale.US, "%.11E", qvalue);
                break;
            case 13: numberToPrint = String.format(Locale.US, "%.12E", qvalue);
                break;
            case 14: numberToPrint = String.format(Locale.US, "%.13E", qvalue);
                break;
            default: numberToPrint = String.format(Locale.US,"%.6E", qvalue);
                break;
        }
        return numberToPrint;
    }

    private String createNotesRemark(Dataset data){
        String temp = data.getExperimentalNotes();
        // split at carriage return
        // add REMARK

        String newLines = String.format("REMARK 265 EXPERIMENTAL INFO%n");
        newLines += String.format("REMARK 265  INFORMATION CAN BE ADDED BY DUPLICATING THE NOTE LINE BELOW%n");
        String[] arrayOfLines = temp.replaceAll("\r\n", "\n").split("\n");
        if (arrayOfLines.length > 0){
            newLines += String.format("REMARK 265%n");
            int total = arrayOfLines.length;
            for(int i=0;i<total;i++){
                newLines += String.format("REMARK 265  NOTE : %s %n", arrayOfLines[i].trim());
            }
            newLines += String.format("REMARK 265%n");
        } else {
            for(int i=0;i<3;i++){
                newLines += String.format("REMARK 265  NOTE : %s %n", "NO INFORMATION PROVIDED");
            }
        }
        return newLines;
    }

    private String createBufferRemark(Dataset data){
        String temp = data.getBufferComposition();
        // split at carriage return
        // add REMARK
        String tempHeader="";
        String[] arrayOfLines = temp.replaceAll("\r\n", "\n").split("\n");
        tempHeader = String.format("REMARK 265 BUFFER COMPOSITION %n");
        tempHeader += "REMARK 265          TEMPERATURE (KELVIN) : 298\n";
        if (arrayOfLines.length > 0 && arrayOfLines[0].length() > 0){

            int total = arrayOfLines.length;
            for(int i=0;i<total;i++){
                tempHeader += String.format("REMARK 265                 SAMPLE BUFFER : %s %n", arrayOfLines[i].trim());
            }

        } else {
            // example
            tempHeader += "REMARK 265                            PH : X.X\n";
            tempHeader += "REMARK 265                 SAMPLE BUFFER : XXX mM HEPES\n";
            tempHeader += "REMARK 265                 SAMPLE BUFFER : XXX mM KCl\n";
            tempHeader += "REMARK 265                 SAMPLE BUFFER : X mM TCEP\n";
            tempHeader += "REMARK 265                 SAMPLE BUFFER : X mM KNitrate\n";
        }
            tempHeader += String.format("REMARK 265%n");
        return tempHeader;
    }

    private String createScatterHeader(){
        String tempHeader="REMARK 265 \n";
        tempHeader += "REMARK 265 EXPERIMENT TYPE : X-RAY SOLUTION SCATTERING\n";
        tempHeader += "REMARK 265 DATA ACQUISITION\n";
        tempHeader += "REMARK 265              RADIATION SOURCE : BENDING MAGNET\n";
        tempHeader += "REMARK 265             SYNCHROTRON (Y/N) : Y\n";
        tempHeader += "REMARK 265                      BEAMLINE : B21 DIAMOND LIGHT SOURCE\n";
        tempHeader += "REMARK 265\n";
        tempHeader += "REMARK 265       DATA REDUCTION SOFTWARE : SCATTER (v3.0)\n";
        tempHeader += "REMARK 265               SOFTWARE AUTHOR : RP RAMBO\n";
        tempHeader += "REMARK 265        DATA ANALYSIS SOFTWARE : SCATTER (v3.0)\n";
        tempHeader += "REMARK 265               SOFTWARE AUTHOR : RP RAMBO\n";
        tempHeader += "REMARK 265\n";
        return tempHeader;
    }

}
