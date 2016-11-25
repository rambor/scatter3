package version3.formfactors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by robertrambo on 16/11/2016.
 */
public class CallableEclipse implements Callable<Ellipse> {

    private int index;
    private double solventContrast;
    private double[] particleContrasts = new double[1];
    private double[] params = new double[3];
    private Double[] qvalues;
    private double deltaqr;


    public CallableEclipse(int index, double solventContrast, double[] particleContrasts, double[] radii, Double[] qvalues, double deltaqr){
        this.index = index;
        this.solventContrast = solventContrast;
        this.qvalues = new Double[qvalues.length];

        this.deltaqr=deltaqr;

        synchronized (qvalues){
            System.arraycopy(qvalues, 0, this.qvalues, 0, qvalues.length);
        }

        synchronized (particleContrasts){
            this.particleContrasts[0] = particleContrasts[0];
        }

        synchronized (radii){
            this.params[0] = radii[0];
            this.params[1] = radii[1];
            this.params[2] = radii[2];
        }
    }

    @Override
    public Ellipse call() throws Exception {
        Ellipse ellipse =  new Ellipse(
                index,
                solventContrast,
                particleContrasts,
                params,
                qvalues,
                deltaqr
        );
        //ellipse.printParams();
        return ellipse;
    }
}
