package version3;

/**
 * Created by robertrambo on 05/01/2016.
 */

import com.sun.org.apache.xml.internal.utils.LocaleUtility;
import org.jfree.data.xy.XYSeries;
import sun.util.locale.LocaleUtils;

import javax.swing.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Read data from file, must be text file with 3 columns
 * @author R. Rambo
 */
public class LoadedFile {
    public XYSeries allData;
    public XYSeries allDataError;
    private String ext;
    public String filebase;
    private Pattern dataFormat = Pattern.compile("(-?[0-9].[0-9]+[Ee][+-]?[0-9]+)|(-?[0-9]+.[0-9]+)");
    //private Pattern nonDataFormat = Pattern.compile("[A-Z]+");
    private Locale loc = Locale.getDefault(Locale.Category.FORMAT);
    private boolean isUSUK = false;
    private DecimalFormat df = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.US));
    // add parameters for reading header from txt file
    // REMARK   INFO

    //Constructor
    public LoadedFile(File file, int index, boolean convert) throws Exception {

        if (loc.toString().equals("en_GB") || loc.toString().equals("en_US")){
            isUSUK = true;
        }

        System.out.println("Default location " + Locale.getDefault(Locale.Category.FORMAT) + " isUSUK " + isUSUK);

        // get file base and extension
        String[] filename = file.getName().split("\\.(?=[^\\.]+$)");
        filebase = filename[0];
        ext = filename[1];
        String keyName = Integer.toString(index) + filebase; // helps keep file names unique so we can load same file multiple times

        allData = new XYSeries(keyName, false, false);
        allDataError = new XYSeries(keyName, false, false);
        double tempQValue;

        try {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            long filesize = file.length();

            if (filesize == 0){
                throw new Exception("File Empty");
            }

            if (ext.equals("dat") || ext.equals("int") || ext.equals("txt") || ext.equals("csv") ) { //regular 3 column file space or tab delimited
                String strLine;

                //Read file line-by-line
                try {
                    DataLine dataPoints;
                    while ((strLine = br.readLine()) != null) {
                        dataPoints = dataFromText(strLine);
                        if (dataPoints.getTest()){

                            if (!ext.equals("fit")){

                                tempQValue = (convert) ? dataPoints.getq()/10 : dataPoints.getq();
                                allData.add(tempQValue, dataPoints.getI() );
                                allDataError.add(tempQValue, dataPoints.getE() );

                            }

                        } else if (checkRemark(strLine)){ // check if header without BUFFER LINE

                        } // move to next line
                    }

                    //endPtNN = originalNNData.getItemCount();
                } catch (IOException ex) {
                    System.err.println("File Index out of bounds");
                }
            } else {
                String message = "Incorrect file extension : " + filename;
                throw new IllegalArgumentException(message);
            }
            // might have a cansas format, open file and read contents
            br.close();
            in.close();
            fstream.close();

        } catch (FileNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }


    public LoadedFile(File file, JLabel jLabel, int ssize, boolean convert) throws Exception{

        if (loc.toString().equals("en_GB") || loc.toString().equals("en_US")){
            isUSUK = true;
        }
        //System.out.println("Default location " + Locale.getDefault(Locale.Category.FORMAT) + " isUSUK " + isUSUK);

        // get file base and extension
        String[] filename = file.getName().split("\\.(?=[^\\.]+$)");
        filebase = filename[0];
        ext = filename[1];
        String keyName = Integer.toString(ssize) + filebase; // helps keep file names unique so we can load same file multiple times

        allData = new XYSeries(keyName, false, false);
        allDataError = new XYSeries(keyName, false, false);
        double tempQValue;

        try {

            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            long filesize = file.length();
            if (filesize == 0){
                throw new Exception("File Empty");
            }

            if (ext.equals("dat") || ext.equals("fit") || ext.equals("int") || ext.equals("txt") || ext.equals("csv")) { //regular 3 column file space or tab delimited
                String strLine;

                //Read file line-by-line
                try {

                    if (ext.equals("fit")){

                        //DataLine dataPoint;
                        while ((strLine = br.readLine()) != null) {
                            DataLine dataPoint = dataFromText(strLine);
                            if (dataPoint.getTest()) {
                                tempQValue = (convert) ? dataPoint.getq() / 10 : dataPoint.getq();

                                allData.add(tempQValue, dataPoint.getE() ); // third column is actual model intensity (2nd is data)
                                allDataError.add(tempQValue, dataPoint.getE()*0.05);
                                //allData.add(tempQValue, dataPoint.getI());
                                //allDataError.add(dataPoint.getq(), dataPoint.getE()*0.05);
                                //allDataError.add(tempQValue, dataPoint.getE());
                            }
                        }

                    } else {

                        //long start = System.nanoTime();
                        int count = 0;
                        //DataLine dataPoint;
                        while ((strLine = br.readLine()) != null) {

                            DataLine dataPoint = dataFromText(strLine);
                            if (dataPoint.getTest()){
                                tempQValue = (convert) ? dataPoint.getq() / 10 : dataPoint.getq();
                                allData.addOrUpdate(tempQValue, dataPoint.getI());
                                allDataError.addOrUpdate(tempQValue,  dataPoint.getE());
                                count++;

                                if (allData.getItemCount() != count){
                                    System.err.println(count + "(" + allData.getItemCount() + ")" + " POSSIBLE DUPLICATE ENTRIES SEE LINE => " + strLine);
                                }
                            }

//                            else if (checkRemark(strLine)){ // check if header without BUFFER LINE
//
//                            } // move to next line
                        }

                        // take the longest range of positive values, excluding negatives?


//                        if (allData.getItemCount() != count){
//                            System.out.println("POSSIBLE DUPLICATE ENTRIES: READ " + count + " LINES in => " + (System.nanoTime() - start)/1000 + " nanoseconds");
//                        }
                    }

                } catch (IOException ex) {
                    System.err.println("File Index out of bounds");
                }
            } else if (ext.equals("pdb")) { // read in PDB file, make intensity from P(r)?

            } else {
                String message = "Incorrect file extension : " + filename;
                throw new IllegalArgumentException(message);
            }
            // might have a cansas format, open file and read contents
            br.close();
            in.close();
            fstream.close();
        } catch (FileNotFoundException ex) {
            jLabel.setText("File is empty");
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private boolean checkRemark(String line){
        if (line.startsWith("REMARK")){
            return true;
        }
        return false;
    }

    private boolean checkBuffer(String line){
        if (line.contains("BUFFER")){
            return true;
        }
        return false;
    }



    private DataLine dataFromText(String line){

        DataLine data;
        String newString;
        String trimmed;
        String[] row;
        newString = line.replaceAll( "[\\s\\t]+", " " );
        trimmed = newString.trim();
        row = trimmed.split("\\s|;");
        // if row[0] and row[1] contain commas, then we are assuming comma is a decimal delimiter
        // Denmark, Sweden, Finland, Netherlands, Spain, Germany, France use comma

        //NumberFormat nf = NumberFormat.getNumberInstance(loc);
        //DecimalFormat df = (DecimalFormat)nf;
        //df.applyPattern("#.##");
        // df.format() returns a string
        // System.out.println("LOCALE " + loc); // en_GB, en_US

        if ( (!trimmed.contains("#") && (row.length >= 2 && row.length <= 5) &&
                !row[0].matches("^[A-Za-z#:_\\/$%*!\\'-].+") &&  // checks for header or footer stuff
                !isZero(row[0]) &&                               // no zero q values
                !isZero(row[1]) &&                               // no zero I(q) values
                isNumeric(row[0]) &&                             // check that value can be parsed as Double
                isNumeric(row[1]) &&                             // check that value can be parsed as Double
                dataFormat.matcher(row[0]).matches() &&          // format must be either scientific with E or decimal
                dataFormat.matcher(row[1]).matches() ))          // format must be either scientific with E or decimal
        {

            //Double iofQValue = Double.valueOf(df.format(Double.parseDouble(row[1])));
            if (!isUSUK){
                //System.out.println("Not USUK : may convert format ");

                if (row[0].contains(",") && row[1].contains(",")){ // convert
                    System.out.println("Number contains a comma in first column, convert format " + line);
                    data = new DataLine(convertToUS(row[0]), convertToUS(row[1]), 1.0, true);
                    if ((row.length == 3 && isNumeric(row[2])) || (row.length == 4 && isNumeric(row[2]))) {
                        data.setE(convertToUS(row[2]));
                    }
                } else {
                    data = new DataLine(Double.parseDouble(row[0]), Double.parseDouble(row[1]), 1.0, true);
                    if ((row.length == 3 && isNumeric(row[2])) || (row.length == 4 && isNumeric(row[2]))) {
                        data.setE(Double.parseDouble(row[2]));
                    }
                }

            } else {
                // default error is 1.0
                // if 2nd row present, we set to value in row[2]
                data = new DataLine(Double.parseDouble(row[0]), Double.parseDouble(row[1]), 1.0, true);
                // if data file is only [q, I(q)], then missing row[2]
                if ((row.length == 3 && isNumeric(row[2])) || (row.length == 4 && isNumeric(row[2]))) {
                    data.setE(Double.parseDouble(row[2]));
                }
            }

        } else {  // this is required to tell me that the data line created is false
            //System.out.println("REJECTING LINE: " + line);
            data = new DataLine(0,0,0,false);
        }
        return data;
    }


    private boolean isNumeric(String str) {

        if (hasOnlyComma(str) && !isUSUK){
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            Number number = null;
            try {
                number = format.parse(str);
            } catch (ParseException e) {
                return false;
            }
        } else {
            try {
                double d = Double.parseDouble(str);
            } catch(NumberFormatException nfe) {
                return false;
            }
        }

        return true;
    }


    private boolean hasOnlyComma(String str) {

        if (str.contains(",") && !str.contains(".")) {
            return true;
        }
        return false;
    }

    private boolean isZero(String str){
        // 0.000 or 0,000 or 0.00E0
        if (hasOnlyComma(str) && !isUSUK){

            if (convertToUS(str) <= 0){
                return true;
            }
        } else if (isUSUK) {

            if (Float.parseFloat(str) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * convert comma delimited decimal to US standard
     * @param str
     * @return
     */
    private double convertToUS(String str){
        //NumberFormat format = NumberFormat.getInstance(new Locale("es", "ES"));
        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        Number number = null;
        // data may contain only a decimal point eventhough it is on non-ideal key board

        try {
            number = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("FormatParser : " + str + " => " + number + " parsed => " + number.doubleValue());
        return number.doubleValue();
    }

    private class RemarkInfo{

        public RemarkInfo(){

        }
    }


}
