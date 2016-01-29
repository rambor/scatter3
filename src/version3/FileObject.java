package version3;


import org.jfree.data.xy.XYSeries;

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
            out.write(String.format("REMARK 265 COLUMNS : q, I(q), error%n"));
            out.write(String.format("REMARK 265 q : defined in inverse Angstroms%n"));

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

            out.write(String.format("REMARK 265 EXPERIMENTAL DETAILS%n"));
            out.write(this.createNotesRemark(data));
            out.write(this.createBufferRemark(data));
            out.write(String.format("REMARK 265  COLUMNS : q, I(q), error%n"));
            out.write(String.format("REMARK 265  q : defined in inverse Angstroms%n"));

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


    public void writePRFile(String name){

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

        String newLines ="";
        String[] arrayOfLines = temp.replaceAll("\r\n", "\n").split("\n");
        if (arrayOfLines.length > 0){
            newLines += String.format("REMARK 265%n");

            int total = arrayOfLines.length;
            for(int i=0;i<total;i++){
                newLines += String.format("REMARK 265  NOTE : %s %n", arrayOfLines[i].trim());
            }
            newLines += String.format("REMARK 265%n");
        }
        return newLines;
    }

    private String createBufferRemark(Dataset data){
        String temp = data.getBufferComposition();
        // split at carriage return
        // add REMARK
        String newLines="";
        String[] arrayOfLines = temp.replaceAll("\r\n", "\n").split("\n");

        if (arrayOfLines.length > 0 && arrayOfLines[0].length() > 0){

            newLines = String.format("REMARK 265 BUFFER COMPOSITION %n");
            newLines += String.format("REMARK 265%n");

            int total = arrayOfLines.length;
            for(int i=0;i<total;i++){
                newLines += String.format("REMARK 265  BUFFER : %s %n", arrayOfLines[i].trim());
            }
            newLines += String.format("REMARK 265%n");
        }

        return newLines;
    }


}
