package version3;


import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import version3.sasCIF.*;

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
    private JComboBox source;
    private String instrument = "BENDING MAGNET";
    private String synchrotron = "Y";
    private String version = "";
    private String beamlineManufacturer = "B21 DIAMOND LIGHT SOURCE";

    public FileObject(File directory){
        this.directoryInfo = directory;
    }
    public FileObject(File directory, String version){
        this.directoryInfo = directory; this.version = version;
    }

    public void setSource(JComboBox box, String nameOf){
        this.source = box;
        this.beamlineManufacturer = nameOf;
        this.instrument = (String)source.getSelectedItem();
        if (instrument.contains("HOME") || instrument.contains("OTHER")){
            synchrotron = "N";
        }
    }

    public void writeSAXSFile(String name, Dataset data){

        int total = data.getAllData().getItemCount();
        XYSeries refData = data.getAllData();
        XYSeries errorValues =data.getAllDataError();

        try {
            FileWriter fw = new FileWriter(directoryInfo +"/"+name+".dat");
            BufferedWriter out = new BufferedWriter(fw);
            out.write(createScatterHeader());
            out.write(this.createNotesRemark(data));
            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
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


    public void writeXYSeries(String name, XYSeries xySeries){
        int total = xySeries.getItemCount();

        try {
            FileWriter fw = new FileWriter(directoryInfo +"/"+name+".dat");
            BufferedWriter out = new BufferedWriter(fw);
            out.write(String.format("# %s %n", name));

            int numberOfDigits;
            for (int n=0; n < total; n++) {
                numberOfDigits = getDigits(xySeries.getX(n).doubleValue());
                out.write( String.format("%s\t%s%n",
                        formattedQ(xySeries.getX(n).doubleValue(), numberOfDigits),
                        Constants.Scientific1dot5e2.format(xySeries.getY(n).doubleValue())
                ));
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeSingleSAXSFile(String name, Dataset data){

        int startAt = data.getStart()-1;
        int endAt = data.getEnd();
        XYSeries refData = data.getAllData();
        XYSeries errorValues =data.getAllDataError();

        try {
            FileWriter fw = new FileWriter(directoryInfo +"/"+name+".dat");
            BufferedWriter out = new BufferedWriter(fw);

            out.write(createScatterHeader());
            out.write(this.createNotesRemark(data));
            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
            out.write(this.createBufferRemark(data));

            if (data.getGuinierIzero() > 0 && data.getGuinierRg() > 0){
                out.write(prIqheader(data));
            }

            out.write(String.format("REMARK 265    COLUMNS : q, I(q), error%n"));
            out.write(String.format("REMARK 265          q : defined in inverse Angstroms%n"));

            int numberOfDigits;
            for (int n=startAt; n < endAt; n++) {
                numberOfDigits = getDigits(refData.getX(n).doubleValue());
                out.write( String.format("%s\t%s\t%s %n", formattedQ(refData.getX(n).doubleValue(), numberOfDigits), Constants.Scientific1dot5e2.format(refData.getY(n).doubleValue()*data.getScaleFactor()),Constants.Scientific1dot5e2.format(errorValues.getY(n).doubleValue()*data.getScaleFactor()) ));
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String writeCIFFile(Dataset dataset, JLabel status, String filename, String workingDirectoryName, boolean isRefined){

        status.setText("Writing COMBO CIF FILE to file: " + filename);

        if (dataset.getRealSpaceModel().getRg() > 0 && dataset.getRealSpaceModel().getIzero() > 0){
            dataset.getRealSpaceModel().estimateErrors(); // this function sets the spline function in indirectFT object
        }


        // clean-up file name
        String[] base = filename.split("\\.");

        FileWriter fstream;

        try{ // create P(r) file
            // Create file
            fstream = new FileWriter(workingDirectoryName+ "/" + base[0] + ".cif");
            BufferedWriter out = new BufferedWriter(fstream);

            SasSet sasset = new SasSet(dataset);

            SasDetails details = new SasDetails(dataset);
            out.write(details.getText());

            SasResult results = new SasResult(dataset);
            out.write(results.getTextForOutput(1));

            SasPofr pofr = new SasPofr(dataset);
            //out.write(pofr.getTextOfFittedDataForOutput());

            SasIntensities iofq = new SasIntensities(dataset);
            out.write(iofq.getTextOfFittedDataForOutput(isRefined));
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        String sx_filename="";
        return sx_filename;
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
        XYSeries r_pr = new XYSeries("r_pr");
        XYDataItem tempXY;

        status.setText("Writing P(r) and plotted I(q) to file: " + filename);


        if (dataset.getRealSpaceModel().getRg() > 0 && dataset.getRealSpaceModel().getIzero() > 0){
            dataset.getRealSpaceModel().estimateErrors(); // this function sets the spline function in indirectFT object
        }


        if (realspaceModel.isPDB()){

            int totalr = realspaceModel.getPrDistribution().getItemCount();
            for(int r=0; r<totalr; r++){
                r_pr.add(realspaceModel.getPrDistribution().getDataItem(r));
            }

        } else {

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
            out.write(this.createNotesRemark(dataset));
            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
            out.write(this.createBufferRemark(dataset));
            out.write(prIqheader(dataset));
            String modelText = dataset.getRealSpaceModel().getIndirectFTModel().getHeader(dataset.getRealSpaceModel().getScale());
            out.write(modelText);
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        /*
         * write denss file format
         */

        try {

            String denss_name =  base[0]+"_denns.dat";
            FileWriter fstreamd = new FileWriter(workingDirectoryName+ "/" +denss_name);
            BufferedWriter out = new BufferedWriter(fstreamd);

            if (isRefined){

                XYSeries tempXYSeries = realspaceModel.getRefinedqIData();
                // estimate delta_q
                double delta_q=0;
                double counter = 0;
                for(int i =1; i < tempXYSeries.getItemCount(); i++){
                    tempXY = tempXYSeries.getDataItem(i-1);
                    XYDataItem tempXY2 =tempXYSeries.getDataItem(i);
                    delta_q += tempXY2.getXValue() - tempXY.getXValue();
                    counter += 1.0;
                }
                delta_q *= 1.0/counter;
                double startq = 0.0d;
                double stopq = tempXYSeries.getMinX();

                // estimate errors from first 51 points
                double errorEstimate=0;
                for(int i=0; i<51; i++){
                    errorEstimate += Math.abs(dataset.getAllDataError().getY(i).doubleValue()/dataset.getAllData().getY(i).doubleValue());
                }
                errorEstimate *= 1.0/(double)51;

                // I(zero)
                out.write(Constants.Scientific1dot5e2.format(0.00) + "\t" +
                        Constants.Scientific1dot5e2.format(realspaceModel.getIzero()) +"\t" +
                        Constants.Scientific1dot5e2.format(realspaceModel.getIzero()*errorEstimate) + "\n");

                // extrapolate low-q
                while( startq < stopq){
                    startq += delta_q;
                    double iCalc = realspaceModel.extrapolateToLowQ(startq);
                    out.write(Constants.Scientific1dot5e2.format(startq) + "\t" +
                            Constants.Scientific1dot5e2.format(iCalc) +"\t" +
                            Constants.Scientific1dot5e2.format(iCalc*errorEstimate) + "\n");
                }

                // add remaining data
                for(int i =0; i < tempXYSeries.getItemCount(); i++){
                    tempXY = tempXYSeries.getDataItem(i);
                    int tempIndex = dataset.getAllData().indexOf(tempXY.getX());   // gets unscale SAXS curve that originated the P(r)
                    double iCalc = realspaceModel.getICalcAtQ(tempXY.getXValue());

                    if (tempIndex > 0) {
                        out.write(Constants.Scientific1dot5e2.format(tempXY.getXValue()) + "\t" +
                                Constants.Scientific1dot5e2.format(iCalc)  + "\t" +
                                Constants.Scientific1dot5e2.format(dataset.getAllDataError().getY(tempIndex)) + "\n");
                    }
                }
                //Close the output stream
                out.close();
            }

        } catch (Exception e){//Catch exception if any

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
            out.write(this.createNotesRemark(dataset));
            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
            out.write(this.createBufferRemark(dataset));

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

            double iCalc;

            if (isRefined){
                XYSeries tempXYSeries = realspaceModel.getRefinedqIData();
                for(int i =0; i < tempXYSeries.getItemCount(); i++){
                    tempXY = tempXYSeries.getDataItem(i);
                    tempIndex = dataset.getAllData().indexOf(tempXY.getX());  // gets unscale SAXS curve that originated the P(r)
                    iCalc = realspaceModel.getICalcAtQ(tempXY.getXValue());

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
                    //tempXY = realspaceModel.getfittedIq().getDataItem(i);
                    tempIndex = dataset.getAllData().indexOf(tempXY.getX());  // gets unscale SAXS curve that originated the P(r)
                    iCalc = realspaceModel.getICalcAtQ(tempXY.getXValue());

                    if (tempIndex > 0) {
                        out.write(Constants.Scientific1dot5e2.format(tempXY.getXValue()) + "\t" +
                                //Constants.Scientific1dot5e2.format(tempXY.getYValue()/tempXY.getXValue()) + "\t" +
                                Constants.Scientific1dot5e2.format(dataset.getAllData().getY(tempIndex)) + "\t" +
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
        newLines += String.format("REMARK 265         REAL I(0) ERROR (+/-) : %.3E %n", dataset.getRealIzeroSigma());
        newLines += String.format("REMARK 265                     RECI I(0) : %.3E %n", dataset.getGuinierIzero());
        newLines += String.format("REMARK 265         RECI I(0) ERROR (+/-) : %.3E %n", dataset.getGuinierIzeroSigma());
        newLines += String.format("REMARK 265            PERCENT DIFFERENCE : %.3f %n", diff);

        diff = 100*Math.abs(dataset.getRealRg() - dataset.getGuinierRg())/(0.5*dataset.getRealRg() + dataset.getGuinierRg());
        newLines += String.format("REMARK 265                       REAL Rg : %.3E (Angstroms)%n", dataset.getRealRg());
        newLines += String.format("REMARK 265           REAL Rg ERROR (+/-) : %.3E (Angstroms)%n", dataset.getRealRgSigma());
        newLines += String.format("REMARK 265                       RECI Rg : %.3E (Angstroms)%n", dataset.getGuinierRg());
        newLines += String.format("REMARK 265           RECI Rg ERROR (+/-) : %.3E %n", dataset.getGuinierRG_sigma());
        newLines += String.format("REMARK 265            PERCENT DIFFERENCE : %.3f %n", diff);
        newLines += String.format("REMARK 265                        VOLUME : %d (Angstroms^3) %n", dataset.getPorodVolume());
        newLines += String.format("REMARK 265    Porod Volume Mass (d 1.11) : %d (Angstroms^3) RECI %n", dataset.getPorodVolumeMass1p1());
        newLines += String.format("REMARK 265    Porod Volume Mass (d 1.11) : %d (Angstroms^3) REAL %n", dataset.getPorodVolumeRealMass1p1());
        newLines += String.format("REMARK 265    Porod Volume Mass (d 1.37) : %d (Angstroms^3) RECI %n", dataset.getPorodVolumeMass1p37());
        newLines += String.format("REMARK 265    Porod Volume Mass (d 1.37) : %d (Angstroms^3) REAL %n", dataset.getPorodVolumeRealMass1p37());
        newLines += String.format("REMARK 265          POROD EXPONENT (P_E) : %.3f %n", dataset.getPorodExponent());
        newLines += String.format("REMARK 265                      REAL <r> : %.3f (Angstroms)%n", dataset.getAverageR());
        newLines += String.format("REMARK 265                          DMAX : %d (Angstroms) %n", (int)dataset.getDmax());
        newLines += String.format("REMARK 265   RESOLUTION LIMIT       QMAX : %.6E (Angstroms^-1) %n", dataset.getRealSpaceModel().getfittedqIq().getMaxX());
        newLines += String.format("REMARK 265   RESOLUTION LIMIT (d-SPACING): %.1f (Angstroms) %n", (2*Math.PI/dataset.getRealSpaceModel().getfittedqIq().getMaxX()));
        newLines += String.format("REMARK 265   RESOLUTION LIMIT  BIN WIDTH : %.1f (Angstroms) %n", (Math.PI/dataset.getRealSpaceModel().getfittedqIq().getMaxX()));
        return newLines;
    }

    public void exportResults(Collection collection){

        try {
            FileWriter fw = new FileWriter(new File(directoryInfo, "scatter_results.txt"));
            //fw = new FileWriter(workingDirectoryName.toString()+"/scatter_results.txt");
            BufferedWriter out = new BufferedWriter(fw);

            out.write("" +
                    "# COLUMNS  (2,3)   Izero(Guinier) ERROR \n" +
                    "# COLUMNS  (4,5)   Izero(Real) ERROR  \n" +
                    "# COLUMNS  (6,7)   Rg(Guinier) ERROR  \n" +
                    "# COLUMNS  (8,9)   Rg(Real) ERROR  \n" +
                    "# COLUMNS  (10)    Vc (Guinier)\n" +
                    "# COLUMNS  (11,12) Volume (Guinier) (Real) \n" +
                    "# COLUMNS  (13,14) Mass (if Protein) (if RNA) \n" +
                    "# COLUMNS  (15)    Average_r\n" +
                    "# COLUMNS  (16)    R_c\n" +
                    "# COLUMNS  (17,18) P_x Error\n" +
                    "# COLUMNS  (19)    d_max\n" +
                    "# COLUMNS  (20)    filename\n");

            Dataset temp;
            int count=1;
            for(int i=0; i<collection.getDatasets().size(); i++){
                temp = collection.getDataset(i);

                if (temp.getInUse()){

                    if (temp.getRealSpaceModel().getRg() > 0 && temp.getRealSpaceModel().getIzero() > 0){
                        temp.getRealSpaceModel().estimateErrors();
                    }

                    String outputLine = String.format("%-3d ", count);
                    outputLine += String.format("%.3E (+- %.3E) ", temp.getGuinierIzero(), temp.getGuinierIzeroSigma());
                    outputLine += String.format("%.3E (+- %.3E) ", temp.getRealIzero(), temp.getRealIzeroSigma());
                    outputLine += String.format("%5.2f (+- %4.2f) ", temp.getGuinierRg(), temp.getGuinierRG_sigma());
                    outputLine += String.format("%5.2f (+- %4.2f) ", temp.getRealRg(), temp.getRealRgSigma());
                    outputLine += String.format("%.3E ", temp.getVC());
                    outputLine += String.format("%.4E %.4E ", (double) temp.getPorodVolume(), (double) temp.getPorodVolumeReal());
                    outputLine += String.format("%.4E %.4E ", (double) temp.getMassProtein(), (double) temp.getMassRna());
                    outputLine += String.format("%5.2f ", temp.getAverageR());
                    outputLine += String.format("%5.2f ", temp.getRc());
                    outputLine += String.format("%2.3f (+- %1.3f) ", temp.getPorodExponent(),temp.getPorodExponentError());
                    outputLine += String.format("%5d %s %n", (int)temp.getDmax(), temp.getFileName());
                    out.write(outputLine);
                    count++;
                }

            }

            out.close();

        } catch (IOException e1) {
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
        tempHeader += String.format("REMARK 265              RADIATION SOURCE : %s %n", instrument);
        tempHeader += String.format("REMARK 265             SYNCHROTRON (Y/N) : %s %n", synchrotron);
        tempHeader += String.format("REMARK 265                      BEAMLINE : %s %n", beamlineManufacturer);
        tempHeader += "REMARK 265\n";
        tempHeader += "REMARK 265       DATA REDUCTION SOFTWARE : SCATTER (v "+version+")\n";
        tempHeader += "REMARK 265               SOFTWARE AUTHOR : RP RAMBO\n";
        tempHeader += "REMARK 265        DATA ANALYSIS SOFTWARE : SCATTER (v "+version+")\n";
        tempHeader += "REMARK 265               SOFTWARE AUTHOR : RP RAMBO\n";
        tempHeader += "REMARK 265\n";
        return tempHeader;
    }

    /**
     * create single file to be re-read by Scatter
     * @param name
     * @param data
     */
    private void createSCT(String name, Dataset data){
        // RECI_RG
        // RECI_RG_ERROR
        // RECI_IZERO
        // RECI_IZERO_ERROR
        //
        // RECIDATA =>
        // MOORE_COEFF   1 =>
        // MOORE_COEFF  11 =>
    }

}
