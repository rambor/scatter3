package version3.powerlawfits;

import org.jfree.data.xy.XYSeries;
import version3.Dataset;

import java.util.concurrent.Callable;

/**
 * Created by xos81802 on 31/07/2017.
 */
public class CallablePowerLawFit implements Callable<PowerLawFit> {

    private XYSeries data, error;
    private double qmin, qmax;
    int rounds, totalToFitPerRound;

    private Dataset dataset;
    private String outputLine;

    public CallablePowerLawFit(Dataset dataset, double qmin, double qmax, int rounds, int totalToFitPerRound){
        this.dataset = dataset;
        this.data = dataset.getAllData();
        this.error = dataset.getAllDataError();
        this.qmin = qmin;
        this.qmax = qmax;
        this.rounds = rounds;
        this.totalToFitPerRound = totalToFitPerRound;
    }


    @Override
    public PowerLawFit call() throws Exception {
        PowerLawFit temp = new PowerLawFit(data, error, qmin, qmax, rounds, totalToFitPerRound);
        temp.fitData();

        outputLine = String.format("%.6E %.6E %.3f %.3f %.3E %.3E %s %n", qmin, qmax, temp.getSlope(), temp.getErrorSlope(), temp.getIntercept(), temp.getErrorIntercept(), dataset.getFileName());
        temp.setOutputLine(outputLine);
        return temp;
    }
}
