package version3.pipeline;

import org.jfree.data.xy.XYSeries;
import version3.*;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

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

    /**
     *
     * @param convertToAngstroms true or false
     * @param cwd  current working directory
     * @param qmin set to a value to avoid beamstop noise
     * @param qmax
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
     * @param filename
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

    public boolean mergeDatasets(String nameOfmergedFile){

        // reject data that has kurtosis > threshold

        // set any datasets unacceptable to false in collection




     return false;
    }


}
