package version3;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by robertrambo on 15/01/2016.
 */
public class ScaleManager {

    private Collection collection;
    private int numberOfCPUs = 1;
    private int totalToScale;
    private int reference_ID;
    private JProgressBar bar;
    private JLabel status;

    public ScaleManager(int numberOfCPUs, Collection collection, JProgressBar bar, JLabel label){
        this.numberOfCPUs = numberOfCPUs;
        this.collection = collection;
        this.bar = bar;
        // determine reference ID
        // determine number of sets to scale
        totalToScale = collection.getTotalSelected() - 1;
        setReferenceID();
        System.out.println("REFERENCE ID " + reference_ID);

        status = label;
        status.setText("Scaling " + totalToScale + " datasets to row : " + (reference_ID + 1) );
    }

    private void setReferenceID(){
        int total = collection.getDatasetCount();
        for (int i=0; i<total; i++){
            if (collection.getDataset(i).getInUse()){
                reference_ID = i;
            }
        }
    }

    public void scaleNow(double lower, double upper){

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);

        for (int i=0; i < collection.getDatasetCount(); i++) {
            // create
            if (collection.getDataset(i).getInUse() && i != reference_ID){
                System.out.println("Scaling set " + i);
                Runnable scaler = new Scaler(new Dataset(collection.getDataset(reference_ID)), collection.getDataset(i), lower, upper, i);
                executor.execute(scaler);
            }
        }

        executor.shutdown();
        //while (!executor.isTerminated()) {
        //}

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            setStatus("Failed scaling, exceeded thread time");
        }
        setStatus("Finished all Models");
    }

    public void setStatus(String text){
        status.setText(text);
    }

}
