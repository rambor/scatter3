package version3;

import org.jfree.data.statistics.Statistics;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by robertrambo on 18/01/2016.
 */
public class Subtraction extends SwingWorker<String, Object> {

    private final XYSeries medianBuffer;
    private final XYSeries medianBufferError;
    private final XYSeries averageBuffer;
    private final XYSeries averageBufferError;

private int refId, scaleToID;
    private Collection samples;
    private Collection buffers;
    private boolean subtractFromMedianBuffer = false;
    private boolean mergebyAverage = false;
    private boolean mergebyMedian = false;
    private boolean scaleBeforeMerging = false;
    private boolean svd=false;
    private JProgressBar bar;
    private JLabel status;
    private String name;
    private String cwd;
    private int cpus;
    private double cutoff, bins;
    private double sqmin, sqmax;
    private Collection returnCollection;
    private Collection collectionToUpdate;


    public Subtraction(Collection buffers, Collection samples, double tqmin, double tqmax, boolean mergeByAverage, boolean singles, boolean scaleBefore, boolean svd, int cpus, JLabel status, final JProgressBar bar){
        this.samples = samples;
        this.buffers = buffers;

        if (buffers.getDatasetCount() > 1){
            ArrayList<XYSeries> stuff = createMedianAverageXYSeries(buffers);
            medianBuffer = stuff.get(0);
            medianBufferError = stuff.get(1);
            averageBuffer = stuff.get(2);
            averageBufferError = stuff.get(3);
        } else {
            medianBuffer = buffers.getDataset(0).getAllData();
            medianBufferError = buffers.getDataset(0).getAllDataError();
            averageBuffer = buffers.getDataset(0).getAllData();
            averageBufferError = buffers.getDataset(0).getAllDataError();
        }

        this.status = status;
        this.bar = bar;

        if (!singles){
            this.mergebyAverage = mergeByAverage;
            this.mergebyMedian = !mergeByAverage;
        } else {
            this.mergebyAverage = false;
            this.mergebyMedian = false;
        }

        status.setText("Merging => average " + mergebyAverage + " median: " + mergebyMedian);

        this.svd = svd;
        this.scaleBeforeMerging = scaleBefore;
        this.cpus = cpus;

        // used for scaling
        this.sqmin = tqmin;
        this.sqmax = tqmax;
    }

