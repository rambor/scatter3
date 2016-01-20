package version3;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private AtomicCounter count;

    public ScaleManager(int numberOfCPUs, Collection collection, JProgressBar bar, JLabel label){
        this.numberOfCPUs = numberOfCPUs;
        this.collection = collection;
        this.bar = bar;
        this.bar.setValue(0);
        this.bar.setStringPainted(true);
        // determine reference ID
        // determine number of sets to scale
        totalToScale = collection.getTotalSelected() - 1;
        setReferenceID();

        System.out.println("REFERENCE ID " + reference_ID);
        status = label;

        count = new AtomicCounter(bar, totalToScale);
    }

    /**
     * sets reference to last checked dataset
     */
    private void setReferenceID(){
        int total = collection.getDatasetCount();
        for (int i=0; i<total; i++){
            if (collection.getDataset(i).getInUse()){
                reference_ID = i;
            }
        }
    }

    public void setReference(int ref){
        reference_ID = ref;
    }



    public void scaleNow(double lower, double upper){

        status.setText("Scaling " + totalToScale + " datasets to row : " + (reference_ID + 1) );
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);

        for (int i=0; i < collection.getDatasetCount(); i++) {
            // create
            if (collection.getDataset(i).getInUse() && i != reference_ID){
                //System.out.println("Submitting dataset " + i + " to thread pool");
                Runnable scaler = new Scaler(new Dataset(collection.getDataset(reference_ID)), collection.getDataset(i), lower, upper, i, count);
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
        setStatus("Finished Scaling Selected Datasets");
    }

    class AtomicCounter {
        private AtomicInteger c = new AtomicInteger(0);
        private JProgressBar bar;
        private double invTotal;

        public AtomicCounter(JProgressBar bar, double total){
            this.bar = bar;
            this.invTotal = 1.0/total*100;
        }

        public void increment() {
            c.incrementAndGet();
            bar.setValue((int)(this.value()*invTotal));
        }

        public void decrement() {
            c.decrementAndGet();
        }

        public int value() {
            return c.get();
        }
    }


    public void setStatus(String text){
        status.setText(text);
    }

}
