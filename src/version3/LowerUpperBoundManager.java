package version3;

import org.jfree.data.xy.XYSeries;
import version3.powerlawfits.PowerLawFit;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by robertrambo on 17/01/2016.
 */
public class LowerUpperBoundManager {

    private Collection collection;
    private int numberOfCPUs;
    private JProgressBar bar;
    private JLabel status;

    public LowerUpperBoundManager(int numberOfCPUs, Collection collection, JProgressBar bar, JLabel label){
        this.numberOfCPUs = numberOfCPUs;
        this.collection = collection;
        this.bar = bar;
        status = label;
    }

    public void boundNow(AnalysisModel analysisModel, int column, double limit){

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numberOfCPUs);

        List<Future<Bounder>> bounderFutures = new ArrayList<>();
        //List<Runnable<Bounder>> bounderFutures = new ArrayList<>();

        for (int i=0; i < collection.getDatasetCount(); i++) {
            // create
            if (collection.getDataset(i).getInUse()){
//                Runnable bounder = new Bounder(collection.getDataset(i), column, limit);
//                executor.execute(bounder);
                Future<Bounder> future = (Future<Bounder>) executor.submit(new Bounder(collection.getDataset(i), column, limit));
                bounderFutures.add(future);
            }
        }


        int completed = 0;
        for(Future<Bounder> fut : bounderFutures){
            try {
                // because Future.get() waits for task to get completed
                fut.get();
                //update progress bar
                completed++;
                setStatus("Fitting Datasets " + completed );
                //publish(completed);
            } catch (InterruptedException | ExecutionException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }


        executor.shutdown();
//        while (!executor.isTerminated()) {
//
//        }
//
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
//            setStatus("Finished Setting q-limits");
//        } catch (InterruptedException e) {
//            System.out.println("InterruptedException " + e.getMessage());
//            setStatus("Failed settings limits, exceeded thread time");
//        }

//        for (int i=0; i < collection.getDatasetCount(); i++) {
//            //
//            if (collection.getDataset(i).getInUse()){
//                Dataset data = collection.getDataset(i);
//                if (column ==4){
//                    analysisModel.setValueAt(data.getStart(), data.getId(), column);
//                } else if (column ==5){
//                    analysisModel.setValueAt(data.getEnd(), data.getId(), column);
//                }
//            }
//        }

        for (int i=0; i < collection.getDatasetCount(); i++) {
            if (collection.getDataset(i).getInUse()){
                Dataset data = collection.getDataset(i);
                data.setPlottedDataNotify(true);
            }
        }

    }



    public void setStatus(String text){
        status.setText(text);
    }

}