    @Override
    protected String doInBackground() throws Exception {

        returnCollection = new Collection();

        bar.setStringPainted(true);
        bar.setIndeterminate(true);
        bar.setValue(0);
        // create subtracted set of files from average buffer
        Collection subtractedCollection = new Collection();
        int total = samples.getDatasetCount();
        status.setText("Subtracting " + total + " files");

        for (int i = 0; i < total; i++) {
            if (samples.getDataset(i).getInUse()) {

                ArrayList<XYSeries> subtraction = subtract(samples.getDataset(i).getAllData(), samples.getDataset(i).getAllDataError(), averageBuffer, averageBufferError);

                int newIndex = subtractedCollection.getDatasetCount();

                subtractedCollection.addDataset( new Dataset(
                        subtraction.get(0),       //data
                        subtraction.get(1),  //original
                        samples.getDataset(i).getFileName()+"_sub",
                        newIndex, false ));

                subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
                subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

                if (i == refId){
                    scaleToID = subtractedCollection.getDatasetCount()-1; //sets refID relative to the new subtracted collection dataset that will get scaled
                }
            }
            bar.setValue((int) (i / (double) total * 100));
        }

        bar.setIndeterminate(true);

        if (mergebyMedian){
            status.setText("MERGING BY MEDIAN");
            // merge subtracted set by taking median
            Dataset medianSetFromAveragedbuffer = medianSubtractedSets(subtractedCollection, "medianSample_averageBuffer");
            medianSetFromAveragedbuffer.setExperimentalNotes("Subtracted : med(Sample) - ave(buffer)");
            returnCollection.addDataset(medianSetFromAveragedbuffer);
            // subtract from average and (median buffer if count > 2)
            if (buffers.getDatasetCount() > 2){ // do median

                status.setText("Creating median buffer set");
                bar.setIndeterminate(false);
                bar.setValue(0);

                subtractedCollection.removeAllDatasets();

                for (int i = 0; i < total; i++) {

                    if (samples.getDataset(i).getInUse()) {

                        ArrayList<XYSeries> subtraction = subtract(samples.getDataset(i).getAllData(), samples.getDataset(i).getAllDataError(), medianBuffer, medianBufferError);
                        int newIndex = subtractedCollection.getDatasetCount();

                        subtractedCollection.addDataset( new Dataset(
                                subtraction.get(0),       //data
                                subtraction.get(1),  //original
                                samples.getDataset(i).getFileName(),
                                newIndex, false ));

                        subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
                        subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

                        if (i == refId){
                            scaleToID = subtractedCollection.getDatasetCount()-1; //sets refID relative to the new subtracted collection dataset that will get scaled
                        }
                    }
                    bar.setValue((int) (i / (double) total * 100));
                }

                // create median subtracted median
                Dataset medianSetFromMedianbuffer = averagedSubtractedSets(subtractedCollection, "medianSample_medianBuffer");
                medianSetFromMedianbuffer.setExperimentalNotes("Subtracted : med(Sample) - med(buffer)");
                medianSetFromMedianbuffer.setId(returnCollection.getDatasetCount());
                returnCollection.addDataset(medianSetFromMedianbuffer);
            }

        } else if (mergebyAverage) {

            Dataset averagedSetFromAveragedbuffer = averagedSubtractedSets(subtractedCollection, "average_average");
            System.out.println("First -----------------");
            averagedSetFromAveragedbuffer.setExperimentalNotes("Subtracted : ave(Sample) - ave(buffer)");
            returnCollection.addDataset(averagedSetFromAveragedbuffer);
            // subtract from average and (median buffer if count > 2)
            if (buffers.getDatasetCount() > 2){ // do median

                bar.setIndeterminate(false);
                bar.setValue(0);
                subtractedCollection.removeAllDatasets();

                for (int i = 0; i < total; i++) {

                    if (samples.getDataset(i).getInUse()) {

                        ArrayList<XYSeries> subtraction = subtract(samples.getDataset(i).getAllData(), samples.getDataset(i).getAllDataError(), medianBuffer, medianBufferError);
                        int newIndex = subtractedCollection.getDatasetCount();

                        subtractedCollection.addDataset( new Dataset(
                                subtraction.get(0),       //data
                                subtraction.get(1),  //original
                                samples.getDataset(i).getFileName(),
                                newIndex, false ));

                        subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
                        subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

                        if (i == refId){
                            scaleToID = subtractedCollection.getDatasetCount()-1; //sets refID relative to the new subtracted collection dataset that will get scaled
                        }
                    }
                    bar.setValue((int) (i / (double) total * 100));
                }
                // do average - mean set
                Dataset averagedSetFromMedianbuffer = averagedSubtractedSets(subtractedCollection, "average_median");
                averagedSetFromMedianbuffer.setExperimentalNotes("Subtracted : ave(Sample) - med(buffer)");
                averagedSetFromMedianbuffer.setId(returnCollection.getDatasetCount());
                returnCollection.addDataset(averagedSetFromMedianbuffer);
            } // end median buffer subtraction

        } else { // subtract with no merging or scaling
            // only do average buffer?
            //subtractFromMedianSample();
            //subtractFromAveragedBuffer();
            int totalSubtracted = subtractedCollection.getDatasetCount();

            for (int i=0; i<totalSubtracted; i++){
                subtractedCollection.getDataset(i).setExperimentalNotes("");
            }

            writeSinglesToFile(subtractedCollection);
            returnCollection = subtractedCollection;
        }

        this.updateCollection();

        status.setText("Finished");
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setIndeterminate(false);
        return null;
    }

