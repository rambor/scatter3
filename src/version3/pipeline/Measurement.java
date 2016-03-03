package version3.pipeline;

import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYSeries;
import version3.*;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by robertrambo on 16/02/2016.
 */
public class Measurement {

    private boolean convert = false;
    private ArrayList<File> datFiles;
    private Collection collection;
    private double percentMerged;
    private Dataset medianDataset;
    private double percentDifferenceFromFirst;
    private ArrayList<Double> kurtosis;
    private double qmin, qmax;
    private double binsFinal = 31;
    private WorkingDirectory workingDirectory;
    private int referenceStartIndex = 0;
    private int referenceEndIndex = 1000000;
    private Dataset mergedFile;

    /**
     *
     * @param convertToAngstroms true or false (for nm bealines)
     * @param cwd  current working directory
     * @param qmin set to a value to avoid like near beamstop
     * @param qmax set to a value to avoid
     */
    public Measurement(boolean convertToAngstroms, WorkingDirectory cwd, double qmin, double qmax){
        collection = new Collection();
        convert = convertToAngstroms;
        datFiles = new ArrayList<>();
        kurtosis = new ArrayList<>();
        kurtosis.add(0.0d);
        this.workingDirectory = cwd;
        this.qmin = qmin;
        this.qmax = qmax;
    }

    /**
     * Take string encoding filename and absolute path
     * @param filename, name of file to read, must be three columns
     * @return
     */
    public boolean addDataset(String filename){

        int index = datFiles.size();

        try {
            LoadedFile loadedFile = new LoadedFile(datFiles.get(index), index, convert);
            datFiles.add(new File(filename));
            // create dataset
            collection.addDataset(new Dataset(
                    loadedFile.allData,
                    loadedFile.allDataError,
                    loadedFile.filebase, // has to be a unique name
                    index
                    ));

            if (index > 0){

                Similarity simObject = new Similarity();
                simObject.setParametersNoPanel(qmin, qmax, binsFinal, 1);
                simObject.setDirectory(workingDirectory.getWorkingDirectory());
                // calculate kurtosis to reference
                // tracks with datasets in collection (one minus)
                kurtosis.add(simObject.calculateSimFunctionPerSet(
                        collection.getDataset(0),
                        referenceStartIndex,
                        referenceEndIndex,
                        collection.getDataset(index)));

            } else { // set reference
                XYSeries tempData = collection.getDataset(0).getAllData();
                int totalPoints = tempData.getItemCount();
                for (int i=0; i<totalPoints; i++){
                    if (tempData.getX(i).doubleValue() >= qmin){
                        referenceStartIndex = i;
                        break;
                    }
                }

                if (qmax > tempData.getMaxX()){
                    referenceEndIndex = totalPoints;
                } else {
                    for (int i=0; i<totalPoints; i++){
                        if (tempData.getX(i).doubleValue() >= qmax){
                            referenceEndIndex = i;
                            break;
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // method to determine change
    /**
     *
     * @param nameOfmergedFile, name of mergedfile to be written out
     * @param threshold, multiplicative factor for determining cutoff to include datasets in merge, should be greater than 1
     * @return
     */
    public boolean mergeDatasets(String nameOfmergedFile, double threshold){

        // reject data that has kurtosis > threshold
        double mergeCount = 1;
        // set any datasets unacceptable to false in collection
        int totalDatasets = collection.getDatasetCount();
        int firstSets = 4;
        ArrayList<Double> values = new ArrayList<>(3);
        // calculate median
        for (int i=1; i<firstSets; i++){
            values.add(kurtosis.get(i));
        }

        double cutOff = threshold*Statistics.calculateMedian(values);

        for (int i=0; i<totalDatasets; i++){
            if (kurtosis.get(i) <= cutOff){
                collection.getDataset(i).setInUse(true);
                mergeCount++;
            } else {
                collection.getDataset(i).setInUse(false);
            }
        }

        Averager tempAverage = new Averager(collection);
        mergedFile = new Dataset(tempAverage.getAveraged(), tempAverage.getAveragedError(), nameOfmergedFile, collection.getDatasetCount(), false);

        FileObject dataToWrite = new FileObject(new File(workingDirectory.getWorkingDirectory()));
        dataToWrite.writeSAXSFile(cleanUpFileName(nameOfmergedFile), mergedFile);

        percentMerged = mergeCount/(double)totalDatasets;

        return false;
    }


    /**
     * write the datasets that passed the cutoff to file in specified directory
     * @return
     */
    public boolean writeDatasetsToFile(){

        return true;
    }

    public double getPercentMerged(){return percentMerged;}

    private String cleanUpFileName(String fileName){
        String name;
        // remove the dot
        Pattern dot = Pattern.compile(".");
        Matcher expression = dot.matcher(fileName);

        if (expression.find()){
            String[] elements;
            elements = fileName.split("\\.");
            name = elements[0];
        } else {
            name = fileName;
        }

        return name;
    }

}
