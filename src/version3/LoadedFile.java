package version3;

/**
 * Created by robertrambo on 05/01/2016.
 */

import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Read data from file, must be text file with 3 columns
 * @author R. Rambo
 */
public class LoadedFile {
    public XYSeries allData;
    public XYSeries allDataError;
    public String ext;
    public String filebase;
    //public int endPtNN;
    private ArrayList<String> fileLines;
    private Pattern dataFormat = Pattern.compile("(-?[0-9].[0-9]+[Ee][+-]?[0-9]+)|(-?[0-9]+.[0-9]+)");
    private Pattern nonDataFormat = Pattern.compile("[A-Z]+");
    private DataLine dataPoints;

    // add parameters for reading header from txt file
    // REMARK   INFO

    //Constructor
    public LoadedFile(File file, JLabel jLabel, int ssize, int sparse, boolean convert) throws Exception{

        // get file base and extension
        String[] filename = file.getName().split("\\.(?=[^\\.]+$)");
        filebase = filename[0];
        ext = filename[1];
        String keyName = Integer.toString(ssize) + filebase; // helps keep file names unique so we can load same file multiple times

        allData = new XYSeries(keyName);
        allDataError = new XYSeries(keyName);
        double tempQValue;

        try {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            long filesize = file.length();

            if (filesize == 0){
                throw new Exception("File Empty");
            }


            if (ext.equals("dat") || ext.equals("fit") || ext.equals("int")) { //regular 3 column file space or tab delimited
                String strLine;

                //Read file line-by-line
                try {

                    while ((strLine = br.readLine()) != null) {
                        dataPoints = dataFromText(strLine);
                        if (dataPoints.getTest()){

                            // select non-negative values
                            if (!ext.equals("fit")){

                                tempQValue = (convert) ? dataPoints.getq()/10 : dataPoints.getq();

                                allData.add(tempQValue, dataPoints.getI() );
                                allDataError.add(tempQValue, dataPoints.getE() );

                            } else { // if fit file, switch columns

                                tempQValue = (convert) ? dataPoints.getq()/10 : dataPoints.getq();

                                allData.add(dataPoints.getq(), dataPoints.getE() );
                                allDataError.add(dataPoints.getq(), dataPoints.getE()*0.05);
                            }

                        } // move to next line
                    }

                    //endPtNN = originalNNData.getItemCount();
                } catch (IOException ex) {
                    System.out.println("File Index out of bounds");
                }
            } // add new file formats here
            // might have a cansas format, open file and read contents

        } catch (FileNotFoundException ex) {
            jLabel.setText("File is empty");
            System.out.println("Error: " + ex.getMessage());
        }
    }


    private DataLine dataFromText(String line){

        DataLine data;
        String newString;
        String trimmed;
        String[] row;
        newString = line.replaceAll( "[\\s\\t]+", " " );
        trimmed = newString.trim();
        row = trimmed.split("\\s");

        if ( ((row.length >= 2 && row.length <= 4) &&
                !row[0].matches("^[A-Za-z#:_\\/$%*!\\'-].+") &&  // checks for header or footer stuff
                isNumeric(row[0]) &&                            // check that value can be parsed as Double
                isNumeric(row[1]) &&                            // check that value can be parsed as Double
                !(Float.parseFloat(row[0]) <= 0) &&               // no zero q values
                dataFormat.matcher(row[0]).matches() &&         // format must be either scientific with E or decimal
                dataFormat.matcher(row[1]).matches() ))         // format must be either scientific with E or decimal
        {
            data = new DataLine(Double.parseDouble(row[0]), Double.parseDouble(row[1]), 1.0, true);

            if ((row.length == 3 && isNumeric(row[2])) || (row.length == 4 && isNumeric(row[2]))) {
                data.setE(Double.parseDouble(row[2]));
            }
        } else {
            data = new DataLine(0,0,0,false);
        }
        return data;
    }

    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