    public void setCollectionToUpdate(Collection collection){
        collectionToUpdate = collection;
    }


    private void updateCollection(){

        int total = returnCollection.getDatasetCount();
        int newIndex = collectionToUpdate.getDatasetCount();
        for (int i=0; i<total; i++){
            Dataset temp = returnCollection.getDataset(i);

            collectionToUpdate.addDataset( new Dataset(
                    temp.getAllData(),       //data
                    temp.getAllDataError(),  //original
                    temp.getFileName(),
                    newIndex, false ));

            newIndex++;
        }
    }

    public void setNameAndDirectory(String baseName, String workingDirectoryName){
        this.name = baseName;
        this.cwd = workingDirectoryName;
    }

    /**
     * No Scaling required use the datasets as is
     * 1. Create average and median representation of sample Collection
     * 2. Subtract from average and median representation of buffer
     * @return
     */
    private Collection subtractFromMedianSample(){

        ArrayList<XYSeries> sampleSeries = createMedianAverageXYSeries(samples);
        XYSeries sampleXY = sampleSeries.get(0);    //median
        XYSeries sampleError = sampleSeries.get(1); //median error

        Collection subtractedCollection = new Collection();
        // do average buffer first
        ArrayList<XYSeries> subtraction = subtract(sampleXY, sampleError, averageBuffer, averageBufferError);

        //double[] izeroRg = Functions.calculateIzeroRg(subtraction.get(0), subtraction.get(1));
        subtractedCollection.addDataset( new Dataset(
                subtraction.get(0),       //data
                subtraction.get(1),
                name+"_from_averaged",
                0, false ));

        subtractedCollection.getLast().setExperimentalNotes("Subtracted from an averaged buffer set");
        subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
        subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

        FileObject dataToWrite = new FileObject(new File(cwd));
        dataToWrite.writeSAXSFile("med_"+name+"_from_averaged_buffer", subtractedCollection.getDataset(0));

        // subtract from averaged samples
        subtraction = subtract(sampleXY, sampleError, medianBuffer, medianBufferError);
        subtractedCollection.addDataset( new Dataset(
                subtraction.get(0),       //data
                subtraction.get(1),       //error
                name+"_from_median",
                1, false ));
        subtractedCollection.getLast().setExperimentalNotes("Subtracted from the median buffer set");
        subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
        subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

        dataToWrite.writeSAXSFile("med_"+name+"_from_median_buffer", subtractedCollection.getDataset(1));

        return subtractedCollection;
    }

    private Collection subtractFromAveragedBuffer(){

        ArrayList<XYSeries> sampleSeries = createMedianAverageXYSeries(samples);
        XYSeries sampleXY = sampleSeries.get(2);    //averaged
        XYSeries sampleError = sampleSeries.get(3); //averaged error

        Collection subtractedCollection = new Collection();
        // do average buffer first
        ArrayList<XYSeries> subtraction = subtract(sampleXY, sampleError, averageBuffer, averageBufferError);

        //double[] izeroRg = Functions.calculateIzeroRg(subtraction.get(0), subtraction.get(1));
        subtractedCollection.addDataset( new Dataset(
                subtraction.get(0),       //data
                subtraction.get(1),
                name+"_from_averaged",
                0, false ));

        subtractedCollection.getLast().setExperimentalNotes("Subtracted from an averaged buffer set");
        subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
        subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

        FileObject dataToWrite = new FileObject(new File(cwd));
        dataToWrite.writeSAXSFile("ave_" + name+"_from_averaged_buffer", subtractedCollection.getDataset(0));

        // subtract from averaged samples
        subtraction = subtract(sampleXY, sampleError, medianBuffer, medianBufferError);
        subtractedCollection.addDataset( new Dataset(
                subtraction.get(0),       //data
                subtraction.get(1),       //error
                name+"_from_median",
                1, false ));
        subtractedCollection.getLast().setExperimentalNotes("Subtracted from the median buffer set");
        subtractedCollection.getLast().setMinq(subtraction.get(0).getMinX());
        subtractedCollection.getLast().setMaxq(subtraction.get(0).getMaxX());

        dataToWrite.writeSAXSFile("ave_" + name+"_from_median_buffer", subtractedCollection.getDataset(1));

        return subtractedCollection;
    }

