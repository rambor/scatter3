package version3.ceengine.Utils;

import org.jfree.data.xy.XYSeries;

import java.io.*;
import java.util.*;

/**
 * Created by Nathan on 27/07/2015.
 *
 * Class provides some utilities for importing data from .txt files.
 */
public class FileIn {

    private static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
    private static XYSeries data = new XYSeries("Inputted Data");
    private static XYSeries errors = new XYSeries("Inputted Errors");

    /**
     * Imports all parameters from the appropriate file.
     *
     * @param fileName The file from which to import parameters
     */
    public static void importFromFile(String fileName){
        importFromFile(fileName, 0, Double.MAX_VALUE);
    }

    public static void importFromFile(String fileName, double upperLimit){
        importFromFile(fileName, 0, upperLimit);
    }

    public static void importFromFile(String fileName, double lowerLimit, double upperLimit){
        BufferedReader br = null;
        boolean importTest;
        do{
            importTest = false;
            try{
                br = new BufferedReader(new FileReader(fileName));
            }catch(FileNotFoundException e){
                System.out.print("The file cannot be found. Please re-enter: ");
                try{
                    fileName = keyboard.readLine();
                }catch(IOException e2){
                    e2.printStackTrace();
                }
                importTest = true;
            }
        }
        while(importTest);

        Scanner lineScan = new Scanner(br);

        WhileLoop:
        while(lineScan.hasNextLine()){
            Scanner scan = new Scanner(lineScan.nextLine());
            try{
                double q = scan.nextDouble();
                if(q<lowerLimit){
                    continue WhileLoop;
                }
                if(q > upperLimit){
                    break WhileLoop;
                }
                data.add(q, scan.nextDouble());
                try{
                    errors.add(q, scan.nextDouble());
                }catch(NoSuchElementException e){
                    errors.add(q, 0.01);
                }
            }catch(InputMismatchException e){
            }
        }
        lineScan.close();
        try{
            br.close();
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Gets the data that has been imported.
     *
     * @return the data
     * @throws NullPointerException if the data has not been imported properly.
     */
    public static XYSeries getData() throws NullPointerException{
        if(data.isEmpty()){
            throw new NullPointerException();
        }
        return data;
    }

    /**
     * Gets the errors that have been imported.
     *
     * @return the errros
     * @throws NullPointerException if the data has not been imported properly.
     */
    public static XYSeries getErrors() throws NullPointerException{
        if(errors.isEmpty()){
            throw new NullPointerException();
        }
        return errors;
    }
}