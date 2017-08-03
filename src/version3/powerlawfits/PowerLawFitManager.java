package version3.powerlawfits;

import version3.Collection;
import version3.Dataset;
import version3.WorkingDirectory;
import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Robert Rambo on 31/07/2017.
 */
public class PowerLawFitManager extends SwingWorker<Void, Void> {

    private Collection collectionInUse;
    private double qmin, qmax;
    private int rounds, totalToFitPerRound;
    private JProgressBar progressBar;
    private JLabel label;
    private int cpus;
    private int totalInUse;

    private WorkingDirectory workingDirectory;


    public PowerLawFitManager(Collection collectionInUse,
                              double qmin,
                              double qmax,
                              int rounds,
                              int totalToFitPerRound,
                              int cpus,
                              JProgressBar bar,
                              JLabel label,
                              WorkingDirectory workdir
    ){

        this.collectionInUse = collectionInUse;
        this.qmin =qmin;
        this.qmax =qmax;
        this.rounds = rounds;
        this.totalToFitPerRound = totalToFitPerRound;
        this.cpus = cpus;
        this.progressBar = bar;
        this.label = label;
        this.workingDirectory = workdir;

        totalInUse = 0;
        for (int i=0; i<collectionInUse.getDatasetCount(); i++){
            if(collectionInUse.getDataset(i).getInUse()){
                totalInUse++;
            }
        }
    }


    @Override
    protected Void doInBackground() throws Exception {
        // need to determine reference state, set scale to 1 and then
        // reference is large Intensity between 0.017 and 0.1
        ArrayList<PowerLawFit> powerLawModels = new ArrayList<>();
        int totalDatasetsInCollection = collectionInUse.getDatasetCount();

        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setMaximum(totalDatasetsInCollection);
        // calculate scale factors for each dataset in use
        ScheduledExecutorService powerLawExecutor = Executors.newScheduledThreadPool(cpus);
        List<Future<PowerLawFit>> powerLawFutures = new ArrayList<>();
        label.setText("Building Threads ");

        for(int i=0; i<totalDatasetsInCollection; i++){
            if (collectionInUse.getDataset(i).getInUse()){

                Dataset target = collectionInUse.getDataset(i);
                Future<PowerLawFit> future = powerLawExecutor.submit(new CallablePowerLawFit(
                        target,
                        qmin,
                        qmax,
                        rounds,
                        totalToFitPerRound
                ));

                powerLawFutures.add(future);
            }
            progressBar.setValue(i);
        }

        progressBar.setValue(0);
        progressBar.setMaximum(totalInUse);

        int completed = 0;
        for(Future<PowerLawFit> fut : powerLawFutures){
            try {
                // because Future.get() waits for task to get completed
                powerLawModels.add(fut.get());
                //update progress bar
                completed++;
                progressBar.setValue(completed);
                label.setText("Fitting Datasets ");
                //publish(completed);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        powerLawExecutor.shutdown();

        label.setText("Writing results to file : powerLawFits.txt "  );
        // write parameters to file
        FileWriter fw = new FileWriter(workingDirectory.getWorkingDirectory() +"/powerLawFits.txt");
        BufferedWriter out = new BufferedWriter(fw);
        out.write(String.format("REMARK COLUMNS: qmin qmax slope error intercept error filename %n"));

        for (int n=0; n < powerLawModels.size(); n++) {
            out.write(powerLawModels.get(n).getOutputLine());
        }
        out.close();

        label.setText("Check Directory => " + workingDirectory.getWorkingDirectory());
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        return null;
    }
}