    /**
     * refID from the JComboBox
     * @param refID
     */
    public void setRefID(int refID){ this.refId = refID;}

    public void setBinsAndCutoff(double bins, double cutoff){
        this.cutoff = cutoff;
        this.bins = bins;
    }


    public Dataset medianSubtractedSets(Collection subtractedCollection, String type){

        if (scaleBeforeMerging) {
            status.setText("scaling sets");
            //scaleSets(subtractedCollection, scaleToID);
            ScaleManager scaler = new ScaleManager(cpus, subtractedCollection, bar, status);
            scaler.setReference(scaleToID);
            // launch scaler
            scaler.scaleNow(sqmin, sqmax);
        }

        status.setText("Merging sets");

        ArrayList<XYSeries> finalSets;
        String output_name;

        // createMedian
        ArrayList<XYSeries> sampleSeries = createMedianAverageXYSeries(subtractedCollection);
        //createMedianAverageXYSeries(samples);
        XYSeries sampleXY = sampleSeries.get(0);    //median
        XYSeries sampleError = sampleSeries.get(1); //median error

        sampleXY.setKey(type);
        sampleError.setKey(type);
        // merge curves using rejection criteria
        output_name = name + "_" + type;

        Dataset tempSingle = new Dataset(
                sampleXY,       //data
                sampleError,  //original
                output_name,
                0, false );

        FileObject dataToWrite = new FileObject(new File(cwd));
        dataToWrite.writeSAXSFile(output_name, tempSingle);

        return tempSingle;
    }


    public Dataset averagedSubtractedSets(Collection subtractedCollection, String type){

        if (scaleBeforeMerging) {
            status.setText("scaling sets");
            //scaleSets(subtractedCollection, scaleToID);
            ScaleManager scaler = new ScaleManager(cpus, subtractedCollection, bar, status);
            scaler.setReference(scaleToID);
            // launch scaler
            scaler.scaleNow(sqmin, sqmax);
        }

        status.setText("Merging sets");

        ArrayList<XYSeries> finalSets;
        String output_name;
        if (svd){
            XYSeriesCollection svdCollection = Functions.svdNoiseReduce(subtractedCollection);
            output_name = name + "_"+ type + "_svd";
            finalSets = new ArrayList<>(2);
            svdCollection.getSeries(0).setKey(output_name);
            svdCollection.getSeries(1).setKey(output_name + "_error");
            finalSets.add(svdCollection.getSeries(0));
            finalSets.add(svdCollection.getSeries(1));
        } else {
            finalSets = createRejectionDataSets(subtractedCollection);
            output_name = name + "_" + type;
        }

        // merge curves using rejection criteria
        // finalSets = createRejectionDataSets(sampleFilesModel, subtractedCollection);
        // write out merged curve
        //Collection tempCollection = new Collection();
        //finalSets.get(0).setKey(type);
        //finalSets.get(1).setKey(type);

        Dataset tempSingle = new Dataset(
                finalSets.get(0),  //data
                finalSets.get(1),  //original
                output_name,
                0, false );

        FileObject dataToWrite = new FileObject(new File(cwd));
        dataToWrite.writeSAXSFile(output_name, tempSingle);

        /*
        tempCollection.addDataset(tempSingle);
        tempCollection.recalculateMinMaxQ();
        tempCollection.getDataset(0).setMaxq(finalSets.get(0).getMaxX());
        tempCollection.getDataset(0).setMinq(finalSets.get(0).getMinX());
        */

        return tempSingle;
    }

    private void writeSinglesToFile(Collection subtractedCollection){
        int total = subtractedCollection.getDatasetCount();

        status.setText("writing each dataset to file");
        FileObject dataToWrite = new FileObject(new File(cwd));

        for (int i = 0; i < total; i++) {
            dataToWrite.writeSAXSFile(subtractedCollection.getDataset(i).getFileName(), subtractedCollection.getDataset(i));
        }
    }


    private ArrayList<XYSeries> subtract(XYSeries sample, XYSeries sampleError, XYSeries buffer, XYSeries bufferError){
        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();
        // for each element in sample collection, do subtraction

        XYSeries subData;
        XYSeries subError;
        XYDataItem tempDataItem;

        int tempTotal, indexOf;
        double qValue, yValue, eValue;

        tempTotal = sample.getItemCount();


        subData = new XYSeries("subtracted");
        subError = new XYSeries("errorSubtracted");
        //Subtract and add to new data

        QLOOP:
        for(int q=0; q<tempTotal; q++){
            tempDataItem = sample.getDataItem(q);
            qValue = tempDataItem.getXValue();
                     /*
                      * check to see if in buffer
                      */
            indexOf = buffer.indexOf(qValue);
            yValue = sampleError.getY(q).doubleValue();

            if (indexOf > -1){
                subData.add(qValue, tempDataItem.getYValue() - buffer.getY(indexOf).doubleValue() );

                eValue = bufferError.getY(indexOf).doubleValue();
                subError.add(qValue, Math.sqrt(yValue*yValue + eValue*eValue));

            } else { // interpolate
                // interpolation requires at least two values on left or right side of value of interest
                // if not, skip value
//                    count = 0;
//                    referenceQ = buffer.getX(count).doubleValue();
//                    find first value in reference greater than targetData.getX
//                    while (referenceQ < qValue) {
//                        count++;
//                        referenceQ = buffer.getX(count).doubleValue();
//                    }
//
//                    if (count < 2) {
//                       break QLOOP;
//                    }
                System.out.println("Interpolating Value at " + qValue);
                Double[] results = Functions.interpolate(buffer, bufferError, qValue, 1);
                Double[] sigmaResults = Functions.interpolateSigma(bufferError, qValue);
                //returns unlogged data
                eValue = sigmaResults[1];

                subData.add(qValue, results[1]);
                subError.add(qValue, Math.sqrt(yValue*yValue + eValue*eValue));
            }
        }

        returnMe.add(subData);
        returnMe.add(subError);

        return returnMe;
    }

    // Buffer : subtract from average, median or single

    //

    private ArrayList<XYSeries> createMedianAverageXYSeries(Collection collection){
        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();

        // calculate Average and Median for set

        ArrayList<XYSeries> median_reduced_set = StatMethods.medianDatasets(collection);
        ArrayList<XYSeries> averaged = StatMethods.weightedAverageDatasets(collection);

        String name = "median_set";

        XYSeries medianAllData = null;
        XYSeries medianAllDataError = null;

        try {
            medianAllData = (XYSeries) median_reduced_set.get(0).clone();
            medianAllData.setKey(name);
            medianAllDataError = (XYSeries) median_reduced_set.get(1).clone();
            medianAllDataError.setKey(name);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        returnMe.add(medianAllData);          // 0
        returnMe.add(medianAllDataError);     // 1
        returnMe.add(averaged.get(0));        // 2
        returnMe.add(averaged.get(1));        // 3

        return returnMe;
    }


    private ArrayList<XYSeries> createRejectionDataSets(Collection thisCollection){

        ArrayList<XYSeries> returnMe = new ArrayList<XYSeries>();
        //int total = model.getSize();
        int total = thisCollection.getDatasets().size();
        int sizeOf = 0;

        double binPercent = bins/100.00;

        int totalObs;
        Dataset tempData;
        double xValue, yValue, median, s_n, value;
        XYDataItem tempDataItem;

        ArrayList<StandardObservations> standardObs = new ArrayList<>();
        StandardObservations tempSobs;

        ArrayList<Double> vector = new ArrayList<Double>();

        int totalObservations = 0;
        for(int i=0;i<total; i++){
            tempData =thisCollection.getDataset(i);

            if (tempData.getInUse()){
                sizeOf = tempData.getAllData().getItemCount();
                if (totalObservations < sizeOf){
                    totalObservations = sizeOf;
                }
            }
        }

        int bbins = (int)(binPercent*sizeOf);
        double qmin = thisCollection.getMinq();
        double qmax = thisCollection.getMaxq();

        double lower, upper, sigma;
        double incr = (qmax-qmin)/(double)bbins;

        HashMap<Double, Double> finalData = new HashMap<Double, Double>();
        HashMap<Double, Double> finalError = new HashMap<Double, Double>();

        // iterate over each bin
        lower = qmin;

        for (int b = 1; b <= bbins; b++){

            upper = qmin + b*incr;

            standardObs.clear();
            vector.clear();

            dataSetLoop:
            for(int i=0;i<total; i++){
                tempData =thisCollection.getDataset(i);

                if (tempData.getInUse()){
                    sizeOf = tempData.getAllData().getItemCount();
                    double tempScaleFactor = tempData.getScaleFactor();

                    for(int j=0; j<sizeOf; j++){
                        tempDataItem = tempData.getAllData().getDataItem(j);
                        xValue = tempDataItem.getXValue();
                        if ((xValue >= lower) && (xValue < upper)){
                            sigma = tempData.getAllDataError().getY(j).doubleValue()*tempScaleFactor;
                            yValue = tempDataItem.getYValue()*tempScaleFactor;
                            standardObs.add(new StandardObservations(xValue, yValue, sigma));
                            vector.add(yValue);
                        } else if (xValue > qmax){
                            break dataSetLoop;
                        }
                    }

                }

            } // end of datasetloop
            // calculate median for standardObs
            totalObs = standardObs.size();
            median = Statistics.calculateMedian(vector, true);

            s_n = 1.0/(1.1926*Functions.s_n(vector));

            for (int i=0; i<totalObs; i++){
                standardObs.get(i).setStandardizedObs(s_n, median);
            }

            // reject all data with standardizedObs > 2.5
            Iterator iter = standardObs.iterator();
            int count=0;
            while (iter.hasNext()){
                tempSobs = (StandardObservations) iter.next();
                if (tempSobs.getStandardizedObs() > cutoff) {
                    iter.remove();
                    count++;
                }
            }

            // collect and average all common q values
            iter = standardObs.iterator();
            double var, sig;

            while (iter.hasNext()){
                tempSobs = (StandardObservations) iter.next();
                xValue = tempSobs.getQ();
                sig = 1.0/tempSobs.getSigma();
                var = sig*sig;

                if (finalData.containsKey(xValue)){
                    value = (finalData.get(xValue) + tempSobs.getObs()*var );

                    finalData.put(xValue, value);
                    finalError.put(xValue, (finalError.get(xValue) + var));
                } else {

                    finalData.put(xValue, tempSobs.getObs()*var);
                    finalError.put(xValue, var);
                }
            }

            lower = upper;
        }

        XYSeries finalSeries = new XYSeries("Pruned");
        XYSeries errorSeries = new XYSeries("PrunedError");

        Iterator it = finalData.entrySet().iterator();
        double weighted;
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();

            weighted = (Double)pairs.getValue()/finalError.get(pairs.getKey());

            finalSeries.add((Double)pairs.getKey(), (Double)weighted );
            it.remove(); // avoids a ConcurrentModificationException
        }

        it = finalError.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            weighted = 1.0 / Math.sqrt((Double)pairs.getValue());

            errorSeries.add((Double)pairs.getKey(), (Double)weighted);
            it.remove(); // avoids a ConcurrentModificationException
        }

        // return finalSeries and errorSeries
        returnMe.add(finalSeries);
        returnMe.add(errorSeries);

        return returnMe;
    }


}
